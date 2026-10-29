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
 * Migrates cl.bcs.portafolio.ModificarPortafolio.
 *
 * Flow: validate portfolio exists → validate plantilla límites → get sequence →
 * build RegistroSolicitudCambio → RestClient CrearRegistroSolicitudCambio →
 * check requiresAuth → (auth path | confirm path with LogGenerarContratos).
 */
@Service
public class ModificarPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(ModificarPortafolioService.class);
    private static final Gson GSON = new Gson();

    private static final String ID_CONCEPTO = "MODPOR";

    private static final String SQL_COUNT_PORT = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Count_Portafolio.sql");
    private static final String SQL_SELECT_CLI = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Select_ClienteCodigo.sql");
    private static final String SQL_COUNT_PLANTILLA = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Count_PlantillaLimitesPorDefecto.sql");
    private static final String SQL_COUNT_CLI_PLANTILLA = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Count_ClientePlantillaPorDefecto.sql");
    private static final String SQL_SELECT_ID_SOL = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Select_IdSolicitudCambio.sql");
    private static final String SQL_SELECT_VALOR = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Select_ValorAntiguo.sql");
    private static final String SQL_SELECT_GLOSA = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Select_GlosaCliente.sql");
    private static final String SQL_INSERT_LOG = loadSql("cl/bcs/portafolio/sql/ModificarPortafolio_Insert_LogGenerarContratos.sql");

    private final HanaUtilService hanaUtil;
    private final JavaCallRestBridge bridge;

    public ModificarPortafolioService(HanaUtilService hanaUtil, JavaCallRestBridge bridge) {
        this.hanaUtil = hanaUtil;
        this.bridge = bridge;
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public Long p_IdCliente;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdPortafolio;
        public String p_IdCanal;
        public List<RegistroCambio> p_RegistroCambio = new ArrayList<>();
    }

    public static class RegistroCambio {
        public String CampoModificado;
        public String ValorNuevo;
    }

    public static class Salida {
        public Long P_OUT_REFID;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<DetalleCambio> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<DetalleCambio> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    public static class DetalleCambio {
        public String Codigo;
        public String Descripcion;
        public String RequiereAutorizacion;
    }

    public static class ContadorRow { public Long Contador; }
    public static class ClienteCodigoRow { public Long IdCliente; public String CodigoPortafolio; }
    public static class IdSolRow { public Long IdSolicitudCambio; }
    public static class ValorAntiguoRow { public String TipoDato; public String NombreColumna; public String CampoModificado; public String ValorNuevo; public String ValorAntiguo; }
    public static class GlosaRow { public String Glosa; }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject modificarPortafolio(Connection conn, JsonObject in) throws Exception {
        Entrada e = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();
        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);

        // 1. Validate portfolio exists
        List<ContadorRow> cnt = query(conn, SQL_COUNT_PORT,
            Map.of("IdPortafolio", e.p_IdPortafolio), fieldMapper(ContadorRow.class));
        if (cnt.isEmpty() || cnt.get(0).Contador == 0) {
            salida.P_OUT_ERRORESCONTADOR = 1L;
            DetalleCambio d = new DetalleCambio(); d.Codigo = "ENTIDAD_NO_EXISTE"; d.Descripcion = "El portafolio no existe";
            salida.P_OUT_ERRORESDETALLE.add(d);
            return toJson(salida);
        }

        List<ClienteCodigoRow> cc = query(conn, SQL_SELECT_CLI,
            Map.of("IdPortafolio", e.p_IdPortafolio), fieldMapper(ClienteCodigoRow.class));
        Long idEntidadBase = cc.get(0).IdCliente;
        String codigoPortafolio = cc.get(0).CodigoPortafolio;

        // 2. Validate plantilla límites
        for (RegistroCambio cambio : e.p_RegistroCambio) {
            if ("IdPerfilLimites".equals(cambio.CampoModificado)) {
                List<ContadorRow> cntPl = query(conn, SQL_COUNT_PLANTILLA,
                    Map.of("ValorNuevo", cambio.ValorNuevo), fieldMapper(ContadorRow.class));
                if (!cntPl.isEmpty() && cntPl.get(0).Contador > 0) {
                    List<ContadorRow> cntCli = query(conn, SQL_COUNT_CLI_PLANTILLA,
                        Map.of("IdCliente", idEntidadBase, "IdPortafolio", e.p_IdPortafolio),
                        fieldMapper(ContadorRow.class));
                    if (!cntCli.isEmpty() && cntCli.get(0).Contador > 0) {
                        salida.P_OUT_ERRORESCONTADOR = 1L;
                        DetalleCambio d = new DetalleCambio();
                        d.Codigo = "CLIENTE_YA_TIENE_PLANTILLA_LIMITE_POR_DEFECTO";
                        d.Descripcion = "El cliente ya tiene un portafolio con plantilla límite por defecto";
                        salida.P_OUT_ERRORESDETALLE.add(d);
                        return toJson(salida);
                    }
                }
            }
        }

        // 3. Get IdSolicitudCambio sequence
        List<IdSolRow> seqRows = query(conn, SQL_SELECT_ID_SOL, Map.of(), fieldMapper(IdSolRow.class));
        if (seqRows.isEmpty() || seqRows.get(0).IdSolicitudCambio == null) {
            salida.P_OUT_ERRORESCONTADOR = 1L;
            DetalleCambio d = new DetalleCambio(); d.Codigo = "ERROR_SECUENCIA"; d.Descripcion = "No se pudo obtener la secuencia";
            salida.P_OUT_ERRORESDETALLE.add(d);
            return toJson(salida);
        }
        Long idSolicitud = seqRows.get(0).IdSolicitudCambio;

        // 4. Build RegistroSolicitudCambio list
        List<Map<String,Object>> registros = new ArrayList<>();
        String entidadOrigen = String.valueOf(e.p_IdPortafolio);
        for (RegistroCambio cambio : e.p_RegistroCambio) {
            List<ValorAntiguoRow> vals = query(conn, SQL_SELECT_VALOR,
                Map.of("IdPortafolio", e.p_IdPortafolio,
                       "CampoModificado", cambio.CampoModificado,
                       "ValorNuevo", cambio.ValorNuevo),
                fieldMapper(ValorAntiguoRow.class));
            if (!vals.isEmpty()) {
                registros.add(Map.of(
                    "IdSolicitudCambio", idSolicitud,
                    "IdTipoEntidad", "PORTAFOLIO",
                    "IdEntidadOrigen", entidadOrigen,
                    "CampoModificado", cambio.CampoModificado,
                    "TipoDato", vals.get(0).TipoDato != null ? vals.get(0).TipoDato : "",
                    "ValorAntiguo", vals.get(0).ValorAntiguo != null ? vals.get(0).ValorAntiguo : "",
                    "ValorNuevo", cambio.ValorNuevo != null ? cambio.ValorNuevo : "",
                    "IdEstadoAutorizacion", "PEN"
                ));
            }
        }

        // 5. Call CrearRegistroSolicitudCambio via RestClient
        if (!registros.isEmpty()) {
            JsonObject crParams = GSON.fromJson(GSON.toJson(Map.of("p_RegistroSolicitudCambio", registros)), JsonObject.class);
            try {
                bridge.callPlataforma("cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio", crParams);
            } catch (Exception ex) {
                log.warn("CrearRegistroSolicitudCambio failed: {}", ex.getMessage());
            }
        }

        // 6. Check authorization requirement
        String requiereAut = hanaUtil.getRequiereAutorizacion(conn, e.p_IdEmpresa, ID_CONCEPTO);

        if ("S".equals(requiereAut)) {
            // Auth path: get glosa, call CrearEventoAutorizacion
            List<GlosaRow> glosas = query(conn, SQL_SELECT_GLOSA,
                Map.of("IdCliente", idEntidadBase, "CodigoPortafolio", codigoPortafolio),
                fieldMapper(GlosaRow.class));
            String glosa = glosas.isEmpty() ? "" : glosas.get(0).Glosa;

            JsonObject evParams = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa,
                "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo,
                "p_UsaReglasNegocio", "N",
                "p_IdTipoEvento", ID_CONCEPTO,
                "p_IdConcepto", ID_CONCEPTO,
                "p_NumeroOperacionConcepto", entidadOrigen,
                "p_IdCliente", idEntidadBase,
                "p_Glosa", glosa,
                "p_Fecha", fechaSistema != null ? fechaSistema.substring(0, 10) : ""
            )), JsonObject.class);
            try {
                JsonObject resp = bridge.callPlataforma("cl.bcs.plataforma.aut.CrearEventoAutorizacion", evParams);
                Long errores = resp.has("p_out_ErroresContador") ? resp.get("p_out_ErroresContador").getAsLong() : 0L;
                salida.P_OUT_ERRORESCONTADOR += errores;
                if (errores == 0) {
                    DetalleCambio adv = new DetalleCambio();
                    adv.Codigo = "REQUIERE_AUTORIZACION"; adv.RequiereAutorizacion = "S";
                    adv.Descripcion = "La modificación requiere autorización";
                    salida.P_OUT_ADVERTENCIASDETALLE.add(adv);
                    salida.P_OUT_ADVERTENCIASCONTADOR++;
                }
            } catch (Exception ex) {
                log.error("CrearEventoAutorizacion failed", ex);
                salida.P_OUT_ERRORESCONTADOR++;
                DetalleCambio d = new DetalleCambio();
                d.Codigo = "ERROR_CREAR_EVENTO_AUTORIZACION"; d.Descripcion = ex.getMessage();
                salida.P_OUT_ERRORESDETALLE.add(d);
            }
        } else {
            // No-auth path: INSERT LogGenerarContratos
            SqlParameterSource[] logSources = e.p_RegistroCambio.stream()
                .map(cambio -> new MapSqlParameterSource()
                    .addValue("CampoModificado", cambio.CampoModificado)
                    .addValue("IdEmpresa", e.p_IdEmpresa)
                    .addValue("IdCliente", idEntidadBase)
                    .addValue("IdUsuario", e.p_IdUsuario)
                    .addValue("FechaHoraSistema", fechaSistema != null ? fechaSistema : ""))
                .toArray(SqlParameterSource[]::new);
            if (logSources.length > 0) {
                batch(conn, SQL_INSERT_LOG, logSources);
            }
        }

        salida.P_OUT_REFID = idSolicitud;
        return toJson(salida);
    }

    private static JsonObject toJson(Object o) {
        return new Gson().fromJson(new Gson().toJson(o), JsonObject.class);
    }
}
