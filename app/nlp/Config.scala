package nlp

import java.util.Properties

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import play.api.GlobalSettings

object Config extends GlobalSettings
{
  private[this] val props = new Properties
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment")

  val pipeline = new StanfordCoreNLP(props)
}