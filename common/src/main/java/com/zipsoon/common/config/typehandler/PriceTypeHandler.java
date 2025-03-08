package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.value.Price;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Price 값 객체를 위한 MyBatis TypeHandler
 */
public class PriceTypeHandler extends BaseTypeHandler<Price> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Price parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, parameter.amount());
    }

    @Override
    public Price getNullableResult(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);
        return value != null ? Price.of(value) : null;
    }

    @Override
    public Price getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        return value != null ? Price.of(value) : null;
    }

    @Override
    public Price getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        BigDecimal value = cs.getBigDecimal(columnIndex);
        return value != null ? Price.of(value) : null;
    }
}