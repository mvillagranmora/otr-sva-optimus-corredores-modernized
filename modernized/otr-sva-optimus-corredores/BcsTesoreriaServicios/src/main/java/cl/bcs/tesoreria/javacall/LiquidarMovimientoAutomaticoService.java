package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cl.bcs.tesoreria.javacall.CrearComprobanteService.CrearComprobante_Entrada;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.CrearComprobante_Salida;
import cl.bcs.tesoreria.javacall.CrearComprobanteService.IngresoEgresoGenerar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.LiquidarMovimientoAutomatico} (behaviours TES-A-810 .. TES-A-822): liquidates already created movements in one
 * step. It updates the payment forms of the movements with the stored procedure {@code ModificarIngresoEgreso} (outside the equivalence proof; called
 * through the {@code StoredProcedure} collaborator with the two work tables filled in the session), creates the vouchers
 * ({@link CrearComprobanteService}) and liquidates them ({@link LiquidarComprobanteService}).
 *
 * <p>Legacy defects reproduced on purpose: the voucher is created with {@code p_UsaReglasNegocio = "N"}, so an annulled movement is accepted
 * (TES-A-816); a payment form whose reference matches no movement is a NullPointerException (TES-A-819); the typed {@code logica} does not recount
 * the errors (only {@link #execute} does).
 */
@Service
public class LiquidarMovimientoAutomaticoService {

    private static final String PROCEDIMIENTO_MODIFICAR = "BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso";
    private static final String COMPROBANTE_NO_CUADRA = "MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO";

    private static final String SQL_SELECT_INGRESO = loadSql("sql/LiquidarMovimientoAutomatico_SelectIngresoEgreso.sql");
    private static final String SQL_SELECT_COMPROBANTES = loadSql("sql/CrearIngresoEgreso_SelectComprobantes.sql");
    private static final String SQL_CREATE_TEMP_FORMA_PAGO = loadSql("sql/LiquidarMovimientoAutomatico_CreateTempTableFormaPago.sql");
    private static final String SQL_CREATE_TEMP_INGRESO_EGRESO = loadSql("sql/LiquidarMovimientoAutomatico_CreateTempTableIngresoEgreso.sql");
    private static final String SQL_DROP_TEMP_FORMA_PAGO = loadSql("sql/LiquidarMovimientoAutomatico_DropTempTableFormaPago.sql");
    private static final String SQL_DROP_TEMP_INGRESO_EGRESO = loadSql("sql/LiquidarMovimientoAutomatico_DropTempTableIngresoEgreso.sql");
    private static final String SQL_INSERT_TEMP_FORMA_PAGO = loadSql("sql/LiquidarMovimientoAutomatico_InsertTempTableFormaPago.sql");
    private static final String SQL_INSERT_TEMP_INGRESO_EGRESO = loadSql("sql/LiquidarMovimientoAutomatico_InsertTempTableIngresoEgreso.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

        public static class LiquidarMovimientoAutomatico_Entrada extends BcsPojo {

            public Long        p_IdEmpresa;                   
            public Long        p_IdUsuario;                    
            public String     p_IdModulo;                 
            public String     p_SimularOperacion;      
            public String     p_UsaReglasNegocio;            
            public String     p_FechaMovimiento;
            public String     p_Canal;

            public List<IngresoEgresoGenerar_Entrada>     p_IngresoEgreso;
            public List<FormaDePago_Entrada>             p_FormaDePago;  

            public LiquidarMovimientoAutomatico_Entrada() {

                p_Canal             = "WEB";
                p_SimularOperacion  = "N";
                p_UsaReglasNegocio    = "S";
                p_IngresoEgreso        = new ArrayList<IngresoEgresoGenerar_Entrada>();
                p_FormaDePago         = new ArrayList<FormaDePago_Entrada>();
            }

        }    

        public static class LiquidarMovimientoAutomatico_Salida extends BcsPojo {

            public List<MensajeSalida>                P_OUT_MENSAJES;
            public Long                                P_OUT_ERRORESCONTADOR;
            public List<ResultadoProceso_Salida>    P_OUT_RESULTADOPROCESO;
            public List<AdvertenciasDetalle>        P_OUT_ADVERTENCIASDETALLE;
            public Long                                P_OUT_ADVERTENCIASCONTADOR;

            public LiquidarMovimientoAutomatico_Salida () {

                P_OUT_ERRORESCONTADOR        = 0L;
                P_OUT_ADVERTENCIASCONTADOR    = 0L;
                P_OUT_MENSAJES                 = new ArrayList<MensajeSalida>();
                P_OUT_RESULTADOPROCESO         = new ArrayList<ResultadoProceso_Salida>();
                P_OUT_ADVERTENCIASDETALLE     = new ArrayList<AdvertenciasDetalle>();

            };

        }

        public static class ResultadoProceso_Salida extends BcsPojo {

            public String     Campo;        
            public Long       Id;           
            public String     Servicio;
            public String    Resultado;

        }

        public static class IngresoEgreso_Entrada extends BcsPojo {

            public Long          NumeroReferencia;                  
            public String        IdTipoMovimiento;                   
            public String        IdTipoPersona;                   
               public Long          IdPersona;                       
               public Long          IdPortafolio;                       
               public String        IdMoneda;                           
               public BigDecimal     Monto;                              
               public String       FechaLiquidacion;                   
               public String        IdConcepto;                       
               public String        IdTipoPersonaTitularPago;          
               public Long          IdPersonaTitularPago;               
               public Long          IdPortafolioTitularPago;          
               public Long          IdCaja;                           
               public String        SecuenciaAgrupacion = "";         
               public Long          SecuenciaRelacionada = 0L;         
               public String        Glosa;                           
               public Long          IdEmpleadoDigitador;               
               public Long          IdSucursalEmpleadoDigitador;      
               public String       IdTipoProducto;                   
               public String        IdTipoOperacion;                   
               public String        IdTipoComprobante;               
               public Long          IdComprobante;                   
               public String        FechaComprobante;                   
               public String        FechaLiquidacionComprobante;                                      
               public Long         IdOperacion;                       
            public String        FechaOperacion;                   
            public String       IdCondicionLiquidacion;          
            public String        IdBolsa;                           
            public String        IdMonedaOperacion;               
            public BigDecimal     MontoOperacion    = BigDecimal.ZERO;                   
            public String        IdTipoReajuste;                   
            public String        FechaParidad;                       
            public BigDecimal     ValorParidad    = BigDecimal.ZERO;                   
            public Long         FolioCartera;                       
            public Long          IdInstrumento;                   
            public String        IdTipoInstrumento;               
            public String        IdClasificacionTipoInstrumento; 
            public String        Bilateral = "N";                
            public String        GeneraComprobante = "N";         
            public String        AgruparEnComprobante = "N";     
            public String        MueveCuentaInversion;              
            public String        TitularPago = "";                   

            public IngresoEgreso_Entrada()
            {
                this.Bilateral                           = "N";
                this.GeneraComprobante                = "N";
                this.AgruparEnComprobante             = "N";
                this.SecuenciaAgrupacion              = "";
                this.SecuenciaRelacionada             = 0L;
                this.IdTipoComprobante                   = "";
                this.IdComprobante                       = 0L;
                this.FechaComprobante                = "0001-01-01";
                this.FechaLiquidacionComprobante     = "0001-01-01";
            }    

        } 

        public static class FormaDePago_Entrada extends BcsPojo {

            public Long         NumeroReferencia;                
            public String        IdMedioDePago;                  
            public String        IdModalidadDePago;              
            public String        IdMoneda;                       
            public BigDecimal   Monto;                             
            public Long         IdCuentaBancariaCorredora;         
            public Long         IdTarjetaBancariaCorredora;     
            public Long         IdChequera;                     
            public Long         NumeroDocumentoCorredora;         
            public String        FechaDocumentoPagoCorredora;    
            public Long         IdCuentaBancariaSocio;             
            public Long         IdBancoSocio;                     
            public String        NombreCuentaBancariaSocio;        
            public String        NumeroCuentaBancariaSocio;        
            public String        IdTipoCuentaBancariaSocio;        
            public String        NombreTipoCuentaBancariaSocio;     
            public Long         IdTarjetaBancariaSocio;            
            public String        IdTipoTarjetaBancariaSocio;      
            public String        NombreTipoTarjetaBancariaSocio; 
            public String        NumeroTarjetaBancariaSocio;      
            public String        NumeroSerieSocio;                  
            public Long         NumeroDocumentoSocio;            
            public String        FechaDocumentoPagoSocio;    

        }



        public static class AdvertenciasDetalle extends BcsPojo {

            public Long NumeroReferencia;
            public String Codigo;
            public String RequiereAutorizacion;
            public String Descripcion;

        }

        public static class ErrorDetalle extends BcsPojo {

            public Long NumeroReferencia;
            public String Codigo;
            public String Descripcion;

        }



    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final CrearComprobanteService crearComprobante;
    private final LiquidarComprobanteService liquidarComprobante;
    private final Colaboradores colaboradores;

    public LiquidarMovimientoAutomaticoService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            CrearComprobanteService crearComprobante, LiquidarComprobanteService liquidarComprobante, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.crearComprobante = crearComprobante;
        this.liquidarComprobante = liquidarComprobante;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: recounts the errors and, for a simulation ("S"), rolls the work back before answering (TES-A-821). */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        LiquidarMovimientoAutomatico_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, LiquidarMovimientoAutomatico_Entrada.class);
        LiquidarMovimientoAutomatico_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        if (in.p_SimularOperacion.equalsIgnoreCase("S")) {
            // legacy con.rollback(): the work is undone but the call still answers; the platform then commits nothing
            jdbc.getJdbcTemplate().execute((ConnectionCallback<Void>) con -> {
                con.rollback();
                return null;
            });
        }
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public LiquidarMovimientoAutomatico_Salida logica(LiquidarMovimientoAutomatico_Entrada in) throws Exception {
        LiquidarMovimientoAutomatico_Salida out = new LiquidarMovimientoAutomatico_Salida();
        String fechaSistema = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");

        if (in.p_IngresoEgreso.size() == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_MOVIMIENTOS_DE_INGRESO_EGRESO",
                    diccionario.mensaje(in.p_IdEmpresa, "NO_SE_INGRESARON_MOVIMIENTOS_DE_INGRESO_EGRESO", null)));
            return out;
        }
        String fechaMovimiento = in.p_FechaMovimiento == null || in.p_FechaMovimiento.isEmpty() || in.p_FechaMovimiento.equalsIgnoreCase("0001-01-01")
                ? fechaSistema : in.p_FechaMovimiento;

        // movements to liquidate (all) and the ones whose payment forms change
        List<Long> aLiquidar = new ArrayList<>();
        List<Long> aActualizarFormas = new ArrayList<>();
        for (IngresoEgresoGenerar_Entrada ie : in.p_IngresoEgreso) {
            aLiquidar.add(ie.IdIngresoEgreso);
            if (in.p_FormaDePago.stream().anyMatch(f -> f.NumeroReferencia.equals(ie.NumeroReferencia))) {
                aActualizarFormas.add(ie.IdIngresoEgreso);
            }
        }
        List<IngresoEgreso_Entrada> movimientos = jdbc.query(SQL_SELECT_INGRESO, Map.of("IdsIngresosEgresos", Consultas.enLista(aActualizarFormas)),
                Filas.de(IngresoEgreso_Entrada.class));

        // the procedure receives the IdIngresoEgreso where the request carries the reference
        Map<Long, Object> porReferencia = Utils.List2Hash(in.p_IngresoEgreso, "NumeroReferencia");
        List<FormaDePago_Entrada> formas = new ArrayList<>();
        for (FormaDePago_Entrada forma : in.p_FormaDePago) {
            IngresoEgresoGenerar_Entrada movimiento = (IngresoEgresoGenerar_Entrada) porReferencia.get(forma.NumeroReferencia);
            // LEGACY DEFECT kept (see README TES-A-819): a forma de pago whose reference does not exist is a NullPointerException here
            forma.NumeroReferencia = movimiento.IdIngresoEgreso;
            formas.add(forma);
        }

        JsonObject modificacion = modificarFormasDePago(in, movimientos, formas);
        Long errores = contador(modificacion, "p_out_ErroresContador");
        if (errores > 0L) {
            out.P_OUT_ERRORESCONTADOR = errores;
            for (ErrorDetalle e : lista(modificacion, "p_out_ErroresDetalle", new TypeToken<List<ErrorDetalle>>() { })) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(e.NumeroReferencia, MensajeSalida.MENSAJE_ERROR, e.Codigo, e.Descripcion));
            }
            return out;
        }
        out.P_OUT_RESULTADOPROCESO.addAll(lista(modificacion, "p_out_ResultadoProceso", new TypeToken<List<ResultadoProceso_Salida>>() { }));
        out.P_OUT_ADVERTENCIASDETALLE.addAll(lista(modificacion, "p_out_AdvertenciasDetalle", new TypeToken<List<AdvertenciasDetalle>>() { }));
        out.P_OUT_ADVERTENCIASCONTADOR = contador(modificacion, "p_out_AdvertenciasContador");
        // from the web service a payment that does not match the movement is an error
        if (in.p_Canal.equalsIgnoreCase("WEB") && out.P_OUT_ADVERTENCIASDETALLE.stream().anyMatch(a -> a.Codigo.equals(COMPROBANTE_NO_CUADRA))) {
            return out;
        }

        CrearComprobante_Salida comprobantes = crearComprobante(in, fechaMovimiento);
        out.P_OUT_MENSAJES.addAll(comprobantes.P_OUT_MENSAJES);
        if (MensajeSalida.numeroErrores(comprobantes.P_OUT_MENSAJES) > 0) {
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
            return out;
        }
        for (CrearComprobanteService.ResultadoProceso_Salida r : comprobantes.P_OUT_RESULTADOPROCESO) {
            ResultadoProceso_Salida resultado = new ResultadoProceso_Salida();
            copiar(r, resultado);
            out.P_OUT_RESULTADOPROCESO.add(resultado);
        }

        LiquidarComprobante_Salida liquidacion = liquidarComprobantes(in, aLiquidar, fechaMovimiento);
        out.P_OUT_MENSAJES.addAll(liquidacion.P_OUT_MENSAJES);
        if (MensajeSalida.numeroErrores(liquidacion.P_OUT_MENSAJES) > 0) {
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
            return out;
        }
        for (LiquidarComprobanteService.ResultadoProceso_Salida r : liquidacion.P_OUT_RESULTADOPROCESO) {
            ResultadoProceso_Salida resultado = new ResultadoProceso_Salida();
            copiar(r, resultado);
            out.P_OUT_RESULTADOPROCESO.add(resultado);
        }
        return out;
    }

    /**
     * Calls the stored procedure with the two session work tables filled in (they are dropped afterwards, also on failure). The collaborator
     * receives the procedure name and its input as the legacy recorded it: the scalars and the two lists (the lists travel in the work tables).
     */
    private JsonObject modificarFormasDePago(LiquidarMovimientoAutomatico_Entrada in, List<IngresoEgreso_Entrada> movimientos, List<FormaDePago_Entrada> formas) {
        Map<String, Object> entrada = new HashMap<>();
        entrada.put("p_IdEmpresa", in.p_IdEmpresa);
        entrada.put("p_IdUsuario", in.p_IdUsuario);
        entrada.put("p_IdModulo", in.p_IdModulo);
        entrada.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
        entrada.put("p_SimularOperacion", in.p_SimularOperacion);
        entrada.put("p_FormaDePago", formas);
        entrada.put("p_IngresoEgresoModificar", movimientos);
        JsonObject peticion = new JsonObject();
        peticion.addProperty("procedimiento", PROCEDIMIENTO_MODIFICAR);
        peticion.add("entrada", JsonLegacy.ENTRADA_SALIDA.toJsonTree(new java.util.TreeMap<>(entrada)));
        try {
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP_FORMA_PAGO);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP_INGRESO_EGRESO);
            Consultas.lote(jdbc, SQL_INSERT_TEMP_FORMA_PAGO, listaDeMapas(formas));
            Consultas.lote(jdbc, SQL_INSERT_TEMP_INGRESO_EGRESO, listaDeMapas(movimientos));
            return colaboradores.invocar("StoredProcedure", peticion);
        } finally {
            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP_FORMA_PAGO);
            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP_INGRESO_EGRESO);
        }
    }

    private static List<Map<String, Object>> listaDeMapas(List<?> filas) {
        try {
            return new ArrayList<>(Utils.List2ListHashGenericObject(filas));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Scalar output of the procedure; an output the procedure did not return is null (and unboxing it fails, as the legacy did). */
    private static Long contador(JsonObject salida, String nombre) {
        JsonElement valor = salida.get(nombre);
        return valor == null || valor.isJsonNull() ? null : valor.getAsLong();
    }

    /** Table output of the procedure; an output the procedure did not return is null (and using it fails, as the legacy did). */
    private static <T> List<T> lista(JsonObject salida, String nombre, TypeToken<List<T>> tipo) {
        JsonElement valor = salida.get(nombre);
        return valor == null || valor.isJsonNull() ? null : JsonLegacy.PLATAFORMA.fromJson((JsonArray) valor, tipo.getType());
    }

    private CrearComprobante_Salida crearComprobante(LiquidarMovimientoAutomatico_Entrada in, String fechaMovimiento) throws Exception {
        CrearComprobante_Entrada entrada = new CrearComprobante_Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_SimularOperacion = in.p_SimularOperacion;
        // LEGACY DEFECT kept (see README TES-A-816): the voucher is created without business rules, so an annulled movement is accepted
        entrada.p_UsaReglasNegocio = "N";
        entrada.p_FechaMovimiento = fechaMovimiento;
        entrada.p_IdCaja = 0L;
        entrada.p_GenerarComprobanteDesdeMovimientos = "S";
        entrada.p_GenerarComprobanteDesdeComprobantes = "N";
        entrada.p_EsIngresoAutomatico = "S";
        entrada.p_IngresoEgresoGenerar.addAll(in.p_IngresoEgreso);
        return crearComprobante.logica(entrada);
    }

    private LiquidarComprobante_Salida liquidarComprobantes(LiquidarMovimientoAutomatico_Entrada in, List<Long> aLiquidar, String fechaMovimiento) throws Exception {
        List<ComprobanteLiquidar_Entrada> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTES, Map.of("IdsIngresoEgreso", Consultas.enLista(aLiquidar)),
                Filas.de(ComprobanteLiquidar_Entrada.class));
        LiquidarComprobante_Entrada entrada = new LiquidarComprobante_Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_SimularOperacion = in.p_SimularOperacion;
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_FechaMovimiento = fechaMovimiento;
        entrada.p_ComprobanteLiquidar = comprobantes;
        return liquidarComprobante.logica(entrada);
    }

    @SuppressWarnings("deprecation")
    private static void copiar(Object origen, Object destino) throws Exception {
        Utils.copyPojo(origen, destino); // same-name, same-type public fields (legacy copyPojo contract)
    }
}
