FROM openjdk:8

ARG BRANCH=master

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
  (cd resource-allocation-service && git checkout $BRANCH && mvn install -Dmaven.test.skip=true)

# Installing Federated Network Service
RUN \
  git clone https://github.com/fogbow/federated-network-service.git && \
  (cd federated-network-service && git checkout $BRANCH)

# Define working directory.
WORKDIR /root/federated-network-service

# Generates the build number based on the commit checksum
RUN \
    (fns_build_number=$(git rev-parse --short 'HEAD') && \
    cd ../resource-allocation-service && ras_build_number=$(git rev-parse --short 'HEAD') && \
    cd /root/federated-network-service && echo "build_number=fns-$fns_build_number-ras-$ras_build_number-(buzios)" > build)

RUN \
  mvn dependency:sources

