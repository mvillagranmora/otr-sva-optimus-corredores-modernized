package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

/**
 * Autoriza/rechaza eventos de autorización pendientes de COM.DetallePerfilProductos (concepto NDPPRO).
 * Invocado por el motor de autorizaciones (ModificarEstadoEventoAutorizacionMasivo) vía
 * AUT.TipoEvento.JavaAutorizacion = "cl.bcs.plataforma.com.AutorizarCrearPerfilProductoDetalleMasivo".
 */
public class AutorizarCrearPerfilProductoDetalleMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearPerfilProductoDetalleMasivo.class);

	private final SQL sql = new SQL();

	private final String SQL_UPDATE_DETALLE_PERFIL_PRODUCTOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilProductoDetalleMasivo_Update_DetallePerfilProductos.sql");

	private final String SQL_RECHAZAR_DETALLE_PERFIL_PRODUCTOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/AutorizarCrearPerfilProductoDetalleMasivo_Update_Rechazar.sql");

	public static class UpdateDetallePerfilProducto {
		public Long IdDetallePerfilProductos;
		public String IdEstadoAutorizacion;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UpdateDetallePerfilProducto that = (UpdateDetallePerfilProducto) o;
			return IdDetallePerfilProductos != null && IdDetallePerfilProductos.equals(that.IdDetallePerfilProductos);
		}

		@Override
		public int hashCode() {
			return IdDetallePerfilProductos != null ? IdDetallePerfilProductos.hashCode() : 0;
		}
	}

	public static class RechazarDetallePerfilProducto {
		public Long IdDetallePerfilProductos;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RechazarDetallePerfilProducto that = (RechazarDetallePerfilProducto) o;
			return IdDetallePerfilProductos != null && IdDetallePerfilProductos.equals(that.IdDetallePerfilProductos);
		}

		@Override
		public int hashCode() {
			return IdDetallePerfilProductos != null ? IdDetallePerfilProductos.hashCode() : 0;
		}
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPerfilProductoDetalleMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<UpdateDetallePerfilProducto> updates = entrada.p_Lista.stream()
				.map(evento -> {
					UpdateDetallePerfilProducto u = new UpdateDetallePerfilProducto();
					u.IdDetallePerfilProductos = Long.valueOf(evento.NumeroOperacionConcepto);
					u.IdEstadoAutorizacion = "AUT";
					return u;
				})
				.distinct()
				.collect(Collectors.toList());

			sql.batch(conn, SQL_UPDATE_DETALLE_PERFIL_PRODUCTOS, updates, UpdateDetallePerfilProducto.class);

			for (UpdateDetallePerfilProducto item : updates) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = item;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizarCrearPerfilProductoDetalleMasivo.autorizarEvento");
		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPerfilProductoDetalleMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<RechazarDetallePerfilProducto> updates = entrada.p_Lista.stream()
				.map(evento -> {
					RechazarDetallePerfilProducto u = new RechazarDetallePerfilProducto();
					u.IdDetallePerfilProductos = Long.valueOf(evento.NumeroOperacionConcepto);
					return u;
				})
				.distinct()
				.collect(Collectors.toList());

			sql.batch(conn, SQL_RECHAZAR_DETALLE_PERFIL_PRODUCTOS, updates, RechazarDetallePerfilProducto.class);

			for (RechazarDetallePerfilProducto item : updates) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = item;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizarCrearPerfilProductoDetalleMasivo.rechazarEvento");
		return salida;
	}
}
