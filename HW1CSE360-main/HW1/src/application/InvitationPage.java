package application;


import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage { 
	
	public void show(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display the title of the page
        Label userLabel = new Label("Invite ");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to navigate to the role selection page
        Button showCodeButton = new Button("Generate Invitation Code");

        // Set action for the button
        showCodeButton.setOnAction(event -> {
            // Navigate to the RoleSelectionPage
            RoleSelectionPage roleSelectionPage = new RoleSelectionPage();
            roleSelectionPage.show(databaseHelper, qHandler, aHandler, uHandler, primaryStage);
        });

        // Add components to the layout
        layout.getChildren().addAll(userLabel, showCodeButton);
        Scene inviteScene = new Scene(layout, 800, 400);

        // Set the scene to the primary stage
        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }

}