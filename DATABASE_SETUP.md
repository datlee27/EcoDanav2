# 🗄️ Database Setup Guide - EvoDana

## 📋 Prerequisites

- MySQL Server installed and running
- MySQL root access
- Java 17+ installed
- Maven installed

## 🚀 Quick Setup

### Option 1: Automated Setup (Recommended)

#### Windows:
```bash
setup_database.bat
```

#### Linux/Mac:
```bash
chmod +x setup_database.sh
./setup_database.sh
```

### Option 2: Manual Setup

#### Step 1: Create Database and Tables
```bash
mysql -u root -p < src/main/resources/db/ecodanang.sql
```

#### Step 2: Insert Sample Data
```bash
mysql -u root -p < src/main/resources/db/insert_sample_data.sql
```

#### Step 3: Insert Test Data
```bash
mysql -u root -p < src/main/resources/db/insert_test_data.sql
```

## 📊 Database Structure

### Tables Created:
- `Users` - User accounts and authentication
- `Roles` - User roles (Admin, Staff, Customer)
- `Vehicle` - Vehicle inventory
- `Booking` - Booking records
- `CarBrand` - Vehicle brands
- `FuelType` - Fuel types
- `TransmissionType` - Transmission types
- `VehicleCategories` - Vehicle categories
- `CarFeature` - Vehicle features
- `Discount` - Discount codes
- `Insurance` - Insurance options
- `Terms` - Terms and conditions

## 👥 Test Accounts Created

| Role | Email | Password | Access Level |
|------|-------|----------|--------------|
| **Admin** | `admin123@test.com` | `admin123` | Full system access |
| **Owner** | `owner123@test.com` | `owner123` | Manage vehicles & bookings |
| **Customer** | `customer@test.com` | `admin123` | Browse & book vehicles |

## 🔧 Configuration

### Database Connection
Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ecodanav2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Environment Variables (Optional)
Create `.env` file in project root:

```env
DB_PASSWORD=your_mysql_password
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecodanav2
```

## 🚗 Sample Data Included

### Vehicles (6 vehicles)
- Toyota Camry Hybrid (Available)
- VinFast VF e34 (Available)
- Honda Wave Alpha (Available)
- Tesla Model 3 (Available)
- BMW X5 (Available)
- Mercedes C-Class (Booked)

### Bookings (3 bookings)
- Customer booking for Toyota Camry (Active)
- Customer booking for Tesla Model 3 (Pending)
- Customer booking for BMW X5 (Completed)

### Discounts (3 discounts)
- New User 10% Off (NEWUSER10)
- Weekend Special 15% Off (WEEKEND15)
- First Time User 20% Off (FIRST20)

## 🔍 Verification

### Check Database Connection
1. Start the application: `mvn spring-boot:run`
2. Visit: `http://localhost:8080/test-setup`
3. Click "Create All Test Accounts" (should show success)
4. Click "List All Users" (check console for user list)

### Test Login
1. Visit: `http://localhost:8080/login`
2. Try logging in with test accounts
3. Verify correct redirection based on role

## 🐛 Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
- Check MySQL root password
- Ensure MySQL service is running
- Try: `mysql -u root -p` to test connection

### Error: "Database 'ecodanav2' doesn't exist"
- Run the database creation script first
- Check MySQL is running
- Verify script execution

### Error: "Table 'Users' doesn't exist"
- Run the table creation script
- Check for SQL syntax errors
- Verify database selection

### Error: "Duplicate entry" during test data insertion
- This is normal if data already exists
- Use `INSERT IGNORE` to skip duplicates
- Or drop and recreate database

## 📁 File Structure

```
src/main/resources/db/
├── ecodanang.sql              # Database schema
├── insert_sample_data.sql     # Basic sample data
└── insert_test_data.sql       # Test accounts & data

setup_database.bat            # Windows setup script
setup_database.sh             # Linux/Mac setup script
DATABASE_SETUP.md             # This guide
```

## ✅ Success Indicators

- [ ] Database `ecodanav2` created
- [ ] All tables created successfully
- [ ] Test accounts inserted
- [ ] Application starts without errors
- [ ] Login works with test accounts
- [ ] Role-based redirection works

## 🎯 Next Steps

1. **Start Application**: `mvn spring-boot:run`
2. **Test Login**: Visit `/test-setup`
3. **Create More Data**: Use the application to add more vehicles/bookings
4. **Customize**: Modify data as needed for your use case

## 📞 Support

If you encounter issues:
1. Check MySQL service status
2. Verify database connection settings
3. Check application logs
4. Ensure all SQL scripts ran successfully

**Database setup complete! Ready to run the application.** 🚀
