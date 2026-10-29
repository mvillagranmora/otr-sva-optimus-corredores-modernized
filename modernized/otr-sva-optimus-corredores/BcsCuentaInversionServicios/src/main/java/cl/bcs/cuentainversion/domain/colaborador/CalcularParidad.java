package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.mer.CalcularParidad}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class CalcularParidad {

    private CalcularParidad() {
    }

    public static final String CLASE = "cl.bcs.plataforma.mer.CalcularParidad";

	// legacy cl/bcs/plataforma/mer/CalcularParidad.java:57-69 (verbatim)
	public static class CalcularParidadEntrada {
		public Long p_IdEmpresa;
		public Long p_IdTrabajo;
		public List<CalculoEntrada> p_CalculosEntrada;
		
public CalcularParidadEntrada() {
			this.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
		}
	}

	public static class CalculoEntrada {
		public Long IdTrabajo;
		public Long IdInterno;
		public String Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
public CalculoEntrada(){
			this.ValorCambioLocalModificado = BigDecimal.ZERO;
			this.IdUso = "";
		}
	}

	public static class CalculoSalida {
		public Long IdTrabajo;
		public Long IdInterno;
		public java.sql.Date Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
		public BigDecimal FactorOrigenDestino;
		public BigDecimal MontoDestino;
		public BigDecimal FactorOrigenLocal;
		public BigDecimal MontoLocal;
		
		public BigDecimal FactorOrigenCambio;
		public java.sql.Date FechaOrigenCambio;
		public String IdTipoRegistroOrigenCambio;
		
		public BigDecimal FactorCambioLocal;
		public java.sql.Date FechaCambioLocal;
		public String IdTipoRegistroCambioLocal;
		
		public BigDecimal FactorLocalCambio;
		public java.sql.Date FechaLocalCambio;
		public String IdTipoRegistroLocalCambio;
		
		public BigDecimal FactorCambioDestino;
		public java.sql.Date FechaCambioDestino;
		public String IdTipoRegistroCambioDestino;
		
		public String Resultado;
		public String CodigoMensaje;
		public BigDecimal ValorParametroFinanciero;
		public Date FechaParametroFinanciero;

public CalculoSalida(){
			this.FactorOrigenDestino = BigDecimal.ZERO;
			this.FactorOrigenLocal = BigDecimal.ZERO;
			this.FactorOrigenCambio = BigDecimal.ZERO;
			this.IdTipoRegistroOrigenCambio = "";
			this.FactorCambioLocal = BigDecimal.ZERO;
			this.IdTipoRegistroCambioLocal = "";
			this.FactorLocalCambio = BigDecimal.ZERO;
			this.IdTipoRegistroLocalCambio = "";
			this.FactorCambioDestino = BigDecimal.ZERO;
			this.IdTipoRegistroCambioDestino = "";
			this.ValorParametroFinanciero =BigDecimal.ZERO;
		}
	
	}

	public static class CalcularParidadSalida {
		public List<CalculoSalida> p_out_CalculosSalida;
		public Long	 P_OUT_ERRORESCONTADOR = 0L;
		
		public CalcularParidadSalida(){
			this.p_out_CalculosSalida = new ArrayList<CalculoSalida>();
		}
		
}
}
