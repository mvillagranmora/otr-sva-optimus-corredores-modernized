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
import cl.bcs.socionegocio.per.ConfirmarEliminarPersonaDireccionFisica.EntradaConfirmarEliminarPersonaDireccionFisica;
import cl.bcs.socionegocio.per.ConfirmarEliminarPersonaDireccionFisica.SalidaConfirmarEliminarPersonaDireccionFisica;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarPersonaDireccionFisica implements JavaCall{

	private static final Logger logger = LogManager.getLogger(EliminarPersonaDireccionFisica.class);

	private final String SQL_SELECT_PERSONA_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionFisica_SelectPersonaDireccionFisica.sql");

	private static final String ID_CONCEPTO = "BORDIF";
	private static final String ID_TIPO_ENTIDAD = "DIRECCIONFISICA-BORRAR";

	public static class EntradaServicioEliminarPersonaDireccionFisica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionFisica;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PersonaDireccionFisica{
		public Long IdDireccionFisica;
		public Long IdPersona;
		public String Borrado;
		public String Predeterminado;
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class SalidaEliminarPersonaDireccionFisica extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaEliminarPersonaDireccionFisica(){
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
		EntradaServicioEliminarPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaServicioEliminarPersonaDireccionFisica.class);
		SalidaEliminarPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaEliminarPersonaDireccionFisica logica(Connection conn, EntradaServicioEliminarPersonaDireccionFisica in) throws Exception{
		logger.info("INICIO::EliminarPersonaDireccionFisica");
		SalidaEliminarPersonaDireccionFisica salida = new SalidaEliminarPersonaDireccionFisica();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDireccionFisica);

		HashMap<String, Object> paramsDireccion = new HashMap<>();
		paramsDireccion.put("IdDireccionFisica", new java.util.ArrayList<>(Arrays.asList(in.p_IdDireccionFisica)));
		List<PersonaDireccionFisica> direcciones = sql.query(conn, SQL_SELECT_PERSONA_DIRECCION_FISICA, paramsDireccion, PersonaDireccionFisica.class);
		if(direcciones.isEmpty()){
			salida.addError("ENTIDAD_NO_EXISTE", "ENTIDAD_NO_EXISTE");
			return salida;
		}
		PersonaDireccionFisica direccion = direcciones.get(0);

		if("S".equals(direccion.Predeterminado)){
			salida.addError("ERROR_ELIMINAR_DIRFISICA_PREDETERMINADA", "No se puede eliminar una direccion fisica predeterminada");
			return salida;
		}

		Long idSolicitudCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L).get(0);

		RegistroSolicitudCambio registroBorrado = new RegistroSolicitudCambio();
		registroBorrado.IdSolicitudCambio = idSolicitudCambio;
		registroBorrado.IdTipoEntidad = ID_TIPO_ENTIDAD;
		registroBorrado.IdEntidadOrigen = idEntidadOrigen;
		registroBorrado.CampoModificado = "Borrado";
		registroBorrado.TipoDato = "NVARCHAR";
		registroBorrado.ValorAntiguo = direccion.Borrado;
		registroBorrado.ValorNuevo = "S";
		registroBorrado.IdEstadoAutorizacion = "PEN";

		CrearRegistroSolicitudCambio crearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistro = new EntradaCrearRegistroSolicitudCambio();
		entradaCrearRegistro.p_RegistroSolicitudCambio = Arrays.asList(registroBorrado);
		crearRegistroSolicitudCambio.logica(conn, entradaCrearRegistro);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);

		if(requiereAutorizacion.equals("S") && !"N".equals(in.p_UsaReglasNegocio)){
			logger.debug("Requiere autorizacion, crear evento");
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			String glosa = direccion.DocumentoIdentidad + " / " + direccion.NombreCompleto + " / IdDirFis: " + in.p_IdDireccionFisica;

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
			evento.IdCliente = direccion.IdPersona;
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
			ConfirmarEliminarPersonaDireccionFisica confirmar = new ConfirmarEliminarPersonaDireccionFisica();
			EntradaConfirmarEliminarPersonaDireccionFisica entradaConfirmar = new EntradaConfirmarEliminarPersonaDireccionFisica();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdDireccionFisica = in.p_IdDireccionFisica;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			SalidaConfirmarEliminarPersonaDireccionFisica salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
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

		logger.info("FIN::EliminarPersonaDireccionFisica");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionFisica\":2469693}";
			EntradaServicioEliminarPersonaDireccionFisica in = gson.fromJson(json, EntradaServicioEliminarPersonaDireccionFisica.class);

			EliminarPersonaDireccionFisica demo = new EliminarPersonaDireccionFisica();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaEliminarPersonaDireccionFisica out = demo.logica(con, in);
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
