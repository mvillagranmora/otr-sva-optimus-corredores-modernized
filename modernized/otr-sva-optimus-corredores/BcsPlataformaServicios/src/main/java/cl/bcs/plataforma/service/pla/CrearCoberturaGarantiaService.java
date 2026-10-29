package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrearCoberturaGarantiaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearCoberturaGarantiaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT;
    private static final String SQL_NEXT_SEQ =
        "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"CoberturaGarantia\".NEXTVAL FROM DUMMY";

    public CrearCoberturaGarantiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/CrearCoberturaGarantia_Insert.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.CrearCoberturaGarantia"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearCoberturaGarantia");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<ObjProcesamiento> items = new ArrayList<>();
            Long lastId = 0L;

            for (Cobertura c : in.p_Cobertura) {
                Long nextId = jdbc.queryForObject(SQL_NEXT_SEQ, new MapSqlParameterSource(), Long.class);
                lastId = nextId;

                ObjProcesamiento prc = new ObjProcesamiento();
                prc.IdCoberturaGarantia = nextId;
                prc.IdEntidadDestinoGarantia = c.p_IdEntidadDestinoGarantia;
                prc.IdTipoInstrumentoExigeGarantia = c.p_IdTipoInstrumentoExigeGarantia;
                prc.IdInstrumentoExigeGarantia = c.p_IdInstrumentoExigeGarantia;
                prc.IdTipoInstrumentoComoGarantia = c.p_IdTipoInstrumentoComoGarantia;
                prc.IdTipoClasificacionRiesgo = c.p_IdTipoClasificacionRiesgo;
                prc.ClasificacionRiesgo = c.p_ClasificacionRiesgo;
                prc.PlazoTope = c.p_PlazoTope;
                prc.UnidadPlazo = c.p_UnidadPlazo;
                prc.CoberturaEntidad = c.p_CoberturaEntidad;
                prc.CoberturaCorredora = c.p_CoberturaCorredora;
                prc.IdEstado = c.p_IdEstado;
                prc.IdEntidadClasificadoraRiesgo = c.p_IdEntidadClasificadoraRiesgo;
                prc.IdConcepto = c.p_IdConcepto;
                items.add(prc);
            }

            SqlParameterSource[] batch = items.stream()
                    .map(BeanPropertySqlParameterSource::new)
                    .toArray(SqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_INSERT, batch);
            out.P_OUT_REF_ID = lastId;

        } catch (Exception e) {
            log.error("Error en CrearCoberturaGarantia: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public List<Cobertura> p_Cobertura = new ArrayList<>();
    }

    public static class Cobertura {
        public Long p_IdEntidadDestinoGarantia;
        public String p_IdTipoInstrumentoExigeGarantia;
        public Long p_IdInstrumentoExigeGarantia;
        public String p_IdTipoInstrumentoComoGarantia;
        public String p_IdTipoClasificacionRiesgo;
        public String p_ClasificacionRiesgo;
        public Long p_PlazoTope;
        public String p_UnidadPlazo;
        public BigDecimal p_CoberturaEntidad;
        public BigDecimal p_CoberturaCorredora;
        public String p_IdEstado;
        public String p_IdEntidadClasificadoraRiesgo;
        public String p_IdConcepto;
    }

    public static class ObjProcesamiento {
        public Long IdCoberturaGarantia;
        public Long IdEntidadDestinoGarantia;
        public String IdTipoInstrumentoExigeGarantia;
        public Long IdInstrumentoExigeGarantia;
        public String IdTipoInstrumentoComoGarantia;
        public String IdTipoClasificacionRiesgo;
        public String ClasificacionRiesgo;
        public Long PlazoTope;
        public String UnidadPlazo;
        public BigDecimal CoberturaEntidad;
        public BigDecimal CoberturaCorredora;
        public String IdEstado;
        public String IdEntidadClasificadoraRiesgo;
        public String IdConcepto;
    }

    public static class Salida {
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_REF_ID;
    }

    private static String loadSql(String path) {
        try (InputStream is = CrearCoberturaGarantiaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
