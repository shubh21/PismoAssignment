package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.Account;
import com.takeHome.Pismo.core.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import static com.takeHome.Pismo.core.Constants.KEY_GENERATION_ERROR_MSG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountPersistenceAdapterTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    AccountPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new AccountPersistenceAdapter(jdbcTemplate);
    }

    @Test
    void givenNullGeneratedKey_whenSave_thenPersistenceExceptionIsThrown() {
        // Given
        when(jdbcTemplate.update(any(), any(KeyHolder.class))).thenAnswer(invocation -> 1);

        Account account = Account.builder().documentNumber(100L).build();

        // When - Then
        assertThatThrownBy(() -> adapter.save(account))
                .isInstanceOf(PersistenceException.class)
                .hasMessage(KEY_GENERATION_ERROR_MSG.formatted("account"));

        verify(jdbcTemplate).update(any(), any(KeyHolder.class));
    }
}
