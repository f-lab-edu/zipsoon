package com.zipsoon.zipsoonbatch.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        Connection conn = ps.getConnection();
        Array array = conn.createArrayOf("text", parameter);
        ps.setArray(i, array);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);
        return getArray(array);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Array array = rs.getArray(columnIndex);
        return getArray(array);
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Array array = cs.getArray(columnIndex);
        return getArray(array);
    }

    private String[] getArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        return (String[]) array.getArray();
    }
}