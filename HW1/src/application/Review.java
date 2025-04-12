package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import databasePart1.DatabaseHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the display and interaction with reviews for posts and replies.
 * Provides functionality for creating, viewing, updating reviews and managing reviewer profiles.
 */
public class Review {
    private ReviewHandler reviewHandler;
    // Instead of replyId, we now use targetId.
    // If isForPost is true, then targetId is a postId; otherwise, it's a replyId.
    private int targetId;
    private boolean isForPost;
    private String currentSortOrder = "None";

    /**
     * Inner class representing a single review record with its metadata.
     */
    private static class ReviewRecord {
        private int reviewId;
        private String content;
        private String reviewerName;
        private int feedbackCount;

        /**
         * Constructs a ReviewRecord with the specified details.
         *
         * @param reviewId the unique identifier for the review
         * @param content the text content of the review
         * @param reviewerName the name of the reviewer
         * @param feedbackCount the number of feedback messages received
         */
        public ReviewRecord(int reviewId, String content, String reviewerName, int feedbackCount) {
            this.reviewId = reviewId;
            this.content = content;
            this.reviewerName = reviewerName;
            this.feedbackCount = feedbackCount;
        }

        /**
         * @return the review ID
         */
        public int getReviewId() { return reviewId; }
        
        /**
         * @return the review content
         */
        public String getContent() { return content; }
        
        /**
         * @return the reviewer's name
         */
        public String getReviewerName() { return reviewerName; }
        
        /**
         * @return the feedback count for this review
         */
        public int getFeedbackCount() { return feedbackCount; }
    }

    /**
     * Constructs a new Review manager for a specific target (post or reply).
     *
     * @param reviewHandler the ReviewHandler instance for database operations
     * @param targetId the ID of the post or reply being reviewed
     * @param isForPost true if the target is a post, false if it's a reply
     */
    public Review(ReviewHandler reviewHandler, int targetId, boolean isForPost) {
        this.reviewHandler = reviewHandler;
        this.targetId = targetId;
        this.isForPost = isForPost;
    }

    /**
     * Displays the main review management window for the current target.
     *
     * @param primaryStage the parent stage for this window
     * @param currentUser the username of the currently logged-in user
     * @param databaseHelper the database helper instance
     */
    public void showReviews(Stage primaryStage, String currentUser, DatabaseHelper databaseHelper) {
        Stage reviewWindow = new Stage();
        String targetType = isForPost ? "Post" : "Reply";
        reviewWindow.setTitle("Review Manager - Reviews for " + targetType + " ID: " + targetId);

        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        // Top: Title
        Label title = new Label("Reviews for " + targetType + " ID: " + targetId);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootLayout.setTop(title);
        BorderPane.setMargin(title, new Insets(10));

        // Center: List of reviews for this target.
        VBox reviewsLayout = createReviewsLayout(currentUser, primaryStage, databaseHelper);
        ScrollPane scrollPane = new ScrollPane(reviewsLayout);
        scrollPane.setFitToWidth(true);
        rootLayout.setCenter(scrollPane);

        // Bottom: Action buttons (only for users with role "reviewer")
        HBox bottomLayout = new HBox(10);
        bottomLayout.setPadding(new Insets(10));
        bottomLayout.setAlignment(Pos.CENTER_LEFT);

        int userId = databaseHelper.getUserIdByUsername(currentUser);
        String role = databaseHelper.getUserRoleById(userId);
        if ("reviewer".equalsIgnoreCase(role)) {
            Button createReviewButton = new Button("Create Review");
            createReviewButton.setOnAction(e -> createReview(currentUser, reviewWindow, databaseHelper));
            Button myProfileButton = new Button("My Profile");
            myProfileButton.setOnAction(e -> showReviewerProfile(currentUser, reviewWindow, databaseHelper));
            bottomLayout.getChildren().addAll(createReviewButton, myProfileButton);
        }
        rootLayout.setBottom(bottomLayout);

        Scene scene = new Scene(rootLayout, 600, 500);
        reviewWindow.setScene(scene);
        reviewWindow.initOwner(primaryStage);
        reviewWindow.setX(primaryStage.getX() + 130);
        reviewWindow.setY(primaryStage.getY() + 30);
        reviewWindow.show();
    }

    /**
     * Creates and displays a window for submitting a new review.
     *
     * @param reviewerName the name of the reviewer
     * @param primaryStage the parent stage for this window
     * @param databaseHelper the database helper instance
     */
    public void createReview(String reviewerName, Stage primaryStage, DatabaseHelper databaseHelper) {
        Stage reviewStage = new Stage();
        String targetType = isForPost ? "Post" : "Reply";
        reviewStage.setTitle("Create Review for " + targetType + " ID: " + targetId);

        TextArea reviewTextArea = new TextArea();
        reviewTextArea.setPromptText("Write your review here...");
        reviewTextArea.setWrapText(true);
        reviewTextArea.setPrefRowCount(5);

        Button submitReviewButton = new Button("Submit Review");
        submitReviewButton.setOnAction(e -> {
            String reviewContent = reviewTextArea.getText().trim();
            List<String> errors = reviewHandler.validateReview(reviewContent);
            if (!errors.isEmpty()) {
                showAlert("Validation Error", String.join("\n", errors), AlertType.ERROR);
            } else {
                boolean success;
                if (isForPost) {
                    success = reviewHandler.addReviewForPost(reviewContent, reviewerName, targetId);
                } else {
                    success = reviewHandler.addReviewForReply(reviewContent, reviewerName, targetId);
                }
                if (success) {
                    showAlert("Success", "Review submitted successfully!", AlertType.INFORMATION);
                    reviewStage.close();
                    // Refresh the review screen.
                    showReviews(primaryStage, reviewerName, databaseHelper);
                } else {
                    showAlert("Error", "Failed to submit review.", AlertType.ERROR);
                }
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(new Label("Write Your Review:"), reviewTextArea, submitReviewButton);

        Scene scene = new Scene(layout, 500, 350);
        reviewStage.setScene(scene);
        reviewStage.initOwner(primaryStage);
        reviewStage.show();
    }

    /**
     * Creates and returns a layout containing all reviews for the current target.
     *
     * @param currentUser the username of the currently logged-in user
     * @param primaryStage the parent stage
     * @param databaseHelper the database helper instance
     * @return VBox containing all review panes
     */
    private VBox createReviewsLayout(String currentUser, Stage primaryStage, DatabaseHelper databaseHelper) {
        VBox reviewsLayout = new VBox(20);
        reviewsLayout.setPadding(new Insets(20));
        reviewsLayout.setStyle("-fx-background-color: #F5F5F5;");

        // Add sorting options at the top
        HBox sortOptions = new HBox(10);
        sortOptions.setAlignment(Pos.CENTER_LEFT);
        
        Label sortLabel = new Label("Sort by:");
        Button normalSortButton = new Button("Normal");
        Button topReviewersButton = new Button("Top Reviewers");
        Button trustedReviewsButton = new Button("Trusted Reviews");
        
        normalSortButton.setOnAction(e -> {
            currentSortOrder = "None";
            refreshReviews(reviewsLayout, currentUser, primaryStage, databaseHelper);
        });
        
        topReviewersButton.setOnAction(e -> {
            currentSortOrder = "TopReviewers";
            refreshReviews(reviewsLayout, currentUser, primaryStage, databaseHelper);
        });
        
        trustedReviewsButton.setOnAction(e -> {
            currentSortOrder = "Trusted";
            refreshReviews(reviewsLayout, currentUser, primaryStage, databaseHelper);
        });
        
        sortOptions.getChildren().addAll(sortLabel, normalSortButton, topReviewersButton, trustedReviewsButton);
        reviewsLayout.getChildren().add(sortOptions);

        refreshReviews(reviewsLayout, currentUser, primaryStage, databaseHelper);
        return reviewsLayout;
    }

    private void refreshReviews(VBox reviewsLayout, String currentUser, Stage primaryStage, DatabaseHelper databaseHelper) {
        // Clear existing reviews (keep the sort buttons)
        if (reviewsLayout.getChildren().size() > 1) {
            reviewsLayout.getChildren().remove(1, reviewsLayout.getChildren().size());
        }

        try {
            ResultSet rs;
            if (isForPost) {
                rs = reviewHandler.getReviewsByPostId(targetId);
            } else {
                rs = reviewHandler.getReviewsByReplyId(targetId);
            }
            List<ReviewRecord> reviews = new ArrayList<>();
            while (rs.next()) {
                int reviewId = rs.getInt("reviewId");
                String content = rs.getString("content");
                String reviewerName = rs.getString("reviewerName");
                int feedbackCount = rs.getInt("feedbackCount");
                reviews.add(new ReviewRecord(reviewId, content, reviewerName, feedbackCount));
            }

            // Apply sorting based on currentSortOrder
            List<ReviewRecord> sortedReviews = sortReviews(reviews, currentUser, databaseHelper);

            for (ReviewRecord review : sortedReviews) {
                BorderPane reviewPane = createReviewPane(review, currentUser, primaryStage, databaseHelper);
                reviewsLayout.getChildren().add(reviewPane);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load reviews", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private List<ReviewRecord> sortReviews(List<ReviewRecord> reviews, String currentUser, DatabaseHelper databaseHelper) {
        switch (currentSortOrder) {
            case "TopReviewers":
                return sortByTopReviewers(reviews, currentUser, databaseHelper);
            case "Trusted":
                return filterTrustedReviews(reviews, currentUser, databaseHelper);
            default:
                return reviews; // Normal view - no sorting
        }
    }

    private List<ReviewRecord> sortByTopReviewers(List<ReviewRecord> reviews, String currentUser, DatabaseHelper databaseHelper) {
        try {
            // Create a map of reviewer names to their average ratings
            Map<String, Double> reviewerRatings = new HashMap<>();
            
            for (ReviewRecord review : reviews) {
                // Only calculate rating once per reviewer
                if (!reviewerRatings.containsKey(review.getReviewerName())) {
                    // Get all ratings for this reviewer from the current user
                    Integer rating = databaseHelper.getReviewerRating(currentUser, review.getReviewerName());
                    // If no rating exists, use 0 as default
                    reviewerRatings.put(review.getReviewerName(), rating != null ? rating.doubleValue() : 0.0);
                }
            }
            
            // Sort reviews by reviewer rating (descending), then by feedback count (descending)
            reviews.sort((a, b) -> {
                double ratingA = reviewerRatings.get(a.getReviewerName());
                double ratingB = reviewerRatings.get(b.getReviewerName());
                
                // First compare by rating
                int ratingCompare = Double.compare(ratingB, ratingA);
                if (ratingCompare != 0) return ratingCompare;
                
                // If ratings are equal, compare by feedback count
                return Integer.compare(b.getFeedbackCount(), a.getFeedbackCount());
            });
            
            return reviews;
        } catch (SQLException e) {
            e.printStackTrace();
            return reviews; // Fallback to original order if error occurs
        }
    }

    private List<ReviewRecord> filterTrustedReviews(List<ReviewRecord> reviews, String currentUser, DatabaseHelper databaseHelper) {
        try {
            List<ReviewRecord> trustedReviews = new ArrayList<>();
            Map<String, Double> trustedReviewerRatings = new HashMap<>();
            
            // First collect all trusted reviewers and their ratings
            for (ReviewRecord review : reviews) {
                String reviewer = review.getReviewerName();
                if (databaseHelper.isReviewerTrusted(reviewer, currentUser)) {
                    trustedReviews.add(review);
                    if (!trustedReviewerRatings.containsKey(reviewer)) {
                        // Get the current user's rating for this reviewer
                        Integer rating = databaseHelper.getReviewerRating(currentUser, reviewer);
                        trustedReviewerRatings.put(reviewer, rating != null ? rating.doubleValue() : 0.0);
                    }
                }
            }
            
            // Sort trusted reviews by their rating (descending), then by feedback count
            trustedReviews.sort((a, b) -> {
                double ratingA = trustedReviewerRatings.get(a.getReviewerName());
                double ratingB = trustedReviewerRatings.get(b.getReviewerName());
                
                // First compare by rating
                int ratingCompare = Double.compare(ratingB, ratingA);
                if (ratingCompare != 0) return ratingCompare;
                
                // If ratings are equal, compare by feedback count
                return Integer.compare(b.getFeedbackCount(), a.getFeedbackCount());
            });
            
            return trustedReviews;
        } catch (SQLException e) {
            e.printStackTrace();
            return reviews; // Fallback to original order if error occurs
        }
    }

    /**
     * Creates an individual pane for displaying a single review.
     *
     * @param review the review record to display
     * @param currentUser the username of the currently logged-in user
     * @param primaryStage the parent stage
     * @param databaseHelper the database helper instance
     * @return BorderPane containing the review display and action buttons
     */
    private BorderPane createReviewPane(ReviewRecord review, String currentUser, Stage primaryStage, DatabaseHelper databaseHelper) {
        BorderPane reviewPane = new BorderPane();
        reviewPane.setPadding(new Insets(10));
        reviewPane.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1;");
        reviewPane.setPrefWidth(760);

        TextArea reviewTextArea = new TextArea(review.getContent());
        reviewTextArea.setStyle("-fx-font-size: 16px;");
        reviewTextArea.setWrapText(true);
        reviewTextArea.setEditable(false);
        reviewPane.setCenter(reviewTextArea);

        Label reviewerLabel = new Label("Reviewed by: " + review.getReviewerName() 
                + " | Feedbacks: " + review.getFeedbackCount());
        reviewerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        reviewPane.setBottom(reviewerLabel);
        BorderPane.setMargin(reviewerLabel, new Insets(5, 0, 0, 0));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Check if this review has a previous version.
        if (reviewHandler.hasPreviousVersion(review.getReviewId())) {
            Button viewPreviousButton = new Button("View Previous Version");
            viewPreviousButton.setOnAction(e -> showPreviousVersion(review.getReviewId(), primaryStage));
            buttonBox.getChildren().add(viewPreviousButton);
        }
        
        // If the current user is the review author, allow update.
        if (review.getReviewerName().equals(currentUser)) {
            Button updateButton = new Button("Update");
            updateButton.setOnAction(e -> updateReview(review.getReviewId(), currentUser, primaryStage, databaseHelper));
            buttonBox.getChildren().add(updateButton);
        } else {
            // Otherwise, allow sending feedback.
            Button feedbackButton = new Button("Send Feedback");
            feedbackButton.setOnAction(e -> sendFeedback(review.getReviewId(), primaryStage, currentUser));
            buttonBox.getChildren().add(feedbackButton);
        }
        
        reviewPane.setRight(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(0, 10, 0, 0));

        return reviewPane;
    }

    /**
     * Displays the previous version of a review in a new window.
     *
     * @param reviewId the ID of the current review version
     * @param primaryStage the parent stage
     */
    private void showPreviousVersion(int reviewId, Stage primaryStage) {
        ReviewHandler.ReviewPrevious previousReview = reviewHandler.getPreviousReview(reviewId);
        
        if (previousReview == null) {
            showAlert("Not Found", "No previous version found for this review.", AlertType.INFORMATION);
            return;
        }
        
        Stage previousVersionStage = new Stage();
        previousVersionStage.setTitle("Previous Version of Review");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Previous Version");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TextArea contentArea = new TextArea(previousReview.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(10);
        
        Label infoLabel = new Label("Reviewed by: " + previousReview.getReviewerName() + 
                                   " | Feedback Count: " + previousReview.getFeedbackCount() +
                                   " | Review ID: " + previousReview.getReviewId());
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> previousVersionStage.close());
        
        layout.getChildren().addAll(titleLabel, contentArea, infoLabel, closeButton);
        
        Scene scene = new Scene(layout, 500, 400);
        previousVersionStage.setScene(scene);
        previousVersionStage.initOwner(primaryStage);
        previousVersionStage.setX(primaryStage.getX() + 50);
        previousVersionStage.setY(primaryStage.getY() + 50);
        previousVersionStage.show();
    }

    /**
     * Displays a window for sending feedback on a review.
     *
     * @param reviewId the ID of the review to provide feedback on
     * @param ownerStage the parent stage
     * @param currentUser the username of the feedback sender
     */
    public void sendFeedback(int reviewId, Stage ownerStage, String currentUser) {
        Stage feedbackStage = new Stage();
        feedbackStage.setTitle("Send Feedback for Review " + reviewId);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        Label prompt = new Label("Enter your feedback message:");
        TextArea feedbackArea = new TextArea();
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefRowCount(5);
        
        Button submitButton = new Button("Submit Feedback");
        submitButton.setOnAction(e -> {
            String feedbackMessage = feedbackArea.getText().trim();
            if (feedbackMessage.isEmpty()) {
                showAlert("Feedback message cannot be empty.", "Validation Error", Alert.AlertType.ERROR);
            } else {
                boolean success = reviewHandler.addFeedback(reviewId, currentUser, feedbackMessage);
                if (success) {
                    showAlert("Feedback submitted successfully!", "Success", Alert.AlertType.INFORMATION);
                    feedbackStage.close();
                } else {
                    showAlert("Failed to submit feedback.", "Error", Alert.AlertType.ERROR);
                }
            }
        });
        
        layout.getChildren().addAll(prompt, feedbackArea, submitButton);
        
        Scene scene = new Scene(layout, 400, 300);
        feedbackStage.setScene(scene);
        feedbackStage.initOwner(ownerStage);
        feedbackStage.setX(ownerStage.getX() + 30);
        feedbackStage.setY(ownerStage.getY() + 30);
        feedbackStage.show();
    }

    /**
     * Displays a window for updating an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param reviewerName the name of the reviewer
     * @param primaryStage the parent stage
     * @param databaseHelper the database helper instance
     */
    public void updateReview(int reviewId, String reviewerName, Stage primaryStage, DatabaseHelper databaseHelper) {
        Stage updateStage = new Stage();
        updateStage.setTitle("Update Review");

        String currentContent = reviewHandler.getReviewContentById(reviewId);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        Label infoLabel = new Label("Edit your review. A link to the previous version will be maintained.");
        TextArea updateArea = new TextArea(currentContent);
        updateArea.setWrapText(true);
        updateArea.setPrefRowCount(5);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            String newContent = updateArea.getText().trim();
            if (newContent.isEmpty()) {
                showAlert("Validation Error", "Updated review cannot be empty.", Alert.AlertType.ERROR);
            } else {
                boolean success = reviewHandler.updateReview(reviewId, newContent);
                if (success) {
                    showAlert("Success", "Review updated successfully!", Alert.AlertType.INFORMATION);
                    updateStage.close();
                    showReviews(primaryStage, reviewerName, databaseHelper);
                } else {
                    showAlert("Error", "Failed to update review.", Alert.AlertType.ERROR);
                }
            }
        });

        layout.getChildren().addAll(infoLabel, updateArea, saveButton);
        Scene scene = new Scene(layout, 500, 350);
        updateStage.setScene(scene);
        updateStage.initOwner(primaryStage);
        updateStage.show();
    }

    /**
     * Displays the profile window for a reviewer, showing their experience and reviews.
     *
     * @param reviewerName the name of the reviewer
     * @param primaryStage the parent stage
     * @param databaseHelper the database helper instance
     */
    public void showReviewerProfile(String reviewerName, Stage primaryStage, DatabaseHelper databaseHelper) {
        Stage profileStage = new Stage();
        profileStage.setTitle("Reviewer Profile: " + reviewerName);

        VBox profileLayout = new VBox(15);
        profileLayout.setPadding(new Insets(20));

        // Experience Section
        Label experiencePrompt = new Label("Experience:");
        Label experienceLabel = new Label();
        experienceLabel.setWrapText(true);
        experienceLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        
        String currentExperience = reviewHandler.getExperience(reviewerName);
        experienceLabel.setText(currentExperience != null ? currentExperience : "No experience details provided.");

        Button editExperienceButton = new Button("Edit Experience");
        VBox experienceContainer = new VBox(5, experienceLabel, editExperienceButton);

        editExperienceButton.setOnAction(e -> {
            TextArea experienceArea = new TextArea(experienceLabel.getText());
            experienceArea.setWrapText(true);
            experienceArea.setPrefRowCount(3);
            
            Button saveButton = new Button("Save");
            Button cancelButton = new Button("Cancel");
            HBox editButtons = new HBox(10, saveButton, cancelButton);
            
            experienceContainer.getChildren().setAll(new Label("Experience:"), experienceArea, editButtons);
            
            saveButton.setOnAction(ev -> {
                String newExperience = experienceArea.getText().trim();
                boolean success = reviewHandler.updateExperience(reviewerName, newExperience);
                if (success) {
                    showAlert("Success", "Experience saved successfully!", Alert.AlertType.INFORMATION);
                    experienceLabel.setText(newExperience);
                    experienceContainer.getChildren().setAll(new Label("Experience:"), experienceLabel, editExperienceButton);
                } else {
                    showAlert("Error", "Failed to save experience.", Alert.AlertType.ERROR);
                }
            });
            
            cancelButton.setOnAction(ev -> {
                experienceContainer.getChildren().setAll(new Label("Experience:"), experienceLabel, editExperienceButton);
            });
        });

        // Reviews Provided Section
        Label reviewsLabel = new Label("Reviews Provided:");
        VBox reviewsBox = new VBox(5);
        try {
            ResultSet rs;
            // Depending on the target type, fetch reviews accordingly.
            if (isForPost) {
                rs = reviewHandler.getReviewsByPostId(targetId);
            } else {
                rs = reviewHandler.getReviewsByReplyId(targetId);
            }
            while (rs.next()) {
                if (reviewerName.equals(rs.getString("reviewerName"))) {
                    int revId = rs.getInt("reviewId");
                    String content = rs.getString("content");
                    Label reviewLabel = new Label("Review ID " + revId + ": " + content);
                    reviewsBox.getChildren().add(reviewLabel);
                }
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Failed to load reviews for profile.", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }

        Label feedbackLabel = new Label("Feedback from Students: [Feedback details go here]");

        profileLayout.getChildren().addAll(
            experienceContainer,
            reviewsLabel,
            reviewsBox,
            feedbackLabel
        );

        ScrollPane scrollPane = new ScrollPane(profileLayout);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 600, 400);
        profileStage.setScene(scene);
        profileStage.initOwner(primaryStage);
        profileStage.show();
    }

    /**
     * Utility method to display an alert dialog.
     *
     * @param title the title of the alert
     * @param message the message content
     * @param type the type of alert (e.g., ERROR, INFORMATION)
     */
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}