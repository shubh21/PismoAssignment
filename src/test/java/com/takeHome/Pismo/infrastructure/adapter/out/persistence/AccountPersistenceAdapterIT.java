package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.Account;
import com.takeHome.Pismo.core.exception.DuplicateDocumentNumberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.takeHome.Pismo.core.Constants.DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.sql.init.mode=always"
})
public class AccountPersistenceAdapterIT {

    private static final long DOCUMENT_NUMBER = 12345678900L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AccountPersistenceAdapter accountPersistenceAdapter;

    @BeforeEach
    void setUp() {
        accountPersistenceAdapter = new AccountPersistenceAdapter(jdbcTemplate);
    }

    @Test
    void givenDocumentNumber_whenSaveAccount_thenReturnSavedAccountId() {

        // When
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(DOCUMENT_NUMBER).build());

        // Then
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.accountId()).isGreaterThan(0L);
        assertThat(savedAccount.documentNumber()).isEqualTo(DOCUMENT_NUMBER);

        Long storedDocNum = jdbcTemplate.queryForObject(
                "SELECT DOCUMENT_NUMBER FROM ACCOUNTS WHERE ACCOUNT_ID = ?",
                Long.class,
                savedAccount.accountId()
        );
        assertThat(storedDocNum).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenExistingAccountForDocumentNumber_whenSaveAccount_thenThrowDuplicateDocumentNumberException() {

        // Given
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(DOCUMENT_NUMBER).build());

        //Then
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.accountId()).isGreaterThan(0L);
        assertThat(savedAccount.documentNumber()).isEqualTo(DOCUMENT_NUMBER);

        Account account2 = Account.builder().documentNumber(DOCUMENT_NUMBER).build();

        assertThatThrownBy(() -> accountPersistenceAdapter.save(account2))
                .isInstanceOf(DuplicateDocumentNumberException.class)
                .hasMessage(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(DOCUMENT_NUMBER));
    }

    @Test
    void givenAccountId_whenRetrieveAccount_thenReturnAccountInfo() {
        // Given
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(DOCUMENT_NUMBER).build());

        // When
        Optional<Account> result = accountPersistenceAdapter.retrieve(savedAccount.accountId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get().accountId()).isEqualTo(savedAccount.accountId());
        assertThat(result.get().documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenAccountDoesNotExistInDb_whenRetrieveAccount_thenReturnEmptyResult() {
        //given-when
        Optional<Account> result = accountPersistenceAdapter.retrieve(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        assertThatThrownBy(result::get).isInstanceOf(NoSuchElementException.class);
    }

}
