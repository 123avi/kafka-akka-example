name := """kafka-akka-example"""

version := "1.0"

scalaVersion := "2.12.2"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.1"
  ,"com.typesafe.akka" %% "akka-testkit" % "2.5.1" % "test"
  ,"org.scalatest" %% "scalatest" % "3.0.3" % "test"
  ,"net.cakesolutions" %% "scala-kafka-client" % "0.10.2.1"
  ,"net.cakesolutions" %% "scala-kafka-client-akka" % "0.10.2.1"
  ,"net.cakesolutions" %% "scala-kafka-client-testkit" % "0.10.2.1" % "test"
  ,"org.slf4j" % "log4j-over-slf4j" % "1.7.21" % "test"
,  "com.typesafe.play" %% "play-json" % "2.6.0-M7"
)


fork in run := true
