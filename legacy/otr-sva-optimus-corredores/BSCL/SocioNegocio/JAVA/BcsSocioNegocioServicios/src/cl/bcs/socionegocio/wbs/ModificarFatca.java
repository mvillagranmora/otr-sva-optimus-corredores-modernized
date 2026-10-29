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
import cl.bcs.socionegocio.enr.EnrolamientoUtils;
import cl.bcs.socionegocio.per.ModificarPersonaFatca;
import cl.bcs.socionegocio.per.ModificarPersonaFatca.EntradaModificarPersonaFatca;
import cl.bcs.socionegocio.per.ModificarPersonaFatca.SalidaModificarPersonaFatca;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Fatca;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarFatca implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarFatca.class);
	
	private String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarFatca_SelectFatca.sql");
	
	public static class EntradaModificarFatca extends EntradaAdministrarWS {
		public List<Fatca> p_Fatcas;
		
		public EntradaModificarFatca(){
			this.p_Fatcas = new ArrayList<Fatca>();
		}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarFatca jin = gson.fromJson(in.toString(), EntradaModificarFatca.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarFatca entrada)  throws Exception {
		logger.info("INICIO::ModificarFatca");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<Fatca> datosFatcas = obtenerDatosFatcas(conn,entrada);
		
		for(Fatca datosFatca : datosFatcas) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = datosFatca.IdPersona;
		    entidadModificada.TipoEntidad = "FATCA";
		    datosFatca.EsBorrador = "N";
		    
		    if(datosFatca.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<Fatca> mergeCambios = new ArrayList<>();
		    Fatca nuevosCampos = entrada.p_Fatcas.parallelStream()
		    		.filter(fatca -> fatca.IdPersona.equals(datosFatca.IdPersona))
		    		.findFirst().orElse(new Fatca());
		    WebServiceUtils.merge(datosFatca, nuevosCampos);
		    mergeCambios.add(datosFatca);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, datosFatca.Tipo, datosFatca.TipoEnrolamiento, "FATCA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, Fatca.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<Fatca> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarPersonaFatca modificarPersonaFatca = new ModificarPersonaFatca();
		    EntradaModificarPersonaFatca entradaModificarPersonaFatca = new EntradaModificarPersonaFatca();
		    entradaModificarPersonaFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaModificarPersonaFatca.p_IdModulo = entrada.p_IdModulo;
		    entradaModificarPersonaFatca.p_IdUsuario = entrada.p_IdUsuario;
		    entradaModificarPersonaFatca.p_IdPersona = datosFatca.IdPersona;
		    entradaModificarPersonaFatca.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaModificarPersonaFatca.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaModificarPersonaFatca salidaModificarPersonaFatca = modificarPersonaFatca.logica(conn, entradaModificarPersonaFatca);
		    
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
		logger.info("FIN::ModificarFatca", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroCambio> crearRegistroCambio(List<Fatca> fatcas) {
		logger.trace("INICIO::ModificarFatca.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambio> registrosCambio = new ArrayList<>();
		
		for(Fatca fatca : fatcas) {
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdClasificacionFatca", fatca.IdClasificacionFatca));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdClasificacionEmpresaFatca", fatca.IdClasificacionEmpresaFatca));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("NumeroGIIN", fatca.NumeroGIIN));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("FechaFirmaFormulario", fatca.FechaFirmaFormulario));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("AutorizacionInformarOrganismo", fatca.AutorizacionInformarOrganismo));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionResidente", fatca.ExcepcionResidente));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionNacimiento", fatca.ExcepcionNacimiento));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionDomicilio", fatca.ExcepcionDomicilio));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionTelefono", fatca.ExcepcionTelefono));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionOrdenPermanente", fatca.ExcepcionOrdenPermanente));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionFirma", fatca.ExcepcionFirma));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ParticipacionSociedadesUSA", fatca.ParticipacionSociedadesUSA));
			registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("OrdenPermanente", fatca.OrdenPermanente));
		}

		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarFatca.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<Fatca> obtenerDatosFatcas(Connection conn, EntradaModificarFatca entrada) throws Exception {
		logger.debug("INICIO::ModificarFatca.obtenerDatosFatcas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsFatcas = entrada.p_Fatcas.stream()
				  .map(fatca -> fatca.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdFatcas", idsFatcas);
		
		SQL sql = new SQL();
		List<Fatca> datosFatcas = sql.query(conn, SQL_SELECT_FATCA, params, Fatca.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFatca.obtenerDatosFatcas", fin - inicio);
		return datosFatcas;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_Fatcas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1732307\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarFatca entradaModificarFatca = gson.fromJson(jsonEntrada, EntradaModificarFatca.class);
			ModificarFatca modificarFatca = new ModificarFatca();				
			try {
				SalidaWBS salida= modificarFatca.logica(con, entradaModificarFatca);
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
