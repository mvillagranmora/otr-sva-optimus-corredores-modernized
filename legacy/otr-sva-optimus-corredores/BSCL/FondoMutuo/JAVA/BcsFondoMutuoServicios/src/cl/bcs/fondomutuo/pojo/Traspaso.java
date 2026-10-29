package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import cl.bcs.utils.Utils;

public class Traspaso {
	
	

	public static class RegistroTraspaso{
		public Long     IdTraspaso;
		public Long 	IdInterno;
		public Long 	IdEmpresa;
		public String 	IdModulo;	
		public Long 	IdUsuario;
		public Long 	IdCliente;
		public Long 	IdPortafolio;
		public String	IdEmisor;
		public String	IdMotivoTraspaso;
		public String	IdTipoMovimientoTraspaso;
		public String 	IdTipoTraspaso;
		public String 	Glosa;
		public String 	FechaHoraSuscripcion;
		public Long 	FolioTraspaso;
		public Long 	IdDivisionPais;
		public String 	IdEstado;
		public BigDecimal 	Monto;
		public Date 	FechaIngreso;
		public Date 	FechaModificacion;
		public String 	IdMotivoAnulacion="";
		public Long 	FolioMLT;
		public Long 	FolioCSC;
		public Long 	IdEmpleadoEjecutivo;
		public Long		IdEntidadRecaudadora;
		
		public Long 	IdPersonaEmisor;
		public Long 	IdPersonaEntidad;
		public Long 	IdPersonaCliente;
		
		public RegistroTraspaso() {
			this.FolioMLT = 0L;
			this.FolioCSC = 0L;
			this.IdMotivoAnulacion = "";
			this.IdEmpleadoEjecutivo = 0L;
			this.IdEntidadRecaudadora= 0L;
			this.IdPersonaEmisor= 0L;
			this.IdPersonaEntidad= 0L;
			this.IdPersonaCliente= 0L;
		}
		 
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	public static class RegistroTraspasoOrigen{
		//public Long   	IdDetalleTraspaso;
		public Long   	IdTraspaso;
		public String 	IdSerieFondo;
		public double 	Cantidad;
		public String 	IdRegimenTributario;	
		public String 	IdEstado;
		public Date 	FechaIngreso;
		public Date 	FechaModificacion;
		
		public Long   	IdSolicitudAporteRescate;
		
			
		public Long IdInterno;
		public Long IdDetalleTraspaso;
	    public RegistroTraspasoOrigen() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    	IdSolicitudAporteRescate = 0L;
	    	IdRegimenTributario = "";
	    	
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	};
	public static class RegistroTraspasoDestino{
		//public Long   	IdDetalleTraspaso;
		public Long   	IdTraspaso;
		public String 	IdSerieFondo;
		public double 	Porcentaje;
		public String 	IdEstado;
		public Date 	FechaIngreso;
		public Date 	FechaModificacion;
		public String	IdRegimenFondoMutuo;	
		public Long IdInterno;
		public Long IdDetalleTraspaso;
		public String RunFFMM;
		public String NombreFFMM;
	    public RegistroTraspasoDestino() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    	IdSerieFondo="";
	    	RunFFMM="";
	    	NombreFFMM="";
	    		
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	};
	
	public static class RegistroRecaudacion{
		
		
		public Long 		IdTraspaso;
		public Date 		FechaDeposito;
		public String 		IdMedioDePago;
		public Long 		IdBanco;
		public Long 		NumeroCuentaBancaria;
		public Long 		IdCuentaBanco;
		public String 		Sucursal;
		public BigDecimal 	MontoDeposito;
		public String 		Observacion;
		//public String 		IdRegimenTributario="";	
		
		public Long 		IdCliente;
		public Long 		IdPortafolio;
			
		public String  		EstadoProceso ="";
		public String 		Codigo;
		
		public Long 		IdRecaudacion= 0L;
		public Long			IdEmpresa= 0L;
		public Long			IdUsuario= 0L;
		public String 		IdEstado ="";
		public Date 		FechaIngreso;
		public Date 		FechaAceptacion;
   
		public Long 		IdPersonaEmisor= 0L;
		public Long 		IdPersonaEntidad= 0L;
		public Long     	IdPersonaCliente= 0L;
		
		public Long			FolioCSC = 0L;
		
		public	List<InMovimientoGenerado>			MovimientoGenerado;
		public String IdMonedaFondo;
		
		public RegistroRecaudacion(){
			MovimientoGenerado 				= new ArrayList<InMovimientoGenerado>();
			IdMonedaFondo = "CLP";
		}
		
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	public static class InMovimientoGenerado{
		
		public Long 	IdRecaudacion;
		public Long 	IdEmpresa;
		public Long 	IdUsuario;
		public String	IdModulo;
	//	public String	IdTipoMovimiento;
		public Long 	IdSecuenciaMovimiento;
	//	public String	TipoEntidad;
		public Date		FechaIngreso;
				
		public String  EstadoProceso ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	
	
	
}
