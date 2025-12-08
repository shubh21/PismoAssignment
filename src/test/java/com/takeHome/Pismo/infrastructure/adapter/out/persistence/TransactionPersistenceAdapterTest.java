package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.takeHome.Pismo.core.Constants.KEY_GENERATION_ERROR_MSG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionPersistenceAdapterTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    TransactionPersistenceAdapter adapter;

    @BeforeEach
    void setup(){
        adapter = new TransactionPersistenceAdapter(jdbcTemplate);
    }

    @Test
    void givenNullGeneratedKey_whenSave_thenPersistenceExceptionIsThrown() {
        // When
        when(jdbcTemplate.update(any(), any(KeyHolder.class))).thenAnswer(invocation -> 1);

        Transaction tx = Transaction.builder()
                .accountId(1L)
                .operationTypeId(1)
                .amount(BigDecimal.TEN)
                .eventDate(LocalDateTime.now())
                .build();

        // When - Then
        assertThatThrownBy(() -> adapter.save(tx))
                .isInstanceOf(PersistenceException.class)
                .hasMessage(KEY_GENERATION_ERROR_MSG.formatted("transaction"));

        verify(jdbcTemplate).update(any(), any(KeyHolder.class));
    }
}
