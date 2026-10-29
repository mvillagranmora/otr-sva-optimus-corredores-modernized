package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.CrearComprobanteService.ComprobanteGenerar_Entrada;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.CrearComprobante_Entrada;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.CrearComprobante_Salida;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.ResultadoProceso_Salida;
import cl.bcs.tesoreria.javacall.GirarComprobanteIngresoService.CuentasBancarias;
import cl.bcs.tesoreria.javacall.GirarComprobanteIngresoService.ParametrosUpdateCuentasBancarias;
import cl.bcs.tesoreria.javacall.GirarComprobanteIngresoService.SalidaGirar;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.GirarComprobantesEgreso}, behaviours TES-A-580..592: gives out ("gira") comprobantes of egreso. As the Ingreso it
 * stages the comprobantes and the payment media, writes the corredora account, asks {@link CrearComprobanteService} to build the partida de egreso
 * (PAREGR), assigns the PAREGR to the forms of payment and, on request, creates the nomina or the transferencia of the PAREGR.
 *
 * <p>LEGACY DEFECT kept (see README TES-A-585, 590, 591): every exception is swallowed and an empty result is returned, while the rows already
 * written stay (the platform commits when the service returns). Also (README TES-A-588): the forms of payment are updated and the nomina is created
 * even when {@code CrearComprobante} answered errors; and {@code p_UsaReglasNegocio} is forced to {@code N}.
 */
@Service
public class GirarComprobantesEgresoService {

    private static final Logger log = LoggerFactory.getLogger(GirarComprobantesEgresoService.class);

    private static final String TABLA_TEMPORAL = "#RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar";
    private static final String SQL_CREAR_TABLA = "CREATE LOCAL TEMPORARY TABLE " + TABLA_TEMPORAL + " (\"IdComprobante\" BIGINT)";
    private static final String SQL_INSERTAR_REGISTROS = "INSERT INTO " + TABLA_TEMPORAL + " (\"IdComprobante\") VALUES (:IdComprobante)";
    private static final String SQL_VACIAR = "DELETE FROM " + TABLA_TEMPORAL + " WHERE 1=1";
    private static final String SQL_ELIMINAR_TABLA = "DROP TABLE " + TABLA_TEMPORAL;

    private static final String SQL_AGRUPAR_COMPROBANTES = loadSql("sql/GirarComprobanteEgreso_InsertCrearComprobante_Entrada_ComprobanteGenerar.sql");
    private static final String SQL_VACIAR_INGRESO_EGRESO = loadSql("sql/GirarComprobanteIngreso_InsertIngresoEgresoGenerarVacio.sql");
    private static final String SQL_INSERTAR_FORMA_DE_PAGO = loadSql("sql/GirarComprobanteIngreso_InsertCrearComprobantesFormaDePago.sql");
    private static final String SQL_CUENTAS_BANCARIAS = loadSql("sql/GirarComprobanteIngreso_SelectConsultarCuentasBancarias.sql");
    private static final String SQL_ACTUALIZAR_FORMAS_DE_PAGO = loadSql("sql/GirarComprobanteIngreso_UpdateComprobantesFormaDePago.sql");
    private static final String SQL_ASIGNAR_PAREGR = loadSql("sql/GirarComprobanteEgreso_UpdateFormaPagoPAREGR.sql");
    private static final String SQL_SIGUIENTE_ID_NOMINA = loadSql("sql/GirarComprobanteEgreso_SelectSiguienteIdNomina.sql");
    private static final String SQL_INSERTAR_NOMINA = loadSql("sql/GirarComprobanteEgreso_InsertGeneraNomina.sql");
    private static final String SQL_INSERTAR_TRANSFERENCIA = loadSql("sql/GirarComprobanteEgreso_InsertGeneraTransferencia.sql");
    private static final String SQL_INSERTAR_DETALLE = loadSql("sql/GirarComprobanteEgreso_InsertGeneraNominaDetalle.sql");
    private static final String SQL_VACIAR_COMPROBANTES = loadSql("sql/GirarComprobanteEgreso_DeleteComprobanteGenerar.sql");

    private static final String RESULTADO_INGRESADO = "ING";

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaGirarComprobantesEgreso extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public Long p_IdCaja;
        public String p_FechaMovimiento;
        public String p_IdMedioDePago;
        public Long p_IdCuentaBancariaCorredora;
        public String p_IdModalidadDePago;
        public String p_IdMoneda;
        public Long p_monto;
        public String p_UsaTalonarioCheque;
        public String p_GeneraNomina;
        public String p_GeneraTransferencia;
        public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
        public List<FormaDePago_Entrada> p_FormasDePago;
    }

    public static class SalidaGirarComprobantesEgreso extends SalidaGirar {
    }

    public static class ParametrosUpdateFormaPago extends BcsPojo {
        public Long IdUsuario;
        public Long IdPAREGR;
        public String IdMedioDePago;
        public String FechaGiro;
        public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
    }

    public static class ParametroSelectIdNomina extends BcsPojo {
        public Long v_IdNominaTransferencia;
    }

    public static class ParametrosGeneraNomina extends BcsPojo {
        public Long v_IdNominaTransferencia;
        public Long p_IdEmpresa;
        public Long v_IdComprobantePartidaEgreso;
        public String p_FechaMovimiento;
        public String p_out_FechaLiquidacion;
        public String p_IdMedioDePago;
        public String p_IdModalidadDePago;
        public Long p_IdCuentaBancariaCorredora;
        public String v_IdTipoCuentaBancariaCorredora;
        public String p_out_IdMoneda;
        public Long p_Monto;
        public Long p_IdUsuario;
        public String p_UsaTalonarioCheque;
    }

    public static class ParametrosGeneraNominaDetalle extends BcsPojo {
        public Long v_IdNominaTransferencia;
        public Long p_IdUsuario;
        public String p_IdMedioDePago;
        public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearComprobanteService crearComprobante;

    public GirarComprobantesEgresoService(NamedParameterJdbcTemplate jdbc, CrearComprobanteService crearComprobante) {
        this.jdbc = jdbc;
        this.crearComprobante = crearComprobante;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaGirarComprobantesEgreso in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaGirarComprobantesEgreso.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaGirarComprobantesEgreso logica(EntradaGirarComprobantesEgreso in) {
        SalidaGirarComprobantesEgreso salida = new SalidaGirarComprobantesEgreso();
        try {
            cargarTablasGlobales(in);
            List<CuentasBancarias> cuentas = actualizarCuentaDeLaCorredora(in);

            CrearComprobante_Salida creado = crearComprobante.logica(entradaCrearComprobante(in));
            long idPartida = idPartidaEgreso(creado);
            // the PAREGR has just been created: the comprobantes are staged again for the update of their forms of payment
            GirarComprobanteLote.ejecutar(jdbc, SQL_AGRUPAR_COMPROBANTES, in.p_ComprobantesAGirar);
            asignarPartida(in, idPartida);

            if (in.p_GeneraNomina.equalsIgnoreCase("S")) {
                crearNomina(in, idPartida, cuentas, SQL_INSERTAR_NOMINA);
            }
            if (in.p_GeneraTransferencia.equalsIgnoreCase("S")) {
                crearNomina(in, idPartida, cuentas, SQL_INSERTAR_TRANSFERENCIA);
            }

            jdbc.getJdbcOperations().execute(SQL_VACIAR_COMPROBANTES);
            jdbc.getJdbcOperations().execute(SQL_VACIAR);
            jdbc.getJdbcOperations().execute(SQL_ELIMINAR_TABLA);
            if (MensajeSalida.numeroErrores(creado.P_OUT_MENSAJES) > 0) {
                salida.P_OUT_MENSAJES = creado.P_OUT_MENSAJES;
                return salida;
            }
            salida.P_OUT_MENSAJES.addAll(creado.P_OUT_MENSAJES);
        } catch (Exception e) {
            log.error("GirarComprobantesEgreso failed; the legacy swallows it and answers an empty result", e);
        }
        return salida;
    }

    private void cargarTablasGlobales(EntradaGirarComprobantesEgreso in) throws Exception {
        GirarComprobanteLote.ejecutar(jdbc, SQL_AGRUPAR_COMPROBANTES, in.p_ComprobantesAGirar);
        GirarComprobanteLote.ejecutar(jdbc, SQL_VACIAR_INGRESO_EGRESO, in.p_ComprobantesAGirar);
        jdbc.getJdbcOperations().execute(SQL_CREAR_TABLA);
        GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_REGISTROS, in.p_ComprobantesAGirar);

        FormaDePago_Entrada agrupada = new FormaDePago_Entrada();
        agrupada.IdMedioDePago = in.p_IdMedioDePago;
        agrupada.IdModalidadDePago = in.p_IdModalidadDePago;
        agrupada.IdMoneda = in.p_IdMoneda;
        agrupada.Monto = new BigDecimal(in.p_monto);
        agrupada.IdCuentaBancariaCorredora = in.p_IdCuentaBancariaCorredora;
        in.p_FormasDePago.add(agrupada);
        GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_FORMA_DE_PAGO, in.p_FormasDePago);
    }

    private List<CuentasBancarias> actualizarCuentaDeLaCorredora(EntradaGirarComprobantesEgreso in) throws Exception {
        List<CuentasBancarias> cuentas = jdbc.query(SQL_CUENTAS_BANCARIAS, Map.of("p_IdCuentaBancariaCorredora", in.p_IdCuentaBancariaCorredora),
                Filas.de(CuentasBancarias.class));
        if (!cuentas.isEmpty()) {
            List<ParametrosUpdateCuentasBancarias> parametros = cuentas.stream().map(cuenta -> {
                ParametrosUpdateCuentasBancarias p = new ParametrosUpdateCuentasBancarias();
                p.IdCuentaBancaria = in.p_IdCuentaBancariaCorredora;
                p.IdMedioDePago = in.p_IdMedioDePago;
                p.IdTipoCuentaBancaria = cuenta.IdTipoCuentaBancaria;
                p.NombreCuentaBancaria = cuenta.NombreCuentaBancaria;
                p.NombreTipoCuentaBancaria = cuenta.NombreTipoCuentaBancaria;
                p.NumeroCuentaBancaria = cuenta.NumeroCuentaBancaria;
                return p;
            }).toList();
            GirarComprobanteLote.ejecutar(jdbc, SQL_ACTUALIZAR_FORMAS_DE_PAGO, parametros);
        }
        return cuentas;
    }

    private static CrearComprobante_Entrada entradaCrearComprobante(EntradaGirarComprobantesEgreso in) {
        CrearComprobante_Entrada e = new CrearComprobante_Entrada();
        e.p_IdEmpresa = in.p_IdEmpresa;
        e.p_IdUsuario = in.p_IdUsuario;
        e.p_IdModulo = in.p_IdModulo;
        e.p_SimularOperacion = in.p_simularOperacion;
        e.p_UsaReglasNegocio = "N"; // LEGACY DEFECT kept (see README TES-A-580): the requested p_UsaReglasNegocio is ignored
        e.p_FechaMovimiento = in.p_FechaMovimiento;
        e.p_IdCaja = in.p_IdCaja;
        e.p_GenerarComprobanteDesdeMovimientos = GirarComprobanteIngresoService.COMPROBANTE_DESDE_MOVIMIENTOS;
        e.p_GenerarComprobanteDesdeComprobantes = GirarComprobanteIngresoService.COMPROBANTE_DESDE_COMPROBANTES;
        e.p_EsIngresoAutomatico = GirarComprobanteIngresoService.ES_INGRESO_AUTOMATICO;
        e.p_ComprobanteGenerar = in.p_ComprobantesAGirar;
        e.p_FormaDePago = in.p_FormasDePago;
        return e;
    }

    /** Id of the PAREGR: the first process result with {@code Resultado = ING}, 0 when there is none (a null Id unboxes to NPE as the legacy). */
    private static long idPartidaEgreso(CrearComprobante_Salida creado) {
        for (ResultadoProceso_Salida r : creado.P_OUT_RESULTADOPROCESO) {
            if (RESULTADO_INGRESADO.equals(r.Resultado)) {
                return r.Id;
            }
        }
        return 0L;
    }

    private void asignarPartida(EntradaGirarComprobantesEgreso in, long idPartida) throws Exception {
        ParametrosUpdateFormaPago p = new ParametrosUpdateFormaPago();
        p.IdUsuario = in.p_IdUsuario;
        p.IdPAREGR = idPartida;
        p.IdMedioDePago = in.p_IdMedioDePago;
        p.p_ComprobantesAGirar = in.p_ComprobantesAGirar;
        p.FechaGiro = in.p_FechaMovimiento;
        GirarComprobanteLote.ejecutar(jdbc, SQL_ASIGNAR_PAREGR, List.of(p));
    }

    /** Creates the header (nomina or transferencia, depending on {@code sqlCabecera}) and its detail for the PAREGR. */
    private void crearNomina(EntradaGirarComprobantesEgreso in, long idPartida, List<CuentasBancarias> cuentas, String sqlCabecera) throws Exception {
        List<ParametroSelectIdNomina> ids = jdbc.query(SQL_SIGUIENTE_ID_NOMINA, Map.of(), Filas.de(ParametroSelectIdNomina.class));
        Long idNomina = ids.get(0).v_IdNominaTransferencia;

        ParametrosGeneraNomina cabecera = new ParametrosGeneraNomina();
        cabecera.v_IdNominaTransferencia = idNomina;
        cabecera.p_IdEmpresa = in.p_IdEmpresa;
        cabecera.v_IdComprobantePartidaEgreso = idPartida;
        cabecera.p_FechaMovimiento = in.p_FechaMovimiento;
        cabecera.p_out_FechaLiquidacion = in.p_FechaMovimiento;
        cabecera.p_IdMedioDePago = in.p_IdMedioDePago;
        cabecera.p_IdModalidadDePago = in.p_IdModalidadDePago;
        cabecera.p_IdCuentaBancariaCorredora = in.p_IdCuentaBancariaCorredora;
        cabecera.v_IdTipoCuentaBancariaCorredora = cuentas.get(0).IdTipoCuentaBancaria;
        cabecera.p_out_IdMoneda = in.p_IdMoneda;
        cabecera.p_Monto = in.p_monto;
        cabecera.p_IdUsuario = in.p_IdUsuario;
        cabecera.p_UsaTalonarioCheque = in.p_UsaTalonarioCheque;
        GirarComprobanteLote.ejecutar(jdbc, sqlCabecera, List.of(cabecera));

        ParametrosGeneraNominaDetalle detalle = new ParametrosGeneraNominaDetalle();
        detalle.v_IdNominaTransferencia = idNomina;
        detalle.p_IdUsuario = in.p_IdUsuario;
        detalle.p_IdMedioDePago = in.p_IdMedioDePago;
        GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_DETALLE, List.of(detalle));
    }
}
