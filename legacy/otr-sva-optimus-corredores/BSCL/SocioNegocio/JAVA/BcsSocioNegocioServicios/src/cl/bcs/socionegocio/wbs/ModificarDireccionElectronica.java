package cl.bcs.socionegocio.wbs;

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

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DireccionElectronica;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDireccionElectronica  implements JavaCall {
static final Logger logger = LogManager.getLogger(ModificarDireccionElectronica.class);
	
	private String SQL_SELECT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDireccionElectronica_SelectDireccionElectronica.sql");
	
	public static class EntradaModificarDireccionElectronica extends EntradaAdministrarWS {
		public List<DireccionElectronica> p_DireccionesElectronicas;
		
		public EntradaModificarDireccionElectronica(){
			this.p_DireccionesElectronicas = new ArrayList<DireccionElectronica>();
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDireccionElectronica jin = gson.fromJson(in.toString(), EntradaModificarDireccionElectronica.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarDireccionElectronica entrada)  throws Exception {
		logger.info("INICIO::ModificarDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<DireccionElectronica> datosDireccionesElectronicas = obtenerDatosDireccionesElectronicas(conn,entrada);

		for(DireccionElectronica direccionElectronica : datosDireccionesElectronicas) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
			SalidaAdministrarWS outValidacionPredeterminacion = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = direccionElectronica.IdDireccionElectronica;
		    entidadModificada.TipoEntidad = "DIRECCION ELECTRONICA";
		    direccionElectronica.EsBorrador = "N";
		    
		    if(direccionElectronica.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DireccionElectronica> mergeCambios = new ArrayList<>();
		    DireccionElectronica nuevosCampos = entrada.p_DireccionesElectronicas.parallelStream()
		    		.filter(direccion -> direccion.IdDireccionElectronica.equals(direccionElectronica.IdDireccionElectronica))
		    		.findFirst().orElse(new DireccionElectronica());
		    
		    outValidacionPredeterminacion = validarPredeterminarDireccionInactiva(direccionElectronica, nuevosCampos, conn, entidadModificada, entrada);
		    
		    WebServiceUtils.merge(direccionElectronica, nuevosCampos);
		    mergeCambios.add(direccionElectronica);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, direccionElectronica.Tipo, direccionElectronica.TipoEnrolamiento, "DIRECCION_ELECTRONICA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, DireccionElectronica.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    if (outValidacionPredeterminacion.P_OUT_ERRORESCONTADOR > 0) {		    	

		    	salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);		    	
				continue;
		    }
		    
		    List<DireccionElectronica> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarPersonaDireccionElectronicaMasivo modificarPersonaDireccionElectronicaMasivo = new ModificarPersonaDireccionElectronicaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDireccionElectronicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
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
		logger.info("FIN::ModificarDireccionElectronica", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaAdministrarWS validarPredeterminarDireccionInactiva(DireccionElectronica direccionElectronica,
			DireccionElectronica nuevosCampos, Connection conn, EntidadManipulada entidadModificada, EntradaModificarDireccionElectronica entrada) throws Exception {
		logger.debug("INICIO::ModificarDireccionElectronica.validarPredeterminarDireccionInactiva");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarWS salidaValidacion = new SalidaAdministrarWS();
	    if(nuevosCampos.IdEstado != null && nuevosCampos.IdEstado.equals("INA")) {		    		    		
	    	if (nuevosCampos.Predeterminado.equals("S")) {
	    		salidaValidacion.P_OUT_ERRORESCONTADOR++;
	    		salidaValidacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CORREO_INACTIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));		    	
	    		entidadModificada.Mensajes.add(salidaValidacion.P_OUT_MENSAJES.get(0));	    		
	    	}
	    }
	    long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionElectronica.validarPredeterminarDireccionInactiva", fin - inicio);
	    return salidaValidacion;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<DireccionElectronica> direccionesElectronicas) {
		logger.trace("INICIO::ModificarDireccionElectronica.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(DireccionElectronica direccion : direccionesElectronicas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccion.IdDireccionElectronica, "Direccion", direccion.Direccion));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccion.IdDireccionElectronica, "CodigoProposito", direccion.CodigoProposito));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccion.IdDireccionElectronica, "Comentario", direccion.Comentario));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccion.IdDireccionElectronica, "IdEstado", direccion.IdEstado));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccion.IdDireccionElectronica, "Predeterminado", direccion.Predeterminado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDireccionElectronica.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<DireccionElectronica> obtenerDatosDireccionesElectronicas(Connection conn, EntradaModificarDireccionElectronica entrada) throws Exception {
		logger.debug("INICIO::ModificarDireccionElectronica.obtenerDatosDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDirecciones = entrada.p_DireccionesElectronicas.stream()
				  .map(direccion -> direccion.IdDireccionElectronica)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDireccionesElectronicas", idsDirecciones);
		
		SQL sql = new SQL();
		List<DireccionElectronica> datosDireccionesElectronicas = sql.query(conn, SQL_SELECT_DIRECCION_ELECTRONICA, params, DireccionElectronica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionElectronica.obtenerDatosDireccionesElectronicas", fin - inicio);
		return datosDireccionesElectronicas;
	}
	

	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_DireccionesElectronicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionElectronica\": 1937769,\r\n"
					+ "      \"Direccion\": \"\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionElectronica\": 1937769,\r\n"
					+ "      \"Direccion\": \"RICKANDMORTY@RICKKANDMORTY.COM\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"CodigoProposito\": \"PART\",\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionElectronica\": 1937769,\r\n"
					+ "      \"Direccion\": \"SUMMERSMITH@RICKKANDMORTY.COM\",\r\n"
					+ "      \"Comentario\": \"NO\",\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			String jsonEntradaFullJur = "{\r\n"
					+ "  \"p_DireccionesElectronicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdDireccionElectronica\": 1919144,\r\n"
					+ "      \"Direccion\": \"\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarDireccionElectronica entradaModificarDireccionElectronica = gson.fromJson(jsonEntradaFullJur, EntradaModificarDireccionElectronica.class);
			ModificarDireccionElectronica modificarDireccionElectronica = new ModificarDireccionElectronica();				
			try {
				SalidaWBS salida= modificarDireccionElectronica.logica(con, entradaModificarDireccionElectronica);
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
