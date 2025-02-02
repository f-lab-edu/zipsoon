package com.zipsoon.common.config.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedTypes(JsonNode.class)
public class JsonTypeHandler extends BaseTypeHandler<JsonNode> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.toString(), Types.OTHER);
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            String json = rs.getString(columnName);
            return json != null ? MAPPER.readTree(json) : null;
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            String json = rs.getString(columnIndex);
            return json != null ? MAPPER.readTree(json) : null;
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            String json = cs.getString(columnIndex);
            return json != null ? MAPPER.readTree(json) : null;
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }
}