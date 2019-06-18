package example

import cats.effect._
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import example.domain.News._
import example.infrastructure.Infrastructure
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model._

import scala.concurrent.ExecutionContext

object WebCrawler extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global
    implicit val sttpBackend = AsyncHttpClientCatsBackend[cats.effect.IO]()

    (for {
      (newService, appConfs, blockingCachedEc) <- Infrastructure.create[IO]

      content <- Resource.liftF(sttp.get(uri"${appConfs.webcrawler.url}").send())

      _ <- Resource.liftF(parseNews(appConfs.webcrawler.scrapeClass, content.body) match {
        case Left(error) => // TODO: log error
          IO.unit
        case Right(list) =>
          list.traverse(newService.create(_).value)
      })
    } yield ())
      .use(_ => IO.unit)
      .as(ExitCode.Success)
    // TODO: stop & exit.
  }

  def parseNews(scrapeClass: String, content: Either[String, String]): Either[String, List[NewsItem]] = {
    content.map(text => {

      val browser = JsoupBrowser()
      val doc = browser.parseString(text)
      val items: List[Element] = doc >> elementList(scrapeClass)
      val news = items.map(item => NewsItem(item.text, item >> attr("href")))

      news
    })
  }
}
