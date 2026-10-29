package cl.bcs.cuentainversion.transaccional.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StreamUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Test-side EXECUTABLE REFERENCE of legacy GenerarArchivoCargoAbono (GenerarArchivoCargoAbono.java:152-285): the same two production SQL statements,
 * the same sequence (configuration, query, header, one line per row, relative path). It is NOT the modern implementation: it exists to prove that the
 * file-capture harness (seed placeholder {@code ${RUTA_BASE}}, captured files, masked timestamp, golden comparison) can go GREEN against the goldens
 * recorded from the legacy and RED when the behaviour is mutated ({@code ArchivosSelfCheckTest}). Expected values still come only from the goldens.
 */
public final class GenerarArchivoReferencia {

    /** Header codes of GenerarArchivoCargoAbono.java:222-255, translated by the oracle's Diccionario fake as {@code [CODE]{}}. */
    private static final List<String> ENCABEZADO = List.of("UI_NOMBRE_CLIENTE", "UI_DOCUMENTO_IDENTIDAD", "UI_PORTAFOLIO", "UI_FECHA_MOVIMIENTO",
            "UI_FECHA_EFECTIVA", "UI_CONCEPTO", "UI_ES_TRANSFERENCIA", "UI_MONEDA", "UI_CARGO", "UI_ABONO", "UI_TIPO_COMPROBANTE",
            "UI_SECUENCIA_COMPROBANTE", "UI_FOLIO_COMPROBANTE", "UI_FECHA_COMPROBANTE", "UI_TIPO_PRODUCTO", "UI_TIPO_OPERACION", "UI_FECHA_OPERACION",
            "UI_FECHA_VENCIMIENTO", "UI_SECUENCIA_OPERACION", "UI_MONTO_OPERACION", "UI_SECUENCIA_MOVIMIENTO", "UI_SECUENCIA_TRANSFERENCIA",
            "UI_NOMBRE_CLIENTE_RELACIONADO", "UI_PORTAFOLIO_RELACIONADO", "UI_EMPLEADO_CREADOR", "UI_FECHA_CERTIFICACION", "UI_EMPLEADO_CERTIFICADOR",
            "UI_DESCRIPCION", "UI_ESTADO", "UI_MOTIVO_ANULACION", "UI_AUTORIZACION", "UI_ESTADO_AUTORIZACION", "UI_PRODUCTO", "UI_RELACION");

    private GenerarArchivoReferencia() {
    }

    private static String sql(String nombre, BiFunction<String, String, String> mutador) throws Exception {
        String s = StreamUtils.copyToString(new ClassPathResource("cl/bcs/cuentaInversion/sql/" + nombre + ".sql").getInputStream(),
                java.nio.charset.StandardCharsets.UTF_8);
        return mutador == null ? s : mutador.apply(nombre, s);
    }

    private static String texto(Object o) {
        return o == null ? "" : o.toString();
    }

    /** Runs the reference; checked exceptions are rethrown as they are (the harness compares the exception TYPE). */
    public static JsonObject ejecutar(NamedParameterJdbcTemplate jdbc, JsonObject in, BiFunction<String, String, String> mutador) {
        try {
            return ejecutarConChequeo(jdbc, in, mutador);
        } catch (Exception e) {
            return GenerarArchivoReferencia.<RuntimeException>sneaky(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> JsonObject sneaky(Throwable t) throws T {
        throw (T) t;
    }

    private static JsonObject ejecutarConChequeo(NamedParameterJdbcTemplate jdbc, JsonObject in, BiFunction<String, String, String> mutador)
            throws Exception {
        JsonObject out = new JsonObject();
        JsonArray mensajes = new JsonArray();
        out.add("P_OUT_MENSAJES", mensajes);

        List<Map<String, Object>> conf = jdbc.queryForList(sql("GenerarArchivoCargoAbono_SelectConfiguracion", mutador),
                new MapSqlParameterSource("IdEmpresa", in.get("p_IdEmpresa").getAsLong()));
        String rutaArchivo = "";
        String rutaCta = "";
        for (Map<String, Object> c : conf) {
            rutaArchivo = texto(c.get("RutaArchivo"));
            rutaCta = texto(c.get("RutaCta"));
        }
        if (rutaArchivo.isEmpty() || rutaCta.isEmpty()) {
            JsonObject m = new JsonObject();
            m.addProperty("id", 0);
            m.addProperty("tipo", "ERROR");
            m.addProperty("codigo", "FALTA_CONFIGURACION_RUTA_ARCHIVO");
            m.addProperty("descripcion", "FALTA_CONFIGURACION_RUTA_ARCHIVO sin traduccion");
            mensajes.add(m);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1);
            out.addProperty("P_OUT_CANTIDADREGISTROS", 0);
            return out;
        }
        String nombre = "MovimientosCargoAbono_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("FechaDesde", in.get("p_FechaDesde").isJsonNull() ? null : in.get("p_FechaDesde").getAsString())
                .addValue("FechaHasta", in.get("p_FechaHasta").isJsonNull() ? null : in.get("p_FechaHasta").getAsString())
                .addValue("IdEjecutivo", in.get("p_IdEjecutivo").isJsonNull() ? null : in.get("p_IdEjecutivo").getAsLong())
                .addValue("IdMoneda", in.get("p_IdMoneda").isJsonNull() ? null : in.get("p_IdMoneda").getAsString())
                .addValue("IdTipoMovimiento", in.get("p_IdTipoMovimiento").isJsonNull() ? null : in.get("p_IdTipoMovimiento").getAsString())
                .addValue("IdConcepto", in.get("p_IdConcepto").isJsonNull() ? null : in.get("p_IdConcepto").getAsString())
                .addValue("IdEstado", in.get("p_IdEstado").isJsonNull() ? null : in.get("p_IdEstado").getAsString());
        List<Map<String, Object>> filas = jdbc.queryForList(sql("GenerarArchivoCargoAbono_SelectCargosAbonos", mutador), p);

        if (!filas.isEmpty()) {
            // like the legacy (GenerarArchivoCargoAbono.java:219-270): no try/finally, so when a row breaks the loop nothing is flushed
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(rutaArchivo + nombre)));
            StringBuilder linea = new StringBuilder();
            for (String codigo : ENCABEZADO) {
                linea.append('[').append(codigo).append("]{};");
            }
            bw.write(linea.toString());
            bw.newLine();
            for (Map<String, Object> f : filas) {
                bw.write((String) f.get("Linea"));
                bw.newLine();
            }
            bw.flush();
            bw.close();
            out.addProperty("P_OUT_RUTAARCHIVO", rutaCta + nombre);
        }
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.addProperty("P_OUT_CANTIDADREGISTROS", filas.size());
        return out;
    }
}
