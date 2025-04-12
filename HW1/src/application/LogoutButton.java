package application;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * The {@code LogoutButton} class creates a reusable logout button.
 * <p>
 * When clicked, the button logs the user out and navigates back to the login selection page.
 * </p>
 */
public class LogoutButton {
    private final Button button;

    /**
     * Constructs a {@code LogoutButton} with the specified database helper, handlers, and primary stage.
     * <p>
     * When the button is clicked, it prints a log message and navigates the user to the
     * {@link SetupLoginSelectionPage} for login selection.
     * </p>
     *
     * @param databaseHelper the {@link DatabaseHelper} instance used for database interactions
     * @param qHandler       the {@link QuestionHandler} for question-related operations
     * @param aHandler       the {@link AnswerHandler} for answer-related operations
     * @param uHandler       the {@link UserHandler} for user-related operations
     * @param rHandler       the {@link ReviewHandler} for review-related operations
     * @param primaryStage   the primary {@link Stage} where the scene will be displayed
     */
    public LogoutButton(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler, Stage primaryStage) {
        button = new Button("Logout");
        button.setOnAction(a -> {
            System.out.println("Logging out and returning to SetupLoginSelectionPage.");
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        });
    }

    /**
     * Returns the logout button.
     *
     * @return the {@link Button} representing the logout button
     */
    public Button getButton() {
        return button;
    }
}
