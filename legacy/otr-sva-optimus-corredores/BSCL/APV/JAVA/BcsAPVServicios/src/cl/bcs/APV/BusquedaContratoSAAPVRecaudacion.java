package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class BusquedaContratoSAAPVRecaudacion implements JavaCall   {

	static final Logger logger = LogManager.getLogger(BusquedaContratoSAAPVRecaudacion.class);
	
	public String SQL_SELECT_CONTRATOS 		= loadResourceToString("cl/bcs/APV/sql/BusquedaContratoSAAPVRecaudacion_SelectContratos.sql");
	public String SQL_SELECT_RECAUDACIONES 	= loadResourceToString("cl/bcs/APV/sql/BusquedaContratoSAAPVRecaudacion_SelectRecaudaciones.sql");
	
	public static class EntradaBusquedaContratoSAAPVRecaudacion extends BcsPojo {
		
		public 	Long	p_IdEmpresa;
		public	Long	p_IdUsuario;
		public 	String	p_IdModulo;
		public  List<Long> p_IdsRecaudacion;

	}			

	public static class SalidaBusquedaContratoSAAPVRecaudacion extends BcsPojo {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<AsignarContratoRecaudacion>	P_OUT_RESULTADO;
		
		public SalidaBusquedaContratoSAAPVRecaudacion(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_RESULTADO = new ArrayList<AsignarContratoRecaudacion>();
		}	
		
	};

	public static class AsignarContratoRecaudacion extends BcsPojo {

		public Long IdRecaudacion;
		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		
	}
	
	public static class Recaudacion extends BcsPojo {
		
		public Long 		IdRecaudacion;
		public Long 		IdRecaudacionCliente;
		public Long 		IdCliente;
		public String 		IdRegimenTributario;
		public String	 	IdEstado;
		public String 		DetalleRecaudacion;
		
	}
	
	public static class ContratoSAAPV extends BcsPojo {
		
		public Long IdCliente;
		public Long IdContratoSAAPV;
		public String TipoContrato;
		public String IdRegimenTributario;
		public String CodigoMedioRecaudacion;
		
		public ContratoSAAPV() {
			
			IdCliente = 0L;
			IdContratoSAAPV = 0L;
			TipoContrato = "";
			IdRegimenTributario = "";
			CodigoMedioRecaudacion = "";
		}

	}

	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaBusquedaContratoSAAPVRecaudacion jin = gson.fromJson(in.toString() ,EntradaBusquedaContratoSAAPVRecaudacion.class);
		SalidaBusquedaContratoSAAPVRecaudacion out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	public SalidaBusquedaContratoSAAPVRecaudacion logica(Connection con, EntradaBusquedaContratoSAAPVRecaudacion in)throws Exception {
		
		logger.info("INICIO::BusquedaContratoSAAPVRecaudacion.logica");
		long inicioServicio = System.currentTimeMillis();
		SalidaBusquedaContratoSAAPVRecaudacion out = new SalidaBusquedaContratoSAAPVRecaudacion();
		
		if ( in.p_IdsRecaudacion.size() == 0 ) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_HAY_RECAUDACIONES", null, con, in.p_IdEmpresa));
			return out;
		}
		
		List<Recaudacion> listadoRecaudaciones 	= obtenerRecaudaciones(con, in.p_IdsRecaudacion);
		
		List<Long> idsClientes = listadoRecaudaciones.stream()
                					.map( e -> e.IdCliente)
                					.collect(Collectors.toList());
		
		List<ContratoSAAPV> listadoContratosSAAPV 	= obtenerContratos(con, idsClientes);
		
		out.P_OUT_RESULTADO.addAll( asignarContratos( con, listadoContratosSAAPV, listadoRecaudaciones) );
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::BusquedaContratoSAAPVRecaudacion.logica", finServicio - inicioServicio);
		return out;
	}
	
	private List<Recaudacion> obtenerRecaudaciones(Connection con, List<Long> recaudaciones) throws SQLException {
		
		logger.debug("INICIO::BusquedaContratoSAAPVRecaudacion.obtenerRecaudaciones");
		long inicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdsRecaudacion", recaudaciones);
	
		List<Recaudacion> listadoRecaudaciones = sql.query(con, SQL_SELECT_RECAUDACIONES, params, Recaudacion.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BusquedaContratoSAAPVRecaudacion.obtenerRecaudaciones", fin - inicio);
		return listadoRecaudaciones;
		
	}
	
	private List<ContratoSAAPV> obtenerContratos(Connection con, List<Long> idsClientes) throws SQLException {

		logger.debug("INICIO::BusquedaContratoSAAPVRecaudacion.obtenerContratos");
		long inicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdsCliente", idsClientes);
	
		List<ContratoSAAPV> listadoContratosSAAPV = sql.query(con, SQL_SELECT_CONTRATOS, params, ContratoSAAPV.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BusquedaContratoSAAPVRecaudacion.obtenerContratos", fin - inicio);
		return listadoContratosSAAPV;
		
	}
	
	private List<AsignarContratoRecaudacion> asignarContratos(Connection con, List<ContratoSAAPV> listadoContratosSAAPV, List<Recaudacion> listadoRecaudaciones) {
		
		logger.debug("INICIO::BusquedaContratoSAAPVRecaudacion.asignarContratos");
		long inicio = System.currentTimeMillis();
	
		List<AsignarContratoRecaudacion> listaContratosAsignados = new ArrayList<AsignarContratoRecaudacion>();
		
		for(Recaudacion recaudacion : listadoRecaudaciones ) {
			
			List<ContratoSAAPV> contratosCliente = new ArrayList<ContratoSAAPV>();
			
			if( recaudacion.IdRegimenTributario.equalsIgnoreCase("APV") || recaudacion.IdRegimenTributario.equalsIgnoreCase("DC") ) {
				
				contratosCliente = listadoContratosSAAPV.stream()
						.filter( 
								item -> item.IdCliente.equals(recaudacion.IdCliente) &&
								item.TipoContrato.equalsIgnoreCase(recaudacion.IdRegimenTributario) &&
								item.CodigoMedioRecaudacion.equalsIgnoreCase(recaudacion.DetalleRecaudacion)
						)
						.collect(Collectors.toList());
				
			} else {
				
				 contratosCliente = listadoContratosSAAPV.stream()
						.filter( 
								item -> item.IdCliente.equals(recaudacion.IdCliente) &&
								item.IdRegimenTributario.equalsIgnoreCase(recaudacion.IdRegimenTributario) &&
								item.CodigoMedioRecaudacion.equalsIgnoreCase(recaudacion.DetalleRecaudacion)
						)
						.collect(Collectors.toList());
				 
			}
			
			ContratoSAAPV contratoCliente = contratosCliente.size() > 1 || contratosCliente.size() == 0 ? new ContratoSAAPV() : contratosCliente.get(0);
			
			AsignarContratoRecaudacion asignarContrato 	= new AsignarContratoRecaudacion();
			asignarContrato.IdContratoSAAPV 			= contratoCliente.IdContratoSAAPV;
			asignarContrato.IdRecaudacion				= recaudacion.IdRecaudacion;
			asignarContrato.IdRecaudacionCliente		= recaudacion.IdRecaudacionCliente;
			listaContratosAsignados.add(asignarContrato);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BusquedaContratoSAAPVRecaudacion.asignarContratos", fin - inicio);
		return listaContratosAsignados;
		
	}


	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				EntradaBusquedaContratoSAAPVRecaudacion Objin = new EntradaBusquedaContratoSAAPVRecaudacion();
				BusquedaContratoSAAPVRecaudacion demo = new BusquedaContratoSAAPVRecaudacion();	

				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";

				List<Long> idsRecaudaciones = new ArrayList<Long>();

				Long recaudacion = 171413L;

				idsRecaudaciones.add(recaudacion);				
				
				Objin.p_IdsRecaudacion = idsRecaudaciones;
								
	 			try { 
	 				SalidaBusquedaContratoSAAPVRecaudacion ObjOut = demo.logica(con, Objin);
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}
