import Dependencies._
import sbt.Keys._

lazy val tmgUtils = (project in file("."))
    .aggregate(baseUtils, genericClient, httpClient, playServerExt, akkaServerExt, httpClientTestKit)

lazy val genericClient  = (project in file("generic-client"))
  .settings(
    coverageHighlighting := false,
    name := "generic-client",
    GenericClient
  )
  .dependsOn(baseUtils)

lazy val httpClient = (project in file("http-client"))
  .settings(
    name := "http-client",
    HttpClient
  )
  .dependsOn(genericClient)

lazy val httpClientTestKit = (project in file("http-client-testkit"))
  .settings(
    name := "http-client-testkit",
    HttpClientTestKit
  )
  .dependsOn(httpClient)

lazy val akkaServerExt = (project in file("akka-server-ext"))
  .settings(
    name := "akka-server-ext",
    AkkaServerExt
  )
  .dependsOn(genericClient)

lazy val playServerExt = (project in file("play-server-ext"))
  .settings(
    name := "play-server-ext",
    PlayServerExt
  )
  .dependsOn(genericClient)

lazy val baseUtils = (project in file("base-utils"))
  .settings(
    name := "base-utils",
    BaseUtils
  )
