package repository

import cats.data.OptionT
import model.{Tour, User}

trait TourRepositoryAlgebra[F[_]] {
  def create(tour: Tour): F[Tour]

  def findBySourceAndDestination(source: String, destination: String) : OptionT[F, Tour]

//  def update(user: User): OptionT[F, User]
//
//  def get(userId: Long): OptionT[F, User]
//
//  def delete(userId: Long): OptionT[F, User]
//
//  def findByUsername(userName: String): OptionT[F, User]
//
//  def deleteByUsername(userName: String): OptionT[F, User]

  def list(pageSize: Int, offset: Int): F[List[Tour]]
}
