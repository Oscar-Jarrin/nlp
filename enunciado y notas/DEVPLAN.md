# DEVPLAN — Etapa 3: Sistema de Reportes con NER y Wikipedia

## Estado actual del proyecto

| Archivo | Estado |
|---|---|
| `src/main/scala/Oracion.scala` | ✅ Completo — modelo de oración con TF y tokenización |
| `src/main/scala/Summarizer.scala` | ✅ Completo — algoritmo TF-IDF |
| `src/main/scala/Main.scala` | ⚠️ Parcial — recibe un path de directorio, no un tema |
| `ner.py` | ⚠️ Nombre incorrecto — debe llamarse `ner_detector.py` |
| `build.sbt` | ⚠️ Falta dependencia JSON |
| `Dockerfile` | ✅ Completo |

---

## Tareas

### 1. Renombrar NER script
Renombrar `ner.py` → `ner_detector.py` en la raíz del proyecto.  
La cátedra y el Dockerfile buscan ese nombre exacto.

---

### 2. Agregar dependencia JSON en `build.sbt`
Agregar `upickle` para serialización/deserialización JSON (estándar del ecosistema Scala, sin transitive deps problemáticos):

```scala
libraryDependencies += "com.lihaoyi" %% "upickle" % "3.3.1"
```

---

### 3. Crear `src/main/scala/NERDetector.scala`

Responsabilidad: invocar `ner_detector.py` como subproceso, enviarle los documentos por stdin y parsear las entidades desde stdout.

```scala
case class Entity(text: String, label: String)

object NERDetector {
  def extractEntities(docs: Map[String, String]): Either[String, Seq[Entity]]
}
```

**Detalles de implementación:**
- Usar `scala.sys.process._` para spawnear `python3 ner_detector.py`
- Construir el JSON de entrada con `ujson.write(docs)` (upickle)
- Pasar JSON por stdin con `ByteArrayInputStream`
- Leer JSON de stdout y parsear con upickle
- Deduplicar entidades por `(text, label)` across todos los docs
- Retornar `Left(errorMsg)` si el proceso falla (exit code != 0)

**Protocolo I/O del script Python:**

*Entrada (stdin):*
```json
{ "doc1.txt": "Elon Musk fundó Tesla.", "doc2.txt": "..." }
```

*Salida (stdout):*
```json
{ "doc1.txt": { "entities": [{"text": "Elon Musk", "label": "PER"}, {"text": "Tesla", "label": "ORG"}] } }
```

---

### 4. Crear `src/main/scala/WikipediaClient.scala`

Responsabilidad: consultar la API REST de Wikipedia y retornar el campo `extract` del resumen.

```scala
object WikipediaClient {
  def getSummary(entity: String): Option[String]
}
```

**Detalles de implementación:**
- Usar `java.net.http.HttpClient` (built-in en Java 17, sin dependencias extra)
- Endpoint: `https://es.wikipedia.org/api/rest_v1/page/summary/{entity}`  
  (URL-encode el nombre de la entidad con `URLEncoder.encode(entity, "UTF-8")`)
- Header obligatorio: `User-Agent: ProyectoLenguajes/1.0 (Comision 20)`
- Si la respuesta es 200 → parsear JSON con upickle y retornar `Some(extract)`
- Si la respuesta es 404 o cualquier otro error → retornar `None` (no lanzar excepción)
- Solo se llama para entidades con label `PER` u `ORG`

---

### 5. Crear `src/main/scala/ReportGenerator.scala`

Responsabilidad: ensamblar el reporte final como String a partir de sus partes.

```scala
object ReportGenerator {
  def generate(
    tema: String,
    summary: Seq[String],
    entities: Seq[Entity],
    wikiInfo: Map[String, String]   // entity.text -> extract
  ): String
}
```

**Formato de salida esperado:**
```
=== REPORTE: energias-renovables ===

--- RESUMEN ---
1. La energía solar fotovoltaica ha experimentado...
2. ...

--- ENTIDADES DETECTADAS ---
Personas: Albert Einstein, Elon Musk
Organizaciones: Tesla, YPF, Naciones Unidas
Lugares: Argentina, Patagonia

--- INFORMACIÓN ADICIONAL ---
Albert Einstein: Albert Einstein fue un físico teórico...
Tesla: Tesla, Inc. es una empresa estadounidense...
```

---

### 6. Refactorizar `src/main/scala/Main.scala`

Cambiar la interfaz del sistema:
- **Antes:** recibe un path de directorio como argumento
- **Después:** recibe el **nombre del tema** (ej: `energias-renovables`)

**Nuevo flujo:**

```
sbt run <tema>
  ↓
Buscar directorio documentos/<tema>/
  ↓ (no existe o sin .txt)
Mostrar: "No hay documentos disponibles para el tema '<tema>'."
  ↓ (existe y tiene .txt)
FileReader.readTextFiles(dir) → Map[filename, content]
  ↓
Summarizer.summarize(combinedText, 10) → Seq[String]
  ↓
NERDetector.extractEntities(docsMap) → Seq[Entity]
  ↓
WikipediaClient.getSummary(entity) para cada PER/ORG → Map[String, String]
  ↓
ReportGenerator.generate(...) → String
  ↓
println(report)
```

**Búsqueda del directorio:**
- El directorio base de documentos se resuelve relativo al directorio de trabajo: `documentos/<tema>/`
- Matching case-insensitive del nombre del tema

---

## Archivos modificados / creados

| Archivo | Acción |
|---|---|
| `ner.py` | Renombrar a `ner_detector.py` |
| `build.sbt` | Agregar `upickle` |
| `src/main/scala/Main.scala` | Refactorizar |
| `src/main/scala/NERDetector.scala` | Crear |
| `src/main/scala/WikipediaClient.scala` | Crear |
| `src/main/scala/ReportGenerator.scala` | Crear |

`Oracion.scala` y `Summarizer.scala` **no se modifican**.

---

## Verificación

```bash
# 1. Compilar
sbt compile

# 2. Probar con tema existente
sbt "run energias-renovables"
sbt "run inteligencia-artificial"
sbt "run ciencia-ficcion"

# 3. Probar con tema inexistente
sbt "run temaInexistente"
# Esperado: mensaje de error adecuado

# 4. Probar en Docker
docker build -t ldp-etapa3 .
docker run -it ldp-etapa3 sbt "run energias-renovables"
```

**Checks de corrección:**
- El reporte muestra las tres secciones (Resumen, Entidades, Información adicional)
- Las entidades sin artículo en Wikipedia no interrumpen la ejecución
- El header `User-Agent` está presente en cada petición HTTP
- El sistema no lanza excepciones no controladas bajo ningún escenario
