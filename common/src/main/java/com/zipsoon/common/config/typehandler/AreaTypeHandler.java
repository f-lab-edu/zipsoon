package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.value.Area;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Area 값 객체를 위한 MyBatis TypeHandler
 */
public class AreaTypeHandler extends BaseTypeHandler<Area> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Area parameter, JdbcType jdbcType) throws SQLException {
        ps.setBigDecimal(i, parameter.squareMeters());
    }

    @Override
    public Area getNullableResult(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);
        return value != null ? Area.ofSquareMeters(value) : null;
    }

    @Override
    public Area getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        return value != null ? Area.ofSquareMeters(value) : null;
    }

    @Override
    public Area getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        BigDecimal value = cs.getBigDecimal(columnIndex);
        return value != null ? Area.ofSquareMeters(value) : null;
    }
}