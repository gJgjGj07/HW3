package application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Manages functionality related to viewing, creating, and interacting with questions in the application.
 * This class handles submission of new questions, viewing existing questions, and related operations 
 * such as editing, deleting, and marking questions as read.
 */
public class Question {
    // Handlers for managing questions, answers, and user-specific operations.
    private QuestionHandler questionHandler;
    private AnswerHandler answerHandler;
    private ReviewHandler rHandler;
    private UserHandler userHandler;
    // Username of the currently logged-in user.
    private String userName;
    private int userId;
    DatabaseHelper dbHelper;

    /**
     * Constructs a Question object with necessary handlers and user information.
     *
     * @param questionHandler Handler for question-related database operations
     * @param answerHandler Handler for answer-related database operations
     * @param userHandler Handler for user-related database operations
     * @param rHandler Handler for review-related database operations
     * @param userName Username of the currently logged-in user
     * @param dbHelper Database helper for direct database operations
     */
    public Question(QuestionHandler questionHandler, AnswerHandler answerHandler, 
                   UserHandler userHandler, ReviewHandler rHandler, String userName, DatabaseHelper dbHelper) {
        this.questionHandler = questionHandler;
        this.answerHandler = answerHandler;
        this.userHandler = userHandler;
        this.rHandler = rHandler;
        this.userName = userName;
        this.dbHelper = dbHelper;
        userId = dbHelper.getUserIdByUsername(userName);
    }

    /**
     * Adds a back button to the bottom left of the scene that returns to StudentHomePage.
     * @param primaryStage The main application stage.
     * @return A Button configured to navigate back to the home page
     */
    private Button createBackButton(Stage primaryStage) {
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> returnToHome(primaryStage));
        return backButton;
    }

    /**
     * Navigates back to the appropriate home page based on user role.
     * @param primaryStage The main application stage.
     */
    private void returnToHome(Stage primaryStage) {
        try {
            // Get the user's role from the database
            String role = dbHelper.getUserRoleById(dbHelper.getUserIdByUsername(userName));
            
            if (role != null && role.equalsIgnoreCase("student")) {
                StudentHomePage homePage = new StudentHomePage(questionHandler, answerHandler, userHandler, rHandler, userName, dbHelper);
                homePage.show(primaryStage);
            } else {
                // Default to ReviewerHomePage for any non-student role
                ReviewerHomePage homePage = new ReviewerHomePage(questionHandler, answerHandler, userHandler, rHandler, userName, dbHelper);
                homePage.show(primaryStage);
            }
        } catch (Exception e) {
            // Fallback to student home if there's an error checking role
            System.err.println("Error determining user role: " + e.getMessage());
            StudentHomePage homePage = new StudentHomePage(questionHandler, answerHandler, userHandler, rHandler, userName, dbHelper);
            homePage.show(primaryStage);
        }
    }
    
    /**
     * Displays the scene for submitting a new question.
     * Creates a form with fields for title and question content, with character counters
     * and validation.
     *
     * @param primaryStage The main application stage.
     */
    public void showQuestionSubmissionScene(Stage primaryStage) {
        // Create a vertical layout with 10px spacing.
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // --- Title Components ---
        Label titleLabel = new Label("Title (optional, max 100 characters):");
        TextField titleField = new TextField();
        Label titleCharLabel = new Label("0/100");
        titleCharLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Listener to update character count for the title.
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            titleCharLabel.setText(length + "/100");
            // Change color to red if length exceeds 100 characters.
            titleCharLabel.setStyle(length > 100 ? "-fx-text-fill: red;" : "-fx-text-fill: #666;");
        });

        // --- Question Components ---
        Label promptLabel = new Label("Enter your question:");
        TextArea questionArea = new TextArea();
        questionArea.setPrefRowCount(5);
        Label questionCharLabel = new Label("0 characters");
        questionCharLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Listener to update character count for the question content.
        questionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.trim().length();
            questionCharLabel.setText(length + " characters");
        });

        // Submit button to post the question.
        Button submitButton = new Button("Submit Question");
        submitButton.setOnAction(e -> {
            // Retrieve and trim input values.
            String title = titleField.getText().trim();
            String question = questionArea.getText().trim();
            
            // Validate question input using QuestionHandler.
            List<String> errors = questionHandler.validateQuestion(title, question);
            
            if (!errors.isEmpty()) {
                // Show error messages if validation fails.
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                         String.join("\n\n", errors));
            } else {
                // Add the post and display success message.
                questionHandler.addPost(userName, title, question);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Question submitted!");
                returnToHome(primaryStage);
            }
        });

        HBox backButtonBox = new HBox(createBackButton(primaryStage));
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        
        layout.getChildren().addAll(
            titleLabel, titleField, titleCharLabel, new Label(""),
            promptLabel, questionArea, questionCharLabel,
            submitButton, backButtonBox
        );

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Submit Question");
    }

    /**
     * Displays all posts in a scrollable view along with filtering options.
     * Allows users to filter questions by read status and search by keywords.
     *
     * @param primaryStage The main application stage.
     */
    public void showPosts(Stage primaryStage) {
        // Layout to hold individual posts.
        VBox postsLayout = new VBox(20);
        postsLayout.setPadding(new Insets(20, 20, 20, 20));
        postsLayout.setStyle("-fx-background-color: #F5F5F5;");

        // Dropdown for filtering posts.
        ComboBox<String> filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll(
            "Show All Posts", 
            "Show Read Posts", 
            "Show Unread Posts", 
            "Show Answered Posts"
        );
        filterDropdown.setValue("Show All Posts");
        
        // Create search bar and button.
        TextField searchField = new TextField();
        searchField.setPromptText("Search posts...");
        Button searchButton = new Button("Search");
        
        // Event handler for search button.
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                // If no keyword is entered, use the dropdown filter.
                refreshPosts(postsLayout, filterDropdown.getValue(), primaryStage);
            } else {
                refreshPostsSearch(postsLayout, keyword, primaryStage);
            }
        });
        
        // Refresh posts when filter selection changes.
        filterDropdown.setOnAction(e -> {
            // Clear search field when changing filter.
            searchField.clear();
            refreshPosts(postsLayout, filterDropdown.getValue(), primaryStage);
        });
        
        // Create an HBox to hold the filter dropdown and search bar inline.
        HBox controlsBox = new HBox(10, filterDropdown, searchField, searchButton);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.setPadding(new Insets(10));
        
        // Initial loading of posts.
        refreshPosts(postsLayout, filterDropdown.getValue(), primaryStage);

        // Main layout with padding to prevent edge-sticking
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));  // Add padding around main content
        mainLayout.getChildren().addAll(controlsBox, postsLayout);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        // Back button with proper alignment and padding
        HBox backButtonBox = new HBox(createBackButton(primaryStage));
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(10)); // Added padding around the back button
        HBox.setMargin(backButtonBox, new Insets(15, 0, 0, 20));  // Add left margin

        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(scrollPane);
        rootLayout.setBottom(backButtonBox);  // Position back button at bottom

        primaryStage.setScene(new Scene(rootLayout, 800, 500));
        primaryStage.setTitle("Questions");
        primaryStage.show();
    }

    /**
     * Refreshes the posts displayed based on the selected filter.
     * Retrieves posts from the database according to the filter criteria and 
     * renders them in the UI.
     *
     * @param postsLayout  The layout that holds the posts.
     * @param filter       The selected filter option.
     * @param primaryStage The main application stage.
     */
    private void refreshPosts(VBox postsLayout, String filter, Stage primaryStage) {
        // Clear the current posts.
        postsLayout.getChildren().clear();
        
        try {
            // Get posts based on the applied filter.
            ResultSet rs = getFilteredResultSet(filter);
            while (rs.next()) {
                int postId = rs.getInt("postId");
                String postUser = rs.getString("userName");
                String title = rs.getString("title");
                String content = rs.getString("post");
                
                // Create a pane for each post.
                BorderPane postPane = createPostPane(postId, postUser, title, content, postsLayout, primaryStage);
                postsLayout.getChildren().add(postPane);
            }
        } catch (SQLException e) {
            // Display an error if posts cannot be loaded.
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load posts");
            e.printStackTrace();
        }
    }
    
    /**
     * Refreshes the posts displayed based on a keyword search.
     * Retrieves posts containing the keyword from the database and renders them in the UI.
     * 
     * @param postsLayout  The layout that holds the posts.
     * @param keyword      The keyword to search for in posts.
     * @param primaryStage The main application stage.
     */
    private void refreshPostsSearch(VBox postsLayout, String keyword, Stage primaryStage) {
        // Clear the current posts.
        postsLayout.getChildren().clear();
        
        try {
            // Get posts based on the applied filter.
            ResultSet rs = questionHandler.searchPostsByKeyword(keyword);
            while (rs.next()) {
                int postId = rs.getInt("postId");
                String postUser = rs.getString("userName");
                String title = rs.getString("title");
                String content = rs.getString("post");
                
                // Create a pane for each post.
                BorderPane postPane = createPostPane(postId, postUser, title, content, postsLayout, primaryStage);
                postsLayout.getChildren().add(postPane);
            }
        } catch (SQLException e) {
            // Display an error if posts cannot be loaded.
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load posts");
            e.printStackTrace();
        }
    }
    
    /**
     * Returns a ResultSet filtered by the selected option.
     * Maps filter options to appropriate database queries.
     *
     * @param filter The filter option.
     * @return A ResultSet containing posts based on the filter.
     * @throws SQLException if a database access error occurs.
     */
    private ResultSet getFilteredResultSet(String filter) throws SQLException {
        // Retrieve list of post IDs that the user has already read.
        List<Integer> readPosts = userHandler.getReadPosts(userName);
        
        // Return the appropriate ResultSet based on the filter.
        switch (filter) {
            case "Show Read Posts":
                return questionHandler.getReadQuestions(readPosts);
            case "Show Unread Posts":
                return questionHandler.getUnreadQuestions(readPosts);
            case "Show Answered Posts":
                return questionHandler.getAnsweredQuestions();
            default:
                return questionHandler.getAllQuestions();
        }
    }

    /**
     * Creates a visual pane for an individual post with title, content, metadata, and action buttons.
     * Each post is displayed in a BorderPane with appropriate styling and interactive elements.
     *
     * @param postId       The ID of the post.
     * @param postUser     The username of the person who posted.
     * @param title        The title of the post.
     * @param content      The content of the post.
     * @param postsLayout  The container layout for posts.
     * @param primaryStage The main application stage.
     * @return A BorderPane representing the post.
     */
    private BorderPane createPostPane(int postId, String postUser, String title, String content, 
                                    VBox postsLayout, Stage primaryStage) {
        // Create the main pane for the post.
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        pane.setPrefWidth(760);

        // --- Title Section ---
        Label titleLabel = new Label(title.isEmpty() ? "(No Title)" : title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // --- Content Section ---
        TextArea contentArea = new TextArea(content);
        contentArea.setStyle("-fx-font-size: 14px;");
        contentArea.setWrapText(true);
        contentArea.setEditable(false);

        // --- Metadata Section ---
        Label userLabel = new Label("Posted by: " + postUser);
        userLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        // Arrange title, content, and metadata vertically.
        VBox contentBox = new VBox(5, titleLabel, contentArea, userLabel);
        pane.setCenter(contentBox);

        // Add action buttons (reply, view answers, edit, delete).
        setupActionButtons(pane, postId, postUser, postsLayout, primaryStage);

        // Setup hover tracking to mark the post as read.
        setupHoverTracking(pane, postId);

        return pane;
    }

    /**
     * Sets up action buttons for a post pane.
     * Adds different buttons based on whether the current user is the post owner,
     * including Reply, View Answers, Review, and notification functionality.
     *
     * @param pane         The post pane to add buttons to.
     * @param postId       The ID of the post.
     * @param postUser     The username of the post creator.
     * @param postsLayout  The layout containing all posts.
     * @param primaryStage The main application stage.
     */
    private void setupActionButtons(BorderPane pane, int postId, String postUser, 
            VBox postsLayout, Stage primaryStage) {
        // Create a single HBox to hold all action buttons.
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // If the current user is the post owner, add Edit and Delete buttons first.
        if (postUser.equals(userName)) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-text-fill: blue;");
            editBtn.setOnAction(e -> showEditPostScreen(postId, primaryStage, postsLayout));
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-text-fill: red;");
            deleteBtn.setOnAction(e -> deletePost(postId, postsLayout, pane));
            
            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }
        
        // Add the common action buttons.
        Button replyBtn = new Button("Reply");
        replyBtn.setOnAction(e -> new Answer(answerHandler, rHandler, postId).writeAnswer(userName, primaryStage));
        
        Button answersBtn = new Button("Answers");
        answersBtn.setOnAction(e -> new Answer(answerHandler, rHandler, postId).show(primaryStage, userName, questionHandler, answerHandler, userHandler, userName, dbHelper));
        
        // Updated Reviews button for questions.
        Button reviewsBtn = new Button("Reviews");
        reviewsBtn.setOnAction(e -> {
            // For a question, we instantiate the Review window with the postId and set isForPost=true.
            application.Review reviewWindow = new application.Review(rHandler, postId, true);
            reviewWindow.showReviews(primaryStage, userName, dbHelper);
        });
        
        Button notifyBtn = new Button("Notify Creator");
        notifyBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Send Notification");
            dialog.setHeaderText("Send a private notification to " + postUser);
            dialog.setContentText("Enter your notification message:");
            
            dialog.showAndWait().ifPresent(message -> {
                int postCreatorId = dbHelper.getUserIdByUsername(postUser);
                boolean success = dbHelper.addNotificationToUser(userName + ": " + message, postCreatorId);
                
                if (success) {
                    answerHandler.addAnswer("[PRIVATE] " + message, postId, userName, true);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Notification sent to " + postUser);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send notification");
                }
            });
        });
        
        // Add buttons to the buttonBox in the desired order.
        buttonBox.getChildren().addAll(replyBtn, answersBtn, reviewsBtn, notifyBtn);
        
        // Place the combined HBox at the bottom of the pane.
        pane.setBottom(buttonBox);
    }

    /**
     * Displays an edit screen for modifying an existing post.
     * Creates a modal dialog with fields pre-filled with the post's current content.
     *
     * @param postId       The ID of the post to edit.
     * @param primaryStage The main application stage.
     * @param postsLayout  The container layout for posts (to be refreshed after editing).
     */
    private void showEditPostScreen(int postId, Stage primaryStage, VBox postsLayout) {
        try {
            // Retrieve current title and content for the post.
            String currentTitle = questionHandler.getPostTitleById(postId);
            String currentContent = questionHandler.getPostContentById(postId);
            
            // Create a new stage for editing the post.
            Stage editStage = new Stage();
            editStage.setTitle("Edit Post");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            
            // --- Title Editor ---
            Label titleLabel = new Label("Title:");
            TextField titleField = new TextField(currentTitle);
            Label titleCharLabel = new Label(currentTitle.length() + "");
            // Update character count for title.
            titleField.textProperty().addListener((obs, oldVal, newVal) -> {
                int length = newVal.length();
                titleCharLabel.setText(length + "");
                titleCharLabel.setStyle(length > 100 ? "-fx-text-fill: red;" : "-fx-text-fill: #666;");
            });
            
            // --- Content Editor ---
            Label contentLabel = new Label("Content:");
            TextArea contentArea = new TextArea(currentContent);
            contentArea.setPrefRowCount(8);
            contentArea.setWrapText(true);
            
            // Save button to update the post.
            Button saveBtn = new Button("Save Changes");
            saveBtn.setOnAction(e -> {
                String newTitle = titleField.getText().trim();
                String newContent = contentArea.getText().trim();
                
                // Validate title length.
                if (newTitle.length() > 100) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Title", 
                             "Title cannot exceed 100 characters");
                    return;
                }
                
                // Attempt to update the post and refresh the posts view.
                if (questionHandler.updatePost(postId, newTitle, newContent)) {
                    refreshPosts(postsLayout, "Show All Posts", primaryStage);
                    editStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update post");
                }
            });
            
            // Add editor components to the layout.
            layout.getChildren().addAll(
                titleLabel,
                titleField,
                titleCharLabel,
                contentLabel,
                contentArea,
                saveBtn
            );
            
            editStage.setScene(new Scene(layout, 800, 400));
            editStage.initOwner(primaryStage);
            editStage.show();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load post content");
            ex.printStackTrace();
        }
    }

    /**
     * Sets up a hover listener on the post pane that marks the post as read
     * after 5 seconds of continuous mouse hover.
     * This implements the automatic read-tracking functionality.
     *
     * @param pane   The pane representing the post.
     * @param postId The ID of the post.
     */
    private void setupHoverTracking(BorderPane pane, int postId) {
        // Create a PauseTransition for 5 seconds.
        PauseTransition hoverTimer = new PauseTransition(Duration.seconds(5));
        hoverTimer.setOnFinished(e -> {
            // Mark the post as read after the hover duration.
            userHandler.addPostRead(userName, postId);
            System.out.println("Marked post " + postId + " as read");
        });
        // Start timer when mouse enters; stop when it exits.
        pane.setOnMouseEntered(e -> hoverTimer.playFromStart());
        pane.setOnMouseExited(e -> hoverTimer.stop());
    }

    /**
     * Deletes a post and its associated replies from the database.
     * Removes the post from both the database and the UI.
     *
     * @param postId      The ID of the post to delete.
     * @param postsLayout The layout containing all post panes.
     * @param pane        The pane corresponding to the post.
     */
    private void deletePost(int postId, VBox postsLayout, BorderPane pane) {
        try {
            // Attempt to delete the post.
            boolean postDeleted = questionHandler.deletePostById(postId);
            boolean repliesDeleted = true;
            // If there are replies, attempt to delete them.
            if (questionHandler.getNumReplies(postId) > 0) {
                repliesDeleted = answerHandler.deleteReplyByPostId(postId);
            }
            
            // If deletion was successful, remove the post pane.
            if (postDeleted && repliesDeleted) {
                postsLayout.getChildren().remove(pane);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post deleted");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database error during deletion");
            e.printStackTrace();
        }
    }

    /**
     * Utility method to display an alert dialog.
     * Creates and shows a JavaFX Alert with the specified type, title, and message.
     *
     * @param type    The type of alert (e.g., ERROR, INFORMATION).
     * @param title   The title of the alert dialog.
     * @param message The message to display in the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}