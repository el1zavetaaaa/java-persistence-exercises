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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private DataSource dataSource;
    private static String SQL_SELECT = "select * from %s where id = ?";
    private static String SQL_UPDATE = "update %s set %s where id = ?";
    final Map<com.bobocode.orm.utils.EntityKey<?>, Object> cache = new HashMap<>();
    final Map<EntityKey<?>, List<Object>> snapshotCopies = new HashMap<>();

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
             PreparedStatement preparedStatement = connection.prepareStatement(String.format(SQL_SELECT, getTableName(type)))) {

            preparedStatement.setObject(1, id);
            System.out.printf("Executing query: %s%n", preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    snapshotCopies.put(key, new ArrayList<>());
                    populateEntityFields(key, entity, type, resultSet);
                    cache.put(key, entity);
                    return entity;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing select query", e);
        }

        return null;
    }

    @SneakyThrows
    private <T> void populateEntityFields(EntityKey<?> key, T entity, Class<T> type, ResultSet resultSet) {
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);

            if (column != null) {
                Object value = resultSet.getObject(column.name());
                if (value != null) {
                    field.set(entity, value);
                    snapshotCopies.get(key).add(value);
                }
            }
        }
    }

    @SneakyThrows
    public <T> void close(){
        for (Map.Entry<EntityKey<?>, List<Object>> currentSnapShot: snapshotCopies.entrySet()){
            EntityKey<?> currentKey = currentSnapShot.getKey();

            Object currentEntity = cache.get(currentKey);

            final List<Object> currentEntityValues = new ArrayList<>();

            for (Field field : currentEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final var value = field.get(currentEntity);
                currentEntityValues.add(value);
            }

            if(!currentSnapShot.getValue().equals(currentEntityValues)){
                updateEntity(currentEntity);
            }
        }
    }

    @SneakyThrows
    private <T> void updateEntity(T entity){
        final List<String> setClause = new ArrayList<>();
        final List<Object> params = new ArrayList<>();
        Object idVal = 0L;
        for (Field field: entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            var column = field.getAnnotation(Column.class);
            if(column != null){
                if(column.name().equals("id")){
                    idVal = field.get(entity);
                } else {
                    setClause.add(column.name() + "= ?");
                    params.add(field.get(entity));
                }
            }
        }

        String setClauseJoin = String.join(",", setClause);
        params.add(idVal);

        try (Connection connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                    String.format(SQL_UPDATE, getTableName(entity.getClass()), setClauseJoin)))
        {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            System.out.printf("Executing query: %s%n", preparedStatement);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error executing update query", e);
        }
    }

    private String getTableName(Class<?> classType) {
        var annotation = classType.getAnnotation(Table.class);
        return annotation.name();
    }
}
