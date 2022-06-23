package dto

final case class LoginRequest(
                               username: String,
                               passwordHash: String,
                             )
