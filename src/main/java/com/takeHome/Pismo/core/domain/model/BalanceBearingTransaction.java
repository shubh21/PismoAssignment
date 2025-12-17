package com.takeHome.Pismo.core.domain.model;

import java.math.BigDecimal;

public interface BalanceBearingTransaction {

    Transaction transaction();

    BigDecimal balance();

    boolean isSettled();

    void applyPayment(BigDecimal amount);

    boolean isPayment();
}