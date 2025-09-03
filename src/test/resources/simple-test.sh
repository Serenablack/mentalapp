#!/bin/bash

# Simple Authentication Endpoints Testing Script
BASE_URL="http://localhost:8080"
AUTH_URL="${BASE_URL}/api/auth"

echo "Testing Authentication Endpoints..."
echo "=================================="

# Test Health Check
echo "1. Testing Health Check..."
curl -s "$AUTH_URL/health" | jq '.'

# Test User Registration
echo -e "\n2. Testing User Registration..."
curl -s -X POST -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }' "$AUTH_URL/register" | jq '.'

# Test User Login
echo -e "\n3. Testing User Login..."
curl -s -X POST -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }' "$AUTH_URL/login" | jq '.'

echo -e "\nTesting Complete!"


