# Сборка и запуск приложения

Для сборки и работы с программой требуется [JDK 27](https://jdk.java.net/27/).

## Сборка

```shell
export JAVA_HOME=/path/to/jdk27
mvn
```

## Запуск из проекта

```shell
mvn exec:exec@run
```

# Дистрибутив

Скачайте и распакуйте [JavaFX JMODs distribution](https://jdk.java.net/javafx26/).

```shell
export JAVAFX_JMODS=/path/to/javafx-jmods-27
mvn -DskipTests=true clean verify jpackage:jpackage
```

## OS X и MS Windows

На этих платформах каталог `target/dist` будет содержать пакет для установки.

## Linux

```shell
mvn -DskipTests=true clean verify jpackage:jpackage assembly:single
```

Каталог ```target``` будет содержать архив `mk52-${project.version}-linux-x86_64.tar.gz` со следующей структурой:

```text
mk52-${project.version}/
├── mk52.sh
├── bin/
└── lib/
```