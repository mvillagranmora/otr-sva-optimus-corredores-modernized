import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY export of Crystal templates from the HANA table "BSCL"."BSCL.Report::Template".
 * Only one fixed SELECT is ever prepared (see SQL below); the connection is set read-only; there is no INSERT/UPDATE/DELETE/DDL here.
 * Credentials are read from the local .env (keys qa01.saphana.*) and are never printed or written.
 * usage: java -cp ngdbc.jar ExportTemplates.java <envFile> <idsCsv> <outDir>
 */
public class ExportTemplates {
    static final String SQL = "SELECT \"ReportName\", \"ReportVersion\", \"CompanyId\", \"ReportFile\" FROM \"BSCL\".\"BSCL.Report::Template\" WHERE \"Id\" = ?";

    public static void main(String[] a) throws Exception {
        Map<String, String> env = new HashMap<>();
        for (String l : Files.readAllLines(Paths.get(a[0]), StandardCharsets.UTF_8)) {
            int i = l.indexOf('=');
            if (i > 0 && !l.startsWith("#")) env.put(l.substring(0, i).trim(), l.substring(i + 1).trim());
        }
        Properties p = new Properties();
        p.setProperty("user", env.get("qa01.saphana.dbuser"));
        p.setProperty("password", env.get("qa01.saphana.dbpass"));
        String extra = env.getOrDefault("qa01.saphana.dbprop", "");
        for (String kv : extra.split(";")) {
            int i = kv.indexOf('=');
            if (i > 0) p.setProperty(kv.substring(0, i).trim(), kv.substring(i + 1).trim());
        }
        Class.forName(env.getOrDefault("qa01.saphana.dbdrv", "com.sap.db.jdbc.Driver"));
        List<String[]> ids = new ArrayList<>();
        for (String l : Files.readAllLines(Paths.get(a[1]), StandardCharsets.UTF_8)) {
            String[] c = l.split(",", -1);
            if (c.length >= 5 && !c[0].equals("Id")) ids.add(c);
        }
        Path out = Paths.get(a[2]);
        Files.createDirectories(out);
        StringBuilder man = new StringBuilder("[\n");
        int ok = 0, bad = 0;
        try (Connection cn = DriverManager.getConnection(env.get("qa01.saphana.dbURL"), p)) {
            cn.setReadOnly(true);
            cn.setAutoCommit(false);
            try (Statement s = cn.createStatement(); ResultSet r = s.executeQuery("SELECT CURRENT_USER, CURRENT_SCHEMA FROM DUMMY")) {
                r.next();
                System.out.println("connected read-only; schema=" + r.getString(2));
            }
            try (PreparedStatement ps = cn.prepareStatement(SQL)) {
                for (String[] c : ids) {
                    ps.setString(1, c[0]);
                    try (ResultSet r = ps.executeQuery()) {
                        if (!r.next()) { System.out.println("MISSING " + c[0]); bad++; continue; }
                        byte[] b = r.getBytes(4);
                        String name = r.getString(1), ver = r.getString(2);
                        long expected = Long.parseLong(c[4]);
                        String fn = (name.replaceAll("(?i)\\.rpt$", "").replaceAll("[^A-Za-z0-9_.-]", "_")) + "_v" + ver + "_" + c[0].substring(0, 8) + ".rpt";
                        Files.write(out.resolve(fn), b);
                        String sha = hex(MessageDigest.getInstance("SHA-256").digest(b));
                        boolean same = b.length == expected;
                        if (same) ok++; else bad++;
                        System.out.println((same ? "OK  " : "SIZE-MISMATCH ") + fn + " " + b.length + " expected " + expected);
                        man.append(" {\"id\":\"").append(c[0]).append("\",\"reportName\":\"").append(name.replace("\"", "'"))
                           .append("\",\"reportVersion\":\"").append(ver).append("\",\"companyId\":\"").append(String.valueOf(r.getString(3)))
                           .append("\",\"file\":\"").append(fn).append("\",\"bytes\":").append(b.length).append(",\"sha256\":\"").append(sha)
                           .append("\",\"sizeMatchesListing\":").append(same).append("},\n");
                    }
                }
            }
            cn.rollback();
        }
        man.append("]\n");
        Files.write(out.resolve("MANIFEST.hana-export.json"), man.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("exported ok=" + ok + " problems=" + bad + " of " + ids.size());
    }

    static String hex(byte[] d) {
        StringBuilder sb = new StringBuilder();
        for (byte x : d) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
