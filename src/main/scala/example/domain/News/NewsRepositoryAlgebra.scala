package example.domain.News

trait NewsRepositoryAlgebra[F[_]] {
  def create(newsItem: NewsItem): F[Unit]
  def get(link: String): F[Option[NewsItem]]
  def list(pageSize: Int, offset: Int): F[List[NewsItem]]
}