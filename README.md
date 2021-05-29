# dotterweide-exp-metals

The dotterweide projects aims to develop an embeddable mini-IDE with support for the Scala programming language.
Please refer to the [organisational project](https://github.com/dotterweide/dotterweide-org) for further information.

This `-exp-metals` repository contains experimental code to understand potential interaction with
the [Metals](https://scalameta.org/metals/) language server.
This repository is covered by the
[GNU Lesser General Public License v2.1](https://www.gnu.org/licenses/lgpl-2.1.txt) or higher.

The project build with [sbt](http://www.scala-sbt.org/) with the main Scala version being 2.13.x.

There is a first [demo video](https://peertube.social/videos/watch/3b142190-26b1-47f6-8950-0e0700e19883) showing that it works in principle (compiler error highlighting).

## credits

We use knowledge from [intelli-lsp](https://github.com/gtache/intellij-lsp) by Guillaume Tâche,
released under Apache 2.0 License.

## installation

You currently have to install Metals in the project's root directory:

    coursier bootstrap org.scalameta:metals_2.12:0.10.3 -o metals -f

(If using a snapshot version of metals, add `-r sonatype:snapshots`).

## notes

For bundling Coursier to install Metals, see [Mellite-launcher project](https://github.com/Sciss/Mellite-launcher).

Possible syntax parsers to replace scalariform:

- https://github.com/scala/vscode-scala-syntax - reg-ex based type script file
- https://github.com/com-lihaoyi/fastparse - Scala library that includes a parser example for Scala 2

