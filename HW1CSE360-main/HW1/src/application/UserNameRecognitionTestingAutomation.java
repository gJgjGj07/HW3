package application;

/*******
 * <p> Title: UsernameTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00	2022-02-25 A set of semi-automated test cases
 * @version 2.00	2024-09-22 Updated for use at ASU
 * 
 */
public class UserNameRecognitionTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/*
	 * This mainline displays a header to the console, performs a sequence of
	 * test cases, and then displays a footer with a summary of the results
	 */
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		/************** Start of the test cases **************/
		
		performTestCase(1, "Aa!15678", false); //exclusive char
		
		performTestCase(2, "Aa1_t-6d.78", true); //valid username
		
		performTestCase(3, "", false); //blank
		
		performTestCase(4, "da", false); //short
		
		performTestCase(5, "Aa156sgfsdgsdfsdasjjdjjjjjjjjjjjjjjjj78", false); //long
		
		performTestCase(6, "_Aa5678", false); //start
		
		performTestCase(7, "Aa.-_5678", false); //special
		

		
		
		/************** End of the test cases **************/
		
		/************** Test cases semi-automation report footer **************/
		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: "+ numPassed);
		System.out.println("Number of tests failed: "+ numFailed);
	}
	
	/*
	 * This method sets up the input value for the test from the input parameters,
	 * displays test execution information, invokes precisely the same recognizer
	 * that the interactive JavaFX mainline uses, interprets the returned value,
	 * and displays the interpreted result.
	 */
	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		/************** Display an individual test case header **************/
		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("______________");
		System.out.println("\nFinite state machine execution trace:");
		
		/************** Call the recognizer to process the input **************/
		String resultText= UserNameRecognizer.checkForValidUserName(inputText);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}
		
		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The username <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The username <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
		displayEvaluation();
	}
	
	private static void displayEvaluation() {

		if (!(UserNameRecognizer.blank)) {
			System.out.println("Username contains input - Satisfied");
		}
		else {
			System.out.println("Username contains input - Not Satisfied");
		}
		
		if (!(UserNameRecognizer.start)) {
			System.out.println("Username must start with A-Z, a-z - Satisfied");
		}
		else {
			System.out.println("Username must start with A-Z, a-z - Not Satisfied");
		}
		
		
		if (!(UserNameRecognizer.tshort)) {
				System.out.println("Username must be at least 4 characters - Satisfied");
		}
		else {
				System.out.println("Username must be at least 4 characters - Not Satisfied");
		}
		if (!(UserNameRecognizer.tlong)) {
				System.out.println("Username must have no more than 16 characters - Satisfied");
		}
		else {
				System.out.println("Username must have no more than 16 characters - Not Satisfied");
		}
		if (!(UserNameRecognizer.exclusive)) {
				System.out.println("Username may contain only the characters A-Z, a-z, 0-9 - Satisfied");
		}
		else {
				System.out.println("Username may contain only the characters A-Z, a-z, 0-9 - Not Satisfied");
		}
			
		if (!(UserNameRecognizer.special)) {
			System.out.println("Special character's must be followed by A-Z, a-z, 0-9 - Satisfied");
		}
		else {
			System.out.println("Special character's must be followed by A-Z, a-z, 0-9 - Not Satisfied");
		}
		
		
	}
}
