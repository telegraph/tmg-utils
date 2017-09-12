import sbt._
import Keys._

object Dependencies {

  val AkkaVersion     = "2.5.3"
  val AkkaHttpVersion = "10.0.9"
  val Json4sVersion   = "3.5.2"
  val PlayVersion     = "2.6.0"
  val LogBackVersion  = "1.2.3"
  val JacksonVersion  = "2.7.8"
  val GuiceVersion    = "4.1.0"

  val Common: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "com.typesafe"      %  "config"                      % "1.3.1",
      "com.typesafe.akka" %% "akka-actor"                  % AkkaVersion,
      "com.typesafe.akka" %% "akka-testkit"                % AkkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                   % "3.0.3"         % Test,
      "org.mockito"       %  "mockito-core"                % "2.7.9"         % Test,
      "org.scalamock"     %% "scalamock-scalatest-support" % "3.6.0"
    ),
    dependencyOverrides ++= Set(
      "com.typesafe"      %  "config"                      % "1.3.1"
    )
  )

  lazy val HttpClient: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      // Json4s
      "org.json4s"             %% "json4s-jackson"      % Json4sVersion,
      "org.json4s"             %% "json4s-core"         % Json4sVersion,
      "org.json4s"             %% "json4s-ext"          % Json4sVersion,

      "com.typesafe.akka"      %% "akka-http"           % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-testkit"   % AkkaHttpVersion % Test,
      "com.typesafe.akka"      %% "akka-stream-testkit" % AkkaVersion     % Test,
      "com.github.tomakehurst" %  "wiremock"            % "2.6.0"         % Test
    ),
    dependencyOverrides ++= Set(
      "com.typesafe.akka" %% "akka-stream"         % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor"          % AkkaVersion
    )
  ) ++ Common

  val GenericClient: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
    )
  ) ++ Common

  val AkkaServerExt: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      // Json4s
      "org.json4s"        %% "json4s-jackson"      % Json4sVersion,
      "org.json4s"        %% "json4s-core"         % Json4sVersion,
      "org.json4s"        %% "json4s-ext"          % Json4sVersion,

      "com.typesafe.akka" %% "akka-http"           % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion     % Test
    ),
    dependencyOverrides ++= Set(
      "com.typesafe.akka" %% "akka-stream"         % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor"          % AkkaVersion
    )
  ) ++ Common

  val PlayServerExt: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      // Others
      "com.google.inject.extensions" %  "guice-multibindings"       % GuiceVersion,

      // Json4s
      "org.json4s"                   %% "json4s-jackson"            % Json4sVersion,
      "org.json4s"                   %% "json4s-core"               % Json4sVersion,
      "org.json4s"                   %% "json4s-ext"                % Json4sVersion,

      // Logging
      "ch.qos.logback"               %  "logback-core"              % LogBackVersion,
      "ch.qos.logback"               %  "logback-classic"           % LogBackVersion,
      "net.logstash.logback"         %  "logstash-logback-encoder"  % "4.11",
      "io.swagger"                   %% "swagger-play2"             % "1.6.0",

      // Play Utilities
      "com.typesafe.play"            %% "filters-helpers"           % PlayVersion,
      "com.typesafe.play"            %% "play"                      % PlayVersion,
      "com.typesafe.play"            %% "play-test"                 % PlayVersion % Test,
      "org.scalatestplus.play"       %% "scalatestplus-play"        % "3.0.0"     % Test

  ),
    dependencyOverrides ++= Set(
      "com.typesafe.akka" %% "akka-actor"          % AkkaVersion,
      "com.typesafe.akka" %% "akka-http"           % AkkaHttpVersion
    )
  ) ++ Common

  lazy val BaseUtils:Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
    )
  ) ++ Common
}
