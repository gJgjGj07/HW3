package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;
    private final AnswerHandler aHandler;
    private final QuestionHandler qHandler;
    private final UserHandler uHandler;
    private final ReviewHandler rHandler;

    /**
     * Constructs a UserLoginPage with the specified database helper and handlers.
     *
     * @param databaseHelper the DatabaseHelper instance for database operations
     * @param aHandler       the AnswerHandler for handling answers
     * @param qHandler       the QuestionHandler for handling questions
     * @param uHandler       the UserHandler for handling user operations
     * @param rHandler       the ReviewHandler for handling review operations
     */
    public UserLoginPage(DatabaseHelper databaseHelper, AnswerHandler aHandler, QuestionHandler qHandler, UserHandler uHandler, ReviewHandler rHandler) {
        this.databaseHelper = databaseHelper;
        this.aHandler = aHandler;
        this.qHandler = qHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the user login page, providing fields for username and password entry,
     * and buttons for login, password reset, and navigation back to the previous page.
     *
     * @param primaryStage the primary Stage where the scene is displayed
     */
    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //Go back to previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        	});
        
        Button forgotPasswordButton = new Button("Forgot Password?");
        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
            // Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                User user = new User(userName, password, "");
                int userId = databaseHelper.getUserIdByUsername(userName);
                
                // Check if the user has forgotten their password
                boolean forgotPassword = databaseHelper.getForgotPasswordStatus(userId);
                

                if (forgotPassword == true) {
                	databaseHelper.deleteNotificationLine(userId, "Temporary");
                	
                    redirectToPasswordReset(userId, primaryStage);
                    return; // Stop further execution of login logic
                }

                WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper, userName, qHandler, aHandler, uHandler, user, rHandler);

                // Retrieve the user's role from the database using userName
                String role = databaseHelper.getUserRole(userName);

                if (role != null) {
                    user.setRole(role);
                    if (databaseHelper.login(user)) {
                        welcomeLoginPage.show(primaryStage, user);
                    } else {
                        // Display an error if the login fails
                        errorLabel.setText("Username or password is wrong");
                    }
                } else {
                    // Display an error if the account does not exist
                    errorLabel.setText("User account doesn't exist");
                }

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        forgotPasswordButton.setOnAction(a -> {
            // Fetch userName and id
            String userName = userNameField.getText();
            int userId = databaseHelper.getUserIdByUsername(userName);

            String notification = "User " + userId + " forgot their password. Send them a temporary one.";
            
            //the getUserIdByUsername will return -1 when the userId does not exist
            if (userId != -1) {
	            // Retrieve admin id to contact about resetting password
	            int adminId = databaseHelper.getUserIdByUsername(databaseHelper.getFirstAdmin());
	            if (databaseHelper.getForgotPasswordStatus(userId) == false) {
		            databaseHelper.addNotificationToUser(notification, adminId);
		            errorLabel.setText("Sent request to Admin");
	            }
	            // Create a task to periodically check for updates
	            Task<Void> task = new Task<Void>() {
	                @Override
	                protected Void call() throws Exception {
	                    while (true) {
	                        String notifications = databaseHelper.getNotifications(userId);
	                        if (notifications != null && notifications.contains("Temporary Password:")) {
	                            Platform.runLater(() -> errorLabel.setText(notifications));
	                            break;
	                        }
	                        Thread.sleep(5000); // Check every 5 seconds
	                    }
	                    return null;
	                }
	            };
	
	            // Start the task in a new thread
	            new Thread(task).start();
            }
            else {
            	errorLabel.setText("Input valid user ID");
            }
        });

        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20, 20, 20, 20));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel, forgotPasswordButton);

        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(layout);
        rootLayout.setBottom(backButtonBox);

        primaryStage.setScene(new Scene(rootLayout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    /**
     * Redirects the user to the password reset page if they have forgotten their password.
     * After the password is reset, the login page is shown again.
     *
     * @param userId       the ID of the user who needs to reset their password
     * @param primaryStage the primary Stage where the scene is displayed
     */
    private void redirectToPasswordReset(int userId, Stage primaryStage) {
        PasswordResetPage passwordResetPage = new PasswordResetPage(databaseHelper, userId);
        passwordResetPage.show(primaryStage);
        

        // Once the password is reset, return to login
        passwordResetPage.setOnPasswordReset(() -> {
            UserLoginPage loginPage = new UserLoginPage(databaseHelper, aHandler, qHandler, uHandler, rHandler);
            loginPage.show(primaryStage);
        });
    }

}
