# Практическая работа №8 — микросервисная система заказов и доставки

Проект демонстрирует полный путь заказа через независимые сервисы поставщиков,
заказов и доставки. У каждого бизнес-сервиса собственный экземпляр PostgreSQL;
обращение к чужим таблицам отсутствует. Внешний клиент работает только через
Traefik и API Gateway.

## Архитектура

```text
Client
  |
  v
Traefik :8090
  |
  v
OAuth2 API Gateway
  |--------------------|--------------------|
  v                    v                    v
Supplier Service    Order Service      Delivery Service
  |                    |   |                |     ^
  v                    |   +--> RabbitMQ ---+     |
suppliers_db            |                         SSE
                       v
                    orders_db              deliveries_db

Infrastructure: Keycloak, Spring Cloud Config (Git), Eureka, RabbitMQ
```

## Что реализовано

- три независимых бизнес-сервиса: Supplier, Order и Delivery;
- отдельный PostgreSQL-контейнер и отдельная схема данных для каждого сервиса;
- Traefik как внешний reverse proxy и Spring Cloud Gateway как точка входа;
- OAuth2/JWT с готовым realm Keycloak;
- Spring Cloud Config с локальным Git-репозиторием внутри Docker-образа;
- Eureka Service Discovery и клиентская балансировка нагрузки;
- синхронный вызов `Order Service -> Supplier Service` через FeignClient;
- timeout, две попытки и Circuit Breaker для вызова поставщика;
- асинхронное событие `OrderCreated` через RabbitMQ;
- идемпотентность HTTP-команды по `clientRequestId`;
- идемпотентность RabbitMQ-потребителя по уникальному `eventId`;
- retry и Dead Letter Queue для необработанных сообщений;
- `X-Request-ID` от Gateway через Feign и RabbitMQ-событие;
- реактивный Delivery Service на WebFlux, Reactor и R2DBC;
- SSE-поток истории и новых состояний заказа;
- health checks всех компонентов;
- unit-тесты бизнес-логики;
- единый запуск всей системы через Docker Compose.

## Ответственность сервисов

| Сервис | Ответственность | Хранилище |
|---|---|---|
| Supplier Service | поставщики, товары и проверка доступности | `suppliers_db` |
| Order Service | идемпотентное создание и чтение заказов | `orders_db` |
| Delivery Service | доставка, переходы статусов и SSE | `deliveries_db` |

Order Service не читает таблицы поставщика: он вызывает Supplier Service по
Feign через Eureka. Delivery Service не читает базу заказов: он получает только
минимальное интеграционное событие `OrderCreated`.

## Запуск

Требуется работающий Docker Desktop. Локальные Java, Maven, PostgreSQL,
RabbitMQ и Keycloak устанавливать не требуется.

```powershell
cd D:\projects\java\7_sem_pkls_zar\pr8
docker compose up --build -d
docker compose ps
```

Первый запуск занимает несколько минут: собираются шесть Java-приложений и
загружаются инфраструктурные образы.

Основные адреса:

| Компонент | Адрес |
|---|---|
| Traefik / единая точка API | `http://localhost:8090` |
| Traefik Dashboard | `http://localhost:8091` |
| Keycloak | `http://localhost:8092` |
| Eureka | `http://localhost:8762` |
| Config Server | `http://localhost:8889` |
| RabbitMQ Management | `http://localhost:15673` |
| Supplier PostgreSQL | `localhost:5441` |
| Order PostgreSQL | `localhost:5442` |
| Delivery PostgreSQL | `localhost:5443` |

RabbitMQ: пользователь `pr8`, пароль `pr8_password`.
Keycloak Admin Console: пользователь `admin`, пароль `admin`.

## API

Все маршруты доступны через `http://localhost:8090`.

| Метод | Маршрут | Назначение |
|---|---|---|
| `GET` | `/api/suppliers` | список поставщиков |
| `GET` | `/api/products` | список товаров |
| `GET` | `/api/products/{id}` | один товар |
| `POST` | `/api/orders` | создать заказ |
| `GET` | `/api/orders` | список заказов |
| `GET` | `/api/orders/{id}` | один заказ |
| `GET` | `/api/deliveries` | список доставок |
| `GET` | `/api/deliveries/{id}` | одна доставка |
| `PUT` | `/api/deliveries/{id}/status` | изменить статус |
| `GET` | `/api/orders/{id}/events` | поток SSE |
