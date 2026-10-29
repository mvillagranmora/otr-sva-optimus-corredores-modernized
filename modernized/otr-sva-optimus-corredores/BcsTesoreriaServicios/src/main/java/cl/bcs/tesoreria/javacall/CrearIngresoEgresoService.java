package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.CrearComprobanteService.IngresoEgresoGenerar_Entrada;
import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.javacall.contrato.AgregarLog;
import cl.bcs.tesoreria.javacall.contrato.RevisarRequisitosBasicos;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja;
import cl.bcs.tesoreria.javacall.pojo.IngresoEgreso;
import cl.bcs.tesoreria.javacall.pojo.IngresoEgresoFormaDePago;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Secuencias;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearIngresoEgreso} (behaviours TES-A-20 .. TES-A-79): creates the treasury movements (ingreso / egreso) and their
 * payment forms from a request. The request is staged in two work tables whose SQL resolves the master data ("_Real" columns), validated item by item
 * (a movement with an error leaves the process together with its payment forms), optionally checked against the cash flow of the portfolio, inserted,
 * logged, and optionally turned into vouchers (CrearComprobante) that can be liquidated (LiquidarComprobante).
 *
 * <p>Legacy defects reproduced on purpose (README "Legacy defects", CrearIngresoEgreso): a card payment form with null card ids ends in a
 * NullPointerException reported as the generic error; {@code IdTipoComprobante == "CSC"} compares references; a user without employee is a
 * NullPointerException turned into the generic error.
 */
@Service
public class CrearIngresoEgresoService {

    static final String SECUENCIA_INGRESO_EGRESO = "BSCL.Tesoreria.BD.Secuencias::IngresoEgreso";
    static final String SECUENCIA_FORMA_DE_PAGO = "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago";

    private static final String SQL_INSERT_INGRESO_EGRESO = loadSql("sql/CrearIngresoEgreso_InsertIngresoEgreso.sql");
    private static final String SQL_INSERT_FORMA_DE_PAGO = loadSql("sql/CrearIngresoEgreso_InsertFormaDePago.sql");
    private static final String SQL_SELECT_PREFERENCIAS_NEGOCIO = loadSql("sql/Globales_SelectPreferenciasNegocioTesoreria.sql");
    private static final String SQL_INSERT_PROCESO = loadSql("sql/CrearIngresoEgreso_InsertIngresoEgresoProceso.sql");
    private static final String SQL_SELECT_PROCESO = loadSql("sql/CrearIngresoEgreso_SelectIngresoEgresoProceso.sql");
    private static final String SQL_INSERT_FORMA_DE_PAGO_PROCESO = loadSql("sql/CrearIngresoEgreso_InsertIngresoEgresoFormaDePagoProceso.sql");
    private static final String SQL_SELECT_FORMA_DE_PAGO_PROCESO = loadSql("sql/CrearIngresoEgreso_SelectIngresoEgresoFormaDePagoProceso.sql");
    private static final String SQL_SELECT_COMPROBANTES = loadSql("sql/CrearIngresoEgreso_SelectComprobantes.sql");
    private static final String SQL_SELECT_CORREDORA = loadSql("sql/CrearIngresoEgreso_SelectCorredora.sql");
    private static final String SQL_SELECT_INSTRUMENTO = loadSql("sql/CrearIngresoEgreso_SelectInstrumento.sql");
    private static final String SQL_SELECT_EMPLEADO_DIGITADOR = loadSql("sql/CrearIngresoEgreso_SelectCodigoEjecutivoDigitador.sql");
    private static final String SQL_SELECT_EMPLEADO_USUARIO = loadSql("sql/CrearIngresoEgreso_SelectEmpleadoUsuario.sql");
    private static final String SQL_DELETE_PROCESO = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso\"";
    private static final String SQL_DELETE_FORMA_DE_PAGO_PROCESO = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso\"";

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

        public static class CrearIngresoEgreso_Entrada 
        {
            public Long                            p_IdEmpresa;
            public Long                            p_IdUsuario;
            public String                        p_IdModulo;
            public String                        p_SimularOperacion = "N";
            public String                        p_UsaReglasNegocio;        
            public String                        p_IdOrigenProceso;
            public String                       p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
            public String                        p_EsIngresoUI = "N";
            public String                        p_FechaMovimiento = "0001-01-01";   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
            public List<IngresoEgreso_Entrada>    p_IngresoEgresoGenerar;
            public List<FormaDePago_Entrada>    p_FormaDePago;


            public CrearIngresoEgreso_Entrada()
            {
                this.p_EsIngresoAutomatico     = "S";
                this.p_EsIngresoUI            = "N";
                this.p_SimularOperacion     = "N";
                this.p_FechaMovimiento         = "0001-01-01";
                this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgreso_Entrada>();
                this.p_FormaDePago             = new ArrayList<FormaDePago_Entrada>();
            }        

            public String toString() {
                return Utils.printJson(this, false);
            }
        }

        public static class IngresoEgreso_Entrada
        {
            public Long          NumeroReferencia;                  //    BIGINT 
            public String        IdTipoMovimiento;                   //    NVARCHAR(6)    -- MOVING, MOVEGR
            public String        IdTipoPersona;                   //    NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
               public Long          IdPersona;                       //    BIGINT
               public Long          IdPortafolio;                       //    BIGINT 
               public String        IdMoneda;                           //    NVARCHAR(3)
               public BigDecimal     Monto                 = BigDecimal.ZERO;                           //    DECIMAL(26,8)
               public String       FechaLiquidacion;                   //    NVARCHAR(10)                                                                      
               public String        IdConcepto;                       //    NVARCHAR(6)
               public String        IdTipoPersonaTitularPago;          //    NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
               public Long          IdPersonaTitularPago;               //    BIGINT
               public Long          IdPortafolioTitularPago;          //    BIGINT
               public Long          IdCaja;                           //    BIGINT
               public String        SecuenciaAgrupacion = "";         //    NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
               public Long          SecuenciaRelacionada = 0L;         //    BIGINT          -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
               public String        Glosa;                           //    NVARCHAR(260)
            public String        RequiereAutorizacion = "N";      //    NVARCHAR(260)    

               //Parámetros Referenciales(Opcionales)
               public Long          IdEmpleadoDigitador;               //    BIGINT
               public Long          IdSucursalEmpleadoDigitador;      //    BIGINT
               public String       IdTipoProducto;                   //    NVARCHAR(4)
               public String        IdTipoOperacion;                   //    NVARCHAR(6)                                        
               public String        IdTipoComprobante;               //    NVARCHAR(3)
               public Long          IdComprobante;                   //    BIGINT
               public String        FechaComprobante;                   //    NVARCHAR(40)
               public String        FechaLiquidacionComprobante;      //    NVARCHAR(40)

               public Long         IdOperacion;                       //    BIGINT            
            public String        FechaOperacion;                   //    NVARCHAR(40)
            public String       IdCondicionLiquidacion;          //    NVARCHAR(2)
            public String        IdBolsa;                           //    NVARCHAR(3)
            public String        IdMonedaOperacion;               //    NVARCHAR(3)
            public BigDecimal     MontoOperacion    = BigDecimal.ZERO;                   //    DECIMAL(26,8)
            public String        IdTipoReajuste;                   //    NVARCHAR(3)
            public String        FechaParidad;                       //    NVARCHAR(40)
            public BigDecimal     ValorParidad    = BigDecimal.ZERO;                   //     DECIMAL(26,8)
            public Long         FolioCartera;                       //    BIGINT
            public Long          IdInstrumento;                   //    BIGINT
            public String        IdTipoInstrumento;               //    NVARCHAR(3)
            public String        IdClasificacionTipoInstrumento; //    NVARCHAR(3)

            //Indicadores
            public String        Bilateral = "N";                //    NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
            public String        GeneraComprobante = "N";         //    NVARCHAR(1)  -- S/N: genera comprobante automático
            public String        LiquidarComprobante = "N";         //    NVARCHAR(1)  -- S/N: genera comprobante automático
            public String        AgruparEnComprobante = "N";     //    NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
            public String        MueveCuentaInversion;              //    NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
            public String        TitularPago;                       //    NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
            public String        EsTraspaso = "N";                //  NVARCHAR(1)  -- S/N: Si es traspaso y se esta generando el ingreso y egreso
            public String        IdTipoCanalIngreso = "OPT"; 
            public String        CodigoEjecutivoDigitador = "";

            //Uso exclusivo Aporte CSC
            public String        FolioCSC = "";

            public IngresoEgreso_Entrada()
            {
                this.Bilateral               = "N";    //Valores por defecto
                this.GeneraComprobante    = "N";
                this.LiquidarComprobante  = "N";
                this.AgruparEnComprobante = "N";
                this.SecuenciaAgrupacion  = "";
                this.SecuenciaRelacionada = 0L;
                this.EsTraspaso = "N";
            }    
            public String toString(){
                return Utils.printJson(this, false);
            }

        }

        public static class FormaDePago_Entrada 
        {
            public Long         NumeroReferencia;                //  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
            public Long         NumeroReferenciaFormaDePago;    //  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
            public String        IdMedioDePago;                  //    NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
            public String        IdModalidadDePago;              //    NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
            public String        IdMoneda;                       //    NVARCHAR(3)
            public BigDecimal   Monto;                             //    DECIMAL(26,8)
            public Long         IdCuentaBancariaCorredora;         //    BIGINT
            public Long         IdTarjetaBancariaCorredora;     //    BIGINT
            public Long         IdChequera;                     //    BIGINT
            public Long         NumeroDocumentoCorredora;         //    BIGINT
            public String        FechaDocumentoPagoCorredora;    //    NVARCHAR(40)
            public Long         IdCuentaBancariaSocio;             //    BIGINT
            public Long         IdBancoSocio;                     //    BIGINT
            public String        NombreCuentaBancariaSocio;        //    NVARCHAR(100)
            public String        NumeroCuentaBancariaSocio;        //    NVARCHAR(100)
            public String        IdTipoCuentaBancariaSocio;        //    NVARCHAR(4)
            public String        NombreTipoCuentaBancariaSocio;     //  NVARCHAR(100)
            public Long         IdTarjetaBancariaSocio;            //    BIGINT
            public String        IdTipoTarjetaBancariaSocio;      //    NVARCHAR(40)
            public String        NombreTipoTarjetaBancariaSocio; //    NVARCHAR(100)
            public String        NumeroTarjetaBancariaSocio;      //    NVARCHAR(100)
            public String        NumeroSerieSocio;                  //    NVARCHAR(40)
            public Long         NumeroDocumentoSocio;            //    BIGINT
            public String        FechaDocumentoPagoSocio;          //    NVARCHAR(40)    

            public String toString(){
                return Utils.printJson(this, false);
            }


        }

        public static class CrearIngresoEgreso_Salida {

            public List<MensajeSalida>                    P_OUT_MENSAJES;
            public Long                                    P_OUT_ERRORESCONTADOR;
            public List<IngresoEgresoGenerado_Salida>    P_OUT_INGRESOEGRESOGENERADO;
            public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;

            public CrearIngresoEgreso_Salida()
            {    
                this.P_OUT_MENSAJES              = new ArrayList<MensajeSalida>();
                this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
                this.P_OUT_RESULTADOPROCESO         = new ArrayList<ResultadoProceso_Salida>();
            }

            public String toString() {
                return Utils.printJson(this, false);
            }
        }    

        public static class IngresoEgresoGenerado_Salida 
        {
            public Long     NumeroReferencia;            // BIGINT
            public Long     IdIngresoEgreso;             // BIGINT
            public String     IdTipoMovimiento;            // NVARCHAR(6)

            public String toString(){
                return Utils.printJson(this, false);
            }
        }

        public static class ResultadoProceso_Salida extends BcsPojo
        {
            public String     Campo;        // VARCHAR(40)   -- Ej: "IdIngresoEgreso"
            public Long       Id;           // BIGINT        -- IdIngresoEgreso generado  Ej: 9999
            public String     Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
            public String    Resultado;    // VARCHAR(40)   -- Ej: "ING"
            public Long NumeroReferencia;

            public ResultadoProceso_Salida (){
                this.NumeroReferencia = 0L;
            }
        }



        public static class IngresoEgreso_Proceso
        {
            public Long                 IdIngresoEgreso;
            public Long                    IdEmpresa; 
            public String                IdModulo; 
            public Long                    IdUsuario;
            public java.sql.Date        FechaMovimiento;
            public String                NombrePersona; 
            public String                IdTipoDocumentoIdentidad; 
            public String                DocumentoIdentidad;
            public String                NombrePersonaTitularPago;
            public String                IdTipoDocumentoIdentidadTitularPago; 
            public String                DocumentoIdentidadTitularPago;
            public String               EsIngresoAutomatico;            // S/N
            public String               NeteaComprobante;               // S/N   preferencias del portafolio
            public String               IdEstado;
            public String                 RequiereAutorizacion = "N";

            //Notificacion
            public String                Tipo;
            public String                Nombre;
            public String                NumeroTel;
            public String                NombreConcepto;
            public String                Corredora;
            public String                Correo;
            public String                DI;
            public String                TipoDoc;

            public Long                  NumeroReferencia;                  //    BIGINT
            public String                IdTipoMovimiento;                   //    NVARCHAR(6)    -- MOVING, MOVEGR
            public String                IdTipoPersona;                   //    NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
               public Long                  IdPersona;                       //    BIGINT
               public Long                  IdPortafolio;                       //    BIGINT 
               public String                IdMoneda;                           //    NVARCHAR(3)
               public BigDecimal             Monto;                           //    DECIMAL(26,8)
               public java.sql.Date        FechaLiquidacion;                                                                                 
               public String                IdConcepto;                       //    NVARCHAR(6)
               public String                IdTipoPersonaTitularPago;          //    NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
               public Long                  IdPersonaTitularPago;               //    BIGINT
               public Long                  IdPortafolioTitularPago;          //    BIGINT
               public Long                  IdCaja;                           //    BIGINT
               public String                SecuenciaAgrupacion;              //    NVARCHAR(40)  -- Aquellos mov con la misma secuencia no son mezclados en distintos comprobante
               public Long                  SecuenciaRelacionada;               //    BIGINT          -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
               public String                Glosa;                           //    NVARCHAR(260)

               //Parámetros Opcionales(Referenciales)
               public Long                  IdEmpleadoDigitador;               //    BIGINT
               public Long                  IdSucursalEmpleadoDigitador;      //    BIGINT
               public String               IdTipoProducto;                   //    NVARCHAR(4)
               public String                IdTipoOperacion;                   //    NVARCHAR(6)
            public String                IdTipoComprobanteOrigen;         //    NVARCHAR(3)
            public Long                    IdComprobanteOrigen;             //    BIGINT
            public java.sql.Timestamp    FechaComprobanteOrigen;            //    NVARCHAR(40)
            public java.sql.Timestamp    FechaLiquidacionComprobanteOrigen;//NVARCHAR(40)
               public Long                 IdOperacion;                       //    BIGINT            
            public java.sql.Timestamp   FechaOperacion;                   //    NVARCHAR(40)
            public String               IdCondicionLiquidacion;          //    NVARCHAR(2)
            public String                IdBolsa;                           //    NVARCHAR(3)
            public String                IdMonedaOperacion;               //    NVARCHAR(3)
            public BigDecimal             MontoOperacion;                   //    DECIMAL(26,8)
            public String                IdTipoReajuste;                   //    NVARCHAR(3)
            public java.sql.Timestamp   FechaParidad;                       //    NVARCHAR(40)
            public BigDecimal             ValorParidad;                   //     DECIMAL(26,8)
            public Long                 FolioCartera;                       //    BIGINT
            public Long                  IdInstrumento;                   //    BIGINT
            public String                IdTipoInstrumento;               //    NVARCHAR(3)
            public String                IdClasificacionTipoInstrumento; //    NVARCHAR(3)

            //Indicadores
            public String                EsBilateral;                       //    NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
            public String                GeneraComprobante;              //    NVARCHAR(1)  -- S/N: genera comprobante automático
            public String                LiquidarComprobante;             //    NVARCHAR(1)  -- S/N: liquida comprobante automático
            public String                AgrupaComprobante;              //    NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
            public String                MueveCuentaInversion;              //    NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
            public String                TitularPago;                       //    NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
            public String                EsTrapaso = "N";                        //  NVARCHAR(1)  -- S/N: Si es traspaso y se esta generando el ingreso y egreso

            //Para validaciones
            public long                 errores         = 0;
            public long                 advertencias     = 0;
            public long                 autorizaciones     = 0;
            public long                 ok                 = 0;
            public String                 IdTipoMovimiento_Real;
            public String                 IdConcepto_Real;
            public String                 IdMoneda_Real;
            public String                 IdEstadoMoneda_Real;
            public Long                   IdCaja_Real;
            public String                 IdMonedaTesoreria_Real;
            public Long                  IdPersona_Real;
            public String                 IdEstado_PersonaReal;
            public Long                  IdPersonaRelacionada_Real;
            public String                 IdEstado_RelacionadoReal;        
            public String                 IdCondicionLiquidacion_Real;    
            public String                 IdTipoProducto_Real;
            public String                 IdTipoOperacion_Real;
            public Long                 CountSec;


            public String                 BloqueadoPorCumplimiento = "N";
            public String                EstadoPortafolio;
            public String                CodigoPortafolio;
            public String                EstadoPortafolioTitularPago;
            public String                CodigoPortafolioTitularPago;

            //para saldo caja
            public BigDecimal            TotalMontoPortafolio = BigDecimal.ZERO;
            public BigDecimal             SaldoCajaPortafolio = BigDecimal.ZERO;

            // Canal de origen
            public String                IdTipoCanalIngreso = "OPT"; 

            //Uso exclusivo Aporte CSC
            public String                FolioCSC = "";
            public String                GeneraComprobanteAutomaticamente = "N";

            public String                CodigoEjecutivoDigitador = "";

            public String toString() {
                return Utils.printJson(this, false);
            }        
        }

        public static class FormaDePago_Proceso
        {    
            public Long                 NumeroReferencia;                    //  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
            public Long                 NumeroReferenciaFormaDePago;        //  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
            public String                IdMedioDePago;                      //    NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
            public String                IdModalidadDePago;                  //    NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
            public String                NombreModalidadDePago;                 //    NVARCHAR(100)        
            public String                NombreMedioDePago;                    //    NVARCHAR(100)
            public String                IdMoneda;                           //    NVARCHAR(3)
            public BigDecimal           Monto;                                 //    DECIMAL(26,8)
            public Long                 IdCuentaBancariaCorredora;             //    BIGINT
            public Long                 IdTarjetaBancariaCorredora;         //    BIGINT
            public Long                 IdChequera;                         //    BIGINT
            public Long                 NumeroDocumentoCorredora;             //    BIGINT
            public java.sql.Timestamp    FechaDocumentoPagoCorredora;        //    SECONDATE(19)
            public Long                 IdCuentaBancariaSocio;                 //    BIGINT
            public Long                 IdBancoSocio;                         //    BIGINT
            public String                NombreCuentaBancariaSocio;            //    NVARCHAR(100)
            public String                NumeroCuentaBancariaSocio;            //    NVARCHAR(100)
            public String                IdTipoCuentaBancariaSocio;            //    NVARCHAR(4)
            public String                NombreTipoCuentaBancariaSocio;         //  NVARCHAR(100)
            public Long                 IdTarjetaBancariaSocio;                //    BIGINT,
            public String                IdTipoTarjetaBancariaSocio;          //    NVARCHAR(40)
            public String                NombreTipoTarjetaBancariaSocio;     //    NVARCHAR(100)
            public String                NumeroTarjetaBancariaSocio;          //    NVARCHAR(100)
            public String                NumeroSerieSocio;                      //    NVARCHAR(40)
            public Long                 NumeroDocumentoSocio;                //    BIGINT
            public java.sql.Timestamp    FechaDocumentoPagoSocio;              //    SECONDATE(19)
            public Long                 IdIngresoEgresoFormaDePago;            //    BIGINT
            public Long                 IdIngresoEgreso;                    //    BIGINT
            public String                IdTipoMovimiento;                    //    NVARCHAR(6)
            public String                IdTipoCuentaBancariaCorredora;       //    NVARCHAR(4)
            public String                NombreTipoCuentaBancariaCorredora;    //    NVARCHAR(100)
            public String                NombreCuentaBancariaCorredora;        //    NVARCHAR(100)
            public String               NumeroCuentaBancariaCorredora;        //    NVARCHAR(100)
            public String               IdTipoTarjetaBancariaCorredora;      //    NVARCHAR(40)
            public String                NombreTipoTarjetaBancariaCorredora;    //    NVARCHAR(100)
            public String                NumeroTarjetaBancariaCorredora;        //    NVARCHAR(100)
            public java.sql.Timestamp      FechaVctoTarjetaBancariaSocio;         //  SECONDATE(19)
            public String                IdEstado;                            //    NVARCHAR(3)

            //Para validaciones
            public long                 errores         = 0;
            public long                 advertencias     = 0;
            public long                 autorizaciones     = 0;
            public long                 ok                 = 0;        
            public String                IdMedioDePago_Real;
            public String                IdModalidadDePago_Real;
            public String                IdMoneda_Real;
            public String                IdEstadoMoneda_Real;
            public Long                   IdCuentaBancariaCorredora_Real;
            public Long                    IdCuentaBancariaSocio_Real;
            public Long                    IdTarjetaCorredoraBancaria_Real;
            public Long                    IdTarjetaSocioBancaria_Real;
            public Integer                 ExisteDescuadreEnMontos;
            public Integer                 ExisteDiferenciaMoneda;


            public String toString() {
                return Utils.printJson(this, false);
            }                
        }    

        public static class PreferenciaTesoreria 
        {    
            public Long               IdPortafolio;
            public Long               IdCliente;
            public String           IdTipoCliente;
            public String           TES_NIE;   //Indicador que Netea Ingresos y Egresos

            public String toString() {
                return Utils.printJson(this, false);
            }
        }

        public static class InformacionIngresoEgreso    
        {
            public Long               NumeroReferencia;
            public String             IdTipoMovimiento_Real;
            public String             IdConcepto_Real;
            public String             IdMoneda_Real;
            public String             IdEstadoMoneda_Real;
            public Long               IdCaja_Real;
            public String             IdMonedaTesoreria_Real;
            public String             IdCondicionLiquidacion_Real;    
            public String             IdTipoProducto_Real;
            public String             IdTipoOperacion_Real;        
            public Long             CountSec;

            public String            Tipo;
            public String            Nombre;
            public String            NumeroTel;
            public String            NombreConcepto;
            public String            Corredora;
            public String            Correo;
            public String            DI;
            public String            TipoDoc;


            public String            BloqueadoPorCumplimiento = "N";
            public String            GeneraComprobanteAutomaticamente = "N";
            public String            EstadoPortafolio;
            public String            CodigoPortafolio;
            public String            EstadoPortafolioTitularPago;
            public String            CodigoPortafolioTitularPago;

            //Datos de la persona
            public String            NombrePersona; 
            public String            IdTipoDocumentoIdentidad; 
            public String            DocumentoIdentidad;
            public Long              IdPersona_Real;
            public String             IdEstado_PersonaReal;


            //Datos del titular pago
            public String            NombrePersonaTitularPago;
            public String            IdTipoDocumentoIdentidadTitularPago; 
            public String            DocumentoIdentidadTitularPago;
            public Long              IdPersonaRelacionada_Real;
            public String             IdEstado_RelacionadoReal;
        }

        public static class InformacionFormaDePago    
        {
            public Long               NumeroReferencia;
            public Long               NumeroReferenciaFormaDePago;
            public String            IdMedioDePago_Real;
            public String            IdModalidadDePago_Real;
            public String            IdMoneda_Real;
            public String            IdEstadoMoneda_Real;
            public Long               IdCuentaBancariaCorredora_Real;
            public Long                IdCuentaBancariaSocio_Real;
            public Long                IdTarjetaCorredoraBancaria_Real;
            public Long                IdTarjetaSocioBancaria_Real;
            public String            NombreMedioDePago;
            public String            NombreModalidadDePago;
            public String            IdTipoCuentaBancariaCorredora;
            public String            NombreTipoCuentaBancariaCorredora;
            public String            NombreCuentaBancariaCorredora;
            public String            NumeroCuentaBancariaCorredora;
            public String            IdTipoCuentaBancariaSocio;
            public String            NombreTipoCuentaBancariaSocio;
            public String            NumeroCuentaBancariaSocio;
            public Long                IdBancoSocio;
            public Integer            ExisteDescuadreEnMontos;
            public Integer            ExisteDiferenciaMoneda;

            public String toString() {
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

        public static class InformacionCorredora extends BcsPojo{
            public String     Nombre;
            public Long     IdPersona;
            public String    DocumentoIdentidad;
        }

        public static class ObjInstrumento extends BcsPojo{
            public Long     IdInstrumento;
            public String     Nemotecnico;
        }

        public static class Empleado extends BcsPojo{
            public Long     IdEmpleadoDigitador;
            public String     CodigoEmpleado;
        }

    /** Employee of a user (row of the legacy {@code SocioNegocioUtils.obtenerEmpleadoUsuario} query). */
    public static class EmpleadoUsuario extends BcsPojo {
        public Long IdUsuario;
        public Long IdEmpleado;
        public String CodigoEmpleado;
    }

    // ------------------------------------------------------------------ service

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Secuencias secuencias;
    private final CrearComprobanteService crearComprobante;
    private final LiquidarComprobanteService liquidarComprobante;
    private final Colaboradores colaboradores;

    public CrearIngresoEgresoService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, CrearComprobanteService crearComprobante, LiquidarComprobanteService liquidarComprobante,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.crearComprobante = crearComprobante;
        this.liquidarComprobante = liquidarComprobante;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: decodes the JSON, runs {@link #logica} and recounts the errors from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        CrearIngresoEgreso_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, CrearIngresoEgreso_Entrada.class);
        CrearIngresoEgreso_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /**
     * Legacy {@code logica}. Any exception inside the process (a missing configuration parameter, a user without employee, a null amount, a failed
     * statement) is swallowed and reported as the generic message {@code UI_ERROR_INGRESO_EGRESO}; the work already written stays (TES-A-20..79).
     * The two staging tables are always emptied at the end.
     */
    @Transactional(rollbackFor = Exception.class)
    public CrearIngresoEgreso_Salida logica(CrearIngresoEgreso_Entrada in) throws Exception {
        CrearIngresoEgreso_Salida out = new CrearIngresoEgreso_Salida();
        try {
            procesar(in, out);
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README, CrearIngresoEgreso): every exception becomes the generic message; null values give "<code> sin traduccion"
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_INGRESO_EGRESO",
                    diccionario.mensaje(in.p_IdEmpresa, "UI_ERROR_INGRESO_EGRESO", null)));
        } finally {
            jdbc.getJdbcTemplate().execute(SQL_DELETE_PROCESO);
            jdbc.getJdbcTemplate().execute(SQL_DELETE_FORMA_DE_PAGO_PROCESO);
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return limpiarSalida(out);
    }

    /** State of one run: the movements and payment forms being validated and the ones already rejected. */
    private static final class Corrida {
        final CrearIngresoEgreso_Entrada in;
        final CrearIngresoEgreso_Salida out;
        String fechaSistema;
        boolean validaFlujoCajaNegocio;
        boolean validaFlujoCajaManual;
        final List<IngresoEgreso_Proceso> movimientos = new ArrayList<>();
        final List<FormaDePago_Proceso> formasDePago = new ArrayList<>();
        final List<IngresoEgreso_Proceso> movimientosConErrores = new ArrayList<>();
        final List<FormaDePago_Proceso> formasDePagoConErrores = new ArrayList<>();

        Corrida(CrearIngresoEgreso_Entrada in, CrearIngresoEgreso_Salida out) {
            this.in = in;
            this.out = out;
        }

        boolean validaFlujoCaja() {
            return validaFlujoCajaNegocio || (validaFlujoCajaManual && "TES".equalsIgnoreCase(in.p_IdModulo) && "N".equalsIgnoreCase(in.p_EsIngresoAutomatico));
        }
    }

    /** The whole process; every early exit of the legacy is a plain {@code return} (the caller finishes the output). */
    private void procesar(CrearIngresoEgreso_Entrada in, CrearIngresoEgreso_Salida out) throws Exception {
        Corrida c = new Corrida(in, out);
        c.fechaSistema = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");
        c.validaFlujoCajaNegocio = "S".equalsIgnoreCase(parametroOpcional(in, "VALIDAFLUJOCAJAEGRESONEGOCIO"));
        c.validaFlujoCajaManual = "S".equalsIgnoreCase(parametroOpcional(in, "VALIDAFLUJOCAJAEGRESOMANUAL"));

        if (in.p_IngresoEgresoGenerar.size() == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_SOLICITUDES_DE_INGRESO_EGRESO",
                    diccionario.mensaje(in.p_IdEmpresa, "NO_SE_INGRESARON_SOLICITUDES_DE_INGRESO_EGRESO", new HashMap<>(0))));
            return;
        }
        if (in.p_FormaDePago.size() == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "NO_SE_INGRESARON_FORMAS_DE_PAGO",
                    diccionario.mensaje(in.p_IdEmpresa, "NO_SE_INGRESARON_FORMAS_DE_PAGO", new HashMap<>(0))));
        }

        // basic requirements of the company, the user and the module
        RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida basicos = Colaborador.invocar(colaboradores, "RevisarRequisitosBasicos",
                requisitosBasicos(in), RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.class);
        if (MensajeSalida.numeroErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
            return;
        }

        if ("S".equals(Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.TESORERIA, "SEPARACOMPROBANTECOMSPT"))) {
            for (IngresoEgreso_Entrada ie : in.p_IngresoEgresoGenerar) {
                if (ie.IdTipoCanalIngreso.equals("FMM") && ie.IdTipoOperacion.equals("COMSPT")) {
                    ie.AgruparEnComprobante = "N";
                }
            }
        }

        // the input is staged in the work tables (the SQL derives the "_Real" data used to validate it)
        Consultas.lote(jdbc, SQL_INSERT_PROCESO, Utils.List2ListHashGenericObject(in.p_IngresoEgresoGenerar));
        Map<Long, InformacionIngresoEgreso> infoMovimientos = indexar(jdbc.query(SQL_SELECT_PROCESO, Map.of("IdEmpresa", in.p_IdEmpresa),
                Filas.de(InformacionIngresoEgreso.class)), m -> m.NumeroReferencia);
        construirMovimientos(c, infoMovimientos);
        aplicarPreferenciasDeNeteo(c);

        Consultas.lote(jdbc, SQL_INSERT_FORMA_DE_PAGO_PROCESO, Utils.List2ListHashGenericObject(in.p_FormaDePago));
        Map<Long, InformacionFormaDePago> infoFormas = indexar(jdbc.query(SQL_SELECT_FORMA_DE_PAGO_PROCESO, Map.<String, Object>of(),
                Filas.de(InformacionFormaDePago.class)), f -> f.NumeroReferenciaFormaDePago);
        construirFormasDePago(c, infoFormas);

        validarMovimientos(c);
        limpiarIngresosEgresos(c.movimientos, c.formasDePago, c.movimientosConErrores);
        if (c.movimientos.size() == 0) {
            return;
        }
        validarFormasDePago(c);
        limpiarFormasDePago(c.formasDePago, c.movimientos, c.formasDePagoConErrores);
        if (c.movimientos.size() == 0) {
            return;
        }

        if (c.validaFlujoCaja()) {
            calcularFlujoDeCaja(c);
            for (IngresoEgreso_Proceso iep : c.movimientos) {
                // outgoing movements only
                requisito(c, iep.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && iep.TotalMontoPortafolio.doubleValue() > iep.SaldoCajaPortafolio.doubleValue(),
                        "FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO", MensajeSalida.MENSAJE_ERROR, iep);
            }
            limpiarIngresosEgresos(c.movimientos, c.formasDePago, c.movimientosConErrores);
            if (c.movimientos.size() == 0) {
                return;
            }
        }

        List<IngresoEgreso> ingresosEgresosFinales = insertarMovimientos(c);
        registrarLog(c, ingresosEgresosFinales);
        insertarFormasDePago(c, ingresosEgresosFinales);
        prepararSalida(c);
        generarYLiquidarComprobantes(c);
    }

    /** Optional TESORERIA flag: absent (or origin PAC, which skips the check) means "N", as the legacy swallowed the missing-parameter error. */
    private String parametroOpcional(CrearIngresoEgreso_Entrada in, String nombre) {
        if ("PAC".equalsIgnoreCase(in.p_IdOrigenProceso)) {
            return "N";
        }
        String valor = configuracion.parametro(in.p_IdEmpresa, Parametros.TESORERIA, nombre);
        return valor == null ? "N" : valor;
    }

    private static Map<String, Object> requisitosBasicos(CrearIngresoEgreso_Entrada in) {
        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("p_IdEmpresa", in.p_IdEmpresa);
        entrada.put("p_IdUsuario", in.p_IdUsuario);
        entrada.put("p_IdModulo", in.p_IdModulo);
        return entrada;
    }

    /** Index by key; a duplicate key is the legacy {@code List2Hash} failure ("key field is not unique"). */
    private static <T> Map<Long, T> indexar(List<T> filas, Function<T, Long> clave) throws Exception {
        Map<Long, T> indice = new HashMap<>();
        for (T fila : filas) {
            if (indice.containsKey(clave.apply(fila))) {
                throw new Exception("key field is not unique");
            }
            indice.put(clave.apply(fila), fila);
        }
        return indice;
    }

    // ------------------------------------------------------------------ movements: build

    private void construirMovimientos(Corrida c, Map<Long, InformacionIngresoEgreso> infoMovimientos) throws Exception {
        CrearIngresoEgreso_Entrada in = c.in;
        List<InformacionCorredora> corredora = jdbc.query(SQL_SELECT_CORREDORA, Map.<String, Object>of(), Filas.de(InformacionCorredora.class));
        EmpleadoUsuario empleadoUsuario = empleadoDelUsuario(in);

        List<String> codigos = new ArrayList<>();
        for (IngresoEgreso_Entrada ie : in.p_IngresoEgresoGenerar) {
            codigos.add(ie.CodigoEjecutivoDigitador);
        }
        Map<String, Empleado> empleados = new HashMap<>();
        for (Empleado e : jdbc.query(SQL_SELECT_EMPLEADO_DIGITADOR, Map.of("CodigoEjecutivoDigitador", Consultas.enLista(codigos)),
                Filas.de(Empleado.class))) {
            if (empleados.containsKey(e.CodigoEmpleado)) {
                throw new Exception("key field is not unique");
            }
            empleados.put(e.CodigoEmpleado, e);
        }

        for (IngresoEgreso_Entrada ie : in.p_IngresoEgresoGenerar) {
            InformacionIngresoEgreso info = infoMovimientos.get(ie.NumeroReferencia);
            IngresoEgreso_Proceso p = new IngresoEgreso_Proceso();
            copiar(ie, p);

            p.IdEmpresa = in.p_IdEmpresa;
            p.IdModulo = in.p_IdModulo;
            p.IdUsuario = in.p_IdUsuario;
            p.EsIngresoAutomatico = in.p_EsIngresoAutomatico;
            p.RequiereAutorizacion = ie.RequiereAutorizacion;

            // person and payment owner
            p.NombrePersona = info.NombrePersona;
            p.IdTipoDocumentoIdentidad = info.IdTipoDocumentoIdentidad;
            p.DocumentoIdentidad = info.DocumentoIdentidad;
            p.NombrePersonaTitularPago = info.NombrePersonaTitularPago;
            p.IdTipoDocumentoIdentidadTitularPago = info.IdTipoDocumentoIdentidadTitularPago;
            p.DocumentoIdentidadTitularPago = info.DocumentoIdentidadTitularPago;

            p.FechaLiquidacion = fechaVacia(ie.FechaLiquidacion) ? Fecha.toDate(c.fechaSistema) : Fecha.toDate(ie.FechaLiquidacion);
            p.SecuenciaRelacionada = ie.SecuenciaRelacionada == null ? 0L : ie.SecuenciaRelacionada;
            p.IdTipoComprobanteOrigen = ie.IdTipoComprobante;
            p.IdComprobanteOrigen = ie.IdComprobante;
            p.FechaComprobanteOrigen = fechaHora(ie.FechaComprobante);
            // LEGACY DEFECT kept (see README, CrearIngresoEgreso): IdTipoComprobante == "CSC" compares references, so it is never true and FolioCSC is always ""
            p.FolioCSC = ie.IdTipoComprobante == "CSC" ? ie.FolioCSC : "";
            p.FechaLiquidacionComprobanteOrigen = fechaHora(ie.FechaLiquidacionComprobante);
            p.FechaMovimiento = fechaVacia(in.p_FechaMovimiento) ? Fecha.toDate(c.fechaSistema) : Fecha.toDate(in.p_FechaMovimiento);
            p.IdOperacion = ie.IdOperacion;
            p.FechaOperacion = fechaHora(ie.FechaOperacion);
            p.FechaParidad = fechaHora(ie.FechaParidad);
            p.NeteaComprobante = "N"; // reviewed below with the business preferences of the portfolio

            // indicators
            p.EsBilateral = ie.Bilateral;
            p.GeneraComprobante = ie.GeneraComprobante;
            p.GeneraComprobanteAutomaticamente = info.GeneraComprobanteAutomaticamente;
            p.AgrupaComprobante = ie.AgruparEnComprobante;
            p.MueveCuentaInversion = ie.MueveCuentaInversion; // obsolete column

            for (InformacionCorredora corredor : corredora) {
                p.BloqueadoPorCumplimiento = corredor.DocumentoIdentidad.equalsIgnoreCase(p.DocumentoIdentidad) ? "N" : info.BloqueadoPorCumplimiento;
            }

            // data used by the validations
            p.IdTipoMovimiento_Real = info.IdTipoMovimiento_Real;
            p.Tipo = info.Tipo;
            p.Nombre = info.Nombre;
            p.NumeroTel = info.NumeroTel;
            p.DI = info.DI;
            p.TipoDoc = info.TipoDoc;
            p.Correo = info.Correo;
            p.Corredora = info.Corredora;
            p.NombreConcepto = info.NombreConcepto;
            p.IdConcepto_Real = info.IdConcepto_Real;
            p.IdMoneda_Real = info.IdMoneda_Real;
            p.IdEstadoMoneda_Real = info.IdEstadoMoneda_Real;
            p.IdCaja_Real = info.IdCaja_Real;
            p.IdMonedaTesoreria_Real = info.IdMonedaTesoreria_Real;
            p.IdPersona_Real = info.IdPersona_Real;
            p.IdEstado_PersonaReal = info.IdEstado_PersonaReal;
            p.IdPersonaRelacionada_Real = info.IdPersonaRelacionada_Real;
            p.IdEstado_RelacionadoReal = info.IdEstado_RelacionadoReal;
            p.IdCondicionLiquidacion_Real = info.IdCondicionLiquidacion_Real;
            p.IdTipoProducto_Real = info.IdTipoProducto_Real;
            p.IdTipoOperacion_Real = info.IdTipoOperacion_Real;
            p.CountSec = info.CountSec;
            p.IdTipoCanalIngreso = ie.IdTipoCanalIngreso;
            p.EstadoPortafolio = info.EstadoPortafolio;
            p.CodigoPortafolio = info.CodigoPortafolio;
            p.EstadoPortafolioTitularPago = info.EstadoPortafolioTitularPago;
            p.CodigoPortafolioTitularPago = info.CodigoPortafolioTitularPago;

            if ("".equalsIgnoreCase(ie.CodigoEjecutivoDigitador)) {
                // LEGACY DEFECT kept (see README, CrearIngresoEgreso): a user without employee is a NullPointerException, reported as the generic error
                p.IdEmpleadoDigitador = empleadoUsuario.IdEmpleado;
            } else {
                Empleado emp = empleados.get(ie.CodigoEjecutivoDigitador);
                p.IdEmpleadoDigitador = emp != null ? emp.IdEmpleadoDigitador : 0L;
            }
            c.movimientos.add(p);
        }
    }

    /** The employee of the user (legacy {@code SocioNegocioUtils.obtenerEmpleadoUsuario}); null when the user has none. */
    private EmpleadoUsuario empleadoDelUsuario(CrearIngresoEgreso_Entrada in) throws Exception {
        List<EmpleadoUsuario> filas = jdbc.query(SQL_SELECT_EMPLEADO_USUARIO,
                Map.of("IdEmpresa", in.p_IdEmpresa, "IdUsuario", Consultas.enLista(List.of(in.p_IdUsuario))), Filas.de(EmpleadoUsuario.class));
        EmpleadoUsuario encontrado = null;
        for (EmpleadoUsuario e : filas) {
            if (encontrado != null && encontrado.IdUsuario.equals(e.IdUsuario)) {
                throw new IllegalStateException("Duplicate key " + e.IdUsuario);
            }
            if (in.p_IdUsuario.equals(e.IdUsuario)) {
                encontrado = e;
            }
        }
        return encontrado;
    }

    private static boolean fechaVacia(String fecha) {
        return fecha == null || fecha.isEmpty() || fecha.equalsIgnoreCase("0001-01-01");
    }

    /** Date or date-time text to a timestamp; the empty date is the placeholder 0001-01-01 00:00:00, a 10 character text gets midnight. */
    private static Timestamp fechaHora(String texto) {
        if (fechaVacia(texto)) {
            return Fecha.toDateTime("0001-01-01 00:00:00");
        }
        return Fecha.toDateTime(texto.length() == 10 ? texto + " 00:00:00" : texto);
    }

    @SuppressWarnings("deprecation")
    private static void copiar(Object origen, Object destino) throws Exception {
        Utils.copyPojo(origen, destino); // same-name, same-type public fields (legacy copyPojo contract)
    }

    private void aplicarPreferenciasDeNeteo(Corrida c) throws Exception {
        List<Long> portafolios = new ArrayList<>();
        for (IngresoEgreso_Proceso p : c.movimientos) {
            portafolios.add(p.IdPortafolioTitularPago);
        }
        List<PreferenciaTesoreria> filas = jdbc.query(SQL_SELECT_PREFERENCIAS_NEGOCIO, Map.of("idsPortafolio", Consultas.enLista(portafolios)),
                Filas.de(PreferenciaTesoreria.class));
        Map<Long, PreferenciaTesoreria> preferencias = new HashMap<>();
        for (PreferenciaTesoreria f : filas) {
            if (preferencias.containsKey(f.IdPortafolio)) {
                throw new Exception("key field is not unique");
            }
            preferencias.put(f.IdPortafolio, f);
        }
        if (preferencias.size() > 0) {
            for (IngresoEgreso_Proceso p : c.movimientos) {
                PreferenciaTesoreria pref = preferencias.get(p.IdPortafolioTitularPago);
                // LEGACY DEFECT kept (not listed in README: String reference compare against ""): an empty flag is not recognised unless the driver hands out the interned ""
                p.NeteaComprobante = pref == null || pref.TES_NIE == null || pref.TES_NIE == "" ? "N" : pref.TES_NIE;
            }
        }
    }

    // ------------------------------------------------------------------ payment forms: build

    private void construirFormasDePago(Corrida c, Map<Long, InformacionFormaDePago> info) throws Exception {
        Map<Long, IngresoEgreso_Proceso> movimientos = indexar(c.movimientos, m -> m.NumeroReferencia);
        for (FormaDePago_Entrada fe : c.in.p_FormaDePago) {
            InformacionFormaDePago i = info.get(fe.NumeroReferenciaFormaDePago);
            FormaDePago_Proceso p = new FormaDePago_Proceso();
            copiar(fe, p);
            IngresoEgreso_Proceso movimiento = movimientos.get(fe.NumeroReferencia);

            p.IdTipoMovimiento = movimiento.IdTipoMovimiento;
            p.Monto = fe.Monto;
            p.FechaVctoTarjetaBancariaSocio = Fecha.toDateTime("0001-01-01 00:00:00"); // not recorded for now
            p.NombreMedioDePago = i.NombreMedioDePago;
            p.NombreModalidadDePago = i.NombreModalidadDePago;
            p.IdTipoCuentaBancariaCorredora = i.IdTipoCuentaBancariaCorredora;
            p.NombreTipoCuentaBancariaCorredora = i.NombreTipoCuentaBancariaCorredora;
            p.NombreCuentaBancariaCorredora = i.NombreCuentaBancariaCorredora;
            p.NumeroCuentaBancariaCorredora = i.NumeroCuentaBancariaCorredora;
            p.IdTipoTarjetaBancariaCorredora = "";
            p.NombreTipoTarjetaBancariaCorredora = "";
            p.NumeroTarjetaBancariaCorredora = "";
            p.IdTipoCuentaBancariaSocio = vacio(p.IdTipoCuentaBancariaSocio) ? i.IdTipoCuentaBancariaSocio : p.IdTipoCuentaBancariaSocio;
            p.NombreTipoCuentaBancariaSocio = vacio(p.NombreTipoCuentaBancariaSocio) ? i.NombreTipoCuentaBancariaSocio : p.NombreTipoCuentaBancariaSocio;
            p.NumeroCuentaBancariaSocio = vacio(p.NumeroCuentaBancariaSocio) ? i.NumeroCuentaBancariaSocio : p.NumeroCuentaBancariaSocio;
            p.IdBancoSocio = (p.IdBancoSocio == null || p.IdBancoSocio.compareTo(0L) == 0) ? i.IdBancoSocio : p.IdBancoSocio;

            p.IdMedioDePago_Real = i.IdMedioDePago_Real;
            p.IdModalidadDePago_Real = i.IdModalidadDePago_Real;
            p.IdMoneda_Real = i.IdMoneda_Real;
            p.IdEstadoMoneda_Real = i.IdEstadoMoneda_Real;
            p.IdCuentaBancariaCorredora_Real = i.IdCuentaBancariaCorredora_Real;
            p.IdCuentaBancariaSocio_Real = i.IdCuentaBancariaSocio_Real;
            p.IdTarjetaCorredoraBancaria_Real = i.IdTarjetaCorredoraBancaria_Real;
            p.IdTarjetaSocioBancaria_Real = i.IdTarjetaSocioBancaria_Real;
            p.ExisteDescuadreEnMontos = i.ExisteDescuadreEnMontos;
            p.ExisteDiferenciaMoneda = i.ExisteDiferenciaMoneda;

            p.FechaDocumentoPagoCorredora = fechaDocumento(fe.FechaDocumentoPagoCorredora);
            p.FechaDocumentoPagoSocio = fechaDocumento(fe.FechaDocumentoPagoSocio);
            c.formasDePago.add(p);
        }
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.equalsIgnoreCase("");
    }

    /**
     * Payment document date. LEGACY DEFECT kept (not listed in README): the legacy compares the text with {@code == ""} (a reference compare), so an
     * empty text (not null) is not treated as empty and reaches the date parser.
     */
    private static Timestamp fechaDocumento(String texto) {
        if (texto == null || texto == "" || texto.equalsIgnoreCase("0001-01-01")) {
            return Fecha.toDateTime("0001-01-01 00:00:00");
        }
        return Fecha.toDateTime(texto.length() == 10 ? texto + " 00:00:00" : texto);
    }

    // ------------------------------------------------------------------ validations

    /** Day zero as the legacy built it (two formats, the second one with a misspelt pattern): the placeholder of an empty liquidation date. */
    private static boolean esFechaVacia(java.sql.Date fecha) throws ParseException {
        Date fechaVacia = new SimpleDateFormat("yyyy-MM-dd").parse("0001-01-01");
        Date fechaHoraVacia = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").parse("0001-01-01 00:00:00.000");
        return fecha == null || fecha.compareTo(fechaVacia) == 0 || fecha.compareTo(fechaHoraVacia) == 0;
    }

    private void validarMovimientos(Corrida c) throws Exception {
        for (IngresoEgreso_Proceso iep : c.movimientos) {
            final String ERR = MensajeSalida.MENSAJE_ERROR;
            final String ADV = MensajeSalida.MENSAJE_ADVERTENCIA;
            requisito(c, iep.IdTipoMovimiento == null || iep.IdTipoMovimiento.length() == 0, "SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA", ERR, iep);
            requisito(c, iep.IdConcepto == null || iep.IdConcepto.length() == 0, "SIN_ESPECIFICAR_CONCEPTO_TESORERIA", ERR, iep);
            requisito(c, iep.IdMoneda == null || iep.IdMoneda.length() == 0, "SIN_ESPECIFICAR_MONEDA_TESORERIA", ERR, iep);
            requisito(c, iep.Monto == null || iep.Monto.compareTo(BigDecimal.ZERO) == 0, "SIN_ESPECIFICAR_MONTO_TESORERIA", ERR, iep);
            requisito(c, esFechaVacia(iep.FechaLiquidacion), "SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA", ERR, iep);
            requisito(c, iep.IdPersona == null || iep.IdPersona == 0, "SIN_ESPECIFICAR_PERSONA_TESORERIA", ERR, iep);
            requisito(c, "CLI".equalsIgnoreCase(iep.IdTipoPersonaTitularPago) && (iep.IdPortafolioTitularPago == null || iep.IdPortafolioTitularPago == 0),
                    "SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA", ERR, iep);
            requisito(c, iep.EsBilateral == null || iep.EsBilateral.length() == 0, "SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA", ERR, iep);
            requisito(c, iep.GeneraComprobante == null || iep.GeneraComprobante.length() == 0, "SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA", ERR, iep);
            requisito(c, iep.AgrupaComprobante == null || iep.AgrupaComprobante.length() == 0, "SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA", ERR, iep);
            requisito(c, iep.TitularPago == null || iep.TitularPago.length() == 0, "SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA", ERR, iep);
            requisito(c, iep.IdTipoMovimiento_Real == null, "SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA", ERR, iep);
            requisito(c, iep.IdConcepto_Real == null, "SIN_DEFINIR_CONCEPTO_TESORERIA", ERR, iep);
            requisito(c, iep.IdMoneda_Real == null, "MONEDA_NO_EXISTE", ERR, iep);
            requisito(c, iep.IdEstadoMoneda_Real == null, "MONEDA_NO_ACTIVA", ERR, iep);
            requisito(c, iep.IdCaja_Real == null && iep.IdCaja != null && iep.IdCaja != 0, "CAJA_NO_EXISTE", ERR, iep);
            requisito(c, iep.IdMonedaTesoreria_Real == null, "MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA", ERR, iep);
            requisito(c, iep.Monto.compareTo(BigDecimal.ZERO) < 0, "MONTO_MENOR_IGUAL_A_CERO", ERR, iep);
            requisito(c, iep.IdPersona_Real == null || iep.IdPersona_Real == 0, "PERSONA_NO_EXISTE", ERR, iep);
            requisito(c, "INA".equalsIgnoreCase(iep.IdEstado_PersonaReal), "PERSONA_NO_ACTIVA", ERR, iep);
            requisito(c, iep.IdPersonaRelacionada_Real == null || iep.IdPersonaRelacionada_Real == 0, "PERSONA_RELACIONADA_NO_EXISTE_CIE", ERR, iep);
            requisito(c, "INA".equalsIgnoreCase(iep.IdEstado_RelacionadoReal), "PERSONA_RELACIONADA_NO_ACTIVA", ERR, iep);
            requisito(c, iep.SecuenciaAgrupacion != null && iep.SecuenciaAgrupacion.length() > 0 && iep.CountSec > 0, "SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA", ERR, iep);
            requisito(c, iep.IdTipoProducto != null && iep.IdTipoProducto.length() > 0 && (iep.IdTipoProducto_Real == null || iep.IdTipoProducto_Real.length() == 0),
                    "TIPO_PRODUCTO_NO_EXISTE", ERR, iep);
            requisito(c, iep.IdTipoOperacion != null && iep.IdTipoOperacion.length() > 0 && (iep.IdTipoOperacion_Real == null || iep.IdTipoOperacion_Real.length() == 0),
                    "TIPO_OPERACION_NO_EXISTE", ERR, iep);
            requisito(c, iep.IdCondicionLiquidacion != null && iep.IdCondicionLiquidacion.length() > 0
                    && (iep.IdCondicionLiquidacion_Real == null || iep.IdCondicionLiquidacion_Real.length() == 0), "CONDICION_DE_LIQUIDACION_NO_EXISTE", ERR, iep);
            requisito(c, "MOVING".equalsIgnoreCase(iep.IdTipoMovimiento) && "S".equalsIgnoreCase(iep.BloqueadoPorCumplimiento), "CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO", ERR, iep);
            requisito(c, "CLI".equalsIgnoreCase(iep.IdTipoPersona) && iep.EstadoPortafolio != null && !"ACT".equalsIgnoreCase(iep.EstadoPortafolio),
                    "PORTAFOLIO_BLOQUEADO", ADV, iep);
            requisito(c, "CLI".equalsIgnoreCase(iep.IdTipoPersonaTitularPago) && iep.EstadoPortafolioTitularPago != null && iep.EstadoPortafolio != null
                    && iep.IdPortafolio.compareTo(iep.IdPortafolioTitularPago) != 0 && !"ACT".equalsIgnoreCase(iep.EstadoPortafolioTitularPago),
                    "PORTAFOLIO_BLOQUEADO_TITULAR_PAGO", ADV, iep);
            requisito(c, iep.CodigoEjecutivoDigitador != null && !iep.CodigoEjecutivoDigitador.trim().isEmpty()
                    && (iep.IdEmpleadoDigitador == null || iep.IdEmpleadoDigitador == 0L), "CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE", ERR, iep);
        }
    }

    private void validarFormasDePago(Corrida c) throws Exception {
        for (FormaDePago_Proceso fpp : c.formasDePago) {
            final String ERR = MensajeSalida.MENSAJE_ERROR;
            requisito(c, fpp.IdMedioDePago_Real == null || fpp.IdMedioDePago_Real.length() == 0, "MEDIO_DE_PAGO_INVALIDO", ERR, fpp);
            // a cuenta de inversion is not a payment medium of treasury: it appears when the client operates with the investment account
            requisito(c, "CIN".equalsIgnoreCase(fpp.IdMedioDePago_Real), "MEDIO_DE_PAGO_INVALIDO", ERR, fpp);
            requisito(c, !"CIN".equalsIgnoreCase(fpp.IdMedioDePago) && (fpp.IdModalidadDePago_Real == null || fpp.IdModalidadDePago_Real.length() == 0),
                    "MODALIDAD_DE_PAGO_INVALIDO", ERR, fpp);
            requisito(c, fpp.IdMoneda_Real == null || fpp.IdMoneda_Real.length() == 0, "MONEDA_NO_EXISTE", ERR, fpp);
            requisito(c, "INA".equalsIgnoreCase(fpp.IdEstadoMoneda_Real), "MONEDA_NO_ACTIVA", ERR, fpp);
            requisito(c, fpp.Monto.compareTo(BigDecimal.ZERO) == 0 || fpp.Monto.compareTo(BigDecimal.ZERO) < 0, "MONTO_MENOR_IGUAL_A_CERO", ERR, fpp);
            requisito(c, fpp.IdCuentaBancariaSocio > 0 && fpp.IdCuentaBancariaSocio_Real == 0, "CUENTA_BANCARIA_SOCIO_INVALIDA", ERR, fpp);
            requisito(c, fpp.IdCuentaBancariaCorredora > 0 && fpp.IdCuentaBancariaCorredora_Real == 0, "CUENTA_BANCARIA_CORREDORA_INVALIDA", ERR, fpp);
            requisito(c, fpp.IdTarjetaBancariaCorredora > 0 && fpp.IdTarjetaCorredoraBancaria_Real == 0, "TARJETA_BANCARIA_CORREDORA_INVALIDA", ERR, fpp);
            requisito(c, fpp.IdTarjetaBancariaSocio > 0 && fpp.IdTarjetaSocioBancaria_Real == 0, "TARJETA_BANCARIA_SOCIO_INVALIDA", ERR, fpp);
            requisito(c, fpp.ExisteDiferenciaMoneda != null && fpp.ExisteDiferenciaMoneda > 0, "DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO", ERR, fpp);
            requisito(c, "TES".equalsIgnoreCase(c.in.p_IdModulo) && fpp.ExisteDescuadreEnMontos > 0, "MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO", ERR, fpp);
        }
    }

    /** Legacy {@code ProcesaRequisitoIngresoEgreso}: when the condition holds, the message goes out and the counter of its type grows. */
    private void requisito(Corrida c, boolean aplica, String codigo, String tipo, IngresoEgreso_Proceso iep) {
        if (!aplica) {
            iep.ok++;
            return;
        }
        Map<String, String> valores = valores("NumeroReferencia", iep.NumeroReferencia, "IdConcepto", iep.IdConcepto, "Monto", iep.Monto,
                "IdPersona", iep.IdPersona, "IdPersonaTitularPago", iep.IdPersonaTitularPago, "NombrePersona", iep.NombrePersona,
                "NombrePersonaTitularPago", iep.NombrePersonaTitularPago, "IdSecuencia", iep.SecuenciaAgrupacion, "IdMoneda", iep.IdMoneda,
                "IdTipoProducto", iep.IdTipoProducto, "IdTipoOperacion", iep.IdTipoOperacion, "IdCondicionDeLiquidacion", iep.IdCondicionLiquidacion,
                "NombreCliente", iep.NombrePersona, "DocumentoIdentidad", iep.DocumentoIdentidad, "CodigoPortafolio", iep.CodigoPortafolio,
                "DocumentoIdentidadTitularPago", iep.DocumentoIdentidadTitularPago, "CodigoPortafolioTitularPago", iep.CodigoPortafolioTitularPago,
                "CodigoEjecutivoDigitador", iep.CodigoEjecutivoDigitador);
        String texto = diccionario.mensaje(c.in.p_IdEmpresa, codigo, valores);
        switch (tipo) {
            case MensajeSalida.MENSAJE_ERROR -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia, MensajeSalida.MENSAJE_ERROR, codigo, texto));
                iep.errores++;
            }
            case MensajeSalida.MENSAJE_AUTORIZACION -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia, MensajeSalida.MENSAJE_AUTORIZACION, codigo, texto));
                iep.autorizaciones++;
            }
            case MensajeSalida.MENSAJE_ADVERTENCIA -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia, MensajeSalida.MENSAJE_ADVERTENCIA, codigo, texto));
                iep.advertencias++;
            }
            default -> iep.ok++;
        }
    }

    /** Legacy {@code ProcesaRequisitoFormaDePago}. */
    private void requisito(Corrida c, boolean aplica, String codigo, String tipo, FormaDePago_Proceso fpp) {
        if (!aplica) {
            fpp.ok++;
            return;
        }
        Map<String, String> valores = valores("IdMedioDePago", fpp.IdMedioDePago, "IdModalidadDePago", fpp.IdModalidadDePago, "IdMoneda", fpp.IdMoneda,
                "IdEstadoMoneda", fpp.IdEstadoMoneda_Real, "IdCuentaBanco", fpp.IdCuentaBancariaCorredora, "IdTarjetaBancaria", fpp.IdTarjetaBancariaCorredora,
                "IdCuentaBancaria", fpp.IdCuentaBancariaSocio, "IdTarjetaBancaria", fpp.IdTarjetaBancariaSocio);
        String texto = diccionario.mensaje(c.in.p_IdEmpresa, codigo, valores);
        switch (tipo) {
            case MensajeSalida.MENSAJE_ERROR -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago, MensajeSalida.MENSAJE_ERROR, codigo, texto));
                fpp.errores++;
            }
            case MensajeSalida.MENSAJE_AUTORIZACION -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago, MensajeSalida.MENSAJE_AUTORIZACION, codigo, texto));
                fpp.autorizaciones++;
            }
            case MensajeSalida.MENSAJE_ADVERTENCIA -> {
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago, MensajeSalida.MENSAJE_ADVERTENCIA, codigo, texto));
                fpp.advertencias++;
            }
            default -> fpp.ok++;
        }
    }

    /** Placeholder values of a message in the legacy {@code Diccionario.add(k, v)} way: a null value is not added, any other is its text. */
    private static Map<String, String> valores(Object... pares) {
        Map<String, String> valores = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            if (pares[i + 1] != null) {
                valores.put((String) pares[i], pares[i + 1].toString());
            }
        }
        return valores;
    }

    /** Removes the invalid movements and their payment forms from the process (legacy {@code limpiarIngresosEgresos}). */
    private static void limpiarIngresosEgresos(List<IngresoEgreso_Proceso> movimientos, List<FormaDePago_Proceso> formasDePago,
            List<IngresoEgreso_Proceso> movimientosConErrores) {
        for (IngresoEgreso_Proceso iep : movimientos) {
            if (iep.errores > 0) {
                movimientosConErrores.add(iep);
            }
        }
        for (IngresoEgreso_Proceso iep : movimientosConErrores) {
            movimientos.remove(iep);
            formasDePago.removeIf(fpp -> iep.NumeroReferencia.equals(fpp.NumeroReferencia));
        }
    }

    /** Removes the payment forms that failed and the movement each belongs to (legacy {@code limpiarFormasDePago}). */
    private static void limpiarFormasDePago(List<FormaDePago_Proceso> formasDePago, List<IngresoEgreso_Proceso> movimientos,
            List<FormaDePago_Proceso> formasDePagoConErrores) throws Exception {
        for (FormaDePago_Proceso fpp : formasDePago) {
            if (fpp.errores > 0) {
                formasDePagoConErrores.add(fpp);
            }
        }
        Map<Long, IngresoEgreso_Proceso> porReferencia = indexar(movimientos, m -> m.NumeroReferencia);
        for (FormaDePago_Proceso fpp : formasDePagoConErrores) {
            formasDePago.remove(fpp);
            movimientos.remove(porReferencia.get(fpp.NumeroReferencia));
        }
    }

    // ------------------------------------------------------------------ cash flow

    /** Total of the outgoing movements per (currency, liquidation date, portfolio) against the cash flow of the same key. */
    private void calcularFlujoDeCaja(Corrida c) {
        CrearIngresoEgreso_Entrada in = c.in;
        SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
        java.sql.Date fechaMinima = c.movimientos.get(0).FechaLiquidacion;
        java.sql.Date fechaMaxima = c.movimientos.get(0).FechaLiquidacion;
        // LEGACY DEFECT kept (not listed in README): the legacy HashSet never deduplicates (equals without hashCode): one entry per outgoing movement
        List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolios = new ArrayList<>();
        Map<FlujoCajaElem, BigDecimal> totalEgresos = new HashMap<>();
        Map<FlujoCajaElem, BigDecimal> totalFlujoCaja = new HashMap<>();

        for (IngresoEgreso_Proceso iep : c.movimientos) {
            if (iep.IdPortafolio.compareTo(0L) != 0 && iep.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")) {
                if (iep.FechaLiquidacion.before(fechaMinima)) {
                    fechaMinima = iep.FechaLiquidacion;
                }
                if (iep.FechaLiquidacion.after(fechaMaxima)) {
                    fechaMaxima = iep.FechaLiquidacion;
                }
                totalEgresos.merge(new FlujoCajaElem(iep.IdMoneda, Fecha.toStringDate(iep.FechaLiquidacion), iep.IdPortafolio), iep.Monto, BigDecimal::add);
                SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
                port.IdMoneda = iep.IdMoneda;
                port.IdPortafolio = iep.IdPortafolio;
                portafolios.add(port);
            }
        }

        if (portafolios.size() > 0) {
            entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
            entradaFlujoCaja.P_FECHADESDE = Fecha.toStringDate(fechaMinima);
            entradaFlujoCaja.P_FECHAHASTA = Fecha.toStringDate(fechaMaxima);
            // manual movements use the flow EGRESOS_TESORERIA (no overdraft line); the ones from the business use CUENTA_INVERSION (with it)
            entradaFlujoCaja.P_USO = "TES".equalsIgnoreCase(in.p_IdModulo) && "N".equalsIgnoreCase(in.p_EsIngresoAutomatico) ? "EGRESOS_TESORERIA" : "CUENTA_INVERSION";
            entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = portafolios;
            SaldoFlujoCaja.ServiceSalida salida = Colaborador.invocar(colaboradores, "SaldoFlujoCaja", entradaFlujoCaja, SaldoFlujoCaja.ServiceSalida.class);
            for (SaldoFlujoCaja.ObjFLUJOCAJA flj : salida.P_OUT_FLUJOCAJA) {
                totalFlujoCaja.merge(new FlujoCajaElem(flj.IdMoneda, flj.FechaSaldo, flj.IdPortafolio), flj.Saldo, BigDecimal::add);
            }
        }

        for (IngresoEgreso_Proceso iep : c.movimientos) {
            FlujoCajaElem llave = new FlujoCajaElem(iep.IdMoneda, Fecha.toStringDate(iep.FechaLiquidacion), iep.IdPortafolio);
            if (totalEgresos.get(llave) != null) {
                iep.TotalMontoPortafolio = totalEgresos.get(llave);
            }
            if (totalFlujoCaja.get(llave) != null) {
                iep.SaldoCajaPortafolio = totalFlujoCaja.get(llave);
            }
        }
    }

    // ------------------------------------------------------------------ insert

    private List<IngresoEgreso> insertarMovimientos(Corrida c) throws Exception {
        // ids of the new movements, one sequence value per movement
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < c.movimientos.size(); i++) {
            ids.add(secuencias.siguiente(SECUENCIA_INGRESO_EGRESO));
        }
        List<IngresoEgreso> finales = new ArrayList<>();
        for (IngresoEgreso_Proceso p : c.movimientos) {
            IngresoEgreso ie = new IngresoEgreso();
            copiar(p, ie);
            ie.FechaIngresoRegistro = ie.FechaIngresoRegistro == null ? p.FechaOperacion : ie.FechaIngresoRegistro;
            ie.IdIngresoEgreso = ids.get(c.movimientos.indexOf(p));
            p.IdIngresoEgreso = ie.IdIngresoEgreso; // used below for the output and the vouchers
            ie.IdUsuarioEmision = p.IdUsuario;
            ie.IdEstado = "S".equals(p.RequiereAutorizacion) ? "PDA" : "ING"; // created INGRESADO, or pending authorization
            finales.add(ie);
        }
        if ("N".equalsIgnoreCase(c.in.p_SimularOperacion) && finales.size() > 0) {
            Consultas.lote(jdbc, SQL_INSERT_INGRESO_EGRESO, Utils.List2ListHashGenericObject(finales));
        }
        return finales;
    }

    private void registrarLog(Corrida c, List<IngresoEgreso> finales) throws Exception {
        if (finales.isEmpty()) {
            return;
        }
        CrearIngresoEgreso_Entrada in = c.in;
        List<Long> idsInstrumento = finales.stream().map(ie -> ie.IdInstrumento).distinct().collect(Collectors.toList());
        Map<Long, ObjInstrumento> instrumentos = new HashMap<>();
        if (!idsInstrumento.isEmpty()) {
            for (ObjInstrumento i : jdbc.query(SQL_SELECT_INSTRUMENTO, Map.of("IdInstrumento", Consultas.enLista(idsInstrumento)), Filas.de(ObjInstrumento.class))) {
                if (instrumentos.containsKey(i.IdInstrumento)) {
                    throw new Exception("key field is not unique");
                }
                instrumentos.put(i.IdInstrumento, i);
            }
        }
        AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
        for (IngresoEgreso ie : finales) {
            ObjInstrumento instrumento = instrumentos.size() > 0 ? instrumentos.get(ie.IdInstrumento) : new ObjInstrumento();
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.NombreProceso = "Crear Ingreso Egreso Tesoreria";
            cabecera.IdCabeceraLog = ie.IdOperacion != null ? ie.IdOperacion : 0L;
            cabecera.IdTipoLog = "INGRESO";
            cabecera.IdModulo = in.p_IdModulo;
            cabecera.IdTipoRegistro = "INGRESO_TESORERIA";
            cabecera.IdUsuario = in.p_IdUsuario;
            cabecera.IdEmpresa = in.p_IdEmpresa;
            cabecera.FechaSistemaLog = c.fechaSistema != null ? c.fechaSistema : "";
            cabecera.IdUsuarioAtendido = in.p_IdUsuario;
            cabecera.IdPersona = ie.IdPersona != null ? ie.IdPersona : 0L;
            cabecera.IdPortafolio = ie.IdPortafolio != null ? ie.IdPortafolio : 0L;
            cabecera.IdTipoOperacion = ie.IdTipoOperacion != null ? ie.IdTipoOperacion : "";
            cabecera.Folio = ie.IdOperacion != null ? ie.IdOperacion : 0L;
            cabecera.Monto = ie.Monto != null ? ie.Monto : BigDecimal.ZERO;
            cabecera.IdMonedaOrigen = ie.IdMoneda != null ? ie.IdMoneda : "";
            cabecera.Nemo = instrumento != null && instrumento.Nemotecnico != null ? instrumento.Nemotecnico : "";
            entradaLog.p_Log.add(cabecera);
        }
        Colaborador.invocar(colaboradores, "AgregarLog", entradaLog, AgregarLog.OutAgregarLog.class);
    }

    private void insertarFormasDePago(Corrida c, List<IngresoEgreso> finales) throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < c.formasDePago.size(); i++) {
            ids.add(secuencias.siguiente(SECUENCIA_FORMA_DE_PAGO));
        }
        Map<Long, IngresoEgreso_Proceso> porReferencia = indexar(c.movimientos, m -> m.NumeroReferencia);
        List<IngresoEgresoFormaDePago> formasFinales = new ArrayList<>();
        for (FormaDePago_Proceso p : c.formasDePago) {
            IngresoEgresoFormaDePago fp = new IngresoEgresoFormaDePago();
            copiar(p, fp);
            fp.IdIngresoEgreso = porReferencia.get(p.NumeroReferencia).IdIngresoEgreso;
            fp.IdIngresoEgresoFormaDePago = ids.get(c.formasDePago.indexOf(p));
            p.IdIngresoEgresoFormaDePago = fp.IdIngresoEgresoFormaDePago;
            fp.IdEstado = "ING";
            formasFinales.add(fp);
        }
        if ("N".equalsIgnoreCase(c.in.p_SimularOperacion) && formasFinales.size() > 0 && finales.size() > 0) {
            Consultas.lote(jdbc, SQL_INSERT_FORMA_DE_PAGO, Utils.List2ListHashGenericObject(formasFinales));
        }
    }

    private void prepararSalida(Corrida c) throws Exception {
        for (IngresoEgreso_Proceso p : c.movimientos) {
            IngresoEgresoGenerado_Salida generado = new IngresoEgresoGenerado_Salida();
            copiar(p, generado);
            ResultadoProceso_Salida resultado = new ResultadoProceso_Salida();
            resultado.Campo = "IdIngresoEgreso";
            resultado.Id = p.IdIngresoEgreso;
            resultado.Servicio = "cl.bcs.tesoreria.CrearIngresoEgreso";
            resultado.Resultado = "ING";
            resultado.NumeroReferencia = p.NumeroReferencia;
            c.out.P_OUT_INGRESOEGRESOGENERADO.add(generado);
            c.out.P_OUT_RESULTADOPROCESO.add(resultado);

            String codigo = "";
            if (generado.IdTipoMovimiento.equalsIgnoreCase("MOVING")) {
                codigo = "UI_CREADO_MOVIMIENTO_INGRESO";
            } else if (generado.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")) {
                codigo = "UI_CREADO_MOVIMIENTO_EGRESO";
            }
            c.out.P_OUT_MENSAJES.add(new MensajeSalida(generado.NumeroReferencia, MensajeSalida.MENSAJE_OK, codigo,
                    diccionario.mensaje(c.in.p_IdEmpresa, codigo, valores("IdIngresoEgreso", generado.IdIngresoEgreso))));
        }
    }

    // ------------------------------------------------------------------ vouchers

    private void generarYLiquidarComprobantes(Corrida c) throws Exception {
        CrearIngresoEgreso_Entrada in = c.in;
        List<IngresoEgresoGenerar_Entrada> aGenerar = new ArrayList<>();
        List<Long> aLiquidar = new ArrayList<>();
        for (IngresoEgreso_Proceso p : c.movimientos) {
            if (p.GeneraComprobante.equalsIgnoreCase("S") || ("N".equals(in.p_EsIngresoUI) && "S".equals(p.GeneraComprobanteAutomaticamente))) {
                IngresoEgresoGenerar_Entrada generar = new IngresoEgresoGenerar_Entrada();
                copiar(p, generar);
                generar.TipoComprobante = ""; // PARING: partida ingreso / PAREGR: partida egreso / "": other
                aGenerar.add(generar);
                if (p.LiquidarComprobante.equalsIgnoreCase("S")) {
                    aLiquidar.add(p.IdIngresoEgreso);
                }
            }
        }
        String fechaMovimiento = fechaVacia(in.p_FechaMovimiento) ? c.fechaSistema : in.p_FechaMovimiento;

        if (aGenerar.size() > 0) {
            boolean requiereAutorizacion = "S".equals(c.movimientos.get(0).RequiereAutorizacion);
            CrearComprobanteService.CrearComprobante_Entrada entrada = new CrearComprobanteService.CrearComprobante_Entrada();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_IdModulo = in.p_IdModulo;
            entrada.p_SimularOperacion = requiereAutorizacion ? "S" : in.p_SimularOperacion;
            entrada.p_UsaReglasNegocio = "N";
            entrada.p_FechaMovimiento = fechaMovimiento;
            entrada.p_IdCaja = 0L;
            entrada.p_GenerarComprobanteDesdeMovimientos = requiereAutorizacion ? "N" : "S";
            entrada.p_GenerarComprobanteDesdeComprobantes = "N";
            entrada.p_EsIngresoAutomatico = "S";
            entrada.p_IngresoEgresoGenerar.addAll(aGenerar);

            CrearComprobanteService.CrearComprobante_Salida salida = crearComprobante.logica(entrada);
            c.out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            if (MensajeSalida.numeroErrores(salida.P_OUT_MENSAJES) > 0) {
                return;
            }
            for (CrearComprobanteService.ResultadoProceso_Salida r : salida.P_OUT_RESULTADOPROCESO) {
                ResultadoProceso_Salida resultado = new ResultadoProceso_Salida();
                copiar(r, resultado);
                c.out.P_OUT_RESULTADOPROCESO.add(resultado);
            }
        }

        if (aLiquidar.size() > 0) {
            List<LiquidarComprobanteService.ComprobanteLiquidar_Entrada> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTES,
                    Map.of("IdsIngresoEgreso", Consultas.enLista(aLiquidar)), Filas.de(LiquidarComprobanteService.ComprobanteLiquidar_Entrada.class));
            LiquidarComprobanteService.LiquidarComprobante_Entrada entrada = new LiquidarComprobanteService.LiquidarComprobante_Entrada();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_IdModulo = in.p_IdModulo;
            entrada.p_SimularOperacion = in.p_SimularOperacion;
            entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
            entrada.p_FechaMovimiento = fechaMovimiento;
            entrada.p_ComprobanteLiquidar = comprobantes;
            entrada.FolioCSC = in.p_IngresoEgresoGenerar.get(0).FolioCSC;

            LiquidarComprobanteService.LiquidarComprobante_Salida salida = liquidarComprobante.logica(entrada);
            c.out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            if (MensajeSalida.numeroErrores(salida.P_OUT_MENSAJES) > 0) {
                return;
            }
            for (LiquidarComprobanteService.ResultadoProceso_Salida r : salida.P_OUT_RESULTADOPROCESO) {
                ResultadoProceso_Salida resultado = new ResultadoProceso_Salida();
                copiar(r, resultado);
                c.out.P_OUT_RESULTADOPROCESO.add(resultado);
            }
        }
    }

    // ------------------------------------------------------------------ output

    /**
     * Legacy {@code LimpiarSalida}: with errors the OK messages are dropped; messages with the same text are collapsed to the first one.
     * The collapsed list comes out of a HashMap keyed by the text, so its ORDER is the hash order of the texts (pinned by the goldens, reproduced
     * on purpose with the same collector).
     */
    private static CrearIngresoEgreso_Salida limpiarSalida(CrearIngresoEgreso_Salida out) {
        if (out.P_OUT_ERRORESCONTADOR > 0) {
            out.P_OUT_MENSAJES.removeIf(m -> MensajeSalida.MENSAJE_OK.equals(m.tipo()));
        }
        if (out.P_OUT_MENSAJES.size() > 0) {
            out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream()
                    .collect(Collectors.toMap(MensajeSalida::descripcion, m -> m, (m1, m2) -> m1))
                    .values().stream().collect(Collectors.toCollection(ArrayList::new));
        }
        return out;
    }
}
