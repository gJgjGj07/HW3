# HW1CSE360
# **README**

## **Overview**
The system supports the following user roles:
- **Admin**
- **Student**
- **Instructor**
- **Staff**
- **Reviewer**

Admins can manage users, modify roles, and remove accounts. Users can register, log in, reset passwords, and navigate their assigned roles.

---

## **Project Structure**
Below is a list of all files in the repository and their purposes. All of the files can be found in HW1/src/application, DatabaseHelper.java can be found in HW1/src/datbasePart1:

### **1. Application Logic**
These files implement the core functionality of the system:

| File Name                      | Description |
|--------------------------------|------------|
| `StartCSE360.java`             | The main entry point of the application. It initializes the database and directs the first user to the setup page. |
| `DatabaseHelper.java`  | Handles all database interactions like user registration, login verification, and role management. |

---

### **2. User Account Setup and Authentication**
These files manage user registration, login, and password recovery.

| File Name                     | Description |
|--------------------------------|------------|
| `FirstPage.java`               | Displayed for the first user, allowing them to create an **admin** account. |
| `AdminSetupPage.java`          | Handles **admin account creation** when setting up the system. |
| `SetupAccountPage.java`        | Handles user account setup when an **invitation code** is used. |
| `SetupLoginSelectionPage.java` | Allows users to choose between **logging in or setting up** an account. |
| `UserLoginPage.java`           | Standard login page for all users. |
| `PasswordResetPage.java`       | Allows users to reset their password when needed. |

---

### **3. User Roles and Home Pages**
Each role has its own **home page**, providing access to relevant functionality.

| File Name                   | Description |
|-----------------------------|------------|
| `AdminHomePage.java`        | Admin dashboard where they can manage users, modify roles, and send temporary passwords. |
| `UserHomePage.java`         | Redirects users to their respective home pages based on their assigned roles. |
| `StudentHomePage.java`      | Home page for **Students**. |
| `InstructorHomePage.java`   | Home page for **Instructors**. |
| `StaffHomePage.java`        | Home page for **Staff**. |
| `ReviewerHomePage.java`     | Home page for **Reviewers**. |

---

### **4. Admin-Specific Features**
Admins can **invite users, modify roles, remove users, and send temporary passwords.**

| File Name                   | Description |
|-----------------------------|------------|
| `InvitationPage.java`       | Allows **admins** to generate invitation codes for new users. |
| `RoleSelectionPage.java`    | Lets **admins** select roles when generating an invitation code. |
| `ModifyUserRole.java`       | Allows **admins** to assign and change roles for existing users. |
| `RemoveUserPage.java`       | Allows **admins** to remove users from the system. |

---

### **5. Utility Classes**
These files handle specific tasks such as **password generation and validation.**

| File Name                    | Description |
|------------------------------|------------|
| `PasswordGenerator.java`     | Generates **secure** random passwords for users. |
| `UserNameRecognizer.java`    | Checks **username validity** to ensure compliance with system rules. |

---

### **6. Testing Automation**
These files are used to automate test cases for input validation:

| File Name                      | Description |
|--------------------------------|------------|
| `PasswordEvaluationTestingAutomation.java`  | Contains the script for password testing automation |
| `UserNameRecognitionTestingAutomation.java`  | Contains the script for username testing automation |
| `databaseCheckTestingAutomation.java`  | Contains the script for the user id testing automation |

---

## **How to Access the System**

### **1. First User (Admin Setup)**
- When the system is launched for the first time, it prompts the **first user** to create an **Admin account**.
- The first user provides a **username and password**, which are validated before registration.
- After registration, the user must **log in again** as an admin.

### **2. User Registration via Invitation Code**
- New users must receive an **invitation code** from an admin.
- Users can sign up using their **username, password, and invitation code** on the **Setup Account Page**.
- The system validates usernames, passwords, and invitation codes before allowing registration.

### **3. User Login**
- Users log in using their **username and password**.
- If a user **forgets their password**, they can request a **temporary password**, which an admin must approve.

### **4. Admin Features**
- **Invite Users**: Admins can generate **invitation codes** for new users.
- **Modify User Roles**: Admins can assign or change user roles (except for admins).
- **Remove Users**: Admins can delete users (except other admins).
- **Send Temporary Passwords**: Admins can generate a **temporary password** for a user who has forgotten theirs.

### **5. Role-Specific Navigation**
- After logging in, users are directed to their **role-specific home pages**.
- Users with **multiple roles** can select which role they want to use.

### **6. Password Reset**
- Users who forget their passwords can reset them after receiving a **temporary password** from an admin.

---

## **Error Handling and Input Validation**
To maintain security and prevent errors, the following **input validation rules** are enforced:

- **Username Validation**
  - Must be **4-16 characters** long.
  - Must start with an **alphabetic** character.
  - Can only contain **letters, numbers, periods (.), hyphens (-), or underscores (_)**.
  - Cannot be left **blank**.

- **Password Validation**
  - Must be **at least 8 characters** long.
  - Must include at least **one uppercase letter, one lowercase letter, one number, and one special character**.
  - Cannot be left **blank**.

- **Admin Restrictions**
  - Admin **roles cannot be removed** from themselves.
  - Admin **accounts cannot be deleted**.

- **Numeric Input Validation**
  - User **IDs must be numeric**.
  - A valid **invitation code** must be entered during user registration.

---
The following recordings are found in the HW1 folder

- **Screencast 1**: https://youtu.be/pe4obepkrd8  
- **Screencast 2 UI Walkthrough**: https://youtu.be/2pF_IYQFY2A


## **Standup Meetings**
  - `Scrum Meeting 1.mp4` - Contains the scrum meeting for 2/3/2024, team discusses basic status update
  - `Scrum Meeting 2.mp4` - Contains the scrum meeting for 2/5/2024, team discusses basic status update
