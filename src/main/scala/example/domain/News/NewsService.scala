package example.domain.News

import cats.Functor
import cats.data.EitherT
//import cats.syntax.all._

import example.domain.{AlreadyExistsError, NotFoundError}

// The entry point of domain, repos, validation
class NewsService[F[_]](
    repository: NewsRepositoryAlgebra[F],
    validation: NewsValidationAlgebra[F]
) {

  def create(newsItem: NewsItem)(implicit F: Functor[F]): EitherT[F, AlreadyExistsError.type, Unit] =
    for {
      _ <- validation.doesNotExist(newsItem)
      _ = repository.create(newsItem)
    } yield ()

  def get(link: String)(implicit F: Functor[F]): EitherT[F, NotFoundError.type, NewsItem] =
    EitherT.fromOptionF(repository.get(link), NotFoundError)

  def list(pageSize: Int, offset: Int): F[List[NewsItem]] =
    repository.list(pageSize, offset)
}

object NewsService {
  def apply[F[_]](repository: NewsRepositoryAlgebra[F], validation: NewsValidationAlgebra[F]): NewsService[F] =
    new NewsService[F](repository, validation)
}
