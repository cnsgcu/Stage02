package controllers

import api.rottentomatoes.models.MovieSearchResponse
import global.Config
import play.api.libs.EventSource
import play.api.libs.iteratee.{Enumeratee, Enumerator}
import services.NLP
import play.api.mvc._
import play.api.{Play, Logger}
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

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
                "score"    -> JsNumber(score),
                "sentence" -> JsString(opinion)
              )
            )
        }
    }

  private[this] implicit val sentimentWrites =
    new Writes[Map[String, Set[(String, Int)]]] {
      def writes(sentiments: Map[String, Set[(String, Int)]]): JsValue =
        JsArray(
          sentiments.map {
            case (topic, opinion) =>
              JsObject(
                Seq(
                  "topic"    -> JsString(topic),
                  "opinions" -> Json.toJson(opinion)
                )
              )
          }.toSeq
        )
    }

  private[this] implicit val reviewWrites =
    new Writes[(String, Map[String, Set[(String, Int)]])] {
      def writes(pair: (String, Map[String, Set[(String, Int)]])): JsValue =
        pair match {
          case (review, sentiments) =>
            JsObject(
              Seq(
                "review"     -> JsString(review),
                "sentiments" -> Json.toJson(sentiments)
              )
            )
        }
    }

  private[this] val reviewFetcher = new FetchReviews

  def index = Action {
    Ok(views.html.index())
  }

  def search(name: String) = Action {
    val (fetchMovies, resp) = (new FetchMovies, new MovieSearchResponse)

    resp.setQuery(name)
    resp.setMovies(fetchMovies.getMovies(name))

    Ok(Config.gson.toJson(resp))
  }

  def sentimentStream(movieId: String) = Action {
    val reviews = reviewFetcher.getReviews(movieId)
    val reviewStream = reviews.toStream.map(r => (r.getQuote, NLP.sentimentAnalyze(r.getQuote)))
    val rEnum = Enumerator.unfold(reviewStream)(rs => Some(rs.tail, Json.toJson(rs.head))) through Enumeratee.take(reviews.size())

    Ok.chunked(rEnum andThen Enumerator(JsString("EOS")) through EventSource())
      .as("text/event-stream")
  }

  def sentiment = Action(parse.json) { request =>
    (request.body \ "id").asOpt[String] map {
      movieId =>
        val reviews = for (review <- reviewFetcher.getReviews(movieId))
          yield (review.getQuote, NLP.sentimentAnalyze(review.getQuote))

        Logger.info(s"Movie $movieId has ${reviews.length} reviews.")

        Ok(Json.toJson(reviews.filter(_._2.nonEmpty).toSeq))
    } getOrElse {
      BadRequest
    }
  }
}