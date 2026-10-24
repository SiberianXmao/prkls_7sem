# Практическая работа №4 — клиент-серверное взаимодействие с RSocket

Консольное Java-приложение демонстрирует работу настоящих RSocket-клиента и
RSocket-сервера через TCP. 

## Что реализовано

| Модель RSocket | Метод клиента | Поведение |
|---|---|---|
| Request-Response | `findProduct` | запрос товара по ID и получение одного объекта |
| Request-Stream | `streamProducts` | постепенное получение нескольких товаров с задержкой |
| Fire-and-Forget | `sendViewEvent` | отправка события просмотра без ответного объекта |
| Channel | `updatePrices` | двусторонний поток обновлений цен и подтверждений сервера |

Также реализованы:

- реактивные типы `Mono` и `Flux` из Project Reactor;
- JSON-сериализация сообщений через Jackson;
- корректное освобождение RSocket `Payload` после чтения;
- обработка отсутствующего товара на сервере и на клиенте;
- потокобезопасное хранилище на основе `ConcurrentSkipListMap`;
- модульные тесты и интеграционный тест через реальный TCP-порт.

Схема работы:

```text
ProductRSocketClient  <-- RSocket/TCP + JSON -->  ProductRSocketServer
                                                    |
                                                    v
                                      InMemoryProductRepository
```

## Структура проекта

```text
src/main/java/ru/pr4/
├── app/          точки запуска и демонстрационный сценарий
├── client/       RSocket-клиент
├── model/        модели запросов, ответов и предметной области
├── protocol/     кодирование и декодирование JSON Payload
├── repository/   потокобезопасное хранилище тестовых данных
└── server/       RSocket-сервер и обработчик четырёх моделей
```

Основной сценарий запускается классом `ru.pr4.app.Main`. Он поднимает сервер на
свободном локальном порту, подключает клиента, выполняет четыре вида запросов и
корректно закрывает соединение.

## Запуск

Требуются Java 21 или новее и Maven 3.9+.

Проверить проект:

```powershell
cd pr4
mvn clean test
```

Запустить всю демонстрацию одной командой:

```powershell
mvn exec:java
```

Если Maven не установлен локально, проект можно проверить и запустить через
Docker:

```powershell
cd pr4
$project = (Get-Location).Path
docker run --rm -v "${project}:/workspace" -v pr4-maven-cache:/root/.m2 `
  -w /workspace maven:3.9.16-eclipse-temurin-25 mvn clean test
docker run --rm -v "${project}:/workspace" -v pr4-maven-cache:/root/.m2 `
  -w /workspace maven:3.9.16-eclipse-temurin-25 mvn -q exec:java
```

## Раздельный запуск сервера и клиента

Для более наглядной демонстрации можно открыть два терминала. Сервер принимает
номер порта и время работы в секундах:

```powershell
cd pr4
mvn exec:java "-Dexec.mainClass=ru.pr4.app.ServerMain" "-Dexec.args=7000 120"
```

В другом терминале запустить клиент:

```powershell
cd pr4
mvn exec:java "-Dexec.mainClass=ru.pr4.app.ClientMain" "-Dexec.args=127.0.0.1 7000"
```

## Ожидаемый результат

В консоли последовательно появятся четыре раздела:

```text
=== Request-Response ===
CLIENT received: ...Монитор 27 дюймов...
CLIENT handled error: Товар с ID 999 не найден

=== Request-Stream ===
CLIENT stream #1: Механическая клавиатура — 7490.00
...

=== Fire-and-Forget ===
CLIENT sent view event without response payload

=== Channel ===
CLIENT -> SERVER: productId=1, newPrice=7290.00
SERVER -> CLIENT: productId=1, 7490.00 -> 7290.00, status=updated
...
```

Для Request-Stream элементы выводятся с интервалом, поэтому видно, что клиент
получает их постепенно. В Channel каждое отправленное обновление сопровождается
отдельным ответом сервера.

## Автоматические тесты

В проекте девять тестов:

- кодирование и декодирование JSON Payload;
- поиск и изменение данных в репозитории;
- все четыре метода `ProductRSocketHandler`;
- обработка неизвестного ID;
- проверка задержанного потока с виртуальным временем;
- интеграционный тест клиента и сервера через TCP.

Проверенный результат:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Предупреждение Netty о `sun.misc.Unsafe` при запуске на новых версиях Java выводится самой
библиотекой и не означает ошибку приложения.

## Что показать на скриншотах

1. Разделы `Request-Response` и `Request-Stream` в консоли клиента.
2. Разделы `Fire-and-Forget` и `Channel` с сообщениями обеих сторон.
3. Строки `Tests run: 9` и `BUILD SUCCESS` после `mvn clean test`.

## Коротко для защиты

- `Mono` представляет ноль или один результат, а `Flux` — поток из нескольких
  элементов.
- Request-Response возвращает один товар, Request-Stream — несколько товаров.
- Fire-and-Forget подтверждения с данными не возвращает.
- Channel одновременно принимает поток обновлений и возвращает поток результатов.
- Медленный получатель управляет объёмом запрашиваемых элементов средствами
  Reactor/RSocket, то есть применяется реактивное противодавление.

