package example

import cats.effect._
import cats.implicits._
import example.config._
import example.domain.News._
import example.infrastructure.endpoint._
import example.infrastructure.repository._
import org.http4s.server.blaze.BlazeServerBuilder
import io.circe.config.parser

import scala.concurrent.ExecutionContext

// TODO: config, db, not a hardcoded test data
// TODO: logging

object Server extends IOApp {
  def createInfractructure[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, (NewsService[F], ServerConfig)] =
    for {
      conf <- Resource.liftF(parser.decodePathF[F, ApplicationConfigs]("application"))

      newsRepo = NewsRepositoryInMemoryInterpreter[F]()
      newsValidation = NewsValidationInterpreter[F](newsRepo)
      newsService = NewsService[F](newsRepo, newsValidation)

      _ <- Resource.liftF(DatabaseConfig.initializeDb(conf.db))

    } yield (newsService, conf.server)

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

    //val test =
    createInfractructure[IO]
      .flatMap {
        case (newService, serverConfig) =>
          BlazeServerBuilder[IO]
            .bindHttp(serverConfig.port, serverConfig.host)
            .withHttpApp(GraphQLEndpoints.graphQLEndpoint(newService))
            .resource
      }
      .use(_ => IO.never)
      .as(ExitCode.Success)

    /*
    BlazeServerBuilder[IO]
      .bindHttp(8081, "0.0.0.0")
      .withHttpApp(GraphQLEndpoints.graphQLEndpoint())
      .resource
      .use(_ => IO.never).as(ExitCode.Success)

   */
  }

  /*
  def createServer(newsService: NewsService[IO])(implicit ec: ExecutionContext, cs: ContextShift[IO]) = {
    val server =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(GraphQLEndpoints.graphQLEndpoint(newsService))
      .resource
    server
  }
   */

  /*
  def run(args: List[String]): IO[ExitCode] =
    createServer(createInfractructure[IO]).use(_ => IO.never).as(ExitCode.Success)
 */
}
