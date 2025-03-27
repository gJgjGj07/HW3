package application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;

public class PasswordResetPage {
    private Stage stage;
    private DatabaseHelper databaseHelper;
    private int userId;
    private Runnable onPasswordReset; // Callback for returning to login page

    public PasswordResetPage(DatabaseHelper databaseHelper, int userId) {
        this.databaseHelper = databaseHelper;
        this.userId = userId;
        this.stage = new Stage();
    }

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

    // Sets the callback to return to login after password reset
    public void setOnPasswordReset(Runnable onPasswordReset) {
        this.onPasswordReset = onPasswordReset;
    }
}
