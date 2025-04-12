package application;

import static org.junit.Assert.*;
import org.junit.*;
import java.sql.*;

public class WeightageTest {
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
        // Create tables and insert sample users
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (userId INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE, role VARCHAR(50))");
        stmt.execute("CREATE TABLE IF NOT EXISTS TrustedReviewers (student VARCHAR(255), reviewer VARCHAR(255), PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerWeights (student VARCHAR(255), reviewer VARCHAR(255), weight INT, PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerRequests (username VARCHAR(255) PRIMARY KEY, status VARCHAR(50))");
        stmt.execute("INSERT INTO Users (username, role) VALUES " +
                    "('student1','student'),('reviewer1','reviewer'),('reviewer2','reviewer'),('reviewer3','reviewer')");
        stmt.close();
    }

    @After
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testAssignWeightToReviewer() throws Exception {
        // Student1 assigns weight 5 to Reviewer1
        boolean set = rHandler.setReviewerWeight("student1", "reviewer1", 5);
        assertTrue("Setting a new weight should succeed", set);
        // Verify the weight is stored
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT weight FROM ReviewerWeights WHERE student='student1' AND reviewer='reviewer1'");
        assertTrue("Weight entry should exist", rs.next());
        int weight = rs.getInt("weight");
        rs.close();
        st.close();
        assertEquals("Stored weight should match assigned value", 5, weight);
    }

    @Test
    public void testUpdateWeightForReviewer() throws Exception {
        // Assign an initial weight and then update it
        rHandler.setReviewerWeight("student1", "reviewer2", 3);
        boolean updated = rHandler.setReviewerWeight("student1", "reviewer2", 4);
        assertTrue("Updating an existing weight should succeed", updated);
        // Verify the weight value is updated to 4 (not duplicated entry)
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT weight FROM ReviewerWeights WHERE student='student1' AND reviewer='reviewer2'");
        rs.next();
        int weight = rs.getInt("weight");
        rs.close();
        st.close();
        assertEquals("Weight for reviewer2 should be updated to 4", 4, weight);
    }

    @Test
    public void testSortReviewsByWeight() throws Exception {
        // Prepare an answer with two reviews by different reviewers
        qHandler.addPost("student1", "Sorting Q", "Sorting body");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("Answer", postId, "student1", false);
        ResultSet repRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        repRs.next();
        int replyId = repRs.getInt("rid");
        repRs.close();
        // Add reviews in a specific order (Reviewer2 then Reviewer1)
        rHandler.addReviewForReply("Review by R2", "reviewer2", replyId);
        rHandler.addReviewForReply("Review by R1", "reviewer1", replyId);
        // Assign weights: Reviewer1 = 5 (higher), Reviewer2 = 2 (lower)
        rHandler.setReviewerWeight("student1", "reviewer1", 5);
        rHandler.setReviewerWeight("student1", "reviewer2", 2);
        // Retrieve reviews sorted by weight (descending)
        ResultSet sorted = rHandler.getReviewsByReplyIdSortedByWeight(replyId, "student1");
        assertTrue("There should be at least one review", sorted.next());
        String firstReviewer = sorted.getString("reviewerName");
        assertEquals("Reviewer with higher weight should come first", "reviewer1", firstReviewer);
        assertTrue("There should be a second review", sorted.next());
        String secondReviewer = sorted.getString("reviewerName");
        assertEquals("Reviewer with lower weight should come after higher weight", "reviewer2", secondReviewer);
        sorted.close();
    }

    @Test
    public void testSortReviewsByWeightTie() throws Exception {
        // Prepare an answer with two reviews by different reviewers
        qHandler.addPost("student1", "Tie Q", "Tie body");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("Answer tie", postId, "student1", false);
        ResultSet repRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        repRs.next();
        int replyId = repRs.getInt("rid");
        repRs.close();
        // Add two reviews (order: Reviewer2 then Reviewer3)
        rHandler.addReviewForReply("Review by R2", "reviewer2", replyId);
        rHandler.addReviewForReply("Review by R3", "reviewer3", replyId);
        // Assign equal weights to both reviewers
        rHandler.setReviewerWeight("student1", "reviewer2", 5);
        rHandler.setReviewerWeight("student1", "reviewer3", 5);
        // Retrieve sorted by weight
        ResultSet sorted = rHandler.getReviewsByReplyIdSortedByWeight(replyId, "student1");
        assertTrue(sorted.next());
        String firstReviewer = sorted.getString("reviewerName");
        assertTrue(sorted.next());
        String secondReviewer = sorted.getString("reviewerName");
        sorted.close();
        // With equal weights, reviews should default to chronological (Reviewer2 was inserted first)
        assertEquals("With tie weights, reviewer2 (first inserted) should appear first", "reviewer2", firstReviewer);
        assertEquals("Second review should be from reviewer3", "reviewer3", secondReviewer);
    }

    @Test
    public void testWeightValueBounds() throws Exception {
        // Attempt to assign an invalid (negative) weight
        boolean setNegative = rHandler.setReviewerWeight("student1", "reviewer1", -1);
        assertFalse("Negative weight value should be rejected", setNegative);
        // Verify that no entry was created for the negative weight
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ReviewerWeights WHERE student='student1' AND reviewer='reviewer1'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        assertEquals("No weight entry should exist for negative value", 0, count);
    }
}

