package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import com.takeHome.Pismo.core.exception.PersistenceException;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.KEY_GENERATION_ERROR_MSG;

public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionPersistenceMapper transactionPersistenceMapper;

    public TransactionPersistenceAdapter(JdbcTemplate jdbcTemplate, TransactionPersistenceMapper transactionPersistenceMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionPersistenceMapper = transactionPersistenceMapper;
    }

    private final RowMapper<TransactionEntity> transactionRowMapper = (rs, rowNum) ->
            new TransactionEntity(rs.getLong("TRANSACTION_ID"),
                    rs.getLong("ACCOUNT_ID"),
                    rs.getInt("OPERATIONTYPE_ID"),
                    rs.getBigDecimal("AMOUNT"),
                    rs.getBigDecimal("BALANCE"),
                    convertToLocalDate(rs.getTimestamp("EVENT_DATE"))
               );

    private LocalDateTime convertToLocalDate(Timestamp eventDate) {
        if(eventDate != null){
            return eventDate.toLocalDateTime();
        }
        return null;
    }


    @Override
    public BalanceBearingTransaction save(BalanceBearingTransaction executableTransaction) {

        TransactionEntity transactionEntity = transactionPersistenceMapper.toEntity(executableTransaction);

        String sql = "INSERT INTO TRANSACTIONS(ACCOUNT_ID, OPERATIONTYPE_ID, AMOUNT, BALANCE, EVENT_DATE) VALUES(?,?,?,?,?)";
        KeyHolder holder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, transactionEntity.accountId());
                ps.setInt(2, transactionEntity.operationTypeId());
                ps.setBigDecimal(3, transactionEntity.amount());
                ps.setBigDecimal(4, transactionEntity.balance());
                ps.setTimestamp(5, Timestamp.valueOf(transactionEntity.eventDate()));
                return ps;
            }, holder);
        } catch(DataAccessException ex) {
            Throwable root = ExceptionUtils.getRootCause(ex);
            if (root instanceof SQLIntegrityConstraintViolationException sqlEx && sqlEx.getErrorCode() == 1452) {
                throw new AccountDoesNotExistException(ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(transactionEntity.accountId()));
            }
            throw ex;
        }

        if (holder.getKey() == null) {
            throw new PersistenceException(KEY_GENERATION_ERROR_MSG.formatted("transaction"));
        }

        transactionEntity.updateTransactionId(holder.getKey().longValue());
        return transactionPersistenceMapper.toTransaction(transactionEntity);
    }

    @Override
    public List<BalanceBearingTransaction> fetchDebitTransactions(long accountId) {

        String sql = "SELECT TRANSACTION_ID,ACCOUNT_ID, OPERATIONTYPE_ID, AMOUNT, BALANCE, EVENT_DATE FROM TRANSACTIONS WHERE ACCOUNT_ID = ? " +
                "AND OPERATIONTYPE_ID != 4 ORDER BY EVENT_DATE";

        List<TransactionEntity> transactionEntities =  jdbcTemplate.query(sql, transactionRowMapper, accountId);

        return transactionEntities.stream().map(transactionPersistenceMapper::toTransaction).toList();
    }


    @Override
    public void updateTransaction(BalanceBearingTransaction executableTransaction) {

        TransactionEntity transactionEntity = transactionPersistenceMapper.toEntity(executableTransaction);

        String sql = "UPDATE TRANSACTIONS SET BALANCE = ? WHERE TRANSACTION_ID = ? ";

        jdbcTemplate.update(sql, transactionEntity.balance(), transactionEntity.transactionId());
    }
}
