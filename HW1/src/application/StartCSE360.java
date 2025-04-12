package application;
import javafx.scene.image.Image;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;

/**
 * The StartCSE360 class serves as the entry point for the CSE360 application.
 * It extends the JavaFX Application class and is responsible for initializing
 * the database connection and launching the appropriate page based on the state
 * of the database.
 */
public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	/**
	 * The main method that launches the JavaFX application.
	 *
	 * @param args the command line arguments
	 */
	public static void main( String[] args )
	{
		 launch(args);
	}
	
	/**
	 * The start method is the main entry point for all JavaFX applications.
	 * It sets the application icon, establishes a connection to the database,
	 * initializes the necessary handlers, and displays the initial page based on
	 * whether the database is empty.
	 *
	 * @param primaryStage the primary stage for this application
	 */
	@Override
    public void start(Stage primaryStage) {
        try {
        	Image icon = new Image(getClass().getResourceAsStream("app-icon.png"));
            primaryStage.getIcons().add(icon);
        	
            Connection conn = databaseHelper.connectToDatabase(); // Connect to the database
            QuestionHandler qHandler = new QuestionHandler(conn);
            AnswerHandler aHandler = new AnswerHandler(conn, qHandler);
            UserHandler uHandler = new UserHandler(conn);
            ReviewHandler rHandler = new ReviewHandler(conn);
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
            } else {
            	new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
                
            }
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}
