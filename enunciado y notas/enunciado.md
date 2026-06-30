# Proyecto de Promoción: Etapa 3

## 1. Descripción General

El objetivo general del proyecto es analizar de qué manera las decisiones que se toman en el diseño de un lenguaje de programación impactan en el desarrollo de un sistema de complejidad media. Para ello, deberán desarrollar un sistema que, dado un tema de interés (por ejemplo, Energías Renovables), recopile y procese automáticamente información proveniente de múltiples fuentes documentales y produzca un resumen que integre distintas perspectivas sobre dicho tema.

> **Recursos:** En el directorio documentos se encuentran los archivos de texto plano a procesar para generar los resumenes con el Summarizer de src/main/scala/Summarizer.scala.

---

## 2. Sistema a Desarrollar

Se desea desarrollar un sistema que, dado un tema de interés, recopile y procese automáticamente información proveniente de múltiples fuentes documentales y produzca un reporte sobre dicho tema. 

Para abordar este problema, se debe desarrollar un sistema por línea de comandos que permita a un usuario pedir un reporte de un tema particular. El sistema estará compuesto por módulos independientes, cada uno responsable de recopilar cierta información a partir de los documentos disponibles. Una vez extraída toda la información de las fuentes de datos, la misma se deberá consolidar en un reporte que se mostrará al usuario como respuesta a su consulta.

---

## 3. Requisitos de la Etapa

* Implementar una aplicación de consola que lea todos los archivos de texto (`.txt`) del directorio Recursos/documentos y produzca un reporte asociado al tema de interés consultado por el usuario.
* Los archivos de texto tendrán el estilo de un documento de Wikipedia.
* La lógica de procesamiento debe estar separada de la entrada/salida.
* La aplicación desarrollada debe considerar las características del reporte que se listan debajo (Resumen, Entidades e Información adicional).
* El resultado deberá mostrarse al usuario en la consola. 
* El sistema debe contemplar la posibilidad de que el usuario consulte por un tema de interés para el cual **no haya documentos disponibles**, y resolver la situación mostrando un mensaje adecuado.

### 3.1 Características de la Salida del Sistema

Ante una consulta de un usuario por un tema de interés (para el cual haya documentos para procesar), el sistema debe brindar un reporte incluyendo tres partes:

#### 3.1.1. Resumen
Se implementará un resumen extractivo de los documentos analizados que incluya los aspectos más importantes del tema de interés consultado por el usuario. Para ello, se utilizará el algoritmo basado en **TF-IDF** desarrollado en la etapa anterior. Este módulo evaluará las oraciones de los documentos y seleccionará las de mayor puntaje para conformar el texto final.

#### 3.1.2. Entidades
A partir del texto de los documentos, el sistema deberá identificar y extraer entidades nombradas (tales como personas, organizaciones y lugares). Para resolver esta tarea, deberán integrar a su aplicación un script provisto en Python que realiza el reconocimiento utilizando la librería `spacy`.

#### 3.1.3. Información adicional
Para las entidades extraídas correspondientes a personas y organizaciones, el sistema deberá obtener un breve texto descriptivo consultando una fuente de conocimiento externa. Para ello, deberán integrarse con la API REST pública de Wikipedia.

Específicamente, deberán realizar una petición HTTP GET al endpoint de resúmenes (`https://es.wikipedia.org/api/rest_v1/page/summary/{entidad}`) y procesar la respuesta en formato JSON para extraer el campo `extract`, el cual se anexará al reporte final.

**Consideraciones técnicas exigidas:**
* **Peticiones HTTP nativas:** La ejecución de las peticiones de red y la deserialización de los datos JSON deben resolverse utilizando el ecosistema de librerías del lenguaje seleccionado (Rust o Scala).
* **Reglas de uso de la API:** Configurar el encabezado `User-Agent` en cada petición HTTP incluyendo el número de comisión asignado (por ejemplo, `User-Agent: ProyectoLenguajes/1.0 (Comision Nro)`) para evitar que los servidores de Wikimedia rechacen la conexión.
* **Manejo robusto de errores:** Es esperable que ciertas entidades detectadas no posean un artículo exacto en Wikipedia, provocando que la API responda con un código de estado HTTP `404 (Not Found)`. El sistema deberá contemplar este escenario, capturar el error de forma controlada y simplemente omitir la información adicional para esa entidad, garantizando que la ejecución del programa no se interrumpa.

---

## 4. Pautas de Entrega

En esta tercera etapa, la entrega consistirá en el código fuente para los requisitos mencionados en la Sección 3. La resolución deberá ser enviada por Moodle en un único archivo comprimido (`.zip`).

Para garantizar la correcta evaluación del trabajo, la entrega debe cumplir obligatoriamente con las siguientes condiciones:

* **Estructura de la raíz:** El archivo zip al descomprimirse no debe generar carpetas anidadas innecesarias. En la raíz del proyecto entregado deben encontrarse los archivos de configuración del lenguaje (`Cargo.toml` o `build.sbt`), el archivo `Dockerfile` provisto por la cátedra y el script `ner_detector.py`.
* **Archivos a excluir:** No incluir directorios con binarios compilados (como `target/` en Rust o Scala) o entornos virtuales de Python locales (`venv/`, `env/`). Se debe entregar **únicamente** el código fuente.
* **Evaluación estandarizada:** La corrección del sistema se realizará construyendo la imagen a partir del `Dockerfile` entregado e instanciando el contenedor. Es responsabilidad del grupo probar localmente esta configuración y garantizar que su sistema compile y ejecute correctamente en este entorno aislado.

**Fecha límite de entrega:** 1 de julio de 2026, 20:00hs.

---

## 5. Informe del proyecto finalizado

Deberás realizar un informe del proyecto. En esta instancia que permita:
* Describir cómo resolvieron el problema (no solo en esta etapa, sino también el de el código dado inicialmente).
* Explicar el funcionamiento del sistema. tanto en general, como, cuáles son las responsabilidades de cada módulo, explicar las partes mas importantes de su implementación, y además acciones tomadas, relacionadas con entornos virtuales, dependencias, y docker
* Evaluar el lenguaje utilizado a la luz de la experiencia adquirida.

**Fecha de defensa:** 2 de julio de 2026 (el horario se definirá días antes de la fecha).

---