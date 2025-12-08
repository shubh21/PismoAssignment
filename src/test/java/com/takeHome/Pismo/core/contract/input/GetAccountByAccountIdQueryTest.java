package com.takeHome.Pismo.core.contract.input;

import com.takeHome.Pismo.core.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAccountByAccountIdQueryTest {

    @Test
    void givenValidAccountId_whenConstructed_thenValueIsStored() {
        // Given
        long accountId = 123L;

        // When
        GetAccountByAccountIdQuery query = new GetAccountByAccountIdQuery(accountId);

        // Then
        assertThat(query.accountId()).isEqualTo(accountId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 10L, 9999L, Long.MAX_VALUE})
    void givenMultipleValidValues_whenConstructed_thenValueIsStored(long accountId) {
        // When
        GetAccountByAccountIdQuery query = new GetAccountByAccountIdQuery(accountId);

        // Then
        assertThat(query.accountId()).isEqualTo(accountId);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -999L, Long.MIN_VALUE})
    void givenZeroOrNegativeValues_whenConstructed_thenExceptionIsThrown(long accountId) {

        assertThatThrownBy(() -> new GetAccountByAccountIdQuery(accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_ACCOUNT_ID_MSG,accountId));
    }

    @Test
    void givenTwoQueriesWithSameId_whenCompared_thenTheyAreEqual() {
        // Given
        long accountId = 123L;

        // When
        GetAccountByAccountIdQuery q1 = new GetAccountByAccountIdQuery(accountId);
        GetAccountByAccountIdQuery q2 = new GetAccountByAccountIdQuery(accountId);

        // Then
        assertThat(q1).isEqualTo(q2);
        assertThat(q1.hashCode()).isEqualTo(q2.hashCode());
    }
}