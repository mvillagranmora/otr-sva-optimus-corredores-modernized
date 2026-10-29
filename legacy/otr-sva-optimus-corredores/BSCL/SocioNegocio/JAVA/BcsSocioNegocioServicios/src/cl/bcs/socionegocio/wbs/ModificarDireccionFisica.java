package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.enr.EnrolamientoUtils;
import cl.bcs.socionegocio.enr.pojo.Ciudad;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DireccionFisica;
import cl.bcs.socionegocio.wbs.pojo.DivisionPais;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDireccionFisica  implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDireccionFisica.class);
	
	private String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDireccionFisica_SelectDireccionFisica.sql");
	
	public static class EntradaModificarDireccionFisica extends EntradaAdministrarWS {
		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaModificarDireccionFisica(){
			this.p_DireccionesFisicas = new ArrayList<DireccionFisica>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDireccionFisica jin = gson.fromJson(in.toString(), EntradaModificarDireccionFisica.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarDireccionFisica entrada)  throws Exception {
		logger.info("INICIO::ModificarDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<DireccionFisica> datosDireccionesFisicas = obtenerDatosDireccionesFisicas(conn,entrada);
		prepararDireccionesDivisionesPais(conn, entrada);
		prepararDireccionesCiudades(conn,entrada);
		
		for(DireccionFisica direccionFisica : datosDireccionesFisicas) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = direccionFisica.IdDireccionFisica;
		    entidadModificada.TipoEntidad = "DIRECCION FISICA";
		    direccionFisica.EsBorrador = "N";
		    
		    if(direccionFisica.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DireccionFisica> mergeCambios = new ArrayList<>();
		    DireccionFisica nuevosCampos = entrada.p_DireccionesFisicas.parallelStream()
		    		.filter(direccion -> direccion.IdDireccionFisica.equals(direccionFisica.IdDireccionFisica))
		    		.findFirst().orElse(new DireccionFisica());
		    WebServiceUtils.merge(direccionFisica, nuevosCampos);
		    mergeCambios.add(direccionFisica);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, direccionFisica.Tipo, direccionFisica.TipoEnrolamiento, "DIRECCION_FISICA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, DireccionFisica.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DireccionFisica> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarPersonaDireccionFisicaMasivo modificarPersonaDireccionFisicaMasivo = new ModificarPersonaDireccionFisicaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDireccionFisicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
		    if( !salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    if(entidadModificada.Mensajes.isEmpty()) {
				entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		}
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica", finServicio - inicioServicio);
		return salida;
	}
	
	private void prepararDireccionesCiudades(Connection con, EntradaModificarDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::ModificarDireccionFisica.prepararDireccionesCiudades");
		long inicio = System.currentTimeMillis();

		List<Long> ciudadIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdCiudad != null).map(u -> u.IdCiudad).collect(Collectors.toList());
		Map<Long, Ciudad> ciudadPorIdCiudad = EnrolamientoUtils.obtenerCiudadPorIdCiudad(con, ciudadIds);
		
		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {

			Ciudad ciudad = ciudadPorIdCiudad.get(direccionFisica.IdCiudad);
			if (ciudad != null) {
				direccionFisica.IdPais = ciudad.IdPais;
				direccionFisica.UsaDivisionPais = ciudad.UsaDivisionPais;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionFisica.prepararDireccionesCiudades {}", fin - inicio);
	}
	
	private void prepararDireccionesDivisionesPais(Connection con, EntradaModificarDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::ModificarDireccionFisica.prepararDireccionesDivisionesPais");
		long inicio = System.currentTimeMillis();

		List<Long> divisionPaisIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdComuna != null).map(u -> u.IdComuna).collect(Collectors.toList());
		Map<Long, DivisionPais> divisionPaisPorIdDivisionPais = WebServiceUtils.obtenerDivisionPaisPorIdDivisionPais(con, divisionPaisIds);
		
		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {

			DivisionPais divisionPais = divisionPaisPorIdDivisionPais.get(direccionFisica.IdComuna);
			if (divisionPais != null) {
				direccionFisica.CodigoDivisionPais = divisionPais.CodigoDivisionPais;
				direccionFisica.CodigoCompuesto = divisionPais.CodigoDivisionPais;
				direccionFisica.IdCiudad = divisionPais.IdCiudad;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionFisica.prepararDireccionesDivisionesPais {}", fin - inicio);
	}
	
	private List<RegistroCambioMasivo> crearRegistroCambio(List<DireccionFisica> direccionesFisicas) {
		logger.trace("INICIO::ModificarDireccionFisica.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(DireccionFisica direccionFisica : direccionesFisicas) {
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Calle", direccionFisica.Calle));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Numero", direccionFisica.Numero));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Departamento", direccionFisica.Departamento));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Sector", direccionFisica.Sector));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "CodigoPostal", direccionFisica.CodigoPostal));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "IdCiudad", direccionFisica.IdCiudad));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "CodigoProposito", direccionFisica.CodigoProposito));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Comentario", direccionFisica.Comentario));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "IdEstado", direccionFisica.IdEstado));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "IdDivisionPais", direccionFisica.IdComuna));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "CodigoCompuesto", direccionFisica.CodigoCompuesto));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Predeterminado", direccionFisica.Predeterminado));
			
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDireccionFisica.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<DireccionFisica> obtenerDatosDireccionesFisicas(Connection conn, EntradaModificarDireccionFisica entrada) throws Exception {
		logger.debug("INICIO::ModificarDireccionFisica.obtenerDatosDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDirecciones = entrada.p_DireccionesFisicas.stream()
				  .map(direccion -> direccion.IdDireccionFisica)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDireccionesFisicas", idsDirecciones);
		
		SQL sql = new SQL();
		List<DireccionFisica> datosDireccionesFisicas = sql.query(conn, SQL_SELECT_DIRECCION_FISICA, params, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionFisica.obtenerDatosDireccionesFisicas", fin - inicio);
		return datosDireccionesFisicas;
	}
	

	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_DireccionesFisicas\": [\r\n"
//					+ "    {\r\n" 
//					+ "      \"IdDireccionFisica\": 2470304,\r\n" 
//					+ "      \"Calle\": \"LAS MARTUCAS\",\r\n" 
//					+ "      \"Numero\": 654,\r\n" 
//					+ "      \"CodigoProposito\": \"PART\",\r\n" 
//					+ "      \"IdEstado\": \"ACT\"\r\n" 
//					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionFisica\": 2470876,\r\n"
					+ "		 \"Calle\": \"\",\r\n"
					+ "		 \"CodigoPostal\": \"\",\r\n"
					+ "		 \"CodigoProposito\": \"\",\r\n"
					+ "		 \"CodigoDivisionPais\": \"\",\r\n"
					+ "		 \"Comentario\": \"\",\r\n"
					+ "		 \"Departamento\": \"\",\r\n"
					+ "		 \"IdCiudad\": 0,\r\n"
					+ "		 \"Numero\": \"\",\r\n"
					+ "		 \"Sector\": \"\",\r\n"
					+ "		 \"IdDivisionPais\": 0,\r\n"
					+ "		 \"Predeterminado\": \"\",\r\n"
					+ "		 \"IdEstado\": \"\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			
			String jsonEntradaFullJur = "{\r\n"
					+ "  \"p_DireccionesFisicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionFisica\": 2438884,\r\n"
					+ "		 \"Calle\": \"\",\r\n"
					+ "		 \"CodigoPostal\": \"\",\r\n"
					+ "		 \"CodigoProposito\": \"\",\r\n"
					+ "		 \"CodigoDivisionPais\": \"\",\r\n"
					+ "		 \"Comentario\": \"\",\r\n"
					+ "		 \"Departamento\": \"\",\r\n"
					+ "		 \"Numero\": \"\",\r\n"
					+ "		 \"Sector\": \"\",\r\n"
					+ "		 \"IdDivisionPais\": 112,\r\n"
					+ "		 \"Predeterminado\": \"\",\r\n"
					+ "		 \"IdEstado\": \"\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarDireccionFisica entradaModificarDireccionFisica = gson.fromJson(jsonEntradaFullJur, EntradaModificarDireccionFisica.class);
			ModificarDireccionFisica modificarDireccionFisica = new ModificarDireccionFisica();				
			try {
				SalidaWBS salida= modificarDireccionFisica.logica(con, entradaModificarDireccionFisica);
				//con.commit();
				con.rollback();
				con.close();
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
