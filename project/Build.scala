import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "typecheckviz"
  val appVersion      = "1.0-SNAPSHOT"
  val buildScalaVersion = "2.10.1"

  val typechecklibcore = ProjectRef( uri("https://github.com/mzuber/typechecklib.git"), "core")

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  ).dependsOn(typechecklibcore)

}
