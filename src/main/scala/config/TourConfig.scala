package config

import config.DatabaseConfig.DatabaseConfig

final case class ServerConfig(host: String, port: Int)
final case class TourConfig(db: DatabaseConfig, server: ServerConfig)
