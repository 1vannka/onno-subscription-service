package com.example.onnosubscriptionservice.ui;

import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.domain.documents.SubscriptionLine;
import com.example.onnosubscriptionservice.domain.enums.SubscriptionStatus;
import com.example.onnosubscriptionservice.repositories.SubscriptionRepository;
import org.springframework.stereotype.Component;
import su.onno.posting.PostingService;
import su.onno.ui.ActionScope;
import su.onno.ui.ActionSpec;
import su.onno.ui.ActionResult;
import su.onno.ui.ActionToast;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.InputType;
import su.onno.ui.ListSpec;

import java.util.UUID;

@Component
public class SubscriptionView implements EntityView<Subscription> {

    private final SubscriptionRepository subscriptionRepository;
    private final PostingService postingService;

    public SubscriptionView(SubscriptionRepository subscriptionRepository, PostingService postingService) {
        this.subscriptionRepository = subscriptionRepository;
        this.postingService = postingService;
    }

    @Override
    public Class<Subscription> entity() {
        return Subscription.class;
    }

    @Override
    public void list(ListSpec<Subscription> list) {
        list.columns(
                Subscription::getNumber,
                Subscription::getDate,
                Subscription::getClient,
                Subscription::getStatus,
                Subscription::getStartDate,
                Subscription::getEndDate,
                Subscription::getTotal
        );
        list.label(Subscription::getTotal, "Total");
        list.sortBy(Subscription::getDate, true);
    }

    @Override
    public void fields(EntityConfigBuilder<Subscription> f) {
        f.field(Subscription::getNumber).order(10).width("half");
        f.field(Subscription::getDate).order(20).width("half")
                .format("dd/MM/yyyy HH:mm");
        f.field(Subscription::getClient).order(30).width("half")
                .hint("Client who purchases the tariffs");
        f.field(Subscription::getStatus).order(40).width("half")
                .hint("Cancelled subscriptions do not create register movements");
        f.field(Subscription::getStartDate).order(50).width("half")
                .format("dd/MM/yyyy")
                .hint("Defaults to today when empty");
        f.field(Subscription::getEndDate).order(60).width("half")
                .format("dd/MM/yyyy")
                .hint("Calculated as start date plus the longest line duration");
        f.field(Subscription::getTotal).order(70).width("half")
                .format("currency:RUB")
                .hint("Sum of line amounts; charged to the client account on posting");
        f.field(Subscription::getCancellationReason).order(80)
                .widget("textarea")
                .hint("Reason provided when subscription was cancelled");

        f.rowField(Subscription::getLines, SubscriptionLine::getTariff).label("Tariff");
        f.rowField(Subscription::getLines, SubscriptionLine::getPeriods).label("Periods");
        f.rowField(Subscription::getLines, SubscriptionLine::getPrice)
                .label("Price").format("currency:RUB");
        f.rowField(Subscription::getLines, SubscriptionLine::getAmount)
                .label("Amount").format("currency:RUB");
        f.action("post").hidden();
        f.action("unpost").hidden();
        f.action("postDetail").primary();
    }

    @Override
    public void actions(ActionSpec a) {
        a.action("postRow").scope(ActionScope.ROW).icon("check").label("Post")
                .visibleWhen(row -> {
                    SubscriptionStatus st = row.enumValue("status", SubscriptionStatus.class);
                    return st == SubscriptionStatus.DRAFT;
                })
                .handler(ctx -> post(ctx.id()));

        a.action("postDetail").scope(ActionScope.DETAIL).icon("check").label("Post")
                .visibleWhen(row -> {
                    SubscriptionStatus st = row.enumValue("status", SubscriptionStatus.class);
                    return st == SubscriptionStatus.DRAFT;
                })
                .handler(ctx -> post(ctx.id()));

        a.action("cancelRow").scope(ActionScope.ROW).icon("ban").label("Cancel")
                .visibleWhen(row -> {
                    SubscriptionStatus st = row.enumValue("status", SubscriptionStatus.class);
                    return st != SubscriptionStatus.CANCELLED;
                })
                .form(f -> f.input("reason").label("Reason").type(InputType.TEXTAREA)
                        .placeholder("Why is this subscription cancelled?").required())
                .handler(ctx -> cancel(ctx.id(), ctx.input("reason")));

        a.action("cancelDetail").scope(ActionScope.DETAIL).icon("ban").label("Cancel subscription")
                .visibleWhen(row -> {
                    SubscriptionStatus st = row.enumValue("status", SubscriptionStatus.class);
                    return st != SubscriptionStatus.CANCELLED;
                })
                .form(f -> f.input("reason").label("Reason").type(InputType.TEXTAREA)
                        .placeholder("Why is this subscription cancelled?").required())
                .handler(ctx -> cancel(ctx.id(), ctx.input("reason")));
    }

    private ActionResult post(UUID id) {
        return subscriptionRepository.findById(id).map(sub -> {
            if (sub.isPosted()) {
                return ActionResult.toast(ActionToast.warning("Subscription is already posted"));
            }

            try {
                postingService.post(sub);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(sub);
                return ActionResult.refresh(ActionToast.success("Subscription was successfully posted and activate"));
            } catch (Exception ex) {
                String errorMsg = ex.getMessage();

                if (errorMsg != null && errorMsg.contains("Insufficient amount in register")) {
                    errorMsg = "Not enough money";
                } else if (errorMsg == null || errorMsg.isBlank()) {
                    errorMsg = "Error with posting";
                }

                return ActionResult.toast(ActionToast.warning(errorMsg));
            }
        }).orElseGet(() -> ActionResult.toast(ActionToast.warning("Subscription was not found")));
    }

    private ActionResult cancel(UUID id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return ActionResult.toast(ActionToast.warning("Cancellation reason is required"));
        }

        return subscriptionRepository.findById(id).map(sub -> {
            if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
                return ActionResult.toast(ActionToast.warning("Subscription is already cancelled"));
            }

            try {
                if (sub.isPosted()) {
                    postingService.unpost(sub);
                }
                sub.setStatus(SubscriptionStatus.CANCELLED);
                sub.setCancellationReason(reason.trim());
                subscriptionRepository.save(sub);
                return ActionResult.refresh(ActionToast.success("Subscription cancelled"));
            } catch (Exception ex) {
                String errorMsg = ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Failed to cancel subscription";
                return ActionResult.toast(ActionToast.warning(errorMsg));
            }
        }).orElseGet(() -> ActionResult.toast(ActionToast.warning("Subscription not found")));
    }
}