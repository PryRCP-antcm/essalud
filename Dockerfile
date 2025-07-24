# Usa una imagen base liviana con Java 17
FROM openjdk:17-slim

# Establece el directorio de trabajo
WORKDIR /app

# Instala dependencias necesarias para compilar Python
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget \
    build-essential \
    libssl-dev \
    zlib1g-dev \
    libncurses5-dev \
    libncursesw5-dev \
    libreadline-dev \
    libsqlite3-dev \
    libgdbm-dev \
    libdb5.3-dev \
    libbz2-dev \
    libexpat1-dev \
    liblzma-dev \
    tk-dev \
    curl \
    xz-utils \
    git \
    ca-certificates \
    libffi-dev \
    && rm -rf /var/lib/apt/lists/*

# Descarga y compila Python 3.11.9
RUN wget https://www.python.org/ftp/python/3.11.9/Python-3.11.9.tgz && \
    tar xvf Python-3.11.9.tgz && \
    cd Python-3.11.9 && \
    ./configure --enable-optimizations && \
    make -j$(nproc) && \
    make altinstall && \
    cd .. && \
    rm -rf Python-3.11.9* && \
    ln -s /usr/local/bin/python3.11 /usr/bin/python3 && \
    ln -s /usr/local/bin/pip3.11 /usr/bin/pip3

# Verifica la versión de Python y pip
RUN python3 --version && pip3 --version

# Copia los archivos del proyecto
COPY . .

# Instala dependencias de Python
RUN pip3 install --upgrade pip && pip3 install -r requirements.txt

# Da permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Compila el proyecto Spring Boot (sin tests)
RUN ./mvnw clean package -DskipTests

# Expone el puerto 8080
EXPOSE 8080

# Ejecuta la aplicación Spring Boot
CMD ["java", "-jar", "target/springbootaidemo-0.0.1-SNAPSHOT.jar"]
