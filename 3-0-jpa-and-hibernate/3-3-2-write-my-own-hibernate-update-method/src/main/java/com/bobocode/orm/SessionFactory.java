package com.bobocode.orm;

import javax.sql.DataSource;

public class SessionFactory {
    private DataSource dataSource;

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        return new Session(dataSource);
    }
}
