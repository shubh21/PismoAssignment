package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;

public record CreateAccountRequest(@JsonProperty("document_number") Long documentNumber){

    public CreateAccountRequest(Long documentNumber){

        if(Objects.isNull(documentNumber) || documentNumber <= 0L){
            throw new IllegalArgumentException(INVALID_DOCUMENT_NUMBER_MSG.formatted(documentNumber));
        }
        this.documentNumber = documentNumber;
    }
}
