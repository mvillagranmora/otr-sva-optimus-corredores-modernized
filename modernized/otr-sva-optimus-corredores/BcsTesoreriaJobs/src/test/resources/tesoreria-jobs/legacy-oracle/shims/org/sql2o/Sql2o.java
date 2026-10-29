package org.sql2o;

/** ORACLE SHIM: BcsJob.getSql2o() is never called by the characterized jobs; only the type has to exist. */
public class Sql2o {
    public Sql2o(javax.sql.DataSource ds) { }
}
