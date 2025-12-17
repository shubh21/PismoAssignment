package com.takeHome.Pismo.core.domain.port.in;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.TransactionDischargeResult;

import java.util.List;

public interface BalanceDischargePort {

    TransactionDischargeResult discharge(
            BalanceBearingTransaction payment,
            List<? extends BalanceBearingTransaction> openDebits
    );
}
