//This class tests review creation, updating (versioning), deletion, 
// viewing one's own reviews, and the private feedback message exchange 
// between a student (answer author) and a reviewer.
package application;


import static org.junit.Assert.*;
import org.junit.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class FeedbackTest {
    private Connection conn;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private ReviewHandler rHandler;

    @Before
    public void setup() throws Exception {
        // Initialise in-memory H2 database (fresh for each test)&#8203;:contentReference[oaicite:0]{index=0}
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=0");
        qHandler = new QuestionHandler(conn);
        aHandler = new AnswerHandler(conn, qHandler);
        rHandler = new ReviewHandler(conn);
        // Create required tables not handled by handlers
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (userId INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE, role VARCHAR(50))");
        stmt.execute("CREATE TABLE IF NOT EXISTS TrustedReviewers (student VARCHAR(255), reviewer VARCHAR(255), PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerWeights (student VARCHAR(255), reviewer VARCHAR(255), weight INT, PRIMARY KEY(student, reviewer))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerRequests (username VARCHAR(255) PRIMARY KEY, status VARCHAR(50))");
        // Insert baseline users
        stmt.execute("INSERT INTO Users (username, role) VALUES " +
                    "('student1','student')," +
                    "('student2','student')," +
                    "('student3','student')," +
                    "('reviewer1','reviewer')," +
                    "('reviewer2','reviewer')," +
                    "('reviewer3','reviewer')," +
                    "('instructor1','instructor')");
        stmt.close();
    }

    @After
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testCreateAndRetrieveReviewForAnswer() throws Exception {
        // Student2 posts a question, Student1 posts an answer to that question
        qHandler.addPost("student2", "Sample Question", "Question body");
        ResultSet postRs = qHandler.getAllQuestions();
        postRs.next();
        int postId = postRs.getInt("postId");
        postRs.close();
        aHandler.addAnswer("This is an answer", postId, "student1", false);
        // Retrieve the replyId of the new answer
        Statement st = conn.createStatement();
        ResultSet replyRs = st.executeQuery("SELECT replyId FROM Replies WHERE postId=" + postId);
        assertTrue("Answer not inserted", replyRs.next());
        int replyId = replyRs.getInt("replyId");
        replyRs.close();
        st.close();
        // Reviewer1 creates a review for that answer
        boolean reviewAdded = rHandler.addReviewForReply("Good answer, but can be improved", "reviewer1", replyId);
        assertTrue("Review should be added successfully", reviewAdded);
        // Verify the review can be retrieved and matches the content/author
        ResultSet reviewsRs = rHandler.getReviewsByReplyId(replyId);
        int reviewCount = 0;
        boolean foundReview = false;
        while (reviewsRs.next()) {
            reviewCount++;
            String content = reviewsRs.getString("content");
            String reviewerName = reviewsRs.getString("reviewerName");
            if (reviewerName.equals("reviewer1") && content.contains("improved")) {
                foundReview = true;
            }
        }
        reviewsRs.close();
        assertEquals("There should be exactly 1 review for the answer", 1, reviewCount);
        assertTrue("Review by reviewer1 with expected content not found", foundReview);
    }

    @Test
    public void testUpdateReviewCreatesNewVersion() throws Exception {
        // Prepare an answer with an initial review by Reviewer1
        qHandler.addPost("student2", "Q", "Body");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("Initial answer", postId, "student1", false);
        ResultSet replyRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        replyRs.next();
        int replyId = replyRs.getInt("rid");
        replyRs.close();
        rHandler.addReviewForReply("Initial review content", "reviewer1", replyId);
        // Get the reviewId of the inserted review
        Statement stmt = conn.createStatement();
        ResultSet revRs = stmt.executeQuery("SELECT MAX(reviewId) AS maxId FROM Reviews");
        revRs.next();
        int originalReviewId = revRs.getInt("maxId");
        revRs.close();
        // Update the review content
        boolean updated = rHandler.updateReview(originalReviewId, "Updated content");
        assertTrue("updateReview should return true on success", updated);
        // Verify a new review record is created and old one remains as previous version
        ResultSet allReviews = rHandler.getReviewsByReplyId(replyId);
        int count = 0;
        int latestId = -1;
        String latestContent = "";
        while (allReviews.next()) {
            count++;
            latestId = allReviews.getInt("reviewId");
            latestContent = allReviews.getString("content");
        }
        allReviews.close();
        assertEquals("There should be 2 review records after update (original + new version)", 2, count);
        assertNotEquals("New review record ID should differ from original", originalReviewId, latestId);
        assertEquals("Content of latest review should match update", "Updated content", latestContent);
        // Verify version linking
        assertTrue("New review should have a previous version", rHandler.hasPreviousVersion(latestId));
        ReviewHandler.ReviewPrevious prev = rHandler.getPreviousReview(latestId);
        assertNotNull("Previous review object should be returned", prev);
        assertEquals("Previous review content should match original", "Initial review content", prev.getContent());
        assertEquals("Previous review ID should match original review ID", originalReviewId, prev.getReviewId());
    }

    @Test
    public void testDeleteReviewRemovesIt() throws Exception {
        // Prepare an answer with two reviews
        qHandler.addPost("student2", "Q2", "Body2");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("Answer body", postId, "student1", false);
        ResultSet replyRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        replyRs.next();
        int replyId = replyRs.getInt("rid");
        replyRs.close();
        rHandler.addReviewForReply("First review", "reviewer1", replyId);
        rHandler.addReviewForReply("Second review", "reviewer2", replyId);
        // Identify one review ID to delete (e.g., the first one added)
        Statement stmt = conn.createStatement();
        ResultSet idsRs = stmt.executeQuery("SELECT MIN(reviewId) AS minId, MAX(reviewId) AS maxId FROM Reviews WHERE replyId=" + replyId);
        idsRs.next();
        int firstReviewId = idsRs.getInt("minId");
        int secondReviewId = idsRs.getInt("maxId");
        idsRs.close();
        // Delete the first review
        boolean deleted = stmt.executeUpdate("DELETE FROM Reviews WHERE reviewId=" + firstReviewId) > 0;
        stmt.close();
        assertTrue("DELETE statement should remove the review", deleted);
        // Verify only the second review remains
        ResultSet remaining = rHandler.getReviewsByReplyId(replyId);
        int remainingCount = 0;
        boolean deletedFound = false;
        while (remaining.next()) {
            remainingCount++;
            if (remaining.getInt("reviewId") == firstReviewId) {
                deletedFound = true;
            }
        }
        remaining.close();
        assertFalse("Deleted review ID should not be present in results", deletedFound);
        assertEquals("Only one review should remain after deletion", 1, remainingCount);
    }

    @Test
    public void testReviewerCanViewOwnReviews() throws Exception {
        // Prepare two different answers and have Reviewer1 review both
        // Answer 1 setup
        qHandler.addPost("student2", "Question1", "Body1");
        ResultSet p1 = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        p1.next();
        int post1 = p1.getInt("pid");
        p1.close();
        aHandler.addAnswer("Answer1", post1, "student1", false);
        ResultSet r1 = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        r1.next();
        int reply1 = r1.getInt("rid");
        r1.close();
        rHandler.addReviewForReply("Review for answer1", "reviewer1", reply1);
        // Answer 2 setup
        qHandler.addPost("student1", "Question2", "Body2");
        ResultSet p2 = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        p2.next();
        int post2 = p2.getInt("pid");
        p2.close();
        aHandler.addAnswer("Answer2", post2, "student2", false);
        ResultSet r2 = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        r2.next();
        int reply2 = r2.getInt("rid");
        r2.close();
        rHandler.addReviewForReply("Review for answer2", "reviewer1", reply2);
        // Retrieve all reviews authored by reviewer1
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT replyId FROM Reviews WHERE reviewerName='reviewer1'");
        Set<Integer> reviewedReplyIds = new HashSet<>();
        while (rs.next()) {
            reviewedReplyIds.add(rs.getInt("replyId"));
        }
        rs.close();
        st.close();
        // Reviewer1 should have reviews on both reply1 and reply2
        assertTrue("Reviewer1's reviews should include replyId " + reply1, reviewedReplyIds.contains(reply1));
        assertTrue("Reviewer1's reviews should include replyId " + reply2, reviewedReplyIds.contains(reply2));
        assertEquals("Reviewer1 should have 2 reviews in total", 2, reviewedReplyIds.size());
    }

    @Test
    public void testPrivateFeedbackMessagingBetweenAuthorAndReviewer() throws Exception {
        // Prepare an answer by Student1 and a review by Reviewer1
        qHandler.addPost("student2", "Question", "Some question text");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("An answer from student1", postId, "student1", false);
        ResultSet replyRs = conn.createStatement().executeQuery("SELECT MAX(replyId) AS rid FROM Replies");
        replyRs.next();
        int replyId = replyRs.getInt("rid");
        replyRs.close();
        rHandler.addReviewForReply("Detailed review", "reviewer1", replyId);
        // Get the reviewId of the newly added review
        ResultSet revRs = conn.createStatement().executeQuery("SELECT MAX(reviewId) AS rid FROM Reviews");
        revRs.next();
        int reviewId = revRs.getInt("rid");
        revRs.close();
        // Student1 (author) sends a private feedback message to Reviewer1
        boolean fbAdded1 = rHandler.addFeedback(reviewId, "student1", "Thank you for the review!");
        // Reviewer1 replies to the feedback
        boolean fbAdded2 = rHandler.addFeedback(reviewId, "reviewer1", "Glad to help.");
        assertTrue("First feedback message should be added", fbAdded1);
        assertTrue("Reply feedback message should be added", fbAdded2);
        // Feedback count on the review should have incremented to 2
        int fbCount = rHandler.getFeedbackCount(reviewId);
        assertEquals("Feedback count should be 2 after two messages", 2, fbCount);
        // Retrieve feedback messages and verify order and content
        ResultSet fbRs = rHandler.getFeedbackForReview(reviewId);
        List<String> senders = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        while (fbRs.next()) {
            senders.add(fbRs.getString("sender"));
            messages.add(fbRs.getString("message"));
        }
        fbRs.close();
        assertEquals("There should be 2 feedback messages", 2, messages.size());
        assertEquals("First message sender should be student1", "student1", senders.get(0));
        assertEquals("First message content mismatch", "Thank you for the review!", messages.get(0));
        assertEquals("Second message sender should be reviewer1", "reviewer1", senders.get(1));
        assertEquals("Second message content mismatch", "Glad to help.", messages.get(1));
    }
}
