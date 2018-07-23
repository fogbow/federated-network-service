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
  git clone https://github.com/fogbow/fogbow-manager-core.git && \
  (cd fogbow-manager-core && git checkout integrate-code && mvn install -Dmaven.test.skip=true)

# Installing Federated Network Service
RUN \
  git clone https://github.com/fogbow/federated-network-service.git && \
  (cd federated-network-service && mvn install -Dmaven.test.skip=true)

# Define working directory.
WORKDIR /root/federated-network-service

CMD java -Dlog4j.configuration=file:log4j.properties -jar target/federated-network-service-0.0.1-SNAPSHOT.jar & > log.out 2> log.err && tail -f /dev/null