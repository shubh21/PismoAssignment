package com.takeHome.Pismo.core.usecase;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.TransactionDischargeResult;
import com.takeHome.Pismo.core.domain.port.in.BalanceDischargePort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BalanceDischargeUseCase implements BalanceDischargePort {

    @Override
    public TransactionDischargeResult discharge(BalanceBearingTransaction paymentTransaction, List<? extends BalanceBearingTransaction> openDebits) {

        if(Objects.isNull(paymentTransaction)|| Objects.isNull(openDebits) || !paymentTransaction.isPayment()){
            throw new IllegalArgumentException("Invalid discharge operation");
        }
        List<BalanceBearingTransaction> mutableDebits = openDebits.stream()
                .sorted(Comparator.comparing(tx -> tx.transaction().eventDate(),
                Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        for (BalanceBearingTransaction debit : mutableDebits) {

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
        return new TransactionDischargeResult(paymentTransaction, mutableDebits);
    }
}
