package com.example.onnosubscriptionservice.domain.documents;

import com.example.onnosubscriptionservice.domain.DomainLookups;
import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.enums.SubscriptionStatus;
import com.example.onnosubscriptionservice.domain.registers.ClientAccount;
import com.example.onnosubscriptionservice.domain.registers.TariffRevenue;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Document;
import su.onno.annotations.TabularSection;
import su.onno.lifecycle.BeforeWriteHandler;
import su.onno.lifecycle.Postable;
import su.onno.model.DocumentObject;
import su.onno.posting.PostingContext;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(name = "Subscriptions", title = "Subscriptions", numberPrefix = "SUB-", context = "Subscriptions")
@AccessControl(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
public class Subscription extends DocumentObject implements Validated, BeforeWriteHandler, Postable {

    @Attribute(displayName = "Client", required = true)
    private Ref<Client> client;

    @Attribute(displayName = "Status", required = true)
    private SubscriptionStatus status = SubscriptionStatus.DRAFT;

    @Attribute(displayName = "Start date")
    private LocalDate startDate;

    @Attribute(displayName = "End date")
    private LocalDate endDate;

    @Attribute(displayName = "Total", precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @TabularSection(name = "lines")
    private List<SubscriptionLine> lines = new ArrayList<>();

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("client-required", "Client is required",
                        () -> client != null && client.id() != null),
                new BusinessRule("lines-required", "Add at least one line",
                        () -> lines != null && !lines.isEmpty()),
                new BusinessRule("periods-positive", "Number of periods must be greater than zero on every line",
                        () -> lines != null && lines.stream().allMatch(this::hasPositivePeriods)),
                new BusinessRule("tariff-available", "Every tariff must be available for connection",
                        () -> lines != null && lines.stream().allMatch(this::tariffAvailableForConnection))
        );
    }

    @Override
    public void beforeWrite() {
        if (getDate() == null) {
            setDate(LocalDateTime.now());
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (lines == null) {
            lines = new ArrayList<>();
        }

        int maxDurationDays = 0;
        BigDecimal documentTotal = BigDecimal.ZERO;

        for (SubscriptionLine line : lines) {
            Tariff tariff = DomainLookups.tariff(line.getTariff());
            if (tariff != null && tariff.getPricePerPeriod() != null) {
                line.setPrice(tariff.getPricePerPeriod());
            }
            if (line.getPrice() == null) {
                line.setPrice(BigDecimal.ZERO);
            }
            int periods = line.getPeriods() == null ? 0 : line.getPeriods();
            line.setAmount(line.getPrice().multiply(BigDecimal.valueOf(periods)));
            documentTotal = documentTotal.add(line.getAmount());

            int durationDays = tariff != null && tariff.getPeriodDurationDays() != null
                    ? tariff.getPeriodDurationDays()
                    : 0;
            maxDurationDays = Math.max(maxDurationDays, periods * durationDays);
        }

        total = documentTotal;
        endDate = startDate.plusDays(maxDurationDays);
    }

    @Override
    public void handlePosting(PostingContext context) {
        if (status == SubscriptionStatus.CANCELLED) {
            return;
        }

        var accounts = context.movements(ClientAccount.class);

        BigDecimal currentBalance = accounts.getBalance().stream()
                .filter(acc -> acc.getClient() != null
                        && client != null
                        && client.id() != null
                        && client.id().equals(acc.getClient().id()))
                .map(ClientAccount::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentBalance.compareTo(total) < 0) {
            throw new IllegalStateException(
                    String.format("Недостаточно средств на лицевом счете. Баланс: %s, требуется: %s",
                            currentBalance, total)
            );
        }

        accounts.addExpense(movement -> {
            movement.setClient(client);
            movement.setAmount(total);
        });

        var revenue = context.movements(TariffRevenue.class);
        if (lines == null) {
            return;
        }
        for (SubscriptionLine line : lines) {
            revenue.addReceipt(movement -> {
                movement.setTariff(line.getTariff());
                movement.setClient(client);
                movement.setAmount(line.getAmount());
                movement.setPeriods(line.getPeriods());
            });
        }
    }

    private boolean hasPositivePeriods(SubscriptionLine line) {
        return line.getPeriods() != null && line.getPeriods() > 0;
    }

    private boolean tariffAvailableForConnection(SubscriptionLine line) {
        Tariff tariff = DomainLookups.tariff(line.getTariff());
        return tariff != null && tariff.isAvailableForConnection();
    }

    public Ref<Client> getClient() {
        return client;
    }

    public void setClient(Ref<Client> client) {
        this.client = client;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<SubscriptionLine> getLines() {
        return lines;
    }

    public void setLines(List<SubscriptionLine> lines) {
        this.lines = lines;
    }
}
