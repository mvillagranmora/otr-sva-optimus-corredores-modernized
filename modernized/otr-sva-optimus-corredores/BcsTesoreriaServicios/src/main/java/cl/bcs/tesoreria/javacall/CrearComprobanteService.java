package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearComprobante} (behaviours TES-A-250..257): the entry point that creates comprobantes. Depending on the company
 * parameter {@code TESORERIA/USARCREARCOMPROBANTEJAVA} it runs the Java implementation ({@link CrearComprobanteJavaService}, value {@code S}) or the
 * HANA stored procedure ({@link CrearComprobanteProcedimiento}, {@code N}, which is also the default when the parameter does not exist). Any other
 * value runs neither and answers an empty result.
 *
 * <p>Other services (CrearIngresoEgreso, LiquidarMovimientoAutomatico, GirarComprobante*) call {@link #logica(CrearComprobante_Entrada)} in process.
 */
@Service
public class CrearComprobanteService {

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
        public Long     IdIngresoEgreso;    // BIGINT        -- Movimientos que conformarán el comprobante
        public String     TipoComprobante;    // NVARCHAR(6)     -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
    }

    public static class ComprobanteGenerar_Entrada
    {
        public Long     NumeroReferencia;   // BIGINT          -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
        public Long     IdComprobante;       // BIGINT        -- Comprobantes que conformará el comprobante 
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

    public static class CrearComprobante_Salida extends BcsPojo
    {
        public List<MensajeSalida>             P_OUT_MENSAJES;
        public Long                             P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;

        public CrearComprobante_Salida()
        {
            P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            P_OUT_RESULTADOPROCESO =  new ArrayList<ResultadoProceso_Salida>();
        }

    }

    public static class ResultadoProceso_Salida extends BcsPojo
    {
        public String     Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
        public Long       Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
        public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
        public String    Resultado;    // VARCHAR(40)   -- Ej: "OK"
        public Long NumeroReferencia;

        public ResultadoProceso_Salida(){
            this.NumeroReferencia = 0L;
        }
    }    

    public static class InformacionDetalle 
    {     
        public String        Codigo; 
        public String        Descripcion; 
    }

    public static class AdvertenciasDetalle 
    {    
        public String        Codigo; 
        public String        RequiereAutorizacion; 
        public String        Descripcion; 
    }

    public static class ErroresDetalle 
    {
        public String        Codigo; 
        public String        Descripcion; 
    }

    private static final String PARAMETRO_USAR_JAVA = "USARCREARCOMPROBANTEJAVA";
    private static final String MODULO_TESORERIA = "TESORERIA";

    private final ConfiguracionSistema configuracion;
    private final CrearComprobanteProcedimiento procedimiento;
    private final CrearComprobanteJavaService crearComprobanteJava;

    public CrearComprobanteService(ConfiguracionSistema configuracion, CrearComprobanteProcedimiento procedimiento,
            CrearComprobanteJavaService crearComprobanteJava) {
        this.configuracion = configuracion;
        this.procedimiento = procedimiento;
        this.crearComprobanteJava = crearComprobanteJava;
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
        String usarJava = configuracion.parametro(in.p_IdEmpresa, MODULO_TESORERIA, PARAMETRO_USAR_JAVA);
        if (usarJava == null) {
            usarJava = "N"; // the legacy swallowed the "parameter not found" error and used the procedure
        }

        if (usarJava.equalsIgnoreCase("N")) {
            var escalares = new CrearComprobanteProcedimiento.Escalares(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_SimularOperacion,
                    in.p_UsaReglasNegocio, in.p_FechaMovimiento, in.p_IdCaja, in.p_GenerarComprobanteDesdeMovimientos,
                    in.p_GenerarComprobanteDesdeComprobantes, in.p_EsIngresoAutomatico);
            var salida = procedimiento.ejecutar(escalares, in.p_IngresoEgresoGenerar, in.p_ComprobanteGenerar, in.p_FormaDePago);
            for (CrearComprobanteProcedimiento.Resultado r : salida.resultados()) {
                out.P_OUT_RESULTADOPROCESO.add(copiar(r, ResultadoProceso_Salida.class));
            }
            out.P_OUT_MENSAJES.addAll(salida.mensajes());
        }

        if (usarJava.equalsIgnoreCase("S")) {
            var salida = crearComprobanteJava.logica(aEntradaJava(in));
            out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
            out.P_OUT_RESULTADOPROCESO = new ArrayList<>();
            for (CrearComprobanteJavaService.ResultadoProceso_Salida r : salida.P_OUT_RESULTADOPROCESO) {
                out.P_OUT_RESULTADOPROCESO.add(copiar(r, ResultadoProceso_Salida.class));
            }
        }

        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out;
    }

    /** The same Entrada in the types of the Java implementation (the legacy copied it field by field; a null list is a NullPointerException). */
    private static CrearComprobanteJavaService.CrearComprobante_Entrada aEntradaJava(CrearComprobante_Entrada in) {
        var java = new CrearComprobanteJavaService.CrearComprobante_Entrada();
        java.p_IdEmpresa = in.p_IdEmpresa;
        java.p_IdUsuario = in.p_IdUsuario;
        java.p_IdModulo = in.p_IdModulo;
        java.p_SimularOperacion = in.p_SimularOperacion;
        java.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        java.p_FechaMovimiento = in.p_FechaMovimiento;
        java.p_IdCaja = in.p_IdCaja;
        java.p_GenerarComprobanteDesdeMovimientos = in.p_GenerarComprobanteDesdeMovimientos;
        java.p_GenerarComprobanteDesdeComprobantes = in.p_GenerarComprobanteDesdeComprobantes;
        java.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
        java.p_IngresoEgresoGenerar = new ArrayList<>();
        for (IngresoEgresoGenerar_Entrada x : in.p_IngresoEgresoGenerar) {
            java.p_IngresoEgresoGenerar.add(copiar(x, CrearComprobanteJavaService.IngresoEgresoGenerar_Entrada.class));
        }
        java.p_ComprobanteGenerar = new ArrayList<>();
        for (ComprobanteGenerar_Entrada x : in.p_ComprobanteGenerar) {
            java.p_ComprobanteGenerar.add(copiar(x, CrearComprobanteJavaService.ComprobanteGenerar_Entrada.class));
        }
        java.p_FormaDePago = new ArrayList<>();
        for (FormaDePago_Entrada x : in.p_FormaDePago) {
            java.p_FormaDePago.add(copiar(x, CrearComprobanteJavaService.FormaDePago_Entrada.class));
        }
        return java;
    }

    /** Copy of a flat contract object into the equivalent class of another service (same field names). */
    private static <T> T copiar(Object origen, Class<T> destino) {
        return JsonLegacy.PLATAFORMA.fromJson(JsonLegacy.PLATAFORMA.toJsonTree(origen), destino);
    }
}
