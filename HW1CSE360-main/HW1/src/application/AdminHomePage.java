package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.*;



/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	
	private int userId;
	private String userName;
	private DatabaseHelper dbHelper; // a DatabaseHelper object is needed because the admin needs to be able to modify the database
	Stage primaryStage = null;
	private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;

	
	//A constructor which initializes the database
    public AdminHomePage(int userId, String userName, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler,  User user) {
        this.userId= userId;
    	this.userName = userName;
    	dbHelper = new DatabaseHelper();  
        try {
            dbHelper.connectToDatabase(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
    }
    private void showSendTempPassword(Stage primaryStage, User user) {
    	VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");
        
        // TextField for the admin to enter the userId
        TextField userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setMaxWidth(200);
        
        // Button to send generated password
        Button sendPasswordButton = new Button("Send Temporary Password");
        sendPasswordButton.setStyle("-fx-font-size: 14px;");
        
        sendPasswordButton.setOnAction( event ->{
        	// Get the userId from the text field
            int sendUserId = Integer.parseInt(userIdField.getText());
            dbHelper.setForgetPassword(sendUserId);
            dbHelper.deleteNotificationLine(userId, "User " + sendUserId + " forgot their password. Send them a temporary one.");
            String password = PasswordGenerator.generatePassword();
            
            dbHelper.addNotificationToUser("Here Is Your Temporary Password: " + password, sendUserId);
            if (dbHelper.setPassword(sendUserId, password)) {
            	Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Password Sent");
                successAlert.setContentText("The user with ID " + sendUserId + " can now reset their password");
                successAlert.showAndWait();
            }
            else {
            	// Show an error message if the user was not found
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("User Not Found");
                errorAlert.setContentText("The user with ID " + sendUserId + " was not found.");
                errorAlert.showAndWait();
            }
         // Redirect back to the home page
            AdminHomePage adminHomePage = new AdminHomePage(userId, userName, qHandler, aHandler, uHandler, user);
            adminHomePage.show(primaryStage, user);
        });
        
        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        
        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });
    	
        // Add components to the layout
        layout.getChildren().addAll(userIdField, sendPasswordButton, backButton);

        // Create the scene for the user information page
        Scene tempPasswordScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(tempPasswordScene);
        primaryStage.setTitle("Send Temporary Password");
        primaryStage.setTitle("Enter User ID To Change Password");
        
    }
    private void showNotifications(Stage primaryStage, User user) {
    	VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");
        
        //Text that displays all the notifications that admin has
        TextArea notificationsArea = new TextArea();
        notificationsArea.setEditable(false); // Make it read-only
        notificationsArea.setPrefSize(600, 300);
        
        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");
        
        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });
        
        try {
			dbHelper.displayNotifications(notificationsArea, userId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Add components to the layout
        layout.getChildren().addAll(notificationsArea, backButton);

        // Create the scene for the user information page
        Scene notificationsScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(notificationsScene);
        primaryStage.setTitle("Notifications");
    }
    private void showUserInfoPage(Stage primaryStage, User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        TextArea userInfoArea = new TextArea();
        userInfoArea.setEditable(false); // Make it read-only
        userInfoArea.setPrefSize(600, 300); 

        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });

        // Fetch and display user information in the TextArea
        try {
            dbHelper.displayAllUsers(userInfoArea); // Populate the TextArea with user data
        } catch (Exception e) {
            e.printStackTrace();
            userInfoArea.setText("Error fetching user data.");
        }

        // Add components to the layout
        layout.getChildren().addAll(userInfoArea, backButton);

        // Create the scene for the user information page
        Scene userInfoScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(userInfoScene);
        primaryStage.setTitle("User Information");
    }
    
    public void show(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;

        // Main content container
        VBox content = new VBox(10);
        content.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // Back button setup
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(dbHelper, userName, qHandler, aHandler, uHandler, user).show(primaryStage, user);
        });
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20));

        // Create root layout with BorderPane
        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(content);  // Main content in center
        rootLayout.setBottom(backButtonBox);  // Back button at bottom

        // Add components to content
        Label adminLabel = new Label("Hello, " + userName + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button viewUsersButton = new Button("View All Users");
        Button removeUserButton = new Button("Remove User");
        Button modifyUserRoleButton = new Button("Modify User Role");
        Button checkNotificationButton = new Button("Notifications (" + dbHelper.getNumNotifications(userId) + ")");
        Button sendTempPassButton = new Button("Send Temporary Password To User");
        Button logoutButton = new Button("Logout");

        // Style buttons
        String buttonStyle = "-fx-font-size: 14px;";
        viewUsersButton.setStyle(buttonStyle);
        removeUserButton.setStyle(buttonStyle);
        modifyUserRoleButton.setStyle(buttonStyle);
        checkNotificationButton.setStyle(buttonStyle);
        sendTempPassButton.setStyle(buttonStyle);

        // Add action handlers
        viewUsersButton.setOnAction(event -> showUserInfoPage(primaryStage, user));
        removeUserButton.setOnAction(event -> new RemoveUserPage(dbHelper, primaryStage, userId, userName, qHandler, aHandler, uHandler, user).show(user));
        modifyUserRoleButton.setOnAction(event -> new ModifyUserRole(dbHelper, primaryStage, userId, userName, qHandler, aHandler, uHandler, user).show(user));
        checkNotificationButton.setOnAction(event -> showNotifications(primaryStage, user));
        sendTempPassButton.setOnAction(event -> showSendTempPassword(primaryStage, user));
        logoutButton.setOnAction(event -> new SetupLoginSelectionPage(dbHelper, qHandler, aHandler, uHandler).show(primaryStage));

        // Add components to content VBox
        content.getChildren().addAll(
            adminLabel,
            viewUsersButton,
            removeUserButton,
            modifyUserRoleButton,
            sendTempPassButton,
            checkNotificationButton,
            logoutButton
        );

        // Set up scene
        primaryStage.setScene(new Scene(rootLayout, 800, 500));
        primaryStage.setTitle("Admin Page");
    }

}