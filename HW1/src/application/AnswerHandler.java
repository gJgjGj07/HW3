package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code AnswerHandler} class manages database interactions related to replies/answers.
 * It performs tasks such as validating answers, inserting new replies, updating and deleting replies,
 * handling likes, and retrieving nested replies.
 */
public class AnswerHandler {
    private Connection connection; // Database connection used for executing SQL queries.
    private QuestionHandler qHandler; // Reference to a QuestionHandler for inter-related operations.

    // Predefined patterns to detect potential SQL injection attempts.
    private static final String[] SQL_INJECTION_PATTERNS = {
        "(?i)\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b",
        "--", 
        ";", 
        "/\\*", 
        "\\*/"
    };

    /**
     * Constructs an {@code AnswerHandler} with the given database connection and question handler.
     *
     * @param connection The database connection.
     * @param qHandler   A reference to the QuestionHandler.
     * @throws SQLException If an SQL error occurs during table creation.
     */
    public AnswerHandler(Connection connection, QuestionHandler qHandler) throws SQLException {
        this.connection = connection;
        this.qHandler = qHandler;
        createTables();
    }

    /**
     * Creates the Replies table if it does not exist.
     *
     * @throws SQLException If an SQL error occurs.
     */
    private void createTables() throws SQLException {
        String repliesTable = "CREATE TABLE IF NOT EXISTS Replies ("
                + "replyId INT AUTO_INCREMENT PRIMARY KEY, "
                + "reply VARCHAR(10000), "
                + "userName VARCHAR(255), "
                + "likes INT DEFAULT 0, "
                + "isPrivate BOOLEAN DEFAULT FALSE, " 
                + "parentReplyId INT DEFAULT NULL, "
                + "likeList VARCHAR(10000), "
                + "numReplies INT DEFAULT 0, "
                + "postId INT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(repliesTable);
        }
    }

    /**
     * Validates the provided answer by checking if it is empty, too short, or contains suspicious content.
     *
     * @param answer The answer string to validate.
     * @return A list of error messages. The list will be empty if the answer is valid.
     */
    public List<String> validateAnswer(String answer) {
        List<String> errors = new ArrayList<>();
        if (answer == null || answer.trim().isEmpty()) {
            errors.add("Answer cannot be empty");
            return errors;
        }
        String cleanAnswer = answer.trim();
        int length = cleanAnswer.length();
        if (length < 5) {
            errors.add("Answer must be at least 5 characters");
        }
        if (detectSQLInjection(cleanAnswer)) {
            errors.add("Answer contains suspicious content");
        }
        return errors;
    }

    /**
     * Checks the input string for patterns that may indicate an SQL injection attempt.
     *
     * @param input The input string to check.
     * @return {@code true} if any SQL injection pattern is detected, otherwise {@code false}.
     */
    public boolean detectSQLInjection(String input) {
        if (input == null || input.isEmpty()) return false;
        String cleaned = sanitizeInput(input).toLowerCase();
        for (String pattern : SQL_INJECTION_PATTERNS) {
            if (cleaned.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitizes the input string by removing potentially dangerous characters and SQL keywords.
     *
     * @param input The input string to sanitize.
     * @return A sanitized version of the input string.
     */
    public String sanitizeInput(String input) {
        if (input == null) return "";
        String sanitized = input.replaceAll("[\"'#;()*/]", "");
        sanitized = sanitized.replaceAll("\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b", "");
        sanitized = sanitized.replaceAll("(?i)(;\\s*--|UNION\\s*--)", ";"); 
        return sanitized;
    }

    /**
     * Inserts a new answer (reply) into the Replies table.
     *
     * @param answer    The reply text.
     * @param postId    The ID of the post this reply is associated with.
     * @param userName  The username of the person posting the reply.
     * @param isPrivate Whether the reply is private.
     */
    public void addAnswer(String answer, int postId, String userName, boolean isPrivate) {
        String cleanAnswer = answer;
        if (detectSQLInjection(answer)) {
            cleanAnswer = sanitizeInput(answer);
        }
        String insertQuery = "INSERT INTO Replies (reply, postId, userName, isPrivate) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, cleanAnswer);
            pstmt.setInt(2, postId);
            pstmt.setString(3, userName);
            pstmt.setBoolean(4, isPrivate);
            pstmt.executeUpdate();
            qHandler.incrementNumReplies(postId);
        } catch (SQLException e) {
            System.err.println("Error inserting reply: " + e.getMessage());
        }
    }

    /**
     * Retrieves all replies associated with a specific post ID.
     *
     * @param postId      The ID of the post whose replies are to be retrieved.
     * @param currentUser The username of the current user.
     * @return A {@code ResultSet} containing the replies, or {@code null} if an error occurs.
     */
    public ResultSet getRepliesByPostId(int postId, String currentUser) {
        String query = "SELECT R.* FROM Replies R "
            + "LEFT JOIN Posts P ON R.postId = P.postId "
            + "WHERE R.postId = ? AND R.parentReplyId IS NULL AND ("
            + "  R.isPrivate = FALSE OR "
            + "  R.userName = ? OR "
            + "  P.userName = ?"
            + ") "
            + "ORDER BY R.replyId ASC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, postId);
            pstmt.setString(2, currentUser);
            pstmt.setString(3, currentUser);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching replies: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes all replies associated with a specific post ID.
     *
     * @param postId The ID of the post.
     * @return {@code true} if one or more rows were deleted, {@code false} otherwise.
     */
    public boolean deleteReplyByPostId(int postId) {
        String sql = "DELETE FROM Replies WHERE postId = ?";
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
     * Deletes a reply based on its reply ID.
     *
     * @param replyId The ID of the reply to be deleted.
     */
    public void deleteReplyById(int replyId) {
        String query = "DELETE FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reply deleted successfully.");
            } else {
                System.out.println("No reply found with ID: " + replyId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the content of a reply by its ID.
     *
     * @param replyId The ID of the reply.
     * @return The reply content, or {@code null} if not found.
     * @throws SQLException If an error occurs during query execution.
     */
    public String getReplyContentById(int replyId) throws SQLException {
        String query = "SELECT reply FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("reply");
            }
        }
        return null;
    }

    /**
     * Updates the content of an existing reply.
     *
     * @param replyId    The ID of the reply to update.
     * @param newContent The new reply content.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean updateReply(int replyId, String newContent) {
        String query = "UPDATE Replies SET reply = ? WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newContent);
            pstmt.setInt(2, replyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increments the like count for a specific reply.
     *
     * @param replyId The ID of the reply.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean incrementLikes(int replyId) {
        String sql = "UPDATE Replies SET likes = COALESCE(likes, 0) + 1 WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, replyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the number of likes for a specific reply.
     *
     * @param replyId The ID of the reply.
     * @return The number of likes, or -1 if an error occurs.
     */
    public int getNumLikes(int replyId) {
        String sql = "SELECT COALESCE(likes, 0) AS likes FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, replyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("likes");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves the postId associated with a given reply.
     *
     * @param replyId The ID of the reply.
     * @return The postId if found, or -1 otherwise.
     */
    public int getPostIdByReplyId(int replyId) {
        String query = "SELECT postId FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("postId");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Inserts a new reply that is a response to an existing reply (nested reply).
     * Also increments the {@code numReplies} count for the parent reply.
     *
     * @param parentReplyId The ID of the parent reply.
     * @param reply         The content of the new reply.
     * @param userName      The username of the poster.
     * @param isPrivate     Whether the reply is private.
     * @return {@code true} if the insertion and update are successful, {@code false} otherwise.
     */
    public boolean addReplyToReply(int parentReplyId, String reply, String userName, boolean isPrivate) {
        String insertQuery = "INSERT INTO Replies (reply, parentReplyId, userName, isPrivate, postId) VALUES (?, ?, ?, ?, ?)";
        String updateQuery = "UPDATE Replies SET numReplies = numReplies + 1 WHERE replyId = ?";

        Connection conn = this.connection;

        try {
            conn.setAutoCommit(false);
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            insertStmt.setString(1, reply);
            insertStmt.setInt(2, parentReplyId);
            insertStmt.setString(3, userName);
            insertStmt.setBoolean(4, isPrivate);
            int postId = getPostIdByReplyId(parentReplyId);
            if (postId == -1) {
                throw new SQLException("Parent reply not found.");
            }
            insertStmt.setInt(5, postId);
            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Failed to insert reply.");
            }
            updateStmt.setInt(1, parentReplyId);
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update numReplies for parent reply.");
            }
            conn.commit();
            insertStmt.close();
            updateStmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the parentReplyId for a given reply.
     * If the reply is a top-level reply, this method returns {@code null}.
     *
     * @param replyId The ID of the reply.
     * @return The parentReplyId as an {@code Integer}, or {@code null} if it is a top-level reply.
     */
    public Integer getParentReplyId(int replyId) {
        String query = "SELECT parentReplyId FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int parentId = rs.getInt("parentReplyId");
                    return rs.wasNull() ? null : parentId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all nested replies for a given parent reply.
     *
     * @param parentReplyId The ID of the parent reply.
     * @param currentUser   The current user's username.
     * @return A {@code ResultSet} containing the nested replies, or {@code null} if an error occurs.
     */
    public ResultSet getNestedReplies(int parentReplyId, String currentUser) {
        int postId = getPostIdByReplyId(parentReplyId);
        if (postId == -1) return null;
        String query = "SELECT R.* FROM Replies R "
            + "LEFT JOIN Posts P ON R.postId = P.postId "
            + "WHERE R.parentReplyId = ? AND ("
            + "  R.isPrivate = FALSE OR "
            + "  R.userName = ? OR "
            + "  P.userName = ?"
            + ") "
            + "ORDER BY R.replyId ASC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, parentReplyId);
            pstmt.setString(2, currentUser);
            pstmt.setString(3, currentUser);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching nested replies: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds the given username to the like list for a reply.
     *
     * @param userName The username to add.
     * @param replyId  The reply's ID.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean addUsertoLikeList(String userName, int replyId) {
        String query = "UPDATE Replies SET likeList = CONCAT(COALESCE(likeList, ''), '\n', ?) WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setInt(2, replyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the like list (a string of usernames separated by newlines) for a reply.
     *
     * @param replyId The ID of the reply.
     * @return A string representing the like list, or an empty string if none exists.
     */
    public String getLikeList(int replyId) {
        String query = "SELECT likeList FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("likeList");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Decrements the like count for a specific reply.
     *
     * @param replyId The ID of the reply.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean decrementLikes(int replyId) {
        String sql = "UPDATE Replies SET likes = COALESCE(likes, 0) - 1 WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, replyId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a username from the like list of a reply.
     *
     * @param userName The username to remove.
     * @param replyId  The reply's ID.
     * @return {@code true} if the removal was successful, {@code false} otherwise.
     */
    public boolean removeUserFromLikeList(String userName, int replyId) {
        String currentList = getLikeList(replyId);
        if (currentList == null || currentList.isEmpty()) return false;
        List<String> users = new ArrayList<>(Arrays.asList(currentList.split("\n")));
        boolean removed = users.removeIf(u -> u.equals(userName));
        if (!removed) return false;
        String newList = String.join("\n", users);
        String query = "UPDATE Replies SET likeList = ? WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newList);
            pstmt.setInt(2, replyId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
