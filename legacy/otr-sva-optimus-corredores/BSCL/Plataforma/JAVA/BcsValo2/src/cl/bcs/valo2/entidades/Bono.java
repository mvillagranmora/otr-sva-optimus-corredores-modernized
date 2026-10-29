package cl.bcs.valo2.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Bono implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ajusteFinDeMes;
    private String calendario;
    private boolean callConfirmado;
    private int cantidadCupones;
    private String convencionDias;
    private boolean cuponeraVariable;
    private Date fechaDevengo;
    private Date fechaVencimiento;
    private String isin;
    private boolean mercadoLocal;
    private String monedaEmision;
    private String monedaPago;
    private String nemo;
    private int periodoPago;
    private BigDecimal tasaEmision;
    private String tenorInflacion;
    private int tipoCalculoInteresesCupon;
    private int tipoCalculoInteresesDevengados;
    private String tipoInflacion;
    private String tipoLibor;
    private boolean valorizarEnDiaHabil;

    public double calcularCantidadPagosAnuales() {
        return 12. / periodoPago;
    }

    public String getCalendario() {
        return calendario;
    }

    public int getCantidadCupones() {
        return cantidadCupones;
    }

    public String getConvencionDias() {
        return convencionDias;
    }

    public Date getFechaDevengo() {
        return fechaDevengo;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getIsin() {
        return isin;
    }

    public String getMonedaEmision() {
        return monedaEmision;
    }

    public String getMonedaPago() {
        return monedaPago;
    }

    public String getNemo() {
        return nemo;
    }

    public int getPeriodoPago() {
        return periodoPago;
    }

    public BigDecimal getTasaEmision() {
        return tasaEmision;
    }

    public String getTenorInflacion() {
        return tenorInflacion;
    }

    public int getTipoCalculoInteresesCupon() {
        return tipoCalculoInteresesCupon;
    }

    public int getTipoCalculoInteresesDevengados() {
        return tipoCalculoInteresesDevengados;
    }

    public String getTipoInflacion() {
        return tipoInflacion;
    }

    public String getTipoLibor() {
        return tipoLibor;
    }

    public boolean isAjusteFinDeMes() {
        return ajusteFinDeMes;
    }

    public boolean isCallConfirmado() {
        return callConfirmado;
    }

    public boolean isCuponeraVariable() {
        return cuponeraVariable;
    }

    public boolean isEnFechaVencimiento(
            final Date fecha) {
        return fecha.compareTo(fechaVencimiento) == 0;
    }

    public boolean isInactivo(
            final Date fecha) {
        return fecha.compareTo(fechaDevengo) < 0;
    }

    public boolean isMercadoLocal() {
        return mercadoLocal;
    }

    public boolean isValorizarEnDiaHabil() {
        return valorizarEnDiaHabil;
    }

    public boolean isVencido(
            final Date fecha) {
        return fecha.compareTo(fechaVencimiento) > 0;
    }

    public void setAjusteFinDeMes(
            final boolean ajusteFinDeMes) {
        this.ajusteFinDeMes = ajusteFinDeMes;
    }

    public void setCalendario(
            final String calendario) {
        this.calendario = calendario;
    }

    public void setCallConfirmado(
            final boolean callConfirmado) {
        this.callConfirmado = callConfirmado;
    }

    public void setCantidadCupones(
            final int cantidadCupones) {
        this.cantidadCupones = cantidadCupones;
    }

    public void setConvencionDias(
            final String convencionDias) {
        this.convencionDias = convencionDias;
    }

    public void setCuponeraVariable(
            final boolean cuponeraVariable) {
        this.cuponeraVariable = cuponeraVariable;
    }

    public void setFechaDevengo(
            final Date fechaDevengo) {
        this.fechaDevengo = fechaDevengo;
    }

    public void setFechaVencimiento(
            final Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public void setIsin(
            final String isin) {
        this.isin = isin;
    }

    public void setMercadoLocal(
            final boolean mercadoLocal) {
        this.mercadoLocal = mercadoLocal;
    }

    public void setMonedaEmision(
            final String monedaEmision) {
        this.monedaEmision = monedaEmision;
    }

    public void setMonedaPago(
            final String monedaPago) {
        this.monedaPago = monedaPago;
    }

    public void setNemo(
            final String nemo) {
        this.nemo = nemo;
    }

    public void setPeriodoPago(
            final int periodoPago) {
        this.periodoPago = periodoPago;
    }

    public void setTasaEmision(
            final BigDecimal tasaEmision) {
        this.tasaEmision = tasaEmision;
    }

    public void setTenorInflacion(
            final String tenorInflacion) {
        this.tenorInflacion = tenorInflacion;
    }

    public void setTipoCalculoInteresesCupon(
            final int tipoCalculoInteresesCupon) {
        this.tipoCalculoInteresesCupon = tipoCalculoInteresesCupon;
    }

    public void setTipoCalculoInteresesDevengados(
            final int tipoCalculoInteresesDevengados) {
        this.tipoCalculoInteresesDevengados = tipoCalculoInteresesDevengados;
    }

    public void setTipoInflacion(
            final String tipoInflacion) {
        this.tipoInflacion = tipoInflacion;
    }

    public void setTipoLibor(
            final String tipoLibor) {
        this.tipoLibor = tipoLibor;
    }

    public void setValorizarEnDiaHabil(
            final boolean valorizarEnDiaHabil) {
        this.valorizarEnDiaHabil = valorizarEnDiaHabil;
    }

    @Override
    public String toString() {
        return "Bono [ajusteFinDeMes=" + ajusteFinDeMes + ", calendario=" + calendario
                + ", callConfirmado=" + callConfirmado + ", cantidadCupones=" + cantidadCupones
                + ", convencionDias=" + convencionDias + ", cuponeraVariable=" + cuponeraVariable
                + ", fechaDevengo=" + fechaDevengo + ", fechaVencimiento=" + fechaVencimiento
                + ", isin=" + isin + ", mercadoLocal=" + mercadoLocal + ", monedaEmision="
                + monedaEmision + ", monedaPago=" + monedaPago + ", nemo=" + nemo + ", periodoPago="
                + periodoPago + ", tasaEmision=" + tasaEmision + ", tenorInflacion="
                + tenorInflacion + ", tipoCalculoInteresesCupon=" + tipoCalculoInteresesCupon
                + ", tipoCalculoInteresesDevengados=" + tipoCalculoInteresesDevengados
                + ", tipoInflacion=" + tipoInflacion + ", tipoLibor=" + tipoLibor
                + ", valorizarEnDiaHabil=" + valorizarEnDiaHabil + "]";
    }

}
