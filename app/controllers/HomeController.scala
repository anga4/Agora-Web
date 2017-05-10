package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    Ok(views.html.home())
  }

  def login = Action {implicit request =>
  Ok(views.html.login())
  }

  def vote = Action { implicit request =>
  Ok (views.html.vote())
  }

  def addelection = Action { implicit request =>
  Ok (views.html.addElection())

  }

}