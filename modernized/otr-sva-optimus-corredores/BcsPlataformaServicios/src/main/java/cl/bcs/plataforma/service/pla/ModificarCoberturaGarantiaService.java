package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
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
public class ModificarCoberturaGarantiaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarCoberturaGarantiaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPDATE;

    public ModificarCoberturaGarantiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/ModificarCoberturaGarantia_Update.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ModificarCoberturaGarantia"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ModificarCoberturaGarantia");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            if (in.p_Cobertura != null && !in.p_Cobertura.isEmpty()) {
                List<ObjProcesamiento> items = new ArrayList<>();
                for (ModificarCobertura mc : in.p_Cobertura) {
                    ObjProcesamiento pmc = new ObjProcesamiento();
                    pmc.IdCoberturaGarantia = mc.p_IdCoberturaGarantia;
                    pmc.IdEntidadDestinoGarantia = mc.p_IdEntidadDestinoGarantia;
                    pmc.IdTipoInstrumentoExigeGarantia = mc.p_IdTipoInstrumentoExigeGarantia;
                    pmc.IdInstrumentoExigeGarantia = mc.p_IdInstrumentoExigeGarantia;
                    pmc.IdTipoInstrumentoComoGarantia = mc.p_IdTipoInstrumentoComoGarantia;
                    pmc.IdTipoClasificacionRiesgo = mc.p_IdTipoClasificacionRiesgo;
                    pmc.ClasificacionRiesgo = mc.p_ClasificacionRiesgo;
                    pmc.PlazoTope = mc.p_PlazoTope;
                    pmc.UnidadPlazo = mc.p_UnidadPlazo;
                    pmc.CoberturaEntidad = mc.p_CoberturaEntidad;
                    pmc.CoberturaCorredora = mc.p_CoberturaCorredora;
                    pmc.IdEstado = mc.p_IdEstado;
                    pmc.IdEntidadClasificadoraRiesgo = mc.p_IdEntidadClasificadoraRiesgo;
                    items.add(pmc);
                }
                SqlParameterSource[] batch = items.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPDATE, batch);
            }

        } catch (Exception e) {
            log.error("Error en ModificarCoberturaGarantia: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<ModificarCobertura> p_Cobertura = new ArrayList<>();
    }

    public static class ModificarCobertura {
        public Long p_IdCoberturaGarantia;
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
    }

    public static class Salida {
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private static String loadSql(String path) {
        try (InputStream is = ModificarCoberturaGarantiaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
