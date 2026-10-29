package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearRegistroSolicitudCambio implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearRegistroSolicitudCambio.class);
	
	private final String SQL_INSERT_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_InsertSolicitudCambio.sql");
	private final String SQL_SELECT_INFORMACION_PORTAFOLIO_APV = loadResourceToString("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_SelectInformacionPortafolioAPV.sql");
	private final String SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS = loadResourceToString("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_SelectPortafoliosAPVAsociados.sql");
	
	public static class EntradaCrearRegistroSolicitudCambio extends BcsPojo{
		public List<RegistroSolicitudCambio> p_RegistroSolicitudCambio;
		
		public EntradaCrearRegistroSolicitudCambio() {
			this.p_RegistroSolicitudCambio = new ArrayList<RegistroSolicitudCambio>();
		}
	}
	
	public static class RegistroSolicitudCambio extends BcsPojo{
		public Long IdSolicitudCambio;
		public String IdTipoEntidad;
		public String IdEntidadOrigen;
		public String CampoModificado;
		public String TipoDato;
		public String ValorAntiguo;
		public String ValorNuevo;
		public String IdEstadoAutorizacion;
	}
	
	public static class SalidaCrearRegistroSolicitudCambio extends BcsPojo{
		public List<Secuencia> P_OUT_IDSECUENCIAS;
		
		public SalidaCrearRegistroSolicitudCambio() {
			this.P_OUT_IDSECUENCIAS = new ArrayList<Secuencia>();
		}
	}
	
	public static class Secuencia extends BcsPojo{
		public Long IdSecuencia;
	}
	
	public static class Portafolio extends BcsPojo{
		public Long IdPortafolio;
		public Long IdMatrizAgrupadorAPV;
		public Long IdCliente;
		public Integer IdProductoAgrupador; 
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearRegistroSolicitudCambio jin = gson.fromJson(in.toString(), EntradaCrearRegistroSolicitudCambio.class);
		SalidaCrearRegistroSolicitudCambio out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearRegistroSolicitudCambio logica(Connection con, EntradaCrearRegistroSolicitudCambio entrada) throws Exception {
		logger.info("INICIO::CrearRegistroSolicitudCambio");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearRegistroSolicitudCambio out = new SalidaCrearRegistroSolicitudCambio();
		RegistroSolicitudCambio registroSolicitudCambioPortafolio = obtenerSolicitudCambioPortafolio(con,entrada.p_RegistroSolicitudCambio);
		List<RegistroSolicitudCambio> registrosSolicitudCambioReplicaPortafolioAPV = obtenerRegistrosSolicitudCambioReplicaPortafolioAPV(con,registroSolicitudCambioPortafolio);
		List<RegistroSolicitudCambio> registrosSolicitudCambioInsertar = obtenerSolicitudCambioInsertar(entrada.p_RegistroSolicitudCambio, registrosSolicitudCambioReplicaPortafolioAPV);
		insertarSolicitudCambio(con, registrosSolicitudCambioInsertar);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearRegistroSolicitudCambio", finServicio - inicioServicio);
		return out;
	}
	
	private RegistroSolicitudCambio obtenerSolicitudCambioPortafolio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudCambio) {
		logger.debug("INICIO::CrearRegistroSolicitudCambio.obtenerSolicitudCambioPortafolio");
		long inicio = System.currentTimeMillis();
		RegistroSolicitudCambio registroSolicitudCambioPortafolio = registrosSolicitudCambio.parallelStream().filter(registro->"PORTAFOLIO".equals(registro.IdTipoEntidad)).findFirst().orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRegistroSolicitudCambio.obtenerSolicitudCambioPortafolio", fin - inicio);
		return registroSolicitudCambioPortafolio;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistrosSolicitudCambioReplicaPortafolioAPV(Connection con, RegistroSolicitudCambio registroSolicitudCambioPortafolio) throws Exception {
		logger.debug("INICIO::CrearRegistroSolicitudCambio.obtenerRegistrosSolicitudCambioReplicaPortafolioAPV");
		long inicio = System.currentTimeMillis();
		List<RegistroSolicitudCambio> registrosSolicitudCambioReplicaPortafolioAPV = new ArrayList<RegistroSolicitudCambio>();
		if(registroSolicitudCambioPortafolio != null) {
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdPortafolio", registroSolicitudCambioPortafolio.IdEntidadOrigen);
			SQL sql = new SQL();
			List<Portafolio> portafoliosAPV = sql.query(con, SQL_SELECT_INFORMACION_PORTAFOLIO_APV, parametros, Portafolio.class);
			Portafolio portafolioAPV = portafoliosAPV.parallelStream().findFirst().orElse(null);
			if(portafolioAPV != null) {
				parametros.clear();
				parametros.put("IdCliente", portafolioAPV.IdCliente);
				parametros.put("IdPortafolio", portafolioAPV.IdPortafolio);
				parametros.put("IdProductoAgrupador", portafolioAPV.IdProductoAgrupador);
				List<Portafolio> portafoliosAPVAsociados = sql.query(con, SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS, parametros, Portafolio.class);
				registrosSolicitudCambioReplicaPortafolioAPV = portafoliosAPVAsociados.parallelStream().map(portafolio->{
					RegistroSolicitudCambio registro = new RegistroSolicitudCambio();
					registro.IdSolicitudCambio = registroSolicitudCambioPortafolio.IdSolicitudCambio;
					registro.IdTipoEntidad = registroSolicitudCambioPortafolio.IdTipoEntidad;
					registro.IdEntidadOrigen = String.valueOf(portafolio.IdPortafolio);
					registro.CampoModificado = registroSolicitudCambioPortafolio.CampoModificado;
					registro.TipoDato = registroSolicitudCambioPortafolio.TipoDato;
					registro.ValorAntiguo = registroSolicitudCambioPortafolio.ValorAntiguo;
					registro.ValorNuevo = registroSolicitudCambioPortafolio.ValorNuevo;
					registro.IdEstadoAutorizacion = registroSolicitudCambioPortafolio.IdEstadoAutorizacion;
					return registro;
				}).collect(Collectors.toList());
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRegistroSolicitudCambio.obtenerRegistrosSolicitudCambioReplicaPortafolioAPV", fin - inicio);
		return registrosSolicitudCambioReplicaPortafolioAPV;
	}
	
	private List<RegistroSolicitudCambio> obtenerSolicitudCambioInsertar(List<RegistroSolicitudCambio> registrosSolicitudCambioEntrada, List<RegistroSolicitudCambio> registrosSolicitudCambioReplicaPortafolioAPV){
		logger.debug("INICIO::CrearRegistroSolicitudCambio.obtenerSolicitudCambioInsertar");
		long inicio = System.currentTimeMillis();
		List<RegistroSolicitudCambio> registrosSolicitudCambioInsertar = new ArrayList<RegistroSolicitudCambio>();
		registrosSolicitudCambioInsertar.addAll(registrosSolicitudCambioEntrada);
		registrosSolicitudCambioInsertar.addAll(registrosSolicitudCambioReplicaPortafolioAPV);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRegistroSolicitudCambio.obtenerSolicitudCambioInsertar", fin - inicio);
		return registrosSolicitudCambioInsertar;
	}
	
	private void insertarSolicitudCambio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudCambio) throws Exception {
		logger.debug("INICIO::CrearRegistroSolicitudCambio.insertarSolicitudCambio");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_SOLICITUD_CAMBIO, registrosSolicitudCambio, RegistroSolicitudCambio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRegistroSolicitudCambio.insertarSolicitudCambio", fin - inicio);
	}
	
	public static Long obtenerIdSolicitudCambio(Connection con) throws Exception {
		logger.debug("INICIO::CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idsSolicitudCambio.get(0);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio", fin - inicio);
		return idSolicitudCambio;
	}

}
