package example.domain.News

final case class NewsItem(
    title: String,
    link: String
)

final case class Subscription(
    news: List[NewsItem]
)