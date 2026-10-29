package cl.bcs.socionegocio.soc.pojo;

import cl.bcs.utils.BcsPojo;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Portafolio extends BcsPojo {

	public Long IdInterno;
	public Long IdPortafolio;

	public DatosGenerales DatosGenerales;
	public Map<String, Indicador> IndicadoresPorCodigoTipoIndicador;

	public Long CantidadCuentaCustodio;
	public Long CantidadPreferenciasPagoPaga;
	public Long CantidadPreferenciasPagoRecibe;

	public static class DatosGenerales extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdNombreFantasia;
		public Long IdPersonaBrokerExtranjero;
		public Long IdDireccionEnvioCorrespondencia;
		public Long IdTelefonoContacto;
		public Long IdEmpleadoEjecutivo;
		public Long IdMatrizAgrupadorAPV;
		public String CodigoPortafolio;
		public String Alias;
		public String NumeroFicha;
		public String Proposito;
		public String DescripcionPortafolio;
		public String AreaCliente;
		public String PropositoPortafolio;
		public String ComentarioBloqueo;
		public String IdTipoBloqueo;
		public java.sql.Timestamp FechaBloqueo;
		public Long IdEmpleadoEjecutivoComercial;
	}

	public Portafolio() {
		IndicadoresPorCodigoTipoIndicador = new HashMap<>();
		CantidadCuentaCustodio = 0L;
		CantidadPreferenciasPagoPaga = 0L;
		CantidadPreferenciasPagoRecibe = 0L;
	}

	public Long getIdEntidadOrDefault(Long defaultId) {
		if (Objects.nonNull(IdPortafolio) && IdPortafolio > 0) {
			return IdPortafolio;
		}

		if (Objects.nonNull(IdInterno) && IdInterno > 0) {
			return IdInterno;
		}

		return defaultId;
	}

}
