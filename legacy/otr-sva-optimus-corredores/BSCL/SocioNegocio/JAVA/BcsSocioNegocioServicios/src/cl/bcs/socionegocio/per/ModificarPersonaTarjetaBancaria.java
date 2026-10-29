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
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaTarjetaBancaria.EntradaConfirmarModificarPersonaTarjetaBancaria;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaTarjetaBancaria.SalidaConfirmarModificarPersonaTarjetaBancaria;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaTarjetaBancaria implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ModificarPersonaTarjetaBancaria.class);

	private final String SQL_SELECT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaTarjetaBancaria_SelectTarjetaBancaria.sql");
	private final String SQL_INSERT_LOG_GENERAR_DOCUMENTOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");

	private static final String ID_CONCEPTO = "MODTJB";
	private static final String ID_TIPO_ENTIDAD = "TARJETABANCARIA";

	public static class EntradaServicioModificarPersonaTarjetaBancaria{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdPersona;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdTarjetaBancaria;
		public List<RegistroCambioCampo> p_RegistroCambio;

		public EntradaServicioModificarPersonaTarjetaBancaria(){
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

	public static class PersonaTarjetaBancaria{
		public Long IdTarjetaBancaria;
		public Long IdPersona;
		public String IdBanco;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String FechaVencimiento;
		public String CodigoVerificacion;
		public String IdEstado;
		public String DocumentoIdentidadPersona;
		public String NombreCompleto;
	}

	public static class SalidaModificarPersonaTarjetaBancaria extends SalidaEstandar{
		public long p_out_RefId;

		public SalidaModificarPersonaTarjetaBancaria(){
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
		EntradaServicioModificarPersonaTarjetaBancaria in = gson.fromJson(jin.toString(), EntradaServicioModificarPersonaTarjetaBancaria.class);
		SalidaModificarPersonaTarjetaBancaria out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaModificarPersonaTarjetaBancaria logica(Connection conn, EntradaServicioModificarPersonaTarjetaBancaria in) throws Exception{
		logger.info("INICIO::ModificarPersonaTarjetaBancaria");
		SalidaModificarPersonaTarjetaBancaria salida = new SalidaModificarPersonaTarjetaBancaria();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdTarjetaBancaria);

		HashMap<String, Object> paramsTarjeta = new HashMap<>();
		paramsTarjeta.put("IdTarjetaBancaria", new ArrayList<>(Arrays.asList(in.p_IdTarjetaBancaria)));
		List<PersonaTarjetaBancaria> tarjetas = sql.query(conn, SQL_SELECT_TARJETA_BANCARIA, paramsTarjeta, PersonaTarjetaBancaria.class);
		if(tarjetas.isEmpty()){
			salida.addError("ENTIDAD_NO_EXISTE", "ENTIDAD_NO_EXISTE");
			return salida;
		}
		PersonaTarjetaBancaria tarjeta = tarjetas.get(0);

		String fechaHoraSistema = Utils.getFechaHoraSistema(conn, in.p_IdEmpresa);
		String fechaEventoAutorizacion = fechaHoraSistema != null && fechaHoraSistema.length() >= 10 ? fechaHoraSistema.substring(0, 10) : fechaHoraSistema;

		Long idSolicitudCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L).get(0);

		List<RegistroSolicitudCambio> registrosSolicitudCambio = new ArrayList<>();
		for(RegistroCambioCampo campo : in.p_RegistroCambio){
			String valorAntiguo = obtenerValorAntiguo(tarjeta, campo.CampoModificado);
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

			// NOTA: el SP original usa el literal "IdCta:" en la glosa (probablemente copiado de CuentaBancaria),
			// no "IdTarjeta:". Se replica tal cual para fidelidad con el comportamiento actual.
			String glosa = tarjeta.DocumentoIdentidadPersona + " / " + tarjeta.NombreCompleto + " / IdCta: " + in.p_IdTarjetaBancaria;

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
			evento.IdCliente = tarjeta.IdPersona;
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

			ConfirmarModificarPersonaTarjetaBancaria confirmar = new ConfirmarModificarPersonaTarjetaBancaria();
			EntradaConfirmarModificarPersonaTarjetaBancaria entradaConfirmar = new EntradaConfirmarModificarPersonaTarjetaBancaria();
			entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmar.p_IdModulo = in.p_IdModulo;
			entradaConfirmar.p_UsaReglasNegocio = "N";
			entradaConfirmar.p_IdTarjetaBancaria = in.p_IdTarjetaBancaria;
			entradaConfirmar.p_IdEstadoAutorizacion = "ING";

			SalidaConfirmarModificarPersonaTarjetaBancaria salidaConfirmar = confirmar.logica(conn, entradaConfirmar);
			for(ErrorDetalle error : salidaConfirmar.P_OUT_ERRORESDETALLE){
				salida.addError(error.Codigo, error.Descripcion);
			}
			for(AdvertenciaDetalle advertencia : salidaConfirmar.P_OUT_ADVERTENCIASDETALLE){
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}

			// El SP original inserta el log de generacion de contratos DESPUES de confirmar (a diferencia de los
			// otros hermanos, que lo hacen antes) - se replica en el mismo orden, aunque el resultado es
			// equivalente porque ambos ocurren dentro de la misma transaccion.
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
					logGenerarContrato.CodigoFormulario = "TARJETA_BANCARIA_";
					logGenerarContrato.CampoBackend = cambio.CampoModificado;
					logGenerarContrato.IdEmpresa = in.p_IdEmpresa;
					logGenerarContrato.IdCliente = 0L;
					logGenerarContrato.IdPersona = in.p_IdPersona;
					logGenerarContrato.IdUsuario = in.p_IdUsuario;
					logGenerarContrato.FechaSistema = fechaSistemaCorta;
					logGenerarContrato.IdPrograma = 0L;
					logGenerarContrato.Estado = "PEN";
					logGenerarContrato.Mensaje = "Persona - ModificarPersonaTarjetaBancaria.sql";
					paramsLog.add(logGenerarContrato);
				}
				sql.batch(conn, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, paramsLog, EntidadLogGenerarContrato.class);
			}

			// El SP original solo asigna p_out_RefId en esta rama (no requiere autorizacion) y solo si no hay errores;
			// en la rama de autorizacion siempre hace SIGNAL SALIDA_CONTROLADA antes de llegar a esa asignacion,
			// por lo que p_out_RefId queda en 0 en cualquier otro caso.
			if(salida.P_OUT_ERRORESCONTADOR == 0){
				salida.p_out_RefId = idSolicitudCambio;
			}
		}

		logger.info("FIN::ModificarPersonaTarjetaBancaria");
		return salida;
	}

	private String obtenerValorAntiguo(PersonaTarjetaBancaria tarjeta, String campoModificado){
		if(campoModificado == null){
			return null;
		}
		switch(campoModificado){
			case "IdBanco": return tarjeta.IdBanco;
			case "IdTipoTarjetaBancaria": return tarjeta.IdTipoTarjetaBancaria;
			case "NumeroTarjetaBancaria": return tarjeta.NumeroTarjetaBancaria;
			case "FechaVencimiento": return tarjeta.FechaVencimiento;
			case "CodigoVerificacion": return tarjeta.CodigoVerificacion;
			case "IdEstado": return tarjeta.IdEstado;
			default: return null;
		}
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":1790725,\"p_IdTarjetaBancaria\":10056,\"p_RegistroCambio\":[{\"CampoModificado\":\"CodigoVerificacion\",\"ValorNuevo\":\"123\"}]}";
			EntradaServicioModificarPersonaTarjetaBancaria in = gson.fromJson(json, EntradaServicioModificarPersonaTarjetaBancaria.class);

			ModificarPersonaTarjetaBancaria demo = new ModificarPersonaTarjetaBancaria();
			System.out.println("ENTRADA");
			System.out.println(in.toString());

			try {
				SalidaModificarPersonaTarjetaBancaria out = demo.logica(con, in);
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
