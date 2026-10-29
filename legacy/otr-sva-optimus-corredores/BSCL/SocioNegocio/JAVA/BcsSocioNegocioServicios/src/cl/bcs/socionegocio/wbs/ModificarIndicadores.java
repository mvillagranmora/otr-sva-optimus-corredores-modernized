package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.demo.ServiceDemo.Coleccion;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo;
import cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.Indicador;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarIndicadores implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarIndicadores.class);
	
	private String SQL_SELECT_INDICADORES_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarIndicadores_SelectIndicadoresPersona.sql");
	private String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarIndicadores_SelectIndicadoresCliente.sql");
	private String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarIndicadores_SelectIndicadoresPortafolio.sql");
	private String SQL_SELECT_DATOS_PERSONA	= "select top 1 \"Tipo\" AS \"TipoPersona\" , \"TipoEnrolamiento\" , \"IdCliente\" FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" WHERE \"IdPersona\" = :IdPersona";
	private String SQL_SELECT_INDICADOR = "SELECT IND.\"IdIndicador\" , IND.\"IdTipoIndicador\" FROM \"BSCL.Plataforma.BD::IND.Indicador\" AS IND LEFT JOIN BSCL.\"BSCL.Plataforma.BD::IND.TipoIndicador\" AS TIND ON TIND.\"IdTipoIndicador\" = IND.\"IdTipoIndicador\" WHERE IND.\"IdTipoEntidad\" = :IdTipoEntidad AND IND.\"IdEntidad\" = :IdEntidad AND TIND.\"CodigoTipoIndicador\"  = :CodigoIndicador ;";

	
	public static class EntradaModificarIndicador extends EntradaAdministrarWS {
		public List<Indicador> p_Indicadores;
		public String p_Formulario;
		public Object p_IdUsoIndicador;
		
		public EntradaModificarIndicador(){
			this.p_Indicadores = new ArrayList<Indicador>();
			this.p_Formulario = "INDICADORES_CLIENTE";	
			this.p_IdUsoIndicador = "IND";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarIndicador jin = gson.fromJson(in.toString(), EntradaModificarIndicador.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarIndicador entrada) throws Exception {
		logger.info("INICIO::ModificarIndicadores");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<Indicador> indicadores = obtenerIndicadores(conn,entrada);
		Map<String, Indicador> entidadValidar = new HashMap<>();

		if(("INDICADORES_PERSONA").equals(entrada.p_Formulario) || ("INDICADORES_CLIENTE").equals(entrada.p_Formulario)) {
		
			List<Long> idsPersonas = entrada.p_Indicadores.parallelStream()
					  .map(indicador -> indicador.IdPersona)
					  .distinct()
					  .collect(Collectors.toList());
			
			for(Long idPersona : idsPersonas) {
				SalidaAdministrarWS out = new SalidaAdministrarWS();
				EntidadManipulada entidadModificada = new EntidadManipulada();			
			
		    	 entidadModificada.IdEntidad = 0L;
			     entidadModificada.TipoEntidad = "INDICADORES_PERSONA".equals(entrada.p_Formulario)? "INDICADOR PERSONA": "INDICADOR CLIENTE";
			    
			    
				List<Indicador> indicadoresEntradaPorPersona = entrada.p_Indicadores.parallelStream().filter(indFiltro -> idPersona.equals(indFiltro.IdPersona) ).collect(Collectors.toList());
			    List<Indicador> indicadoresPorPersona = indicadores.parallelStream().filter(indFiltro -> idPersona.equals(indFiltro.IdPersona) ).collect(Collectors.toList());
	    
			    for(Indicador indicador : indicadoresPorPersona) {
			    	indicadoresEntradaPorPersona.parallelStream().forEach(indEnt -> {
			    		if(indicador.CodigoTipoIndicador.equals(indEnt.CodigoTipoIndicador)) {
			    			indicador.ValorIndicador = indEnt.ValorIndicador;
			    			entidadValidar.put(indicador.CodigoTipoIndicador, indicador);
			    		}
			    	});
			    	if(indicador.IdIndicador != 0 && !entidadValidar.containsKey(indicador.CodigoTipoIndicador)) {
						entidadValidar.put(indicador.CodigoTipoIndicador, indicador);
					}
			    }
			    
			    
				String tipoPersona 		= "";
				String tipoEnrolamiento = "";
				String modeloCliente    = "";
	
				try {
					tipoPersona 		= indicadoresPorPersona.parallelStream().filter(ind->ind.IdPersona.equals(idPersona)).findFirst().get().TipoPersona;
					tipoEnrolamiento 	= indicadoresPorPersona.parallelStream().filter(ind->ind.IdPersona.equals(idPersona)).findFirst().get().TipoEnrolamiento;
					modeloCliente 		= indicadoresPorPersona.parallelStream().filter(ind->ind.IdPersona.equals(idPersona)).findFirst().get().ModeloCliente;
				}catch(Exception e) {}
				
				SQL sql = new SQL();
			    HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdPersona", entrada.p_Indicadores.get(0).IdPersona);
				Indicador datosIndicadorPersona = sql.query(conn, SQL_SELECT_DATOS_PERSONA , params, Indicador.class).get(0);
			
	
				Indicador indicadorPersonaCliente = new Indicador();
				params.clear();
				
				Long idEntidad 						= "INDICADORES_PERSONA".equalsIgnoreCase(entrada.p_Formulario) 	? entrada.p_Indicadores.get(0).IdPersona : datosIndicadorPersona.IdCliente;
		        String idTipoIdentidad				= "INDICADORES_PERSONA".equalsIgnoreCase(entrada.p_Formulario)  ? "PERSONA" : "CLIENTE";
		        
		        params.put ("IdEntidad"		, idEntidad );
		        params.put ("IdTipoEntidad"	, idTipoIdentidad);
		        
				for (Indicador Indicadores : indicadoresEntradaPorPersona) {
					// No se actualiza el id indicador e id tipo indicador en base el codigo indicador si.. no se recibvio codigotipo indicador o se recivio id indicador e idtipoindicador
					if ( Indicadores.CodigoTipoIndicador == null || Indicadores.CodigoTipoIndicador.trim().isEmpty() ) { continue ; }
					if ( (Indicadores.IdIndicador != null && Indicadores.IdIndicador > 0) && (Indicadores.IdTipoIndicador != null && Indicadores.IdTipoIndicador > 0 ) ) { continue; }	
					try {
						params.put ("CodigoIndicador", Indicadores.CodigoTipoIndicador);
						indicadorPersonaCliente = sql.query(conn, SQL_SELECT_INDICADOR , params, Indicador.class).get(0);
						Indicadores.IdIndicador = indicadorPersonaCliente.IdIndicador;
						Indicadores.IdTipoIndicador = indicadorPersonaCliente.IdTipoIndicador;
					}catch (Exception e) { 
						e.printStackTrace();
						System.out.println("ERROR - " + e);
						MensajeSalida mensaje = new MensajeSalida();
						mensaje.id = 0L;
						mensaje.tipo 		= PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
						mensaje.descripcion = Indicadores.toString();
						mensaje.codigo 		= "ERROR_INDICADOR" ;
						out.P_OUT_MENSAJES.add( mensaje );
					}
				}				
			    
				if (tipoPersona.isEmpty()) { tipoPersona = datosIndicadorPersona.TipoPersona; }
				if (tipoEnrolamiento.isEmpty()) { tipoEnrolamiento = datosIndicadorPersona.TipoEnrolamiento; }
				
				FormularioCanal formulario = new FormularioCanal();
			    formulario.IdEmpresa = entrada.p_IdEmpresa;
			    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, tipoPersona, tipoEnrolamiento, entrada.p_Formulario);
			    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
	
			    try {
					out = entrada.validarCamposObligatoriosEntradaIndicadores(conn, entidadValidar, Indicador.class, formulario, out);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    if(out.P_OUT_ERRORESCONTADOR > 0) {
					entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
					continue;
			    }
			    
			    ModificarIndicador modificarIndicador = new ModificarIndicador();
			    cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador entradaModificarIndicador = new cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador();
			    entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			    entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			    entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			    entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			    
			
		    	entradaModificarIndicador.p_IdEntidad = idPersona;
			    entradaModificarIndicador.p_IdTipoEntidad = "INDICADORES_PERSONA".equals(entrada.p_Formulario)? "PERSONA": "CLIENTE" ;
			   
			    
			    entradaModificarIndicador.p_ModeloCliente = modeloCliente == null ? "" : modeloCliente;
			    entradaModificarIndicador.p_RegistroAtributo= crearRegistroAtributo(indicadoresEntradaPorPersona);
			    
			    SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(conn, entradaModificarIndicador);
	
			    if( !salidaModificarIndicador.P_OUT_MENSAJES.isEmpty() ) {
					entidadModificada.Mensajes.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
					continue;
			    }
			    
			    if(entidadModificada.Mensajes.isEmpty()) {
					entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
			    }
			}
		
		}
		else {
			Set<Long> IdsPortafoliosSet = entrada.p_Indicadores.stream()
					.filter(indicador ->  indicador.IdPortafolio > 0L)
					.map( indicador -> indicador.IdPortafolio)
					.collect(Collectors.toSet());
			
			for(Long idPortafolio : IdsPortafoliosSet) {
				
				SalidaAdministrarWS out = new SalidaAdministrarWS();
				EntidadManipulada entidadModificada = new EntidadManipulada();
				entidadModificada.IdEntidad = idPortafolio;
				entidadModificada.TipoEntidad = "PORTAFOLIO";
				
				List<Indicador> indicadoresEntradaPortafolio = entrada.p_Indicadores.stream().filter(indFiltroPortafolio -> idPortafolio.equals(indFiltroPortafolio.IdPortafolio)).collect(Collectors.toList());
				List<Indicador> indicadoresPorIdPortfalio = indicadores.stream().filter(indFiltroPortafolio -> idPortafolio.equals(indFiltroPortafolio.IdPortafolio)).collect(Collectors.toList());
				
 				for(Indicador indicador : indicadoresPorIdPortfalio) {
					
					indicadoresEntradaPortafolio.parallelStream().forEach(indEnt -> {
			    		if(indicador.CodigoTipoIndicador.equals(indEnt.CodigoTipoIndicador)) {
			    			indicador.ValorIndicador = indEnt.ValorIndicador;
			    			entidadValidar.put(indicador.CodigoTipoIndicador, indicador);
			    			
			    		}
			    	});
					if(indicador.IdIndicador != 0L && !entidadValidar.containsKey(indicador.CodigoTipoIndicador)) {
						entidadValidar.put(indicador.CodigoTipoIndicador, indicador);
					}
				}
 				
 				for(Indicador indicador : indicadoresPorIdPortfalio) {
 					for(Indicador indicadorEntrada : indicadoresEntradaPortafolio ) {
 						if(indicador.CodigoTipoIndicador.equals(indicadorEntrada.CodigoTipoIndicador) && indicador.IdPortafolio.equals(indicadorEntrada.IdPortafolio)) {
 							indicadorEntrada.IdTipoIndicador = indicador.IdTipoIndicador;
 							indicadorEntrada.IdIndicador = indicador.IdIndicador;
 						}
 					}
 				}
				
				String tipoPersona = indicadoresPorIdPortfalio.stream().filter(ind -> ind.IdPortafolio.equals(idPortafolio)).findFirst().get().TipoPersona;
				String tipoEnrolamiento = indicadoresPorIdPortfalio.stream().filter(ind -> ind.IdPortafolio.equals(idPortafolio)).findFirst().get().TipoEnrolamiento;
				String modeloCliente = indicadoresPorIdPortfalio.stream().filter(ind -> ind.IdPortafolio.equals(idPortafolio)).findFirst().get().ModeloCliente;
				
				FormularioCanal formulario = new FormularioCanal();
			    formulario.IdEmpresa = entrada.p_IdEmpresa;
			    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, tipoPersona, tipoEnrolamiento, entrada.p_Formulario);
			    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
			    
			    out = entrada.validarCamposObligatoriosEntradaIndicadores(conn, entidadValidar, Indicador.class, formulario, out);
			    
			    if(out.P_OUT_ERRORESCONTADOR > 0) {
					entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
					continue;
			    }
			    
			    ModificarIndicador modificarIndicador = new ModificarIndicador();
			    ModificarIndicador.EntradaModificarIndicador entradaModificarIndicador = new ModificarIndicador.EntradaModificarIndicador();
			    entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			    entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			    entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			    entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			    entradaModificarIndicador.p_IdEntidad = idPortafolio;
			    entradaModificarIndicador.p_IdTipoEntidad = "PORTAFOLIO";
			    entradaModificarIndicador.p_ModeloCliente = modeloCliente == null ? "" : modeloCliente;
			    entradaModificarIndicador.p_RegistroAtributo= crearRegistroAtributo(indicadoresEntradaPortafolio);
			    
			    SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(conn, entradaModificarIndicador);
			    
			    if( !salidaModificarIndicador.P_OUT_MENSAJES.isEmpty() ) {
					entidadModificada.Mensajes.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
					continue;
			    }
			    
			    if(entidadModificada.Mensajes.isEmpty()) {
					entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
			    }								
			}
		}
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIndicadores", finServicio - inicioServicio);
		return salida;
		
	}
	

	private EntidadManipulada noExisteIndicador(EntidadManipulada entidadModificada) {
		logger.trace("INICIO::ModificarIndicadores.noExisteIndicador");
		long inicio = System.currentTimeMillis();
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.id = 0L;
		mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.descripcion = "ERROR_INDICADOR_NO_ENCONTRADO";
		mensaje.codigo = "ERROR_INDICADOR_NO_ENCONTRADO";
		entidadModificada.Mensajes.add(mensaje);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarIndicadores.noExisteIndicador", fin - inicio);
	
		return entidadModificada;
	}

	private List<RegistroAtributo> crearRegistroAtributo(List<Indicador> indicadores) {
		logger.trace("INICIO::ModificarIndicadores.crearRegistroAtributo");
		long inicio = System.currentTimeMillis();
		
		List<RegistroAtributo> registrosCambio = indicadores.parallelStream().map(indicador->{
		    RegistroAtributo registroAtributo = new RegistroAtributo();
		    	registroAtributo.IdIndicador = indicador.IdIndicador;
		    	registroAtributo.IdTipoIndicador = indicador.IdTipoIndicador;
		    	registroAtributo.ValorIndicador = indicador.ValorIndicador;
		    	registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
		    	return registroAtributo;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarIndicadores.crearRegistroAtributo", fin - inicio);
		return registrosCambio;
	}

	private List<Indicador> obtenerIndicadores(Connection con, EntradaModificarIndicador entrada) throws Exception {
		logger.debug("INICIO::ModificarIndicadores.obtenerIndicadores");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_Indicadores.parallelStream()
				  .map(indicador -> indicador.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());

		HashMap<String, Object> params = new HashMap<String, Object>(8);
		params.put("IdPersonas", idsPersonas);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdUso", entrada.p_IdUsoIndicador);
		SQL sql = new SQL();
		List<Indicador> datosIndicadores;
		if("INDICADORES_PORTAFOLIO".equals(entrada.p_Formulario)) {
			HashMap<String, Object> paramsp = new HashMap<String, Object>(8);
			paramsp.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsp.put("IdUso", entrada.p_IdUsoIndicador);
			paramsp.put("IdPortafolio", entrada.p_Indicadores.stream().map(indicador -> indicador.IdPortafolio).distinct().collect(Collectors.toList()));
			datosIndicadores = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsp, Indicador.class);
		}
		else if("INDICADORES_PERSONA".equalsIgnoreCase(entrada.p_Formulario)) {
		    datosIndicadores = sql.query(con, SQL_SELECT_INDICADORES_PERSONA, params, Indicador.class);
		}
		else if("INDICADORES_CLIENTE".equalsIgnoreCase(entrada.p_Formulario)) {
		    datosIndicadores = sql.query(con, SQL_SELECT_INDICADORES_CLIENTE, params, Indicador.class);
		}
		else {
			datosIndicadores = new ArrayList<Indicador>();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicadores.obtenerIndicadores", fin - inicio);
		return datosIndicadores;
	}

	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada =  "{\"p_Indicadores\":[{\"IdInterno\":null,\"IdIndicador\":null,\"IdTipoEntidad\":null,\"IdTipoIndicador\":null,\"ModeloCliente\":null,\"IdEmpresa\":null,\"ValorIndicador\":\"4\",\"IdIndicadorPersona\":null,\"CodigoTipoIndicador\":\"NCG380\",\"IdPersona\":307717,\"IdCliente\":null,\"IdEnrolamiento\":null,\"TipoEnrolamiento\":\"FULL\",\"TipoPersona\":\"N\",\"EsBorrador\":null,\"IdPortafolio\":null}],\"p_Formulario\":\"INDICADORES_CLIENTE\",\"p_IdUsoIndicador\":\"IND\",\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"WBS\"}";
			EntradaModificarIndicador entradaModificarDatosGenerales = gson.fromJson(jsonEntrada, EntradaModificarIndicador.class);
			ModificarIndicadores modificarDatosGenerales = new ModificarIndicadores();				
			try {
				SalidaWBS salida= modificarDatosGenerales.logica(con, entradaModificarDatosGenerales);
				con.rollback();		 
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
	
}