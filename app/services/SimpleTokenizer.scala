package services

import java.io.Serializable
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.spark.SparkContext

import scala.collection.JavaConversions._

private class SimpleTokenizer(sc: SparkContext, stopwordFile: String) extends Serializable
{
  private[this] val minWordLength = 1

  private[this] val stopwords: Set[String] =
    if (stopwordFile.isEmpty) {
      Set.empty[String]
    } else {
      val stopwordText = sc.textFile(stopwordFile).collect()
      stopwordText.flatMap(_.stripMargin.split("\\s+")).toSet
    }

  def getWords(text: String): IndexedSeq[String] = {
    val props = new Properties
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma")

    val pipeline = new StanfordCoreNLP(props)

    val doc = new Annotation(text)
    pipeline.annotate(doc)

    val tokens: List[CoreLabel] = doc.get(classOf[TokensAnnotation]).toList

    tokens.map(_.lemma)
      .filter(word => !stopwords.contains(word) && word.length > minWordLength)
      .toIndexedSeq
  }
}