package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1820 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
    private String rutCliente;
    private String taxID;
    private String codigoPaisResidencia;
    private String codigoRelacion;
    private String codigoModoContrato;
    private String tipoAcuerdoMarco;
    private long numeroAcuerdoMarco;
    private Date fechaSuscripcionAcuerdo;
    private long numeroContrato;
    private Date fechaSuscripcionContrato;
    private String tipoEvento;
    private String tipoContrato;
    private String instrumento;
    private String modalidadCumplimiento;
    private String codigoPosicionDeclarante;
    private String tipoActivoSubyacente;
    private String codigoActivoSubyacente;    
    private String otroActivoSubyacente;
    private double tasaActivoSubyacente;
    private String tipoSegundoActivoSubyacente;
    private String codigoSegundoActivoSubyacente;
    private String otroSegundoActivoSubyacente;
    private double tasaSegundoActivoSubyacente;
    private String codigoPrecioFuturo;
    private double precioFuturo;
    private String codigoMonedaFuturo;
    private String codigoUnidad;
    private double montoNocional;
    private String codigoSegundaUnidad;
    private String segundoMontoNocional;
    private Date fechaVencimiento;
    
    /**
     * Lectores
     */
    public String getRutCliente() {
        return rutCliente;
    }
    
    
    public String getTaxID() {
        return taxID;
    }
        
    public String getCodigoPaisResidencia() {
        return codigoPaisResidencia;
    }
    
    public String getCodigoRelacion() {
        return codigoRelacion;
    }
       
    public String getCodigoModoContrato() {
        return codigoModoContrato;
    }
       
    public String getTipoAcuerdoMarco() {
        return tipoAcuerdoMarco;
    }
    
    
	public long getNumeroAcuerdoMarco() {
	    return numeroAcuerdoMarco;
	}
       
    
	public Date getFechaSuscripcionAcuerdo() {
	    return fechaSuscripcionAcuerdo;
	}
	
	public long getNumeroContrato() {
	    return numeroContrato;
	}
	
	public Date getFechaSuscripcionContrato() {
	    return fechaSuscripcionContrato;
	}
	
	 public String getTipoEvento() {
	        return tipoEvento;
	}	
	
	 public String getTipoContrato() {
	        return tipoContrato;
	}	
	 
	 public String getInstrumento() {
	        return instrumento;
	}	 
	 
	 public String getModalidadCumplimiento() {
	        return modalidadCumplimiento;
	}
	 
	 public String getCodigoPosicionDeclarante() {
	        return codigoPosicionDeclarante;
	}	 
	 
	 public String getTipoActivoSubyacente() {
	        return tipoActivoSubyacente;
	}
	 
	 public String getCodigoActivoSubyacente() {
	        return codigoActivoSubyacente;
	}	 
	
	 public String getOtroActivoSubyacente() {
	        return otroActivoSubyacente;
	}	 
	 	

	 public double getTasaActivoSubyacente() {
	        return tasaActivoSubyacente;
	}	 
	 	
	 public String getTipoSegundoActivoSubyacente() {
	        return tipoSegundoActivoSubyacente;
	} 
	 
	 public String getCodigoSegundoActivoSubyacente() {
	        return codigoSegundoActivoSubyacente;
	} 
	 
	 public String getOtroSegundoActivoSubyacente() {
	        return otroSegundoActivoSubyacente;
	}	 
	 
	 public double getTasaSegundoActivoSubyacente() {
	        return tasaSegundoActivoSubyacente;
	} 
	 
	 public String getCodigoPrecioFuturo() {
	        return codigoPrecioFuturo;
	}	 
	
	 public double getPrecioFuturo() {
	        return precioFuturo;
	}  
	 
	 public String getCodigoMonedaFuturo() {
	        return codigoMonedaFuturo;
	}	 

	 public String getCodigoUnidad() {
	        return codigoUnidad;
	}		 
	 
	 public double getMontoNocional() {
	        return montoNocional;
	}  	 
	  
	 public String getCodigoSegundaUnidad() {
	        return codigoSegundaUnidad;
	} 
	 
	 public String getSegundoMontoNocional() {
	        return segundoMontoNocional;
	} 
	 
	public Date getFechaVencimiento() {
			return fechaVencimiento;
	}	 
	 
	 
    public RegistroDJ1820() {
    }
    
    //Obtener valor del registro
    public RegistroDJ1820(final Registro registro) {
    	
   	 this.rutCliente = (String) registro.get("RutCliente");
   	 this.taxID = (String) registro.get("TaxID");
   	 this.codigoPaisResidencia = (String) registro.get("CodigoPaisResidencia");
   	 this.codigoRelacion = (String) registro.get("CodigoRelacion");
   	 this.codigoModoContrato = (String) registro.get("CodigoModoContrato");
   	 this.tipoAcuerdoMarco = (String) registro.get("TipoAcuerdoMarco");
   	 this.numeroAcuerdoMarco = (Long) registro.get("NumeroAcuerdoMarco");
   	 this.fechaSuscripcionAcuerdo = (Date) registro.get("FechaSuscripcionAcuerdo");
   	 this.numeroContrato = (Long) registro.get("NumeroContrato");
   	 this.fechaSuscripcionContrato = (Date) registro.get("FechaSuscripcionContrato");
   	 this.tipoEvento = (String) registro.get("TipoEvento");
   	 this.tipoContrato = (String) registro.get("TipoContrato");
   	 this.instrumento = (String) registro.get("Instrumento");
   	 this.modalidadCumplimiento = (String) registro.get("ModalidadCumplimiento");
   	 this.codigoPosicionDeclarante = (String) registro.get("CodigoPosicionDeclarante");
   	 this.tipoActivoSubyacente = (String) registro.get("TipoActivoSubyacente");
   	 this.codigoActivoSubyacente = (String) registro.get("CodigoActivoSubyacente");
   	 this.otroActivoSubyacente = (String) registro.get("OtroActivoSubyacente");
   	 this.tasaActivoSubyacente = (double) ((Number) registro.get("TasaActivoSubyacente")).doubleValue();
   	 this.tipoSegundoActivoSubyacente = (String) registro.get("TipoSegundoActivoSubyacente");  	
   	 this.codigoSegundoActivoSubyacente = (String) registro.get("CodigoSegundoActivoSubyacente");
   	 this.otroSegundoActivoSubyacente = (String) registro.get("OtroSegundoActivoSubyacente");
   	 this.tasaSegundoActivoSubyacente = (double) ((Number) registro.get("TasaSegundoActivoSubyacente")).doubleValue();
   	 this.codigoPrecioFuturo = (String) registro.get("CodigoPrecioFuturo");
   	 this.precioFuturo = (double) ((Number) registro.get("PrecioFuturo")).doubleValue();
   	 this.codigoMonedaFuturo = (String) registro.get("CodigoMonedaFuturo");
   	 this.codigoUnidad = (String) registro.get("CodigoUnidad");
   	 this.montoNocional = (double) ((Number) registro.get("MontoNocional")).doubleValue();
   	 this.codigoSegundaUnidad = (String) registro.get("CodigoSegundaUnidad");
   	 this.segundoMontoNocional = (String) registro.get("SegundoMontoNocional");
   	 this.fechaVencimiento = (Date) registro.get("FechaVencimiento");
   	
   	 
    }


    
    public void acumular(final RegistroDJ1820 registroDJ1820) {
      
    }
	
}
