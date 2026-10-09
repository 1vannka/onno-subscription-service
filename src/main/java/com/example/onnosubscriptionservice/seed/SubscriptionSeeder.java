package com.example.onnosubscriptionservice.seed;

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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import su.onno.posting.PostingService;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SubscriptionSeeder implements ApplicationRunner {

    private final ClientRepository clientRepository;
    private final TariffRepository tariffRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PostingService postingService;

    public SubscriptionSeeder(ClientRepository clientRepository,
                              TariffRepository tariffRepository,
                              PaymentRepository paymentRepository,
                              SubscriptionRepository subscriptionRepository,
                              PostingService postingService) {
        this.clientRepository = clientRepository;
        this.tariffRepository = tariffRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.postingService = postingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (tariffRepository.count() > 0) {
            return;
        }

        Ref<Tariff> basicTariff = createTariff("Базовый", new BigDecimal("500.00"), 30, true);
        Ref<Tariff> standardTariff = createTariff("Стандарт", new BigDecimal("1500.00"), 30, true);
        Ref<Tariff> proTariff = createTariff("Профессиональный", new BigDecimal("4000.00"), 90, true);
        Ref<Tariff> annualTariff = createTariff("Корпоративный Годовой", new BigDecimal("15000.00"), 365, true);
        Ref<Tariff> legacyTariff = createTariff("Архивный Старт", new BigDecimal("350.00"), 30, false);

        Ref<Client> clientVector = createClient("ООО 'Вектор Плюс'", "billing@vector-plus.ru", "+7 999 111-22-33", ClientStatus.ACTIVE, 60);
        Ref<Client> clientDataLab = createClient("АО 'ДатаЛаб'", "finance@datalab.tech", "+7 999 222-33-44", ClientStatus.ACTIVE, 45);
        Ref<Client> clientRetail = createClient("ООО 'Ритейл Системы'", "it@retail-sys.ru", "+7 999 333-44-55", ClientStatus.ACTIVE, 30);
        Ref<Client> clientSmirnov = createClient("ИП Смирнов А.В.", "smirnov.dev@mail.ru", "+7 999 444-55-66", ClientStatus.ACTIVE, 20);
        Ref<Client> clientKuznetsov = createClient("Кузнецов Максим Сергеевич", "kuznetsov.m@gmail.com", "+7 999 555-66-77", ClientStatus.ACTIVE, 15);
        Ref<Client> clientBlocked = createClient("ООО 'Телеком Дроп'", "bad-debt@telecom.ru", "+7 999 666-77-88", ClientStatus.BLOCKED, 90);

        createAndPostPayment(clientVector, new BigDecimal("20000.00"), PaymentMethod.BANK_TRANSFER, LocalDateTime.now().minusDays(25));
        createAndPostPayment(clientDataLab, new BigDecimal("15000.00"), PaymentMethod.BANK_TRANSFER, LocalDateTime.now().minusDays(20));
        createAndPostPayment(clientDataLab, new BigDecimal("5000.00"), PaymentMethod.BANK_CARD, LocalDateTime.now().minusDays(5));
        createAndPostPayment(clientRetail, new BigDecimal("6000.00"), PaymentMethod.BANK_TRANSFER, LocalDateTime.now().minusDays(18));
        createAndPostPayment(clientSmirnov, new BigDecimal("5000.00"), PaymentMethod.BANK_CARD, LocalDateTime.now().minusDays(14));
        createAndPostPayment(clientSmirnov, new BigDecimal("3000.00"), PaymentMethod.CASH, LocalDateTime.now().minusDays(4));
        createAndPostPayment(clientKuznetsov, new BigDecimal("2500.00"), PaymentMethod.BANK_CARD, LocalDateTime.now().minusDays(40));
        createAndPostPayment(clientKuznetsov, new BigDecimal("1000.00"), PaymentMethod.CASH, LocalDateTime.now().minusDays(2));


        createSubscription(clientVector, annualTariff, 1, LocalDate.now().minusDays(20), SubscriptionStatus.ACTIVE, true);

        Subscription multiSub = new Subscription();
        multiSub.setClient(clientDataLab);
        multiSub.setDate(LocalDateTime.now().minusDays(15));
        multiSub.setStartDate(LocalDate.now().minusDays(15));
        multiSub.setStatus(SubscriptionStatus.ACTIVE);
        multiSub.getLines().add(createLine(standardTariff, 2));
        multiSub.getLines().add(createLine(proTariff, 1));
        multiSub.beforeWrite();
        multiSub = subscriptionRepository.save(multiSub);
        postingService.post(multiSub);

        createSubscription(clientRetail, standardTariff, 2, LocalDate.now().minusDays(10), SubscriptionStatus.ACTIVE, true);

        createSubscription(clientSmirnov, proTariff, 1, LocalDate.now().minusDays(3), SubscriptionStatus.ACTIVE, true);

        createSubscription(clientKuznetsov, basicTariff, 1, LocalDate.now().minusDays(40), SubscriptionStatus.EXPIRED, true);

        Subscription cancelledSub = new Subscription();
        cancelledSub.setClient(clientBlocked);
        cancelledSub.setDate(LocalDateTime.now().minusDays(12));
        cancelledSub.setStartDate(LocalDate.now().minusDays(12));
        cancelledSub.setStatus(SubscriptionStatus.CANCELLED);
        cancelledSub.getLines().add(createLine(standardTariff, 1));
        cancelledSub.beforeWrite();
        cancelledSub = subscriptionRepository.save(cancelledSub);
        postingService.post(cancelledSub);

        createSubscription(clientSmirnov, standardTariff, 1, LocalDate.now(), SubscriptionStatus.DRAFT, false);
        createSubscription(clientRetail, annualTariff, 1, LocalDate.now(), SubscriptionStatus.DRAFT, false);
    }

    private Ref<Tariff> createTariff(String name, BigDecimal price, int days, boolean available) {
        Tariff t = new Tariff();
        t.setName(name);
        t.setPricePerPeriod(price);
        t.setPeriodDurationDays(days);
        t.setAvailableForConnection(available);
        return Ref.of(Tariff.class, tariffRepository.save(t).getId());
    }

    private Ref<Client> createClient(String name, String email, String phone, ClientStatus status, int daysAgo) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setStatus(status);
        c.setRegistrationDate(LocalDate.now().minusDays(daysAgo));
        return Ref.of(Client.class, clientRepository.save(c).getId());
    }

    private void createAndPostPayment(Ref<Client> client, BigDecimal amount, PaymentMethod method, LocalDateTime date) {
        Payment p = new Payment();
        p.setClient(client);
        p.setAmount(amount);
        p.setPaymentMethod(method);
        p.setDate(date);
        Payment saved = paymentRepository.save(p);
        postingService.post(saved);
    }

    private SubscriptionLine createLine(Ref<Tariff> tariff, int periods) {
        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(tariff);
        line.setPeriods(periods);
        return line;
    }

    private void createSubscription(Ref<Client> client, Ref<Tariff> tariff, int periods,
                                    LocalDate startDate, SubscriptionStatus status, boolean post) {
        Subscription sub = new Subscription();
        sub.setClient(client);
        sub.setDate(startDate.atStartOfDay());
        sub.setStartDate(startDate);
        sub.setStatus(status);
        sub.getLines().add(createLine(tariff, periods));
        sub.beforeWrite();
        sub = subscriptionRepository.save(sub);
        if (post) {
            postingService.post(sub);
        }
    }
}