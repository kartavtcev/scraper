package example

import cats.effect._
import cats.implicits._
import example.config._
import example.infrastructure._
import example.infrastructure.endpoint._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

// TODO: logging

object GraphQLServer extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

    (for {
      (newService, appConfs, blockingCachedEc) <- Infrastructure.create[IO]
      _ <- Resource.liftF(DatabaseConfig.initializeDb(appConfs.db)(Sync[IO]))

      server <- BlazeServerBuilder[IO]
        .bindHttp(appConfs.server.port, appConfs.server.host)
        .withHttpApp(GraphQLEndpoints.graphQLEndpoint(newService, blockingCachedEc))
        .resource
    } yield server)
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}