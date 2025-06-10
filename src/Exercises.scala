package fs2explore
package exercises

import fs2.*
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
def drain[F[_], O](s: Stream[F, O]): Stream[F, Nothing] = s.flatMap(o => Stream.empty[F])

// Stream.exec(IO.println("!!")).compile.toVector.unsafeRunSync()
// res29: Vector[Nothing] = Vector()
def exec[F[_]](action: F[Unit]): Stream[F, Nothing] = drain(Stream.eval(action))

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
// Try implementing takeWhile, intersperse, and scan:
// Stream.range(0,100).takeWhile(_ < 7).toList
// res42: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
def takeWhile[F[_], O](s: Stream[F, O])(p: O => Boolean): Stream[F, O] =
  def go(st: Stream[F, O]): Pull[F, O, Unit] = st.pull.uncons1
    .flatMap:
      case Some(o, stream) => if p(o) then Pull.output1(o) >> go(stream) else Pull.done
      case None            => Pull.done
  go(s).stream

// Stream("Alice","Bob","Carol").intersperse("|").toList
// res43: List[String] = List("Alice", "|", "Bob", "|", "Carol")
def intersperse[F[_], O, O2 >: O](s: Stream[F, O])(separator: O2): Stream[F, O2] =
  def go(st: Stream[F, O]): Pull[F, O2, Unit] = st.pull.uncons1
    .flatMap:
      case Some(o, stream) =>
        Pull.output1(separator) >> Pull.output1(o) >> go(stream)
      case None => Pull.done
  go(s).stream.tail

// Stream.range(1,10).scan(0)(_ + _).toList // running sum
// res44: List[Int] = List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
def scan[F[_], O, O2](s: Stream[F, O])(z: O2)(f: (O2, O) => O2): Stream[F, O2] = ???

@main
def run: Unit =
  println(repeat(Stream(1, 0)).take(6).toList)
  println:
    attempt(Stream(1, 2) ++ Stream(3).map(_ => throw new Exception("nooo!!!"))).toList
  println(drain(Stream(1, 2, 3)).toList)
  println:
    exec(IO.println("!!")).compile.toVector
      .unsafeRunSync()

  // Stream transforming
  println(takeWhile(Stream.range(0, 10))(x => x % 2 == 0 || x % 3 == 1).toList)
  println(takeWhile(Stream.range(0, 100))(_ < 7).toList)
  println(intersperse(Stream("Alice", "Bob", "Carol"))("|").toList)
  // println(scan(Stream.range(1, 10))(0)(_ + _).toList)
