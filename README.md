# dotterweide-exp-metals

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/dotterweide/dotterweide-org?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://github.com/dotterweide/dotterweide-exp-metals/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/dotterweide/dotterweide-exp-metals/actions?query=workflow%3A%22Scala+CI%22)

The Dotterweide projects aims to develop an embeddable mini-IDE with support for the Scala programming language.
Please refer to the [organisational project](https://github.com/dotterweide/dotterweide-org) for further information.

This `-exp-metals` repository contains experimental code to understand potential interaction with
the [Metals](https://scalameta.org/metals/) language server.
This repository is covered by the
[GNU Lesser General Public License v2.1](https://www.gnu.org/licenses/lgpl-2.1.txt) or higher.

The project build with [sbt](http://www.scala-sbt.org/) with the main Scala version being 2.13.x.

There was a first [demo video](https://peertube.social/videos/watch/3b142190-26b1-47f6-8950-0e0700e19883) showing that
it works in principle (compiler error highlighting).

## credits

We use knowledge from [intelli-lsp](https://github.com/gtache/intellij-lsp) by Guillaume Tâche,
released under Apache 2.0 License.

File `Scala.tmLanguage.json`, while evaluating tm4s, is taken from https://github.com/scala/vscode-scala-syntax ,
released under MIT License.

## installation

You currently have to install Metals in the project's root directory:

    coursier bootstrap org.scalameta:metals_2.13:1.0.1 -o metals -f

(If using a snapshot version of metals, add `-r sonatype:snapshots`).

## notes

For bundling Coursier to install Metals, see [Mellite-launcher project](https://github.com/Sciss/Mellite-launcher).

Possible syntax parsers to replace scalariform. First, based on TextMate and VS Code:

- https://github.com/scala/vscode-scala-syntax - reg-ex based type script file
- https://github.com/Microsoft/vscode-textmate - the regex parser for textmate bundles used by vs code
  (wraps Oniguruma C regex library via wasm)
- https://github.com/jruby/joni - Java port of Oniguruma
- https://github.com/eclipse/tm4e - Java port of vscode-textmate for use within eclipse, but core package seems
  usable outside of eclipse

Ólafur says "you can populate the download cache by calling this dedicated main method 
https://github.com/scalameta/metals/blob/main/metals/src/main/scala/scala/meta/metals/DownloadDependencies.scala "

Then Ammonite:

- https://github.com/com-lihaoyi/fastparse - Scala library that includes a parser example for Scala 2

LH says "Ammonite uses scalaparse for syntax highlighting without generating a token stream; you can look at the code
and see how it's done with intercept and tracking character ranges".

## Current issues

Some compiler errors are caught by bloop (`stale bloop error: ...`) instead of being communicated via LSP, see
https://github.com/scalameta/metals/discussions/5527
