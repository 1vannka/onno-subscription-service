package com.example.onnosubscriptionservice.repositories;

import com.example.onnosubscriptionservice.domain.catalogs.Client;
import org.springframework.stereotype.Repository;
import su.onno.repository.CatalogRepository;

@Repository
public interface ClientRepository extends CatalogRepository<Client> {
}