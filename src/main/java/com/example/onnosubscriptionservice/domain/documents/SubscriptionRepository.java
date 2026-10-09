package com.example.onnosubscriptionservice.domain.documents;

import org.springframework.stereotype.Repository;
import su.onno.repository.DocumentRepository;

@Repository
public interface SubscriptionRepository extends DocumentRepository<Subscription> {
}