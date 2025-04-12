package application;

/**
 * <p><b>Title:</b> Directed Graph-translated Password Assessor</p>
 *
 * <p><b>Description:</b> A demonstration of the mechanical translation of a Directed Graph
 * diagram into an executable Java program using the Password Evaluator Directed Graph.
 * The core logic is implemented using a `while` loop with a cascading set of `if` statements
 * to evaluate the characteristics of a password input.</p>
 *
 * <p><b>Copyright:</b> Lynn Robert Carter © 2022</p>
 *
 * @version 0.00, 2018-02-22 — Initial baseline
 */
public class PasswordEvaluator {

	// =============================================================================================
	// Result attributes for GUI integration — used to display error messages, input context, and
	// highlight error positions visually to enhance the user experience.
	// =============================================================================================

	/** Holds the error message after evaluating the password. */
	public static String passwordErrorMessage = "";

	/** Stores the original password input being evaluated. */
	public static String passwordInput = "";

	/** Index of the character where the first error was detected. */
	public static int passwordIndexofError = -1;

	/** Flag indicating whether the password contains at least one uppercase letter. */
	public static boolean foundUpperCase = false;

	/** Flag indicating whether the password contains at least one lowercase letter. */
	public static boolean foundLowerCase = false;

	/** Flag indicating whether the password contains at least one numeric digit. */
	public static boolean foundNumericDigit = false;

	/** Flag indicating whether the password contains at least one special character. */
	public static boolean foundSpecialChar = false;

	/** Flag indicating whether the password meets the minimum length requirement. */
	public static boolean foundLongEnough = false;

	/** Flag indicating whether the password contains an invalid character. */
	public static boolean otherChar = false;

	/** Internal copy of the input string. */
	private static String inputLine = "";

	/** Current character being evaluated in the input string. */
	private static char currentChar;

	/** Index of the current character in the input string. */
	private static int currentCharNdx;

	/** Control flag for the finite state machine (FSM) execution loop. */
	private static boolean running;

	/**
	 * Returns the appropriate ordinal suffix ("st", "nd", "rd", "th") for a given integer.
	 *
	 * @param input The integer to evaluate.
	 * @return A string containing the corresponding ordinal suffix.
	 */
	private static String numberSuffix(int input) {
		if (input == 1) return "st";
		else if (input == 2) return "nd";
		else if (input == 3) return "rd";
		else return "th";
	}

	/**
	 * Evaluates the given password string to check whether it satisfies the following requirements:
	 * <ul>
	 *     <li>Contains at least one uppercase letter</li>
	 *     <li>Contains at least one lowercase letter</li>
	 *     <li>Contains at least one numeric digit</li>
	 *     <li>Contains at least one special character</li>
	 *     <li>Is at least 8 characters long</li>
	 *     <li>Does not contain any invalid characters</li>
	 * </ul>
	 *
	 * <p>This method performs a mechanical transformation of a Directed Graph diagram into
	 * an executable Java method and can be used in a GUI application to provide detailed feedback
	 * on password strength or errors.</p>
	 *
	 * @param input The password string to evaluate.
	 * @return An error message string if any requirements are not met. Returns an empty string if
	 *         the password is valid.
	 */
	public static String evaluatePassword(String input) {
		passwordErrorMessage = "";
		passwordIndexofError = 0;
		inputLine = input;
		currentCharNdx = 0;

		if (input.length() <= 0)
			return "The password is empty!";

		currentChar = input.charAt(0);
		passwordInput = input;

		// Reset flags
		foundUpperCase = false;
		foundLowerCase = false;
		foundNumericDigit = false;
		foundSpecialChar = false;
		foundLongEnough = false;
		otherChar = false;
		running = true;

		// FSM loop to process each character
		while (running) {
			if (currentChar >= 'A' && currentChar <= 'Z') {
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
				foundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				otherChar = true;
			}

			if (currentCharNdx >= 7) {
				foundLongEnough = true;
			}

			// Move to next character
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length()) {
				running = false;
			} else {
				currentChar = input.charAt(currentCharNdx);
			}
		}

		int otherIndex = passwordIndexofError + 1;
		String otherCharSuffix = "";
		String errMessage = "";

		if (!foundUpperCase)
			errMessage += "Must contain a uppercase letter.\n";
		if (!foundLowerCase)
			errMessage += "Must contain a lowercase letter.\n";
		if (!foundNumericDigit)
			errMessage += "Must contain a number.\n";
		if (!foundSpecialChar)
			errMessage += "Must contain a special character.\n";
		if (!foundLongEnough)
			errMessage += "Must be at least 8 characters.\n";

		if (otherChar) {
			otherCharSuffix = numberSuffix(otherIndex);
			errMessage += String.format("%d%s character is an invalid character.\n", otherIndex, otherCharSuffix);
		}

		if (errMessage.equals(""))
			return "";

		passwordIndexofError = currentCharNdx;
		return errMessage;
	}
}
