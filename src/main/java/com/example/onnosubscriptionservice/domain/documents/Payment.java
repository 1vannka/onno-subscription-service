package com.example.onnosubscriptionservice.domain.documents;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.enums.PaymentMethod;
import com.example.onnosubscriptionservice.domain.registers.ClientAccount;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Document;
import su.onno.lifecycle.BeforeWriteHandler;
import su.onno.lifecycle.Postable;
import su.onno.model.DocumentObject;
import su.onno.posting.PostingContext;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(name = "Payments", title = "Payments", numberPrefix = "PAY-", context = "Subscriptions")
@AccessControl(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
public class Payment extends DocumentObject implements Postable, Validated, BeforeWriteHandler {

    @Attribute(displayName = "Client", required = true)
    private Ref<Client> client;

    @Attribute(displayName = "Amount", required = true, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Attribute(displayName = "Payment method", required = true)
    private PaymentMethod paymentMethod;

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("client-required", "Client is required",
                        () -> client != null && client.id() != null),
                new BusinessRule("amount-positive", "Payment amount must be greater than zero",
                        () -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0),
                new BusinessRule("payment-method-required", "Payment method is required",
                        () -> paymentMethod != null)
        );
    }

    @Override
    public void beforeWrite() {
        if (getDate() == null) {
            setDate(LocalDateTime.now());
        }
    }

    @Override
    public void handlePosting(PostingContext context) {
        var accounts = context.movements(ClientAccount.class);
        accounts.addReceipt(movement -> {
            movement.setClient(client);
            movement.setAmount(amount);
        });
    }

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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
