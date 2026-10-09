# onno-subscription-service

### Матрица трассируемости требований

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