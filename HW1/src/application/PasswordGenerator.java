package application;

import java.util.Random;

/**
 * <p><b>Title:</b> Password Generator</p>
 *
 * <p><b>Description:</b> This class provides a utility method to generate strong, randomized
 * passwords that satisfy basic complexity requirements. The generated passwords:</p>
 * <ul>
 *     <li>Are exactly 15 characters long</li>
 *     <li>Contain at least one uppercase letter</li>
 *     <li>Contain at least one lowercase letter</li>
 *     <li>Contain at least one digit</li>
 *     <li>Contain at least one special character</li>
 * </ul>
 *
 * <p>Additional characters are randomly chosen from a combined pool of all character types and
 * the resulting password is shuffled to ensure randomness in character positions.</p>
 *
 * @version 1.0
 */
public class PasswordGenerator {

    /** Pool of uppercase letters */
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Pool of lowercase letters */
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";

    /** Pool of numeric digits */
    private static final String DIGITS = "0123456789";

    /** Pool of special characters */
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{};:'\",.<>/?`~";

    /** Combined pool of all characters */
    private static final String ALL_CHARS = UPPER_CASE + LOWER_CASE + DIGITS + SPECIAL_CHARS;

    /** Random number generator for selecting random characters */
    private static final Random random = new Random();

    /**
     * Generates a secure, randomized password that meets complexity requirements.
     *
     * <p>The password will always be 15 characters long and will contain at least one character
     * from each of the following categories: uppercase letters, lowercase letters, digits,
     * and special characters. The remaining characters are randomly selected from the combined
     * character pool and the result is shuffled to avoid predictable patterns.</p>
     *
     * @return A randomized, 15-character password string
     */
    public static String generatePassword() {
        StringBuilder password = new StringBuilder(15);

        // Ensure at least one character from each pool
        password.append(getRandomChar(UPPER_CASE));
        password.append(getRandomChar(LOWER_CASE));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL_CHARS));

        // Fill the remaining characters with random characters from all pools
        for (int i = 4; i < 15; i++) {
            password.append(getRandomChar(ALL_CHARS));
        }

        // Shuffle the password to ensure randomness in character positions
        return shuffleString(password.toString());
    }

    /**
     * Selects a random character from the given character pool.
     *
     * @param charPool The string of characters to select from
     * @return A randomly selected character from the pool
     */
    private static char getRandomChar(String charPool) {
        int index = random.nextInt(charPool.length());
        return charPool.charAt(index);
    }

    /**
     * Randomly shuffles the characters in a given string.
     *
     * @param input The string to be shuffled
     * @return A new string with characters randomly shuffled
     */
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
