# Profile API with Cat Facts

A RESTful API built with Java and Spring Boot that returns personal profile information along with random cat facts fetched from an external API.

## Features

- RESTful `/me` endpoint returning JSON profile data
- Integration with Cat Facts API for dynamic content
- Proper error handling and fallback mechanisms
- CORS enabled for cross-origin requests
- ISO 8601 timestamp formatting
- Comprehensive logging

## Requirements

- **Java 17** or higher
- **Maven 3.6+**
- **Internet connection** (for fetching cat facts)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/profile-api.git
cd profile-api
```

### 2. Configure Your Personal Details

Open `src/main/java/com/yourname/profileapi/controller/ProfileController.java` and update:

```java
.email("your.email@example.com")  // Your email
.name("Your Full Name")            // Your full name
.stack("Java/Spring Boot")         // Your backend stack
```

### 3. Build the Project

```bash
mvn clean install
```

This command:
- `clean` - Removes previous build artifacts
- `install` - Compiles code, runs tests, packages as JAR

### 4. Run Locally

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/profile-api-1.0.0.jar
```

The API will start on `http://localhost:8080`

### 5. Test the Endpoint

Open your browser or use curl:

```bash
curl http://localhost:8080/me
```

Expected response:

```json
{
  "status": "success",
  "user": {
    "email": "your.email@example.com",
    "name": "Your Full Name",
    "stack": "Your stack"
  },
  "timestamp": "2025-10-18T14:30:45.123Z",
  "fact": "Sample cat fact"
}
```

## Dependencies

All dependencies are managed by Maven through `pom.xml`:

- **Spring Boot Starter Web** (3.2.0) - REST API framework
- **Spring Boot DevTools** - Development utilities
- **Lombok** - Reduces boilerplate code
- **Jackson** - JSON serialization (included with Spring Web)
- **Spring Boot Starter Test** - Testing framework

To install dependencies:

```bash
mvn dependency:resolve
```

## Running Tests

```bash
mvn test
```

This runs all unit tests including:
- Endpoint response structure validation
- JSON field existence checks
- HTTP status code verification

## Deploying to Railway

### Step 1: Prepare for Deployment

1. Ensure your code is pushed to GitHub
2. Make sure `pom.xml` includes the Spring Boot Maven plugin (already configured)

### Step 2: Create Railway Account

1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub

### Step 3: Deploy

1. Click **"New Project"**
2. Select **"Deploy from GitHub repo"**
3. Choose your `profile-api` repository
4. Railway will automatically detect it's a Java/Maven project
5. It will run `mvn clean install` and deploy the JAR

### Step 4: Configure (if needed)

Railway auto-detects the port, but you can set environment variables:

- Click on your project
- Go to **Variables** tab
- Add any environment variables (none needed for this project)

### Step 5: Get Your URL

- Railway provides a public URL like: `https://your-app.up.railway.app`
- Your endpoint: `https://your-app.up.railway.app/me`

## Project Structure

```
profile-api/
├── src/
│   ├── main/
│   │   ├── java/com/yourname/profileapi/
│   │   │   ├── ProfileApiApplication.java      # Main entry point
│   │   │   ├── controller/
│   │   │   │   └── ProfileController.java      # REST endpoint
│   │   │   ├── model/
│   │   │   │   └── ProfileResponse.java        # Response model
│   │   │   └── service/
│   │   │       └── CatFactService.java         # External API service
│   │   └── resources/
│   │       └── application.properties          # Configuration
│   └── test/
│       └── java/com/yourname/profileapi/
│           └── controller/
│               └── ProfileControllerTest.java  # Unit tests
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
server.port=8080                          # Server port
spring.application.name=profile-api       # Application name
logging.level.root=INFO                   # Logging level
```

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in application.properties
server.port=8081
```

### Cat Facts API Down

The service includes fallback logic. If the external API fails, it returns:
```
"Cats are amazing creatures!"
```

### Build Failures

```bash
# Clear Maven cache and rebuild
mvn clean install -U
```

## API Documentation

### Endpoint: GET /me

**URL:** `/me`

**Method:** `GET`

**Success Response:**

- **Code:** 200 OK
- **Content-Type:** `application/json`

**Response Schema:**

```json
{
  "status": "string (always 'success')",
  "user": {
    "email": "string",
    "name": "string",
    "stack": "string"
  },
  "timestamp": "string (ISO 8601 format)",
  "fact": "string (random cat fact)"
}
```

**Example:**

```bash
curl -X GET http://localhost:8080/me
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request