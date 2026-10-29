package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cl.bcs.portafolio.javacall.JavaCallInfra.MensajeSalida.*;

/**
 * Migrates cl.bcs.portafolio.CrearPortafolio.
 *
 * Delegates to HANA SP "BSCL.Portafolio.BD.Servicios::CrearPortafolio" via SimpleJdbcCall.
 * Post-SP orchestration: GestionCuentaInversion + CrearEventoAutorizacion or
 * ConfirmarCrearPortafolio + ReplicaPortafolioAPV + AgregarLog — all via RestClient or
 * additional SimpleJdbcCall on the same DataSource.
 */
@Service
public class CrearPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(CrearPortafolioService.class);
    private static final Gson GSON = new Gson();

    private final HanaUtilService hanaUtil;
    private final JavaCallRestBridge bridge;

    public CrearPortafolioService(HanaUtilService hanaUtil, JavaCallRestBridge bridge) {
        this.hanaUtil = hanaUtil;
        this.bridge = bridge;
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_CodigoPortafolio = "";
        public String p_DescripcionPortafolio = "Portafolio Default";
        public String p_PropositoPortafolio = "PROPGENERALES";
        public Long p_IdCuentaOrigen;
        public String p_CodigoRegimen = "";
        public String p_NombrePlan = "";
        public String p_CodigoAgrupador = "";
        public Long p_IdCliente;
        public String p_IdPerfilComercialBase;
        public Long p_IdPersonaBrokerExtranjero = 0L;
        public String p_Estado = "ACT";
        public String p_AreaCliente = "";
        public String p_DesdeEnrolamiento = "";
        public String p_IdPlantillaProducto;
        public String p_IdPlantillaCobro;
        public Long p_IdEjecutivoPortafolio = 0L;
        public Long p_IdEjecutivoComercial = 0L;
    }

    public static class Salida {
        public Long p_out_RefId = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<DetalleSalida> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<DetalleSalida> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    public static class DetalleSalida {
        public String Codigo;
        public String Descripcion;
        public String RequiereAutorizacion;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject crearPortafolio(Connection conn, JsonObject in) throws Exception {
        Entrada e = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = logica(conn, e);
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    /**
     * Called directly from CrearPortafolioConReplicaService (same JVM).
     */
    public Salida logica(Connection conn, Entrada e) {
        Salida salida = new Salida();
        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);

        // ── 1. Call main SP (HIGH-3: use same conn to share the transaction) ──
        SimpleJdbcCall call = new SimpleJdbcCall(new SingleConnectionDataSource(conn, true))
            .withSchemaName("BSCL")
            .withProcedureName("Portafolio.BD.Servicios::CrearPortafolio")
            .withoutProcedureColumnMetaDataAccess();

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("p_IdEmpresa", e.p_IdEmpresa)
            .addValue("p_IdUsuario", e.p_IdUsuario)
            .addValue("p_IdModulo", e.p_IdModulo)
            .addValue("p_UsaReglasNegocio", "N")
            .addValue("p_IdCliente", e.p_IdCliente)
            .addValue("p_IdPerfilComercialBase", e.p_IdPerfilComercialBase)
            .addValue("p_IdPersonaBrokerExtranjero", e.p_IdPersonaBrokerExtranjero)
            .addValue("p_PropositoPortafolio", e.p_PropositoPortafolio)
            .addValue("p_IdCuentaOrigen", e.p_IdCuentaOrigen)
            .addValue("p_DescripcionPortafolio", e.p_DescripcionPortafolio)
            .addValue("p_CodigoPortafolio", e.p_CodigoPortafolio)
            .addValue("p_Estado", e.p_Estado)
            .addValue("p_AreaCliente", e.p_AreaCliente)
            .addValue("p_DesdeEnrolamiento", e.p_DesdeEnrolamiento);

        try {
            Map<String, Object> result = call.execute(params);

            Object refId = result.get("p_out_RefId");
            salida.p_out_RefId = refId instanceof Number ? ((Number) refId).longValue() : 0L;

            Object errCnt = result.get("p_out_ErroresContador");
            salida.P_OUT_ERRORESCONTADOR = errCnt instanceof Number ? ((Number) errCnt).longValue() : 0L;

            Object advCnt = result.get("p_out_AdvertenciasContador");
            salida.P_OUT_ADVERTENCIASCONTADOR = advCnt instanceof Number ? ((Number) advCnt).longValue() : 0L;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errores = (List<Map<String, Object>>) result.get("p_out_ErroresDetalle");
            if (errores != null) errores.forEach(m -> {
                DetalleSalida d = new DetalleSalida();
                d.Codigo = String.valueOf(m.get("Codigo"));
                d.Descripcion = String.valueOf(m.get("Descripcion"));
                salida.P_OUT_ERRORESDETALLE.add(d);
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> advertencias = (List<Map<String, Object>>) result.get("p_out_AdvertenciasDetalle");
            if (advertencias != null) advertencias.forEach(m -> {
                DetalleSalida d = new DetalleSalida();
                d.Codigo = String.valueOf(m.get("Codigo"));
                d.Descripcion = String.valueOf(m.get("Descripcion"));
                d.RequiereAutorizacion = String.valueOf(m.getOrDefault("RequiereAutorizacion", "N"));
                salida.P_OUT_ADVERTENCIASDETALLE.add(d);
            });

        } catch (Exception ex) {
            log.error("Error executing SP CrearPortafolio", ex);
            salida.P_OUT_ERRORESCONTADOR = 1L;
            DetalleSalida d = new DetalleSalida();
            d.Codigo = "ERROR_SP_CREAR_PORTAFOLIO"; d.Descripcion = ex.getMessage();
            salida.P_OUT_ERRORESDETALLE.add(d);
            return salida;
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0 || salida.p_out_RefId == 0L) return salida;

        Long idPortafolio = salida.p_out_RefId;

        // ── 2. GestionCuentaInversion (cross-service: BcsCuentaInversionServicios) ──
        llamarGestionCuentaInversion(e, idPortafolio, salida);
        if (salida.P_OUT_ERRORESCONTADOR > 0) return salida;

        // ── 3. Auth or confirm path ────────────────────────────────────────────
        String requiereAut = hanaUtil.getRequiereAutorizacion(conn, e.p_IdEmpresa, "NUEPOR");
        if ("S".equals(requiereAut)) {
            llamarCrearEventoAutorizacion(e, idPortafolio, fechaSistema, salida);
        } else {
            llamarConfirmarCrearPortafolio(conn, e, idPortafolio, salida);
            llamarReplicaPortafolioAPV(e, idPortafolio, salida);
            llamarAgregarLog(e, idPortafolio, fechaSistema, salida);
        }

        return salida;
    }

    // ── Cross-service delegations ─────────────────────────────────────────────

    private void llamarGestionCuentaInversion(Entrada e, Long idPortafolio, Salida salida) {
        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa,
                "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo,
                "p_IdPortafolio", idPortafolio
            )), JsonObject.class);
            JsonObject resp = bridge.callPlataforma("cl.bcs.cuentaInversion.GestionCuentaInversion", params);
            Long errores = resp.has("P_OUT_ERRORESCONTADOR") ? resp.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
            if (errores > 0) {
                salida.P_OUT_ERRORESCONTADOR += errores;
                DetalleSalida d = new DetalleSalida();
                d.Codigo = "ERROR_GESTION_CUENTA_INVERSION"; d.Descripcion = "Errores en GestionCuentaInversion";
                salida.P_OUT_ERRORESDETALLE.add(d);
            }
        } catch (Exception ex) {
            log.warn("GestionCuentaInversion failed (non-fatal): {}", ex.getMessage());
        }
    }

    private void llamarCrearEventoAutorizacion(Entrada e, Long idPortafolio, String fechaSistema, Salida salida) {
        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa,
                "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo,
                "p_UsaReglasNegocio", "N",
                "p_IdConcepto", "NUEPOR",
                "p_NumeroOperacionConcepto", String.valueOf(idPortafolio),
                "p_IdCliente", e.p_IdCliente,
                "p_Fecha", fechaSistema != null ? fechaSistema.substring(0, Math.min(10, fechaSistema.length())) : ""
            )), JsonObject.class);
            JsonObject resp = bridge.callPlataforma("cl.bcs.plataforma.aut.CrearEventoAutorizacion", params);
            Long errores = resp.has("p_out_ErroresContador") ? resp.get("p_out_ErroresContador").getAsLong() : 0L;
            salida.P_OUT_ERRORESCONTADOR += errores;
            if (errores == 0) {
                DetalleSalida d = new DetalleSalida(); d.Codigo = "REQUIERE_AUTORIZACION"; d.RequiereAutorizacion = "S";
                d.Descripcion = "La creación del portafolio requiere autorización";
                salida.P_OUT_ADVERTENCIASDETALLE.add(d);
                salida.P_OUT_ADVERTENCIASCONTADOR++;
            }
        } catch (Exception ex) {
            log.error("CrearEventoAutorizacion failed", ex);
            salida.P_OUT_ERRORESCONTADOR++;
            DetalleSalida d = new DetalleSalida(); d.Codigo = "ERROR_CREAR_EVENTO_AUTORIZACION"; d.Descripcion = ex.getMessage();
            salida.P_OUT_ERRORESDETALLE.add(d);
        }
    }

    private void llamarConfirmarCrearPortafolio(Connection conn, Entrada e, Long idPortafolio, Salida salida) {
        SimpleJdbcCall confirmCall = new SimpleJdbcCall(new SingleConnectionDataSource(conn, true))
            .withSchemaName("BSCL")
            .withProcedureName("Portafolio.BD.Servicios::ConfirmarCrearPortafolio")
            .withoutProcedureColumnMetaDataAccess();
        try {
            confirmCall.execute(new MapSqlParameterSource()
                .addValue("p_IdEmpresa", e.p_IdEmpresa)
                .addValue("p_IdUsuario", e.p_IdUsuario)
                .addValue("p_IdModulo", e.p_IdModulo)
                .addValue("p_IdPortafolio", idPortafolio));
        } catch (Exception ex) {
            log.warn("ConfirmarCrearPortafolio failed (non-fatal): {}", ex.getMessage());
        }
    }

    private void llamarReplicaPortafolioAPV(Entrada e, Long idPortafolio, Salida salida) {
        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa,
                "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo,
                "p_UsaReglasNegocio", e.p_UsaReglasNegocio != null ? e.p_UsaReglasNegocio : "N",
                "p_Portafolio", idPortafolio,
                "p_IdEntidad", idPortafolio,
                "p_TipoEntidad", "PORTAFOLIO",
                "p_TipoOperacion", "I"
            )), JsonObject.class);
            bridge.callReplica("cl.bcs.APV.ReplicaPortafolioAPV", params);
        } catch (Exception ex) {
            log.warn("ReplicaPortafolioAPV failed (non-fatal): {}", ex.getMessage());
        }
    }

    private void llamarAgregarLog(Entrada e, Long idPortafolio, String fechaSistema, Salida salida) {
        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa,
                "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo,
                "p_IdPortafolio", idPortafolio,
                "p_FechaSistema", fechaSistema != null ? fechaSistema : ""
            )), JsonObject.class);
            bridge.callPlataforma("cl.bcs.plataforma.pla.AgregarLog", params);
        } catch (Exception ex) {
            log.warn("AgregarLog failed (non-fatal): {}", ex.getMessage());
        }
    }
}
