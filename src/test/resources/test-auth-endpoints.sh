#!/bin/bash

# Mental Health App - Authentication Endpoints Testing Script
# This script tests the authentication endpoints using curl

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URL - Update this according to your environment
BASE_URL="http://localhost:8080"
AUTH_URL="${BASE_URL}/api/auth"

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test results
print_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓ PASS${NC} - $test_name: $message"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC} - $test_name: $message"
        ((TESTS_FAILED++))
    fi
}

# Function to test endpoint
test_endpoint() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$endpoint")
    elif [ "$method" = "POST" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST -H "Content-Type: application/json" -d "$data" "$endpoint")
        else
            response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST "$endpoint")
        fi
    fi
    
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        print_result "$test_name" "PASS" "HTTP $http_code (Expected: $expected_status)"
        echo "Response: $(cat /tmp/response.json)"
    else
        print_result "$test_name" "FAIL" "HTTP $http_code (Expected: $expected_status)"
        echo "Response: $(cat /tmp/response.json)"
    fi
    
    echo "---"
}

# Function to test with authentication
test_auth_endpoint() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local token="$4"
    local expected_status="$5"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -H "Authorization: Bearer $token" "$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST -H "Authorization: Bearer $token" "$endpoint")
    fi
    
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        print_result "$test_name" "PASS" "HTTP $http_code (Expected: $expected_status)"
        echo "Response: $(cat /tmp/response.json)"
    else
        print_result "$test_name" "FAIL" "HTTP $http_code (Expected: $expected_status)"
        echo "Response: $(cat /tmp/response.json)"
    fi
    
    echo "---"
}

# Check if server is running
echo -e "${YELLOW}Checking if server is running...${NC}"
if ! curl -s "$BASE_URL/api/health" > /dev/null; then
    echo -e "${RED}Error: Server is not running at $BASE_URL${NC}"
    echo "Please start the server first and update BASE_URL if needed."
    exit 1
fi
echo -e "${GREEN}Server is running!${NC}"
echo ""

# Start testing
echo -e "${YELLOW}Starting Authentication Endpoints Testing...${NC}"
echo "=================================================="
echo ""

# Test 1: Health Check
test_endpoint "Health Check" "GET" "$AUTH_URL/health" "" "200"

# Test 2: User Registration - Valid Data
test_endpoint "User Registration - Valid" "POST" "$AUTH_URL/register" '{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}' "200"

# Test 3: User Registration - Duplicate Username
test_endpoint "User Registration - Duplicate Username" "POST" "$AUTH_URL/register" '{
  "username": "testuser",
  "email": "another@example.com",
  "password": "password123",
  "firstName": "Another",
  "lastName": "User"
}' "400"

# Test 4: User Registration - Duplicate Email
test_endpoint "User Registration - Duplicate Email" "POST" "$AUTH_URL/register" '{
  "username": "anotheruser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Another",
  "lastName": "User"
}' "400"

# Test 5: User Registration - Invalid Data
test_endpoint "User Registration - Invalid Data" "POST" "$AUTH_URL/register" '{
  "username": "test",
  "email": "invalid-email",
  "password": "123"
}' "400"

# Test 6: User Login - Valid Credentials
test_endpoint "User Login - Valid" "POST" "$AUTH_URL/login" '{
  "usernameOrEmail": "testuser",
  "password": "password123"
}' "200"

# Test 7: User Login - Invalid Password
test_endpoint "User Login - Invalid Password" "POST" "$AUTH_URL/register" '{
  "usernameOrEmail": "testuser",
  "password": "wrongpassword"
}' "400"

# Test 8: User Login - Non-existent User
test_endpoint "User Login - Non-existent User" "POST" "$AUTH_URL/login" '{
  "usernameOrEmail": "nonexistent",
  "password": "password123"
}' "400"

# Test 9: Get Current User - Without Token
test_endpoint "Get Current User - No Token" "GET" "$AUTH_URL/me" "" "401"

# Test 10: Test CORS Preflight
echo -e "${BLUE}Testing: CORS Preflight${NC}"
response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X OPTIONS \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -H "Origin: http://localhost:4200" \
  "$AUTH_URL/register")

http_code="${response: -3}"
if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
    print_result "CORS Preflight" "PASS" "HTTP $http_code"
else
    print_result "CORS Preflight" "FAIL" "HTTP $http_code"
fi
echo "---"

# Test 11: Test with Different Content Types
test_endpoint "XML Content Type" "POST" "$AUTH_URL/register" '<user><username>xmluser</username></user>' "400"

# Test 12: Test with Different HTTP Methods
test_endpoint "PUT to Login Endpoint" "PUT" "$AUTH_URL/login" '{"usernameOrEmail": "testuser", "password": "password123"}' "405"

# Test 13: Test Edge Cases
test_endpoint "Very Long Username" "POST" "$AUTH_URL/register" '{
  "username": "thisisareallylongusernamewithmorethanfiftycharacterswhichshouldfailvalidation",
  "email": "long@example.com",
  "password": "password123",
  "firstName": "Long",
  "lastName": "Username"
}' "400"

test_endpoint "Very Short Password" "POST" "$AUTH_URL/register" '{
  "username": "shortpass",
  "email": "short@example.com",
  "password": "123",
  "firstName": "Short",
  "lastName": "Password"
}' "400"

test_endpoint "Invalid Email Format" "POST" "$AUTH_URL/register" '{
  "username": "invalidemail",
  "email": "notanemail",
  "password": "password123",
  "firstName": "Invalid",
  "lastName": "Email"
}' "400"

# Test 14: Test SQL Injection Protection
test_endpoint "SQL Injection Attempt" "POST" "$AUTH_URL/login" '{
  "usernameOrEmail": "''; DROP TABLE users; --",
  "password": "password123"
}' "400"

# Test 15: Test XSS Protection
test_endpoint "XSS Attempt" "POST" "$AUTH_URL/login" '{
  "usernameOrEmail": "<script>alert(''xss'')</script>",
  "password": "password123"
}' "400"

# Test 16: Test Performance - Multiple Rapid Requests
echo -e "${BLUE}Testing: Multiple Rapid Requests${NC}"
for i in {1..3}; do
    response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST \
      -H "Content-Type: application/json" \
      -d '{"usernameOrEmail": "testuser", "password": "password123"}' \
      "$AUTH_URL/login")
    
    http_code="${response: -3}"
    if [ "$http_code" = "200" ]; then
        print_result "Rapid Request $i" "PASS" "HTTP $http_code"
    else
        print_result "Rapid Request $i" "FAIL" "HTTP $http_code"
    fi
done
echo "---"

# Test 17: Test Authentication Flow
echo -e "${BLUE}Testing: Complete Authentication Flow${NC}"

# Register a new user for flow testing
echo "Step 1: Registering new user for flow test..."
register_response=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{
    "username": "flowtest",
    "email": "flow@example.com",
    "password": "password123",
    "firstName": "Flow",
    "lastName": "Test"
  }' "$AUTH_URL/register")

if echo "$register_response" | grep -q "accessToken"; then
    print_result "Authentication Flow - Registration" "PASS" "User registered successfully"
    
    # Extract token
    token=$(echo "$register_response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$token" ]; then
        print_result "Authentication Flow - Token Extraction" "PASS" "Token extracted successfully"
        
        # Test protected endpoint with token
        echo "Step 2: Testing protected endpoint with token..."
        auth_response=$(curl -s -w "%{http_code}" -o /tmp/response.json \
          -H "Authorization: Bearer $token" "$AUTH_URL/me")
        
        http_code="${auth_response: -3}"
        if [ "$http_code" = "200" ]; then
            print_result "Authentication Flow - Protected Endpoint" "PASS" "HTTP $http_code"
        else
            print_result "Authentication Flow - Protected Endpoint" "FAIL" "HTTP $http_code"
        fi
        
        # Test logout
        echo "Step 3: Testing logout..."
        logout_response=$(curl -s -w "%{http_code}" -o /tmp/response.json \
          -X POST -H "Authorization: Bearer $token" "$AUTH_URL/logout")
        
        http_code="${logout_response: -3}"
        if [ "$http_code" = "200" ]; then
            print_result "Authentication Flow - Logout" "PASS" "HTTP $http_code"
        else
            print_result "Authentication Flow - Logout" "FAIL" "HTTP $http_code"
        fi
        
    else
        print_result "Authentication Flow - Token Extraction" "FAIL" "No token found in response"
    fi
else
    print_result "Authentication Flow - Registration" "FAIL" "Registration failed"
fi
echo "---"

# Print final results
echo "=================================================="
echo -e "${YELLOW}Testing Complete!${NC}"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please check the output above.${NC}"
    exit 1
fi


