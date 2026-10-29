package cl.bcs.plataforma.service.pla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Migrated from legacy cl.bcs.plataforma.pla.ObtenerNemoSVS (JavaCall).
 *
 * Computes the SVS (Superintendencia de Valores y Seguros) nemotecnico code
 * for each instrument based on issuer type, currency, and maturity date.
 */
@Service
public class ObtenerNemoSVSService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerNemoSVSService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_EMISORES;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public ObtenerNemoSVSService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_EMISORES = loadSql(
                "cl/bcs/plataforma/pla/sql/ObtenerNemoSVS_ConsultarEmisores.sql");
    }

    /**
     * Executes the service.
     *
     * @param jin JSON with: p_Instrumentos (array of instrument objects)
     * @return JSON with: p_out_Instrumento (array of { IdInterno, NemoSVS })
     */
    @Transactional(readOnly = true)
    public JsonObject execute(JsonObject jin) {
        log.info("INICIO::ObtenerNemoSVS");

        Type listType = new TypeToken<List<InstrumentoEntrada>>() {}.getType();
        List<InstrumentoEntrada> instrumentos = new ArrayList<>();
        if (jin.has("p_Instrumentos")) {
            instrumentos = gson.fromJson(jin.get("p_Instrumentos"), listType);
        }

        // Collect distinct issuer IDs
        List<String> idsEmisor = instrumentos.stream()
                .map(i -> i.IdEmisor)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // Query emitters from DB
        Map<String, EmisorProceso> emisores = java.util.Collections.emptyMap();
        if (!idsEmisor.isEmpty()) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("idsEmisor", idsEmisor);

            List<EmisorProceso> emisoresList = jdbc.query(
                    SQL_SELECT_EMISORES,
                    params,
                    (rs, rowNum) -> {
                        EmisorProceso e = new EmisorProceso();
                        e.IdEmisor = rs.getString("IdEmisor");
                        e.CodigoRegulador = rs.getString("CodigoRegulador");
                        e.TipoEntidad = rs.getString("TipoEntidad");
                        return e;
                    });

            emisores = emisoresList.stream()
                    .collect(Collectors.toMap(e -> e.IdEmisor, e -> e, (a, b) -> a));
        }

        // Compute NemoSVS for each instrument
        List<InstrumentoSalida> salida = new ArrayList<>();
        for (InstrumentoEntrada inst : instrumentos) {
            InstrumentoSalida s = new InstrumentoSalida();
            s.IdInterno = inst.IdInterno;
            s.NemoSVS = computeNemoSVS(inst, emisores.get(inst.IdEmisor));
            salida.add(s);
        }

        JsonObject out = gson.toJsonTree(
                java.util.Collections.singletonMap("p_out_Instrumento", salida)).getAsJsonObject();

        log.info("FIN::ObtenerNemoSVS ({} instrumentos)", instrumentos.size());
        return out;
    }

    private String computeNemoSVS(InstrumentoEntrada inst, EmisorProceso emi) {
        if (emi == null) return "";

        // Entity-type code
        String codigoTipoEntidad = switch (emi.TipoEntidad == null ? "" : emi.TipoEntidad) {
            case "1" -> "B";
            case "2" -> "F";
            case "3" -> "S";
            case "4" -> "O";
            case "5" -> "T";
            default -> "";
        };

        // Issuer code
        String codigoEmisor;
        if ("CENTRAL".equalsIgnoreCase(emi.IdEmisor)) {
            String nemo = inst.Nemotecnico == null ? "" : inst.Nemotecnico;
            if (nemo.equalsIgnoreCase("PAGARE R") || nemo.equalsIgnoreCase("PRBC")) {
                codigoEmisor = "PRBC";
            } else if (nemo.equalsIgnoreCase("PAGARE NR") || nemo.equalsIgnoreCase("PDBC")) {
                codigoEmisor = "PDBC";
            } else {
                codigoEmisor = "";
            }
        } else {
            String cr = emi.CodigoRegulador == null ? "" : emi.CodigoRegulador;
            codigoEmisor = cr.length() == 3 ? cr + "-" : cr;
        }

        // Readjustment type code
        String tipoReajuste = inst.IdTipoReajuste == null ? "" : inst.IdTipoReajuste;
        String pagaderoEn = inst.PagaderoEn == null ? "" : inst.PagaderoEn;
        String nemo = inst.Nemotecnico == null ? "" : inst.Nemotecnico;

        String codigoTipoReajuste = switch (tipoReajuste.toUpperCase()) {
            case "CLP" -> "N";
            case "UF" -> "U";
            case "USD" -> {
                if (nemo.equalsIgnoreCase("AUTODESEMBOLSO") ||
                        nemo.equalsIgnoreCase("DEPOSITO") ||
                        nemo.equalsIgnoreCase("PAGARE R")) {
                    yield "*";
                } else {
                    yield "CLP".equalsIgnoreCase(pagaderoEn) ? "D" : "*";
                }
            }
            case "EUR" -> "EUR".equalsIgnoreCase(pagaderoEn) ? "R" : "E";
            case "IVP" -> "I";
            case "ICP" -> "O";
            default -> "O";
        };

        // Date parts from FechaVencimiento (expected format: "yyyy-MM-dd")
        String day = "", month = "", year = "";
        if (inst.FechaVencimiento != null && !inst.FechaVencimiento.isBlank()) {
            try {
                java.sql.Date fechaVencimiento = java.sql.Date.valueOf(inst.FechaVencimiento);
                day   = new SimpleDateFormat("dd").format(fechaVencimiento);
                month = new SimpleDateFormat("MM").format(fechaVencimiento);
                year  = new SimpleDateFormat("yy").format(fechaVencimiento);
            } catch (Exception e) {
                log.warn("Invalid FechaVencimiento '{}' for IdInterno {}", inst.FechaVencimiento, inst.IdInterno);
            }
        }

        return codigoTipoEntidad + codigoTipoReajuste + codigoEmisor + day + month + year;
    }

    // ---- DTOs ----

    public static class InstrumentoEntrada {
        public Long IdInterno;
        public String IdEmisor;
        public String FechaVencimiento;
        public Long Plazo;
        public String Nemotecnico;
        public String IdTipoReajuste;
        public String PagaderoEn;
    }

    public static class InstrumentoSalida {
        public Long IdInterno;
        public String NemoSVS;
    }

    public static class EmisorProceso {
        public String IdEmisor;
        public String CodigoRegulador;
        public String TipoEntidad;
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerNemoSVSService.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
