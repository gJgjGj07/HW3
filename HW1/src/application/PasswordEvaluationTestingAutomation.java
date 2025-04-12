package application;

/*******
 * <p> Title: PasswordEvaluationTestingAutomation Class. </p>
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
public class PasswordEvaluationTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/**
	 * <p><b>main</b></p>
	 * 
	 * <p>This method initiates the semi-automated test process for evaluating passwords
	 * using the PasswordEvaluator. It runs a series of predefined test cases to 
	 * determine if the password validation logic works correctly and prints results 
	 * to the console including the total passed and failed tests.</p>
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		// Run a series of test cases for password validation
		performTestCase(1, "Aa!15678", true);
		performTestCase(2, "A!", false); // too short, no lowercase or number
		performTestCase(3, "", false); // blank
		performTestCase(4, "Helloworld123", false); // no special char
		performTestCase(5, "helloworld!80", false); // no uppercase
		performTestCase(6, "hellowDrld!", false); // no number
		performTestCase(7, "Forty Eighty 89!bro!", false); // invalid chars
		performTestCase(8, "FortyEighty89!bro!", true); // valid

		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/**
	 * <p><b>performTestCase</b></p>
	 * 
	 * <p>Executes a single password validation test case. It uses the 
	 * PasswordEvaluator to assess the given password, compares the result with the 
	 * expected outcome, and prints detailed diagnostics including evaluation criteria.</p>
	 * 
	 * @param testCase     The test case number for identification.
	 * @param inputText    The password to be evaluated.
	 * @param expectedPass Indicates whether the password is expected to be valid or not.
	 */
	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("______________");
		System.out.println("\nFinite state machine execution trace:");

		String resultText = PasswordEvaluator.evaluatePassword(inputText);
		System.out.println();

		if (!resultText.equals("")) {
			if (expectedPass) {
				System.out.println("***Failure*** The password <" + inputText + "> is invalid." +
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			} else {
				System.out.println("***Success*** The password <" + inputText + "> is invalid." +
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		} else {
			if (expectedPass) {
				System.out.println("***Success*** The password <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			} else {
				System.out.println("***Failure*** The password <" + inputText + 
						"> was judged as valid" +
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
		displayEvaluation();
	}

	/**
	 * <p><b>displayEvaluation</b></p>
	 * 
	 * <p>Prints out the password evaluation criteria results based on flags 
	 * set by the PasswordEvaluator. It shows whether each security requirement 
	 * (uppercase, lowercase, digit, special character, length, and invalid characters) 
	 * has been met for the last evaluated password.</p>
	 */
	private static void displayEvaluation() {
		if (PasswordEvaluator.foundUpperCase)
			System.out.println("At least one upper case letter - Satisfied");
		else
			System.out.println("At least one upper case letter - Not Satisfied");

		if (PasswordEvaluator.foundLowerCase)
			System.out.println("At least one lower case letter - Satisfied");
		else
			System.out.println("At least one lower case letter - Not Satisfied");

		if (PasswordEvaluator.foundNumericDigit)
			System.out.println("At least one digit - Satisfied");
		else
			System.out.println("At least one digit - Not Satisfied");

		if (PasswordEvaluator.foundSpecialChar)
			System.out.println("At least one special character - Satisfied");
		else
			System.out.println("At least one special character - Not Satisfied");

		if (PasswordEvaluator.foundLongEnough)
			System.out.println("At least 8 characters - Satisfied");
		else
			System.out.println("At least 8 characters - Not Satisfied");

		if (!PasswordEvaluator.otherChar)
			System.out.println("No invalid characters - Satisfied");
		else
			System.out.println("At least one invalid character found - Not Satisfied");
	}
}
