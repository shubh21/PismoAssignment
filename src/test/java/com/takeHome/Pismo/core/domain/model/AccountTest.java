package com.takeHome.Pismo.core.domain.model;

import com.takeHome.Pismo.core.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final long ACCOUNT_ID = 1L;
    private static final long DOCUMENT_NUMBER = 123456789L;

    @Test

    void givenValidNonNullValues_whenConstructed_thenFieldsAreSetCorrectly() {

        // When
        Account account = new Account(ACCOUNT_ID, DOCUMENT_NUMBER);

        // Then
        assertThat(account.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenNullValues_whenConstructed_thenFieldsAreNullAndAccepted() {
        // When
        Account account = new Account(null, null);

        // Then
        assertThat(account.accountId()).isNull();
        assertThat(account.documentNumber()).isNull();
    }

    @Test
    void givenNullAccountIdAndValidDocumentNumber_whenConstructed_thenOnlyDocumentNumberIsSet() {

        // When
        Account account = new Account(null, DOCUMENT_NUMBER);

        // Then
        assertThat(account.accountId()).isNull();
        assertThat(account.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenValidAccountIdAndNullDocumentNumber_whenConstructed_thenOnlyAccountIdIsSet() {

        // When
        Account account = new Account(ACCOUNT_ID, null);

        // Then
        assertThat(account.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.documentNumber()).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = { 1L, 10L, Long.MAX_VALUE})
    void givenPositive_whenConstructed_thenAccountIsCreated(long value) {
        // When
        Account account = new Account(value, value);

        // Then
        assertThat(account.accountId()).isEqualTo(value);
        assertThat(account.documentNumber()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10L, Long.MIN_VALUE})
    void givenNonPositiveAccountId_whenConstructed_thenIllegalArgumentExceptionThrown(long invalidId) {
        // When- Then
        assertThatThrownBy(() -> new Account(invalidId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_ACCOUNT_ID_MSG, invalidId));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, -10L, Long.MIN_VALUE})
    void givenNegativeDocumentNumber_whenConstructed_thenIllegalArgumentExceptionThrown(long invalidDoc) {
        // When - Then
        assertThatThrownBy(() -> new Account(1L, invalidDoc))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_DOCUMENT_NUMBER_MSG, invalidDoc));
    }

    @Test
    void givenValidValues_whenBuilderUsed_thenAccountIsCreatedCorrectly() {

        // When
        Account account = Account.builder()
                .accountId(ACCOUNT_ID)
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(account.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenOnlyAccountIdSet_whenBuilderUsed_thenAccountHasNullDocumentNumber() {

        // When
        Account account = Account.builder()
                .accountId(ACCOUNT_ID)
                .build();

        // Then
        assertThat(account.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.documentNumber()).isNull();
    }

    @Test
    void givenOnlyDocumentNumberSet_whenBuilderUsed_thenAccountHasNullAccountId() {
        // When
        Account account = Account.builder()
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(account.accountId()).isNull();
        assertThat(account.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenNoFieldsSet_whenBuilderUsed_thenAccountWithNullFieldsIsCreated() {
        // When
        Account account = Account.builder().build();

        // Then
        assertThat(account.accountId()).isNull();
        assertThat(account.documentNumber()).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, -10L, Long.MIN_VALUE})
    void givenNegativeAccountId_whenBuilderAccountIdCalled_thenIllegalArgumentExceptionThrown(long invalidId) {
        // Given
        Account.AccountBuilder builder = Account.builder();

        // When / Then
        assertThatThrownBy(() -> builder.accountId(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_ACCOUNT_ID_MSG, invalidId));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, -10L, Long.MIN_VALUE})
    void givenNegativeDocumentNumber_whenBuilderDocumentNumberCalled_thenIllegalArgumentExceptionThrown(long invalidDoc) {
        // Given
        Account.AccountBuilder builder = Account.builder();

        // When / Then
        assertThatThrownBy(() -> builder.documentNumber(invalidDoc))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_DOCUMENT_NUMBER_MSG, invalidDoc));
    }

    @Test
    void givenSameValuesSetInDifferentOrder_whenBuilderUsed_thenResultingAccountsAreEqual() {
        // When
        Account account1 = Account.builder()
                .accountId(ACCOUNT_ID)
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        Account account2 = Account.builder()
                .documentNumber(DOCUMENT_NUMBER)
                .accountId(ACCOUNT_ID)
                .build();

        // Then
        assertThat(account1).isEqualTo(account2);
        assertThat(account1.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account1.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }


    @Test
    void givenTwoAccountsWithSameValues_whenCompared_thenTheyAreEqual() {
        // Given
        Account a1 = new Account(ACCOUNT_ID, DOCUMENT_NUMBER);
        Account a2 = new Account(ACCOUNT_ID, DOCUMENT_NUMBER);

        // Then
        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void givenTwoAccountsWithDifferentValues_whenCompared_thenTheyAreNotEqual() {
        // Given
        Account a1 = new Account(1L, 2L);
        Account a2 = new Account(1L, 3L);

        // Then
        assertThat(a1).isNotEqualTo(a2);
    }
}