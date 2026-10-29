package cl.bcs.plataforma.ins.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

import cl.bcs.utils.Utils;

public class Instrumento {
	public Long IdInstrumento;
	public String Nemotecnico;
	public String ISIN;
	public BigDecimal CorteMinimo;
	public String EstaInscrito;
	public String RazonSocial;
	public Date FechaEmision;
	public Date FechaVencimiento;
	public String IdEmisor;
	public String IdTipoReajuste;
	public String IdMoneda;
	public String IdTipoInstrumento;
	public String IdTipoInstrumentoOrigen;
	public String IdEstado;
	public BigDecimal Precio;
	public BigDecimal PrecioReferencia;
	public Date FechaReferencia;
	public BigDecimal FrecuenciaCupon;
	public Integer NumeroCupones;
	public String InstrumentoAcreditado;
	public String ValorizaPor;
	public Long IdInstrumentoRelacionado;
	public String TipoBonoIRF;
	public String OrigenInstrumento;
	public String TipoRenta;
	public String RelacionadoGrupo;
	public String FormadorMercado;
	public String PactoAndino;
	public BigDecimal FactorExposicion;
	public String LargoPlazo;
	public BigDecimal Tasa;
	public String EsDivisible;
	public String Reajustable;
	public Long DecimalesPrecio;
	public Date FechaInicioVigencia;
	public String IndArt104;
	public BigDecimal TasaFiscal;
	public String TipoValorizacion;
	public String TieneCupon;
	public Long NumeroCuotasCirculacionMesActual;
	public Long NumeroAccionesCirculacion;
	public String IndicadorLiquidaDCV;
	public Long IdPaisEmisor;
	public String Serie;
	public String IndicadorBolson;
	public Long CodigoRubro;
	public Long PresenciaBursatilAjustada;
	public BigDecimal PorcentajeCastigo;
	public Date FechaHastaAcreditado;
	public String IdMonedaEmision;
	public Long IdCarga;
	public Long NumeroAmortizaciones;
	public BigDecimal Tera;
	public Long IdOrdenSerie;
	public String AfectoArt107;
	public String AfectoArt104;
	public String CobroComisionesCustodia;
	public String EsInstrumentoCalificado;
	public String EsAutorizado;
	public Long IdInstrumentoRaiz;
	public String IdCondicionLiquidacion;
	//DERIVADOS (FU,OP)
	public Long Tamano;
	public String EsPutEsCall;
	public BigDecimal PrecioStrike;
	public String EstiloOpcion;
	public String ModalidadLiquidacion;
	public String ModalidadEjercicio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

	@Override
	public int hashCode() {
		return Objects.hash(AfectoArt104, AfectoArt107, CobroComisionesCustodia, CodigoRubro, CorteMinimo,
				DecimalesPrecio, EsAutorizado, EsDivisible, EsInstrumentoCalificado, EsPutEsCall, EstaInscrito,
				EstiloOpcion, FactorExposicion, FechaEmision, FechaHastaAcreditado, FechaInicioVigencia,
				FechaReferencia, FechaVencimiento, FormadorMercado, FrecuenciaCupon, ISIN, IdCarga,
				IdCondicionLiquidacion, IdEmisor, IdEstado, IdInstrumento, IdInstrumentoRaiz, IdInstrumentoRelacionado,
				IdMoneda, IdMonedaEmision, IdOrdenSerie, IdPaisEmisor, IdTipoInstrumento, IdTipoInstrumentoOrigen, IdTipoReajuste, IndArt104,
				IndicadorBolson, IndicadorLiquidaDCV, InstrumentoAcreditado, LargoPlazo, ModalidadEjercicio,
				ModalidadLiquidacion, Nemotecnico, NumeroAccionesCirculacion, NumeroAmortizaciones,
				NumeroCuotasCirculacionMesActual, NumeroCupones, OrigenInstrumento, PactoAndino, PorcentajeCastigo,
				Precio, PrecioReferencia, PrecioStrike, PresenciaBursatilAjustada, RazonSocial, Reajustable,
				RelacionadoGrupo, Serie, Tamano, Tasa, TasaFiscal, Tera, TieneCupon, TipoBonoIRF, TipoRenta,
				TipoValorizacion, ValorizaPor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instrumento other = (Instrumento) obj;
		return Objects.equals(AfectoArt104, other.AfectoArt104) && Objects.equals(AfectoArt107, other.AfectoArt107)
				&& Objects.equals(CobroComisionesCustodia, other.CobroComisionesCustodia)
				&& Objects.equals(CodigoRubro, other.CodigoRubro) && Objects.equals(CorteMinimo, other.CorteMinimo)
				&& Objects.equals(DecimalesPrecio, other.DecimalesPrecio)
				&& Objects.equals(EsAutorizado, other.EsAutorizado) && Objects.equals(EsDivisible, other.EsDivisible)
				&& Objects.equals(EsInstrumentoCalificado, other.EsInstrumentoCalificado)
				&& Objects.equals(EsPutEsCall, other.EsPutEsCall) && Objects.equals(EstaInscrito, other.EstaInscrito)
				&& Objects.equals(EstiloOpcion, other.EstiloOpcion)
				&& Objects.equals(FactorExposicion, other.FactorExposicion)
				&& Objects.equals(FechaEmision, other.FechaEmision)
				&& Objects.equals(FechaHastaAcreditado, other.FechaHastaAcreditado)
				&& Objects.equals(FechaInicioVigencia, other.FechaInicioVigencia)
				&& Objects.equals(FechaReferencia, other.FechaReferencia)
				&& Objects.equals(FechaVencimiento, other.FechaVencimiento)
				&& Objects.equals(FormadorMercado, other.FormadorMercado)
				&& Objects.equals(FrecuenciaCupon, other.FrecuenciaCupon) && Objects.equals(ISIN, other.ISIN)
				&& Objects.equals(IdCarga, other.IdCarga)
				&& Objects.equals(IdCondicionLiquidacion, other.IdCondicionLiquidacion)
				&& Objects.equals(IdEmisor, other.IdEmisor) && Objects.equals(IdEstado, other.IdEstado)
				&& Objects.equals(IdInstrumento, other.IdInstrumento)
				&& Objects.equals(IdInstrumentoRaiz, other.IdInstrumentoRaiz)
				&& Objects.equals(IdInstrumentoRelacionado, other.IdInstrumentoRelacionado)
				&& Objects.equals(IdMoneda, other.IdMoneda) && Objects.equals(IdMonedaEmision, other.IdMonedaEmision)
				&& Objects.equals(IdOrdenSerie, other.IdOrdenSerie) && Objects.equals(IdPaisEmisor, other.IdPaisEmisor)
				&& Objects.equals(IdTipoInstrumento, other.IdTipoInstrumento)
				&& Objects.equals(IdTipoInstrumentoOrigen, other.IdTipoInstrumentoOrigen)
				&& Objects.equals(IdTipoReajuste, other.IdTipoReajuste) && Objects.equals(IndArt104, other.IndArt104)
				&& Objects.equals(IndicadorBolson, other.IndicadorBolson)
				&& Objects.equals(IndicadorLiquidaDCV, other.IndicadorLiquidaDCV)
				&& Objects.equals(InstrumentoAcreditado, other.InstrumentoAcreditado)
				&& Objects.equals(LargoPlazo, other.LargoPlazo)
				&& Objects.equals(ModalidadEjercicio, other.ModalidadEjercicio)
				&& Objects.equals(ModalidadLiquidacion, other.ModalidadLiquidacion)
				&& Objects.equals(Nemotecnico, other.Nemotecnico)
				&& Objects.equals(NumeroAccionesCirculacion, other.NumeroAccionesCirculacion)
				&& Objects.equals(NumeroAmortizaciones, other.NumeroAmortizaciones)
				&& Objects.equals(NumeroCuotasCirculacionMesActual, other.NumeroCuotasCirculacionMesActual)
				&& Objects.equals(NumeroCupones, other.NumeroCupones)
				&& Objects.equals(OrigenInstrumento, other.OrigenInstrumento)
				&& Objects.equals(PactoAndino, other.PactoAndino)
				&& Objects.equals(PorcentajeCastigo, other.PorcentajeCastigo) && Objects.equals(Precio, other.Precio)
				&& Objects.equals(PrecioReferencia, other.PrecioReferencia)
				&& Objects.equals(PrecioStrike, other.PrecioStrike)
				&& Objects.equals(PresenciaBursatilAjustada, other.PresenciaBursatilAjustada)
				&& Objects.equals(RazonSocial, other.RazonSocial) && Objects.equals(Reajustable, other.Reajustable)
				&& Objects.equals(RelacionadoGrupo, other.RelacionadoGrupo) && Objects.equals(Serie, other.Serie)
				&& Objects.equals(Tamano, other.Tamano) && Objects.equals(Tasa, other.Tasa)
				&& Objects.equals(TasaFiscal, other.TasaFiscal) && Objects.equals(Tera, other.Tera)
				&& Objects.equals(TieneCupon, other.TieneCupon) && Objects.equals(TipoBonoIRF, other.TipoBonoIRF)
				&& Objects.equals(TipoRenta, other.TipoRenta)
				&& Objects.equals(TipoValorizacion, other.TipoValorizacion)
				&& Objects.equals(ValorizaPor, other.ValorizaPor);
	}
	
}
