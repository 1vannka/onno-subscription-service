package com.example.onnosubscriptionservice.domain.catalogs;

import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.model.CatalogObject;

import java.math.BigDecimal;

@Catalog(name = "Tariffs", title = "Tariffs", codePrefix = "TR-", context = "Subscriptions")
@AccessControl(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
public class Tariff extends CatalogObject {

    @Attribute(displayName = "Price per period", required = true, precision = 15, scale = 2)
    private BigDecimal pricePerPeriod = BigDecimal.ZERO;

    @Attribute(displayName = "Period duration in days", required = true, min = 1)
    private Integer periodDurationDays;

    @Attribute(displayName = "Available for connection")
    private boolean availableForConnection = true;

    public BigDecimal getPricePerPeriod() {
        return pricePerPeriod;
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        this.pricePerPeriod = pricePerPeriod;
    }

    public Integer getPeriodDurationDays() {
        return periodDurationDays;
    }

    public void setPeriodDurationDays(Integer periodDurationDays) {
        this.periodDurationDays = periodDurationDays;
    }

    public boolean isAvailableForConnection() {
        return availableForConnection;
    }

    public void setAvailableForConnection(boolean availableForConnection) {
        this.availableForConnection = availableForConnection;
    }
}
