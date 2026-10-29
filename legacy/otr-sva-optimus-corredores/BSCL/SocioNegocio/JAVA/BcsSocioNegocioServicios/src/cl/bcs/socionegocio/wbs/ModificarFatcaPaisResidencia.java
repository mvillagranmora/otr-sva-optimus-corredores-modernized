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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarFatcaPaisResidencia implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarFatcaPaisResidencia.class);
	
	private String SQL_SELECT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarFatcaPaisResidencia_SelectFatcaPaisResidencia.sql");
	
	public static class EntradaModificarFatcaPaisResidencia extends EntradaAdministrarWS {
		public Long p_IdPersona;
		public List<FatcaPaisResidencia> p_FatcasPaisesResidencias;
		
		public EntradaModificarFatcaPaisResidencia(){
			this.p_IdPersona = 0L;
			this.p_FatcasPaisesResidencias = new ArrayList<FatcaPaisResidencia>();
		}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaModificarFatcaPaisResidencia.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarFatcaPaisResidencia entrada)  throws Exception {
		logger.info("INICIO::ModificarFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<FatcaPaisResidencia> datosFatcasPaises = obtenerDatosFatcas(conn,entrada);
		
		for(FatcaPaisResidencia datosFatcaPais : datosFatcasPaises) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
			entidadModificada.IdEntidad = datosFatcaPais.IdPaisResidencia;
		    entidadModificada.TipoEntidad = "FATCA PAIS RESIDENCIA";
		    datosFatcaPais.EsBorrador = "N";
		    
		    if(datosFatcaPais.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<FatcaPaisResidencia> mergeCambios = new ArrayList<>();
		    FatcaPaisResidencia nuevosCampos = entrada.p_FatcasPaisesResidencias.parallelStream()
		    		.filter(paisResidencia -> paisResidencia.IdPaisResidencia.equals(datosFatcaPais.IdPaisResidencia))
		    		.findFirst().orElse(new FatcaPaisResidencia());
		    WebServiceUtils.merge(datosFatcaPais, nuevosCampos);
		    mergeCambios.add(datosFatcaPais);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, datosFatcaPais.Tipo, datosFatcaPais.TipoEnrolamiento, "FATCA_PAIS_RESIDENCIA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, FatcaPaisResidencia.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia> listaDeUno =  new ArrayList<>();
		    cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia entidadPaisResidencia = new cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia();
		    entidadPaisResidencia.IdPersona = datosFatcaPais.IdPersona;
		    entidadPaisResidencia.IdPaisResidencia = datosFatcaPais.IdPaisResidencia;
    		entidadPaisResidencia.TIN = datosFatcaPais.TIN;
			entidadPaisResidencia.Observacion = datosFatcaPais.Observacion;
			entidadPaisResidencia.IdCausaNoTin = datosFatcaPais.IdCausaNoTin;
			entidadPaisResidencia.PaisResidenciaPrincipal = datosFatcaPais.PaisResidenciaPrincipal;
		    listaDeUno.add(entidadPaisResidencia);
		    
		    cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia modificarFatcaPaisResidencia = new cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia();
		    cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.EntradaModificarFatcaPaisResidencia entradaModificarPersonaFatca = new cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.EntradaModificarFatcaPaisResidencia();
		    entradaModificarPersonaFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaModificarPersonaFatca.p_FatcaPaisResidencia = listaDeUno;
		    
		    cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.SalidaModificarFatcaPaisResidencia salidaModificarPersonaFatca = modificarFatcaPaisResidencia.logica(conn, entradaModificarPersonaFatca);
		    
		    if( !salidaModificarPersonaFatca.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaModificarPersonaFatca.P_OUT_MENSAJES);
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
		logger.info("FIN::ModificarFatcaPaisResidencia", finServicio - inicioServicio);
		return salida;
	}

	private List<FatcaPaisResidencia> obtenerDatosFatcas(Connection conn, EntradaModificarFatcaPaisResidencia entrada) throws Exception {
		logger.debug("INICIO::ModificarFatcaPaisResidencia.obtenerDatosFatcas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPaisesResidencias = entrada.p_FatcasPaisesResidencias.stream()
				  .map(paisResidencia -> paisResidencia.IdPaisResidencia)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPaisesResidencias", idsPaisesResidencias);
		params.put("IdPersona", entrada.p_IdPersona);
		SQL sql = new SQL();
		List<FatcaPaisResidencia> datosFatcas = sql.query(conn, SQL_SELECT_FATCA_PAIS_RESIDENCIA, params, FatcaPaisResidencia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFatcaPaisResidencia.obtenerDatosFatcas", fin - inicio);
		return datosFatcas;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_FatcasPaisesResidencias\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPaisResidencia\": 2,\r\n"
					+ "      \"IdCausaNoTin\": \"OTRA\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdPersona\": 1732307,\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarFatcaPaisResidencia entradaModificarFatcaPaisResidencia = gson.fromJson(jsonEntrada, EntradaModificarFatcaPaisResidencia.class);
			ModificarFatcaPaisResidencia modificarFatcaPaisResidencia = new ModificarFatcaPaisResidencia();				
			try {
				SalidaWBS salida= modificarFatcaPaisResidencia.logica(con, entradaModificarFatcaPaisResidencia);
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
