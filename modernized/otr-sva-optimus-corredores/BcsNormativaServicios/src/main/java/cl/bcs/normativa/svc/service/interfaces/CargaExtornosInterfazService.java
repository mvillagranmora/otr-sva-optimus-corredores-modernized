package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class CargaExtornosInterfazService {

    private static final Logger log = LoggerFactory.getLogger(CargaExtornosInterfazService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_EXTORNOS;
    private final String SQL_INSERT_LOG;

    private static final int LINE_SIZE = 145;

    public CargaExtornosInterfazService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_EXTORNOS = loadSql("cl/bcs/normativa/interfaces/sql/CargaExtornosInterfaz_InsertExtornos.sql");
        this.SQL_INSERT_LOG = loadSql("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CargaExtornosInterfazService.execute called");
        SimpleDateFormat fm = new SimpleDateFormat("ddMMyyyyHHmm");
        String fecha = fm.format(new Date());

        String ruta = params.has("p_Ruta") ? params.get("p_Ruta").getAsString() : "";
        String archivoB64 = params.has("p_Archivo") ? params.get("p_Archivo").getAsString() : "";

        int numRegistros = 0;
        int numCorrectos = 0;
        int numErroneos = 0;

        List<MapSqlParameterSource> extornos = new ArrayList<>();
        try {
            String archivoDecoded = new String(Base64.getDecoder().decode(archivoB64));
            String[] registros = archivoDecoded.split(System.getProperty("line.separator"));

            for (String registro : registros) {
                numRegistros++;
                if (registro.length() != LINE_SIZE) {
                    numErroneos++;
                    log.warn("Registro con error de tamaño: esperado={} obtenido={}", LINE_SIZE, registro.length());
                } else {
                    try {
                        MapSqlParameterSource p = new MapSqlParameterSource();
                        p.addValue("FechaOperacion", registro.substring(0, 8));
                        p.addValue("ModalidadOperacion", Integer.parseInt(registro.substring(8, 10)));
                        p.addValue("NumeroOperacion", Integer.parseInt(registro.substring(10, 16)));
                        p.addValue("CodigoAgente", Integer.parseInt(registro.substring(16, 20)));
                        p.addValue("TipoDetalle", Integer.parseInt(registro.substring(20, 21)));
                        p.addValue("CodigoComision", Integer.parseInt(registro.substring(21, 29)));
                        p.addValue("CantidadCava", new BigDecimal(registro.substring(29, 43).trim() + "." + registro.substring(43, 45).trim()));
                        p.addValue("CantidadFisi", new BigDecimal(registro.substring(45, 59).trim() + "." + registro.substring(59, 61).trim()));
                        p.addValue("GiroCheq", Integer.parseInt(registro.substring(61, 63)));
                        p.addValue("FlagTrabajador", Integer.parseInt(registro.substring(63, 65)));
                        p.addValue("ComisionContado", new BigDecimal(registro.substring(65, 68).trim() + "." + registro.substring(68, 74).trim()));
                        p.addValue("ComisionPlazo", new BigDecimal(registro.substring(74, 77).trim() + "." + registro.substring(77, 83).trim()));
                        p.addValue("FlagPoliza", registro.substring(83, 84));
                        p.addValue("FlagContado", registro.substring(84, 85));
                        p.addValue("FlagPlazo", registro.substring(85, 86));
                        p.addValue("CodigoCustodioValores", Integer.parseInt(registro.substring(86, 90)));
                        p.addValue("CodigoCustodioFondos", Integer.parseInt(registro.substring(90, 94)));
                        p.addValue("InteresContado", new BigDecimal(registro.substring(94, 102).trim() + "." + registro.substring(102, 104).trim()));
                        p.addValue("InteresPlazo", new BigDecimal(registro.substring(104, 112).trim() + "." + registro.substring(112, 114).trim()));
                        p.addValue("LiqDtc", registro.substring(114, 115));
                        p.addValue("FlagDayTrade", Integer.parseInt(registro.substring(115, 116)));
                        p.addValue("FechaVencimientoDocumento", registro.substring(116, 124));
                        p.addValue("IndicadorCartaFianza", Integer.parseInt(registro.substring(124, 125)));
                        p.addValue("MontoCartaFianza", new BigDecimal(registro.substring(125, 139).trim() + "." + registro.substring(139, 141).trim()));
                        p.addValue("CodigoBanco", Integer.parseInt(registro.substring(141, 145)));
                        extornos.add(p);
                        numCorrectos++;
                    } catch (Exception e) {
                        numErroneos++;
                        log.warn("Error al parsear registro: {}", registro, e);
                    }
                }
            }

            if (!extornos.isEmpty()) {
                jdbc.batchUpdate(SQL_INSERT_EXTORNOS, extornos.toArray(new MapSqlParameterSource[0]));
            }

            MapSqlParameterSource logP = new MapSqlParameterSource();
            logP.addValue("IdInterfaz", 12L);
            logP.addValue("NumeroRegistros", (long) numRegistros);
            logP.addValue("NumeroRegistrosCorrectos", (long) numCorrectos);
            logP.addValue("NumeroRegistrosIncorrectos", (long) numErroneos);
            jdbc.update(SQL_INSERT_LOG, logP);

        } catch (Exception e) {
            log.error("Error en CargaExtornosInterfaz", e);
            JsonObject err = new JsonObject();
            err.addProperty("p_out_ErroresContador", 1L);
            err.addProperty("p_out_Comentario", "Error: " + e.getMessage());
            err.addProperty("p_out_NombreArchivoLog", "nor/EXT_" + fecha + ".log");
            err.addProperty("p_out_RutaArchivoLog", ruta + "/nor/EXT_" + fecha + ".log");
            return err;
        }

        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_NombreArchivoLog", "nor/EXT_" + fecha + ".log");
        out.addProperty("p_out_RutaArchivoLog", ruta + "/nor/EXT_" + fecha + ".log");
        out.addProperty("p_out_Comentario",
            "Total Registros:" + numRegistros + " / Registros Correctos:" + numCorrectos + " / Registros Erroneos:" + numErroneos);
        return out;
    }

    private static String loadSql(String path) {
        try (var is = CargaExtornosInterfazService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
