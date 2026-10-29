package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDocumentosIdentidad.EntradaConfirmarModificarPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDocumentosIdentidad.SalidaConfirmarModificarPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDocumentosIdentidad implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ModificarPersonaDocumentosIdentidad.class);

	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDocumentosIdentidad_SelectDocumentoIdentidad.sql");
	private final String SQL_INSERT_LOG_GENERAR_DOCUMENTOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");

	private static final String ID_CONCEPTO = "MODDOC";
	private static final String ID_TIPO_ENTIDAD = "DOCUMENTOIDENTIDAD";

	public static class EntradaServicioModificarPersonaDocumentosIdentidad{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public Long p_IdDocumentoIdentidad;
		public List<RegistroCambioCampo> p_RegistroCambio;

		public EntradaServicioModificarPersonaDocumentosIdentidad(){
			this.p_RegistroCambio = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RegistroCambioCampo{
		public String CampoModificado;
		public String ValorNuevo;
	}

	public static class PersonaDocumentoIdentidad{
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String NumeroSerie;
		public String FechaEmision;
		public String FechaVencimiento;
		public String IdEstado;
		public String DocumentoIdentidadPersona;
		public String NombreCompleto;
	}

	public static class SalidaModificarPersonaDocumentosIdentidad extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaModificarPersonaDocumentosIdentidad(){
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
		EntradaServicioModificarPersonaDocumentosIdentidad in = gson.fromJson(jin.toString(), EntradaServicioModificarPersonaDocumentosIdentidad.class);
		SalidaModificarPersonaDocumentosIdentidad out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaModificarPersonaDocumentosIdentidad logica(Connection conn, EntradaServicioModificarPersonaDocumentosIdentidad in) throws Exception{
		logger.info("INICIO::ModificarPersonaDocumentosIdentidad");
		SalidaModificarPersonaDocumentosIdentidad salida = new SalidaModificarPersonaDocumentosIdentidad();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDocumentoIdentidad);

		HashMap<String, Object> paramsDocumento = new HashMap<>();
		paramsDocumento.put("IdDocumentoIdentidad", new ArrayList<>(Arrays.asList(in.p_IdDocumentoIdentidad)));
		List<PersonaDocumentoIdentidad> documentos = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, paramsDocumento, PersonaDocumentoIdentidad.class);
		if(documentos.isEmpty()){
			salida.addError("UI_DOCUMENTO_IDENTIDAD_NO_EXISTE", "UI_DOCUMENTO_IDENTIDAD_NO_EXISTE");
			return salida;
		}
		PersonaDocumentoIdentidad documento = documentos.get(0);

		// NOTA: el SP original valida duplicados de DocumentoIdentidad con "IF (v_documentoIdentidad <> NULL)",
		// que en SQL estandar nunca es verdadero (comparar con NULL da UNKNOWN) -> esa validacion nunca se
		// ejecuta en produccion hoy. Confirmado con el usuario: se replica fielmente el comportamiento actual
		// (sin validar duplicados), no se corrige el bug.

		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, in.p_IdEmpresa);
		String fechaEventoAutorizacion = fechaHoraSistema != null && fechaHoraSistema.length() >= 10 ? fechaHoraSistema.substring(0, 10) : fechaHoraSistema;

		Long idSolicitudCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L).get(0);

		List<RegistroSolicitudCambio> registrosSolicitudCambio = new ArrayList<>();
		for(RegistroCambioCampo campo : in.p_RegistroCambio){
			String valorAntiguo = obtenerValorAntiguo(documento, campo.CampoModificado);
			if(valorAntiguo == null){
				continue; // campo desconocido: el SP tampoco lo procesa (no matchea contra SYS.TABLE_COLUMNS)
			}
			RegistroSolicitudCambio registro = new RegistroSolicitudCambio();
			registro.IdSolicitudCambio = idSolicitudCambio;
			registro.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registro.IdEntidadOrigen = idEntidadOrigen;
			registro.CampoModificado = campo.CampoModificado;
			registro.TipoDato = "NVARCHAR";
			registro.ValorAntiguo = valorAntiguo;
			registro.ValorNuevo = campo.ValorNuevo != null ? campo.ValorNuevo : "";
			registro.IdEstadoAutorizacion = "PEN";
			registrosSolicitudCambio.add(registro);
		}

		CrearRegistroSolicitudCambio crearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistro = new EntradaCrearRegistroSolicitudCambio();
		entradaCrearRegistro.p_RegistroSolicitudCambio = registrosSolicitudCambio;
		crearRegistroSolicitudCambio.logica(conn, entradaCrearRegistro);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);

		if(requiereAutorizacion.equals("S") && !"N".equals(in.p_UsaReglasNegocio)){
			logger.debug("Requiere autorizacion, crear evento");

			String glosa = documento.DocumentoIdentidadPersona + " / " + documento.NombreCompleto + " / IdDocMod: " + in.p_IdDocumentoIdentidad;

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
			evento.Fecha = fechaEventoAutorizacion;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = idEntidadOrigen;
			evento.FechaOperacionConcepto = fechaEventoAutorizacion;
			evento.IdCliente = documento.IdPersona;
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
			logger.debug("No requiere autorizacion, confirma modificacion");

			List<RegistroSolicitudCambio> cambiosReales = registrosSolicitudCambio.stream()
					.filter(r -> !r.ValorAntiguo.equals(r.ValorNuevo))
					.collect(Collectors.toList());
			if(!cambiosReales.isEmpty()){
				String fechaSistemaCorta = fechaHoraSistema != null && fechaHoraSistema.length() >= 10 ? fechaHoraSistema.substring(0, 10) : fechaHoraSistema;
				List<Long> idsLog = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) cambiosReales.size());
				List<EntidadLogGenerarContrato> paramsLog = new ArrayList<>();
				for(int i = 0; i < cambiosReales.size(); i++){
					RegistroSolicitudCambio cambio = cambiosReales.get(i);
					EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
					logGenerarContrato.Idlog = idsLog.get(i);
					logGenerarContrato.CodigoFormulario = "DOCUMENTO_IDENTIDAD_";
					logGenerarContrato.CampoBackend = cambio.CampoModificado;
					logGenerarContrato.IdEmpresa = in.p_IdEmpresa;
					logGenerarContrato.IdCliente = 0L;
					logGenerarContrato.IdPersona = in.p_IdPersona;
					logGenerarContrato.IdUsuario = in.p_IdUsuario;
					logGenerarContrato.FechaSistema = fechaSistemaCorta;
					logGenerarContrato.IdPrograma = 0L;
					logGenerarContrato.Estado = "PEN";
					logGenerarContrato.Mensaje = "Persona - ModificarPersonaDocumentosIdentidad.sql";
					paramsLog.add(logGenerarContrato);
				}
				sql.batch(conn, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, paramsLog, EntidadLogGenerarContrato.class);
			}

			ConfirmarModificarPersonaDocumentosIdentidad confirmar = new ConfirmarModificarPersonaDocumentosIdentidad();
			EntradaConfirmarModificarPersonaDocumentosIdentidad entradaConfirmar = new EntradaConfirmarModificarPersonaDocumentosIdentidad();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdDocumentoIdentidad = in.p_IdDocumentoIdentidad;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			SalidaConfirmarModificarPersonaDocumentosIdentidad salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
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

		logger.info("FIN::ModificarPersonaDocumentosIdentidad");
		return salida;
	}

	private String obtenerValorAntiguo(PersonaDocumentoIdentidad documento, String campoModificado){
		if(campoModificado == null){
			return null;
		}
		switch(campoModificado){
			case "IdTipoDocumentoIdentidad": return documento.IdTipoDocumentoIdentidad;
			case "DocumentoIdentidad": return documento.DocumentoIdentidad;
			case "NumeroSerie": return documento.NumeroSerie;
			case "FechaEmision": return documento.FechaEmision;
			case "FechaVencimiento": return documento.FechaVencimiento;
			case "IdEstado": return documento.IdEstado;
			default: return null;
		}
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":1790725,\"p_IdDocumentoIdentidad\":1790725,\"p_RegistroCambio\":[{\"CampoModificado\":\"NumeroSerie\",\"ValorNuevo\":\"TEST123\"}]}";
			EntradaServicioModificarPersonaDocumentosIdentidad in = gson.fromJson(json, EntradaServicioModificarPersonaDocumentosIdentidad.class);

			ModificarPersonaDocumentosIdentidad demo = new ModificarPersonaDocumentosIdentidad();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaModificarPersonaDocumentosIdentidad out = demo.logica(con, in);
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
