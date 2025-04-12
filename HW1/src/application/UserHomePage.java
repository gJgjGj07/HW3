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
 * It allows the user to select a role if multiple roles are assigned,
 * and then navigates to the corresponding home page.
 */
public class UserHomePage {
	private int userId;
	private String userName;
	private DatabaseHelper databaseHelper;
	private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;
	private ReviewHandler rHandler;

	/**
	 * Constructs a UserHomePage with the specified parameters.
	 *
	 * @param userId          the ID of the user
	 * @param databaseHelper  the DatabaseHelper instance for database operations
	 * @param qHandler        the QuestionHandler for managing questions
	 * @param aHandler        the AnswerHandler for managing answers
	 * @param uHandler        the UserHandler for managing users
	 * @param rHandler        the ReviewHandler for managing reviews
	 * @param userName        the username of the user
	 * @param user            the User object representing the current user
	 */
	public UserHomePage(int userId, DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler, String userName, User user) {
		this.userId = userId;
		this.databaseHelper = databaseHelper;
		this.qHandler = qHandler;
		this.aHandler = aHandler;
		this.uHandler = uHandler;
		this.userName = userName;
		this.rHandler = rHandler;
	}

	/**
	 * Displays the User Home Page. If the user has multiple roles, a role selection
	 * drop-down is presented. Based on the selected or single role, the method navigates
	 * to the corresponding home page for Student, Staff, Reviewer, or Instructor.
	 *
	 * @param primaryStage the primary Stage where the scene is displayed
	 * @param user         the User object representing the current user
	 */
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
            		StudentHomePage studentHomePage = new StudentHomePage(qHandler, aHandler, uHandler, rHandler, userName, databaseHelper);
            		studentHomePage.show(primaryStage);
            		return;
            	}
            	else if (selectedRole.equals("Staff")) {
            		StaffHomePage staffHomePage = new StaffHomePage(databaseHelper, qHandler, aHandler, uHandler, rHandler);
            		staffHomePage.show(primaryStage);
            		return;
            	}
            	else if (selectedRole.equals("Reviewer")) {
            		ReviewerHomePage reviewerHomePage = new ReviewerHomePage(qHandler, aHandler, uHandler, rHandler, userName, databaseHelper);
            		reviewerHomePage.show(primaryStage);
            		return;
            	}
            	else {
            		InstructorHomePage instructorHomePage = new InstructorHomePage(databaseHelper, qHandler, aHandler, uHandler, rHandler);
            		instructorHomePage.show(primaryStage, userName);
            		return;
            	}

            });

    	}
    	else {
    		if (role.equals("Student")) {
        		StudentHomePage studentHomePage = new StudentHomePage(qHandler, aHandler, uHandler, rHandler, userName, databaseHelper);
        		studentHomePage.show(primaryStage);
        		return;
        	}
        	else if (role.equals("Staff")) {
        		StaffHomePage staffHomePage = new StaffHomePage(databaseHelper, qHandler, aHandler, uHandler, rHandler);
        		staffHomePage.show(primaryStage);
        		return;
        	}
        	else if (role.equals("Reviewer")) {
        		ReviewerHomePage reviewerHomePage = new ReviewerHomePage(qHandler, aHandler, uHandler, rHandler, userName, databaseHelper);
        		reviewerHomePage.show(primaryStage);
        		return;
        	}
        	else {
        		InstructorHomePage instructorHomePage = new InstructorHomePage(databaseHelper, qHandler, aHandler, uHandler, rHandler);
        		instructorHomePage.show(primaryStage, userName);
        		return;
        	}
    	}
    	Scene roleScene = new Scene(layout, 800, 500);

        primaryStage.setScene(roleScene);
        primaryStage.setTitle("Role Selection Page");

    }
}
