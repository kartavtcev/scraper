package example.infrastructure.repository

import cats.Applicative
import cats.implicits._
import example.domain.News.{NewsItem, NewsRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class NewsRepositoryInMemoryInterpreter[F[_]: Applicative] extends NewsRepositoryAlgebra[F] {
  private val cache = new TrieMap[String, NewsItem]

  // test data setup:
  val link1 = "https://www.wsj.com/articles/acting-budget-chief-seeks-reprieve-on-huawei-ban-11560108418"
  cache += (link1 ->
    NewsItem("Acting Budget Chief Seeks Reprieve on Huawei Ban", link1))

  val link2 = "https://www.wsj.com/articles/rewrite-of-bank-rules-bogs-down-11560159001"
  cache += (link2 ->
    NewsItem("Rewrite of Bank Rules Makes Little Progress, Frustrating Republicans", link2))

  def create(newsItem: NewsItem): F[Unit] = {
    cache += (newsItem.link -> newsItem)
    ().pure[F]
  }

  def get(link: String): F[Option[NewsItem]] =
    cache.get(link).pure[F] // also eq. findByLink

  def list(pageSize: Int, offset: Int): F[List[NewsItem]] =
    cache.values.toList.sortBy(_.title).slice(offset, offset + pageSize).pure[F]
}

object NewsRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() = new NewsRepositoryInMemoryInterpreter[F]()
}
