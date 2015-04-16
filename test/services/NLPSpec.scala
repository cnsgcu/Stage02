package services

import java.util.Properties

import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import global.Config
import org.specs2.mutable.Specification

import scala.collection.JavaConversions._

class NLPSpec extends Specification
{
  val props = new Properties
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment")

  val pipeline = new StanfordCoreNLP(props)

  "NLP" should {
    "set annotators property" in {
      val doc = Config.pipeline.process("Frequently reactionary, bordering on retrograde, bordering on reprobate. But it's also a tremendous amount of fun.")
      val graph = doc.get(classOf[CorefChainAnnotation]).toMap
      println(graph)

      props.getProperty("annotators") mustEqual "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment"
    }
  }
}
