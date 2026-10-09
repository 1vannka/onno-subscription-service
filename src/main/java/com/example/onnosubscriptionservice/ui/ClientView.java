package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class ClientView implements EntityView {

    @Override
    public Class<?> entity() {
        return Client.class;
    }

    @Override
    public void list(ListSpec spec) {
        spec.column("code", "Code");
        spec.column("description", "Description");
        spec.column("status", "Status");
        spec.column("email", "Email");
        spec.column("phone", "Phone");
        spec.column("registrationDate", "Registration date");
    }

    @Override
    public void fields(EntityConfigBuilder fields) {
        fields.field("code").order(10).width("half").label("Code");
        fields.field("description").order(20).width("half").label("Description")
                .hint("Display name of the client");
        fields.field("status").order(30).width("half")
                .hint("Active clients can be billed and subscribed");
        fields.field("registrationDate").order(40).width("half")
                .format("dd/MM/yyyy")
                .hint("Date the client was registered");
        fields.field("email").order(50).width("half")
                .hint("Used for notices and invoices");
        fields.field("phone").order(60).width("half")
                .hint("Contact phone number");
    }
}
