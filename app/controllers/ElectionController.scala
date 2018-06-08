package controllers

import java.net.URLDecoder
import java.util.Date

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.ElectionData
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.Election._
import models._
import models.swagger.{BallotList, ElectionList, ResponseMessage, VoterList}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{ElectionService, UserService}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Api(value = "Election")
class ElectionController @Inject()(components: ControllerComponents,
                                   userService: UserService,
                                   configuration: Configuration,
                                   silhouette: Silhouette[DefaultEnv],
                                   electionService: ElectionService,
                                   clock: Clock,
                                   mailerClient: MailerClient,
                                   credentialsProvider: CredentialsProvider,
                                   authInfoRepository: AuthInfoRepository,
                                   passwordHasherRegistry: PasswordHasherRegistry,
                                   messagesApi: MessagesApi)
                                  (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  @ApiOperation(value = "Creates a new election for the user", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Election data",
        required = true,
        dataType = "formatters.json.ElectionData",
        paramType = "body"
      )
    )
  )
  def createElection() = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[ElectionData].map { data =>
      userService.retrieve(request.authenticator.loginInfo).flatMap {
        case Some(user) =>
          electionService.save(
            Election(
              None,
              name = data.name,
              description = data.description,
              creatorName = s"${user.firstName} ${user.lastName}",
              creatorEmail = user.email,
              start = data.startingDate,
              end = data.endingDate,
              realtimeResult = data.isRealTime,
              votingAlgo = data.votingAlgo,
              candidates = data.candidates,
              ballotVisibility = data.ballotVisibility,
              voterListVisibility = data.voterListVisibility,
              isInvite = data.isInvite,
              isCompleted = false,
              isStarted = false,
              createdTime = new Date(),
              adminLink = "",
              inviteCode = s"${Random.alphanumeric take 10 mkString ("")}",
              ballot = List.empty[Ballot],
              voterList = List.empty[Voter],
              winners = List.empty[Winner],
              isCounted = false,
              noVacancies = data.noVacancies,
              loginInfo = Some(request.authenticator.loginInfo)
            )
          ).flatMap(_ => Future.successful(Ok("Election created successfuly")))

        case None => Future.successful(BadRequest("Failed to create election"))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Get election data with specified ID", response = classOf[Election])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getElection(id: String) = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap {
      case Some(_) => electionService.retrieve(id).flatMap {
        case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
          && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
          Future.successful(Ok(Json.toJson(election.copy(loginInfo = None))))
        case None => Future.successful(NotFound("Election not found"))
      }
    }
  }

  @ApiOperation(value = "Edit election with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Election data",
        required = true,
        dataType = "formatters.json.ElectionData",
        paramType = "body"
      )
    )
  )
  def editElection(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[ElectionData].map { data =>
      userService.retrieve(request.authenticator.loginInfo).flatMap {
        case Some(user) => electionService.retrieve(id).flatMap {
          case Some(election) if election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
            && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey
            && !election.isStarted =>
            electionService.update(
              Election(
                election.id,
                name = data.name,
                description = data.description,
                creatorName = s"${user.firstName} ${user.lastName}",
                creatorEmail = user.email,
                start = data.startingDate,
                end = data.endingDate,
                realtimeResult = data.isRealTime,
                votingAlgo = data.votingAlgo,
                candidates = data.candidates,
                ballotVisibility = data.ballotVisibility,
                voterListVisibility = data.voterListVisibility,
                isInvite = data.isInvite,
                isCompleted = false,
                isStarted = false,
                createdTime = election.createdTime,
                adminLink = election.adminLink,
                inviteCode = election.inviteCode,
                ballot = List.empty[Ballot],
                voterList = List.empty[Voter],
                winners = List.empty[Winner],
                isCounted = false,
                noVacancies = data.noVacancies,
                loginInfo = election.loginInfo
              )
            ).flatMap(_ => Future.successful(Ok("Election updated successfully")))

          case None => Future.successful(NotFound("Election not found"))
        }
        case None => Future.successful(BadRequest("Failed to update election"))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete election with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def deleteElection(id: String) = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap {
      case Some(_) => electionService.retrieve(id).flatMap {
        case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
          && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
          electionService.delete(id).flatMap(_ => Future.successful(Ok("Election deleted")))
        case _ => Future.successful(NotFound("Election not found"))
      }
    }
  }

  @ApiOperation(value = "Get elections created by user", response = classOf[ElectionList])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getUserElections() = silhouette.SecuredAction.async { implicit request =>
    electionService.userElectionList(request.authenticator.loginInfo).flatMap {elections =>
      //Remove login info from elections and send the elections
      val electionsInJson = ListBuffer[Election]()
      elections.foreach(e => electionsInJson += e.copy(loginInfo = None))
      Future.successful(Ok(Json.obj("elections" -> electionsInJson.toList)))
    }
  }

  @ApiOperation(value = "Add new voter for election with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Voter data",
        required = true,
        dataType = "models.Voter",
        paramType = "body"
      )
    )
  )
  def addVoter(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Voter].map { data =>
      userService.retrieve(request.authenticator.loginInfo).flatMap {
        case Some(_) => electionService.retrieve(id).flatMap {
          case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
            && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
            for {
              isAdded <- electionService.addVoter(id, data)
            } yield {
              if (isAdded) {
                val url = routes.VoteController.vote(election.id.get).absoluteURL()
                val passCode  = PassCodeGenerator.encrypt(election.inviteCode, data.email)
                mailerClient.send(Email(
                  subject = Messages("email.vote.subject"),
                  from = Messages("email.from"),
                  to = Seq(URLDecoder.decode(data.email, "UTF-8")),
                  bodyText = Some(views.txt.emails.vote(election, data, url, passCode).body),
                  bodyHtml = Some(views.html.emails.vote(election, data, url, passCode).body)
                ))
                Future.successful(Ok(s"Voter added to election ${election.name}"))
              } else {
                Future.successful(InternalServerError("Failed to add voter"))
              }

            }
            electionService.addVoter(id, data).flatMap(_ =>
              Future.successful(Ok(s"Voter added to election ${election.name}")))
          case _ => Future.successful(NotFound("Election not found"))
        }
        case None => Future.successful(BadRequest("Failed to add voter"))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Get list of voters for election with specified ID", response = classOf[VoterList])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getVoters(id: String) = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap {
      case Some(_) => electionService.retrieve(id).flatMap {
        case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
          && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
          electionService.getVoterList(id).flatMap(voters => Future.successful(Ok(Json.obj("voters" -> voters))))
        case _ => Future.successful(NotFound("Election with specified ID not found"))
      }
    }
  }

  @ApiOperation(value = "Add list of voters to election", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Voters list",
        required = true,
        allowMultiple = true,
        paramType = "body",
        dataType = "models.swagger.VoterList"

      )
    )
  )
  def addVoters(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[List[Voter]].map { data =>
      userService.retrieve(request.authenticator.loginInfo).flatMap {
        case Some(_) => electionService.retrieve(id).flatMap {
          case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
            && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
            electionService.addVoters(id, data).flatMap{filteredVoters =>
              filteredVoters.foreach{ v =>
                val url = routes.VoteController.vote(election.id.get).absoluteURL()
                val passCode  = PassCodeGenerator.encrypt(election.inviteCode, v.email)
                mailerClient.send(Email(
                  subject = Messages("email.vote.subject"),
                  from = Messages("email.from"),
                  to = Seq(URLDecoder.decode(v.email, "UTF-8")),
                  bodyText = Some(views.txt.emails.vote(election, v, url, passCode).body),
                  bodyHtml = Some(views.html.emails.vote(election, v, url, passCode).body)
                ))
              }
              Future.successful(Ok(s"Voters list updated for election ${election.name}"))
            }
          case _ => Future.successful(NotFound("Election not found"))
        }
        case None => Future.successful(BadRequest("Failed to add voters"))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Get ballots for elections with specified id", response = classOf[BallotList])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getBallots(id: String) = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap {
      case Some(_) => electionService.retrieve(id).flatMap {
        case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
          && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
          electionService.getBallots(id).flatMap(ballots => Future.successful(Ok(Json.obj("ballots" -> ballots))))
        case _ => Future.successful(NotFound("Election not found"))
      }
      case None => Future.successful(BadRequest("Failed to get ballots"))
    }
  }
}
