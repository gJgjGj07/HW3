package application;

import static org.junit.Assert.*;
import org.junit.*;
import java.sql.*;

public class InstructorReviewTest {
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
        // Create tables and base users
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (userId INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE, role VARCHAR(50))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerRequests (username VARCHAR(255) PRIMARY KEY, status VARCHAR(50))");
        stmt.execute("INSERT INTO Users (username, role) VALUES " +
                    "('student1','student'),('student2','student'),('student3','student')," +
                    "('instructor1','instructor')");
        stmt.close();
    }

    @After
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testInstructorViewPendingRequests() throws Exception {
        // Create two pending requests (student1 and student2)
        Statement st = conn.createStatement();
        st.execute("INSERT INTO ReviewerRequests (username, status) VALUES ('student1','pending'),('student2','pending')");
        st.close();
        // Instructor fetches pending requests list
        ResultSet pending = rHandler.getPendingReviewerRequests();
        int count = 0;
        boolean hasStudent1 = false;
        boolean hasStudent2 = false;
        while (pending.next()) {
            count++;
            String user = pending.getString("username");
            if ("student1".equals(user)) hasStudent1 = true;
            if ("student2".equals(user)) hasStudent2 = true;
        }
        pending.close();
        assertEquals("There should be 2 pending requests", 2, count);
        assertTrue("Pending list should include student1", hasStudent1);
        assertTrue("Pending list should include student2", hasStudent2);
    }

    @Test
    public void testInstructorApprovesReviewerRequest() throws Exception {
        // student1 has a pending request
        conn.createStatement().execute("INSERT INTO ReviewerRequests (username, status) VALUES ('student1','pending')");
        // Instructor approves student1's request
        conn.createStatement().execute("MERGE INTO Users (username, role) KEY(username) VALUES ('student3', 'student')");
        boolean approved = rHandler.approveReviewerRequest("student1");
        assertTrue("Approving an existing request should return true", approved);
        // Verify student1's role is now 'reviewer'
        Statement st = conn.createStatement();
        ResultSet rsUser = st.executeQuery("SELECT role FROM Users WHERE username='student1'");
        rsUser.next();
        String role = rsUser.getString("role");
        rsUser.close();
        assertEquals("User should be promoted to reviewer", "reviewer", role);
        // Verify the request entry is removed from pending list
        ResultSet rsReq = st.executeQuery("SELECT COUNT(*) FROM ReviewerRequests WHERE username='student1'");
        rsReq.next();
        int remaining = rsReq.getInt(1);
        rsReq.close();
        st.close();
        assertEquals("Pending request entry should be removed after approval", 0, remaining);
    }

    @Test
    public void testInstructorDeniesReviewerRequest() throws Exception {
        // student2 has a pending request
        conn.createStatement().execute("INSERT INTO ReviewerRequests (username, status) VALUES ('student2','pending')");
        // Instructor denies student2's request
        boolean denied = rHandler.denyReviewerRequest("student2");
        assertTrue("Denying an existing request should return true", denied);
        // Verify student2's role remains 'student'
        Statement st = conn.createStatement();
        ResultSet rsUser = st.executeQuery("SELECT role FROM Users WHERE username='student2'");
        rsUser.next();
        String role = rsUser.getString("role");
        rsUser.close();
        assertEquals("User should remain a student after denial", "student", role);
        // Verify the request entry is removed
        ResultSet rsReq = st.executeQuery("SELECT COUNT(*) FROM ReviewerRequests WHERE username='student2'");
        rsReq.next();
        int remaining = rsReq.getInt(1);
        rsReq.close();
        st.close();
        assertEquals("Pending request should be removed after denial", 0, remaining);
    }

    @Test
    public void testApproveNonexistentRequest() throws Exception {
        // Have one pending request for student1, but attempt to approve student3 who has none
        conn.createStatement().execute("INSERT INTO ReviewerRequests (username, status) VALUES ('student1','pending')");
        boolean approved = rHandler.approveReviewerRequest("student3");
        assertFalse("Approving a non-existent request should return false", approved);
        // Ensure student3 remains a student and student1's request is still pending
        Statement st = conn.createStatement();
        ResultSet rsRole = st.executeQuery("SELECT role FROM Users WHERE username='student3'");
        rsRole.next();
        String role3 = rsRole.getString("role");
        rsRole.close();
        assertEquals("Student3's role should remain student", "student", role3);
        ResultSet rsCount = st.executeQuery("SELECT COUNT(*) FROM ReviewerRequests WHERE username='student1'");
        rsCount.next();
        int count = rsCount.getInt(1);
        rsCount.close();
        st.close();
        assertEquals("Student1's pending request should remain untouched", 1, count);
    }

    @Test
    public void testInstructorReviewsStudentContributions() throws Exception {
        // Create a question and an answer by student1 (so student1 has both a question and an answer)
        qHandler.addPost("student1", "Sample Title", "Sample question content");
        ResultSet postRs = conn.createStatement().executeQuery("SELECT MAX(postId) AS pid FROM Posts WHERE userName='student1'");
        postRs.next();
        int postId = postRs.getInt("pid");
        postRs.close();
        aHandler.addAnswer("Student1's answer content", postId, "student1", false);
        // Instructor fetches all contributions of student1
        // (In practice, might call separate methods; here we query directly for demonstration)
        Statement st = conn.createStatement();
        ResultSet qRs = st.executeQuery("SELECT title FROM Posts WHERE userName='student1'");
        assertTrue("Student1 should have at least one question", qRs.next());
        String title = qRs.getString("title");
        qRs.close();
        ResultSet aRs = st.executeQuery("SELECT reply FROM Replies WHERE userName='student1'");
        assertTrue("Student1 should have at least one answer", aRs.next());
        String answerContent = aRs.getString("reply");
        aRs.close();
        st.close();
        // Verify the fetched question and answer correspond to student1's contributions
        assertEquals("Retrieved question title should match", "Sample Title", title);
        assertEquals("Retrieved answer content should match", "Student1's answer content", answerContent);
    }
}
