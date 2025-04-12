package application;

/**
 * <p> Title: FSM-translated UserNameRecognizer. </p>
 * 
 * <p> Description: A demonstration of the mechanical translation of a Finite State Machine 
 * diagram into an executable Java program using the UserName Recognizer. The code's 
 * detailed design is based on a while loop with a select list.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2024 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00   2024-09-13 Initial baseline derived from the Even Recognizer
 * @version 1.01   2024-09-17 Correction to address UNChar coding error, improper error
 *                             message, and improve internal documentation
 */
public class UserNameRecognizer {

	// Error and debug flags for GUI and testing support
	public static String userNameRecognizerErrorMessage = ""; // The error message text
	public static String userNameRecognizerInput = "";        // The input being processed
	public static int userNameRecognizerIndexofError = -1;    // The index of error location
	public static boolean blank = false;                      // True if the input is blank
	public static boolean start = false;                      // True if input starts with invalid char
	public static boolean tshort = false;                     // True if input is too short
	public static boolean tlong = false;                      // True if input is too long
	public static boolean exclusive = false;                  // True if contains invalid chars
	public static boolean special = false;                    // True if improper special char usage

	// FSM state variables
	private static int state = 0;
	private static int nextState = 0;
	private static boolean finalState = false;
	private static String inputLine = "";
	private static char currentChar;
	private static int currentCharNdx;
	private static boolean running;
	private static int userNameSize = 0;

	/**
	 * Moves to the next character in the input string.
	 * Sets `currentChar` to blank if end of input is reached.
	 */
	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length())
			currentChar = inputLine.charAt(currentCharNdx);
		else {
			currentChar = ' ';
			running = false;
		}
	}

	/**
	 * This method simulates a Finite State Machine (FSM) to validate a username based on a set
	 * of rules defined in a state diagram.
	 *
	 * @param input The input string representing the username to be validated
	 * @return An error message if the username is invalid, or an empty string if valid
	 */
	public static String checkForValidUserName(String input) {
		
		// Reset test flags
		blank = false;
		start = false;
		tshort = false;
		tlong = false;
		exclusive = false;
		special = false;
		
		// Handle empty input
		if (input.length() <= 0) {
			blank = true;
			userNameRecognizerIndexofError = 0;
			state = -1;
			return "The username is empty!\n";
		}
		
		// Initialize FSM variables
		state = 0;
		inputLine = input;
		currentCharNdx = 0;
		currentChar = input.charAt(0);
		userNameRecognizerInput = input;
		running = true;
		nextState = -1;
		userNameSize = 0;

		// Run FSM loop
		while (running) {
			switch (state) {
			case 0:
				if ((currentChar >= 'A' && currentChar <= 'Z') || 
					(currentChar >= 'a' && currentChar <= 'z')) {
					nextState = 1;
					userNameSize++;
				} else {
					running = false;
				}
				break;

			case 1:
				if ((currentChar >= 'A' && currentChar <= 'Z') || 
					(currentChar >= 'a' && currentChar <= 'z') || 
					(currentChar >= '0' && currentChar <= '9')) {
					nextState = 1;
					userNameSize++;
				} else if (currentChar == '.' || currentChar == '-' || currentChar == '_') {
					nextState = 2;
					userNameSize++;
				} else {
					running = false;
				}
				if (userNameSize > 16)
					running = false;
				break;

			case 2:
				if ((currentChar >= 'A' && currentChar <= 'Z') || 
					(currentChar >= 'a' && currentChar <= 'z') || 
					(currentChar >= '0' && currentChar <= '9')) {
					nextState = 1;
					userNameSize++;
				} else {
					running = false;
				}
				if (userNameSize > 16)
					running = false;
				break;
			}

			if (running) {
				moveToNextCharacter();
				state = nextState;
				if (state == 1) finalState = true;
				nextState = -1;
			}
		}

		// Error handling and result generation
		userNameRecognizerIndexofError = currentCharNdx;
		userNameRecognizerErrorMessage = "";

		switch (state) {
		case 0:
			start = true;
			userNameRecognizerErrorMessage += "Must start with A-Z, a-z.\n";
			return userNameRecognizerErrorMessage;

		case 1:
			if (userNameSize < 4) {
				tshort = true;
				userNameRecognizerErrorMessage += "Must be at least 4 characters.\n";
				return userNameRecognizerErrorMessage;
			} else if (userNameSize > 16) {
				tlong = true;
				userNameRecognizerErrorMessage += "Must have no more than 16 characters.\n";
				return userNameRecognizerErrorMessage;
			} else if (currentCharNdx < input.length()) {
				exclusive = true;
				userNameRecognizerErrorMessage += "May contain only the characters A-Z, a-z, 0-9.\n";
				return userNameRecognizerErrorMessage;
			} else {
				userNameRecognizerIndexofError = -1;
				userNameRecognizerErrorMessage = "";
				return userNameRecognizerErrorMessage;
			}

		case 2:
			special = true;
			userNameRecognizerErrorMessage += "Special character must be followed by A-Z, a-z, 0-9.\n";
			return userNameRecognizerErrorMessage;

		default:
			return "";
		}
	}
}
