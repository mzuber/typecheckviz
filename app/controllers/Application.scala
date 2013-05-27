/*
 * Copyright (c) 2013, Fabian Linges and Martin Zuber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 * - Neither the name of the TU Berlin nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

 this: TraceableConstraintSolver with TraceableTypeChecker =>

  def eval = Action {
    request =>
      val body: AnyContent = request.body
      val textBody: Option[String] = body.asText
      textBody.map { text =>
	parse(text) match {
          case Right(expr) =>
            val tv = TypeVariable()
            val judgement = Judgement(context, expr, tv)
            typeDerivation(judgement).fold(
              { error => BadRequest("Error in type derivation: " + error) },
              { tree  => val intermediateResults = traceSolver(flatten(tree))
		         val result = intermediateResults.last match {
			   case IntermediateResult(_, Left(error), _, _) => "error" -> toJson(error.toString)
			   case IntermediateResult(_, Right(_), σ, _)    => "result" -> toJson(σ[Type](tv).toString)
	                 }

		         Ok(toJson(Map("tree" -> treeToJson(tree),
				       "solverSteps" -> toJson(intermediateResults.map(IntermediateResultToJson(_))),
                                       result)))
	      }
            )
          case Left(error) => BadRequest(error)
	}
      }.getOrElse(BadRequest("Expecting text/plain request body"))
  }

  def index = Action {
    Ok(views.html.index())
  }

  def IntermediateResultToJson(ir: IntermediateResult) : JsValue = {
    toJson(Map("current" -> toJson(ir.constraint.toString),
	       "result" -> ir.result.fold({x => toJson(x.toString)}, {x => mapToJson(x.sub)}),
	       "unsolved" -> toJson(ir.remainingConstraints.map(x => toJson(x.toString))),
	       "substitution" -> mapToJson(ir.substitution.sub)))
  }

  def mapToJson[A,B](m: Map[A,B]) =  JsObject(m.map({
                        case (key, value) => (key.toString(),JsString(value.toString()))
                    }).toList) 

  def treeToJson(tree: ConstraintTree): JsValue = {

    Json.toJson(Map("rulename"       -> toJson(tree.rule.name),
		    "conclusion"     -> toJson("Γ ⊢ " + tree.rule.conclusion.expr + " : " + tree.rule.conclusion.ty),
                    "conclusionExpr" -> toJson(tree.rule.conclusion.expr.toString),
                    "conclusionTy"   -> toJson(tree.rule.conclusion.ty.toString),
		    "context"        -> mapToJson(tree.rule.conclusion.ctx.ctx), 
		    "constraints"    -> toJson(tree.rule.constraints.map(c => toJson(c.constraint.toString))),
		    "premises"       -> toJson(tree.children.map(treeToJson(_)))))
  }

}


/**
  * A trait for visualizable type checkers.
  */
trait TraceableTypeChecker extends TypeChecker {

  this: ConstraintGeneration with TreeTraversal with TraceableConstraintSolver =>

  /**
    * Parse an expression.
    */
  def parse(in: String): Either[String, Any]

  /**
    * Initial context for the type checker.
    */
  val context: Context
}
