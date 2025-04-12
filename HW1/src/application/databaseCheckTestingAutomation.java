package application;

import java.sql.SQLException;

import databasePart1.*;

/*******
 * <p> Title: UsernameTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00 2022-02-25 A set of semi-automated test cases
 * @version 2.00 2024-09-22 Updated for use at ASU
 * 
 */
public class databaseCheckTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/**
	 * The main method serves as the entry point for the test automation.
	 * It displays a header, executes a series of test cases for username validation,
	 * and prints a summary of the test results.
	 *
	 * @param args Command-line arguments (not used).
	 */
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		/************** Start of the test cases **************/
		
		performTestCase(1, "Aa!15678", 2, true); 
		performTestCase(2, "adfa2", 5, true); 
		performTestCase(3, "A_dddd", 4, true); 
		
		/************** Test cases semi-automation report footer **************/
		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}
	
	/**
	 * Executes an individual test case for username validation.
	 * <p>
	 * The method sets up the test input parameters, registers test users via
	 * {@link DatabaseHelper}, retrieves the generated user ID, and then compares it
	 * against the expected user ID to determine whether the test passed or failed.
	 * </p>
	 *
	 * @param testCase       The test case number.
	 * @param username       The username to be validated.
	 * @param expecteduserid The expected user ID for the username.
	 * @param expectedPass   {@code true} if the test is expected to pass, {@code false} otherwise.
	 */
	private static void performTestCase(int testCase, String username, int expecteduserid, boolean expectedPass) {
		/************** Display an individual test case header **************/
		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + username + "\"");
		System.out.println("______________");
		System.out.println("\nFinite state machine execution trace:");
		
		DatabaseHelper databaseHelper = new DatabaseHelper();
		try {
			databaseHelper.connectToDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Register users; the expected user is registered last to produce the expected user ID.
		for (int i = 1; i <= expecteduserid; i++) {
			System.out.println("c1");
			System.out.println(expecteduserid);
			if (i == expecteduserid) {
				User user = new User(username, "test", "student");
				try {
					databaseHelper.register(user);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("c2");
				User user = new User(String.format("test-%d", i), "test", "student");
				try {
					databaseHelper.register(user);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(username);
		int output = databaseHelper.getUserIdByUsername(username);
		databaseHelper.closeConnection();
		System.out.println(output);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the output does not match the expected user ID, determine test result based on expectation.
		if (output != expecteduserid) {
			// If the test case expected the test to pass then this is a failure.
			if (expectedPass) {
				System.out.println("***Failure*** The username <" + username + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + output);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success.
			else {			
				System.out.println("***Success*** The username <" + username + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + output);
				numPassed++;
			}
		} else {	
			// If the output matches expected value.
			if (expectedPass) {	
				System.out.println("***Success*** The username <" + username + 
						"> is valid, so this is a pass!");
				numPassed++;
			} else {
				System.out.println("***Failure*** The username <" + username + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}
}
