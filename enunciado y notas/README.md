# Recursos para la Etapa 3 del Proyecto

En esta carpeta se encuentran los recursos necesarios para desarrollar la Etapa 3 del proyecto de promoción.

## Estructura de archivos provistos

* `documentos/`: Carpeta con los archivos de texto plano (`.txt`) que el sistema debe procesar para generar el resumen.
* `ner_detector.py`: Script en Python encargado de la extracción de entidades nombradas mediante la librería spaCy.
* `plantilla_rust/` y `plantilla_scala/`: Carpetas que contienen la definición del entorno de ejecución (`Dockerfile`) para cada lenguaje.

## Instrucciones de configuración del proyecto

Para garantizar que el sistema pueda ser compilado y evaluado correctamente mediante Docker, se debe estructurar el proyecto siguiendo estos pasos:

1. **Selección del entorno:** Ingresar a la carpeta `plantilla_rust` o `plantilla_scala` según el lenguaje seleccionado por el grupo.
2. **Ubicación del Dockerfile:** Copiar el archivo `Dockerfile` y pegarlo en la **raíz del repositorio** (el mismo directorio donde se encuentra el archivo `Cargo.toml` o `build.sbt`). Es importante que el archivo se llame exactamente `Dockerfile` y no tenga ninguna extensión adicional (como `.txt`).
3. **Ubicación del script NER:** Copiar el archivo `ner_detector.py` y ubicarlo también en la raíz del repositorio. 
4. **Ejecución:** La aplicación de consola debe asumir que este script de Python se encuentra ubicado en el directorio actual de ejecución.