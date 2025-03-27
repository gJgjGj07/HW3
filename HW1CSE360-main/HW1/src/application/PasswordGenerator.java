package application;
import java.util.Random;

public class PasswordGenerator {

	    // Define the character pools for each type of character
	    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
	    private static final String DIGITS = "0123456789";
	    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{};:'\",.<>/?`~";

	    // Combine all character pools into one
	    private static final String ALL_CHARS = UPPER_CASE + LOWER_CASE + DIGITS + SPECIAL_CHARS;

	    // Random object for generating random indices
	    private static final Random random = new Random();

	    public static String generatePassword() {
	        StringBuilder password = new StringBuilder(15);

	        // Ensure at least one character from each pool
	        password.append(getRandomChar(UPPER_CASE));
	        password.append(getRandomChar(LOWER_CASE));
	        password.append(getRandomChar(DIGITS));
	        password.append(getRandomChar(SPECIAL_CHARS));

	        // Fill the remaining 11 characters with random characters from all pools
	        for (int i = 4; i < 15; i++) {
	            password.append(getRandomChar(ALL_CHARS));
	        }

	        // Shuffle the password to ensure randomness
	        return shuffleString(password.toString());
	    }

	    // Helper method to get a random character from a given string
	    private static char getRandomChar(String charPool) {
	        int index = random.nextInt(charPool.length());
	        return charPool.charAt(index);
	    }

	    // Helper method to shuffle the characters in the password
	    private static String shuffleString(String input) {
	        char[] characters = input.toCharArray();
	        for (int i = 0; i < characters.length; i++) {
	            int randomIndex = random.nextInt(characters.length);
	            char temp = characters[i];
	            characters[i] = characters[randomIndex];
	            characters[randomIndex] = temp;
	        }
	        return new String(characters);
	    }
}