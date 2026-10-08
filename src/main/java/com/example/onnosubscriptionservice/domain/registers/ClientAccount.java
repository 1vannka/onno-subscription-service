package com.example.onnosubscriptionservice.domain.registers;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import su.onno.annotations.AccessControl;
import su.onno.annotations.AccumulationRegister;
import su.onno.annotations.Dimension;
import su.onno.annotations.Resource;
import su.onno.model.AccumulationRecord;
import su.onno.model.AccumulationType;
import su.onno.types.Ref;

import java.math.BigDecimal;

@AccumulationRegister(
        name = "ClientAccounts",
        title = "Client accounts",
        type = AccumulationType.BALANCE,
        context = "Subscriptions")
@AccessControl(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
public class ClientAccount extends AccumulationRecord {

    @Dimension(displayName = "Client")
    private Ref<Client> client;

    @Resource(displayName = "Amount", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public Ref<Client> getClient() {
        return client;
    }

    public void setClient(Ref<Client> client) {
        this.client = client;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
