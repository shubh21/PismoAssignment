package com.takeHome.Pismo.infrastructure.config;

import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.domain.usecase.AccountManagementUseCase;
import com.takeHome.Pismo.core.domain.usecase.TransactionManagementUseCase;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.AccountPersistenceAdapter;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.TransactionPersistenceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class BeanConfigurationTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private TransactionPersistencePort transactionPersistencePort;

    @Mock
    private TransactionManagementPort transactionManagementPort;

    @Mock
    private AccountManagementPort accountManagementPort;

    private BeanConfiguration configuration;

    @BeforeEach
    void setup() {
        configuration = new BeanConfiguration();
    }


    @Test
    void givenDataSource_whenJdbcTemplateBeanCreated_thenJdbcTemplateUsesSameDataSource() {

        // When
        JdbcTemplate jdbcTemplate = configuration.jdbcTemplate(dataSource);

        // Then
        assertThat(jdbcTemplate).isNotNull();
        assertThat(jdbcTemplate.getDataSource()).isSameAs(dataSource);
    }

    @Test
    void givenJdbcTemplate_whenAccountPersistencePortBeanCreated_thenAccountPersistenceAdapterIsReturned() {

        // When
        AccountPersistencePort port = configuration.accountPersistencePort(jdbcTemplate);

        // Then
        assertThat(port).isNotNull();
        assertThat(port).isInstanceOf(AccountPersistenceAdapter.class);
    }

    @Test
    void givenJdbcTemplate_whenTransactionPersistencePortBeanCreated_thenTransactionPersistenceAdapterIsReturned() {

        TransactionPersistencePort port = configuration.transactionPersistencePort(jdbcTemplate);

        // Then
        assertThat(port).isNotNull();
        assertThat(port).isInstanceOf(TransactionPersistenceAdapter.class);
    }

    @Test
    void givenAccountPersistencePort_whenAccountManagementPortBeanCreated_thenAccountManagementUseCaseIsReturned() {
        //When
        AccountManagementPort port = configuration.accountManagementPort(accountPersistencePort);

        // Then
        assertThat(port).isNotNull();
        assertThat(port).isInstanceOf(AccountManagementUseCase.class);
    }

    @Test
    void givenTransactionPersistencePort_whenTransactionManagementPortBeanCreated_thenTransactionManagementUseCaseIsReturned() {

        // When
        TransactionManagementPort port = configuration.transactionManagementPort(transactionPersistencePort);

        // Then
        assertThat(port).isNotNull();
        assertThat(port).isInstanceOf(TransactionManagementUseCase.class);
    }
}
