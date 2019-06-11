package example

import cats.effect.{ExitCode, IO, IOApp}

// TODO: config, db later - hardcode now & check that sangria works...

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = ???
}
