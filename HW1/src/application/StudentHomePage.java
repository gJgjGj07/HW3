package application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
// Import your new handler classes
import application.QuestionHandler;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import application.AnswerHandler;

/**
 * The StudentHomePage class represents the main user interface for a student.
 * It provides options for submitting questions, displaying posts, managing notifications,
 * sending messages, rating reviewers, and managing the reviewer list.
 */
public class StudentHomePage {
    private final QuestionHandler questionHandler;
    private final AnswerHandler answerHandler;
    private final UserHandler userHandler;
    private final ReviewHandler rHandler;
    private String userName;
    DatabaseHelper databaseHelper;

    /**
     * Constructs a StudentHomePage with the specified handlers, username, and database helper.
     *
     * @param questionHandler the QuestionHandler for managing questions
     * @param answerHandler   the AnswerHandler for managing answers
     * @param userHandler     the UserHandler for managing user operations
     * @param rHandler        the ReviewHandler for managing reviews
     * @param userName        the username of the current student
     * @param databaseHelper  the DatabaseHelper for database operations
     */
    public StudentHomePage(QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, ReviewHandler rHandler, String userName, DatabaseHelper databaseHelper) {
        this.questionHandler = questionHandler;
        this.answerHandler = answerHandler;
        this.userHandler = userHandler;
        this.rHandler = rHandler;
        this.userName = userName;
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the student home page scene with various options such as submitting a question,
     * displaying posts, viewing notifications, sending messages, and managing reviewer status.
     *
     * @param primaryStage the primary Stage where the scene is displayed
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
        
        Button requestStatusButton = new Button("Request to Become Reviewer");
     
        requestStatusButton.setOnAction(e -> {
            String notification = "Request to become reviewer from student: " + userName + " (ID: " + databaseHelper.getUserIdByUsername(userName) + ")";
            
            try {
                boolean sent = databaseHelper.addNotificationToAllInstructors(notification);
                
                if(sent) {
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("Success");
                    infoAlert.setHeaderText(null);
                    infoAlert.setContentText("Request sent to all instructors");
                    infoAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("No Instructors Found");
                    errorAlert.setContentText("Could not find any instructors to send request to");
                    errorAlert.showAndWait();
                }
            } catch (SQLException ex) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Database Error");
                errorAlert.setHeaderText("Request Failed");
                errorAlert.setContentText("Could not send request: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });
        
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
            
    
            Button reviewerListButton = new Button("Reviewer List");
            reviewerListButton.setOnAction(event -> showReviewerList(primaryStage));

         
            
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
        
        Button rateReviewersButton = new Button("Rate Reviewers");
        rateReviewersButton.setOnAction(e -> showRateReviewersDialog(primaryStage));
        
        Button reviewerListButton = new Button("Reviewer List");
        reviewerListButton.setOnAction(e -> showReviewerList(primaryStage));
        
        layout.getChildren().addAll(welcomeLabel, submitQuestionButton, displayPostsButton, notificationsButton, sendMessageButton, requestStatusButton, rateReviewersButton, reviewerListButton, logoutButton);
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Menu");
    }
 
    /**
     * Displays a dialog for rating reviewers and marking them as trusted.
     * The dialog allows the student to select a reviewer, assign a rating, and optionally mark them as trusted.
     *
     * @param primaryStage the primary Stage used as the owner for the dialog
     */
    private void showRateReviewersDialog(Stage primaryStage) {
        try {
            List<String> reviewers = databaseHelper.getAllReviewersUsernames();
            if (reviewers.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Reviewers");
                alert.setHeaderText(null);
                alert.setContentText("There are currently no reviewers to rate.");
                alert.showAndWait();
                return;
            }

            // Create custom dialog with rating and trusted options
            Dialog<Pair<Pair<String, Integer>, Boolean>> dialog = new Dialog<>();
            dialog.setTitle("Rate Reviewers");
            dialog.setHeaderText("Select a reviewer, give rating, and choose to trust");

            // Set button types
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Create UI components
            ComboBox<String> reviewerComboBox = new ComboBox<>();
            reviewerComboBox.getItems().addAll(reviewers);
            reviewerComboBox.setPromptText("Select Reviewer");

            Spinner<Integer> ratingSpinner = new Spinner<>(1, 10, 5);
            ratingSpinner.setEditable(true);

            CheckBox trustedCheckBox = new CheckBox("Add to Trusted Reviewers");
            trustedCheckBox.setTooltip(new Tooltip("Mark this reviewer as trusted"));

            // Check if already trusted and disable checkbox if true
            reviewerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        boolean alreadyTrusted = databaseHelper.isReviewerTrusted(newVal, userName);
                        trustedCheckBox.setSelected(alreadyTrusted);
                        trustedCheckBox.setDisable(alreadyTrusted);
                        if (alreadyTrusted) {
                            trustedCheckBox.setText("Already Trusted");
                        } else {
                            trustedCheckBox.setText("Add to Trusted Reviewers");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Reviewer:"), 0, 0);
            grid.add(reviewerComboBox, 1, 0);
            grid.add(new Label("Rating (1-10):"), 0, 1);
            grid.add(ratingSpinner, 1, 1);
            grid.add(trustedCheckBox, 0, 2, 2, 1);

            dialog.getDialogPane().setContent(grid);

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Pair<>(
                        new Pair<>(reviewerComboBox.getValue(), ratingSpinner.getValue()),
                        trustedCheckBox.isSelected()
                    );
                }
                return null;
            });

            Optional<Pair<Pair<String, Integer>, Boolean>> result = dialog.showAndWait();

            result.ifPresent(reviewerRatingTrusted -> {
                String reviewer = reviewerRatingTrusted.getKey().getKey();
                int rating = reviewerRatingTrusted.getKey().getValue();
                boolean trusted = reviewerRatingTrusted.getValue();
                
                try {
                    // Save rating
                    databaseHelper.addReview(reviewer, rating, userName);
                    
                    // Save trusted status if selected
                    if (trusted) {
                        databaseHelper.addTrustedReviewer(reviewer, userName);
                    }
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText(null);
                    StringBuilder message = new StringBuilder("Rating submitted for " + reviewer);
                    if (trusted) {
                        message.append("\nReviewer added to your trusted list");
                    }
                    success.setContentText(message.toString());
                    success.showAndWait();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Database Error");
                    error.setContentText("Could not save rating/trust status: " + e.getMessage());
                    error.showAndWait();
                }
            });
        } catch (SQLException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Database Error");
            error.setContentText("Could not retrieve reviewers: " + e.getMessage());
            error.showAndWait();
        }
    }

    /**
     * Displays the reviewer's list page where the student can view, add, remove, and rate reviewers.
     *
     * @param primaryStage the primary Stage where the reviewer list is displayed
     */
    private void showReviewerList(Stage primaryStage) {
        try {
            // Create a new stage for the reviewer list
            Stage reviewerStage = new Stage();
            reviewerStage.setTitle("My Reviewer List");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            
            // Get all available reviewers
            List<String> allReviewers = databaseHelper.getAllReviewersUsernames();
            
            // Get the student's current reviewers (from trusted reviewers table)
            List<String> myReviewers = databaseHelper.getMyReviewers(userName);
            
            // Create a list view to display current reviewers with ratings
            ListView<String> reviewerListView = new ListView<>();
            for (String reviewer : myReviewers) {
                // Get the rating for this reviewer
                Integer rating = databaseHelper.getReviewerRating(userName, reviewer);
                String displayText = reviewer + (rating != null ? " (Your rating: " + rating + ")" : " (Not rated yet)");
                reviewerListView.getItems().add(displayText);
            }
            
            // Create a combo box for adding new reviewers
            ComboBox<String> reviewerComboBox = new ComboBox<>();
            // Only show reviewers not already in the list
            allReviewers.removeAll(myReviewers);
            reviewerComboBox.getItems().addAll(allReviewers);
            reviewerComboBox.setPromptText("Select Reviewer to Add");
            
            // Add button
            Button addButton = new Button("Add Reviewer");
            addButton.setOnAction(e -> {
                String selectedReviewer = reviewerComboBox.getSelectionModel().getSelectedItem();
                if (selectedReviewer != null) {
                    try {
                        // Add to trusted reviewers
                        databaseHelper.addTrustedReviewer(selectedReviewer, userName);
                        
                        // Refresh the list
                        reviewerListView.getItems().add(selectedReviewer + " (Not rated yet)");
                        reviewerComboBox.getItems().remove(selectedReviewer);
                        
                        // Notify the reviewer
                        String notification = userName + " has added you to their reviewer list!";
                        databaseHelper.addNotificationToUser(notification, 
                            databaseHelper.getUserIdByUsername(selectedReviewer));
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Success");
                        success.setHeaderText(null);
                        success.setContentText(selectedReviewer + " added to your reviewer list");
                        success.showAndWait();
                        refreshPage(primaryStage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Error");
                        error.setHeaderText("Database Error");
                        error.setContentText("Could not add reviewer: " + ex.getMessage());
                        error.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Selection");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select a reviewer to add");
                    alert.showAndWait();
                }
            });
            
            // Delete button
            Button deleteButton = new Button("Delete Selected");
            deleteButton.setOnAction(e -> {
                String selected = reviewerListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Extract the reviewer name (remove the rating part if present)
                    String reviewerName = selected.split(" ")[0];
                    
                    try {
                        // Remove from trusted reviewers
                        databaseHelper.removeTrustedReviewer(reviewerName, userName);
                        
                        // Refresh the list
                        reviewerListView.getItems().remove(selected);
                        reviewerComboBox.getItems().add(reviewerName);
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Success");
                        success.setHeaderText(null);
                        success.setContentText(reviewerName + " removed from your reviewer list");
                        success.showAndWait();
                        refreshPage(primaryStage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Error");
                        error.setHeaderText("Database Error");
                        error.setContentText("Could not remove reviewer: " + ex.getMessage());
                        error.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Selection");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select a reviewer to remove");
                    alert.showAndWait();
                }
            });
            
            // Rate button
            Button rateButton = new Button("Rate Selected");
            rateButton.setOnAction(e -> {
                String selected = reviewerListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String reviewerName = selected.split(" ")[0];
                    showRatingDialog(reviewerName, primaryStage);
                    
                    // Refresh the list after rating
                    try {
                        refreshReviewerList(reviewerListView);
                        refreshPage(primaryStage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Selection");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select a reviewer to rate");
                    alert.showAndWait();
                }
            });
            
            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> reviewerStage.close());
            
            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(addButton, deleteButton, rateButton, closeButton);
            
            layout.getChildren().addAll(
                new Label("My Reviewers:"),
                reviewerListView,
                new Label("Add New Reviewer:"),
                reviewerComboBox,
                buttonBox
            );
            
            Scene scene = new Scene(layout, 400, 400);
            reviewerStage.setScene(scene);
            reviewerStage.show();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Database Error");
            error.setContentText("Could not load reviewer list: " + e.getMessage());
            error.showAndWait();
        }
    }

    /**
     * Displays a dialog for rating a selected reviewer.
     * Allows the student to provide a rating between 1 and 10 and updates the review in the database.
     *
     * @param reviewerName the name of the reviewer to be rated
     */
    private void showRatingDialog(String reviewerName, Stage primaryStage) {
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Rate Reviewer");
        dialog.setHeaderText("Rate " + reviewerName);
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        int initialRating = 5;
        try {
            Integer existingRating = databaseHelper.getReviewerRating(userName, reviewerName);
            if (existingRating != null) {
                initialRating = existingRating;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 10, initialRating);
        ratingSpinner.setEditable(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Rating (1-10):"), 0, 0);
        grid.add(ratingSpinner, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(reviewerName, ratingSpinner.getValue());
            }
            return null;
        });
        
        Optional<Pair<String, Integer>> result = dialog.showAndWait();
        
        result.ifPresent(reviewerRating -> {
            try {
                databaseHelper.addOrUpdateReview(reviewerRating.getKey(), reviewerRating.getValue(), userName);
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Rating " + 
                    (databaseHelper.reviewExists(reviewerName, userName) ? "updated" : "submitted") + 
                    " for " + reviewerRating.getKey());
                success.showAndWait();
                
                // Use the passed primaryStage instead
                showReviewerList(primaryStage);
            } catch (SQLException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Database Error");
                error.setContentText("Could not save rating: " + e.getMessage());
                error.showAndWait();
            }
        });
    }

    /**
     * Refreshes the contents of the reviewer list view with the latest data from the database.
     *
     * @param reviewerListView the ListView displaying the reviewer list
     * @throws SQLException if a database access error occurs
     */
    private void refreshReviewerList(ListView<String> reviewerListView) throws SQLException {
        reviewerListView.getItems().clear();
        List<String> myReviewers = databaseHelper.getMyReviewers(userName);
        for (String reviewer : myReviewers) {
            Integer rating = databaseHelper.getReviewerRating(userName, reviewer);
            String displayText = reviewer + (rating != null ? " (Your rating: " + rating + ")" : " (Not rated yet)");
            reviewerListView.getItems().add(displayText);
        }
    }

    /**
     * Refreshes the current page by reloading the reviewer list.
     *
     * @param primaryStage the primary Stage of the application
     */
    private void refreshPage(Stage primaryStage) {
    	showReviewerList(primaryStage);
    }
    
}
