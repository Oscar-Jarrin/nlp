import java.io.File
import scala.io.Source
import scala.util.Using

object FileReader {
  def readTextFiles(directoryPath: String): Either[String, Map[String, String]] = {
    val directory = new File(directoryPath)

    if (!directory.exists() || !directory.isDirectory) {
      Left(s"El directorio '$directoryPath' no existe.")
    } else {
      val textFiles = directory.listFiles(_.getName.endsWith(".txt")).toSeq

      if (textFiles.isEmpty) {
        Left(s"No se encontraron archivos .txt en '$directoryPath'.")
      } else {
        val contents = textFiles.map { file =>
          val content = Using(Source.fromFile(file, "UTF-8")) { source =>
            source.getLines().mkString("\n")
          }.recover { case ex =>
            System.err.println(s"No se pudo leer ${file.getName}: ${ex.getMessage}")
            ""
          }.getOrElse("")
          file.getName -> content
        }.toMap
        Right(contents)
      }
    }
  }
}

object Main extends App {
  if (args.length != 1) {
    println("Uso: sbt \"run <tema>\"")
    sys.exit(1)
  }

  val tema    = args(0)
  val baseDir = new File("documentos")

  val temaDir: Option[File] =
    if (baseDir.exists() && baseDir.isDirectory)
      baseDir.listFiles().find(f => f.isDirectory && f.getName.equalsIgnoreCase(tema))
    else
      None

  temaDir match {
    case None =>
      println(s"No hay documentos disponibles para el tema '$tema'.")
      sys.exit(1)

    case Some(dir) =>
      FileReader.readTextFiles(dir.getPath) match {
        case Left(_) =>
          println(s"No hay documentos disponibles para el tema '$tema'.")
          sys.exit(1)

        case Right(docsMap) =>
          val combinedText = docsMap.values.mkString("\n\n")
          val summary      = Summarizer.summarize(combinedText, 10)

          val entities = NERDetector.extractEntities(docsMap) match {
            case Right(ents) => ents
            case Left(err) =>
              System.err.println(s"Advertencia: no se pudieron extraer entidades: $err")
              Seq.empty[Entity]
          }

          val wikiInfo = entities
            .filter(e => e.label == "PER" || e.label == "ORG")
            .flatMap(e => WikipediaClient.getSummary(e.text).map(e.text -> _))
            .toMap

          println(ReportGenerator.generate(tema, summary, entities, wikiInfo))
      }
  }
}
