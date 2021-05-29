/*
 *  Implicits.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019-2021 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package dotterweide.metals

import java.util.concurrent.{CompletableFuture, Future => JFuture}

import dotterweide.editor.Aborted

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

object Implicits {
  implicit def liftJFuture[A](in: JFuture[A])(implicit exec: ExecutionContext): Future[A] = Future {
    in.get()
  }

  implicit def liftFutureToCompletable[A](in: Future[A])(implicit exec: ExecutionContext): CompletableFuture[A] = {
    val res = new CompletableFuture[A]
    in.onComplete {
      case Success(x)           => res.complete(x)
      case Failure(Aborted())   => res.cancel(false)
      case Failure(ex)          => res.completeExceptionally(ex)
    }
    res
  }
}
