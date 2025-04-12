package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the creation, retrieval, updating, and validation of reviews and associated feedback.
 * Provides functionality for managing review versions and reviewer experience information.
 */
public class ReviewHandler {
    private Connection connection;
    // Predefined patterns to detect potential SQL injection attempts.
    private static final String[] SQL_INJECTION_PATTERNS = {
        "(?i)\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b",
        "--", 
        ";", 
        "/\\*", 
        "\\*/"
    };

    /**
     * Constructs a new ReviewHandler with the specified database connection.
     * Creates necessary tables if they don't exist.
     *
     * @param connection the database connection to use
     * @throws SQLException if there's an error creating the tables
     */
    public ReviewHandler(Connection connection) throws SQLException {
        this.connection = connection;
        createTables();
    }

    /**
     * Creates the necessary database tables (Reviews, ReviewFeedback, ReviewerExperience)
     * if they don't already exist.
     *
     * @throws SQLException if there's an error creating the tables
     */
    private void createTables() throws SQLException {
        String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews ("
                + "reviewId INT AUTO_INCREMENT PRIMARY KEY, "
                + "replyId INT DEFAULT NULL, "       // Tied to an answer (reply)
                + "postId INT DEFAULT NULL, "        // Tied to a question (post)
                + "content VARCHAR(10000), "
                + "reviewerName VARCHAR(255), "
                + "feedbackCount INT DEFAULT 0, "
                + "previousReviewId INT DEFAULT NULL"
                + ")";
        
        String feedbackTable = "CREATE TABLE IF NOT EXISTS ReviewFeedback ("
                + "feedbackId INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewId INT, "
                + "sender VARCHAR(255), "
                + "message VARCHAR(10000), "
                + "FOREIGN KEY (reviewId) REFERENCES Reviews(reviewId) ON DELETE CASCADE"
                + ")";
        
        String experienceTable = "CREATE TABLE IF NOT EXISTS ReviewerExperience ("
                + "username VARCHAR(255) PRIMARY KEY, "
                + "experience VARCHAR(10000)"
                + ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(reviewsTable);
            stmt.execute(feedbackTable);
            stmt.execute(experienceTable);
        }
    }

    /**
     * Validates a review by checking for empty content, minimum length,
     * and potential SQL injection patterns.
     *
     * @param review the review content to validate
     * @return list of error messages, empty list indicates valid review
     */
    public List<String> validateReview(String review) {
        List<String> errors = new ArrayList<>();
        if (review == null || review.trim().isEmpty()) {
            errors.add("Review cannot be empty");
            return errors;
        }
        String cleanReview = review.trim();
        if (cleanReview.length() < 5) {
            errors.add("Review must be at least 5 characters");
        }
        if (detectSQLInjection(cleanReview)) {
            errors.add("Review contains suspicious content");
        }
        return errors;
    }

    /**
     * Checks if the input string contains potential SQL injection patterns.
     *
     * @param input the string to check for SQL injection patterns
     * @return true if suspicious patterns are found, false otherwise
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
     * Sanitizes input by removing potentially dangerous SQL characters and keywords.
     *
     * @param input the string to sanitize
     * @return sanitized version of the input string
     */
    public String sanitizeInput(String input) {
        if (input == null) return "";
        String sanitized = input.replaceAll("[\"'#;()*/]", "");
        sanitized = sanitized.replaceAll("\\b(union|select|insert|update|delete|drop|alter|create|execute|shutdown)\\b", "");
        sanitized = sanitized.replaceAll("(?i)(;\\s*--|UNION\\s*--)", ";");
        return sanitized;
    }

    /**
     * Adds a review for a reply (answer) with the specified content and reviewer name.
     *
     * @param review the review content
     * @param reviewerName the name of the reviewer
     * @param replyId the ID of the reply being reviewed
     * @return true if the review was successfully added, false otherwise
     */
    public boolean addReviewForReply(String review, String reviewerName, int replyId) {
        String cleanReview = review;
        if (detectSQLInjection(review)) {
            cleanReview = sanitizeInput(review);
        }
        String insertQuery = "INSERT INTO Reviews (content, reviewerName, replyId, postId) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, cleanReview);
            pstmt.setString(2, reviewerName);
            pstmt.setInt(3, replyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting review for reply: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a review for a post (question) with the specified content and reviewer name.
     *
     * @param review the review content
     * @param reviewerName the name of the reviewer
     * @param postId the ID of the post being reviewed
     * @return true if the review was successfully added, false otherwise
     */
    public boolean addReviewForPost(String review, String reviewerName, int postId) {
        String cleanReview = review;
        if (detectSQLInjection(review)) {
            cleanReview = sanitizeInput(review);
        }
        String insertQuery = "INSERT INTO Reviews (content, reviewerName, postId, replyId) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, cleanReview);
            pstmt.setString(2, reviewerName);
            pstmt.setInt(3, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting review for post: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all reviews associated with a specific reply.
     *
     * @param replyId the ID of the reply
     * @return ResultSet containing all reviews for the specified reply
     */
    public ResultSet getReviewsByReplyId(int replyId) {
        String query = "SELECT * FROM Reviews WHERE replyId = ? ORDER BY reviewId ASC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, replyId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching reviews for reply: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all reviews associated with a specific post.
     *
     * @param postId the ID of the post
     * @return ResultSet containing all reviews for the specified post
     */
    public ResultSet getReviewsByPostId(int postId) {
        String query = "SELECT * FROM Reviews WHERE postId = ? ORDER BY reviewId ASC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, postId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching reviews for post: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the content of a specific review by its ID.
     *
     * @param reviewId the ID of the review
     * @return the review content as a String, or null if not found
     */
    public String getReviewContentById(int reviewId) {
        String query = "SELECT content FROM Reviews WHERE reviewId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("content");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review content: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates a review by creating a new version while preserving the old version.
     *
     * @param reviewId the ID of the review to update
     * @param newContent the new content for the review
     * @return true if the update was successful, false otherwise
     */
    public boolean updateReview(int reviewId, String newContent) {
        // Retrieve the reviewerName and replyId from the existing review record.
    	if (reviewId <= 0) {
            System.err.println("Invalid review ID provided.");
            return false;
        }
        String getQuery = "SELECT reviewerName, replyId FROM Reviews WHERE reviewId = ?";
        String reviewerName = null;
        int replyId = -1;
        int postId = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(getQuery)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                reviewerName = rs.getString("reviewerName");
                replyId = rs.getInt("replyId");
                postId = rs.getInt("postId");
            } else {
                System.err.println("Review not found for update.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving review for update: " + e.getMessage());
            return false;
        }
        
        // Insert a new record for the updated review with a link to the previous version.
        String insertQuery = "INSERT INTO Reviews (content, reviewerName, replyId, postId, previousReviewId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, newContent);
            pstmt.setString(2, reviewerName);
            // Preserve the original linkage (only one of these should be set).
            if (replyId > 0) {
                pstmt.setInt(3, replyId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            if (postId > 0) {
                pstmt.setInt(4, postId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, reviewId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the feedback count for a specific review.
     *
     * @param reviewId the ID of the review
     * @return the number of feedback messages for the review, or 0 if not found
     */
    public int getFeedbackCount(int reviewId) {
        String query = "SELECT feedbackCount FROM Reviews WHERE reviewId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("feedbackCount");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching feedback count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Increments the feedback count for a specific review.
     *
     * @param reviewId the ID of the review
     * @return true if the count was successfully incremented, false otherwise
     */
    public boolean incrementFeedbackCount(int reviewId) {
        String sql = "UPDATE Reviews SET feedbackCount = feedbackCount + 1 WHERE reviewId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing feedback count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds feedback to a specific review and increments the feedback count.
     *
     * @param reviewId the ID of the review receiving feedback
     * @param sender the username of the feedback sender
     * @param message the feedback message content
     * @return true if the feedback was successfully added, false otherwise
     */
    public boolean addFeedback(int reviewId, String sender, String message) {
        String insertFeedback = "INSERT INTO ReviewFeedback (reviewId, sender, message) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertFeedback)) {
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, sender);
            pstmt.setString(3, message);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // If feedback insertion was successful, increment the feedback count.
                return incrementFeedbackCount(reviewId);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding feedback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all feedback messages for a specific review.
     *
     * @param reviewId the ID of the review
     * @return ResultSet containing all feedback messages for the review
     */
    public ResultSet getFeedbackForReview(int reviewId) {
        String query = "SELECT sender, message FROM ReviewFeedback WHERE reviewId = ? ORDER BY feedbackId ASC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, reviewId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching feedback for review: " + e.getMessage());
            return null;
        }
    }
    
    // ============================
    // Experience handling methods
    // ============================
    
    /**
     * Retrieves the experience information for a specific reviewer.
     *
     * @param username the username of the reviewer
     * @return the reviewer's experience information, or null if not found
     */
    public String getExperience(String username) {
        String query = "SELECT experience FROM ReviewerExperience WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("experience");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving experience: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates or inserts experience information for a reviewer.
     *
     * @param username the username of the reviewer
     * @param experience the experience information to store
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateExperience(String username, String experience) {
        // First, check if an entry already exists.
        String checkQuery = "SELECT username FROM ReviewerExperience WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // Entry exists, so update it.
                String updateQuery = "UPDATE ReviewerExperience SET experience = ? WHERE username = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, experience);
                    updateStmt.setString(2, username);
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            } else {
                // No entry exists, insert a new row.
                String insertQuery = "INSERT INTO ReviewerExperience (username, experience) VALUES (?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, experience);
                    int rowsAffected = insertStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating/inserting experience: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves the previous version of a review.
     *
     * @param reviewId the ID of the current review version
     * @return ReviewPrevious object containing the previous version, or null if none exists
     */
    public ReviewPrevious getPreviousReview(int reviewId) {
        String query = "SELECT r1.reviewId, r1.content, r1.reviewerName, r1.feedbackCount " +
                       "FROM Reviews r1 " +
                       "JOIN Reviews r2 ON r1.reviewId = r2.previousReviewId " +
                       "WHERE r2.reviewId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int prevReviewId = rs.getInt("reviewId");
                String content = rs.getString("content");
                String reviewerName = rs.getString("reviewerName");
                int feedbackCount = rs.getInt("feedbackCount");
                
                return new ReviewPrevious(prevReviewId, content, reviewerName, feedbackCount);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching previous review: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a review has a previous version.
     *
     * @param reviewId the ID of the review to check
     * @return true if a previous version exists, false otherwise
     */
    public boolean hasPreviousVersion(int reviewId) {
        String query = "SELECT 1 FROM Reviews r1 " +
                       "JOIN Reviews r2 ON r1.reviewId = r2.previousReviewId " +
                       "WHERE r2.reviewId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a row was found
        } catch (SQLException e) {
            System.err.println("Error checking for previous review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Represents a previous version of a review.
     */
    public static class ReviewPrevious {
        private int reviewId;
        private String content;
        private String reviewerName;
        private int feedbackCount;

        /**
         * Constructs a ReviewPrevious object.
         *
         * @param reviewId the ID of the previous review
         * @param content the content of the previous review
         * @param reviewerName the name of the reviewer
         * @param feedbackCount the feedback count for the previous review
         */
        public ReviewPrevious(int reviewId, String content, String reviewerName, int feedbackCount) {
            this.reviewId = reviewId;
            this.content = content;
            this.reviewerName = reviewerName;
            this.feedbackCount = feedbackCount;
        }

        /**
         * Gets the review ID.
         * @return the review ID
         */
        public int getReviewId() { return reviewId; }
        
        /**
         * Gets the review content.
         * @return the review content
         */
        public String getContent() { return content; }
        
        /**
         * Gets the reviewer name.
         * @return the reviewer name
         */
        public String getReviewerName() { return reviewerName; }
        
        /**
         * Gets the feedback count.
         * @return the feedback count
         */
        public int getFeedbackCount() { return feedbackCount; }
    }

	public ResultSet getPendingReviewerRequests() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean approveReviewerRequest(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean denyReviewerRequest(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean requestReviewer(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addTrustedReviewer(String string, String string2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeTrustedReviewer(String string, String string2) {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getTrustedReviewsByReplyId(int replyId, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setReviewerWeight(String string, String string2, int i) {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getReviewsByReplyIdSortedByWeight(int replyId, String string) {
		// TODO Auto-generated method stub
		return null;
	}
    
}