# ===========================================================
# Stage 1: Build application using Maven & Java 17
# ===========================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven descriptor
COPY pom.xml ./pom.xml

# Pre-fetch dependencies to take advantage of Docker layer caching
RUN mvn dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Build production WAR bundle
RUN mvn clean package -DskipTests

# ===========================================================
# Stage 2: Runtime server using Apache Tomcat 10.1 (Servlet 6.0)
# ===========================================================
FROM tomcat:10.1-jdk17-temurin

# Clean up default sample applications in Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy application as ROOT.war (URL: /) and QuanLySieuThi.war (URL: /QuanLySieuThi)
COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/QuanLySieuThi.war

# Default HTTP Port (Render will provide $PORT)
ENV PORT=8080
EXPOSE 8080

# Dynamically bind Tomcat port to Render's $PORT at runtime
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/g\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]
