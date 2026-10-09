package com.example.onnosubscriptionservice.repositories;

import com.example.onnosubscriptionservice.domain.documents.Subscription;
import org.springframework.stereotype.Repository;
import su.onno.repository.DocumentRepository;

@Repository
public interface SubscriptionRepository extends DocumentRepository<Subscription> {
}