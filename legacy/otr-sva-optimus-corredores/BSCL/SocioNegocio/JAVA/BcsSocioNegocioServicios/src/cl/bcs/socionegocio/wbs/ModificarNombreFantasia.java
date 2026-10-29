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
import cl.bcs.socionegocio.per.ModificarPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.NombreFantasia;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarNombreFantasia implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarNombreFantasia.class);
	
	private String SQL_SELECT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarNombreFantasia_SelectNombreFantasia.sql");

	
	public static class EntradaModificarNombreFantasia extends EntradaAdministrarWS {
		public List<NombreFantasia> p_NombresFantasia;
		
		public EntradaModificarNombreFantasia(){
			this.p_NombresFantasia = new ArrayList<NombreFantasia>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarNombreFantasia jin = gson.fromJson(in.toString(), EntradaModificarNombreFantasia.class);
		
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarNombreFantasia entrada) throws Exception {
		logger.info("INICIO::ModificarNombreFantasia");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<NombreFantasia> datosNombresFantasia = obtenerNombresFantasia(conn,entrada);
		
		for(NombreFantasia nombreFantasia : datosNombresFantasia) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = nombreFantasia.IdNombreFantasia;
		    entidadModificada.TipoEntidad = "NOMBRE FANTASIA";
		    nombreFantasia.EsBorrador = "N";
		    nombreFantasia.DescripcionNombreFantasia = nombreFantasia.NombreFantasia;
		    
			if (nombreFantasia.TipoEnrolamiento == null) {
				entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
			}
			if ("N".equals(nombreFantasia.TipoPersona)) {
				entidadModificada = noEsPersonaJuridica(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
			}
		    
		    List<NombreFantasia> mergeCambios = new ArrayList<>();
		    NombreFantasia nuevosCampos = entrada.p_NombresFantasia.parallelStream()
		    		.filter(nom -> nom.IdNombreFantasia.equals(nombreFantasia.IdNombreFantasia))
		    		.findFirst().orElse(new NombreFantasia());
		    WebServiceUtils.merge(nombreFantasia, nuevosCampos);
		    mergeCambios.add(nombreFantasia);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, nombreFantasia.TipoEnrolamiento, nombreFantasia.TipoEnrolamiento, "NOMBRES_FANTASIA_PERSONA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, NombreFantasia.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<NombreFantasia> lista =  new ArrayList<>();
		    lista.add(nombreFantasia);
		    
		    ModificarPersonaNombreFantasiaMasivo modificarNombreFantasiaMasivo= new ModificarPersonaNombreFantasiaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(lista);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarNombreFantasiaMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
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
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarNombreFantasia", finServicio - inicioServicio);
		return salida;
	}

	private EntidadManipulada noEsPersonaJuridica(EntidadManipulada entidadModificada) {
		logger.trace("INICIO::WebServiceUtils.noEsCliente");
		long inicio = System.currentTimeMillis();
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.id = 0L;
		mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.descripcion = "ERROR_PERSONA_NATURAL";
		mensaje.codigo = "ERROR_PERSONA_NATURAL";
		entidadModificada.Mensajes.add(mensaje);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.noEsCliente", fin - inicio);
	
		return entidadModificada;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<NombreFantasia> nombresFantasia) {
		logger.trace("INICIO::ModificarNombreFantasia.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		List<cl.bcs.socionegocio.per.pojo.NombreFantasia> modificarNombres = nombresFantasia.parallelStream().map(nom ->{
		    cl.bcs.socionegocio.per.pojo.NombreFantasia modNom =  new cl.bcs.socionegocio.per.pojo.NombreFantasia();
			modNom.IdInterno = nom.IdInterno;
			modNom.IdNombreFantasia = nom.IdNombreFantasia;
			modNom.DescripcionNombreFantasia = nom.NombreFantasia;
			modNom.IdPersona = nom.IdPersona;
			modNom.FechaIngreso = (nom.FechaIngreso ==null || nom.FechaIngreso.isEmpty())?Fecha.toTimeStamp("0001-01-01 00:00:00"): Fecha.toTimeStamp(nom.FechaIngreso);
			modNom.IdEstado = nom.IdEstado;
			modNom.IdEstadoAutorizacion = nom.IdEstadoAutorizacion;
		    return modNom;
		}).collect(Collectors.toList());
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(cl.bcs.socionegocio.per.pojo.NombreFantasia nombreFantasia : modificarNombres) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(nombreFantasia.IdNombreFantasia, "DescripcionNombreFantasia", nombreFantasia.DescripcionNombreFantasia));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(nombreFantasia.IdNombreFantasia, "IdEstado", nombreFantasia.IdEstado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarNombreFantasia.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<NombreFantasia> obtenerNombresFantasia(Connection conn, EntradaModificarNombreFantasia entrada) throws SQLException {
		logger.debug("INICIO::ModificarNombreFantasia.obtenerNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsNombresFantasia = entrada.p_NombresFantasia.stream()
				  .map(nombre -> nombre.IdNombreFantasia)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdNombreFantasia", idsNombresFantasia);
		
		SQL sql = new SQL();
		List<NombreFantasia> datosNombreFantasia = sql.query(conn, SQL_SELECT_NOMBRE_FANTASIA, params, NombreFantasia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarNombreFantasia.obtenerNombresFantasia", fin - inicio);
		return datosNombreFantasia;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			System.out.println("eeeeeeeeeeeee");
			String jsonEntrada = "{\r\n"
				+ "  \"p_NombresFantasia\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdPersona\": 1732307,\r\n"
				+ "      \"IdNombreFantasia\": 10089,\r\n"
				+ "      \"NombreFantasia\": \"\",\r\n"
				+ "      \"IdEstado\": \"INA\"\r\n"
				+ "    }\r\n"
//				+ "    {\r\n"
//				+ "      \"IdPersona\": 1774214,\r\n"
//				+ "      \"IdNombreFantasia\": 1,\r\n"
//				+ "      \"NombreFantasia\": \"ATAKO D CERKITA\",\r\n"
//				+ "      \"IdEstado\": \"INA\"\r\n"
//				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 9\r\n"
				+ "}";
			EntradaModificarNombreFantasia entradaModificarCuentaBancaria = gson.fromJson(jsonEntrada, EntradaModificarNombreFantasia.class);
			ModificarNombreFantasia modificarTelefono = new ModificarNombreFantasia();				
			try {
			    System.out.println("eeeeeeeeeeeee");
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
