package example.infrastructure.repository

import cats.effect._
import com.typesafe.config.ConfigFactory
import example.config.DatabaseConfig
import example.domain.News._
import io.getquill._

import scala.concurrent.ExecutionContext

class QuillNewsRepositoryInterpreter[F[_]](dbConfig: DatabaseConfig, blockingCachedEc: ExecutionContext)(
    implicit F: Sync[F],
    cs: ContextShift[F])
    extends NewsRepositoryAlgebra[F] {

  // I'm using circe config, not Lightbend.
  val config = ConfigFactory.parseString(s"""dataSourceClassName="${dbConfig.dataSourceClassName}",
      dataSource.url="${dbConfig.url}",
      dataSource.user=${dbConfig.user}""")
  /*
    The return type of (performIO) varies according to the context.
    For instance, async contexts return Futures while JDBC returns values synchronously.
   */
  lazy val ctx = new H2JdbcContext(SnakeCase, config)
  import ctx._

  object schema {
    val headlines = quote {
      querySchema[NewsItem](
        "headlines",
        _.link -> "link",
        _.title -> "title"
      )
    }
  }

  private def insert(newsItem: NewsItem): Unit =
    ctx.run(schema.headlines.insert(lift(newsItem)))

  private def selectOne(link: String): Option[NewsItem] =
    ctx.run(schema.headlines.filter(_.link == lift(link))) headOption

  // TODO: !MPRT: move pagination to DB sql to not select the entire DB over the network in PROD. Or Stream.
  private def selectAllLines(pageSize: Int, offset: Int): List[NewsItem] =
    ctx.run(schema.headlines).slice(offset, offset + pageSize)

  def create(newsItem: NewsItem): F[Unit] =
    cs.evalOn(blockingCachedEc)(Sync[F].delay(insert(newsItem)))

  def get(link: String): F[Option[NewsItem]] =
    cs.evalOn(blockingCachedEc)(Sync[F].delay(selectOne(link)))

  def list(pageSize: Int, offset: Int): F[List[NewsItem]] =
    cs.evalOn(blockingCachedEc)(Sync[F].delay(selectAllLines(pageSize, offset)))
}

object QuillNewsRepositoryInterpreter {
  def apply[F[_]](dbConf: DatabaseConfig, blockingCachedEc: ExecutionContext)(implicit F: Sync[F], cs: ContextShift[F]) =
    new QuillNewsRepositoryInterpreter[F](dbConf, blockingCachedEc)
}
