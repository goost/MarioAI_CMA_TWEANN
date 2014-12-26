lazy val root = (project in file(".")).
  settings(
  name := "scario",
  version := "1.0",
  scalaVersion := "2.11.4"
)

libraryDependencies  ++= Seq(
  //"org.scalanlp" %% "breeze" % "0.11-SNAPSHOT"
  // other dependencies here
  "org.scalanlp" %% "breeze" % "0.10",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp" %% "breeze-natives" % "0.10"
)

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.11-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)