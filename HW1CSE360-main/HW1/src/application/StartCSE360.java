package application;
import javafx.scene.image.Image;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;


public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	public static void main( String[] args )
	{
		 launch(args);
	}
	
	@Override
    public void start(Stage primaryStage) {
        try {
        	Image icon = new Image(getClass().getResourceAsStream("app-icon.png"));
            primaryStage.getIcons().add(icon);
        	
            Connection conn = databaseHelper.connectToDatabase(); // Connect to the database
            QuestionHandler qHandler = new QuestionHandler(conn);
            AnswerHandler aHandler = new AnswerHandler(conn, qHandler);
            UserHandler uHandler = new UserHandler(conn);
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
            } else {
            	new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
                
            }
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}
