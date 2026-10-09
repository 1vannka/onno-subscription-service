package com.example.onnosubscriptionservice.repositories;

import com.example.onnosubscriptionservice.domain.documents.Payment;
import org.springframework.stereotype.Repository;
import su.onno.repository.DocumentRepository;

@Repository
public interface PaymentRepository extends DocumentRepository<Payment> {
}