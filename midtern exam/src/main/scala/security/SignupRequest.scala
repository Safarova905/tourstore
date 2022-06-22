package security

import model.{Role, User}
import tsec.passwordhashers.PasswordHash

final case class SignupRequest(
                                username: String,
                                password: String,
                                role: Role,
                              ) {
  def asUser[A](hashedPassword: PasswordHash[A]): User = User(
    username,
    hashedPassword,
    role = role,
  )
}
