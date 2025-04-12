package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper;

/**
 * The {@code ModifyUserRole} class provides a UI for administrators to modify user roles.
 * <p>
 * Admins can enter a user ID and select multiple roles to assign. The system prevents
 * modifying admin users and ensures that at least one role is selected before applying changes.
 * </p>
 */
public class ModifyUserRole {
    private final DatabaseHelper database;
    private final Stage primaryStage;
    private final int adminUserId;
    private final String adminUserName;
    private final QuestionHandler qHandler;
    private final AnswerHandler aHandler;
    private final UserHandler uHandler;
    private final ReviewHandler rHandler;

    /**
     * Constructs a {@code ModifyUserRole} instance.
     *
     * @param database     the {@link DatabaseHelper} instance for database operations
     * @param primaryStage the primary {@link Stage} where the scene will be displayed
     * @param userId       the admin's user ID
     * @param userName     the admin's username
     * @param qHandler     the {@link QuestionHandler} for question-related operations
     * @param aHandler     the {@link AnswerHandler} for answer-related operations
     * @param uHandler     the {@link UserHandler} for user-related operations
     * @param rHandler     the {@link ReviewHandler} for review-related operations
     * @param user         the currently logged-in {@link User}
     */
    public ModifyUserRole(DatabaseHelper database, Stage primaryStage, int userId, String userName, 
                          QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, 
                          ReviewHandler rHandler, User user) {
        this.database = database;
        this.primaryStage = primaryStage;
        this.adminUserId = userId;
        this.adminUserName = userName;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the Modify User Role page.
     *
     * @param user the currently logged-in {@link User}
     */
    public void show(User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // Label for page title
        Label titleLabel = new Label("Modify User Role");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // TextField for entering the user ID
        TextField userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setMaxWidth(200);

        // CheckBoxes for selecting user roles
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox reviewerCheckBox = new CheckBox("Reviewer");
        CheckBox instructorCheckBox = new CheckBox("Instructor");
        CheckBox staffCheckBox = new CheckBox("Staff");

        // Container to hold role selection checkboxes
        VBox checkBoxContainer = new VBox(5, studentCheckBox, reviewerCheckBox, instructorCheckBox, staffCheckBox);
        checkBoxContainer.setStyle("-fx-alignment: center;");

        // Button to modify user role
        Button modifyButton = new Button("Modify User Role");
        modifyButton.setStyle("-fx-font-size: 14px;");

        modifyButton.setOnAction(event -> {
            try {
                int userId = Integer.parseInt(userIdField.getText());

                // Prevent modifying admin roles
                if (database.getUserRoleById(userId).equals("admin")) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Cannot Change Admin Role", 
                              "You cannot change the role of an Admin.");
                    return;
                }

                // Gather selected roles
                List<String> selectedRoles = new ArrayList<>();
                if (studentCheckBox.isSelected()) selectedRoles.add("Student");
                if (reviewerCheckBox.isSelected()) selectedRoles.add("Reviewer");
                if (instructorCheckBox.isSelected()) selectedRoles.add("Instructor");
                if (staffCheckBox.isSelected()) selectedRoles.add("Staff");

                if (selectedRoles.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No Role Selected", 
                              "Please select at least one role.");
                    return;
                }

                // Modify user role in the database
                String roles = String.join(",", selectedRoles);
                boolean isModified = database.changeUserRole(userId, roles);

                if (!isModified) {
                    showAlert(Alert.AlertType.ERROR, "Error", "User Not Found", 
                              "User ID: " + userId + " not found.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User Role Modified", 
                              "The user with ID " + userId + " has been assigned role(s): " + roles);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", 
                          "Please enter a valid numeric User ID.");
            }
        });

        // Button to return to Admin Home Page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        backButton.setOnAction(event -> {
            AdminHomePage adminHomePage = new AdminHomePage(adminUserId, adminUserName, qHandler, aHandler, uHandler, rHandler, user);
            adminHomePage.show(primaryStage, user);
        });

        // Add elements to layout
        layout.getChildren().addAll(titleLabel, userIdField, checkBoxContainer, modifyButton, backButton);

        // Create scene and set to stage
        Scene modifyUserScene = new Scene(layout, 800, 500);
        primaryStage.setScene(modifyUserScene);
        primaryStage.setTitle("Modify User Role");
    }

    /**
     * Displays an alert message.
     *
     * @param type    the {@link Alert.AlertType} (e.g., ERROR, INFORMATION)
     * @param title   the title of the alert dialog
     * @param header  the header text of the alert
     * @param content the content message of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
