package com.bobocode;

import com.bobocode.model.Movie;
import com.bobocode.orm.Session;
import com.bobocode.orm.SessionFactory;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestHibernateFindMethod {

    @Test
    public void testThatTheSameObjectWasTakenFromCache() {

        var mySession = createSession();

        var movie = mySession.find(Movie.class, 1L);

        var theSameMovie = mySession.find(Movie.class, 1L);

        assertEquals(movie, theSameMovie);
    }

    @Test
    public void testSending2SelectRequestsToDbInCaseOfQueryingDifferentObjects() {
        var mySession = createSession();

        var movie = mySession.find(Movie.class, 1L);

        var theSameMovie = mySession.find(Movie.class, 2L);

        assertNotEquals(movie, theSameMovie);
    }

    private Session createSession() {
        SessionFactory mySessionFactory = new SessionFactory(getdataSource());
        return mySessionFactory.createSession();
    }

    private DataSource getdataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("170503Dima@");
        return dataSource;
    }
}
