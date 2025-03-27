package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;
	private int userId;
	private String userName;
	private final QuestionHandler qHandler;
	private final AnswerHandler aHandler;
	private final UserHandler uHandler;

    public WelcomeLoginPage(DatabaseHelper databaseHelper, String userName, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler,  User user) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
        userId= databaseHelper.getUserIdByUsername(userName);
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        		
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	    	String role =user.getRole();
	    	System.out.println(role);

	    	if(role.equals("admin")) {
	    		AdminHomePage adminHomePage = new AdminHomePage(userId, userName, qHandler, aHandler, uHandler, user);
	    		adminHomePage.show(primaryStage, user);
	    	}
	    	else{
	    		UserHomePage userHomePage = new UserHomePage(userId, databaseHelper, qHandler, aHandler, uHandler, userName, user);
	    		userHomePage.show(primaryStage, user);
	    		

	    	}
	    });
	    
	    // Logout button to take user back to SetupLoginSelectionPage
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> {
	        System.out.println("Logging out and returning to SetupLoginSelectionPage.");
	        new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
	    });
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	    if ("admin".equals(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, qHandler, aHandler, uHandler, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }
	    
	

	    

	    layout.getChildren().addAll(welcomeLabel,continueButton,quitButton, logoutButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}