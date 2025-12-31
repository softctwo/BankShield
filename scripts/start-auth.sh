#!/bin/bash

# BankShield Authentication Module Startup Script

echo "Starting BankShield Authentication Module..."
echo "============================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Navigate to auth module directory
cd "$(dirname "$0")/../bankshield-auth" || exit 1

# Start the Spring Boot application
echo "Starting application on port 8081..."
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Check if startup was successful
if [ $? -eq 0 ]; then
    echo "BankShield Authentication Module started successfully!"
    echo "Access the application at: http://localhost:8081/auth"
    echo "API documentation: http://localhost:8081/auth/api/auth"
else
    echo "Failed to start BankShield Authentication Module"
    exit 1
fi