package controllers

import api.rottentomatoes.models.MovieSearchResponse
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

  private[this] implicit val reviewWrites =
    new Writes[(String, Map[String, Set[(String, Int)]])] {
      def writes(pair: (String, Map[String, Set[(String, Int)]])): JsValue =
        pair match {
          case (review, sentiments) =>
            val jsonSentiments = JsArray(
              sentiments.map {
                case (topic, opinion) =>
                  JsObject(
                    Seq(
                      "topic" -> JsString(topic),
                      "opinions" -> Json.toJson(opinion)
                    )
                  )
              }.toSeq
            )

            JsObject(
              Seq(
                "review" -> JsString(review),
                "sentiments" -> jsonSentiments
              )
            )
        }
    }

  def index = Action {
    Ok(views.html.index())
  }

  def search(name: String) = Action {
    val (fetchMovies, resp) = (new FetchMovies, new MovieSearchResponse)

    resp.setQuery(name)
    resp.setMovies(fetchMovies.getMovies(name))

    Ok(Config.gson.toJson(resp))
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