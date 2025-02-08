package com.zipsoon.common.config.typehandler;

import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(EstateSnapshot.PlatformType.class)
public class PlatformTypeHandler extends BaseTypeHandler<EstateSnapshot.PlatformType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EstateSnapshot.PlatformType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getKoreanName());
    }

    @Override
    public EstateSnapshot.PlatformType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EstateSnapshot.PlatformType.of(rs.getString(columnName));
    }

    @Override
    public EstateSnapshot.PlatformType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EstateSnapshot.PlatformType.of(rs.getString(columnIndex));
    }

    @Override
    public EstateSnapshot.PlatformType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EstateSnapshot.PlatformType.of(cs.getString(columnIndex));
    }
}