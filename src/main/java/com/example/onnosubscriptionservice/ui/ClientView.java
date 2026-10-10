package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class ClientView implements EntityView<Client> {

    @Override
    public Class<Client> entity() {
        return Client.class;
    }

    @Override
    public void list(ListSpec<Client> list) {
        list.columns(
                Client::getCode,
                Client::getDescription,
                Client::getStatus,
                Client::getEmail,
                Client::getPhone,
                Client::getRegistrationDate
        );
        list.label(Client::getCode, "Code");
        list.label(Client::getDescription, "Description");
    }

    @Override
    public void fields(EntityConfigBuilder<Client> f) {
        f.field(Client::getCode).order(10).width("half").label("Code");
        f.field(Client::getDescription).order(20).width("half").label("Description")
                .hint("Display name of the client");

        f.field(Client::getStatus).order(30).width("half")
                .hint("Active clients can be billed and subscribed");

        f.field(Client::getRegistrationDate).order(40).width("half")
                .format("dd/MM/yyyy")
                .hint("Date the client was registered");

        f.field(Client::getEmail).order(50).width("half")
                .hint("Used for notices and invoices");

        f.field(Client::getPhone).order(60).width("half")
                .hint("Contact phone number");
    }
}