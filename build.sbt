import sbt.Keys._
import SonatypeKeys._

// ··· Settings ···

sonatypeSettings

// ··· Project Info ···

name := "spray-extensions"

organization := "com.github.jarlakxen"

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalaVersion <<= (crossScalaVersions) { versions => versions.head }

fork in run   := true

publishMavenStyle := true

publishArtifact in Test := false

// ··· Project Enviroment ···

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil

unmanagedSourceDirectories in Test := (scalaSource in Test).value :: Nil

resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "resources"


// ··· Project Options ···

javacOptions ++= Seq(
    "-source", "1.7",
    "-target", "1.7"
)

scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-unchecked",
    "-deprecation"
)

// ··· Project Repositories ···


resolvers ++= Seq(
    "spray repo"                     at "http://repo.spray.io/",
    "OSS"                            at "http://oss.sonatype.org/content/repositories/releases/")

// ··· Project Dependancies···

libraryDependencies ++= Seq(
  // --- Akka ---
  "com.typesafe.akka"             %%  "akka-actor"            % "2.2.3"   %  "provided",
  // --- Spray ---
  "io.spray"                      %   "spray-routing"         % "1.2.1"   %  "provided",
  // --- JSON ---
  "io.spray"                      %%  "spray-json"            % "1.2.6"   %  "provided",
  "org.json4s"                    %%  "json4s-jackson"        % "3.2.10"  %  "provided",
  // --- View ---
  "org.fusesource.scalate"        %%  "scalate-core"          % "1.6.1"   %  "provided",
  // --- Utils ---
  "com.typesafe"                  %   "config"                % "1.2.1",
  // --- Testing ---
  "org.specs2"                    %%  "specs2-core"           % "2.3.12"  %  "test",
  "org.specs2"                    %%  "specs2-junit"          % "2.3.12"  %  "test",
  "io.spray"                      %   "spray-testkit"         % "1.2.1"   %  "test",
  "junit"                         %   "junit"                 % "4.11"    %  "test"
)

pomExtra := (
  <url>https://github.com/Jarlakxen/spray-extensions</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/Jarlakxen/spray-extensions</url>
    <connection>scm:git:git@github.com:Jarlakxen/spray-extensions.git</connection>
    <developerConnection>scm:git:git@github.com:Jarlakxen/spray-extensions.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>Jarlakxen</id>
      <name>Facundo Viale</name>
      <url>https://github.com/Jarlakxen/spray-extensions</url>
    </developer>
  </developers>
)