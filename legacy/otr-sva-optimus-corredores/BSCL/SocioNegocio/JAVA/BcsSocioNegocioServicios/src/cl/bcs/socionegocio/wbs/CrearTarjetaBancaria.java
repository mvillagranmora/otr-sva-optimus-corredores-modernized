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
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.EntradaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.SalidaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTarjetaBancaria implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearTarjetaBancaria.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearTarjetaBancaria extends EntradaAdministrarWS {
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaCrearTarjetaBancaria() {
			super();
			this.p_TarjetasBancarias = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaCrearTarjetaBancaria.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_TarjetasBancarias = asignarTipoPersona(conn,entrada);
		
		for (TarjetaBancaria tarjeta : entrada.p_TarjetasBancarias) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = tarjeta.IdTarjetaBancaria;
		    entidadCreada.TipoEntidad = "TARJETA BANCARIA";
		    tarjeta.EsBorrador = "N";
		    
		    if(tarjeta.TipoEnrolamiento == null) {
			entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = tarjeta.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<TarjetaBancaria> listaDeUno = new ArrayList<>();
		    listaDeUno.add(tarjeta);
		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, TarjetaBancaria.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
			entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		        
		    
		    CrearPersonaTarjetaBancariaMasivo crearPersonaTarjetaBancariaMasivo = new CrearPersonaTarjetaBancariaMasivo();
		    EntradaCrearPersonaTarjetaBancariaMasivo entradaCrearPersonaTarjetaBancariaMasivo = new EntradaCrearPersonaTarjetaBancariaMasivo();
		    entradaCrearPersonaTarjetaBancariaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearPersonaTarjetaBancariaMasivo.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearPersonaTarjetaBancariaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearPersonaTarjetaBancariaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearPersonaTarjetaBancariaMasivo.p_TarjetasBancarias = obtenerTarjetas(listaDeUno);
		    
		    SalidaCrearPersonaTarjetaBancariaMasivo salidaCrearPersonaTarjetaBancariaMasivo = new SalidaCrearPersonaTarjetaBancariaMasivo();
		    salidaCrearPersonaTarjetaBancariaMasivo = crearPersonaTarjetaBancariaMasivo.logica(conn, entradaCrearPersonaTarjetaBancariaMasivo);
		    if(!salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadCreada.Mensajes.addAll(salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES);
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
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearTarjetaBancaria", finServicio - inicioServicio);
		return salida;
	}



	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearTarjetaBancaria entrada) throws SQLException {
		logger.debug("INICIO::CrearTarjetaBancaria.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_TarjetasBancarias.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTarjetaBancaria.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria> obtenerTarjetas( List<TarjetaBancaria> tarjetas) {
	    List<cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria> tarjetasCreacion =  tarjetas.parallelStream().map(tarjeta -> {
			cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria tarjetaBancaria = new cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria();
			tarjetaBancaria.IdInterno = tarjeta.IdInterno;
			tarjetaBancaria.IdTarjetaBancaria = tarjeta.IdTarjetaBancaria;
			tarjetaBancaria.IdBanco = tarjeta.IdBanco;
			tarjetaBancaria.IdTipoTarjetaBancaria = tarjeta.IdTipoTarjetaBancaria;
			tarjetaBancaria.NumeroTarjetaBancaria = tarjeta.NumeroTarjetaBancaria;
			tarjetaBancaria.IdPersona = tarjeta.IdPersona;
			tarjetaBancaria.FechaVencimiento = Fecha.toStringDate(tarjeta.FechaVencimiento);
			tarjetaBancaria.CodigoVerificacion = Long.valueOf(tarjeta.CodigoVerificacion);
			tarjetaBancaria.IdEstado = tarjeta.IdEstado;
			tarjetaBancaria.IdUsuarioCreador = tarjeta.IdUsuarioCreador;
			tarjetaBancaria.IdEstadoAutorizacion = tarjeta.IdEstadoAutorizacion;
			return tarjetaBancaria;
		}).collect(Collectors.toList());
	    
	    return tarjetasCreacion;
	}
	
	private List<TarjetaBancaria> asignarTipoPersona(Connection con, EntradaCrearTarjetaBancaria entrada) throws SQLException{
		logger.debug("INICIO::CrearTarjetaBancaria.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_TarjetasBancarias.parallelStream().forEach(tarjeta -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(tarjeta.IdPersona)).findFirst().orElse(new TipoPersona());
			tarjeta.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "TARJETA_BANCARIA");
			tarjeta.TipoEnrolamiento = tipo.TipoEnrolamiento;
			tarjeta.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTarjetaBancaria.asignarTipoPersona", fin - inicio);
		return entrada.p_TarjetasBancarias;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_TarjetasBancarias\": [{\r\n"
					+ "        \"IdPersona\": 1732307,\r\n"
					/*
					 * + "        \"IdBanco\": 0,\r\n" +
					 * "        \"IdTipoTarjetaBancaria\": \"\",\r\n" +
					 * "        \"NumeroTarjetaBancaria\": \"\",\r\n" +
					 * "        \"CodigoVerificacion\": \"321\",\r\n" +
					 * "        \"FechaVencimiento\": \"\",\r\n"
					 */
					+ "        \"IdEstado\": \"ACT\"\r\n"
					+ "    } ],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaCrearTarjetaBancaria entradaCrearTarjetaBancaria = gson.fromJson(jsonEntrada, EntradaCrearTarjetaBancaria.class);
			CrearTarjetaBancaria crearTarjetaBancaria = new CrearTarjetaBancaria();				
			try {
			    SalidaWBS salida= crearTarjetaBancaria.logica(con, entradaCrearTarjetaBancaria);
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
