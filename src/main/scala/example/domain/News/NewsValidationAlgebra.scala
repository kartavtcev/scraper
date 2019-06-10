package example.domain.News

import cats.data.EitherT
import example.domain.AlreadyExistsError

trait NewsValidationAlgebra[F[_]] {
  def doesNotExist(news: NewsItem): EitherT[F, AlreadyExistsError.type, Unit]
}