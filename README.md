# Plugin Playground — этап 3

Учебное консольное приложение с бесконечной ASCII-анимацией. Теперь оно позволяет сравнить `new`, явную загрузку класса из JAR и стандартное обнаружение SPI-провайдеров. Выход из анимации — Ctrl+C.

## Модули и роли

- `plugin-api` — общий контракт `AnimationPlugin`. Метод `renderFrame()` возвращает текст кадра заданного размера и не обращается к терминалу. Это SPI: хост определяет интерфейс, а разные реализации предоставляют поведение.
- `plugin-host` — `LaunchOptions` разбирает CLI, `PluginSession` получает плагин и владеет его загрузчиком, `AnimationRunner` готовит диагностику и терминал. `AnimationEngine` запрашивает кадры через `AnimationPlugin`, `TerminalRenderer` выводит их с помощью ANSI. Встроенный `StarfieldPlugin` создаётся через `new`. `ExternalPluginLoader` оставляет явную загрузку класса и Reflection для режима `url`; `SpiPluginLoader` использует `ServiceLoader`, а `PluginMenu` выбирает обнаруженный плагин.
- `plugin-matrix` — независимый JAR с `MatrixRainPlugin` и падающими столбцами символов.
- `plugin-aquarium` — независимый JAR с `AquariumPlugin`: рыбки, пузырьки и водоросли.

Оба внешних модуля имеют только `compileOnly(project(":plugin-api"))`. Хост не зависит от их классов при компиляции или запуске.

## Сборка и запуск

Нужна JDK 21. Gradle Wrapper включён в репозиторий. В `gradle.properties` выбран обычный консольный вывод Gradle, чтобы индикатор сборки не перекрывал анимацию. Используйте терминал не менее 80×30 символов с поддержкой ANSI: сцена занимает 60×20, над ней несколько строк диагностики.

```bash
./gradlew clean build
./gradlew :plugin-host:run
./gradlew :plugin-host:run --args="--mode=builtin"
./gradlew :plugin-matrix:jar
./gradlew :plugin-host:run --args="--mode=url --jar=plugin-matrix/build/libs/plugin-matrix.jar --class=playground.matrix.MatrixRainPlugin"
./gradlew preparePlugins
./gradlew :plugin-host:run --args="--mode=spi"
./gradlew :plugin-host:run --args="--mode=spi --plugins-dir=plugins"
./gradlew :plugin-host:run --args="--help"
```

Пути в аргументах отсчитываются от корня проекта. Для `url` обязательны `--jar` и `--class`. Задача `preparePlugins` собирает оба внешних JAR и копирует их в `plugins/`, сохраняя добавленные вами файлы. В режиме `spi` можно положить туда ещё один JAR с реализацией `AnimationPlugin` и собственным `META-INF/services/playground.api.AnimationPlugin`: меню сформируется по фактически обнаруженным провайдерам. При отсутствующем или пустом каталоге приложение выдаст подсказку.

## Что происходит при загрузке

В режиме `builtin` `PluginSession` выполняет `AnimationPlugin plugin = new StarfieldPlugin();`: класс уже находится в classpath приложения. В режиме `url` он использует `ExternalPluginLoader`, который проверяет путь и создаёт `URLClassLoader` с адресом JAR. Его родитель — `AnimationPlugin.class.getClassLoader()`. Вызов `loadClass()` находит класс по полному имени, проверка `isAssignableFrom()` подтверждает контракт, а Reflection (`getConstructor().newInstance()`) вызывает публичный конструктор без аргументов. Полученный объект передаётся тому же `AnimationEngine`.

В режиме `spi` `SpiPluginLoader` собирает URL всех JAR в каталоге и передаёт их одному `URLClassLoader`. В каждом JAR файл `META-INF/services/playground.api.AnimationPlugin` содержит полное имя своего класса. Это стандартная регистрация Java SPI: два JAR могут независимо объявлять реализации одного интерфейса. `ServiceLoader.load(AnimationPlugin.class, pluginLoader)` читает эти файлы и создаёт провайдеров сам. Приложению не нужно знать имена классов и вызывать Reflection вручную. `URLClassLoader` даёт доступ к JAR, а `ServiceLoader` обнаруживает объявленные в них реализации.

Загрузчик остаётся открытым во время анимации. При обычном завершении runner восстанавливает курсор, закрывает `PluginSession` и удаляет shutdown hook; `try-with-resources` в `Main` служит дополнительной гарантией закрытия. При Ctrl+C единственный hook прерывает поток анимации и ждёт его завершения; если он не успеет выйти, hook сам восстановит терминал и закроет сессию. Повторное закрытие безопасно, а загрузчик закрывается после остановки анимации, насколько это позволяет завершение JVM.

Интерфейс должен иметь одну идентичность для хоста и плагина: в Java тип определяется парой **имя класса + загрузивший ClassLoader**. Поэтому внешний JAR не содержит `AnimationPlugin.class`, а родительский загрузчик предоставляет его обеим сторонам. Если загрузить вторую копию интерфейса отдельным загрузчиком, одинаковое имя не спасёт от несовместимости типов и возможного `ClassCastException`.

`ServiceLoader.load(AnimationPlugin.class)` использует контекстный загрузчик текущего потока (TCCL). Перед меню приложение выполняет поиск с исходным TCCL, затем временно назначает ему загрузчик JAR и повторяет поиск. Результаты материализуются до восстановления TCCL, поскольку `ServiceLoader` загружает провайдеров лениво. В изолированной конфигурации первый поиск обычно даёт 0, второй — 2; приложение выводит фактические результаты. После эксперимента исходный TCCL восстанавливается. Над сценой остаются режим, загрузчики интерфейса и выбранной реализации, текущий TCCL и оба счётчика. TCCL потока и загрузчик, определивший класс плагина, — разные понятия.

## Следующие этапы

1. Hot Swap внешнего плагина без перезапуска JVM и сравнение разных загрузчиков.

Hot Swap будет добавлен рядом с `builtin`, `url` и `spi`.
