package example.domain.News

import cats._
import cats.data._

import example.domain.{AlreadyExistsError, NotFoundError}

// The entry point of domain, repos, validation
class NewsService[F[_]](
    repository: NewsRepositoryAlgebra[F],
    validation: NewsValidationAlgebra[F]
)(implicit F: Monad[F]) {

  def create(newsItem: NewsItem): EitherT[F, AlreadyExistsError.type, Unit] =
    for {
      _ <- validation.doesNotExist(newsItem)
      _ <- EitherT.liftF(repository.create(newsItem))
    } yield ()

  def get(link: String): EitherT[F, NotFoundError.type, NewsItem] =
    EitherT.fromOptionF(repository.get(link), NotFoundError)

  def list(pageSize: Int, offset: Int): F[List[NewsItem]] = // TODO: may be EitherT with NonEmptyList
    repository.list(pageSize, offset)
}

object NewsService {
  def apply[F[_]](repository: NewsRepositoryAlgebra[F], validation: NewsValidationAlgebra[F])(implicit F: Monad[F]): NewsService[F] =
    new NewsService[F](repository, validation)
}
