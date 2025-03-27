package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * RemoveUserPage class represents the page where an admin can remove a user by entering their userId.
 */
public class RemoveUserPage {

    private DatabaseHelper dbHelper; // DatabaseHelper instance
    private Stage primaryStage; // Reference to the primary stage
    private int adminUserId;
	private String adminUserName;
	private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;

    public RemoveUserPage(DatabaseHelper dbHelper, Stage primaryStage, int userId, String userName, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, User user) {
        this.dbHelper = dbHelper;
        this.primaryStage = primaryStage;
		adminUserId = userId;
		adminUserName = userName;
		this.qHandler = qHandler;
		this.aHandler = aHandler;
		this.uHandler = uHandler;
    }


	public void show(User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // Label to display the title of the page
        Label titleLabel = new Label("Remove User");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // TextField for the admin to enter the userId
        TextField userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setMaxWidth(200);

        // Button to confirm the removal
        Button removeButton = new Button("Remove User");
        removeButton.setStyle("-fx-font-size: 14px;");

        // Set action for the remove button
        removeButton.setOnAction(event -> {
            try {
                // Get the userId from the text field
                int userId = Integer.parseInt(userIdField.getText());

                // Retrieve the role of the user
                String userRole = dbHelper.getUserRoleById(userId);

                // Check if the user is an admin
                if ("Admin".equalsIgnoreCase(userRole)) {
                    // Show an error message if the user is an admin
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Cannot Remove Admin");
                    errorAlert.setContentText("You cannot remove a user with the Admin role.");
                    errorAlert.showAndWait();
                    return; // Exit the method to prevent deletion
                }

                // Show a confirmation dialog
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirm Removal");
                confirmationDialog.setHeaderText("Are you sure you want to remove this user?");
                confirmationDialog.setContentText("User ID: " + userId);

                // Wait for the user's response
                confirmationDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // If the user confirms, remove the user, isRemoved returns false if the remove sql command does not work
                        boolean isRemoved = dbHelper.removeUser(userId);
                        if (isRemoved) {
                            // Show a success message
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText("User Removed");
                            successAlert.setContentText("The user with ID " + userId + " has been removed.");
                            successAlert.showAndWait();
                        } else {
                            // Show an error message if the user was not found
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText("User Not Found");
                            errorAlert.setContentText("The user with ID " + userId + " was not found.");
                            errorAlert.showAndWait();
                        }

                        // Redirect back to the home page
                        AdminHomePage adminHomePage = new AdminHomePage(adminUserId, adminUserName, qHandler, aHandler, uHandler, user);
                        adminHomePage.show(primaryStage, user);
                    }
                });
            } catch (NumberFormatException e) {
                // Handle invalid input (non-numeric userId)
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Invalid Input");
                errorAlert.setContentText("Please enter a valid numeric User ID.");
                errorAlert.showAndWait();
            }
        });

        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        // Set action for the back button
        backButton.setOnAction(event -> {
            AdminHomePage adminHomePage = new AdminHomePage(adminUserId, adminUserName, qHandler, aHandler, uHandler, user);
            adminHomePage.show(primaryStage, user);
        });

        // Add components to the layout
        layout.getChildren().addAll(titleLabel, userIdField, removeButton, backButton);

        // Create the scene
        Scene removeUserScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(removeUserScene);
        primaryStage.setTitle("Remove User");
    }
}