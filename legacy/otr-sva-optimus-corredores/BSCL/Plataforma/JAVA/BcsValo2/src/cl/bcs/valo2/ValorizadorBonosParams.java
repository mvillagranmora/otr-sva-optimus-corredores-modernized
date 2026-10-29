package cl.bcs.valo2;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.entidades.Cupon;

public class ValorizadorBonosParams implements Serializable {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final long serialVersionUID = 1L;

    private BigDecimal convexidad;
    private List<Cupon> cupones;
    private BigDecimal duration;
    private Date fechaValorizacion;
    private long idRequerimiento;
    private String moneda;
    private String monedaPago;
    private BigDecimal montoCapital;
    private BigDecimal montoIntereses;
    private BigDecimal montoReajuste;
    private BigDecimal montoTotal;
    private BigDecimal montoTotalPago;
    private String nemo;
    private BigDecimal precio;
    private double proporcionCapital;
    private double proporcionIntereses;
    private double proporcionReajuste;
    private double proporcionTotal;
    private BigDecimal tasa;
    private BigDecimal tasa360;
    private BigDecimal valorNominal;
    private int codigoRetorno;
    private String mensaje;

    public double calcularPrecioUnitario() {
        return precio.doubleValue() / 100;
    }

    public double calcularTasaUnitaria() {
        return tasa.doubleValue() / 100;
    }

    public int getCodigoRetorno() {
        return codigoRetorno;
    }

    public BigDecimal getConvexidad() {
        return convexidad;
    }

    public List<Cupon> getCupones() {
        return cupones;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public Date getFechaValorizacion() {
        return fechaValorizacion;
    }

    public long getIdRequerimiento() {
        return idRequerimiento;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getMonedaPago() {
        return monedaPago;
    }

    public BigDecimal getMontoCapital() {
        return montoCapital;
    }

    public BigDecimal getMontoIntereses() {
        return montoIntereses;
    }

    public BigDecimal getMontoReajuste() {
        return montoReajuste;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public BigDecimal getMontoTotalPago() {
        return montoTotalPago;
    }

    public String getNemo() {
        return nemo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public double getProporcionCapital() {
        return proporcionCapital;
    }

    public double getProporcionIntereses() {
        return proporcionIntereses;
    }

    public double getProporcionReajuste() {
        return proporcionReajuste;
    }

    public double getProporcionTotal() {
        return proporcionTotal;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public BigDecimal getTasa360() {
        return tasa360;
    }

    public BigDecimal getValorNominal() {
        return valorNominal;
    }

    public void setCodigoRetorno(
            final int codigoRetorno) {
        this.codigoRetorno = codigoRetorno;
    }

    public void setConvexidad(
            final BigDecimal convexidad) {
        this.convexidad = convexidad;
    }

    public void setCupones(
            final List<Cupon> cupones) {
        this.cupones = cupones;
    }

    public void setDuration(
            final BigDecimal duration) {
        this.duration = duration;
    }

    public void setFechaValorizacion(
            final Date fechaValorizacion) {
        this.fechaValorizacion = fechaValorizacion;
    }

    public void setIdRequerimiento(
            final long idRequerimiento) {
        this.idRequerimiento = idRequerimiento;
    }

    public void setMensaje(
            final String mensaje) {
        this.mensaje = mensaje;
    }

    public void setMoneda(
            final String moneda) {
        this.moneda = moneda;
    }

    public void setMonedaPago(
            final String monedaPago) {
        this.monedaPago = monedaPago;
    }

    public void setMontoCapital(
            final BigDecimal montoCapital) {
        this.montoCapital = montoCapital;
    }

    public void setMontoIntereses(
            final BigDecimal montoIntereses) {
        this.montoIntereses = montoIntereses;
    }

    public void setMontoReajuste(
            final BigDecimal montoReajuste) {
        this.montoReajuste = montoReajuste;

    }

    public void setMontoTotal(
            final BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public void setMontoTotalPago(
            final BigDecimal montoTotalPago) {
        this.montoTotalPago = montoTotalPago;
    }

    public void setNemo(
            final String nemo) {
        this.nemo = nemo;
    }

    public void setPrecio(
            final BigDecimal precio) {
        this.precio = precio;
    }

    public void setProporcionCapital(
            final double proporcionCapital) {
        this.proporcionCapital = proporcionCapital;
    }

    public void setProporcionIntereses(
            final double proporcionIntereses) {
        this.proporcionIntereses = proporcionIntereses;
    }

    public void setProporcionReajuste(
            final double proporcionReajuste) {
        this.proporcionReajuste = proporcionReajuste;

    }

    public void setProporcionTotal(
            final double proporcionTotal) {
        this.proporcionTotal = proporcionTotal;
    }

    public void setTasa(
            final BigDecimal tasa) {
        this.tasa = tasa;
    }

    public void setTasa360(
            final BigDecimal tasa360) {
        this.tasa360 = tasa360;
    }

    public void setValorNominal(
            final BigDecimal valorNominal) {
        this.valorNominal = valorNominal;
    }

    public String toPrintableString() {
        String str = "\n";
        str += "\nNemo: " + nemo;
        str += "\nFecha Valorizacion: " + DATE_FORMAT.format(fechaValorizacion);
        str += "\nValor Nominal: " + valorNominal;
        str += "\nPrecio: " + precio;
        str += "\nTasa: " + tasa;
        str += "\nTasa (Base 360): " + tasa360;
        str += "\nDuration: " + duration;
        str += "\nConvexidad: " + convexidad;
        str += "\nMoneda: " + moneda;
        str += "\nMonto Capital: " + montoCapital;
        str += "\nMonto Reajuste: " + montoReajuste;
        str += "\nMonto Intereses: " + montoIntereses;
        str += "\nMonto Total: " + montoTotal;
        str += "\nProporcion Capital: " + proporcionCapital;
        str += "\nProporcion Reajuste: " + proporcionReajuste;
        str += "\nProporcion Intereses: " + proporcionIntereses;
        str += "\nProporcion Total: " + proporcionTotal;
        str += "\nMoneda Pago: " + monedaPago;
        str += "\nMonto Total Pago: " + montoTotalPago;
        str += "\n";
        return str;
    }

    @Override
    public String toString() {
        return "ValorizadorBonosParams [convexidad=" + convexidad + ", cupones=" + cupones
                + ", duration=" + duration + ", fechaValorizacion=" + fechaValorizacion
                + ", idRequerimiento=" + idRequerimiento + ", moneda=" + moneda + ", monedaPago="
                + monedaPago + ", montoCapital=" + montoCapital + ", montoIntereses="
                + montoIntereses + ", montoReajuste=" + montoReajuste + ", montoTotal=" + montoTotal
                + ", montoTotalPago=" + montoTotalPago + ", nemo=" + nemo + ", precio=" + precio
                + ", proporcionCapital=" + proporcionCapital + ", proporcionIntereses="
                + proporcionIntereses + ", proporcionReajuste=" + proporcionReajuste
                + ", proporcionTotal=" + proporcionTotal + ", tasa=" + tasa + ", tasa360=" + tasa360
                + ", valorNominal=" + valorNominal + ", codigoRetorno=" + codigoRetorno
                + ", mensaje=" + mensaje + "]";
    }

}
