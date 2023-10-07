# Use a base image that contains Java and X11 libraries
FROM debian:bullseye-slim

# Install required packages, including libXext6
RUN apt-get update && apt-get install -y openjdk-11-jdk libxext6

# Set the working directory
WORKDIR /home/sungp/Sung/Java_socket_project

# Copy the Java files into the container
COPY . /home/sungp/Sung/Java_socket_project

# Compile the Java application
RUN javac Server.java
RUN javac Client.java

# Run the application
CMD ["java", "Server"] && ["java", "Client"]