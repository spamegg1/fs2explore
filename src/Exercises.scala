package fs2explore
package exercises

import fs2.Stream
import fs2.Chunk
import cats.effect.IO
import cats.effect.unsafe.implicits.global

// Stream Building
// Implement:
// repeat, which repeats a stream indefinitely,
// drain, which strips all output from a stream,
// exec, which runs an effect and ignores its output, and
// attempt, which catches any errors produced by a stream:

// Stream(1,0).repeat.take(6).toList
// res27: List[Int] = List(1, 0, 1, 0, 1, 0)
def repeat[F[_], O](s: Stream[F, O]): Stream[F, O] = s ++ repeat(s)

// Stream(1,2,3).drain.toList
// res28: List[Nothing] = List()
def drain[F[_], O](s: Stream[F, O]): Stream[F, Nothing] = s
  .flatMap(o => Stream.empty[F])

// Stream.exec(IO.println("!!")).compile.toVector.unsafeRunSync()
// res29: Vector[Nothing] = Vector()
def exec[F[_]](action: F[Unit]): Stream[F, Nothing] =
  drain(Stream.eval(action))

// (Stream(1,2) ++ Stream(3).map(_ => throw new Exception("nooo!!!"))).attempt.toList
// res30: List[Either[Throwable, Int]] = List(
//   Right(value = 1),
//   Right(value = 2),
//   Left(value = java.lang.Exception: nooo!!!)
// )
def attempt[F[_], O](s: Stream[F, O]): Stream[F, Either[Throwable, O]] = s
  .map(Right(_))
  .handleErrorWith(err => Stream.emit(Left(err)))

// Stream Transforming

@main
def run: Unit =
  println(repeat(Stream(1, 0)).take(6).toList)
  println:
    attempt(Stream(1, 2) ++ Stream(3).map(_ => throw new Exception("nooo!!!"))).toList
  println(drain(Stream(1, 2, 3)).toList)
  println:
    exec(IO.println("!!")).compile.toVector.unsafeRunSync()
