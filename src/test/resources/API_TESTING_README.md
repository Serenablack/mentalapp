# Mental Health App - Authentication API Testing Guide

This guide provides various methods to test the authentication endpoints of the Mental Health App.

## Prerequisites

1. Make sure the Spring Boot application is running on `http://localhost:8080`
2. Database is properly configured and migrations have run
3. Required environment variables are set (JWT_SECRET, DATABASE_URL, etc.)

## Available Endpoints

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info (requires authentication)
- `POST /api/auth/logout` - User logout
- `GET /api/auth/health` - Health check

## Testing Methods

### 1. Using cURL Commands

#### Health Check

```bash
curl -X GET http://localhost:8080/api/auth/health
```

#### User Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'
```

#### Get Current User (with token)

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 2. Using the Test Script

Make the script executable and run:

```bash
chmod +x src/test/resources/simple-test.sh
./src/test/resources/simple-test.sh
```

### 3. Using HTTP Client Files

If you're using IntelliJ IDEA or VS Code with REST Client extension, use the provided `auth-endpoints.http` file.

### 4. Using Postman

Import the following requests into Postman:

#### Environment Variables

- `baseUrl`: `http://localhost:8080`
- `authUrl`: `{{baseUrl}}/api/auth`

#### Test Collection

1. **Health Check**

   - Method: GET
   - URL: `{{authUrl}}/health`

2. **Register User**

   - Method: POST
   - URL: `{{authUrl}}/register`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):

   ```json
   {
     "username": "testuser",
     "email": "test@example.com",
     "password": "password123",
     "firstName": "Test",
     "lastName": "User"
   }
   ```

3. **Login User**

   - Method: POST
   - URL: `{{authUrl}}/login`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):

   ```json
   {
     "usernameOrEmail": "testuser",
     "password": "password123"
   }
   ```

4. **Get Current User**

   - Method: GET
   - URL: `{{authUrl}}/me`
   - Headers: `Authorization: Bearer {{authToken}}`

5. **Logout**
   - Method: POST
   - URL: `{{authUrl}}/logout`
   - Headers: `Authorization: Bearer {{authToken}}`

## Expected Responses

### Successful Registration Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "message": "User registered successfully"
}
```

### Successful Login Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "message": "Login successful"
}
```

### Error Response

```json
{
  "message": "Registration failed: Username already exists"
}
```

## Common Test Scenarios

### 1. Valid Registration

- Should return 200 with access token
- User should be created in database

### 2. Duplicate Username/Email

- Should return 400 with error message
- No user should be created

### 3. Invalid Data

- Should return 400 with validation errors
- Test cases: short password, invalid email, missing fields

### 4. Valid Login

- Should return 200 with access token
- Token should be valid for accessing protected endpoints

### 5. Invalid Login

- Should return 400 with error message
- Test cases: wrong password, non-existent user

### 6. Protected Endpoint Access

- Without token: Should return 401
- With valid token: Should return 200
- With expired/invalid token: Should return 401

## Security Testing

### 1. SQL Injection

Try injecting SQL in login fields:

```json
{
  "usernameOrEmail": "'; DROP TABLE users; --",
  "password": "password123"
}
```

### 2. XSS Attempts

Try script injection:

```json
{
  "usernameOrEmail": "<script>alert('xss')</script>",
  "password": "password123"
}
```

### 3. Input Validation

Test edge cases:

- Very long usernames (>50 characters)
- Very short passwords (<6 characters)
- Invalid email formats
- Empty required fields

## Performance Testing

Test multiple rapid requests to check:

- Rate limiting (if implemented)
- Server response time
- Database connection handling

## CORS Testing

Test from different origins:

```bash
curl -X OPTIONS http://localhost:8080/api/auth/register \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -H "Origin: http://localhost:4200"
```

## Troubleshooting

### Common Issues

1. **Server not running**: Check if Spring Boot app is started
2. **Database connection**: Verify database is running and accessible
3. **JWT errors**: Check JWT_SECRET is properly configured
4. **CORS errors**: Verify CORS configuration in SecurityConfig
5. **Validation errors**: Check DTO validation annotations

### Debug Tips

1. Enable debug logging in `application.yml`:

   ```yaml
   logging:
     level:
       com.mentalapp: DEBUG
   ```

2. Check application logs for detailed error messages

3. Use database client to verify user creation/updates

4. Test with simple tools first (cURL) before using complex clients

## Next Steps

After authentication endpoints are working:

1. Test mood entry endpoints
2. Test suggested activity endpoints
3. Test emotion endpoints
4. Integrate with frontend Angular application


