package cl.bcs.reports.report;

import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.equiv.H2Shim;
import cl.bcs.reports.support.CrystalTestEnvironment;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** H4: no temp file (*.rpt / *.png) survives a failed upload, a failed image, or a close(); stale ones are swept. */
class TempFilesLeakTest {

    @TempDir Path tmp;
    private JdbcDataSource ds;
    private String previous;

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @BeforeEach
    void setUp() throws Exception {
        previous = System.getProperty("bcs.reports.tmpdir");
        System.setProperty("bcs.reports.tmpdir", tmp.toString());
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:leak;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        H2Shim.init(ds);
    }

    @AfterEach
    void tearDown() {
        if (previous == null) {
            System.clearProperty("bcs.reports.tmpdir");
        } else {
            System.setProperty("bcs.reports.tmpdir", previous);
        }
    }

    private String[] files() {
        String[] n = tmp.toFile().list();
        return n == null ? new String[0] : n;
    }

    @Test
    void tmpDirIsAbsoluteAndConfiguredInOnePlace() {
        assertTrue(TempFiles.dir().isAbsolute());
        assertEquals(tmp.toFile().getAbsoluteFile(), TempFiles.dir());
    }

    @Test
    void garbageUploadLeavesNoFile() throws Exception {
        ReportDocument d = new ReportDocument("BSCL", ds);
        try {
            String id = d.uploadDocument("garbage", new ByteArrayInputStream("this is not a report".getBytes(StandardCharsets.UTF_8)), 1, 1L);
            assertEquals("", id);
        } finally {
            d.close();
        }
        assertEquals(0, files().length, "leftover: " + String.join(",", files()));
    }

    @Test
    void closeWithoutOpenAndTwiceIsHarmless() throws Exception {
        ReportDocument d = new ReportDocument("BSCL", ds);
        d.close();
        d.close();
        assertEquals(0, files().length);
    }

    @Test
    void readDocumentOfAGarbageBlobLeavesNoFile() throws Exception {
        try (java.sql.Connection c = ds.getConnection();
             java.sql.PreparedStatement p = c.prepareStatement("INSERT INTO BSCL.\"BSCL.Report::Template\" VALUES (?,?,?,?,?,?)")) {
            p.setString(1, "BAD");
            p.setString(2, "bad.rpt");
            p.setString(3, null);
            p.setBytes(4, "not a report".getBytes(StandardCharsets.UTF_8));
            p.setInt(5, 1);
            p.setLong(6, 1L);
            p.executeUpdate();
        }
        ReportDocument d = new ReportDocument("BSCL", ds);
        try {
            assertFalse(d.readDocument("BAD"));
        } finally {
            d.close();
        }
        assertEquals(0, files().length, "leftover: " + String.join(",", files()));
    }

    @Test
    void badBase64ImageLeavesNoFileAndGoodDocumentIsClosedClean() throws Exception {
        EquivCase c = new EquivCase(Paths.get("src/test/resources/crystal/equiv", "APV_CONTRATO_SAAPV"));
        H2Shim.putTemplate(ds, c.name, Paths.get(c.template));
        ReportDocument d = new ReportDocument("BSCL", ds);
        try {
            assertTrue(d.readDocument(c.name));
            assertEquals(1, files().length, "the opened template is the only temp file");
            assertThrows(IllegalArgumentException.class, () -> d.setDocumentImage("%%%not-base64%%%", "Picture1"));
            // valid base64 that is not an image: ImageIO.read returns null, nothing is written
            d.setDocumentImage(Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8)), "Picture1");
            // valid image: png written, imported and deleted
            d.setDocumentImage(c.params.stream().filter(p -> "Picture1".equals(p.get("name"))).findFirst().get().get("data"), "Picture1");
            // barcode with an unknown picture name: the failure path must not leave the png either
            d.setDocumentBarcodeImage("pdf417", "12345", "NoSuchPicture");
            d.setDocumentBarcodeImage("pdf417", "12345", "Picture1");
            for (String n : files()) {
                assertTrue(n.endsWith(".rpt"), "unexpected temp file " + n);
            }
        } finally {
            d.close();
        }
        assertEquals(0, files().length, "leftover: " + String.join(",", files()));
    }

    @Test
    void sweepRemovesOnlyStaleRptAndPngFiles() throws Exception {
        File oldRpt = Files.createFile(tmp.resolve("old.rpt")).toFile();
        File oldPng = Files.createFile(tmp.resolve("old.png")).toFile();
        File oldOther = Files.createFile(tmp.resolve("old.txt")).toFile();
        File fresh = Files.createFile(tmp.resolve("fresh.rpt")).toFile();
        long twoHoursAgo = System.currentTimeMillis() - 2 * 3600_000L;
        assertTrue(oldRpt.setLastModified(twoHoursAgo));
        assertTrue(oldPng.setLastModified(twoHoursAgo));
        assertTrue(oldOther.setLastModified(twoHoursAgo));
        assertEquals(2, TempFiles.sweep(3600_000L));
        assertFalse(oldRpt.exists());
        assertFalse(oldPng.exists());
        assertTrue(oldOther.exists());
        assertTrue(fresh.exists());
    }
}
