package cl.bcs.sql.utils;

import java.io.Serializable;
import java.util.Properties;

import cl.bcs.utils.PropertiesUtil;

public final class SqlConnectionSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private String password;
    private String url;
    private String user;

    public SqlConnectionSettings(final Properties props, final String name) {
        this.url = PropertiesUtil.getRequiredString(props, name + ".url");
        this.user = PropertiesUtil.getRequiredString(props, name + ".user");
        this.password = PropertiesUtil.getRequiredString(props, name + ".password");
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ConnectionConfig [password=" + password + ", url=" + url + ", user=" + user + "]";
    }

}
