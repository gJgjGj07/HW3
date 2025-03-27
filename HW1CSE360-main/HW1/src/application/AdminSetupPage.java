package application;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.geometry.Insets;
import java.sql.SQLException;

import databasePart1.*;

import javafx.scene.layout.HBox;


/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;

    public AdminSetupPage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
       //label to display error messages related to the username constraints
        Label userError = new Label();
        userError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //label to display the error messages related to the password constraints
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new FirstPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
        	});
        
        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
            // Resets the values of the fields, so the try block does not process the previous user input
            userError.setText("");
            passwordError.setText("");
            
            String userName = "";
            String password = "";
            
            userName = userNameField.getText();
            password = passwordField.getText();

            try {
            	
            	//If the username field produces an error, display the error
                if (UserNameRecognizer.checkForValidUserName(userName).length() > 0){
                	userError.setText("Invalid Username:\n" + UserNameRecognizer.checkForValidUserName(userNameField.getText()));
            	}
                //if the password field produces an error, display the error
                if (PasswordEvaluator.evaluatePassword(password).length() > 0) {
            		passwordError.setText("Invalid Password:\n" + PasswordEvaluator.evaluatePassword(passwordField.getText()));
            	}
                // If no errors, proceed
                if (userError.getText().isEmpty() && passwordError.getText().isEmpty()) { 
                    // Register admin in the database
                    User user = new User(userName, password, "admin");
                    databaseHelper.register(user);
                    System.out.println("Administrator setup completed.");
               
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Security Notice");
                    alert.setHeaderText("Re-login Required");
                    alert.setContentText("As a security protocol, please log in again.");
                    alert.showAndWait();

                    new UserLoginPage(databaseHelper, aHandler, qHandler, uHandler).show(primaryStage);
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

     // Updated styling to clean up error messages and center beneath respective boxes
        userError.setMaxWidth(400);
        passwordError.setMaxWidth(250);

        HBox userErrorBox = new HBox(userError);
        userErrorBox.setAlignment(Pos.CENTER_LEFT);
        userErrorBox.setMaxWidth(250);

        HBox passwordErrorBox = new HBox(passwordError);
        passwordErrorBox.setAlignment(Pos.CENTER_LEFT);
        passwordErrorBox.setMaxWidth(250);
        
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20, 20, 20, 20));
        
        //padding for the top 
        VBox.setMargin(userNameField, new Insets(45, 0, 0, 0));

        VBox layout = new VBox(10, 
            userNameField, 
            userErrorBox, 
            passwordField, 
            passwordErrorBox, 
            setupButton
        );
        layout.setAlignment(Pos.TOP_CENTER); 
        layout.setStyle("-fx-padding: 20;");
        
        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(layout);
        rootLayout.setBottom(backButtonBox);

        primaryStage.setScene(new Scene(rootLayout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
