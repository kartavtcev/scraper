package example.config

import cats.effect.Sync
import cats.syntax.functor._
import org.flywaydb.core.Flyway

case class DatabaseConfig(url: String, driver: String, user: String, password: String)

object DatabaseConfig {
  // Runs the flyway migrations against the target database
  def initializeDb[F[_]](cfg : DatabaseConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway = {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      }
      fw.migrate()
    }.as(())
}