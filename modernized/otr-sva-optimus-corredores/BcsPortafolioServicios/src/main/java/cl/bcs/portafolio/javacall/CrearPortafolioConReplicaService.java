package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;

/**
 * Migrates cl.bcs.portafolio.CrearPortafolioConReplica.
 *
 * Flow:
 * 1. Call CrearPortafolio (same JVM, via CrearPortafolioService)
 * 2. If p_ReplicaPortafolio is set, query origin portafolio data via SQL
 * 3. Replicate: configuracion (via ModificarPortafolioService), preferencias negocio
 *    (via RestClient → ModificarIndicador), cuenta custodio (via RestClient → CrearCuentaCustodioMasivo),
 *    preferencias pago (via RestClient → CrearPreferenciaPagoPortafolioMasivo)
 * 4. Register audit log (via RestClient → AgregarLog)
 */
@Service
public class CrearPortafolioConReplicaService {

    private static final Logger log = LoggerFactory.getLogger(CrearPortafolioConReplicaService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_PORTAFOLIO_ORIGEN =
        loadSql("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPortafolio.sql");
    private static final String SQL_PREF_NEGOCIO =
        loadSql("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPreferenciasDeNegocio.sql");
    private static final String SQL_IND_DESTINO =
        loadSql("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarIndicadoresDestino.sql");
    private static final String SQL_PREF_PAGO =
        loadSql("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPreferenciasDePago.sql");
    private static final String SQL_CUENTA_CUSTODIO =
        loadSql("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarCuentaCustodio.sql");

    private final CrearPortafolioService crearPortafolioService;
    private final ModificarPortafolioService modificarPortafolioService;
    private final HanaUtilService hanaUtil;
    private final JavaCallRestBridge bridge;

    public CrearPortafolioConReplicaService(
            CrearPortafolioService crearPortafolioService,
            ModificarPortafolioService modificarPortafolioService,
            HanaUtilService hanaUtil,
            JavaCallRestBridge bridge) {
        this.crearPortafolioService = crearPortafolioService;
        this.modificarPortafolioService = modificarPortafolioService;
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
        public String p_IdPortafolio;
        public String p_ReplicaPortafolio = "";
        public String p_IdPlantillaProductos = "";
        public String p_IdPlantillaCobros = "";
        public Long p_IdEjecutivoPortafolio = 0L;
        public Long p_IdEjecutivoComercial = 0L;
    }

    public static class Salida {
        public Long p_out_RefId = 0L;
        public String p_out_RefMessage = "";
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

    public static class PortafolioOrigenRow {
        public Long IdPortafolio;
        public Long IdDireccionEnvioCorrespondencia;
        public Long IdTelefonoContacto;
        public String IdAreaCliente;
        public Long IdCliente;
        public String IdPlantillaProductos;
        public String IdPlantillaCobros;
        public String PropositoPortafolio;
        public Long IdEjecutivoPortafolio;
        public Long IdEjecutivoComercial;
    }

    public static class PrefNegocioRow { public Long IdIndicador; public Long IdTipoIndicador; public String ValorIndicador; }
    public static class PrefPagoRow {
        public String IdPreferenciaPago; public Long IdCuentaBancaria; public Long IdTarjetaBancaria;
        public String IdEstado; public String PorDefecto; public Long IdPersonaRelacionada; public Long IdCarga;
    }
    public static class CuentaCustodioRow {
        public String IdBoveda; public String Codigo; public String IdTipoCuentaCustodia;
        public String IdTipoCustodia; public String IdModeloCustodia; public Long IdPersonaBancoCustodio;
        public Long IdPersonaBancoLiquidador; public String IdEstado; public String LiquidaVencimientos;
        public String LiquidaCupones; public String LiquidaPrepagos; public String LiquidaDividendos;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject crearPortafolioConReplica(Connection conn, JsonObject in) throws Exception {
        Entrada e = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();

        // 1. Create base portafolio
        CrearPortafolioService.Entrada cpEntrada = mapToCrearPortafolioEntrada(e);
        CrearPortafolioService.Salida cpSalida = crearPortafolioService.logica(conn, cpEntrada);

        if (cpSalida.P_OUT_ERRORESCONTADOR > 0) {
            salida.P_OUT_ERRORESCONTADOR = cpSalida.P_OUT_ERRORESCONTADOR;
            cpSalida.P_OUT_ERRORESDETALLE.forEach(d -> {
                DetalleSalida ds = new DetalleSalida(); ds.Codigo = d.Codigo; ds.Descripcion = d.Descripcion;
                salida.P_OUT_ERRORESDETALLE.add(ds);
            });
            salida.p_out_RefMessage = "Error al crear portafolio";
            return toJson(salida);
        }

        salida.p_out_RefId = cpSalida.p_out_RefId;
        e.p_IdPortafolio = String.valueOf(cpSalida.p_out_RefId);

        if (e.p_ReplicaPortafolio == null || e.p_ReplicaPortafolio.isBlank()) {
            salida.p_out_RefMessage = "Portafolio creado correctamente";
            return toJson(salida);
        }

        // 2. Query origin portafolio data
        List<PortafolioOrigenRow> origenList = query(conn, SQL_PORTAFOLIO_ORIGEN,
            Map.of("CodigoPortafolio", e.p_ReplicaPortafolio.trim(), "IdCliente", e.p_IdCliente),
            fieldMapper(PortafolioOrigenRow.class));

        if (origenList.isEmpty()) {
            log.warn("Portafolio origen {} no encontrado para cliente {}", e.p_ReplicaPortafolio, e.p_IdCliente);
            agregarAdvertencia(salida, "PORTAFOLIO_NO_ENCONTRADO", "Portafolio origen no encontrado");
            salida.p_out_RefMessage = "Portafolio creado con advertencias en la replicación";
            return toJson(salida);
        }

        PortafolioOrigenRow origen = origenList.get(0);
        Long idOrigen = origen.IdPortafolio;

        // 3. Replicate configuracion (ModificarPortafolio cross-service)
        replicarConfiguracion(conn, e, origen, salida);

        // 4. Replicate preferencias negocio (ModificarIndicador cross-service)
        replicarPreferenciasNegocio(conn, e, idOrigen, salida);

        // 5. Replicate cuenta custodio (CrearCuentaCustodioMasivo cross-service)
        replicarCuentaCustodio(conn, e, idOrigen, salida);

        // 6. Replicate preferencias pago (CrearPreferenciaPagoPortafolioMasivo cross-service)
        replicarPreferenciasPago(conn, e, idOrigen, salida);

        // 7. Audit log
        registrarLog(conn, e, salida);

        if (salida.P_OUT_ERRORESCONTADOR > 0) salida.p_out_RefMessage = "Portafolio creado con errores en la replicación";
        else if (salida.P_OUT_ADVERTENCIASCONTADOR > 0) salida.p_out_RefMessage = "Portafolio creado con advertencias en la replicación";
        else salida.p_out_RefMessage = "Portafolio creado correctamente con replicación completa";

        return toJson(salida);
    }

    // ── Replica steps ─────────────────────────────────────────────────────────

    private void replicarConfiguracion(Connection conn, Entrada e, PortafolioOrigenRow origen, Salida salida) {
        ModificarPortafolioService.Entrada mpE = new ModificarPortafolioService.Entrada();
        mpE.p_IdEmpresa = e.p_IdEmpresa; mpE.p_IdUsuario = e.p_IdUsuario;
        mpE.p_IdModulo = e.p_IdModulo; mpE.p_UsaReglasNegocio = e.p_UsaReglasNegocio;
        mpE.p_IdCliente = e.p_IdCliente; mpE.p_IdPortafolio = Long.parseLong(e.p_IdPortafolio);

        mpE.p_RegistroCambio = new ArrayList<>();
        addCambio(mpE.p_RegistroCambio, "IdDireccionEnvioCorrespondencia",
            origen.IdDireccionEnvioCorrespondencia != null ? origen.IdDireccionEnvioCorrespondencia.toString() : "");
        addCambio(mpE.p_RegistroCambio, "IdTelefonoContacto",
            origen.IdTelefonoContacto != null ? origen.IdTelefonoContacto.toString() : "");
        addCambio(mpE.p_RegistroCambio, "AreaCliente",
            origen.IdAreaCliente != null ? origen.IdAreaCliente : "");
        addCambio(mpE.p_RegistroCambio, "IdPerfilProductos",
            (!e.p_IdPlantillaProductos.isBlank()) ? e.p_IdPlantillaProductos :
                (origen.IdPlantillaProductos != null ? origen.IdPlantillaProductos : ""));
        addCambio(mpE.p_RegistroCambio, "IdPerfilCobros",
            (!e.p_IdPlantillaCobros.isBlank()) ? e.p_IdPlantillaCobros :
                (origen.IdPlantillaCobros != null ? origen.IdPlantillaCobros : ""));
        addCambio(mpE.p_RegistroCambio, "Proposito",
            (!e.p_PropositoPortafolio.isBlank()) ? e.p_PropositoPortafolio :
                (origen.PropositoPortafolio != null ? origen.PropositoPortafolio : ""));

        try {
            JsonObject mpIn = GSON.fromJson(GSON.toJson(mpE), JsonObject.class);
            modificarPortafolioService.modificarPortafolio(conn, mpIn);
        } catch (Exception ex) {
            log.error("replicarConfiguracion failed", ex);
            agregarAdvertencia(salida, "REPLICAR_PORTAFOLIO_PARCIAL", "configuracion general");
        }
    }

    private void replicarPreferenciasNegocio(Connection conn, Entrada e, Long idOrigen, Salida salida) {
        List<PrefNegocioRow> antiguas = query(conn, SQL_PREF_NEGOCIO,
            Map.of("IdEntidad", idOrigen, "IdEmpresa", e.p_IdEmpresa), fieldMapper(PrefNegocioRow.class));
        if (antiguas.isEmpty()) return;

        List<PrefNegocioRow> nuevas = query(conn, SQL_IND_DESTINO,
            Map.of("IdEntidad", Long.parseLong(e.p_IdPortafolio), "IdEmpresa", e.p_IdEmpresa),
            fieldMapper(PrefNegocioRow.class));

        Map<Long, Long> mapaDestino = new java.util.HashMap<>();
        nuevas.forEach(n -> mapaDestino.put(n.IdTipoIndicador, n.IdIndicador));

        List<Map<String, Object>> registros = new ArrayList<>();
        for (PrefNegocioRow ref : antiguas) {
            registros.add(Map.of(
                "IdIndicador", mapaDestino.getOrDefault(ref.IdTipoIndicador, 0L),
                "IdTipoIndicador", ref.IdTipoIndicador,
                "ValorIndicador", ref.ValorIndicador != null ? ref.ValorIndicador : "",
                "FechaVencimiento", "0001-01-01 00:00:00"
            ));
        }

        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa, "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo, "p_UsaReglasNegocio", e.p_UsaReglasNegocio,
                "p_IdTipoEntidad", "PORTAFOLIO",
                "p_IdEntidad", Long.parseLong(e.p_IdPortafolio),
                "p_RegistroAtributo", registros
            )), JsonObject.class);
            bridge.callSocioNegocio("cl.bcs.plataforma.ind.ModificarIndicador", params);
        } catch (Exception ex) {
            log.error("replicarPreferenciasNegocio failed", ex);
            agregarAdvertencia(salida, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de negocio");
        }
    }

    private void replicarCuentaCustodio(Connection conn, Entrada e, Long idOrigen, Salida salida) {
        List<CuentaCustodioRow> custodios = query(conn, SQL_CUENTA_CUSTODIO,
            Map.of("IdPortafolio", idOrigen), fieldMapper(CuentaCustodioRow.class));
        if (custodios.isEmpty()) return;

        List<Map<String, Object>> cuentas = new ArrayList<>();
        for (CuentaCustodioRow cc : custodios) {
            cuentas.add(Map.ofEntries(
                Map.entry("IdPortafolio", Long.parseLong(e.p_IdPortafolio)),
                Map.entry("IdBoveda", cc.IdBoveda),
                Map.entry("Codigo", cc.Codigo),
                Map.entry("IdTipoCuentaCustodia", cc.IdTipoCuentaCustodia),
                Map.entry("IdTipoCustodia", cc.IdTipoCustodia != null ? cc.IdTipoCustodia : ""),
                Map.entry("IdModeloCustodia", cc.IdModeloCustodia != null ? cc.IdModeloCustodia : ""),
                Map.entry("IdPersonaBancoCustodio", cc.IdPersonaBancoCustodio != null ? cc.IdPersonaBancoCustodio : 0L),
                Map.entry("IdPersonaBancoLiquidador", cc.IdPersonaBancoLiquidador != null ? cc.IdPersonaBancoLiquidador : 0L),
                Map.entry("IdEstado", cc.IdEstado),
                Map.entry("LiquidaDividendos", cc.LiquidaDividendos),
                Map.entry("LiquidaCupones", cc.LiquidaCupones),
                Map.entry("LiquidaVencimientos", cc.LiquidaVencimientos),
                Map.entry("LiquidaPrepagos", cc.LiquidaPrepagos)
            ));
        }

        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa, "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo, "p_UsaReglasNegocio", e.p_UsaReglasNegocio,
                "p_CuentasCustodio", cuentas
            )), JsonObject.class);
            bridge.callSocioNegocio("cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo", params);
        } catch (Exception ex) {
            log.error("replicarCuentaCustodio failed", ex);
            agregarAdvertencia(salida, "REPLICAR_PORTAFOLIO_PARCIAL", "cuentas custodio");
        }
    }

    private void replicarPreferenciasPago(Connection conn, Entrada e, Long idOrigen, Salida salida) {
        List<PrefPagoRow> pagos = query(conn, SQL_PREF_PAGO,
            Map.of("IdPortafolio", idOrigen), fieldMapper(PrefPagoRow.class));
        if (pagos.isEmpty()) return;

        List<Map<String, Object>> prefs = new ArrayList<>();
        for (PrefPagoRow p : pagos) {
            prefs.add(Map.of(
                "IdPortafolio", Long.parseLong(e.p_IdPortafolio),
                "IdPreferenciaPago", p.IdPreferenciaPago,
                "IdCuentaBancaria", p.IdCuentaBancaria != null ? p.IdCuentaBancaria : 0L,
                "IdTarjetaBancaria", p.IdTarjetaBancaria != null ? p.IdTarjetaBancaria : 0L,
                "IdEstado", p.IdEstado, "PorDefecto", p.PorDefecto,
                "IdPersonaRelacionada", p.IdPersonaRelacionada != null ? p.IdPersonaRelacionada : 0L,
                "IdCarga", p.IdCarga != null ? p.IdCarga : 0L
            ));
        }

        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa, "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo, "p_UsaReglasNegocio", e.p_UsaReglasNegocio,
                "p_PreferenciasPagoPortafolio", prefs
            )), JsonObject.class);
            bridge.callSocioNegocio("cl.bcs.socionegocio.por.CrearPreferenciaPagoPortafolioMasivo", params);
        } catch (Exception ex) {
            log.error("replicarPreferenciasPago failed", ex);
            agregarAdvertencia(salida, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de pago");
        }
    }

    private void registrarLog(Connection conn, Entrada e, Salida salida) {
        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);
        try {
            JsonObject params = GSON.fromJson(GSON.toJson(Map.of(
                "p_IdEmpresa", e.p_IdEmpresa, "p_IdUsuario", e.p_IdUsuario,
                "p_IdModulo", e.p_IdModulo, "p_IdCliente", e.p_IdCliente,
                "p_IdPortafolio", e.p_IdPortafolio,
                "p_ReplicaPortafolio", e.p_ReplicaPortafolio,
                "p_FechaSistema", fechaSistema != null ? fechaSistema : "",
                "p_IdTipoLog", "CREACION", "p_IdTipoRegistro", "REPLICA_PORTAFOLIO"
            )), JsonObject.class);
            bridge.callPlataforma("cl.bcs.plataforma.pla.AgregarLog", params);
        } catch (Exception ex) {
            log.warn("registrarLog failed (non-fatal): {}", ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CrearPortafolioService.Entrada mapToCrearPortafolioEntrada(Entrada e) {
        CrearPortafolioService.Entrada cp = new CrearPortafolioService.Entrada();
        cp.p_IdEmpresa = e.p_IdEmpresa; cp.p_IdUsuario = e.p_IdUsuario;
        cp.p_IdModulo = e.p_IdModulo; cp.p_UsaReglasNegocio = e.p_UsaReglasNegocio;
        cp.p_CodigoPortafolio = e.p_CodigoPortafolio; cp.p_DescripcionPortafolio = e.p_DescripcionPortafolio;
        cp.p_PropositoPortafolio = e.p_PropositoPortafolio; cp.p_IdCuentaOrigen = e.p_IdCuentaOrigen;
        cp.p_CodigoRegimen = e.p_CodigoRegimen; cp.p_NombrePlan = e.p_NombrePlan;
        cp.p_CodigoAgrupador = e.p_CodigoAgrupador; cp.p_IdCliente = e.p_IdCliente;
        cp.p_IdPerfilComercialBase = e.p_IdPerfilComercialBase; cp.p_Estado = e.p_Estado;
        cp.p_AreaCliente = e.p_AreaCliente; cp.p_DesdeEnrolamiento = e.p_DesdeEnrolamiento;
        cp.p_IdPersonaBrokerExtranjero = e.p_IdPersonaBrokerExtranjero;
        cp.p_IdPlantillaProducto = e.p_IdPlantillaProductos;
        cp.p_IdPlantillaCobro = e.p_IdPlantillaCobros;
        cp.p_IdEjecutivoPortafolio = e.p_IdEjecutivoPortafolio;
        cp.p_IdEjecutivoComercial = e.p_IdEjecutivoComercial;
        return cp;
    }

    private void addCambio(List<ModificarPortafolioService.RegistroCambio> list, String campo, String valor) {
        ModificarPortafolioService.RegistroCambio c = new ModificarPortafolioService.RegistroCambio();
        c.CampoModificado = campo; c.ValorNuevo = valor;
        list.add(c);
    }

    private void agregarAdvertencia(Salida salida, String codigo, String descripcion) {
        DetalleSalida d = new DetalleSalida(); d.Codigo = codigo; d.Descripcion = descripcion;
        salida.P_OUT_ADVERTENCIASDETALLE.add(d);
        salida.P_OUT_ADVERTENCIASCONTADOR++;
    }

    private static JsonObject toJson(Object o) {
        return new Gson().fromJson(new Gson().toJson(o), JsonObject.class);
    }
}
