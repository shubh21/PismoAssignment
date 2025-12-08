package com.takeHome.Pismo.core.contract.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountResultTest {

    private static final long DOCUMENT_NUMBER = 123456789L;
    private static final long ACCOUNT_ID = 123L;

    @Test
    void givenValidValues_whenBuilderUsed_thenAccountResultIsCreatedCorrectly() {

        // When
        AccountResult result = AccountResult.builder()
                .accountId(ACCOUNT_ID)
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenValidValues_whenConstructorCalled_thenAccountResultIsCreatedCorrectly() {

        // When
        AccountResult result = new AccountResult(ACCOUNT_ID, DOCUMENT_NUMBER);

        // Then
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100",
            "999999, 123456789",
            "5555, 8888"
    })
    void givenMultiplePairs_whenBuilderUsed_thenCorrectValuesAreStored(long accountId, long documentNumber) {
        // When
        AccountResult result = AccountResult.builder()
                .accountId(accountId)
                .documentNumber(documentNumber)
                .build();

        // Then
        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.documentNumber()).isEqualTo(documentNumber);
    }

    @Test
    void givenOnlyDocumentNumberSet_whenBuilderUsed_thenAccountIdDefaultsToZero() {
        // Given

        // When
        AccountResult result = AccountResult.builder()
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(result.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(result.accountId()).isZero();
    }

    @Test
    void givenOnlyAccountIdSet_whenBuilderUsed_thenDocumentNumberDefaultsToZero() {

        // When
        AccountResult result = AccountResult.builder()
                .accountId(ACCOUNT_ID)
                .build();

        // Then
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.documentNumber()).isZero();
    }

    @Test
    void givenFieldsSetInDifferentOrder_whenBuilderUsed_thenResultIsSame() {


        // When
        AccountResult resultOrder1 = AccountResult.builder()
                .documentNumber(DOCUMENT_NUMBER)
                .accountId(ACCOUNT_ID)
                .build();

        AccountResult resultOrder2 = AccountResult.builder()
                .accountId(ACCOUNT_ID)
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(resultOrder1.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(resultOrder1.accountId()).isEqualTo(ACCOUNT_ID);

        assertThat(resultOrder2.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(resultOrder2.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(resultOrder1).isEqualTo(resultOrder2);
    }
}

