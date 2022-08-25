Visualizing type checkers
=========================

Visualize the different phases of type checkers defined with the [type checker library](https://github.com/mzuber/typechecklib).

Installation
------------

This project can be built with JDK 8 and [sbt](https://www.scala-sbt.org/index.html) version `0.13.18`.

To compile and start the application, run:

```
sbt run
```

Usage
-----

The example application in this project provides a [type checker for a simply-typed lambda calculus](app/controllers/Example.scala)
with built-in types for integers (`int`) and booleans (`bool`) as well as some built-in functions on integers and 
booleans (`+`, `-`, `*`, `/`, `<`, `>`, `=`, `true`, `false`).

The _Derivation Tree View_ shows the derivation tree annotated with constraints for the given program.

![Derivation Tree](/docs/typecheckviz-derivation-tree.png)

The _Constraint Solver View_ allows the user to step through the constraint solving process.

![Constraint Solver](/docs/typecheckviz-constraint-solver.png)
