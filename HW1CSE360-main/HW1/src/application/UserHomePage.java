package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	private int userId;
	private String userName;
	private DatabaseHelper databaseHelper;
	private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;
	
	public UserHomePage(int userId, DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, String userName, User user) {
		this.userId = userId;
		this.databaseHelper = databaseHelper;
		this.qHandler = qHandler;
		this.aHandler = aHandler;
		this.uHandler = uHandler;
		this.userName = userName;
	}
	
    public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
    	String role = databaseHelper.getUserRoleById(userId);
    	if (role.contains(",")) {
    		// Create a label (text box) stating "Select Role"
            String[] userRole = role.split(",");
    		// Drop-down menu for role selection
            ComboBox<String> roleComboBox = new ComboBox<>();
            //Adds the users role to the selection box
            for (int i = 0; i < userRole.length ; ++i) {
            	roleComboBox.getItems().add(userRole[i]);
            }
            roleComboBox.setPromptText("Select Role");
            
            Button rolePage = new Button();
            rolePage.setText("Go to Role Home Page");
            
            layout.getChildren().addAll(roleComboBox, rolePage);
            rolePage.setOnAction(event ->{
            	String selectedRole = roleComboBox.getValue();
            	if (selectedRole.equals("Student")) {
            		StudentHomePage studentHomePage = new StudentHomePage(qHandler, aHandler, uHandler, userName, databaseHelper);
            		studentHomePage.show(primaryStage);
            		return;
            	}
            	else if (selectedRole.equals("Staff")) {
            		StaffHomePage staffHomePage = new StaffHomePage(databaseHelper, qHandler, aHandler, uHandler);
            		staffHomePage.show(primaryStage);
            		return;
            	}
            	else if (selectedRole.equals("Reviewer")) {
            		ReviewerHomePage reviewerHomePage = new ReviewerHomePage(databaseHelper, qHandler, aHandler, uHandler);
            		reviewerHomePage.show(primaryStage);
            		return;
            	}
            	else {
            		InstructorHomePage instructorHomePage = new InstructorHomePage(databaseHelper, qHandler, aHandler, uHandler);
            		instructorHomePage.show(primaryStage);
            		return;
            	}
            	
            });
  
    	}
    	else {
    		if (role.equals("Student")) {
        		StudentHomePage studentHomePage = new StudentHomePage(qHandler, aHandler, uHandler, userName, databaseHelper);
        		studentHomePage.show(primaryStage);
        		return;
        	}
        	else if (role.equals("Staff")) {
        		StaffHomePage staffHomePage = new StaffHomePage(databaseHelper, qHandler, aHandler, uHandler);
        		staffHomePage.show(primaryStage);
        		return;
        	}
        	else if (role.equals("Reviewer")) {
        		ReviewerHomePage reviewerHomePage = new ReviewerHomePage(databaseHelper, qHandler, aHandler, uHandler);
        		reviewerHomePage.show(primaryStage);
        		return;
        	}
        	else {
        		InstructorHomePage instructorHomePage = new InstructorHomePage(databaseHelper, qHandler, aHandler, uHandler);
        		instructorHomePage.show(primaryStage);
        		return;
        	}
    	}
    	Scene roleScene = new Scene(layout, 800, 500);

    	 
        primaryStage.setScene(roleScene);
        primaryStage.setTitle("Role Selection Page");
    	
    }
}