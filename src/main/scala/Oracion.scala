case class Oracion(private val originalText: String) {

  private lazy val internalTokens: Seq[String] = Tokenizer.tokenize(originalText)

  def getTokens: Seq[String] = internalTokens

  def getOriginalText: String = originalText

  def relativeFreq(token: String): Double = {
    if (internalTokens.isEmpty) 0.0
    //utiliza un comodín para pasar una función anónima que compara cada token con el token dado
    //es una abreviación de internalTokens.count(t => t == token).toDouble / internalTokens.length
    else internalTokens.count(_ == token).toDouble / internalTokens.length
  }

  def getUniqueTokens(): Seq[String] = internalTokens.distinct

  def contains(token: String): Boolean = internalTokens.contains(token)
}

object Tokenizer {
  private val stopwords: Set[String] = Set(
    "a", "al", "con", "de", "del", "el", "en", "la", "las", "lo",
    "los", "mi", "mis", "o", "para", "pero", "por", "que", "se", "sin",
    "sobre", "su", "sus", "tu", "tus", "un", "una", "unas", "unos", "y"
  )

  def tokenize(sentence: String): Seq[String] = {
    sentence
      .toLowerCase
      .replaceAll("[^\\p{L}\\s]", "")
      .split("\\s+")
      .filter(_.nonEmpty)
      .filterNot(stopwords.contains)
      .toSeq
  }
}
