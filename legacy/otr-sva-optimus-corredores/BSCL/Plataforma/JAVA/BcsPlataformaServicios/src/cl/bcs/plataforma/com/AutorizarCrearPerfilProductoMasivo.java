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
 * Autoriza/rechaza eventos de autorización pendientes de COM.PerfilProductos (concepto NCPPRO).
 * Invocado por el motor de autorizaciones (ModificarEstadoEventoAutorizacionMasivo) vía
 * AUT.TipoEvento.JavaAutorizacion = "cl.bcs.plataforma.com.AutorizarCrearPerfilProductoMasivo".
 */
public class AutorizarCrearPerfilProductoMasivo implements BcsAutorizaciones {

	private static final Logger logger = LogManager.getLogger(AutorizarCrearPerfilProductoMasivo.class);

	private final SQL sql = new SQL();

	private final String SQL_UPDATE_PERFIL_PRODUCTOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilProductoMasivo_Update_PerfilProductos.sql");

	private final String SQL_RECHAZAR_PERFIL_PRODUCTOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/AutorizarCrearPerfilProductoMasivo_Update_Rechazar.sql");

	public static class UpdatePerfilProducto {
		public String IdPerfilProductos;
		public String IdEstadoAutorizacion;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UpdatePerfilProducto that = (UpdatePerfilProducto) o;
			return IdPerfilProductos != null && IdPerfilProductos.equals(that.IdPerfilProductos);
		}

		@Override
		public int hashCode() {
			return IdPerfilProductos != null ? IdPerfilProductos.hashCode() : 0;
		}
	}

	public static class RechazarPerfilProducto {
		public String IdPerfilProductos;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RechazarPerfilProducto that = (RechazarPerfilProducto) o;
			return IdPerfilProductos != null && IdPerfilProductos.equals(that.IdPerfilProductos);
		}

		@Override
		public int hashCode() {
			return IdPerfilProductos != null ? IdPerfilProductos.hashCode() : 0;
		}
	}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPerfilProductoMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<UpdatePerfilProducto> updates = entrada.p_Lista.stream()
				.map(evento -> {
					UpdatePerfilProducto u = new UpdatePerfilProducto();
					u.IdPerfilProductos = evento.NumeroOperacionConcepto;
					u.IdEstadoAutorizacion = "AUT";
					return u;
				})
				.distinct()
				.collect(Collectors.toList());

			sql.batch(conn, SQL_UPDATE_PERFIL_PRODUCTOS, updates, UpdatePerfilProducto.class);

			for (UpdatePerfilProducto item : updates) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = item;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
		} catch (Exception e) {
			logger.error("Error en autorizarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizarCrearPerfilProductoMasivo.autorizarEvento");
		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::AutorizarCrearPerfilProductoMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();

		try {
			List<RechazarPerfilProducto> updates = entrada.p_Lista.stream()
				.map(evento -> {
					RechazarPerfilProducto u = new RechazarPerfilProducto();
					u.IdPerfilProductos = evento.NumeroOperacionConcepto;
					return u;
				})
				.distinct()
				.collect(Collectors.toList());

			sql.batch(conn, SQL_RECHAZAR_PERFIL_PRODUCTOS, updates, RechazarPerfilProducto.class);

			for (RechazarPerfilProducto item : updates) {
				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
				resultado.evento = item;
				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
		} catch (Exception e) {
			logger.error("Error en rechazarEvento: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}

		logger.info("FIN::AutorizarCrearPerfilProductoMasivo.rechazarEvento");
		return salida;
	}
}
