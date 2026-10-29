package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.pla.AgregarLog (BcsPlataformaServicios/.../pla/AgregarLog.java).
 * The input/output classes (InAgregarLog, Log, DetalleLog, OutAgregarLog) are copied VERBATIM from AgregarLog.java:
 * the nested part below. The real logica() inserts the audit rows into HANA staging tables and calls the stored
 * procedure "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java" (AgregarLog.java:187-206), which cannot run on H2.
 * The shim records every entrada it receives (AUDITORIA) so that the oracle pins WHAT each reporting class sends to
 * the platform audit service; it returns P_OUT_ERRORESCONTADOR = 0 like the real one on success. Like the real one,
 * an entrada without p_Log returns immediately (AgregarLog.java:183-185).
 */
public class AgregarLog implements JavaCall {

    /** Every InAgregarLog received, serialised with the legacy gson configuration. */
    public static final List<String> AUDITORIA = new ArrayList<>();

	//ENTRADA Y SALIDA DE LA CLASE
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
	
	public static class Persona{
		public Long IdPersona=0L;
		public String NombreCompleto="";
		public String DocumentoIdentidad="";
		public String TipoDocumentoIdentidad="";
	}
	
	public static class Cliente extends Persona{
		public Long IdCliente=0L;
		public String EstadoCliente="";
	}
	
	public static class Portafolio extends Cliente{
		public Long IdPortafolio=0L;
		public String CodigoPortafolio="";
		public String EstadoPortafolio="";
	}

	public static class OutAgregarLog{
		public Long P_OUT_ERRORESCONTADOR;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = new Gson();
        InAgregarLog in = gson.fromJson(jin.toString(), InAgregarLog.class);
        OutAgregarLog out = logica(con, in);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public OutAgregarLog logica(Connection con, InAgregarLog entrada) throws Exception {
        OutAgregarLog salida = new OutAgregarLog();
        salida.P_OUT_ERRORESCONTADOR = Long.valueOf(0);
        if (entrada.p_Log.size() == 0) {
            return salida;
        }
        AUDITORIA.add(Utils.printJson(entrada, false));
        return salida;
    }
}
