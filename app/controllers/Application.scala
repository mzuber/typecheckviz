package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import scala.reflect.runtime.universe.typeOf

import typechecklib.Syntax.Ide
import typechecklib.Rules._
import typechecklib.Types._
import typechecklib.Constraints._
import typechecklib.Constraints._
import typechecklib._
import example._
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers
import scala.reflect.runtime.universe.typeOf

object Application extends Controller with ExampleVisuTypeChecker {
 this : ScanConstraintSolver with VisualisationTypeChecker =>

  def eval = Action {
    request =>
      val body: AnyContent = request.body
      val textBody: Option[String] = body.asText
      textBody.map { text =>
        parse(text) match {
          case Right(t) =>
            val judgement = Judgement(context, t, TypeVariable())
            typeDerivation(judgement).fold(
              {e =>
                BadRequest("Error in type derivation:" +e.toString)},
              {s => 
                val irs = scanSolveConstraints(flatten(s))
                Ok(toJson(Map("tree" ->treeToJson(s),
                  "solverSteps" -> toJson(irs.map(IntermediateResultToJson(_))))))}
            )
          case Left(s) => BadRequest(s)
        }
      }.getOrElse {
        BadRequest("Expecting text/plain request body")
      }
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
      "conclusion" -> toJson("Γ⊢" + c.rule.conclusion.expr + "<:>" + c.rule.conclusion.ty),
      "context" -> mapToJson(c.rule.conclusion.ctx.ctx), 
      "constraints" -> toJson(c.rule.constraints.map(x=>toJson(x.toString))),
      "premises" -> toJson(c.children.map(treeToJson(_)))))
  }

}

trait VisualisationTypeChecker extends TypeChecker
{
 this: ScanConstraintSolver with TreeTraversal with ConstraintGeneration =>
 def parse(in: String): Either[String, Any]
 val context : Context
}

