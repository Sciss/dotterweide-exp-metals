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
