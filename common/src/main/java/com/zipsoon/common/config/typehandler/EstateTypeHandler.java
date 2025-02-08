package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(EstateSnapshot.EstateType.class)
public class EstateTypeHandler extends BaseTypeHandler<EstateSnapshot.EstateType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EstateSnapshot.EstateType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public EstateSnapshot.EstateType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EstateSnapshot.EstateType.of(rs.getString(columnName));
    }

    @Override
    public EstateSnapshot.EstateType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EstateSnapshot.EstateType.of(rs.getString(columnIndex));
    }

    @Override
    public EstateSnapshot.EstateType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EstateSnapshot.EstateType.of(cs.getString(columnIndex));
    }
}