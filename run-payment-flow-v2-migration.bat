@echo off
echo ========================================
echo Payment Flow V2 Migration
echo Three-party system: Customer - Staff - Owner
echo ========================================
echo.
echo This will add new columns to Booking and Payment tables:
echo - Approval tracking (Owner approve/reject)
echo - Return confirmation (Customer + Owner dual confirm)
echo - Money holding (Staff holds money)
echo - Transfer to owner tracking
echo - Refund tracking
echo.
echo Please ensure:
echo 1. MySQL is running
echo 2. You have backup of the database
echo 3. You have correct database credentials
echo.
pause

REM Update these with your database credentials
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=ecodana
set DB_USER=root
set DB_PASS=

echo.
echo Running migration...
echo.

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < src\main\resources\db\payment-flow-v2-migration.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Migration completed successfully!
    echo ========================================
    echo.
    echo Next steps:
    echo 1. Update Java models (Booking, Payment)
    echo 2. Create new endpoints for Staff and Owner
    echo 3. Update UI for new workflow
    echo.
) else (
    echo.
    echo ========================================
    echo Migration failed!
    echo ========================================
    echo.
    echo Please check:
    echo 1. Database credentials
    echo 2. MySQL is running
    echo 3. Database exists
    echo 4. Error messages above
    echo.
)

pause
