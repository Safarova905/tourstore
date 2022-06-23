package validation

import cats.data.EitherT
import model.{Tour, User}

trait TourValidationAlgebra[F[_]] {
  def doesNotExist(tour: Tour): EitherT[F, TourAlreadyExistsError, Unit]

//  def exists(userId: Option[Long]): EitherT[F, TourNotFoundError.type, Unit]
}
