package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ConfirmarEliminarPersonaCuentaBancaria.EntradaConfirmarEliminarPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.ConfirmarEliminarPersonaCuentaBancaria.SalidaConfirmarEliminarPersonaCuentaBancaria;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarPersonaCuentaBancaria implements JavaCall{

	private static final Logger logger = LogManager.getLogger(EliminarPersonaCuentaBancaria.class);

	private final String SQL_SELECT_PERSONA_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaCuentaBancaria_SelectPersonaCuentaBancaria.sql");

	private static final String ID_CONCEPTO = "BORCTB";
	private static final String ID_TIPO_ENTIDAD = "CUENTABANCARIA-BORRAR";

	public static class EntradaServicioEliminarPersonaCuentaBancaria{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdCuentaBancaria;
		public String p_Comentario;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PersonaCuentaBancaria{
		public Long IdCuentaBancaria;
		public Long IdPersona;
		public String Borrado;
		public String NumeroCuentaBancaria;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
	}

	public static class SalidaEliminarPersonaCuentaBancaria extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaEliminarPersonaCuentaBancaria(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new java.util.ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new java.util.ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioEliminarPersonaCuentaBancaria in = gson.fromJson(jin.toString(), EntradaServicioEliminarPersonaCuentaBancaria.class);
		SalidaEliminarPersonaCuentaBancaria out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaEliminarPersonaCuentaBancaria logica(Connection conn, EntradaServicioEliminarPersonaCuentaBancaria in) throws Exception{
		logger.info("INICIO::EliminarPersonaCuentaBancaria");
		SalidaEliminarPersonaCuentaBancaria salida = new SalidaEliminarPersonaCuentaBancaria();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdCuentaBancaria);

		HashMap<String, Object> paramsCuenta = new HashMap<>();
		paramsCuenta.put("IdCuentaBancaria", new java.util.ArrayList<>(Arrays.asList(in.p_IdCuentaBancaria)));
		List<PersonaCuentaBancaria> cuentas = sql.query(conn, SQL_SELECT_PERSONA_CUENTA_BANCARIA, paramsCuenta, PersonaCuentaBancaria.class);
		if(cuentas.isEmpty()){
			salida.addError("ENTIDAD_NO_EXISTE", "ENTIDAD_NO_EXISTE");
			return salida;
		}
		PersonaCuentaBancaria cuentaBancaria = cuentas.get(0);

		Long idSolicitudCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L).get(0);

		RegistroSolicitudCambio registroBorrado = new RegistroSolicitudCambio();
		registroBorrado.IdSolicitudCambio = idSolicitudCambio;
		registroBorrado.IdTipoEntidad = ID_TIPO_ENTIDAD;
		registroBorrado.IdEntidadOrigen = idEntidadOrigen;
		registroBorrado.CampoModificado = "Borrado";
		registroBorrado.TipoDato = "NVARCHAR";
		registroBorrado.ValorAntiguo = cuentaBancaria.Borrado;
		registroBorrado.ValorNuevo = "S";
		registroBorrado.IdEstadoAutorizacion = "PEN";

		RegistroSolicitudCambio registroComentario = new RegistroSolicitudCambio();
		registroComentario.IdSolicitudCambio = idSolicitudCambio;
		registroComentario.IdTipoEntidad = ID_TIPO_ENTIDAD;
		registroComentario.IdEntidadOrigen = idEntidadOrigen;
		registroComentario.CampoModificado = "Comentario";
		registroComentario.TipoDato = "NVARCHAR";
		registroComentario.ValorAntiguo = "";
		registroComentario.ValorNuevo = in.p_Comentario != null ? in.p_Comentario : "";
		registroComentario.IdEstadoAutorizacion = "PEN";

		CrearRegistroSolicitudCambio crearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistro = new EntradaCrearRegistroSolicitudCambio();
		entradaCrearRegistro.p_RegistroSolicitudCambio = Arrays.asList(registroBorrado, registroComentario);
		crearRegistroSolicitudCambio.logica(conn, entradaCrearRegistro);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);

		if(requiereAutorizacion.equals("S") && !"N".equals(in.p_UsaReglasNegocio)){
			logger.debug("Requiere autorizacion, crear evento");
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			String glosa = cuentaBancaria.DocumentoIdentidad + " / " + cuentaBancaria.NombreCompleto + " / IdCta: " + in.p_IdCuentaBancaria;

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
			evento.NumeroOperacionConcepto = idEntidadOrigen;
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = cuentaBancaria.IdPersona;
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
			logger.debug("No requiere autorizacion, confirma eliminacion");
			ConfirmarEliminarPersonaCuentaBancaria confirmar = new ConfirmarEliminarPersonaCuentaBancaria();
			EntradaConfirmarEliminarPersonaCuentaBancaria entradaConfirmar = new EntradaConfirmarEliminarPersonaCuentaBancaria();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdCuentaBancaria = in.p_IdCuentaBancaria;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			SalidaConfirmarEliminarPersonaCuentaBancaria salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
			for(ErrorDetalle error : salidaConfirmar.P_OUT_ERRORESDETALLE){
				salida.addError(error.Codigo, error.Descripcion);
			}
			for(AdvertenciaDetalle advertencia : salidaConfirmar.P_OUT_ADVERTENCIASDETALLE){
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}
			// El SP original solo asigna p_out_RefId en esta rama (no requiere autorizacion) y solo si no hay errores;
			// en la rama de autorizacion siempre hace SIGNAL SALIDA_CONTROLADA antes de llegar a esa asignacion,
			// por lo que p_out_RefId queda en 0 en cualquier otro caso.
			if(salida.P_OUT_ERRORESCONTADOR == 0){
				salida.p_out_RefId = idSolicitudCambio;
			}
		}

		logger.info("FIN::EliminarPersonaCuentaBancaria");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdCuentaBancaria\":1448,\"p_Comentario\":\"Borrado por interfaz\"}";
			EntradaServicioEliminarPersonaCuentaBancaria in = gson.fromJson(json, EntradaServicioEliminarPersonaCuentaBancaria.class);

			EliminarPersonaCuentaBancaria demo = new EliminarPersonaCuentaBancaria();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaEliminarPersonaCuentaBancaria out = demo.logica(con, in);
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
