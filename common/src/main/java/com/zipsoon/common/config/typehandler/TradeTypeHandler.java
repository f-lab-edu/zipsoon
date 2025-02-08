package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(EstateSnapshot.TradeType.class)
public class TradeTypeHandler extends BaseTypeHandler<EstateSnapshot.TradeType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EstateSnapshot.TradeType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public EstateSnapshot.TradeType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EstateSnapshot.TradeType.of(rs.getString(columnName));
    }

    @Override
    public EstateSnapshot.TradeType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EstateSnapshot.TradeType.of(rs.getString(columnIndex));
    }

    @Override
    public EstateSnapshot.TradeType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EstateSnapshot.TradeType.of(cs.getString(columnIndex));
    }
}