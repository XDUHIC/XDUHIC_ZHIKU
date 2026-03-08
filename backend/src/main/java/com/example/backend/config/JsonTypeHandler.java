package com.example.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON类型处理器
 */
public class JsonTypeHandler extends BaseTypeHandler<Object> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 如果参数是 String 类型，直接存储原始值，不进行 JSON 序列化
            // 这避免了字符串被加上额外的引号（如 /uploads/images/xxx.png 变成 "/uploads/images/xxx.png"）
            if (parameter instanceof String) {
                ps.setString(i, (String) parameter);
            } else {
                ps.setString(i, objectMapper.writeValueAsString(parameter));
            }
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting object to JSON", e);
        }
    }
    
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }
    
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }
    
    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }
    
    private Object parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json; // 如果解析失败，返回原始字符串
        }
    }
}
