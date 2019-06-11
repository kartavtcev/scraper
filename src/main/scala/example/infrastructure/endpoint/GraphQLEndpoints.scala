package example.infrastructure.endpoint

import java.util.concurrent._

import cats.data._
import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.circe.Json
import io.circe.jawn._
import io.circe.optics.JsonPath._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.{QueryParser, SyntaxError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

import example.domain.News.NewsService

object GraphQLEndpoints extends Http4sDsl[IO] {
  val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

  def graphQLEndpoint(newsService: NewsService[IO])(implicit ec: ExecutionContext, cs: ContextShift[IO]) = {

    def routes = HttpRoutes.of[IO] {
      case request @ GET -> Root ⇒ {
        val route: OptionT[IO, Response[IO]] = StaticFile.fromResource("/assets/graphiql.html", blockingEc, Some(request))
        val result: IO[Response[IO]] = route.getOrElseF(NotFound())
        result
      }

      case request @ POST -> Root / "graphql" ⇒
        request.as[Json].flatMap { body ⇒
          val query = root.query.string.getOption(body)
          val operationName = root.operationName.string.getOption(body)
          val variablesStr = root.variables.string.getOption(body)

          def execute = query.map(QueryParser.parse(_)) match {
            case Some(Success(ast)) ⇒
              variablesStr.map(parse) match {
                case Some(Left(error)) ⇒ Future.successful(BadRequest(formatError(error)))
                case Some(Right(json)) ⇒ executeGraphQL(newsService)(ast, operationName, json)
                case None ⇒ executeGraphQL(newsService)(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj())
              }
            case Some(Failure(error)) ⇒ Future.successful(BadRequest(formatError(error)))
            case None ⇒ Future.successful(BadRequest(formatError("No query to execute")))
          }

          IO.fromFuture(IO(execute)).flatten
        }
    }

    Router("/" -> routes)
  }

  def executeGraphQL(newsService: NewsService[IO])(query: Document, operationName: Option[String], variables: Json)(
      implicit ec: ExecutionContext) =
    GraphQLUtil
      .executeGraphQL(newsService)(query, operationName, variables)
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
        case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
      }

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError ⇒
      Json.obj(
        "errors" → Json.arr(Json.obj(
          "message" → Json.fromString(syntaxError.getMessage),
          "locations" → Json.arr(Json.obj(
            "line" → Json.fromBigInt(syntaxError.originalError.position.line),
            "column" → Json.fromBigInt(syntaxError.originalError.position.column)))
        )))
    case NonFatal(e) ⇒
      formatError(e.getMessage)
    case e ⇒
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" → Json.arr(Json.obj("message" → Json.fromString(message))))
}