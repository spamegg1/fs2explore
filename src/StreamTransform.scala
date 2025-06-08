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

@main
def run: Unit =
  println(Stream(1, 2, 3, 4).through(tk(3)).toList)
