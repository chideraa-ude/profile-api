# Complete AWS EC2 Deployment Guide

This guide provides step-by-step instructions for deploying the Profile API to AWS EC2 with NGINX as a reverse proxy.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Part 1: AWS EC2 Setup](#part-1-aws-ec2-setup)
3. [Part 2: Server Configuration](#part-2-server-configuration)
4. [Part 3: Application Deployment](#part-3-application-deployment)
5. [Part 4: NGINX Configuration](#part-4-nginx-configuration)
6. [Part 5: SSL/TLS Setup (Optional)](#part-5-ssltls-setup-optional)
7. [Part 6: Monitoring & Maintenance](#part-6-monitoring--maintenance)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required

- ✅ AWS Account ([Sign up here](https://aws.amazon.com/free/))
- ✅ Built JAR file (`mvn clean package`)
- ✅ SSH client (Terminal on Mac/Linux, PuTTY on Windows)
- ✅ Basic command line knowledge

### Optional

- Domain name (for SSL/custom URL)
- GitHub account (for continuous deployment)

---

## Part 1: AWS EC2 Setup

### Step 1.1: Access AWS Console

1. Log in to [AWS Management Console](https://console.aws.amazon.com/)
2. Select your preferred region (e.g., `us-east-1`, `eu-west-1`)
3. Navigate to **EC2 Dashboard**

### Step 1.2: Launch EC2 Instance

Click **"Launch Instance"** and configure:

#### Name and Tags
```
Name: profile-api-server
```

#### Application and OS Images (AMI)
```
AMI: Ubuntu Server 22.04 LTS (HVM), SSD Volume Type
Architecture: 64-bit (x86)
```
**Why Ubuntu?** Stable, well-documented, and widely used in production.

#### Instance Type
```
Instance Type: t2.micro
```
**Specifications:**
- 1 vCPU
- 1 GiB Memory
- Free Tier eligible (750 hours/month for 12 months)

#### Key Pair (Login)

1. Click **"Create new key pair"**
2. Configure:
   ```
   Key pair name: profile-api-key
   Key pair type: RSA
   Private key file format: .pem (Mac/Linux) or .ppk (Windows/PuTTY)
   ```
3. Click **"Create key pair"**
4. **IMPORTANT:** Download and save securely - you cannot download it again!

**For Mac/Linux users:**
```bash
# Move to secure location
mv ~/Downloads/profile-api-key.pem ~/.ssh/
chmod 400 ~/.ssh/profile-api-key.pem
```

#### Network Settings

Click **"Edit"** and configure:

```
VPC: Default VPC (or create new)
Subnet: No preference (default subnet)
Auto-assign public IP: Enable
```

**Security Group Configuration:**

Create a new security group named: `profile-api-sg`

Add these inbound rules:

| Type  | Protocol | Port Range | Source    | Description           |
|-------|----------|------------|-----------|-----------------------|
| SSH   | TCP      | 22         | My IP     | SSH access            |
| HTTP  | TCP      | 80         | 0.0.0.0/0 | Public HTTP access    |
| HTTPS | TCP      | 443        | 0.0.0.0/0 | Public HTTPS access   |

**Security Note:** Setting SSH source to "My IP" restricts access to your current IP address for better security.

#### Storage

```
Volume Type: gp3 (General Purpose SSD)
Size: 8 GiB (default)
Delete on Termination: Yes
```

#### Advanced Details

Leave default settings.

### Step 1.3: Launch and Connect

1. Review all settings
2. Click **"Launch Instance"**
3. Wait for instance state to show **"Running"** (1-2 minutes)
4. Note your **Public IPv4 address** (e.g., `54.123.45.67`)

---

## Part 2: Server Configuration

### Step 2.1: Connect to EC2 Instance

#### Mac/Linux:

```bash
ssh -i ~/.ssh/profile-api-key.pem ubuntu@54.123.45.67
```

Replace `54.123.45.67` with your instance's public IP.

**First-time connection:**
```
The authenticity of host '54.123.45.67' can't be established.
Are you sure you want to continue connecting (yes/no)? yes
```

#### Windows (PuTTY):

1. Open PuTTY
2. **Host Name:** `ubuntu@54.123.45.67`
3. **Connection → SSH → Auth:** Browse to your `.ppk` file
4. Click **"Open"**

### Step 2.2: Update System Packages

```bash
# Update package lists
sudo apt update

# Upgrade installed packages
sudo apt upgrade -y
```

This may take 2-5 minutes.

### Step 2.3: Install Java 17

```bash
# Install OpenJDK 17
sudo apt install openjdk-17-jdk -y

# Verify installation
java -version
```

**Expected output:**
```
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment (build 17.0.9+9-Ubuntu-122.04)
OpenJDK 64-Bit Server VM (build 17.0.9+9-Ubuntu-122.04, mixed mode, sharing)
```

### Step 2.4: Install NGINX

```bash
# Install NGINX
sudo apt install nginx -y

# Start NGINX service
sudo systemctl start nginx

# Enable NGINX to start on boot
sudo systemctl enable nginx

# Check NGINX status
sudo systemctl status nginx
```

**Expected output:**
```
● nginx.service - A high performance web server and a reverse proxy server
   Loaded: loaded (/lib/systemd/system/nginx.service; enabled)
   Active: active (running)
```

**Test NGINX:**
Open your browser and navigate to: `http://54.123.45.67`

You should see the **"Welcome to nginx!"** page.

### Step 2.5: Configure Firewall (UFW)

```bash
# Enable UFW
sudo ufw enable

# Allow SSH (IMPORTANT: Do this first!)
sudo ufw allow 22/tcp

# Allow HTTP
sudo ufw allow 80/tcp

# Allow HTTPS
sudo ufw allow 443/tcp

# Check status
sudo ufw status
```

**Expected output:**
```
Status: active

To                         Action      From
--                         ------      ----
22/tcp                     ALLOW       Anywhere
80/tcp                     ALLOW       Anywhere
443/tcp                    ALLOW       Anywhere
```

---

## Part 3: Application Deployment

### Step 3.1: Transfer JAR File to EC2

#### Option A: Using SCP (From Your Local Machine)

```bash
# Navigate to your project directory
cd /path/to/profile-api

# Build JAR if not already built
mvn clean package

# Transfer to EC2
scp -i ~/.ssh/profile-api-key.pem target/profile-api-0.0.1-SNAPSHOT.jar ubuntu@54.123.45.67:/home/ubuntu/
```

#### Option B: Using Git (Recommended for Updates)

On your **EC2 instance**:

```bash
# Install Git and Maven
sudo apt install git maven -y

# Clone repository
cd /home/ubuntu
git clone https://github.com/yourusername/profile-api.git

# Navigate to project
cd profile-api

# Build project
mvn clean package

# Verify JAR was created
ls -lh target/profile-api-1.0.0.jar
```

### Step 3.2: Create Application Directory

```bash
# Create directory for application
sudo mkdir -p /opt/profile-api

# Move JAR to application directory
sudo mv ~/profile-api-1.0.0.jar /opt/profile-api/
# Or if using Git:
sudo mv ~/profile-api/target/profile-api-1.0.0.jar /opt/profile-api/

# Set permissions
sudo chown -R ubuntu:ubuntu /opt/profile-api

# Verify
ls -lh /opt/profile-api/
```

### Step 3.3: Test Application Manually

```bash
# Run application (foreground test)
java -jar /opt/profile-api/profile-api-1.0.0.jar
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started ProfileApiApplication in 3.456 seconds (JVM running for 4.123)
```

**Test the endpoint:**

Open a new terminal and SSH into your instance, then:

```bash
curl http://localhost:8080/me
```

You should see your JSON response!

**Stop the test:** Press `Ctrl+C`

### Step 3.4: Create Systemd Service

Create a systemd service file for automatic startup and management:

```bash
sudo nano /etc/systemd/system/profile-api.service
```

**Paste this configuration:**

```ini
[Unit]
Description=Profile API Spring Boot Application
Documentation=https://github.com/yourusername/profile-api
After=syslog.target network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/profile-api
ExecStart=/usr/bin/java -jar /opt/profile-api/profile-api-1.0.0.jar
SuccessExitStatus=143

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=profile-api

# Restart policy
Restart=always
RestartSec=10

# Security settings
NoNewPrivileges=true
PrivateTmp=true

# Resource limits
LimitNOFILE=65536

# Environment variables (add as needed)
# Environment="SPRING_PROFILES_ACTIVE=production"
# Environment="SERVER_PORT=8080"

[Install]
WantedBy=multi-user.target
```

**Save and exit:** Press `Ctrl+X`, then `Y`, then `Enter`

**Understanding the configuration:**

- `After=`: Ensures network is available before starting
- `User=ubuntu`: Runs as non-root user (security best practice)
- `Restart=always`: Automatically restarts if it crashes
- `RestartSec=10`: Waits 10 seconds before restarting
- `StandardOutput=journal`: Logs to systemd journal

### Step 3.5: Start and Enable Service

```bash
# Reload systemd to recognize new service
sudo systemctl daemon-reload

# Start the service
sudo systemctl start profile-api

# Check status
sudo systemctl status profile-api

# Enable auto-start on boot
sudo systemctl enable profile-api
```

**Expected output:**
```
● profile-api.service - Profile API Spring Boot Application
     Loaded: loaded (/etc/systemd/system/profile-api.service; enabled)
     Active: active (running) since Sat 2025-10-18 10:30:45 UTC; 5s ago
   Main PID: 12345 (java)
      Tasks: 25 (limit: 1137)
     Memory: 256.0M
```

### Step 3.6: View Application Logs

```bash
# View last 50 lines
sudo journalctl -u profile-api -n 50

# Follow logs in real-time
sudo journalctl -u profile-api -f

# View logs since last boot
sudo journalctl -u profile-api -b
```

**Exit real-time logs:** Press `Ctrl+C`

### Step 3.7: Test Application

```bash
curl http://localhost:8080/me
```

You should see your JSON response. ✅

---

## Part 4: NGINX Configuration

### Step 4.1: Remove Default NGINX Site

```bash
# Remove default site
sudo rm /etc/nginx/sites-enabled/default
```

### Step 4.2: Create NGINX Configuration

```bash
sudo nano /etc/nginx/sites-available/profile-api
```

**Paste this configuration:**

```nginx
# Upstream definition - Spring Boot application
upstream spring_boot {
    server 127.0.0.1:8080;
    keepalive 32;
}

# HTTP server block
server {
    listen 80;
    listen [::]:80;
    
    # Replace with your domain or EC2 public IP
    server_name 54.123.45.67;
    
    # Maximum upload size
    client_max_body_size 10M;
    
    # Logging
    access_log /var/log/nginx/profile-api-access.log;
    error_log /var/log/nginx/profile-api-error.log warn;
    
    # Root location - proxy to Spring Boot
    location / {
        proxy_pass http://spring_boot;
        
        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        
        # HTTP version
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
    
    # Health check endpoint (optional)
    location /actuator/health {
        proxy_pass http://spring_boot/actuator/health;
        access_log off;
    }
    
    # Deny access to hidden files
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }
}
```

**Important:** Replace `54.123.45.67` with your actual EC2 public IP address!

**Save and exit:** `Ctrl+X`, `Y`, `Enter`

### Step 4.3: Enable Configuration

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/profile-api /etc/nginx/sites-enabled/

# Test NGINX configuration
sudo nginx -t
```

**Expected output:**
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### Step 4.4: Restart NGINX

```bash
sudo systemctl restart nginx

# Verify NGINX is running
sudo systemctl status nginx
```

### Step 4.5: Test Your Deployment

**From your browser:**
```
http://54.123.45.67/me
```

**Using curl:**
```bash
curl http://54.123.45.67/me
```

**Expected response:**
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

🎉 **Congratulations! Your API is now live!**

---

## Part 5: SSL/TLS Setup (Optional)

For production deployments, SSL/TLS encryption is essential.

### Step 5.1: Prerequisites

You need a domain name pointing to your EC2 instance:

1. **Register a domain** (Namecheap, GoDaddy, AWS Route 53)
2. **Create an A record** pointing to your EC2 public IP
3. **Wait for DNS propagation** (5-30 minutes)

**Verify DNS:**
```bash
nslookup yourdomain.com
```

Should return your EC2 IP address.

### Step 5.2: Install Certbot

```bash
# Install Certbot and NGINX plugin
sudo apt install certbot python3-certbot-nginx -y

# Verify installation
certbot --version
```

### Step 5.3: Update NGINX Configuration

```bash
sudo nano /etc/nginx/sites-available/profile-api
```

Change `server_name` to your domain:
```nginx
server_name yourdomain.com www.yourdomain.com;
```

Save and restart NGINX:
```bash
sudo nginx -t
sudo systemctl restart nginx
```

### Step 5.4: Obtain SSL Certificate

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

**Follow the prompts:**

1. **Enter email address:** (for renewal notifications)
2. **Agree to Terms of Service:** Yes
3. **Share email with EFF:** Your choice
4. **Redirect HTTP to HTTPS:** Choose option 2 (Redirect)

**Certbot will:**
- Obtain SSL certificate from Let's Encrypt
- Automatically configure NGINX for HTTPS
- Set up HTTP to HTTPS redirect
- Configure auto-renewal

### Step 5.5: Verify SSL

**Visit your site:**
```
https://yourdomain.com/me
```

You should see a padlock icon in your browser! 🔒

**Check SSL certificate:**
```bash
sudo certbot certificates
```

### Step 5.6: Test Auto-Renewal

```bash
# Dry run (test without actually renewing)
sudo certbot renew --dry-run
```

**Expected output:**
```
Congratulations, all simulated renewals succeeded
```

Certbot automatically renews certificates before expiration.

---

## Part 6: Monitoring & Maintenance

### Application Management

#### Start/Stop/Restart Application

```bash
# Start
sudo systemctl start profile-api

# Stop
sudo systemctl stop profile-api

# Restart
sudo systemctl restart profile-api

# Status
sudo systemctl status profile-api
```

#### View Application Logs

```bash
# Last 100 lines
sudo journalctl -u profile-api -n 100

# Follow in real-time
sudo journalctl -u profile-api -f

# Logs from last hour
sudo journalctl -u profile-api --since "1 hour ago"

# Logs from specific date
sudo journalctl -u profile-api --since "2025-10-18 00:00:00"
```

### NGINX Management

#### Start/Stop/Restart NGINX

```bash
# Start
sudo systemctl start nginx

# Stop
sudo systemctl stop nginx

# Restart (brief downtime)
sudo systemctl restart nginx

# Reload (no downtime)
sudo systemctl reload nginx

# Status
sudo systemctl status nginx
```

#### View NGINX Logs

```bash
# Access log (successful requests)
sudo tail -f /var/log/nginx/profile-api-access.log

# Error log
sudo tail -f /var/log/nginx/profile-api-error.log

# Last 100 lines
sudo tail -100 /var/log/nginx/profile-api-access.log
```

### System Monitoring

#### Check Resource Usage

```bash
# CPU and memory
htop
# Or
top

# Disk space
df -h

# Memory usage
free -h

# Network connections
sudo netstat -tulpn | grep LISTEN
```

#### Check Application Process

```bash
# Find Java processes
ps aux | grep java

# Check port 8080
sudo lsof -i :8080

# Check port 80
sudo lsof -i :80
```

### Application Updates

#### Method 1: Manual Update

```bash
# 1. Build new version locally
mvn clean package

# 2. Transfer to EC2
scp -i ~/.ssh/profile-api-key.pem target/profile-api-1.0.0.jar ubuntu@54.123.45.67:/home/ubuntu/

# 3. SSH into EC2
ssh -i ~/.ssh/profile-api-key.pem ubuntu@54.123.45.67

# 4. Stop application
sudo systemctl stop profile-api

# 5. Backup old version
sudo cp /opt/profile-api/profile-api-1.0.0.jar /opt/profile-api/profile-api-backup.jar

# 6. Replace with new version
sudo mv /home/ubuntu/profile-api-1.0.0.jar /opt/profile-api/

# 7. Start application
sudo systemctl start profile-api

# 8. Check status
sudo systemctl status profile-api

# 9. View logs
sudo journalctl -u profile-api -f
```

#### Method 2: Git Update (If deployed via Git)

```bash
# SSH into EC2
ssh -i ~/.ssh/profile-api-key.pem ubuntu@54.123.45.67

# Navigate to project
cd ~/profile-api

# Pull latest changes
git pull origin main

# Build
mvn clean package

# Stop service
sudo systemctl stop profile-api

# Update JAR
sudo mv target/profile-api-1.0.0.jar /opt/profile-api/

# Start service
sudo systemctl start profile-api
```

### Automated Backups

Create a backup script:

```bash
sudo nano /opt/profile-api/backup.sh
```

**Paste this script:**

```bash
#!/bin/bash

# Configuration
BACKUP_DIR="/opt/profile-api/backups"
APP_DIR="/opt/profile-api"
JAR_NAME="profile-api-1.0.0.jar"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup JAR file
cp $APP_DIR/$JAR_NAME $BACKUP_DIR/${JAR_NAME%.jar}_$DATE.jar

# Keep only last 7 days of backups
find $BACKUP_DIR -name "profile-api-*.jar" -mtime +7 -delete

# Log
echo "Backup completed: ${JAR_NAME%.jar}_$DATE.jar"
```

**Make executable:**

```bash
sudo chmod +x /opt/profile-api/backup.sh
```

**Schedule with cron:**

```bash
sudo crontab -e
```

**Add this line** (runs daily at 2 AM):

```cron
0 2 * * * /opt/profile-api/backup.sh >> /var/log/profile-api-backup.log 2>&1
```

**Save and exit:** `Ctrl+X`, `Y`, `Enter`

### Log Rotation

Configure log rotation to prevent logs from consuming disk space:

```bash
sudo nano /etc/logrotate.d/profile-api
```

**Paste this configuration:**

```
/var/log/nginx/profile-api-*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 www-data adm
    sharedscripts
    postrotate
        if [ -f /var/run/nginx.pid ]; then
            kill -USR1 `cat /var/run/nginx.pid`
        fi
    endscript
}
```

**Test log rotation:**

```bash
sudo logrotate -d /etc/logrotate.d/profile-api
```

---

## Troubleshooting

### Issue 1: Cannot Connect via SSH

**Symptoms:**
```
ssh: connect to host 54.123.45.67 port 22: Connection timed out
```

**Solutions:**

1. **Check Security Group:**
   - AWS Console → EC2 → Security Groups
   - Verify port 22 is open for your IP

2. **Check Instance State:**
   - Ensure instance is "Running"
   - Check "Status Checks" (should be 2/2 checks passed)

3. **Verify Key Permissions:**
   ```bash
   chmod 400 ~/.ssh/profile-api-key.pem
   ```

4. **Check Public IP:**
   - Verify you're using the correct public IP address
   - Public IP changes if instance is stopped/started

### Issue 2: Application Not Starting

**Symptoms:**
```
● profile-api.service - Profile API Spring Boot Application
     Active: failed (Result: exit-code)
```

**Diagnosis:**

```bash
# Check detailed logs
sudo journalctl -u profile-api -n 100 --no-pager

# Check Java is installed
java -version

# Verify JAR file exists
ls -lh /opt/profile-api/profile-api-1.0.0.jar

# Test JAR manually
java -jar /opt/profile-api/profile-api-1.0.0.jar
```

**Common Causes:**

1. **Wrong Java version:**
   ```bash
   # Install Java 17
   sudo apt install openjdk-17-jdk -y
   ```

2. **Port 8080 already in use:**
   ```bash
   # Check what's using port 8080
   sudo lsof -i :8080
   
   # Kill the process if needed
   sudo kill -9 <PID>
   ```

3. **Insufficient memory:**
   ```bash
   # Check memory
   free -h
   
   # Limit Java memory in systemd service
   sudo nano /etc/systemd/system/profile-api.service
   # Add to [Service] section:
   Environment="JAVA_OPTS=-Xmx512m -Xms256m"
   
   sudo systemctl daemon-reload
   sudo systemctl restart profile-api
   ```

4. **Permission issues:**
   ```bash
   sudo chown -R ubuntu:ubuntu /opt/profile-api
   ```

### Issue 3: 502 Bad Gateway from NGINX

**Symptoms:**
Browser shows "502 Bad Gateway" when accessing the API.

**Diagnosis:**

```bash
# Check if Spring Boot is running
sudo systemctl status profile-api

# Check if app is listening on 8080
sudo netstat -tlnp | grep 8080

# Check NGINX error logs
sudo tail -50 /var/log/nginx/profile-api-error.log

# Test direct connection to Spring Boot
curl http://localhost:8080/me
```

**Solutions:**

1. **Application not running:**
   ```bash
   sudo systemctl start profile-api
   ```

2. **Wrong upstream configuration:**
   ```bash
   sudo nano /etc/nginx/sites-available/profile-api
   # Verify: proxy_pass http://127.0.0.1:8080;
   ```

3. **SELinux blocking connection (rare on Ubuntu):**
   ```bash
   # Check SELinux status
   sestatus
   
   # If enabled and causing issues:
   sudo setsebool -P httpd_can_network_connect 1
   ```

### Issue 4: 404 Not Found

**Symptoms:**
```json
{
  "timestamp": "2025-10-18T10:30:45.123Z",
  "status": 404,
  "error": "Not Found",
  "path": "/me"
}
```

**Diagnosis:**

```bash
# Test Spring Boot directly
curl http://localhost:8080/me

# Check application logs
sudo journalctl -u profile-api -n 50
```

**Solutions:**

1. **Missing @GetMapping annotation:**
   - Verify controller has `@GetMapping("/me")`
   - Check application startup logs for mapped endpoints

2. **Wrong package structure:**
   - Ensure controller is in correct package
   - Check `@SpringBootApplication` is in root package

3. **Application context path:**
   - Check if `server.servlet.context-path` is set in application.properties

### Issue 5: External API (Cat Facts) Failing

**Symptoms:**
API returns fallback message instead of real cat facts.

**Diagnosis:**

```bash
# Check application logs
sudo journalctl -u profile-api | grep "cat fact"

# Test external API from EC2
curl https://catfact.ninja/fact

# Check DNS resolution
nslookup catfact.ninja

# Check outbound internet connectivity
ping -c 3 8.8.8.8
```

**Solutions:**

1. **Security group blocking outbound traffic:**
   - Check outbound rules allow HTTPS (port 443)
   - Default is usually "All traffic" allowed outbound

2. **Network ACL restrictions:**
   - Check VPC Network ACLs
   - Ensure outbound rules allow ports 80 and 443

3. **External API is down:**
   - This is normal - your fallback is working correctly
   - Application will resume getting cat facts when API recovers

### Issue 6: High Memory Usage

**Symptoms:**
```bash
free -h
# Shows very low available memory
```

**Solutions:**

1. **Limit Java heap size:**
   ```bash
   sudo nano /etc/systemd/system/profile-api.service
   ```
   
   Add under `[Service]`:
   ```ini
   Environment="JAVA_OPTS=-Xmx384m -Xms128m"
   ```
   
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart profile-api
   ```

2. **Add swap space:**
   ```bash
   # Create 1GB swap file
   sudo fallocate -l 1G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   
   # Make permanent
   echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
   
   # Verify
   free -h
   ```

3. **Upgrade instance type:**
   - Stop instance
   - Change instance type to t2.small (2GB RAM)
   - Start instance
   - **Note:** t2.small is not free tier eligible

### Issue 7: SSL Certificate Renewal Fails

**Symptoms:**
```
certbot renew
# Shows errors
```

**Solutions:**

1. **Check Certbot timer:**
   ```bash
   sudo systemctl status certbot.timer
   sudo systemctl start certbot.timer
   sudo systemctl enable certbot.timer
   ```

2. **Manual renewal:**
   ```bash
   sudo certbot renew --dry-run
   sudo certbot renew
   ```

3. **NGINX blocking challenge:**
   ```bash
   # Temporarily allow .well-known directory
   sudo nano /etc/nginx/sites-available/profile-api
   ```
   
   Add to server block:
   ```nginx
   location /.well-known/acme-challenge/ {
       root /var/www/html;
   }
   ```
   
   ```bash
   sudo systemctl reload nginx
   sudo certbot renew
   ```

### Issue 8: Can't Access EC2 After Reboot

**Symptoms:**
Instance running but cannot SSH or access application.

**Solutions:**

1. **Public IP changed:**
   - Check new public IP in AWS Console
   - Consider using Elastic IP (static IP)

2. **Services not auto-starting:**
   ```bash
   # Enable services
   sudo systemctl enable profile-api
   sudo systemctl enable nginx
   ```

3. **Allocate Elastic IP (prevents IP changes):**
   - AWS Console → EC2 → Elastic IPs
   - Allocate new address
   - Associate with your instance

---

## Performance Optimization

### Enable GZIP Compression

```bash
sudo nano /etc/nginx/nginx.conf
```

Find the `gzip` settings and ensure:

```nginx
gzip on;
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss;
```

```bash
sudo systemctl reload nginx
```

### Enable HTTP/2 (with SSL)

If you have SSL configured:

```bash
sudo nano /etc/nginx/sites-available/profile-api
```

Change:
```nginx
listen 443 ssl;
```

To:
```nginx
listen 443 ssl http2;
```

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### Add Caching Headers

```bash
sudo nano /etc/nginx/sites-available/profile-api
```

Add to location block:

```nginx
location / {
    proxy_pass http://spring_boot;
    
    # Add caching headers for static content
    location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # ... rest of proxy settings
}
```

### Monitor Performance

```bash
# Install htop for better monitoring
sudo apt install htop -y

# Run htop
htop

# Monitor NGINX connections
watch -n 1 'sudo netstat -an | grep :80 | wc -l'

# Check application response time
time curl http://localhost:8080/me
```

---

## Security Best Practices

### 1. Regular Updates

```bash
# Create update script
sudo nano /opt/scripts/system-update.sh
```

```bash
#!/bin/bash
apt update
apt upgrade -y
apt autoremove -y
```

```bash
sudo chmod +x /opt/scripts/system-update.sh

# Schedule weekly updates (Sundays at 3 AM)
sudo crontab -e
# Add:
0 3 * * 0 /opt/scripts/system-update.sh >> /var/log/system-update.log 2>&1
```

### 2. Fail2Ban (Prevent Brute Force)

```bash
# Install Fail2Ban
sudo apt install fail2ban -y

# Create local configuration
sudo cp /etc/fail2ban/jail.conf /etc/fail2ban/jail.local

# Enable SSH protection
sudo nano /etc/fail2ban/jail.local
```

Find `[sshd]` section and ensure:
```ini
[sshd]
enabled = true
port = 22
filter = sshd
logpath = /var/log/auth.log
maxretry = 3
bantime = 3600
```

```bash
sudo systemctl restart fail2ban
sudo systemctl enable fail2ban

# Check status
sudo fail2ban-client status sshd
```

### 3. Disable Root Login

```bash
sudo nano /etc/ssh/sshd_config
```

Ensure these lines:
```
PermitRootLogin no
PasswordAuthentication no
```

```bash
sudo systemctl restart sshd
```

### 4. Enable Automatic Security Updates

```bash
sudo apt install unattended-upgrades -y
sudo dpkg-reconfigure -plow unattended-upgrades
```

### 5. Configure AWS Security Group Properly

- **SSH (22):** Only from your IP
- **HTTP (80):** From anywhere (0.0.0.0/0)
- **HTTPS (443):** From anywhere (0.0.0.0/0)
- **Block all other ports**

---

## Cost Management

### Monitor Your Costs

1. **AWS Billing Dashboard**
   - Set up billing alerts
   - Monitor daily costs

2. **Set Budget Alerts**
   - AWS Console → Billing → Budgets
   - Create budget: $10/month
   - Get email alerts at 80% and 100%

### Free Tier Limits

- **EC2 t2.micro:** 750 hours/month (one instance always on)
- **Data Transfer:** 15 GB/month outbound
- **Elastic IP:** Free when attached to running instance

### Reduce Costs

1. **Stop instance when not needed:**
   ```bash
   # From AWS CLI
   aws ec2 stop-instances --instance-ids i-1234567890abcdef0
   ```
   **Note:** Public IP will change unless using Elastic IP

2. **Use Reserved Instances** (after free tier):
   - Commit to 1-3 years
   - Save up to 72% vs on-demand

3. **Clean up unused resources:**
   - Delete unused snapshots
   - Release unattached Elastic IPs
   - Remove old AMIs

---

## Backup and Disaster Recovery

### Create AMI (Amazon Machine Image)

1. **AWS Console → EC2 → Instances**
2. Select your instance
3. **Actions → Image and templates → Create image**
4. Name: `profile-api-backup-YYYYMMDD`
5. Click **Create image**

**Recovery:** Launch new instance from this AMI

### Backup Application Data

```bash
# Create comprehensive backup script
sudo nano /opt/scripts/full-backup.sh
```

```bash
#!/bin/bash

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# Backup application JAR
cp /opt/profile-api/profile-api-1.0.0.jar $BACKUP_DIR/app_$DATE.jar

# Backup NGINX config
cp /etc/nginx/sites-available/profile-api $BACKUP_DIR/nginx_$DATE.conf

# Backup systemd service
cp /etc/systemd/system/profile-api.service $BACKUP_DIR/systemd_$DATE.service

# Backup application.properties (if exists)
if [ -f /opt/profile-api/application.properties ]; then
    cp /opt/profile-api/application.properties $BACKUP_DIR/props_$DATE.properties
fi

# Create tarball
tar -czf $BACKUP_DIR/full_backup_$DATE.tar.gz $BACKUP_DIR/*_$DATE.*

# Remove individual files
rm $BACKUP_DIR/*_$DATE.{jar,conf,service,properties}

# Keep only last 7 backups
ls -t $BACKUP_DIR/full_backup_*.tar.gz | tail -n +8 | xargs rm -f

echo "Backup completed: full_backup_$DATE.tar.gz"
```

```bash
sudo chmod +x /opt/scripts/full-backup.sh

# Test it
sudo /opt/scripts/full-backup.sh
```

---

## Monitoring and Alerting

### CloudWatch Integration

1. **Install CloudWatch agent:**
   ```bash
   wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
   sudo dpkg -i amazon-cloudwatch-agent.deb
   ```

2. **Configure metrics:**
   ```bash
   sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-config-wizard
   ```

### Simple Health Check Script

```bash
sudo nano /opt/scripts/health-check.sh
```

```bash
#!/bin/bash

# Check if application responds
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/me)

if [ $HTTP_CODE -eq 200 ]; then
    echo "$(date): Application is healthy (HTTP $HTTP_CODE)"
else
    echo "$(date): Application is down! (HTTP $HTTP_CODE)"
    # Restart application
    sudo systemctl restart profile-api
    echo "$(date): Application restarted"
fi
```

```bash
sudo chmod +x /opt/scripts/health-check.sh

# Run every 5 minutes
sudo crontab -e
# Add:
*/5 * * * * /opt/scripts/health-check.sh >> /var/log/health-check.log 2>&1
```

---

## Additional Resources

- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [NGINX Documentation](https://nginx.org/en/docs/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Ubuntu Server Guide](https://ubuntu.com/server/docs)

---

## Conclusion

You now have a fully functional, production-ready deployment of your Spring Boot API on AWS EC2 with NGINX. This setup demonstrates:

✅ Cloud infrastructure management  
✅ Linux system administration  
✅ Web server configuration  
✅ Process management  
✅ Security best practices  
✅ Monitoring and maintenance  

**Next Steps:**
- Add a custom domain
- Set up SSL/TLS
- Implement monitoring dashboards
- Configure automated backups
- Set up CI/CD pipeline

For questions or issues, please open an issue on the [GitHub repository](https://github.com/yourusername/profile-api/issues).