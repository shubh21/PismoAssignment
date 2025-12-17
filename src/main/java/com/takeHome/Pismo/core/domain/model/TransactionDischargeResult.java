package com.takeHome.Pismo.core.domain.model;

import java.util.List;

public record TransactionDischargeResult(
        BalanceBearingTransaction payment,
        List<? extends BalanceBearingTransaction> updatedDebits
) {}

