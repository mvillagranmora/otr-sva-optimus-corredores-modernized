package cl.bcs.tesoreria.javacall.dep;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.FormaDePagoLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.dep.GestionDeDepositos} (TES-B-243..266): manages deposit payrolls or their details.
 * EDP sends to deposit, LIQ liquidates (through LiquidarComprobante or, when every payment form is already liquidated, marks them deposited and updates the
 * bank balances), ANU annuls and releases the comprobante payment forms.
 *
 * <p>LEGACY DEFECT kept (see README TES-B-248): the LIQ branch compares the entity with {@code " TES.DepositoDetalle"} (leading space), so the details never
 * contribute ids to the payment-form lookup.
 * LEGACY DEFECT kept (see README TES-B-259, TES-B-260): DEPOSITO_NO_EXISTE / DETALLE_DEPOSITO_NO_EXISTE are unreachable: the validator dereferences a null id first
 * (NullPointerException).
 * LEGACY DEFECT kept (see README TES-B-244): EDP of a single detail sets the deposit status to NULL (the SQL CASE has no EDP branch).
 * The comprobante / bank-account lists are de-duplicated with {@code contains}, which compares by identity in the legacy classes, so nothing is ever de-duplicated.
 */
@Service
public class GestionDeDepositosService {

    private static final Logger log = LoggerFactory.getLogger(GestionDeDepositosService.class);

    private static final String SQL_INSERT_DEPOSITO_PROCESO = SqlNombrado.recurso("GestionDeDepositos_InsertDepositoProceso");
    private static final String SQL_SELECT_DEPOSITO_PROCESO = SqlNombrado.recurso("GestionDeDepositos_SelectDepositoProceso");
    private static final String SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITO = SqlNombrado.recurso("GestionDeDepositos_UpdateDepositoDetalle_EntidadDeposito");
    private static final String SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITO = SqlNombrado.recurso("GestionDeDepositos_UpdateDeposito_EntidadDeposito");
    private static final String SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DETALLE = SqlNombrado.recurso("GestionDeDepositos_UpdateDepositoDetalle_EntidadDepositoDetalle");
    private static final String SQL_UPDATE_DEPOSITO_ENTIDAD_DETALLE = SqlNombrado.recurso("GestionDeDepositos_UpdateDeposito_EntidadDepositoDetalle");
    private static final String SQL_LIBERAR_COMPROBANTE_FORMA_DE_PAGO = SqlNombrado.recurso("GestionDeDepositos_LiberarComprobanteFormaDePago");
    private static final String SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO = SqlNombrado.recurso("GestionDeDepositos_SelectComprobanteFormaDePago");
    private static final String SQL_UPDATE_COMPROBANTE_FORMA_DE_PAGO = SqlNombrado.recurso("GestionDeDepositos_UpdateComprobanteFormaDePago");

    // ------------------------------------------------------------------------------------------------ request / response (the JavaCall contract)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        /** EDP: send to deposit, LIQ: liquidate, ANU: annul. */
        public String p_TipoEjecucion;
        public List<RegistroDeposito> p_Registros = new ArrayList<>();
    }

    public static class RegistroDeposito {
        public Long Id;
        public String Entidad;
        public String IdConcepto;
        public String Descripcion;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    /** One row of the process query; the SQL statements read their parameters from these public fields by name. */
    public static class RegistroDepositoProceso {
        public Long IdInterno;
        public String TipoEjecucion;
        public Long Id;
        public String Entidad;
        public String IdConcepto;
        public String Descripcion;
        public Long IdDeposito;
        public String IdEstadoDeposito;
        public Long IdDepositoDetalle;
        public String IdEstadoDepositoDetalle;
        public Long IdUsuario;
        public String FechaMovimiento;
        public long errores = 0;
        public long advertencias = 0;
        public long autorizaciones = 0;
        public long ok = 0;
    }

    public record ComprobanteFormaPago(Long IdComprobante, String IdTipoMovimiento, Long IdComprobanteFormaDePago, Long IdCuentaBancariaCorredora,
            String IdEstadoDepositoDetalle, String IdEstadoComprobanteFormaDePago) {
    }

    /** Input row of ActualizarSaldosCuentaBanco (legacy CuentaBanco_Entrada). */
    private record CuentaBanco(Long IdCuentaBanco) {
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ActualizarSaldosCuentaBancoService actualizarSaldos;
    private final Colaboradores colaboradores;

    public GestionDeDepositosService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ActualizarSaldosCuentaBancoService actualizarSaldos,
            Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.actualizarSaldos = actualizarSaldos;
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        var gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(entrada.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return gson.toJsonTree(out).getAsJsonObject();
    }

    private Salida logica(Entrada in) throws Exception {
        log.info(":: INICIO GestionDeDepositos ::");
        Salida out = new Salida();

        if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio)) {
            List<MensajeSalida> basicos = RequisitosBasicos.revisar(colaboradores, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo).p_out_Mensajes;
            if (MensajeSalida.contarErrores(basicos) > 0) {
                out.P_OUT_MENSAJES.addAll(basicos);
                return out;
            }
            if (in.p_Registros.isEmpty()) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "DEPOSITO_NO_EXISTE", null));
                return out;
            }
        }

        sql.lote(SQL_INSERT_DEPOSITO_PROCESO, in.p_Registros);

        Map<String, Object> param = new HashMap<>();
        param.put("TipoEjecucion", in.p_TipoEjecucion);
        param.put("IdUsuario", in.p_IdUsuario);
        param.put("FechaMovimiento", in.p_FechaMovimiento);
        List<RegistroDepositoProceso> procesar = sql.consultar(SQL_SELECT_DEPOSITO_PROCESO, param, RegistroDepositoProceso.class);

        // ---- validations (one requirement per rule; every rule reports an ERROR)
        for (RegistroDepositoProceso d : procesar) {
            requisito(in.p_IdEmpresa, d.TipoEjecucion.equalsIgnoreCase("ANU") && d.IdEstadoDeposito.equalsIgnoreCase("EDP"), "NOMINA_DEPOSITADA", d, out);
            requisito(in.p_IdEmpresa, d.TipoEjecucion.equalsIgnoreCase("LIQ") && d.IdEstadoDeposito.equalsIgnoreCase("ING"), "NOMINA_SIN_DEPOSITAR", d, out);

            // TES.Deposito
            // LEGACY DEFECT kept (see README TES-B-259): IdDeposito is null when the deposit does not exist, so compareTo throws before the message
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.Deposito") && d.IdDeposito.compareTo(0L) == 0, "DEPOSITO_NO_EXISTE", d, out);
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.Deposito") && d.IdEstadoDeposito.equalsIgnoreCase("ANU"), "DEPOSITO_ANULADO", d, out);
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.Deposito") && d.IdEstadoDeposito.equalsIgnoreCase("LIQ"), "DEPOSITO_LIQUIDADO", d, out);

            // TES.DepositoDetalle
            // LEGACY DEFECT kept (see README TES-B-260): same null dereference for a missing detail
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && d.IdDepositoDetalle.compareTo(0L) == 0, "DETALLE_DEPOSITO_NO_EXISTE", d, out);
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && d.IdEstadoDepositoDetalle.equalsIgnoreCase("LIQ"), "DETALLE_DEPOSITO_LIQUIDADO", d, out);
            requisito(in.p_IdEmpresa, d.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && d.IdEstadoDepositoDetalle.equalsIgnoreCase("ANU"), "DETALLE_DEPOSITO_ANULADO", d, out);
        }

        List<RegistroDepositoProceso> conErrores = procesar.stream().filter(d -> d.errores > 0).toList();
        procesar.removeAll(conErrores);
        if (procesar.isEmpty()) {
            log.debug("Invalid deposits: {}", conErrores.size());
            return out;
        }

        // ---- state updates (entity TES.Deposito, then TES.DepositoDetalle)
        sql.lote(SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITO, procesar);
        sql.lote(SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITO, procesar);
        sql.lote(SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DETALLE, procesar);
        sql.lote(SQL_UPDATE_DEPOSITO_ENTIDAD_DETALLE, procesar);

        // ---- annulment: the payment forms of the annulled deposits are released
        if (in.p_TipoEjecucion.equalsIgnoreCase("ANU")) {
            sql.lote(SQL_LIBERAR_COMPROBANTE_FORMA_DE_PAGO, procesar);
        }

        // ---- liquidation
        if (in.p_TipoEjecucion.equalsIgnoreCase("LIQ") && !liquidar(in, procesar, out)) {
            return out;
        }

        // ---- success messages
        String codigoExito = "";
        for (RegistroDepositoProceso d : procesar) {
            if (in.p_TipoEjecucion.equalsIgnoreCase("EDP")) {
                codigoExito = "ENVIO_NOMINA_A_DEPOSITAR_EXITO";
            }
            if (in.p_TipoEjecucion.equalsIgnoreCase("LIQ")) {
                codigoExito = d.Entidad.equalsIgnoreCase("TES.Deposito") ? "DEPOSITO_LIQUIDADO_EXITO" : "DEPOSITO_DETALLE_LIQUIDADO_EXITO";
            }
            if (in.p_TipoEjecucion.equalsIgnoreCase("ANU")) {
                codigoExito = d.Entidad.equalsIgnoreCase("TES.Deposito") ? "DEPOSITO_ANULADO_EXITO" : "DEPOSITO_DETALLE_ANULADO_EXITO";
            }
            out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, d.Id, MensajeSalida.MENSAJE_OK, codigoExito, Plataforma.valores().con("Id", d.Id)));
        }

        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        log.info(":: TERMINO GestionDeDepositos ::");
        return out;
    }

    /** @return false when LiquidarComprobante reported errors (the service then answers with its messages only). */
    private boolean liquidar(Entrada in, List<RegistroDepositoProceso> procesar, Salida out) throws Exception {
        List<Long> idsDeposito = new ArrayList<>(List.of(0L));
        List<Long> idsDepositoDetalle = new ArrayList<>(List.of(0L));
        for (RegistroDepositoProceso d : procesar) {
            if (d.Entidad.equalsIgnoreCase("TES.Deposito")) {
                idsDeposito.add(d.Id);
            } else if (d.Entidad.equalsIgnoreCase(" TES.DepositoDetalle")) { // LEGACY DEFECT kept (see README TES-B-248): leading space
                idsDepositoDetalle.add(d.Id);
            }
        }

        Map<String, Object> param = new HashMap<>();
        param.put("IdsDeposito", idsDeposito);
        param.put("IdsDetalle", idsDepositoDetalle);
        List<ComprobanteFormaPago> formasDePago = sql.consultar(SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO, param, ComprobanteFormaPago.class);

        List<ComprobanteFormaPago> aLiquidar = new ArrayList<>();
        for (ComprobanteFormaPago cfp : formasDePago) {
            if (cfp.IdEstadoDepositoDetalle().equalsIgnoreCase("LIQ") && !cfp.IdEstadoComprobanteFormaDePago().equalsIgnoreCase("LIQ")
                    && cfp.IdComprobante().compareTo(0L) != 0) {
                aLiquidar.add(cfp);
            }
        }

        if (!aLiquidar.isEmpty()) {
            LiquidarComprobante_Entrada entrada = new LiquidarComprobante_Entrada();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_IdModulo = in.p_IdModulo;
            entrada.p_SimularOperacion = "N";
            entrada.p_UsaReglasNegocio = "S";
            entrada.p_FechaMovimiento = in.p_FechaMovimiento;
            for (ComprobanteFormaPago cfp : aLiquidar) {
                ComprobanteLiquidar_Entrada comprobante = new ComprobanteLiquidar_Entrada();
                comprobante.IdComprobante = cfp.IdComprobante();
                comprobante.IdTipoMovimiento = cfp.IdTipoMovimiento();
                comprobante.AfectaCuentaInversion = "CLI";
                // LEGACY quirk kept: the legacy guards with contains() on a class without equals, i.e. identity, so a comprobante is added once per payment form
                entrada.p_ComprobanteLiquidar.add(comprobante);

                FormaDePagoLiquidar_Entrada formaDePago = new FormaDePagoLiquidar_Entrada();
                formaDePago.IdComprobante = cfp.IdComprobante();
                formaDePago.IdComprobanteFormaDePago = cfp.IdComprobanteFormaDePago();
                entrada.p_FormaDePagoLiquidar.add(formaDePago);
            }

            LiquidarComprobante_Salida salida = LiquidarComprobante.invocar(colaboradores, entrada);
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
                out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
                return false;
            }
        } else {
            sql.lote(SQL_UPDATE_COMPROBANTE_FORMA_DE_PAGO, procesar);

            List<CuentaBanco> cuentas = new ArrayList<>();
            for (ComprobanteFormaPago cfp : formasDePago) {
                if (cfp.IdCuentaBancariaCorredora().compareTo(0L) != 0) {
                    // LEGACY quirk kept: contains() on a class without equals never de-duplicates
                    cuentas.add(new CuentaBanco(cfp.IdCuentaBancariaCorredora()));
                }
            }
            if (!cuentas.isEmpty()) {
                JsonObject entradaSaldos = new JsonObject();
                entradaSaldos.addProperty("p_FechaMovimiento", in.p_FechaMovimiento);
                entradaSaldos.add("p_CuentaBanco", JsonLegacy.gson().toJsonTree(cuentas));
                actualizarSaldos.execute(entradaSaldos);
            }
        }
        return true;
    }

    private void requisito(Long idEmpresa, boolean esError, String codigoMensaje, RegistroDepositoProceso deposito, Salida out) throws SQLException {
        if (esError) {
            MensajeSalida mensaje = Plataforma.mensaje(diccionario, idEmpresa, deposito.Id, MensajeSalida.MENSAJE_ERROR, codigoMensaje, Plataforma.valores().con("Id", deposito.Id));
            out.P_OUT_MENSAJES.add(mensaje);
            deposito.errores++;
        } else {
            deposito.ok++;
        }
    }
}
