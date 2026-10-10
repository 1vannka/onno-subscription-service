package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.documents.Payment;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class PaymentView implements EntityView<Payment> {

    @Override
    public Class<Payment> entity() {
        return Payment.class;
    }

    @Override
    public void list(ListSpec<Payment> list) {
        list.columns(
                Payment::getNumber,
                Payment::getDate,
                Payment::getClient,
                Payment::getAmount,
                Payment::getPaymentMethod,
                Payment::isPosted
        );
        list.label(Payment::getNumber, "Number");
        list.label(Payment::getDate, "Date");
        list.label(Payment::getClient, "Client");
        list.label(Payment::getAmount, "Amount");
        list.label(Payment::getPaymentMethod, "Payment method");
        list.label(Payment::isPosted, "Posted");
    }

    @Override
    public void fields(EntityConfigBuilder<Payment> f) {
        f.field(Payment::getNumber).order(10).width("half");

        f.field(Payment::getDate).order(20).width("half")
                .format("dd/MM/yyyy HH:mm")
                .hint("Document date of the payment");

        f.field(Payment::getClient).order(30).width("half")
                .hint("Client whose account is topped up");

        f.field(Payment::getPaymentMethod).order(40).width("half")
                .hint("How the client paid");

        f.field(Payment::getAmount).order(50).width("half")
                .format("currency:RUB")
                .hint("Amount credited to the client account on posting");
    }
}