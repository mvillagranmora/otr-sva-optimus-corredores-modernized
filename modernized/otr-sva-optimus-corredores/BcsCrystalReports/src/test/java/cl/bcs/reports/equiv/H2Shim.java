package cl.bcs.reports.equiv;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * The H2 stand-in for HANA used by BOTH engines: schema BSCL, table "BSCL.Report::Template" (the .rpt bytes of the case,
 * copied from templates/) and the DUMMY(SYSUUID) shim that ReportDocument.insertDocument() needs for templates with
 * subreports. DUMMY is a DUMMY shim only (it replaces the HANA system table; no other HANA behaviour is imitated).
 */
public final class H2Shim {

    private H2Shim() {
    }

    public static void init(DataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE SCHEMA IF NOT EXISTS BSCL");
            s.execute("CREATE TABLE IF NOT EXISTS BSCL.\"BSCL.Report::Template\"(\"Id\" VARCHAR(64), "
                    + "\"ReportName\" VARCHAR(255), \"MasterReportId\" VARCHAR(64), \"ReportFile\" BLOB, "
                    + "\"ReportVersion\" INT, \"CompanyId\" BIGINT)");
            boolean dummy;
            try (java.sql.ResultSet rs = c.getMetaData().getTables(null, "PUBLIC", "DUMMY", null)) {
                dummy = rs.next();
            }
            if (!dummy) {
                s.execute("CREATE TABLE PUBLIC.DUMMY(SYSUUID UUID GENERATED ALWAYS AS (RANDOM_UUID()))");
                s.execute("INSERT INTO PUBLIC.DUMMY DEFAULT VALUES");
            }
        }
    }

    /** Stores the template of the case under Id = case name (idempotent). */
    public static void putTemplate(DataSource ds, String id, Path rpt) throws Exception {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement d = c.prepareStatement("DELETE FROM BSCL.\"BSCL.Report::Template\" WHERE \"Id\" = ? "
                    + "OR \"MasterReportId\" = ?")) {
                d.setString(1, id);
                d.setString(2, id);
                d.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO BSCL.\"BSCL.Report::Template\" VALUES (?,?,?,?,?,?)")) {
                p.setString(1, id);
                p.setString(2, rpt.getFileName().toString());
                p.setString(3, null);
                p.setBytes(4, Files.readAllBytes(rpt));
                p.setInt(5, 1);
                p.setLong(6, 1L);
                p.executeUpdate();
            }
        }
    }
}
