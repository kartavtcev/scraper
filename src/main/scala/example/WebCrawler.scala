package example

import cats.effect._
import cats.implicits._
import example.infrastructure.Infrastructure

import scala.concurrent.ExecutionContext

object WebCrawler extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

    (for {
      (newService, appConfs, blockingCachedEc) <- Infrastructure.create[IO]
    } yield ())
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
