package com.xdata.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // Das Modul verhindert das Laden von Lazy-Beziehungen bei der Serialisierung,
        // es sei denn, sie wurden bereits geladen (z.B. durch @Transactional oder Eager Loading).
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        // Ermöglicht die Serialisierung der ID für nicht geladene Lazy-Objekte
        module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        return module;
    }
}
