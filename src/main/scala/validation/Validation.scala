package validation

import model.{Tour, User}

sealed trait ValidationError extends Product with Serializable
case object UserNotFoundError extends ValidationError
case class UserAlreadyExistsError(user: User) extends ValidationError
case class UserAuthenticationFailedError(userName: String) extends ValidationError
case class TourAlreadyExistsError(tour: Tour) extends ValidationError
