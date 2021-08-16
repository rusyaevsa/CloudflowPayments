import sbt._
import sbt.Keys._

lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      name := "CloudflowPayments",
      skip in publish := true,
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      paymentsPipeline,
      datamodel,
      fileIngress,
    )

lazy val commonSettings = Seq(
  organization := "com.lightbend.cloudflow",
  headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2021", "Lightbend Inc. <https://www.lightbend.com>")),
  scalaVersion := "2.12.11",
  javacOptions += "-Xlint:deprecation",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ), scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  runLocalConfigFile := Some("src/main/resources/local.conf")
)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(
      name := moduleID
    )
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val paymentsPipeline = appModule("payments-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .dependsOn(datamodel)

lazy val httpIngress = appModule("http")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"         %% "akka-http-spray-json"   % "10.1.12",
      "ch.qos.logback"            %  "logback-classic"        % "1.2.3",
      "com.typesafe.akka"         %% "akka-http-testkit"         % "10.1.12" % "test",
      "org.scalatest"             %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)

lazy val loggerIngress = appModule("logger-streamlet")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings
  )
  .dependsOn(datamodel)

lazy val checkStreamlet = appModule("check-streamlet")
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings
  )
  .dependsOn(datamodel)

lazy val fileIngress =  appModule("file-ingress")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    name := "file-ingress",

    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-stream-alpakka-file"  % "1.1.2"
    )
  ).dependsOn(paymentsPipeline, datamodel)

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(CloudflowLibraryPlugin)