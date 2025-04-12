package TP3.application;

public class HW4 {

    public static void main(String[] args) {
        System.out.println("===== HW4 Staff Role Demo =====");


        UserHandler userHandler = new UserHandler();
        ModifyUserRole roleModifier = new ModifyUserRole();
        RemoveUserPage remover = new RemoveUserPage();


        System.out.println("→ Creating user 'user123' with role 'regular'...");
        userHandler.addUser("user123", "regular");
        System.out.println("User created.\n");


        System.out.println("→ Listing all users and roles:");
        userHandler.printAllUsers(); 
        System.out.println();


        System.out.println("→ Promoting 'user123' to reviewer...");
        boolean promoted = roleModifier.promoteToReviewer("user123");
        System.out.println("Promoted: " + promoted + "\n");


        System.out.println("→ Updating 'user123' to trusted reviewer...");
        boolean updated = roleModifier.updateUserRole("user123", "trusted reviewer");
        System.out.println("Updated: " + updated + "\n");

        System.out.println("→ Resetting password for 'user123'...");
        boolean reset = userHandler.resetPassword("user123");
        System.out.println("Password reset: " + reset + "\n");

        System.out.println("→ Removing 'user123' from system...");
        boolean removed = remover.removeUser("user123");
        System.out.println("Removed: " + removed + "\n");

        System.out.println("→ Final user list:");
        userHandler.printAllUsers();
        System.out.println("===== End of Demo =====");
    }
}
