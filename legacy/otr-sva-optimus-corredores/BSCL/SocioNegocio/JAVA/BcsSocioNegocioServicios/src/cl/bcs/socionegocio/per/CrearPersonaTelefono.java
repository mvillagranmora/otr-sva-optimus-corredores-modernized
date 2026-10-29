package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaTelefono.EntradaConfirmarCrearPersonaTelefono;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaTelefono implements JavaCall{

	private static final Logger logger = LogManager.getLogger(CrearPersonaTelefono.class);

	private final String SQL_INSERT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTelefono_Insert.sql");
	private final String SQL_SELECT_PERSONA_GLOSA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTelefono_SelectPersonaGlosa.sql");

	private static final String ID_CONCEPTO = "NUETEL";

	public static class EntradaServicioCrearPersonaTelefono{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public Long p_IdPais;
		public String p_IdTipoTelefono;
		public String p_Numero;
		public String p_Anexo;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;

		public EntradaServicioCrearPersonaTelefono(){
			this.p_Anexo = "";
			this.p_Comentario = "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadTelefono{
		public Long p_IdTelefono;
		public Long p_IdPais;
		public String p_IdTipoTelefono;
		public String p_Numero;
		public String p_Anexo;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;
		public String p_FechaIngreso;
		public Long p_IdPersona;
		public String p_IdEstadoAutorizacion;
		public Long p_IdUsuario;
	}

	public static class PersonaGlosa{
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class SalidaCrearPersonaTelefono extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaCrearPersonaTelefono(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioCrearPersonaTelefono in = gson.fromJson(jin.toString(), EntradaServicioCrearPersonaTelefono.class);
		SalidaCrearPersonaTelefono out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaCrearPersonaTelefono logica(Connection conn, EntradaServicioCrearPersonaTelefono in) throws Exception{
		logger.info("INICIO::CrearPersonaTelefono");
		SalidaCrearPersonaTelefono salida = new SalidaCrearPersonaTelefono();
		SQL sql = new SQL();

		Long idTelefono = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Telefono", 1L).get(0);
		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
		boolean generaAutorizacion = requiereAutorizacion.equals("S") && !"N".equals(in.p_UsaReglasNegocio);
		String idEstadoAutorizacion = generaAutorizacion ? "PEN" : "ING";

		EntidadTelefono telefono = new EntidadTelefono();
		telefono.p_IdTelefono = idTelefono;
		telefono.p_IdPais = in.p_IdPais;
		telefono.p_IdTipoTelefono = in.p_IdTipoTelefono;
		telefono.p_Numero = in.p_Numero;
		telefono.p_Anexo = in.p_Anexo;
		telefono.p_CodigoProposito = in.p_CodigoProposito;
		telefono.p_Comentario = in.p_Comentario;
		telefono.p_IdEstado = in.p_IdEstado;
		telefono.p_FechaIngreso = fechaHoraSistema;
		telefono.p_IdPersona = in.p_IdPersona;
		telefono.p_IdEstadoAutorizacion = idEstadoAutorizacion;
		telefono.p_IdUsuario = in.p_IdUsuario;

		logger.debug("INICIO::SQL_INSERT_TELEFONO");
		int[] res = sql.batch(conn, SQL_INSERT_TELEFONO, Arrays.asList(telefono), EntidadTelefono.class);
		logger.debug("FIN::SQL_INSERT_TELEFONO");

		if(res == null || res.length == 0 || res[0] != 1){
			salida.addError("UI_ERROR_AL_CREAR_TELEFONO", "UI_ERROR_AL_CREAR_TELEFONO");
			logger.info("FIN::CrearPersonaTelefono");
			return salida;
		}

		salida.p_out_RefId = idTelefono;

		if(generaAutorizacion){
			logger.debug("Requiere autorizacion, crear evento");

			String glosa = in.p_Numero;
			HashMap<String, Object> paramsGlosa = new HashMap<>();
			paramsGlosa.put("p_IdPersona", in.p_IdPersona);
			List<PersonaGlosa> personaGlosa = sql.query(conn, SQL_SELECT_PERSONA_GLOSA, paramsGlosa, PersonaGlosa.class);
			if(!personaGlosa.isEmpty()){
				PersonaGlosa datosPersona = personaGlosa.get(0);
				glosa = datosPersona.DocumentoIdentidad + " / " + datosPersona.NombreCompleto + " / NroTel: " + in.p_Numero;
			}

			CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
			CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
			entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
			entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
			entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
			entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
			entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";

			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = in.p_IdModulo;
			evento.IdUsuarioSolicita = in.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(idTelefono);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = in.p_IdPersona;
			evento.Glosa = glosa;
			entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);

			CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn, entradaEvtAutorizacion);

			for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}
			for(CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE){
				salida.addError(error.Codigo, error.Descripcion);
			}

			if(salida.P_OUT_ERRORESCONTADOR == 0){
				salida.addAdvertencia("REQUIERE_AUTORIZACION", Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<>()));
			}
		}else{
			logger.debug("No requiere autorizacion, confirma ingreso de telefono");
			ConfirmarCrearPersonaTelefono confirmar = new ConfirmarCrearPersonaTelefono();
			EntradaConfirmarCrearPersonaTelefono entradaConfirmar = new EntradaConfirmarCrearPersonaTelefono();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdTelefono = idTelefono;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			try {
				confirmar.logica(conn, entradaConfirmar);
			} catch (Exception exConfirmar) {
				logger.error("Error confirmando ingreso de telefono en CrearPersonaTelefono: " + exConfirmar.getMessage(), exConfirmar);
			}
		}

		logger.info("FIN::CrearPersonaTelefono");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":61355,\"p_IdPais\":45,\"p_IdTipoTelefono\":\"MOVIL\",\"p_Numero\":\"+56912345678\",\"p_Anexo\":\"\",\"p_CodigoProposito\":\"PART\",\"p_Comentario\":\"\",\"p_IdEstado\":\"ACT\"}";
			EntradaServicioCrearPersonaTelefono in = gson.fromJson(json, EntradaServicioCrearPersonaTelefono.class);

			CrearPersonaTelefono demo = new CrearPersonaTelefono();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaCrearPersonaTelefono out = demo.logica(con, in);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(out.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
