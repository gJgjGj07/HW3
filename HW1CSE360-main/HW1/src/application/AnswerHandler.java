package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AnswerHandler {
    // Database connection for executing SQL queries.
    private Connection connection;
    // Reference to the QuestionHandler, possibly used for inter-related question operations.
    private QuestionHandler qHandler;

    // Predefined patterns to detect potential SQL injection attempts.
    private static final String[] SQL_INJECTION_PATTERNS = {
        "(?i)\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b",
        "--", 
        ";", 
        "/\\*", 
        "\\*/"
    };

    public AnswerHandler(Connection connection, QuestionHandler qHandler) throws SQLException {
        this.connection = connection;
        this.qHandler = qHandler;
        createTables();
    }

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
        
        // Check if the answer is null or empty after trimming.
        if (answer == null || answer.trim().isEmpty()) {
            errors.add("Answer cannot be empty");
            return errors;
        }
        
        // Remove leading/trailing spaces and calculate the length.
        String cleanAnswer = answer.trim();
        int length = cleanAnswer.length();
        
        // Ensure the answer meets the minimum length requirement.
        if (length < 5) {
            errors.add("Answer must be at least 5 characters");
        }
        // Check for suspicious content that might indicate an SQL injection attempt.
        if (detectSQLInjection(cleanAnswer)) {
            errors.add("Answer contains suspicious content");
        }
        
        return errors;
    }

    /**
     * Checks the input string for patterns that may indicate an SQL injection attempt.
     *
     * @param input The input string to check.
     * @return true if any SQL injection pattern is detected, otherwise false.
     */
    public boolean detectSQLInjection(String input) {
        if (input == null || input.isEmpty()) return false;
        
        // Sanitize input and convert to lowercase for uniform matching.
        String cleaned = sanitizeInput(input).toLowerCase();
        
        // Iterate over each SQL injection pattern.
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

        // Remove dangerous characters that can be used for SQL injection.
        String sanitized = input.replaceAll("[\"'#;()*/]", "");

        // Remove common SQL keywords to prevent injection.
        sanitized = sanitized.replaceAll("\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b", "");

        // Remove specific sequences like "--" when following ";" or "UNION" (case-insensitive).
        sanitized = sanitized.replaceAll("(?i)(;\\s*--|UNION\\s*--)", ";"); 

        return sanitized;
    }

    /**
     * Inserts a new answer (reply) into the Replies table. If suspicious content is detected, 
     * the answer is sanitized before insertion.
     *
     * @param answer   The reply text.
     * @param postId   The ID of the post this reply is associated with.
     * @param userName The username of the person posting the reply.
     * @param isPrivate Whether the reply is private (visible only to the poster and post creator).
     */
    public void addAnswer(String answer, int postId, String userName, boolean isPrivate) {
        String cleanAnswer = answer;
        // Sanitize the answer if any SQL injection pattern is detected.
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
     * Retrieves all replies associated with a specific post ID, filtered by visibility.
     *
     * @param postId The ID of the post whose replies are to be retrieved.
     * @param currentUser The username of the current user (to filter private replies).
     * @return A ResultSet containing all matching replies, or null if an error occurs.
     */
    public ResultSet getRepliesByPostId(int postId, String currentUser) {
        String query = "SELECT R.* FROM Replies R "
            + "LEFT JOIN Posts P ON R.postId = P.postId "
            + "WHERE R.postId = ? AND ("
            + "  R.isPrivate = FALSE OR "
            + "  R.userName = ? OR "
            + "  P.userName = ?"
            + ") "
            + "ORDER BY R.replyId ASC";  // Add sorting

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
     * @param postId The ID of the post for which replies should be deleted.
     * @return true if one or more rows were deleted, false otherwise.
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
     * Retrieves the content of a reply using its reply ID.
     *
     * @param replyId The ID of the reply.
     * @return The reply content as a String, or null if no reply is found.
     * @throws SQLException if an error occurs during the SQL query.
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
     * @return true if the update was successful (one or more rows affected), false otherwise.
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
     * Increments the "likes" count for a specific reply by its replyId.
     * Handles NULL values in the "likes" column by treating them as 0.
     *
     * @param replyId The ID of the reply to increment likes for.
     * @return true if the update was successful (at least 1 row affected), false otherwise.
     */
    public boolean incrementLikes(int replyId) {
        String sql = "UPDATE Replies SET likes = COALESCE(likes, 0) + 1 WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, replyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // True if the reply exists and was updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the number of likes for a specific reply.
     * 
     * @param replyId The ID of the reply to fetch likes for.
     * @return The number of likes (0 if no likes exist), or `-1` if the reply does not exist 
     *         or an error occurs.
     */
    public int getNumLikes(int replyId) {
        String sql = "SELECT COALESCE(likes, 0) AS likes FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, replyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("likes"); // Returns 0 or the actual like count
            } else {
                return -1; // Reply does not exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Error occurred
        }
    }

    /**
     * Retrieves the postId associated with a specific reply (answer).
     * 
     * @param replyId The ID of the reply (answer).
     * @return The postId linked to the reply, or `-1` if the reply does not exist or an error occurs.
     */
    public int getPostIdByReplyId(int replyId) {
        String query = "SELECT postId FROM Replies WHERE replyId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("postId");
                } else {
                    return -1; // Reply not found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Error during database operation
        }
    }
    /**
     * Adds a reply to a parent reply and increments the numReplies count for the parent.
     *
     * @param parentReplyId The ID of the parent reply.
     * @param reply         The content of the new reply.
     * @param userName      The username of the user posting the reply.
     * @param isPrivate     Whether the reply is private (visible only to the poster and post creator).
     * @return true if the operation is successful, false otherwise.
     */
    public boolean addReplyToReply(int parentReplyId, String reply, String userName, boolean isPrivate) {
        String insertQuery = "INSERT INTO Replies (reply, parentReplyId, userName, isPrivate, postId) VALUES (?, ?, ?, ?, ?)";
        String updateQuery = "UPDATE Replies SET numReplies = numReplies + 1 WHERE replyId = ?";

        try (Connection conn = this.connection;
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            // Start a transaction
            conn.setAutoCommit(false);

            // Step 1: Insert the new reply
            insertStmt.setString(1, reply);
            insertStmt.setInt(2, parentReplyId); // Link to the parent reply
            insertStmt.setString(3, userName);
            insertStmt.setBoolean(4, isPrivate);

            // Retrieve the postId associated with the parent reply
            int postId = getPostIdByReplyId(parentReplyId);
            if (postId == -1) {
                throw new SQLException("Parent reply not found.");
            }
            insertStmt.setInt(5, postId);

            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Failed to insert reply.");
            }

            // Step 2: Increment the numReplies count for the parent reply
            updateStmt.setInt(1, parentReplyId);
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update numReplies for parent reply.");
            }

            // Commit the transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // Rollback the transaction in case of an error
                this.connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                // Reset auto-commit to true
                this.connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Retrieves all nested replies for a specific parent reply.
     *
     * @param parentReplyId The ID of the parent reply.
     * @param currentUser   The username of the current user (to filter private replies).
     * @return A ResultSet containing all nested replies, or null if an error occurs.
     */
    public ResultSet getNestedReplies(int parentReplyId, String currentUser) {
        // Get postId first to avoid potential NPE
        int postId = getPostIdByReplyId(parentReplyId);
        if(postId == -1) return null;
        
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
    public boolean addUsertoLikeList(String userName, int replyId) {
		String query = "UPDATE Replies SET likeList = CONCAT(COALESCE(likeList, ''), '\n', ?) WHERE replyId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the role and userId parameters
            pstmt.setString(1, userName);
            pstmt.setInt(2, replyId);

            // Execute the query
            int rowsAffected = pstmt.executeUpdate();

            // Return true if a row was updated, false otherwise
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
	}
    public String getLikeList(int postId) {
		String query = "SELECT likeList FROM Replies WHERE replyId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, postId);
			// Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the listList if the postId is found
                    return rs.getString("likeList");
                }
            }
		} 
		catch (SQLException e) {
			e.printStackTrace();
	    }
			
		return "";		
	}
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
