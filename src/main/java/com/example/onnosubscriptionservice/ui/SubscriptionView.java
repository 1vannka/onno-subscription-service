package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.documents.Subscription;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class SubscriptionView implements EntityView {

    @Override
    public Class<?> entity() {
        return Subscription.class;
    }

    @Override
    public void list(ListSpec spec) {
        spec.column("number", "Number");
        spec.column("date", "Date");
        spec.column("client", "Client");
        spec.column("status", "Status");
        spec.column("startDate", "Start date");
        spec.column("endDate", "End date");
        spec.column("total", "Total");
        spec.column("posted", "Posted");
    }

    @Override
    public void fields(EntityConfigBuilder fields) {
        fields.field("number").order(10).width("half");
        fields.field("date").order(20).width("half")
                .format("dd/MM/yyyy HH:mm");
        fields.field("client").order(30).width("half")
                .hint("Client who purchases the tariffs");
        fields.field("status").order(40).width("half")
                .hint("Cancelled subscriptions do not create register movements");
        fields.field("startDate").order(50).width("half")
                .format("dd/MM/yyyy")
                .hint("Defaults to today when empty");
        fields.field("endDate").order(60).width("half")
                .format("dd/MM/yyyy")
                .hint("Calculated as start date plus the longest line duration");
        fields.field("total").order(70).width("half")
                .format("currency:USD")
                .hint("Sum of line amounts; charged to the client account on posting");
        fields.field("posted").order(80).width("half");
        fields.field("lines.tariff").order(90)
                .hint("Tariff must be available for connection");
        fields.field("lines.periods").order(100)
                .format("integer")
                .hint("Number of billing periods, must be greater than zero");
        fields.field("lines.price").order(110)
                .format("currency:USD")
                .hint("Filled from the tariff on save");
        fields.field("lines.amount").order(120)
                .format("currency:USD")
                .hint("Price multiplied by the number of periods");
    }
}
