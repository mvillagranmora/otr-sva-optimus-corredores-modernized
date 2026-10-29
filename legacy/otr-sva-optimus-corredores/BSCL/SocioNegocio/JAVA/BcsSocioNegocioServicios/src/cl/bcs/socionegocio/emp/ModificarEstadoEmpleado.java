package cl.bcs.socionegocio.emp;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: EMP.ModificarEstadoEmpleado
 * Crea la SolicitudCambio de bloqueo/desbloqueo de un Empleado y, según si el concepto
 * CAEEMP requiere autorización, genera el evento de autorización o confirma el cambio
 * de inmediato (estado 'ING'), reutilizando AutorizacionModificacionEstadoEmpleado.confirmarCambioEstado().
 */
public class ModificarEstadoEmpleado implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarEstadoEmpleado.class);

	private static final String ID_CONCEPTO = "CAEEMP";
	private static final String ID_TIPO_ENTIDAD = "EMPLEADO-ESTADO";
	private static final String ESTADO_PENDIENTE = "PEN";
	private static final String ESTADO_INGRESADO = "ING";
	private static final String ESTADO_BLOQUEO = "BLO";
	private static final String ESTADO_DESBLOQUEO = "ACT";

	private final String SQL_COUNT_EXISTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/ModificarEstadoEmpleado_Count_Existe.sql");
	private final String SQL_SELECT_DATOS_EMPLEADO_ESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/ModificarEstadoEmpleado_Select_DatosEmpleadoEstado.sql");
	private final String SQL_SELECT_DATOS_EMPLEADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/emp/sql/ModificarEstadoEmpleado_Select_DatosEmpleado.sql");

	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdEmpleado;
		public String p_IdEstado;
		public String p_IdTipo;
		public String p_Fecha;
		public String p_Comentario;
	}

	public static class Salida {
		public Long P_OUT_REFID;
		public long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public Salida() {
			P_OUT_REFID = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle() {
		}

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class Contador {
		public Long Contador;
	}

	public static class DatosEmpleadoEstado {
		public String IdEstado;
		public String IdTipoBloqueo;
		public String IdTipoDesbloqueo;
		public Timestamp FechaBloqueo;
		public Timestamp FechaDesbloqueo;
		public String ComentarioBloqueo;
		public String ComentarioDesbloqueo;
	}

	public static class DatosEmpleado {
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
	}

	public Salida logica(Connection conn, Entrada in) throws Exception {
		logger.info("INICIO::ModificarEstadoEmpleado");
		Salida salida = new Salida();
		SQL sql = new SQL(); // única instancia reutilizable

		// 1) Validar existencia de empleado
		HashMap<String, Object> paramsExiste = new HashMap<>();
		paramsExiste.put("IdEmpleado", in.p_IdEmpleado);
		List<Contador> cnt = sql.query(conn, SQL_COUNT_EXISTE, paramsExiste, Contador.class);
		Long existe = (cnt != null && !cnt.isEmpty() && cnt.get(0).Contador != null) ? cnt.get(0).Contador : 0L;
		if (existe == 0L) {
			String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa, "EMPLEADO_NO_EXISTE").msg();
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("EMPLEADO_NO_EXISTE", msg));
			salida.P_OUT_ERRORESCONTADOR = 1;
			return salida;
		}

		// 2) Validar que el estado solicitado sea uno de los soportados (BLO/ACT)
		if (!ESTADO_BLOQUEO.equals(in.p_IdEstado) && !ESTADO_DESBLOQUEO.equals(in.p_IdEstado)) {
			String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa, "ESTADO_NO_DEFINIDO").msg();
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ESTADO_NO_DEFINIDO", msg));
			salida.P_OUT_ERRORESCONTADOR = 1;
			return salida;
		}

		// 3) Armar y crear los registros de SolicitudCambio (campos de bloqueo o desbloqueo según corresponda)
		Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(conn);
		List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registros =
			obtenerRegistrosSolicitudCambio(conn, sql, in, idSolicitudCambio);

		CrearRegistroSolicitudCambio servicioCrear = new CrearRegistroSolicitudCambio();
		CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio inCrear =
			new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
		inCrear.p_RegistroSolicitudCambio = registros;
		servicioCrear.logica(conn, inCrear);

		// 4) Determinar si el concepto CAEEMP requiere autorización
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);

		if ("S".equals(requiereAutorizacion)) {
			crearEventoAutorizacion(conn, sql, in, salida);
		} else {
			confirmarSinAutorizacion(conn, in, salida, idSolicitudCambio);
		}

		logger.info("FIN::ModificarEstadoEmpleado");
		return salida;
	}

	private List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> obtenerRegistrosSolicitudCambio(
			Connection conn, SQL sql, Entrada in, Long idSolicitudCambio) throws Exception {

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpleado", in.p_IdEmpleado);
		List<DatosEmpleadoEstado> datos = sql.query(conn, SQL_SELECT_DATOS_EMPLEADO_ESTADO, params, DatosEmpleadoEstado.class);
		DatosEmpleadoEstado datosEmpleado = (datos != null && !datos.isEmpty()) ? datos.get(0) : new DatosEmpleadoEstado();

		String[] campos = ESTADO_BLOQUEO.equals(in.p_IdEstado)
			? new String[] { "IdEstado", "IdTipoBloqueo", "FechaBloqueo", "ComentarioBloqueo" }
			: new String[] { "IdEstado", "IdTipoDesbloqueo", "FechaDesbloqueo", "ComentarioDesbloqueo" };
		String[] valoresNuevos = new String[] { in.p_IdEstado, in.p_IdTipo, in.p_Fecha, in.p_Comentario };

		List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registros = new ArrayList<>();
		for (int i = 0; i < campos.length; i++) {
			ValorAntiguo valorAntiguo = obtenerValorAntiguoSeguro(datosEmpleado, campos[i]);

			CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
			registro.IdSolicitudCambio = idSolicitudCambio;
			registro.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registro.IdEntidadOrigen = String.valueOf(in.p_IdEmpleado);
			registro.CampoModificado = campos[i];
			registro.TipoDato = valorAntiguo.TipoDato;
			registro.ValorAntiguo = valorAntiguo.Valor != null ? valorAntiguo.Valor : "";
			registro.ValorNuevo = valoresNuevos[i];
			registro.IdEstadoAutorizacion = ESTADO_PENDIENTE;

			registros.add(registro);
		}
		return registros;
	}

	/**
	 * Igual a ValorAntiguo.obtenerValorAntiguo(), pero sin NPE cuando el campo Timestamp
	 * (FechaBloqueo/FechaDesbloqueo) está en null en BD (p.ej. primer bloqueo del empleado).
	 */
	private ValorAntiguo obtenerValorAntiguoSeguro(DatosEmpleadoEstado datosEmpleado, String campoModificado) throws Exception {
		ValorAntiguo valorAntiguo = new ValorAntiguo();
		if ("FechaBloqueo".equals(campoModificado) || "FechaDesbloqueo".equals(campoModificado)) {
			Timestamp valor = "FechaBloqueo".equals(campoModificado) ? datosEmpleado.FechaBloqueo : datosEmpleado.FechaDesbloqueo;
			valorAntiguo.TipoDato = "SECONDDATE";
			valorAntiguo.Valor = (valor != null) ? Fecha.toStringDate(valor) : "";
			return valorAntiguo;
		}
		return ValorAntiguo.obtenerValorAntiguo(datosEmpleado, DatosEmpleadoEstado.class, campoModificado);
	}

	private void crearEventoAutorizacion(Connection conn, SQL sql, Entrada in, Salida salida) throws Exception {
		logger.debug("INICIO::ModificarEstadoEmpleado.crearEventoAutorizacion");

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		DatosEmpleado datosEmpleado = obtenerDatosEmpleado(conn, sql, in.p_IdEmpleado);
		String glosa = datosEmpleado.DocumentoIdentidad + " / " + datosEmpleado.NombreCompleto;

		CrearEventoAutorizacion crearEvt = new CrearEventoAutorizacion();
		CrearEventoAutorizacion.ServiceEntrada inEvt = new CrearEventoAutorizacion.ServiceEntrada();
		inEvt.p_IdEmpresa = in.p_IdEmpresa;
		inEvt.p_IdUsuario = in.p_IdUsuario;
		inEvt.p_IdModulo = in.p_IdModulo;
		inEvt.p_UsaReglasNegocio = "N";
		inEvt.p_IdTipoEvento = ID_CONCEPTO;
		inEvt.p_IdModuloSolicita = in.p_IdModulo;
		inEvt.p_IdUsuarioSolicita = in.p_IdUsuario;
		inEvt.p_Fecha = fechaSistema;
		inEvt.p_IdConcepto = ID_CONCEPTO;
		inEvt.p_NumeroOperacionConcepto = String.valueOf(in.p_IdEmpleado);
		inEvt.p_FechaOperacionConcepto = fechaSistema;
		inEvt.p_IdFuncionario = in.p_IdEmpleado;
		inEvt.p_IdCliente = in.p_IdEmpleado;
		inEvt.p_Glosa = glosa;

		CrearEventoAutorizacion.ServiceSalida outEvt = crearEvt.logica(conn, inEvt);

		salida.P_OUT_ADVERTENCIASCONTADOR += (outEvt.p_out_AdvertenciasContador != null) ? outEvt.p_out_AdvertenciasContador : 0L;
		if (outEvt.p_out_AdvertenciasDetalle != null) {
			for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : outEvt.p_out_AdvertenciasDetalle) {
				AdvertenciaDetalle a = new AdvertenciaDetalle();
				a.Codigo = adv.Codigo;
				a.RequiereAutorizacion = adv.RequiereAutorizacion;
				a.Descripcion = adv.Descripcion;
				salida.P_OUT_ADVERTENCIASDETALLE.add(a);
			}
		}

		salida.P_OUT_ERRORESCONTADOR += (outEvt.p_out_ErroresContador != null) ? outEvt.p_out_ErroresContador : 0L;
		if (outEvt.p_out_ErroresDetalle != null) {
			for (CrearEventoAutorizacion.ObjERRORESDETALLE err : outEvt.p_out_ErroresDetalle) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
			}
		}

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION").msg();
			AdvertenciaDetalle ad = new AdvertenciaDetalle();
			ad.Codigo = "REQUIERE_AUTORIZACION";
			ad.RequiereAutorizacion = "S";
			ad.Descripcion = descripcion;
			salida.P_OUT_ADVERTENCIASDETALLE.add(0, ad);
			salida.P_OUT_ADVERTENCIASCONTADOR += 1;
		}

		logger.debug("FIN::ModificarEstadoEmpleado.crearEventoAutorizacion");
	}

	/**
	 * Reutiliza AutorizacionModificacionEstadoEmpleado.confirmarCambioEstado() (aplica los cambios
	 * al Empleado, actualiza la SolicitudCambio y registra el AgregarLog), pasando estado 'ING'
	 * en vez de 'AUT' ya que aquí se ingresa directo, sin pasar por el flujo de autorización.
	 */
	private void confirmarSinAutorizacion(Connection conn, Entrada in, Salida salida, Long idSolicitudCambio) {
		logger.debug("INICIO::ModificarEstadoEmpleado.confirmarSinAutorizacion");

		EntradaAutorizacion entradaAut = new EntradaAutorizacion();
		entradaAut.p_IdEmpresa = in.p_IdEmpresa;
		entradaAut.p_IdUsuario = in.p_IdUsuario;
		entradaAut.p_IdModulo = in.p_IdModulo;
		entradaAut.p_UsaReglasNegocio = in.p_UsaReglasNegocio;

		Evento evento = new Evento();
		evento.NumeroOperacionConcepto = String.valueOf(in.p_IdEmpleado);
		evento.IdTipoEvento = ID_CONCEPTO;
		entradaAut.p_Lista.add(evento);

		AutorizacionModificacionEstadoEmpleado servicioConfirmar = new AutorizacionModificacionEstadoEmpleado();
		SalidaAutorizacion salidaConfirmar = servicioConfirmar.confirmarCambioEstado(conn, entradaAut, ESTADO_INGRESADO);

		salida.P_OUT_ERRORESCONTADOR = (salidaConfirmar.P_OUT_ERRORESCONTADOR != null) ? salidaConfirmar.P_OUT_ERRORESCONTADOR : 0L;
		for (MensajeSalida msg : salidaConfirmar.P_OUT_MENSAJES) {
			if (MensajeSalida.MENSAJE_ERROR.equals(msg.tipo)) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(msg.codigo, msg.descripcion));
			} else if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(msg.tipo)) {
				AdvertenciaDetalle ad = new AdvertenciaDetalle();
				ad.Codigo = msg.codigo;
				ad.Descripcion = msg.descripcion;
				salida.P_OUT_ADVERTENCIASDETALLE.add(ad);
				salida.P_OUT_ADVERTENCIASCONTADOR += 1;
			}
		}

		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			salida.P_OUT_REFID = idSolicitudCambio;
		}

		logger.debug("FIN::ModificarEstadoEmpleado.confirmarSinAutorizacion");
	}

	private DatosEmpleado obtenerDatosEmpleado(Connection conn, SQL sql, Long idEmpleado) throws Exception {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpleado", idEmpleado);
		List<DatosEmpleado> datos = sql.query(conn, SQL_SELECT_DATOS_EMPLEADO, params, DatosEmpleado.class);
		return (datos != null && !datos.isEmpty()) ? datos.get(0) : new DatosEmpleado();
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdEmpleado\":123456,\"p_IdEstado\":\"BLO\",\"p_IdTipo\":\"1\",\"p_Fecha\":\"2026-07-10\",\"p_Comentario\":\"Prueba de bloqueo\"}";
				Entrada entrada = gson.fromJson(json, Entrada.class);

				ModificarEstadoEmpleado servicio = new ModificarEstadoEmpleado();

				System.out.println("ENTRADA");
				System.out.println(gson.toJson(entrada));
				System.out.println("SALIDA");

				Salida salida = servicio.logica(conn, entrada);
				System.out.println(gson.toJson(salida));
				System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);

				conn.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
