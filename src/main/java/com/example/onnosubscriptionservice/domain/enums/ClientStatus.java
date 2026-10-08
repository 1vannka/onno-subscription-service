package com.example.onnosubscriptionservice.domain.enums;

import su.onno.annotations.EnumLabel;
import su.onno.annotations.Enumeration;

@Enumeration(name = "ClientStatuses", title = "Client status")
public enum ClientStatus {

    @EnumLabel(value = "Active", color = "#059669")
    ACTIVE,

    @EnumLabel(value = "Blocked", color = "#DC2626")
    BLOCKED,

    @EnumLabel(value = "Inactive", color = "#6B7280")
    INACTIVE
}
