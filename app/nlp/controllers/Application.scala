package nlp.controllers

import com.api.rottentomatoes.{FetchReviews, FetchMovies}
import play.api.mvc._
import play.api.libs.json._
import scala.collection.JavaConversions._

import global.services.NLP

object Application extends Controller
{
  private[this] implicit val pairWrites = new Writes[(String, Int)] {
    def writes(pair: (String, Int)): JsValue =
      pair match {
        case (opinion, score) =>
          JsObject(
            Seq(
              "score" -> JsNumber(score),
              "opinion" -> JsString(opinion)
            )
          )
      }
  }

  private[this] implicit val pairSetWrites = new Writes[Set[(String, Int)]] {
    def writes(set: Set[(String, Int)]): JsValue = JsArray(set.map(Json.toJson(_)).toSeq)
  }

  private[this] implicit val sentimentWrites = new Writes[Map[String, Set[(String, Int)]]] {
    def writes(sentiment: Map[String, Set[(String, Int)]]): JsValue =
      JsObject(
        sentiment.map {
          case (topic, opinions) => topic -> Json.toJson(opinions)
        }.toSeq
      )
  }

  def index = Action {
    Ok(nlp.views.html.index())
  }

  def sentiment = Action(parse.json) { request =>
    (request.body \ "movie").asOpt[String] map {
      name =>
        val fetchMovies = new FetchMovies
        val movies = fetchMovies.getMovies("king").toList

        val fetchReviews = new FetchReviews
        val reviews = fetchReviews.getReviews(movies.head.getmId()).toList

        for (i <- 0 until reviews.size) {
          println(s"$i - ${reviews.get(i).getQuote}\n${NLP.sentimentAnalyze(reviews.get(i).getQuote)}\n")
        }

        val message =
          """
            |Tim Burton's Batman wasn't a bad movie.
            |It was entertaining and had a nice comic-book feel to it, but it struck me as being quite formulaic.
            |Some scenes worked for me, but others felt forced and oddly out of place.
            |I imagine Tim Burton had some of the actors, like Jack Palance, overact on purpose, to give the movie a similar feel to the Batman TV show from the 1960s; however, the result felt more like a high-school play than a movie.
          """.stripMargin

        Ok(Json.toJson(NLP.sentimentAnalyze(message)))
    } getOrElse {
      BadRequest
    }
  }
}