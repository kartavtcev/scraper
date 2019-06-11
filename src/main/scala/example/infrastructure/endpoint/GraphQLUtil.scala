package example.infrastructure.endpoint

import cats.effect.IO
import example.domain.News.NewsService
import io.circe.Json
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.circe._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

// TODO: refactor this IO to F type parameter
//class SchemaDefinition[F[_]: IO](newsService: NewsService[F]){


object GraphQLUtil {
  def executeGraphQL(newsService: NewsService[IO])(query: Document, operationName: Option[String], variables: Json) =
    Executor.execute(SchemaDefinition.schema, query, newsService,
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName,
      exceptionHandler = exceptionHandler)//,
      //deferredResolver = DeferredResolver.fetchers(SchemaDefinition.characters))
/*
  def executeAndPrintGraphQL(query: String) =
    QueryParser.parse(query) match {
      case Success(doc) ⇒
        println(Await.result(executeGraphQL(doc, None, Json.obj()), 10 seconds).spaces2)
      case Failure(error) ⇒
        Console.err.print(error.getMessage())
    }
*/
  val exceptionHandler = ExceptionHandler {
    case (_, e) ⇒ HandledException(e.getMessage)
  }
}