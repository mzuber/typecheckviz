package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._

import typechecklib.Syntax.Ide
import typechecklib.Rules._
import typechecklib.Types._
import typechecklib.Constraints._
import typechecklib._

import example._


/**
  * The type checker application.
  *
  * Extend this object with your implementation of the 'TraceableTypeChecker' trait.
  */
object Application extends Controller with ExampleTypeChecker {

 this: ScanConstraintSolver with TraceableTypeChecker =>

  def eval = Action {
    request =>
      val body: AnyContent = request.body
      val textBody: Option[String] = body.asText
      textBody.map { text =>
        parse(text) match {
          case Right(t) =>
            val judgement = Judgement(context, t, TypeVariable())
            typeDerivation(judgement).fold(
              {e => BadRequest("Error in type derivation:" + e.toString)},
              {s => val irs = scanSolveConstraints(flatten(s))
                    Ok(toJson(Map("tree" -> treeToJson(s),
				  "solverSteps" -> toJson(irs.map(IntermediateResultToJson(_))))))}
            )
          case Left(s) => BadRequest(s)
        }
      }.getOrElse(BadRequest("Expecting text/plain request body"))
  }

  def index = Action {
    Ok(views.html.index())
  }

  def IntermediateResultToJson(ir: IntermediateResult) : JsValue = {
    toJson(Map("current" -> toJson(ir.current.toString),
      "result" -> ir.result.fold({x=>toJson(x.toString)},{x=>mapToJson(x.sub)}),
      "unsolved" -> toJson(ir.unsolved.map(x=>toJson(x.toString))),
      "substitution" -> ir.sub.fold({x=>toJson(x.toString)},{x=>mapToJson(x.sub)})))
  }

  def mapToJson[A,B](m: Map[A,B]) =  JsObject(m.map({
                        case (key, value) => (key.toString(),JsString(value.toString()))
                    }).toList) 

  def treeToJson(c: ConstraintTree): JsValue = {

    Json.toJson(Map("rulename" -> toJson(c.rule.name),
      "conclusion" -> toJson("Γ ⊢ " + c.rule.conclusion.expr + " : " + c.rule.conclusion.ty),
      "context" -> mapToJson(c.rule.conclusion.ctx.ctx), 
      "constraints" -> toJson(c.rule.constraints.map(x=>toJson(x.toString))),
      "premises" -> toJson(c.children.map(treeToJson(_)))))
  }

}


/**
  * A trait for visualizable type checkers.
  */
trait TraceableTypeChecker extends TypeChecker {

  this: ConstraintGeneration with TreeTraversal with ScanConstraintSolver =>

  /**
    * Parse an expression.
    */
  def parse(in: String): Either[String, Any]

  /**
    * Initial context for the type checker.
    */
  val context: Context
}
