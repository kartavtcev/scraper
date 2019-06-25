package example

import cats.effect._
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import example.parser.ScraperParser
import example.infrastructure.Infrastructure

import scala.concurrent.ExecutionContext

object WebCrawler extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global
    implicit val sttpBackend = AsyncHttpClientCatsBackend[cats.effect.IO]()

    (for {
      (newsService, appConfs, blockingCachedEc) <- Infrastructure.create[IO]
      call <- Resource(IO.delay {
        (sttp.get(uri"${appConfs.webcrawler.url}").send(), IO.delay(sttpBackend.close()))
      })
      _ <- Resource.liftF(call >>= { content =>
        ScraperParser.parseNews(appConfs.webcrawler.scrapeClass, content.body) match {
          case Left(error) => // TODO: log error
            IO.unit
          case Right(list) =>
            list.traverse(newsService.create(_).value)
        }
      })
    } yield ())
      .use(_ => IO.unit)
      //.handleErrorWith(_ => IO.unit)  // TODO: handle & log errors.
      .as(ExitCode.Success)
  }
}