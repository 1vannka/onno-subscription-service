package com.example.onnosubscriptionservice.domain;

import com.example.onnosubscriptionservice.domain.catalogs.Tariff;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import su.onno.types.Ref;
import su.onno.types.RefResolver;

import java.util.function.Function;

@Component
public class DomainLookups implements ApplicationContextAware {

    private static ApplicationContext context;
    private static Function<Ref<Tariff>, Tariff> tariffResolver;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static Tariff tariff(Ref<Tariff> ref) {
        if (tariffResolver != null) {
            return tariffResolver.apply(ref);
        }
        if (context == null || ref == null || ref.id() == null) {
            return null;
        }
        return context.getBean(RefResolver.class)
                .resolve(ref)
                .orElse(null);
    }

    public static void setTariffResolver(Function<Ref<Tariff>, Tariff> resolver) {
        DomainLookups.tariffResolver = resolver;
    }

    public static void reset() {
        tariffResolver = null;
    }
}