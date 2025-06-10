package fs2explore
package streamTransform

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] = in =>
  in.scanChunksOpt[Long, O, O](n): k =>
    if k <= 0 then None
    else
      Some: c =>
        c.size match
          case m if m < k => (k - m, c)
          case _          => (0, c.take(k.toInt))

// transforming using Pull[F[_], O, R]
// F effect, O output, R result, forms a monad in R
val p1 = Pull.output1(1) // Pull[Nothing, Int, Unit]
val s1 = p1.stream       // Stream[Nothing, Int]
val p2 = s1.pull.echo    // same as p1
val p3 = s1.pull.uncons  // Pull[Nothing,Nothing,Option[(Chunk[Int],Stream[Nothing,Int])]]

def tk2[F[_], O](n: Long): Pipe[F, O, O] =
  def go(s: Stream[F, O], k: Long): Pull[F, O, Unit] = s.pull.uncons
    .flatMap:
      case Some((hd, tl)) =>
        hd.size match
          case m if m <= k => Pull.output(hd) >> go(tl, k - m)
          case _           => Pull.output(hd.take(k.toInt))
      case None => Pull.done
  in => go(in, n).stream

def tk3[F[_], O](n: Long): Pipe[F, O, O] =
  in => in.pull.take(n).void.stream

@main
def run: Unit =
  val s = Stream(1, 2, 3, 4)
  println(s.through(tk(3)).toList)
  println(s.through(tk2(3)).toList)
  println(s.through(tk3(3)).toList)
