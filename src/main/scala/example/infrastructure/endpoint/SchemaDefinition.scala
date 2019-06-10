package example.infrastructure.endpoint

import example.domain.News.{NewsItem, NewsService}
import sangria.schema._

class SchemaDefinition[F[_]](newsService: NewsService[F]) {

  // Might define with macros yet Scala 3
  // TODO: change to repository dependency
  // TODO: auth; DELAY
  val news =
    List(
      NewsItem(
        "Acting Budget Chief Seeks Reprieve on Huawei Ban",
        "https://www.wsj.com/articles/acting-budget-chief-seeks-reprieve-on-huawei-ban-11560108418"
      ),
      NewsItem(
        "Rewrite of Bank Rules Makes Little Progress, Frustrating Republicans",
        "https://www.wsj.com/articles/rewrite-of-bank-rules-bogs-down-11560159001"
      )
    )

  val NewsItemType = ObjectType(
    "NewsItem",
    fields[Unit, NewsItem](
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link)
    ))

  val SubscriptionType = ObjectType(
    "Subscription",
    fields[Unit, Unit](
      Field("news", ListType(NewsItemType), resolve = _ => news)
    ))

  val schema = Schema(SubscriptionType)
}
