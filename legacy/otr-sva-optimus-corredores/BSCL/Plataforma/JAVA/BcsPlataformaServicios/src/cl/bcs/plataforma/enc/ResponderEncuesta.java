package cl.bcs.plataforma.enc;

import cl.bcs.plataforma.enc.pojo.DefinicionEncuesta;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento;
import cl.bcs.socionegocio.cli.ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento;
import cl.bcs.socionegocio.cli.ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento;
import cl.bcs.socionegocio.cli.pojo.Cliente;
import cl.bcs.socionegocio.cli.pojo.Portafolio;
import cl.bcs.socionegocio.cli.pojo.RegistroEncuesta;
import cl.bcs.socionegocio.cli.pojo.RegistroPregunta;
import cl.bcs.socionegocio.cli.pojo.RegistroRespuesta;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponderEncuesta implements JavaCall {

	private final String SQL_SELECT_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectEncuesta.sql");
	private final String SQL_SELECT_PREGUNTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPreguntas.sql");
	private final String SQL_SELECT_RESPUESTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectRespuestas.sql");
	private final String SQL_SELECT_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectDefinicionPuntaje.sql");
	private final String SQL_UPDATE_CLIENTE = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_UpdateCategoriaCliente.sql");
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectCliente.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPortafolio.sql");

	private final String SQL_INSERT_REGISTRO_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroEncuesta.sql");
	private final String SQL_INSERT_REGISTRO_PREGUNTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroPreguntas.sql");
	private final String SQL_INSERT_REGISTRO_RESPUESTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_InsertRegistroRespuestas.sql");
	
	public static class ClientePerfilComercial {
		public Long IdCliente;
		public String IdPerfilProductos;
		public String IdPerfilComercialBase;
		public String IdCategoriaInversionista;		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ResponderEncuesta_Entrada in = gson.fromJson(jin.toString(), ResponderEncuesta_Entrada.class);
		ResponderEncuesta_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public ResponderEncuesta_Salida logica(Connection conn, ResponderEncuesta_Entrada entrada) throws Exception {
		ResponderEncuesta_Salida salida = new ResponderEncuesta_Salida();
		
		try {
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			if (entrada.p_IdCliente == null) {
				String codigo = "UI_CLIENTE_NO_VALIDO";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				salida.P_OUT_ERRORESCONTADOR = 1L;

				return salida;
			}
			
			SQL _sql = new SQL();

			HashMap<String,Object> params = new HashMap<>(4);
			params.put("IdCliente",entrada.p_IdCliente);
			List<Cliente> clientes = _sql.query(conn, SQL_SELECT_CLIENTE, params, Cliente.class);
			if (clientes.size() == 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CLIENTE_NO_EXISTE", new HashMap<>(), conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;

				return salida;
			}

			params.put("IdCliente", entrada.p_IdCliente);
			List<Portafolio> ListaPortafoliosCliente = _sql.query(conn, SQL_SELECT_PORTAFOLIO, params, Portafolio.class);

			
			List<RegistroPregunta> registroPreguntas = new ArrayList<RegistroPregunta>();
			List<RegistroRespuesta> registroRespuestas = new ArrayList<RegistroRespuesta>();
			List<RegistroEncuesta> registrosEncuesta = new ArrayList<>();

			ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Entrada entradaObtenerResultadoEncuesta = new ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Entrada();
			entradaObtenerResultadoEncuesta.p_Respuestas = entrada.p_Respuestas;
			entradaObtenerResultadoEncuesta.p_IdDefinicionEncuesta = entrada.p_IdDefinicionEncuesta;
			entradaObtenerResultadoEncuesta.p_IdUsuario = entrada.p_IdUsuario;
			entradaObtenerResultadoEncuesta.p_IdModulo = entrada.p_IdModulo;
			entradaObtenerResultadoEncuesta.p_IdCanal = entrada.p_IdCanal;
			entradaObtenerResultadoEncuesta.p_IdEmpresa = entrada.p_IdEmpresa;
			ObtenerResultadoEncuesta obtenerResultadoEncuesta = new ObtenerResultadoEncuesta();
			ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Salida salidaObtenerResultadoEncuesta = obtenerResultadoEncuesta.logica(conn, entradaObtenerResultadoEncuesta);

			salida.P_OUT_MENSAJES.addAll(salidaObtenerResultadoEncuesta.P_OUT_MENSAJES);

			if (salidaObtenerResultadoEncuesta.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR += salidaObtenerResultadoEncuesta.P_OUT_ERRORESCONTADOR;

				return salida;
			}

			clientes.get(0).IdCategoriaInversionista = salidaObtenerResultadoEncuesta.IdCategoriaInversionista;
			final DefinicionEncuesta definicionEncuesta = salidaObtenerResultadoEncuesta.DefinicionEncuestas.get(0);
			final int cantidadRespuestas = salidaObtenerResultadoEncuesta.RegistroRespuestas.size();
			final int cantidadPreguntas = salidaObtenerResultadoEncuesta.RegistroPreguntas.size();
			final int cantidadPortafolios = ListaPortafoliosCliente.size();
			
			List<Long> idsRegistroEncuesta = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::IdRegistroEncuesta", Long.valueOf(cantidadPortafolios));
			List<Long> idsRegistroPreguntas = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::IdRegistroPregunta", Long.valueOf(cantidadPortafolios * cantidadPreguntas));
			List<Long> idsRegistroRespuestas = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::IdRegistroRespuesta", Long.valueOf(cantidadPortafolios * cantidadRespuestas));

			int indiceActualRegEnc = 0;
			int indiceActualRegResp = 0;
			final AtomicInteger indiceActualRegPreg = new AtomicInteger();

			Map<String, Long> idRegPregPorLlave = new HashMap<>(cantidadPreguntas * cantidadPortafolios);

			for (Portafolio portafolio : ListaPortafoliosCliente) {				
				RegistroEncuesta registroEncuesta = new RegistroEncuesta();
				registroEncuesta.IdEmpresa = entrada.p_IdEmpresa;
				registroEncuesta.IdCliente = entrada.p_IdCliente;
				registroEncuesta.IdRegistroEncuesta = idsRegistroEncuesta.get(indiceActualRegEnc);
				registroEncuesta.IdCategoriaInversionista = salidaObtenerResultadoEncuesta.IdCategoriaInversionista;
				registroEncuesta.NombreEncuesta = definicionEncuesta.Nombre;
				registroEncuesta.Puntaje = salidaObtenerResultadoEncuesta.Puntaje;
				registroEncuesta.FechaIngresoRegistro = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
				registroEncuesta.FechaEncuesta = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
				registroEncuesta.IdUsuarioResponde = entrada.p_IdUsuario;
				registroEncuesta.AsignadoPorEncuesta = "S";
				registroEncuesta.IdCanal = entrada.p_IdCanal;
				registroEncuesta.IdPortafolio = portafolio.IdPortafolio;
				registroEncuesta.IdPerfilProductos = portafolio.IdPerfilProductos;

				registrosEncuesta.add(registroEncuesta);

				for (RegistroPregunta registroPreguntaBase : salidaObtenerResultadoEncuesta.RegistroPreguntas) {
					String llaveIdRegPreg = "enc-" + indiceActualRegEnc + ":preg-" + registroPreguntaBase.IdRegistroPregunta;
					Long idRegPreg = idRegPregPorLlave.computeIfAbsent(llaveIdRegPreg, key -> idsRegistroPreguntas.get(indiceActualRegPreg.getAndIncrement()));
					RegistroPregunta registroPregunta = new RegistroPregunta();
					registroPregunta.IdRegistroEncuesta = registroEncuesta.IdRegistroEncuesta;
					registroPregunta.IdRegistroPregunta = idRegPreg;
					registroPregunta.TextoPregunta = registroPreguntaBase.TextoPregunta;
					registroPregunta.IdEmpresa = registroPreguntaBase.IdEmpresa;

					registroPreguntas.add(registroPregunta);
				}

				for (RegistroRespuesta registroRespuestaBase : salidaObtenerResultadoEncuesta.RegistroRespuestas) {
					String llaveIdRegPreg = "enc-" + indiceActualRegEnc + ":preg-" + registroRespuestaBase.IdRegistroPregunta;
					Long idRegPreg = idRegPregPorLlave.get(llaveIdRegPreg);
					RegistroRespuesta registroRespuesta = new RegistroRespuesta();
					registroRespuesta.IdRegistroPregunta = idRegPreg;
					registroRespuesta.IdRegistroRespuesta = idsRegistroRespuestas.get(indiceActualRegResp++);
					registroRespuesta.TextoRespuesta = registroRespuestaBase.TextoRespuesta;
					registroRespuesta.IdEmpresa = registroRespuestaBase.IdEmpresa;
					registroRespuesta.Puntaje = registroRespuestaBase.Puntaje;
					registroRespuesta.Seleccionado = registroRespuestaBase.Seleccionado;

					registroRespuestas.add(registroRespuesta);
				}

				indiceActualRegEnc++;
			}

			_sql.batch(conn, SQL_INSERT_REGISTRO_ENCUESTA, registrosEncuesta, RegistroEncuesta.class);
			_sql.batch(conn, SQL_INSERT_REGISTRO_PREGUNTAS, registroPreguntas, RegistroPregunta.class);
			_sql.batch(conn, SQL_INSERT_REGISTRO_RESPUESTAS, registroRespuestas, RegistroRespuesta.class);

			_sql.batch(conn, SQL_UPDATE_CLIENTE, clientes, Cliente.class);

			// Obtener el perfil comercial por defecto de la categoria de inversionista y actualizar los portafolios del cliente

			EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento entradaModificarPerfilComercial = new EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
			entradaModificarPerfilComercial.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarPerfilComercial.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarPerfilComercial.p_IdModulo 	= entrada.p_IdModulo;
			entradaModificarPerfilComercial.p_IdCliente = clientes.get(0).IdCliente;
			ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento modificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento = new ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
			SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento salidaModificarPerfilComercial = modificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.logica(conn, entradaModificarPerfilComercial);

			salida.P_OUT_MENSAJES.addAll(salidaModificarPerfilComercial.P_OUT_MENSAJES);

			if (salidaModificarPerfilComercial.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR += salidaModificarPerfilComercial.P_OUT_ERRORESCONTADOR;

				return salida;
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return salida;
		
	}


	public static class ResponderEncuesta_Entrada {
		
		public String p_IdModulo;
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public Long p_IdCliente;
		public String p_IdCanal;
		
		public Long p_IdDefinicionEncuesta;
		public List<RespuestaEncuesta_Entrada> p_Respuestas;
		
		public ResponderEncuesta_Entrada() {
			p_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RespuestaEncuesta_Entrada {
		public Long IdDefinicionPregunta;
		public Long IdDefinicionRespuesta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResponderEncuesta_Salida {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ResponderEncuesta_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdModulo\":\"SOC\",\"p_IdUsuario\":5,\"p_IdEmpresa\":1,\"p_IdCliente\":1423323,\"p_IdDefinicionEncuesta\":32,\"p_Respuestas\":[{\"IdDefinicionPregunta\":63,\"IdDefinicionRespuesta\":105},{\"IdDefinicionPregunta\":64,\"IdDefinicionRespuesta\":109},{\"IdDefinicionPregunta\":65,\"IdDefinicionRespuesta\":115},{\"IdDefinicionPregunta\":66,\"IdDefinicionRespuesta\":119},{\"IdDefinicionPregunta\":67,\"IdDefinicionRespuesta\":124},{\"IdDefinicionPregunta\":68,\"IdDefinicionRespuesta\":125},{\"IdDefinicionPregunta\":69,\"IdDefinicionRespuesta\":131},{\"IdDefinicionPregunta\":70,\"IdDefinicionRespuesta\":134},{\"IdDefinicionPregunta\":71,\"IdDefinicionRespuesta\":140},{\"IdDefinicionPregunta\":72,\"IdDefinicionRespuesta\":141},{\"IdDefinicionPregunta\":73,\"IdDefinicionRespuesta\":147},{\"IdDefinicionPregunta\":74,\"IdDefinicionRespuesta\":153},{\"IdDefinicionPregunta\":75,\"IdDefinicionRespuesta\":154},{\"IdDefinicionPregunta\":76,\"IdDefinicionRespuesta\":162},{\"IdDefinicionPregunta\":77,\"IdDefinicionRespuesta\":166}],\"p_IdCanal\":\"AGE\"}";
			
			ResponderEncuesta_Entrada Objin = gson.fromJson(json, ResponderEncuesta_Entrada.class);
			ResponderEncuesta demo=new ResponderEncuesta();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ResponderEncuesta_Salida ObjOut=demo.logica(con, Objin);
 				//con.rollback();
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				
				con.rollback();
			}
 		
 			
	    	}
		 	
		System.out.println("== END ==");
	}
		

}
