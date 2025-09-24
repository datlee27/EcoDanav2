# 🚗 EvoDana - Vehicle Rental System

A modern vehicle rental system built with Spring Boot, featuring role-based authentication and comprehensive vehicle management.

## 🚀 Quick Start

### 1. Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 2. Database Setup
```bash
# Windows
setup_database.bat

# Linux/Mac
chmod +x setup_database.sh
./setup_database.sh
```

### 3. Run Application
```bash
mvn spring-boot:run
```

### 4. Access Application
- **Homepage:** http://localhost:8080
- **Login:** http://localhost:8080/login

## 👥 Test Accounts

| Role | Email | Password | Dashboard |
|------|-------|----------|-----------|
| **Admin** | `admin123@test.com` | `admin123` | `/admin` |
| **Owner** | `owner123@test.com` | `owner123` | `/owner/dashboard` |
| **Customer** | `customer@test.com` | `admin123` | `/dashboard` |

## 🏗️ Architecture

### Backend
- **Spring Boot 3.x** - Main framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **MySQL** - Database
- **OAuth2** - Google login support

### Frontend
- **Thymeleaf** - Template engine
- **Tailwind CSS** - Styling
- **Font Awesome** - Icons
- **JavaScript** - Interactive features

## 🔐 Authentication System

### Features
- ✅ Username/Email login
- ✅ Password hashing (BCrypt)
- ✅ Role-based access control
- ✅ OAuth2 Google login
- ✅ Session management
- ✅ Automatic role-based redirection

### Roles
- **Admin:** Full system access, user management
- **Staff/Owner:** Vehicle management, booking management
- **Customer:** Browse vehicles, make bookings

## 📁 Project Structure

```
src/main/java/com/ecodana/evodanavn1/
├── controller/
│   ├── AuthController.java          # Login/logout
│   ├── DashboardController.java     # Role-based dashboards
│   ├── OwnerController.java         # Owner management
│   └── TestController.java          # Test utilities
├── security/
│   └── OAuth2LoginSuccessHandler.java
├── service/
│   ├── UserService.java             # User management
│   └── RoleService.java             # Role management
├── model/
│   ├── User.java                    # User entity
│   └── Role.java                    # Role entity
└── SecurityConfig.java              # Security configuration

src/main/resources/
├── templates/
│   ├── user/login.html              # Login page
│   ├── test-setup.html              # Test setup page
│   ├── admin/admin.html             # Admin dashboard
│   ├── owner/dashboard.html         # Owner dashboard
│   └── customer/customer-dashboard.html
├── db/
│   ├── ecodanang.sql                # Database schema
│   ├── insert_sample_data.sql       # Sample data
│   └── insert_test_data.sql         # Test accounts
└── application.properties           # Configuration
```

## 🛠️ Development

### Database Setup
1. Create MySQL database
2. Run SQL scripts in order:
   - `ecodanang.sql` (schema)
   - `insert_sample_data.sql` (basic data)
   - `insert_test_data.sql` (test accounts)

### Configuration
Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecodanav2
spring.datasource.username=root
spring.datasource.password=your_password
```

### Testing
- Use test accounts for login testing
- Check console logs for debugging

## 📊 Features

### Admin Features
- User management
- System overview
- Revenue tracking
- Vehicle management

### Owner/Staff Features
- Vehicle management
- Booking management
- Customer support
- Revenue tracking

### Customer Features
- Browse vehicles
- Make bookings
- View booking history
- Profile management

## 🔧 Troubleshooting

### Common Issues
1. **Database Connection Error**
   - Check MySQL is running
   - Verify credentials in `application.properties`

2. **Login Not Working**
   - Run database setup scripts
   - Check test accounts exist

3. **Role Redirection Issues**
   - Verify role data in database
   - Check SecurityConfig settings


## 📚 Documentation

- [Database Setup Guide](DATABASE_SETUP.md)
- [Login System Guide](LOGIN_GUIDE.md)
- [Role System Guide](ROLE_SYSTEM_GUIDE.md)

## 🚀 Deployment

### Production Setup
1. Configure production database
2. Update `application.properties`
3. Set environment variables
4. Build and deploy JAR file

### Docker (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/evodanav2-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For support and questions:
- Check documentation files
- Review test setup page
- Check application logs
- Create issue in repository

---

**EvoDana - Modern Vehicle Rental System** 🚗✨