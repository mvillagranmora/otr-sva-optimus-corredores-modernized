package cl.bcs.fondomutuo.cierre;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * Legacy {@code pojo.SerieFondoSolicitudes}: row of {@code ComunesSolicitudes_SelectSerieFondo.sql}, filled BY COLUMN LABEL onto these public fields
 * (exact type, no conversion, as the legacy). Field names and order are the legacy ones.
 */
public class SerieFondoSolicitudes {
    public String 		IdSerieFondo;
    public Long 		IdInstrumento;
    public String 		IdFondo;
    public String 		NombreSerieFondo;
    public String		CodigoSerie;
    public String 		Alias;
    public Long			DecimalesValorCuota;
    public Long			DecimalesCuota;
    public BigDecimal	MontoMinimo;
    public BigDecimal	MontoMaximo;
    public Long			DiasRetencionesAporte;
    public Long			DiasRetencionesRescate;
    public Long			DiasValutaCurseAporte;
    public Long			DiasValutaCurseRescate;
    public Long			DiasValutaValorCuotaAporte;
    public Long			DiasValutaValorCuotaRescate;
    public Long			DiasValutaCertificacionAporte;
    public Long			DiasValutaCertificacionRescate;
    public String 		ExcluyeGarantia;
    public String 		CobraComision;
    public String 		Diferido;
    public String		InstrumentoDCV;
    public String 		Omnibus;
    public BigDecimal	PatrimonioMinimo;
    public String		IdSerieFondoOrigen;
    public Timestamp	FechaIngresoRegistro;
    public String		IdEstado;
    public Long			IdCliente;
    public Long			IdPortafolio;
    public String		IdMedioDePagoAbono;
    public String		IdMedioDePagoCargo;
    public String		IdFamiliaFondo;
    public Long			IdEmpresa;
    public String		CambioFondo;
    public Long			DiasComision;
 	public BigDecimal	PorcentajeComision;
 	public BigDecimal 	PorcentajeRemuneracion;
 	public BigDecimal	PorcentajeGastoOpera;
 	public BigDecimal	PorcentajeCostoAdm;
 	public String		IdEmisor;
    public Long			IdPaisEmisor;
    public String		Nemotecnico;
    public String		NombreFondo;
    public String		IdTipoFondo;
    public Long			IdPaisOrigen;
    public String		CodigoSVS;
    public String		MoneyMarket;
    public Date			FechaCreacion;
    public Timestamp 	FechaIngresoRegistroFondo;
    public Date			FechaConstitucion;
    public Date			FechaCierre;
    public String		Vigencia;
    public String		FondoExtranjero;
    public String		IdMoneda;
    public String		IdTipoInstrumento;
    public String		IdCategoriaPerfilFondoMutuo;
    public String		UsaHorarioAporteDefault;
    public String		UsaHorarioRescateDefault;
    public String		UsaHorarioReinversionDefault;
    public String		UsaHorarioLiquidezDefault;
    public String		EstadoAporteComercial;
    public String		EstadoRescateComercial;
    public String		EstadoReinversionComercial;
    public String		EstadoLiquidezComercial;
    public String 		EstadoAporteBackoffice;
    public String		EstadoRescateBackoffice;
    public String		EstadoReinversionBackoffice;
    public String		EstadoLiquidezBackoffice;
    public String		BloqueoAporte;
    public String		BloqueoRescate;
    public String		BloqueoIndefinido;
    public Date			FechaBloqueoDesde;
    public Date			FechaBloqueoHasta;
    public Date			FechaFondo;
    public String		AperturaAutomaticaFondo;
    public String		FondoNormativo;
    public String		IdTipoDocumentoIdentidadFondo;
    public String		DocumentoIdentidadFondo;
    public String		NombreCortoTipoFondo;
    public String		DescripcionTipoFondo;
    public String		IndicadorMoneyMarket;
    public String		AportesAltos;
    public String		PatrimonioAltos;
    public String		IdMercadoFondoMutuo;
    public String		EsAPV;
    public String		RestringeAporte;
    public String		RestringeRescate;
    public String 		FondoNacionalDolar;
    public String 		EmisorFondoInternacionalDolar;
    public String 		EmisorFondoNacionalPesos;
    public String 		EmisorFondoNacionalDolar;
    public String toString() {
        return JsonLegacy.conNulos().toJson(this);
    }

}