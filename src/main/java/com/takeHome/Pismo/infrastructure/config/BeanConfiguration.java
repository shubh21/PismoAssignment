package com.takeHome.Pismo.infrastructure.config;

import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.core.domain.port.in.BalanceDischargePort;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.usecase.AccountManagementUseCase;
import com.takeHome.Pismo.core.usecase.BalanceDischargeUseCase;
import com.takeHome.Pismo.core.usecase.TransactionManagementUseCase;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.AccountPersistenceAdapter;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.TransactionPersistenceAdapter;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.JdbcTransactionPersistenceMapper;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
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
    public TransactionPersistenceMapper transactionPersistenceMapper(){
        return new JdbcTransactionPersistenceMapper();
    }

    @Bean
    public TransactionPersistencePort transactionPersistencePort(JdbcTemplate jdbcTemplate,
                                                                 TransactionPersistenceMapper transactionPersistenceMapper){
        return new TransactionPersistenceAdapter(jdbcTemplate, transactionPersistenceMapper);
    }

    @Bean
    public AccountManagementPort accountManagementPort(AccountPersistencePort accountPersistencePort){
        return new AccountManagementUseCase(accountPersistencePort);
    }

    @Bean
    public BalanceDischargePort balanceDischargePort(){
        return new BalanceDischargeUseCase();
    }

    @Bean
    public TransactionManagementPort transactionManagementPort(TransactionPersistencePort transactionPersistencePort,
                                                               BalanceDischargePort balanceDischargePort){
        return new TransactionManagementUseCase(transactionPersistencePort, balanceDischargePort);
    }
}
