#!/bin/bash

# Jenkins YAS Pipeline Setup Script
# Usage: ./jenkins-setup.sh

set -e

echo "=================================="
echo "YAS Jenkins Pipeline Setup Script"
echo "=================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}[1/5] Checking Prerequisites...${NC}"
command -v docker &> /dev/null || { echo -e "${RED}Docker not found${NC}"; exit 1; }
command -v docker-compose &> /dev/null || { echo -e "${RED}Docker Compose not found${NC}"; exit 1; }
command -v git &> /dev/null || { echo -e "${RED}Git not found${NC}"; exit 1; }
echo -e "${GREEN}✓ All prerequisites installed${NC}"
echo ""

# Stop existing Jenkins
echo -e "${YELLOW}[2/5] Starting Jenkins Server...${NC}"
docker-compose -f docker-compose.jenkins.yml down 2>/dev/null || true
sleep 2
docker-compose -f docker-compose.jenkins.yml up -d

# Wait for Jenkins to be ready
echo -e "${YELLOW}Waiting for Jenkins to start (this may take 1-2 minutes)...${NC}"
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Jenkins is ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}✗ Jenkins failed to start${NC}"
    exit 1
fi
echo ""

# Get initial admin password
echo -e "${YELLOW}[3/5] Getting Jenkins Admin Password...${NC}"
JENKINS_PASSWORD=$(docker-compose -f docker-compose.jenkins.yml exec -T jenkins cat /var/jenkins_home/secrets/initialAdminPassword)
echo -e "${GREEN}✓ Jenkins Admin Password:${NC}"
echo -e "${YELLOW}${JENKINS_PASSWORD}${NC}"
echo ""

# Display setup instructions
echo -e "${YELLOW}[4/5] Next Steps:${NC}"
echo ""
echo "1. Access Jenkins:"
echo -e "   ${YELLOW}http://localhost:8080${NC}"
echo ""
echo "2. Login with:"
echo "   Username: admin"
echo -e "   Password: ${YELLOW}${JENKINS_PASSWORD}${NC}"
echo ""
echo "3. Install recommended plugins:"
echo "   - Go to: Manage Jenkins > Plugins > Available"
echo "   - Search and install:"
echo "     • Pipeline"
echo "     • Pipeline: Stage View"
echo "     • Git"
echo "     • GitHub (if using GitHub)"
echo "     • HTML Publisher"
echo "     • JUnit Plugin"
echo ""
echo "4. Setup Credentials (Manage Jenkins > Credentials):"
echo "   - SonarCloud Token:"
echo "     • Kind: Secret text"
echo "     • ID: sonarcloud-token"
echo "     • Secret: <paste from https://sonarcloud.io/account/security/>"
echo ""
echo "   - Docker Registry:"
echo "     • Kind: Username with password"
echo "     • ID: docker-hub-credentials"
echo "     • Username: <your-docker-username>"
echo "     • Password: <your-docker-password>"
echo ""

# Test pipeline syntax
echo -e "${YELLOW}[5/5] Validating Jenkinsfile Syntax...${NC}"

if command -v groovy &> /dev/null; then
    # Validate with Groovy if available
    if groovy -c Jenkinsfile 2>/dev/null; then
        echo -e "${GREEN}✓ Jenkinsfile syntax is valid${NC}"
    else
        echo -e "${YELLOW}⚠ Could not validate Jenkinsfile syntax${NC}"
    fi
else
    echo -e "${YELLOW}⚠ Groovy not installed, skipping syntax check${NC}"
    echo "  (This is OK - Jenkins will validate when you create the job)"
fi

echo ""
echo -e "${GREEN}=================================="
echo "Setup Complete!"
echo "==================================${NC}"
echo ""
echo "Jenkins is running at: http://localhost:8080"
echo ""
echo "To create your pipeline job:"
echo "1. New Item > Enter name 'yas-monorepo-pipeline' > Pipeline > OK"
echo "2. Pipeline section: Pipeline script from SCM"
echo "3. SCM: Git"
echo "4. Repository URL: https://github.com/nashtech-garage/yas.git"
echo "5. Branch: */main"
echo "6. Script Path: Jenkinsfile"
echo "7. Save"
echo ""
echo "Documentation: See JENKINS_SETUP.md"
echo ""
