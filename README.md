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
git clone https://github.com/chideraa-ude/profile-api.git
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
java -jar target/profile-api-0.0.1-SNAPSHOT.jar
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

## 🌐 Deploying to AWS EC2 with NGINX

This application is deployed on AWS EC2 using NGINX as a reverse proxy, demonstrating professional infrastructure management and DevOps practices.

### Deployment Architecture

```
Internet → AWS EC2 → NGINX (Port 80/443) → Spring Boot Application (Port 8080)
```

### Why AWS EC2 + NGINX?

- **Full Control**: Complete server configuration and customization
- **Professional Setup**: Mirrors real-world production environments
- **Scalability**: Easy to upgrade instance types as traffic grows
- **Security**: Fine-grained control over networking and access
- **Cost-Effective**: AWS Free Tier eligible (t2.micro)

### Prerequisites for Deployment

- AWS Account (Free Tier available)
- Basic Linux/Unix command line knowledge
- SSH client installed
- Your built JAR file (`mvn clean package`)

### Quick Deployment Overview

1. **Launch EC2 Instance**
   - AMI: Ubuntu Server 22.04 LTS
   - Instance Type: t2.micro (Free Tier)
   - Configure Security Group (ports 22, 80, 443)

2. **Server Setup**
   ```bash
   # Install Java 17
   sudo apt update
   sudo apt install openjdk-17-jdk -y
   
   # Install NGINX
   sudo apt install nginx -y
   ```

3. **Deploy Application**
   ```bash
   # Transfer JAR to EC2
   scp -i your-key.pem target/profile-api-0.0.1-SNAPSHOT.jar ubuntu@your-ec2-ip:/home/ubuntu/
   
   # Create systemd service for auto-restart
   sudo nano /etc/systemd/system/profile-api.service
   ```

4. **Configure NGINX as Reverse Proxy**
   ```bash
   # Create NGINX configuration
   sudo nano /etc/nginx/sites-available/profile-api
   
   # Enable configuration
   sudo ln -s /etc/nginx/sites-available/profile-api /etc/nginx/sites-enabled/
   sudo systemctl restart nginx
   ```

5. **Access Your API**
   ```
   http://your-ec2-public-ip/me
   ```

### Detailed Deployment Guide

For complete step-by-step instructions, see [DEPLOYMENT.md](DEPLOYMENT.md)

The deployment guide covers:
- ✅ EC2 instance creation and configuration
- ✅ Security group setup
- ✅ Java and NGINX installation
- ✅ Application deployment with systemd
- ✅ NGINX reverse proxy configuration
- ✅ SSL/TLS setup with Let's Encrypt
- ✅ Monitoring and maintenance
- ✅ Troubleshooting common issues

### Live API Endpoint

**Public Endpoint:** `http://your-ec2-public-ip/me`

**Example Request:**
```bash
curl http://your-ec2-public-ip/me
```

**Example Response:**
```json
{
  "status": "success",
  "user": {
    "email": "your.email@example.com",
    "name": "Your Full Name",
    "stack": "Java/Spring Boot"
  },
  "timestamp": "2025-10-18T15:30:45.123Z",
  "fact": "Cats can rotate their ears 180 degrees."
}
```

### Monitoring Your Deployment

#### Check Application Status
```bash
# SSH into your EC2 instance
ssh -i your-key.pem ubuntu@your-ec2-ip

# Check application service
sudo systemctl status profile-api

# View application logs
sudo journalctl -u profile-api -f
```

#### Check NGINX Status
```bash
# Check NGINX status
sudo systemctl status nginx

# View access logs
sudo tail -f /var/log/nginx/profile-api-access.log

# View error logs
sudo tail -f /var/log/nginx/profile-api-error.log
```

### Updating Your Deployment

```bash
# 1. Build new version locally
mvn clean package

# 2. Stop the service
ssh -i your-key.pem ubuntu@your-ec2-ip
sudo systemctl stop profile-api

# 3. Upload new JAR
scp -i your-key.pem target/profile-api-1.0.0.jar ubuntu@your-ec2-ip:/opt/profile-api/

# 4. Restart service
sudo systemctl start profile-api
```

### Cost Estimation

**AWS Free Tier (First 12 months):**
- EC2 t2.micro: Free (750 hours/month)
- Elastic IP: Free (when attached to running instance)
- Data Transfer: 15 GB/month free

**After Free Tier:**
- EC2 t2.micro: ~$8-10/month
- Data transfer: ~$0.09/GB after 15GB
- **Total estimated cost:** ~$10-15/month

### Security Considerations

- ✅ Security group configured to allow only necessary ports
- ✅ SSH access restricted to specific IP addresses
- ✅ Application runs as non-root user
- ✅ NGINX acts as security layer between internet and application
- ✅ Optional SSL/TLS encryption with Let's Encrypt

### Alternative Deployment Options

If you prefer a Platform-as-a-Service (PaaS) approach with less infrastructure management:

- **Render**: Simple deployment with GitHub integration (free tier available)
- **Fly.io**: Global deployment with free tier
- **Google Cloud Run**: Serverless container deployment
- **Heroku**: Classic PaaS (paid plans only)

### Support & Troubleshooting

If you encounter issues during deployment:

1. Check the [DEPLOYMENT.md](https://github.com/chideraa-ude/profile-api/blob/main/DEPLOYMENT.md) troubleshooting section
2. Review application logs: `sudo journalctl -u profile-api -n 100`
3. Check NGINX logs: `sudo tail -100 /var/log/nginx/error.log`
4. Verify security group allows traffic on port 80
5. Ensure application is running: `sudo systemctl status profile-api`

For additional help, please [open an issue](https://github.com/chideraa-ude/profile-api/issues) on GitHub.

---

### Infrastructure Diagram

```
┌─────────────────────────────────────────────────────────┐
│                     AWS Cloud                            │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │              EC2 Instance (t3.micro)              │  │
│  │                                                    │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │           NGINX (Port 80/443)            │   │  │
│  │  │         (Reverse Proxy + SSL)            │   │  │
│  │  └──────────────┬───────────────────────────┘   │  │
│  │                 │                                 │  │
│  │                 ▼                                 │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │   Spring Boot Application (Port 8080)    │   │  │
│  │  │         (Managed by systemd)             │   │  │
│  │  └──────────────┬───────────────────────────┘   │  │
│  │                 │                                 │  │
│  │                 ▼                                 │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │      External API (catfact.ninja)        │   │  │
│  │  └──────────────────────────────────────────┘   │  │
│  │                                                    │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
└─────────────────────────────────────────────────────────┘
          ▲
          │
     HTTP Requests
          │
    ┌─────┴─────┐
    │  Internet │
    │   Users   │
    └───────────┘
```

### DevOps Skills Demonstrated

This deployment showcases:

- ☑️ **Cloud Infrastructure**: AWS EC2 management
- ☑️ **Linux System Administration**: Ubuntu server configuration
- ☑️ **Web Server Configuration**: NGINX reverse proxy setup
- ☑️ **Process Management**: systemd service configuration
- ☑️ **Security**: Security groups, SSL/TLS, firewall rules
- ☑️ **Monitoring**: Log management and application monitoring
- ☑️ **Networking**: Understanding of ports, proxies, and HTTP/HTTPS
- ☑️ **CI/CD Readiness**: Infrastructure prepared for automated deployments

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