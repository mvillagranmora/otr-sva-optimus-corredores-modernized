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

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;
import static cl.bcs.portafolio.javacall.JavaCallInfra.MensajeSalida.*;

/**
 * Migrates cl.bcs.portafolio.CrearCuentaCustodio.
 *
 * Flow: getRequiereAutorizacion → getSequence → INSERT → (auth path | confirm path).
 * Confirm path: UPDATE IdEstadoAutorizacion=AUT → if portafolio is APV → RestClient ReplicaPortafolioAPV.
 * Auth path: RestClient CrearEventoAutorizacion.
 */
@Service
public class CrearCuentaCustodioService {

    private static final Logger log = LoggerFactory.getLogger(CrearCuentaCustodioService.class);
    private static final Gson GSON = new Gson();

    private static final String ID_CONCEPTO = "NPCCUS";
    private static final String PROPOSITO_APV = "PROPOSITOAPV";
    private static final String SEQ_NAME = "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio";

    private static final String SQL_INSERT = loadSql("cl/bcs/portafolio/sql/CrearCuentaCustodio_InsertCuentaCustodio.sql");
    private static final String SQL_UPDATE = loadSql("cl/bcs/portafolio/sql/CrearCuentaCustodio_UpdateEstadoAutorizacion.sql");
    private static final String SQL_SELECT_POR = loadSql("cl/bcs/portafolio/sql/CrearCuentaCustodio_SelectPortafolio.sql");

    private final HanaUtilService hanaUtil;
    private final JavaCallRestBridge bridge;

    public CrearCuentaCustodioService(HanaUtilService hanaUtil, JavaCallRestBridge bridge) {
        this.hanaUtil = hanaUtil;
        this.bridge = bridge;
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdPortafolio;
        public String p_IdBoveda;
        public String p_Codigo;
        public String p_IdEstado;
        public String p_IdTipoCuentaCustodia;
        public String p_IdTipoCustodia;
        public String p_IdModeloCustodia;
        public Long p_IdPersonaBancoCustodio;
        public Long p_IdPersonaBancoLiquidador;
        public String p_LiquidaDividendos;
        public String p_LiquidaCupones;
        public String p_LiquidaVencimientos;
        public String p_LiquidaPrepagos;
    }

    public static class Salida {
        public Long P_OUT_REFID = 0L;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class PortafolioRow { public Long IdPortafolio; public String Proposito; }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject crearCuentaCustodio(Connection conn, JsonObject in) throws Exception {
        Entrada e = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();

        // 1. Check if authorization is required
        String requiereAut = hanaUtil.getRequiereAutorizacion(conn, e.p_IdEmpresa, ID_CONCEPTO);
        String idEstadoAut = "S".equals(requiereAut) ? "PEN" : "AUT";

        // 2. Get sequence
        List<Long> seq = hanaUtil.getSequence(conn, SEQ_NAME, 1);
        Long idCuenta = seq.get(0);
        salida.P_OUT_REFID = idCuenta;

        // 3. Get system date
        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);
        if (fechaSistema == null) fechaSistema = java.time.LocalDateTime.now().toString();

        // 4. INSERT
        SqlParameterSource[] sources = new SqlParameterSource[]{
            new MapSqlParameterSource()
                .addValue("IdCuentaCustodioPortafolio", idCuenta)
                .addValue("IdPortafolio", e.p_IdPortafolio)
                .addValue("IdBoveda", e.p_IdBoveda)
                .addValue("Codigo", e.p_Codigo)
                .addValue("FechaIngreso", fechaSistema)
                .addValue("FechaCreacion", fechaSistema)
                .addValue("FechaModificacion", fechaSistema)
                .addValue("IdUsuarioCreador", e.p_IdUsuario)
                .addValue("IdEstado", e.p_IdEstado)
                .addValue("IdEstadoAutorizacion", idEstadoAut)
                .addValue("IdTipoCuentaCustodia", nvl(e.p_IdTipoCuentaCustodia, ""))
                .addValue("IdTipoCustodia", nvl(e.p_IdTipoCustodia, ""))
                .addValue("IdModeloCustodia", nvl(e.p_IdModeloCustodia, ""))
                .addValue("IdPersonaBancoCustodio", nvl(e.p_IdPersonaBancoCustodio, 0L))
                .addValue("IdPersonaBancoLiquidador", nvl(e.p_IdPersonaBancoLiquidador, 0L))
                .addValue("LiquidaDividendos", nvl(e.p_LiquidaDividendos, "N"))
                .addValue("LiquidaCupones", nvl(e.p_LiquidaCupones, "N"))
                .addValue("LiquidaVencimientos", nvl(e.p_LiquidaVencimientos, "N"))
                .addValue("LiquidaPrepagos", nvl(e.p_LiquidaPrepagos, "N"))
        };
        batch(conn, SQL_INSERT, sources);

        // 5. Auth or confirm path
        if ("S".equals(requiereAut)) {
            crearEventoAutorizacion(e, idCuenta, fechaSistema, salida);
        } else {
            // UPDATE IdEstadoAutorizacion = AUT (already set, but explicit update for clarity)
            SqlParameterSource[] upd = new SqlParameterSource[]{
                new MapSqlParameterSource()
                    .addValue("IdCuentaCustodioPortafolio", idCuenta)
                    .addValue("IdEstadoAutorizacion", "AUT")
            };
            batch(conn, SQL_UPDATE, upd);

            // Check if APV portafolio → call ReplicaPortafolioAPV
            List<PortafolioRow> portafolios = query(conn, SQL_SELECT_POR,
                Map.of("IdPortafolio", e.p_IdPortafolio),
                fieldMapper(PortafolioRow.class));
            if (!portafolios.isEmpty() && PROPOSITO_APV.equals(portafolios.get(0).Proposito)) {
                replicarPortafolioAPV(e, idCuenta, salida);
            }
        }

        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        return new Gson().fromJson(GSON.toJson(salida), JsonObject.class);
    }

    // ── Cross-service ─────────────────────────────────────────────────────────

    private void crearEventoAutorizacion(Entrada e, Long idCuenta, String fechaSistema, Salida salida) {
        JsonObject params = GSON.fromJson(GSON.toJson(Map.ofEntries(
            Map.entry("p_IdEmpresa", e.p_IdEmpresa),
            Map.entry("p_IdUsuario", e.p_IdUsuario),
            Map.entry("p_IdModulo", e.p_IdModulo),
            Map.entry("p_UsaReglasNegocio", "N"),
            Map.entry("p_IdTipoEvento", ID_CONCEPTO),
            Map.entry("p_IdModuloSolicita", e.p_IdModulo),
            Map.entry("p_IdUsuarioSolicita", e.p_IdUsuario),
            Map.entry("p_Fecha", fechaSistema),
            Map.entry("p_IdConcepto", ID_CONCEPTO),
            Map.entry("p_NumeroOperacionConcepto", String.valueOf(idCuenta)),
            Map.entry("p_FechaOperacionConcepto", fechaSistema),
            Map.entry("p_IdCliente", 0L),
            Map.entry("p_Glosa", "Cuenta Custodio: " + e.p_Codigo)
        )), JsonObject.class);
        try {
            JsonObject resp = bridge.callPlataforma("cl.bcs.plataforma.aut.CrearEventoAutorizacion", params);
            Long errores = resp.has("p_out_ErroresContador") ? resp.get("p_out_ErroresContador").getAsLong() : 0L;
            if (errores > 0) {
                salida.P_OUT_ERRORESCONTADOR += errores;
                salida.P_OUT_MENSAJES.add(MensajeSalida.error("ERROR_CREAR_EVENTO_AUTORIZACION",
                    "Errores en CrearEventoAutorizacion: " + errores));
            } else {
                salida.P_OUT_MENSAJES.add(MensajeSalida.ok("REQUIERE_AUTORIZACION"));
            }
        } catch (Exception ex) {
            log.error("CrearEventoAutorizacion failed", ex);
            salida.P_OUT_MENSAJES.add(MensajeSalida.error("ERROR_CREAR_EVENTO_AUTORIZACION", ex.getMessage()));
            salida.P_OUT_ERRORESCONTADOR++;
        }
    }

    private void replicarPortafolioAPV(Entrada e, Long idCuenta, Salida salida) {
        JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
            "p_IdEmpresa", e.p_IdEmpresa,
            "p_IdUsuario", e.p_IdUsuario,
            "p_IdModulo", e.p_IdModulo,
            "p_UsaReglasNegocio", e.p_UsaReglasNegocio,
            "p_Portafolio", e.p_IdPortafolio,
            "p_IdEntidad", idCuenta,
            "p_TipoEntidad", "CUENTACUSTODIO",
            "p_TipoOperacion", "I"
        )), JsonObject.class);
        try {
            JsonObject resp = bridge.callReplica("cl.bcs.APV.ReplicaPortafolioAPV", params);
            Long errores = resp.has("P_OUT_ERRORESCONTADOR") ? resp.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
            if (errores > 0) {
                log.warn("ReplicaPortafolioAPV returned {} errors", errores);
                salida.P_OUT_MENSAJES.add(MensajeSalida.error("ERROR_REPLICA_PORTAFOLIO_APV",
                    "Errores en ReplicaPortafolioAPV: " + errores));
                salida.P_OUT_ERRORESCONTADOR += errores;
            }
        } catch (Exception ex) {
            log.warn("ReplicaPortafolioAPV failed (non-fatal): {}", ex.getMessage());
        }
    }

    private static <T> T nvl(T value, T def) { return value != null ? value : def; }
}
