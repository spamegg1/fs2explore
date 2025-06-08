package fs2explore

import cats.effect.{IO, IOApp}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object Converter extends IOApp.Simple:
  def fahrenheitToCelsius(f: Double): Double = (f - 32.0) * (5.0 / 9.0)

  def converter(inPath: Path, outPath: Path): Stream[IO, Unit] = Files[IO]
    .readUtf8Lines(inPath)
    .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
    .map: line =>
      fahrenheitToCelsius(line.toDouble).toString
    .intersperse("\n")
    .through(text.utf8.encode)
    .through(Files[IO].writeAll(outPath))

  def run: IO[Unit] =
    val inPath  = Path("resources") / "fahrenheit.txt"
    val outPath = Path("resources") / "celcius.txt"
    converter(inPath, outPath).compile.drain
