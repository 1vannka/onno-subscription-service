package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.documents.Payment;
import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.domain.registers.ClientAccount;
import com.example.onnosubscriptionservice.domain.registers.TariffRevenue;
import org.springframework.stereotype.Component;
import su.onno.ui.Layout;
import su.onno.ui.LayoutSpec;

@Component
public class AppLayout implements Layout {

    @Override
    public void configure(LayoutSpec spec) {
        spec.shell().brand("Subscription Management");

        spec.section("Operations")
                .order(10)
                .icon("file-text")
                .document(Subscription.class)
                .document(Payment.class);

        spec.section("Catalogs")
                .order(20)
                .icon("folder")
                .catalog(Client.class)
                .catalog(Tariff.class);

        spec.section("Registers")
                .order(30)
                .icon("bar-chart")
                .register(ClientAccount.class)
                .register(TariffRevenue.class);
    }
}