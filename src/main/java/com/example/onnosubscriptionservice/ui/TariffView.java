package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffView implements EntityView<Tariff> {

    @Override
    public Class<Tariff> entity() {
        return Tariff.class;
    }

    @Override
    public void list(ListSpec<Tariff> list) {
        list.columns(
                Tariff::getCode,
                Tariff::getDescription,
                Tariff::getPricePerPeriod,
                Tariff::getPeriodDurationDays,
                Tariff::isAvailableForConnection
        );
        list.label(Tariff::getCode, "Code");
        list.label(Tariff::getDescription, "Description");
        list.label(Tariff::getPricePerPeriod, "Price per period");
        list.label(Tariff::getPeriodDurationDays, "Period duration (days)");
        list.label(Tariff::isAvailableForConnection, "Available");
    }

    @Override
    public void fields(EntityConfigBuilder<Tariff> f) {
        f.field(Tariff::getCode).order(10).width("half").label("Code");
        f.field(Tariff::getDescription).order(20).width("half").label("Description")
                .hint("Display name of the tariff plan");

        f.field(Tariff::getPricePerPeriod).order(30).width("half")
                .format("currency:RUB")
                .hint("Price charged for one billing period");

        f.field(Tariff::getPeriodDurationDays).order(40).width("half")
                .format("integer")
                .hint("Length of one billing period in days");

        f.field(Tariff::isAvailableForConnection).order(50).width("half")
                .widget("switch")
                .hint("Only available tariffs can be added to a subscription");
    }
}