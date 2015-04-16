package global

import global.SimpleTokenizer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object LDA
{
  def run(): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf().setMaster("local[4]").setAppName("LDA")
    val sc = new SparkContext(conf)

    val tokenizer = new SimpleTokenizer(sc, getClass.getResource("/lda/stopwords.txt").getFile)
    val plotsRDD: RDD[String] = sc.textFile(getClass.getResource("/lda/train.txt").getFile)

    val tokenized: RDD[(Long, IndexedSeq[String])] = plotsRDD.zipWithIndex().map { case (text, id) =>
      id -> tokenizer.getWords(text)
    }
    tokenized.cache()

    val wordCounts: RDD[(String, Long)] = tokenized
      .flatMap { case (_, tokens) => tokens.map(_ -> 1L) }
      .reduceByKey(_ + _)
    wordCounts.cache()

    val (vocab: Map[String, Int], selectedTokenCount: Long) = {
      val sortedWC: Array[(String, Long)] = wordCounts.collect().sortBy(-_._2)

      (sortedWC.map(_._1).zipWithIndex.toMap, sortedWC.map(_._2).sum)
    }

    val corpus = tokenized.map { case (id, tokens) =>
      val wc = new mutable.HashMap[Int, Int]()

      tokens.foreach { term =>
        if (vocab.contains(term)) {
          val termIndex = vocab(term)
          wc(termIndex) = wc.getOrElse(termIndex, 0) + 1
        }
      }

      val indices = wc.keys.toArray.sorted
      val values = indices.map(i => wc(i).toDouble)

      val sb = Vectors.sparse(vocab.size, indices, values)
      (id, sb)
    }

    val vocabArray = new Array[String](vocab.size)
    vocab.foreach { case (term, i) => vocabArray(i) = term }

    val lda = new LDA

    lda.setK(10)
      .setMaxIterations(100)
      .setDocConcentration(-1)
      .setTopicConcentration(-1)

    val ldaModel = lda.run(corpus)

    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 5)

    val topics = topicIndices.map { case (terms, termWeights) =>
      terms.zip(termWeights).map { case (term, weight) => (vocabArray(term.toInt), weight) }
    }

    sc.stop()

    // TODO forward to Naive Bayer
  }
}
