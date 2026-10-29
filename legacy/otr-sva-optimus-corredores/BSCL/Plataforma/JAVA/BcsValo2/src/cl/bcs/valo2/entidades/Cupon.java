package cl.bcs.valo2.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Cupon implements Serializable {
    private static final long serialVersionUID = 1L;

    public static Cupon duplicar(
            final Cupon cupon) {
        final Cupon duplicado = new Cupon();
        duplicado.setAmortizacion(cupon.amortizacion);
        duplicado.setFecha(cupon.fecha);
        duplicado.setFechaAnterior(cupon.fechaAnterior);
        duplicado.setIntereses(cupon.intereses);
        duplicado.setNemo(cupon.nemo);
        duplicado.setNumero(cupon.numero);
        duplicado.setSaldoPorAmortizar(cupon.saldoPorAmortizar);
        duplicado.setPeriodoEnAnos(cupon.periodoEnAnos);
        duplicado.setVencimientoEnAnos(cupon.vencimientoEnAnos);
        return duplicado;
    }

    private BigDecimal amortizacion;
    private boolean call;
    private Date fecha;
    private Date fechaAnterior;
    private BigDecimal intereses;
    private String nemo;
    private int numero;
    private double periodoEnAnos;
    private int periodoEnDias;
    private BigDecimal saldoPorAmortizar;
    private double vencimientoEnAnos;
    private int vencimientoEnDias;

    public double calcularAmortizacionUnitaria() {
        return amortizacion.doubleValue() / 100;
    }

    public double calcularFlujoUnitario() {
        return calcularAmortizacionUnitaria() + calcularInteresesUnitarios();
    }

    public double calcularInteresesUnitarios() {
        return intereses!=null? intereses.doubleValue() / 100 : 0;
    }

    public double calcularSaldoCapitalUnitario() {
        return calcularSaldoPorAmortizarUnitario() + calcularAmortizacionUnitaria();
    }

    public double calcularSaldoPorAmortizarUnitario() {
        return saldoPorAmortizar.doubleValue() / 100;
    }

    public BigDecimal getAmortizacion() {
        return amortizacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public Date getFechaAnterior() {
        return fechaAnterior;
    }

    public BigDecimal getIntereses() {
        return intereses;
    }

    public String getNemo() {
        return nemo;
    }

    public int getNumero() {
        return numero;
    }

    public double getPeriodoEnAnos() {
        return periodoEnAnos;
    }

    public int getPeriodoEnDias() {
        return periodoEnDias;
    }

    public BigDecimal getSaldoPorAmortizar() {
        return saldoPorAmortizar;
    }

    public double getVencimientoEnAnos() {
        return vencimientoEnAnos;
    }

    public int getVencimientoEnDias() {
        return vencimientoEnDias;
    }

    public boolean isCall() {
        return call;
    }

    public void setAmortizacion(
            final BigDecimal amortizacion) {
        this.amortizacion = amortizacion;
    }

    public void setCall(
            final boolean call) {
        this.call = call;
    }

    public void setFecha(
            final Date fecha) {
        this.fecha = fecha;
    }

    public void setFechaAnterior(
            final Date fechaAnterior) {
        this.fechaAnterior = fechaAnterior;

    }

    public void setIntereses(
            final BigDecimal intereses) {
        this.intereses = intereses;
    }

    public void setNemo(
            final String nemo) {
        this.nemo = nemo;
    }

    public void setNumero(
            final int numero) {
        this.numero = numero;
    }

    public void setPeriodoEnAnos(
            final double periodoEnAnos) {
        this.periodoEnAnos = periodoEnAnos;
    }

    public void setPeriodoEnDias(
            final int periodoEnDias) {
        this.periodoEnDias = periodoEnDias;
    }

    public void setSaldoPorAmortizar(
            final BigDecimal saldoPorAmortizar) {
        this.saldoPorAmortizar = saldoPorAmortizar;
    }

    public void setVencimientoEnAnos(
            final double vencimientoEnAnos) {
        this.vencimientoEnAnos = vencimientoEnAnos;
    }

    public void setVencimientoEnDias(
            final int vencimientoEnDias) {
        this.vencimientoEnDias = vencimientoEnDias;
    }

    @Override
    public String toString() {
        return "Cupon [amortizacion=" + amortizacion + ", call=" + call + ", fecha=" + fecha
                + ", fechaAnterior=" + fechaAnterior + ", intereses=" + intereses + ", nemo=" + nemo
                + ", numero=" + numero + ", periodoEnAnos=" + periodoEnAnos + ", periodoEnDias="
                + periodoEnDias + ", saldoPorAmortizar=" + saldoPorAmortizar
                + ", vencimientoEnAnos=" + vencimientoEnAnos + ", vencimientoEnDias="
                + vencimientoEnDias + "]";
    }
}
