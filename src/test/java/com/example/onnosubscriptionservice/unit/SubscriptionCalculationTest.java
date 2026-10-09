package com.example.onnosubscriptionservice.unit;

import com.example.onnosubscriptionservice.domain.DomainLookups;
import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.domain.documents.SubscriptionLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SubscriptionCalculationTest {

    @AfterEach
    void resetDomainLookups() {
        DomainLookups.reset();
    }

    @Test
    void testLineAmountsAreCalculatedFromTariffPrice() {
        Ref<Tariff> tariffRef = tariffRef();
        Tariff tariff = tariff(new BigDecimal("12.50"), 30);
        SubscriptionLine line = line(tariffRef, 3);
        Subscription subscription = subscription(LocalDate.of(2026, 1, 1), line);
        DomainLookups.setTariffResolver(Map.of(tariffRef, tariff)::get);

        subscription.beforeWrite();

        assertEquals(new BigDecimal("12.50"), line.getPrice());
        assertEquals(new BigDecimal("37.50"), line.getAmount());
    }

    @Test
    void testTotalIsSumOfAllLineAmounts() {
        Ref<Tariff> firstRef = tariffRef();
        Ref<Tariff> secondRef = tariffRef();
        SubscriptionLine first = line(firstRef, 2);
        SubscriptionLine second = line(secondRef, 3);
        Subscription subscription = subscription(LocalDate.of(2026, 1, 1), first, second);
        DomainLookups.setTariffResolver(Map.of(
                firstRef, tariff(new BigDecimal("10.00"), 30),
                secondRef, tariff(new BigDecimal("7.25"), 30)
        )::get);

        subscription.beforeWrite();

        assertEquals(new BigDecimal("41.75"), subscription.getTotal());
    }

    @Test
    void testEndDateUsesLongestLineDuration() {
        Ref<Tariff> monthlyRef = tariffRef();
        Ref<Tariff> annualRef = tariffRef();
        SubscriptionLine monthly = line(monthlyRef, 3);
        SubscriptionLine annual = line(annualRef, 1);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        Subscription subscription = subscription(startDate, monthly, annual);
        DomainLookups.setTariffResolver(Map.of(
                monthlyRef, tariff(new BigDecimal("10.00"), 30),
                annualRef, tariff(new BigDecimal("100.00"), 365)
        )::get);

        subscription.beforeWrite();

        assertEquals(startDate.plusDays(365), subscription.getEndDate());
    }

    @Test
    void testBeforeWriteHandlesNullLines() {
        Subscription subscription = subscription(LocalDate.of(2026, 1, 1));
        subscription.setLines(null);

        assertDoesNotThrow(subscription::beforeWrite);
        assertEquals(BigDecimal.ZERO, subscription.getTotal());
        assertEquals(LocalDate.of(2026, 1, 1), subscription.getEndDate());
    }

    @Test
    void testBeforeWriteHandlesEmptyLines() {
        Subscription subscription = subscription(LocalDate.of(2026, 1, 1));

        assertDoesNotThrow(subscription::beforeWrite);
        assertEquals(BigDecimal.ZERO, subscription.getTotal());
        assertEquals(LocalDate.of(2026, 1, 1), subscription.getEndDate());
    }

    @Test
    void testBeforeWriteHandlesNullAndZeroPeriods() {
        Ref<Tariff> tariffRef = tariffRef();
        SubscriptionLine nullPeriods = line(tariffRef, null);
        SubscriptionLine zeroPeriods = line(tariffRef, 0);
        Subscription subscription = subscription(LocalDate.of(2026, 1, 1), nullPeriods, zeroPeriods);
        DomainLookups.setTariffResolver(Map.of(tariffRef, tariff(new BigDecimal("10.00"), 30))::get);

        assertDoesNotThrow(subscription::beforeWrite);
        assertEquals(0, BigDecimal.ZERO.compareTo(nullPeriods.getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(zeroPeriods.getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(subscription.getTotal()));
    }

    private static Subscription subscription(LocalDate startDate, SubscriptionLine... lines) {
        Subscription subscription = new Subscription();
        subscription.setStartDate(startDate);
        subscription.setLines(List.of(lines));
        return subscription;
    }

    private static SubscriptionLine line(Ref<Tariff> tariffRef, Integer periods) {
        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(tariffRef);
        line.setPeriods(periods);
        return line;
    }

    private static Tariff tariff(BigDecimal price, int durationDays) {
        Tariff tariff = new Tariff();
        tariff.setPricePerPeriod(price);
        tariff.setPeriodDurationDays(durationDays);
        return tariff;
    }

    private static Ref<Tariff> tariffRef() {
        return Ref.of(Tariff.class, UUID.randomUUID());
    }
}
