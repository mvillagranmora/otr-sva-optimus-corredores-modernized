package cl.bcs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The PURE subset of the legacy {@code Utils.java} (scope A, 2026-10-16). Data access, servlet and platform helpers of
 * the legacy class are not ported.
 *
 * <ul>
 *   <li>UTIL-12 {@link #obfuscateEmail} (Utils.java:725-742), UTIL-13 {@link #emailValidate} (:923-934),
 *       UTIL-14 {@link #codificarEvento}/{@link #decodificarEvento} (:1366-1377), UTIL-15 {@link #appendKeys}
 *       (:1405-1412), UTIL-16 {@link #generateToken}/{@link #generateAlphanumericToken} (:576-603).</li>
 *   <li>UTIL-20 {@link #printJson} (:750-758), UTIL-22 {@link #getGsonBuilder} (:1018-1025).</li>
 *   <li>UTIL-24 {@link #List2Hash} (:768-783), UTIL-25 {@link #getColumn} (:792-813), UTIL-26
 *       {@link #List2ListHashGenericObject} (:1385-1398), UTIL-27 {@link #copyPojo} (:827-915).</li>
 *   <li>UTIL-33 {@link #diferenciaEnDias} (:1379-1383), UTIL-52 {@link #touch} (:1088-1093), UTIL-55
 *       {@link #fileToZipFile} (:1040-1056).</li>
 * </ul>
 * The legacy {@code throws Exception} signatures and {@code java.lang.Exception} failures of the reflection helpers are
 * kept, because 300+ legacy call sites are written against them.
 */
public final class Utils {

    private static final String ALFABETO_RECUPERACION = "BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz1234567890!#$%&=?@";
    private static final String ALFABETO_ALFANUMERICO = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Pattern EMAIL = Pattern.compile(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    private static final String FORMATO_FECHA_JSON = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final long MS_POR_DIA = 86_400_000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Utils() {
    }

    // ------------------------------------------------------------------ strings

    /** UTIL-16: password-recovery token: consonants, digits and {@code !#$%&=?@}; {@code size <= 0} gives "". */
    public static String generateToken(int size) {
        return token(size, ALFABETO_RECUPERACION);
    }

    /** UTIL-16: single-use alphanumeric token ({@code a-z0-9}). */
    public static String generateAlphanumericToken(int size) {
        return token(size, ALFABETO_ALFANUMERICO);
    }

    private static String token(int size, String alfabeto) {
        StringBuilder sb = new StringBuilder(Math.max(size, 0));
        for (int i = 0; i < size; i++) {
            sb.append(alfabeto.charAt(RANDOM.nextInt(alfabeto.length())));
        }
        return sb.toString();
    }

    /**
     * UTIL-12: keeps the first char, the last {@code @}, the char after it, the last {@code .} and the char after it;
     * the rest becomes {@code *}. {@code null} gives "". Legacy defect kept: it works on UTF-16 units, so a surrogate
     * pair can be split.
     */
    public static String obfuscateEmail(String email) {
        if (email == null) {
            return "";
        }
        if (email.length() <= 1) {
            return email;
        }
        int arroba = email.lastIndexOf('@');
        int punto = email.lastIndexOf('.');
        StringBuilder sb = new StringBuilder(email.length());
        for (int i = 0; i < email.length(); i++) {
            boolean visible = i == 0 || i == arroba || i == arroba + 1 || i == punto || i == punto + 1;
            sb.append(visible ? email.charAt(i) : '*');
        }
        return sb.toString();
    }

    /** UTIL-13: ASCII e-mail with a 2+ letter TLD; {@code null} throws NPE. */
    public static boolean emailValidate(String email) {
        return EMAIL.matcher(email).matches();
    }

    /** UTIL-14: left-pads to 10 with spaces ({@code null} is the text "null"), UTF-8, standard Base64. */
    public static String codificarEvento(String idEvento) {
        byte[] relleno = String.format("%1$10s", idEvento).getBytes(StandardCharsets.UTF_8);
        return java.util.Base64.getEncoder().encodeToString(relleno);
    }

    /** UTIL-14: LENIENT Base64 decode (commons-codec: invalid characters are skipped), UTF-8, trimmed. */
    public static String decodificarEvento(String codificado) {
        return new String(Base64.decodeBase64(codificado), StandardCharsets.UTF_8).trim();
    }

    /** UTIL-15: every key followed by {@code -}, including the last ({@code ("a","b") -> "a-b-"}). */
    public static String appendKeys(String... llaves) {
        StringBuilder sb = new StringBuilder();
        for (String llave : llaves) {
            sb.append(llave).append('-');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ JSON

    /**
     * UTIL-20: Gson JSON of {@code obj} ({@code "[]"} for {@code null}): nulls serialized, NaN/Infinity literals, HTML
     * escaping on, dates as {@code yyyy-MM-dd HH:mm:ss.SSS} in the default zone.
     */
    public static String printJson(Object obj, boolean pretty) {
        if (obj == null) {
            return "[]";
        }
        GsonBuilder b = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat(FORMATO_FECHA_JSON).serializeNulls();
        if (pretty) {
            b.setPrettyPrinting();
        }
        return b.create().toJson(obj);
    }

    /**
     * UTIL-22: a NEW {@link Gson} per call (its date format captures the default zone when first used): dates as
     * {@code yyyy-MM-dd HH:mm:ss.SSS}, lenient {@link GsonTimestampDeserializer} / {@link GsonDateDeserializer},
     * nulls and NaN/Infinity serialized. Legacy Gson leniency kept.
     */
    public static Gson getGsonBuilder() {
        return new GsonBuilder()
                .setDateFormat(FORMATO_FECHA_JSON)
                .registerTypeAdapter(Timestamp.class, new GsonTimestampDeserializer())
                .registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer())
                .serializeSpecialFloatingPointValues()
                .serializeNulls()
                .create();
    }

    // ------------------------------------------------------------------ reflection / collections

    /**
     * UTIL-24: indexes {@code list} by its public field {@code keyfield} (looked up on the class of the FIRST element).
     * A {@code null} list or a duplicate key throws {@code java.lang.Exception}; an unknown field throws
     * {@link NoSuchFieldException}.
     */
    @SuppressWarnings("unchecked")
    public static <T, K> HashMap<T, Object> List2Hash(List<K> list, String keyfield) throws Exception {
        requerirLista(list);
        HashMap<T, Object> h = new HashMap<>();
        if (list.isEmpty()) {
            return h;
        }
        Field field = list.getFirst().getClass().getField(keyfield);
        for (Object obj : list) {
            T clave = (T) field.get(obj);
            if (h.containsKey(clave)) {
                throw new Exception("key field is not unique");
            }
            h.put(clave, obj);
        }
        return h;
    }

    /** UTIL-25: the values of the public field {@code keyfield}, in order; without duplicates unless {@code duplicate}. */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getColumn(List<?> list, String keyfield, boolean duplicate) throws Exception {
        requerirLista(list);
        List<T> columna = new ArrayList<>();
        if (list.isEmpty()) {
            return columna;
        }
        Field field = list.getFirst().getClass().getField(keyfield);
        Set<T> vistos = new HashSet<>();
        for (Object obj : list) {
            T valor = (T) field.get(obj);
            if (duplicate || vistos.add(valor)) {
                columna.add(valor);
            }
        }
        return columna;
    }

    /** UTIL-26: each element as a map of its public fields (static ones included, as in the legacy). */
    public static <K> List<HashMap<String, Object>> List2ListHashGenericObject(List<K> list) throws Exception {
        requerirLista(list);
        List<HashMap<String, Object>> filas = new ArrayList<>(list.size());
        for (Object obj : list) {
            HashMap<String, Object> fila = new HashMap<>();
            for (Field f : obj.getClass().getFields()) {
                fila.put(f.getName(), f.get(obj));
            }
            filas.add(fila);
        }
        return filas;
    }

    private static void requerirLista(List<?> list) throws Exception {
        if (list == null) {
            throw new Exception("List must be not null");
        }
    }

    /** UTIL-27: {@code copyPojo(objSrc, objTarget, true)}. */
    @Deprecated
    public static <K, T> T copyPojo(K objSrc, T objTarget) throws Exception {
        return copyPojo(objSrc, objTarget, true);
    }

    /**
     * UTIL-27: copies the public fields with the same name and type from {@code objSrc} to {@code objTarget}.
     * Copies are fresh instances for String, BigDecimal, Timestamp, java.sql.Date/Time and List (a new ArrayList of
     * recursive copies); Long/Integer are immutable and shared. With {@code restrictDate == false} it also converts
     * String to/from Timestamp and java.sql.Date through {@link Fecha} (bad text propagates
     * {@link java.time.format.DateTimeParseException}).
     *
     * <p>Legacy defects kept as pinned: other types (Double, Boolean, primitives, nested POJOs) are silently NOT copied
     * unless the source value is {@code null}; fields missing in the source or not writable are skipped.
     *
     * @deprecated as in the legacy: prefer copying field by field.
     */
    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, T> T copyPojo(K objSrc, T objTarget, boolean restrictDate) throws Exception {
        if (objSrc == null) {
            throw new Exception("objSrc  must be not null");
        }
        if (objTarget == null) {
            throw new Exception("objTarget  must be not null");
        }
        Class<?> tipoOrigen = objSrc.getClass();
        for (Field destino : objTarget.getClass().getFields()) {
            try {
                Field origen = tipoOrigen.getField(destino.getName());
                String td = destino.getType().getName();
                String to = origen.getType().getName();
                Object valor = origen.get(objSrc);
                if (td.equals(to)) {
                    if (valor == null) {
                        destino.set(objTarget, null);
                        continue;
                    }
                    switch (td) {
                        case "java.lang.Long", "java.lang.Integer" -> destino.set(objTarget, valor); // immutable: shared
                        case "java.lang.String" -> destino.set(objTarget, new String((String) valor)); // fresh instance (identity is pinned)
                        case "java.math.BigDecimal" -> destino.set(objTarget, new BigDecimal(valor.toString()));
                        case "java.sql.Timestamp" -> destino.set(objTarget, new Timestamp(((Timestamp) valor).getTime()));
                        case "java.sql.Date" -> destino.set(objTarget, new java.sql.Date(((java.sql.Date) valor).getTime()));
                        case "java.sql.Time" -> destino.set(objTarget, new Time(((Time) valor).getTime()));
                        case "java.util.List" -> {
                            List copia = new ArrayList();
                            destino.set(objTarget, copia);
                            for (Object item : (List) valor) {
                                copia.add(copyPojo(item, item.getClass().getDeclaredConstructor().newInstance()));
                            }
                        }
                        default -> {
                            // legacy: no branch for this declared type (Double, Boolean, int, nested POJO...): not copied
                        }
                    }
                } else if (!restrictDate) {
                    Object convertido = convertirFecha(td, to, valor);
                    if (convertido != NO_CONVIERTE) {
                        destino.set(objTarget, convertido);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // legacy: field skipped
            }
        }
        return objTarget;
    }

    private static final Object NO_CONVIERTE = new Object();

    private static Object convertirFecha(String destino, String origen, Object valor) {
        boolean soportada = switch (destino + "<-" + origen) {
            case "java.sql.Timestamp<-java.lang.String", "java.sql.Date<-java.lang.String",
                 "java.lang.String<-java.sql.Timestamp", "java.lang.String<-java.sql.Date" -> true;
            default -> false;
        };
        if (!soportada) {
            return NO_CONVIERTE;
        }
        if (valor == null) {
            return null;
        }
        return switch (valor) {
            case String s when destino.equals("java.sql.Timestamp") -> Fecha.toDateTime(s);
            case String s -> Fecha.toDate(s);
            case Timestamp t -> Fecha.toStringTimeStamp(t);
            case java.sql.Date d -> Fecha.toStringDate(d);
            default -> NO_CONVIERTE;
        };
    }

    // ------------------------------------------------------------------ dates

    /**
     * UTIL-33: elapsed milliseconds / 86 400 000, truncated. Legacy defect kept: across a DST jump two calendar days
     * can count as one.
     */
    public static int diferenciaEnDias(Date fechaMayor, Date fechaMenor) {
        return (int) ((fechaMayor.getTime() - fechaMenor.getTime()) / MS_POR_DIA);
    }

    // ------------------------------------------------------------------ files

    /**
     * UTIL-55: zips one file (entry = its name) into {@code fullzipFilename}, optionally deleting the source. The
     * output is opened first, as in the legacy: a missing input leaves an empty zip and throws
     * {@link java.io.FileNotFoundException}.
     */
    public static void fileToZipFile(String fullfileName, String fullzipFilename, boolean deleteSrc) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(fullzipFilename))) {
            File file = new File(fullfileName);
            try (FileInputStream in = new FileInputStream(file)) {
                zip.putNextEntry(new ZipEntry(file.getName()));
                in.transferTo(zip);
            }
            if (deleteSrc) {
                file.delete();
            }
        }
    }

    /**
     * UTIL-52: creates the file if missing ({@link java.io.FileNotFoundException} when its folder is missing) and sets
     * its modification time ({@link IllegalArgumentException} for a negative time).
     */
    public static void touch(File file, long timestamp) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(timestamp);
    }
}
