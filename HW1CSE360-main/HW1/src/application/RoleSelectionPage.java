package application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RoleSelectionPage {
    
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Timer for invalidation

    public void show(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Title label
        Label titleLabel = new Label("Select Role(s) and Generate Invitation Code");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create CheckBoxes for each role
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox reviewerCheckBox = new CheckBox("Reviewer");
        CheckBox instructorCheckBox = new CheckBox("Instructor");
        CheckBox staffCheckBox = new CheckBox("Staff");

        // Container for CheckBoxes
        VBox checkBoxContainer = new VBox(10, studentCheckBox, reviewerCheckBox, instructorCheckBox, staffCheckBox);
        checkBoxContainer.setStyle("-fx-alignment: center;");

        // Button to generate the invitation code
        Button generateCodeButton = new Button("Generate Invitation Code");

        // Label to display the generated invitation code
        Label inviteCodeLabel = new Label();
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        
        // Logout button to return to SetupLoginSelectionPage
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler).show(primaryStage);
        });

        // Set action for the generate code button
        generateCodeButton.setOnAction(event -> {
            List<String> selectedRoles = new ArrayList<>();
            if (studentCheckBox.isSelected()) selectedRoles.add("Student");
            if (reviewerCheckBox.isSelected()) selectedRoles.add("Reviewer");
            if (instructorCheckBox.isSelected()) selectedRoles.add("Instructor");
            if (staffCheckBox.isSelected()) selectedRoles.add("Staff");

            if (selectedRoles.isEmpty()) {
                inviteCodeLabel.setText("Please select at least one role.");
            } else {
                // Join selected roles into a comma-separated string
                String roles = String.join(",", selectedRoles);
                // Generate the invitation code using the roles string
                String invitationCode = databaseHelper.generateInvitationCode(roles);
                inviteCodeLabel.setText("Generated Invitation Code: " + invitationCode);

                // Schedule invalidation after 60 minutes
                scheduler.schedule(() -> {
                    databaseHelper.markInvitationCodeAsUsed(invitationCode);
                    System.out.println("Invitation Code invalidated: " + invitationCode);
                }, 60, TimeUnit.MINUTES);
            }
        });

        // Add all components to the layout
        layout.getChildren().addAll(titleLabel, checkBoxContainer, generateCodeButton, inviteCodeLabel, logoutButton);

        // Create and set the scene
        Scene roleSelectionScene = new Scene(layout, 800, 400);
        primaryStage.setScene(roleSelectionScene);
        primaryStage.setTitle("Role Selection Page");
    }
}