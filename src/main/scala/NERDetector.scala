import scala.sys.process._
import java.io.ByteArrayInputStream
import upickle.default._

case class Entity(text: String, label: String)

object NERDetector {
  def extractEntities(docs: Map[String, String]): Either[String, Seq[Entity]] = {
    val inputJson  = write(docs)
    val inputBytes = inputJson.getBytes("UTF-8")
    val inputStream = new ByteArrayInputStream(inputBytes)

    val stdout = new StringBuilder
    val stderr = new StringBuilder

    val pythonCmd = if (System.getProperty("os.name").toLowerCase.contains("win")) "python" else "python3"
    val exitCode = (Process(s"$pythonCmd ner_detector.py") #< inputStream) !
      ProcessLogger(
        line => { stdout.append(line); stdout.append("\n") },
        line => { stderr.append(line); stderr.append("\n") }
      )

    if (exitCode != 0) {
      Left(s"NER script falló (exit $exitCode): ${stderr.toString.trim}")
    } else {
      try {
        val parsed = ujson.read(stdout.toString.trim)
        val entities = parsed.obj.values.flatMap { docResult =>
          docResult("entities").arr.map { ent =>
            Entity(ent("text").str, ent("label").str)
          }
        }.toSeq.distinct
        Right(entities)
      } catch {
        case e: Exception => Left(s"Error parseando salida NER: ${e.getMessage}")
      }
    }
  }
}
