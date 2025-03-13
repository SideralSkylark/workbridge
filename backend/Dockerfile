# *****Deployment env *****
# FROM debian:bullseye-slim

# # Install essential tools and certificates
# RUN apt-get update && apt-get install -y \
#     curl \
#     maven \
#     gnupg2 \
#     ca-certificates \
#     && apt-get clean \
#     && rm -rf /var/lib/apt/lists/*

# # Download and install Oracle JDK dynamically
# RUN curl -fsSL https://download.oracle.com/java/23/latest/jdk-23_linux-x64_bin.deb -o /tmp/jdk-23.deb \
#     && dpkg -i /tmp/jdk-23.deb \
#     && JDK_DIR=$(find /usr/lib/jvm -type d -name "jdk-23*" | head -n 1) \
#     && mv "$JDK_DIR" /usr/lib/jvm/jdk-23 \
#     && rm /tmp/jdk-23.deb

# # Set JAVA_HOME and update PATH
# ENV JAVA_HOME=/usr/lib/jvm/jdk-23
# ENV PATH="$JAVA_HOME/bin:$PATH"

# # Maven project setup
# WORKDIR /app

# # Copy pom.xml first to cache dependencies
# COPY pom.xml .
# RUN mvn dependency:go-offline -B

# # Copy source code and build the project
# COPY src ./src
# RUN mvn clean package -DskipTests

# # Expose the application's port
# EXPOSE 8080

# # Copy the final JAR file to the root of the container
# RUN cp target/*.jar app.jar

# # Set the default command
# CMD ["java", "-jar", "app.jar"]
# *****Deployment env *****

# *****Development env *****
# Use the locally built base image
FROM workbridge-base:1.0

# Set working directory
WORKDIR /app

# Copy local JDK package (since you already have it)
COPY .jdk/jdk-23_linux-x64_bin.deb /tmp/jdk-23.deb

# Install Oracle JDK from the local .deb file
RUN dpkg -i /tmp/jdk-23.deb \
    && JDK_DIR=$(find /usr/lib/jvm -type d -name "jdk-23*" | head -n 1) \
    && mv "$JDK_DIR" /usr/lib/jvm/jdk-23 \
    && rm /tmp/jdk-23.deb

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/usr/lib/jvm/jdk-23
ENV PATH="$JAVA_HOME/bin:$PATH"

# Copy pom.xml first to cache dependencies
COPY pom.xml . 

# Download dependencies **while online** so we can build offline later
RUN mvn dependency:go-offline -B

# Copy the source code and build the project
COPY src ./src
RUN mvn clean package -DskipTests

# Expose the application port
EXPOSE 8080

# Copy the final JAR file to the root of the container
RUN cp target/*.jar app.jar

# Set the default command
CMD ["java", "-jar", "app.jar"]

# *****Development env *****