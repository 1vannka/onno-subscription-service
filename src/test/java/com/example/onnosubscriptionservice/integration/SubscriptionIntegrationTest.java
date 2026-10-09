package com.example.onnosubscriptionservice.integration;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.documents.Payment;
import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.domain.documents.SubscriptionLine;
import com.example.onnosubscriptionservice.domain.enums.ClientStatus;
import com.example.onnosubscriptionservice.domain.enums.PaymentMethod;
import com.example.onnosubscriptionservice.domain.enums.SubscriptionStatus;
import com.example.onnosubscriptionservice.repositories.ClientRepository;
import com.example.onnosubscriptionservice.repositories.PaymentRepository;
import com.example.onnosubscriptionservice.repositories.SubscriptionRepository;
import com.example.onnosubscriptionservice.repositories.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import su.onno.posting.PostingService;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubscriptionIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PostingService postingService;

    @Autowired
    private com.example.onnosubscriptionservice.jobs.SubscriptionStatusJob subscriptionStatusJob;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Ref<Client> clientRef;
    private Ref<Tariff> tariffRef;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setName("Клиент Интеграционный " + UUID.randomUUID());
        client.setEmail("integration_" + UUID.randomUUID() + "@example.com");
        client.setStatus(ClientStatus.ACTIVE);
        client = clientRepository.save(client);
        clientRef = Ref.of(Client.class, client.getId());

        Tariff tariff = new Tariff();
        tariff.setName("Тариф Тестовый 1000");
        tariff.setPricePerPeriod(new BigDecimal("1000.00"));
        tariff.setPeriodDurationDays(30);
        tariff.setAvailableForConnection(true);
        tariff = tariffRepository.save(tariff);
        tariffRef = Ref.of(Tariff.class, tariff.getId());
    }

    @Test
    @DisplayName("Проведение без средств: падает с ошибкой отрицательного баланса")
    void testPostingFailsWhenInsufficientBalance() {
        Subscription sub = createSubscription(clientRef, tariffRef, 1);
        sub = subscriptionRepository.save(sub);

        Subscription finalSub = sub;
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> postingService.post(finalSub));
        assertTrue(ex.getMessage().contains("ClientAccounts") || ex.getMessage().contains("negative balance"));
    }

    @Test
    @DisplayName("Цикл оплаты и списания: пополнение счета и корректный учет остатка")
    void testPaymentAndSubscriptionBalanceLifecycle() {
        Payment payment = new Payment();
        payment.setClient(clientRef);
        payment.setAmount(new BigDecimal("2500.00"));
        payment.setPaymentMethod(PaymentMethod.BANK_CARD);
        payment.setDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        postingService.post(payment);

        Subscription sub1 = createSubscription(clientRef, tariffRef, 2);
        sub1 = subscriptionRepository.save(sub1);
        postingService.post(sub1);

        assertTrue(sub1.isPosted());

        Subscription sub2 = createSubscription(clientRef, tariffRef, 1);
        sub2 = subscriptionRepository.save(sub2);

        Subscription finalSub2 = sub2;
        assertThrows(IllegalStateException.class, () -> postingService.post(finalSub2));
    }

    @Test
    @DisplayName("Отмененная подписка не списывает деньги и проводится без ошибок")
    void testCancelledSubscriptionDoesNotRequireMoney() {
        Subscription sub = createSubscription(clientRef, tariffRef, 5);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub = subscriptionRepository.save(sub);

        Subscription finalSub = sub;
        assertDoesNotThrow(() -> postingService.post(finalSub));
        assertTrue(finalSub.isPosted());
    }

    @Test
    @DisplayName("Выручка: проведение подписки формирует записи в оборотном регистре TariffRevenue")
    void testSubscriptionPostingRecognizesTariffRevenue() {
        Payment payment = new Payment();
        payment.setClient(clientRef);
        payment.setAmount(new BigDecimal("3000.00"));
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        postingService.post(payment);

        Subscription sub = createSubscription(clientRef, tariffRef, 3);
        sub = subscriptionRepository.save(sub);
        postingService.post(sub);

        String tableName = jdbcTemplate.queryForObject(
                "SELECT table_name FROM information_schema.tables WHERE UPPER(table_name) LIKE '%TARIFF%REVENUE%' LIMIT 1",
                String.class
        );
        assertNotNull(tableName, "Таблица регистра TariffRevenue должна существовать в БД");

        var rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName);
        assertFalse(rows.isEmpty(), "В регистре TariffRevenue должны появиться записи о выручке");
    }

    @Test
    @DisplayName("Регламентная джоба: перевод проведенных подписок в ACTIVE и EXPIRED по датам")
    void testSubscriptionStatusJobTransitions() {
        Subscription activeCandidate = createSubscription(clientRef, tariffRef, 1);
        activeCandidate.setStartDate(LocalDate.now());
        activeCandidate.setStatus(SubscriptionStatus.DRAFT);
        activeCandidate.setPosted(true);
        activeCandidate = subscriptionRepository.save(activeCandidate);

        Subscription expiredCandidate = createSubscription(clientRef, tariffRef, 1);
        expiredCandidate.setStartDate(LocalDate.now().minusDays(40));
        expiredCandidate.setEndDate(LocalDate.now().minusDays(1));
        expiredCandidate.setStatus(SubscriptionStatus.ACTIVE);
        expiredCandidate.setPosted(true);
        expiredCandidate = subscriptionRepository.save(expiredCandidate);

        subscriptionStatusJob.execute();

        Subscription updatedActive = subscriptionRepository.findById(activeCandidate.getId()).orElseThrow();
        assertEquals(SubscriptionStatus.ACTIVE, updatedActive.getStatus());

        Subscription updatedExpired = subscriptionRepository.findById(expiredCandidate.getId()).orElseThrow();
        assertEquals(SubscriptionStatus.EXPIRED, updatedExpired.getStatus());
    }

    @Test
    @DisplayName("Отмена проведения (Unpost): восстанавливает баланс клиента")
    void testUnpostingReversesMovements() {
        Payment payment = new Payment();
        payment.setClient(clientRef);
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setPaymentMethod(PaymentMethod.BANK_CARD);
        payment.setDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        postingService.post(payment);

        Subscription sub = createSubscription(clientRef, tariffRef, 1);
        sub = subscriptionRepository.save(sub);
        postingService.post(sub);

        Subscription sub2 = createSubscription(clientRef, tariffRef, 1);
        sub2 = subscriptionRepository.save(sub2);
        Subscription finalSub2 = sub2;
        assertThrows(IllegalStateException.class, () -> postingService.post(finalSub2));

        postingService.unpost(sub);

        assertDoesNotThrow(() -> postingService.post(finalSub2));
        assertTrue(finalSub2.isPosted());
    }

    private Subscription createSubscription(Ref<Client> client, Ref<Tariff> tariff, int periods) {
        Subscription sub = new Subscription();
        sub.setClient(client);
        sub.setDate(LocalDateTime.now());
        sub.setStartDate(LocalDate.now());

        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(tariff);
        line.setPeriods(periods);
        sub.getLines().add(line);

        sub.beforeWrite();
        return sub;
    }
}