package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Optional;

// Import your new handler classes
import application.QuestionHandler;
import databasePart1.DatabaseHelper;
import application.AnswerHandler;

/**
 * This page displays the reviewer home page with messaging capabilities.
 */
public class ReviewerHomePage {
    private final QuestionHandler questionHandler;
    private final AnswerHandler answerHandler;
    private final UserHandler userHandler;
    private final ReviewHandler rHandler;
    private String userName;
    DatabaseHelper databaseHelper;

    /**
     * Constructs the ReviewerHomePage with necessary dependencies.
     * 
     * @param questionHandler The handler for managing questions.
     * @param answerHandler The handler for managing answers.
     * @param userHandler The handler for managing users.
     * @param rHandler The handler for managing reviews.
     * @param userName The username of the logged-in reviewer.
     * @param databaseHelper The helper for interacting with the database.
     */
    public ReviewerHomePage(QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, ReviewHandler rHandler, String userName, DatabaseHelper databaseHelper) {
        this.questionHandler = questionHandler;
        this.answerHandler = answerHandler;
        this.userHandler = userHandler;
        this.rHandler = rHandler;
        this.userName = userName;
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the reviewer home page scene with options to submit questions, view posts, 
     * and manage notifications and messages.
     * 
     * @param primaryStage The primary stage for displaying the scene.
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label welcomeLabel = new Label("Hello, " + userName + "!");
        Button submitQuestionButton = new Button("Submit a Question");
        Button displayPostsButton = new Button("Display All Posts");

        // Retrieve the userId using a helper method (assumed to exist)
        int userId = databaseHelper.getUserIdByUsername(userName);
        // Get the number of notifications for this user.
        int numNotifications = databaseHelper.getNumNotifications(userId);

        // Create the Notifications button with the count in its label.
        Button notificationsButton = new Button("Notifications (" + numNotifications + ")");
        notificationsButton.setOnAction(e -> {
            // Retrieve notifications for the current user.
            String notifications = databaseHelper.getNotifications(userId);

            // Create a new Stage to display notifications in a TextArea.
            Stage notifStage = new Stage();
            notifStage.setTitle("Your Notifications");

            VBox notifLayout = new VBox(10);
            notifLayout.setPadding(new Insets(20));

            // Create a non-editable TextArea for displaying notifications.
            TextArea notifTextArea = new TextArea(notifications);
            notifTextArea.setEditable(false);
            notifTextArea.setWrapText(true);
            notifTextArea.setPrefWidth(400);
            notifTextArea.setPrefHeight(300);

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(event -> notifStage.close());

            notifLayout.getChildren().addAll(new Label("Your Notifications:"), notifTextArea, closeBtn);
            Scene notifScene = new Scene(notifLayout);
            notifStage.setScene(notifScene);
            notifStage.showAndWait();

            // Once the notifications window is closed, clear the notifications.
            boolean cleared = databaseHelper.clearNotifications(userId);
            if (cleared) {
                notificationsButton.setText("Notifications (0)");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not clear notifications");
                alert.showAndWait();
            }
        });

        // Create the Send Message button.
        Button sendMessageButton = new Button("Send Message");
        sendMessageButton.setOnAction(e -> {
            // Create a new Stage for sending a message.
            Stage messageStage = new Stage();
            messageStage.setTitle("Send Message");

            VBox messageLayout = new VBox(10);
            messageLayout.setPadding(new Insets(20));

            Label recipientLabel = new Label("Recipient Username:");
            TextField recipientField = new TextField();

            Label messageLabel = new Label("Message:");
            TextArea messageArea = new TextArea();
            messageArea.setPrefRowCount(5);
            messageArea.setWrapText(true);

            Button sendBtn = new Button("Send");
            sendBtn.setOnAction(event -> {
                String recipientUsername = recipientField.getText().trim();
                String messageContent = messageArea.getText().trim();
                if(recipientUsername.isEmpty() || messageContent.isEmpty()){
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Invalid Input");
                    errorAlert.setContentText("Please fill in both recipient username and message.");
                    errorAlert.showAndWait();
                    return;
                }
                // Retrieve recipient's user ID.
                int recipientId = databaseHelper.getUserIdByUsername(recipientUsername);
                if(recipientId == -1){
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("User Not Found");
                    errorAlert.setContentText("Could not find a user with username: " + recipientUsername);
                    errorAlert.showAndWait();
                    return;
                }
                // Send the message as a notification to the recipient.
                boolean sent = databaseHelper.addNotificationToUser(userName + " (User " + userId + "): "+ messageContent, recipientId);
                if(sent){
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("Success");
                    infoAlert.setHeaderText(null);
                    infoAlert.setContentText("Message sent to " + recipientUsername);
                    infoAlert.showAndWait();
                    messageStage.close();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Message Failed");
                    errorAlert.setContentText("Could not send message to " + recipientUsername);
                    errorAlert.showAndWait();
                }
            });

            messageLayout.getChildren().addAll(recipientLabel, recipientField, messageLabel, messageArea, sendBtn);
            Scene messageScene = new Scene(messageLayout, 400, 300);
            messageStage.setScene(messageScene);
            messageStage.show();
        });

        // When "Submit a Question" is clicked, display the question submission scene.
        submitQuestionButton.setOnAction(e -> {
            new Question(questionHandler, answerHandler, userHandler, rHandler, userName, databaseHelper)
                .showQuestionSubmissionScene(primaryStage);
        });

        // When "Display All Posts" is clicked, display all questions (posts).
        displayPostsButton.setOnAction(e -> {
            new Question(questionHandler, answerHandler, userHandler, rHandler, userName, databaseHelper)
                .showPosts(primaryStage);
        });

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            // Return to the SetupLoginSelectionPage.
            new SetupLoginSelectionPage(databaseHelper, questionHandler, answerHandler, userHandler, rHandler).show(primaryStage);
        });

        layout.getChildren().addAll(welcomeLabel, submitQuestionButton, displayPostsButton, notificationsButton, sendMessageButton, logoutButton);
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Menu");
    }
}
