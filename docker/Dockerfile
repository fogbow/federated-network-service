FROM openjdk:8

# Install.
RUN \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update -y && \
  apt-get upgrade -y && \
  apt-get install -y build-essential && \
  apt-get install -y software-properties-common && \
  apt-get install -y byobu curl git htop man unzip vim wget maven && \
  apt-get install -y net-tools iputils-ping && \
  rm -rf /var/lib/apt/lists/*

# Set environment variables.
ENV HOME /root

# Define working directory.
WORKDIR /root

# Installing Manager Core
RUN \
  git clone https://github.com/fogbow/resource-allocation-service.git && \
  (cd resource-allocation-service && git checkout develop && mvn install -Dmaven.test.skip=true)

# Installing Federated Network Service
RUN \
  git clone https://github.com/fogbow/federated-network-service.git && \
  (cd federated-network-service && git checkout develop)

# Define working directory.
WORKDIR /root/federated-network-service

RUN \
  mvn dependency:sources
  
# Generates the build number based on the commit checksum
RUN \
    (build_number=$(git rev-parse --short 'HEAD') && echo "build_number=$build_number" > build)

