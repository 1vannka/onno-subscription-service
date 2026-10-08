package com.example.onnosubscriptionservice.domain.enums;

import su.onno.annotations.EnumLabel;
import su.onno.annotations.Enumeration;

@Enumeration(name = "PaymentMethods", title = "Payment method")
public enum PaymentMethod {

    @EnumLabel(value = "Cash", color = "#6B7280")
    CASH,

    @EnumLabel(value = "Bank card", color = "#2563EB")
    BANK_CARD,

    @EnumLabel(value = "Bank transfer", color = "#7C3AED")
    BANK_TRANSFER
}
