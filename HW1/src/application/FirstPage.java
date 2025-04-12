package application;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The {@code FirstPage} class represents the initial screen for the first user.
 * It prompts the user to set up administrator access and navigates to the setup screen.
 */
public class FirstPage {

    // Reference to the DatabaseHelper for database interactions
    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private UserHandler uHandler;
    private ReviewHandler rHandler;

    /**
     * Constructs a {@code FirstPage} instance with the given database helper and handlers.
     *
     * @param databaseHelper the database helper for database interactions
     * @param qHandler       the question handler
     * @param aHandler       the answer handler
     * @param uHandler       the user handler
     * @param rHandler       the review handler
     */
    public FirstPage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the first page in the provided primary stage.
     * <p>
     * The page welcomes the first user and provides a button to continue to the administrator setup screen.
     * </p>
     *
     * @param primaryStage the primary stage where the scene will be displayed
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Label to display the welcome message for the first user
        Label userLabel = new Label("Hello! You are the first person here. \nPlease select continue to setup administrator access");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button to navigate to the AdminSetupPage
        Button continueButton = new Button("Continue");
        continueButton.setOnAction(a -> {
            new AdminSetupPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        });

        layout.getChildren().addAll(userLabel, continueButton);
        Scene firstPageScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage and display it
        primaryStage.setScene(firstPageScene);
        primaryStage.setTitle("First Page");
        primaryStage.show();
    }
}
