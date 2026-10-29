package cl.bcs.normativa.registros;

import java.io.Serializable;
import java.util.Date;

import cl.bcs.normativa.data.Registro;

/**
 * Registro DJ1891.
 *
 * @author fpino@email.com
 *
 */
public class RegistroDJ1891 implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Cantidad de acciones.
     */
    private final long cantidadAcciones;

    /**
     * Fecha del documento.
     */
    private final Date fechaDocumento;

    /**
     * Monto.
     */
   // private final long monto;

    private double montoCompras;
    private double montoVentas;
    private double valorAdquisicion;
    
    
    /**
     * Nemot&eacute;cnico.
     */
    private final String nemotecnico;

    /**
     * N&uacute;mero de documento.
     */
    private final long numeroDocumento;

    /**
     * RUT del emisor.
     */
    private final String rutEmisor;

    /**
     * RUT del operante.
     */
    private final String rutOperante;

    /**
     * Tipo de documento.
     */
    private final String tipoDocumento;

    /**
     * Tipo de operaci&oacute;n.
     */
    private final String tipoOperacion;

    /**
     * Tipo de transacci&oacute;n.
     */
    private final String tipoTransaccion;

    public RegistroDJ1891(final Registro registro) {
        this.tipoDocumento = (String) registro.get("TipoDocumento");
        this.numeroDocumento = (Long) registro.get("NumeroDocumento");
        this.fechaDocumento = (Date) registro.get("FechaDocumento");
        this.rutEmisor = (String) registro.get("RutEmisor");
        this.nemotecnico = (String) registro.get("Nemotecnico");
        this.rutOperante = (String) registro.get("RutCliente");
        this.cantidadAcciones = (Long) registro.get("Cantidad");
        this.tipoOperacion = (String) registro.get("IdTipoOperacion");
        //this.monto = (Long) registro.get("Monto");
    	this.montoCompras = (double) ((Number) registro.get("MontoCompras")).doubleValue();
      	this.montoVentas = (double) ((Number) registro.get("MontoVentas")).doubleValue();
      	this.valorAdquisicion = (double) ((Number) registro.get("ValorAdquisicion")).doubleValue();
        this.tipoTransaccion = (String) registro.get("TipoTransaccion");
    }

    /**
     * Obtiene la cantidad de acciones.
     *
     * @return La cantidad de acciones.
     */
    public long getCantidadAcciones() {
        return cantidadAcciones;
    }

    /**
     * Obtiene la fecha del documento.
     *
     * @return La fecha del documento.
     */
    public Date getFechaDocumento() {
        return fechaDocumento;
    }

    /*
    public long getMonto() {
        return monto;
    }

 
    public long getMontoCompra() {
        if (tipoOperacion.startsWith("CO")) {
            return monto;
        } else {
            return 0;
        }
    }


    public long getMontoVenta() {
        if (tipoOperacion.startsWith("VE")) {
            return monto;
        } else {
            return 0;
        }
    }
*/
    
    public double getMontoCompras() {
		return montoCompras;
	}


	public void setMontoCompras(double montoCompras) {
		this.montoCompras = montoCompras;
	}


	public double getMontoVentas() {
		return montoVentas;
	}


	public void setMontoVentas(double montoVentas) {
		this.montoVentas = montoVentas;
	}
	
	public double getValorAdquisicion() {
		return valorAdquisicion;
	}


	public void setValorAdquisicion(double valorAdquisicion) {
		this.valorAdquisicion = valorAdquisicion;
	}

    
    /**
     * Obtiene el nemot&eacute;cnico.
     *
     * @return El nemot&eacute;cnico.
     */
    public String getNemotecnico() {
        return nemotecnico;
    }

    /**
     * Obtiene el n&uacute;mero de documento.
     *
     * @return El n&uacute;mero de documento.
     */
    public long getNumeroDocumento() {
        return numeroDocumento;
    }

    /**
     * Obtiene el RUT del emisor.
     *
     * @return El RUT del emisor.
     */
    public String getRutEmisor() {
        return rutEmisor;
    }

    /**
     * Obtiene el RUT del operante.
     *
     * @return El RUT del operante.
     */
    public String getRutOperante() {
        return rutOperante;
    }

    /**
     * Obtiene el tipo de documento.
     *
     * @return El tipo de documento.
     */
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    /**
     * Obtiene el tipo de operaci&oacute;n.
     *
     * @return El tipo de operaci&oacute;n.
     */
    public String getTipoOperacion() {
        return tipoOperacion;
    }

    /**
     * Obtiene el tipo de transacci&oacute;n.
     *
     * @return El tipo de transacci&oacute;n.
     */
    public String getTipoTransaccion() {
        return tipoTransaccion;
    }

    @Override
    public String toString() {
        return "RegistroDJ1891 [cantidadAcciones=" + cantidadAcciones + ", fechaDocumento="
                + fechaDocumento + ", montoCompra=" + montoCompras+ ", montoVenta=" + montoVentas + ", valorAdquisicion=" + valorAdquisicion+ ", nemotecnico=" + nemotecnico
                + ", numeroDocumento=" + numeroDocumento + ", rutEmisor=" + rutEmisor
                + ", rutOperante=" + rutOperante + ", tipoDocumento=" + tipoDocumento
                + ", tipoOperacion=" + tipoOperacion + ", tipoTransaccion=" + tipoTransaccion + "]";
    }

}
