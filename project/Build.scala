import sbt._
import Keys._
import com.gu.SbtJasminePlugin._
import net.virtualvoid.sbt.graph.Plugin._
import com.typesafe.sbt.SbtScalariform._
import sbtjslint.Plugin._
import sbtjslint.Plugin.LintKeys._

object Resolvers {
  val bootzookaResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )
}

object BuildSettings {

  import Resolvers._

  val buildSettings = Defaults.defaultSettings ++ defaultScalariformSettings ++ Seq(

    organization := "org.a29",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.0",

    resolvers := bootzookaResolvers,
    scalacOptions += "-unchecked",
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging
  )

}

object Dependencies {

  val slf4jVersion = "1.7.2"
  val logBackVersion = "1.0.9"
  val scalatraVersion = "2.2.0"
  val scalaLoggingVersion = "1.0.1"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
//  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  val scalaLogging = "com.typesafe" %% "scalalogging-slf4j" % scalaLoggingVersion

  val logging = Seq(slf4jApi, logBackClassic, scalaLogging)

  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.1"

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.1.0"
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"
  val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "test"

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, json4s, scalatraAuth, commonsLang)

  val testingDependencies = Seq(mockito, scalatest)

  val seleniumVer = "2.29.0"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"

  val selenium = Seq(seleniumJava, seleniumFirefox, fest)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))
}

object BootzookaBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings

  lazy val parent: Project = Project(
    "stmate-root",
    file("."),
    settings = buildSettings
  ) aggregate(domain, rest, ui)

  lazy val domain: Project = Project(
    "stmate-domain",
    file("stmate-domain"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())
  )

  lazy val rest: Project = Project(
    "stmate-rest",
    file("stmate-rest"),
    settings = buildSettings ++ Seq(libraryDependencies ++= scalatraStack ++ Seq(servletApiProvided))
  ) dependsOn(domain)


  val lintCustomSettings = lintSettingsFor(Test) ++ inConfig(Test)(Seq(
    sourceDirectory in jslint <<= (baseDirectory)(_ / "src/main/webapp/scripts"),
    excludeFilter in jslint := "angular-*.js" || "bootstrap-*.js" || "jquery*.js",
    flags in jslint ++= Seq("undef", "vars", "browser", "plusplus"),
    compile in Test <<= (compile in Test) dependsOn (jslint)
  ))

  lazy val ui: Project = Project(
    "stmate-ui",
    file("stmate-ui"),
    settings = buildSettings ++ jasmineSettings ++ graphSettings ++ webSettings ++ lintCustomSettings ++ Seq(
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "stmate." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      libraryDependencies ++= Seq(jetty, servletApiProvided),
      appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "scripts" },
      appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "scripts" / "vendor" },
      jasmineTestDir <+= sourceDirectory { src => src / "test" / "unit" },
      jasmineConfFile <+= sourceDirectory { src => src / "test" / "unit" / "test.dependencies.js" },
      jasmineRequireJsFile <+= sourceDirectory { src => src / "test" / "lib" / "require" / "require-2.0.6.js" },
      jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "unit" / "require.conf.js" },
      (test in Test) <<= (test in Test) dependsOn (jasmine))
  ) dependsOn (rest)

  lazy val uiTests = Project(
    "stmate-ui-tests",
    file("stmate-ui-tests"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= selenium ++ Seq(jettyTest, servletApiProvided)
    )

  ) dependsOn (rest)

}
