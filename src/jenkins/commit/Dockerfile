FROM eclipse-temurin:25-jdk

RUN apt-get update

# Install Maven
RUN apt-get install -y maven

# Install Rust
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

# Configure audio
RUN apt-get install -y \
    build-essential \
    curl \
    ca-certificates \
    pkg-config \
    libasound2-dev
COPY src/jenkins/commit/asound.conf /etc/asound.conf
