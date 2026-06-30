import scala.math.log

class Corpus(private val oraciones: Seq[Oracion]) {

  private lazy val tokens: Seq[String] = oraciones.flatMap(_.getTokens).distinct

  private lazy val idfScores: Map[String, Double] = tokens.map(t => t -> getIDF(t)).toMap

  private def appearancesInOraciones(token: String): Int = oraciones.count(_.contains(token))

  private def getIDF(token: String): Double = {
    log(oraciones.length.toDouble / (1.0 + appearancesInOraciones(token)))
  }

  // this is private and in Corpus because it needs info from the corpus
  // so it cannot be in Oracion
  private def getScore(oracion: Oracion): Double = {
    oracion.getUniqueTokens().map { token =>
      oracion.relativeFreq(token) * idfScores.getOrElse(token, 0.0)
    }.sum
  }

  def getSentencesScores(): Map[Oracion, Double] = {
    oraciones.map { oracion =>
      oracion -> getScore(oracion)
    }.toMap
  }

}

object Summarizer {
  private def segmentSentences(text: String): Seq[String] = {
    text.split("\\r?\\n+|(?<=\\.)\\s+")
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSeq
  }

  def summarize(text: String, maxSentences: Int): Seq[String] = {
    val originalSentences = segmentSentences(text)
    if (originalSentences.isEmpty) return Seq.empty

    val oraciones = originalSentences.map(s => Oracion(s))
    val corpus = new Corpus(oraciones)

    val sentenceScores = corpus.getSentencesScores()

    sentenceScores.toSeq
      // the - sorts descendingly by score, _2 using the second component of the tuple
      .sortBy(-_._2)
      .take(maxSentences)
      .map(_._1.getOriginalText)
  }
}