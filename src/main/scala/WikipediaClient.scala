import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{URI, URLEncoder}

object WikipediaClient {
  private val client = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

  def getSummary(entity: String): Option[String] = {
    try {
      val encoded = URLEncoder.encode(entity, "UTF-8").replace("+", "%20")
      val url = s"https://es.wikipedia.org/api/rest_v1/page/summary/$encoded"
      val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("User-Agent", "ProyectoLenguajes/1.0 (Comision 20)")
        .GET()
        .build()

      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      if (response.statusCode() == 200) {
        val json = ujson.read(response.body())
        Some(json("extract").str)
      } else {
        None
      }
    } catch {
      case _: Exception => None
    }
  }
}
