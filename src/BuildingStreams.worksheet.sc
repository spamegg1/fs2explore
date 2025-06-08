import fs2.Stream
import fs2.Chunk
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val s0 = Stream.empty
val s1 = Stream.emit(1)

// variadic
val s1a = Stream(1, 2, 3)

// accepts any Seq
val s1b = Stream.emits(List(1, 2, 3))

s1.toList
s1.toVector

// Operations
// (Stream(1, 2, 3) ++ Stream(4, 5)).toList
// Stream(1, 2, 3).map(_ + 1).toList
// Stream(1, 2, 3).filter(_ % 2 != 0).toList
// Stream(1, 2, 3).fold(0)(_ + _).toList
// Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
// Stream.range(0, 5).intersperse(42).toList
// Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList
// Stream(1, 2, 3).repeat.take(9).toList
// Stream(1, 2, 3).repeatN(2).toList

// effectful streams. eval doesn't do anything yet.
val eff = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
// eff.toList // error
// eff.compile.toVector.unsafeRunSync()

// gather all output into a Vector
val ra = eff.compile.toVector

// purely for effects
val rb = eff.compile.drain

// run and accumulate some result
val rc = eff.compile.fold(0)(_ + _)

// Running
// ra.unsafeRunSync()
// rb.unsafeRunSync()
// rc.unsafeRunSync()
// rc.unsafeRunSync()

// Chunking
val s1c = Stream.chunk(Chunk.array(Array(1.0, 2.0, 3.0)))
