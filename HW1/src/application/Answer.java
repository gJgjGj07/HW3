package application;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import databasePart1.DatabaseHelper;

/**
 * The Answer class handles the display and interaction of replies (answers)
 * to a post. It shows a list of top-level replies (those with no parent reply)
 * and provides functionality for users to write, edit, delete, reply, and like answers.
 * 
 * This class serves as the main user interface component for managing answers in the
 * question-answer application. It allows users to view answers sorted by different criteria,
 * interact with answers through liking, replying, editing and deleting, and provides
 * a way to navigate back to the questions view.
 */
public class Answer {

    /** The handler for answer-related database operations */
    private AnswerHandler answerHandler;
    
    /** The handler for review-related operations */
    private ReviewHandler reviewHandler;
    
    /** The ID of the post being answered */
    private int postId;
    
    /** The current sorting order for displaying answers */
    private String currentSortOrder = "None";

    /**
     * Inner class representing a reply to a post or another reply.
     * Contains all information required to display and interact with a reply.
     */
    private static class Reply {
        /** The unique identifier for this reply */
        private int replyId;
        
        /** The text content of the reply */
        private String content;
        
        /** The username of the reply author */
        private String userName;
        
        /** The number of likes this reply has received */
        private int likes;
        
        /** The number of replies to this reply */
        private int numReplies;

        /**
         * Constructs a new Reply object with the specified parameters.
         *
         * @param replyId The unique identifier for this reply
         * @param content The text content of the reply
         * @param userName The username of the reply author
         * @param likes The number of likes this reply has received
         * @param numReplies The number of replies to this reply
         */
        public Reply(int replyId, String content, String userName, int likes, int numReplies) {
            this.replyId = replyId;
            this.content = content;
            this.userName = userName;
            this.likes = likes;
            this.numReplies = numReplies;
        }

        /**
         * Gets the unique identifier for this reply.
         * @return The reply ID
         */
        public int getReplyId() { return replyId; }
        
        /**
         * Gets the text content of the reply.
         * @return The reply content
         */
        public String getContent() { return content; }
        
        /**
         * Gets the username of the reply author.
         * @return The username
         */
        public String getUserName() { return userName; }
        
        /**
         * Gets the number of likes this reply has received.
         * @return The like count
         */
        public int getLikes() { return likes; }
        
        /**
         * Gets the number of replies to this reply.
         * @return The count of nested replies
         */
        public int getNumReplies() { return numReplies; }
    }

    /**
     * Constructs a new Answer object with the specified handlers and post ID.
     *
     * @param answerHandler The handler for answer-related database operations
     * @param reviewHandler The handler for review-related operations
     * @param postId The ID of the post being answered
     */
    public Answer(AnswerHandler answerHandler, ReviewHandler reviewHandler, int postId) {
        this.answerHandler = answerHandler;
        this.reviewHandler = reviewHandler;
        this.postId = postId;
    }

    /**
     * Opens a window allowing a user to write an answer to the current post.
     * The window includes a text area for the answer content, character count feedback,
     * and validation before submission.
     *
     * @param userName The username of the current user
     * @param primaryStage The primary stage of the application
     */
    public void writeAnswer(String userName, Stage primaryStage) {
        Stage answerStage = new Stage();
        answerStage.setTitle("Answer the Question");

        TextArea answerTextArea = new TextArea();
        answerTextArea.setPromptText("Type your answer here...");
        answerTextArea.setWrapText(true);
        answerTextArea.setPrefRowCount(5);

        Label charCountLabel = new Label("0");
        charCountLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        answerTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.trim().length();
            charCountLabel.setText(length + "");
            charCountLabel.setStyle(length < 5 ? "-fx-text-fill: orange;" : "-fx-text-fill: #666;");
        });

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String answer = answerTextArea.getText().trim();
            List<String> errors = answerHandler.validateAnswer(answer);
            if (!errors.isEmpty()) {
                showAlert("Validation Error", String.join("\n\n", errors), Alert.AlertType.ERROR);
            } else {
                answerHandler.addAnswer(answer, postId, userName, false);
                showAlert("Success", "Answer submitted!", Alert.AlertType.INFORMATION);
                answerStage.close();
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(answerTextArea, charCountLabel, submitButton);

        Scene scene = new Scene(layout, 400, 300);
        answerStage.setScene(scene);
        answerStage.initOwner(primaryStage);
        answerStage.show();
    }

    /**
     * Displays all answers for the current post in the main application window.
     * Includes sorting options, the list of answers with their nested replies,
     * and navigation controls.
     *
     * @param primaryStage The primary stage of the application
     * @param currentUser The username of the current user
     * @param questionHandler The handler for question-related operations
     * @param answerHandler The handler for answer-related operations
     * @param userHandler The handler for user-related operations
     * @param userName The username of the current user (possibly redundant with currentUser)
     * @param databaseHelper The database helper for database operations
     */
    public void show(Stage primaryStage, String currentUser, QuestionHandler questionHandler,
                     AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        // Top: Sorting controls
        ComboBox<String> sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("None", "Most Likes");
        sortComboBox.setValue(currentSortOrder);
        sortComboBox.setOnAction(e -> {
            currentSortOrder = sortComboBox.getValue();
            VBox answersLayout = createAnswersLayout(currentUser, primaryStage, questionHandler,
                    answerHandler, userHandler, userName, databaseHelper);
            ScrollPane scrollPane = new ScrollPane(answersLayout);
            scrollPane.setFitToWidth(true);
            rootLayout.setCenter(scrollPane);
        });
        rootLayout.setTop(sortComboBox);

        // Center: Answers list
        VBox answersLayout = createAnswersLayout(currentUser, primaryStage, questionHandler,
                answerHandler, userHandler, userName, databaseHelper);
        ScrollPane scrollPane = new ScrollPane(answersLayout);
        scrollPane.setFitToWidth(true);
        rootLayout.setCenter(scrollPane);

        // Bottom: Back button
        HBox bottomLayout = new HBox(10);
        bottomLayout.setAlignment(Pos.CENTER_LEFT);
        bottomLayout.setPadding(new Insets(10, 0, 0, 0)); // Top padding only

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new Question(questionHandler, answerHandler, userHandler, reviewHandler, userName, databaseHelper).showPosts(primaryStage);
        });

        bottomLayout.getChildren().addAll(backButton);
        rootLayout.setBottom(bottomLayout);

        Scene scene = new Scene(rootLayout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Display Answers");
        primaryStage.show();
    }

    /**
     * Creates the layout containing all answers to the current post.
     * Retrieves answers from the database, applies sorting if specified,
     * and creates visual components for each answer and its nested replies.
     *
     * @param currentUser The username of the current user
     * @param primaryStage The primary stage of the application
     * @param questionHandler The handler for question-related operations
     * @param answerHandler The handler for answer-related operations
     * @param userHandler The handler for user-related operations
     * @param userName The username of the current user (possibly redundant with currentUser)
     * @param databaseHelper The database helper for database operations
     * @return A VBox containing all answer components
     */
    private VBox createAnswersLayout(String currentUser, Stage primaryStage, QuestionHandler questionHandler,
                                     AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
        VBox answersLayout = new VBox(20);
        answersLayout.setPadding(new Insets(20));
        answersLayout.setStyle("-fx-background-color: #F5F5F5;");

        try {
            ResultSet rs = answerHandler.getRepliesByPostId(postId, currentUser);
            List<Reply> replies = new ArrayList<>();

            while (rs.next()) {
                int replyId = rs.getInt("replyId");
                String replyContent = rs.getString("reply");
                String replyUserName = rs.getString("userName");
                int likes = rs.getInt("likes");
                int numReplies = rs.getInt("numReplies");
                replies.add(new Reply(replyId, replyContent, replyUserName, likes, numReplies));
            }

            if ("Most Likes".equals(currentSortOrder)) {
                replies.sort((a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
            }

            for (Reply reply : replies) {
                BorderPane answerPane = createAnswerPane(reply, currentUser, primaryStage, questionHandler,
                        answerHandler, userHandler, userName, databaseHelper);
                answersLayout.getChildren().add(answerPane);

                if (reply.getNumReplies() > 0) {
                    ResultSet nestedReplies = answerHandler.getNestedReplies(reply.getReplyId(), currentUser);
                    while (nestedReplies.next()) {
                        int nestedReplyId = nestedReplies.getInt("replyId");
                        String nestedReplyContent = nestedReplies.getString("reply");
                        String nestedReplyUserName = nestedReplies.getString("userName");
                        int nestedLikes = nestedReplies.getInt("likes");
                        int nestedNumReplies = nestedReplies.getInt("numReplies");

                        BorderPane nestedReplyPane = createAnswerPane(
                                new Reply(nestedReplyId, nestedReplyContent, nestedReplyUserName, nestedLikes, nestedNumReplies),
                                currentUser, primaryStage, questionHandler, answerHandler, userHandler, userName, databaseHelper
                        );
                        nestedReplyPane.setPadding(new Insets(10, 30, 10, 30));
                        answersLayout.getChildren().add(nestedReplyPane);
                    }
                }

            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load answers", Alert.AlertType.ERROR);
            e.printStackTrace();
        }

        return answersLayout;
    }

    /**
     * Creates a visual component for a single answer or reply.
     * The component includes the answer content, author information,
     * and buttons for actions like edit, delete, like, reply, and viewing reviews.
     * Available actions depend on whether the current user is the author of the answer.
     *
     * @param reply The Reply object containing answer data
     * @param currentUser The username of the current user
     * @param primaryStage The primary stage of the application
     * @param questionHandler The handler for question-related operations
     * @param answerHandler The handler for answer-related operations
     * @param userHandler The handler for user-related operations
     * @param userName The username of the current user (possibly redundant with currentUser)
     * @param databaseHelper The database helper for database operations
     * @return A BorderPane containing the answer component
     */
    private BorderPane createAnswerPane(Reply reply, String currentUser, Stage primaryStage, 
                                        QuestionHandler questionHandler, AnswerHandler answerHandler, 
                                        UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
        BorderPane answerPane = new BorderPane();
        answerPane.setPadding(new Insets(10));
        answerPane.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1;");
        answerPane.setPrefWidth(760);

        TextArea answerTextArea = new TextArea(reply.getContent());
        answerTextArea.setStyle("-fx-font-size: 18px;");
        answerTextArea.setWrapText(true);
        answerTextArea.setEditable(false);
        answerPane.setCenter(answerTextArea);

        Label answererLabel = new Label("Answered by: " + reply.getUserName());
        answererLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        answerPane.setBottom(answererLabel);
        BorderPane.setMargin(answererLabel, new Insets(5, 0, 0, 0));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Updated Reviews button for answers.
        Button reviewsButton = new Button("Reviews");
        reviewsButton.setOnAction(e -> {
            // Instantiate the Review window for an answer using replyId and set isForPost to false.
            application.Review reviewWindow = new application.Review(reviewHandler, reply.getReplyId(), false);
            reviewWindow.showReviews(primaryStage, currentUser, databaseHelper);
        });
        buttonBox.getChildren().add(reviewsButton);

        if (reply.getUserName().equals(currentUser)) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            editButton.setOnAction(e -> showEditAnswerScreen(reply.getReplyId(), primaryStage, currentUser, 
                    questionHandler, answerHandler, userHandler, userName, databaseHelper));

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> {
                answerHandler.deleteReplyById(reply.getReplyId());
                show(primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper);
            });
            Button replyButton = new Button("Reply");
            replyButton.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Reply to Answer");
                dialog.setHeaderText("Reply to: " + reply.getContent());
                dialog.setContentText("Enter your reply:");
                dialog.showAndWait().ifPresent(replyText -> {
                    boolean success = answerHandler.addReplyToReply(reply.getReplyId(), replyText, currentUser, false);
                    if (success) {
                        showAlert("Success", "Reply submitted!", Alert.AlertType.INFORMATION);
                        show(primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper);
                    } else {
                        showAlert("Error", "Failed to submit reply", Alert.AlertType.ERROR);
                    }
                });
            });

            buttonBox.getChildren().addAll(editButton, deleteButton, replyButton);
        } else {
            String likeList = answerHandler.getLikeList(reply.getReplyId());
            List<String> likes = new ArrayList<>();
            if (likeList != null && !likeList.trim().isEmpty()) {
                likes = Arrays.asList(likeList.split("\n"));
            }
            boolean isLiked = likes.contains(currentUser);

            Button likeButton = new Button((isLiked ? "Unlike " : "Like ") + reply.getLikes());
            likeButton.setOnAction(e -> {
                if (isLiked) {
                    answerHandler.decrementLikes(reply.getReplyId());
                    answerHandler.removeUserFromLikeList(currentUser, reply.getReplyId());
                } else {
                    answerHandler.incrementLikes(reply.getReplyId());
                    answerHandler.addUsertoLikeList(currentUser, reply.getReplyId());
                }
                show(primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper);
            });

            Button replyButton = new Button("Reply");
            replyButton.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Reply to Answer");
                dialog.setHeaderText("Reply to: " + reply.getContent());
                dialog.setContentText("Enter your reply:");
                dialog.showAndWait().ifPresent(replyText -> {
                    boolean success = answerHandler.addReplyToReply(reply.getReplyId(), replyText, currentUser, false);
                    if (success) {
                        showAlert("Success", "Reply submitted!", Alert.AlertType.INFORMATION);
                        show(primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper);
                    } else {
                        showAlert("Error", "Failed to submit reply", Alert.AlertType.ERROR);
                    }
                });
            });

            buttonBox.getChildren().addAll(likeButton, replyButton);
        }

        answerPane.setRight(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(0, 10, 0, 10));

        return answerPane;
    }

    /**
     * Opens a window allowing a user to edit their answer.
     * Retrieves the current answer content from the database and
     * provides a text area for editing and a save button.
     *
     * @param replyId The ID of the reply being edited
     * @param primaryStage The primary stage of the application
     * @param currentUser The username of the current user
     * @param questionHandler The handler for question-related operations
     * @param answerHandler The handler for answer-related operations
     * @param userHandler The handler for user-related operations
     * @param userName The username of the current user (possibly redundant with currentUser)
     * @param databaseHelper The database helper for database operations
     */
    private void showEditAnswerScreen(int replyId, Stage primaryStage, String currentUser, 
                                      QuestionHandler questionHandler, AnswerHandler answerHandler, 
                                      UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
        try {
            String currentContent = answerHandler.getReplyContentById(replyId);
            Stage editStage = new Stage();

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));

            TextArea editArea = new TextArea(currentContent);
            editArea.setWrapText(true);
            editArea.setPrefRowCount(5);

            Button saveBtn = new Button("Save Changes");
            saveBtn.setOnAction(e -> {
                String newContent = editArea.getText().trim();
                if (!newContent.isEmpty()) {
                    if (answerHandler.updateReply(replyId, newContent)) {
                        show(primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper);
                        editStage.close();
                    } else {
                        showAlert("Error", "Failed to update answer", Alert.AlertType.ERROR);
                    }
                }
            });

            layout.getChildren().addAll(new Label("Edit your answer:"), editArea, saveBtn);
            editStage.setScene(new Scene(layout, 400, 300));
            editStage.initOwner(primaryStage);
            editStage.show();
        } catch (SQLException ex) {
            showAlert("Error", "Could not load answer content", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified title, message, and type.
     *
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     * @param type The type of alert (e.g., ERROR, INFORMATION)
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}