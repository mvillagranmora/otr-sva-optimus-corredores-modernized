package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;

/**
 * Row classes of the legacy {@code IngresarOrdenFMBonificacionFiscal} (IngresarOrdenFMBonificacionFiscal.java:56-100): the SQL result is mapped by column
 * label onto their public fields (SqlNombrado), so they are copied VERBATIM; only {@code toString} uses {@link JsonLegacy}. Also read by the RV job
 * ({@code ParametrosPrograma}).
 */
public final class FilasBonificacion {

    private FilasBonificacion() {
    }

	public static class ParametrosPrograma{
		public Long IdSecuencia;
		public String NombreParametro;
		public String Valor;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}
	
	public static class ApoResFFMM{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String TipoSolicitud;
		public String IdSerieFondo;
		public String CalculoPorMontoCuota;
		public String EsPorMontoLiquido;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String IdMoneda;
		public String IdMedioDePago;
		public Long IdPreferenciaPagoPortafolio;
		public String Visado;
		public String SolicitudBatch;
		public String OrigenOperacion;
		public String FechaOperacion;
		public String IdRegimenTributario;
		public String Glosa;
		public String IndicadorMLT;
		public String IdSerieFondoDestino;
		public String IdEmisorDestino;
		public Long IdAutorizadoOrigen;
		public BigDecimal Porcentaje;
		public Long FolioContrato;
		public String IdMonedaPago;
		public String IdTipoMovimientoFondoMutuo;
		public String TipoContratoOperacion;
		public String FolioContratoOperacion;
		public Long IdSolicitudAporteRescate;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
		
	}

    /**
     * Projection of the legacy {@code cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes} (87 fields): the job reads only {@code IdInstrumento}; the other columns of
     * ComunesSolicitudes_SelectSerieFondo.sql have no field here and are ignored by the label mapping, as in the legacy for an unknown label.
     */
    public static class SerieFondoSolicitudes {
        public String IdSerieFondo;
        public Long IdInstrumento;
    }
}
