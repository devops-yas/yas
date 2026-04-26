# Sử dụng bản LTS JDK 21 theo yêu cầu đồ án
FROM jenkins/jenkins:lts-jdk21

USER root

# Cập nhật và cài đặt các công cụ cơ bản
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Tải và cài đặt Gitleaks (Yêu cầu 7c)
RUN wget https://github.com/gitleaks/gitleaks/releases/download/v8.15.0/gitleaks-linux-x64 -O /usr/local/bin/gitleaks \
    && chmod +x /usr/local/bin/gitleaks

# Trả lại quyền cho user jenkins để đảm bảo bảo mật
USER jenkins