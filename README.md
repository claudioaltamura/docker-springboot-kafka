[![Build Status](https://travis-ci.org/claudioaltamura/docker-springboot-kafka.svg?branch=master)](https://travis-ci.org/claudioaltamura/docker-springboot-kafka)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# docker-springboot-kafka
spring boot example with docker and kafka

## Building the example

	./gradlew clean build

or without tests

	./gradle clean bootJar

## Running the example java -jar 

Start current kafka docker container

	docker-compose up -d kafka

Get the current IP address from kafka

	docker inspect docker-springboot-kafka_kafka_1 | grep IP
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "",
            "IPPrefixLen": 0,
            "IPv6Gateway": "",
                    "IPAMConfig": null,
                    "IPAddress": "192.168.112.3",
                    "IPPrefixLen": 20,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,

Change bootstrap-servers in application.properties

	spring.kafka.bootstrap-servers=192.168.144.3:9092

Build the example

	./gradle clean bootJar

Run

	java -jar build/libs/docker-springboot-kafka-0.1.0.jar

Test

	curl http://127.0.0.1:8080/helloworld
	

Enter Kafka container	

	docker exec -it docker-springboot-kafka_kafka_1 bash
 
Consume message with kafka-consule-consumer.sh
 
    bash-4.4# /opt/kafka/bin/kafka-console-consumer.sh     --bootstrap-server localhost:9092     --topic helloworld  --from-beginning
                    {"timestamp":0,"message":"Hello World!"}

Some other basic admin commands

    bash-4.4# $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper $KAFKA_ZOOKEEPER_CONNECT
                    __consumer_offsets
                    helloworld

    bash-4.4# $KAFKA_HOME/bin/kafka-topics.sh --describe --zookeeper $KAFKA_ZOOKEEPER_CONNECT --topic helloworld
                    Topic:helloworld        PartitionCount:1        ReplicationFactor:1     Configs:
                            Topic: helloworld       Partition: 0    Leader: 1001    Replicas: 1001  Isr: 1001
