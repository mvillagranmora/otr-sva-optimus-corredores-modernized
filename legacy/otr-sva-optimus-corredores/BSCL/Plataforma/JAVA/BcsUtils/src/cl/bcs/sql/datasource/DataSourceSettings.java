package cl.bcs.sql.datasource;

import java.io.Serializable;
import java.util.Properties;

public class DataSourceSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String dataSourceName;
    private final String driverClassName;
    private final String password;
    private final String url;
    private final String username;

    public DataSourceSettings(
            final Properties props) {
        this.dataSourceName = props.getProperty("datasource.name");
        this.driverClassName = props.getProperty("datasource.driver");
        this.password = props.getProperty("datasource.password");
        this.url = props.getProperty("datasource.url");
        this.username = props.getProperty("datasource.user");
    }

    public String getDataSourceName() {
        return this.dataSourceName;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public String toString() {
        return "DataSourceSettings [dataSourceName=" + this.dataSourceName + ", driverClassName="
                + this.driverClassName + ", password=" + this.password + ", url=" + this.url + ", username="
                + this.username + "]";
    }

}
