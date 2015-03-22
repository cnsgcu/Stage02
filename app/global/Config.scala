package global

import java.util.Properties

import com.google.gson.Gson
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import play.api.GlobalSettings

object Config extends GlobalSettings
{
  private[this] val props = new Properties
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment")

  val gson = new Gson()

  val pipeline = new StanfordCoreNLP(props)
}