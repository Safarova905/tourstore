package repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.circe.parser.decode
import io.circe.syntax._
import model.{Role, User}
import repository.UserRepositoryAlgebra
import tsec.authentication.IdentityStore
import repository.doobie.SQLPagination.paginate

private object UserSQL {
  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USER_NAME, HASH, ROLE)
    VALUES (${user.username}, ${user.passwordHash}, ${user.role})
  """.update

  def update(user: User, id: Long): Update0 = sql"""
    UPDATE USERS
    SET USER_NAME = ${user.username}, HASH = ${user.passwordHash}, ROLE = ${user.role}
    WHERE ID = $id
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT USER_NAME, HASH, ID, ROLE
    FROM USERS
    WHERE ID = $userId
  """.query

  def byusername(username: String): Query0[User] = sql"""
    SELECT USER_NAME, HASH, ID, ROLE
    FROM USERS
    WHERE USER_NAME = $username
  """.query[User]

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM USERS WHERE ID = $userId
  """.update

  val selectAll: Query0[User] = sql"""
    SELECT USER_NAME, HASH, ID, ROLE
    FROM USERS
  """.query
}

class DoobieUserRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends UserRepositoryAlgebra[F]
    with IdentityStore[F, Long, User] { self =>
  import UserSQL._

  def create(user: User): F[User] =
    insert(user).withUniqueGeneratedKeys[Long]("ID").map(id => user.copy(id = id.some)).transact(xa)

  def update(user: User): OptionT[F, User] =
    OptionT.fromOption[F](user.id).semiflatMap { id =>
      UserSQL.update(user, id).run.transact(xa).as(user)
    }

  def get(userId: Long): OptionT[F, User] = OptionT(select(userId).option.transact(xa))

  def findByUsername(username: String): OptionT[F, User] =
    OptionT(byusername(username).option.transact(xa))

  def delete(userId: Long): OptionT[F, User] =
    get(userId).semiflatMap(user => UserSQL.delete(userId).run.transact(xa).as(user))

  def deleteByUsername(username: String): OptionT[F, User] =
    findByUsername(username).mapFilter(_.id).flatMap(delete)

  def list(pageSize: Int, offset: Int): F[List[User]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieUserRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieUserRepositoryInterpreter[F] =
    new DoobieUserRepositoryInterpreter(xa)
}

