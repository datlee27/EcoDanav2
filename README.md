# EvoDana - Car Rental System

A modern car rental web application built with Spring Boot and Thymeleaf.

## Features

- 🚗 **Vehicle Management** - Browse and search available vehicles
- 👤 **User Authentication** - Register, login, and user management
- 🔐 **Role-based Access** - Customer, Staff, and Admin roles
- 📱 **Responsive Design** - Mobile-friendly interface
- 🔑 **OAuth2 Integration** - Google login support
- 💳 **Booking System** - Vehicle booking and management

## Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript
- **Database**: MySQL
- **Authentication**: Spring Security + OAuth2

## Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Database Setup
1. Create MySQL database: `ecodanav2`
2. Run the SQL scripts in `src/main/resources/db/`:
   - `ecodanang.sql` - Database schema
   - `insert_sample_data.sql` - Sample data

### Configuration
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecodanav2?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application
```bash
mvn spring-boot:run
```

Access the application at: http://localhost:8080

## Default Accounts

After running `insert_sample_data.sql`, you can use:
- **Customer**: Register new account
- **Staff**: Use OAuth2 Google login
- **Admin**: Use OAuth2 Google login

## Project Structure

```
src/main/java/com/ecodana/evodanavn1/
├── config/          # Configuration classes
├── controller/       # REST controllers
├── model/           # Entity models
├── repository/      # Data repositories
├── security/        # Security configuration
└── service/         # Business logic

src/main/resources/
├── db/              # Database scripts
├── static/          # Static assets (CSS, JS)
└── templates/       # Thymeleaf templates
```

## API Endpoints

- `GET /` - Home page
- `GET /login` - Login page
- `POST /login` - Login form submission
- `GET /register` - Registration page
- `POST /register` - Registration form submission
- `GET /vehicles` - Vehicle listing
- `GET /logout` - Logout

## Development

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

## License

This project is licensed under the MIT License.