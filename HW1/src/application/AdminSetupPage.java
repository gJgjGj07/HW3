package application;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.sql.SQLException;
import databasePart1.*;
import javafx.scene.layout.HBox;

/**
 * The {@code AdminSetupPage} class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {

    private final DatabaseHelper databaseHelper;
    private QuestionHandler qHandler;
    private AnswerHandler aHandler;
    private UserHandler uHandler;
    private ReviewHandler rHandler;

    /**
     * Constructs an {@code AdminSetupPage} with the specified database helper and handlers.
     *
     * @param databaseHelper the database helper used to interact with the database
     * @param qHandler       the question handler
     * @param aHandler       the answer handler
     * @param uHandler       the user handler
     * @param rHandler       the review handler
     */
    public AdminSetupPage(DatabaseHelper databaseHelper, QuestionHandler qHandler, AnswerHandler aHandler, UserHandler uHandler, ReviewHandler rHandler) {
        this.databaseHelper = databaseHelper;
        this.qHandler = qHandler;
        this.aHandler = aHandler;
        this.uHandler = uHandler;
        this.rHandler = rHandler;
    }

    /**
     * Displays the administrator setup page which allows the creation of an admin account.
     * The page includes fields for entering a username and password, and displays error messages
     * if the input does not meet the required constraints. Upon successful validation, the admin
     * account is registered in the database.
     *
     * @param primaryStage the primary stage where the setup page is displayed
     */
    public void show(Stage primaryStage) {
        // Input field for admin username
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        // Input field for admin password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        // Label to display error messages for username constraints
        Label userError = new Label();
        userError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Label to display error messages for password constraints
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new FirstPage(databaseHelper, qHandler, aHandler, uHandler, rHandler).show(primaryStage);
        });

        // Button to initiate the admin setup process
        Button setupButton = new Button("Setup");
        setupButton.setOnAction(a -> {
            // Clear previous error messages
            userError.setText("");
            passwordError.setText("");

            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                // Validate username; if invalid, display error message
                if (UserNameRecognizer.checkForValidUserName(userName).length() > 0) {
                    userError.setText("Invalid Username:\n" + UserNameRecognizer.checkForValidUserName(userName));
                }
                // Validate password; if invalid, display error message
                if (PasswordEvaluator.evaluatePassword(password).length() > 0) {
                    passwordError.setText("Invalid Password:\n" + PasswordEvaluator.evaluatePassword(password));
                }
                // If no errors, proceed with registration
                if (userError.getText().isEmpty() && passwordError.getText().isEmpty()) {
                    User user = new User(userName, password, "admin");
                    databaseHelper.register(user);
                    System.out.println("Administrator setup completed.");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Security Notice");
                    alert.setHeaderText("Re-login Required");
                    alert.setContentText("As a security protocol, please log in again.");
                    alert.showAndWait();

                    new UserLoginPage(databaseHelper, aHandler, qHandler, uHandler, rHandler).show(primaryStage);
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Styling and layout for error messages and input fields
        userError.setMaxWidth(400);
        passwordError.setMaxWidth(250);

        HBox userErrorBox = new HBox(userError);
        userErrorBox.setAlignment(Pos.CENTER_LEFT);
        userErrorBox.setMaxWidth(250);

        HBox passwordErrorBox = new HBox(passwordError);
        passwordErrorBox.setAlignment(Pos.CENTER_LEFT);
        passwordErrorBox.setMaxWidth(250);

        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        backButtonBox.setPadding(new Insets(20, 20, 20, 20));

        VBox.setMargin(userNameField, new Insets(45, 0, 0, 0));

        VBox layout = new VBox(10,
            userNameField,
            userErrorBox,
            passwordField,
            passwordErrorBox,
            setupButton
        );
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-padding: 20;");

        BorderPane rootLayout = new BorderPane();
        rootLayout.setCenter(layout);
        rootLayout.setBottom(backButtonBox);

        primaryStage.setScene(new Scene(rootLayout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
