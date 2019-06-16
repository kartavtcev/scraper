package example.infrastructure.repository

import cats._
import example.domain.News._
import org.scalatest._

class InMemoryNewsRepositoryInterpreterTests extends FlatSpec with Matchers {
  "InMemoryRepo" should "correctly create, get and list elements." in {

    val newsItem1 = new NewsItem("1", "one")
    val newsItem2 = new NewsItem("2", "two")

    val repository: NewsRepositoryAlgebra[Id] = InMemoryNewsRepositoryInterpreter[Id]

    val result1: Id[Option[NewsItem]] = repository.get(newsItem1.link)
    result1 shouldEqual(None)

    repository.create(newsItem1)
    val result2: Id[Option[NewsItem]] = repository.get(newsItem1.link)
    val result3: Id[Option[NewsItem]] = repository.get(newsItem2.link)

    result2 shouldEqual(Some(newsItem1))
    result3 shouldEqual(None)

    val result4: Id[List[NewsItem]] = repository.list(10,0)
    result4.length shouldEqual(1)

  }
}
