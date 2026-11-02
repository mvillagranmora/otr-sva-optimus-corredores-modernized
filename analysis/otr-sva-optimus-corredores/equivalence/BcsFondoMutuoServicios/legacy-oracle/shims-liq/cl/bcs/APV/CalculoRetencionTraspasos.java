package cl.bcs.APV;

import java.math.BigDecimal;

/**
 * ORACLE SHIM (F3 W3 T6-B): the legacy CalculoRetencionTraspasos has a closure of its own (APV module) that LiquidacionSolicitudAporteRescate does not need: it only reads the ROWS of
 * ConsultarRetencion.sql into the nested class Retencion. This shim holds that ONE nested class; record_liq.sh checks that its field block is, token for token, the one of the legacy source.
 */
public class CalculoRetencionTraspasos {
	public static class Retencion{
		public String CodigoRetiro;
		public Long IdPersonaRetencion;
		public Long IdRegimenTributario;
		public BigDecimal PorcentajeRetencion;
		public BigDecimal MontoTopeRetencion;
	}
}
