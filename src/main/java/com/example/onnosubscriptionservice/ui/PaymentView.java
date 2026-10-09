package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.documents.Payment;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class PaymentView implements EntityView {

    @Override
    public Class<?> entity() {
        return Payment.class;
    }

    @Override
    public void list(ListSpec spec) {
        spec.column("number", "Number");
        spec.column("date", "Date");
        spec.column("client", "Client");
        spec.column("amount", "Amount");
        spec.column("paymentMethod", "Payment method");
        spec.column("posted", "Posted");
    }

    @Override
    public void fields(EntityConfigBuilder fields) {
        fields.field("number").order(10).width("half");
        fields.field("date").order(20).width("half")
                .format("dd/MM/yyyy HH:mm")
                .hint("Document date of the payment");
        fields.field("client").order(30).width("half")
                .hint("Client whose account is topped up");
        fields.field("paymentMethod").order(40).width("half")
                .hint("How the client paid");
        fields.field("amount").order(50).width("half")
                .format("currency:RUB")
                .hint("Amount credited to the client account on posting");
    }
}
