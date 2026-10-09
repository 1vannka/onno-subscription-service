package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffView implements EntityView {

    @Override
    public Class<?> entity() {
        return Tariff.class;
    }

    @Override
    public void list(ListSpec spec) {
        spec.column("code", "Code");
        spec.column("description", "Name");
        spec.column("pricePerPeriod", "Price per period");
        spec.column("periodDurationDays", "Period duration (days)");
        spec.column("availableForConnection", "Available");
    }

    @Override
    public void fields(EntityConfigBuilder fields) {
        fields.field("code").order(10).width("half").label("Code");
        fields.field("description").order(20).width("half").label("Name")
                .hint("Display name of the tariff plan");
        fields.field("pricePerPeriod").order(30).width("half")
                .format("currency:USD")
                .hint("Price charged for one billing period");
        fields.field("periodDurationDays").order(40).width("half")
                .format("integer")
                .hint("Length of one billing period in days");
        fields.field("availableForConnection").order(50).width("half")
                .widget("switch")
                .hint("Only available tariffs can be added to a subscription");
    }
}
