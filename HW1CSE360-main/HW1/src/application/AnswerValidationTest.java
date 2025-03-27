package application;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import application.AnswerHandler;
import databasePart1.DatabaseHelper;

public class AnswerValidationTest {
    private static int testCount = 0;
    private static int passedCount = 0;
    private final AnswerHandler answerHandler;
    static DatabaseHelper dbHelper = new DatabaseHelper();
    public AnswerValidationTest(AnswerHandler answerHandler) {
        this.answerHandler = answerHandler;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
    	Connection conn = null;
    	try {
			conn = dbHelper.connectToDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        AnswerHandler handler = null;
		try {
			handler = new AnswerHandler(conn, null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // null connection for testing
        AnswerValidationTest tester = new AnswerValidationTest(handler);
        
        System.out.println("Running Answer Validation Tests\n");
        
        // Valid answers
        tester.runTest("Good answer", 
                     "Manual testing is time-consuming and often error-prone.  You will be required to do automated testing using JUnit after the midterm, so why not practice now?", 
                     false, 
                     "Should pass validation");
        
        // Length tests
        tester.runTest("Short answer", 
                     "Nope", 
                     true, 
                     "Should fail due to length <5 characters");
        
        // SQL injection tests
        tester.runTest("Basic SQL injection", 
                     "SELECT * FROM users; DROP TABLE posts;", 
                     true, 
                     "Should detect SQL keywords");
        
        tester.runTest("Comment injection", 
                     "Yes, but make sure you highlight the basic requirements first in your submission so as to make it easier for the graders to grade' -- This is a comment", 
                     true, 
                     "Should detect SQL comment syntax");
        
        // Empty content
        tester.runTest("Empty answer", 
                     "", 
                     true, 
                     "Should detect empty input");
        
        // Special characters
        tester.runTest("Special characters", 
                     "This # of user stories is up to you, but make sure to implement CRUD!", 
                     false, 
                     "Should pass validation");
        
        System.out.println("\nTest Results:");
        System.out.println("Total tests: " + testCount);
        System.out.println("Passed: " + passedCount);
        System.out.println("Failed: " + (testCount - passedCount));
    }

    private void runTest(String testName, String input, boolean expectError, String description) {
        testCount++;
        System.out.println("Test #" + testCount + ": " + testName);
        System.out.println("Description: " + description);
        System.out.println("Input: \"" + input + "\"");
        
        List<String> errors = answerHandler.validateAnswer(input);
        String sanitized = input;
        if (answerHandler.detectSQLInjection(input)) {
        	sanitized = answerHandler.sanitizeInput(input);
        }
        boolean result = validateResult(errors, expectError, sanitized);
        
        System.out.println("Errors: " + (errors.isEmpty() ? "None" : String.join(", ", errors)));
        System.out.println("Sanitized: \"" + sanitized + "\"");
        System.out.println("Test Result: " + (result ? "PASSED" : "FAILED"));
        System.out.println("----------------------------------------");
        
        if (result) passedCount++;
    }

    private boolean validateResult(List<String> errors, boolean expectError, String sanitized) {
        // Check expected error state matches
        if (expectError != !errors.isEmpty()) return false;
        
        if (answerHandler.detectSQLInjection(sanitized)) {
        // Additional check for SQL injection cleaning
	        if (!sanitized.matches("^[a-zA-Z0-9 .,!?-]*$")) {
	            return false;
	        }
        }
        return true;
    }
}