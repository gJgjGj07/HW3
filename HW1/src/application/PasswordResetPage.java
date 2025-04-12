package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;

/**
 * <p><b>Class:</b> PasswordResetPage</p>
 *
 * <p><b>Description:</b> This class represents a password reset UI window in a JavaFX application. 
 * It allows a user to input a new password, validates it, and updates the password in the database 
 * using the provided {@link DatabaseHelper}. After a successful reset, it optionally calls a 
 * callback (typically to redirect the user back to a login screen).</p>
 *
 * @author 
 * @version 1.0
 */
public class PasswordResetPage {

    /** JavaFX stage for displaying the reset password window. */
    private Stage stage;

    /** Reference to the helper class for interacting with the database. */
    private DatabaseHelper databaseHelper;

    /** User ID of the user whose password is being reset. */
    private int userId;

    /** Callback function to execute after a successful password reset (e.g., return to login). */
    private Runnable onPasswordReset;

    /**
     * Constructs a new {@code PasswordResetPage}.
     *
     * @param databaseHelper Reference to the {@code DatabaseHelper} for DB operations.
     * @param userId         The user ID whose password is being reset.
     */
    public PasswordResetPage(DatabaseHelper databaseHelper, int userId) {
        this.databaseHelper = databaseHelper;
        this.userId = userId;
        this.stage = new Stage();
    }

    /**
     * Displays the password reset window.
     *
     * @param primaryStage The main JavaFX stage (not used internally but can be passed for ownership).
     */
    public void show(Stage primaryStage) {
        Label instructionLabel = new Label("Enter your new password:");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button resetButton = new Button("Reset Password");
        Label messageLabel = new Label();

        resetButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();

            if (newPassword.isEmpty()) {
                messageLabel.setText("Password cannot be empty.");
                return;
            }

            // Update the user's password in the database
            boolean success = databaseHelper.setPassword(userId, newPassword);
            if (success) {
                messageLabel.setText("Password reset successfully!");
                databaseHelper.setForgetPassword(userId); // Reset forgotPassword flag

                // Redirect back to login page
                if (onPasswordReset != null) {
                    onPasswordReset.run();
                    stage.close();
                }
            } else {
                messageLabel.setText("Failed to reset password.");
            }
        });

        VBox layout = new VBox(10, instructionLabel, newPasswordField, resetButton, messageLabel);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Reset Password");
        stage.show();
    }

    /**
     * Sets a callback to be executed after the password is successfully reset.
     *
     * @param onPasswordReset A {@code Runnable} callback to invoke (e.g., redirect to login).
     */
    public void setOnPasswordReset(Runnable onPasswordReset) {
        this.onPasswordReset = onPasswordReset;
    }
}
