package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;

/**
 * Spring Boot implementation of three J-type JavaCall classes from BcsPortafolioServicios:
 *   - cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV
 *   - cl.bcs.portafolio.EliminarMatrizAgrupadorNOAPV
 *   - cl.bcs.portafolio.ModificarMatrizAgrupadorNOAPV
 *
 * Replaces cl.bcs.utils.SQL with NamedParameterJdbcTemplate via JavaCallInfra.
 * MensajeSalida uses the code as descripcion (no DB dictionary lookup).
 */
@Service
public class MatrizJavaCallService {

    private static final Logger log = LoggerFactory.getLogger(MatrizJavaCallService.class);

    private static final String SQL_SELECT = loadSql(
            "cl/bcs/portafolio/sql/ComunMatrizAgrupadorNOAPV_SelectMatrizAgrupadorNOAPV.sql");
    private static final String SQL_INSERT = loadSql(
            "cl/bcs/portafolio/sql/CrearMatrizAgrupadorNOAPV_InsertMatrizAgrupadorNOAPV.sql");
    private static final String SQL_DELETE = loadSql(
            "cl/bcs/portafolio/sql/EliminarMatrizAgrupadorNOAPV_EliminarMatrizAgrupadorNOAPV.sql");
    private static final String SQL_UPDATE = loadSql(
            "cl/bcs/portafolio/sql/ModificarMatrizAgrupadorNOAPV_ActualizarMatrizAgrupadorNOAPV.sql");

    // ─── Shared POJO ─────────────────────────────────────────────────────────

    public static class MatrizRow {
        public Long   IdMatrizAgrupadorNOAPV;
        public String CodigoPortafolio;
        public String EsADC;
        public String IdEstado;
    }

    // ─── Shared response ─────────────────────────────────────────────────────

    private static class Salida {
        public Long               P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES       = new ArrayList<>();
    }

    // ─── CrearMatrizAgrupadorNOAPV ────────────────────────────────────────────

    public JsonObject crearMatriz(Connection conn, JsonObject in) throws Exception {
        log.info("INICIO::CrearMatrizAgrupadorNOAPV");
        Gson gson = new Gson();

        var entrada = gson.fromJson(in.toString(), CrearEntrada.class);
        Salida salida = new Salida();

        List<String> codigos = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> m.CodigoPortafolio)
                .collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put("CodigoPortafolio", codigos);
        params.put("IdMatrizAgrupadorNOAPV", List.of());

        List<MatrizRow> existentes = query(conn, SQL_SELECT, params, fieldMapper(MatrizRow.class));

        for (MatrizRow m : existentes) {
            salida.P_OUT_MENSAJES.add(MensajeSalida.error(0L,
                    "CODIGO_PORTAFOLIO_YA_EXISTE_MATRIZ",
                    "CodigoPortafolio=" + m.CodigoPortafolio));
            salida.P_OUT_ERRORESCONTADOR++;
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            log.info("FIN::CrearMatrizAgrupadorNOAPV (con errores)");
            return toJson(gson, salida);
        }

        MapSqlParameterSource[] sources = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> new MapSqlParameterSource()
                        .addValue("CodigoPortafolio", m.CodigoPortafolio)
                        .addValue("IdEstado", m.IdEstado)
                        .addValue("EsADC", m.EsADC))
                .toArray(MapSqlParameterSource[]::new);
        batch(conn, SQL_INSERT, sources);

        salida.P_OUT_MENSAJES.add(MensajeSalida.ok("MATRIZ_CREADA_EXISTOSA"));
        log.info("FIN::CrearMatrizAgrupadorNOAPV");
        return toJson(gson, salida);
    }

    public static class CrearEntrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<MatrizRow> p_MatrizAgrupadorNOAPV = new ArrayList<>();
    }

    // ─── EliminarMatrizAgrupadorNOAPV ─────────────────────────────────────────

    public JsonObject eliminarMatriz(Connection conn, JsonObject in) throws Exception {
        log.info("INICIO::EliminarMatrizAgrupadorNOAPV");
        Gson gson = new Gson();

        var entrada = gson.fromJson(in.toString(), EliminarEntrada.class);
        Salida salida = new Salida();

        List<Long> ids = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> m.IdMatrizAgrupadorNOAPV)
                .collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put("IdMatrizAgrupadorNOAPV", ids);
        params.put("CodigoPortafolio", List.of());

        List<MatrizRow> existentes = query(conn, SQL_SELECT, params, fieldMapper(MatrizRow.class));
        Map<Long, MatrizRow> existMap = existentes.stream()
                .collect(Collectors.toMap(m -> m.IdMatrizAgrupadorNOAPV, m -> m));

        for (MatrizRow m : entrada.p_MatrizAgrupadorNOAPV) {
            if (!existMap.containsKey(m.IdMatrizAgrupadorNOAPV)) {
                salida.P_OUT_MENSAJES.add(MensajeSalida.error(0L,
                        "ID_MATRIZ_NO_EXISTE_MATRIZ",
                        "IdMatrizAgrupadorNOAPV=" + m.IdMatrizAgrupadorNOAPV));
                salida.P_OUT_ERRORESCONTADOR++;
            }
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            log.info("FIN::EliminarMatrizAgrupadorNOAPV (con errores)");
            return toJson(gson, salida);
        }

        MapSqlParameterSource[] sources = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> new MapSqlParameterSource(
                        "IdMatrizAgrupadorNOAPV", m.IdMatrizAgrupadorNOAPV))
                .toArray(MapSqlParameterSource[]::new);
        batch(conn, SQL_DELETE, sources);

        salida.P_OUT_MENSAJES.add(MensajeSalida.ok("MATRIZ_ELIMINACION_EXISTOSA"));
        log.info("FIN::EliminarMatrizAgrupadorNOAPV");
        return toJson(gson, salida);
    }

    public static class EliminarEntrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<MatrizRow> p_MatrizAgrupadorNOAPV = new ArrayList<>();
    }

    // ─── ModificarMatrizAgrupadorNOAPV ────────────────────────────────────────

    public JsonObject modificarMatriz(Connection conn, JsonObject in) throws Exception {
        log.info("INICIO::ModificarMatrizAgrupadorNOAPV");
        Gson gson = new Gson();

        var entrada = gson.fromJson(in.toString(), ModificarEntrada.class);
        Salida salida = new Salida();

        List<Long> ids = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> m.IdMatrizAgrupadorNOAPV)
                .collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put("IdMatrizAgrupadorNOAPV", ids);
        params.put("CodigoPortafolio", List.of());

        List<MatrizRow> existentes = query(conn, SQL_SELECT, params, fieldMapper(MatrizRow.class));
        Map<Long, MatrizRow> existMap = existentes.stream()
                .collect(Collectors.toMap(m -> m.IdMatrizAgrupadorNOAPV, m -> m));

        for (MatrizRow m : entrada.p_MatrizAgrupadorNOAPV) {
            if (!existMap.containsKey(m.IdMatrizAgrupadorNOAPV)) {
                salida.P_OUT_MENSAJES.add(MensajeSalida.error(0L,
                        "ID_MATRIZ_NO_EXISTE_MATRIZ",
                        "IdMatrizAgrupadorNOAPV=" + m.IdMatrizAgrupadorNOAPV));
                salida.P_OUT_ERRORESCONTADOR++;
            }
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            log.info("FIN::ModificarMatrizAgrupadorNOAPV (con errores)");
            return toJson(gson, salida);
        }

        MapSqlParameterSource[] sources = entrada.p_MatrizAgrupadorNOAPV.stream()
                .map(m -> new MapSqlParameterSource()
                        .addValue("EsADC", m.EsADC)
                        .addValue("IdMatrizAgrupadorNOAPV", m.IdMatrizAgrupadorNOAPV))
                .toArray(MapSqlParameterSource[]::new);
        batch(conn, SQL_UPDATE, sources);

        salida.P_OUT_MENSAJES.add(MensajeSalida.ok("MATRIZ_MODIFICACION_EXISTOSA"));
        log.info("FIN::ModificarMatrizAgrupadorNOAPV");
        return toJson(gson, salida);
    }

    public static class ModificarEntrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<MatrizRow> p_MatrizAgrupadorNOAPV = new ArrayList<>();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static JsonObject toJson(Gson gson, Object obj) {
        return JsonParser.parseString(gson.toJson(obj)).getAsJsonObject();
    }
}
