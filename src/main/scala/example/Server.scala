package example

import cats.effect._
import cats.implicits._
import example.domain.News._
import example.infrastructure.endpoint._
import example.infrastructure.repository._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

// TODO: config, db later - hardcode now & check that sangria works...

object Server extends IOApp {
  def createInfractructure[F[_] : ContextShift : ConcurrentEffect : Timer] : NewsService[F] = {
    //for{
    //_ <- IO("test")
    val newsRepo = NewsRepositoryInMemoryInterpreter[F]()
    val newsValidation = NewsValidationInterpreter[F](newsRepo)
    val newsService = NewsService[F](newsRepo, newsValidation)
    //} yield newsService
    newsService
  }

  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(GraphQLEndpoints.graphQLEndpoint(createInfractructure[IO]))
      .resource
      .use(_ => IO.never).as(ExitCode.Success)
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
