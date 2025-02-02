package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.PropertySnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(PropertySnapshot.PlatformType.class)
public class PlatformTypeHandler extends BaseTypeHandler<PropertySnapshot.PlatformType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PropertySnapshot.PlatformType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public PropertySnapshot.PlatformType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return PropertySnapshot.PlatformType.of(rs.getString(columnName));
    }

    @Override
    public PropertySnapshot.PlatformType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return PropertySnapshot.PlatformType.of(rs.getString(columnIndex));
    }

    @Override
    public PropertySnapshot.PlatformType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return PropertySnapshot.PlatformType.of(cs.getString(columnIndex));
    }
}