**Akka Scala-Kafka-Client example**

The project shows simple example of using akka actors and scala-kafka-client, communicating through Apache Kafka.

For further reading checkout this step by step guide: https://medium.com/@123avi/communicating-with-kafka-using-akka-actors-ce4af02482c6

**Setting up the development environment:**

In order to run the application you need to have a running kafka instance on your machine.

Download kafka from here: https://kafka.apache.org/downloads

cd ~/_your_download_directory_

**Start zookeeper**

bin/zookeeper-server-start.sh config/zookeeper.properties

**Start Server**

> bin/kafka-server-start.sh config/server.properties

**Create topic**

> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic ping

> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic pong


For detailed explanation checkout this post: https://medium.com/@123avi/communicating-with-kafka-using-akka-actors-ce4af02482c6