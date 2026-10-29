package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

import java.math.BigDecimal;
import java.util.Date;
public class RegistroDJ1849 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
    
	private long idCartera;
    private Date fechaOperacion;
    private long numero;
    private String rutEmisor;
    private String rutTenedor;
    private String tipoContribuyenteRutTenedor;
    private String nemotecnicoSVS;
    private BigDecimal interesDevengado;
    private long retencionImpuesto;
    private BigDecimal sumaInteresDevengado;
    
	public long getIdCartera() {
		return idCartera;
	}

	public void setIdCartera(long idCartera) {
		this.idCartera = idCartera;
	}

	public Date getFechaOperacion() {
		return fechaOperacion;
	}

	public void setFechaOperacion(Date fechaOperacion) {
		this.fechaOperacion = fechaOperacion;
	}

	public long getNumero() {
		return numero;
	}

	public void setNumero(long numero) {
		this.numero = numero;
	}

	public String getRutEmisor() {
		return rutEmisor;
	}

	public void setRutEmisor(String rutEmisor) {
		this.rutEmisor = rutEmisor;
	}

	public String getRutTenedor() {
		return rutTenedor;
	}

	public void setRutTenedor(String rutTenedor) {
		this.rutTenedor = rutTenedor;
	}

	public String getTipoContribuyenteRutTenedor() {
		return tipoContribuyenteRutTenedor;
	}

	public void setTipoContribuyenteRutTenedor(String tipoContribuyenteRutTenedor) {
		this.tipoContribuyenteRutTenedor = tipoContribuyenteRutTenedor;
	}

	public String getNemotecnicoSVS() {
		return nemotecnicoSVS;
	}

	public void setNemotecnicoSVS(String nemotecnicoSVS) {
		this.nemotecnicoSVS = nemotecnicoSVS;
	}

	public BigDecimal getInteresDevengado() {
		return interesDevengado;
	}

	public void setInteresDevengado(BigDecimal interesDevengado) {
		this.interesDevengado = interesDevengado;
	}

	public long getRetencionImpuesto() {
		return retencionImpuesto;
	}

	public void setRetencionImpuesto(long retencionImpuesto) {
		this.retencionImpuesto = retencionImpuesto;
	}

	public BigDecimal getSumaInteresDevengado() {
		return sumaInteresDevengado;
	}

	public void setSumaInteresDevengado(BigDecimal sumaInteresDevengado) {
		this.sumaInteresDevengado = sumaInteresDevengado;
	}

	//Obtener valor del registro
    public RegistroDJ1849(final Registro registro) {
    	
   	 this.idCartera = (Long) registro.get("IdCartera");
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.numero = (Long) registro.get("Numero");
   	 this.rutEmisor = (String) registro.get("RutEmisor");
   	 this.rutTenedor = (String) registro.get("RutTenedor");
   	 this.tipoContribuyenteRutTenedor = (String) registro.get("TipoContribuyenteRutTenedor");
   	 this.nemotecnicoSVS = (String) registro.get("NemotecnicoSVS");
   	 this.interesDevengado = (BigDecimal) registro.get("InteresDevengado");
   	 this.retencionImpuesto = (Long) registro.get("RetencionImpuesto");
   	 
    }

	public RegistroDJ1849() {
    }
	
    public void acumular(final RegistroDJ1849 registroDJ1849) {        
        if(this.sumaInteresDevengado != null){
        	this.sumaInteresDevengado = this.sumaInteresDevengado.add(registroDJ1849.getInteresDevengado());
        }
        else{
        	this.sumaInteresDevengado = registroDJ1849.getInteresDevengado();
        }
    	
    }
	
}
