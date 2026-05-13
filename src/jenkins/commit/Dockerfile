FROM eclipse-temurin:25-jdk

RUN apt-get update

# Install Tools
RUN apt-get install -y \
    curl \
    git

# Install Maven
RUN apt-get install -y maven

# Configure audio
RUN apt-get install -y \
    build-essential \
    ca-certificates \
    pkg-config \
    libasound2-dev

# Install Rust
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

COPY src/jenkins/commit/asound.conf /etc/asound.conf
