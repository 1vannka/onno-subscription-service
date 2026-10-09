package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.registers.TariffRevenue;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffRevenueView implements EntityView<TariffRevenue> {

    @Override
    public Class<TariffRevenue> entity() {
        return TariffRevenue.class;
    }

    @Override
    public void list(ListSpec<TariffRevenue> list) {
        list.columns(
                TariffRevenue::getTariff,
                TariffRevenue::getClient,
                TariffRevenue::getPeriods,
                TariffRevenue::getAmount
        );
        list.label(TariffRevenue::getAmount, "Revenue");
    }

    @Override
    public void fields(EntityConfigBuilder<TariffRevenue> f) {
        f.field(TariffRevenue::getAmount)
                .format("currency:RUB")
                .label("Revenue");
        f.field(TariffRevenue::getPeriods)
                .format("integer")
                .label("Periods");
    }
}