package service

import cats.data._
import cats.syntax.functor._
import cats.{Functor, Monad}
import model.{Tour, User}
import repository.{TourRepositoryAlgebra, UserRepositoryAlgebra}
import validation.{TourAlreadyExistsError, TourValidationAlgebra, UserAlreadyExistsError, UserNotFoundError, UserValidationAlgebra}

class TourService[F[_]](tourRepo: TourRepositoryAlgebra[F], validation: TourValidationAlgebra[F]) {
  def createTour(tour: Tour)(implicit M: Monad[F]): EitherT[F, TourAlreadyExistsError, Tour] =
    for {
      _ <- validation.doesNotExist(tour)
      saved <- EitherT.liftF(tourRepo.create(tour))
    } yield saved

//  def getUser(userId: Long)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
//    userRepo.get(userId).toRight(UserNotFoundError)
//
//  def getUserByName(
//                     userName: String,
//                   )(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
//    userRepo.findByUsername(userName).toRight(UserNotFoundError)
//
//  def deleteUser(userId: Long)(implicit F: Functor[F]): F[Unit] =
//    userRepo.delete(userId).value.void
//
//  def deleteByUserName(userName: String)(implicit F: Functor[F]): F[Unit] =
//    userRepo.deleteByUsername(userName).value.void
//
//  def update(user: User)(implicit M: Monad[F]): EitherT[F, UserNotFoundError.type, User] =
//    for {
//      _ <- validation.exists(user.id)
//      saved <- userRepo.update(user).toRight(UserNotFoundError)
//    } yield saved

  def list(pageSize: Int, offset: Int): F[List[Tour]] =
    tourRepo.list(pageSize, offset)
}

object TourService {
  def apply[F[_]](
                   repository: TourRepositoryAlgebra[F],
                   validation: TourValidationAlgebra[F],
                 ): TourService[F] =
    new TourService[F](repository, validation)
}
