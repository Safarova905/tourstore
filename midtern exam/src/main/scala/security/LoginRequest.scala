package security

import tsec.passwordhashers.PasswordHash

final case class LoginRequest(
                               username: String,
                               passwordHash: String,
                             )
