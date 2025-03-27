package application;


public class PasswordEvaluator {
	/**
	 * <p> Title: Directed Graph-translated Password Assessor. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Directed Graph 
	 * diagram into an executable Java program using the Password Evaluator Directed Graph. 
	 * The code detailed design is based on a while loop with a cascade of if statements</p>
	 * 
	 * <p> Copyright: Lynn Robert Carter © 2022 </p>
	 * 
	 * 
	 * @version 0.00		2018-02-22	Initial baseline 
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	public static boolean otherChar = false;            
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	
	/**********
	 * This is a utility method for getting the suffix of a number
	 * 
	 * @param input		The input integer for grabbing suffix.
	 * @return			An output string that is the relevant suffix for the number.
	 */
	private static String numberSuffix(int input) {
		if(input == 1) {
			return "st";
		}
		else if(input == 2) {
			return "nd";
		}
		else if(input == 3) {
			return "rd";
		}
		else {
			return "th";
		}
	}
	

	/**********
	 * This method is a mechanical transformation of a Directed Graph diagram into a Java
	 * method.
	 * 
	 * @param input		The input string for directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a help description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) return "The password is empty!";
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		passwordInput = input;				// Save a copy of the input
		foundUpperCase = false;				// Reset the Boolean flag
		foundLowerCase = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundSpecialChar = false;			// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundLongEnough = false;           // Reset the Boolean flag
		otherChar = false;                 //Reset the otherChar flag
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition
		while (running) {
			// The cascading if statement sequentially tries the current character against all of the
			// valid transitions
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
				otherChar = true; //This means that the inputed character is unrecognized and is not a special character, numeric, or alphabetic
			}
			if (currentCharNdx >= 7) {
				foundLongEnough = true;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
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
			otherCharSuffix = numberSuffix(otherIndex); //retrieves the suffix for the index
			errMessage += String.format("%d%s character is an invalid character.\n", otherIndex, otherCharSuffix); //Shows an error message containing the position at which an invalid character is at
		}
			
		if (errMessage == "")
			return "";
		
		passwordIndexofError = currentCharNdx;
		return errMessage;

	}
	
}
