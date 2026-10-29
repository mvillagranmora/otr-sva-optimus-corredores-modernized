package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;
import static cl.bcs.portafolio.javacall.JavaCallInfra.MensajeSalida.*;

/**
 * Migrates cl.bcs.portafolio.AdministrarPortafolioDefecto.
 *
 * INSERT path: getSequence → validate regex → batch INSERT.
 * DELETE path: validate existence → batch DELETE.
 * Auth/Log: RestClient → bcs-plataforma-servicios (CrearEventoAutorizacionMasivo / AgregarLog).
 */
@Service
public class AdministrarPortafolioDefectoService {

    private static final Logger log = LoggerFactory.getLogger(AdministrarPortafolioDefectoService.class);
    private static final Gson GSON = new Gson();

    private static final String ID_CONCEPTO = "ADMPOR";
    private static final String SEQ_NAME = "BSCL.Portafolio.BD.Secuencias::PortafolioConfiguracionDefecto";

    private static final String SQL_INSERT = loadSql("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_InsertPortafolio.sql");
    private static final String SQL_SELECT = loadSql("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_SelectPortafolio.sql");
    private static final String SQL_SELECT_ALL = loadSql("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_SelectPortafolioAll.sql");
    private static final String SQL_DELETE = loadSql("cl/bcs/portafolio/sql/AdministrarPortafolioDefecto_DeletePortafolio.sql");

    private final HanaUtilService hanaUtil;
    private final JavaCallRestBridge bridge;

    public AdministrarPortafolioDefectoService(HanaUtilService hanaUtil, JavaCallRestBridge bridge) {
        this.hanaUtil = hanaUtil;
        this.bridge = bridge;
    }

    // ── POJOs ────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_Opcion;
        public List<PortafolioItem> p_Portafolios = new ArrayList<>();
    }

    public static class PortafolioItem {
        public Long IdPortafolioConfiguracionDefecto;
        public String CodigoPortafolio;
        public String Proposito;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<RefId> P_OUT_REFID = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class RefId { public Long IdEntidad; public Long IdInterno; }

    // ── Insert row for batch ──────────────────────────────────────────────────
    public static class PortafolioRow {
        public Long IdPortafolioConfiguracionDefecto;
        public String CodigoPortafolio;
        public String Proposito;
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    public JsonObject administrarPortafolioDefecto(Connection conn, JsonObject in) throws Exception {
        Entrada entrada = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();

        boolean requiereAut = hanaUtil.esOperacionConAutorizacion(
            conn, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);

        if ("INSERT".equals(entrada.p_Opcion)) {
            salida = insertar(conn, entrada, salida);
        } else if ("DELETE".equals(entrada.p_Opcion)) {
            salida = eliminar(conn, entrada, salida);
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0) return toJson(salida);

        if (requiereAut) {
            crearEventoAutorizacion(entrada);
        } else {
            agregarLog(conn, entrada);
        }

        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        return toJson(salida);
    }

    // ── INSERT ────────────────────────────────────────────────────────────────

    private Salida insertar(Connection conn, Entrada e, Salida salida) {
        List<Long> secuencia = hanaUtil.getSequence(conn, SEQ_NAME, e.p_Portafolios.size());

        List<PortafolioRow> existentes = query(conn, SQL_SELECT_ALL, Map.of(), fieldMapper(PortafolioRow.class));

        List<PortafolioRow> nuevos = new ArrayList<>();
        for (int i = 0; i < e.p_Portafolios.size(); i++) {
            PortafolioItem item = e.p_Portafolios.get(i);
            Long id = secuencia.get(i);
            item.IdPortafolioConfiguracionDefecto = id;

            boolean existe = existentes.stream()
                .anyMatch(ex -> ex.CodigoPortafolio.equals(item.CodigoPortafolio));

            if (existe) {
                salida.P_OUT_MENSAJES.add(error("CODIGO_PORTAFOLIO_YA_EXISTE",
                    "CodigoPortafolio=" + item.CodigoPortafolio));
            } else {
                PortafolioRow row = new PortafolioRow();
                row.IdPortafolioConfiguracionDefecto = id;
                row.CodigoPortafolio = item.CodigoPortafolio;
                row.Proposito = item.Proposito;
                nuevos.add(row);
            }
        }

        if (!nuevos.isEmpty()) {
            SqlParameterSource[] sources = nuevos.stream()
                .map(r -> new MapSqlParameterSource()
                    .addValue("IdPortafolioConfiguracionDefecto", r.IdPortafolioConfiguracionDefecto)
                    .addValue("CodigoPortafolio", r.CodigoPortafolio)
                    .addValue("Proposito", r.Proposito))
                .toArray(SqlParameterSource[]::new);
            batch(conn, SQL_INSERT, sources);
            salida.P_OUT_MENSAJES.add(ok("INSERCION_PORTAFOLIO_MANT_EXITOSA"));

            for (int i = 0; i < nuevos.size(); i++) {
                RefId ref = new RefId();
                ref.IdEntidad = nuevos.get(i).IdPortafolioConfiguracionDefecto;
                ref.IdInterno = (long) i;
                salida.P_OUT_REFID.add(ref);
            }
        }
        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        return salida;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    private Salida eliminar(Connection conn, Entrada e, Salida salida) {
        List<String> codigos = e.p_Portafolios.stream()
            .map(p -> p.CodigoPortafolio).collect(Collectors.toList());

        List<PortafolioRow> existentes = query(conn, SQL_SELECT,
            Map.of("CodigosPortafolio", codigos),
            fieldMapper(PortafolioRow.class));

        for (PortafolioItem item : e.p_Portafolios) {
            boolean existe = existentes.stream()
                .anyMatch(ex -> ex.CodigoPortafolio.equals(item.CodigoPortafolio));
            if (!existe) {
                salida.P_OUT_MENSAJES.add(error("CODIGO_PORTAFOLIO_NO_EXISTE",
                    "CodigoPortafolio=" + item.CodigoPortafolio));
            } else {
                salida.P_OUT_MENSAJES.add(ok("ELIMINACION_PORTAFOLIO_MANT_EXITOSA",
                    "CodigoPortafolio=" + item.CodigoPortafolio));
            }
        }

        if (!existentes.isEmpty()) {
            SqlParameterSource[] sources = existentes.stream()
                .map(r -> new MapSqlParameterSource()
                    .addValue("IdPortafolioConfiguracionDefecto", r.IdPortafolioConfiguracionDefecto))
                .toArray(SqlParameterSource[]::new);
            batch(conn, SQL_DELETE, sources);
        }

        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        return salida;
    }

    // ── Cross-service calls ───────────────────────────────────────────────────

    private void crearEventoAutorizacion(Entrada e) {
        JsonObject params = new Gson().fromJson(GSON.toJson(Map.of(
            "P_IDEMPRESA", e.p_IdEmpresa,
            "P_IDUSUARIO", e.p_IdUsuario,
            "P_IDMODULO", e.p_IdModulo,
            "P_USAREGLASNEGOCIO", "N"
        )), JsonObject.class);
        try {
            bridge.callPlataforma("cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo", params);
        } catch (Exception ex) {
            log.warn("CrearEventoAutorizacionMasivo failed (non-fatal): {}", ex.getMessage());
        }
    }

    private void agregarLog(Connection conn, Entrada e) {
        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);
        JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
            "p_IdEmpresa", e.p_IdEmpresa,
            "p_IdUsuario", e.p_IdUsuario,
            "p_IdModulo", e.p_IdModulo,
            "p_FechaSistema", fechaSistema != null ? fechaSistema : "",
            "p_Opcion", e.p_Opcion,
            "p_Portafolios", e.p_Portafolios
        )), JsonObject.class);
        try {
            bridge.callPlataforma("cl.bcs.plataforma.pla.AgregarLog", params);
        } catch (Exception ex) {
            log.warn("AgregarLog failed (non-fatal): {}", ex.getMessage());
        }
    }

    private static JsonObject toJson(Object o) {
        return new Gson().fromJson(new Gson().toJson(o), JsonObject.class);
    }
}
