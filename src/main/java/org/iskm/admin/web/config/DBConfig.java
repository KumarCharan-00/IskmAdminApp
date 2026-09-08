package org.iskm.admin.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class DBConfig {
	
	@Bean("transactionManager")
    public PlatformTransactionManager jpaTransactionManager(@NonNull EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

}
