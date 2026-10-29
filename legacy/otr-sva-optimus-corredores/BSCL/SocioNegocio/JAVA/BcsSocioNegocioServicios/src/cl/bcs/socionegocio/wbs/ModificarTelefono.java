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
import cl.bcs.socionegocio.per.ModificarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Telefono;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarTelefono implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarTelefono.class);
	
	private String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarTelefono_SelectTelefono.sql");

	
	public static class EntradaModificarTelefono extends EntradaAdministrarWS {
		public List<Telefono> p_Telefonos;
		
		public EntradaModificarTelefono(){
			this.p_Telefonos = new ArrayList<Telefono>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarTelefono jin = gson.fromJson(in.toString(), EntradaModificarTelefono.class);
		
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarTelefono entrada) throws Exception {
		logger.info("INICIO::ModificarTelefono");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<Telefono> datosTelefonos = obtenerDatosTelefonos(conn,entrada);
		
		for(Telefono telefono : datosTelefonos) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = telefono.IdTelefono;
		    entidadModificada.TipoEntidad = "TELEFONO";
		    telefono.EsBorrador = "N";
		    
			if (telefono.TipoEnrolamiento == null) {
				entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
			}
		    
		    List<Telefono> mergeCambios = new ArrayList<>();
		    Telefono nuevosCampos = entrada.p_Telefonos.parallelStream()
		    		.filter(tel -> tel.IdTelefono.equals(telefono.IdTelefono))
		    		.findFirst().orElse(new Telefono());
		    WebServiceUtils.merge(telefono, nuevosCampos);
		    mergeCambios.add(telefono);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, telefono.Tipo, telefono.TipoEnrolamiento, "TELEFONO");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, Telefono.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<Telefono> lista =  new ArrayList<>();
		    lista.add(telefono);
		    
		    ModificarPersonaTelefonoMasivo modificarPersonaTelefonoMasivo = new ModificarPersonaTelefonoMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(lista);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaTelefonoMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
		    if(!salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
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
				(sum, ent) -> sum += ent.Mensajes.parallelStream()
						.filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<Telefono> telefonos) {
		logger.trace("INICIO::ModificarTelefono.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(Telefono telefono : telefonos) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "IdPais", telefono.IdPais));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "IdTipoTelefono", telefono.IdTipoTelefono));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "Numero", telefono.Numero));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "Anexo", telefono.Anexo));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "CodigoProposito", telefono.CodigoProposito));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "Comentario", telefono.Comentario));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "Predeterminado", telefono.Predeterminado));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "IdEstado", telefono.IdEstado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTelefono.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<Telefono> obtenerDatosTelefonos(Connection conn, EntradaModificarTelefono entrada) throws SQLException {
		logger.debug("INICIO::ModificarTelefono.obtenerDatosTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsTelefonos = entrada.p_Telefonos.stream()
				  .map(telefono -> telefono.IdTelefono)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdTelefono", idsTelefonos);
		
		SQL sql = new SQL();
		List<Telefono> datosTelefonos = sql.query(conn, SQL_SELECT_TELEFONO, params, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTelefono.obtenerDatosTelefonos", fin - inicio);
		return datosTelefonos;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_Telefonos\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdTelefono\": 2551274,\r\n"
				+ "      \"Anexo\": \"\",\r\n"
				+ "      \"Comentario\": \"\",\r\n"
				+ "      \"IdEstado\": \"\",\r\n"
				+ "      \"IdTipoTelefono\": \"\",\r\n"
				+ "      \"Numero\": \"\",\r\n"
				+ "      \"IdPais\": 0,\r\n"
				+ "      \"CodigoProposito\": \"\"\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 4,\r\n"
				+ "  \"p_IdModulo\": \"SOC\",\r\n"
				+ "  \"p_UsaReglasNegocio\": \"S\",\r\n"
				+ "  \"p_CodigoFormulario\": \"\",\r\n"
				+ "  \"p_IdTipoCanal\": \"WBS\"\r\n"
				+ "}";
			
			
			EntradaModificarTelefono entradaModificarCuentaBancaria = gson.fromJson(jsonEntrada, EntradaModificarTelefono.class);
			System.out.println(entradaModificarCuentaBancaria.toString());
			ModificarTelefono modificarTelefono = new ModificarTelefono();				
			try {
				SalidaWBS salida= modificarTelefono.logica(con, entradaModificarCuentaBancaria);
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
