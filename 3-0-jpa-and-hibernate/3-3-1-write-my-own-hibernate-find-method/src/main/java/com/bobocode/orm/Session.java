package com.bobocode.orm;

import com.bobocode.orm.annotations.Column;
import com.bobocode.orm.annotations.Table;
import com.bobocode.orm.utils.EntityKey;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Session {
    private DataSource dataSource;
    private static String SQL_SELECT = "select * from %s where id = ?";
    final Map<com.bobocode.orm.utils.EntityKey<?>, Object> cache = new HashMap<>();

    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    public <T> T find(Class<T> type, Object id) {
        var key = new EntityKey<>(type, id);

        if (cache.containsKey(key)) {
            return type.cast(cache.get(key));
        }

        T entity = type.getDeclaredConstructor().newInstance();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getSelectQuery(type))) {

            preparedStatement.setObject(1, id);
            System.out.printf("Executing query: %s%n", preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    populateEntityFields(entity, type, resultSet);
                    cache.put(key, entity);
                    return entity;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }

        return null;
    }

    @SneakyThrows
    private <T> void populateEntityFields(T entity, Class<T> type, ResultSet resultSet) {
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);

            if (column != null) {
                Object value = resultSet.getObject(column.name());
                if (value != null) {
                    field.set(entity, value);
                }
            }
        }
    }

    private String getSelectQuery(Class<?> classType) {
        var annotation = classType.getAnnotation(Table.class);
        final String tableName = annotation.name();
        return String.format(SQL_SELECT, tableName);
    }
}
