package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ999 {
	
	/**
     * Declaracion de variables para la DJ
     */
	private long IdEmpresa; 						//BOGINT DEFAULT 0
	private long IdOperacionSpot;  					//BIGINT DEFAULT 0 
	private String EventoReporte;  					//NVARCHAR(1) DEFAULT '' NOT NULL 
	private long CodigoInstitucion;  				//BIGINT DEFAULT 0 
	private long NumeroOperacion;  					//BIGINT DEFAULT 0 
	private Date FechaOperacion;					//DATE DEFAULT '0001-01-01' NOT NULL 
	private long TipoDocumento;						//BIGINT DEFAULT 1 
	private long CodigoComercio;					//BIGINT DEFAULT 0 
	private long IdPais;							//BIGINT DEFAULT 0
	private long MonedaOperacion;					//BIGINT DEFAULT 0 
	private double MontoOperacion;					//DECIMAL(20,2) DEFAULT 0 NOT NULL 
	private double MontoDolar;						//DECIMAL(20,2) DEFAULT 0 NOT NULL 
	private double TipoCambio;						//DECIMAL(10,2) DEFAULT 0 NOT NULL 
	private double MontoEquiClp;					//DECIMAL(20,2) DEFAULT 0 NOT NULL 
	private long AfectoADerivado;					//BIGINT DEFAULT 0 
	private String DocumentoIdentidad;				//NVARCHAR(10) DEFAULT '' NOT NULL 
	private String NombreCompleto;					//NVARCHAR(100) DEFAULT '' NOT NULL  
	private String TipoAutorizacion; 				//NVARCHAR(2) DEFAULT ''NOT NULL 
	private long NumeroAutorizacion; 				//BIGINT DEFAULT 0 
	private Date FechaAutorizacion;					//DATE DEFAULT '0001-01-01' NOT NULL 
	private long SectorEconomicoBeneficiario;  		//BIGINT DEFAULT 0 
	private long NumeroInscripcion;					//BIGINT DEFAULT 0 
	private Date FechaDesembolso;					//DATE DEFAULT '0001-01-01' NOT NULL 
	private long SectorEconomicoInversionista;		//BIGINT DEFAULT 0 
	private String NombreFinancistaExtranjero;  	//NVARCHAR(100) DEFAULT ''NOT NULL 
	private Date FechaVencimientoCapital;			//DATE DEFAULT '0001-01-01' NOT NULL 
	private long NroContDerivado;					//NVARCHAR(52) DEFAULT ''NOT NULL 
	private Date FechaSuscDerivado;					//DATE DEFAULT '0001-01-01' NOT NULL 
	private Date FechaVencDerivado;					//DATE DEFAULT '0001-01-01' NOT NULL 
	private String Observacion;						//NVARCHAR(250) DEFAULT ''NOT NULL 
	private long EsArbitraje;						//NVARCHAR(250) DEFAULT ''NOT NULL 
    
    /**
     * Lectores
     */
	public long getIdEmpresa() {
		return IdEmpresa;
	}
	 
    public long getIdOperacionSpot() {
		return IdOperacionSpot;
	}

	public String getEventoReporte() {
		return EventoReporte;
	}

	public long getCodigoInstitucion() {
		return CodigoInstitucion;
	}

	public long getNumeroOperacion() {
		return NumeroOperacion;
	}

	public Date getFechaOperacion() {
		return FechaOperacion;
	}

	public long getTipoDocumento() {
		return TipoDocumento;
	}

	public long getCodigoComercio() {
		return CodigoComercio;
	}

	public long getIdPais() {
		return IdPais;
	}

	public long getMonedaOperacion() {
		return MonedaOperacion;
	}

	public double getMontoOperacion() {
		return MontoOperacion;
	}

	public double getMontoDolar() {
		return MontoDolar;
	}

	public double getTipoCambio() {
		return TipoCambio;
	}

	public double getMontoEquiClp() {
		return MontoEquiClp;
	}

	public long getAfectoADerivado() {
		return AfectoADerivado;
	}

	public String getDocumentoIdentidad() {
		return DocumentoIdentidad;
	}

	public String getNombreCompleto() {
		return NombreCompleto;
	}

	public String getTipoAutorizacion() {
		return TipoAutorizacion;
	}

	public long getNumeroAutorizacion() {
		return NumeroAutorizacion;
	}

	public Date getFechaAutorizacion() {
		return FechaAutorizacion;
	}

	public long getSectorEconomicoBeneficiario() {
		return SectorEconomicoBeneficiario;
	}

	public long getNumeroInscripcion() {
		return NumeroInscripcion;
	}

	public Date getFechaDesembolso() {
		return FechaDesembolso;
	}

	public long getSectorEconomicoInversionista() {
		return SectorEconomicoInversionista;
	}

	public String getNombreFinancistaExtranjero() {
		return NombreFinancistaExtranjero;
	}

	public Date getFechaVencimientoCapital() {
		return FechaVencimientoCapital;
	}

	public long getNroContDerivado() {
		return NroContDerivado;
	}

	public Date getFechaSuscDerivado() {
		return FechaSuscDerivado;
	}

	public Date getFechaVencDerivado() {
		return FechaVencDerivado;
	}

	public String getObservacion() {
		return Observacion;
	}
	
	public long getEsArbitraje() {
		return EsArbitraje;
	}
 
	/**
     * Constructores
     */
    public RegistroDJ999() {
    }
    
    //Obtener valor del registro
    public RegistroDJ999(final Registro registro) {
     this.IdEmpresa = (Long) registro.get("IdEmpresa");
   	 this.IdOperacionSpot = (Long) registro.get("IdOperacionSpot");
   	 this.EventoReporte = (String) registro.get("EventoReporte");
   	 this.CodigoInstitucion = (Long) registro.get("CodigoInstitucion");
   	 this.NumeroOperacion = (Long) registro.get("NumeroOperacion");
   	 this.FechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.TipoDocumento = (Long) registro.get("TipoDocumento");
   	 this.CodigoComercio = (Long) registro.get("CodigoComercio");
   	 this.IdPais = (Long) registro.get("IdPais");
   	 this.MonedaOperacion = (Long) registro.get("MonedaOperacion");
   	 this.MontoOperacion = ((Number) registro.get("MontoOperacion")).doubleValue();
   	 this.MontoDolar = ((Number) registro.get("MontoDolar")).doubleValue();
   	 this.TipoCambio = ((Number) registro.get("TipoCambio")).doubleValue();
   	 this.MontoEquiClp = ((Number) registro.get("MontoEquiClp")).doubleValue();
   	 this.AfectoADerivado = (Long) registro.get("AfectoADerivado");
   	 this.DocumentoIdentidad = (String) registro.get("DocumentoIdentidad");
   	 this.NombreCompleto = (String) registro.get("NombreCompleto");
   	 this.TipoAutorizacion = (String) registro.get("TipoAutorizacion");
   	 this.NumeroAutorizacion = (Long) registro.get("NumeroAutorizacion");
   	 this.FechaAutorizacion = (Date) registro.get("FechaAutorizacion");
   	 this.SectorEconomicoBeneficiario = (Long) registro.get("SectorEconomicoBeneficiario");
   	 this.NumeroInscripcion = (Long) registro.get("NumeroInscripcion");
   	 this.FechaDesembolso = (Date) registro.get("FechaDesembolso");
   	 this.SectorEconomicoInversionista = (Long) registro.get("SectorEconomicoInversionista");
   	 this.NombreFinancistaExtranjero = (String) registro.get("NombreFinancistaExtranjero");
   	 this.FechaVencimientoCapital = (Date) registro.get("FechaVencimientoCapital");
   	 this.NroContDerivado = (Long) registro.get("NroContDerivado");
   	 this.FechaSuscDerivado = (Date) registro.get("FechaSuscDerivado");
   	 this.FechaVencDerivado = (Date) registro.get("FechaVencDerivado");
   	 this.Observacion = (String) registro.get("Observacion");
   	 this.EsArbitraje = (Long) registro.get("EsArbitraje");
    }
}
