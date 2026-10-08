package com.example.onnosubscriptionservice.domain.documents;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import su.onno.annotations.Attribute;
import su.onno.model.TabularSectionRow;
import su.onno.types.Ref;

import java.math.BigDecimal;

public class SubscriptionLine extends TabularSectionRow {

    @Attribute(displayName = "Tariff", required = true)
    private Ref<Tariff> tariff;

    @Attribute(displayName = "Periods", required = true, min = 1)
    private Integer periods;

    @Attribute(displayName = "Price", precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Attribute(displayName = "Amount", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public Ref<Tariff> getTariff() {
        return tariff;
    }

    public void setTariff(Ref<Tariff> tariff) {
        this.tariff = tariff;
    }

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
