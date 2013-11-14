import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "tweet-play2"
  val appVersion      = "1.8.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    // "com.googlecode.json-simple"% "json-simple"% "1.1",
    "org.apache.poi" % "poi" % "3.9",
    "org.twitter4j" % "twitter4j-core" % "[3.0,)",
    "commons-configuration" % "commons-configuration" % "1.9",
    "com.google.code.geocoder-java" % "geocoder-java" % "0.15",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "org.twitter4j" % "twitter4j-core" % "[3.0,)",
    "org.twitter4j" % "twitter4j-stream" % "[3.0,)",
    "org.twitter4j" % "twitter4j-async" % "[3.0,)",
    "org.elasticsearch" % "elasticsearch" % "0.90.5"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
