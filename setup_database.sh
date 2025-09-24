#!/bin/bash

echo "========================================"
echo "    EvoDana Database Setup Script"
echo "========================================"
echo

echo "[1/3] Creating database and tables..."
mysql -u root -p < src/main/resources/db/ecodanang.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create database and tables"
    exit 1
fi
echo "✓ Database and tables created successfully"
echo

echo "[2/3] Inserting sample data..."
mysql -u root -p < src/main/resources/db/insert_sample_data.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to insert sample data"
    exit 1
fi
echo "✓ Sample data inserted successfully"
echo

echo "[3/3] Inserting test data..."
mysql -u root -p < src/main/resources/db/insert_test_data.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to insert test data"
    exit 1
fi
echo "✓ Test data inserted successfully"
echo

echo "========================================"
echo "    Database Setup Complete!"
echo "========================================"
echo
echo "Test Accounts Created:"
echo "- Admin: admin123@test.com / admin123"
echo "- Owner: owner123@test.com / owner123"  
echo "- Customer: customer@test.com / admin123"
echo
echo "You can now start the application!"
echo
