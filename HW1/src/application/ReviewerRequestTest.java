package application;

import static org.junit.Assert.*;
import org.junit.*;
import java.sql.*;

public class ReviewerRequestTest {
    private Connection conn;
    private ReviewHandler rHandler;

    @Before
    public void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=0");
        rHandler = new ReviewHandler(conn);
        Statement stmt = conn.createStatement();
        // Create tables and users
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (userId INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE, role VARCHAR(50))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ReviewerRequests (username VARCHAR(255) PRIMARY KEY, status VARCHAR(50))");
        stmt.execute("INSERT INTO Users (username, role) VALUES " +
                    "('student1','student'),('student2','student'),('reviewer1','reviewer'),('instructor1','instructor')");
        stmt.close();
    }

    @After
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testStudentSubmitReviewerRequest() throws Exception {
        // student1 submits a reviewer request
        boolean requested = rHandler.requestReviewer("student1");
        assertTrue("Student1's request should be accepted", requested);
        // Verify a pending request is recorded
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT status FROM ReviewerRequests WHERE username='student1'");
        assertTrue("Pending request entry should exist for student1", rs.next());
        String status = rs.getString("status");
        rs.close();
        st.close();
        assertEquals("New request should have status 'pending'", "pending", status);
    }

    @Test
    public void testDuplicateReviewerRequest() throws Exception {
        // student2 submits a request twice
        boolean first = rHandler.requestReviewer("student2");
        boolean second = rHandler.requestReviewer("student2");
        assertTrue("First request should succeed", first);
        assertFalse("Second request should be rejected as duplicate", second);
        // Verify only one request entry exists
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ReviewerRequests WHERE username='student2'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        assertEquals("There should only be one pending request for student2", 1, count);
    }

    @Test
    public void testAlreadyReviewerCannotRequest() throws Exception {
        // reviewer1 (already a reviewer) attempts to request reviewer status
        boolean requested = rHandler.requestReviewer("reviewer1");
        assertFalse("Existing reviewers should not be able to request reviewer status again", requested);
        // Verify no request entry was created for reviewer1
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ReviewerRequests WHERE username='reviewer1'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        st.close();
        assertEquals("No request should be recorded for an existing reviewer", 0, count);
    }

    @Test
    public void testRoleNotChangedOnRequest() throws Exception {
        // student1 submits a request
        rHandler.requestReviewer("student1");
        // Check that student1's role is still 'student' (not yet a reviewer until approved)
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT role FROM Users WHERE username='student1'");
        rs.next();
        String role = rs.getString("role");
        rs.close();
        st.close();
        assertEquals("User role should remain 'student' after requesting", "student", role);
    }

    @Test
    public void testMultipleStudentsCanRequestReviewer() throws Exception {
        // student1 and student2 both submit requests
        rHandler.requestReviewer("student1");
        rHandler.requestReviewer("student2");
        // Verify both requests are recorded
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT username FROM ReviewerRequests");
        boolean student1Found = false;
        boolean student2Found = false;
        int count = 0;
        while (rs.next()) {
            count++;
            String user = rs.getString("username");
            if ("student1".equals(user)) student1Found = true;
            if ("student2".equals(user)) student2Found = true;
        }
        rs.close();
        st.close();
        assertEquals("There should be 2 pending requests", 2, count);
        assertTrue("student1's request should be present", student1Found);
        assertTrue("student2's request should be present", student2Found);
    }
}
