package example

import cats.effect._
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import example.domain.News.NewsItem
import example.infrastructure.Infrastructure
import net.ruippeixotog.scalascraper.browser._
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

      content <- Resource.liftF(sttp.get(uri"https://www.wsj.com").send())

      _ = parseNews(content.body).map(_.foreach(newService.create(_)))

    } yield ()) //(ExitCode.Success)
      .use(_ => IO.unit)
      .as(ExitCode.Success)

    //return IO(ExitCode.Success)
  }

  def parseNews(content: Either[String, String]): Either[String, List[NewsItem]] = {
    content.map(text => {

      val browser = JsoupBrowser()
      val doc = browser.parseString(text)
      val items: List[Element] = doc >> elementList(".wsj-headline-link")
      val news = items.map(item => NewsItem(item >> attr("href"), item.text))
      //news
      List(NewsItem("1", "one"), NewsItem("2", "two"))
    })
  }
}
