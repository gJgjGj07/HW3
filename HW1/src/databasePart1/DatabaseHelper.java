package databasePart1;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javafx.scene.control.TextArea;
import javafx.util.Pair;
import application.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Random.*;



/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase;AUTO_SERVER=TRUE";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public Connection connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
//			 You can use this command to clear the database and restart from fresh.
//			statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
		return connection;
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(200), "
				+ "notifications VARCHAR(10000), "
				+ "forgotPassword BOOLEAN DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "role VARCHAR(200), "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    String reviewTable = "CREATE TABLE IF NOT EXISTS reviewer_ratings ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "reviewer_username VARCHAR(255), "
	            + "rating INT, "
	            + "student_username VARCHAR(255), "
	            + "trusted BOOLEAN DEFAULT FALSE, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(reviewTable);

	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (userName, password, role, notifications) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());

	        // Check if notifications is null and replace it with an empty string
	        String notifications = user.getNotifications();
	        pstmt.setString(4, notifications != null ? notifications : "");

	        pstmt.executeUpdate();
	    }
	}

	public boolean removeUser(int userId) {
        // SQL query to delete the user with the given userId
        String query = "DELETE FROM cse360users WHERE id = ?";
        try {
			connectToDatabase();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		}
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the userId parameter
            pstmt.setInt(1, userId);

            // Execute the query
            int rowsAffected = pstmt.executeUpdate();

            // Return true if a row was deleted, false otherwise
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
    }

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	public String generateInvitationCode(String role) {
	    // Generate a random 4-character code
	    String code = UUID.randomUUID().toString().substring(0, 4);
	    try {
			connectToDatabase();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		}
	    // SQL query to insert the invitation code and role into the InvitationCodes table
	    String query = "INSERT INTO InvitationCodes (code, role) VALUES (?, ?)";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        // Set the code and role parameters
	        pstmt.setString(1, code);
	        pstmt.setString(2, role);

	        // Execute the query
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code; // Return the generated code
	}
	public String getInvitationRole(String invitationCode) {
        // SQL query to retrieve the role associated with the invitation code
        String query = "SELECT role FROM InvitationCodes WHERE code = ?";
        try {
			connectToDatabase();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		}
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the invitation code parameter
            pstmt.setString(1, invitationCode);

            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the role if the code is found
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if the code is invalid or not found
        return null;
    }
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	public void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	public void displayAllUsers(TextArea textArea) throws SQLException {
        // Clear the TextArea before appending new data
        textArea.clear();

        // SQL query to select all users from the cse360users table
        String query = "SELECT id, userName, role FROM cse360users";
        connectToDatabase();
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Iterate through the result set and append each user's information to the TextArea
            while (rs.next()) {
                String id = rs.getString("id");
                String userName = rs.getString("userName");
                String role = rs.getString("role");


                // Append user information to the TextArea
                textArea.appendText("ID: " + id + "\n");
                textArea.appendText("Username: " + userName + "\n");
                textArea.appendText("Role: " + role + "\n");
                textArea.appendText("-----------------------------\n");
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
            textArea.appendText("Error fetching user data from the database.\n");
        }
    }
	public void displayNotifications(TextArea textArea, int userId) throws SQLException {
		// Clear the TextArea before appending new data
        textArea.clear();

        // SQL query to select the notifications from the specified user
        String query = "SELECT notifications FROM cse360users WHERE id = ?";
        connectToDatabase();
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1,  userId);
        	
            try(ResultSet rs = pstmt.executeQuery()) {
	            // Append the notification to the text area
	            if (rs.next()) {
	                String notifications = rs.getString("notifications");
	                
	                //Working with a null notification creates null pointer errors
	                if (notifications == null) {
	                	notifications = "";
	                }
	                
	                textArea.appendText(notifications);
	            }
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
            textArea.appendText("Error fetching user data from the database.\n");
        }
	}
	public String getUserRoleById(int userId) {
        // SQL query to retrieve the role of the user
        String query = "SELECT role FROM cse360users WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the userId parameter
            pstmt.setInt(1, userId);

            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the role if the user is found
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if the user is not found
        return "";
    }
	public boolean changeUserRole(int userId, String role) {
        // SQL query to update the role of the user
        String query = "UPDATE cse360users SET role = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the role and userId parameters
            pstmt.setString(1, role);
            pstmt.setInt(2, userId);

            // Execute the query
            int rowsAffected = pstmt.executeUpdate();

            // Return true if a row was updated, false otherwise
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
    }
	public int getUserIdByUsername(String username) {
        // SQL query to retrieve the userId based on the username
        String query = "SELECT id FROM cse360users WHERE userName = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the username parameter
            pstmt.setString(1, username);

            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the userId if the user is found
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return -1 if the user is not found or an error occurs
        return -1;
    }
	public String getFirstAdmin() {
        // SQL query to retrieve the first admin
        String query = "SELECT userName FROM cse360users WHERE role = 'admin' LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                // Return the username of the first admin
                return rs.getString("userName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if no admin is found or an error occurs
        return null;
    }
	public boolean addNotificationToUser(String notification, int userId) {
		String query = "UPDATE cse360users SET notifications = CONCAT(COALESCE(notifications, ''), '\n', ?) WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Set the role and userId parameters
            pstmt.setString(1, notification);
            pstmt.setInt(2, userId);

            // Execute the query
            int rowsAffected = pstmt.executeUpdate();

            // Return true if a row was updated, false otherwise
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
	}
	public int getNumNotifications(int userId) {
		String query = "SELECT notifications FROM cse360users WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			// Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the userId if the user is found
                    return countNewlines(rs.getString("notifications"));
                }
            }
		} 
		catch (SQLException e) {
			e.printStackTrace();
	    }
			
		return -1;		
	}
	public String getNotifications(int userId) {
		String query = "SELECT notifications FROM cse360users WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			// Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Return the userId if the user is found
                    return rs.getString("notifications");
                }
            }
		} 
		catch (SQLException e) {
			e.printStackTrace();
	    }
			
		return "";		
	}
	public static int countNewlines(String text) {
	    if (text == null || text.isEmpty()) {
	        return 0;
	    }
	    return text.split("\n", -1).length - 1;
	}
	public boolean setForgetPassword(int userId) {
	    // Query to retrieve the current value of forgotPassword for the user
	    String selectQuery = "SELECT forgotPassword FROM cse360users WHERE id = ?";
	    // Query to update the forgotPassword field
	    String updateQuery = "UPDATE cse360users SET forgotPassword = ? WHERE id = ?";

	    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
	         PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

	        // Retrieve the current value of forgotPassword
	        selectStmt.setInt(1, userId);
	        try (ResultSet rs = selectStmt.executeQuery()) {
	            if (rs.next()) {
	                boolean currentValue = rs.getBoolean("forgotPassword");
	                boolean newValue = !currentValue; // Toggle the value

	                // Update the forgotPassword field
	                updateStmt.setBoolean(1, newValue);
	                updateStmt.setInt(2, userId);

	                int rowsUpdated = updateStmt.executeUpdate(); // Execute the update
	                return rowsUpdated > 0; // Return true if the update was successful
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); // Log the exception
	    }
	    return false; // Return false if the operation failed
	}
	public boolean setPassword(int userId, String password) {
	    // SQL query to update the password for the specified user
	    String query = "UPDATE cse360users SET password = ? WHERE id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        // Set the password and userId parameters
	        pstmt.setString(1, password);
	        pstmt.setInt(2, userId);

	        // Execute the update query
	        int rowsAffected = pstmt.executeUpdate();

	        // Return true if the update was successful (at least one row affected)
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        e.printStackTrace(); // Log the exception
	        return false; // Return false if an error occurs
	    }
	}
	public boolean getForgotPasswordStatus(int userId) {
	    String query = "SELECT forgotPassword FROM cse360users WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, userId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getBoolean("forgotPassword");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // Default to false if userId not found or error occurs
	}
	public boolean deleteNotificationLine(int userId, String notification) {
	    // Retrieve the current notifications for the user
	    String currentNotifications = getNotifications(userId);
	    if (currentNotifications == null || currentNotifications.isEmpty()) {
	        return false; // No notifications to delete
	    }

	    // Split the notifications into lines
	    String[] lines = currentNotifications.split("\n");

	    // Use a StringBuilder to reconstruct the notifications without the line containing userId
	    StringBuilder updatedNotifications = new StringBuilder();
	    boolean lineFound = false;

	    for (String line : lines) {
	        // Check if the line contains the userId
	        if (!line.contains(notification)) {
	            updatedNotifications.append(line).append("\n");
	        	
	        } else {
	            lineFound = true; // Mark that the line was found and remove
	        }
	    }
	

	    // If the line was not found, return false
	    if (!lineFound) {
	        return false;
	    }

	    // Remove the trailing newline character
	    if (updatedNotifications.length() > 0) {
	        updatedNotifications.setLength(updatedNotifications.length() - 1);
	    }

	    // Update the notifications in the database
	    String query = "UPDATE cse360users SET notifications = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, updatedNotifications.toString());
	        pstmt.setInt(2, userId);

	        // Execute the update query
	        int rowsAffected = pstmt.executeUpdate();

	        // Return true if the update was successful (at least one row affected)
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        e.printStackTrace(); // Log the exception
	        return false; // Return false if an error occurs
	    }
	}
	public boolean clearNotifications(int userId) {
	    String query = "UPDATE cse360users SET notifications = '' WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, userId);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	/**
     * Retrieves all usernames of users whose role contains 'reviewer'.
     *
     * @return A list of usernames of users whose role contains 'reviewer'.
     * @throws SQLException If a database access error occurs.
     */
    public List<String> getAllReviewersUsernames() throws SQLException {
        List<String> reviewersUsernames = new ArrayList<>();
        connectToDatabase(); // Ensure connection is established

        String query = "SELECT userName FROM cse360users WHERE role LIKE '%Reviewer%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                reviewersUsernames.add(rs.getString("userName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return reviewersUsernames;
    }
    public void addReview(String reviewerUsername, int rating, String studentUsername) throws SQLException {
        String query = "INSERT INTO reviewer_ratings (reviewer_username, rating, student_username) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setInt(2, rating);
            pstmt.setString(3, studentUsername);
            pstmt.executeUpdate();
        }
    }
    public void addTrustedReviewer(String reviewerUsername, String studentUsername) throws SQLException {
        String query = "UPDATE reviewer_ratings SET trusted = TRUE WHERE reviewer_username = ? AND student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setString(2,  studentUsername);
            pstmt.executeUpdate();
            
            // Also add a notification to the reviewer
            String notification = studentUsername + " has added you to their trusted reviewers list!";
            addNotificationToUser(notification, getUserIdByUsername(reviewerUsername));
        }
    }

    public boolean isReviewerTrusted(String reviewerUsername, String studentUsername) throws SQLException {
        String query = "SELECT trusted FROM reviewer_ratings WHERE reviewer_username = ? AND student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setString(2,  studentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("trusted");
            }
        }
        return false;
    }
    public List<String> getMyReviewers(String studentUsername) throws SQLException {
        List<String> reviewers = new ArrayList<>();
        // Modified query to get distinct reviewers and include trusted status
        String query = "SELECT DISTINCT reviewer_username FROM reviewer_ratings " +
                       "WHERE student_username = ? AND trusted = TRUE " +
                       "ORDER BY reviewer_username";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviewers.add(rs.getString("reviewer_username"));
                }
            }
        }
        return reviewers;
    }

    public Integer getReviewerRating(String studentUsername, String reviewerUsername) throws SQLException {
        String query = "SELECT rating FROM reviewer_ratings WHERE student_username = ? AND reviewer_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, reviewerUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rating");
                }
            }
        }
        return null;
    }

    public void removeTrustedReviewer(String reviewerUsername, String studentUsername) throws SQLException {
        String query = "UPDATE reviewer_ratings SET trusted = FALSE WHERE reviewer_username = ? AND student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setString(2, studentUsername);
            pstmt.executeUpdate();
        }
    }
 // Check if a review already exists for this student-reviewer pair
    public boolean reviewExists(String reviewerUsername, String studentUsername) throws SQLException {
        String query = "SELECT COUNT(*) FROM reviewer_ratings WHERE reviewer_username = ? AND student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            pstmt.setString(2, studentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Update an existing review
    public void updateReview(String reviewerUsername, int rating, String studentUsername) throws SQLException {
        String query = "UPDATE reviewer_ratings SET rating = ?, timestamp = CURRENT_TIMESTAMP WHERE reviewer_username = ? AND student_username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, rating);
            pstmt.setString(2, reviewerUsername);
            pstmt.setString(3, studentUsername);
            pstmt.executeUpdate();
        }
    }

    // Modified addReview to handle updates
    public void addOrUpdateReview(String reviewerUsername, int rating, String studentUsername) throws SQLException {
        if (reviewExists(reviewerUsername, studentUsername)) {
            updateReview(reviewerUsername, rating, studentUsername);
        } else {
            String query = "INSERT INTO reviewer_ratings (reviewer_username, rating, student_username) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, reviewerUsername);
                pstmt.setInt(2, rating);
                pstmt.setString(3, studentUsername);
                pstmt.executeUpdate();
            }
        }
    }
    /**
     * Adds a notification to all users who have a role of "Instructor"
     * @param notification The notification message to be added
     * @return true if the notification was added to at least one instructor, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean addNotificationToAllInstructors(String notification) throws SQLException {
        // First, get all user IDs with Instructor role
        List<Integer> instructorIds = new ArrayList<>();
        String query = "SELECT id FROM cse360users WHERE role = 'Instructor'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                instructorIds.add(rs.getInt("id"));
            }
        }
        
        // If no instructors found, return false
        if (instructorIds.isEmpty()) {
            return false;
        }
        
        // Add notification to each instructor
        boolean success = false;
        for (int id : instructorIds) {
            if (addNotificationToUser(notification, id)) {
                success = true;
            }
        }
        
        return success;
    }
    
	
}
