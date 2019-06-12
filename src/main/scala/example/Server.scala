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

// TODO: logging

object Server extends IOApp {
  def createInfractructure[F[_]: ContextShift: Sync]: Resource[F, (NewsService[F], ServerConfig)] =
    for {
      conf <- Resource.liftF(parser.decodePathF[F, ApplicationConfigs]("application"))

      newsRepo = QuillNewsRepositoryInterpreter[F](conf.db)
      newsValidation = NewsValidationInterpreter[F](newsRepo)
      newsService = NewsService[F](newsRepo, newsValidation)

      _ <- Resource.liftF(DatabaseConfig.initializeDb(conf.db))

    } yield (newsService, conf.server)

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

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
  }
}
