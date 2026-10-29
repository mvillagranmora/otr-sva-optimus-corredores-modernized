package cl.bcs.reports.report;

import java.sql.ResultSet;

public class SubreportDocument {
    private String id;
    private String name;
    private String resultSetAlias;
    private String query;
    private ResultSet resultSet;

    public SubreportDocument() {
    }

    public SubreportDocument(String id, String name, String resultSetAlias, String query, ResultSet resultSet) {
        this.id = id;
        this.name = name;
        this.resultSetAlias = resultSetAlias;
        this.query = query;
        this.resultSet = resultSet;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRSAlias(String resultSetAlias) {
        this.resultSetAlias = resultSetAlias;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getRSAlias() {
        return this.resultSetAlias;
    }

    public String getQuery() {
        return this.query;
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }
}
