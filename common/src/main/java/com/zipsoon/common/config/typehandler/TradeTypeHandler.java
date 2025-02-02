package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.PropertySnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(PropertySnapshot.TradeType.class)
public class TradeTypeHandler extends BaseTypeHandler<PropertySnapshot.TradeType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PropertySnapshot.TradeType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public PropertySnapshot.TradeType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return PropertySnapshot.TradeType.of(rs.getString(columnName));
    }

    @Override
    public PropertySnapshot.TradeType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return PropertySnapshot.TradeType.of(rs.getString(columnIndex));
    }

    @Override
    public PropertySnapshot.TradeType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return PropertySnapshot.TradeType.of(cs.getString(columnIndex));
    }
}