package example.infrastructure.repository

import cats.effect._
import com.typesafe.config.ConfigFactory
import example.config.DatabaseConfig
import example.domain.News._
import io.getquill._

class QuillNewsRepositoryInterpreter[F[_] : Sync](dbConfig: DatabaseConfig) extends NewsRepositoryAlgebra[F] {

  // I'm using circe config, not Lightbend for the app.
  val config = ConfigFactory.parseString(
    s"""dataSourceClassName="${dbConfig.dataSourceClassName}",
      dataSource.url="${dbConfig.url}",
      dataSource.user=${dbConfig.user}""")
  lazy val ctx = new H2JdbcContext(SnakeCase, config)
  import ctx._

  def selectAllLines: List[NewsItem] = {
    val newsItemsQuery = quote {
      querySchema[NewsItem](
        "headlines",
        _.link -> "link",
        _.title -> "title"
      )
    }
    val q = quote {
      newsItemsQuery
    }

    ctx.run(q)
  }


  def create(newsItem: NewsItem): F[Unit] = ???

  def get(link: String): F[Option[NewsItem]] = ???

  def list(pageSize: Int, offset: Int): F[List[NewsItem]] = {
    Sync[F].delay(selectAllLines)
  }
}

object QuillNewsRepositoryInterpreter {
  def apply[F[_]: Sync](dbConfig: DatabaseConfig) = new QuillNewsRepositoryInterpreter[F](dbConfig)
}