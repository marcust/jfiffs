FROM resin/rpi-raspbian:latest
ENTRYPOINT []

RUN sudo apt-get update && \
    sudo apt-get -qy install wget && \
    gpg --keyserver pgpkeys.mit.edu --recv-key  8B48AD6246925553 && \
    gpg -a --export 8B48AD6246925553 | sudo apt-key add - && \
    gpg --keyserver pgpkeys.mit.edu --recv-key  7638D0442B90D010 && \
    gpg -a --export 7638D0442B90D010 | sudo apt-key add - && \
    wget -O - https://debian.neo4j.org/neotechnology.gpg.key | sudo apt-key add - && \
    echo 'deb http://debian.neo4j.org/repo stable/' | tee -a /etc/apt/sources.list.d/neo4j.list && \
    echo "deb http://httpredir.debian.org/debian jessie-backports main" | sudo tee -a /etc/apt/sources.list.d    /jessie-backports.list && \
    sudo apt-get update && \
    sudo apt-get -t jessie-backports install ca-certificates-java && \
    sudo apt-get update && \
    sudo apt-get install neo4j=3.2.8 && \
    update-java-alternatives --jre-headless --set java-1.8.0-openjdk-armhf

CMD ["service", "neo4j", "start"]
