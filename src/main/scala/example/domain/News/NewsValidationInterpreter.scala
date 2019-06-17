package example.domain.News

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import example.domain._

class NewsValidationInterpreter[F[_]: Applicative](repository: NewsRepositoryAlgebra[F]) extends NewsValidationAlgebra[F] {
  def doesNotExist(news: NewsItem): EitherT[F, AlreadyExistsError.type, Unit] = EitherT { // Many layers of mtl would degrade performance.
    repository.get(news.link).map {
      case Some(_) => Left(AlreadyExistsError)
      case None => Right(())
    }
  }
}

object NewsValidationInterpreter {
  def apply[F[_]: Applicative](repository: NewsRepositoryAlgebra[F]) =
    new NewsValidationInterpreter[F](repository)
}
