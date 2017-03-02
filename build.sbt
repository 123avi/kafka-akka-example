name := """kafka-akka-example"""

version := "1.0"

scalaVersion := "2.11.6"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11"
  ,"com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
  ,"org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ,"net.cakesolutions" %% "scala-kafka-client" % "0.10.1.2"
  ,"net.cakesolutions" %% "scala-kafka-client-akka" % "0.10.1.2"
  ,"net.cakesolutions" %% "scala-kafka-client-testkit" % "0.10.1.2"
  ,"org.slf4j" % "log4j-over-slf4j" % "1.7.21" % "test"
  ,"ch.qos.logback"           % "logback-classic" % "1.1.9" % "test"
,  "com.typesafe.play" % "play-json_2.11" % "2.5.12"
)


fork in run := true