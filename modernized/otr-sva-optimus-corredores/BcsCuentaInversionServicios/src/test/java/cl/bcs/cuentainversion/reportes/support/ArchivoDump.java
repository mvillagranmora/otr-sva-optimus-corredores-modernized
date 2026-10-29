package cl.bcs.cuentainversion.reportes.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Serialises the files a class generated so they can be recorded (legacy oracle) and compared (modern implementation).
 * It lives under src/test-pending because it needs Apache POI, which BcsCuentaInversionServicios does not yet declare;
 * the oracle runner compiles it with POI on the classpath.
 *
 * <ul>
 *   <li>File names pass through {@link Mask#texto(String)} (files are stamped with the current time).</li>
 *   <li>Text files (csv, txt, log, json, xml): content decoded as UTF-8 and masked with {@link Mask#texto(String)}.</li>
 *   <li>xls / xlsx: one entry per sheet: name and rows as arrays of cell texts (DataFormatter text); trailing empty cells are dropped, empty rows are kept as [].</li>
 *   <li>zip: entry names, text entries decoded like text files.</li>
 *   <li>pdf: only {@code "pdf"} and whether it starts with %PDF (PDF producers stamp dates and ids).</li>
 * </ul>
 */
public final class ArchivoDump {

    private ArchivoDump() {
    }

    public static JsonObject directorio(Path dir) throws Exception {
        JsonObject out = new JsonObject();
        if (!Files.exists(dir)) {
            return out;
        }
        List<Path> files;
        try (Stream<Path> s = Files.walk(dir)) {
            files = s.filter(Files::isRegularFile).sorted().toList();
        }
        for (Path f : files) {
            String rel = dir.relativize(f).toString().replace('\\', '/');
            out.add(Mask.texto(rel), archivo(rel, Files.readAllBytes(f)));
        }
        return out;
    }

    static JsonObject archivo(String nombre, byte[] bytes) throws Exception {
        String n = nombre.toLowerCase();
        JsonObject o = new JsonObject();
        if (n.endsWith(".xlsx") || n.endsWith(".xls")) {
            o.addProperty("tipo", "excel");
            o.add("hojas", excel(bytes));
        } else if (n.endsWith(".pdf")) {
            o.addProperty("tipo", "pdf");
            o.addProperty("empiezaConPdf", bytes.length > 4 && new String(bytes, 0, 4, StandardCharsets.ISO_8859_1).equals("%PDF"));
        } else if (n.endsWith(".zip")) {
            o.addProperty("tipo", "zip");
            JsonObject entries = new JsonObject();
            try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                ZipEntry e;
                while ((e = z.getNextEntry()) != null) {
                    entries.add(Mask.texto(e.getName()), archivo(e.getName(), z.readAllBytes()));
                }
            }
            o.add("entradas", entries);
        } else {
            o.addProperty("tipo", "texto");
            o.addProperty("contenido", Mask.texto(new String(bytes, StandardCharsets.UTF_8)));
        }
        return o;
    }

    static JsonArray excel(byte[] bytes) throws Exception {
        JsonArray hojas = new JsonArray();
        DataFormatter fmt = new DataFormatter(java.util.Locale.US);
        try (InputStream in = new ByteArrayInputStream(bytes); Workbook wb = WorkbookFactory.create(in)) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sh = wb.getSheetAt(i);
                JsonObject h = new JsonObject();
                h.addProperty("nombre", sh.getSheetName());
                JsonArray filas = new JsonArray();
                for (int r = 0; r <= sh.getLastRowNum(); r++) {
                    Row row = sh.getRow(r);
                    JsonArray celdas = new JsonArray();
                    if (row != null) {
                        int last = row.getLastCellNum();
                        int keep = 0;
                        String[] vals = new String[Math.max(last, 0)];
                        for (int c = 0; c < last; c++) {
                            Cell cell = row.getCell(c);
                            String v = "";
                            if (cell != null) {
                                v = fmt.formatCellValue(cell);
                            }
                            vals[c] = Mask.texto(v);
                            if (!v.isEmpty()) {
                                keep = c + 1;
                            }
                        }
                        for (int c = 0; c < keep; c++) {
                            celdas.add(vals[c]);
                        }
                    }
                    filas.add(celdas);
                }
                h.add("filas", filas);
                hojas.add(h);
            }
        }
        return hojas;
    }
}
