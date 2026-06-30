object ReportGenerator {
  def generate(
    tema: String,
    summary: Seq[String],
    entities: Seq[Entity],
    wikiInfo: Map[String, String]
  ): String = {
    val sb = new StringBuilder

    sb.append(s"=== REPORTE: $tema ===\n\n")

    sb.append("--- RESUMEN ---\n")
    if (summary.nonEmpty) {
      summary.zipWithIndex.foreach { case (sentence, i) =>
        sb.append(s"${i + 1}. $sentence\n")
      }
    } else {
      sb.append("No se pudo generar un resumen.\n")
    }
    sb.append("\n")

    sb.append("--- ENTIDADES DETECTADAS ---\n")
    val persons = entities.filter(_.label == "PER").map(_.text)
    val orgs    = entities.filter(_.label == "ORG").map(_.text)
    val locs    = entities.filter(_.label == "LOC").map(_.text)

    if (persons.nonEmpty) sb.append(s"Personas: ${persons.mkString(", ")}\n")
    if (orgs.nonEmpty)    sb.append(s"Organizaciones: ${orgs.mkString(", ")}\n")
    if (locs.nonEmpty)    sb.append(s"Lugares: ${locs.mkString(", ")}\n")
    if (persons.isEmpty && orgs.isEmpty && locs.isEmpty)
      sb.append("No se detectaron entidades.\n")
    sb.append("\n")

    sb.append("--- INFORMACIÓN ADICIONAL ---\n")
    if (wikiInfo.nonEmpty) {
      wikiInfo.foreach { case (name, extract) =>
        sb.append(s"$name: $extract\n\n")
      }
    } else if (entities.exists(e => e.label == "PER" || e.label == "ORG")) {
      sb.append("No se encontró información en Wikipedia para las entidades detectadas.\n")
    } else {
      sb.append("No se encontraron personas u organizaciones para consultar.\n")
    }

    sb.toString()
  }
}
