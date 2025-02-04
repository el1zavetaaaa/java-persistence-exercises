package com.bobocode;

import com.bobocode.model.Movie;
import com.bobocode.orm.Session;
import com.bobocode.orm.SessionFactory;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testUpdateMethodWhenTheFieldsAreChanged() {
        ByteArrayOutputStream outContent = setUpByteArrayOutputStream();
        var mySession = createSession();

        var movie = mySession.find(Movie.class, 1L);
        movie.setName("UPDATED");

        mySession.close();

        String printedOutput = outContent.toString();
        assertAll(
                () -> assertTrue(printedOutput.contains("select")),
                () -> assertTrue(printedOutput.contains("update"))
        );
    }

    @Test
    public void testUpdateMethodWhenTheFieldsAreNOTChanged() {
        ByteArrayOutputStream outContent = setUpByteArrayOutputStream();
        var mySession = createSession();

        var movie = mySession.find(Movie.class, 1L);

        mySession.close();


        String printedOutput = outContent.toString();
        assertAll(
                () -> assertTrue(printedOutput.contains("select")),
                () -> assertFalse(printedOutput.contains("update"))
        );
    }

    private ByteArrayOutputStream setUpByteArrayOutputStream() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        return outContent;
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
