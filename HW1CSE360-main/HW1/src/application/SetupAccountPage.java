package application;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler) {
        this.databaseHelper = databaseHelper;
        this.aHandler = aHandler;
        this.qHandler = qHandler;
        this.uHandler = uHandler;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //label to display error messages related to the username constraints
        Label userError = new Label();
        userError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //label to display the error messages related to the password constraints
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
        	});
        
        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	//Resets the values of the fields, so the try block does not process the previous user input
        	userError.setText("");
        	passwordError.setText("");
        	errorLabel.setText("");
        	String userName = "";
            String password = "";
            String code = "";
            
        	
        	// Retrieve user input
            userName = userNameField.getText();
            password = passwordField.getText();
            code = inviteCodeField.getText();
        
            
            try {
            	//If the username field produces an error, display the error
                if (UserNameRecognizer.checkForValidUserName(userName).length() > 0){
     
                	userError.setText("Username error:\n" + UserNameRecognizer.checkForValidUserName(userNameField.getText()));
            	
            	}
                //if the password field produces an error, display the error
                if (PasswordEvaluator.evaluatePassword(password).length() > 0) {
            
            		passwordError.setText("Password Error:\n" + PasswordEvaluator.evaluatePassword(passwordField.getText()));
            		
            	}
                //If a user with the same username inputed does exist in the database, produce an error message
                if(databaseHelper.doesUserExist(userName)) {
                	
                	errorLabel.setText("This username is already take! Please use a different username.");
                }
                //If the invitation code is not valid, produce an error message
                else if(!databaseHelper.validateInvitationCode(code)) {
                	
                	errorLabel.setText("Please enter a valid invitation code.");
                	
                }
                //If there are no errors at all in the user input, move on to the next page and register the user
                if ((errorLabel.getText().isEmpty()) && (userError.getText().isEmpty()) && (passwordError.getText().isEmpty())){
                	// Create a new user and register them in the database
                	String userRole = databaseHelper.getInvitationRole(code);
	            	User user=new User(userName, password, userRole);
	                databaseHelper.register(user);
	                
	             // Navigate to the Welcome Login Page
	                new WelcomeLoginPage(databaseHelper, userName, qHandler, aHandler, uHandler, user).show(primaryStage,user);
                }
                	
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

     // Updated styling to clean up error messages and center beneath respective boxes
        userError.setMaxWidth(250);
        passwordError.setMaxWidth(250);

        HBox userErrorBox = new HBox(userError);
        userErrorBox.setAlignment(Pos.CENTER_LEFT);
        userErrorBox.setMaxWidth(400);

        HBox passwordErrorBox = new HBox(passwordError);
        passwordErrorBox.setAlignment(Pos.CENTER_LEFT);
        passwordErrorBox.setMaxWidth(400);
        
        HBox inviteCodeErrorBox = new HBox(errorLabel);
        inviteCodeErrorBox.setAlignment(Pos.CENTER_LEFT);
        inviteCodeErrorBox.setMaxWidth(400);
        
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20, 20, 20, 20));
        
        //padding for the top 
        VBox.setMargin(userNameField, new Insets(30, 0, 0, 0));

        VBox layout = new VBox(10, 
            userNameField, 
            userErrorBox, 
            passwordField, 
            passwordErrorBox, 
            inviteCodeField,
            inviteCodeErrorBox,
            setupButton
        );
        layout.setAlignment(Pos.TOP_CENTER); 
        layout.setStyle("-fx-padding: 20;");
        
        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(layout);
        rootLayout.setBottom(backButtonBox);

        primaryStage.setScene(new Scene(rootLayout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
