# Jenkins Setup Guide - YAS Monorepo Pipeline

## 📋 Overview

Pipeline này tự động:
- ✅ Detect service changes trong monorepo (chỉ build service thay đổi)
- ✅ Quét secrets với Gitleaks
- ✅ Chạy Unit + Integration tests
- ✅ Validate code coverage (>70% required)
- ✅ Scan security & quality với SonarCloud
- ✅ Build & push Docker images

---

## 🔧 Prerequisites

### 1. Jenkins Server Setup
```bash
# Jenkins docker-compose (đã có)
docker-compose -f docker-compose.jenkins.yml up -d

# Get admin password
docker-compose -f docker-compose.jenkins.yml exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Access Jenkins
http://localhost:8080
```

### 2. Required Jenkins Plugins
Install via **Manage Jenkins → Plugins → Available**:

**Bắt buộc:**
- Pipeline
- Pipeline: Stage View  
- Git
- GitHub (nếu dùng GitHub)
- Credentials Binding
- JUnit plugin
- HTML Publisher plugin

**Optional:**
- Blue Ocean (modern UI)
- Email Extension
- Slack Notification

---

## 🔐 Setup Jenkins Credentials

### 1. SonarCloud Token
1. Tạo token tại: https://sonarcloud.io/account/security/
2. Copy token
3. Jenkins: **Manage Jenkins → Credentials → System → Global credentials**
4. **New credentials**:
   - Kind: `Secret text`
   - Secret: `<paste token từ SonarCloud>`
   - ID: `sonarcloud-token`
   - Description: `SonarCloud Token`
   - Click **Create**

### 2. Docker Registry Credentials
1. Jenkins: **Manage Jenkins → Credentials → System → Global credentials**
2. **New credentials**:
   - Kind: `Username with password`
   - Username: `<docker_username>`
   - Password: `<docker_password>`
   - ID: `docker-hub-credentials`
   - Description: `Docker Hub Credentials`
   - Click **Create**

**Nếu dùng private registry:**
```
- ID: docker-hub-credentials (giữ ID này)
- Update REGISTRY_URL trong Jenkinsfile nếu cần
- Update REGISTRY_URL ở line 19 của Jenkinsfile
```

---

## 📂 Create Jenkins Pipeline Job

### Option 1: Declarative Pipeline (Recommended)

1. **Jenkins Dashboard** → **New Item**
2. Enter name: `yas-monorepo-pipeline`
3. Select: **Pipeline**
4. Click **OK**
5. **Configuration**:
   - Scroll to **Pipeline** section
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/nashtech-garage/yas.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
6. **Save**

### Option 2: Webhook Trigger (GitHub)

1. **GitHub Settings → Webhooks → Add webhook**
   - Payload URL: `http://your-jenkins-domain/github-webhook/`
   - Content type: `application/json`
   - Events: 
     - ✅ Push events
     - ✅ Pull requests
   - Click **Add webhook**

2. **Jenkins Job Config**:
   - **Build Triggers**: Check ✅ **GitHub hook trigger for GITScm polling**
   - **Save**

---

## 🚀 Running the Pipeline

### Option 1: Manual Trigger
1. Jenkins Dashboard → `yas-monorepo-pipeline`
2. Click **Build with Parameters**
3. Select:
   - **SERVICE**: 
     - `auto` = auto-detect changed service
     - `product`, `order`, etc. = specific service
   - **SKIP_TESTS**: false (run tests)
   - **SKIP_SONAR**: false (run SonarCloud)
4. Click **Build**

### Option 2: Git Push Trigger
```bash
# Push to main branch
git push origin main

# Webhook automatically triggers the pipeline
```

### Option 3: Specific Service Build
```bash
git commit -m "Fix: media service image upload"
git push origin main

# Pipeline auto-detects "media" service and only builds/tests it
```

---

## 📊 Pipeline Stages Explanation

| Stage | Purpose | Condition |
|-------|---------|-----------|
| **Checkout** | Git clone/pull code | Always |
| **Detect Changed Service** | Identify which service changed | Auto or manual selection |
| **Initialize** | Display build info | Always |
| **Gitleaks** | Scan for secrets/credentials | Always |
| **Build** | `mvn clean install` | Always |
| **Unit Tests** | `mvn test` (Surefire) | When SKIP_TESTS=false |
| **Integration Tests** | `mvn verify` (Failsafe) | When SKIP_TESTS=false |
| **Coverage Validation** | Fail if coverage < 70% | When SKIP_TESTS=false |
| **SonarCloud** | Security + quality scan | When SKIP_SONAR=false |
| **Publish Reports** | JUnit + JaCoCo HTML | When tests run |
| **Docker Build** | Build image | On main/master/develop + has Dockerfile |
| **Docker Push** | Push to registry | On main/master/develop |

---

## 🔍 View Results

After pipeline runs:

1. **Test Results**: 
   - Job page → **Test Result**
   - View failed/passed tests

2. **Coverage Report**:
   - Job page → **JaCoCo Code Coverage Report**
   - Drill down by package/class

3. **Build Logs**:
   - Job page → **Console Output**
   - Full build details

4. **SonarCloud**:
   - https://sonarcloud.io/organizations/nashtech-garage
   - Link from Jenkins console

5. **Gitleaks Report**:
   - Job page → **Artifacts** → `gitleaks-report.json`

---

## 🛠️ Troubleshooting

### Issue 1: Pipeline fails on "Detect Changed Service"
**Solution**:
```bash
# Ensure remote tracking branch exists
git fetch origin main
git merge-base origin/main HEAD  # Test if it works
```

### Issue 2: Code coverage < 70%
**Solution**:
```bash
# Check JaCoCo report:
service-name/target/jacoco-report/index.html

# Add more tests to increase coverage
# Or update jacoco-maven-plugin rules in pom.xml
```

### Issue 3: SonarCloud authentication failed
**Solution**:
1. Verify `sonarcloud-token` credential exists
2. Check token hasn't expired at https://sonarcloud.io/account/security/
3. Verify `sonar.projectKey` in service's pom.xml

### Issue 4: Docker push fails
**Solution**:
```bash
# Verify Docker credentials
echo "password" | docker login -u "username" --password-stdin docker.io

# Check registry URL matches in Jenkinsfile line 19
```

---

## 📝 Example: Build Service Flow

```
Developer commits to media service:
  ↓
git push origin main
  ↓
GitHub webhook triggers Jenkins
  ↓
Pipeline starts:
  - Checkout code
  - Auto-detect: "media" service changed
  - Gitleaks: scan "media/" folder
  - Build: mvn clean install -f media
  - Unit Tests: mvn test -f media
  - Coverage: Check if >= 70%
  - SonarCloud: mvn sonar:sonar for media
  - Docker Build: docker build ./media (if Dockerfile exists)
  - Docker Push: push to docker.io
  ↓
Pipeline SUCCESS
  Reports available at {{ BUILD_URL }}
```

---

## 🔄 Monorepo Service Detection Algorithm

```groovy
// How the pipeline detects which service changed:

1. Get changed files: git diff origin/main...HEAD
2. Extract first path segment: CHANGED_FILE | cut -d'/' -f1
3. Validate service exists: -f "$SERVICE/pom.xml"
4. If valid → build that service
5. If not valid → build root (all services)

Example:
  Changes in: auth/src/main/java/...
  Auto-detect service: "auth"
  
  Changes in: docs/README.md
  Auto-detect service: "root" (builds all)
```

---

## 📚 Additional Resources

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [SonarCloud Integration](https://sonarcloud.io/project/configuration)
- [JaCoCo Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html)
- [Gitleaks GitHub](https://github.com/gitleaks/gitleaks)
- [Docker Registry Help](https://docs.docker.com/registry/)

---

## ✅ Validation Checklist

- [ ] Jenkins server running (port 8080)
- [ ] `sonarcloud-token` credential created
- [ ] `docker-hub-credentials` credential created
- [ ] GitHub webhook configured (if using GitHub)
- [ ] Jenkinsfile placed in repo root
- [ ] Service has `pom.xml` in folder root
- [ ] Service has JaCoCo plugin configured (for coverage)
- [ ] SonarQube keys exist in service pom.xml files
