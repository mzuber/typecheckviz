package example

import controllers._ 

import typechecklib.Syntax.Ide
import typechecklib.Rules._
import typechecklib.Types._
import typechecklib.Constraints._
import typechecklib._

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers


trait ExampleTypeChecker extends TraceableTypeChecker with ReflectionBasedConstraintGeneration with DepthFirstPreOrder with TraceableLinearConstraintSolver {
  import SimpleTypes._
  import scala.reflect.runtime.universe.typeOf

  /**
    * List of type rules.
    */
  val rules = List(typeOf[AbsRule], typeOf[VarRule], typeOf[AppRule], typeOf[ConstRule])

  /**
    * Some built-in functions on integers and booleans.
    */
  val context = {
    val int = BaseType("int")
    val bool = BaseType("bool")

    new Context(Var("+") -> (int --> (int --> int)),
		Var("-") -> (int --> (int --> int)),
		Var("*") -> (int --> (int --> int)),
		Var("/") -> (int --> (int --> int)),
		Var("<") -> (int --> (int --> bool)),
		Var(">") -> (int --> (int --> bool)),
		Var("=") -> (int --> (int --> bool)),
		Var("true") -> bool,
		Var("false") -> bool)
  }

  /**
    * Expression parser.
    */
  def parse(in: String): Either[String, Term] = ExampleParser.parse(in)

  object ExampleParser extends RegexParsers {

    def parse(in: String): Either[String, Term] = parseAll(term, in) match {
      case Success(result, _) => Right(result)
      case failure: NoSuccess => Left(failure.msg)
    }

    def parseVar: Parser[Var] = """[a-z][a-zA-Z0-9]*""".r ^^ { case s: String => Var(s) }
    def parseConst: Parser[Const] = """\d+""".r ^^ { case d => Const(d.toInt) }
    def parseAbs: Parser[Abs] = """\\""".r ~> parseVar ~ "." ~ term ^^ { case v ~ _ ~ t => Abs(v, t) }

    def simpleterm: Parser[Term] = parseConst | parseVar | "(" ~> term <~ ")" | parseAbs
    def term: Parser[Term] = chainl1(simpleterm, simpleterm, success(()) ^^ (_ => ((x: Term, y: Term) => App(x, y))))
  }
}


/**
  * A simply typed lambda calculus extended with constants.
  */
object SimpleTypes {

  /* Abstract Syntax */
  abstract class Term
  case class Var(ide: Ide) extends Term {
    override def toString = ide.toString
  }
  case class Abs(x: Var, e: Term) extends Term {
    override def toString = "(λ " + x + ". " + e + ")"
  }
  case class App(f: Term, e: Term) extends Term {
    override def toString = f + " " + e
  }
  case class Const(n: Int) extends Term {
    override def toString = n.toString
  }

  /*
   * Typing rule for variable lookup:
   * 
   *         T = Γ(x)
   *       ----------- (Var)
   *        Γ ⊢ x : T
   */
  case class VarRule(ctx: Context, x: Var, t: Type) extends Rule {

    Nil ==> ctx ⊢ x <:> t | t =:= ctx(x)

    override val name = "Var"
  }

  /*
   * Typing rule for lambda abstraction:
   * 
   *      Γ,x:T1 ⊢ e : T2       T = T1 -> T2
   *     ------------------------------------ (Abs)
   *                Γ ⊢ λ x.e : T
   */
  case class AbsRule(ctx: Context, abs: Abs, t: Type) extends Rule {
    val t1 = TypeVariable()
    val t2 = TypeVariable()
    val newCtx = ctx + (abs.x -> t1)

    newCtx ⊢ abs.e <:> t2 ==>
      ctx ⊢ abs <:> t | (t =:= t1 --> t2)

    override val name = "Abs"
  }

  /*
   * Typing rule for application.
   * 
   *    Γ ⊢ f : T1     Γ ⊢ e : T2     T1 = T2 -> T
   *   -------------------------------------------- (App)
   *                  Γ ⊢ (f) e : T
   */
  case class AppRule(ctx: Context, app: App, t: Type) extends Rule {
    val t1 = TypeVariable()
    val t2 = TypeVariable()

    List(ctx ⊢ app.f <:> t1, ctx ⊢ app.e <:> t2) ==>
      ctx ⊢ app <:> t | (t1 =:= t2 --> t)

    override val name = "App"
  }

  /*
   * Typing rule for integer values:
   *
   *     T = int
   *   ----------- (Int)
   *    Γ ⊢ n : T
   */
  case class ConstRule(ctx: Context, n: Const, t: Type) extends Rule {

    Nil ==> ctx ⊢ n <:> t | t =:= BaseType("int")

    override val name = "Const"
  }

}
