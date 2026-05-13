FROM eclipse-temurin:25-jdk

# Install Rust
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

# Configure audio
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    ca-certificates \
    pkg-config \
    libasound2-dev \
 && rm -rf /var/lib/apt/lists/*
COPY src/jenkins/commit/asound.conf /etc/asound.conf
