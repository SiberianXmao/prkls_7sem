# Практическая работа №3. Реактивное программирование с RxJava

Консольное приложение обрабатывает собственный набор данных поставщиков как
реактивный поток RxJava 3. 
Используется `Flowable`, потому что кроме обычной цепочки задание требует
продемонстрировать ситуацию, когда producer быстрее consumer, и применить
механизм backpressure.

## Модель данных

Исходный `SupplierProduct` содержит:

- `supplierId`;
- `supplierName`;
- `product`;
- `price`;
- `quantity`.

После преобразования создаётся `ProductView` с общей стоимостью доступного
товара и именем потока, в котором выполнена обработка.

## Реактивная цепочка

```text
Flowable<SupplierProduct>
        ↓ subscribeOn(Schedulers.io())
flatMap: валидация каждого элемента и локальная обработка ошибки
        ↓
filter: quantity > 0
        ↓ observeOn(Schedulers.computation())
map: SupplierProduct → ProductView + price × quantity
        ↓
groupBy: группировка по supplierName
        ↓
reduce: суммарная стоимость по каждому поставщику
        ↓
toList: итоговый список SupplierTotal
```

Ошибка одного элемента перехватывается внутри `flatMap` через
`onErrorResumeNext`: создаётся `ProcessingIssue`, проблемный элемент исключается,
а основной поток продолжает обрабатывать следующие данные. Поэтому ошибки не
доходят до подписчика как необработанный `onError`.

## Реализованные требования

- модель предметной области поставщиков и товаров;
- реактивный источник `Flowable` из нескольких объектов;
- операторы `flatMap`, `filter` и `map`;
- группировка и агрегация через `groupBy`, `reduce`, `toList`;
- обработка некорректных элементов без завершения всего потока;
- `subscribeOn` и `observeOn` с выводом имён потоков;
- автоматическое поступление новых элементов через `delay` и `concatMap`;
- backpressure с ограниченным буфером и стратегией `DROP_OLDEST`;
- 7 тестов, включая `TestScheduler` и проверку переполнения буфера.

## Структура проекта

```text
src
├── main/java/ru/pr3
│   ├── app
│   │   ├── Main.java
│   │   └── SampleData.java
│   ├── model
│   │   ├── SupplierProduct.java
│   │   ├── ProductView.java
│   │   ├── SupplierTotal.java
│   │   ├── ProcessingIssue.java
│   │   └── BackpressureReport.java
│   └── reactive
│       ├── ReactiveProductService.java
│       ├── TimedDataSource.java
│       └── BackpressureDemo.java
└── test/java/ru/pr3/reactive
    ├── ReactiveProductServiceTest.java
    ├── TimedDataSourceTest.java
    └── BackpressureDemoTest.java
```

## Сборка и запуск

Проект настроен на Java 25, RxJava 3.1.12 и JUnit 5.

```powershell
java --version
mvn --version
mvn clean test
mvn exec:java
```

Если Maven не установлен локально, проект можно проверить официальным
Maven-образом с Java 25:

```powershell
$project = (Get-Location).Path

docker run --rm `
  -v "${project}:/workspace" `
  -v pr3-maven-cache:/root/.m2 `
  -w /workspace `
  maven:3.9.16-eclipse-temurin-25 `
  mvn test

docker run --rm `
  -v "${project}:/workspace" `
  -v pr3-maven-cache:/root/.m2 `
  -w /workspace `
  maven:3.9.16-eclipse-temurin-25 `
  mvn -q exec:java
```

Контейнер одноразовый; volume `pr3-maven-cache` хранит только скачанные Maven-
зависимости. PostgreSQL-контейнер практической №2 не используется.

## Результат контрольного запуска

После фильтрации получено 5 доступных товаров. Товар с нулевым количеством
исключён, ещё два некорректных элемента зарегистрированы как ошибки.

| Поставщик | Суммарная стоимость |
|---|---:|
| Офис, Сервис | 195436,00 |
| Северный склад | 134820,00 |
| ТехноИмпорт | 599670,00 |

В выводе видна смена scheduler:

```text
Источник  | Механическая клавиатура | thread=RxCachedThreadScheduler-1
Обработка | Механическая клавиатура | thread=RxComputationThreadPool-1
```

Затем три новых товара поступают с интервалом 150 мс и автоматически проходят
ту же цепочку.

В демонстрации backpressure быстрый источник сформировал 300 элементов,
медленный consumer работал с задержкой 5 мс, а буфер вмещал 8 элементов:

```text
Produced: 300
Consumed: 54
Dropped: 246
Buffer capacity: 8
```

Конкретное число обработанных элементов может немного отличаться между
запусками из-за планирования потоков, но выполняется инвариант:
`Produced = Consumed + Dropped`.
