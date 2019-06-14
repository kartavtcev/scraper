package example

import cats.effect._
import cats.implicits._
import example.config._
import example.domain.News._
import example.infrastructure.endpoint._
import example.infrastructure.repository._
import org.http4s.server.blaze.BlazeServerBuilder
import io.circe.config.parser

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


// TODO: logging

object Server extends IOApp {
  def createInfractructure[F[_]: ContextShift: Sync]: Resource[F, (NewsService[F], ServerConfig, ExecutionContext)] =
    for {
      appConf <- Resource.liftF(parser.decodePathF[F, ApplicationConfigs]("application"))
      blockingCachedEc <- blockingThreadPool[F]

      newsRepo = QuillNewsRepositoryInterpreter[F](appConf.db, blockingCachedEc)
      newsValidation = NewsValidationInterpreter[F](newsRepo)
      newsService = NewsService[F](newsRepo, newsValidation)

      _ <- Resource.liftF(DatabaseConfig.initializeDb(appConf.db))

    } yield (newsService, appConf.server, blockingCachedEc)

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

    createInfractructure[IO]
      .flatMap {
        case (newService, serverConf, blockingCachedEc) =>
          BlazeServerBuilder[IO]
            .bindHttp(serverConf.port, serverConf.host)
            .withHttpApp(GraphQLEndpoints.graphQLEndpoint(newService, blockingCachedEc))
            .resource
      }
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()  // many short-lived asynchronous tasks
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })
}
