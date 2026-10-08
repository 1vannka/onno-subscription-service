package com.example.onnosubscriptionservice.domain.catalogs;

import org.springframework.stereotype.Repository;
import su.onno.repository.CatalogRepository;

@Repository
public interface TariffRepository extends CatalogRepository<Tariff> {
}
