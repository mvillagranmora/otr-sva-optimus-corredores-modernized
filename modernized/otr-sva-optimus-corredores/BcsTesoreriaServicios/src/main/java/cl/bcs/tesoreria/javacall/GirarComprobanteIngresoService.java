package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.GirarComprobanteIngreso}, behaviours TES-A-560..573: gives out ("gira") comprobantes of ingreso. It stages the
 * comprobantes and the payment media in the global tables of {@code CrearComprobante}, writes the corredora account into the forms of payment,
 * asks {@link CrearComprobanteService} to build the partida de ingreso (PARING) and leaves the comprobantes in state GIR.
 *
 * <p>LEGACY DEFECT kept (see README TES-A-567, 568, 585, 590, 591): every exception is swallowed and an empty result is returned, while the rows
 * already written stay (the platform commits when the service returns). Also (README TES-A-562): the comprobantes are set to GIR even when
 * {@code CrearComprobante} answered errors; and (README, all cases): {@code p_UsaReglasNegocio} is forced to {@code N} for {@code CrearComprobante}.
 */
@Service
public class GirarComprobanteIngresoService {

    private static final Logger log = LoggerFactory.getLogger(GirarComprobanteIngresoService.class);

    private static final String TABLA_TEMPORAL = "#RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar";
    private static final String SQL_CREAR_TABLA = "CREATE LOCAL TEMPORARY TABLE " + TABLA_TEMPORAL + " (\"IdComprobante\" BIGINT)";
    private static final String SQL_INSERTAR_REGISTROS = "INSERT INTO " + TABLA_TEMPORAL + " (\"IdComprobante\") VALUES (:IdComprobante)";
    private static final String SQL_VACIAR = "DELETE FROM " + TABLA_TEMPORAL + " WHERE 1=1";
    private static final String SQL_ELIMINAR_TABLA = "DROP TABLE " + TABLA_TEMPORAL;

    private static final String SQL_VACIAR_INGRESO_EGRESO = loadSql("sql/GirarComprobanteIngreso_InsertIngresoEgresoGenerarVacio.sql");
    private static final String SQL_AGRUPAR_COMPROBANTES = loadSql("sql/GirarComprobanteIngreso_InsertCrearComprobante_Entrada_ComprobanteGenerar.sql");
    private static final String SQL_INSERTAR_FORMA_DE_PAGO = loadSql("sql/GirarComprobanteIngreso_InsertCrearComprobantesFormaDePago.sql");
    private static final String SQL_CUENTAS_BANCARIAS = loadSql("sql/GirarComprobanteIngreso_SelectConsultarCuentasBancarias.sql");
    private static final String SQL_ACTUALIZAR_FORMAS_DE_PAGO = loadSql("sql/GirarComprobanteIngreso_UpdateComprobantesFormaDePago.sql");
    private static final String SQL_ACTUALIZAR_ESTADO = loadSql("sql/GirarComprobanteIngreso_UpdateComprobantesEstado.sql");

    static final String COMPROBANTE_DESDE_MOVIMIENTOS = "N";
    static final String COMPROBANTE_DESDE_COMPROBANTES = "S";
    static final String ES_INGRESO_AUTOMATICO = "S";

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaGirarComprobanteIngreso extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
        public String p_GeneraNomina;
        public String p_GeneraTransferencia;
        public String p_IdMedioDePago;
        public Long p_IdCuentaBancariaCorredora;
        public List<FormaDePago_Entrada> p_FormasDePago;
        public Long p_monto;
        public String p_usaTalonarioCheque;
        public String p_IdModalidadDePago;
        public String p_IdMoneda;
        public Long p_IdCaja;
    }

    public static class InformacionDetalle {
        public String Codigo;
        public String Descripcion;
    }

    /** Result shared by the Ingreso and the Egreso (identical in the legacy). */
    public static class SalidaGirar extends BcsPojo {
        public int p_out_advertenciascontador;
        public List<CrearComprobante_Salida> P_OUT_COMPROBANTESCREADOS = new ArrayList<>();
        public List<InformacionDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Long> P_OUT_SECUENCIA = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class SalidaGirarComprobanteIngreso extends SalidaGirar {
    }

    public static class CuentasBancarias extends BcsPojo {
        public String IdTipoCuentaBancaria;
        public String NombreTipoCuentaBancaria;
        public String NombreCuentaBancaria;
        public String NumeroCuentaBancaria;
    }

    public static class Comprobantes extends BcsPojo {
        public Long IdComprobante;
        public String IdEstado;
    }

    public static class ParametrosUpdateCuentasBancarias extends BcsPojo {
        public Long IdComprobante;
        public String IdTipoCuentaBancaria;
        public Long IdCuentaBancaria;
        public String NombreTipoCuentaBancaria;
        public String NombreCuentaBancaria;
        public String NumeroCuentaBancaria;
        public String IdMedioDePago;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearComprobanteService crearComprobante;

    public GirarComprobanteIngresoService(NamedParameterJdbcTemplate jdbc, CrearComprobanteService crearComprobante) {
        this.jdbc = jdbc;
        this.crearComprobante = crearComprobante;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaGirarComprobanteIngreso in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaGirarComprobanteIngreso.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaGirarComprobanteIngreso logica(EntradaGirarComprobanteIngreso entrada) {
        SalidaGirarComprobanteIngreso salida = new SalidaGirarComprobanteIngreso();
        try {
            cargarTablasGlobales(entrada);
            actualizarCuentaDeLaCorredora(entrada);
            CrearComprobante_Salida creado = crearComprobante.logica(entradaCrearComprobante(entrada));
            marcarComoGirados(entrada);
            jdbc.getJdbcOperations().execute(SQL_VACIAR);
            jdbc.getJdbcOperations().execute(SQL_ELIMINAR_TABLA);
            if (MensajeSalida.numeroErrores(creado.P_OUT_MENSAJES) > 0) {
                salida.P_OUT_MENSAJES = creado.P_OUT_MENSAJES;
                return salida;
            }
            salida.P_OUT_MENSAJES.addAll(creado.P_OUT_MENSAJES);
        } catch (Exception e) {
            log.error("GirarComprobanteIngreso failed; the legacy swallows it and answers an empty result", e);
        }
        return salida;
    }

    private void cargarTablasGlobales(EntradaGirarComprobanteIngreso entrada) throws Exception {
        GirarComprobanteLote.ejecutar(jdbc, SQL_AGRUPAR_COMPROBANTES, entrada.p_ComprobantesAGirar);
        GirarComprobanteLote.ejecutar(jdbc, SQL_VACIAR_INGRESO_EGRESO, entrada.p_ComprobantesAGirar);
        jdbc.getJdbcOperations().execute(SQL_CREAR_TABLA);
        GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_REGISTROS, entrada.p_ComprobantesAGirar);

        FormaDePago_Entrada agrupada = new FormaDePago_Entrada();
        agrupada.IdMedioDePago = entrada.p_IdMedioDePago;
        agrupada.IdModalidadDePago = entrada.p_IdModalidadDePago;
        agrupada.IdMoneda = entrada.p_IdMoneda;
        agrupada.Monto = new BigDecimal(entrada.p_monto);
        agrupada.IdCuentaBancariaCorredora = entrada.p_IdCuentaBancariaCorredora;
        entrada.p_FormasDePago.add(agrupada);
        GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_FORMA_DE_PAGO, entrada.p_FormasDePago);
    }

    /** Writes the data of the corredora account into the live forms of payment of the medium (nothing happens if the account is unknown). */
    private void actualizarCuentaDeLaCorredora(EntradaGirarComprobanteIngreso entrada) throws Exception {
        List<CuentasBancarias> cuentas = jdbc.query(SQL_CUENTAS_BANCARIAS, Map.of("p_IdCuentaBancariaCorredora", entrada.p_IdCuentaBancariaCorredora),
                Filas.de(CuentasBancarias.class));
        if (cuentas.isEmpty()) {
            return;
        }
        List<ParametrosUpdateCuentasBancarias> parametros = cuentas.stream().map(cuenta -> {
            ParametrosUpdateCuentasBancarias p = new ParametrosUpdateCuentasBancarias();
            p.IdCuentaBancaria = entrada.p_IdCuentaBancariaCorredora;
            p.IdMedioDePago = entrada.p_IdMedioDePago;
            p.IdTipoCuentaBancaria = cuenta.IdTipoCuentaBancaria;
            p.NombreCuentaBancaria = cuenta.NombreCuentaBancaria;
            p.NombreTipoCuentaBancaria = cuenta.NombreTipoCuentaBancaria;
            p.NumeroCuentaBancaria = cuenta.NumeroCuentaBancaria;
            return p;
        }).toList();
        GirarComprobanteLote.ejecutar(jdbc, SQL_ACTUALIZAR_FORMAS_DE_PAGO, parametros);
    }

    private static CrearComprobante_Entrada entradaCrearComprobante(EntradaGirarComprobanteIngreso entrada) {
        CrearComprobante_Entrada e = new CrearComprobante_Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_SimularOperacion = entrada.p_simularOperacion;
        e.p_UsaReglasNegocio = "N"; // LEGACY DEFECT kept (see README TES-A-560): the requested p_UsaReglasNegocio is ignored
        e.p_FechaMovimiento = entrada.p_FechaMovimiento;
        e.p_IdCaja = entrada.p_IdCaja;
        e.p_GenerarComprobanteDesdeMovimientos = COMPROBANTE_DESDE_MOVIMIENTOS;
        e.p_GenerarComprobanteDesdeComprobantes = COMPROBANTE_DESDE_COMPROBANTES;
        e.p_EsIngresoAutomatico = ES_INGRESO_AUTOMATICO;
        e.p_ComprobanteGenerar = entrada.p_ComprobantesAGirar;
        e.p_FormaDePago = entrada.p_FormasDePago;
        return e;
    }

    /** LEGACY DEFECT kept (see README TES-A-562): the state GIR is written whatever {@code CrearComprobante} answered. */
    private void marcarComoGirados(EntradaGirarComprobanteIngreso entrada) throws Exception {
        List<Comprobantes> actualizar = new ArrayList<>();
        for (ComprobanteGenerar_Entrada comprobante : entrada.p_ComprobantesAGirar) {
            Comprobantes c = new Comprobantes();
            c.IdComprobante = comprobante.IdComprobante;
            actualizar.add(c);
        }
        GirarComprobanteLote.ejecutar(jdbc, SQL_ACTUALIZAR_ESTADO, actualizar);
    }
}
