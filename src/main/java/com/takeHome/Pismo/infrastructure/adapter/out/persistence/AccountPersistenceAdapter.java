package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.exception.DuplicateDocumentNumberException;
import com.takeHome.Pismo.core.exception.PersistenceException;
import com.takeHome.Pismo.core.domain.model.Account;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;

import static com.takeHome.Pismo.core.Constants.DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.KEY_GENERATION_ERROR_MSG;

public class AccountPersistenceAdapter implements AccountPersistencePort {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) ->
                                                            new Account(rs.getLong("ACCOUNT_ID"),
                                                                    rs.getLong("DOCUMENT_NUMBER"));

    public AccountPersistenceAdapter(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    public Account save(Account account){
        String sql = "INSERT INTO ACCOUNTS (DOCUMENT_NUMBER) VALUES(?)";
        KeyHolder holder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, account.documentNumber());
                return ps;
            }, holder);
        } catch (DataIntegrityViolationException ex) {
            Throwable rootException = ExceptionUtils.getRootCause(ex);

            if (rootException instanceof SQLIntegrityConstraintViolationException sqlEx
                    && sqlEx.getErrorCode() == 1062) {
                throw new DuplicateDocumentNumberException(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(account.documentNumber()));
            }
            throw ex;
        }

        if (holder.getKey() == null) {
            throw new PersistenceException(KEY_GENERATION_ERROR_MSG.formatted("account"));
        }

        return Account.builder()
                .accountId(holder.getKey().longValue())
                .documentNumber(account.documentNumber()).build();
    }

    public Optional<Account> retrieve(long accountId){
        String sql = "SELECT ACCOUNT_ID, DOCUMENT_NUMBER FROM ACCOUNTS WHERE ACCOUNT_ID= ?";
        try {
            Account account = jdbcTemplate.queryForObject(sql, accountRowMapper, accountId);
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }
}
