package repository

import cats.data.OptionT
import model.User

trait UserRepositoryAlgebra[F[_]] {
  def create(user: User): F[User]

  def update(user: User): OptionT[F, User]

  def get(userId: Long): OptionT[F, User]

  def delete(userId: Long): OptionT[F, User]

  def findByUsername(userName: String): OptionT[F, User]

  def deleteByUsername(userName: String): OptionT[F, User]

  def list(pageSize: Int, offset: Int): F[List[User]]
}
