package cl.bcs.sql.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public final class DataSourceProvider {
    private static final Map<String, DataSource> DATASOURCES = new ConcurrentHashMap<String, DataSource>();
    private static final Object DATASOURCES_MUTEX = new Object();

    public static DataSource getInstance(
            final DataSourceSettings settings) {
        final String key = settings.getDataSourceName();

        synchronized (DATASOURCES_MUTEX) {
            if (!DATASOURCES.containsKey(key)) {
                final DataSource dataSource = newDataSource(settings);
                DATASOURCES.put(key, dataSource);
            }

            return DATASOURCES.get(key);
        }
    }

    private static DataSource newDataSource(
            final DataSourceSettings settings) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(settings.getDriverClassName());
        dataSource.setUrl(settings.getUrl());
        dataSource.setUsername(settings.getUsername());
        dataSource.setPassword(settings.getPassword());
        return dataSource;
    }

    private DataSourceProvider() {
    }
}
