package application;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * The LogoutButton class creates a reusable logout button.
 */
public class LogoutButton {
    private final Button button;
    

    public LogoutButton(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, Stage primaryStage) {
        button = new Button("Logout");
        button.setOnAction(a -> {
            System.out.println("Logging out and returning to SetupLoginSelectionPage.");
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
        });
    }

    public Button getButton() {
        return button;
    }
}
