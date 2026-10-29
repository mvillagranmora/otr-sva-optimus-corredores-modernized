package cl.bcs.tesoreria.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.RecaudacionTraspaso.InRecaudacion;
import cl.bcs.utils.Utils;

public class DistribucionPac {
	
	

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
		public Long 	FolioMLT = 0L;
		public Long 	FolioCSC = 0L;
		public Long 	IdEmpleadoEjecutivo = 0L;
		
		
		public Long 	IdPersonaEmisor= 0L;
		
		
	   
	    
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
			
		public Long IdInterno;
		public Long IdDetalleTraspaso;
	    public RegistroTraspasoDestino() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
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
   
		public Long 	IdPersonaEmisor= 0L;
		
		public	List<InMovimientoGenerado>			MovimientoGenerado;
		
		
		public RegistroRecaudacion(){
			MovimientoGenerado 				= new ArrayList<InMovimientoGenerado>();
			
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
				
		private String  EstadoProceso ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	
	
	
}

