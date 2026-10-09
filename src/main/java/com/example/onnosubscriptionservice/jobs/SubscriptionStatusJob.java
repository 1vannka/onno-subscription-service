package com.example.onnosubscriptionservice.jobs;

import com.example.onnosubscriptionservice.domain.documents.Subscription;
import com.example.onnosubscriptionservice.repositories.SubscriptionRepository;
import com.example.onnosubscriptionservice.domain.enums.SubscriptionStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import su.onno.annotations.ScheduledJob;
import su.onno.jobs.BackgroundTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ScheduledJob(name = "UpdateSubscriptionStatuses", cron = "0 0 0 * * *")
public class SubscriptionStatusJob implements BackgroundTask {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionStatusJob(@Lazy SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public void execute() {
        LocalDate today = LocalDate.now();
        List<Subscription> toSave = new ArrayList<>();

        for (Subscription sub : subscriptionRepository.findAll()) {
            if (sub.getStatus() == SubscriptionStatus.CANCELLED || !sub.isPosted()) {
                continue;
            }

            boolean changed = false;

            if (sub.getEndDate() != null && sub.getEndDate().isBefore(today)) {
                if (sub.getStatus() != SubscriptionStatus.EXPIRED) {
                    sub.setStatus(SubscriptionStatus.EXPIRED);
                    changed = true;
                }
            } else if (sub.getStartDate() != null && !sub.getStartDate().isAfter(today)) {
                if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    changed = true;
                }
            }

            if (changed) {
                toSave.add(sub);
            }
        }

        if (!toSave.isEmpty()) {
            subscriptionRepository.saveAll(toSave);
        }
    }
}