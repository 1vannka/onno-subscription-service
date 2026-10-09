package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import com.example.onnosubscriptionservice.domain.documents.Payment;
import com.example.onnosubscriptionservice.domain.documents.Subscription;
import org.springframework.stereotype.Component;
import su.onno.ui.Page;
import su.onno.ui.PageBuilder;

@Component
public class DashboardPage implements Page {

    @Override
    public String route() {
        return "/";
    }

    @Override
    public void compose(PageBuilder b) {
        b.title("Dashboard");
        b.subtitle("Ключевые показатели сервиса подписок и последние операции");

        b.widget("Клиенты")
                .type("count")
                .catalog(Client.class)
                .width("1/4")
                .order(10)
                .hint("Всего зарегистрированных клиентов");

        b.widget("Всего подписок")
                .type("count")
                .document(Subscription.class)
                .width("1/4")
                .order(20)
                .hint("Общее количество созданных подписок");

        b.widget("Сумма подписок")
                .type("metric")
                .document(Subscription.class)
                .config("metric", "sum")
                .config("metricField", "total")
                .width("1/4")
                .order(30)
                .hint("Общий объем оформленных подписок (руб)");

        b.widget("Поступления")
                .type("metric")
                .document(Payment.class)
                .config("metric", "sum")
                .config("metricField", "amount")
                .width("1/4")
                .order(40)
                .hint("Фактически поступившие платежи клиентов (руб)");

        b.widget("Подписки по статусам")
                .type("chart")
                .document(Subscription.class)
                .width("1/2")
                .order(50)
                .config("chart", "bar")
                .config("groupBy", "status")
                .hint("Распределение подписок по жизненному циклу");

        b.widget("Платежи по способам оплаты")
                .type("chart")
                .document(Payment.class)
                .width("1/2")
                .order(60)
                .config("chart", "pie")
                .config("groupBy", "paymentMethod")
                .config("metric", "sum")
                .config("metricField", "amount")
                .hint("Структура входящих платежей");

        b.widget("Последние подписки")
                .type("list")
                .document(Subscription.class)
                .order(70)
                .width("full")
                .maxItems(10);
    }
}