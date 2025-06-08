package fs2explore
package basicOpsErrHand

import fs2.Stream
import fs2.Chunk
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4)) // 1,2,3,4
val appendEx3 = appendEx1.toVector                         // 1,2,3,42
val appendEx4 = appendEx2.compile.toVector.unsafeRunSync()
val appendEx5 = appendEx1.map(_ + 1).toList                // 2,3,4,43
val appendEx6 = appendEx1.flatMap(i => Stream.emits(List(i, i))).toList
// 1,1,2,2,3,3,42,42

// Error handling
val err1 = Stream.raiseError[IO](new Exception("oh noes!"))
val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
val err4 =
  Stream(1, 2, 3).covary[IO] ++
    Stream.raiseError[IO](new Exception("bad things!")) ++
    Stream.eval(IO(4))

// resource acquisition and cleanup
val count = new java.util.concurrent.atomic.AtomicLong(0)
val acquire = IO:
  println(s"incremented: ${count.incrementAndGet}")
  ()
val release = IO:
  println(s"decremented: ${count.decrementAndGet}")
  ()

@main
def run: Unit =
  try err1.compile.toList.unsafeRunSync()
  catch case e: Exception => println(e)

  try err2.toList
  catch case e: Exception => println(e)

  try err3.compile.drain.unsafeRunSync()
  catch case e: Exception => println(e)

  err1
    .handleErrorWith: e => // catch errors
      Stream.emit(e.getMessage)
    .compile
    .toList
    .unsafeRunSync()

  // after error, further values are not pulled. Here 4 is not pulled
  err4
    .handleErrorWith: _ =>
      Stream(0)
    .compile
    .toList
    .unsafeRunSync() // List(1,2,3,0)

  Stream
    .bracket(acquire)(_ => release)
    .flatMap: _ =>
      Stream(1, 2, 3) ++ err1
    .compile
    .drain
    .unsafeRunSync()

  println(count.get) // resource is still released even though stream failed.
