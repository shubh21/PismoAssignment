package com.takeHome.Pismo.infrastructure.config;

import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.domain.usecase.AccountManagementUseCase;
import com.takeHome.Pismo.core.domain.usecase.TransactionManagementUseCase;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.AccountPersistenceAdapter;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.TransactionPersistenceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
@Profile("!Test")
public class BeanConfiguration {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public AccountPersistencePort accountPersistencePort(JdbcTemplate jdbcTemplate){
        return new AccountPersistenceAdapter(jdbcTemplate);
    }

    @Bean
    public TransactionPersistencePort transactionPersistencePort(JdbcTemplate jdbcTemplate){
        return new TransactionPersistenceAdapter(jdbcTemplate);
    }

    @Bean
    public AccountManagementPort accountManagementPort(AccountPersistencePort accountPersistencePort){
        return new AccountManagementUseCase(accountPersistencePort);
    }

    @Bean
    public TransactionManagementPort transactionManagementPort(TransactionPersistencePort transactionPersistencePort){
        return new TransactionManagementUseCase(transactionPersistencePort);
    }
}
