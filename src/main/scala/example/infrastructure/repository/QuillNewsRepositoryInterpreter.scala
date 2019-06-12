package example.infrastructure.repository

import cats.effect._
import cats.implicits._

import com.typesafe.config.ConfigFactory
import example.config.DatabaseConfig
import example.domain.News._
import io.getquill._

class QuillNewsRepositoryInterpreter[F[_]](dbConfig: DatabaseConfig)(implicit F: Sync[F], cs: ContextShift[F])
    extends NewsRepositoryAlgebra[F] {

  // I'm using circe config, not Lightbend.
  val config = ConfigFactory.parseString(s"""dataSourceClassName="${dbConfig.dataSourceClassName}",
      dataSource.url="${dbConfig.url}",
      dataSource.user=${dbConfig.user}""")
  /*
    The return type of performIO varies according to the context.
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

  // TODO: !MPRT: move pagination to DB sql to not select the entire DB over the network in PROD.
  private def selectAllLines(pageSize: Int, offset: Int): List[NewsItem] =
    ctx.run(schema.headlines).slice(offset, offset + pageSize)

  // TODO: MUST fix FP reasoning mistake. Do not used F.pure. Used some effect/concurrent effect!
  def create(newsItem: NewsItem): F[Unit] =
    cs.shift *> F.pure(insert(newsItem))

  // TODO: MUST fix FP reasoning mistake. Do not used F.pure. Used some effect/concurrent effect!
  def get(link: String): F[Option[NewsItem]] =
    cs.shift *> F.pure(selectOne(link))

  // TODO: MUST fix FP reasoning mistake. Do not used F.pure. Used some effect/concurrent effect!
  def list(pageSize: Int, offset: Int): F[List[NewsItem]] =
    cs.shift *> F.pure(selectAllLines(pageSize, offset))
}

object QuillNewsRepositoryInterpreter {
  def apply[F[_]](dbConfig: DatabaseConfig)(implicit F: Sync[F], cs: ContextShift[F]) = new QuillNewsRepositoryInterpreter[F](dbConfig)
}
