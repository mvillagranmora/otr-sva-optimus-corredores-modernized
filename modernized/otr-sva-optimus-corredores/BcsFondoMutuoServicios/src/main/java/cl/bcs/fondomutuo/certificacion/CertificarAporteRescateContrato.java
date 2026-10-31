package cl.bcs.fondomutuo.certificacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** JSON and row types of legacy {@code CertificarSolicitudAporteRescate} (names and order = legacy). */
public final class CertificarAporteRescateContrato {

    private CertificarAporteRescateContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaCertificacion;
        public String p_IdTipoCanalIngreso;
        public String p_NotificaOWP = "S";
        public String p_CertificacionAporteSwitchSimulado = "N";
        public List<InSolicitudes> p_Solicitudes = new ArrayList<>();
    }

    public static class InSolicitudes {
        public Long IdSolicitudAporteRescate;
        public String IdTipoCanalIngreso = "";
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoCertificacion> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class ResultadoCertificacion {
        public Long IdSolicitudAporteRescate;
        public Long IdSolicitudAporteRescateRelacionado = 0L;
        public String IdSerieFondo;
        public String Codigo;
        public String Estado;
        public String NombreEstado;
        public String DescripcionCodigo;
        public String DocumentoIdentidad = "";
        public String CodigoPortafolio = "";
    }

    /** Legacy {@code SolicitudesProcesoCertificacion}: the row of {@code SelectSolicitudes}. */
    public static class SolicitudProceso {
        public Long IdSolicitudAporteRescate;
        public Long IdSolicitudAporteRescateRelacionado;
        public java.sql.Date FechaCertificacion;
        public String TipoSolicitud;
        public String IdSerieFondo;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public String IdTipoMovimientoFondoMutuo;
        public String IdEmisor;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public String IdMonedaPago;
        public String IdMonedaFondo;
        public BigDecimal CostoMonedaLocal;
        public BigDecimal Monto;
        public BigDecimal ValorCuota;
        public BigDecimal NumeroCuotas;
        public BigDecimal ParidadFechaCertificacion;
        public java.sql.Date FechaLiquidacion;
        public java.sql.Date FechaAceptacion;
        public Long IdUsuario;
        public Long IdPersona;
        public String IdMedioDePago;
        public String MueveCta;
        public String CalculoPorMontoCuota;
        public BigDecimal MontoComision;
        public BigDecimal MontoPago;
        public BigDecimal MontoComisionLocal;
        public BigDecimal MontoPagoLocal;
        public BigDecimal ValorUFFechaInversion;
        public BigDecimal CostoUF;
        public BigDecimal RentabilidadReal;
        public String IndicadorMLT;
        public String Overnight;
        public String Glosa;
        public String IdSerieFondoDestino;
        public String IdEmisorDestino;
        public String OrigenOperacion;
        public String SolicitudBatch;
        public String ComisionInformada;
        public String PropositoPortafolio;
        public String IdRegimenTributario;
        public String IdRegimenTributarioAPV;
        public String TipoCarteraNormativa;
        public String IdRegimenTributarioDestino;
        public String TipoCarteraNormativaDestino;
        public BigDecimal Spread;
        public String EstadoSolicitud;
        public Long IdPreferenciaPagoPortafolio;
        public String IdMonedaSistema;
        public BigDecimal MontoMonedaSistema;
        public String EsPorMontoLiquido;
        public BigDecimal MontoLiquido;
        public BigDecimal MontoLiquidoLocal;
        public String IdEstadoComprobante;
        public String EstadoLiquidacion;
        public Long FolioCartaLiquidacion;
        public String EstadoRelacionado;
        public String EstadoLiquidacionRelacionado;
        public String IdConcepto;
        public Long IdEmpleadoEjecutivo;
        public String EstadoProceso = "PEN";
        public String Codigo = "";
        public String DescripcionCodigo = "";
        public String GeneraSpot;
        public Long IdIngresoEgreso;
        public Long IdOperacionSpot;
        public Long IdCartera;
        public String RelacionTraspaso = "N";
        public Long IdAgrupador;
        public Long IdPersonaEmisorDestino = 0L;
        public String IdTipoCanalIngreso;
        public String Switch;
        public String IdFamiliaFondo;
        public java.sql.Date FechaOriginalInversion;
        public java.sql.Date FechaValorCuota;
        public String IngresoAutomatico;
        public String RazonRetiro = "";
        public Long FolioContrato;
        public Long IdOmnibus = 0L;
        public String CertificaSinAsociado;
        public Long IdComprobanteTesoreria;
        public String TieneMontoRetencion = "N";
        public BigDecimal MontoRetencion = BigDecimal.ZERO;
        public BigDecimal MontoRetencionLocal = BigDecimal.ZERO;
        public String EsAPV;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public String GeneraOmnibus;
        public String TipoComision;
        public String TieneComisionNNRelacionada = "N";
        public String OperaSaldoNegativo = "N";
    }

    public static class Estado {
        public String IdEstado;
        public String Nombre;
    }

    public static class ComprobanteNoLiquidado {
        public Long IdSolicitudAporteRescate;
        public Long IdComprobante;
    }

    public static class Traspaso {
        public Long IdSolicitudAporteRescate;
        public Long IdPersona;
        public Long IdCuentaBancaria;
        public Long IdBanco;
        public String IdTipoCuentaBancaria;
        public String IdMoneda;
        public String NumeroCuentaBancaria;
        public String NombreTipoCuentaBancaria;
    }

    public static class Persona {
        public Long IdPersona = 0L;
        public Long IdCuentaBancaria = 0L;
        public Long IdBanco = 0L;
        public String IdTipoCuentaBancaria = "";
        public String IdMoneda = "";
        public String NumeroCuentaBancaria = "";
        public String NombreTipoCuentaBancaria = "";
    }

    public static class IndicadorPortafolio {
        public Long IdPortafolio;
        public String OperaLineaSobregiro;
    }

    public static class ParametroOperaCSN {
        public String ValorIndicador = "N";
        public Long IdEntidad = 0L;
        public String CodigoTipoIndicador = "";
    }

    /** Legacy {@code Cobro} (COM.Cobro): the IVA of the invoicing of the commission (F-G). */
    public static class Cobro {
        public String IdCobro;
        public BigDecimal ValorCobro;
    }

    /** Legacy {@code ReinversionSerieFondoDestino}: one destination of a rescate of reinversion (the defaults of the legacy constructor). */
    public static class ReinversionDestino {
        public Long IdReinversionSerieFondo;
        public Long IdSolicitudAporteRescate;
        public String IdSerieFondoDestino;
        public BigDecimal Porcentaje;
        public String IdRegimenTributario;
        public String IdFamiliaFondo;
        public String Switch = "N";
        public String IdMoneda;
    }

    /** Legacy {@code SolicitudOrigenReinversionSerieFondo}: the dollar flags of the origin and destination series of a rescate (the defaults of the legacy constructor). */
    public static class OrigenReinversion {
        public Long IdSolicitudAporteRescate = 0L;
        public String IdSerieFondoOrigen = "";
        public String FondoOrigenNacionalDolar = "N";
        public String FondoDestinoNacionalDolar = "N";
        public String IdSerieFondoDestino = "";
        public String FondoOrigenInternacionalDolar = "N";
        public String FondoDestinoInternacionalDolar = "N";
    }

    /** Legacy {@code SolicitudReinversion}: the sum of {@code MontoMonedaSistema} of the rows related to a rescate (the id column carries the id of the RESCATE); also the row of its UPDATE. */
    public static class SolicitudReinversion {
        public Long IdSolicitudAporteRescate;
        public BigDecimal MontoMonedaSistema;
    }

    /** One row of the log of certification: the id from the sequence port plus the legacy {@code InLog}. */
    public static class LogFila {
        public Long IdLogCertificacionAporteRescate;
        public Long IdSolicitudAporteRescate;
        public String Codigo;
        public String IdEstado;
        public String TipoSolicitud;
        public String IdTipoMovimientoFondoMutuo;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String FechaSistema;
        public String IdSerieFondo;
        public String IdEmisor;
        public BigDecimal Monto;
        public String IdMoneda;
        public BigDecimal MontoOriginal;
        public BigDecimal TipoCambio;
        public String IdMonedaOriginal;
        public BigDecimal ValorCuota;
        public BigDecimal NumeroCuotas;
        public Long IdCliente;
        public Long IdPortafolio;
        public String Simulacion;
    }
}
