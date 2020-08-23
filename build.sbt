
import com.lightbend.lagom.core.LagomVersion
import com.lightbend.lagom.sbt.LagomImport.lagomScaladslPersistenceCassandra

name := "Collector"

version := "0.1"

scalaVersion := "2.13.1"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val jsoup = "org.jsoup" % "jsoup" % "1.8.2"
val yahooFinance = "com.yahoofinance-api" % "YahooFinanceAPI" % "3.15.0"
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.3"
val lagomScaladslAkkaDiscovery = "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % LagomVersion.current
val cats = "org.typelevel" %% "cats-core" % "2.1.1"



ThisBuild / scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked", "-Xfatal-warnings", "-language:higherKinds", "-Ypartial-unification")

def dockerSettings = Seq(
  dockerBaseImage := "adoptopenjdk/openjdk8"
)


lazy val collectorApi = (project in file("collector-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )



lazy val collectorImpl = (project in file("collector-impl"))
  .enablePlugins(LagomScala)
  .settings(
    name := "collector",
    libraryDependencies ++= Seq(
      macwire,
      yahooFinance,
      jsoup,
      cats,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi,
      lagomScaladslPersistenceCassandra
    )
  ).settings(dockerSettings)
  .dependsOn(collectorApi)


lagomCassandraEnabled in ThisBuild := false
