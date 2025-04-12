package application;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReviewHandlerTest {

    private ReviewHandler handler;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Creates an in-memory H2 database for testing (no external DB needed)
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        handler = new ReviewHandler(connection);
    }

    @Test
    public void testAddReview_validInput() {
        boolean result = handler.addReviewForReply("This is a great answer", "reviewer1", 101);
        assertTrue("Review should be added successfully", result);
    }
    
    @Test
    public void testAddReview_withSQLInjection() {
        boolean result = handler.addReviewForReply("DROP TABLE Users; --", "reviewer1", 102);
        assertTrue("Sanitized review should still be added", result);
    }

    @Test
    public void testValidateReview_emptyReview() {
        // Expecting one error message: "Review cannot be empty"
        assertEquals("Empty review should trigger one validation error", 1, handler.validateReview("").size());
    }

    @Test
    public void testValidateReview_sqlInjectionDetected() {
        // Expecting true since the input contains SQL keywords
        assertTrue("SQL injection should be detected", handler.detectSQLInjection("DROP TABLE Reviews"));
    }

    @Test
    public void testUpdateReview_validReview() throws SQLException {
        // Add a review first.
        boolean addResult = handler.addReviewForReply("Original content", "reviewer1", 201);
        assertTrue("Review should be added successfully", addResult);
        
        // Retrieve the review ID.
        int reviewId = -1;
        try (ResultSet rs = handler.getReviewsByReplyId(201)) {
            if (rs.next()) {
                reviewId = rs.getInt("reviewId");
            }
        }
        assertTrue("Review ID should be greater than 0", reviewId > 0);
        
        // Update the review.
        boolean updateResult = handler.updateReview(reviewId, "Updated content");
        assertTrue("Update should succeed for an existing review", updateResult);
        
        // Verify that the new record with updated content exists.
        String updatedContent = null;
        String query = "SELECT content FROM Reviews WHERE previousReviewId = " + reviewId;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             if (rs.next()) {
                 updatedContent = rs.getString("content");
             }
        }
        assertEquals("Updated content", updatedContent);
    }
}
