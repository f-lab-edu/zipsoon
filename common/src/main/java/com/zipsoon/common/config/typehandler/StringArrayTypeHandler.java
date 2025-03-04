package com.zipsoon.common.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@MappedTypes(List.class)
public class StringArrayTypeHandler extends BaseTypeHandler<List<String>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        String[] array = parameter.toArray(new String[0]);
        ps.setArray(i, ps.getConnection().createArrayOf("varchar", array));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        java.sql.Array sqlArray = rs.getArray(columnName);
        if (sqlArray == null) {
            return null;
        }
        String[] array = (String[]) sqlArray.getArray();
        return Arrays.asList(array);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Array sqlArray = rs.getArray(columnIndex);
        if (sqlArray == null) {
            return null;
        }
        String[] array = (String[]) sqlArray.getArray();
        return Arrays.asList(array);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        java.sql.Array sqlArray = cs.getArray(columnIndex);
        if (sqlArray == null) {
            return null;
        }
        String[] array = (String[]) sqlArray.getArray();
        return Arrays.asList(array);
    }
}
