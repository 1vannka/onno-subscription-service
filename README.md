# onno-subscription-service
Сервис учета и управления клиентскими подписками на базе фреймворка [Onno](https://github.com/onno-erp/onno-framework).

## <a id="content"></a>Содержание
- [1. Технологический стек](#tech-stack)
- [2. Постановка задачи](#task-scope)
- [3. Быстрый старт и запуск](#quick-start)
- [4. Архитектура и модель данных](#architecture)
- [5. Бизнес-логика и правила валидации](#business-logic)
- [6. Dashboard](#dashboard)
- [7. Тестирование и обеспечение качества](#testing)
---
## <a id="tech-stack"></a>1. Технологический стек
[К содержанию](#content)

- **Язык и платформа:** Java 21, Spring Boot 3.
- **Бизнес-движок:** Onno Framework
- **База данных:** H2 (файловый режим `jdbc:h2:file:./data/app` для рантайма; изолированный режим в памяти `jdbc:h2:mem:testdb` для тестового контура)
- **Фоновые задачи:** JobRunr / `@ScheduledJob` (периодическая актуализация статусов документов)
- **Сборка и зависимости:** Gradle Wrapper
- **Тестирование:** JUnit 5, AssertJ, Spring Boot Test

---
## <a id="task-scope"></a>2. Постановка задачи 
[К содержанию](#content)

### 2.1. Предметная область
Система автоматизирует учет и биллинг услуг по подписочной модели:
- Клиент регистрируется в системе и пополняет персональный лицевой счет входящими платежами
- Оформление подписки происходит на один или несколько тарифов в рамках единого документа (каждая строка - тариф на N расчетных периодов)
- В момент проведения документа подписки требуемая сумма списывается с лицевого счета; списание в минус блокируется на уровне регистра остатков
- Срок действия подписки рассчитывается динамически как дата начала плюс максимальная длительность среди всех строк документа
- Отмененные подписки не формируют финансовых движений (деньги не списываются, выручка не признается)
- Формируется управленческая отчетность по текущим остаткам лицевых счетов клиентов и накопленной выручке в разрезе тарифов и контрагентов

### 2.2. Соответствие требованиям ТЗ

| Уровень | Требования ТЗ | Статус | Что реализовано |
|---|---|:---:|---|
| **Уровень 1. Модель и интерфейс** | Справочники, документы, регистры, EntityView с форматированием, Layout навигации, базовый CRUD через UI | **Выполнено** | Справочники `Client`, `Tariff`, документы `Payment`, `Subscription` со строками `SubscriptionLine`. Настроены списки, подсказки, ширина колонок, маски валют (`RUB`) и дат. Организовано боковое меню `AppLayout`. |
| **Уровень 2. Бизнес-логика** | Балансовый и оборотный регистры, списание/пополнение, запрет отрицательного сальдо, BusinessRule-валидации, авторасчеты `beforeWrite`, регламентная джоба | **Выполнено** | Регистры `ClientAccount` (остатки) и `TariffRevenue` (выручка). Проведение с контролем остатка. Валидаторы обязательности клиента, доступности тарифа, строк и периодов. Фоновый пересчет статусов `SubscriptionStatusJob`. |
| **Уровень 3. Качество и надежность** | Полное покрытие тестами бизнес-правил, изоляция тестового окружения | **Выполнено** | 26 тестов (20 модульных без контекста Spring + 6 сквозных интеграционных). Тестовый контур изолирован в оперативной памяти (`H2 in-memory`). Составлена матрица трассируемости требований (RTM). |
| **Дополнительно (раздел 5 ТЗ)** | Аналитический дашборд, кастомные действия в списках и формах через ActionSpec | **Выполнено** | Аналитический `DashboardPage` (KPI-карточки, диаграммы распределения оплат и статусов, реестр). Модальное действие отмены подписки в строке и карточке документа с вводом причины (`ActionSpec`) и автоматическим откатом движений. |

---
## <a id="quick-start"></a>3. Быстрый старт и запуск
[К содержанию](#content)

### 3.1. Системные требования
- JDK 21 или выше
- Git

### 3.2. Запуск приложения

Склонируйте репозиторий и запустите сервис через Gradle Wrapper:
```bash
git clone <URL_РЕПОЗИТОРИЯ>
cd onno-subscription-service
./gradlew bootRun
```

Сервис запустится на порту 8080. При первом старте компонент `SubscriptionSeeder` автоматически наполняет базу демонстрационными данными:
- 6 контрагентов с различными статусами (`ACTIVE`, `BLOCKED`);
- 5 тарифных планов с разной периодичностью (от 30 до 365 дней, включая архивный);
- 8 платежей, распределенных по способам оплаты (`BANK_CARD`, `BANK_TRANSFER`, `CASH`);
- 8 документов подписок во всех статусах жизненного цикла (`ACTIVE`, `EXPIRED`, `CANCELLED`, `DRAFT`) с проведенными движениями по счетам.

### 3.3. Параметры доступа к веб-интерфейсу
- **URL:** [http://localhost:8080](http://localhost:8080)
- **Логин:** `admin`
- **Пароль:** `admin`

После входа открывается стартовая аналитическая панель (`Dashboard`), а в боковом меню доступны разделы операций, справочников и регистров.

### 3.4. Запуск тестового набора
Тестовый контур изолирован в оперативной памяти и не затрагивает файл рабочей базы данных. Для запуска тестов выполните в терминале:
```bash
./gradlew test
```

### 3.5. Сброс данных к начальному состоянию
Рабочая база данных сохраняется в локальной папке `./data`. Если требуется полностью очистить базу и пересоздать демонстрационный набор заново:
```bash
rm -rf data/
./gradlew bootRun
```

---
## <a id="architecture"></a>4. Архитектура и модель данных
[К содержанию](#content)

Архитектура сервиса построена на метамодели Onno Framework с четким разделением на справочники, документы с табличной частью и регистры накопления.

### 4.1. Основные сущности и регистры

| Сущность / Регистр | Класс метамодели | Ключевые реквизиты | Роль в системе учета |
|---|---|---|---|
| **Клиент** | `Client` (`CatalogObject`) | `status`, `email`, `phone`, `registrationDate`, `name` | Контрагент, владелец лицевого счета и измерение в регистрах |
| **Тариф** | `Tariff` (`CatalogObject`) | `pricePerPeriod`, `periodDurationDays`, `availableForConnection`, `name` | Прейскурант услуг с контролем доступности для новых подключений |
| **Платёж** | `Payment` (`DocumentObject`) | `client`, `amount`, `paymentMethod`, `date` | Факт внесения средств, формирует приход в балансовый регистр |
| **Подписка** | `Subscription` (`DocumentObject`) | `client`, `status`, `startDate`, `endDate`, `total`, `cancellationReason`, `lines` | Документ продажи: списывает баланс и фиксирует выручку |
| **Строка подписки** | `SubscriptionLine` (`TabularSectionRow`) | `tariff`, `periods`, `price`, `amount` | Табличная часть с перечнем тарифов и количеством расчетных периодов |
| **Лицевой счёт** | `ClientAccount` (`BALANCE`) | **Измерения:** `client`<br>**Ресурсы:** `amount` | Балансовый регистр: контроль остатка клиента, защита от ухода в минус |
| **Выручка по тарифам** | `TariffRevenue` (`TURNOVER`) | **Измерения:** `tariff`, `client`<br>**Ресурсы:** `amount`, `periods` | Оборотный регистр: учет объема продаж в разрезе тарифов и клиентов |

### 4.2. Перечисления (Enums)

| Перечисление | Возможные значения | Описание |
|---|---|---|
| **ClientStatus** | `ACTIVE`, `BLOCKED`, `INACTIVE` | Статус клиента |
| **PaymentMethod** | `CASH`, `BANK_CARD`, `BANK_TRANSFER` | Способ оплаты |
| **SubscriptionStatus** | `DRAFT`, `ACTIVE`, `EXPIRED`, `CANCELLED` | Жизненный цикл подписки  |

### 4.3. Навигация в интерфейсе 

Панель управления сгруппирована по функциональным разделам:
- **Аналитика:** стартовая страница Dashboard (`/`)
- **Операции:** журнал документов `Subscriptions` и `Payments`
- **Справочники:** реестры `Clients` и `Tariffs`
- **Регистры:** отчетные формы `Client accounts` и `Tariff Revenue`
---
## <a id="business-logic"></a>5. Бизнес-логика и правила валидации
[К содержанию](#content)

Бизнес-логика сервиса реализована через механизмы жизненного цикла Onno Framework (`BeforeWriteHandler`, `OnFillingHandler`, `Postable`, `Validated`), декларативные бизнес-правила и регламентные задачи.

### 5.1. Автоподстановка и расчеты 

При создании и сохранении документов автоматически выполняются расчеты:
- **Инициализация дат:** если дата документа или дата старта (`startDate`) не заданы пользователем, автоматически выставляется текущее число.
- **Подтягивание цен:** для каждой строки подписки цена за период (`price`) берется из актуального прейскуранта тарифа через справочный резолвер `DomainLookups.tariff(...)`.
- **Расчет стоимостей:** сумма строки рассчитывается как `price * periods`, а общая сумма документа (`total`) - как сумма по всем строкам.
- **Динамический срок действия:** дата окончания (`endDate`) вычисляется как `startDate + max(durationDays * periods)` среди всех строк документа. Если в одном документе объединены годовой тариф (365 дней) и месячный (30 дней), срок подписки составит 365 дней.
- **Автоматическая активация:** при проведении документа в статусе `DRAFT` с датой старта `<= today`, статус переводится в `ACTIVE`.

### 5.2. Декларативные правила валидации (BusinessRule)

Перед сохранением и проведением система выполняет валидацию бизнес-ограничений:
- **Для платежа:**
  - `client-required`: ссылка на клиента обязательна.
  - `amount-positive`: сумма платежа строго больше нуля.
  - `payment-method-required`: способ оплаты обязателен к заполнению.
- **Для подписки:**
  - `client-required`: ссылка на клиента обязательна.
  - `lines-required`: в табличной части должна присутствовать хотя бы одна строка.
  - `periods-positive`: число расчетных периодов в каждой строке строго больше нуля.
  - `tariff-available`: каждый выбранный тариф должен быть активен. Архивные тарифы сохранять запрещено.

### 5.3. Механика проведения и учет в регистрах 

Проведение документов выполняется через `PostingService`:
- **Проведение платежа:** формирует приходное движение (`addReceipt`) в балансовый регистр `ClientAccount`, увеличивая остаток на счете клиента.
- **Проведение подписки:**
  - Формирует расходное движение (`addExpense`) в регистр `ClientAccount` на сумму `total`. Если остатка на счете клиента недостаточно, проведение блокируется платформой с ошибкой нехватки средств.
  - Формирует приходные движения (`addReceipt`) в оборотный регистр `TariffRevenue` по каждой строке документа с фиксацией суммы и периодов в разрезе тарифа и контрагента.
- **Изоляция отмененных документов:** если подписка имеет статус `CANCELLED`, проведение не формирует движений (деньги со счета не списываются, выручка не признается).

### 5.4. Жизненный цикл и отмена подписки 

Для управления отменой подписок реализовано модальное действие:
- Доступно как в таблице журнала подписок (`cancelRow`), так и внутри карточки документа (`cancelDetail`).
- Кнопка отображается только для подписок, не находящихся в статусе `CANCELLED`.
- Открывает модальное окно с обязательным вводом причины отмены (`reason`).
- Обработчик действия:
  1. Если документ был проведен, вызывает `postingService.unpost(sub)`, что автоматически сторнирует все записи в регистрах и возвращает баланс клиенту.
  2. Переводит подписку в статус `CANCELLED`.
  3. Сохраняет введенный комментарий в поле `cancellationReason`.

### 5.5. Фоновая актуализация статусов (SubscriptionStatusJob)

Регламентная задача на базе `@ScheduledJob` запускается в фоне и актуализирует статусы документов по датам:
- Переводит подписки с наступившей датой старта (`startDate <= today`) в статус `ACTIVE`.
- Переводит подписки с истекшим сроком действия (`endDate < today`) в статус `EXPIRED`.
---
## <a id="dashboard"></a>6. Dashboard
[К содержанию](#content)

Стартовая страница приложения (`/`) реализована на базе `PageBuilder` (Onno Framework) и предоставляет сводную бизнес-аналитику в реальном времени:

- **Ключевые метрики:** общее число клиентов, суммарное количество оформленных подписок, совокупный объем подписок в рублях и общая сумма поступивших оплат.
- **Интерактивные диаграммы:**
  - распределение подписок по статусам жизненного цикла (`DRAFT`, `ACTIVE`, `EXPIRED`, `CANCELLED`);
  - распределение платежей по способам оплаты (`BANK_CARD`, `BANK_TRANSFER`, `CASH`).
- **Последние операции:** таблица последних созданных подписок с отображением клиентов, статусов, дат действия и итоговых сумм с сортировкой от свежих к старым.
---

## <a id="testing"></a>7. Тестирование
[К содержанию](#content)

Тестовый набор покрывает бизнес-логику, правила валидации, жизненный цикл документов и корректность движений по регистрам.

### 7.1. Организация тестового контура
- **Пирамида тестирования:** 26 тестов:
  - 20 модульных тестов: изолированная проверка формул, дат, граничных условий и валидаторов без накладных расходов на поднятие Spring-контекста;
  - 6 интеграционных тестов: сквозная проверка сценариев создания, проведения документов и записи проводок в базу данных через сервисы Onno.
- **Полная изоляция данных:** тестовый контур работает исключительно в оперативной памяти (`jdbc:h2:mem:testdb`) под отдельным `src/test/resources/application.yml`. Прогон тестов гарантированно не затрагивает и не засоряет рабочую файловую базу (`./data/app`).
- **Запуск тестов:** `./gradlew test`

### 7.2. Матрица трассируемости требований 

| № | Требование ТЗ / Инвариант | Механизм в коде | Тестовый метод | Тип |
|---|---|---|---|---|
| 1 | Клиент обязателен в платеже (отсутствует) | `Payment.rules()` (`client-required`) | `PaymentValidationTest.testClientRequiredFailsWithoutClient` | Unit |
| 2 | Клиент обязателен в платеже (заполнен) | `Payment.rules()` (`client-required`) | `PaymentValidationTest.testClientRequiredPassesWithValidReference` | Unit |
| 3 | Сумма платежа строго > 0 (null, ноль, отрицательная) | `Payment.rules()` (`amount-positive`) | `PaymentValidationTest.testAmountPositiveFailsForNullZeroAndNegativeAmounts` | Unit |
| 4 | Сумма платежа строго > 0 (положительное число) | `Payment.rules()` (`amount-positive`) | `PaymentValidationTest.testAmountPositivePassesForPositiveAmount` | Unit |
| 5 | Способ оплаты обязателен (отсутствует) | `Payment.rules()` (`payment-method-required`) | `PaymentValidationTest.testPaymentMethodRequiredFailsWithoutPaymentMethod` | Unit |
| 6 | Способ оплаты обязателен (выбран из перечисления) | `Payment.rules()` (`payment-method-required`) | `PaymentValidationTest.testPaymentMethodRequiredPassesWithPaymentMethod` | Unit |
| 7 | Подтягивание цены из тарифа и расчет суммы строки | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testLineAmountsAreCalculatedFromTariffPrice` | Unit |
| 8 | Итоговая сумма подписки равна сумме всех строк | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testTotalIsSumOfAllLineAmounts` | Unit |
| 9 | Срок действия = дата начала + макс. длительность строк | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testEndDateUsesLongestLineDuration` | Unit |
| 10 | Защита от NPE при отсутствии строк (null) | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testBeforeWriteHandlesNullLines` | Unit |
| 11 | Корректный расчет при пустом списке строк | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testBeforeWriteHandlesEmptyLines` | Unit |
| 12 | Корректная обработка нулевых и null-периодов | `Subscription.beforeWrite()` | `SubscriptionCalculationTest.testBeforeWriteHandlesNullAndZeroPeriods` | Unit |
| 13 | Клиент обязателен в подписке (отсутствует) | `Subscription.rules()` (`client-required`) | `SubscriptionValidationTest.testClientRequiredFailsWithoutClient` | Unit |
| 14 | Клиент обязателен в подписке (заполнен) | `Subscription.rules()` (`client-required`) | `SubscriptionValidationTest.testClientRequiredPassesWithValidReference` | Unit |
| 15 | Хотя бы одна строка в подписке (null или пусто) | `Subscription.rules()` (`lines-required`) | `SubscriptionValidationTest.testLinesRequiredFailsForNullOrEmptyLines` | Unit |
| 16 | Хотя бы одна строка в подписке (строка добавлена) | `Subscription.rules()` (`lines-required`) | `SubscriptionValidationTest.testLinesRequiredPassesWithLine` | Unit |
| 17 | Число периодов строго > 0 (null, ноль, минус) | `Subscription.rules()` (`periods-positive`) | `SubscriptionValidationTest.testPeriodsPositiveFailsForNullOrNonPositivePeriods` | Unit |
| 18 | Число периодов строго > 0 (валидные значения) | `Subscription.rules()` (`periods-positive`) | `SubscriptionValidationTest.testPeriodsPositivePassesWhenAllLinesArePositive` | Unit |
| 19 | Запрет подключения архивного тарифа | `Subscription.rules()` (`tariff-available`) | `SubscriptionValidationTest.testTariffAvailableFailsWhenAnyTariffIsUnavailable` | Unit |
| 20 | Разрешение подключения активного тарифа | `Subscription.rules()` (`tariff-available`) | `SubscriptionValidationTest.testTariffAvailablePassesWhenAllTariffsAreAvailable` | Unit |
| 21 | Запрет проведения подписки при нехватке денег | `ClientAccount` (`BALANCE`) в `PostingEngine` | `SubscriptionIntegrationTest.testPostingFailsWhenInsufficientBalance` | Integration |
| 22 | Пополнение счета, списание и контроль остатка | `Payment.handlePosting`, `Subscription.handlePosting` | `SubscriptionIntegrationTest.testPaymentAndSubscriptionBalanceLifecycle` | Integration |
| 23 | Отмененная подписка не списывает деньги со счета | `Subscription.handlePosting` (обход при `CANCELLED`) | `SubscriptionIntegrationTest.testCancelledSubscriptionDoesNotRequireMoney` | Integration |
| 24 | Фиксация выручки в регистре TariffRevenue | `Subscription.handlePosting` -> `TariffRevenue.addReceipt` | `SubscriptionIntegrationTest.testSubscriptionPostingRecognizesTariffRevenue` | Integration |
| 25 | Перевод подписок в ACTIVE и EXPIRED по датам | `SubscriptionStatusJob.execute()` | `SubscriptionIntegrationTest.testSubscriptionStatusJobTransitions` | Integration |
| 26 | Отмена проведения (unpost) восстанавливает баланс | Механизм отката `PostingService.unpost()` | `SubscriptionIntegrationTest.testUnpostingReversesMovements` | Integration |
