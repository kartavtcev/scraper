package example.domain.News

import cats.effect._
import example.config._
import example.domain._
import example.infrastructure._
import org.scalatest._

import scala.concurrent.ExecutionContext

class NewsServiceIntegrationTests extends FlatSpec with Matchers {
  "NewsService" should "correctly create, get and list elements with news repo and validation" in {
    implicit val ec = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    val newsItem1 = new NewsItem("1", "one")
    val newsItem2 = new NewsItem("2", "two")

    Infrastructure
      .create[IO]
      .use {
        case (newsService, appConfs, blockingCachedEc) =>
          IO {
            DatabaseConfig.initializeDb(appConfs.db)(Sync[IO]).unsafeRunSync()

            newsService.get(newsItem1.link).value.unsafeRunSync() shouldEqual (Left(NotFoundError))

            newsService.create(NewsItem("1", "one")).value.unsafeRunSync()

            newsService.get(newsItem1.link).value.unsafeRunSync() shouldEqual (Right(newsItem1))
            newsService.get(newsItem2.link).value.unsafeRunSync() shouldEqual (Left(NotFoundError))

            newsService.list(10, 0).unsafeRunSync().headOption shouldEqual ((Some(newsItem1)))
          }
      }
      .unsafeRunSync()
  }
}