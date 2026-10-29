package cl.bcs.plataforma.com;
 
import java.sql.Connection;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.HashMap;

import java.util.List;
 
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
 
import cl.bcs.plataforma.aut.BcsAutorizaciones;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.utils.SQL;
 
/**

* Autoriza/Rechaza eventos de autorización pendientes de COM.DetallePerfilCobros (concepto MDPCOB).

* Migrado de: COM.ConfirmarModificarPerfilCobrosDetalleMasivo / COM.RechazaAutorizacionPerfilCobroDetalleMasivo (rama MDPCOB)

* Invocado por el motor de autorizaciones (ModificarEstadoEventoAutorizacionMasivo) vía

* AUT.TipoEvento.JavaAutorizacion = "cl.bcs.plataforma.com.ConfirmarModificarPerfilCobrosDetalleMasivo".

*/

public class ConfirmarModificarPerfilCobrosDetalleMasivo implements BcsAutorizaciones {
 
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPerfilCobrosDetalleMasivo.class);

	private static final String TIPO_ENTIDAD = "PERFILCOBRODETALLE";
 
	private final SQL sql = new SQL();
 
	// Reusa los mismos SQL de la clase individual (mismo paquete cl/bcs/plataforma/com/sql/)

	private final String SQL_SELECT_SOLICITUDCAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Select_SolicitudCambio.sql");

	private final String SQL_UPDATE_IDVALORCOBRO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdValorCobro.sql");

	private final String SQL_UPDATE_IDBOLSA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdBolsa.sql");

	private final String SQL_UPDATE_IDTIPOPRODUCTO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdTipoProducto.sql");

	private final String SQL_UPDATE_IDTIPOOPERACION = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdTipoOperacion.sql");

	private final String SQL_UPDATE_IDCANALINGRESO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdCanalIngreso.sql");

	private final String SQL_UPDATE_IDMONEDA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdMoneda.sql");

	private final String SQL_UPDATE_DIASPLAZO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_DiasPlazo.sql");

	private final String SQL_UPDATE_PRIORIDAD = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_Prioridad.sql");

	private final String SQL_UPDATE_IDESTADO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdEstado.sql");
 
	// SQL nuevo, solo para rechazo

	private final String SQL_UPDATE_ESTADO_RECHAZO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalleMasivo_Update_EstadoRechazo.sql");
 
	public static class SolicitudCambioData {

		public Long IdSolicitudCambio;

		public String IdEntidadOrigen;

		public String IdTipoEntidad;

		public String CampoModificado;

		public String ValorAntiguo;

		public String ValorNuevo;

		public String IdEstadoAutorizacion;

	}
 
	public static class UpdateCampo {

		public String ValorNuevo;

		public Long IdDetallePerfilCobros;

	}
 
	public static class UpdateEstadoRechazo {

		public String IdEstadoAutorizacion;

		public Long IdDetallePerfilCobros;

	}
 
	@Override

	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
 
		logger.info("INICIO::ConfirmarModificarPerfilCobrosDetalleMasivo.autorizarEvento");
 
		SalidaAutorizacion salida = new SalidaAutorizacion();

		List<EntidadOrigen> entidadesOrigen = new ArrayList<>();
 
		try {
 
			for (cl.bcs.plataforma.aut.pojo.Evento evento : entrada.p_Lista) {
 
				Long idDetallePerfilCobros = Long.valueOf(evento.NumeroOperacionConcepto);

				String idEntidadOrigen = String.valueOf(idDetallePerfilCobros);
 
				HashMap<String, Object> paramsValidar = new HashMap<>();

				paramsValidar.put("p_IdEntidadOrigen", idEntidadOrigen);

				paramsValidar.put("p_IdTipoEntidad", TIPO_ENTIDAD);
 
				List<SolicitudCambioData> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUDCAMBIO, paramsValidar, SolicitudCambioData.class);
 
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();

				resultado.evento = evento;
 
				if (solicitudes == null || solicitudes.isEmpty()) {

					logger.warn("SOLICITUD_NO_EXISTE para IdDetallePerfilCobros: " + idEntidadOrigen);

					resultado.resultado = BcsAutorizaciones.ERROR_AUTORIZACION;

					salida.P_OUT_RESULTADO.add(resultado);

					salida.P_OUT_ERRORESCONTADOR++;

					continue;

				}
 
				for (SolicitudCambioData solicitud : solicitudes) {
 
					UpdateCampo update = new UpdateCampo();

					update.ValorNuevo = solicitud.ValorNuevo;

					update.IdDetallePerfilCobros = idDetallePerfilCobros;

					List<UpdateCampo> updateList = Arrays.asList(update);
 
					switch (solicitud.CampoModificado) {

						case "IdValorCobro":

							sql.batch(conn, SQL_UPDATE_IDVALORCOBRO, updateList, UpdateCampo.class);

							break;

						case "IdBolsa":

							sql.batch(conn, SQL_UPDATE_IDBOLSA, updateList, UpdateCampo.class);

							break;

						case "IdTipoProducto":

							sql.batch(conn, SQL_UPDATE_IDTIPOPRODUCTO, updateList, UpdateCampo.class);

							break;

						case "IdTipoOperacion":

							sql.batch(conn, SQL_UPDATE_IDTIPOOPERACION, updateList, UpdateCampo.class);

							break;

						case "IdCanalIngreso":

							sql.batch(conn, SQL_UPDATE_IDCANALINGRESO, updateList, UpdateCampo.class);

							break;

						case "IdMoneda":

							sql.batch(conn, SQL_UPDATE_IDMONEDA, updateList, UpdateCampo.class);

							break;

						case "DiasPlazo":

							sql.batch(conn, SQL_UPDATE_DIASPLAZO, updateList, UpdateCampo.class);

							break;

						case "Prioridad":

							sql.batch(conn, SQL_UPDATE_PRIORIDAD, updateList, UpdateCampo.class);

							break;

						case "IdEstado":

							sql.batch(conn, SQL_UPDATE_IDESTADO, updateList, UpdateCampo.class);

							break;

						default:

							break;

					}

				}
 
				EntidadOrigen eo = new EntidadOrigen();

				eo.IdEntidadOrigen = idEntidadOrigen;

				eo.IdTipoEntidad = TIPO_ENTIDAD;

				entidadesOrigen.add(eo);
 
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;

				salida.P_OUT_RESULTADO.add(resultado);

			}
 
			if (!entidadesOrigen.isEmpty()) {

				ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaModificar = new EntradaModificarEstado();

				entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;

				entradaModificar.p_IdUsuario = entrada.p_IdUsuario;

				entradaModificar.p_IdModulo = entrada.p_IdModulo;

				entradaModificar.p_EntidadOrigen = entidadesOrigen;

				entradaModificar.p_IdEstadoAutorizacion = "AUT";
 
				SalidaModificarEstado salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
				if (salidaModificar.p_out_ErroresContador > 0) {

					salida.P_OUT_ERRORESCONTADOR += salidaModificar.p_out_ErroresContador;

				}

			}
 
		} catch (Exception e) {

			logger.error("Error en autorizarEvento: " + e.getMessage(), e);

			salida.P_OUT_ERRORESCONTADOR++;

		}
 
		logger.info("FIN::ConfirmarModificarPerfilCobrosDetalleMasivo.autorizarEvento");

		return salida;

	}
 
	@Override

	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
 
		logger.info("INICIO::ConfirmarModificarPerfilCobrosDetalleMasivo.rechazarEvento");
 
		SalidaAutorizacion salida = new SalidaAutorizacion();

		List<EntidadOrigen> entidadesOrigen = new ArrayList<>();
 
		try {
 
			for (cl.bcs.plataforma.aut.pojo.Evento evento : entrada.p_Lista) {
 
				Long idDetallePerfilCobros = Long.valueOf(evento.NumeroOperacionConcepto);

				String idEntidadOrigen = String.valueOf(idDetallePerfilCobros);
 
				EntidadOrigen eo = new EntidadOrigen();

				eo.IdEntidadOrigen = idEntidadOrigen;

				eo.IdTipoEntidad = TIPO_ENTIDAD;

				entidadesOrigen.add(eo);
 
				UpdateEstadoRechazo updateRechazo = new UpdateEstadoRechazo();

				updateRechazo.IdEstadoAutorizacion = "RZD";

				updateRechazo.IdDetallePerfilCobros = idDetallePerfilCobros;

				sql.batch(conn, SQL_UPDATE_ESTADO_RECHAZO, Arrays.asList(updateRechazo), UpdateEstadoRechazo.class);
 
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();

				resultado.evento = evento;

				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;

				salida.P_OUT_RESULTADO.add(resultado);

			}
 
			if (!entidadesOrigen.isEmpty()) {

				ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();

				EntradaModificarEstado entradaModificar = new EntradaModificarEstado();

				entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;

				entradaModificar.p_IdUsuario = entrada.p_IdUsuario;

				entradaModificar.p_IdModulo = entrada.p_IdModulo;

				entradaModificar.p_EntidadOrigen = entidadesOrigen;

				entradaModificar.p_IdEstadoAutorizacion = "RZD";
 
				SalidaModificarEstado salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
				if (salidaModificar.p_out_ErroresContador > 0) {

					salida.P_OUT_ERRORESCONTADOR += salidaModificar.p_out_ErroresContador;

				}

			}
 
		} catch (Exception e) {

			logger.error("Error en rechazarEvento: " + e.getMessage(), e);

			salida.P_OUT_ERRORESCONTADOR++;

		}
 
		logger.info("FIN::ConfirmarModificarPerfilCobrosDetalleMasivo.rechazarEvento");

		return salida;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
 
			ConfirmarModificarPerfilCobrosDetalleMasivo demo = new ConfirmarModificarPerfilCobrosDetalleMasivo();
 
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 76770L;
			entrada.p_IdModulo = "PLA";
			entrada.p_UsaReglasNegocio = "N";
 
			cl.bcs.plataforma.aut.pojo.Evento evento = new cl.bcs.plataforma.aut.pojo.Evento();
			evento.NumeroOperacionConcepto = "131946";
			entrada.p_Lista.add(evento);
 
			SalidaAutorizacion salida = demo.autorizarEvento(conn, entrada);
 
			System.out.println(salida.toString());
 
			conn.rollback();
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		System.out.println("== END ==");
	}
} 