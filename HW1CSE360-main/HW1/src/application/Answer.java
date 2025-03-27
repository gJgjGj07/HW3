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

public class Answer {

    private AnswerHandler answerHandler;
    private int postId;
    private String currentSortOrder = "None";

    private static class Reply {
        private int replyId;
        private String content;
        private String userName;
        private int likes;
        private int numReplies;

        public Reply(int replyId, String content, String userName, int likes, int numReplies) {
            this.replyId = replyId;
            this.content = content;
            this.userName = userName;
            this.likes = likes;
            this.numReplies = numReplies;
        }

        public int getReplyId() { return replyId; }
        public String getContent() { return content; }
        public String getUserName() { return userName; }
        public int getLikes() { return likes; }
        public int getNumReplies() { return numReplies; }
    }

    public Answer(AnswerHandler answerHandler, int postId) {
        this.answerHandler = answerHandler;
        this.postId = postId;
    }

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
            charCountLabel.setStyle(
                length < 5 ? "-fx-text-fill: orange;" : "-fx-text-fill: #666;"
            );
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

    public void show(Stage primaryStage, String currentUser, QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
    	BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        // Create back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new Question(questionHandler, answerHandler, userHandler, userName, databaseHelper).showPosts(primaryStage);
        });

        // Container for bottom section
        HBox bottomLayout = new HBox(backButton);
        bottomLayout.setAlignment(Pos.CENTER_LEFT);
        bottomLayout.setPadding(new Insets(10, 0, 0, 0)); // Top padding only
        
        ComboBox<String> sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("None", "Most Likes");
        sortComboBox.setValue(currentSortOrder);

        sortComboBox.setOnAction(e -> {
            currentSortOrder = sortComboBox.getValue();
            VBox answersLayout = createAnswersLayout(currentUser, primaryStage, questionHandler, answerHandler, userHandler, userName, databaseHelper);
            ScrollPane scrollPane = new ScrollPane(answersLayout);
            scrollPane.setFitToWidth(true);
            rootLayout.setCenter(scrollPane);
        });

        VBox answersLayout = createAnswersLayout(currentUser, primaryStage, questionHandler, answerHandler, userHandler, userName, databaseHelper);
        ScrollPane scrollPane = new ScrollPane(answersLayout);
        scrollPane.setFitToWidth(true);

        rootLayout.setTop(sortComboBox);
        rootLayout.setCenter(scrollPane);
        rootLayout.setBottom(bottomLayout);

        Scene scene = new Scene(rootLayout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Display Answers");
        primaryStage.show();
        
    }

    private VBox createAnswersLayout(String currentUser, Stage primaryStage, QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
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
                BorderPane answerPane = createAnswerPane(reply, currentUser, primaryStage, questionHandler, answerHandler, userHandler, userName, databaseHelper);
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

    private BorderPane createAnswerPane(Reply reply, String currentUser, Stage primaryStage, QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
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
        if (reply.getUserName().equals(currentUser)) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            editButton.setOnAction(e -> showEditAnswerScreen(reply.getReplyId(), primaryStage, currentUser, questionHandler, answerHandler, userHandler, userName, databaseHelper));

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
                    boolean success = answerHandler.addReplyToReply(
                        reply.getReplyId(), replyText, currentUser, false
                    );
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
                    boolean success = answerHandler.addReplyToReply(
                        reply.getReplyId(), replyText, currentUser, false
                    );
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
        BorderPane.setMargin(buttonBox, new Insets(0, 10, 0, 0));

        return answerPane;
    }

    private void showEditAnswerScreen(int replyId, Stage primaryStage, String currentUser, QuestionHandler questionHandler, AnswerHandler answerHandler, UserHandler userHandler, String userName, DatabaseHelper databaseHelper) {
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
            
            layout.getChildren().addAll(
                new Label("Edit your answer:"),
                editArea,
                saveBtn
            );
            
            editStage.setScene(new Scene(layout, 400, 300));
            editStage.initOwner(primaryStage);
            editStage.show();
        } catch (SQLException ex) {
            showAlert("Error", "Could not load answer content", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


