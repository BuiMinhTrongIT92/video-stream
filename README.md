# video-stream
Tech: Java Spring Boot JDK17, AWS (EC2, S3, IAM), Docker
# AWS Video-stream Service Setup Guide

## AWS Setup
```
# AWS EC2 Instance and S3 Setup Guide

## 1. Set Up EC2 Instance with Linux
- Launch an EC2 instance using a Linux-based Amazon Machine Image (AMI).
- Configure the instance settings according to video tutorial.

## 2. Configure Security Groups
- Set up security group rules to allow specific inbound and outbound traffic.
- Open the necessary ports for your application (e.g., HTTP, HTTPS, custom ports).

## 3. Set Up IAM and Policy
- Create an IAM role and assign it to your EC2 instance.
- Configure the role with the necessary permissions, such as access to S3, using IAM policies.

## 4. Set Up S3
- Create an S3 bucket for storing video files or other data.
- Configure bucket permissions for access control, ensuring proper access for your EC2 instance or other services.

---
### Reference
- For detailed steps, refer to a tutorial video on setting up and configuring these AWS services.
```
## Begin Install Services On EC2
## Docker Installation
```bash
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \
"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
$(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo docker run hello-world
```

## Java Installation (OpenJDK 17)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
sudo update-alternatives --config java
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
nano ~/.bashrc
# Add the following line:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
source ~/.bashrc
# Verify
echo $JAVA_HOME
```

## Maven Installation
```bash
sudo apt update
sudo apt install maven
whereis mvn
# Add Maven to PATH
nano ~/.bashrc
export PATH=$PATH:/path/to/maven/bin
source ~/.bashrc
```

## FFmpeg Installation
```bash
Install On Docker Container Instance (ex.SpringBoot Service - (**NOT EC2**))
sudo apt update
sudo apt install ffmpeg
ffmpeg -version

which ffmpeg
export PATH=$PATH:/usr/bin
nano ~/.bashrc
# Add the following line:
export PATH=$PATH:/usr/bin
source ~/.bashrc
ffmpeg -version
```

## Git Installation and Configuration
```bash
sudo apt update
sudo apt install git
git --version
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --list
```

## Without CI/CD -> Using deploy.sh file
```
SSH to EC2 Instance
"cd" to project folder ex. "cd /home/video-stream"

**Exec this command**
sudo git pull
cd /home/video-stream
sudo bash ./deploy.sh

**If having problems with permissions of folder for file**
Using chmod +x for example
```
