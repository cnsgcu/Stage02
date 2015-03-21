package global.services

import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.dcoref.CorefChain
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.dcoref.CorefChain.CorefMention
import edu.stanford.nlp.dcoref.Dictionaries.MentionType
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import nlp.Config

import scala.collection.JavaConversions._

object NLP
{
  private[this] def getMentions(graph: Map[Integer, CorefChain]): Iterable[Iterable[CorefMention]] =
    graph filter {
      case (_, node) => node.getMentionMap.size > 1
    } map {
      case(_, node) => node.getMentionMap.values.flatten
    }

  private[this] def getTopics(mentions: Iterable[CorefMention]): List[CorefMention] =
    mentions.filter(_.mentionType != MentionType.PRONOMINAL).toList
            .sortBy(_.mentionSpan.length)

  def sentimentAnalyze(message: String): Map[String, Set[(String, Int)]] = {
    val doc = Config.pipeline.process(message)
    val (sentences, graph) = (doc.get(classOf[SentencesAnnotation]), doc.get(classOf[CorefChainAnnotation]).toMap)

    // Map each sentence to its mentioned topic
    val maps = for {
      mentions <- getMentions(graph)
      topics = getTopics(mentions)
      if topics.nonEmpty
      mention <- mentions
    } yield (topics.head.mentionSpan, sentences.get(mention.sentNum - 1))

    maps match {
      case Nil =>
        Map.empty[String, Set[(String, Int)]]
      case _ =>
        // Reduce all sentences of a topic into a bag.
        val reduces = maps.foldLeft(Map.empty[String, Set[CoreMap]]) {
          case (bag, (topic, sentence)) =>
            bag + (topic -> (bag.getOrDefault(topic, Set.empty[CoreMap]) + sentence))
        }

        // Perform sentiment analysis on sentences of each topic
        reduces.mapValues(_.map(sentence => (sentence.toString, RNNCoreAnnotations.getPredictedClass(sentence.get(classOf[AnnotatedTree])))))
    }
  }
}
