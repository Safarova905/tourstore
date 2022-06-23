package repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.circe.parser.decode
import io.circe.syntax._
import model.{Role, Tour, User}
import repository.{TourRepositoryAlgebra, UserRepositoryAlgebra}
import tsec.authentication.IdentityStore
import repository.doobie.SQLPagination.paginate

private object TourSQL {
  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def insert(tour: Tour): Update0 = sql"""
    INSERT INTO TOURS (SOURCE, DESTINATION, COST, DESCRIPTION)
    VALUES (${tour.source}, ${tour.destination}, ${tour.cost}, ${tour.description})
  """.update

//  def update(user: User, id: Long): Update0 = sql"""
//    UPDATE USERS
//    SET USER_NAME = ${user.username}, HASH = ${user.passwordHash}, ROLE = ${user.role}
//    WHERE ID = $id
//  """.update
//
  def select(tourId: Long): Query0[Tour] = sql"""
    SELECT SOURCE, DESTINATION, COST, DESCRIPTION
    FROM TOURS
    WHERE ID = $tourId
  """.query

  def bysourceanddestination(source: String, destination: String): Query0[Tour] = sql"""
    SELECT SOURCE, DESTINATION, COST, DESCRIPTION
    FROM TOURS
    WHERE SOURCE = $source
        AND DESTINATION = $destination
  """.query[Tour]
//
//  def delete(userId: Long): Update0 = sql"""
//    DELETE FROM USERS WHERE ID = $userId
//  """.update

  val selectAll: Query0[Tour] = sql"""
    SELECT SOURCE, DESTINATION, COST, DESCRIPTION
    FROM TOURS
  """.query
}

class DoobieTourRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends TourRepositoryAlgebra[F]
    with IdentityStore[F, Long, Tour] { self =>
  import TourSQL._

  def create(tour: Tour): F[Tour] =
    insert(tour).withUniqueGeneratedKeys[Long]("id").map(id => tour.copy(id = id.some)).transact(xa)

//  def update(user: User): OptionT[F, User] =
//    OptionT.fromOption[F](user.id).semiflatMap { id =>
//      UserSQL.update(user, id).run.transact(xa).as(user)
//    }
//
  def get(tourId: Long): OptionT[F, Tour] = OptionT(select(tourId).option.transact(xa))

  def findBySourceAndDestination(source: String, destination: String): OptionT[F, Tour] =
    OptionT(bysourceanddestination(source, destination).option.transact(xa))
//
//  def delete(userId: Long): OptionT[F, User] =
//    get(userId).semiflatMap(user => UserSQL.delete(userId).run.transact(xa).as(user))
//
//  def deleteByUsername(username: String): OptionT[F, User] =
//    findByUsername(username).mapFilter(_.id).flatMap(delete)

  def list(pageSize: Int, offset: Int): F[List[Tour]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieTourRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieTourRepositoryInterpreter[F] =
    new DoobieTourRepositoryInterpreter(xa)
}

