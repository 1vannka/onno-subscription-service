package com.example.onnosubscriptionservice.repositories;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import org.springframework.stereotype.Repository;
import su.onno.repository.CatalogRepository;

@Repository
public interface TariffRepository extends CatalogRepository<Tariff> {
}