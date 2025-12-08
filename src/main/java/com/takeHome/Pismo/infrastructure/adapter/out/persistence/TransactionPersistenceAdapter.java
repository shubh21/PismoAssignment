package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import com.takeHome.Pismo.core.exception.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.KEY_GENERATION_ERROR_MSG;

public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final JdbcTemplate jdbcTemplate;

    public TransactionPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Transaction save(Transaction transaction) {

        String sql = "INSERT INTO TRANSACTIONS(ACCOUNT_ID, OPERATIONTYPE_ID, AMOUNT, EVENT_DATE) VALUES(?,?,?,?)";
        KeyHolder holder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, transaction.accountId());
                ps.setInt(2, transaction.operationTypeId());
                ps.setBigDecimal(3, transaction.amount());
                ps.setTimestamp(4, Timestamp.valueOf(transaction.eventDate()));
                return ps;
            }, holder);
        } catch(DataAccessException ex) {
            Throwable root = ExceptionUtils.getRootCause(ex);
            if (root instanceof SQLIntegrityConstraintViolationException sqlEx && sqlEx.getErrorCode() == 1452) {
                throw new AccountDoesNotExistException(ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(transaction.accountId()));
            }
            throw ex;
        }

        if (holder.getKey() == null) {
            throw new PersistenceException(KEY_GENERATION_ERROR_MSG.formatted("transaction"));
        }

        return Transaction.builder().transactionId(holder.getKey().longValue())
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId())
                .amount(transaction.amount())
                .eventDate(transaction.eventDate())
                .build();
    }
}
