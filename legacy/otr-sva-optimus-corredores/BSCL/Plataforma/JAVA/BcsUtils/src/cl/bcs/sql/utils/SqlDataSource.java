package cl.bcs.sql.utils;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public final class SqlDataSource {

    private static BasicDataSource ds;
    private static Object mutex = new Object();

    private static BasicDataSource createDataSource(
            final SqlConnectionSettings settings)
                    throws IOException,
                    SQLException,
                    PropertyVetoException {
        final BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("com.sap.db.jdbc.Driver");
        bds.setUsername(settings.getUser());
        bds.setPassword(settings.getPassword());
        bds.setUrl(settings.getUrl());

        // the settings below are optional -- dbcp can work with defaults.
        bds.setMinIdle(5);
        bds.setMaxIdle(20);
        bds.setMaxOpenPreparedStatements(180);
        return bds;
    }

    public static DataSource getInstance(
            final SqlConnectionSettings settings)
                    throws IOException,
                    SQLException,
                    PropertyVetoException {

        synchronized (mutex) {
            if (ds == null) {
                ds = createDataSource(settings);
            }

            return ds;
        }
    }

}
