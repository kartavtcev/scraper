package example

import cats.effect._
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import example.domain.News._
import example.infrastructure.Infrastructure

import scala.concurrent.ExecutionContext

object WebCrawler extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global
    implicit val sttpBackend = AsyncHttpClientCatsBackend[cats.effect.IO]()

    (for {
      (newService, appConfs, blockingCachedEc) <- Infrastructure.create[IO]

      content <- Resource.liftF(sttp.get(uri"${appConfs.webcrawler.url}").send())

      _ <-Resource.liftF(parseNews(content.body) match {
        case Left(error) => // TODO: log error
          IO.unit
        case Right(list) =>
          list.traverse(newService.create(_).value)
      })
    } yield ())
      .use(_ => IO.unit)
      .as(ExitCode.Success)
  }

  def parseNews(content: Either[String, String]): Either[String, List[NewsItem]] = {
    content.map(text => {
      /*
      val browser = JsoupBrowser()
      val doc = browser.parseString(text)
      val items: List[Element] = doc >> elementList(".wsj-headline-link")
      val news = items.map(item => NewsItem(item >> attr("href"), item.text))
       */
      //news
      List(NewsItem("1", "one"), NewsItem("2", "two"))
    })
  }
}
