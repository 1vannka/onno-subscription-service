package com.example.onnosubscriptionservice.domain;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import com.example.onnosubscriptionservice.domain.catalogs.TariffRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import su.onno.types.Ref;

@Component
public class DomainLookups implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static Tariff tariff(Ref<Tariff> ref) {
        if (context == null || ref == null || ref.id() == null) {
            return null;
        }
        return context.getBean(TariffRepository.class)
                .findActiveById(ref.id())
                .orElse(null);
    }
}
