import cl.bcs.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.*;

/**
 * Verify-3 fresh inputs for BcsUtils. One source, compiled once against the legacy classes and once against the
 * modernized ones; same flags (-Duser.timezone=America/Santiago -Duser.language=es -Duser.country=CL).
 * Output per input: <out>/<id>/out.txt. Exception messages are never printed, only the class.
 */
public class Fresh {
    static Path out;
    static StringBuilder sb;

    static void begin(String id) { sb = new StringBuilder(); }
    static void end(String id) throws IOException {
        Path d = out.resolve(id); Files.createDirectories(d);
        Files.write(d.resolve("out.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
    static void line(String label, Callable<Object> c) {
        String r;
        try { r = String.valueOf(c.call()); } catch (Throwable t) { r = "THROWS " + t.getClass().getName(); }
        sb.append(label).append(" => ").append(r.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")).append('\n');
    }
    static String ts(java.util.Date d) { return d == null ? "null" : d.getTime() + "|" + new Timestamp(d.getTime()); }

    public static class P { public String codigo; public BigDecimal monto; public java.util.Date fecha; public List<String> tags; public Integer n; public Long l; public Double d; }
    public static class Q { public String codigo; public BigDecimal monto; public java.util.Date fecha; public List<String> tags; public Integer n; public Long l; public Double d; public String extra; }

    public static void main(String[] a) throws Exception {
        out = Paths.get(a[0]);
        Path tmp = Files.createTempDirectory("fresh");

        begin("F01"); // RUT: boundaries and odd shapes
        for (String s : new String[]{"1-9", "6-k", "6-K", "11.111.111-1", "12.345.678-5", "7.654.321-6", " 7.654.321-6 ", "76.543.210-K", "9-9", "100-0", "20.000.000-K", "99.999.999-9",
                "1.2.3-4", "12345678-5-", "-5", "5-", "ab-1", "1 2-3", "1\t-9", "٣-5", "0-0", "00000000-0", "123456789012-3", "7.654.321-6\n", "2-7", "3-5", "4-3", "5-1"})
            { final String v = s; line("validar(" + v + ")", () -> RutUtil.validar(v)); }
        line("validar(null)", () -> RutUtil.validar(null));
        end("F01");

        begin("F02"); // StringUtils
        line("join1", () -> StringUtils.join("; ", Arrays.asList("a", null, "", " b")));
        line("join2", () -> StringUtils.join("", Arrays.asList("x")));
        line("join3", () -> StringUtils.join("--", new ArrayList<String>()));
        line("join4", () -> StringUtils.join(null, Arrays.asList("p", "q")));
        line("join5", () -> StringUtils.join(",", null));
        for (String s : new String[]{null, "", " ", "\t", "\u00a0", "\u2003", "\n", "  x ", "0"})
            { final String v = s; line("isNullOrBlank(" + v + ")", () -> StringUtils.isNullOrBlank(v)); }
        end("F02");

        begin("F03"); // e-mail
        for (String s : new String[]{"a@b.cl", "ab@c.cl", "abc@d.cl", "juan.perez@bcs.cl", "j.p+x@d-e.co.uk", "x@", "@x.cl", "user@@d.cl", "u@d.c", "u@d.cl.", "ñu@d.cl", "u_v-w@d1.cl", "", " a@b.cl", "a@b.cl "})
            { final String v = s; line("obfuscate(" + v + ")", () -> Utils.obfuscateEmail(v)); line("valid(" + v + ")", () -> Utils.emailValidate(v)); }
        line("obfuscate(null)", () -> Utils.obfuscateEmail(null));
        line("valid(null)", () -> Utils.emailValidate(null));
        end("F03");

        begin("F04"); // eventos
        StringBuilder big = new StringBuilder(); for (int i = 0; i < 90; i++) big.append("ñ-ev/").append(i);
        for (String s : new String[]{"", "A", "evento ñandú/2026?", big.toString(), "\u00e9\u00e8", "a+b/c=d"})
            { final String v = s; line("cod(" + (v.length() > 20 ? v.substring(0, 20) + "…" + v.length() : v) + ")", () -> Utils.codificarEvento(v));
              line("roundtrip", () -> Utils.decodificarEvento(Utils.codificarEvento(v)).equals(v)); }
        for (String s : new String[]{"!!!!", "YQ", "YQ==", "YWJj\nZGVm", "YWJj ZGVm", "YW-_"}) { final String v = s; line("dec(" + v + ")", () -> Utils.decodificarEvento(v)); }
        line("appendKeys1", () -> Utils.appendKeys("a", "b", null, "", "c"));
        line("appendKeys2", () -> Utils.appendKeys());
        line("appendKeys3", () -> Utils.appendKeys("solo"));
        end("F04");

        begin("F05"); // printJson / gson builder
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", null); m.put("b", Arrays.asList(1, 2.5, "x\"y", 'c')); m.put("c", new BigDecimal("10.500")); m.put("d", new java.util.Date(86400000L * 20000)); m.put("e", new Timestamp(86400000L * 20000 + 123));
        m.put("f", Collections.emptyMap()); m.put("g", new int[]{3, 1}); m.put("h", "ñ<>&'");
        line("pretty", () -> Utils.printJson(m, true));
        line("compact", () -> Utils.printJson(m, false));
        line("null", () -> Utils.printJson(null, false));
        line("gson-date", () -> Utils.getGsonBuilder().toJson(new java.util.Date(1700000000000L)));
        line("gson-ts", () -> Utils.getGsonBuilder().toJson(new Timestamp(1700000000123L)));
        line("gson-sqldate", () -> Utils.getGsonBuilder().toJson(new java.sql.Date(1700000000000L)));
        line("gson-parse", () -> ts(Utils.getGsonBuilder().fromJson("\"2025-09-07 00:30:00.000\"", java.util.Date.class)));
        line("gson-parse-bad", () -> ts(Utils.getGsonBuilder().fromJson("\"07/09/2025\"", java.util.Date.class)));
        end("F05");

        begin("F06"); // diferenciaEnDias across DST in America/Santiago
        int[][] pairs = {{2025, 4, 6, 2025, 4, 5}, {2025, 4, 7, 2025, 4, 5}, {2025, 9, 8, 2025, 9, 6}, {2025, 9, 7, 2025, 9, 6}, {2025, 9, 9, 2025, 9, 6},
                {2026, 4, 5, 2026, 4, 4}, {2026, 9, 7, 2026, 9, 5}, {2025, 1, 1, 2025, 1, 1}, {2025, 1, 1, 2025, 1, 2}, {2024, 3, 1, 2024, 2, 28}};
        for (int[] p : pairs) { final int[] q = p;
            line("dif(" + Arrays.toString(q) + ")", () -> Utils.diferenciaEnDias(DateUtil.getDate(q[0], q[1] - 1, q[2]), DateUtil.getDate(q[3], q[4] - 1, q[5]))); }
        line("dif(null)", () -> Utils.diferenciaEnDias(null, new java.util.Date()));
        end("F06");

        begin("F07"); // DateUtil
        for (String s : new String[]{"2025-02-29", "2024-02-29", "2025-04-06", "2025-9-7", "07-09-2025", "2025-09-07 00:30", "2025-09-07 00:30:00", "2025-09-07 01:30:00", "2025-04-05 23:59:59", "2025-04-05T23:59:59", "20250907", "", " 2025-04-06", "2025-13-01", "2025-00-10", "2025-04-31", "2025-04-06 24:00:00"})
            { final String v = s; line("getDate(" + v + ")", () -> ts(DateUtil.getDate(v))); }
        line("anoMes(2025-12)", () -> ts(DateUtil.getAnoMes("2025-12")));
        line("anoMes(2025-02)", () -> ts(DateUtil.getAnoMes("2025-02")));
        line("anoMes(null)", () -> ts(DateUtil.getAnoMes(null)));
        line("getDate(2025,12,31)", () -> ts(DateUtil.getDate(2025, 12, 31)));
        line("getDate(2025,-1,0)", () -> ts(DateUtil.getDate(2025, -1, 0)));
        java.util.Date base = new java.util.Date(DateUtil.getDate(2025, 3, 5).getTime() + 12 * 3600000L);
        for (int d : new int[]{-1, 0, 1, 2, 30, 365, -400}) { final int dd = d; line("addDays(" + dd + ")", () -> ts(DateUtil.addDays(base, dd))); }
        line("startOfDay", () -> ts(DateUtil.getStartOfDay(base)));
        line("endOfDay", () -> ts(DateUtil.getEndOfDay(base)));
        line("startOfMonth", () -> ts(DateUtil.getStartOfMonth(new java.util.Date(DateUtil.getDate(2025, 8, 30).getTime() + 86399000L))));
        line("day/month/year", () -> DateUtil.getDay(base) + "/" + DateUtil.getMonth(base) + "/" + DateUtil.getYear(base));
        line("calendar", () -> DateUtil.getCalendar(base).getTimeInMillis());
        end("F07");

        begin("F08"); // Fecha strings and DST
        for (String s : new String[]{"2025-09-07 00:30:00", "2025-09-07 01:00:00", "2025-04-05 23:30:00", "2025-04-06 00:30:00", "2025-04-06 01:30:00", "2025-02-30 10:00:00", "2025-12-31 23:59:59", "garbage"})
            { final String v = s; line("toDateTime(" + v + ")", () -> ts(Fecha.toDateTime(v))); line("toTimeStamp(" + v + ")", () -> ts(Fecha.toTimeStamp(v))); }
        for (String s : new String[]{"2025-09-07", "2025-02-29", "2024-02-29", "2025-9-7", "2025-09-07 10:00:00", "", "x"}) { final String v = s; line("toDate(" + v + ")", () -> ts(Fecha.toDate(v))); }
        for (String s : new String[]{"00:00:00", "23:59:59", "24:00:00", "7:5:9", "12:30"}) { final String v = s; line("toTime(" + v + ")", () -> Fecha.toTime(v).getTime()); }
        end("F08");

        begin("F09"); // Fecha arithmetic and formatting with offsets
        Timestamp t0 = Timestamp.valueOf("2025-04-05 22:30:00");
        line("addSeconds", () -> ts(Fecha.addSeconds(t0, 7200)));
        line("substractSeconds", () -> ts(Fecha.substractSeconds(t0, -86400)));
        line("addHours", () -> ts(Fecha.addHours(t0, 3)));
        line("addMillis", () -> ts(Fecha.addMilliseconds(t0, 999)));
        line("addDays-ts", () -> ts(Fecha.addDays(t0, 1)));
        line("substractDays-ts", () -> ts(Fecha.substractDays(t0, 1)));
        line("addDays-date", () -> ts(Fecha.addDays(java.sql.Date.valueOf("2025-09-06"), 1)));
        line("substractDays-date", () -> ts(Fecha.substractDays(java.sql.Date.valueOf("2025-09-08"), 1)));
        line("toStringTimeStamp", () -> Fecha.toStringTimeStamp(t0));
        line("toStringDatetime", () -> Fecha.toStringDatetime(t0));
        line("toStringDate-ts", () -> Fecha.toStringDate(t0));
        line("toStringDate-null", () -> Fecha.toStringDate((java.sql.Date) null, "1900-01-01"));
        line("toStringTime-ts", () -> Fecha.toStringTime(t0));
        line("toFormat", () -> Fecha.toFormat(t0, "EEEE d 'de' MMMM yyyy HH:mm"));
        line("toFormat-bad", () -> Fecha.toFormat(t0, "qqqq"));
        line("stringToSqlDate", () -> Fecha.stringToSqlDate(new java.util.Date(86400000L * 19000)));
        line("utilDateToSqlDate", () -> Fecha.utilDateToSqlDate(new java.util.Date(86400000L * 19000 + 5)));
        line("default-offset", () -> Fecha.getLocalOffSet());
        for (int off : new int[]{-3, -4, 0, 5, 14}) { final int o = off;
            line("offset " + o, () -> { Fecha.setLocalOffSet(o); return ts(Fecha.toLocalTimeStamp(t0)) + " ; " + ts(Fecha.toUTCTimeStamp(t0)) + " ; " + Fecha.toLocalFormat(t0, "yyyy-MM-dd HH:mm:ss") + " ; " + Fecha.toLocalTime(new java.sql.Time(3600000L * 5)).getTime(); }); }
        line("fechaSistemaHoraLocal", () -> ts(Fecha.fechaSistemaHoraLocal_fechaHoraUTC("2025-04-05", "23:30:00")));
        end("F09");

        begin("F10"); // copyPojo
        P src = new P(); src.codigo = "Z9"; src.monto = new BigDecimal("0.000"); src.fecha = new java.util.Date(1700000000000L); src.tags = new ArrayList<>(Arrays.asList("u", "v")); src.n = 7; src.l = Long.MAX_VALUE; src.d = -0.0;
        Q dst = new Q(); dst.extra = "keep";
        line("copy", () -> { Utils.copyPojo(src, dst); return Utils.printJson(dst, false) + " sameTags=" + (dst.tags == src.tags) + " sameBD=" + (dst.monto == src.monto) + " sameDate=" + (dst.fecha == src.fecha) + " sameStr=" + (dst.codigo == src.codigo); });
        Q dst2 = new Q();
        line("copy-restrictDate", () -> { Utils.copyPojo(src, dst2, true); return Utils.printJson(dst2, false) + " sameDate=" + (dst2.fecha == src.fecha); });
        P empty = new P(); Q dst3 = new Q(); dst3.codigo = "old"; dst3.n = 3;
        line("copy-nulls", () -> { Utils.copyPojo(empty, dst3); return Utils.printJson(dst3, false); });
        line("copy-nullsrc", () -> Utils.copyPojo(null, new Q()));
        line("copy-nulltarget", () -> Utils.copyPojo(src, (Q) null));
        end("F10");

        begin("F11"); // List2Hash / getColumn / List2ListHashGenericObject
        List<P> ps = new ArrayList<>();
        for (String c : new String[]{"B", "A", "B", null, "C"}) { P p = new P(); p.codigo = c; p.n = ps.size(); p.monto = new BigDecimal(ps.size() + ".50"); ps.add(p); }
        line("List2Hash", () -> new TreeMap<String, Object>(String.valueOf("") == null ? null : toStr(Utils.<String, P>List2Hash(ps, "codigo"))));
        line("List2Hash-badfield", () -> Utils.<String, P>List2Hash(ps, "nope"));
        line("List2Hash-empty", () -> Utils.<String, P>List2Hash(new ArrayList<P>(), "codigo"));
        line("getColumn-dup", () -> Utils.getColumn(ps, "codigo", true));
        line("getColumn-nodup", () -> Utils.getColumn(ps, "codigo", false));
        line("getColumn-n", () -> Utils.getColumn(ps, "n", true));
        line("getColumn-badfield", () -> Utils.getColumn(ps, "nope", true));
        line("L2LH", () -> { List<HashMap<String, Object>> r = Utils.List2ListHashGenericObject(ps.subList(0, 2)); List<String> o = new ArrayList<>(); for (HashMap<String, Object> h : r) o.add(new TreeMap<String, Object>(h).toString()); return o; });
        line("L2LH-empty", () -> Utils.List2ListHashGenericObject(new ArrayList<P>()));
        end("F11");

        begin("F12"); // files and zip
        Path f1 = tmp.resolve("a.bin"); Files.write(f1, new byte[]{1, 2, 3, (byte) 255});
        Path f2 = tmp.resolve("vacio.txt"); Files.write(f2, new byte[0]);
        Path f3 = tmp.resolve("grande.dat"); byte[] bg = new byte[100000]; for (int i = 0; i < bg.length; i++) bg[i] = (byte) (i * 31 + i / 7); Files.write(f3, bg);
        Path f4 = tmp.resolve("rar.x"); Files.write(f4, "Rar!\u001a\u0007\u0000rest\nline2".getBytes(StandardCharsets.UTF_8));
        Path f5 = tmp.resolve("rar2.x"); Files.write(f5, "rar!no".getBytes(StandardCharsets.UTF_8));
        Path f6 = tmp.resolve("rar3.x"); Files.write(f6, "\nRar!second".getBytes(StandardCharsets.UTF_8));
        Path f7 = tmp.resolve("rar4.x"); Files.write(f7, new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'R', 'a', 'r', '!'});
        for (Path p : new Path[]{f1, f2, f3}) { final Path pp = p; line("b64(" + pp.getFileName() + ")", () -> FileUtils.encodeBase64String(pp.toFile())); }
        line("b64(missing)", () -> FileUtils.encodeBase64String(tmp.resolve("no.such").toFile()));
        for (Path p : new Path[]{f4, f5, f6, f7, f2}) { final Path pp = p; line("isRar(" + pp.getFileName() + ")", () -> ZipUtil.isRar(pp.toString())); }
        line("isRar(missing)", () -> ZipUtil.isRar(tmp.resolve("no.such").toString()));
        line("createFolder", () -> { File d = FileUtils.createFolder(tmp.toFile(), "sub nuevo"); return d.getName() + " " + d.isDirectory(); });
        line("createFolder-again", () -> { File d = FileUtils.createFolder(tmp.toFile(), "sub nuevo"); return d.getName() + " " + d.isDirectory(); });
        line("createFolder-nested", () -> { File d = FileUtils.createFolder(tmp.toFile(), "n1/n2"); return d.getName() + " " + d.isDirectory(); });
        line("fileToZip", () -> { Path z = tmp.resolve("one.zip"); Utils.fileToZipFile(f1.toString(), z.toString(), false); return zipDump(z) + " srcExists=" + Files.exists(f1); });
        line("fileToZip-delete", () -> { Path src2 = tmp.resolve("del.txt"); Files.write(src2, "hola".getBytes(StandardCharsets.UTF_8)); Path z = tmp.resolve("two.zip"); Utils.fileToZipFile(src2.toString(), z.toString(), true); return zipDump(z) + " srcExists=" + Files.exists(src2); });
        line("fileToZip-missing", () -> { Path z = tmp.resolve("three.zip"); try { Utils.fileToZipFile(tmp.resolve("zz").toString(), z.toString(), false); return "no throw"; } catch (IOException e) { return "IOException zipExists=" + Files.exists(z) + " size=" + Files.size(z); } });
        line("generateMultipleZip", () -> { Path z = tmp.resolve("multi.zip"); boolean r = ZipUtil.generateMultipleZip(Arrays.asList(f1.toString(), f2.toString(), f3.toString()), z.toString()); return r + " " + zipDump(z); });
        line("generateMultipleZip-empty", () -> { Path z = tmp.resolve("multi0.zip"); boolean r = ZipUtil.generateMultipleZip(new ArrayList<String>(), z.toString()); return r + " " + zipDump(z); });
        line("touch", () -> { File t = tmp.resolve("touch.txt").toFile(); Utils.touch(t, 1700000000000L); return t.exists() + " " + t.lastModified() + " " + t.length(); });
        line("touch-existing", () -> { Utils.touch(f1.toFile(), 1600000000000L); return f1.toFile().lastModified() + " " + Files.size(f1); });
        end("F12");

        begin("F13"); // gson deserializers
        Gson g = new GsonBuilder().registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer()).registerTypeAdapter(Timestamp.class, new GsonTimestampDeserializer()).create();
        for (String s : new String[]{"\"2025-09-07\"", "\"2025-09-07 10:00:00\"", "\"2025-09-07T10:00:00\"", "\"07-09-2025\"", "\"\"", "null", "12345", "\"2025-02-29\"", "\"1700000000000\"", "true", "\"2025-09-07 00:30:00.000\"", "\"2025-04-06 23:30:00.5\"", "[]", "{}"})
            { final String v = s; line("date(" + v + ")", () -> { java.sql.Date d = g.fromJson(v, java.sql.Date.class); return d == null ? "null" : d.getTime(); });
              line("ts(" + v + ")", () -> { Timestamp d = g.fromJson(v, Timestamp.class); return d == null ? "null" : d.getTime() + "/" + d.getNanos(); }); }
        end("F13");

        begin("F14"); // JavaUtil / JavaCall contract pieces that do not need a database
        line("jc-class", () -> JavaCall.class.isInterface() + " " + JavaCallExtra.class.isInterface() + " " + JavaUtil.class.isInterface());
        end("F14");
    }

    static Map<String, Object> toStr(HashMap<String, Object> h) {
        Map<String, Object> r = new TreeMap<>();
        for (Map.Entry<String, Object> e : h.entrySet()) r.put(String.valueOf(e.getKey()), ((P) e.getValue()).n + "/" + ((P) e.getValue()).monto);
        return r;
    }

    static String zipDump(Path z) throws Exception {
        List<String> r = new ArrayList<>();
        try (ZipFile zf = new ZipFile(z.toFile())) {
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                byte[] b = new byte[0];
                try (InputStream in = zf.getInputStream(e)) { b = in.readAllBytes(); }
                r.add(e.getName() + ":" + e.isDirectory() + ":" + b.length + ":" + hex(MessageDigest.getInstance("SHA-256").digest(b)).substring(0, 16) + ":" + e.getMethod());
            }
        }
        return r.toString();
    }
    static String hex(byte[] b) { StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x)); return s.toString(); }
}
