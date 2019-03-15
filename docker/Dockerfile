FROM openjdk:8

ARG COMMON_BRANCH
ARG AS_BRANCH
ARG RAS_BRANCH
ARG FNS_BRANCH

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

# Installing Common
RUN \
  git clone https://github.com/fogbow/common.git && \
 (cd common && git checkout $COMMON_BRANCH && mvn install -Dmaven.test.skip=true)

# Installing Authentication Service
RUN \
  git clone https://github.com/fogbow/authentication-service.git && \
 (cd authentication-service && git checkout $AS_BRANCH && mvn install -Dmaven.test.skip=true)

# Installing Resource Allocation Service
RUN \
  git clone https://github.com/fogbow/resource-allocation-service.git && \
  (cd resource-allocation-service && git checkout $RAS_BRANCH && mvn install -Dmaven.test.skip=true)

# Installing Federated Network Service
RUN \
  git clone https://github.com/fogbow/federated-network-service.git && \
  (cd federated-network-service && git checkout $FNS_BRANCH)

# Define working directory.
WORKDIR /root/federated-network-service

# Generates the build number based on the commit checksum
RUN \
    (fns_build_number=$(git rev-parse --short 'HEAD') && \
    cd ../common && common_build_number=$(git rev-parse --short 'HEAD') && \
    cd ../authentication-service && as_build_number=$(git rev-parse --short 'HEAD') && \
    cd ../resource-allocation-service && ras_build_number=$(git rev-parse --short 'HEAD') && \
    cd /root/federated-network-service && \
    echo "build_number=fns-$fns_build_number-ras-$ras_build_number-as-$as_build_number-common-$common_build_number" > build)

RUN \
  mvn dependency:sources

