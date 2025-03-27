package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private String role;
    private String notifications;

    // Constructor to initialize a new User object with userName, password, and role.
    public User( String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.notifications = "";
    }
    
    // Sets the role of the user.
    public void setRole(String role) {
    	this.role=role;
    }
    public void setNotifications(String notifications) {
    	this.notifications = notifications;
    }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getNotifications() {return notifications;}
}
