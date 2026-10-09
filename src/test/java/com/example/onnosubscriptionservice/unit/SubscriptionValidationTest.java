package com.example.onnosubscriptionservice.unit;

import com.example.onnosubscriptionservice.domain.DomainLookups;
import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.domain.documents.SubscriptionLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionValidationTest {

    @AfterEach
    void resetDomainLookups() {
        DomainLookups.reset();
    }

    @Test
    void testClientRequiredFailsWithoutClient() {
        assertFalse(holds(new Subscription(), "client-required"));
    }

    @Test
    void testClientRequiredPassesWithValidReference() {
        Subscription subscription = new Subscription();
        subscription.setClient(Ref.of(Client.class, UUID.randomUUID()));

        assertTrue(holds(subscription, "client-required"));
    }

    @Test
    void testLinesRequiredFailsForNullOrEmptyLines() {
        Subscription subscription = new Subscription();
        subscription.setLines(null);
        assertFalse(holds(subscription, "lines-required"));

        subscription.setLines(List.of());
        assertFalse(holds(subscription, "lines-required"));
    }

    @Test
    void testLinesRequiredPassesWithLine() {
        Subscription subscription = new Subscription();
        subscription.setLines(List.of(line(null, 1)));

        assertTrue(holds(subscription, "lines-required"));
    }

    @Test
    void testPeriodsPositiveFailsForNullOrNonPositivePeriods() {
        for (Integer periods : Arrays.asList(null, 0, -1)) {
            Subscription subscription = new Subscription();
            subscription.setLines(List.of(line(null, periods)));

            assertFalse(holds(subscription, "periods-positive"));
        }
    }

    @Test
    void testPeriodsPositivePassesWhenAllLinesArePositive() {
        Subscription subscription = new Subscription();
        subscription.setLines(List.of(line(null, 1), line(null, 2)));

        assertTrue(holds(subscription, "periods-positive"));
    }

    @Test
    void testTariffAvailableFailsWhenAnyTariffIsUnavailable() {
        Ref<Tariff> availableRef = tariffRef();
        Ref<Tariff> unavailableRef = tariffRef();
        Subscription subscription = new Subscription();
        subscription.setLines(List.of(line(availableRef, 1), line(unavailableRef, 1)));
        Tariff unavailable = tariff(false);
        DomainLookups.setTariffResolver(Map.of(
                availableRef, tariff(true),
                unavailableRef, unavailable
        )::get);

        assertFalse(holds(subscription, "tariff-available"));
    }

    @Test
    void testTariffAvailablePassesWhenAllTariffsAreAvailable() {
        Ref<Tariff> firstRef = tariffRef();
        Ref<Tariff> secondRef = tariffRef();
        Subscription subscription = new Subscription();
        subscription.setLines(List.of(line(firstRef, 1), line(secondRef, 1)));
        DomainLookups.setTariffResolver(Map.of(
                firstRef, tariff(true),
                secondRef, tariff(true)
        )::get);

        assertTrue(holds(subscription, "tariff-available"));
    }

    private static boolean holds(Subscription subscription, String name) {
        return subscription.rules().stream()
                .filter(rule -> rule.name().equals(name))
                .findFirst()
                .orElseThrow()
                .holds();
    }

    private static SubscriptionLine line(Ref<Tariff> tariffRef, Integer periods) {
        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(tariffRef);
        line.setPeriods(periods);
        return line;
    }

    private static Tariff tariff(boolean available) {
        Tariff tariff = new Tariff();
        tariff.setPricePerPeriod(BigDecimal.ONE);
        tariff.setPeriodDurationDays(30);
        tariff.setAvailableForConnection(available);
        return tariff;
    }

    private static Ref<Tariff> tariffRef() {
        return Ref.of(Tariff.class, UUID.randomUUID());
    }
}
