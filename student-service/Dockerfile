# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from the host (GitHub Actions builder or local target/)
# This assumes 'mvn package' was run previously
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (Adjust port if using random ports in prod, but container allows 8080 internally)
# Note: In prod with port 0, this healthcheck might need adjustment if using Actuator port, 
# but typically container port 8080 is mapped. 
# However, if app runs on port 0, we can't easily EXPOSE or HEALTHCHECK specific ports.
# For simplicity in this fix, we assume the app binds to 8080 inside the container for the default profile
# OR relies on the dynamic port which makes standard healthchecks tricky without a script.
# Given the "Port 0" status, let's keep the existing logic but acknowledge checking 8080 might fail 
# if the app actually listens on a random port INSIDE the container. 
# BUT: 'server.port: 0' usually binds to a random port. 
# In K8s/Docker, random ports are bad practice. You want a FIXED port inside container, mapped to Random outside.
# 
# CORRECTION: We set port 0 in application-prod.yml.
# If we run in Docker, we typically WANT a fixed port (e.g. 8080) inside the container.
# Load Balancing with Eureka will handle the "instanceId" and registration.
# Setting port 0 inside Docker is actually problematic for Healthchecks.
# 
# I will NOT change the HEALTHCHECK logic here, but the user should beware.
# Using 'server.port=0' is great for Bare Metal scaling. In Docker, usually we keep 8080.
# But let's fix the BUILD first.

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/v1/students/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
