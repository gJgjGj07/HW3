package application;

//This class tests maintaining a trusted reviewers list and filtering reviews by trusted status. 
//It verifies adding and removing trusted reviewers, preventing duplicates, and filtering review results.

import static org.junit.Assert.*;
import org.junit.*;
import java.sql.*;

public class TrustedReviewerTest {
    private Connection conn;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private ReviewHandler rHandler;

    @Before
    public void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=0");
        qHandler = new QuestionHandler(conn);
        aHandler = new AnswerHandler(conn, qHandler);
        rHandler = new ReviewHandler(conn);
        Statement stmt = conn.createStatement();
        // Create tables and insert users (similar to FeedbackTest setup)
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (userId INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE, role VARCHAR(50))");
        stmt.execute("CREATE TABLE IF NOT EXISTS TrustedReviewers (student VARCHAR(255), reviewer VARCHAR(255), PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerWeights (student VARCHAR(255), reviewer VARCHAR(255), weight INT, PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerRequests (username VARCHAR(255) PRIMARY KEY, status VARCHAR(50))");
        stmt.execute("INSERT INTO Users (username, role) VALUES " +
                    "('student1','student'),('student2','student')," +
                    "('reviewer1','reviewer'),('reviewer2','reviewer'),('reviewer3','reviewer')");
        stmt.close();
    }

    @After
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testAddTrustedReviewer() throws Exception {
        // Student1 adds Reviewer2 to trusted list
        boolean added = rHandler.addTrustedReviewer("student1", "reviewer2");
        assertTrue("addTrustedReviewer should return true on success", added);
        // Verify the trusted relationship is stored in DB
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM TrustedReviewers WHERE student='student1' AND reviewer='reviewer2'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        assertEquals("Trusted reviewer entry should exist", 1, count);
        // Adding the same reviewer again should be prevented (no duplicate)
        boolean addedAgain = rHandler.addTrustedReviewer("student1", "reviewer2");
        assertFalse("Duplicate trust entry should be prevented", addedAgain);
    }

    @Test
    public void testRemoveTrustedReviewer() throws Exception {
        // First, add a trusted reviewer to remove
        rHandler.addTrustedReviewer("student1", "reviewer3");
        // Now remove reviewer3 from student1's trusted list
        boolean removed = rHandler.removeTrustedReviewer("student1", "reviewer3");
        assertTrue("removeTrustedReviewer should return true on success", removed);
        // Verify the entry is gone from the TrustedReviewers table
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM TrustedReviewers WHERE student='student1' AND reviewer='reviewer3'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        assertEquals("Trusted reviewer entry should be removed", 0, count);
    }

    @Test
    public void testFilterReviewsByTrusted() throws Exception {
        // Setup: an answer with two reviews (by reviewer1 and reviewer2)
        qHandler.addPost("student1", "Title", "Body");
        ResultSet pRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        pRs.next();
        int postId = pRs.getInt("pid");
        pRs.close();
        aHandler.addAnswer("Answer content", postId, "student1", false);
        ResultSet rRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        rRs.next();
        int replyId = rRs.getInt("rid");
        rRs.close();
        rHandler.addReviewForReply("Review by R1", "reviewer1", replyId);
        rHandler.addReviewForReply("Review by R2", "reviewer2", replyId);
        // Student1 trusts reviewer1
        rHandler.addTrustedReviewer("student1", "reviewer1");
        // Retrieve only trusted reviews for the answer
        ResultSet trustedReviews = rHandler.getTrustedReviewsByReplyId(replyId, "student1");
        // We expect only reviewer1's review to be present
        assertTrue("There should be at least one trusted review", trustedReviews.next());
        String reviewerName = trustedReviews.getString("reviewerName");
        assertEquals("Trusted reviews result should only contain reviewer1", "reviewer1", reviewerName);
        assertFalse("There should be only one trusted review result", trustedReviews.next());
        trustedReviews.close();
    }

    @Test
    public void testFilterReviewsByTrustedNoMatch() throws Exception {
        // Setup: an answer with two reviews (no trusted reviewers set)
        qHandler.addPost("student1", "QX", "BodyX");
        ResultSet pRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        pRs.next();
        int postId = pRs.getInt("pid");
        pRs.close();
        aHandler.addAnswer("Ans", postId, "student1", false);
        ResultSet rRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        rRs.next();
        int replyId = rRs.getInt("rid");
        rRs.close();
        rHandler.addReviewForReply("Review by R1", "reviewer1", replyId);
        rHandler.addReviewForReply("Review by R2", "reviewer2", replyId);
        // Student1 has no trusted reviewers
        ResultSet trustedReviews = rHandler.getTrustedReviewsByReplyId(replyId, "student1");
        // Expect no reviews in the trusted filter result
        assertFalse("No trusted reviewers, so no reviews should be returned", trustedReviews.next());
        trustedReviews.close();
    }
}

