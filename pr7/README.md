# Практическая работа №7 — реактивный HTTP API на Spring WebFlux

Проект представляет собой реактивную систему управления доставками. Все HTTP-обработчики,
межсервисный вызов и доступ к PostgreSQL построены на `Mono`, `Flux`, Spring WebFlux,
Project Reactor и Spring Data R2DBC. JDBC, JPA и `block()` в основном коде не используются.

## Архитектура

```text
                         HTTP / SSE
Client  --------------------------------------------+
  |                                                  |
  | REST                                             | Flux<ServerSentEvent>
  v                                                  v
Delivery Service ------------------------------> SSE stream
  |          |
  | R2DBC    | WebClient (reactive HTTP)
  |          v
  |       Order Service
  |          |
  |          | R2DBC
  v          v
PostgreSQL: deliveries + customer_orders
```

Оба сервиса используют одну PostgreSQL для простого запуска, но каждый работает только
со своей таблицей. `Delivery Service` не читает `customer_orders` напрямую: сведения о
заказе он получает только по HTTP через `WebClient`.

## Что реализовано

- реактивный CRUD для доставок;
- `Mono<DeliveryResponse>` для одного объекта и `Flux<DeliveryResponse>` для коллекции;
- PostgreSQL через неблокирующий драйвер R2DBC;
- внешний реактивный вызов из Delivery Service в Order Service через `WebClient`;
- SSE-поток изменений состояния доставки;
- backpressure через `onBackpressureLatest`: медленный клиент получает актуальное состояние;
- операторы `map`, `filter`, `flatMap`, `flatMapMany`, `switchIfEmpty`, `onErrorResume`;
- проверка допустимых переходов состояния;
- единый JSON-формат ошибок;
- unit-тесты с `StepVerifier` и тесты HTTP-контроллера через `WebTestClient`;
- запуск PostgreSQL и обоих сервисов одной командой Docker Compose.

## Структура проекта

```text
pr7/
├── order-service/       реактивный источник данных о заказах
├── delivery-service/    CRUD, WebClient, SSE и бизнес-правила
├── compose.yaml
├── demo.ps1             автоматическая демонстрация WebClient и SSE
├── requests.http        запросы для IntelliJ IDEA
└── README.md
```

## REST API

### Delivery Service — `http://localhost:8087`

| Метод | Маршрут | Реактивный результат | Назначение |
|---|---|---|---|
| `GET` | `/api/deliveries` | `Flux<DeliveryResponse>` | список и фильтрация доставок |
| `GET` | `/api/deliveries/{id}` | `Mono<DeliveryResponse>` | одна доставка |
| `POST` | `/api/deliveries` | `Mono<ResponseEntity<...>>` | создание после проверки заказа через WebClient |
| `PUT` | `/api/deliveries/{id}` | `Mono<DeliveryResponse>` | изменение курьера и статуса |
| `DELETE` | `/api/deliveries/{id}` | `Mono<ResponseEntity<Void>>` | удаление |
| `GET` | `/api/deliveries/{id}/stream` | `Flux<ServerSentEvent<...>>` | поток событий SSE |

Для списка доступны фильтры `status` и `minPrice`, например:

```text
GET /api/deliveries?status=DELIVERED&minPrice=2000
```

### Order Service — `http://localhost:8088`

| Метод | Маршрут | Назначение |
|---|---|---|
| `GET` | `/api/orders` | тестовые заказы из PostgreSQL |
| `GET` | `/api/orders/{id}` | заказ для внешнего WebClient-вызова |

При старте создаются заказы `1`, `2` и `3`. Первые два подтверждены, третий отменён.

## Запуск

Требуется только работающий Docker Desktop. Из каталога `pr7` выполнить:

```powershell
docker compose up --build -d
docker compose ps
```

Первый запуск может занять несколько минут из-за загрузки Maven-зависимостей и образов.
В результате должны быть запущены три healthy-контейнера:

- `postgres` — PostgreSQL 17, порт `5437`;
- `order-service` — порт `8088`;
- `delivery-service` — порт `8087`.

Проверка:

```powershell
Invoke-RestMethod http://localhost:8088/api/orders/1
Invoke-RestMethod http://localhost:8087/actuator/health
```