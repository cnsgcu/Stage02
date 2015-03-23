package controllers

import global.Config
import global.services.NLP

import play.api.mvc._
import play.api.Logger
import play.api.libs.json._

import scala.collection.JavaConversions._

import api.rottentomatoes.{FetchReviews, FetchMovies}

object Application extends Controller
{
  private[this] implicit val pairWrites =
    new Writes[(String, Int)] {
      def writes(pair: (String, Int)): JsValue =
        pair match {
          case (opinion, score) =>
            JsObject(
              Seq(
                "score" -> JsNumber(score),
                "sentence" -> JsString(opinion)
              )
            )
        }
    }

  private[this] implicit val pairSetWrites =
    new Writes[Set[(String, Int)]] {
      def writes(set: Set[(String, Int)]): JsValue = JsArray(set.map(Json.toJson(_)).toSeq)
    }

  private[this] implicit val sentimentWrites =
    new Writes[Map[String, Set[(String, Int)]]] {
      def writes(sentiment: Map[String, Set[(String, Int)]]): JsValue =
        JsObject(
          sentiment.map {
            case (topic, opinions) => topic -> Json.toJson(opinions)
          }.toSeq
        )
    }

  private[this] implicit val reviewWrites =
    new Writes[(String, Map[String, Set[(String, Int)]])] {
      def writes(pair: (String, Map[String, Set[(String, Int)]])): JsValue =
        pair match {
          case (review, opinion) =>
            JsObject(
              Seq(
                "review" -> JsString(review),
                "opinion" -> Json.toJson(opinion)
              )
            )
        }
    }

  def index = Action {
    Ok(views.html.index())
  }

  def search(name: String) = Action {
    val fetchMovies = new FetchMovies
    val movies = fetchMovies.getMovies(name)

    Ok(Config.gson.toJson(movies))
  }

  def sentiment = Action(parse.json) { request =>
    (request.body \ "id").asOpt[String] map {
      movieId =>
        val fetchReviews = new FetchReviews
        val reviews = for (review <- fetchReviews.getReviews(movieId))
          yield (review.getQuote, NLP.sentimentAnalyze(review.getQuote))

        Logger.info(s"Movie $movieId has ${reviews.length} reviews.")

        Ok(Json.toJson(reviews.filter(_._2.nonEmpty).toSeq))
    } getOrElse {
      BadRequest
    }
  }
}