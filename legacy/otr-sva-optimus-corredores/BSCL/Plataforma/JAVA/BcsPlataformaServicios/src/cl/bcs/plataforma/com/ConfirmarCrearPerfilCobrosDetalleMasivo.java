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

* Autoriza eventos de autorización pendientes de COM.DetallePerfilCobros (concepto NDPCOB).
* Migrado de: COM.ConfirmarCrearPerfilCobrosDetalleMasivo
* Invocado por el motor de autorizaciones (ModificarEstadoEventoAutorizacionMasivo) vía
* AUT.TipoEvento.JavaAutorizacion = "cl.bcs.plataforma.com.ConfirmarCrearPerfilCobrosDetalleMasivo".
* El evento NDPCOB no tiene SPRechaza, por lo que no se implementa lógica de rechazo.
*/

public class ConfirmarCrearPerfilCobrosDetalleMasivo implements BcsAutorizaciones {
 
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPerfilCobrosDetalleMasivo.class);
 
	private final SQL sql = new SQL();
 
	private final String SQL_UPDATE_DETALLE_PERFIL_COBROS = PlataformaUtils.loadResourceToString(

			"cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobrosDetalleMasivo_Update_DetallePerfilCobros.sql");
 
	public static class UpdateDetallePerfilCobro {

		public Long IdDetallePerfilCobros;

		public String IdEstadoAutorizacion;
 
		@Override

		public boolean equals(Object o) {

			if (this == o) return true;

			if (o == null || getClass() != o.getClass()) return false;

			UpdateDetallePerfilCobro that = (UpdateDetallePerfilCobro) o;

			return IdDetallePerfilCobros != null && IdDetallePerfilCobros.equals(that.IdDetallePerfilCobros);

		} 
		@Override

		public int hashCode() {

			return IdDetallePerfilCobros != null ? IdDetallePerfilCobros.hashCode() : 0;

		}
	}
 
	@Override

	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {

		logger.info("INICIO::ConfirmarCrearPerfilCobrosDetalleMasivo.autorizarEvento");

		SalidaAutorizacion salida = new SalidaAutorizacion();
 
		try {

			List<UpdateDetallePerfilCobro> updates = entrada.p_Lista.stream()

					.map(evento -> {

						UpdateDetallePerfilCobro u = new UpdateDetallePerfilCobro();

						u.IdDetallePerfilCobros = Long.valueOf(evento.NumeroOperacionConcepto);

						u.IdEstadoAutorizacion = "AUT";

						return u;

					})

					.distinct()

					.collect(Collectors.toList());
 
			sql.batch(conn, SQL_UPDATE_DETALLE_PERFIL_COBROS, updates, UpdateDetallePerfilCobro.class);
 
			for (UpdateDetallePerfilCobro item : updates) {

				BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();

				resultado.evento = item;

				resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;

				salida.P_OUT_RESULTADO.add(resultado);

			}

		} catch (Exception e) {
		    logger.error("Error en autorizarEvento: " + e.getMessage(), e);
		    salida.P_OUT_ERRORESCONTADOR = 1L;
		}
 
		logger.info("FIN::ConfirmarCrearPerfilCobrosDetalleMasivo.autorizarEvento");

		return salida;

	}
 
	@Override

	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {

		logger.info("INICIO::ConfirmarCrearPerfilCobrosDetalleMasivo.rechazarEvento");

		// El evento NDPCOB no tiene SPRechaza definido en AUT.TipoEvento.

		// No existe logica de rechazo para este concepto.

		SalidaAutorizacion salida = new SalidaAutorizacion();

		logger.info("FIN::ConfirmarCrearPerfilCobrosDetalleMasivo.rechazarEvento");

		return salida;

	} 
}
 