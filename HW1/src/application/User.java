package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, role, and notifications.
 */
public class User {
    private String userName;
    private String password;
    private String role;
    private String notifications;

    /**
     * Constructs a new User object with the specified username, password, and role.
     *
     * @param userName the username of the user
     * @param password the user's password
     * @param role the role assigned to the user
     */
    public User( String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.notifications = "";
    }
    
    /**
     * Sets the role of the user.
     *
     * @param role the new role to be assigned to the user
     */
    public void setRole(String role) {
    	this.role=role;
    }
    
    /**
     * Sets the notifications for the user.
     *
     * @param notifications the notifications to be set for the user
     */
    public void setNotifications(String notifications) {
    	this.notifications = notifications;
    }
    
    /**
     * Returns the username of the user.
     *
     * @return the user's username
     */
    public String getUserName() { return userName; }
    
    /**
     * Returns the password of the user.
     *
     * @return the user's password
     */
    public String getPassword() { return password; }
    
    /**
     * Returns the role of the user.
     *
     * @return the user's role
     */
    public String getRole() { return role; }
    
    /**
     * Returns the notifications of the user.
     *
     * @return the user's notifications
     */
    public String getNotifications() {return notifications;}
}
