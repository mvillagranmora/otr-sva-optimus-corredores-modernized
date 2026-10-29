package cl.bcs.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datadog.trace.api.Trace;

public class AgregarLog implements JavaCall{	
	private final Logger log = LogManager.getLogger(AgregarLog.class);

	private final String SQL_INSERT_LOG = loadResourceToString("cl/bcs/plataforma/services/sql/AgregarLog_InsertLog.sql");
	private final String SQL_INSERT_DETALLELOG = loadResourceToString("cl/bcs/plataforma/services/sql/AgregarLog_InsertDetalleLog.sql");

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
	}
	public static class DetalleLog {
		public Long			IdTrabajo; 
		public Long			IdCabeceraLog; 
		public String		SubRegistro; 
		public String		CampoRegistro; 
		public String		ValorAntiguo; 
		public String		ValorNuevo; 
	}
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
	public static class InAgregarLog{
		public Long p_IdTrabajo = 0L ;
		public List<Log> p_Log;
		public List<DetalleLog> p_LogDetalle;
		public InAgregarLog(){
			p_Log = new ArrayList<Log>();
			p_LogDetalle = new ArrayList<DetalleLog>();
		}
		
		public void agregarLogBasico(Long idEmpresa, Long idUsuario, String idModulo, Long idCabeceraLog, Long idTrabajo, String idTipoLog, String idTipoRegistro, String fechaLog, String nombreProceso) {
			Log l1 = new Log();
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
			DetalleLog dl2 = new DetalleLog();
			dl2.IdCabeceraLog = idCabeceraLog;
			dl2.IdTrabajo = idTrabajo;
			dl2.CampoRegistro = campoRegistro;
			dl2.ValorAntiguo = valorAntiguo;
			dl2.ValorNuevo = valorNuevo;
			p_LogDetalle.add(dl2);
		}
		
		
	}

	public static class OutAgregarLog{
		public Long P_OUT_ERRORESCONTADOR;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Log_Proceso extends BcsPojo{
		public Long IdLog;
		public String IdTipoLog;
		public String IdModulo;
		public Long IdUsuario;
		public Long IdEmpresa;
		public Date FechaLog;
		public String PeriodoLog;
		public Long IdUsuarioAtendido;
		public Long IdCliente;
		public String EstadoCliente;
		public String NombreCliente;
		public String IdTipoDocumentoIdentidad;
		public String IdDocumentoIdentidad;
		public Long IdPortafolio;
		public String EstadoPortafolio;
		public String IdRegistro;
		public String EstadoRegistro;
		public String IdTipoRegistro;
		public String IdTipoOperacion;
		public Long Folio;
		public BigDecimal Cantidad;
		public BigDecimal PrecioTasa;
		public BigDecimal Monto;
		public String IdMonedaOrigen;
		public String IdMonedaLiquidacion;
		public String TipoCambio;
		public String Nemo;
		public Long IdProceso;
		public String EstadoProceso;
		public String NombreProceso;
		public String FechaRegistro;
		public Long IdPersona;
		public String CodigoAutenticacion;
	}
	
	public static class DetalleLog_Proceso extends BcsPojo{
		public Long IdDetalleLog;
		public Long IdLog;
		public String CampoRegistro;
		public String SubRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;
		public String CodigoAutenticacion;
	}
	
	
	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();		
		InAgregarLog in=gson.fromJson(jin.toString() ,InAgregarLog.class);
		OutAgregarLog out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public OutAgregarLog logica(Connection con, InAgregarLog entrada) throws Exception {
		log.info("INICIO:AgregarLog");
		
		OutAgregarLog salida 			= new OutAgregarLog();
		salida.P_OUT_ERRORESCONTADOR 	= 0L;
		SQL sql 						= new SQL();		
		
		List<Long> ids = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Log", (long) entrada.p_Log.size());
		List<Long> idsDetalle = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::DetalleLog", (long) entrada.p_LogDetalle.size());
		int i = 0;
		HashMap<Long, Log_Proceso> map_Proceso = new HashMap<>();
		List<Log_Proceso> list_log = new ArrayList<>();
		List<DetalleLog_Proceso> list_detalleLog = new ArrayList<>();
		for (Log asdf : entrada.p_Log) {
			Log_Proceso l = new Log_Proceso();
			l.IdLog= ids.get(i++);
			l.IdTipoLog = asdf.IdTipoLog;
			l.IdModulo = asdf.IdModulo;
			l.IdUsuario = asdf.IdUsuario;
			l.IdEmpresa = asdf.IdEmpresa;
			l.FechaLog  = Fecha.toDate(asdf.FechaSistemaLog);
			l.PeriodoLog = asdf.FechaSistemaLog.substring(0,7);
			l.IdUsuarioAtendido = asdf.IdUsuarioAtendido;
			l.IdCliente = asdf.IdCliente;
			l.EstadoCliente = asdf.EstadoCliente;
			l.NombreCliente =  asdf.NombreCliente;
			l.IdTipoDocumentoIdentidad = asdf.IdTipoDocumentoIdentidad;
			l.IdDocumentoIdentidad = asdf.IdDocumentoIdentidad;
			l.IdPortafolio = asdf.IdPortafolio;
			l.EstadoPortafolio = asdf.EstadoPortafolio;
			l.IdRegistro = asdf.IdRegistro;
			l.EstadoRegistro = asdf.EstadoRegistro;
			l.IdTipoRegistro = asdf.IdTipoRegistro;
			l.IdTipoOperacion = asdf.IdTipoOperacion;
			l.Folio = asdf.Folio;
			l.Cantidad = asdf.Cantidad;
			l.PrecioTasa = asdf.PrecioTasa;
			l.Monto = asdf.Monto;
			l.IdMonedaOrigen = asdf.IdMonedaOrigen;
			l.IdMonedaLiquidacion = asdf.IdMonedaLiquidacion;
			l.TipoCambio = asdf.TipoCambio;
			l.Nemo = asdf.Nemo;
			l.IdProceso = asdf.IdProceso;
			l.EstadoProceso = asdf.EstadoProceso;
			l.NombreProceso = asdf.NombreProceso;
			l.IdPersona = asdf.IdPersona;

			l.CodigoAutenticacion = hashSHA256Hex(l.IdLog+l.IdTipoLog+l.IdModulo+l.IdUsuario+l.IdEmpresa+l.FechaLog+asdf.FechaSistemaLog.substring(0,7)+l.IdUsuarioAtendido+l.IdCliente+
					l.EstadoCliente+l.NombreCliente+l.IdTipoDocumentoIdentidad+l.IdDocumentoIdentidad+l.IdPortafolio+l.EstadoPortafolio+l.IdRegistro+l.EstadoRegistro+
					l.IdTipoRegistro+l.IdTipoOperacion+l.Folio+l.Cantidad+l.PrecioTasa+l.Monto+l.IdProceso+l.EstadoProceso+l.NombreProceso+l.IdMonedaOrigen+
					l.IdMonedaLiquidacion+l.TipoCambio+l.Nemo+l.IdPersona).toUpperCase();
			
			map_Proceso.put(asdf.IdCabeceraLog, l);
			list_log.add(l);
		}
		
		int j = 0;
		for (DetalleLog asdf : entrada.p_LogDetalle) {
			Log_Proceso log = map_Proceso.get(asdf.IdCabeceraLog);
			
			DetalleLog_Proceso det = new DetalleLog_Proceso();
			det.IdLog = log.IdLog;
			det.IdDetalleLog = idsDetalle.get(j++);
			det.SubRegistro = asdf.SubRegistro;
			det.CampoRegistro = asdf.CampoRegistro;
			det.ValorAntiguo = asdf.ValorAntiguo;
			if (asdf.ValorNuevo!=null && asdf.ValorNuevo.length()>5000)
				asdf.ValorNuevo = asdf.ValorNuevo.substring(0,5000);
			det.ValorNuevo = asdf.ValorNuevo;
			det.CodigoAutenticacion = hashSHA256Hex(det.IdDetalleLog+det.IdLog+det.SubRegistro+det.ValorNuevo+det.ValorNuevo+det.CampoRegistro).toUpperCase();
			list_detalleLog.add(det);
		}
		
		sql.batch(con,SQL_INSERT_LOG , list_log, Log_Proceso.class);
		sql.batch(con,SQL_INSERT_DETALLELOG , list_detalleLog, DetalleLog_Proceso.class);
		
		log.info("FIN:AgregarLog");
		return salida;
	}	
	
	public static String hashSHA256Hex(String input) throws NoSuchAlgorithmException {
        if (input == null) return null;
        byte[] hashBytes = hashSHA256(input);
        return bytesToHex(hashBytes);
    }
	
	public static byte[] hashSHA256(String input) throws NoSuchAlgorithmException {
        if (input == null) return null;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        
        // En Java 17+ podría usar: return java.util.HexFormat.of().formatHex(bytes);
        
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) return null;
        
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
