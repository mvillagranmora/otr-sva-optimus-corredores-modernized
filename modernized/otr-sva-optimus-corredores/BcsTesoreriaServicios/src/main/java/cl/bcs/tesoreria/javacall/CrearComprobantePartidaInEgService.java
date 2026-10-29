package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.Secuencias;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearComprobantePartidaInEg} (behaviours TES-A-260..266): creates the "partida" comprobantes of ingreso/egreso, from
 * movements (state ING) or from comprobantes (state GIR). It stages the movements in a session temporary table, reads the movements and payment forms
 * to copy, gives every new comprobante and payment form the next value of its sequence and inserts them. The requirements procedure
 * {@code BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes} (outside the proof) is called through the {@code StoredProcedure}
 * collaborator and reports through the global output tables.
 */
@Service
public class CrearComprobantePartidaInEgService {

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
        public String     p_GenerarComprobanteDesdeComprobantes = "S";    //  NVARCHAR(1)        -- S: debe llenar p_ComprobanteGenerar    
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
            this.p_GenerarComprobanteDesdeComprobantes = "S";    // S: debe llenar p_ComprobanteGenerar
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
        public long     IdIngresoEgreso;    // BIGINT        -- Movimientos que conformarán el comprobante
        public String     TipoComprobante;    // NVARCHAR(6)     -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ComprobanteGenerar_Entrada
    {
        public Long     NumeroReferencia;   // BIGINT          -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
        public Long     IdComprobante;       // BIGINT        -- Comprobantes que conformará el comprobante 
        public String     TipoComprobante;       // NVARVCHAR(6)  -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class FormaDePago_Entrada implements Cloneable 
    {
        public Long         IdComprobanteFormaDePago;
        public Long            IdUsuarioLiquidacion;
        public Long            IdEmpresa;                     //    BIGINT  
        public Long         IdComprobante;
        public String         IdTipoMovimiento;
        public String         NombreMedioDePago;
        public Long         NumeroReferencia;               // BIGINT
        public String         IdMedioDePago;                  // NVARCHAR(3)
        public String         IdModalidadDePago;              // NVARCHAR(6)
        public String         NombreModalidadDePago;
        public BigDecimal    Monto;                           // DECIMAL(26,8)
        public Long         IdCuentaBancariaCorredora;       // BIGINT
        public String         NombreCuentaBancariaCorredora;
        public String        IdTipoCuentaBancariaCorredora;
        public String        NombreTipoCuentaBancariaCorredora;
        public Long         IdTarjetaBancariaCorredora;       // BIGINT
        public String         IdTipoTarjetaBancariaCorredora;
        public String         NombreTipoTarjetaBancariaCorredora;
        public String         NumeroTarjetaBancariaCorredora;
        public Long         IdChequera;                       // BIGINT
        public Long         NumeroDocumentoCorredora;       // BIGINT
        public Date         FechaDocumentoPagoCorredora;    // NVARCHAR(40)
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
        public Date         FechaDocumentoPagoSocio;         // NVARCHAR(40)
        public String         IdMoneda;
        public BigDecimal     SaldoDisponible;
        public BigDecimal    SaldoContable;
        public Date         FechaIngresoRegistro;
        public String         FechaAfectaSaldo;
        public Long         IdUsuarioGiro;
        public Date         FechaGiro;
        public Long            IdComprobantePartida;
        public Long            IdNominaTransferencia;
        public Long            IdDeposito;
        public String         IdEstado;
        public Long         DiasRetencion;
        public Long            IdPlaza;
        public String         Depositado; 
        public Long            IdUsuarioCertificacion;
        public Date         FechaHoraCertificacion; 
        public String         ClaveDCV; 
        public String         NumeroCuentaBancariaCorredora;
        public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }

    }

    public static class Comprobante
    {
        public Long        IdComprobante;                 //    BIGINT  
        public Long        IdEmpresa;                     //    BIGINT  
        public Long        IdUsuario;                     //    BIGINT 
        public String     TipoComprobante;                 //    NVARCHAR(6)    
        public String     IdTipoMovimiento;             //    NVARCHAR(6)   
        public String     IdConcepto;                     //    NVARCHAR(6)   
        public Long     IdCaja;                         //    BIGINT 
        public String     IdMoneda;                     //    NVARCHAR(3) 
        public BigDecimal     Monto;                         //    DECIMAL   
        public Date     FechaLiquidacion;             //    DATE   
        public Date     FechaIngresoRegistro;         //    TIMESTAMP   
        public Date     FechaMovimiento;                 //    DATE   
        public String     IdTipoPersona;                 //    NVARCHAR(3)   
        public Long     IdPersona;                     //    BIGINT    
        public String     NombrePersona;                 //    NVARCHAR(3)   
        public String     IdTipoDocumentoIdentidad;     //    NVARCHAR(3)   
        public String     DocumentoIdentidad;             //    NVARCHAR(3)   
        public String     TitularPago;                     //    NVARCHAR(3)   
        public String     IdTipoPersonaTitularPago;     //    NVARCHAR(3)   
        public Long     IdPersonaTitularPago;         //    BIGINT   
        public String     NombrePersonaTitularPago;     //    NVARCHAR(300)   
        public Long     IdPortafolioTitularPago;         //    BIGINT   
        public String     SecuenciaAgrupacion;             //    NVARCHAR(3)   
        public String     EsBilateral;                     //    NVARCHAR(3)   
        public String     ComprobanteNeteado;             //    NVARCHAR(1)   
        public String     SeparadoPorConcepto;             //    NVARCHAR(1)   
        public String     GeneradoDesdeMovimientos;     //    NVARCHAR(1)   
        public Long     IdUsuarioBloqueo;             //    BIGINT  
        public Long     IdCarga;                         //    BIGINT 
        public Long     IdEvento;                     //    BIGINT 
        public String     IdEstado;                     //    NVARCHAR(3) 
        public String     MueveCuentaInversion;         //    NVARCHAR(1)   
        public String     ComprobanteAgrupado;             //    NVARCHAR(1)   
        public Long     IdUsuarioAnulacion;             //    BIGINT  
        ////////////////
        public Long    IdComprobanteDetalle;             //    BIGINT  
        public int            NroLinea;                         //    BIGINT 
        public Long    IdEmpleadoDigitador;             //    BIGINT 
        public Long    IdSucursalEmpleadoDigitador;     //    BIGINT 
        public String        IdTipoComprobanteOrigen;         //    BIGINT 
        public Long    IdComprobanteOrigen;             //    BIGINT 
        public Date            FechaComprobanteOrigen;         //    BIGINT 
        public Date            FechaLiquidacionComprobanteOrigen;         //    BIGINT 
        public String        IdTipoProducto;                 //    BIGINT 
        public String        IdTipoOperacion;                 //    BIGINT 
        public String        IdBolsa;                         //    BIGINT 
        public String        IdMonedaOperacion;                 //    BIGINT 
        public BigDecimal    MontoOperacion;                 //    BIGINT 
        public Date            FechaParidad;                     //    BIGINT 
        public String        IdTipoReajuste;                 //    BIGINT 
        public BigDecimal    ValorParidad;                     //    BIGINT 
        public Long    FolioCartera;                     //    BIGINT 
        public Long    IdInstrumento;                     //    BIGINT 
        public String        IdTipoInstrumento;                 //    BIGINT 
        public String        IdClasificacionTipoInstrumento;     //    BIGINT 
        public Long    SecuenciaRelacionada;             //    BIGINT 
        public String toString()
        {
            return Utils.printJson(this, false);
        }

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

    public static class ResultadoProceso_Salida
    {
        public String     Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
        public Long       Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
        public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
        public String    Resultado;    // VARCHAR(40)   -- Ej: "OK"

        public String toString(){
            return Utils.printJson(this, false);
        }
    }

    private static final String SECUENCIA_COMPROBANTE = "BSCL.Tesoreria.BD.Secuencias::Comprobante";
    private static final String SECUENCIA_FORMA_DE_PAGO = "BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago";
    private static final String PROCEDIMIENTO_REQUISITOS = "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes";

    private static final String SQL_CREATE_TABLA_TEMPORAL = CrearComprobanteSql.cargar("sql/CrearComprobantePartidaInEg_CreateTablaTemporal.sql");
    private static final String SQL_INSERT_REGISTROS = CrearComprobanteSql.cargar("sql/CrearComprobantePartidaInEg_InsertRegistros.sql");
    private static final String SQL_DELETE_REGISTROS = CrearComprobanteSql.cargar("sql/CrearComprobantePartidaInEg_DeleteRegistros.sql");
    private static final List<String> SQL_DROP_TABLAS = CrearComprobanteSql.sentencias("sql/CrearComprobantePartidaInEg_DropTablasTemporales.sql");
    private static final String SQL_INSERT_COMPROBANTES = CrearComprobanteSql.cargar("sql/CrearComprobanteJava_InsertComprobante.sql");
    private static final String SQL_INSERT_COMPROBANTES_DETALLE = CrearComprobanteSql.cargar("sql/CrearComprobanteJava_InsertComprobanteDetalle.sql");
    private static final String SQL_INSERT_COMPROBANTES_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobanteJava_InsertComprobanteFormaPago.sql");
    private static final String SQL_SELECT_INGRESO_EGRESO = CrearComprobanteSql.cargar("sql/CrearComprobanteJava_SelectIngresoEgreso.sql");
    private static final String SQL_SELECT_FORMA_DE_PAGO = CrearComprobanteSql.cargar("sql/CrearComprobante_SelectFormaPago.sql");
    private static final String SQL_SELECT_RESULTADO = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectResultadoProceso.sql");
    private static final String SQL_SELECT_INFORMACION = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectInformacionDetalle.sql");
    private static final String SQL_SELECT_ADVERTENCIAS = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectAdvertenciasDetalle.sql");
    private static final String SQL_SELECT_ERRORES = CrearComprobanteSql.cargar("sql/CrearComprobante_Gbl_SelectErroresDetalle.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final Secuencias secuencias;
    private final Colaboradores colaboradores;

    public CrearComprobantePartidaInEgService(NamedParameterJdbcTemplate jdbc, Secuencias secuencias, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: JSON in, JSON out. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        CrearComprobante_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, CrearComprobante_Entrada.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public CrearComprobante_Salida logica(CrearComprobante_Entrada in) throws Exception {
        CrearComprobante_Salida out = new CrearComprobante_Salida();
        invocarProcedimientoDeRequisitos(in);

        CrearComprobanteSql.ejecutar(jdbc, List.of(SQL_CREATE_TABLA_TEMPORAL));
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_REGISTROS, in.p_IngresoEgresoGenerar);

        List<Comprobante> movimientos = jdbc.query(SQL_SELECT_INGRESO_EGRESO, Map.of("p_IdEmpresa", in.p_IdEmpresa), Filas.de(Comprobante.class));
        List<FormaDePago_Entrada> formasDePago = jdbc.query(SQL_SELECT_FORMA_DE_PAGO, Map.of("p_IdEmpresa", in.p_IdEmpresa), Filas.de(FormaDePago_Entrada.class));

        // from comprobantes (GIR) wins over from movements (ING), as the legacy order of the two ifs
        String estado = "ING";
        String generadoDesdeMovimientos = "S";
        if (in.p_GenerarComprobanteDesdeComprobantes.equals("S")) {
            estado = "GIR";
            generadoDesdeMovimientos = "N";
        }

        List<Comprobante> comprobantes = new ArrayList<>();
        for (Comprobante origen : movimientos) {
            comprobantes.add(nuevoComprobante(in, origen, estado, generadoDesdeMovimientos));
        }
        List<FormaDePago_Entrada> formasNuevas = new ArrayList<>();
        for (FormaDePago_Entrada origen : formasDePago) {
            formasNuevas.add(nuevaFormaDePago(origen));
        }

        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES, comprobantes);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_DETALLE, comprobantes);
        CrearComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_FORMA_DE_PAGO, formasNuevas);

        leerSalida(out);
        CrearComprobanteSql.ejecutar(jdbc, List.of(SQL_DELETE_REGISTROS));
        eliminarTablasTemporales();
        return out;
    }

    /**
     * Requirements procedure. LEGACY DEFECT kept (see README TES-A-266): the legacy binds its parameters to the wrong input names
     * (see the call text resource), so the procedure receives NULLs; and the answer is never read, the messages come from the output tables.
     */
    private void invocarProcedimientoDeRequisitos(CrearComprobante_Entrada in) {
        JsonObject escalares = new JsonObject();
        escalares.addProperty("p_FechaMovimiento", in.p_FechaMovimiento);
        escalares.addProperty("p_IdCaja", in.p_IdCaja);
        escalares.addProperty("p_GenerarComprobanteDesdeMovimientos", in.p_GenerarComprobanteDesdeMovimientos);
        escalares.addProperty("p_GenerarComprobanteDesdeComprobantes", in.p_GenerarComprobanteDesdeComprobantes);
        escalares.addProperty("p_ValidaComprobanteGenerado", "S");
        JsonObject llamada = new JsonObject();
        llamada.addProperty("procedimiento", PROCEDIMIENTO_REQUISITOS);
        llamada.add("entrada", escalares);
        colaboradores.invocar("StoredProcedure", llamada);
    }

    /** A comprobante (header and its single detail line) copied from a movement, with the next id of the sequence. */
    private Comprobante nuevoComprobante(CrearComprobante_Entrada in, Comprobante m, String estado, String generadoDesdeMovimientos) {
        Comprobante c = new Comprobante();
        c.IdComprobante = secuencias.siguiente(SECUENCIA_COMPROBANTE);
        c.IdEmpresa = in.p_IdEmpresa;
        c.IdUsuario = in.p_IdUsuario;
        c.TipoComprobante = m.TipoComprobante;
        c.IdTipoMovimiento = m.IdTipoMovimiento;
        c.IdConcepto = m.IdConcepto;
        c.IdCaja = m.IdCaja;
        c.IdMoneda = m.IdMoneda;
        c.Monto = m.Monto;
        c.FechaLiquidacion = m.FechaLiquidacion;
        c.FechaIngresoRegistro = m.FechaIngresoRegistro;
        c.FechaMovimiento = m.FechaMovimiento;
        c.IdTipoPersona = m.IdTipoPersona;
        c.IdPersona = m.IdPersona;
        c.NombrePersona = m.NombrePersona;
        c.IdTipoDocumentoIdentidad = m.IdTipoDocumentoIdentidad;
        c.DocumentoIdentidad = m.DocumentoIdentidad;
        c.TitularPago = m.TitularPago;
        c.IdTipoPersonaTitularPago = m.IdTipoPersonaTitularPago;
        c.IdPersonaTitularPago = m.IdPersonaTitularPago;
        c.NombrePersonaTitularPago = m.NombrePersonaTitularPago;
        c.IdPortafolioTitularPago = m.IdPortafolioTitularPago;
        c.SecuenciaAgrupacion = m.SecuenciaAgrupacion;
        c.EsBilateral = m.EsBilateral;
        c.ComprobanteNeteado = m.ComprobanteNeteado;
        c.SeparadoPorConcepto = "S";
        c.GeneradoDesdeMovimientos = generadoDesdeMovimientos;
        c.IdUsuarioBloqueo = m.IdUsuarioBloqueo;
        c.IdCarga = m.IdCarga;
        c.IdEvento = m.IdEvento;
        c.IdEstado = estado;
        c.MueveCuentaInversion = m.MueveCuentaInversion;
        c.ComprobanteAgrupado = "N";
        c.IdUsuarioAnulacion = m.IdUsuarioAnulacion;
        c.NroLinea = 1;
        c.IdEmpleadoDigitador = m.IdEmpleadoDigitador;
        c.IdSucursalEmpleadoDigitador = m.IdSucursalEmpleadoDigitador;
        c.IdTipoComprobanteOrigen = m.IdTipoComprobanteOrigen;
        c.IdComprobanteOrigen = m.IdComprobanteOrigen;
        c.FechaComprobanteOrigen = m.FechaComprobanteOrigen;
        c.FechaLiquidacionComprobanteOrigen = m.FechaLiquidacionComprobanteOrigen;
        c.IdTipoProducto = m.IdTipoProducto;
        c.IdTipoOperacion = m.IdTipoOperacion;
        c.IdBolsa = m.IdBolsa;
        c.IdMonedaOperacion = m.IdMonedaOperacion;
        c.MontoOperacion = m.MontoOperacion;
        c.FechaParidad = m.FechaParidad;
        c.IdTipoReajuste = m.IdTipoReajuste;
        c.ValorParidad = m.ValorParidad;
        c.FolioCartera = m.FolioCartera;
        c.IdInstrumento = m.IdInstrumento;
        c.IdTipoInstrumento = m.IdTipoInstrumento;
        c.IdClasificacionTipoInstrumento = m.IdClasificacionTipoInstrumento;
        c.SecuenciaRelacionada = m.SecuenciaRelacionada;
        return c;
    }

    /**
     * A payment form copied from the source one, with the next id of the sequence. LEGACY DEFECT kept (see README TES-A-260): every form is attached to
     * comprobante 1, with retention 1 day, plaza 1, account number "00001" and account type "1" (hard-coded in the legacy, the real values stay commented out).
     */
    private FormaDePago_Entrada nuevaFormaDePago(FormaDePago_Entrada f) {
        FormaDePago_Entrada n = new FormaDePago_Entrada();
        n.IdComprobanteFormaDePago = secuencias.siguiente(SECUENCIA_FORMA_DE_PAGO);
        n.IdComprobante = 1L;
        n.IdTipoMovimiento = f.IdTipoMovimiento;
        n.IdMedioDePago = f.IdMedioDePago;
        n.NombreMedioDePago = f.NombreMedioDePago;
        n.IdModalidadDePago = f.IdModalidadDePago;
        n.NombreModalidadDePago = f.NombreModalidadDePago;
        n.IdCuentaBancariaCorredora = f.IdCuentaBancariaCorredora;
        n.NombreCuentaBancariaCorredora = f.NombreCuentaBancariaCorredora;
        n.NumeroCuentaBancariaCorredora = "00001";
        n.IdTipoCuentaBancariaCorredora = "1";
        n.NombreTipoCuentaBancariaCorredora = f.NombreCuentaBancariaCorredora;
        n.IdTarjetaBancariaCorredora = f.IdTarjetaBancariaCorredora;
        n.IdTipoTarjetaBancariaCorredora = f.IdTipoTarjetaBancariaCorredora;
        n.NombreTipoTarjetaBancariaCorredora = f.NombreTipoTarjetaBancariaCorredora;
        n.NumeroTarjetaBancariaCorredora = f.NumeroTarjetaBancariaCorredora;
        n.IdChequera = f.IdChequera;
        n.NumeroDocumentoCorredora = f.NumeroDocumentoCorredora;
        n.FechaDocumentoPagoCorredora = f.FechaDocumentoPagoCorredora;
        n.IdCuentaBancariaSocio = f.IdCuentaBancariaSocio;
        n.IdBancoSocio = f.IdBancoSocio;
        n.NombreCuentaBancariaSocio = f.NombreCuentaBancariaSocio;
        n.NumeroCuentaBancariaSocio = f.NumeroCuentaBancariaSocio;
        n.IdTipoCuentaBancariaSocio = f.IdTipoCuentaBancariaSocio;
        n.NombreTipoCuentaBancariaSocio = f.NombreTipoCuentaBancariaSocio;
        n.IdTarjetaBancariaSocio = f.IdTarjetaBancariaSocio;
        n.IdTipoTarjetaBancariaSocio = f.IdTipoTarjetaBancariaSocio;
        n.NumeroTarjetaBancariaSocio = f.NumeroTarjetaBancariaSocio;
        n.NumeroSerieSocio = f.NumeroSerieSocio;
        n.NumeroDocumentoSocio = f.NumeroDocumentoSocio;
        n.FechaDocumentoPagoSocio = f.FechaDocumentoPagoSocio;
        n.IdMoneda = f.IdMoneda;
        n.Monto = f.Monto;
        n.SaldoDisponible = f.SaldoDisponible;
        n.SaldoContable = f.SaldoContable;
        n.FechaIngresoRegistro = f.FechaIngresoRegistro;
        n.FechaAfectaSaldo = f.FechaAfectaSaldo;
        n.IdUsuarioGiro = f.IdUsuarioGiro;
        n.FechaGiro = f.FechaGiro;
        n.IdComprobantePartida = f.IdComprobantePartida;
        n.IdNominaTransferencia = f.IdNominaTransferencia;
        n.IdDeposito = f.IdDeposito;
        n.IdEstado = f.IdEstado;
        n.DiasRetencion = 1L;
        n.IdPlaza = 1L;
        n.Depositado = f.Depositado;
        n.IdUsuarioCertificacion = f.IdUsuarioCertificacion;
        n.FechaHoraCertificacion = f.FechaHoraCertificacion;
        n.ClaveDCV = f.ClaveDCV;
        n.IdUsuarioLiquidacion = f.IdUsuarioLiquidacion;
        return n;
    }

    /** Reads the output tables of the requirements procedure into the messages and the result. */
    private void leerSalida(CrearComprobante_Salida out) {
        out.P_OUT_RESULTADOPROCESO = jdbc.getJdbcOperations().query(SQL_SELECT_RESULTADO, Filas.de(ResultadoProceso_Salida.class));
        for (CrearComprobanteProcedimiento.InformacionDetalle i : jdbc.getJdbcOperations().query(SQL_SELECT_INFORMACION,
                Filas.de(CrearComprobanteProcedimiento.InformacionDetalle.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, i.Codigo, i.Descripcion));
        }
        for (CrearComprobanteProcedimiento.AdvertenciaDetalle a : jdbc.getJdbcOperations().query(SQL_SELECT_ADVERTENCIAS,
                Filas.de(CrearComprobanteProcedimiento.AdvertenciaDetalle.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, a.Codigo, a.Descripcion));
        }
        for (CrearComprobanteProcedimiento.ErrorDetalle e : jdbc.getJdbcOperations().query(SQL_SELECT_ERRORES,
                Filas.de(CrearComprobanteProcedimiento.ErrorDetalle.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, e.Codigo, e.Descripcion));
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
    }

    /** The legacy tried to drop both staging tables and ignored the failures (the second one never exists). */
    private void eliminarTablasTemporales() {
        for (String sentencia : SQL_DROP_TABLAS) {
            try {
                jdbc.getJdbcOperations().execute(sentencia);
            } catch (DataAccessException e) {
                // ignored on purpose, as the legacy
            }
        }
    }
}
