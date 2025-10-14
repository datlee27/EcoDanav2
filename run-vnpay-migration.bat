@echo off
echo Running VNPay Migration...
echo.
echo Please ensure MySQL is running and you have the correct credentials.
echo.
pause

REM Update these with your database credentials
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=ecodana
set DB_USER=root
set DB_PASS=

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < src\main\resources\db\vnpay-migration.sql

echo.
echo Migration completed!
pause
