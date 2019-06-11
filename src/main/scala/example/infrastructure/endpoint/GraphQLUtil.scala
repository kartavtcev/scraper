package example.infrastructure.endpoint

import cats.effect.IO
import io.circe.Json
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.circe._
import example.domain.News.NewsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object GraphQLUtil {
  def executeGraphQL(newsService: NewsService[IO])(query: Document, operationName: Option[String], variables: Json) =
    Executor.execute(
      SchemaDefinition.schema,
      query,
      newsService,
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName,
      exceptionHandler = exceptionHandler
    )
  val exceptionHandler = ExceptionHandler {
    case (_, e) ⇒ HandledException(e.getMessage)
  }
}
