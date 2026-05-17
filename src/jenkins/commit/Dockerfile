FROM eclipse-temurin:25-jdk

RUN apt-get update

# Install Tools
RUN apt-get install -y \
    curl \
    git

# JavaFX native dependencies for headless GUI-Tests
RUN apt-get install -y \
    libpangoft2-1.0-0 \
    libpango-1.0-0 \
    libgtk-3-0 \
    libglib2.0-0 \
    xvfb \
    libgl1 \
    libglx-mesa0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    libxcursor1 \
    libxinerama1 \
    libxxf86vm1 \
    libfreetype6 \
    libfontconfig1

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
