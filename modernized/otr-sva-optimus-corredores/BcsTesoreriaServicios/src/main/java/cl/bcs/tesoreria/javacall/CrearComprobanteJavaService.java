package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.javacall.contrato.AgregarLog;
import cl.bcs.tesoreria.javacall.contrato.RevisarRequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearComprobanteJava} (behaviours TES-A-200..234): creates treasury comprobantes from movimientos de ingreso/egreso
 * (partidas de ingreso/egreso included). The generation FROM other comprobantes is still delegated to the HANA stored procedure
 * ({@link CrearComprobanteProcedimiento}, outside the proof).
 *
 * <p>Flow of {@link #logica}: stage the inputs in the global temporary tables, read the movements and payment forms to process, log, run the business
 * rules (basic requirements, movement states, groups), build the comprobante groups, validate every new comprobante, insert (unless
 * {@code p_SimularOperacion = S}), refresh the bank balances and report the generated ids. Any error message ends the flow with the messages gathered so far
 * and the rows already staged (the transaction is rolled back by the platform only when an exception is thrown).
 */
@Service
public class CrearComprobanteJavaService {

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class CrearComprobante_Entrada
    {
        public Long        p_IdEmpresa;                                     //    BIGINT  
        public Long        p_IdUsuario;                                     //    BIGINT 
        public String     p_IdModulo;                                     //    NVARCHAR(3)    
        public String     p_SimularOperacion = "N";                         //    NVARCHAR(1)
        public String     p_UsaReglasNegocio = "S";                         //    NVARCHAR(1)      
        public String     p_FechaMovimiento;                                //  NVARCHAR(10)  
        public Long        p_IdCaja = 0L;                                   //    BIGINT            -- Caja donde se procesaran los comprobantes        
        public String     p_GenerarComprobanteDesdeMovimientos  = "N";    //    NVARCHAR(1)        -- S: debe llenar p_IngresoEgresoGenerar    
        public String     p_GenerarComprobanteDesdeComprobantes = "N";    //  NVARCHAR(1)        -- S: debe llenar p_ComprobanteGenerar    
        public String     p_EsIngresoAutomatico = "N";                    //  NVARCHAR(1)     -- S: Es ingreso automatico, N: Es ingreso Manual

        public List<IngresoEgresoGenerar_Entrada>    p_IngresoEgresoGenerar; //Listado de movimientos para generar comprobantes
        public List<ComprobanteGenerar_Entrada>        p_ComprobanteGenerar;   //Listado de comprobantes para generar comprobantes
        public List<FormaDePago_Entrada>            p_FormaDePago;            //Formas de pago para PARING/PAREGR, para comprobantes normales los toma desde el origen

        public CrearComprobante_Entrada()
        {
            this.p_SimularOperacion = "N";
            this.p_UsaReglasNegocio = "S";
            this.p_IdCaja = 0L;        
            this.p_GenerarComprobanteDesdeMovimientos  = "N";    // S: debe llenar p_IngresoEgresoGenerar
            this.p_GenerarComprobanteDesdeComprobantes = "N";    // S: debe llenar p_ComprobanteGenerar
            this.p_EsIngresoAutomatico = "N";
            this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgresoGenerar_Entrada>();
            this.p_ComprobanteGenerar = new ArrayList<ComprobanteGenerar_Entrada>();
            this.p_FormaDePago = new ArrayList<FormaDePago_Entrada>();
        }

        public String toString()
        {
            return Utils.printJson(this, false);
        }

    }

    public static class IngresoEgresoGenerar_Entrada
    {
        public Long     NumeroReferencia;   // BIGINT         -- Correlativo para relacionar los Mov que van a formar el comprobante
        public Long     IdIngresoEgreso;    // BIGINT        -- Movimientos que conformaran el comprobante
        public String     TipoComprobante;    // NVARCHAR(6)     -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
    }

    public static class ComprobanteGenerar_Entrada
    {
        public Long     NumeroReferencia;   // BIGINT          -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
        public Long     IdComprobante;       // BIGINT        -- Comprobantes que conformaran el comprobante 
        public String     TipoComprobante;       // NVARVCHAR(6)  -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
    }

    public static class FormaDePago_Entrada implements Cloneable 
    {
        public Long         NumeroReferencia;               // BIGINT
        public String         IdMedioDePago;                  // NVARCHAR(3)
        public String         IdModalidadDePago;              // NVARCHAR(6)
        public String         IdMoneda;                          // NVARCHAR(3)
        public BigDecimal    Monto;                           // DECIMAL(26,8)
        public Long         IdCuentaBancariaCorredora;       // BIGINT        
        public Long         IdTarjetaBancariaCorredora;       // BIGINT
        public Long         IdChequera;                       // BIGINT
        public Long         NumeroDocumentoCorredora;       // BIGINT
        public String         FechaDocumentoPagoCorredora;    // NVARCHAR(40)        
        public Long         IdCuentaBancariaSocio;           // BIGINT
        public Long         IdBancoSocio;                   // BIGINT
        public String         NombreCuentaBancariaSocio;         // NVARCHAR(100)
        public String         NumeroCuentaBancariaSocio;         // NVARCHAR(100)
        public String         IdTipoCuentaBancariaSocio;         // NVARCHAR(4)        
        public String         NombreTipoCuentaBancariaSocio;     // NVARCHAR(100)
        public Long         IdTarjetaBancariaSocio;          // BIGINT
        public String         IdTipoTarjetaBancariaSocio;     // NVARCHAR(40)
        public String         NombreTipoTarjetaBancariaSocio; // NVARCHAR(100)        
        public String         NumeroTarjetaBancariaSocio;     // NVARCHAR(100)
        public String         NumeroSerieSocio;                 // NVARCHAR(40)
        public Long         NumeroDocumentoSocio;           // BIGINT
        public String         FechaDocumentoPagoSocio;         // NVARCHAR(40)

        public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }

    }

    public static class ObjInstrumento extends BcsPojo
    {
        public Long                     IdInstrumento;
        public String                     Nemotecnico;
    }    

    public static class CrearComprobante_Salida
    {
        public List<MensajeSalida>             P_OUT_MENSAJES;
        public Long                             P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;

        public CrearComprobante_Salida()
        {
            P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            P_OUT_RESULTADOPROCESO =  new ArrayList<ResultadoProceso_Salida>();
        }

        public String toString()
        {
            return Utils.printJson(this, false);
        }
    }

    public static class ResultadoProceso_Salida extends BcsPojo
    {
        public String     Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
        public Long       Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
        public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
        public String    Resultado;    // VARCHAR(40)   -- Ej: "OK"
        public Long        NumeroReferencia;

        public ResultadoProceso_Salida(){
            this.NumeroReferencia = 0L;
        }
    }    

    public static class IngresoEgresoGenerar_Proceso 
    {    
        public Long            NumeroReferencia;              // BIGINT
        public Long            IdIngresoEgresoInformado;      // BIGINT
        public String        TipoComprobante;              // NVARCHAR(6)
        public Long            IdIngresoEgreso;              // BIGINT
        public String        IdTipoMovimiento;              // NVARCHAR(6)
        public String        IdConcepto;                  // NVARCHAR(6)
        public Long            IdCaja;                      // BIGINT
        public String        IdMoneda;                      // NVARCHAR(3)
        public BigDecimal     Monto;                      // DECIMAL(26,8)
        public String        FechaLiquidacion;              // NVARCHAR(10)
        public String        FechaIngresoRegistro;          // NVARCHAR(40)
        public String        FechaMovimiento;              // NVARCHAR(10)
        public String        IdTipoPersona;              // NVARCHAR(3)
        public Long            IdPersona;                  // BIGINT
        public String        IdTipoDocumentoIdentidad;      // NVARCHAR(3)
        public String        IdTipoPersonaTitularPago;      // NVARCHAR(3)
        public Long            IdPersonaTitularPago;          // BIGINT
        public Long            IdPortafolioTitularPago;      // BIGINT
        public String        SecuenciaAgrupacion;          // NVARCHAR(40)
        public Long            SecuenciaRelacionada;          // BIGINT
        public String         Glosa;                      // NVARCHAR(260)
        public Long            IdEmpleadoDigitador;          // BIGINT
        public Long            IdSucursalEmpleadoDigitador;// BIGINT,
        public String        IdTipoComprobanteOrigen;      // NVARCHAR(3),
        public Long            IdComprobanteOrigen;          // BIGINT,
        public String        FechaComprobanteOrigen;      // NVARCHAR(40),
        public String        FechaLiquidacionComprobanteOrigen;  //  NVARCHAR(40),
        public String        IdTipoProducto;              // NVARCHAR(4),
        public String        IdTipoOperacion;              // NVARCHAR(6),
        public String        IdBolsa;                      // NVARCHAR(3),
        public String        IdMonedaOperacion;          // NVARCHAR(3),
        public BigDecimal     MontoOperacion;              // DECIMAL(26,8),
        public String        FechaParidad;                  // NVARCHAR(40),
        public String        IdTipoReajuste;              // NVARCHAR(3),
        public BigDecimal     ValorParidad;                  // DECIMAL(26,8),
        public Long            FolioCartera;                  // BIGINT,
        public Long            IdInstrumento;              // BIGINT,
        public String        IdTipoInstrumento;          // NVARCHAR(3),
        public String        IdClasificacionTipoInstrumento;  // NVARCHAR(3),
        public String        EsBilateral;                  // NVARCHAR(1)
        public String        TitularPago;                  // NVARCHAR(1)
        public String        EsIngresoAutomatico;          // NVARCHAR(1)
        public String        NeteaComprobante;              // NVARCHAR(1)
        public String        AgrupaComprobante;          // NVARCHAR(1)
        public String        MueveCuentaInversion;          // NVARCHAR(1)
        public String        IdEstado;                      // NVARCHAR(3)
        public String         IdCondicionDeLiquidacion;    // NVARCHAR(10)        
        public Long            IdEmpresa;
        public Long            IdUsuario;
        public Long            IdCajaComprobante;
        public int             CantidadSecuenciaMovimiento;  //Para validar si falta incluir movimientos segun la secuencia agrupacion comprobante
        public int             CantidadSecuenciaComprobante; //Para validar que la secuencia de comprobante no este utilizada
        public String        NombrePersona;
        public String        DocumentoIdentidad;
        public String        NombrePersonaTitularPago;

        //Para validaciones
        public long         errores = 0;
        public long         advertencias = 0;
        public long         autorizaciones = 0;
        public long         ok = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class FormaDePago_Proceso 
    {
        public Long            NumeroReferencia;                  // BIGINT
        public String        IdMedioDePago;                  // NVARCHAR(3)
        public String        IdModalidadDePago;              // NVARCHAR(6)
        public String        IdMoneda;                          // NVARCHAR(3)
        public BigDecimal    Monto;                          // DECIMAL(26,8)
        public Long            IdCuentaBancariaCorredora;      // BIGINT        
        public Long            IdTarjetaBancariaCorredora;     // BIGINT
        public Long            IdChequera;                      // BIGINT
        public Long            NumeroDocumentoCorredora;          // BIGINT
        public String        FechaDocumentoPagoCorredora;    // NVARCHAR(40)        
        public Long            IdCuentaBancariaSocio;          // BIGINT
        public Long            IdBancoSocio;                      // BIGINT
        public String        NombreCuentaBancariaSocio;      // NVARCHAR(100)
        public String        NumeroCuentaBancariaSocio;      // NVARCHAR(100)
        public String        IdTipoCuentaBancariaSocio;      // NVARCHAR(4)        
        public String        NombreTipoCuentaBancariaSocio;  // NVARCHAR(100)
        public Long            IdTarjetaBancariaSocio;          // BIGINT
        public String        IdTipoTarjetaBancariaSocio;     // NVARCHAR(40)
        public String        NombreTipoTarjetaBancariaSocio; // NVARCHAR(100)        
        public String        NumeroTarjetaBancariaSocio;     // NVARCHAR(100)
        public String        NumeroSerieSocio;                  // NVARCHAR(40)
        public Long            NumeroDocumentoSocio;              // BIGINT
        public String        FechaDocumentoPagoSocio;          // NVARCHAR(40)    
        public String        NombreMedioDePago;              // NVARCHAR(100)
        public String        NombreModalidadDePago;          // NVARCHAR(100)
        public Long            IdPersona;                      // BIGINT
        public String        NombreCompleto;                  // NVARCHAR(300)
        public String        DocumentoIdentidadDefecto;      // NVARCHAR(40)
        public String        CodigoTipoPersona;              // NVARCHAR(2)
        public Long            IdCuentaBanco;                  // BIGINT
        public Long            IdBanco;                          // BIGINT
        public Long            IdPlaza;                          // BIGINT
        public Long            PlazoRetencionMismoBanco;          // BIGINT
        public Long            PlazoRetencionOtroBanco;          // BIGINT
        public String        NombreCuentaBancaria;              // NVARCHAR(100)
        public String        NumeroCuentaBancaria;              // NVARCHAR(100)
        public String        IdTipoCuentaBancaria;              // NVARCHAR(4)
        public String        NombreTipoCuentaBancaria;          // NVARCHAR(100)
        public String        IdTipoTarjetaBancaria;          // NVARCHAR(4)
        public String        NombreTipoTarjetaBancaria;      // NVARCHAR(40)
        public String        NumeroTarjetaBancaria;          // NVARCHAR(100)        
        public Long            IdEmpresa;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    public static class ConfiguracionTesoreria
    {
        public String     SEPARAPORCONCEPTO;
        public String     SEPARAPORAGENTE;
        public String     SEPARAPORSUCURSAL;
        public String     SEPARAPORCORREDORACONTRAPARTE;
        public String     SEPARAPORTIPOCOMPROBANTE;
        public String     SEPARAPORTIPOPRODUCTO;
        public String     SEPARAPORTIPOOPERACION;
        public String     SEPARAPORBOLSA;
        public String    SEPARAPORCONDICIONLIQUIDACION;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }    

    public static class ValidacionComprobante
    {
        public Long            NumeroReferencia;
        public Long            IdComprobante;
        public Long            IdCaja;
        public String         IdMoneda;
        public String         IdTipoPersona;
        public String         IdMonedaFormaDePago;
        public String        GeneraComprobanteAutomaticamente;
        public Long         CantidadTipoComprobante;
        public Long         CantidadMoneda;
        public Long         CantidadFechaLiquidacion;         
        public Long         CantidadFormasDePago;
        public Long         CantidadMonedaFormaDePago;
        public Long         CantidadConModalidadCaja;
        public BigDecimal     TotalMonto;
        public BigDecimal    TotalMontoFormaDePago;

        //Para validaciones
        public long         errores = 0;
        public long         advertencias = 0;
        public long         autorizaciones = 0;
        public long         ok = 0;

        public String toString(){
            return Utils.printJson(this, false);
        }
    }    

    public static class ResultadoProceso extends BcsPojo
    {
        public String     Campo;
        public Long       Id;
        public String     Servicio;
        public String    Resultado;
        public String    IdTipoMovimiento;
        public String    CodigoMensaje;
        public Long     NumeroReferencia;

        public ResultadoProceso(){
            this.NumeroReferencia = 0L;
        }
    }

    // ------------------------------------------------------------------ SQL

    private static final String SQL_INSERT_CUENTA_BANCARIA_CORREDORA = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertCuentaBancariaCorredora.sql");
    private static final String SQL_SELECT_CONFIGURACION_TESORERIA = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectConfiguracionTesoreria.sql");
    private static final String SQL_INSERT_ENTRADA_INGRESO_EGRESO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertEntradaIngresoEgresoGenerar.sql");
    private static final String SQL_INSERT_INGRESO_EGRESO_PROCESO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertIngresoEgresoGenerarProceso.sql");
    private static final String SQL_SELECT_INGRESO_EGRESO_PROCESO = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectIngresoEgresoGenerarProceso.sql");
    private static final String SQL_DELETE_INGRESO_EGRESO_PROCESO = CrearComprobanteSql.cargar("sql/CrearComprobante_DeleteIngresoEgresoGenerarProceso.sql");
    private static final String SQL_INSERT_ENTRADA_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertEntradaFormaDePago.sql");
    private static final String SQL_INSERT_FORMA_DE_PAGO_PROCESO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertFormaDePagoProceso.sql");
    private static final String SQL_INSERT_GRUPOS = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertGruposDeComprobantes.sql");
    private static final String SQL_INSERT_MOVIMIENTO_DETALLE = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertMovimientoDetalle.sql");
    private static final String SQL_INSERT_GLOBAL_COMPROBANTE = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertGlobalComprobante.sql");
    private static final String SQL_INSERT_GLOBAL_DETALLE = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertGlobalComprobanteDetalle.sql");
    private static final String SQL_INSERT_GLOBAL_DETALLE_ORIGEN = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertGlobalComprobanteDetalleOrigen.sql");
    private static final String SQL_INSERT_GLOBAL_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertGlobalComprobanteFormaDePago.sql");
    private static final String SQL_UPDATE_GLOBAL_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_UpdateGlobalComprobanteFormaDePago.sql");
    private static final String SQL_INSERT_COMPROBANTE = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertComprobante.sql");
    private static final String SQL_INSERT_COMPROBANTE_DETALLE = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertComprobanteDetalle.sql");
    private static final String SQL_INSERT_COMPROBANTE_DETALLE_ORIGEN = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertComprobanteDetalleOrigen.sql");
    private static final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_InsertComprobanteFormaDePago.sql");
    private static final String SQL_UPDATE_INGRESO_EGRESO = CrearComprobanteSql.cargar("sql/CrearComprobante_UpdateIngresoEgreso.sql");
    private static final String SQL_UPDATE_INGRESO_EGRESO_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_UpdateIngresoEgresoFormaDePago.sql");
    private static final String SQL_SELECT_CUENTA_BANCARIA_ACTUALIZAR_SALDO = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectCuentaBancariaActualizarSaldo.sql");
    private static final String SQL_SELECT_RESULTADO_PROCESO = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectResultadoProceso.sql");
    private static final String SQL_SELECT_DATA_VALIDACION = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectDataValidacionComprobante.sql");
    private static final String SQL_SELECT_INSTRUMENTO = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectInstrumento.sql");
    private static final List<String> LIMPIAR_GLOBALES = CrearComprobanteSql.sentencias("sql/CrearComprobante_Gbl_LimpiarJava.sql");

    private static final String SI = "S";
    private static final String NO = "N";

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;
    private final CrearComprobanteProcedimiento procedimiento;
    private final Colaboradores colaboradores;

    public CrearComprobanteJavaService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, CrearComprobanteProcedimiento procedimiento,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
        this.procedimiento = procedimiento;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: JSON in, JSON out, through the typed {@link #logica}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        CrearComprobante_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, CrearComprobante_Entrada.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public CrearComprobante_Salida logica(CrearComprobante_Entrada in) throws Exception {
        CrearComprobante_Salida out = new CrearComprobante_Salida();
        if (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase(SI)) {
            generarDesdeProcedimiento(in, out);
            return out;
        }
        procesarMovimientos(in, out);
        return out;
    }

    // ------------------------------------------------------------------ from comprobantes: the stored procedure

    private void generarDesdeProcedimiento(CrearComprobante_Entrada in, CrearComprobante_Salida out) throws Exception {
        var escalares = new CrearComprobanteProcedimiento.Escalares(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_SimularOperacion,
                in.p_UsaReglasNegocio, in.p_FechaMovimiento, in.p_IdCaja, in.p_GenerarComprobanteDesdeMovimientos,
                in.p_GenerarComprobanteDesdeComprobantes, in.p_EsIngresoAutomatico);
        var salida = procedimiento.ejecutar(escalares, in.p_IngresoEgresoGenerar, in.p_ComprobanteGenerar, in.p_FormaDePago);
        out.P_OUT_RESULTADOPROCESO = salida.resultados().stream().map(CrearComprobanteJavaService::resultado).collect(Collectors.toCollection(ArrayList::new));
        out.P_OUT_MENSAJES.addAll(salida.mensajes());
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        CrearComprobanteSql.ejecutar(jdbc, LIMPIAR_GLOBALES);
    }

    private static ResultadoProceso_Salida resultado(CrearComprobanteProcedimiento.Resultado r) {
        ResultadoProceso_Salida s = new ResultadoProceso_Salida();
        s.Campo = r.Campo;
        s.Id = r.Id;
        s.Servicio = r.Servicio;
        s.Resultado = r.Resultado;
        s.NumeroReferencia = r.NumeroReferencia;
        return s;
    }

    // ------------------------------------------------------------------ from movements

    private void procesarMovimientos(CrearComprobante_Entrada in, CrearComprobante_Salida out) throws Exception {
        // inputs staged in the global tables
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_ENTRADA_INGRESO_EGRESO, in.p_IngresoEgresoGenerar);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_ENTRADA_FORMA_DE_PAGO, in.p_FormaDePago);

        IngresoEgresoGenerar_Proceso cuentas = new IngresoEgresoGenerar_Proceso();
        cuentas.IdEmpresa = in.p_IdEmpresa;
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_CUENTA_BANCARIA_CORREDORA, List.of(cuentas));

        List<ConfiguracionTesoreria> configuracion = jdbc.query(SQL_SELECT_CONFIGURACION_TESORERIA, Map.of("IdEmpresa", in.p_IdEmpresa),
                Filas.de(ConfiguracionTesoreria.class));

        IngresoEgresoGenerar_Proceso entradaProceso = new IngresoEgresoGenerar_Proceso();
        entradaProceso.IdEmpresa = in.p_IdEmpresa;
        entradaProceso.IdCajaComprobante = in.p_IdCaja;
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_INGRESO_EGRESO_PROCESO, List.of(entradaProceso));
        List<IngresoEgresoGenerar_Proceso> movimientos = jdbc.getJdbcOperations().query(SQL_SELECT_INGRESO_EGRESO_PROCESO,
                Filas.de(IngresoEgresoGenerar_Proceso.class));
        registrarLog(in, movimientos);

        FormaDePago_Proceso entradaFormaDePago = new FormaDePago_Proceso();
        entradaFormaDePago.IdEmpresa = in.p_IdEmpresa;
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_FORMA_DE_PAGO_PROCESO, List.of(entradaFormaDePago));

        if (in.p_UsaReglasNegocio.contentEquals(SI) && !validarReglasDeNegocio(in, out, movimientos)) {
            return;
        }

        if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase(SI)) {
            armarGrupos(in, configuracion);
        }

        List<ValidacionComprobante> nuevos = jdbc.getJdbcOperations().query(SQL_SELECT_DATA_VALIDACION, Filas.de(ValidacionComprobante.class));
        for (ValidacionComprobante nuevo : nuevos) {
            validarNuevoComprobante(in.p_IdEmpresa, nuevo, out.P_OUT_MENSAJES);
        }
        if (nuevos.stream().anyMatch(n -> n.errores > 0)) {
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
            return;
        }

        if (in.p_SimularOperacion.equalsIgnoreCase(NO)) {
            insertarComprobantes(in);
        }
        informarResultado(in, out);
        CrearComprobanteSql.ejecutar(jdbc, LIMPIAR_GLOBALES);
    }

    /** Log of the process, one entry per movement (collaborator {@code AgregarLog}). */
    private void registrarLog(CrearComprobante_Entrada in, List<IngresoEgresoGenerar_Proceso> movimientos) throws Exception {
        if (movimientos == null || movimientos.isEmpty()) {
            return;
        }
        List<Long> idsInstrumento = movimientos.stream().map(m -> m.IdInstrumento).distinct().collect(Collectors.toList());
        Map<Long, ObjInstrumento> instrumentos = new HashMap<>();
        if (!idsInstrumento.isEmpty()) {
            for (ObjInstrumento i : jdbc.query(SQL_SELECT_INSTRUMENTO, Map.of("IdInstrumento", idsInstrumento), Filas.de(ObjInstrumento.class))) {
                if (instrumentos.put(i.IdInstrumento, i) != null) {
                    throw new Exception("key field is not unique");
                }
            }
        }
        AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
        for (IngresoEgresoGenerar_Proceso m : movimientos) {
            ObjInstrumento instrumento = instrumentos.isEmpty() ? new ObjInstrumento() : instrumentos.get(m.IdInstrumento);
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.NombreProceso = "Crear Comprobante Tesoreria";
            cabecera.IdCabeceraLog = m.IdComprobanteOrigen != null ? m.IdComprobanteOrigen : 0L;
            cabecera.IdTipoLog = "INGRESO";
            cabecera.IdModulo = in.p_IdModulo;
            cabecera.IdTipoRegistro = "COMPROBANTE_TESORERIA";
            cabecera.IdUsuario = in.p_IdUsuario;
            cabecera.IdEmpresa = in.p_IdEmpresa;
            cabecera.FechaSistemaLog = in.p_FechaMovimiento != null ? in.p_FechaMovimiento : "";
            cabecera.IdUsuarioAtendido = in.p_IdUsuario;
            cabecera.IdPersona = m.IdPersona != null ? m.IdPersona : 0L;
            cabecera.IdPortafolio = m.IdPortafolioTitularPago != null ? m.IdPortafolioTitularPago : 0L;
            cabecera.IdTipoOperacion = m.IdTipoOperacion != null ? m.IdTipoOperacion : "";
            cabecera.Folio = m.IdComprobanteOrigen != null ? m.IdComprobanteOrigen : 0L;
            cabecera.Monto = m.Monto != null ? m.Monto : BigDecimal.ZERO;
            cabecera.IdMonedaOrigen = m.IdMoneda != null ? m.IdMoneda : "";
            cabecera.Nemo = instrumento != null && instrumento.Nemotecnico != null ? instrumento.Nemotecnico : "";
            entradaLog.p_Log.add(cabecera);
        }
        Colaborador.invocar(colaboradores, "AgregarLog", entradaLog, AgregarLog.OutAgregarLog.class);
    }

    /**
     * Business rules. Returns false when the flow must stop (the messages are already in {@code out}): basic requirements with errors, nothing to
     * process, or every movement rejected.
     */
    private boolean validarReglasDeNegocio(CrearComprobante_Entrada in, CrearComprobante_Salida out, List<IngresoEgresoGenerar_Proceso> movimientos)
            throws Exception {
        if (in.p_EsIngresoAutomatico.equalsIgnoreCase(NO)) {
            Map<String, Object> requisitos = new java.util.LinkedHashMap<>();
            requisitos.put("p_IdEmpresa", in.p_IdEmpresa);
            requisitos.put("p_IdUsuario", in.p_IdUsuario);
            requisitos.put("p_IdModulo", in.p_IdModulo);
            var basicos = Colaborador.invocar(colaboradores, "RevisarRequisitosBasicos", requisitos,
                    RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.class);
            if (MensajeSalida.numeroErrores(basicos.p_out_Mensajes) > 0) {
                out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
                out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
                return false;
            }
        }

        if ((in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase(SI) && in.p_IngresoEgresoGenerar.isEmpty())
                || (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase(SI) && in.p_ComprobanteGenerar.isEmpty())) {
            // LEGACY DEFECT kept (see README TES-A-224): the message is built without values, so it is never translated
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS",
                    diccionario.mensaje(in.p_IdEmpresa, "NO_VIENEN_MOVIMIENTOS", null)));
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
            return false;
        }

        if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase(SI)) {
            for (IngresoEgresoGenerar_Proceso m : movimientos) {
                Long id = m.IdIngresoEgresoInformado;
                rechazar(in, out, m.IdIngresoEgreso.compareTo(0L) == 0, "MOVIMIENTO_TESORERIA_NO_EXISTE", id, m);
                rechazar(in, out, "EMI".equalsIgnoreCase(m.IdEstado), "MOVIMIENTO_TESORERIA_EMITIDO", id, m);
                rechazar(in, out, "BLO".equalsIgnoreCase(m.IdEstado), "MOVIMIENTO_TESORERIA_BLOQUEADO", id, m);
                rechazar(in, out, "ANU".equalsIgnoreCase(m.IdEstado), "MOVIMIENTO_TESORERIA_ANULADO", id, m);
                rechazar(in, out, "PDA".equalsIgnoreCase(m.IdEstado), "MOVIMIENTO_TESORERIA_PENDIENTE_AUTORIZACION", id, m);
                // LEGACY DEFECT kept (see README TES-A-219): the counters are never above 1 (the query counts the movement itself), the check never fires
                rechazar(in, out, m.CantidadSecuenciaComprobante > 1, "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA", id, m);
                // LEGACY DEFECT kept (see README TES-A-220): same, the check never fires
                rechazar(in, out, m.CantidadSecuenciaMovimiento > 1, "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE", id, m);
            }

            // movements with errors leave the process (and their payment forms)
            List<IngresoEgresoGenerar_Proceso> conErrores = new ArrayList<>();
            for (IngresoEgresoGenerar_Proceso m : movimientos) {
                if (m.errores > 0) {
                    conErrores.add(m);
                }
            }
            movimientos.removeAll(conErrores);
            // LEGACY DEFECT kept (see README TES-A-225): the legacy also tried to drop the payment forms of the rejected movements, but ran that clean-up
            // over the movements that were just removed, so it removed nothing; the payment forms of a rejected movement stay in the global table.
            if (!conErrores.isEmpty()) {
                CrearComprobanteSql.lote(jdbc, SQL_DELETE_INGRESO_EGRESO_PROCESO, conErrores);
            }
            if (movimientos.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
                return false;
            }
        }
        return true;
    }

    private void rechazar(CrearComprobante_Entrada in, CrearComprobante_Salida out, boolean esError, String codigo, Long id, IngresoEgresoGenerar_Proceso m) {
        if (esError) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(id, MensajeSalida.MENSAJE_ERROR, codigo, diccionario.mensaje(in.p_IdEmpresa, codigo, valor("Id", id))));
            m.errores++;
        }
    }

    private static Map<String, String> valor(String clave, Object valor) {
        Map<String, String> valores = new HashMap<>();
        if (valor != null) {
            valores.put(clave, valor.toString());
        }
        return valores;
    }

    /** Groups the movements into the comprobantes to generate (global tables) and computes the retention dates of the payment forms. */
    private void armarGrupos(CrearComprobante_Entrada in, List<ConfiguracionTesoreria> configuracion) throws Exception {
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_GRUPOS, configuracion);

        IngresoEgresoGenerar_Proceso detalle = new IngresoEgresoGenerar_Proceso();
        detalle.IdEmpresa = in.p_IdEmpresa;
        detalle.IdUsuario = in.p_IdUsuario;
        detalle.FechaMovimiento = in.p_FechaMovimiento;
        detalle.IdCajaComprobante = in.p_IdCaja;
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_MOVIMIENTO_DETALLE, List.of(detalle));

        IngresoEgresoGenerar_Proceso globales = new IngresoEgresoGenerar_Proceso();
        globales.IdEmpresa = in.p_IdEmpresa;
        List<IngresoEgresoGenerar_Proceso> filaUnica = List.of(globales);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_GLOBAL_COMPROBANTE, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_GLOBAL_DETALLE, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_GLOBAL_DETALLE_ORIGEN, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_GLOBAL_FORMA_DE_PAGO, filaUnica);

        IngresoEgresoGenerar_Proceso retencion = new IngresoEgresoGenerar_Proceso();
        retencion.FechaMovimiento = in.p_FechaMovimiento;
        CrearComprobanteSql.lote(jdbc, SQL_UPDATE_GLOBAL_FORMA_DE_PAGO, List.of(retencion));
    }

    private void validarNuevoComprobante(Long idEmpresa, ValidacionComprobante c, List<MensajeSalida> mensajes) {
        error(idEmpresa, c, c.CantidadTipoComprobante.compareTo(1L) == 1, "COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE", mensajes);
        error(idEmpresa, c, c.CantidadMoneda.compareTo(1L) == 1, "COMPROBANTE_CON_MEZCLA_DE_MONEDAS", mensajes);
        error(idEmpresa, c, c.CantidadFechaLiquidacion.compareTo(1L) == 1, "COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION", mensajes);
        error(idEmpresa, c, c.CantidadFormasDePago.compareTo(0L) == 0 && c.IdTipoPersona.equalsIgnoreCase("CLI") && NO.equals(c.GeneraComprobanteAutomaticamente),
                "NO_VIENEN_FORMAS_DE_PAGO", mensajes);
        error(idEmpresa, c, c.CantidadFormasDePago.compareTo(0L) > 0 && c.CantidadMonedaFormaDePago > 1, "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS", mensajes);
        error(idEmpresa, c, c.CantidadFormasDePago.compareTo(0L) > 0 && !c.IdMoneda.equalsIgnoreCase(c.IdMonedaFormaDePago),
                "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO", mensajes);
        error(idEmpresa, c, c.CantidadFormasDePago.compareTo(0L) > 0 && c.TotalMonto.abs().subtract(c.TotalMontoFormaDePago).compareTo(BigDecimal.ZERO) < 0,
                "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE", mensajes);
        error(idEmpresa, c, c.CantidadFormasDePago.compareTo(0L) > 0 && c.TotalMonto.setScale(2, RoundingMode.FLOOR).abs()
                .subtract(c.TotalMontoFormaDePago.setScale(2, RoundingMode.FLOOR)).compareTo(BigDecimal.ZERO) > 0, "MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE", mensajes);
        error(idEmpresa, c, c.CantidadConModalidadCaja.compareTo(0L) > 0 && c.IdCaja.compareTo(0L) == 0, "COMPROBANTE_SIN_CAJA_ASIGNADA", mensajes);
    }

    private void error(Long idEmpresa, ValidacionComprobante c, boolean esError, String codigo, List<MensajeSalida> mensajes) {
        if (esError) {
            mensajes.add(new MensajeSalida(c.IdComprobante, MensajeSalida.MENSAJE_ERROR, codigo,
                    diccionario.mensaje(idEmpresa, codigo, valor("IdComprobante", c.IdComprobante))));
            c.errores++;
        }
    }

    private void insertarComprobantes(CrearComprobante_Entrada in) throws Exception {
        IngresoEgresoGenerar_Proceso datos = new IngresoEgresoGenerar_Proceso();
        datos.IdEmpresa = in.p_IdEmpresa;
        datos.IdUsuario = in.p_IdUsuario;
        datos.FechaMovimiento = in.p_FechaMovimiento;
        List<IngresoEgresoGenerar_Proceso> filaUnica = List.of(datos);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_DETALLE, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, filaUnica);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_DETALLE_ORIGEN, filaUnica);
        if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase(SI)) {
            CrearComprobanteSql.lote(jdbc, SQL_UPDATE_INGRESO_EGRESO, filaUnica);
            CrearComprobanteSql.lote(jdbc, SQL_UPDATE_INGRESO_EGRESO_FORMA_DE_PAGO, filaUnica);
        }

        // bank balances of the accounts of the corredora touched by the new payment forms
        List<ActualizarSaldosCuentaBanco.CuentaBanco_Entrada> cuentas = jdbc.getJdbcOperations().query(SQL_SELECT_CUENTA_BANCARIA_ACTUALIZAR_SALDO,
                Filas.de(ActualizarSaldosCuentaBanco.CuentaBanco_Entrada.class));
        if (!cuentas.isEmpty()) {
            ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada actualizar = new ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada();
            actualizar.p_FechaMovimiento = in.p_FechaMovimiento;
            actualizar.p_CuentaBanco = cuentas;
            Colaborador.invocar(colaboradores, "ActualizarSaldosCuentaBanco", actualizar, ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida.class);
        }
    }

    /** Result of the process and one success message per comprobante generated. */
    private void informarResultado(CrearComprobante_Entrada in, CrearComprobante_Salida out) {
        List<ResultadoProceso> resultados = jdbc.getJdbcOperations().query(SQL_SELECT_RESULTADO_PROCESO, Filas.de(ResultadoProceso.class));
        for (ResultadoProceso r : resultados) {
            ResultadoProceso_Salida s = new ResultadoProceso_Salida();
            s.Campo = r.Campo;
            s.Id = r.Id;
            s.Servicio = r.Servicio;
            s.Resultado = r.Resultado;
            s.NumeroReferencia = r.NumeroReferencia;
            out.P_OUT_RESULTADOPROCESO.add(s);
            if (!r.CodigoMensaje.equalsIgnoreCase("") && r.Campo.equalsIgnoreCase("IdComprobante") && r.Resultado.equalsIgnoreCase("ING")) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(r.Id, MensajeSalida.MENSAJE_OK, r.CodigoMensaje,
                        diccionario.mensaje(in.p_IdEmpresa, r.CodigoMensaje, valor("IdComprobante", r.Id))));
            }
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
    }
}
