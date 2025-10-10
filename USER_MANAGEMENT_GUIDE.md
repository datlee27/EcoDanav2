# User Management System - Complete Guide

## Overview

The User Management System provides comprehensive CRUD operations for managing users in the EcoDana platform. This system is built with modern web technologies and follows best practices for security, accessibility, and user experience.

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [Backend Implementation](#backend-implementation)
5. [Frontend Implementation](#frontend-implementation)
6. [API Documentation](#api-documentation)
7. [Security](#security)
8. [Accessibility](#accessibility)
9. [Usage Guide](#usage-guide)
10. [Troubleshooting](#troubleshooting)

---

## Features

### Core Functionality
- ✅ **Create Users**: Add new users with complete profile information
- ✅ **Read Users**: View user list with search and filtering
- ✅ **Update Users**: Edit existing user information
- ✅ **Delete Users**: Remove users from the system
- ✅ **Role Management**: Assign and manage user roles (Admin, Staff, Owner, Customer)
- ✅ **Status Management**: Control user status (Active, Inactive, Banned)

### Advanced Features
- 🔍 **Real-time Search**: Debounced search by username, email, or name
- 🎯 **Multi-filter**: Filter by status and role simultaneously
- 📊 **Statistics Dashboard**: View user counts by status
- 🔐 **Security**: CSRF protection, XSS prevention, password hashing
- ♿ **Accessibility**: WCAG 2.1 AA compliant with keyboard navigation
- 📱 **Responsive Design**: Mobile-first design that works on all devices
- 🎨 **Modern UI**: Clean, intuitive interface with Tailwind CSS

---

## Architecture

### Technology Stack

#### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **ORM**: JPA/Hibernate
- **Database**: MySQL 8.0
- **Security**: Spring Security with BCrypt
- **Validation**: Jakarta Validation

#### Frontend
- **HTML**: Thymeleaf templates
- **CSS**: Tailwind CSS + Custom CSS
- **JavaScript**: Vanilla ES5 (cross-browser compatible)
- **Icons**: Font Awesome 6.4

### Project Structure

```
src/
├── main/
│   ├── java/com/ecodana/evodanavn1/
│   │   ├── controller/admin/
│   │   │   └── UserAdminController.java
│   │   ├── dto/
│   │   │   ├── UserRequest.java
│   │   │   └── UserResponse.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── RoleRepository.java
│   │   └── service/
│   │       ├── UserService.java
│   │       └── RoleService.java
│   └── resources/
│       ├── templates/admin/
│       │   └── user-management.html
│       ├── static/
│       │   ├── css/
│       │   │   └── admin-users.css
│       │   └── js/
│       │       └── admin-users.js
│       └── db/
│           └── user-sample-data.sql
```

---

## Database Schema

### Users Table

```sql
CREATE TABLE Users (
    UserId CHAR(36) PRIMARY KEY,
    Username VARCHAR(100) UNIQUE NOT NULL,
    FirstName VARCHAR(256),
    LastName VARCHAR(256),
    UserDOB DATE,
    PhoneNumber VARCHAR(15),
    AvatarUrl VARCHAR(255),
    Gender ENUM('Male', 'Female', 'Other'),
    Status ENUM('Active', 'Inactive', 'Banned') DEFAULT 'Active',
    RoleId CHAR(36) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    EmailVerifed TINYINT(1) DEFAULT 0,
    PasswordHash VARCHAR(255) NOT NULL,
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    NormalizedUserName VARCHAR(256),
    NormalizedEmail VARCHAR(256),
    SecurityStamp TEXT,
    ConcurrencyStamp TEXT,
    TwoFactorEnabled TINYINT(1) DEFAULT 0,
    LockoutEnd DATETIME,
    LockoutEnabled TINYINT(1) DEFAULT 0,
    AccessFailedCount INT DEFAULT 0,
    FOREIGN KEY (RoleId) REFERENCES Roles(RoleId)
);
```

### Roles Table

```sql
CREATE TABLE Roles (
    RoleId CHAR(36) PRIMARY KEY,
    RoleName VARCHAR(50),
    NormalizedName VARCHAR(256)
);
```

### Default Roles
- **Admin**: Full system access
- **Staff**: Manage bookings and vehicles
- **Owner**: Manage own vehicles
- **Customer**: Book and rent vehicles

---

## Backend Implementation

### UserAdminController

**Location**: `src/main/java/com/ecodana/evodanavn1/controller/admin/UserAdminController.java`

#### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | Display user management page |
| GET | `/admin/users/api/list` | Get all users with filters |
| GET | `/admin/users/api/{id}` | Get user by ID |
| POST | `/admin/users/api/create` | Create new user |
| PUT | `/admin/users/api/update/{id}` | Update user |
| DELETE | `/admin/users/api/delete/{id}` | Delete user |
| PATCH | `/admin/users/api/status/{id}` | Update user status |
| GET | `/admin/users/api/roles` | Get all roles |
| GET | `/admin/users/api/search` | Search users |

### DTOs

#### UserRequest
```java
public class UserRequest {
    @NotBlank String username;
    @NotBlank String firstName;
    @NotBlank String lastName;
    @Email String email;
    String phoneNumber;
    LocalDate userDOB;
    String gender;
    @NotBlank String status;
    @NotBlank String roleId;
    String password;
    Boolean emailVerified;
    Boolean twoFactorEnabled;
    Boolean lockoutEnabled;
}
```

#### UserResponse
```java
public class UserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    LocalDate userDOB;
    String gender;
    String status;
    String roleId;
    String roleName;
    LocalDateTime createdDate;
    Boolean emailVerified;
    Boolean twoFactorEnabled;
    Boolean lockoutEnabled;
}
```

---

## Frontend Implementation

### HTML Template

**Location**: `src/main/resources/templates/admin/user-management.html`

#### Key Components
1. **Statistics Cards**: Display user counts by status
2. **Search Bar**: Real-time search with debouncing
3. **Filters**: Status and role filters
4. **User Table**: Responsive table with user data
5. **Modal Form**: Add/Edit user form
6. **Toast Notifications**: Success/error messages

### CSS Styling

**Location**: `src/main/resources/static/css/admin-users.css`

#### Features
- Modern, clean design
- Responsive breakpoints (mobile, tablet, desktop)
- Smooth animations and transitions
- Status and role badges with color coding
- Accessible focus states
- Print-friendly styles

### JavaScript

**Location**: `src/main/resources/static/js/admin-users.js`

#### Key Functions

```javascript
// Data Loading
loadUsers()          // Fetch and display all users
loadRoles()          // Fetch available roles
filterUsers()        // Apply search and filters

// CRUD Operations
createUser(data)     // Create new user
updateUser(id, data) // Update existing user
deleteUser(id)       // Delete user
editUser(id)         // Open edit modal

// UI Functions
showModal()          // Display modal
closeUserModal()     // Hide modal
showToast(msg, type) // Show notification
renderUsers(users)   // Render user table
```

---

## API Documentation

### Get All Users

```http
GET /admin/users/api/list?search=john&status=Active&role=Customer
```

**Query Parameters:**
- `search` (optional): Search term for username, email, or name
- `status` (optional): Filter by status (Active, Inactive, Banned)
- `role` (optional): Filter by role name

**Response:**
```json
{
  "success": true,
  "users": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phoneNumber": "0901234567",
      "status": "Active",
      "roleName": "Customer",
      "createdDate": "2024-03-01T10:00:00"
    }
  ],
  "total": 1
}
```

### Create User

```http
POST /admin/users/api/create
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "new_user",
  "firstName": "New",
  "lastName": "User",
  "email": "newuser@example.com",
  "password": "password123",
  "phoneNumber": "0901234567",
  "roleId": "550e8400-e29b-41d4-a716-446655440003",
  "status": "Active",
  "gender": "Male",
  "emailVerified": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "user": { /* UserResponse object */ }
}
```

### Update User

```http
PUT /admin/users/api/update/{id}
Content-Type: application/json
```

**Request Body:** Same as Create User (password optional)

### Delete User

```http
DELETE /admin/users/api/delete/{id}
```

**Response:**
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

### Update User Status

```http
PATCH /admin/users/api/status/{id}?status=Inactive
```

**Response:**
```json
{
  "success": true,
  "message": "User status updated successfully",
  "user": { /* UserResponse object */ }
}
```

---

## Security

### Authentication & Authorization
- All endpoints require admin authentication
- Session-based authentication with Spring Security
- CSRF protection enabled for all state-changing operations

### Password Security
- Passwords hashed using BCrypt (strength 10)
- Minimum password length: 6 characters
- Passwords never returned in API responses

### Input Validation
- Server-side validation using Jakarta Validation
- XSS prevention through HTML escaping
- SQL injection prevention through JPA/Hibernate

### CSRF Protection
```javascript
// CSRF token automatically included in requests
var headers = {
    'Content-Type': 'application/json'
};
if (csrfHeader && csrfToken) {
    headers[csrfHeader] = csrfToken;
}
```

---

## Accessibility

### WCAG 2.1 AA Compliance
- ✅ Keyboard navigation support
- ✅ Screen reader compatible
- ✅ ARIA labels and roles
- ✅ Focus indicators
- ✅ Sufficient color contrast
- ✅ Responsive text sizing

### Keyboard Shortcuts
- `ESC`: Close modal
- `Ctrl/Cmd + K`: Focus search input
- `Tab`: Navigate between elements
- `Enter`: Submit forms

### Screen Reader Support
- Semantic HTML elements
- ARIA labels for interactive elements
- Status announcements for dynamic content
- Descriptive button labels

---

## Usage Guide

### Accessing User Management

1. Log in as an admin user
2. Navigate to `/admin/users`
3. View the user management dashboard

### Adding a New User

1. Click the **"Add User"** button
2. Fill in the required fields:
   - Username (unique)
   - Email (unique)
   - First Name
   - Last Name
   - Password
   - Role
   - Status
3. Optionally fill in:
   - Phone Number
   - Date of Birth
   - Gender
   - Avatar URL
4. Click **"Save User"**

### Editing a User

1. Click the **edit icon** (pencil) next to the user
2. Modify the desired fields
3. Leave password blank to keep current password
4. Click **"Save User"**

### Deleting a User

1. Click the **delete icon** (trash) next to the user
2. Confirm the deletion in the popup
3. User will be permanently removed

### Searching Users

1. Type in the search box at the top
2. Search works on:
   - Username
   - Email
   - First Name
   - Last Name
3. Results update automatically (300ms debounce)

### Filtering Users

1. Use the **Status** dropdown to filter by status
2. Use the **Role** dropdown to filter by role
3. Filters can be combined with search
4. Select "All" to clear a filter

### Refreshing Data

- Click the **"Refresh"** button to reload user data
- Data automatically refreshes after create/update/delete operations

---

## Troubleshooting

### Common Issues

#### Users Not Loading

**Problem**: Table shows "Loading users..." indefinitely

**Solutions**:
1. Check browser console for errors
2. Verify you're logged in as admin
3. Check database connection
4. Verify API endpoint is accessible

#### Cannot Create User

**Problem**: "Username already exists" or "Email already exists"

**Solutions**:
1. Choose a different username
2. Use a different email address
3. Check if user was already created

#### Password Not Working

**Problem**: Cannot log in with created user

**Solutions**:
1. Ensure password meets minimum requirements (6 characters)
2. Check if password was properly hashed
3. Try resetting the password

#### Modal Not Closing

**Problem**: Modal stays open after submission

**Solutions**:
1. Press ESC key
2. Click outside the modal
3. Refresh the page

### Debug Mode

Enable debug logging in `application.properties`:

```properties
logging.level.com.ecodana.evodanavn1.controller.admin=DEBUG
logging.level.com.ecodana.evodanavn1.service=DEBUG
```

### Browser Compatibility

Tested and working on:
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+

---

## Sample Data

### Default Users

The system includes sample data with the following users:

| Username | Email | Password | Role | Status |
|----------|-------|----------|------|--------|
| admin | admin@ecodana.com | password123 | Admin | Active |
| staff_nguyen | nguyen.an@ecodana.com | password123 | Staff | Active |
| staff_tran | tran.binh@ecodana.com | password123 | Staff | Active |
| owner_le | le.cuong@ecodana.com | password123 | Owner | Active |
| owner_pham | pham.dung@ecodana.com | password123 | Owner | Active |
| customer_hoang | hoang.em@gmail.com | password123 | Customer | Active |

**Note**: Change default passwords in production!

### Loading Sample Data

```bash
mysql -u root -p ecodanangv2 < src/main/resources/db/user-sample-data.sql
```

---

## Best Practices

### For Administrators

1. **Regular Audits**: Review user list regularly
2. **Strong Passwords**: Enforce strong password policies
3. **Role Assignment**: Assign appropriate roles to users
4. **Status Management**: Deactivate instead of deleting when possible
5. **Backup**: Regular database backups

### For Developers

1. **Validation**: Always validate input on both client and server
2. **Error Handling**: Provide clear error messages
3. **Logging**: Log important operations for audit trail
4. **Testing**: Test all CRUD operations thoroughly
5. **Security**: Keep dependencies updated

---

## Future Enhancements

### Planned Features
- [ ] Bulk user operations
- [ ] Export users to CSV/Excel
- [ ] Advanced filtering (date range, multiple roles)
- [ ] User activity logs
- [ ] Email verification workflow
- [ ] Password reset functionality
- [ ] User profile pictures upload
- [ ] Pagination for large datasets
- [ ] Advanced search with operators
- [ ] User groups/teams

---

## Support

For issues or questions:
- Check this documentation first
- Review the troubleshooting section
- Check browser console for errors
- Contact the development team

---

## License

Copyright © 2024 EcoDana. All rights reserved.

---

**Last Updated**: 2024-10-10
**Version**: 1.0.0
**Author**: EcoDana Development Team
