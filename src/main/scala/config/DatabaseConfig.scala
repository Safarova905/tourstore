package config

import cats.syntax.functor._
import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext

import org.flywaydb.core.Flyway

import java.sql.{Connection,DriverManager}

object DatabaseConfig {
  case class DatabaseConnectionsConfig(poolSize: Int)
  case class DatabaseConfig(
                             url: String,
                             driver: String,
                             user: String,
                             password: String,
                             connections: DatabaseConnectionsConfig,
                           )

  object DatabaseConfig {
    def dbTransactor[F[_]: Async: ContextShift](
                                                 dbc: DatabaseConfig,
                                                 connEc: ExecutionContext,
                                                 blocker: Blocker,
                                               ): Resource[F, HikariTransactor[F]] =
      HikariTransactor
        .newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)

    /**
     * Runs the flyway migrations against the target database
     */
    def initializeDb[F[_]](cfg: DatabaseConfig)(implicit S: Sync[F]): F[Unit] =
      S.delay {
//                val fw: Flyway =
//                  Flyway
//                    .configure()
//                    .dataSource(cfg.url, cfg.user, cfg.password)
//                    .load()
//                fw.migrate()
        //        val mysql: MySql
//        Class.forName(cfg.driver)
//        DriverManager.getConnection(cfg.url, cfg.user, cfg.password)
      }.as(())
  }
}
