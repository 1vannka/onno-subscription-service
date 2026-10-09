package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.registers.ClientAccount;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class ClientAccountView implements EntityView<ClientAccount> {

    @Override
    public Class<ClientAccount> entity() {
        return ClientAccount.class;
    }

    @Override
    public void list(ListSpec<ClientAccount> list) {
        list.columns(
                ClientAccount::getClient,
                ClientAccount::getAmount
        );
        list.label(ClientAccount::getAmount, "Amount");
    }

    @Override
    public void fields(EntityConfigBuilder<ClientAccount> f) {
        f.field(ClientAccount::getAmount)
                .format("currency:RUB")
                .label("Amount");
    }
}