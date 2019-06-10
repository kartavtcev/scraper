package example.domain.News

import cats.Applicative
import cats.data.EitherT
import cats.implicits._

import example.domain.AlreadyExistsError

class NewsValidationInterpreter[F[_]: Applicative](repository: NewsRepositoryAlgebra[F]) extends NewsValidationAlgebra[F] {
  def doesNotExist(news: NewsItem): EitherT[F, AlreadyExistsError.type, Unit] = EitherT { // Many layers of EitherT would degrade the performance.
    repository.get(news.link).map {
      case Some(_) => Right(())
      case None => Left(AlreadyExistsError)
    }
  }
}

object NewsValidationInterpreter {
  def apply[F[_]: Applicative](repository: NewsRepositoryAlgebra[F]) =
    new NewsValidationInterpreter[F](repository)
}