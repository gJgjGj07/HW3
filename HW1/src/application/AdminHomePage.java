package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.*;

/**
 * The AdminHomePage class represents the administrative interface.
 * It provides functionalities for the admin to manage users, send temporary passwords,
 * view notifications, and navigate to other sections such as user removal and role modification.
 */
public class AdminHomePage {

    private int userId;
    private String userName;
    private DatabaseHelper dbHelper; // Provides database operations needed for admin tasks.
    private Stage primaryStage = null;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private UserHandler uHandler;
    private ReviewHandler rHandler;

    /**
     * Constructs an AdminHomePage object and initializes the database connection.
     *
     * @param userId   the ID of the admin user
     * @param userName the name of the admin user
     * @param qHandler the handler for questions
     * @param aHandler the handler for answers
     * @param uHandler the handler for users
     * @param rHandler the handler for reviews
     * @param user     the current user object
     */
    public AdminHomePage(int userId, String userName, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler, User user) {
        this.userId = userId;
        this.userName = userName;
        dbHelper = new DatabaseHelper();
        try {
            dbHelper.connectToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the interface to send a temporary password to a user.
     *
     * <p>This method shows a simple form with a text field to enter the user ID,
     * a button to send the temporary password, and a back button to return to the home page.</p>
     *
     * @param primaryStage the primary stage of the application
     * @param user         the current user object
     */
    private void showSendTempPassword(Stage primaryStage, User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // TextField for the admin to enter the userId
        TextField userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setMaxWidth(200);

        // Button to send generated password
        Button sendPasswordButton = new Button("Send Temporary Password");
        sendPasswordButton.setStyle("-fx-font-size: 14px;");

        sendPasswordButton.setOnAction(event -> {
            // Get the userId from the text field
            int sendUserId = Integer.parseInt(userIdField.getText());
            dbHelper.setForgetPassword(sendUserId);
            dbHelper.deleteNotificationLine(userId, "User " + sendUserId + " forgot their password. Send them a temporary one.");
            String password = PasswordGenerator.generatePassword();

            dbHelper.addNotificationToUser("Here Is Your Temporary Password: " + password, sendUserId);
            if (dbHelper.setPassword(sendUserId, password)) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Password Sent");
                successAlert.setContentText("The user with ID " + sendUserId + " can now reset their password");
                successAlert.showAndWait();
            } else {
                // Show an error message if the user was not found
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("User Not Found");
                errorAlert.setContentText("The user with ID " + sendUserId + " was not found.");
                errorAlert.showAndWait();
            }
            // Redirect back to the home page
            AdminHomePage adminHomePage = new AdminHomePage(userId, userName, qHandler, aHandler, uHandler, rHandler, user);
            adminHomePage.show(primaryStage, user);
        });

        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });

        // Add components to the layout
        layout.getChildren().addAll(userIdField, sendPasswordButton, backButton);

        // Create the scene for the temporary password page
        Scene tempPasswordScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(tempPasswordScene);
        primaryStage.setTitle("Enter User ID To Change Password");
    }

    /**
     * Displays the notifications for the admin.
     *
     * <p>This method fetches and displays the notifications in a read-only text area,
     * and provides a back button to return to the home page.</p>
     *
     * @param primaryStage the primary stage of the application
     * @param user         the current user object
     */
    private void showNotifications(Stage primaryStage, User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // TextArea that displays all the notifications for the admin
        TextArea notificationsArea = new TextArea();
        notificationsArea.setEditable(false); // Make it read-only
        notificationsArea.setPrefSize(600, 300);

        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });

        try {
            dbHelper.displayNotifications(notificationsArea, userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add components to the layout
        layout.getChildren().addAll(notificationsArea, backButton);

        // Create the scene for the notifications page
        Scene notificationsScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(notificationsScene);
        primaryStage.setTitle("Notifications");
    }

    /**
     * Displays the page with user information.
     *
     * <p>This method shows all user details in a read-only text area along with a back button
     * to return to the admin home page.</p>
     *
     * @param primaryStage the primary stage of the application
     * @param user         the current user object
     */
    private void showUserInfoPage(Stage primaryStage, User user) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        TextArea userInfoArea = new TextArea();
        userInfoArea.setEditable(false); // Make it read-only
        userInfoArea.setPrefSize(600, 300);

        // Button to go back to the home page
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-font-size: 14px;");

        // Set action for the back button
        backButton.setOnAction(event -> {
            show(primaryStage, user); // Return to the home page
        });

        // Fetch and display user information in the TextArea
        try {
            dbHelper.displayAllUsers(userInfoArea);
        } catch (Exception e) {
            e.printStackTrace();
            userInfoArea.setText("Error fetching user data.");
        }

        // Add components to the layout
        layout.getChildren().addAll(userInfoArea, backButton);

        // Create the scene for the user information page
        Scene userInfoScene = new Scene(layout, 800, 500);

        // Set the scene to the primary stage
        primaryStage.setScene(userInfoScene);
        primaryStage.setTitle("User Information");
    }

    /**
     * Displays the Admin Home Page.
     *
     * <p>This method constructs the main admin interface with options to view users,
     * remove a user, modify user roles, send temporary passwords, view notifications,
     * and log out. It also includes a back button to navigate to the previous page.</p>
     *
     * @param primaryStage the primary stage of the application
     * @param user         the current user object
     */
    public void show(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;

        // Main content container
        VBox content = new VBox(10);
        content.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-spacing: 10;");

        // Back button setup
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(dbHelper, userName, qHandler, aHandler, uHandler, user, rHandler).show(primaryStage, user);
        });
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20));

        // Create root layout with BorderPane
        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(content);  // Main content in center
        rootLayout.setBottom(backButtonBox);  // Back button at bottom

        // Add components to content
        Label adminLabel = new Label("Hello, " + userName + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button viewUsersButton = new Button("View All Users");
        Button removeUserButton = new Button("Remove User");
        Button modifyUserRoleButton = new Button("Modify User Role");
        Button checkNotificationButton = new Button("Notifications (" + dbHelper.getNumNotifications(userId) + ")");
        Button sendTempPassButton = new Button("Send Temporary Password To User");
        Button logoutButton = new Button("Logout");

        // Style buttons uniformly
        String buttonStyle = "-fx-font-size: 14px;";
        viewUsersButton.setStyle(buttonStyle);
        removeUserButton.setStyle(buttonStyle);
        modifyUserRoleButton.setStyle(buttonStyle);
        checkNotificationButton.setStyle(buttonStyle);
        sendTempPassButton.setStyle(buttonStyle);

        // Set action handlers for buttons
        viewUsersButton.setOnAction(event -> showUserInfoPage(primaryStage, user));
        removeUserButton.setOnAction(event -> new RemoveUserPage(dbHelper, primaryStage, userId, userName, qHandler, aHandler, uHandler, rHandler, user).show(user));
        modifyUserRoleButton.setOnAction(event -> new ModifyUserRole(dbHelper, primaryStage, userId, userName, qHandler, aHandler, uHandler, rHandler, user).show(user));
        checkNotificationButton.setOnAction(event -> showNotifications(primaryStage, user));
        sendTempPassButton.setOnAction(event -> showSendTempPassword(primaryStage, user));
        logoutButton.setOnAction(event -> new SetupLoginSelectionPage(dbHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage));

        // Add components to the content VBox
        content.getChildren().addAll(
            adminLabel,
            viewUsersButton,
            removeUserButton,
            modifyUserRoleButton,
            sendTempPassButton,
            checkNotificationButton,
            logoutButton
        );

        // Set up scene and display it
        primaryStage.setScene(new Scene(rootLayout, 800, 500));
        primaryStage.setTitle("Admin Page");
    }
}
