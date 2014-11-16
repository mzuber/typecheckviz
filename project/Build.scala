import sbt._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "typecheckviz"
  val appVersion = "0.1"
  val buildScalaVersion = "2.10.4"

  val typechecklibcore = ProjectRef(uri("https://github.com/mzuber/typechecklib.git"), "core")

  val appDependencies = Seq()


  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
//    version := appVersion,
//    libraryDependencies ++= appDependencies
  ).dependsOn(typechecklibcore)

}
