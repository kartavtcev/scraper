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

    Infrastructure
      .create[IO]
      .use {
        case (newService, appConfs, blockingCachedEc) =>
          sttp.get(uri"${appConfs.webcrawler.url}").send() >>= { content =>
            ScraperParser.parseNews(appConfs.webcrawler.scrapeClass, content.body) match {
              case Left(error) => // TODO: log error
                IO.unit
              case Right(list) =>
                list.traverse(newService.create(_).value)
            }
          }
      }
      //.handleErrorWith(_ => IO.unit) // TODO: handle & log errors.
      .map(_ => sttpBackend.close()) // Manually close sttp "at the end of the world" to stop the program.
      .as(ExitCode.Success)
  }
}