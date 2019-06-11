package example.domain.News

// ContextShift or parallel for repo access.
trait NewsRepositoryAlgebra[F[_]] {
  def create(newsItem: NewsItem): F[Unit]
  def get(link: String): F[Option[NewsItem]]
  // TODO: paginate implementation
  def list(pageSize: Int, offset: Int): F[List[NewsItem]]
}
