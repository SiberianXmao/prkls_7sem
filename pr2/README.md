# Практическая работа №2. Java NIO

Приложение автоматически обрабатывает файлы обмена данными поставщиков.
CSV/JSON-наборы с теми же предметными полями:

- `supplierId` — ID поставщика;
- `supplierName` — название поставщика;
- `product` — товар;
- `price` — цена;
- `quantity` — количество.

Основной сценарий:

```text
появление файла в incoming
        ↓
обнаружение расширения CSV/JSON
        ↓
чтение через Java NIO
        ↓
валидация и преобразование записей
        ↓
SHA-256 + 16-битная контрольная сумма
        ↓
сохранение товаров и журнала в PostgreSQL
        ↓
атомарное перемещение файла в processed
```

## Реализованные требования

- создание `data/incoming`, `data/processed` и `data/failed` через
  `Path` и `Files.createDirectories`;
- вывод имени, размера и времени изменения каждого входного файла;
- поиск файлов по расширениям `.csv` и `.json`;
- чтение CSV через `FileChannel`, прямой `ByteBuffer` и `CharsetDecoder`;
- чтение и преобразование JSON;
- вычисление SHA-256 каждого файла через `FileChannel` и `ByteBuffer`;
- дополнительная 16-битная контрольная сумма из методички;
- перемещение обработанных файлов с попыткой `ATOMIC_MOVE`;
- перенос некорректных файлов в `failed`;
- `WatchService` для автоматической обработки вновь созданных файлов;
- ожидание стабилизации размера файла, чтобы не прочитать его посередине копирования;
- PostgreSQL в Docker и транзакционная запись через JDBC;
- in-memory режим для автономных тестов без базы данных.

## Структура

```text
pr2/
├── compose.yaml
├── pom.xml
├── samples/                    четыре исходных тестовых файла
├── data/
│   ├── incoming/
│   ├── processed/
│   └── failed/
└── src/
    ├── main/java/ru/pr2/
    │   ├── app/                Main, workflow и WatchService
    │   ├── db/                 PostgreSQL и in-memory репозитории
    │   ├── fs/                 Path/Files и рабочие каталоги
    │   ├── model/              модель SupplierProduct
    │   └── processing/         парсеры, NIO-чтение и checksum
    └── test/java/ru/pr2/       7 тестов
```

## Подготовка

Требуются Java 21 или новее, Maven и запущенный Docker Desktop.

```powershell
java --version
mvn --version
docker compose up -d
docker compose ps
mvn clean test
```

Контейнер публикует PostgreSQL на `localhost:5434`:

| Параметр | Значение |
|---|---|
| Database | `suppliers` |
| User | `pr2` |
| Password | `pr2` |
| JDBC URL | `jdbc:postgresql://localhost:5434/suppliers` |

Схема создаётся самим Java-приложением. Таблица `supplier_products` содержит
актуальные сведения о товарах, а `processing_log` — имя, размер, SHA-256,
число записей и время обработки файла.

## Команды приложения

Класс `ru.pr2.app.Main` можно запускать кнопкой Run из IDE без аргументов.
Он автоматически находит папку `pr2`, даже если рабочей директорией выбран
корень общего проекта. Для штатного запуска PostgreSQL должен быть поднят
командой `docker compose up -d` из папки `pr2`.

Подготовить три файла и выполнить весь сценарий:

```powershell
mvn exec:java "-Dexec.args=demo"
```

Другие команды:

```powershell
# Только скопировать три примера в incoming
mvn exec:java "-Dexec.args=seed"

# Обработать то, что уже находится в incoming
mvn exec:java "-Dexec.args=process"

# Вывести имя, размер и время изменения входных файлов
mvn exec:java "-Dexec.args=list"

# Наблюдать за incoming в течение 60 секунд
mvn exec:java "-Dexec.args=watch 60"

# Выполнить demo без PostgreSQL
mvn exec:java "-Dexec.args=demo --no-db"
```

Пути и подключение можно переопределять системными свойствами:

```powershell
mvn exec:java `
  "-Dpr2.data.dir=D:\temp\pr2-data" `
  "-Dpr2.db.url=jdbc:postgresql://localhost:5434/suppliers" `
  "-Dpr2.db.user=pr2" `
  "-Dpr2.db.password=pr2" `
  "-Dexec.args=demo"
```
