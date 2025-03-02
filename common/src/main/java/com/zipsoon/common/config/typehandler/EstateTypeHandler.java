package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.EstateType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(EstateType.class)
public class EstateTypeHandler extends BaseTypeHandler<EstateType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EstateType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public EstateType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EstateType.of(rs.getString(columnName));
    }

    @Override
    public EstateType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EstateType.of(rs.getString(columnIndex));
    }

    @Override
    public EstateType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EstateType.of(cs.getString(columnIndex));
    }
}