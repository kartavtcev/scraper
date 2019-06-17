package example.infrastructure

import cats.effect._
import cats.implicits._
import example.config._
import example.domain.News._
import example.config._
import example.infrastructure.repository._
import io.circe.config.parser

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

// TODO: refactor this infrastructure to service in SOA architecture; API calls based access.
object Infrastructure {
  def create[F[_]: ContextShift: Sync]: Resource[F, (NewsService[F], ApplicationConfigs, ExecutionContext)] =
    for {
      appConfs <- Resource.liftF(parser.decodePathF[F, ApplicationConfigs]("application"))
      blockingCachedEc <- blockingThreadPool[F]

      newsRepo = QuillNewsRepositoryInterpreter[F](appConfs.db, blockingCachedEc)
      //newsRepo = InMemoryNewsRepositoryInterpreter[F]()
      newsValidation = NewsValidationInterpreter[F](newsRepo)
      newsService = NewsService[F](newsRepo, newsValidation)

    } yield (newsService, appConfs, blockingCachedEc)

  private def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool() // many short-lived asynchronous tasks
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })
}
