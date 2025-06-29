# -------- Build Stage --------
FROM workbridge-base:1.0 as builder

WORKDIR /app

# Copy the JDK installer (if needed for this stage, optional)
COPY .jdk/jdk-23_linux-x64_bin.deb /tmp/jdk-23.deb

# Set up JDK from local .deb (ensures consistency)
RUN dpkg -i /tmp/jdk-23.deb \
    && JDK_DIR=$(find /usr/lib/jvm -type d -name "jdk-23*" | head -n 1) \
    && mv "$JDK_DIR" /usr/lib/jvm/jdk-23 \
    && rm /tmp/jdk-23.deb

ENV JAVA_HOME=/usr/lib/jvm/jdk-23
ENV PATH="$JAVA_HOME/bin:$PATH"

# Optimize Maven dependency caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy the source and build the app
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# -------- Runtime Stage --------
FROM debian:bullseye-slim

WORKDIR /app

# Install CA certs only (no Maven in runtime)
RUN apt-get update && apt-get install -y \
    ca-certificates \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy the JDK .deb file and install it
COPY .jdk/jdk-23_linux-x64_bin.deb /tmp/jdk-23.deb
RUN dpkg -i /tmp/jdk-23.deb \
    && JDK_DIR=$(find /usr/lib/jvm -type d -name "jdk-23*" | head -n 1) \
    && mv "$JDK_DIR" /usr/lib/jvm/jdk-23 \
    && rm /tmp/jdk-23.deb

ENV JAVA_HOME=/usr/lib/jvm/jdk-23
ENV PATH="$JAVA_HOME/bin:$PATH"

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar ./app.jar

# Expose application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
