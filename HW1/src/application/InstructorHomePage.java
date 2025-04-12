package application;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays the instructor interface with notifications and reviewer approval functionality.
 */
public class InstructorHomePage {
    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private UserHandler uHandler;
    private ReviewHandler rHandler;
    private String userName; // Added to track current user

    public InstructorHomePage(DatabaseHelper databaseHelper, QuestionHandler qHandler, 
                            AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    public void show(Stage primaryStage, String userName) {
        this.userName = userName;
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        // Label to display Hello user
        Label userLabel = new Label("Hello, " + userName + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to view notifications
        Button notificationsButton = new Button("View Notifications");
        notificationsButton.setOnAction(event -> showNotifications(primaryStage));

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        });

        layout.getChildren().addAll(userLabel, notificationsButton, logoutButton);
        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Instructor Dashboard");
    }

    private void showNotifications(Stage primaryStage) {
        int userId = databaseHelper.getUserIdByUsername(userName);
        String notifications = databaseHelper.getNotifications(userId);

        Stage notificationStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        if (notifications == null || notifications.isEmpty()) {
            layout.getChildren().add(new Label("No new notifications"));
        } else {
            ScrollPane scrollPane = new ScrollPane();
            VBox notificationsBox = new VBox(10);
            
            for (String notification : notifications.split("\n")) {
                if (notification.trim().isEmpty()) continue;
                
                HBox notificationBox = new HBox(10);
                Label notificationLabel = new Label(notification);
                
                if (notification.contains("Request to become reviewer from student:")) {
                    String[] parts = notification.split("student: ")[1].split(" \\(");
                    String studentUsername = parts[0];
                    
                    // Create button panel for reviewer request actions
                    HBox buttonPanel = new HBox(10);
                    
                    Button viewActivityButton = new Button("View Activity");
                    viewActivityButton.setOnAction(e -> showStudentActivity(studentUsername));
                    
                    Button approveButton = new Button("Approve");
                    approveButton.setOnAction(e -> handleReviewerApproval(studentUsername, notification, userId, notificationStage, primaryStage));
                    
                    Button rejectButton = new Button("Reject");
                    rejectButton.setOnAction(e -> {
                        databaseHelper.deleteNotificationLine(userId, notification);
                        notificationStage.close();
                        showNotifications(primaryStage);
                    });
                    
                    buttonPanel.getChildren().addAll(viewActivityButton, approveButton, rejectButton);
                    notificationBox.getChildren().addAll(notificationLabel, buttonPanel);
                } else {
                    notificationBox.getChildren().add(notificationLabel);
                }
                
                notificationsBox.getChildren().add(notificationBox);
            }
            
            scrollPane.setContent(notificationsBox);
            layout.getChildren().add(scrollPane);
        }


        Button clearButton = new Button("Clear Notifications");
        clearButton.setOnAction(e -> {
            databaseHelper.clearNotifications(userId);
            notificationStage.close();
            show(primaryStage, userName); // Refresh the view
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> notificationStage.close());

        HBox buttonBox = new HBox(10, clearButton, closeButton);
        layout.getChildren().add(buttonBox);

        notificationStage.setScene(new Scene(layout, 600, 400));
        notificationStage.setTitle("Your Notifications");
        notificationStage.show();
    }
    private void showStudentActivity(String studentUsername) {
        Stage activityStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        TabPane tabPane = new TabPane();
        
        // Tab for student's questions
        Tab questionsTab = new Tab("Questions");
        VBox questionsBox = new VBox(5);
        try {
            ResultSet questions = qHandler.getAllQuestions();
            while (questions.next()) {
                if (questions.getString("userName").equals(studentUsername)) {
                    String title = questions.getString("title");
                    String content = questions.getString("post");
                    
                    VBox questionBox = new VBox(5);
                    questionBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
                    questionBox.getChildren().addAll(
                        new Label("Title: " + title),
                        new Label("Content: " + content),
                        new Separator()
                    );
                    questionsBox.getChildren().add(questionBox);
                }
            }
        } catch (SQLException e) {
            questionsBox.getChildren().add(new Label("Error loading questions: " + e.getMessage()));
        }
        questionsTab.setContent(new ScrollPane(questionsBox));
        
        // Tab for student's replies
        Tab repliesTab = new Tab("Replies");
        VBox repliesBox = new VBox(5);
        try {
            String query = "SELECT * FROM Replies WHERE userName = ?";
            PreparedStatement pstmt = databaseHelper.connectToDatabase().prepareStatement(query);
            pstmt.setString(1, studentUsername);
            ResultSet replies = pstmt.executeQuery();
            
            while (replies.next()) {
                String replyContent = replies.getString("reply");
                int postId = replies.getInt("postId");
                boolean isPrivate = replies.getBoolean("isPrivate");
                
                VBox replyBox = new VBox(5);
                replyBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
                
                try {
                    String postTitle = qHandler.getPostTitleById(postId);
                    replyBox.getChildren().addAll(
                        new Label("On post: " + postTitle),
                        new Label("Reply: " + replyContent),
                        new Label("Visibility: " + (isPrivate ? "Private" : "Public")),
                        new Separator()
                    );
                } catch (SQLException e) {
                    replyBox.getChildren().add(new Label("Error loading post details"));
                }
                
                repliesBox.getChildren().add(replyBox);
            }
        } catch (SQLException e) {
            repliesBox.getChildren().add(new Label("Error loading replies: " + e.getMessage()));
        }
        repliesTab.setContent(new ScrollPane(repliesBox));
        
        tabPane.getTabs().addAll(questionsTab, repliesTab);
        layout.getChildren().addAll(
            new Label("Activity for student: " + studentUsername),
            tabPane
        );
        
        activityStage.setScene(new Scene(layout, 800, 600));
        activityStage.setTitle("Student Activity Review");
        activityStage.show();
    }

    private void handleReviewerApproval(String studentUsername, String notification, 
                                      int userId, Stage notificationStage, Stage primaryStage) {
        int studentId = databaseHelper.getUserIdByUsername(studentUsername);
		String currentRole = databaseHelper.getUserRole(studentUsername);
		String newRole = currentRole.contains("Reviewer") ? currentRole : 
		                currentRole.isEmpty() ? "Reviewer" : currentRole + ",Reviewer";
		
		if (databaseHelper.changeUserRole(studentId, newRole)) {
		    // Notify student
		    String approvalMsg = "Your reviewer request has been approved by " + userName;
		    databaseHelper.addNotificationToUser(approvalMsg, studentId);
		    
		    // Remove this notification
		    databaseHelper.deleteNotificationLine(userId, notification);
		    
		    Alert success = new Alert(Alert.AlertType.INFORMATION);
		    success.setTitle("Success");
		    success.setContentText(studentUsername + " is now a Reviewer");
		    success.showAndWait();
		    
		    // Refresh notifications
		    notificationStage.close();
		    showNotifications(primaryStage);
		}
    }

    
}
