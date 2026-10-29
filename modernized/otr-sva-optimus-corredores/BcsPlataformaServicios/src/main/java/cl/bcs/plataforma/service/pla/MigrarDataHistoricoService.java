package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrates operational HANA table data to historic tables within a date range.
 * Params: p_Tabla (table code), p_Filtro (date field name), p_Desde, p_Hasta.
 */
@Service
public class MigrarDataHistoricoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(MigrarDataHistoricoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;

    // Each table has a SELECT (read from live) and INSERT (write to history)
    private final String SQL_SEL_HIS_CARTERA;             private final String SQL_INS_HIS_CARTERA;
    private final String SQL_SEL_HIS_SUBYACENTE;          private final String SQL_INS_HIS_SUBYACENTE;
    private final String SQL_SEL_HIS_GARANTIA;            private final String SQL_INS_HIS_GARANTIA;
    private final String SQL_SEL_HIS_CARTERA_NEGATIVA;    private final String SQL_INS_HIS_CARTERA_NEGATIVA;
    private final String SQL_SEL_HIS_FUSION_CARTERA;      private final String SQL_INS_HIS_FUSION_CARTERA;
    private final String SQL_SEL_HIS_FUSION_CARTERA_DET;  private final String SQL_INS_HIS_FUSION_CARTERA_DET;
    private final String SQL_SEL_HIS_RESERVA;             private final String SQL_INS_HIS_RESERVA;
    private final String SQL_SEL_CUS_PATRIMONIO;          private final String SQL_INS_CUS_PATRIMONIO;
    private final String SQL_SEL_CTA_PATRIMONIO_CAJA;     private final String SQL_INS_CTA_PATRIMONIO_CAJA;

    public MigrarDataHistoricoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        SQL_SEL_HIS_CARTERA          = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisCartera.sql");
        SQL_INS_HIS_CARTERA          = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisCartera.sql");
        SQL_SEL_HIS_SUBYACENTE       = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisSubyacente.sql");
        SQL_INS_HIS_SUBYACENTE       = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisSubyacente.sql");
        SQL_SEL_HIS_GARANTIA         = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisGarantia.sql");
        SQL_INS_HIS_GARANTIA         = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisGarantia.sql");
        SQL_SEL_HIS_CARTERA_NEGATIVA = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisCarteraNegativa.sql");
        SQL_INS_HIS_CARTERA_NEGATIVA = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisCarteraNegativa.sql");
        SQL_SEL_HIS_FUSION_CARTERA   = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisFusionCartera.sql");
        SQL_INS_HIS_FUSION_CARTERA   = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisFusionCartera.sql");
        SQL_SEL_HIS_FUSION_CARTERA_DET = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisFusionCarteraDetalle.sql");
        SQL_INS_HIS_FUSION_CARTERA_DET = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisFusionCarteraDetalle.sql");
        SQL_SEL_HIS_RESERVA          = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_HisReserva.sql");
        SQL_INS_HIS_RESERVA          = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_HisReserva.sql");
        SQL_SEL_CUS_PATRIMONIO       = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_CusPatrimonioCustodia.sql");
        SQL_INS_CUS_PATRIMONIO       = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_CusPatrimonioCustodia.sql");
        SQL_SEL_CTA_PATRIMONIO_CAJA  = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Select_Registros_CtaPatrimonioCaja.sql");
        SQL_INS_CTA_PATRIMONIO_CAJA  = loadSql("cl/bcs/plataforma/pla/sql/MigrarDataHistorica_Insert_Registros_CtaPatrimonioCaja.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.MigrarDataHistorico"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::MigrarDataHistorico");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();
        try {
            String[] sqlPair = resolveSqlPair(in.p_Tabla);
            if (sqlPair == null) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                addMsg(out, "ERROR", "TABLA_DESCONOCIDA", "Tabla no reconocida: " + in.p_Tabla);
                return toJson(out);
            }

            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("p_Desde", in.p_Desde).addValue("p_Hasta", in.p_Hasta)
                .addValue("p_Filtro", in.p_Filtro).addValue("p_Tabla", in.p_Tabla)
                .addValue("p_IdEmpresa", in.p_IdEmpresa);

            List<Map<String, Object>> rows = jdbc.queryForList(sqlPair[0], ps);
            if (!rows.isEmpty()) {
                MapSqlParameterSource[] batch = rows.stream()
                    .map(row -> {
                        MapSqlParameterSource src = new MapSqlParameterSource();
                        row.forEach(src::addValue);
                        return src;
                    }).toArray(MapSqlParameterSource[]::new);
                jdbc.batchUpdate(sqlPair[1], batch);
                log.info("Migrados {} registros de {}", rows.size(), in.p_Tabla);
            }
            addMsg(out, "OK", "MIGRADO", in.p_Tabla + " migrada entre " + in.p_Desde + " y " + in.p_Hasta);
        } catch (Exception e) {
            log.error("Error en MigrarDataHistorico: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        return toJson(out);
    }

    private String[] resolveSqlPair(String tabla) {
        if (tabla == null) return null;
        return switch (tabla) {
            case "\"BSCL.Custodia.BD::HIS.Cartera\""             -> new String[]{SQL_SEL_HIS_CARTERA,          SQL_INS_HIS_CARTERA};
            case "\"BSCL.Custodia.BD::HIS.SubyacenteCartera\""   -> new String[]{SQL_SEL_HIS_SUBYACENTE,       SQL_INS_HIS_SUBYACENTE};
            case "\"BSCL.Custodia.BD::HIS.Garantia\""            -> new String[]{SQL_SEL_HIS_GARANTIA,         SQL_INS_HIS_GARANTIA};
            case "\"BSCL.Custodia.BD::HIS.CarteraNegativa\""     -> new String[]{SQL_SEL_HIS_CARTERA_NEGATIVA, SQL_INS_HIS_CARTERA_NEGATIVA};
            case "\"BSCL.Custodia.BD::HIS.FusionCartera\""       -> new String[]{SQL_SEL_HIS_FUSION_CARTERA,   SQL_INS_HIS_FUSION_CARTERA};
            case "\"BSCL.Custodia.BD::HIS.FusionCarteraDetalle\"" -> new String[]{SQL_SEL_HIS_FUSION_CARTERA_DET, SQL_INS_HIS_FUSION_CARTERA_DET};
            case "\"BSCL.Custodia.BD::HIS.Reserva\""             -> new String[]{SQL_SEL_HIS_RESERVA,          SQL_INS_HIS_RESERVA};
            case "\"BSCL.Custodia.BD::CUS.PatrimonioCustodia\""  -> new String[]{SQL_SEL_CUS_PATRIMONIO,       SQL_INS_CUS_PATRIMONIO};
            case "\"BSCL.CuentaInversion.BD::CTA.PatrimonioCaja\"" -> new String[]{SQL_SEL_CTA_PATRIMONIO_CAJA, SQL_INS_CTA_PATRIMONIO_CAJA};
            default -> null;
        };
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) {
        return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario;
        public String p_Tabla; public String p_Filtro;
        public String p_Desde; public String p_Hasta; public String p_SoloLimpia;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
