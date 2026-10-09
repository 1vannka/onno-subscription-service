package com.example.onnosubscriptionservice.unit;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.documents.Payment;
import com.example.onnosubscriptionservice.domain.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentValidationTest {

    @Test
    void testClientRequiredFailsWithoutClient() {
        assertFalse(holds(new Payment(), "client-required"));
    }

    @Test
    void testClientRequiredPassesWithValidReference() {
        Payment payment = new Payment();
        payment.setClient(Ref.of(Client.class, UUID.randomUUID()));

        assertTrue(holds(payment, "client-required"));
    }

    @Test
    void testAmountPositiveFailsForNullZeroAndNegativeAmounts() {
        for (BigDecimal amount : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-0.01")}) {
            Payment payment = new Payment();
            payment.setAmount(amount);

            assertFalse(holds(payment, "amount-positive"));
        }
    }

    @Test
    void testAmountPositivePassesForPositiveAmount() {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("0.01"));

        assertTrue(holds(payment, "amount-positive"));
    }

    @Test
    void testPaymentMethodRequiredFailsWithoutPaymentMethod() {
        assertFalse(holds(new Payment(), "payment-method-required"));
    }

    @Test
    void testPaymentMethodRequiredPassesWithPaymentMethod() {
        Payment payment = new Payment();
        payment.setPaymentMethod(PaymentMethod.CASH);

        assertTrue(holds(payment, "payment-method-required"));
    }

    private static boolean holds(Payment payment, String name) {
        return payment.rules().stream()
                .filter(rule -> rule.name().equals(name))
                .findFirst()
                .orElseThrow()
                .holds();
    }
}
