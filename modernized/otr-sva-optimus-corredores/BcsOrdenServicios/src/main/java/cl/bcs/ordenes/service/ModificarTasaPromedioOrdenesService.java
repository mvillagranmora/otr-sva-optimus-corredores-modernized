package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.ModificarTasaPromedioOrdenes.
 * Validates empresa/usuario/modulo, checks module state, then batch-updates TasaPromedioReporte on each orden.
 */
@Service
public class ModificarTasaPromedioOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(ModificarTasaPromedioOrdenesService.class);
    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private static final String ID_MODULO_ORDENES   = "ORD";
    private static final String ESTADO_CERRADO      = "C";

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_ESTADO_MODULO;
    private final String SQL_UPDATE_ORDEN;

    public ModificarTasaPromedioOrdenesService(NamedParameterJdbcTemplate jdbc,
                                               RequisitosBasicosUtil requisitosUtil) {
        this.jdbc           = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_ESTADO_MODULO = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_UPDATE_ORDEN  = loadSql("cl/bcs/ordenes/sql/ModificarTasaPromedioOrdenes_UpdateOrden.sql");
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion = "N";
        public String p_UsaReglasNegocio = "S";
        public List<OrdenEntrada> p_Ordenes = new ArrayList<>();
    }

    public static class OrdenEntrada {
        public Long       IdInterno;
        public Long       IdOrden;
        public BigDecimal TasaPromedioReporte;
    }

    public static class Salida {
        public Long                  P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida>   P_OUT_MENSAJES        = new ArrayList<>();
        public List<ResultadoSalida> p_out_Resultado       = new ArrayList<>();
    }

    public static class MensajeSalida {
        public Long   IdInterno;
        public String tipo;
        public String codigo;
        public String descripcion;
        public MensajeSalida(Long id, String tipo, String codigo) {
            this.IdInterno = id; this.tipo = tipo; this.codigo = codigo; this.descripcion = codigo;
        }
    }

    public static class ResultadoSalida {
        public Long   IdInterno;
        public Long   IdOrden;
        public String Msg;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ModificarTasaPromedioOrdenesService.execute()");
        Entrada e = GSON.fromJson(params.toString(), Entrada.class);
        Salida salida = logica(e);
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    private Salida logica(Entrada in) {
        Salida out = new Salida();

        // 1. RevisarRequisitosBasicos
        RequisitosBasicosUtil.Salida basicos = requisitosUtil.validar(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        if (basicos.contarErrores() > 0) {
            basicos.p_out_Mensajes.forEach(m -> out.P_OUT_MENSAJES.add(
                    new MensajeSalida(m.IdInterno, m.tipo, m.codigo)));
            out.P_OUT_ERRORESCONTADOR = basicos.contarErrores();
            return out;
        }

        // 2. Validate module state
        if (in.p_Ordenes == null || in.p_Ordenes.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "NO_SE_INGRESARON_ORDENES"));
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        List<Map<String, Object>> modulo = jdbc.queryForList(SQL_ESTADO_MODULO,
                new MapSqlParameterSource("IdModulo", ID_MODULO_ORDENES).addValue("IdEmpresa", in.p_IdEmpresa));
        if (modulo.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "MODULO_NO_EXISTE"));
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }
        String estadoModulo = String.valueOf(modulo.get(0).get("IdEstadoModulo"));
        if (ESTADO_CERRADO.equalsIgnoreCase(estadoModulo)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "MODULO_CERRADO"));
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        // 3. Build result list and batch-update
        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        for (OrdenEntrada ord : in.p_Ordenes) {
            ResultadoSalida r = new ResultadoSalida();
            r.IdInterno = ord.IdInterno;
            r.IdOrden   = ord.IdOrden;
            r.Msg       = "OK";
            out.p_out_Resultado.add(r);
            batchParams.add(new MapSqlParameterSource()
                    .addValue("IdOrden", ord.IdOrden)
                    .addValue("TasaPromedioReporte", ord.TasaPromedioReporte));
        }

        if ("N".equalsIgnoreCase(in.p_simularOperacion) && !batchParams.isEmpty()) {
            jdbc.batchUpdate(SQL_UPDATE_ORDEN, batchParams.toArray(new MapSqlParameterSource[0]));
        }

        return out;
    }

    private static String loadSql(String path) {
        try (var is = ModificarTasaPromedioOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
