# dotterweide-exp-metals

The dotterweide projects aims to develop an embeddable mini-IDE with support for the Scala programming language.
Please refer to the [organisational project](https://github.com/dotterweide/dotterweide-org) for further information.

This `-exp-metals` repository contains experimental code to understand potential interaction with
the [Metals](https://scalameta.org/metals/) language server.
This repository is covered by the
[GNU Lesser General Public License v2.1](https://www.gnu.org/licenses/lgpl-2.1.txt) or higher.

The project build with [sbt](http://www.scala-sbt.org/) with the main Scala version being 2.12.x.

## credits

We use knowledge from [intelli-lsp](https://github.com/gtache/intellij-lsp) by Guillaume Tâche,
released under Apache 2.0 License.

## installation

You currently have to install Metals in the project's root directory:

    coursier bootstrap --java-opt -Dmetals.http=on org.scalameta:metals_2.12:0.5.0 -o metals -f