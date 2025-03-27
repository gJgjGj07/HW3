package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class UserHandler {
    // Database connection used for executing SQL statements.
    private Connection connection;


    public UserHandler(Connection connection) throws SQLException {
        this.connection = connection;
        createTables();
    }


    private void createTables() throws SQLException {
        String usersTable = "CREATE TABLE IF NOT EXISTS Users ("
                + "userName VARCHAR(255) PRIMARY KEY, "
                + "PostsRead VARCHAR(10000), "
                + "RepliesRead VARCHAR(10000))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(usersTable);
        }
    }

    /**
     * Adds a post ID to the list of posts read by the user. If the user does not exist, 
     * a new record is created with the post ID as the initial value. Otherwise, the post ID 
     * is appended to the existing comma-separated list.
     *
     * @param userName The username of the user.
     * @param postId   The ID of the post that has been read.
     */
    public void addPostRead(String userName, int postId) {
        try {
            if (!userExists(userName)) {
                // If the user does not exist, insert a new user record with the initial PostsRead value.
                String insertSQL = "INSERT INTO Users (userName, PostsRead) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                    pstmt.setString(1, userName);
                    pstmt.setString(2, String.valueOf(postId));
                    pstmt.executeUpdate();
                }
            } else {
                // If the user exists, update the PostsRead field by appending the new post ID.
                // If PostsRead is null, it sets it to the new postId; otherwise, it concatenates with a comma.
                String updateSQL = "UPDATE Users SET PostsRead = "
                        + "CASE WHEN PostsRead IS NULL THEN ? ELSE CONCAT(PostsRead, ',', ?) END "
                        + "WHERE userName = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
                    pstmt.setString(1, String.valueOf(postId));
                    pstmt.setString(2, String.valueOf(postId));
                    pstmt.setString(3, userName);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating PostsRead: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of post IDs that the specified user has read.
     *
     * @param userName The username of the user.
     * @return A List of Integer post IDs that the user has read.
     */
    public List<Integer> getReadPosts(String userName) {
        List<Integer> readPosts = new ArrayList<>();
        String query = "SELECT PostsRead FROM Users WHERE userName = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Retrieve the comma-separated string of post IDs.
                String postsRead = rs.getString("PostsRead");
                if (postsRead != null) {
                    // Split the string into individual IDs, parse them as integers, and add to the list.
                    for (String id : postsRead.split(",")) {
                        try {
                            readPosts.add(Integer.parseInt(id.trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid post ID format: " + id);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving read posts: " + e.getMessage());
        }
        return readPosts;
    }

    /**
     * Checks if a user exists in the Users table.
     *
     * @param userName The username to check.
     * @return true if the user exists, false otherwise.
     * @throws SQLException if an error occurs during the query.
     */
    private boolean userExists(String userName) throws SQLException {
        String query = "SELECT 1 FROM Users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            // If the query returns a result, the user exists.
            return pstmt.executeQuery().next();
        }
    }
}
