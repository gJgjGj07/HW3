package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestionHandler {
    // Database connection used for executing SQL queries.
    private Connection connection;
    
    // Predefined patterns used to detect potential SQL injection attempts.
    private static final String[] SQL_INJECTION_PATTERNS = {
        "(?i)\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b",
        "--", 
        ";", 
        "/\\*", 
        "\\*/"
    };

    /**
     * Constructs a QuestionHandler with a given database connection.
     *
     * @param connection The database connection.
     * @throws SQLException if an error occurs while creating the tables.
     */
    public QuestionHandler(Connection connection) throws SQLException {
        this.connection = connection;
        createTables();
    }

    /**
     * Creates the Posts table if it does not already exist.
     *
     * @throws SQLException if an error occurs while executing the SQL query.
     */
    private void createTables() throws SQLException {
        String postTable = "CREATE TABLE IF NOT EXISTS Posts (" 
                + "postId INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255), "
                + "title VARCHAR(255), "  
                + "numReplies INT DEFAULT 0, "
                + "post VARCHAR(10000))";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(postTable);
        }
    }

    /**
     * Inserts a new post into the Posts table. This method sanitizes the title and post content
     * if potential SQL injection patterns are detected.
     *
     * @param userName The username of the user submitting the post.
     * @param title    The title of the post.
     * @param post     The content of the post.
     */
    public void addPost(String userName, String title, String post) {  
        // Initialize clean copies of title and post.
        String cleanTitle = title;
        String cleanPost = post;
        
        // Sanitize title if SQL injection is detected.
        if (detectSQLInjection(title)){
            cleanTitle = sanitizeInput(title);
        }
        // Sanitize post content if SQL injection is detected.
        if (detectSQLInjection(post)){
            cleanPost = sanitizeInput(post);
        }
        
        // SQL query to insert a new post.
        String insertQuery = "INSERT INTO Posts (userName, title, post) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, cleanTitle);
            pstmt.setString(3, cleanPost);
            pstmt.executeUpdate();
            
            // Retrieve the generated post ID and log it.
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int postId = rs.getInt(1);
                System.out.println("Post added successfully with ID: " + postId);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting post: " + e.getMessage());
        }
    }

    /**
     * Retrieves all posts from the Posts table.
     *
     * @return A ResultSet containing all posts.
     * @throws SQLException if an error occurs during the query.
     */
    public ResultSet getAllQuestions() throws SQLException {
        String query = "SELECT * FROM Posts";
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    /**
     * Retrieves the number of replies for a specific post.
     *
     * @param postId The ID of the post.
     * @return The number of replies for the given post, or 0 if not found.
     * @throws SQLException if an error occurs during the query.
     */
    public int getNumReplies(int postId) throws SQLException {
        String query = "SELECT numReplies FROM Posts WHERE postId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("numReplies");
            }
        }
        return 0;
    }

    /**
     * Deletes a post from the Posts table based on its ID.
     *
     * @param postId The ID of the post to be deleted.
     * @return true if the deletion was successful; false otherwise.
     */
    public boolean deletePostById(int postId) {
        String sql = "DELETE FROM Posts WHERE postId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increments the number of replies for a specific post.
     *
     * @param postId The ID of the post.
     * @return true if at least one row was updated; false otherwise.
     */
    public boolean incrementNumReplies(int postId) {
        String query = "UPDATE Posts SET numReplies = numReplies + 1 WHERE postId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Returns true if at least one row was updated.
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Decrements the number of replies for a specific post.
     *
     * @param postId The ID of the post.
     * @return true if at least one row was updated; false otherwise.
     */
    public boolean decrementNumReplies(int postId) {
        String query = "UPDATE Posts SET numReplies = numReplies - 1 WHERE postId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Returns true if at least one row was updated.
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all posts that contain a keyword in their title or text.
     *
     * @return A ResultSet containing posts that contain the keyword.
     * @throws SQLException if an error occurs during the query.
     */
    public ResultSet searchPostsByKeyword(String keyword) throws SQLException {
        // SQL query using LOWER() for a case-insensitive match on title and post columns.
        String query = "SELECT * FROM Posts WHERE LOWER(title) LIKE ? OR LOWER(post) LIKE ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        
        // Convert the keyword to lowercase and wrap with wildcards.
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        pstmt.setString(1, searchPattern);
        pstmt.setString(2, searchPattern);
        
        return pstmt.executeQuery();
    }
    
    /**
     * Retrieves all posts that have at least one reply.
     *
     * @return A ResultSet containing posts that have been answered.
     * @throws SQLException if an error occurs during the query.
     */
    public ResultSet getAnsweredQuestions() throws SQLException {
        String query = "SELECT * FROM Posts WHERE numReplies > 0";
        PreparedStatement statement = connection.prepareStatement(query);
        return statement.executeQuery();
    }

    /**
     * Retrieves posts that have been read by the user.
     *
     * @param readPostIds A list of post IDs that have been read.
     * @return A ResultSet containing posts that have been read.
     * @throws SQLException if an error occurs during the query.
     */
    public ResultSet getReadQuestions(List<Integer> readPostIds) throws SQLException {
        // If no posts have been read, return an empty result set.
        if (readPostIds.isEmpty()) return connection.createStatement().executeQuery("SELECT * FROM Posts WHERE 1=0");
        
        // Convert list of IDs into a comma-separated string.
        String ids = readPostIds.stream()
                              .map(String::valueOf)
                              .collect(Collectors.joining(","));
        
        return connection.createStatement().executeQuery(
            "SELECT * FROM Posts WHERE postId IN (" + ids + ")"
        );
    }

    /**
     * Retrieves posts that have not been read by the user.
     *
     * @param readPostIds A list of post IDs that have been read.
     * @return A ResultSet containing posts that have not been read.
     * @throws SQLException if an error occurs during the query.
     */
    public ResultSet getUnreadQuestions(List<Integer> readPostIds) throws SQLException {
        // If no posts have been read, return all posts.
        if (readPostIds.isEmpty()) return getAllQuestions();
        
        // Convert list of IDs into a comma-separated string.
        String ids = readPostIds.stream()
                              .map(String::valueOf)
                              .collect(Collectors.joining(","));
        
        return connection.createStatement().executeQuery(
            "SELECT * FROM Posts WHERE postId NOT IN (" + ids + ")"
        );
    }

    /**
     * Retrieves the content of a specific post.
     *
     * @param postId The ID of the post.
     * @return The content of the post, or null if not found.
     * @throws SQLException if an error occurs during the query.
     */
    public String getPostContentById(int postId) throws SQLException {
        String query = "SELECT post FROM Posts WHERE postId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("post");
            }
        }
        return null;
    }

    /**
     * Updates an existing post with a new title and content.
     *
     * @param postId     The ID of the post to update.
     * @param newTitle   The new title for the post.
     * @param newContent The new content for the post.
     * @return true if the update was successful; false otherwise.
     */
    public boolean updatePost(int postId, String newTitle, String newContent) {  
        String query = "UPDATE Posts SET title = ?, post = ? WHERE postId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newContent);
            pstmt.setInt(3, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the title of a specific post.
     *
     * @param postId The ID of the post.
     * @return The title of the post, or an empty string if not found.
     * @throws SQLException if an error occurs during the query.
     */
    public String getPostTitleById(int postId) throws SQLException {
        String query = "SELECT title FROM Posts WHERE postId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("title") : "";
        }
    }

    /**
     * Validates a question's title and content.
     * Checks for potential SQL injection attempts and enforces constraints on title length
     * and question length.
     *
     * @param title    The title of the question.
     * @param question The content of the question.
     * @return A list of error messages. The list will be empty if the inputs are valid.
     */
    public List<String> validateQuestion(String title, String question) {
        List<String> errors = new ArrayList<>();

        // Check for SQL injection attempts in the title and question.
        if (detectSQLInjection(title)) {
            errors.add("Title contains suspicious content");
        }
        if (detectSQLInjection(question)) {
            errors.add("Question contains suspicious content");
        }

        // Enforce maximum title length.
        if (title.length() > 100) {
            errors.add("Title cannot exceed 100 characters");
        }
        // Ensure the question is not blank and meets the minimum length requirement.
        if (question.isEmpty()) {
            errors.add("Question cannot be blank");
        } else {
            if (question.trim().length() < 5) {
                errors.add("Question must be at least 5 characters");
            }
        }

        return errors;
    }

    /**
     * Checks if the input string contains potential SQL injection content.
     *
     * @param input The string to check.
     * @return true if suspicious content is detected; false otherwise.
     */
    public boolean detectSQLInjection(String input) {
        if (input == null || input.isEmpty()) return false;
        
        // Sanitize the input and convert to lowercase for case-insensitive matching.
        String cleaned = sanitizeInput(input).toLowerCase();
        
        // Iterate through each SQL injection pattern.
        for (String pattern : SQL_INJECTION_PATTERNS) {
            if (cleaned.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitizes the input by removing suspicious characters and SQL keywords.
     *
     * @param input The string to sanitize.
     * @return The sanitized string.
     */
    public String sanitizeInput(String input) {
        if (input == null) return "";
        
        // Remove dangerous characters and SQL keywords to prevent injection.
        return input.replaceAll("[\"'#;()*/]", "")
                    .replaceAll("\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b", "");
    }
}
