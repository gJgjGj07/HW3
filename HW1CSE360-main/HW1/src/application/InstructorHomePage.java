package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the instructor user.
 */

public class InstructorHomePage {

    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
	private AnswerHandler aHandler;
	private UserHandler uHandler;

    public InstructorHomePage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label userLabel = new Label("Hello, Instructor!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Logout button to return to SetupLoginSelectionPage
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
        });

        layout.getChildren().addAll(userLabel, logoutButton);
        Scene userScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Instructor Page");
    }
}
