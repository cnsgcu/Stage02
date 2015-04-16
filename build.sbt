name := "PlayJournal"

version := "1.0"

lazy val `playjournal` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc , anorm , cache , ws,
  "com.google.code.gson" % "gson" % "2.3.1",

  "org.apache.spark" % "spark-core_2.11" % "1.3.0",
  "org.apache.spark" % "spark-mllib_2.11" % "1.3.0",
  "org.apache.spark" % "spark-streaming_2.11" % "1.3.0",
  "org.apache.spark" % "spark-streaming-twitter_2.11" % "1.3.0",

  "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly(),

  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1" classifier "models"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  