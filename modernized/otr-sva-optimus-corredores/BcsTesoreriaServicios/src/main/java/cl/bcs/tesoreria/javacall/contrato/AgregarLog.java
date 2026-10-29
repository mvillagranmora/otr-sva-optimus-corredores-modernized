package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.pla.AgregarLog} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class AgregarLog {

	private AgregarLog() {
	}


	// legacy cl/bcs/plataforma/pla/AgregarLog.java:37-166 (verbatim)
	public static class InAgregarLog{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdEstadoAutorizacion;
		public Long p_IdTrabajo = 0L ;
		public List<InAgregarLog.Log> p_Log;
		public List<InAgregarLog.DetalleLog> p_LogDetalle;
		public InAgregarLog(){
			p_Log = new ArrayList<InAgregarLog.Log>();
			p_LogDetalle = new ArrayList<InAgregarLog.DetalleLog>();
		}
		
		public void agregarLogBasico(Long idEmpresa, Long idUsuario, String idModulo, Long idCabeceraLog, Long idTrabajo, String idTipoLog, String idTipoRegistro, String fechaLog, String nombreProceso) {
			InAgregarLog.Log l1 = new InAgregarLog.Log();
			l1.IdEmpresa = idEmpresa;
			l1.IdModulo = idModulo;
			l1.IdUsuario = idUsuario;
			
			l1.IdCabeceraLog = idCabeceraLog;
			l1.IdTrabajo = idTrabajo;
			l1.IdTipoLog = idTipoLog;
			l1.IdTipoRegistro= idTipoRegistro;
			l1.FechaSistemaLog = fechaLog;
			l1.NombreProceso= nombreProceso;
			p_Log.add(l1);
		}
		
		public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo) {
			InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
			dl2.IdCabeceraLog = idCabeceraLog;
			dl2.IdTrabajo = idTrabajo;
			dl2.CampoRegistro = campoRegistro;
			dl2.ValorAntiguo = valorAntiguo;
			dl2.ValorNuevo = valorNuevo;
			p_LogDetalle.add(dl2);
		}
		
		public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo, String subRegistro) {
			InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
			dl2.IdCabeceraLog = idCabeceraLog;
			dl2.IdTrabajo = idTrabajo;
			dl2.CampoRegistro = campoRegistro;
			dl2.ValorAntiguo = valorAntiguo;
			dl2.ValorNuevo = valorNuevo;
			dl2.SubRegistro = subRegistro;
			p_LogDetalle.add(dl2);
		}
		
		public static class Log {
			public Long			IdTrabajo; 
			public Long			IdCabeceraLog; 
			public String		IdTipoLog; 
			public String		IdModulo; 
			public Long			IdUsuario; 
			public Long			IdEmpresa; 
			public String		FechaSistemaLog; 
			public Long			IdUsuarioAtendido; 
			public Long			IdCliente; 
			public String		EstadoCliente; 
			public String		NombreCliente; 
			public String		IdTipoDocumentoIdentidad; 
			public String		IdDocumentoIdentidad; 
			public Long			IdPortafolio; 
			public String		EstadoPortafolio; 
			public String		IdTipoRegistro; 
			public String		IdRegistro; 
			public String		EstadoRegistro; 
			public String		IdTipoOperacion; 
			public Long			Folio; 
			public BigDecimal	Cantidad; 
			public BigDecimal	PrecioTasa; 
			public BigDecimal	Monto; 
			public String		IdMonedaOrigen; 
			public String		IdMonedaLiquidacion; 
			public String		TipoCambio; 
			public String		Nemo; 
			public Long			IdProceso; 
			public String		EstadoProceso; 
			public String		NombreProceso; 
			public Long			IdPersona;
			
			public Log() {
				 this.IdTrabajo = 0L;  
				 this.IdCliente = 0L; 
				 this.EstadoCliente = ""; 
				 this.NombreCliente = ""; 
				 this.IdTipoDocumentoIdentidad = ""; 
				 this.IdDocumentoIdentidad = ""; 
				 this.IdPortafolio = 0L; 
				 this.EstadoPortafolio = "";  
				 this.IdRegistro = ""; 
				 this.EstadoRegistro = ""; 
				 this.IdTipoOperacion = ""; 
				 this.Folio = 0L; 
				 this.Cantidad = BigDecimal.ZERO; 
				 this.PrecioTasa = BigDecimal.ZERO; 
				 this.Monto = BigDecimal.ZERO; 
				 this.IdMonedaOrigen = ""; 
				 this.IdMonedaLiquidacion = ""; 
				 this.TipoCambio = ""; 
				 this.Nemo = ""; 
				 this.IdProceso = 0L; 
				 this.EstadoProceso = ""; 
				 this.NombreProceso = ""; 
				 this.IdPersona = 0L;
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		public static class DetalleLog {
			public Long			IdTrabajo; 
			public Long			IdCabeceraLog; 
			public String		SubRegistro; 
			public String		CampoRegistro; 
			public String		ValorAntiguo; 
			public String		ValorNuevo; 
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/pla/AgregarLog.java:186-192 (verbatim)
	public static class OutAgregarLog{
		public Long P_OUT_ERRORESCONTADOR;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
