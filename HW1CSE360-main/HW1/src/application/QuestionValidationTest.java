package application;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;

public class QuestionValidationTest {
    private static int testCount = 0;
    private static int passedCount = 0;
    private final QuestionHandler questionHandler;
    static DatabaseHelper dbHelper = new DatabaseHelper();

    public QuestionValidationTest(QuestionHandler questionHandler) {
        this.questionHandler = questionHandler;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // Use in-memory database for testing
        Connection connection = dbHelper.connectToDatabase();
        QuestionHandler handler = new QuestionHandler(connection);
        QuestionValidationTest tester = new QuestionValidationTest(handler);
        
        System.out.println("Running Question Validation Tests\n");
        
        // Valid cases
        tester.runTest("Valid Input", 
                     "HW2 User Stories", 
                     "I am a little confused on HW2. Which user stories should we apply and how do we know which ones follow CRUD?", 
                     false,
                     "Should pass validation");

        // Title tests
        tester.runTest("Long Title", 
                     "A".repeat(101), 
                     "Valid question?", 
                     true,
                     "Should fail with title length error");
        
        tester.runTest("SQL in Title", 
                     "SELECT * FROM users", 
                     "Just wanted to verify that homework2 is due Friday as per the syllabus and that turning it in tonight is purely extra credit correct?", 
                     true,
                     "Should detect SQL in title");

        // Question content tests
        tester.runTest("Empty Question", 
                     "HW2 User Stories", 
                     "", 
                     true,
                     "Should detect empty question");
        
        tester.runTest("Short Question", 
                     "Title", 
                     "Hi?", 
                     true,
                     "Should detect short question");
        

        tester.runTest("SQL in Question", 
                     "Hw2 due date", 
                     "DROP TABLE posts;", 
                     true,
                     "Should detect SQL in question");

        // Combined errors
        tester.runTest("Multiple Errors", 
                     "A".repeat(150), 
                     "", 
                     true,
                     "Should detect title length + empty question");

        // Edge cases
        tester.runTest("Max Title Length", 
                     "A".repeat(100), 
                     "Let me preface this with the fact that I understand D stands for delete. However, none of the stories listed on canvas include any mention of deletion, CRU operations. If we are expected to create a subset of these user stories, where does the deletion come from? Are we supposed to write our own user stories for deletion - violating the subset operation?", 
                     false,
                     "Should accept exact 100 character title");
        
        tester.runTest("Min Question Length", 
                     "Title", 
                     "Nope", 
                     true,
                     "Should reject 4 character question");

        // Sanitization check
        tester.runTest("Special Characters", 
                     "Test#Title';", 
                     "Some; *content'", 
                     false,
                     "Should pass validation");

        System.out.println("\nTest Results:");
        System.out.println("Total tests: " + testCount);
        System.out.println("Passed: " + passedCount);
        System.out.println("Failed: " + (testCount - passedCount));
    }

    private void runTest(String testName, String title, String question, 
                        boolean expectError, String description) {
        testCount++;
        System.out.println("Test #" + testCount + ": " + testName);
        System.out.println("Description: " + description);
        System.out.println("Title: \"" + title + "\"");
        System.out.println("Question: \"" + question + "\"");
        
        List<String> errors = questionHandler.validateQuestion(title, question);
        String sanitizedTitle = title;
        String sanitizedQuestion = question;
        if (questionHandler.detectSQLInjection(title)) {
        	sanitizedTitle = questionHandler.sanitizeInput(title);
        }
        if (questionHandler.detectSQLInjection(question)) {
        	sanitizedQuestion = questionHandler.sanitizeInput(question);
    	}
        boolean result = validateResult(errors, expectError, sanitizedTitle, sanitizedQuestion);
        
        System.out.println("Errors: " + (errors.isEmpty() ? "None" : String.join(", ", errors)));
        System.out.println("Sanitized Title: \"" + sanitizedTitle + "\"");
        System.out.println("Sanitized Question: \"" + sanitizedQuestion + "\"");
        System.out.println("Test Result: " + (result ? "PASSED" : "FAILED"));
        System.out.println("----------------------------------------");
        
        if (result) passedCount++;
    }

    private boolean validateResult(List<String> errors, boolean expectError, 
                                  String sanitizedTitle, String sanitizedQuestion) {
        // Check error expectation
        if (expectError != !errors.isEmpty()) return false;
        
        if (questionHandler.detectSQLInjection(sanitizedQuestion)) {
        // Verify sanitization
	        if (!sanitizedTitle.matches("^[a-zA-Z0-9 ]*$")) return false;
	        if (!sanitizedQuestion.matches("^[a-zA-Z0-9 .,!?-]*$")) return false;
        }
        return true;
    }
}