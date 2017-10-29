import sbt.AutoPlugin
import sbt._
import sbt.Keys.{publishTo, _}
import scoverage.ScoverageKeys._
import fm.sbt.S3Implicits._

object Common extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  lazy val buildNumber: Option[String] = sys.env.get("BUILD_NUMBER").map(bn => s"b$bn")

  override lazy val projectSettings = Seq(
    organization      := "uk.co.telegraph",
    organizationName  := "Telegraph Media Group",

    version           := "1.0.1-" + buildNumber.getOrElse("SNAPSHOT"),
    scalaVersion      := "2.11.8",
    isSnapshot        := buildNumber.isEmpty,
    scalacOptions     ++= Seq(
      "-target:jvm-1.8",
      "-feature",
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Xfuture"
    ),
    javacOptions ++= Seq(
      "-Xlint:unchecked"
    ),
    parallelExecution in Test := false,
    coverageFailOnMinimum     := false,
    coverageHighlighting      := true,
    autoAPIMappings           := true,

    publishMavenStyle         := true,
    publishTo                 := {
      if( isSnapshot.value ){
        Some("mvn-artifacts" atS3 "s3://mvn-artifacts/snapshot")
      }else{
        Some("mvn-artifacts" atS3 "s3://mvn-artifacts/release")
      }
    }
  )
}

