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
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.EntradaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.SalidaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoDocumento;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDocumentoIdentidad implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearDocumentoIdentidad.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearDocumentoIdentidad extends EntradaAdministrarWS {
		public List<TipoDocumento> p_TiposDocumento;
		
		public EntradaCrearDocumentoIdentidad() {
			super();
			this.p_TiposDocumento = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearDocumentoIdentidad jin = gson.fromJson(in.toString(), EntradaCrearDocumentoIdentidad.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::CrearDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_TiposDocumento = asignarTipoPersona(conn,entrada);
		
		for (TipoDocumento direccion : entrada.p_TiposDocumento) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = direccion.IdDocumentoIdentidad;
		    entidadCreada.TipoEntidad = "DOCUMENTO IDENTIDAD";
		    direccion.EsBorrador = "N";
		    
		    if(direccion.TipoEnrolamiento == null || direccion.TipoEnrolamiento.isEmpty()) {
			entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = direccion.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<TipoDocumento> listaDeUno = new ArrayList<>();
		    listaDeUno.add(direccion);
		     
		    CrearDocumentoIdentidadMasivo crearDocumentoIdentidadMasivo = new CrearDocumentoIdentidadMasivo();
		    EntradaCrearDocumentoIdentidadMasivo entradaCrearDocumentoIdentidadMasivo = new EntradaCrearDocumentoIdentidadMasivo();
		    entradaCrearDocumentoIdentidadMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearDocumentoIdentidadMasivo.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearDocumentoIdentidadMasivo.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearDocumentoIdentidadMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearDocumentoIdentidadMasivo.p_DocumentosIdentidad = obtenerDocumentos(listaDeUno);
		    
		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, entradaCrearDocumentoIdentidadMasivo.p_DocumentosIdentidad, DocumentoIdentidad.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
			entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    SalidaCrearDocumentoIdentidadMasivo salidaCrearDocumentoIdentidadMasivo = new SalidaCrearDocumentoIdentidadMasivo();
		    salidaCrearDocumentoIdentidadMasivo = crearDocumentoIdentidadMasivo.logica(conn, entradaCrearDocumentoIdentidadMasivo);
		    if(!salidaCrearDocumentoIdentidadMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadCreada.Mensajes.addAll(salidaCrearDocumentoIdentidadMasivo.P_OUT_MENSAJES);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    if(entidadCreada.Mensajes.isEmpty()) {
			entidadCreada = WebServiceUtils.agregarMensajeOk(entidadCreada, "CREADA");
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
		    }
		}
		
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
			(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
			(sum1, sum2) -> sum1 + sum2);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0 ) {
		    conn.rollback();
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDocumentoIdentidad", finServicio - inicioServicio);
		return salida;
	}



	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearDocumentoIdentidad entrada) throws SQLException {
		logger.debug("INICIO::CrearDocumentoIdentidad.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_TiposDocumento.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidad.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.DocumentoIdentidad> obtenerDocumentos(List<TipoDocumento> tiposDocumentos) {
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
	
	private List<TipoDocumento> asignarTipoPersona(Connection con, EntradaCrearDocumentoIdentidad entrada) throws SQLException{
		logger.debug("INICIO::CrearDocumentoIdentidad.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_TiposDocumento.parallelStream().forEach(tarjeta -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(tarjeta.IdPersona)).findFirst().orElse(new TipoPersona());
			tarjeta.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "DOCUMENTO_IDENTIDAD");
			tarjeta.TipoEnrolamiento = tipo.TipoEnrolamiento;
			tarjeta.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDocumentoIdentidad.asignarTipoPersona", fin - inicio);
		return entrada.p_TiposDocumento;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_TiposDocumento\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdDocumentoIdentidad\": 0,\r\n"
				+ "      \"TipoDocumento\": \"P\",\r\n"
				+ "      \"NumeroDocumento\": \"1234\",\r\n"
				+ "      \"NumeroSerie\": \"123\",\r\n"
				+ "      \"FechaEmision\": \"2020-11-03\",\r\n"
				+ "      \"IdEstado\": \"\",\r\n"
				+ "      \"IdPersona\": 370978\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdDocumentoIdentidad\": 0,\r\n"
				+ "      \"TipoDocumento\": \"TAX\",\r\n"
				+ "      \"NumeroDocumento\": \"1241234\",\r\n"
				+ "      \"FechaEmision\": \"2020-11-03\",\r\n"
				+ "      \"IdEstado\": \"\",\r\n"
				+ "      \"IdPersona\": 1774023\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 9\r\n"
				+ "}";
			
			String jsonReq = "{\r\n"
					+ "  \"p_TiposDocumento\": [\r\n"
					+ "    {\r\n"
//					+ "  	 \"TipoDocumento\": \"TAX\",\r\n"
//					+ "  	 \"NumeroDocumento\": \"123654987\",\r\n"
//					+ "  	 \"NumeroSerie\": \"987\",\r\n"
//					+ "  	 \"FechaEmision\": \"2020-11-03\",\r\n"
//					+ "  	 \"FechaVencimiento\": \"2028-11-03\",\r\n"
					+ "      \"IdPersona\": 1732307\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}";
			
			EntradaCrearDocumentoIdentidad entradaCrearTarjetaBancaria = gson.fromJson(jsonReq, EntradaCrearDocumentoIdentidad.class);
			CrearDocumentoIdentidad crearTarjetaBancaria = new CrearDocumentoIdentidad();		
			System.out.println("ENTRADA:");
			System.out.println(entradaCrearTarjetaBancaria.toString());
			try {
			    SalidaWBS salida= crearTarjetaBancaria.logica(con, entradaCrearTarjetaBancaria);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA:");
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
