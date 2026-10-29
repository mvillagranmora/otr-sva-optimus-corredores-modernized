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
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearFatcaPaisResidencia implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearFatcaPaisResidencia.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearFatcaPaisResidencia extends EntradaAdministrarWS {
		public Long p_IdPersona;
		public List<FatcaPaisResidencia> p_FatcasPaisesResidencias;
		
		public EntradaCrearFatcaPaisResidencia() {
			super();
			this.p_IdPersona = 0L;
			this.p_FatcasPaisesResidencias = new ArrayList<FatcaPaisResidencia>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaCrearFatcaPaisResidencia.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::CrearDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_FatcasPaisesResidencias = asignarTipoPersona(conn,entrada);
		
		for (FatcaPaisResidencia datosPaisResidencia : entrada.p_FatcasPaisesResidencias) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = datosPaisResidencia.IdPersona;
		    entidadCreada.TipoEntidad = "FATCA PAIS RESIDENCIA";
		    datosPaisResidencia.EsBorrador = "N";
		    if(datosPaisResidencia.TipoEnrolamiento == null || datosPaisResidencia.TipoEnrolamiento.isEmpty()) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = datosPaisResidencia.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<FatcaPaisResidencia> listaDeUno = new ArrayList<>();
		    listaDeUno.add(datosPaisResidencia);
		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, FatcaPaisResidencia.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		        
		    
		    cl.bcs.socionegocio.per.CrearFatcaPaisResidencia crearFatcaPaisResidencia = new cl.bcs.socionegocio.per.CrearFatcaPaisResidencia();
		    cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia = new cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia();
		    entradaCrearFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearFatcaPaisResidencia.p_FatcaPaisResidencia = obtenerPaisesResidencias(listaDeUno);
		    
		    cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.SalidaCrearFatcaPaisResidencia salidaCrearFatcaPaisResidencia = new cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.SalidaCrearFatcaPaisResidencia();
		    salidaCrearFatcaPaisResidencia = crearFatcaPaisResidencia.logica(conn, entradaCrearFatcaPaisResidencia);
		    
		    if(!salidaCrearFatcaPaisResidencia.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearFatcaPaisResidencia.P_OUT_MENSAJES);
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
		logger.info("FIN::CrearDireccionFisica", finServicio - inicioServicio);
		return salida;
	}



	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearFatcaPaisResidencia entrada) throws SQLException {
		logger.debug("INICIO::CrearDireccionFisica.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = new ArrayList<Long>();
		idsPersonas.add(entrada.p_IdPersona);
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisica.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia> obtenerPaisesResidencias(List<FatcaPaisResidencia> paisesResidencias) {
	    List<cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia> paisesResidenciasCreacion = paisesResidencias.parallelStream().map(paisResidencia -> {
	    	cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia paisResidenciaCreacion = new cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia();
	    	paisResidenciaCreacion.IdPersona = paisResidencia.IdPersona;
	    	paisResidenciaCreacion.IdPaisResidencia = paisResidencia.IdPaisResidencia;
	    	paisResidenciaCreacion.TIN = paisResidencia.TIN;
	    	paisResidenciaCreacion.Observacion = paisResidencia.Observacion;
	    	paisResidenciaCreacion.IdCausaNoTin = paisResidencia.IdCausaNoTin;
	    	paisResidenciaCreacion.PaisResidenciaPrincipal = "N";
		return paisResidenciaCreacion;
	    }).collect(Collectors.toList());

	    return paisesResidenciasCreacion;
	}
	
	private List<FatcaPaisResidencia> asignarTipoPersona(Connection con, EntradaCrearFatcaPaisResidencia entrada) throws SQLException{
		logger.debug("INICIO::CrearDireccionFisica.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_FatcasPaisesResidencias.parallelStream().forEach(paisResidencia -> {
			paisResidencia.IdPersona = entrada.p_IdPersona;
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(paisResidencia.IdPersona)).findFirst().orElse(new TipoPersona());
			paisResidencia.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "FATCA_PAIS_RESIDENCIA");
			paisResidencia.TipoEnrolamiento = tipo.TipoEnrolamiento;
			paisResidencia.IdCliente = tipo.IdCliente;
			if(paisResidencia.TIN != null && paisResidencia.TIN.isEmpty()) {
				paisResidencia.TieneTin = "N";
			}else {
				paisResidencia.TieneTin = "S";
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisica.asignarTipoPersona", fin - inicio);
		return entrada.p_FatcasPaisesResidencias;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_FatcasPaisesResidencias\": [\r\n"
				+ "    {\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdPersona\": 1732307,\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 4\r\n"
				+ "}";
			
			
			EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia = gson.fromJson(jsonEntrada, EntradaCrearFatcaPaisResidencia.class);
			CrearFatcaPaisResidencia crearFatcaPaisResidencia = new CrearFatcaPaisResidencia();		
			System.out.println("ENTRADA:");
			System.out.println(entradaCrearFatcaPaisResidencia.toString());
			try {
			    SalidaWBS salida= crearFatcaPaisResidencia.logica(con, entradaCrearFatcaPaisResidencia);
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
