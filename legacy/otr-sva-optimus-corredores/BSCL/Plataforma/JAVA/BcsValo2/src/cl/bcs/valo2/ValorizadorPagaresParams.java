package cl.bcs.valo2;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValorizadorPagaresParams implements Serializable {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final long serialVersionUID = 1L;

    // No se usa en Bonos
    private int diasAno;
    // No se usa en Bonos
    private int diasVencimiento;
    // Fecha de emisión del bono
    private Date fechaCaptacion;
    // Fecha de vencimiento del bono
    private Date fechaVencimiento;
    // idem
    private long idRequerimiento;
    // idem
    private String moneda;
    // idem
    private String monedaPago;
    // idem
    private String nemo;
    // idem
    private BigDecimal tasa;
    // dato fijo
    private int tipoInteres;
    // monto
    private BigDecimal valorCaptacion;
    // no aplica 
    private BigDecimal valorCaptacionPago;
    // Resultado
    private BigDecimal valorRescate;
    // Resultado
    private BigDecimal valorRescatePago;
    private int codigoRetorno;
    private String mensaje;

    private BigDecimal duration;

    public int getCodigoRetorno() {
        return codigoRetorno;
    }

    public int getDiasAno() {
        return diasAno;
    }

    public int getDiasVencimiento() {
        return diasVencimiento;
    }

    public Date getFechaCaptacion() {
        return fechaCaptacion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
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

    public String getNemo() {
        return nemo;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public int getTipoInteres() {
        return tipoInteres;
    }

    public BigDecimal getValorCaptacion() {
        return valorCaptacion;
    }

    public BigDecimal getValorCaptacionPago() {
        return valorCaptacionPago;
    }

    public BigDecimal getValorRescate() {
        return valorRescate;
    }

    public BigDecimal getValorRescatePago() {
        return valorRescatePago;
    }
    
    public BigDecimal getDuration() {
        return duration;
    }
    
    public void setCodigoRetorno(
            final int codigoRetorno) {
        this.codigoRetorno = codigoRetorno;
    }

    public void setDiasAno(
            final int diasAno) {
        this.diasAno = diasAno;
    }

    public void setDiasVencimiento(
            final int diasVencimiento) {
        this.diasVencimiento = diasVencimiento;
    }

    public void setFechaCaptacion(
            final Date fechaCaptacion) {
        this.fechaCaptacion = fechaCaptacion;
    }

    public void setFechaVencimiento(
            final Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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

    public void setNemo(
            final String nemo) {
        this.nemo = nemo;
    }

    public void setTasa(
            final BigDecimal tasa) {
        this.tasa = tasa;
    }

    public void setTipoInteres(
            final int tipoInteres) {
        this.tipoInteres = tipoInteres;
    }

    public void setValorCaptacion(
            final BigDecimal valorCaptacion) {
        this.valorCaptacion = valorCaptacion;
    }

    public void setValorCaptacionPago(
            final BigDecimal valorCaptacionPago) {
        this.valorCaptacionPago = valorCaptacionPago;
    }

    public void setValorRescate(
            final BigDecimal valorRescate) {
        this.valorRescate = valorRescate;
    }

    public void setValorRescatePago(
            final BigDecimal valorRescatePago) {
        this.valorRescatePago = valorRescatePago;
    }
    
    public void setDuration(
            final BigDecimal duration) {
        this.duration = duration;
    }

    public String toPrintableString() {
        String str = "\n";
        str += "\nNemo: " + nemo;
        str += "\nFecha Captacion: " + DATE_FORMAT.format(fechaCaptacion);
        str += "\nFecha Vencimiento: " + DATE_FORMAT.format(fechaVencimiento);
        str += "\nDias Ano: " + diasAno;
        str += "\nDias Vencimiento: " + diasVencimiento;
        str += "\nValor Captacion: " + valorCaptacion;
        str += "\nValor Rescate: " + valorRescate;
        str += "\nValor Captacion Pago: " + valorCaptacionPago;
        str += "\nValor Rescate Pago: " + valorRescatePago;
        str += "\nTasa: " + tasa;
        str += "\nTipo Interes: " + tipoInteres;
        str += "\nMoneda: " + moneda;
        str += "\nMoneda Pago: " + monedaPago;
        str += "\n";
        return str;
    }

    @Override
    public String toString() {
        return "ValorizadorPagaresParams [diasAno=" + diasAno + ", diasVencimiento="
                + diasVencimiento + ", fechaCaptacion=" + fechaCaptacion + ", fechaVencimiento="
                + fechaVencimiento + ", idRequerimiento=" + idRequerimiento + ", moneda=" + moneda
                + ", monedaPago=" + monedaPago + ", nemo=" + nemo + ", tasa=" + tasa
                + ", tipoInteres=" + tipoInteres + ", valorCaptacion=" + valorCaptacion
                + ", valorCaptacionPago=" + valorCaptacionPago + ", valorRescate=" + valorRescate
                + ", valorRescatePago=" + valorRescatePago + ", codigoRetorno=" + codigoRetorno
                + ", mensaje=" + mensaje + ", duration=" + duration + "]";
    }
}
