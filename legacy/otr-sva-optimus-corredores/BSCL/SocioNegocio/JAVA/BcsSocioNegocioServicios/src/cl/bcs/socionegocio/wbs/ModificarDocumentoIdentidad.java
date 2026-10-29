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
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoDocumento;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDocumentoIdentidad implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDocumentoIdentidad.class);
	
	private String SQL_SELECT_DOCUMENTO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDocumentoIdentidad_SelectDocumentoIdentidad.sql");
	
	public static class EntradaModificarDocumentoIdentidad extends EntradaAdministrarWS {
		public List<TipoDocumento> p_DocumentosIdentidad;
		
		public EntradaModificarDocumentoIdentidad(){
			this.p_DocumentosIdentidad = new ArrayList<TipoDocumento>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDocumentoIdentidad jin = gson.fromJson(in.toString(), EntradaModificarDocumentoIdentidad.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::ModificarDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		List<TipoDocumento> datosDocumentoIdentidad = obtenerDatosDocumentoIdentidad(conn,entrada);

		for(TipoDocumento documentoIdentidad : datosDocumentoIdentidad) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = documentoIdentidad.IdDocumentoIdentidad;
		    entidadModificada.TipoEntidad = "DOCUMENTO IDENTIDAD";
		    documentoIdentidad.EsBorrador = "N";
		    
		    if(documentoIdentidad.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<TipoDocumento> mergeCambios = new ArrayList<>();
		    TipoDocumento nuevosCampos = entrada.p_DocumentosIdentidad.parallelStream()
		    		.filter(documento -> documento.IdDocumentoIdentidad.equals(documentoIdentidad.IdDocumentoIdentidad))
		    		.findFirst().orElse(new TipoDocumento());
		    WebServiceUtils.merge(documentoIdentidad, nuevosCampos);
		    mergeCambios.add(documentoIdentidad);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, documentoIdentidad.Tipo, documentoIdentidad.TipoEnrolamiento, "DOCUMENTO_IDENTIDAD");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    List<DocumentoIdentidad> entidadValidar = obtenerEntidadDocumento(mergeCambios);
		    out = entrada.validarCamposObligatoriosEntrada(conn, entidadValidar, DocumentoIdentidad.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<TipoDocumento> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarPersonaDocumentoIdentidadMasivo modificarPersonaDocumentoIdentidad = new ModificarPersonaDocumentoIdentidadMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDocumentoIdentidad.logica(conn, entradaEjecutarCambiosPersona);
		    
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
		logger.info("FIN::ModificarDocumentoIdentidad", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<TipoDocumento> documentosIdentidad) {
		logger.trace("INICIO::ModificarDocumentoIdentidad.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(TipoDocumento documento : documentosIdentidad) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(documento.IdDocumentoIdentidad, "NumeroSerie", documento.NumeroSerie));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(documento.IdDocumentoIdentidad, "FechaEmision", documento.FechaEmision));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(documento.IdDocumentoIdentidad, "FechaVencimiento", documento.FechaVencimiento));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(documento.IdDocumentoIdentidad, "IdEstado", documento.IdEstado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDocumentoIdentidad.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<TipoDocumento> obtenerDatosDocumentoIdentidad(Connection conn, EntradaModificarDocumentoIdentidad entrada) throws Exception {
		logger.debug("INICIO::ModificarDocumentoIdentidad.obtenerDatosDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDocumentos = entrada.p_DocumentosIdentidad.stream()
				  .map(documento -> documento.IdDocumentoIdentidad)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDocumentoIdentidad", idsDocumentos);
		
		SQL sql = new SQL();
		List<TipoDocumento> datosDocumentosIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTO, params, TipoDocumento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDocumentoIdentidad.obtenerDatosDocumentoIdentidad", fin - inicio);
		return datosDocumentosIdentidad;
	}
	
	private List<DocumentoIdentidad> obtenerEntidadDocumento(List<TipoDocumento> tiposDocumentos) {
	    List<cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad> documentosCreacion = tiposDocumentos.parallelStream().map(documento -> {
		cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad documentoCreacion = new cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad();
		documentoCreacion.IdInterno = documento.IdPersona;
		documentoCreacion.IdDocumentoIdentidad = documento.IdDocumentoIdentidad;
		documentoCreacion.IdTipoDocumentoIdentidad = documento.TipoDocumento;
		documentoCreacion.DocumentoIdentidad = documento.NumeroDocumento;
		documentoCreacion.NumeroSerie = documento.NumeroSerie;
		documentoCreacion.FechaEmision = documento.FechaEmision;
		documentoCreacion.FechaVencimiento = documento.FechaVencimiento;
		documentoCreacion.IdEstado = documento.IdEstado;
		documentoCreacion.IdPersona = documento.IdPersona;
		documentoCreacion.Predeterminado = documento.Predeterminado;
		documentoCreacion.IdTipoDocumentoIdentidadAsociado = documento.DocumentoIdentidadAsociado;
		
		return documentoCreacion;
	    }).collect(Collectors.toList());

	    return documentosCreacion;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_DocumentosIdentidad\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdDocumentoIdentidad\": 1726482,\r\n"
				+ "      \"NumeroSerie\": \"5678\",\r\n"
				+ "      \"FechaEmision\": \"2019-11-03\",\r\n"
				+ "      \"IdPersona\": 1774023\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdDocumentoIdentidad\": 1745295,\r\n"
				+ "      \"NumeroSerie\": \"5478\",\r\n"
				+ "      \"FechaEmision\": \"2019-11-03\",\r\n"
				+ "      \"IdPersona\": 1774023\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 9\r\n"
				+ "}";
			
			String jsonReq = "{\r\n"
					+ "  \"p_DocumentosIdentidad\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdDocumentoIdentidad\": 1684634,\r\n"
//					+ "  	 \"NumeroSerie\": \"987\",\r\n"
//					+ "      \"FechaEmision\": \"2019-11-03\",\r\n"
//					+ "  	 \"FechaVencimiento\": \"2028-11-03\",\r\n"
					+ "      \"IdPersona\": 1732307\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaModificarDocumentoIdentidad entradaModificarDocumentoIdentidad = gson.fromJson(jsonReq, EntradaModificarDocumentoIdentidad.class);
			ModificarDocumentoIdentidad ModificarDocumentoIdentidad = new ModificarDocumentoIdentidad();				
			try {
				SalidaWBS salida= ModificarDocumentoIdentidad.logica(con, entradaModificarDocumentoIdentidad);
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
