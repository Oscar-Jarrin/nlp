# Documentación Técnica — Etapa 2: Summarizer TF-IDF

## Descripción general del algoritmo

El sistema implementa un **resumen extractivo** basado en el algoritmo **TF-IDF** (_Term Frequency – Inverse Document Frequency_). A diferencia de un resumen abstractivo (que genera texto nuevo), el extractivo selecciona y devuelve las oraciones más representativas del corpus original.

El proceso tiene tres etapas:

1. **Segmentación:** el texto de entrada se divide en oraciones.
2. **Puntuación:** cada oración recibe un puntaje numérico que mide su relevancia dentro del corpus.
3. **Selección:** se devuelven las N oraciones de mayor puntaje.

El puntaje de una oración se calcula combinando dos métricas complementarias:

- **TF (Term Frequency):** mide qué tan frecuente es un término _dentro de una oración_. Un término que aparece mucho en una oración probablemente es central para ella.

  ```
  TF(término, oración) = apariciones del término en la oración
                         ─────────────────────────────────────
                           total de tokens en la oración
  ```

- **IDF (Inverse Document Frequency):** mide qué tan _raro_ es un término a lo largo de todas las oraciones del corpus. Los términos que aparecen en muchas oraciones (como artículos o preposiciones) tienen IDF bajo; los que aparecen en pocas oraciones tienen IDF alto y aportan más información.

  ```
  IDF(término) = ln( cantidad de oraciones del corpus )
                     ────────────────────────────────────────
                     1 + cantidad de oraciones que contienen el término
  ```

El puntaje final de una oración es la suma del producto TF × IDF para cada uno de sus tokens únicos:

```
Score(oración) = Σ  TF(token, oración) × IDF(token)
               token ∈ tokens únicos de la oración
```

---

## Módulos

### `Oracion.scala`

Contiene dos entidades: el tipo de dato `Oracion` y el objeto utilitario `Tokenizer`.

---

#### `case class Oracion`

Representa una oración del corpus. Encapsula el texto original y su forma tokenizada.

```scala
case class Oracion(originalText: String) {
  private lazy val internalTokens: Seq[String] = Tokenizer.tokenize(originalText)
  ...
}
```

El campo `internalTokens` es `lazy`: la tokenización no ocurre al construir el objeto sino la primera vez que se accede. Esto evita trabajo innecesario si una oración nunca llega a ser evaluada.

**Métodos principales:**

| Método | Descripción |
|---|---|
| `getTokens` | Devuelve la secuencia completa de tokens de la oración. |
| `getUniqueTokens()` | Devuelve los tokens sin duplicados (usado para no sumar el mismo token dos veces al calcular el score). |
| `relativeFreq(token)` | Calcula el TF: `apariciones / total de tokens`. Devuelve `0.0` si la oración no tiene tokens. |
| `contains(token)` | Indica si el token existe en la oración. Usado por `Corpus` para calcular el IDF. |

---

#### `object Tokenizer`

Singleton responsable de convertir una oración en texto plano a una lista de tokens normalizados.

```scala
def tokenize(sentence: String): Seq[String] = {
  sentence
    .toLowerCase
    .replaceAll("[^\\p{L}\\s]", "")
    .split("\\s+")
    .filter(_.nonEmpty)
    .filterNot(stopwords.contains)
    .toSeq
}
```

El pipeline de tokenización hace cuatro transformaciones en cadena:

1. **Minúsculas** (`toLowerCase`): "Tesla" y "tesla" se tratan como el mismo término.
2. **Eliminación de puntuación** (`replaceAll`): elimina todo carácter que no sea letra (`\p{L}` cubre letras Unicode, incluyendo acentos como "á", "ñ") ni espacio.
3. **Split por espacios** (`split("\\s+")`): divide en tokens individuales.
4. **Filtrado de stopwords** (`filterNot`): elimina las 30 palabras vacías del español definidas en `stopwords` (artículos, preposiciones, conjunciones). Estas palabras no aportan significado semántico y distorsionarían los puntajes TF-IDF.

---

### `Summarizer.scala`

Contiene la clase `Corpus` y el objeto `Summarizer`. Implementan el cálculo TF-IDF y la orquestación del resumen.

---

#### `class Corpus`

Agrupa todas las oraciones del corpus y calcula los puntajes IDF de cada token.

```scala
class Corpus(private val oraciones: Seq[Oracion]) {
  private lazy val tokens: Seq[String] = oraciones.flatMap(_.getTokens).distinct
  private lazy val idfScores: Map[String, Double] = tokens.map(t => t -> getIDF(t)).toMap
  ...
}
```

Ambos campos son `lazy`: `tokens` reúne todos los tokens distintos del corpus completo (aplanando la lista de listas con `flatMap`), y `idfScores` precalcula el IDF de cada uno en un `Map` para acceso eficiente en O(1). Ambos se computan una única vez al ser accedidos por primera vez.

**Métodos:**

`appearancesInOraciones(token)`: cuenta en cuántas oraciones aparece un token (no cuántas veces, sino en cuántas oraciones distintas). Es el denominador del IDF.

```scala
private def appearancesInOraciones(token: String): Int =
  oraciones.count(_.contains(token))
```

`getIDF(token)`: implementa la fórmula IDF. El `+1` en el denominador es un suavizado (_smoothing_) para evitar división por cero si un token no aparece en ninguna oración.

```scala
private def getIDF(token: String): Double =
  log(oraciones.length.toDouble / (1.0 + appearancesInOraciones(token)))
```

`getSentencesScores()`: es el método central del algoritmo. Para cada oración, itera sobre sus tokens únicos, multiplica el TF de cada token por su IDF precalculado, y suma los resultados. Devuelve un `Map[Oracion, Double]`.

```scala
def getSentencesScores(): Map[Oracion, Double] = {
  oraciones.map { oracion =>
    val score = oracion.getUniqueTokens().map { token =>
      oracion.relativeFreq(token) * idfScores.getOrElse(token, 0.0)
    }.sum
    oracion -> score
  }.toMap
}
```

El uso de `getUniqueTokens()` (tokens sin duplicados) es intencional: si se iterara sobre todos los tokens, un término repetido se contaría múltiples veces alterando el score. Con tokens únicos, cada término contribuye exactamente una vez con su TF × IDF.

---

#### `object Summarizer`

Orquesta el pipeline completo: segmentación del texto, construcción del corpus y selección de las mejores oraciones.

`segmentSentences(text)`: divide el texto en oraciones usando dos criterios con una expresión regular:

```scala
text.split("\\r?\\n+|(?<=\\.)\\s+")
```

- `\\r?\\n+`: separa por saltos de línea (uno o más), compatible con finales de línea Windows (`\r\n`) y Unix (`\n`).
- `(?<=\\.)\\s+`: separa después de un punto seguido de espacio (lookbehind), para dividir párrafos en oraciones individuales.

`summarize(text, maxSentences)`: método público principal.

```scala
def summarize(text: String, maxSentences: Int): Seq[String] = {
  val originalSentences = segmentSentences(text)
  if (originalSentences.isEmpty) return Seq.empty

  val oraciones = originalSentences.map(s => Oracion(s))
  val corpus = new Corpus(oraciones)
  val sentenceScores = corpus.getSentencesScores()

  sentenceScores.toSeq
    .sortBy(-_._2)
    .take(maxSentences)
    .map(_._1.originalText)
}
```

El pipeline:
1. Segmenta el texto en oraciones crudas.
2. Construye un `Oracion` por cada una.
3. Instancia `Corpus` con el conjunto completo.
4. Obtiene los puntajes y los ordena de mayor a menor (`sortBy(-_._2)`).
5. Toma las `maxSentences` mejores y devuelve su texto original.

Se devuelve `originalText` y no los tokens: el resumen debe mostrar las oraciones tal como aparecen en el documento fuente.

---

### `Main.scala`

Contiene el punto de entrada del programa y el lector de archivos.

---

#### `object FileReader`

Encapsula toda la lógica de lectura de archivos, devolviendo un `Either` para manejar errores sin excepciones.

```scala
def readTextFiles(directoryPath: String): Either[String, Seq[String]]
```

El tipo de retorno `Either[String, Seq[String]]` es una forma funcional de representar resultados con posible error: `Left(mensaje)` en caso de fallo, `Right(contenidos)` en caso de éxito. Esto obliga al código que llama a manejar explícitamente ambos casos.

La función verifica en orden:
1. Que el directorio exista y sea un directorio (no un archivo).
2. Que contenga al menos un archivo `.txt`.
3. Lee cada archivo con `scala.util.Using`, que garantiza el cierre del recurso aunque ocurra una excepción (equivalente a _try-with-resources_ en Java).

Si un archivo individual falla al leerse, se registra el error y se retorna una cadena vacía para ese archivo, sin abortar la lectura del resto del directorio.

---

#### `object Main`

Punto de entrada de la aplicación (`extends App`).

```scala
FileReader.readTextFiles(directoryPath) match {
  case Left(errorMessage) =>
    println(errorMessage)
    sys.exit(1)
  case Right(fileContents) =>
    val combinedText = fileContents.mkString("\n\n")
    val summarySentences = Summarizer.summarize(combinedText, 10)
    ...
}
```

Usa _pattern matching_ sobre el `Either` devuelto por `FileReader`: si es `Left` imprime el error y termina con código 1; si es `Right` combina todos los textos con separadores de párrafo (`\n\n`) y delega en `Summarizer.summarize` para generar hasta 10 oraciones de resumen.

La separación explícita de la lógica (en `FileReader` y `Summarizer`) y la entrada/salida (en `Main`) permite testear las partes funcionales de forma independiente sin depender del sistema de archivos ni de la consola.
