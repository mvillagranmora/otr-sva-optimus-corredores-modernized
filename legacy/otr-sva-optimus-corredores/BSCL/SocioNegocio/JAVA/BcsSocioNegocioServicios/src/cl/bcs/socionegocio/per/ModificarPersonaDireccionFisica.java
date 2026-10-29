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
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDireccionFisica.EntradaConfirmarModificarPersonaDireccionFisica;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDireccionFisica.SalidaConfirmarModificarPersonaDireccionFisica;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDireccionFisica implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ModificarPersonaDireccionFisica.class);

	private final String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionFisica_SelectDireccionFisica.sql");
	private final String SQL_INSERT_LOG_GENERAR_DOCUMENTOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");
	private final String SQL_UPDATE_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionFisica_UpdatePaisResidencia.sql");

	private static final String ID_CONCEPTO = "MODDIF";
	private static final String ID_TIPO_ENTIDAD = "DIRECCIONFISICA";

	public static class EntradaServicioModificarPersonaDireccionFisica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionFisica;
		public List<RegistroCambioCampo> p_RegistroCambio;

		public EntradaServicioModificarPersonaDireccionFisica(){
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

	public static class PersonaDireccionFisica{
		public Long IdDireccionFisica;
		public Long IdPersona;
		public String Calle;
		public String Numero;
		public String Departamento;
		public String Sector;
		public String CodigoPostal;
		public String IdCiudad;
		public String IdDivisionPais;
		public String CodigoProposito;
		public String Comentario;
		public String IdEstado;
		public String CodigoCompuesto;
		public String DocumentoIdentidadPersona;
		public String NombreCompleto;
	}

	public static class DireccionFisicaIdUpdate{
		public Long p_IdDireccionFisica;
	}

	public static class SalidaModificarPersonaDireccionFisica extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaModificarPersonaDireccionFisica(){
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
		EntradaServicioModificarPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaServicioModificarPersonaDireccionFisica.class);
		SalidaModificarPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaModificarPersonaDireccionFisica logica(Connection conn, EntradaServicioModificarPersonaDireccionFisica in) throws Exception{
		logger.info("INICIO::ModificarPersonaDireccionFisica");
		SalidaModificarPersonaDireccionFisica salida = new SalidaModificarPersonaDireccionFisica();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDireccionFisica);

		HashMap<String, Object> paramsDireccion = new HashMap<>();
		paramsDireccion.put("IdDireccionFisica", new ArrayList<>(Arrays.asList(in.p_IdDireccionFisica)));
		List<PersonaDireccionFisica> direcciones = sql.query(conn, SQL_SELECT_DIRECCION_FISICA, paramsDireccion, PersonaDireccionFisica.class);
		if(direcciones.isEmpty()){
			salida.addError("ENTIDAD_NO_EXISTE", "ENTIDAD_NO_EXISTE");
			return salida;
		}
		PersonaDireccionFisica direccion = direcciones.get(0);

		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, in.p_IdEmpresa);
		String fechaEventoAutorizacion = fechaHoraSistema != null && fechaHoraSistema.length() >= 10 ? fechaHoraSistema.substring(0, 10) : fechaHoraSistema;

		Long idSolicitudCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L).get(0);

		List<RegistroSolicitudCambio> registrosSolicitudCambio = new ArrayList<>();
		for(RegistroCambioCampo campo : in.p_RegistroCambio){
			String valorAntiguo = obtenerValorAntiguo(direccion, campo.CampoModificado);
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

			String glosa = direccion.DocumentoIdentidadPersona + " / " + direccion.NombreCompleto + " / IdDirFis: " + in.p_IdDireccionFisica;

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
					logGenerarContrato.CodigoFormulario = "DIRECCION_FISICA_";
					logGenerarContrato.CampoBackend = cambio.CampoModificado;
					logGenerarContrato.IdEmpresa = in.p_IdEmpresa;
					logGenerarContrato.IdCliente = 0L;
					logGenerarContrato.IdPersona = direccion.IdPersona;
					logGenerarContrato.IdUsuario = in.p_IdUsuario;
					logGenerarContrato.FechaSistema = fechaSistemaCorta;
					logGenerarContrato.IdPrograma = 0L;
					logGenerarContrato.Estado = "PEN";
					logGenerarContrato.Mensaje = "Persona - ModificarPersonaDireccionFisica.sql";
					paramsLog.add(logGenerarContrato);
				}
				sql.batch(conn, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, paramsLog, EntidadLogGenerarContrato.class);
			}

			ConfirmarModificarPersonaDireccionFisica confirmar = new ConfirmarModificarPersonaDireccionFisica();
			EntradaConfirmarModificarPersonaDireccionFisica entradaConfirmar = new EntradaConfirmarModificarPersonaDireccionFisica();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdDireccionFisica = in.p_IdDireccionFisica;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			SalidaConfirmarModificarPersonaDireccionFisica salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
			for(ErrorDetalle error : salidaConfirmar.P_OUT_ERRORESDETALLE){
				salida.addError(error.Codigo, error.Descripcion);
			}
			for(AdvertenciaDetalle advertencia : salidaConfirmar.P_OUT_ADVERTENCIASDETALLE){
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}

			// El SP original solo asigna p_out_RefId y ejecuta la actualizacion de "Pais Residencia" en esta rama
			// (no requiere autorizacion) y solo si no hay errores; en cualquier otro caso (autorizacion requerida,
			// o error en la confirmacion) el SP hace SIGNAL SALIDA_CONTROLADA antes de llegar a ambas acciones,
			// que quedan fuera del bloque protegido por el EXIT HANDLER y por lo tanto nunca se ejecutan.
			if(salida.P_OUT_ERRORESCONTADOR == 0){
				salida.p_out_RefId = idSolicitudCambio;

				DireccionFisicaIdUpdate updatePais = new DireccionFisicaIdUpdate();
				updatePais.p_IdDireccionFisica = in.p_IdDireccionFisica;
				sql.batch(conn, SQL_UPDATE_PAIS_RESIDENCIA, Arrays.asList(updatePais), DireccionFisicaIdUpdate.class);
			}
		}

		logger.info("FIN::ModificarPersonaDireccionFisica");
		return salida;
	}

	private String obtenerValorAntiguo(PersonaDireccionFisica direccion, String campoModificado){
		if(campoModificado == null){
			return null;
		}
		switch(campoModificado){
			case "Calle": return direccion.Calle;
			case "Numero": return direccion.Numero;
			case "Departamento": return direccion.Departamento;
			case "Sector": return direccion.Sector;
			case "CodigoPostal": return direccion.CodigoPostal;
			case "IdCiudad": return direccion.IdCiudad;
			case "IdDivisionPais": return direccion.IdDivisionPais;
			case "CodigoProposito": return direccion.CodigoProposito;
			case "Comentario": return direccion.Comentario;
			case "IdEstado": return direccion.IdEstado;
			case "CodigoCompuesto": return direccion.CodigoCompuesto;
			default: return null;
		}
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionFisica\":2469693,\"p_RegistroCambio\":[{\"CampoModificado\":\"Comentario\",\"ValorNuevo\":\"TEST\"}]}";
			EntradaServicioModificarPersonaDireccionFisica in = gson.fromJson(json, EntradaServicioModificarPersonaDireccionFisica.class);

			ModificarPersonaDireccionFisica demo = new ModificarPersonaDireccionFisica();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaModificarPersonaDireccionFisica out = demo.logica(con, in);
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
