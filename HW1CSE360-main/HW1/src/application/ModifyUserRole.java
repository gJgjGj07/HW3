package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

class ModifyUserRole{
	DatabaseHelper database= null;
	Stage primaryStage = null;
	private int adminUserId;
	private String adminUserName;
	private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;
	public ModifyUserRole(DatabaseHelper database, Stage primaryStage, int userId, String userName, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, User user) {
		this.database = database;
		this.primaryStage = primaryStage;
		adminUserName = userName;
		adminUserId = userId;
		this.qHandler = qHandler;
		this.aHandler = aHandler;
		this.uHandler = uHandler;
				
	}
	public void show(User user) {
	    VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

	    // Label to display the title of the page
	    Label titleLabel = new Label("Modify User Role");
	    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // TextField for the admin to enter the userId
	    TextField userIdField = new TextField();
	    userIdField.setPromptText("Enter User ID");
	    userIdField.setMaxWidth(200);
	    
	    // Create CheckBoxes for multiple role selection
	    CheckBox studentCheckBox = new CheckBox("Student");
	    CheckBox reviewerCheckBox = new CheckBox("Reviewer");
	    CheckBox instructorCheckBox = new CheckBox("Instructor");
	    CheckBox staffCheckBox = new CheckBox("Staff");
	    
	    // Container to hold the checkboxes
	    VBox checkBoxContainer = new VBox(5, studentCheckBox, reviewerCheckBox, instructorCheckBox, staffCheckBox);
	    checkBoxContainer.setStyle("-fx-alignment: center;");

	    // Button to modify user role
	    Button modifyButton = new Button("Modify User Role");
	    modifyButton.setStyle("-fx-font-size: 14px;");
	    
	    modifyButton.setOnAction(event -> {
	        try {
	            // Get the userId from the text field
	            int userId = Integer.parseInt(userIdField.getText());
	            
	            // Check if the user's current role is admin
	            if (database.getUserRoleById(userId).equals("admin")) {
	                // Show an error message if the user is an admin
	                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                errorAlert.setTitle("Error");
	                errorAlert.setHeaderText("Cannot Change Admin Role");
	                errorAlert.setContentText("You cannot change the role of an Admin.");
	                errorAlert.showAndWait();
	                return;
	            } else {
	                // Gather the selected roles from the checkboxes
	                List<String> selectedRoles = new ArrayList<>();
	                if (studentCheckBox.isSelected()) selectedRoles.add("Student");
	                if (reviewerCheckBox.isSelected()) selectedRoles.add("Reviewer");
	                if (instructorCheckBox.isSelected()) selectedRoles.add("Instructor");
	                if (staffCheckBox.isSelected()) selectedRoles.add("Staff");
	                
	                if (selectedRoles.isEmpty()) {
	                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                    errorAlert.setTitle("Error");
	                    errorAlert.setHeaderText("No Role Selected");
	                    errorAlert.setContentText("Please select at least one role.");
	                    errorAlert.showAndWait();
	                    return;
	                }
	                
	                // Join the selected roles into a comma-separated string
	                String roles = String.join(",", selectedRoles);
	                
	                // Attempt to change the user's role in the database
	                boolean isMod = database.changeUserRole(userId, roles);
	                if (!isMod) {
	                    // Error message if the user id is not found
	                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
	                    notFoundAlert.setTitle("Error");
	                    notFoundAlert.setHeaderText("User Not Found");
	                    notFoundAlert.setContentText("User id: " + userId + " not found");
	                    notFoundAlert.showAndWait();
	                    return;
	                } else {
	                    // Show a success message
	                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
	                    successAlert.setTitle("Success");
	                    successAlert.setHeaderText("User Role Modified");
	                    successAlert.setContentText("The user with ID " + userId + " has been modified with role(s): " + roles);
	                    successAlert.showAndWait();
	                }
	            } 
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
	    layout.getChildren().addAll(titleLabel, userIdField, checkBoxContainer, modifyButton, backButton);

	    // Create the scene
	    Scene modifyUserScene = new Scene(layout, 800, 500);

	    // Set the scene to the primary stage
	    primaryStage.setScene(modifyUserScene);
	    primaryStage.setTitle("Modify User Role");
	}

}