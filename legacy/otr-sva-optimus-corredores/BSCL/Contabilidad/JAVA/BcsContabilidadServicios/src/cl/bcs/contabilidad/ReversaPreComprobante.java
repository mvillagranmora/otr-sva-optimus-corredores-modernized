package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.EnvioContabilidad.EntradaEnvioContabilidad;
import cl.bcs.contabilidad.EnvioContabilidad.SalidaEnvioContabilidad;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReversaPreComprobante implements JavaCall{
	
	private final Log log = new Log("CON.ReversaPreComprobante");
	
	private final String SQL_SELECT_REVERSA_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectReversaPrecomprobante.sql");
	private final String SQL_SELECT_PRECOMPROBANTE_ANTERIOR = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectPrecomprobanteAnterior.sql");
	private final String SQL_SELECT_PRECOMPROBANTE_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectPrecomprobanteAsiento.sql");
	private final String SQL_SELECT_EVENTO_PRECOMPROBANTE_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_SelectEventoPrecomprobanteAsiento.sql");
	private final String SQL_INSERT_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertPrecomprobante.sql");
	private final String SQL_INSERT_PRECOMPROBANTE_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertPrecomprobanteAsiento.sql");
	private final String SQL_INSERT_EVENTO_PRECOMPROBANTE_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/ReversaPreComprobante_InsertEventoPrecomprobanteAsiento.sql");
	
	public static class EntradaReversaPreComprobante{
		public Long p_IdEmpresa;
		public List<InPrecomprobante> p_IdPreComprobantes;
		public String p_FechaContable;
		public String p_CodigoUsuario;
		
		public EntradaReversaPreComprobante(){
			this.p_IdPreComprobantes = new ArrayList<InPrecomprobante>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InPrecomprobante{
		public Long IdPreComprobanteAnt;
		public Long IdPreComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Precomprobante{
		public Long IdPreComprobante;
        public Long IdTipoContabilidad;
        public Long IdEmpresa;
        public Long Ejercicio;
        public String Tipo;
        public String FechaContable;
        public String Glosa;
        public String FechaProceso;
        public String HoraProceso;
        public String CodigoUsuario;
        public Long FolioContabilidad;
        public Long IdReversa;
        public String Estado;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecomprobanteAsiento{
		public Long IdPreComprobanteAsiento;
		public Long IdEmpresa;
		public Long IdPreComprobante;
		public String IdCuentaContable;  
		public Long Ejercicio;
		public Long Linea;
		public BigDecimal MontoDebe;  
		public BigDecimal MontoHaber;
		public String FechaContable;
		public String Glosa;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoPrecomprobanteAsiento{
		public Long IdPreComprobanteAsiento;
		public Long IdEvento;
		public String Operador;
		public Long Linea;
		public String Estado;
		public String GlosaDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaReversaPreComprobante{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaReversaPreComprobante(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaReversaPreComprobante in = gson.fromJson(jin.toString(), EntradaReversaPreComprobante.class);
		SalidaReversaPreComprobante out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaReversaPreComprobante logica(Connection con, EntradaReversaPreComprobante entrada) throws Exception {
		log.write("INICIO::ReversaPreComprobante");
		SalidaReversaPreComprobante out = new SalidaReversaPreComprobante();
		SQL sql = new SQL();
		List<Long> idAnteriores = new ArrayList<Long>();
		String fechaProceso = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyy-MM-dd");
		String horaProceso = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		for(InPrecomprobante pre : entrada.p_IdPreComprobantes){
			if(!idAnteriores.contains(pre.IdPreComprobanteAnt)) idAnteriores.add(pre.IdPreComprobanteAnt);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPreComprobanteAnt", idAnteriores);
		
		List<Precomprobante> precomprobantesReversados = sql.query(con, SQL_SELECT_REVERSA_PRECOMPROBANTE, params, Precomprobante.class);
		
		HashMap<Long,Object> hPrecomprobantesReversados = Utils.List2Hash(precomprobantesReversados, "IdReversa");
		
		List<Long> idPrecomprobantes = new ArrayList<Long>();
		Long nuevosIdPrecomprobante = 0L;
		for(InPrecomprobante pre : entrada.p_IdPreComprobantes){
			Precomprobante reversa = (Precomprobante) hPrecomprobantesReversados.get(pre.IdPreComprobanteAnt);
			if(reversa == null){
				if(!idPrecomprobantes.contains(pre.IdPreComprobanteAnt)){
					idPrecomprobantes.add(pre.IdPreComprobanteAnt);
					if((new Long(0)).equals(pre.IdPreComprobante)) nuevosIdPrecomprobante++;
				}
			}else{
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("IdPreComprobanteAnt", String.valueOf(pre.IdPreComprobanteAnt));
				out.P_OUT_MENSAJES.add(new MensajeSalida(pre.IdPreComprobanteAnt,MensajeSalida.MENSAJE_ERROR,"PRECOMPROBANTE_YA_ESTA_REVERSADO",valores,con,entrada.p_IdEmpresa));
			}
		}
		
		params.clear();
		params.put("IdPreComprobanteAnt", idPrecomprobantes);
		List<Precomprobante> precomprobantesAnteriores = sql.query(con, SQL_SELECT_PRECOMPROBANTE_ANTERIOR, params, Precomprobante.class);
		
		params.clear();
		params.put("IdPreComprobanteAnt", idPrecomprobantes);
		List<PrecomprobanteAsiento> precomprobantesAsiento = sql.query(con, SQL_SELECT_PRECOMPROBANTE_ASIENTO, params, PrecomprobanteAsiento.class);
		
		params.clear();
		params.put("IdPreComprobanteAnt", idPrecomprobantes);
		List<EventoPrecomprobanteAsiento> eventosPrecomprobantesAsiento = sql.query(con, SQL_SELECT_EVENTO_PRECOMPROBANTE_ASIENTO, params, EventoPrecomprobanteAsiento.class);
		
		HashMap<Long, Object> hInPrecomprobante = Utils.List2Hash(entrada.p_IdPreComprobantes, "IdPreComprobanteAnt");
		
		List<Long> secuenciasPrecomprobantes = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdPreComprobante", nuevosIdPrecomprobante);
		List<Long> secuenciasPrecomprobantesAsiento = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento", new Long(precomprobantesAsiento.size()));
		
		int i = 0;
		int j = 0;
		List<Precomprobante> insertsPrecomprobante = new ArrayList<Precomprobante>();
		List<PrecomprobanteAsiento> insertsPrecomprobanteAsiento = new ArrayList<PrecomprobanteAsiento>();
		List<EventoPrecomprobanteAsiento> insertsEventoPrecomprobanteAsiento = new ArrayList<EventoPrecomprobanteAsiento>();
		for(Precomprobante pre : precomprobantesAnteriores){
			InPrecomprobante inPre = (InPrecomprobante) hInPrecomprobante.get(pre.IdPreComprobante);
			Precomprobante insertPreComprobante = new Precomprobante();
			insertPreComprobante = Utils.copyPojo(pre, insertPreComprobante);
			if((new Long(0)).equals(inPre.IdPreComprobante)){
				insertPreComprobante.IdPreComprobante = secuenciasPrecomprobantes.get(i);
				i++;
			}else{
				insertPreComprobante.IdPreComprobante = inPre.IdPreComprobante;
			}
			
			insertPreComprobante.FechaContable = entrada.p_FechaContable;
			insertPreComprobante.Glosa = "REVERSA PRE-COMPROBANTE N° " + inPre.IdPreComprobanteAnt;
			insertPreComprobante.FechaProceso = fechaProceso;
			insertPreComprobante.HoraProceso = horaProceso;
			insertPreComprobante.CodigoUsuario = entrada.p_CodigoUsuario;
			insertPreComprobante.IdReversa = inPre.IdPreComprobanteAnt;
			insertPreComprobante.Estado = "V";
			insertsPrecomprobante.add(insertPreComprobante);
			
			for(PrecomprobanteAsiento precomprobanteAsiento : precomprobantesAsiento){
				if(precomprobanteAsiento.IdPreComprobante.equals(inPre.IdPreComprobanteAnt)){
					PrecomprobanteAsiento insertPreComprobanteAsiento = new PrecomprobanteAsiento();
					insertPreComprobanteAsiento = Utils.copyPojo(precomprobanteAsiento, insertPreComprobanteAsiento);
					insertPreComprobanteAsiento.IdPreComprobanteAsiento = secuenciasPrecomprobantesAsiento.get(j);
					insertPreComprobanteAsiento.IdPreComprobante = insertPreComprobante.IdPreComprobante;
					insertPreComprobanteAsiento.FechaContable = entrada.p_FechaContable;
					insertPreComprobanteAsiento.Glosa = "REVERSA PRE-COMPROBANTE N° " + inPre.IdPreComprobanteAnt + " CUENTA N° " + precomprobanteAsiento.IdCuentaContable;
					insertsPrecomprobanteAsiento.add(insertPreComprobanteAsiento);
					j++;
					for(EventoPrecomprobanteAsiento eventoPreComprobanteAsiento : eventosPrecomprobantesAsiento){
						if(eventoPreComprobanteAsiento.IdPreComprobanteAsiento.equals(precomprobanteAsiento.IdPreComprobanteAsiento)){
							EventoPrecomprobanteAsiento eventoPreComprobanteAsientoNuevo = new EventoPrecomprobanteAsiento();
							eventoPreComprobanteAsientoNuevo = Utils.copyPojo(eventoPreComprobanteAsiento, eventoPreComprobanteAsientoNuevo);
							eventoPreComprobanteAsientoNuevo.IdPreComprobanteAsiento = insertPreComprobanteAsiento.IdPreComprobanteAsiento;
							insertsEventoPrecomprobanteAsiento.add(eventoPreComprobanteAsientoNuevo);
						}
					}
				}
			}
		}
		
		sql.batch(con, SQL_INSERT_PRECOMPROBANTE, insertsPrecomprobante, Precomprobante.class);
		sql.batch(con, SQL_INSERT_PRECOMPROBANTE_ASIENTO, insertsPrecomprobanteAsiento, PrecomprobanteAsiento.class);
		sql.batch(con, SQL_INSERT_EVENTO_PRECOMPROBANTE_ASIENTO, insertsEventoPrecomprobanteAsiento, EventoPrecomprobanteAsiento.class);
		
		EntradaEnvioContabilidad entradaEnvioContabilidad = new EntradaEnvioContabilidad();
		for(Precomprobante pre : insertsPrecomprobante) {
			EnvioContabilidad.Secuencia secuencia = new EnvioContabilidad.Secuencia();
			secuencia.SecuenciaComprobante = pre.IdPreComprobante;
			entradaEnvioContabilidad.p_SecCmbte.add(secuencia);
		}
		
		if(entradaEnvioContabilidad.p_SecCmbte.size() > 0){
			entradaEnvioContabilidad.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEnvioContabilidad.p_UsaReglasNegocio = "S";
			entradaEnvioContabilidad.p_Destino = "B";
			EnvioContabilidad envioContabilidad = new EnvioContabilidad();
			SalidaEnvioContabilidad salidaEnvioContabilidad = envioContabilidad.logica(con, entradaEnvioContabilidad);
			out.P_OUT_ERRORESCONTADOR += salidaEnvioContabilidad.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaEnvioContabilidad.P_OUT_MENSAJES);
		}
		
		
		log.write("FIN::ReversaPreComprobante");
		return out;
		
	}
	
}
