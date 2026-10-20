# Plugin Playground — этап 2

Учебное консольное приложение с бесконечной ASCII-анимацией. Здесь можно сравнить два способа подключения плагина: создание встроенного объекта через `new` и загрузку класса из внешнего JAR через `URLClassLoader`. Выход из анимации — Ctrl+C.

## Модули и роли

- `plugin-api` — общий контракт `AnimationPlugin`. Метод `renderFrame()` возвращает текст кадра заданного размера и не обращается к терминалу. Это SPI: хост определяет интерфейс, а разные реализации предоставляют поведение.
- `plugin-host` — `LaunchOptions` разбирает CLI, `PluginSession` получает плагин и владеет его загрузчиком, а компактный `Main` соединяет их с `AnimationRunner`. Runner готовит диагностику и терминал, `AnimationEngine` запрашивает кадры через `AnimationPlugin`, `TerminalRenderer` выводит их с помощью ANSI. `StarfieldPlugin` рисует встроенное звёздное поле; `ExternalPluginLoader` по-прежнему отвечает только за загрузку класса из JAR.
- `plugin-matrix` — самостоятельный `MatrixRainPlugin` с падающими столбцами символов. Он зависит от `plugin-api` через `compileOnly` и не зависит от хоста.

## Сборка и запуск

Нужна JDK 21. Gradle Wrapper включён в репозиторий. В `gradle.properties` выбран обычный консольный вывод Gradle, чтобы индикатор сборки не перекрывал анимацию. Используйте терминал не менее 80×30 символов с поддержкой ANSI: сцена занимает 60×20, над ней три строки диагностики.

```bash
./gradlew clean build
./gradlew :plugin-host:run
./gradlew :plugin-host:run --args="--mode=builtin"
./gradlew :plugin-matrix:jar
./gradlew :plugin-host:run --args="--mode=url --jar=plugin-matrix/build/libs/plugin-matrix.jar --class=playground.matrix.MatrixRainPlugin"
./gradlew :plugin-host:run --args="--help"
```

Путь в `--jar` при запуске через Gradle отсчитывается от корня проекта. Для режима `url` обязательны `--jar` и `--class`. `plugin-host` не зависит от `plugin-matrix`: сначала соберите JAR отдельной командой. При обычном запуске класс `MatrixRainPlugin` отсутствует в classpath хоста.

## Что происходит при загрузке

В режиме `builtin` `PluginSession` выполняет `AnimationPlugin plugin = new StarfieldPlugin();`: класс уже находится в classpath приложения. В режиме `url` он использует `ExternalPluginLoader`, который проверяет путь и создаёт `URLClassLoader` с адресом JAR. Его родитель — `AnimationPlugin.class.getClassLoader()`. Вызов `loadClass()` находит класс по полному имени, проверка `isAssignableFrom()` подтверждает контракт, а Reflection (`getConstructor().newInstance()`) вызывает публичный конструктор без аргументов. Полученный объект передаётся тому же `AnimationEngine`.

Загрузчик остаётся открытым во время анимации. При обычном завершении runner восстанавливает курсор, затем `Main` закрывает `PluginSession` через `try-with-resources`. При Ctrl+C один shutdown hook сначала прерывает поток анимации и ждёт его завершения; если он не успеет выйти, hook сам восстановит терминал и закроет сессию. Повторное закрытие безопасно, а загрузчик закрывается после остановки анимации, насколько это позволяет завершение JVM.

Интерфейс должен иметь одну идентичность для хоста и плагина: в Java тип определяется парой **имя класса + загрузивший ClassLoader**. Поэтому внешний JAR не содержит `AnimationPlugin.class`, а родительский загрузчик предоставляет его обеим сторонам. Если загрузить вторую копию интерфейса отдельным загрузчиком, одинаковое имя не спасёт от несовместимости типов и возможного `ClassCastException`.

Над сценой отображаются режим, имя плагина и загрузчики интерфейса, реализации и текущего потока (TCCL). В режиме `builtin` реализацию обычно загружает `AppClassLoader`; в режиме `url` — отдельный `URLClassLoader`. TCCL задаётся потоку и может оставаться `AppClassLoader`, даже когда объект плагина пришёл из `URLClassLoader`. На этом этапе приложение TCCL не меняет.

## Следующие этапы

1. `ServiceLoader`, `META-INF/services` и эксперименты с TCCL.
2. Hot Swap внешнего плагина без перезапуска JVM и сравнение разных загрузчиков.

Новые способы подключения будут добавляться рядом с `builtin` и `url`.
