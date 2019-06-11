package example.infrastructure.endpoint

import cats.data.Validated.Valid
import cats.data._
import cats.effect.{ContextShift, IO, Sync}
import example.domain.News.NewsService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.Message._
import org.http4s.dsl.io._
import org.http4s.server.blaze._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import java.util.concurrent._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import io.circe.Json
import io.circe.jawn._
import io.circe.optics.JsonPath._
import org.http4s.server.Router
import sangria.marshalling.circe._

import cats.implicits._
import org.http4s.implicits._

class GraphQLEndpoints[F[_]: Sync](implicit F: ContextShift[F]) extends Http4sDsl[F] {
  val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def graphQLEndpoint(newsService: NewsService[IO]) = {

    def routes1[F[_]] = HttpRoutes.of[F] {
      case request @ GET -> Root ⇒ {
        val route: OptionT[F, Response[F]] = StaticFile.fromResource("/assets/graphiql.html", blockingEc, Some(request))
        val result: F[Response[F]] = route.getOrElseF(NotFound())
        result
      }
    }

    val routes2 = HttpRoutes.of[IO] {

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

    val routes: HttpRoutes[IO] = routes1[IO] <+> routes2

    Router("/" -> routes)
  }

  def executeGraphQL(newsService: NewsService[IO])(query: Document, operationName: Option[String], variables: Json) =
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
  /*
  def start() = {
    BlazeBuilder[IO].bindHttp(8080, "0.0.0.0").mountService(service, "/").start.map(Util.awaitServerShutdown).unsafeRunSync()
  }

  // NEW
  val serverBuilder = BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(httpApp)
 */
}

object GraphQLEndpoints {
  def endpoints(newsService: NewsService[IO]): HttpRoutes[IO] =
    new GraphQLEndpoints[IO].graphQLEndpoint(newsService)
}
