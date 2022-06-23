package controller

import cats.data.EitherT
import cats.effect.Sync
import controller.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}
import endpoint.{AuthController, AuthService}
import dto.AddTourRequest
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import model.{Tour, User}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import security.Auth
import service.TourService
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler}
import tsec.jwt.algorithms.JWTMacAlgo
import validation.TourAlreadyExistsError
import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import controller.endpoint.AuthController
import dto.{LoginRequest, SignupRequest}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import model.User
import security.Auth
import validation.{UserAlreadyExistsError, UserAuthenticationFailedError, UserNotFoundError}
import service.UserService
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import tsec.authentication._

class TourController[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  implicit val tourDecoder: EntityDecoder[F, Tour] = jsonOf
  implicit val addTourReqDecoder: EntityDecoder[F, AddTourRequest] = jsonOf

//  implicit val listToursReqDecoder: EntityDecoder[F, ListToursRequest] = jsonOf

  private def addTourEndpoint(
                             tourService: TourService[F],
//                             auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]],
                           ): AuthController[F, Auth] = {
//                             ): HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "add" asAuthed _ =>
//    case (POST -> Root / "add" / source / destination / cost) =>
      val action = for {
        addTour <- EitherT.liftF(req.request.as[AddTourRequest])
        source = addTour.source
        destination = addTour.destination
        cost = addTour.cost
        tour <- tourService.createTour(Tour(source, destination, cost, ""))
      } yield (tour)

//      Ok(action.value.asJson)

      action.value.flatMap {
        case Right(tour) => Ok(tour.asJson)
        case Left(TourAlreadyExistsError(tour)) =>
          BadRequest(s"Tour with source '${tour.source}' and destination '${tour.destination}' already exists")
      }
    }

//  private def listToursEndpoint(tourService: TourService[F]): AuthController[F, Auth] = {
private def listToursEndpoint(tourService: TourService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
//    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
  case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) =>
      for {
//        Ok(tourService.list(pageSize.getOrElse(10), offset.getOrElse(0)).asJson)
        retrieved <- tourService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
                 tourService: TourService[F],
//                 cryptService: PasswordHasher[F, A],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        addTourEndpoint(tourService)
//          .orElse(listToursEndpoint(tourService))
      }

    val unauthEndpoints =
      listToursEndpoint(tourService)
//        .orElse(addTourEndpoint(tourService))

    unauthEndpoints <+> auth.liftService(authEndpoints)
//    auth.liftService(authEndpoints)
  }
}

object TourController {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
                                                  tourService: TourService[F],
                                                  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                                ): HttpRoutes[F] =
    new TourController[F, Auth].endpoints(tourService, auth)
}
