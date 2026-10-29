package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.javacall.contrato.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.javacall.contrato.AgregarLog;
import cl.bcs.tesoreria.javacall.contrato.ConsultarFlujoCaja;
import cl.bcs.tesoreria.javacall.contrato.CrearCargoAbono;
import cl.bcs.tesoreria.javacall.contrato.CrearEventoAutorizacionMasivo;
import cl.bcs.tesoreria.javacall.contrato.RevisarRequisitosBasicos;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.LiquidarComprobante} (HANA name {@code LiquidarComprobantes}), behaviours TES-A-100..164: liquidates comprobantes of
 * tesoreria and their formas de pago, updates the balances of the bank accounts and sends the movements to the cuenta de inversion.
 *
 * <p>The legacy works on session (global temporary) tables: it loads the comprobantes to liquidate, enriches them, validates them
 * (basic requirements, comprobante, detalle, forma de pago), removes the invalid ones, creates authorization events for the ones that need it,
 * liquidates what is left and finally empties the tables. The steps keep that order because the SQL depends on it.
 *
 * <p>Entry points: {@link #logica} (typed, called in process by CrearIngresoEgreso, LiquidarMovimientoAutomatico, EdicionMultiple,
 * AutorizacionLiquidacion) and {@link #execute} (the JSON {@code call}: recounts the errors and rolls back a simulation).
 */
@Service
public class LiquidarComprobanteService {

    private static final String SQL_INSERT_COMPROBANTES_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_ComprobantesALiquidar.sql");
    private static final String SQL_INSERT_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobante.sql");
    private static final String SQL_SELECT_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectComprobante.sql");
    private static final String SQL_INSERT_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobanteDetalle.sql");
    private static final String SQL_SELECT_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectComprobanteDetalle.sql");
    private static final String SQL_INSERT_FORMASDEPAGO_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertFormasDepagoALiquidar.sql");
    private static final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobanteFormaDepago.sql");
    private static final String SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectComprobanteFormaDePago.sql");
    private static final String SQL_LIQUIDAR_FORMAS_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_LiquidarFormasDepago.sql");
    private static final String SQL_LIQUIDAR_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_LiquidarComprobante.sql");
    private static final String SQL_LIQUIDAR_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_LiquidarComprobanteDetalle.sql");
    private static final String SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobantesMuevenCuentaInversion.sql");
    private static final String SQL_SELECT_CARGO_ABONO_A_GENERAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectCargoAbonoAGenerar.sql");
    private static final String SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectCuentaBancoAActualizar.sql");
    private static final String SQL_UPDATE_COMPROBANTE_REQUIERE_AUTORIZAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_UpdateComprobanteRequiereAutorizar.sql");
    private static final String SQL_DELETE_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobante.sql");
    private static final String SQL_DELETE_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobanteDetalle.sql");
    private static final String SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobanteFormaDePago.sql");
    private static final String SQL_DELETE_COMPROBANTES_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobantesALiquidar.sql");
    private static final String SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteFormasDepagoALiquidar.sql");
    private static final String SQL_DELETE_COMPROBANTES_MUEVEN_CI = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobantesMuevenCuentaInversion.sql");
    private static final String SQL_SELECT_DATOS_PORTAFOLIO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectDatosPortafolio.sql");
    private static final String SQL_SELECT_CONCEPTO_TESORERIA = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectConceptoTesoreria.sql");
    private static final String SQL_SELECT_REQUISITO_OPERATIVO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectRequisitoOperativo.sql");
    private static final String SQL_SELECT_MONEDA_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectMonedaComprobanteDetalle.sql");
    private static final String SQL_SELECT_INDICADORES_PORTAFOLIO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectIndicadoresPortafolio.sql");

    private static final String SERVICIO = "cl.bcs.tesoreria.LiquidarComprobante";
    private static final String USO_FLUJO_EGRESOS = "EGRESOS_TESORERIA";
    private static final String MOVIMIENTO_EGRESO = "MOVEGR";
    private static final String ESTADO_POR_AUTORIZAR = "PDA";

    /** Estados en que el monto de las formas de pago debe cuadrar con el del comprobante. */
    private static final Set<String> ESTADOS_CON_FORMA_DE_PAGO = Set.of("ING", "LIP", "GIP", "GIR");

    // ------------------------------------------------------------------ JavaCall contract (legacy names, public fields)

    public static class LiquidarComprobante_Entrada
    {
        public Long        p_IdEmpresa;
        public Long        p_IdUsuario;
        public String     p_IdModulo;
        public String     p_SimularOperacion = "N";
        public String     p_UsaReglasNegocio;
        public String     p_FechaMovimiento;     // Corresponde a Fecha Sistema

        //Uso exclusivo Aporte CSC
        public String    FolioCSC = "";
        // En SQL se llama "p_TablaComprobante"
        public List<ComprobanteLiquidar_Entrada> p_ComprobanteLiquidar;  // Comprobantes a Liquidar

        // En SQL se llama "p_TablaComprobanteFormaDePago"
        public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar;  // Formas de Pago a Liquidar > Si no especifica la FP a liquidar, se asume que se deben liquidar todas las FP de ese comprobante

        public LiquidarComprobante_Entrada()
        {
            p_SimularOperacion    = "N";
            p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidar_Entrada>();
            p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteLiquidar_Entrada
    {
        public Long     IdComprobante;
        public String     IdTipoMovimiento;          // MOVING / MOVEGR
        public String     AfectaCuentaInversion;  // CLI: Cliente / COR: Corredora / VACIO: Ambos
    }

    public static class FormaDePagoLiquidar_Entrada
    {
        public Long     IdComprobante;
        public Long     IdComprobanteFormaDePago;
    }

    public static class LiquidarComprobante_Salida
    {
        public List<MensajeSalida>                P_OUT_MENSAJES;
        public Long                                P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida>    P_OUT_RESULTADOPROCESO;

        public LiquidarComprobante_Salida (){
            P_OUT_MENSAJES                 = new ArrayList<MensajeSalida>();
            P_OUT_RESULTADOPROCESO         = new ArrayList<ResultadoProceso_Salida>();
        };

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ResultadoProceso_Salida
    {
        public String     Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
        public Long       Id;           // BIGINT        -- IdComprobante liquidado  Ej: 9999
        public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
        public String    Resultado;    // VARCHAR(40)   -- Ej: "OK"

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class Comprobante
    {
        public Long           IdInterno;
        public Long           IdComprobanteInformado;
        public String         IdTipoMovimientoInformado;
        public Long           IdComprobante;
        public String         IdTipoMovimiento;
        public BigDecimal    MontoComprobante;
        public Long           CantidadFormaDePago;
        public BigDecimal   MontoTotalFormaDePago;
        public String         IdEstadoComprobante;
        public String         AfectaCuentaInversion;
           public Long           IdPortafolioTitularPago;
           public String         IdConcepto;
           public String        FechaMovimiento;    //Fecha de sistema en que se liquida
           public Long           IdPersona;
           public String         NombrePersona;
        public String         TitularPago;
        public String         IdTipoPersonaTitularPago;
        public Long           IdPersonaTitularPago;
        public String         FechaLiquidacion;
        public Long           IdCaja;

        //Para validaciones
        public long                 errores         = 0;
        public long                 advertencias     = 0;
        public long                 autorizaciones     = 0;
        public long                 ok                 = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteRequiereAutorizar
    {
        public Long       IdComprobante;
        public String     IdTipoMovimiento;
        public String     AfectaCuentaInversion;
        public Long       IdComprobanteFormaDePago;
        public Long        IdUsuario;
        public String     UsaReglasNegocio;
        public String    FechaMovimiento;
        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class RequisitosOperativosLiquidacion{
        public String IdCondicionOperativa;
        public String IdTipoEtapa;
        public String IdTipoCanalIngreso;
        public String IdTipoComportamiento;
        public String IdEstado;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteDetalle
    {
        public Long           IdInterno;
        public Long           IdComprobanteInformado;
        public Long           IdComprobanteDetalle;
           public String         IdTipoMovimiento;
           public String         IdTipoComprobanteOrigen;
           public Long           SecuenciaRelacionada;
           public int           PendienteDeLiquidar;
           public String         IdConcepto;
           public BigDecimal    Monto;

        //Para validaciones
        public long                 errores         = 0;
        public long                 advertencias     = 0;
        public long                 autorizaciones     = 0;
        public long                 ok                 = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteDetalleMoneda
    {
        public Long           IdComprobante;
           public String         IdMoneda;

        //Para validaciones
        public long                 errores         = 0;
        public long                 advertencias     = 0;
        public long                 autorizaciones     = 0;
        public long                 ok                 = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteFormaDePago
    {
           public Long           IdInterno;
           public Long           IdComprobanteInformado;
           public Long           IdComprobanteFormaDePagoInformado;
           public Long           IdComprobanteFormaDePago;
           public Long           IdCuentaBancariaCorredora;
        public String         IdEstadoFormaDePago;
        public String         IdEstadoComprobante;
        public String         TipoMedioDePago;
        public String         LiquidacionParcialEnviaACTA;   //es un parametro de tesoreria
        public String         EstadosPosibleTipoMovimientoMedioDePago;
        public String         AfectaSaldoEnEstado;
        public String         IdMoneda;
        public BigDecimal    Monto;
        public Long           IdDeposito;
        public String         IdEstadoDepositoDetalle;
        public java.sql.Date FechaLiquidacion;
        public Long           DiasRetencion;
        public String         IdMedioDePago;
        public Long           IdCaja;
        public String        FechaMovimiento;  //Fecha de sistema en que se liquida
        public Long           IdUsuario;

        public String        NumeroCuentaBancariaCorredora;
        public String        IdTipoMovimiento;
        public String        IdModalidadDePago;
        public Long         IdCuentaBancariaSocio;
        public String        NumeroCuentaBancariaSocio;

        public Long         IdBancoSocio;

        public String        TipoComprobante;

        public String        ValidarFormaDePagoSinCertificar = "S";   // Para recudacion APV no se valida
        public String        ValidarFormaDePagoSinDepositar  = "S";   // Para recudacion APV no se valida

        //Para validaciones
        public long                 errores         = 0;
        public long                 advertencias     = 0;
        public long                 autorizaciones     = 0;
        public long                 ok                 = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ParametrosConfiguracion
    {

        public String LiquidacionParcialEnviaACTA;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class FlujoCajaElem {
        String IdMoneda;
        String Fecha;
        Long IdPortafolio;

        public FlujoCajaElem(String moneda,String fecha,Long idPortafolio) {
            this.IdMoneda = moneda;
            this.Fecha = fecha;
            this.IdPortafolio = idPortafolio;
        }

        @Override
        public boolean equals(Object o) {
            FlujoCajaElem e = (FlujoCajaElem)o;
            return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.IdPortafolio.equals(e.IdPortafolio);
        }

        @Override
        public int hashCode() {
            return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
        }

    }

    public static class ComprobantesMuevenCuentaInversion
    {

        public Long       IdComprobante;
        public Long       IdComprobanteDetalle;
        public Long       IdComprobanteFormaDePago;
        public String     AfectaCuentaInversion;
        public String     FechaMovimiento;
        public String     IdConcepto;
        public Long       IdPersona;
        public String     NombrePersona;
        public String     TitularPago;
        public String     IdTipoPersonaTitularPago;
        public Long       IdPersonaTitularPago;
        public String     IdMoneda;
        public BigDecimal Monto;
        public Long       IdPortafolio;
        public String     IdTipoMovimientoCtaInvCliente;
        public String     IdTipoMovimientoCtaInvCorredora;
        public String     IdTipoComprobante;
        public String     IdTipoMovimientoAsociado;
        public Long       IdMovimientoAsociado;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ConceptoTesoreria extends BcsPojo {
        public String IdConcepto;
        public String IndicadorNoValidaSaldoCaja;

        public ConceptoTesoreria() {
            IndicadorNoValidaSaldoCaja = "N";
        }
    }

    public static class DatoPortafolio{
        public Long IdPortafolio;
        public String NombreCompleto;
        public String DocumentoIdentidad;
    }

    /** Indicators of a portafolio; only the column used here (the legacy row class had many more). */
    public static class IndicadoresPortafolio {
        public Long IdPortafolio;
        public String OperaCuentaInversion;
    }

    /** Result of evaluating one requirement over a row. */
    private enum Resultado { ERROR, AUTORIZACION, ADVERTENCIA, OK }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public LiquidarComprobanteService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    // ------------------------------------------------------------------ entry points

    /** Legacy {@code call}: recounts the errors and, for a simulation, rolls back the transaction after the work is done. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        LiquidarComprobante_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, LiquidarComprobante_Entrada.class);
        LiquidarComprobante_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        if (in.p_SimularOperacion.equalsIgnoreCase("S")) {
            LiquidarComprobanteTransaccion.rollback(jdbc);
        }
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /**
     * Legacy {@code logica}. The error counter of the result is only set when the process reaches the point where the legacy set it: the early
     * returns (basic requirements failed, nothing valid left) leave it null.
     */
    @Transactional(rollbackFor = Exception.class)
    public LiquidarComprobante_Salida logica(LiquidarComprobante_Entrada in) throws Exception {
        LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
        List<Comprobante> comprobantesAProcesar = in.p_ComprobanteLiquidar.stream().map(c -> {
            Comprobante comprobante = new Comprobante();
            comprobante.IdComprobante = c.IdComprobante;
            return comprobante;
        }).collect(Collectors.toList());

        // mandatory configuration (a missing parameter raises, before anything is written)
        String liquidacionParcialEnviaACTA = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.TESORERIA, "LIQUIDACIONPARCIALENVIAACTA");
        String permiteCuentaBancariaSocioNegocio = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.TESORERIA, "PERMITECUENTABANCARSOCIONEGOCIO");
        Map<String, String> comportamientos = comportamientosOperativos(in.p_IdEmpresa);

        boolean terminado = false;
        try {
            terminado = procesar(in, out, liquidacionParcialEnviaACTA, permiteCuentaBancariaSocioNegocio, comportamientos);
        } finally {
            limpiarTablasDeTrabajo(comprobantesAProcesar);
        }
        if (terminado) {
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        }
        return out;
    }

    // ------------------------------------------------------------------ process

    /** @return true when the process reached its end, false when it returned early (the result stays as it was). */
    private boolean procesar(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, String liquidacionParcialEnviaACTA,
            String permiteCuentaBancariaSocioNegocio, Map<String, String> comportamientos) throws Exception {
        // comprobantes to liquidate and their data, in the session tables
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_A_LIQUIDAR, in.p_ComprobanteLiquidar);
        Comprobante fechaComprobante = new Comprobante();
        fechaComprobante.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE, List.of(fechaComprobante));
        List<Comprobante> comprobante = jdbc.query(SQL_SELECT_COMPROBANTE, Map.of(), Filas.de(Comprobante.class));

        registrarLog(in, comprobante);

        ComprobanteDetalle soloIdInterno = new ComprobanteDetalle();
        soloIdInterno.IdInterno = 1L; // only so that a parameter exists, otherwise the batch does not run
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_DETALLE, List.of(soloIdInterno));
        List<ComprobanteDetalle> comprobanteDetalle = jdbc.query(SQL_SELECT_COMPROBANTE_DETALLE, Map.of(), Filas.de(ComprobanteDetalle.class));

        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_FORMASDEPAGO_A_LIQUIDAR, in.p_FormaDePagoLiquidar);
        ComprobanteFormaDePago fechaFormaDePago = new ComprobanteFormaDePago();
        fechaFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, List.of(fechaFormaDePago));
        List<ComprobanteFormaDePago> comprobanteFormaDePago = jdbc.query(SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO, Map.of("IdEmpresa", in.p_IdEmpresa),
                Filas.de(ComprobanteFormaDePago.class));

        String fechaSistema = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");
        Map<Long, BigDecimal> saldos = saldosEgresosPorPortafolio(in, comprobante, fechaSistema);
        Map<Long, String> operaCuentaInversion = operaCuentaInversion(comprobante);

        if (in.p_UsaReglasNegocio.equalsIgnoreCase("S")) {
            RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida basicos = Colaborador.invocar(colaboradores, "RevisarRequisitosBasicos",
                    requisitosBasicos(in), RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.class);
            if (MensajeSalida.numeroErrores(basicos.p_out_Mensajes) > 0) {
                out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
                return false;
            }
            Map<Long, List<String>> autorizaciones = validar(in, out, comprobante, comprobanteDetalle, comprobanteFormaDePago, fechaSistema, saldos,
                    operaCuentaInversion, comportamientos, permiteCuentaBancariaSocioNegocio);
            quitarInvalidos(comprobante, comprobanteDetalle, comprobanteFormaDePago);
            depurarMensajes(out);
            solicitarAutorizaciones(in, out, comprobante, autorizaciones);
        }

        // after the validations: is there anything left to liquidate?
        if (jdbc.query(SQL_SELECT_COMPROBANTE, Map.of(), Filas.de(Comprobante.class)).isEmpty()) {
            return false;
        }

        // liquidate the formas de pago, the comprobantes and their detalles
        ComprobanteFormaDePago liquidarFormaDePago = new ComprobanteFormaDePago();
        liquidarFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
        liquidarFormaDePago.IdUsuario = in.p_IdUsuario;
        LiquidarComprobanteSql.lote(jdbc, SQL_LIQUIDAR_FORMAS_DE_PAGO, List.of(liquidarFormaDePago));
        Comprobante liquidarComprobante = new Comprobante();
        liquidarComprobante.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_LIQUIDAR_COMPROBANTE, List.of(liquidarComprobante));
        ComprobanteDetalle liquidarDetalle = new ComprobanteDetalle();
        liquidarDetalle.IdInterno = 1L; // only so that a parameter exists, otherwise the batch does not run
        LiquidarComprobanteSql.lote(jdbc, SQL_LIQUIDAR_COMPROBANTE_DETALLE, List.of(liquidarDetalle));

        if (!actualizarSaldosBancarios(in, out)) {
            return false;
        }

        for (Comprobante com : comprobante) {
            if (com.ok > 0 && com.IdComprobante.compareTo(0L) == 1) {
                out.P_OUT_RESULTADOPROCESO.add(resultado("IdComprobante", com.IdComprobante, SERVICIO, "LIQ"));
            }
        }
        return generarCargosAbonos(in, out, liquidacionParcialEnviaACTA);
    }

    private Map<String, String> comportamientosOperativos(Long idEmpresa) {
        Map<String, RequisitosOperativosLiquidacion> requisitos = new HashMap<>();
        for (RequisitosOperativosLiquidacion r : jdbc.query(SQL_SELECT_REQUISITO_OPERATIVO, Map.of("IdEmpresa", idEmpresa),
                Filas.de(RequisitosOperativosLiquidacion.class))) {
            requisitos.put(r.IdCondicionOperativa, r);
        }
        Map<String, String> comportamientos = new HashMap<>();
        for (String condicion : List.of("LIQFECHADIS", "LIQSALDONEG")) {
            RequisitosOperativosLiquidacion r = requisitos.get(condicion);
            if (r != null) {
                comportamientos.put(condicion, r.IdEstado.equals("ACT") ? r.IdTipoComportamiento : "");
            }
        }
        return comportamientos;
    }

    private static Map<String, Object> requisitosBasicos(LiquidarComprobante_Entrada in) {
        Map<String, Object> entrada = new java.util.LinkedHashMap<>();
        entrada.put("p_IdEmpresa", in.p_IdEmpresa);
        entrada.put("p_IdUsuario", in.p_IdUsuario);
        entrada.put("p_IdModulo", in.p_IdModulo);
        return entrada;
    }

    /** One log header per comprobante found (the log service is not asked when none was found). */
    private void registrarLog(LiquidarComprobante_Entrada in, List<Comprobante> comprobantes) {
        if (comprobantes == null || comprobantes.isEmpty()) {
            return;
        }
        AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
        for (Comprobante c : comprobantes) {
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.NombreProceso = "Liquidar Comprobante Tesoreria";
            cabecera.IdCabeceraLog = c.IdComprobante != null ? c.IdComprobante : 0L;
            cabecera.IdTipoLog = "INGRESO";
            cabecera.IdModulo = in.p_IdModulo;
            cabecera.IdTipoRegistro = "LIQUIDACION_TESORERIA";
            cabecera.IdUsuario = in.p_IdUsuario;
            cabecera.IdEmpresa = in.p_IdEmpresa;
            cabecera.FechaSistemaLog = c.FechaLiquidacion != null ? c.FechaLiquidacion : "";
            cabecera.IdUsuarioAtendido = in.p_IdUsuario;
            cabecera.IdPersona = c.IdPersonaTitularPago != null ? c.IdPersonaTitularPago : 0L;
            cabecera.IdPortafolio = c.IdPortafolioTitularPago != null ? c.IdPortafolioTitularPago : 0L;
            cabecera.Folio = c.IdComprobante != null ? c.IdComprobante : 0L;
            cabecera.Monto = c.MontoComprobante != null ? c.MontoComprobante : BigDecimal.ZERO;
            entradaLog.p_Log.add(cabecera);
        }
        Colaborador.invocar(colaboradores, "AgregarLog", entradaLog, AgregarLog.OutAgregarLog.class);
    }

    /**
     * Cash-flow balance (EGRESOS_TESORERIA) of the portafolio that pays each comprobante, by portafolio.
     * The currency query only receives the LAST comprobante (the legacy keeps one key in a HashMap): LEGACY DEFECT kept (see README TES-A-133).
     */
    private Map<Long, BigDecimal> saldosEgresosPorPortafolio(LiquidarComprobante_Entrada in, List<Comprobante> comprobante, String fechaSistema) {
        MapSqlParameterSource idComprobante = new MapSqlParameterSource("IdComprobante", null);
        for (Comprobante c : comprobante) {
            idComprobante.addValue("IdComprobante", c.IdComprobante);
        }
        Map<Long, String> monedas = new HashMap<>();
        for (ComprobanteDetalleMoneda m : jdbc.query(SQL_SELECT_MONEDA_COMPROBANTE_DETALLE, idComprobante, Filas.de(ComprobanteDetalleMoneda.class))) {
            monedas.put(m.IdComprobante, m.IdMoneda);
        }
        List<ConsultarFlujoCaja.PortafolioAConsultar> portafolios = new ArrayList<>();
        for (Comprobante c : comprobante) {
            if (c.IdPortafolioTitularPago != null && c.IdPortafolioTitularPago != 0) {
                ConsultarFlujoCaja.PortafolioAConsultar p = new ConsultarFlujoCaja.PortafolioAConsultar();
                p.IdMoneda = monedas.get(c.IdComprobante);
                p.IdPortafolio = c.IdPortafolioTitularPago;
                portafolios.add(p);
            }
        }
        Map<Long, BigDecimal> saldos = new HashMap<>();
        if (!portafolios.isEmpty()) {
            ConsultarFlujoCaja.EntradaConsultarFlujoCaja entrada = new ConsultarFlujoCaja.EntradaConsultarFlujoCaja();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_IdModulo = in.p_IdModulo;
            entrada.p_UsaReglasNegocio = "S";
            entrada.p_FechaDesde = fechaSistema;
            entrada.p_FechaHasta = fechaSistema;
            entrada.p_Uso = USO_FLUJO_EGRESOS;
            entrada.p_PortafolioAConsultar = portafolios;
            ConsultarFlujoCaja.SalidaConsultarFlujoCaja salida = Colaborador.invocar(colaboradores, "ConsultarFlujoCaja", entrada,
                    ConsultarFlujoCaja.SalidaConsultarFlujoCaja.class);
            for (SaldoFlujoCaja.ObjFLUJOCAJA flujo : salida.P_OUT_FLUJOCAJA.stream().filter(s -> s.NombreUso.equals(USO_FLUJO_EGRESOS)).toList()) {
                saldos.put(flujo.IdPortafolio, flujo.Saldo);
            }
        }
        return saldos;
    }

    private Map<Long, String> operaCuentaInversion(List<Comprobante> comprobante) {
        Map<Long, String> opera = new HashMap<>();
        List<Long> portafolios = comprobante.stream().map(c -> c.IdPortafolioTitularPago).collect(Collectors.toList());
        if (!portafolios.isEmpty()) {
            for (IndicadoresPortafolio i : jdbc.query(SQL_SELECT_INDICADORES_PORTAFOLIO, Map.of("idsPortafolio", portafolios),
                    Filas.de(IndicadoresPortafolio.class))) {
                opera.put(i.IdPortafolio, i.OperaCuentaInversion);
            }
        }
        return opera;
    }

    // ------------------------------------------------------------------ validations

    /** @return the authorization codes raised by each comprobante (keyed by its id). */
    private Map<Long, List<String>> validar(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, List<Comprobante> comprobante,
            List<ComprobanteDetalle> detalles, List<ComprobanteFormaDePago> formasDePago, String fechaSistema, Map<Long, BigDecimal> saldos,
            Map<Long, String> operaCuentaInversion, Map<String, String> comportamientos, String permiteCuentaBancariaSocioNegocio) throws Exception {
        Long empresa = in.p_IdEmpresa;
        Map<Long, List<String>> autorizaciones = new HashMap<>();
        for (Comprobante com : comprobante) {
            List<String> codigosAutorizacion = new ArrayList<>();
            validarComprobante(empresa, com, out.P_OUT_MENSAJES, codigosAutorizacion);
            requisitoFecha(empresa, com, fechaSistema, comportamientos.get("LIQFECHADIS"), out.P_OUT_MENSAJES, codigosAutorizacion);
            requisitoSaldo(empresa, com, saldos, operaCuentaInversion, comportamientos.get("LIQSALDONEG"), out.P_OUT_MENSAJES, codigosAutorizacion);
            if (!codigosAutorizacion.isEmpty()) {
                autorizaciones.put(com.IdComprobante, codigosAutorizacion);
            }
        }
        for (ComprobanteDetalle det : detalles) {
            aplicar(registrar(empresa, det.IdTipoMovimiento.equalsIgnoreCase(MOVIMIENTO_EGRESO) && det.PendienteDeLiquidar > 0,
                    "SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS", MensajeSalida.MENSAJE_ERROR, det.IdComprobanteInformado,
                    valores("IdComprobante", det.IdComprobanteInformado), out.P_OUT_MENSAJES, null), det);
        }
        for (ComprobanteFormaDePago fp : formasDePago) {
            validarFormaDePago(empresa, fp, permiteCuentaBancariaSocioNegocio, out.P_OUT_MENSAJES);
        }
        return autorizaciones;
    }

    private void validarComprobante(Long empresa, Comprobante com, List<MensajeSalida> mensajes, List<String> autorizaciones) throws Exception {
        List<MensajeSalida> m = mensajes;
        aplicar(requisito(empresa, com, com.IdComprobante == null || com.IdComprobante == 0, "COMPROBANTE_TESORERIA_NO_EXISTE", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.IdEstadoComprobante.equalsIgnoreCase("BLO"), "COMPROBANTE_TESORERIA_BLOQUEADO", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.IdEstadoComprobante.equalsIgnoreCase("ANU"), "COMPROBANTE_TESORERIA_ANULADO", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.IdEstadoComprobante.equalsIgnoreCase("MOD"), "COMPROBANTE_TESORERIA_MODIFICADO", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.IdEstadoComprobante.equalsIgnoreCase("LIQ"), "COMPROBANTE_LIQUIDADO_TESORERIA", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.CantidadFormaDePago.compareTo(0L) == 0 && esEstadoConFormaDePago(com), "COMPROBANTE_TESORERIA_SIN_FORMA_PAGO", m,
                autorizaciones), com);
        aplicar(requisito(empresa, com, com.MontoTotalFormaDePago.compareTo(com.MontoComprobante) == -1 && esEstadoConFormaDePago(com),
                "MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE", m, autorizaciones), com);
        aplicar(requisito(empresa, com, com.MontoTotalFormaDePago.compareTo(com.MontoComprobante) == 1 && esEstadoConFormaDePago(com),
                "MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE", m, autorizaciones), com);
    }

    private static boolean esEstadoConFormaDePago(Comprobante com) {
        return ESTADOS_CON_FORMA_DE_PAGO.stream().anyMatch(com.IdEstadoComprobante::equalsIgnoreCase);
    }

    /** Operational requirement: liquidation on a date other than the system date (an egreso that is not already pending authorization). */
    private void requisitoFecha(Long empresa, Comprobante com, String fechaSistema, String comportamiento, List<MensajeSalida> mensajes,
            List<String> autorizaciones) throws Exception {
        if (comportamiento == null) {
            return;
        }
        // LEGACY DEFECT kept (see README TES-A-128): the rejection variant reuses the ADVERTIR message code
        String codigo = switch (comportamiento) {
            case "AUTORIZACION" -> "COMPROBANTE_TESORERIA_AUTORIZAR_FECHA";
            case "ADVERTENCIA", "RECHAZO" -> "COMPROBANTE_TESORERIA_ADVERTIR_FECHA";
            default -> null;
        };
        String tipo = switch (comportamiento) {
            case "AUTORIZACION" -> MensajeSalida.MENSAJE_AUTORIZACION;
            case "ADVERTENCIA" -> MensajeSalida.MENSAJE_ADVERTENCIA;
            default -> MensajeSalida.MENSAJE_ERROR;
        };
        if (codigo != null && com.IdTipoMovimiento.equalsIgnoreCase(MOVIMIENTO_EGRESO) && !com.IdEstadoComprobante.equalsIgnoreCase(ESTADO_POR_AUTORIZAR)) {
            aplicar(registrar(empresa, !com.FechaLiquidacion.equals(fechaSistema), codigo, tipo, com.IdComprobanteInformado, valoresComprobante(com), mensajes,
                    autorizaciones), com);
        }
    }

    /** Operational requirement: negative cash flow when a client pays out of the cuenta de inversion (concepts CIECDV / REPDIN). */
    private void requisitoSaldo(Long empresa, Comprobante com, Map<Long, BigDecimal> saldos, Map<Long, String> operaCuentaInversion, String comportamiento,
            List<MensajeSalida> mensajes, List<String> autorizaciones) throws Exception {
        if (comportamiento == null) {
            return;
        }
        boolean concepto = com.IdConcepto.equalsIgnoreCase("CIECDV") || com.IdConcepto.equalsIgnoreCase("REPDIN");
        if (!(concepto && com.IdTipoPersonaTitularPago.equalsIgnoreCase("CLI") && com.IdPortafolioTitularPago != 0)
                || !operaCuentaInversion.get(com.IdPortafolioTitularPago).equalsIgnoreCase("S")) {
            return;
        }
        String codigo = switch (comportamiento) {
            case "AUTORIZACION" -> "COMPROBANTE_TESORERIA_AUTORIZAR_SALDO";
            case "ADVERTENCIA", "RECHAZO" -> "COMPROBANTE_TESORERIA_ADVERTIR_SALDO";
            default -> null;
        };
        String tipo = switch (comportamiento) {
            case "AUTORIZACION" -> MensajeSalida.MENSAJE_AUTORIZACION;
            case "ADVERTENCIA" -> MensajeSalida.MENSAJE_ADVERTENCIA;
            default -> MensajeSalida.MENSAJE_ERROR;
        };
        if (codigo != null && com.IdTipoMovimiento.equalsIgnoreCase(MOVIMIENTO_EGRESO) && !com.IdEstadoComprobante.equalsIgnoreCase(ESTADO_POR_AUTORIZAR)) {
            aplicar(registrar(empresa, saldos.get(com.IdPortafolioTitularPago).compareTo(BigDecimal.ZERO) < 0, codigo, tipo, com.IdComprobanteInformado,
                    valoresComprobante(com), mensajes, autorizaciones), com);
        }
    }

    private void validarFormaDePago(Long empresa, ComprobanteFormaDePago fp, String permiteCuentaBancariaSocioNegocio, List<MensajeSalida> mensajes)
            throws Exception {
        boolean anulada = fp.IdEstadoComprobante.equalsIgnoreCase("ANU");
        formaDePago(empresa, fp, !anulada && (fp.IdComprobanteFormaDePago == null || fp.IdComprobanteFormaDePago.compareTo(0L) == 0),
                "FORMA_PAGO_COMPROBANTE_NO_EXISTE", mensajes);
        formaDePago(empresa, fp, anulada, "FORMA_PAGO_COMPROBANTE_ANULADA", mensajes);
        formaDePago(empresa, fp, fp.IdEstadoComprobante.equalsIgnoreCase("LIQ"), "FORMA_PAGO_COMPROBANTE_LIQUIDADA", mensajes);
        formaDePago(empresa, fp, fp.IdEstadoFormaDePago.equalsIgnoreCase("NDP") && !fp.IdEstadoDepositoDetalle.equalsIgnoreCase("LIQ"),
                "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO", mensajes);
        formaDePago(empresa, fp, !anulada && (fp.IdCuentaBancariaCorredora.compareTo(0L) == 0 || fp.NumeroCuentaBancariaCorredora.equals(""))
                && !fp.IdMedioDePago.equalsIgnoreCase("EFE"), "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA", mensajes);
        if (permiteCuentaBancariaSocioNegocio.equals("S")) {
            formaDePago(empresa, fp, !anulada && (fp.IdTipoMovimiento.equalsIgnoreCase(MOVIMIENTO_EGRESO) && !fp.TipoComprobante.equalsIgnoreCase("PAREGR")
                    && fp.IdModalidadDePago.equalsIgnoreCase("MODDEP") && (fp.IdBancoSocio.compareTo(0L) == 0 || fp.NumeroCuentaBancariaSocio.equals(""))),
                    "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_SOCIO", mensajes);
        }
        formaDePago(empresa, fp, fp.ValidarFormaDePagoSinDepositar.equalsIgnoreCase("S") && !anulada
                && (fp.IdMedioDePago.equalsIgnoreCase("CHE") || fp.IdMedioDePago.equalsIgnoreCase("VVI"))
                && fp.EstadosPosibleTipoMovimientoMedioDePago.contains("NDP") && !fp.IdEstadoFormaDePago.equalsIgnoreCase("NDP"),
                "FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR", mensajes);
        formaDePago(empresa, fp, fp.ValidarFormaDePagoSinCertificar.equalsIgnoreCase("S") && !anulada && fp.EstadosPosibleTipoMovimientoMedioDePago.contains("CRT")
                && (!fp.IdEstadoFormaDePago.equalsIgnoreCase("CRT") && !fp.IdEstadoFormaDePago.equalsIgnoreCase("EBV")
                        && !fp.IdEstadoFormaDePago.equalsIgnoreCase("NDP")),
                "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR", mensajes);
    }

    private void formaDePago(Long empresa, ComprobanteFormaDePago fp, boolean incumple, String codigo, List<MensajeSalida> mensajes) throws Exception {
        // LEGACY DEFECT kept (see README TES-A-111): the {IdComprobanteFormaDePago} placeholder is filled with the medio de pago
        Map<String, String> valores = valores("IdComprobante", fp.IdComprobanteInformado, "IdComprobanteFormaDePago", fp.IdMedioDePago);
        aplicar(registrar(empresa, incumple, codigo, MensajeSalida.MENSAJE_ERROR, fp.IdComprobanteInformado, valores, mensajes, null), fp);
    }

    private Resultado requisito(Long empresa, Comprobante com, boolean incumple, String codigo, List<MensajeSalida> mensajes, List<String> autorizaciones)
            throws Exception {
        return registrar(empresa, incumple, codigo, MensajeSalida.MENSAJE_ERROR, com.IdComprobanteInformado, valoresComprobante(com), mensajes, autorizaciones);
    }

    private static Map<String, String> valoresComprobante(Comprobante com) {
        return valores("IdComprobante", com.IdComprobanteInformado, "Monto", com.MontoComprobante);
    }

    /** Message placeholders; a null value is left out, as the legacy dictionary builder did. Pairs are key, value, key, value... */
    private static Map<String, String> valores(Object... pares) {
        Map<String, String> valores = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            if (pares[i + 1] != null) {
                valores.put((String) pares[i], pares[i + 1].toString());
            }
        }
        return valores;
    }

    /**
     * Evaluates one requirement: when it is not met the message is added according to its kind and the authorization code is remembered.
     */
    private Resultado registrar(Long empresa, boolean incumple, String codigo, String tipo, Long id, Map<String, String> valores, List<MensajeSalida> mensajes,
            List<String> autorizaciones) throws Exception {
        if (!incumple) {
            return Resultado.OK;
        }
        String descripcion = diccionario.mensaje(empresa, codigo, valores);
        switch (tipo) {
            case MensajeSalida.MENSAJE_ERROR -> {
                mensajes.add(new MensajeSalida(id, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
                return Resultado.ERROR;
            }
            case MensajeSalida.MENSAJE_AUTORIZACION -> {
                mensajes.add(new MensajeSalida(id, MensajeSalida.MENSAJE_AUTORIZACION, codigo, descripcion));
                autorizaciones.add(codigo);
                return Resultado.AUTORIZACION;
            }
            case MensajeSalida.MENSAJE_ADVERTENCIA -> {
                mensajes.add(new MensajeSalida(id, MensajeSalida.MENSAJE_ADVERTENCIA, codigo, descripcion));
                return Resultado.ADVERTENCIA;
            }
            default -> {
                return Resultado.OK;
            }
        }
    }

    private static void aplicar(Resultado r, Comprobante c) {
        switch (r) {
            case ERROR -> c.errores++;
            case AUTORIZACION -> c.autorizaciones++;
            case ADVERTENCIA -> c.advertencias++;
            case OK -> c.ok++;
        }
    }

    private static void aplicar(Resultado r, ComprobanteDetalle c) {
        switch (r) {
            case ERROR -> c.errores++;
            case AUTORIZACION -> c.autorizaciones++;
            case ADVERTENCIA -> c.advertencias++;
            case OK -> c.ok++;
        }
    }

    private static void aplicar(Resultado r, ComprobanteFormaDePago c) {
        switch (r) {
            case ERROR -> c.errores++;
            case AUTORIZACION -> c.autorizaciones++;
            case ADVERTENCIA -> c.advertencias++;
            case OK -> c.ok++;
        }
    }

    /** Takes the comprobantes with errors (found at any level) out of the session tables. */
    private void quitarInvalidos(List<Comprobante> comprobante, List<ComprobanteDetalle> detalles, List<ComprobanteFormaDePago> formasDePago)
            throws SQLException {
        List<Comprobante> conErrores = new ArrayList<>();
        comprobante.stream().filter(c -> c.errores > 0).forEach(c -> agregarSiNoEsta(conErrores, c.IdComprobante));
        formasDePago.stream().filter(f -> f.errores > 0).forEach(f -> agregarSiNoEsta(conErrores, f.IdComprobanteInformado));
        detalles.stream().filter(d -> d.errores > 0).forEach(d -> agregarSiNoEsta(conErrores, d.IdComprobanteInformado));
        if (!conErrores.isEmpty()) {
            LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE, conErrores);
            LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_DETALLE, conErrores);
            LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, conErrores);
        }
    }

    private static void agregarSiNoEsta(List<Comprobante> lista, Long idComprobante) {
        // the legacy compared Comprobante objects with the default equals (identity), so every row is added; kept
        Comprobante c = new Comprobante();
        c.IdComprobante = idComprobante;
        lista.add(c);
    }

    /** When there are errors, warnings and authorizations are dropped from the messages (the first equal message is removed for each one). */
    private static void depurarMensajes(LiquidarComprobante_Salida out) {
        List<MensajeSalida> resultado = new ArrayList<>(out.P_OUT_MENSAJES);
        boolean hayErrores = out.P_OUT_MENSAJES.stream().anyMatch(m -> m.tipo().equals(MensajeSalida.MENSAJE_ERROR));
        boolean hayOtros = out.P_OUT_MENSAJES.stream().anyMatch(
                m -> m.tipo().equals(MensajeSalida.MENSAJE_ADVERTENCIA) || m.tipo().equals(MensajeSalida.MENSAJE_AUTORIZACION));
        if (hayErrores && hayOtros) {
            for (MensajeSalida m : out.P_OUT_MENSAJES) {
                if (m.tipo().equals(MensajeSalida.MENSAJE_ADVERTENCIA) || m.tipo().equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
                    resultado.remove(resultado.indexOf(m));
                }
            }
            out.P_OUT_MENSAJES = resultado;
        }
    }

    // ------------------------------------------------------------------ authorization events

    /** Comprobantes that raised an authorization and survived the validations: an authorization event is created for each requirement. */
    private void solicitarAutorizaciones(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, List<Comprobante> comprobante,
            Map<Long, List<String>> autorizaciones) throws Exception {
        Map<Long, Comprobante> vigentes = new HashMap<>();
        for (Comprobante c : jdbc.query(SQL_SELECT_COMPROBANTE, Map.of(), Filas.de(Comprobante.class))) {
            vigentes.put(c.IdComprobante, c);
        }
        List<ComprobanteRequiereAutorizar> requieren = new ArrayList<>();
        for (Comprobante com : comprobante) {
            if (com.autorizaciones > 0 && vigentes.get(com.IdComprobante) != null) {
                ComprobanteRequiereAutorizar r = new ComprobanteRequiereAutorizar();
                r.IdComprobante = com.IdComprobante;
                r.IdTipoMovimiento = com.IdTipoMovimiento;
                r.AfectaCuentaInversion = com.AfectaCuentaInversion;
                r.IdUsuario = in.p_IdUsuario;
                r.UsaReglasNegocio = in.p_UsaReglasNegocio;
                r.FechaMovimiento = com.FechaMovimiento;
                for (FormaDePagoLiquidar_Entrada fp : in.p_FormaDePagoLiquidar) {
                    if (fp.IdComprobante.equals(r.IdComprobante)) {
                        r.IdComprobanteFormaDePago = fp.IdComprobanteFormaDePago;
                    }
                }
                // the legacy compared with the default equals (identity): never a duplicate
                requieren.add(r);
            }
        }
        if (requieren.isEmpty()) {
            return;
        }

        CrearEventoAutorizacionMasivo.ServiceEntrada entrada = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entrada.P_IDEMPRESA = in.p_IdEmpresa;
        entrada.P_IDUSUARIO = in.p_IdUsuario;
        entrada.P_IDMODULO = in.p_IdModulo;
        entrada.P_SIMULAROPERACION = "N";
        entrada.P_USAREGLASNEGOCIO = "N";
        long idUnion = 0;
        for (ComprobanteRequiereAutorizar com : requieren) {
            for (String codigo : autorizaciones.get(com.IdComprobante)) {
                if (codigo.equalsIgnoreCase("COMPROBANTE_TESORERIA_AUTORIZAR_FECHA")) {
                    agregarEvento(entrada, ++idUnion, "ALFDDS", "La fecha de liquidación del comprobante no coincide con la fecha de sistema", in, com);
                }
                if (codigo.equalsIgnoreCase("COMPROBANTE_TESORERIA_AUTORIZAR_SALDO")) {
                    agregarEvento(entrada, ++idUnion, "ALFSTN", "El flujo de caja es negativo", in, com);
                }
            }
        }

        CrearEventoAutorizacionMasivo.ServiceSalida salida = Colaborador.invocar(colaboradores, "CrearEventoAutorizacionMasivo", entrada,
                CrearEventoAutorizacionMasivo.ServiceSalida.class);
        List<MensajeSalida> advertencias = salida.P_OUT_ADVERTENCIASDETALLE.stream()
                .map(a -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, a.Codigo, a.Descripcion)).collect(Collectors.toList());
        List<MensajeSalida> errores = salida.P_OUT_ERRORESDETALLE.stream()
                .map(e -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, e.Codigo, e.Descripcion)).collect(Collectors.toList());
        out.P_OUT_MENSAJES.addAll(advertencias);
        out.P_OUT_MENSAJES.addAll(errores);
        if (!advertencias.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION",
                    diccionario.mensaje(in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<>())));
        }
        if (!errores.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_CREAR_AUTORIZACION",
                    diccionario.mensaje(in.p_IdEmpresa, "ERROR_AL_CREAR_AUTORIZACION", new HashMap<>())));
        }
        for (ComprobanteRequiereAutorizar com : requieren) {
            out.P_OUT_RESULTADOPROCESO.add(resultado("IdComprobante", com.IdComprobante, SERVICIO, "PDA"));
        }
        LiquidarComprobanteSql.lote(jdbc, SQL_UPDATE_COMPROBANTE_REQUIERE_AUTORIZAR, requieren);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE, requieren);
    }

    private static void agregarEvento(CrearEventoAutorizacionMasivo.ServiceEntrada entrada, long idUnion, String tipoEvento, String motivo,
            LiquidarComprobante_Entrada in, ComprobanteRequiereAutorizar com) {
        CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
        evento.IdTipoEvento = tipoEvento;
        evento.IdUnion = idUnion;
        evento.IdModuloSolicita = in.p_IdModulo;
        evento.IdUsuarioSolicita = in.p_IdUsuario;
        evento.Fecha = in.p_FechaMovimiento;
        evento.IdConcepto = tipoEvento;
        evento.NumeroOperacionConcepto = com.IdComprobante.toString();
        evento.FechaOperacionConcepto = in.p_FechaMovimiento;
        evento.Glosa = com.IdComprobante + " / " + com.IdTipoMovimiento + " / " + motivo;
        entrada.P_TABLAEVENTOS.add(evento);
        // LEGACY DEFECT kept (see README TES-A-126): IdTipoMovimiento is declared as LONG although it is text
        detalleEvento(entrada, idUnion, "IdComprobante", "LONG", com.IdComprobante.toString());
        detalleEvento(entrada, idUnion, "IdTipoMovimiento", "LONG", com.IdTipoMovimiento);
        detalleEvento(entrada, idUnion, "AfectaCuentaInversion", "STRING", com.AfectaCuentaInversion);
        detalleEvento(entrada, idUnion, "IdUsuario", "LONG", com.IdUsuario.toString());
        detalleEvento(entrada, idUnion, "UsaReglasNegocio", "STRING", com.UsaReglasNegocio);
        detalleEvento(entrada, idUnion, "FechaMovimiento", "STRING", com.FechaMovimiento);
    }

    private static void detalleEvento(CrearEventoAutorizacionMasivo.ServiceEntrada entrada, long idUnion, String campo, String tipo, String valor) {
        CrearEventoAutorizacionMasivo.ObjTABLADETALLE detalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
        detalle.IdUnion = idUnion;
        detalle.Clasificacion = "1"; // this value goes at header level
        detalle.NombreCampo = campo;
        detalle.TipoDato = tipo;
        detalle.Valor = valor.toString();
        entrada.P_DETALLE.add(detalle);
    }

    // ------------------------------------------------------------------ banks and cuenta de inversion

    /** @return false when the bank balance update reported errors (the process stops there). */
    private boolean actualizarSaldosBancarios(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out) {
        List<ActualizarSaldosCuentaBanco.CuentaBanco_Entrada> cuentas = jdbc.query(SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR, Map.of(),
                Filas.de(ActualizarSaldosCuentaBanco.CuentaBanco_Entrada.class));
        if (cuentas.isEmpty()) {
            return true;
        }
        ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada entrada = new ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada();
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_CuentaBanco = cuentas;
        ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida salida = Colaborador.invocar(colaboradores, "ActualizarSaldosCuentaBanco", entrada,
                ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida.class);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out.P_OUT_ERRORESCONTADOR.compareTo(0L) <= 0;
    }

    /** Sends the movements to the cuenta de inversion (client and/or broker according to the comprobante). @return false when it ends with errors. */
    private boolean generarCargosAbonos(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, String liquidacionParcialEnviaACTA) throws Exception {
        ParametrosConfiguracion parametros = new ParametrosConfiguracion();
        parametros.LiquidacionParcialEnviaACTA = liquidacionParcialEnviaACTA;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION, List.of(parametros));

        List<CrearCargoAbono.ObjCargoAbonoAGenerar> cargosAbonos = jdbc.query(SQL_SELECT_CARGO_ABONO_A_GENERAR,
                Map.of("IdEmpresa", in.p_IdEmpresa, "FechaMovimiento", in.p_FechaMovimiento), Filas.de(CrearCargoAbono.ObjCargoAbonoAGenerar.class));
        if (cargosAbonos.isEmpty()) {
            return true;
        }
        if (!validarFlujoDeCaja(in, out, cargosAbonos)) {
            return false;
        }

        long numero = 0;
        for (CrearCargoAbono.ObjCargoAbonoAGenerar cargo : cargosAbonos) {
            cargo.NumeroCargoAbono = ++numero;
        }
        CrearCargoAbono.CrearCargoAbonoEntrada entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = "TES";
        entrada.p_simularOperacion = in.p_SimularOperacion;
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_FechaSistema = in.p_FechaMovimiento;
        entrada.p_EsIngresoManual = "N";
        entrada.p_CargoAbonoAGenerar = cargosAbonos;
        entrada.p_CargoAbonoAGenerar.get(0).FolioComprobante = in.FolioCSC;
        CrearCargoAbono.CrearCargoAbonoSalida salida = Colaborador.invocar(colaboradores, "CrearCargoAbono", entrada, CrearCargoAbono.CrearCargoAbonoSalida.class);

        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        for (CrearCargoAbono.ObjCargoAbonoGenerado generado : salida.P_OUT_CARGOABONOGENERADO) {
            out.P_OUT_RESULTADOPROCESO.add(resultado("IdCargoAbonoMovimiento", generado.IdCargoAbonoMovimiento, "cl.bcs.cuentaInversion.CrearCargoAbono", "CRT"));
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out.P_OUT_ERRORESCONTADOR.compareTo(0L) <= 0;
    }

    /**
     * When the company asks for it ({@code CUENTAINVERSION/VALIDAFLUJOCAJAENCREARCARGOABONO = S}) the egresses (MOVCAR) to be sent are compared with the
     * cash flow of their portafolio and date. @return false when a shortfall was reported.
     */
    private boolean validarFlujoDeCaja(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, List<CrearCargoAbono.ObjCargoAbonoAGenerar> cargosAbonos)
            throws Exception {
        // a missing parameter means "do not validate"
        String validaFlujoCaja = Objects.requireNonNullElse(configuracion.parametro(in.p_IdEmpresa, Parametros.CUENTAINVERSION,
                "VALIDAFLUJOCAJAENCREARCARGOABONO"), "N");
        Set<String> conceptosQueIgnoran = conceptosIgnoranValidacionFlujoCaja(in.p_IdEmpresa, cargosAbonos);
        List<CrearCargoAbono.ObjCargoAbonoAGenerar> cargosQueValidan = cargosAbonos.stream().filter(c -> !conceptosQueIgnoran.contains(c.IdConcepto)).toList();
        if (!"S".equals(validaFlujoCaja) || cargosQueValidan.isEmpty()) {
            return true;
        }

        java.sql.Date fechaMinima = Fecha.toDate(cargosQueValidan.get(0).FechaCondicionDeLiquidacion);
        java.sql.Date fechaMaxima = fechaMinima;
        // LEGACY DEFECT kept (see README TES-A-155): the portafolios were collected in a HashSet whose elements use identity equality, so none is deduplicated
        List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolios = new ArrayList<>();
        Map<FlujoCajaElem, BigDecimal> egresos = new HashMap<>();
        for (CrearCargoAbono.ObjCargoAbonoAGenerar cargo : cargosQueValidan) {
            if ("MOVCAR".equals(cargo.IdTipoMovimiento)) {
                java.sql.Date fecha = Fecha.toDate(cargo.FechaCondicionDeLiquidacion);
                if (fecha.before(fechaMinima)) {
                    fechaMinima = fecha;
                }
                if (fecha.after(fechaMaxima)) {
                    fechaMaxima = fecha;
                }
                egresos.merge(new FlujoCajaElem(cargo.IdMoneda, cargo.FechaCondicionDeLiquidacion, cargo.IdPortafolio), cargo.Monto, BigDecimal::add);
                SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR p = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
                p.IdMoneda = cargo.IdMoneda;
                p.IdPortafolio = cargo.IdPortafolio;
                portafolios.add(p);
            }
        }
        if (portafolios.isEmpty()) {
            return true;
        }

        SaldoFlujoCaja.ServiceEntrada entradaFlujo = new SaldoFlujoCaja.ServiceEntrada();
        entradaFlujo.P_IDEMPRESA = in.p_IdEmpresa;
        entradaFlujo.P_FECHADESDE = Fecha.toStringDate(fechaMinima);
        entradaFlujo.P_FECHAHASTA = Fecha.toStringDate(fechaMaxima);
        entradaFlujo.P_USO = "LIQUIDACION_TESORERIA";
        entradaFlujo.P_PORTAFOLIOACONSULTAR = portafolios;
        SaldoFlujoCaja.ServiceSalida salidaFlujo = Colaborador.invocar(colaboradores, "SaldoFlujoCaja", entradaFlujo, SaldoFlujoCaja.ServiceSalida.class);
        Map<FlujoCajaElem, BigDecimal> flujo = new HashMap<>();
        for (SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujo.P_OUT_FLUJOCAJA) {
            flujo.merge(new FlujoCajaElem(f.IdMoneda, f.FechaSaldo, f.IdPortafolio), f.Saldo, BigDecimal::add);
        }

        List<Long> idsPortafolio = cargosQueValidan.stream().map(c -> c.IdPortafolio).collect(Collectors.toList());
        Map<Long, DatoPortafolio> datosPortafolio = jdbc.query(SQL_SELECT_DATOS_PORTAFOLIO, Map.of("IdPortafolio", idsPortafolio), Filas.de(DatoPortafolio.class))
                .stream().collect(Collectors.toMap(p -> p.IdPortafolio, p -> p));
        for (CrearCargoAbono.ObjCargoAbonoAGenerar cargo : cargosQueValidan) {
            if ("MOVCAR".equals(cargo.IdTipoMovimiento)) {
                FlujoCajaElem llave = new FlujoCajaElem(cargo.IdMoneda, cargo.FechaCondicionDeLiquidacion, cargo.IdPortafolio);
                BigDecimal totalEgresos = egresos.get(llave);
                BigDecimal totalFlujo = flujo.get(llave);
                if (validaSaldos(in.p_IdEmpresa, cargosAbonos) && totalEgresos.compareTo(totalFlujo) == 1) {
                    saldoInsuficiente(in.p_IdEmpresa, cargo, datosPortafolio.get(cargo.IdPortafolio), out.P_OUT_MENSAJES);
                }
            }
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out.P_OUT_ERRORESCONTADOR <= 0;
    }

    private void saldoInsuficiente(Long empresa, CrearCargoAbono.ObjCargoAbonoAGenerar cargo, DatoPortafolio datos, List<MensajeSalida> mensajes) {
        Map<String, String> valores = valores("IdComprobante", cargo.NumeroCargoAbono, "Monto", cargo.Monto, "IdMoneda", cargo.IdMoneda,
                "DocumentoIdentidad", datos.DocumentoIdentidad, "NombreCompleto", "IdPortafolio " + datos.IdPortafolio + " " + datos.NombreCompleto);
        mensajes.add(new MensajeSalida(cargo.NumeroCargoAbono, MensajeSalida.MENSAJE_ERROR, "SALDO_CAJA_INSUFICIENTE",
                diccionario.mensaje(empresa, "SALDO_CAJA_INSUFICIENTE", valores)));
    }

    /**
     * Concepts whose tesoreria indicator "no validar saldo de caja" is S and that the company lists in
     * {@code PLATAFORMA/PLA.ConceptosCheckSinValidacionFlujoCaja} (separated by {@code ;}).
     */
    private Set<String> conceptosIgnoranValidacionFlujoCaja(Long empresa, List<CrearCargoAbono.ObjCargoAbonoAGenerar> cargosAbonos) {
        Map<String, String> indicador = jdbc.query(SQL_SELECT_CONCEPTO_TESORERIA,
                Map.of("ConceptoIds", cargosAbonos.stream().map(c -> c.IdConcepto).collect(Collectors.toList())), Filas.de(ConceptoTesoreria.class))
                .stream().collect(Collectors.toMap(c -> c.IdConcepto, c -> c.IndicadorNoValidaSaldoCaja));
        String habilitados = configuracion.parametro(empresa, Parametros.PLATAFORMA, "PLA.ConceptosCheckSinValidacionFlujoCaja");
        Set<String> conceptosHabilitados = habilitados == null ? Collections.emptySet() : new HashSet<>(List.of(habilitados.split(";")));
        return conceptosHabilitados.stream().filter(c -> "S".equals(indicador.get(c))).collect(Collectors.toSet());
    }

    /**
     * Whether the cash flow is validated at all: false when ANY of the concepts is flagged "no validar saldo de caja".
     * LEGACY DEFECT kept (see README TES-A-157): the indicator of one concept switches the check off for every cargo of the liquidation.
     */
    private boolean validaSaldos(Long empresa, List<CrearCargoAbono.ObjCargoAbonoAGenerar> cargosAbonos) {
        return jdbc.query(SQL_SELECT_CONCEPTO_TESORERIA, Map.of("ConceptoIds", cargosAbonos.stream().map(c -> c.IdConcepto).collect(Collectors.toList())),
                Filas.de(ConceptoTesoreria.class)).stream().filter(c -> c.IndicadorNoValidaSaldoCaja.equals("S"))
                .collect(Collectors.toMap(c -> c.IdConcepto, c -> c.IndicadorNoValidaSaldoCaja)).isEmpty();
    }

    // ------------------------------------------------------------------ helpers

    private static ResultadoProceso_Salida resultado(String campo, Long id, String servicio, String resultado) {
        ResultadoProceso_Salida r = new ResultadoProceso_Salida();
        r.Campo = campo;
        r.Id = id;
        r.Servicio = servicio;
        r.Resultado = resultado;
        return r;
    }

    /** The session tables are always emptied, also when the process fails or returns early. */
    private void limpiarTablasDeTrabajo(List<Comprobante> comprobantesAProcesar) throws SQLException {
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_DETALLE, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTES_A_LIQUIDAR, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTES_MUEVEN_CI, comprobantesAProcesar);
    }
}
