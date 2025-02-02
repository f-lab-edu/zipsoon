package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.PropertySnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(PropertySnapshot.PropType.class)
public class PropTypeHandler extends BaseTypeHandler<PropertySnapshot.PropType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PropertySnapshot.PropType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public PropertySnapshot.PropType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return PropertySnapshot.PropType.of(rs.getString(columnName));
    }

    @Override
    public PropertySnapshot.PropType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return PropertySnapshot.PropType.of(rs.getString(columnIndex));
    }

    @Override
    public PropertySnapshot.PropType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return PropertySnapshot.PropType.of(cs.getString(columnIndex));
    }
}