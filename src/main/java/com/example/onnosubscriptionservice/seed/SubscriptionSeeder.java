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
        Ref<Tariff> proTariff = createTariff("Профессиональный", new BigDecimal("1500.00"), 30, true);
        Ref<Tariff> annualTariff = createTariff("Годовой Корпоративный", new BigDecimal("12000.00"), 365, true);
        Ref<Tariff> legacyTariff = createTariff("Архивный (Недоступен)", new BigDecimal("300.00"), 30, false);

        Ref<Client> clientRich = createClient("ООО Вектор Плюс", "vector@example.com", "+79991112233", ClientStatus.ACTIVE);
        Ref<Client> clientBroke = createClient("ИП Пупкин", "pupkin@example.com", "+79994445566", ClientStatus.ACTIVE);
        Ref<Client> clientNew = createClient("АО Технопарк", "tech@example.com", "+79997778899", ClientStatus.ACTIVE);

        createAndPostPayment(clientRich, new BigDecimal("20000.00"), PaymentMethod.BANK_TRANSFER, LocalDateTime.now().minusDays(5));
        createAndPostPayment(clientBroke, new BigDecimal("300.00"), PaymentMethod.BANK_CARD, LocalDateTime.now().minusDays(2));

        Subscription sub1 = new Subscription();
        sub1.setClient(clientRich);
        sub1.setDate(LocalDateTime.now().minusDays(3));
        sub1.setStartDate(LocalDate.now().minusDays(3));
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.getLines().add(createLine(proTariff, 2));
        sub1.getLines().add(createLine(basicTariff, 1));
        sub1.beforeWrite();
        sub1 = subscriptionRepository.save(sub1);
        postingService.post(sub1);

        Subscription sub2 = new Subscription();
        sub2.setClient(clientBroke);
        sub2.setDate(LocalDateTime.now());
        sub2.setStartDate(LocalDate.now());
        sub2.setStatus(SubscriptionStatus.DRAFT);
        sub2.getLines().add(createLine(proTariff, 1));
        sub2.beforeWrite();
        subscriptionRepository.save(sub2);
    }

    private Ref<Tariff> createTariff(String name, BigDecimal price, int days, boolean available) {
        Tariff t = new Tariff();
        t.setName(name);
        t.setPricePerPeriod(price);
        t.setPeriodDurationDays(days);
        t.setAvailableForConnection(available);
        return Ref.of(Tariff.class, tariffRepository.save(t).getId());
    }

    private Ref<Client> createClient(String name, String email, String phone, ClientStatus status) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setStatus(status);
        c.setRegistrationDate(LocalDate.now().minusMonths(1));
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
}