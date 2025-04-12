package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the staff user.
 */
public class StaffHomePage {

    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;
	private  ReviewHandler rHandler;

    /**
     * Constructs a StaffHomePage with the given database helper and handler objects.
     *
     * @param databaseHelper the DatabaseHelper used for database operations
     * @param qHandler the QuestionHandler used for managing questions
     * @param aHandler the AnswerHandler used for managing answers
     * @param uHandler the UserHandler used for managing users
     * @param rHandler the ReviewHandler used for managing reviews
     */
    public StaffHomePage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler
) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the Staff Home Page in the given primary stage.
     * The page shows a welcome message and a logout button which navigates back to the login selection page.
     *
     * @param primaryStage the primary Stage where the scene is set
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label userLabel = new Label("Hello, Staff!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Logout button to return to SetupLoginSelectionPage
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        });

        layout.getChildren().addAll(userLabel, logoutButton);
        Scene userScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Staff Page");
    }
}
