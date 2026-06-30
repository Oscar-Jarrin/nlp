# Utilizar una imagen base de Java 17 oficial y estable
FROM eclipse-temurin:17-jdk-jammy

# Instalar Python, dependencias del sistema y descargar sbt manualmente
RUN apt-get update && apt-get install -y python3 python3-pip python3-venv curl && \
    curl -L https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz | tar -xz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/bin/sbt

# Crear un entorno virtual de Python y agregarlo al PATH
ENV VIRTUAL_ENV=/opt/venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Instalar spaCy y descargar el modelo lingüístico en español
RUN pip install --upgrade pip setuptools wheel && \
    pip install spacy click && \
    python -m spacy download es_core_news_sm

# Configurar el directorio de trabajo dentro del contenedor
WORKDIR /usr/src/app

# Copiar todo el código fuente y archivos al contenedor
COPY . .

# Compilar el código Scala
RUN sbt compile

# Comando por defecto al ejecutar el contenedor
CMD ["sbt", "run"]