package com.example.onnosubscriptionservice.domain.catalogs;

import com.example.onnosubscriptionservice.domain.enums.ClientStatus;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.model.CatalogObject;

import java.time.LocalDate;

@Catalog(name = "Clients", title = "Clients", codePrefix = "CL-", context = "Subscriptions")
@AccessControl(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
public class Client extends CatalogObject {

    @Attribute(displayName = "Status", required = true)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Attribute(displayName = "Email", required = true, email = true, length = 200)
    private String email;

    @Attribute(displayName = "Phone", length = 32)
    private String phone;

    @Attribute(displayName = "Registration date", required = true)
    private LocalDate registrationDate = LocalDate.now();

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getName() {
        return getDescription();
    }

    public void setName(String name) {
        setDescription(name);
    }
}
