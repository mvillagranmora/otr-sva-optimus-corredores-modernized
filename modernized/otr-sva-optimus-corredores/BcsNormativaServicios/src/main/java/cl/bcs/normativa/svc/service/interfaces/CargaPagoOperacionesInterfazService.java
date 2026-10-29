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
public class CargaPagoOperacionesInterfazService {

    private static final Logger log = LoggerFactory.getLogger(CargaPagoOperacionesInterfazService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_PAGO;
    private final String SQL_INSERT_LOG;

    private static final int LINE_SIZE = 258;

    public CargaPagoOperacionesInterfazService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_PAGO = loadSql("cl/bcs/normativa/interfaces/sql/CargaPagoOperacionesInterfaz_InsertPagoOperaciones.sql");
        this.SQL_INSERT_LOG = loadSql("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CargaPagoOperacionesInterfazService.execute called");
        SimpleDateFormat fm = new SimpleDateFormat("ddMMyyyyHHmm");
        String fecha = fm.format(new Date());
        String ruta = params.has("p_Ruta") ? params.get("p_Ruta").getAsString() : "";
        String archivoB64 = params.has("p_Archivo") ? params.get("p_Archivo").getAsString() : "";

        int numRegistros = 0;
        int numCorrectos = 0;
        int numErroneos = 0;

        List<MapSqlParameterSource> pagos = new ArrayList<>();
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
                        p.addValue("FechaOperacion",             registro.substring(0, 8));
                        p.addValue("Modalidad",                  Integer.parseInt(registro.substring(8, 10)));
                        p.addValue("NumeroOperacion",            Integer.parseInt(registro.substring(10, 16)));
                        p.addValue("TipoMoneda",                 Integer.parseInt(registro.substring(16, 18)));
                        p.addValue("TipoOperacion",              Integer.parseInt(registro.substring(18, 20)));
                        p.addValue("TipoInstrumento",            Integer.parseInt(registro.substring(20, 22)));
                        p.addValue("EstadoOperacion",            Integer.parseInt(registro.substring(22, 24)));
                        p.addValue("CodigoValor",                registro.substring(24, 36));
                        p.addValue("Nemotecnico",                registro.substring(36, 46));
                        p.addValue("Precio",                     new BigDecimal(registro.substring(46, 54).trim() + "." + registro.substring(54, 62).trim()));
                        p.addValue("Cantidad",                   new BigDecimal(registro.substring(62, 76).trim() + "." + registro.substring(76, 80).trim()));
                        p.addValue("AgenteComprador",            Integer.parseInt(registro.substring(80, 84)));
                        p.addValue("AgenteVendedor",             Integer.parseInt(registro.substring(84, 88)));
                        p.addValue("MontoEfectivo",              new BigDecimal(registro.substring(88, 102).trim() + "." + registro.substring(102, 104).trim()));
                        p.addValue("ValorNominal",               new BigDecimal(registro.substring(104, 112).trim() + "." + registro.substring(112, 120).trim()));
                        p.addValue("TasaOperacion",              new BigDecimal(registro.substring(120, 124).trim() + "." + registro.substring(124, 128).trim()));
                        p.addValue("FechaLiquidacionContado",    registro.substring(128, 136));
                        p.addValue("FechaLiquidacionPlazo",      registro.substring(136, 144));
                        p.addValue("CantidadDiasOperacion",      Integer.parseInt(registro.substring(144, 148)));
                        p.addValue("TipoAval",                   Integer.parseInt(registro.substring(148, 150)));
                        p.addValue("CodigoAval",                 Integer.parseInt(registro.substring(150, 154)));
                        p.addValue("TipoLiquidacion",            Integer.parseInt(registro.substring(154, 156)));
                        p.addValue("IndicadorHabilitacionFondos",Integer.parseInt(registro.substring(156, 158)));
                        p.addValue("TipoCambio",                 new BigDecimal(registro.substring(158, 164).trim() + "." + registro.substring(164, 170).trim()));
                        p.addValue("DescripcionValorNegociado",  registro.substring(170, 230));
                        p.addValue("HoraOperacion",              registro.substring(230, 236));
                        p.addValue("InteresesCorridosContadoPlazo", new BigDecimal(registro.substring(236, 246).trim() + "." + registro.substring(246, 250).trim()));
                        p.addValue("NumeroSubasta",              Integer.parseInt(registro.substring(250, 252)));
                        p.addValue("AgenteCompradorEspecialista",Integer.parseInt(registro.substring(252, 254)));
                        p.addValue("AgenteVendedorEspecialista", Integer.parseInt(registro.substring(254, 256)));
                        p.addValue("FlagOfertaPublica",          Integer.parseInt(registro.substring(256, 258)));
                        pagos.add(p);
                        numCorrectos++;
                    } catch (Exception e) {
                        numErroneos++;
                        log.warn("Error al parsear registro: {}", registro, e);
                    }
                }
            }

            if (!pagos.isEmpty()) {
                jdbc.batchUpdate(SQL_INSERT_PAGO, pagos.toArray(new MapSqlParameterSource[0]));
            }

            MapSqlParameterSource logP = new MapSqlParameterSource();
            logP.addValue("IdInterfaz", 11L);
            logP.addValue("NumeroRegistros", (long) numRegistros);
            logP.addValue("NumeroRegistrosCorrectos", (long) numCorrectos);
            logP.addValue("NumeroRegistrosIncorrectos", (long) numErroneos);
            jdbc.update(SQL_INSERT_LOG, logP);

        } catch (Exception e) {
            log.error("Error en CargaPagoOperacionesInterfaz", e);
            JsonObject err = new JsonObject();
            err.addProperty("p_out_ErroresContador", 1L);
            err.addProperty("p_out_Comentario", "Error: " + e.getMessage());
            err.addProperty("p_out_NombreArchivoLog", "nor/POP_" + fecha + ".log");
            err.addProperty("p_out_RutaArchivoLog", ruta + "/nor/POP_" + fecha + ".log");
            return err;
        }

        JsonObject out = new JsonObject();
        out.addProperty("p_out_ErroresContador", 0L);
        out.addProperty("p_out_NombreArchivoLog", "nor/POP_" + fecha + ".log");
        out.addProperty("p_out_RutaArchivoLog", ruta + "/nor/POP_" + fecha + ".log");
        out.addProperty("p_out_Comentario",
            "Total Registros:" + numRegistros + " / Registros Correctos:" + numCorrectos + " / Registros Erroneos:" + numErroneos);
        return out;
    }

    private static String loadSql(String path) {
        try (var is = CargaPagoOperacionesInterfazService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
