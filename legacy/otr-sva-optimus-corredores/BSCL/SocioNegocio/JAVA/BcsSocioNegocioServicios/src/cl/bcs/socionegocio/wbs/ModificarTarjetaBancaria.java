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
import cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TarjetaBancaria;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarTarjetaBancaria implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarTarjetaBancaria.class);
	
	private String SQL_SELECT_TARJETA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarTarjetaBancaria_SelectTarjetaBancaria.sql");
	
	public static class EntradaModificarTarjetaBancaria extends EntradaAdministrarWS {
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaModificarTarjetaBancaria(){
			this.p_TarjetasBancarias = new ArrayList<TarjetaBancaria>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaModificarTarjetaBancaria.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::ModificarTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		List<TarjetaBancaria> datosTarjetasBancarias = obtenerDatosTarjetasBancarias(conn,entrada);

		for(TarjetaBancaria tarjetaBancaria : datosTarjetasBancarias) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = tarjetaBancaria.IdTarjetaBancaria;
		    entidadModificada.TipoEntidad = "TARJETA BANCARIA";
		    
		    if(tarjetaBancaria.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<TarjetaBancaria> mergeCambios = new ArrayList<>();
		    TarjetaBancaria nuevosCampos = entrada.p_TarjetasBancarias.parallelStream()
		    		.filter(tarjeta -> tarjeta.IdTarjetaBancaria.equals(tarjetaBancaria.IdTarjetaBancaria))
		    		.findFirst().orElse(new TarjetaBancaria());
		    WebServiceUtils.merge(tarjetaBancaria, nuevosCampos);
		    mergeCambios.add(tarjetaBancaria);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, tarjetaBancaria.Tipo, tarjetaBancaria.TipoEnrolamiento, "TARJETA_BANCARIA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, TarjetaBancaria.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<TarjetaBancaria> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarPersonaTarjetaBancariaMasivo modificarPersonaTarjetaBancariaMasivo = new ModificarPersonaTarjetaBancariaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaTarjetaBancariaMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
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
		logger.info("FIN::ModificarTarjetaBancaria", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<TarjetaBancaria> tarjetasBancarias) {
		logger.trace("INICIO::ModificarTarjetaBancaria.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(TarjetaBancaria tarjeta : tarjetasBancarias) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "IdBanco", tarjeta.IdBanco));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "IdTipoTarjetaBancaria", tarjeta.IdTipoTarjetaBancaria));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "NumeroTarjetaBancaria", tarjeta.NumeroTarjetaBancaria));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "FechaVencimiento", tarjeta.FechaVencimiento));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "CodigoVerificacion", tarjeta.CodigoVerificacion));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjeta.IdTarjetaBancaria, "IdEstado", tarjeta.IdEstado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTarjetaBancaria.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<TarjetaBancaria> obtenerDatosTarjetasBancarias(Connection conn, EntradaModificarTarjetaBancaria entrada) throws Exception {
		logger.debug("INICIO::ModificarTarjetaBancaria.obtenerDatosTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsTarjetas = entrada.p_TarjetasBancarias.stream()
				  .map(tarjeta -> tarjeta.IdTarjetaBancaria)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdTarjetasBancarias", idsTarjetas);
		
		SQL sql = new SQL();
		List<TarjetaBancaria> datosTarjetasBancarias = sql.query(conn, SQL_SELECT_TARJETA, params, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTarjetaBancaria.obtenerDatosTarjetasBancarias", fin - inicio);
		return datosTarjetasBancarias;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_TarjetasBancarias\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdTarjetaBancaria\": 10493,\r\n"
					+ "      \"IdBanco\": 0,\r\n"
					+ "      \"NumeroTarjetaBancaria\": \"\",\r\n"
					+ "      \"FechaVencimiento\": \"\",\r\n"
					+ "      \"CodigoVerificacion\": 0,\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarTarjetaBancaria entradaModificarTarjetaBancaria = gson.fromJson(jsonEntrada, EntradaModificarTarjetaBancaria.class);
			ModificarTarjetaBancaria modificarTarjetaBancaria = new ModificarTarjetaBancaria();				
			try {
				SalidaWBS salida= modificarTarjetaBancaria.logica(con, entradaModificarTarjetaBancaria);
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
