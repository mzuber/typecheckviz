package example

import controllers._ 

import typechecklib.Rules._
import typechecklib.Types._
import typechecklib.Constraints._
import typechecklib._

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers

import scala.collection.mutable.Stack
import scala.collection.mutable.Queue
object ExampleParser extends RegexParsers {
  import SimpleTypes._
  import typechecklib.Syntax._

  def parse(in: String): Either[String, Term] = parseAll(term, in) match {
    case Success(result, _) => Right(result)
    case failure: NoSuccess => Left(failure.msg)
  }

  def parseVar: Parser[Var] = """[a-z][a-zA-Z0-9]*""".r ^^ { case s: String => Var(s) }
  def parseConst: Parser[Const] = """\d+""".r ^^ { case d => Const(d.toInt) }
  def parseAbs: Parser[Abs] = """\\""".r ~> parseVar ~ "." ~ term ^^ { case v ~ _ ~ t => Abs(v, t) }

  def simpleterm: Parser[Term] = parseConst | parseVar | "(" ~> term <~ ")" | parseAbs
  def app: Parser[Term] = chainl1(simpleterm, simpleterm, success(()) ^^ (_ => ((x: Term, y: Term) => App(x, y))))
  def term: Parser[Term] = binop
  def simpleexpr: Parser[Term] = neg | app
  def neg: Parser[Term] = "-" ~> term ^^ {
    case Const(i) => Const(-i)
    case e => App(App(Var("*"), Const(-1)), e)
  }
  def reservedOpsRegex: Parser[String] = """(\+s)|[\+\-\*><]|/|<=|==|/=|>=""".r ^^ { case s: String => s }
  def binopRegex: Parser[Var] = reservedOpsRegex ^^ { s => Var(s) }
  //Shunting-yard algorithm
  def binop: Parser[Term] = simpleexpr ~ rep(binopRegex ~ simpleexpr) ^^ {
    case x ~ xs =>
      var input = new Queue ++= (x :: (xs.flatMap({ case a ~ b => List(a, b) }))) //TODO
      val out: Stack[Term] = new Stack
      val ops: Stack[Term] = new Stack
      var isOp = false
      while (!input.isEmpty) {
        val o1 = input.dequeue
        if (isOp) {
          while (!ops.isEmpty && prec(o1) <= prec(ops.head)) {
            clearStack(out, ops)
          }
          ops.push(o1)
        } else {
          out.push(o1.asInstanceOf[Term])
        }
        isOp = !isOp
      }
      while (!ops.isEmpty) clearStack(out, ops)
      if (out.size != 1) failure("OutputStack should have only one value")
      out.pop
  }

  private def clearStack(out: Stack[Term], ops: Stack[Term]) =
    {
      val o2 = ops.pop
      val a = out.pop
      val b = out.pop
      out.push(App(App(o2, b), a))
    }

  def prec(op: Any): Int = op match {
    case Var(">") => 1
    case Var("=") => 1
    case Var("<") => 1
    case Var("+") => 2
    case Var("-") => 2
    case Var("*") => 3
    case Var("/") => 3
    case _ => 0
  }
}
