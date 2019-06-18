package example.infrastructure.endpoint

import cats.effect.IO
import example.domain.News.{NewsItem, NewsService}
import sangria.schema._

object SchemaDefinition {

  // Might define with macros yet Scala 3 ...
  // TODO: auth

  val NewsItemType = ObjectType(
    "NewsItem",
    fields[Unit, NewsItem](
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link)
    ))

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 20)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val SubscriptionType = ObjectType(
    "Subscription",
    fields[NewsService[IO], Unit](
      Field(
        "news", ListType(NewsItemType),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.list(ctx arg LimitArg, ctx arg OffsetArg).unsafeToFuture())
    ))

  val schema = Schema(SubscriptionType)
}
