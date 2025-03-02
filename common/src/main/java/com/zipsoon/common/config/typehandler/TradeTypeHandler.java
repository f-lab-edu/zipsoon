package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.TradeType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TradeType.class)
public class TradeTypeHandler extends BaseTypeHandler<TradeType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TradeType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public TradeType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return TradeType.of(rs.getString(columnName));
    }

    @Override
    public TradeType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return TradeType.of(rs.getString(columnIndex));
    }

    @Override
    public TradeType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return TradeType.of(cs.getString(columnIndex));
    }
}