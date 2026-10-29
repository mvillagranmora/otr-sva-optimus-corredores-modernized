package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model for an aporte/rescate (contribution/redemption) request.
 * Migrated from: cl.bcs.fondomutuo.pojo.SolicitudAporteRescate
 */
public class SolicitudAporteRescate {

    private Long idInterno;
    private Long idSolicitudAporteRescate;
    private String idTipoCanalIngreso = "";
    private Long folioContrato;
    private Long idCliente;
    private Long idPortafolio;
    private String tipoSolicitud;
    private String idSerieFondo;
    private String idTipoMovimientoFondoMutuo;
    private Long idDividendo;
    private String idSerieFondoDestino = "";
    private String idEmisorDestino = "";
    private Long idClienteDestino = 0L;
    private Long idPortafolioDestino = 0L;
    private String idSerieFondoOrigen = "";
    private String idEmisorOrigen = "";
    private BigDecimal montoOrigen = BigDecimal.ZERO;
    private String indicadorMLT;
    private String overnight;
    private Long idSolicitudAporteRescateRelacionado;
    private String idTipoCartera;
    private String calculoPorMontoCuota;
    private LocalDateTime fechaIngresoRegistro;
    private LocalDate fechaEmision;
    private LocalDate fechaAceptacion;
    private LocalDate fechaValorCuota;
    private LocalDate fechaCertificacion;
    private LocalDate fechaLiquidacion;
    private LocalDate fechaAnulacion;
    private LocalDate fechaModificacion;
    private LocalDate fechaOriginalInversion;
    private String esPorMontoLiquido = ConstantesFondoMutuo.NO;
    private BigDecimal monto;
    private BigDecimal numeroCuotas;
    private BigDecimal porcentaje = BigDecimal.ZERO;
    private BigDecimal valorCuota;
    private BigDecimal costoUF;
    private BigDecimal costoMonedaLocal;
    private BigDecimal paridadFechaCertificacion = BigDecimal.ZERO;
    private BigDecimal valorUFFechaInversion;
    private BigDecimal paridadFechaInversion;
    private String idMoneda;
    private String idMonedaPago = "";
    private BigDecimal montoMonedaPago = BigDecimal.ZERO;
    private String validaSpot;
    private String generaSpot;
    private BigDecimal montoComision;
    private String comisionInformada = ConstantesFondoMutuo.NO;
    private BigDecimal montoComisionMonedaSistema = BigDecimal.ZERO;
    private BigDecimal montoComisionLocal = BigDecimal.ZERO;
    private BigDecimal montoLiquido = BigDecimal.ZERO;
    private BigDecimal montoLiquidoLocal = BigDecimal.ZERO;
    private BigDecimal montoPago;
    private BigDecimal montoPagoLocal = BigDecimal.ZERO;
    private BigDecimal montoInversion = BigDecimal.ZERO;
    private BigDecimal numeroCuotasInversion = BigDecimal.ZERO;
    private BigDecimal valorCuotaInversion = BigDecimal.ZERO;
    private BigDecimal costoMonedaLocalInversion = BigDecimal.ZERO;
    private BigDecimal spread = BigDecimal.ZERO;
    private String estado;
    private String estadoLiquidacion = ConstantesFondoMutuo.ESTADO_INGRESADO;
    private String mueveCta = ConstantesFondoMutuo.SI;
    private Long folioCartaLiquidacion = 0L;
    private String idMedioDePago = "";
    private String glosa;
    private Long idEmpresa;
    private Long idUsuario;
    private String idModulo;
    private String origenOperacion = ConstantesFondoMutuo.ORIGEN_MANUAL;
    private String solicitudBatch = ConstantesFondoMutuo.NO;
    private String idRegimenTributario = "";
    private String tipoCarteraNormativa = "";
    private String tributacion = "";
    private String idRegimenTributarioDestino = "";
    private String tipoCarteraNormativaDestino = "";
    private String administracionLiquidez = ConstantesFondoMutuo.NO;
    private String visado = ConstantesFondoMutuo.NO;
    private Long idPreferenciaPagoPortafolio = 0L;
    private Long idAutorizadoOrigen = 0L;
    private Long idAutorizadoDestino = 0L;
    private String idMonedaSistema = "";
    private BigDecimal montoMonedaSistema = BigDecimal.ZERO;
    private String switchFlag = ConstantesFondoMutuo.NO;
    private String estadoAnulacion = "";
    private Long folioOmnibus = 0L;
    private Long folioAgfOmnibus = 0L;
    private Long idEmpleadoEjecutivo = 0L;
    private Long idEmpleadoEjecutivoDestino = 0L;
    private Long idPersona;
    private Long idInstrumento;
    private String idTipoInstrumento;
    private String estadoProceso;
    private Long idIngresoEgreso = 0L;
    private Long idOperacionSpot = 0L;
    private Long idCartera = 0L;
    private Long idAgrupador = 0L;
    private Long idAgrupadorInterno = 1L;
    private String relacionTraspaso = ConstantesFondoMutuo.NO;
    private Long idPersonaEmisorDestino = 0L;
    private BigDecimal rentabilidadReal = BigDecimal.ZERO;
    private Long idMatrizAgrupador = 0L;
    private String idMotivoRetiro = "";
    private String ingresoAutomatico = ConstantesFondoMutuo.NO;
    private String forzarSpot = ConstantesFondoMutuo.NO;
    private String razonRetiro = "";
    private String tipoContratoOperacion = "";
    private String folioContratoOperacion = "";
    private Long idPersonaTraspaso = 0L;
    private BigDecimal valorPivoteLocalConversionLocal = BigDecimal.ZERO;
    private BigDecimal valorPivoteSistemaConversionSistema = BigDecimal.ZERO;
    private Long secuenciaRelacionada = 0L;
    private String idConcepto = "";
    private Long idOrigenRecaCliente = 0L;
    private Long folioCartera = 0L;
    private BigDecimal comision = BigDecimal.ZERO;
    private Long idClienteAGF = 0L;
    private Long idPortafolioAGF = 0L;
    private String tipoComision = "";
    private String simulaCambioPortafolioEjecutadoOk = ConstantesFondoMutuo.NO;
    private String ingresoDesde = "";

    // Generated getters / setters omitted for brevity — use Lombok if needed
    public Long getIdInterno() { return idInterno; }
    public void setIdInterno(Long idInterno) { this.idInterno = idInterno; }
    public Long getIdSolicitudAporteRescate() { return idSolicitudAporteRescate; }
    public void setIdSolicitudAporteRescate(Long v) { this.idSolicitudAporteRescate = v; }
    public String getIdTipoCanalIngreso() { return idTipoCanalIngreso; }
    public void setIdTipoCanalIngreso(String v) { this.idTipoCanalIngreso = v; }
    public Long getFolioContrato() { return folioContrato; }
    public void setFolioContrato(Long v) { this.folioContrato = v; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long v) { this.idCliente = v; }
    public Long getIdPortafolio() { return idPortafolio; }
    public void setIdPortafolio(Long v) { this.idPortafolio = v; }
    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String v) { this.tipoSolicitud = v; }
    public String getIdSerieFondo() { return idSerieFondo; }
    public void setIdSerieFondo(String v) { this.idSerieFondo = v; }
    public String getIdTipoMovimientoFondoMutuo() { return idTipoMovimientoFondoMutuo; }
    public void setIdTipoMovimientoFondoMutuo(String v) { this.idTipoMovimientoFondoMutuo = v; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal v) { this.monto = v; }
    public BigDecimal getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(BigDecimal v) { this.numeroCuotas = v; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal v) { this.porcentaje = v; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public void setValorCuota(BigDecimal v) { this.valorCuota = v; }
    public String getCalculoPorMontoCuota() { return calculoPorMontoCuota; }
    public void setCalculoPorMontoCuota(String v) { this.calculoPorMontoCuota = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long v) { this.idEmpresa = v; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long v) { this.idUsuario = v; }
    public String getIdModulo() { return idModulo; }
    public void setIdModulo(String v) { this.idModulo = v; }
    public String getIdMoneda() { return idMoneda; }
    public void setIdMoneda(String v) { this.idMoneda = v; }
    public String getIdMonedaSistema() { return idMonedaSistema; }
    public void setIdMonedaSistema(String v) { this.idMonedaSistema = v; }
    public BigDecimal getMontoMonedaSistema() { return montoMonedaSistema; }
    public void setMontoMonedaSistema(BigDecimal v) { this.montoMonedaSistema = v; }
    public BigDecimal getMontoComision() { return montoComision; }
    public void setMontoComision(BigDecimal v) { this.montoComision = v; }
    public BigDecimal getMontoLiquido() { return montoLiquido; }
    public void setMontoLiquido(BigDecimal v) { this.montoLiquido = v; }
    public String getIdRegimenTributario() { return idRegimenTributario; }
    public void setIdRegimenTributario(String v) { this.idRegimenTributario = v; }
    public Long getIdInstrumento() { return idInstrumento; }
    public void setIdInstrumento(Long v) { this.idInstrumento = v; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate v) { this.fechaEmision = v; }
    public LocalDate getFechaValorCuota() { return fechaValorCuota; }
    public void setFechaValorCuota(LocalDate v) { this.fechaValorCuota = v; }
    public LocalDate getFechaCertificacion() { return fechaCertificacion; }
    public void setFechaCertificacion(LocalDate v) { this.fechaCertificacion = v; }
    public LocalDate getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(LocalDate v) { this.fechaLiquidacion = v; }
    public LocalDate getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDate v) { this.fechaAnulacion = v; }
    public String getTipoCarteraNormativa() { return tipoCarteraNormativa; }
    public void setTipoCarteraNormativa(String v) { this.tipoCarteraNormativa = v; }
    public String getTributacion() { return tributacion; }
    public void setTributacion(String v) { this.tributacion = v; }
    public BigDecimal getSpread() { return spread; }
    public void setSpread(BigDecimal v) { this.spread = v; }
    public BigDecimal getCostoUF() { return costoUF; }
    public void setCostoUF(BigDecimal v) { this.costoUF = v; }
    public BigDecimal getCostoMonedaLocal() { return costoMonedaLocal; }
    public void setCostoMonedaLocal(BigDecimal v) { this.costoMonedaLocal = v; }
    public String getOrigenOperacion() { return origenOperacion; }
    public void setOrigenOperacion(String v) { this.origenOperacion = v; }
    public String getSolicitudBatch() { return solicitudBatch; }
    public void setSolicitudBatch(String v) { this.solicitudBatch = v; }
    public String getIdConcepto() { return idConcepto; }
    public void setIdConcepto(String v) { this.idConcepto = v; }
    public String getIdSerieFondoDestino() { return idSerieFondoDestino; }
    public void setIdSerieFondoDestino(String v) { this.idSerieFondoDestino = v; }
    public Long getIdPortafolioDestino() { return idPortafolioDestino; }
    public void setIdPortafolioDestino(Long v) { this.idPortafolioDestino = v; }
    public String getAdministracionLiquidez() { return administracionLiquidez; }
    public void setAdministracionLiquidez(String v) { this.administracionLiquidez = v; }
    public Long getFolioOmnibus() { return folioOmnibus; }
    public void setFolioOmnibus(Long v) { this.folioOmnibus = v; }
    public String getEstadoProceso() { return estadoProceso; }
    public void setEstadoProceso(String v) { this.estadoProceso = v; }
}
