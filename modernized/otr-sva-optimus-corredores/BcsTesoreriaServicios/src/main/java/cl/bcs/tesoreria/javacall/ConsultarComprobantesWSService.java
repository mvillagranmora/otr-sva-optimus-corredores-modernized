package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.ConsultarComprobantesWS}, behaviours TES-A-606..624: web-service query of comprobantes by liquidation date range
 * with their detail lines and payment forms. Read only. The range limit is the configuration parameter
 * {@code TESORERIA/RANGO_CONSULTAR_FECHA_LIQUIDACION_WS} (30 days when it is missing or not a number).
 */
@Service
public class ConsultarComprobantesWSService {

    private static final String SQL_COMPROBANTES = loadSql("sql/ConsultarComprobantesWS_SelectComprobantes.sql");
    private static final String SQL_FORMA_DE_PAGO = loadSql("sql/ConsultarComprobantesWS_SelectFormaDePago.sql");
    private static final String SQL_DETALLE = loadSql("sql/ConsultarComprobantesWS_SelectDetalle.sql");
    private static final int RANGO_POR_DEFECTO = 30;

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaConsultarComprobantes extends BcsPojo {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "TES";
        public String TipoMovimiento;
        public Date FechaInLiquidacion;
        public Date FechaTerLiquidacion;
        public String TipoSocioNegocio;
        public Long IdCaja;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public String IdMoneda;
        public String IdConcepto;
        public String IdEstado;
        public String IdMedioDePago;
        public String BancoCorredora;
        public String Alias;
        public String BancoCliente;
        public String CuentaCorredora;
    }

    public static class SalidaConsultarComprobantes extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ComprobanteSalida> P_OUT_COMPROBANTES;
    }

    public static class ComprobanteSalida extends BcsPojo {
        
        public Long IdComprobante; 
        public Long IdEmpresa; 
        public String NombreEmpresa; 
        public Long IdUsuario; 
        public String TipoComprobante; 
        public String IdTipoMovimiento; 
        public String NombreTipoMovimiento; 
        public String IdConcepto; 
        public String NombreConcepto; 
        public Long IdCaja; 
        public String NombreCaja; 
        public String CodigoCaja; 
        public String IdMoneda; 
        public String NombreMoneda; 
        public BigDecimal Monto;
        public BigDecimal MontoEgreso;
        public BigDecimal MontoIngreso;
        public BigDecimal MontoConSigno;
        public Date FechaLiquidacion;
        public Timestamp FechaIngresoRegistro;
        public Date FechaMovimiento;
        public String IdTipoPersona;
        public String NombreTipoSocioDeNegocio;
        public Long IdPersona;
        public String NombrePersona;
        public String IdTipoDocumentoIdentidad;
        public String DocumentoIdentidad;
        public String TitularPago;
        public String IdTipoPersonaTitularPago;
        public Long IdPersonaTitularPago;
        public String NombrePersonaTitularPago;
        public String MueveCuentaInversion;
        public Long IdPortafolioTitularPago;
        public String NombrePortafolioTitularPago;
        public String SecuenciaAgrupacion;
        public String EsBilateral;
        public String ComprobanteNeteado;
        public String SeparadoPorConcepto;
        public Long IdUsuarioBloqueo;
        public Timestamp FechaHoraBloqueo;
        public Long IdCarga;
        public String IdEstado;
        public String NombreEstado;
        public String IdTipoEvento;
        public String Autorizacion;
        public String SolicitudAutorizacion;
        public String MotivoAutorizacion;
        public String IdEstadoAutorizacion;
        public String NombreEstadoAutorizacion;
        public String IdMedioDePago;
        public String NombreMedioDePago;
        public String IdModalidadDePago;
        public String NombreModalidadDePago;
        public Long IdCuentaBancoCorredora;
        public Long IdChequera;
        public String NombreBancoCorredora;
        public String NumeroCuentaBancariaCorredora;
        public String NombreTipoCuentaBancariaCorredora;
        public String NumeroSerieSocio; 
        public Long NumeroDocumentoSocio;
        public Timestamp FechaDocumentoPagoSocio; 
        public Long IdBancoSocio;
        public String NombreBancoSocio;
        public String NumeroCuentaBancariaSocio;
        public String NombreTipoCuentaBancariaSocio;
        public String AliasBanco;
        public String CuentaDCVOrigen;
        public String CuentaDCVDestino;
        public String NombreUsuarioLiquidacion;
        public Long IdUsuarioAnulacion;
        public Timestamp FechaHoraAnulacion;
        public Long IdComprobanteOrigen;
        public Long FolioComprobanteDTE;
        public Date FechaComprobanteOrigen;
        public Long IdPortafolio;
        public String NombrePortafolio;
        public String IdTipoCanalIngreso;
        public String Alias;
        public String Nemotecnico;
        public Long IdComprobantePartidaEgreso;
        public String NombreEstadoNomina;
        public String CondicionDeLiquidacion;
        public String Glosa;
        
        public List<DetalleSalida> DetalleComprobante;
         public List<FormaDePagoSalida> FormaDePagoComprobante;

    }
    
    public static class DetalleSalida extends BcsPojo {
        public Long IdComprobanteDetalle; 
        public Integer NroLinea; 
        public String IdTipoMovimiento; 
        public String TipoMovimiento; 
        public String NombreTipoMovimiento; 
        public String IdConcepto; 
        public String NombreConcepto; 
        public String IdMoneda;
        public BigDecimal Monto; 
        public String IdTipoComprobanteOrigen; 
        public Long IdComprobanteOrigen; 
        public Timestamp FechaComprobanteOrigen;
        public String IdTipoProducto; 
        public String NombreTipoProducto; 
        public String IdTipoOperacion; 
        public String NombreTipoOperacion; 
        public Long IdInstrumento; 
        public String Nemotecnico; 
        public String IdTipoInstrumento;
        public String IdClasificacionTipoInstrumento;  
        public Long IdComprobante;

    }
    
    public static class FormaDePagoSalida extends BcsPojo {
        public Long IdComprobanteFormaDePago;
        public Long IdComprobante;
        public String IdMedioDePago;
        public String ClaveDCV;
        public String NombreMedioDePago;
        public String TipoMedioDePago;
        public String IdModalidadDePago;
        public String NombreModalidadDePago;
        public Long IdCuentaBancariaCorredora;
        public String IdTipoCuentaBancariaCorredora;
        public String NombreTipoCuentaBancariaCorredora;
        public Long IdBancoCorredora;
        public String NombreBancoCorredora;
        public String NombreCuentaBancariaCorredora;
        public String NumeroCuentaBancariaCorredora;
        public Long IdTarjetaBancariaCorredora;
        public Long IdChequera;
        public String NumeroSerieCorredora;
        public Long NumeroDocumentoCorredora;
        public Timestamp FechaDocumentoPagoCorredora;
        public Long IdCuentaBancariaSocio;
        public String IdTipoCuentaBancariaSocio;
        public String NombreTipoCuentaBancariaSocio;
        public String NombreCuentaBancariaSocio;
        public Long IdBancoSocio;
        public String NombreBancoSocio;
        public String NumeroCuentaBancariaSocio;
        public Long IdTarjetaBancariaSocio;
        public String NumeroSerieSocio;
        public Long NumeroDocumentoSocio;
        public Timestamp FechaDocumentoPagoSocio;
        public String IdMoneda;
        public BigDecimal Monto;
        public String IdEstado;
        public String NombreEstado;
        public Long IdUsuarioGiro;
        public String NombreUsuarioGiro;
        public Long IdComprobantePartida;
        public Long IdUsuarioCertificacion;
        public String NombreUsuarioCertificacion;
        public Long IdUsuarioLiquidacion;
        public String NombreUsuarioLiquidacion;

    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;

    public ConsultarComprobantesWSService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    /** Legacy {@code call}. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaConsultarComprobantes in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaConsultarComprobantes.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SalidaConsultarComprobantes logica(EntradaConsultarComprobantes entrada) throws Exception {
        SalidaConsultarComprobantes salida = new SalidaConsultarComprobantes();
        if (entrada.FechaInLiquidacion == null || entrada.FechaTerLiquidacion == null) {
            // LEGACY DEFECT kept (see README TES-A-612): a one-sided range is reported as an invalid date, not as a missing one
            error(entrada, salida, "FECHA_LIQUIDACION_NO_VALIDA");
            return salida;
        }
        if (!rangoValido(entrada)) {
            error(entrada, salida, "RANGO_FECHA_LIQUIDACION_NO_VALIDO");
            return salida;
        }
        List<ComprobanteSalida> comprobantes = jdbc.query(SQL_COMPROBANTES, filtros(entrada), Filas.de(ComprobanteSalida.class));
        if (comprobantes.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensaje(entrada, MensajeSalida.MENSAJE_INFORMACION, "NO_EXISTEN_COMPROBANTES"));
            return salida;
        }
        List<Long> ids = comprobantes.stream().map(c -> c.IdComprobante).collect(Collectors.toCollection(ArrayList::new));
        Map<String, Object> porIds = Map.of("IdsComprobantes", ids);
        List<DetalleSalida> detalles = jdbc.query(SQL_DETALLE, porIds, Filas.de(DetalleSalida.class));
        List<FormaDePagoSalida> formasDePago = jdbc.query(SQL_FORMA_DE_PAGO, porIds, Filas.de(FormaDePagoSalida.class));
        for (ComprobanteSalida c : comprobantes) {
            c.DetalleComprobante = detalles.stream().filter(d -> d.IdComprobante.equals(c.IdComprobante)).collect(Collectors.toList());
            c.FormaDePagoComprobante = formasDePago.stream().filter(f -> f.IdComprobante.equals(c.IdComprobante)).collect(Collectors.toList());
        }
        salida.P_OUT_MENSAJES.add(mensaje(entrada, MensajeSalida.MENSAJE_INFORMACION, "COMPROBANTES_WS_EXISTOSO"));
        salida.P_OUT_COMPROBANTES = comprobantes;
        return salida;
    }

    /** The range, counting both end days, must not exceed the configured limit (legacy {@code ValidadorRangoDeFechas}). */
    private boolean rangoValido(EntradaConsultarComprobantes entrada) {
        int limite;
        try {
            limite = Integer.parseInt(configuracion.parametro(entrada.p_IdEmpresa, "TESORERIA", "RANGO_CONSULTAR_FECHA_LIQUIDACION_WS"));
        } catch (RuntimeException e) {
            limite = RANGO_POR_DEFECTO; // missing or not a number
        }
        LocalDate inicio = entrada.FechaInLiquidacion.toLocalDate();
        LocalDate termino = entrada.FechaTerLiquidacion.toLocalDate();
        // LEGACY DEFECT kept (see README TES-A-613): both end days count, so the limit is 30 days inclusive
        long dias = inicio.isAfter(termino) ? 0 : ChronoUnit.DAYS.between(inicio, termino) + 1;
        return dias != 0 && dias <= limite;
    }

    private static Map<String, Object> filtros(EntradaConsultarComprobantes e) {
        Map<String, Object> p = new HashMap<>();
        p.put("FechaInLiquidacion", e.FechaInLiquidacion);
        p.put("FechaTerLiquidacion", e.FechaTerLiquidacion);
        p.put("CodigoPortafolio", e.CodigoPortafolio);
        p.put("IdEstado", e.IdEstado);
        p.put("IdTipoMovimiento", e.TipoMovimiento);
        p.put("IdTipoSocioNegocio", e.TipoSocioNegocio);
        p.put("IdCaja", e.IdCaja);
        p.put("DocumentoIdentidad", e.DocumentoIdentidad);
        p.put("IdMoneda", e.IdMoneda);
        p.put("IdConcepto", e.IdConcepto);
        p.put("IdMedioDePago", e.IdMedioDePago);
        p.put("NombreBancoCorredora", e.BancoCorredora);
        p.put("AliasBanco", e.Alias);
        p.put("NombreBancoSocio", e.BancoCliente);
        p.put("NumeroCuentaBancariaCorredora", e.CuentaCorredora);
        return p;
    }

    private void error(EntradaConsultarComprobantes entrada, SalidaConsultarComprobantes salida, String codigo) {
        salida.P_OUT_ERRORESCONTADOR++;
        salida.P_OUT_MENSAJES.add(mensaje(entrada, MensajeSalida.MENSAJE_ERROR, codigo));
    }

    private MensajeSalida mensaje(EntradaConsultarComprobantes entrada, String tipo, String codigo) {
        return new MensajeSalida(0L, tipo, codigo, diccionario.mensaje(entrada.p_IdEmpresa, codigo, Map.of()));
    }
}
