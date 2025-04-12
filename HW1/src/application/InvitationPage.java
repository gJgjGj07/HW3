package application;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The {@code InvitationPage} class represents the page where an admin can generate an invitation code.
 * <p>
 * This page displays a simple interface with a title and a button. When the button is clicked,
 * the application navigates to the {@link RoleSelectionPage} where the invitation code is generated
 * and displayed.
 * </p>
 */
public class InvitationPage { 

    /**
     * Displays the invitation page on the provided primary stage.
     * <p>
     * The page consists of a title label and a button to generate the invitation code.
     * Clicking the button navigates the user to the {@link RoleSelectionPage}.
     * </p>
     *
     * @param databaseHelper the {@link DatabaseHelper} instance for database operations
     * @param qHandler       the {@link QuestionHandler} for question-related operations
     * @param aHandler       the {@link AnswerHandler} for answer-related operations
     * @param uHandler       the {@link UserHandler} for user-related operations
     * @param rHandler       the {@link ReviewHandler} for review-related operations
     * @param primaryStage   the primary {@link Stage} where the scene will be displayed
     */
    public void show(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler, Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display the title of the page
        Label userLabel = new Label("Invite ");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to generate the invitation code and navigate to the RoleSelectionPage
        Button showCodeButton = new Button("Generate Invitation Code");
        showCodeButton.setOnAction(event -> {
            // Navigate to the RoleSelectionPage
            RoleSelectionPage roleSelectionPage = new RoleSelectionPage();
            roleSelectionPage.show(databaseHelper, qHandler, aHandler, uHandler, rHandler, primaryStage);
        });

        // Add components to the layout
        layout.getChildren().addAll(userLabel, showCodeButton);
        Scene inviteScene = new Scene(layout, 800, 400);

        // Set the scene to the primary stage and update the title
        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }
}
