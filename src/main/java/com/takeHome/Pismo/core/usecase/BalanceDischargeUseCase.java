package com.takeHome.Pismo.core.usecase;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.TransactionDischargeResult;
import com.takeHome.Pismo.core.domain.port.in.BalanceDischargePort;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class BalanceDischargeUseCase implements BalanceDischargePort {

    @Override
    public TransactionDischargeResult discharge(BalanceBearingTransaction paymentTransaction, List<? extends BalanceBearingTransaction> openDebits) {

        if(!paymentTransaction.isPayment()){
            throw new IllegalArgumentException("Invalid discharge operation");
        }

        openDebits.sort(Comparator.comparing((BalanceBearingTransaction tx) -> tx.transaction().eventDate()));

        for (BalanceBearingTransaction debit : openDebits) {

            BigDecimal paymentBalance = paymentTransaction.balance();

            if (debit.isPayment() || debit.isSettled()) {
                continue;
            }
            if (paymentBalance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal amountToApply = paymentBalance.min(debit.balance().negate());
            debit.applyPayment(amountToApply);
            paymentTransaction.applyPayment(amountToApply.negate());
        }
        return new TransactionDischargeResult(paymentTransaction, openDebits);
    }
}
