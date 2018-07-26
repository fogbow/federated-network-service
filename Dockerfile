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
  (cd resource-allocation-service && git checkout integrate-code && mvn install -Dmaven.test.skip=true)

# Installing Federated Network Service
RUN \
  git clone https://github.com/fogbow/federated-network-service.git && \
  (cd federated-network-service && mvn install -Dmaven.test.skip=true)

# Define working directory.
WORKDIR /root/federated-network-service

CMD bash bin/start-service > log.out 2> log.err && tail -f /dev/null
