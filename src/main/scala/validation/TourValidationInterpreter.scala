package validation

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import model.Tour
import repository.TourRepositoryAlgebra

class TourValidationInterpreter[F[_]: Applicative](tourRepo: TourRepositoryAlgebra[F])
  extends TourValidationAlgebra[F] {
  def doesNotExist(tour: Tour): EitherT[F, TourAlreadyExistsError, Unit] =
    tourRepo
      .findBySourceAndDestination(tour.source, tour.destination)
      .map(TourAlreadyExistsError)
      .toLeft(())

//  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit] =
//    userId match {
//      case Some(id) =>
//        userRepo
//          .get(id)
//          .toRight(UserNotFoundError)
//          .void
//      case None =>
//        EitherT.left[Unit](UserNotFoundError.pure[F])
//    }
}

object TourValidationInterpreter {
  def apply[F[_]: Applicative](repo: TourRepositoryAlgebra[F]): TourValidationAlgebra[F] =
    new TourValidationInterpreter[F](repo)
}
