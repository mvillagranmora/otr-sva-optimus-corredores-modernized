package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1922 {

    /**
     * Declaracion de variables para la DJ
     */
    
    private String RutAdministradora;
    private String RutFondo;
    private String RunFondo;
    private String Fondo;
    private String TipoFondo;
    private String RutCliente;
    private String NombreParticipe;
    private String TaxId;
    private String CodigoPais;
    private String TipoOperacion;
    private Date   fechaInversion;
    private String MonedaReajuste;
	private double MontoNominalAportesActualizado;
	private long NumeroSerieCuotas;
	private double CantidadCuotasParticipe;
	private long ParticipeCumpleRequisitos;
	private long CuotasCumplenRequisitos;
	private double ValorCuotaPesos;
	private double NumCuotasSerieParticipe;
	private double PorcentajeParticipacion31Dic;
	private double ValNominalCuotasRescatadas;
	private double CantidadCuotasRescatadas;
	private double DiferenciaPorRescateOEnajenacionActualizado;
	private double DifPorRescateOEnajenacionCFIAcogido107Actualizado;
	private double ValNominalInversionaFechaLiquidacion;
	private double CantCuotasCedidasPorLiquidacion;
	private double DiferenciaValorCuotasFechaLiquidacionActualizado;
	private String TributaSa;
	private double DivRemDistAfectGCConIDPC;
	private double DivRemDistAfectGCConIDPCal31122016;
	private double DivRemDistAfectGCConIDPCVOL;
	private double DivRemDistAfectGCSINDC;
	private double DisRemRentasTribCProvRap_C96;
	private double DisRemRentasTribCORP_C97;
	private double DisRemRentasTribCExDD_C98;
	private double DisRemRentasTribCUtAFFut_C99;
	private double DivRemDistNoRentaFUT;
	private double DisRemRentasExIGCAfecImptoAdi_C100;
	private double CredImpGloCompNSRSD;
	private double CredImpGloCompNSRCD;
	private double CredIGCAcumD01012017RANSRD01012020SDD_C101;
	private double CredIGCAcumD01012017RANSRD01012020CDD_C102;
	private double CredImpGloCompSRSD;
	private double CredImpGloCompSRCD;
	private double CredIGCAcumD01012017RXSRSDD_C103;
	private double CredIGCAcumD01012017RXSRCDD_C104;
	private double CredImpGloCompCTDCIF;
	private double CredImpGloCompCDD31122016;
	private double CredImpGloCompSDD31122016;
	private double CredIGCAcumH31122016RXSDD_C105;
	private double CredIGCAcumH31122016RXCDD_C106;
	private double CredImpGloCompCTDCIF31122016;
	private double CredImpGloCompCITAExArt21;
	private String DetBenNetoPerRutFdo_C107;
	private double DetBenNetoPerPOS_C113;
	private double DetBenNetoPerNEG_C114;
	private double DividendosExentosIGCoAdicionalActualizado;
	private double DividendosNoConstitutivosDeRentaActualizado;
	private double DivRemDistNoRentaFE;
	private double DividendosAfectosImptoUnicoTasa10Actualizado;
	private double DividendosAfectosImptoUnicoTasa4Actualizado;
	private double DistribucionXDiminucionNoImputadaCapital;
	private double DevolucionesDeCapitalActualizado;
	private double MontoInversionenCuotasCFIAnterior93;
	private long NumeroCertificado;
	private double DetBenNetoUtilPer_C133;
	private double DetBenNetoIntPer_C134;
	private double DetBenNetoDivPer_C135;
	private double DetBenNetoGanCapPer_C136;
	private double DetBenNetoPerGastDev_C137;
	private String NumSerieCuotasParticipe_C138;
	private String RunNemotecnico;
	private String TipoContribuyente;

	//Sumatorias
	 private double sumaDiferenciaPorRescateOEnajenacionActualizado;
	 private double sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado;
	 private double sumaDiferenciaValorCuotasFechaLiquidacionActualizado;
	 private double sumaDivRemDistAfectGCConIDPC;
	 private double sumaDivRemDistAfectGCConIDPCal31122016;
	 private double sumaDivRemDistAfectGCConIDPCVOL;
	 private double sumaDivRemDistAfectGCSINDC;
	 private double sumaDisRemRentasTribCProvRap_C96;
	 private double sumaDisRemRentasTribCORP_C97;
	 private double sumaDisRemRentasTribCExDD_C98;
	 private double sumaDisRemRentasTribCUtAFFut_C99;
	 private double sumaDivRemDistNoRentaFUT;
	 private double sumaDisRemRentasExIGCAfecImptoAdi_C100;
	 private double sumaDividendosExentosIGCoAdicionalActualizado;
	 private double sumaDividendosNoConstitutivosDeRentaActualizado;
	 private double sumaDivRemDistNoRentaFE;
	 private double sumaDividendosAfectosImptoUnicoTasa10Actualizado;
	 private double sumaDividendosAfectosImptoUnicoTasa4Actualizado;
	 private double sumaDistribucionXDiminucionNoImputadaCapital;
	 private double sumaDevolucionesDeCapitalActualizado;
	 private double sumaCredImpGloCompNSRSD;
	 private double sumaCredImpGloCompNSRCD;
	 private double sumaCredIGCAcumD01012017RANSRD01012020SDD_C101;
	 private double sumaCredIGCAcumD01012017RANSRD01012020CDD_C102;
	 private double sumaCredImpGloCompSRSD;
	 private double sumaCredImpGloCompSRCD;
	 private double sumaCredIGCAcumD01012017RXSRSDD_C103;
	 private double sumaCredIGCAcumD01012017RXSRCDD_C104;
	 private double sumaCredImpGloCompCTDCIF;
	 private double sumaCredImpGloCompCDD31122016;
	 private double sumaCredImpGloCompSDD31122016;
	 private double sumaCredIGCAcumH31122016RXSDD_C105;
	 private double sumaCredIGCAcumH31122016RXCDD_C106;
	 private double sumaCredImpGloCompCTDCIF31122016;
	 private double sumaCredImpGloCompCITAExArt21;
	 private double sumaMontoInversionenCuotasCFIAnterior93;
	 private double sumaDetBenNetoPerPOS_C113;
	 private double sumaDetBenNetoPerNEG_C114;
	 
	//Obtener valor del registro
    public RegistroDJ1922(final Registro registro) {
    	
    this.RutAdministradora = (String) registro.get("RutAdministradora");
    this.RutFondo = (String) registro.get("RutFondo");
    this.RunFondo = (String) registro.get("RunFondo");
    this.Fondo = (String) registro.get("Fondo");
    this.TipoFondo = (String) registro.get("TipoFondo");
    this.RutCliente = (String) registro.get("RutCliente");
    this.NombreParticipe = (String) registro.get("NombreParticipe");
    this.TaxId = (String) registro.get("TaxId");
    this.CodigoPais = (String) registro.get("CodigoPais");
    this.TipoOperacion = (String) registro.get("TipoOperacion");
    this.fechaInversion = (Date) registro.get("FechaInversion");
    this.MonedaReajuste = (String) registro.get("MonedaReajuste");
    this.MontoNominalAportesActualizado = (double) ((Number) registro.get("MontoNominalAportesActualizado")).doubleValue();
    this.NumeroSerieCuotas = (Long) registro.get("NumeroSerieCuotas");
    this.CantidadCuotasParticipe = (double) ((Number) registro.get("CantidadCuotasParticipe")).doubleValue();
    this.ParticipeCumpleRequisitos = (Long) registro.get("ParticipeCumpleRequisitos");
    this.CuotasCumplenRequisitos = (Long) registro.get("CuotasCumplenRequisitos");
    this.ValorCuotaPesos = (double) ((Number) registro.get("ValorCuotaPesos")).doubleValue();
    this.NumCuotasSerieParticipe = (double) ((Number) registro.get("NumCuotasSerieParticipe")).doubleValue();
    this.PorcentajeParticipacion31Dic = (double) ((Number) registro.get("PorcentajeParticipacion31Dic")).doubleValue();
    this.ValNominalCuotasRescatadas = (double) ((Number) registro.get("ValNominalCuotasRescatadas")).doubleValue();
    this.CantidadCuotasRescatadas = (double) ((Number) registro.get("CantidadCuotasRescatadas")).doubleValue();
    this.DiferenciaPorRescateOEnajenacionActualizado = (double) ((Number) registro.get("DiferenciaPorRescateOEnajenacionActualizado")).doubleValue();
    this.DifPorRescateOEnajenacionCFIAcogido107Actualizado = (double) ((Number) registro.get("DifPorRescateOEnajenacionCFIAcogido107Actualizado")).doubleValue();
    this.ValNominalInversionaFechaLiquidacion = (double) ((Number) registro.get("ValNominalInversionaFechaLiquidacion")).doubleValue();
    this.CantCuotasCedidasPorLiquidacion = (double) ((Number) registro.get("CantCuotasCedidasPorLiquidacion")).doubleValue();
    this.DiferenciaValorCuotasFechaLiquidacionActualizado = (double) ((Number) registro.get("DiferenciaValorCuotasFechaLiquidacionActualizado")).doubleValue();
    this.TributaSa = (String) registro.get("TributaSa");
    this.DivRemDistAfectGCConIDPC = (double) ((Number) registro.get("DivRemDistAfectGCConIDPC")).doubleValue();
    this.DivRemDistAfectGCConIDPCal31122016 = (double) ((Number) registro.get("DivRemDistAfectGCConIDPCal31122016")).doubleValue();
    this.DivRemDistAfectGCConIDPCVOL = (double) ((Number) registro.get("DivRemDistAfectGCConIDPCVOL")).doubleValue();
    this.DivRemDistAfectGCSINDC = (double) ((Number) registro.get("DivRemDistAfectGCSINDC")).doubleValue();
    this.DisRemRentasTribCProvRap_C96 = (double) ((Number) registro.get("DisRemRentasTribCProvRap_C96")).doubleValue();
    this.DisRemRentasTribCORP_C97 = (double) ((Number) registro.get("DisRemRentasTribCORP_C97")).doubleValue();
    this.DisRemRentasTribCExDD_C98 = (double) ((Number) registro.get("DisRemRentasTribCExDD_C98")).doubleValue();
    this.DisRemRentasTribCUtAFFut_C99 = (double) ((Number) registro.get("DisRemRentasTribCUtAFFut_C99")).doubleValue();
    this.DivRemDistNoRentaFUT = (double) ((Number) registro.get("DivRemDistNoRentaFUT")).doubleValue();
    this.DisRemRentasExIGCAfecImptoAdi_C100 = (double) ((Number) registro.get("DisRemRentasExIGCAfecImptoAdi_C100")).doubleValue();
    this.CredImpGloCompNSRSD = (double) ((Number) registro.get("CredImpGloCompNSRSD")).doubleValue();
    this.CredImpGloCompNSRCD = (double) ((Number) registro.get("CredImpGloCompNSRCD")).doubleValue();
    this.CredIGCAcumD01012017RANSRD01012020SDD_C101 = (double) ((Number) registro.get("CredIGCAcumD01012017RANSRD01012020SDD_C101")).doubleValue();
    this.CredIGCAcumD01012017RANSRD01012020CDD_C102 = (double) ((Number) registro.get("CredIGCAcumD01012017RANSRD01012020CDD_C102")).doubleValue();
    this.CredImpGloCompSRSD = (double) ((Number) registro.get("CredImpGloCompSRSD")).doubleValue();
    this.CredImpGloCompSRCD = (double) ((Number) registro.get("CredImpGloCompSRCD")).doubleValue();
    this.CredIGCAcumD01012017RXSRSDD_C103 = (double) ((Number) registro.get("CredIGCAcumD01012017RXSRSDD_C103")).doubleValue();
    this.CredIGCAcumD01012017RXSRCDD_C104 = (double) ((Number) registro.get("CredIGCAcumD01012017RXSRCDD_C104")).doubleValue();
    this.CredImpGloCompCTDCIF = (double) ((Number) registro.get("CredImpGloCompCTDCIF")).doubleValue();
    this.CredImpGloCompCDD31122016 = (double) ((Number) registro.get("CredImpGloCompCDD31122016")).doubleValue();
    this.CredImpGloCompSDD31122016 = (double) ((Number) registro.get("CredImpGloCompSDD31122016")).doubleValue();
    this.CredIGCAcumH31122016RXSDD_C105 = (double) ((Number) registro.get("CredIGCAcumH31122016RXSDD_C105")).doubleValue();
    this.CredIGCAcumH31122016RXCDD_C106 = (double) ((Number) registro.get("CredIGCAcumH31122016RXCDD_C106")).doubleValue();
    this.CredImpGloCompCTDCIF31122016 = (double) ((Number) registro.get("CredImpGloCompCTDCIF31122016")).doubleValue();
    this.CredImpGloCompCITAExArt21 = (double) ((Number) registro.get("CredImpGloCompCITAExArt21")).doubleValue();
    this.DetBenNetoPerRutFdo_C107 = (String) registro.get("DetBenNetoPerRutFdo_C107");
    this.DetBenNetoPerPOS_C113 = (double) ((Number) registro.get("DetBenNetoPerPOS_C113")).doubleValue();
    this.DetBenNetoPerNEG_C114 = (double) ((Number) registro.get("DetBenNetoPerNEG_C114")).doubleValue();
    this.DividendosExentosIGCoAdicionalActualizado = (double) ((Number) registro.get("DividendosExentosIGCoAdicionalActualizado")).doubleValue();
    this.DividendosNoConstitutivosDeRentaActualizado = (double) ((Number) registro.get("DividendosNoConstitutivosDeRentaActualizado")).doubleValue();
    this.DivRemDistNoRentaFE = (double) ((Number) registro.get("DivRemDistNoRentaFE")).doubleValue();
    this.DividendosAfectosImptoUnicoTasa10Actualizado = (double) ((Number) registro.get("DividendosAfectosImptoUnicoTasa10Actualizado")).doubleValue();
    this.DividendosAfectosImptoUnicoTasa4Actualizado = (double) ((Number) registro.get("DividendosAfectosImptoUnicoTasa4Actualizado")).doubleValue();
    this.DistribucionXDiminucionNoImputadaCapital = (double) ((Number) registro.get("DistribucionXDiminucionNoImputadaCapital")).doubleValue();
    this.DevolucionesDeCapitalActualizado = (double) ((Number) registro.get("DevolucionesDeCapitalActualizado")).doubleValue();
    this.MontoInversionenCuotasCFIAnterior93 = (double) ((Number) registro.get("MontoInversionenCuotasCFIAnterior93")).doubleValue();
    this.NumeroCertificado = (Long) registro.get("NumeroCertificado");
	this.DetBenNetoUtilPer_C133 = (double) ((Number) registro.get("DetBenNetoUtilPer_C133")).doubleValue();
	this.DetBenNetoIntPer_C134 = (double) ((Number) registro.get("DetBenNetoIntPer_C134")).doubleValue();
	this.DetBenNetoDivPer_C135 = (double) ((Number) registro.get("DetBenNetoDivPer_C135")).doubleValue();
	this.DetBenNetoGanCapPer_C136 = (double) ((Number) registro.get("DetBenNetoGanCapPer_C136")).doubleValue();
	this.DetBenNetoPerGastDev_C137 = (double) ((Number) registro.get("DetBenNetoPerGastDev_C137")).doubleValue();
	this.NumSerieCuotasParticipe_C138 = (String) registro.get("NumSerieCuotasParticipe_C138");
	this.RunNemotecnico = (String) registro.get("RunNemotecnico");
	this.TipoContribuyente = (String) registro.get("TipoContribuyente");
    
    }

    

	public String getRutAdministradora() {return RutAdministradora;}
	public void setRutAdministradora(String RutAdministradora) {this.RutAdministradora = RutAdministradora;}
	
	public String getRutFondo() {return RutFondo;}
	public void setRutFondo(String RutFondo) {this.RutFondo = RutFondo;}	
	
	public String getRunFondo() {return RunFondo;}
	public void setRunFondo(String RunFondo) {this.RunFondo = RunFondo;}	
	
	public String getFondo() {return Fondo;}
	public void setFondo(String Fondo) {this.Fondo = Fondo;}	
	
	public String getTipoFondo() {return TipoFondo;}
	public void setTipoFondo(String TipoFondo) {this.TipoFondo = TipoFondo;}	
  
	public String getRutCliente() {return RutCliente;}
	public void setRutCliente(String RutCliente) {this.RutCliente = RutCliente;}	
    
	public String getNombreParticipe() {return NombreParticipe;}
	public void setNombreParticipe(String NombreParticipe) {this.NombreParticipe = NombreParticipe;}	
    
	public String getTaxId() {return TaxId;}
	public void setTaxId(String TaxId) {this.TaxId = TaxId;}	
    
	public String getCodigoPais() {return CodigoPais;}
	public void setCodigoPais(String CodigoPais) {this.CodigoPais = CodigoPais;}	
	
	public String getTipoOperacion() {return TipoOperacion;}
	public void setTipoOperacion(String TipoOperacion) {this.TipoOperacion = TipoOperacion;}	
		
	public Date getFechaInversion() {return fechaInversion;}
	public void setFechaInversion(Date FechaInversion) {fechaInversion = FechaInversion;}

	public String getMonedaReajuste() {return MonedaReajuste;}
	public void setMonedaReajuste(String MonedaReajuste) {this.MonedaReajuste = MonedaReajuste;}	
		
	public double getMontoNominalAportesActualizado() {return MontoNominalAportesActualizado;}
	public void setMontoNominalAportesActualizado(double MontoNominalAportesActualizado) {this.MontoNominalAportesActualizado = MontoNominalAportesActualizado;}
    
	public long getNumeroSerieCuotas() {return NumeroSerieCuotas;}
	public void setNumeroSerieCuotas(long NumeroSerieCuotas) {this.NumeroSerieCuotas = NumeroSerieCuotas;}
	
	public double getCantidadCuotasParticipe() {return CantidadCuotasParticipe;}
	public void setCantidadCuotasParticipe(double CantidadCuotasParticipe) {this.CantidadCuotasParticipe = CantidadCuotasParticipe;}
    
	public long getParticipeCumpleRequisitos() {return ParticipeCumpleRequisitos;}
	public void setParticipeCumpleRequisitos(long ParticipeCumpleRequisitos) {this.ParticipeCumpleRequisitos = ParticipeCumpleRequisitos;}
	
	public long getCuotasCumplenRequisitos() {return CuotasCumplenRequisitos;}
	public void setCuotasCumplenRequisitos(long CuotasCumplenRequisitos) {this.CuotasCumplenRequisitos = CuotasCumplenRequisitos;}
	
	public double getValorCuotaPesos() {return ValorCuotaPesos;}
	public void setValorCuotaPesos(double ValorCuotaPesos) {this.ValorCuotaPesos = ValorCuotaPesos;}
	
	public double getNumCuotasSerieParticipe() {return NumCuotasSerieParticipe;}
	public void setNumCuotasSerieParticipe(double NumCuotasSerieParticipe) {this.NumCuotasSerieParticipe = NumCuotasSerieParticipe;}
    
	public double getPorcentajeParticipacion31Dic() {return PorcentajeParticipacion31Dic;}
	public void setPorcentajeParticipacion31Dic(double PorcentajeParticipacion31Dic) {this.PorcentajeParticipacion31Dic = PorcentajeParticipacion31Dic;}
    
	public double getValNominalCuotasRescatadas() {return ValNominalCuotasRescatadas;}
	public void setValNominalCuotasRescatadas(double ValNominalCuotasRescatadas) {this.ValNominalCuotasRescatadas = ValNominalCuotasRescatadas;}
    
	public double getCantidadCuotasRescatadas() {return CantidadCuotasRescatadas;}
	public void setCantidadCuotasRescatadas(double CantidadCuotasRescatadas) {this.CantidadCuotasRescatadas = CantidadCuotasRescatadas;}
    
	public double getDiferenciaPorRescateOEnajenacionActualizado() {return DiferenciaPorRescateOEnajenacionActualizado;}
	public void setDiferenciaPorRescateOEnajenacionActualizado(double DiferenciaPorRescateOEnajenacionActualizado) {this.DiferenciaPorRescateOEnajenacionActualizado = DiferenciaPorRescateOEnajenacionActualizado;}
    
	public double getDifPorRescateOEnajenacionCFIAcogido107Actualizado() {return DifPorRescateOEnajenacionCFIAcogido107Actualizado;}
	public void setDifPorRescateOEnajenacionCFIAcogido107Actualizado(double DifPorRescateOEnajenacionCFIAcogido107Actualizado) {this.DifPorRescateOEnajenacionCFIAcogido107Actualizado = DifPorRescateOEnajenacionCFIAcogido107Actualizado;}
    
	public double getValNominalInversionaFechaLiquidacion() {return ValNominalInversionaFechaLiquidacion;}
	public void setValNominalInversionaFechaLiquidacion(double ValNominalInversionaFechaLiquidacion) {this.ValNominalInversionaFechaLiquidacion = ValNominalInversionaFechaLiquidacion;}
 
	public double getCantCuotasCedidasPorLiquidacion() {return CantCuotasCedidasPorLiquidacion;}
	public void setCantCuotasCedidasPorLiquidacion(double CantCuotasCedidasPorLiquidacion) {this.CantCuotasCedidasPorLiquidacion = CantCuotasCedidasPorLiquidacion;}
    
	public double getDiferenciaValorCuotasFechaLiquidacionActualizado() {return DiferenciaValorCuotasFechaLiquidacionActualizado;}
	public void setDiferenciaValorCuotasFechaLiquidacionActualizado(double DiferenciaValorCuotasFechaLiquidacionActualizado) {this.DiferenciaValorCuotasFechaLiquidacionActualizado = DiferenciaValorCuotasFechaLiquidacionActualizado;}

	public String getTributaSa() {return TributaSa;}
	public void setTributaSa(String TributaSa) {this.TributaSa = TributaSa;}	
	
	public double getDivRemDistAfectGCConIDPC() {return DivRemDistAfectGCConIDPC;}
	public void setDivRemDistAfectGCConIDPC(double DivRemDistAfectGCConIDPC) {this.DivRemDistAfectGCConIDPC = DivRemDistAfectGCConIDPC;}

	public double getDivRemDistAfectGCConIDPCal31122016() {return DivRemDistAfectGCConIDPCal31122016;}
	public void setDivRemDistAfectGCConIDPCal31122016(double DivRemDistAfectGCConIDPCal31122016) {this.DivRemDistAfectGCConIDPCal31122016 = DivRemDistAfectGCConIDPCal31122016;}

	public double getDivRemDistAfectGCConIDPCVOL() {return DivRemDistAfectGCConIDPCVOL;}
	public void setDivRemDistAfectGCConIDPCVOL(double DivRemDistAfectGCConIDPCVOL) {this.DivRemDistAfectGCConIDPCVOL = DivRemDistAfectGCConIDPCVOL;}

	public double getDivRemDistAfectGCSINDC() {return DivRemDistAfectGCSINDC;}
	public void setDivRemDistAfectGCSINDC(double DivRemDistAfectGCSINDC) {this.DivRemDistAfectGCSINDC = DivRemDistAfectGCSINDC;}

	public double getDisRemRentasTribCProvRap_C96() {return DisRemRentasTribCProvRap_C96;}
	public void setDisRemRentasTribCProvRap_C96(double DisRemRentasTribCProvRap_C96) {this.DisRemRentasTribCProvRap_C96 = DisRemRentasTribCProvRap_C96;}

	public double getDisRemRentasTribCORP_C97() {return DisRemRentasTribCORP_C97;}
	public void setDisRemRentasTribCORP_C97(double DisRemRentasTribCORP_C97) {this.DisRemRentasTribCORP_C97 = DisRemRentasTribCORP_C97;}

	public double getDisRemRentasTribCExDD_C98() {return DisRemRentasTribCExDD_C98;}
	public void setDisRemRentasTribCExDD_C98(double DisRemRentasTribCExDD_C98) {this.DisRemRentasTribCExDD_C98 = DisRemRentasTribCExDD_C98;}

	public double getDisRemRentasTribCUtAFFut_C99() {return DisRemRentasTribCUtAFFut_C99;}
	public void setDisRemRentasTribCUtAFFut_C99(double DisRemRentasTribCUtAFFut_C99) {this.DisRemRentasTribCUtAFFut_C99 = DisRemRentasTribCUtAFFut_C99;}

	public double getDivRemDistNoRentaFUT() {return DivRemDistNoRentaFUT;}
	public void setDivRemDistNoRentaFUT(double DivRemDistNoRentaFUT) {this.DivRemDistNoRentaFUT = DivRemDistNoRentaFUT;}

	public double getDisRemRentasExIGCAfecImptoAdi_C100() {return DisRemRentasExIGCAfecImptoAdi_C100;}
	public void setDisRemRentasExIGCAfecImptoAdi_C100(double DisRemRentasExIGCAfecImptoAdi_C100) {this.DisRemRentasExIGCAfecImptoAdi_C100 = DisRemRentasExIGCAfecImptoAdi_C100;}

	public double getCredImpGloCompNSRSD() {return CredImpGloCompNSRSD;}
	public void setCredImpGloCompNSRSD(double CredImpGloCompNSRSD) {this.CredImpGloCompNSRSD = CredImpGloCompNSRSD;}

	public double getCredImpGloCompNSRCD() {return CredImpGloCompNSRCD;}
	public void setCredImpGloCompNSRCD(double CredImpGloCompNSRCD) {this.CredImpGloCompNSRCD = CredImpGloCompNSRCD;}
	
	public double getCredIGCAcumD01012017RANSRD01012020SDD_C101() {return CredIGCAcumD01012017RANSRD01012020SDD_C101;}
	public void setCredIGCAcumD01012017RANSRD01012020SDD_C101(double CredIGCAcumD01012017RANSRD01012020SDD_C101) {this.CredIGCAcumD01012017RANSRD01012020SDD_C101 = CredIGCAcumD01012017RANSRD01012020SDD_C101;}
	
	public double getCredIGCAcumD01012017RANSRD01012020CDD_C102() {return CredIGCAcumD01012017RANSRD01012020CDD_C102;}
	public void setCredIGCAcumD01012017RANSRD01012020CDD_C102(double CredIGCAcumD01012017RANSRD01012020CDD_C102) {this.CredIGCAcumD01012017RANSRD01012020CDD_C102 = CredIGCAcumD01012017RANSRD01012020CDD_C102;}
	
	public double getCredImpGloCompSRSD() {return CredImpGloCompSRSD;}
	public void setCredImpGloCompSRSD(double CredImpGloCompSRSD) {this.CredImpGloCompSRSD = CredImpGloCompSRSD;}
	
	public double getCredImpGloCompSRCD() {return CredImpGloCompSRCD;}
	public void setCredImpGloCompSRCD(double CredImpGloCompSRCD) {this.CredImpGloCompSRCD = CredImpGloCompSRCD;}
	
	public double getCredIGCAcumD01012017RXSRSDD_C103() {return CredIGCAcumD01012017RXSRSDD_C103;}
	public void setCredIGCAcumD01012017RXSRSDD_C103(double CredIGCAcumD01012017RXSRSDD_C103) {this.CredIGCAcumD01012017RXSRSDD_C103 = CredIGCAcumD01012017RXSRSDD_C103;}
	
	public double getCredIGCAcumD01012017RXSRCDD_C104() {return CredIGCAcumD01012017RXSRCDD_C104;}
	public void setCredIGCAcumD01012017RXSRCDD_C104(double CredIGCAcumD01012017RXSRCDD_C104) {this.CredIGCAcumD01012017RXSRCDD_C104 = CredIGCAcumD01012017RXSRCDD_C104;}
	
	public double getCredImpGloCompCTDCIF() {return CredImpGloCompCTDCIF;}
	public void setCredImpGloCompCTDCIF(double CredImpGloCompCTDCIF) {this.CredImpGloCompCTDCIF = CredImpGloCompCTDCIF;}
	
	public double getCredImpGloCompCDD31122016() {return CredImpGloCompCDD31122016;}
	public void setCredImpGloCompCDD31122016(double CredImpGloCompCDD31122016) {this.CredImpGloCompCDD31122016 = CredImpGloCompCDD31122016;}
	
	public double getCredImpGloCompSDD31122016() {return CredImpGloCompSDD31122016;}
	public void setCredImpGloCompSDD31122016(double CredImpGloCompSDD31122016) {this.CredImpGloCompSDD31122016 = CredImpGloCompSDD31122016;}
	
	public double getCredIGCAcumH31122016RXSDD_C105() {return CredIGCAcumH31122016RXSDD_C105;}
	public void setCredIGCAcumH31122016RXSDD_C105(double CredIGCAcumH31122016RXSDD_C105) {this.CredIGCAcumH31122016RXSDD_C105 = CredIGCAcumH31122016RXSDD_C105;}
	
	public double getCredIGCAcumH31122016RXCDD_C106() {return CredIGCAcumH31122016RXCDD_C106;}
	public void setCredIGCAcumH31122016RXCDD_C106(double CredIGCAcumH31122016RXCDD_C106) {this.CredIGCAcumH31122016RXCDD_C106 = CredIGCAcumH31122016RXCDD_C106;}
	
	public double getCredImpGloCompCTDCIF31122016() {return CredImpGloCompCTDCIF31122016;}
	public void setCredImpGloCompCTDCIF31122016(double CredImpGloCompCTDCIF31122016) {this.CredImpGloCompCTDCIF31122016 = CredImpGloCompCTDCIF31122016;}
	
	public double getCredImpGloCompCITAExArt21() {return CredImpGloCompCITAExArt21;}
	public void setCredImpGloCompCITAExArt21(double CredImpGloCompCITAExArt21) {this.CredImpGloCompCITAExArt21 = CredImpGloCompCITAExArt21;}
	
	public String getDetBenNetoPerRutFdo_C107() {return DetBenNetoPerRutFdo_C107;}
	public void setDetBenNetoPerRutFdo_C107(String DetBenNetoPerRutFdo_C107) {this.DetBenNetoPerRutFdo_C107 = DetBenNetoPerRutFdo_C107;}	

	public double getDetBenNetoPerPOS_C113() {return DetBenNetoPerPOS_C113;}
	public void setDetBenNetoPerPOS_C113(double DetBenNetoPerPOS_C113) {this.DetBenNetoPerPOS_C113 = DetBenNetoPerPOS_C113;}
		
	public double getDetBenNetoPerNEG_C114() {return DetBenNetoPerNEG_C114;}
	public void setDetBenNetoPerNEG_C114(double DetBenNetoPerNEG_C114) {this.DetBenNetoPerNEG_C114 = DetBenNetoPerNEG_C114;}
			
	public double getDividendosExentosIGCoAdicionalActualizado() {return DividendosExentosIGCoAdicionalActualizado;}
	public void setDividendosExentosIGCoAdicionalActualizado(double DividendosExentosIGCoAdicionalActualizado) {this.DividendosExentosIGCoAdicionalActualizado = DividendosExentosIGCoAdicionalActualizado;}
		
	public double getDividendosNoConstitutivosDeRentaActualizado() {return DividendosNoConstitutivosDeRentaActualizado;}
	public void setDividendosNoConstitutivosDeRentaActualizado(double DividendosNoConstitutivosDeRentaActualizado) {this.DividendosNoConstitutivosDeRentaActualizado = DividendosNoConstitutivosDeRentaActualizado;}
		
	public double getDivRemDistNoRentaFE() {return DivRemDistNoRentaFE;}
	public void setDivRemDistNoRentaFE(double DivRemDistNoRentaFE) {this.DivRemDistNoRentaFE = DivRemDistNoRentaFE;}
		
	public double getDividendosAfectosImptoUnicoTasa10Actualizado() {return DividendosAfectosImptoUnicoTasa10Actualizado;}
	public void setDividendosAfectosImptoUnicoTasa10Actualizado(double DividendosAfectosImptoUnicoTasa10Actualizado) {this.DividendosAfectosImptoUnicoTasa10Actualizado = DividendosAfectosImptoUnicoTasa10Actualizado;}
				
	public double getDividendosAfectosImptoUnicoTasa4Actualizado() {return DividendosAfectosImptoUnicoTasa4Actualizado;}
	public void setDividendosAfectosImptoUnicoTasa4Actualizado(double DividendosAfectosImptoUnicoTasa4Actualizado) {this.DividendosAfectosImptoUnicoTasa4Actualizado = DividendosAfectosImptoUnicoTasa4Actualizado;}
				
	public double getDistribucionXDiminucionNoImputadaCapital() {return DistribucionXDiminucionNoImputadaCapital;}
	public void setDistribucionXDiminucionNoImputadaCapital(double DistribucionXDiminucionNoImputadaCapital) {this.DistribucionXDiminucionNoImputadaCapital = DistribucionXDiminucionNoImputadaCapital;}		

	public double getDevolucionesDeCapitalActualizado() {return DevolucionesDeCapitalActualizado;}
	public void setDevolucionesDeCapitalActualizado(double DevolucionesDeCapitalActualizado) {this.DevolucionesDeCapitalActualizado = DevolucionesDeCapitalActualizado;}

	public double getMontoInversionenCuotasCFIAnterior93() {return MontoInversionenCuotasCFIAnterior93;}
	public void setMontoInversionenCuotasCFIAnterior93(double MontoInversionenCuotasCFIAnterior93) {this.MontoInversionenCuotasCFIAnterior93 = MontoInversionenCuotasCFIAnterior93;}

	
	public long getNumeroCertificado() {return NumeroCertificado;}
	public void setNumeroCertificado(long NumeroCertificado) {this.NumeroCertificado = NumeroCertificado;}

	public double getDetBenNetoUtilPer_C133() {return DetBenNetoUtilPer_C133;}
	public void setDetBenNetoUtilPer_C133(double DetBenNetoUtilPer_C133) {this.DetBenNetoUtilPer_C133 = DetBenNetoUtilPer_C133;}

	public double getDetBenNetoIntPer_C134() {return DetBenNetoIntPer_C134;}
	public void setDetBenNetoIntPer_C134(double DetBenNetoIntPer_C134) {this.DetBenNetoIntPer_C134 = DetBenNetoIntPer_C134;}

	public double getDetBenNetoDivPer_C135() {return DetBenNetoDivPer_C135;}
	public void setDetBenNetoDivPer_C135(double DetBenNetoDivPer_C135) {this.DetBenNetoDivPer_C135 = DetBenNetoDivPer_C135;}

	public double getDetBenNetoGanCapPer_C136() {return DetBenNetoGanCapPer_C136;}
	public void setDetBenNetoGanCapPer_C136(double DetBenNetoGanCapPer_C136) {this.DetBenNetoGanCapPer_C136 = DetBenNetoGanCapPer_C136;}

	public double getDetBenNetoPerGastDev_C137() {return DetBenNetoPerGastDev_C137;}
	public void setDetBenNetoPerGastDev_C137(double DetBenNetoPerGastDev_C137) {this.DetBenNetoPerGastDev_C137 = DetBenNetoPerGastDev_C137;}

	public String getNumSerieCuotasParticipe_C138() {return NumSerieCuotasParticipe_C138;}
	public void setNumSerieCuotasParticipe_C138(String NumSerieCuotasParticipe_C138) {this.NumSerieCuotasParticipe_C138 = NumSerieCuotasParticipe_C138;}
	
	public String getRunNemotecnico() {return RunNemotecnico;}
	public void setRunNemotecnico(String RunNemotecnico) {this.RunNemotecnico = RunNemotecnico;}

	public String getTipoContribuyente() {return TipoContribuyente;}
	public void setTipoContribuyente(String TipoContribuyente) {this.TipoContribuyente = TipoContribuyente;}	
	
	public RegistroDJ1922() {};	
	
	public double getsumaDiferenciaPorRescateOEnajenacionActualizado() {
		return sumaDiferenciaPorRescateOEnajenacionActualizado;
	}

	public void setsumaDiferenciaPorRescateOEnajenacionActualizado(double sumaDiferenciaPorRescateOEnajenacionActualizado) {
		this.sumaDiferenciaPorRescateOEnajenacionActualizado = sumaDiferenciaPorRescateOEnajenacionActualizado;
	}
				
	public double getsumaDifPorRescateOEnajenacionCFIAcogido107Actualizado() {
		return sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado;
	}

	public void setsumaDifPorRescateOEnajenacionCFIAcogido107Actualizado(double sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado) {
		this.sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado = sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado;
	}
	
	public double getsumaDiferenciaValorCuotasFechaLiquidacionActualizado() {
		return sumaDiferenciaValorCuotasFechaLiquidacionActualizado;
	}

	public void setsumaDiferenciaValorCuotasFechaLiquidacionActualizado(double sumaDiferenciaValorCuotasFechaLiquidacionActualizado) {
		this.sumaDiferenciaValorCuotasFechaLiquidacionActualizado = sumaDiferenciaValorCuotasFechaLiquidacionActualizado;
	}
		
	
	
	public double getsumaDivRemDistAfectGCConIDPC() {
		return sumaDivRemDistAfectGCConIDPC;
	}

	public void setsumDivRemDistAfectGCConIDPC(double sumaDivRemDistAfectGCConIDPC) {
		this.sumaDivRemDistAfectGCConIDPC = sumaDivRemDistAfectGCConIDPC;
	}
		
	
	public double getsumaDivRemDistAfectGCConIDPCal31122016() {
		return sumaDivRemDistAfectGCConIDPCal31122016;
	}

	public void setsumDivRemDistAfectGCConIDPCal31122016(double sumaDivRemDistAfectGCConIDPCal31122016) {
		this.sumaDivRemDistAfectGCConIDPCal31122016 = sumaDivRemDistAfectGCConIDPCal31122016;
	}
		
	public double getsumaDivRemDistAfectGCConIDPCVOL() {
		return sumaDivRemDistAfectGCConIDPCVOL;
	}

	public void setsumDivRemDistAfectGCConIDPCVOL(double sumaDivRemDistAfectGCConIDPCVOL) {
		this.sumaDivRemDistAfectGCConIDPCVOL = sumaDivRemDistAfectGCConIDPCVOL;
	}
		

	public double getsumaDivRemDistAfectGCSINDC() {
		return sumaDivRemDistAfectGCSINDC;
	}

	public void setsumDivRemDistAfectGCSINDC(double sumaDivRemDistAfectGCSINDC) {
		this.sumaDivRemDistAfectGCSINDC = sumaDivRemDistAfectGCSINDC;
	}


	public double getsumaDisRemRentasTribCProvRap_C96() {
		return sumaDisRemRentasTribCProvRap_C96;
	}

	public void setsumDisRemRentasTribCProvRap_C96(double sumaDisRemRentasTribCProvRap_C96) {
		this.sumaDisRemRentasTribCProvRap_C96 = sumaDisRemRentasTribCProvRap_C96;
	}

	
	public double getsumaDisRemRentasTribCORP_C97() {
		return sumaDisRemRentasTribCORP_C97;
	}

	public void setsumDisRemRentasTribCORP_C97(double sumaDisRemRentasTribCORP_C97) {
		this.sumaDisRemRentasTribCORP_C97 = sumaDisRemRentasTribCORP_C97;
	}

	public double getsumaDisRemRentasTribCExDD_C98() {
		return sumaDisRemRentasTribCExDD_C98;
	}

	public void setsumaDisRemRentasTribCExDD_C98(double sumaDisRemRentasTribCExDD_C98) {
		this.sumaDisRemRentasTribCExDD_C98 = sumaDisRemRentasTribCExDD_C98;
	}

	
	public double getsumaDisRemRentasTribCUtAFFut_C99() {
		return sumaDisRemRentasTribCUtAFFut_C99;
	}

	public void setsumaDisRemRentasTribCUtAFFut_C99(double sumaDisRemRentasTribCUtAFFut_C99) {
		this.sumaDisRemRentasTribCUtAFFut_C99 = sumaDisRemRentasTribCUtAFFut_C99;
	}

	public double getsumaDivRemDistNoRentaFUT() {
		return sumaDivRemDistNoRentaFUT;
	}

	public void setsumaDivRemDistNoRentaFUT(double sumaDivRemDistNoRentaFUT) {
		this.sumaDivRemDistNoRentaFUT = sumaDivRemDistNoRentaFUT;
	}
	
	
	public double getsumaDisRemRentasExIGCAfecImptoAdi_C100() {
		return sumaDisRemRentasExIGCAfecImptoAdi_C100;
	}

	public void setsumaDisRemRentasExIGCAfecImptoAdi_C100(double sumaDisRemRentasExIGCAfecImptoAdi_C100) {
		this.sumaDisRemRentasExIGCAfecImptoAdi_C100 = sumaDisRemRentasExIGCAfecImptoAdi_C100;
	}
	
	
	public double getsumaDividendosExentosIGCoAdicionalActualizado() {
		return sumaDividendosExentosIGCoAdicionalActualizado;
	}

	public void setsumaDividendosExentosIGCoAdicionalActualizado(double sumaDividendosExentosIGCoAdicionalActualizado) {
		this.sumaDividendosExentosIGCoAdicionalActualizado = sumaDividendosExentosIGCoAdicionalActualizado;
	}
	
	
	
	public double getsumaDividendosNoConstitutivosDeRentaActualizado() {
		return sumaDividendosNoConstitutivosDeRentaActualizado;
	}

	public void setsumaDividendosNoConstitutivosDeRentaActualizado(double sumaDividendosNoConstitutivosDeRentaActualizado) {
		this.sumaDividendosNoConstitutivosDeRentaActualizado = sumaDividendosNoConstitutivosDeRentaActualizado;
	}
	
	

	public double getsumaDivRemDistNoRentaFE() {
		return sumaDivRemDistNoRentaFE;
	}

	public void setsumaDivRemDistNoRentaFE(double sumaDivRemDistNoRentaFE) {
		this.sumaDivRemDistNoRentaFE = sumaDivRemDistNoRentaFE;
	}
	

	public double getsumaDividendosAfectosImptoUnicoTasa10Actualizado() {
		return sumaDividendosAfectosImptoUnicoTasa10Actualizado;
	}

	public void setsumaDividendosAfectosImptoUnicoTasa10Actualizado(double sumaDividendosAfectosImptoUnicoTasa10Actualizado) {
		this.sumaDividendosAfectosImptoUnicoTasa10Actualizado = sumaDividendosAfectosImptoUnicoTasa10Actualizado;
	}
	
	public double getsumaDividendosAfectosImptoUnicoTasa4Actualizado() {
		return sumaDividendosAfectosImptoUnicoTasa4Actualizado;
	}

	public void setsumaDividendosAfectosImptoUnicoTasa4Actualizado(double sumaDividendosAfectosImptoUnicoTasa4Actualizado) {
		this.sumaDividendosAfectosImptoUnicoTasa4Actualizado = sumaDividendosAfectosImptoUnicoTasa4Actualizado;
	}
	
	public double getsumaDistribucionXDiminucionNoImputadaCapital() {
		return sumaDistribucionXDiminucionNoImputadaCapital;
	}

	public void setsumaDistribucionXDiminucionNoImputadaCapital(double sumaDistribucionXDiminucionNoImputadaCapital) {
		this.sumaDistribucionXDiminucionNoImputadaCapital = sumaDistribucionXDiminucionNoImputadaCapital;
	}
	
	
	
	public double getsumaDevolucionesDeCapitalActualizado() {
		return sumaDevolucionesDeCapitalActualizado;
	}

	public void setsumaDevolucionesDeCapitalActualizado(double sumaDevolucionesDeCapitalActualizado) {
		this.sumaDevolucionesDeCapitalActualizado = sumaDevolucionesDeCapitalActualizado;
	}
	
	
	public double getsumaCredImpGloCompNSRSD() {
		return sumaCredImpGloCompNSRSD;
	}

	public void setsumaCredImpGloCompNSRSD(double sumaCredImpGloCompNSRSD) {
		this.sumaCredImpGloCompNSRSD = sumaCredImpGloCompNSRSD;
	}
	
	public double getsumaCredImpGloCompNSRCD() {
		return sumaCredImpGloCompNSRCD;
	}

	public void setsumaCredImpGloCompNSRCD(double sumaCredImpGloCompNSRCD) {
		this.sumaCredImpGloCompNSRCD = sumaCredImpGloCompNSRCD;
	}
	
	
	public double getsumaCredIGCAcumD01012017RANSRD01012020SDD_C101() {
		return sumaCredIGCAcumD01012017RANSRD01012020SDD_C101;
	}

	public void setsumaCredIGCAcumD01012017RANSRD01012020SDD_C101(double sumaCredIGCAcumD01012017RANSRD01012020SDD_C101) {
		this.sumaCredIGCAcumD01012017RANSRD01012020SDD_C101 = sumaCredIGCAcumD01012017RANSRD01012020SDD_C101;
	}
	
	public double getsumaCredIGCAcumD01012017RANSRD01012020CDD_C102() {
		return sumaCredIGCAcumD01012017RANSRD01012020CDD_C102;
	}

	public void setsumaCredIGCAcumD01012017RANSRD01012020CDD_C102(double sumaCredIGCAcumD01012017RANSRD01012020CDD_C102) {
		this.sumaCredIGCAcumD01012017RANSRD01012020CDD_C102 = sumaCredIGCAcumD01012017RANSRD01012020CDD_C102;
	}
	
	public double getsumaCredImpGloCompSRSD() {
		return sumaCredImpGloCompSRSD;
	}

	public void setsumaCredImpGloCompSRSD(double sumaCredImpGloCompSRSD) {
		this.sumaCredImpGloCompSRSD = sumaCredImpGloCompSRSD;
	}
	
	public double getsumaCredImpGloCompSRCD() {
		return sumaCredImpGloCompSRCD;
	}

	
	public void setsumaCredImpGloCompSRCD(double sumaCredImpGloCompSRCD) {
			this.sumaCredImpGloCompSRCD = sumaCredImpGloCompSRCD;
	}	

	
	public double getsumaCredIGCAcumD01012017RXSRSDD_C103() {
		return sumaCredIGCAcumD01012017RXSRSDD_C103;
	}

	
	public void setsumaCredIGCAcumD01012017RXSRSDD_C103(double sumaCredIGCAcumD01012017RXSRSDD_C103) {
			this.sumaCredIGCAcumD01012017RXSRSDD_C103 = sumaCredIGCAcumD01012017RXSRSDD_C103;
	}	
	
	public double getsumaCredIGCAcumD01012017RXSRCDD_C104() {
		return sumaCredIGCAcumD01012017RXSRCDD_C104;
	}

	
	public void setsumaCredIGCAcumD01012017RXSRCDD_C104(double sumaCredIGCAcumD01012017RXSRCDD_C104) {
			this.sumaCredIGCAcumD01012017RXSRCDD_C104 = sumaCredIGCAcumD01012017RXSRCDD_C104;
	}	
	
	public double getsumaCredImpGloCompCTDCIF() {
		return sumaCredImpGloCompCTDCIF;
	}

	
	public void setsumaCredImpGloCompCTDCIF(double sumaCredImpGloCompCTDCIF) {
			this.sumaCredImpGloCompCTDCIF = sumaCredImpGloCompCTDCIF;
	}	
	
	public double getsumaCredImpGloCompCDD31122016() {
		return sumaCredImpGloCompCDD31122016;
	}

	
	public void setsumaCredImpGloCompCDD31122016(double sumaCredImpGloCompCDD31122016) {
			this.sumaCredImpGloCompCDD31122016 = sumaCredImpGloCompCDD31122016;
	}	
			
	public double getsumaCredImpGloCompSDD31122016() {
		return sumaCredImpGloCompSDD31122016;
	}

	
	public void setsumaCredImpGloCompSDD31122016(double sumaCredImpGloCompSDD31122016) {
			this.sumaCredImpGloCompSDD31122016 = sumaCredImpGloCompSDD31122016;
	}		
	
	public double getsumaCredIGCAcumH31122016RXSDD_C105() {
		return sumaCredIGCAcumH31122016RXSDD_C105;
	}

	
	public void setsumaCredIGCAcumH31122016RXSDD_C105(double sumaCredIGCAcumH31122016RXSDD_C105) {
			this.sumaCredIGCAcumH31122016RXSDD_C105 = sumaCredIGCAcumH31122016RXSDD_C105;
	}		
	public double getsumaCredIGCAcumH31122016RXCDD_C106() {
		return sumaCredIGCAcumH31122016RXCDD_C106;
	}

	
	public void setsumaCredIGCAcumH31122016RXCDD_C106(double sumaCredIGCAcumH31122016RXCDD_C106) {
			this.sumaCredIGCAcumH31122016RXCDD_C106 = sumaCredIGCAcumH31122016RXCDD_C106;
	}	
	
	
	public double getsumaCredImpGloCompCTDCIF31122016() {
		return sumaCredImpGloCompCTDCIF31122016;
	}

	
	public void setsumaCredImpGloCompCTDCIF31122016(double sumaCredImpGloCompCTDCIF31122016) {
			this.sumaCredImpGloCompCTDCIF31122016 = sumaCredImpGloCompCTDCIF31122016;
	}	
	
	public double getsumaCredImpGloCompCITAExArt21() {
		return sumaCredImpGloCompCITAExArt21;
	}

	
	public void setsumaCredImpGloCompCITAExArt21(double sumaCredImpGloCompCITAExArt21) {
			this.sumaCredImpGloCompCITAExArt21 = sumaCredImpGloCompCITAExArt21;
	}	
	

	public double getsumaMontoInversionenCuotasCFIAnterior93() {
		return sumaMontoInversionenCuotasCFIAnterior93;
	}

	
	public void setsumaMontoInversionenCuotasCFIAnterior93(double sumaMontoInversionenCuotasCFIAnterior93) {
			this.sumaMontoInversionenCuotasCFIAnterior93 = sumaMontoInversionenCuotasCFIAnterior93;
	}	
	


	public double getsumaDetBenNetoPerPOS_C113() {
		return sumaDetBenNetoPerPOS_C113;
	}

	
	public void setsumaDetBenNetoPerPOS_C113(double sumaDetBenNetoPerPOS_C113) {
			this.sumaDetBenNetoPerPOS_C113 = sumaDetBenNetoPerPOS_C113;
	}	
	
	public double getsumaDetBenNetoPerNEG_C114() {
		return sumaDetBenNetoPerNEG_C114;
	}

	
	public void setsumaDetBenNetoPerNEG_C114(double sumaDetBenNetoPerNEG_C114) {
			this.sumaDetBenNetoPerNEG_C114 = sumaDetBenNetoPerNEG_C114;
	}	
		
	private double r(double valor) {
		return Math.round(valor); // devuelve long pero se castea a double (ej: 20.0)
	}
	
	
	public void acumular(RegistroDJ1922 r) {
		this.sumaDiferenciaPorRescateOEnajenacionActualizado += r(r.getDiferenciaPorRescateOEnajenacionActualizado());
		this.sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado += r(r.getDifPorRescateOEnajenacionCFIAcogido107Actualizado());
		this.sumaDiferenciaValorCuotasFechaLiquidacionActualizado += r(r.getDiferenciaValorCuotasFechaLiquidacionActualizado());
		this.sumaDivRemDistAfectGCConIDPC += r(r.getDivRemDistAfectGCConIDPC());
		this.sumaDivRemDistAfectGCConIDPCal31122016 += r(r.getDivRemDistAfectGCConIDPCal31122016());
		this.sumaDivRemDistAfectGCConIDPCVOL += r(r.getDivRemDistAfectGCConIDPCVOL());
		this.sumaDivRemDistAfectGCSINDC += r(r.getDivRemDistAfectGCSINDC());
		this.sumaDisRemRentasTribCProvRap_C96 += r(r.getDisRemRentasTribCProvRap_C96());
		this.sumaDisRemRentasTribCORP_C97 += r(r.getDisRemRentasTribCORP_C97());
		this.sumaDisRemRentasTribCExDD_C98 += r(r.getDisRemRentasTribCExDD_C98());
		this.sumaDisRemRentasTribCUtAFFut_C99 += r(r.getDisRemRentasTribCUtAFFut_C99());
		this.sumaDivRemDistNoRentaFUT += r(r.getDivRemDistNoRentaFUT());
		this.sumaDisRemRentasExIGCAfecImptoAdi_C100 += r(r.getDisRemRentasExIGCAfecImptoAdi_C100());
		this.sumaDividendosExentosIGCoAdicionalActualizado += r(r.getDividendosExentosIGCoAdicionalActualizado());
		this.sumaDividendosNoConstitutivosDeRentaActualizado += r(r.getDividendosNoConstitutivosDeRentaActualizado());
		this.sumaDivRemDistNoRentaFE += r(r.getDivRemDistNoRentaFE());
		this.sumaDividendosAfectosImptoUnicoTasa10Actualizado += r(r.getDividendosAfectosImptoUnicoTasa10Actualizado());
		this.sumaDividendosAfectosImptoUnicoTasa4Actualizado += r(r.getDividendosAfectosImptoUnicoTasa4Actualizado());
		this.sumaDistribucionXDiminucionNoImputadaCapital += r(r.getDistribucionXDiminucionNoImputadaCapital());
		this.sumaDevolucionesDeCapitalActualizado += r(r.getDevolucionesDeCapitalActualizado());
		this.sumaCredImpGloCompNSRSD += r(r.getCredImpGloCompNSRSD());
		this.sumaCredImpGloCompNSRCD += r(r.getCredImpGloCompNSRCD());
		this.sumaCredIGCAcumD01012017RANSRD01012020SDD_C101 += r(r.getCredIGCAcumD01012017RANSRD01012020SDD_C101());
		this.sumaCredIGCAcumD01012017RANSRD01012020CDD_C102 += r(r.getCredIGCAcumD01012017RANSRD01012020CDD_C102());
		this.sumaCredImpGloCompSRSD += r(r.getCredImpGloCompSRSD());
		this.sumaCredImpGloCompSRCD += r(r.getCredImpGloCompSRCD());
		this.sumaCredIGCAcumD01012017RXSRSDD_C103 += r(r.getCredIGCAcumD01012017RXSRSDD_C103());
		this.sumaCredIGCAcumD01012017RXSRCDD_C104 += r(r.getCredIGCAcumD01012017RXSRCDD_C104());
		this.sumaCredImpGloCompCTDCIF += r(r.getCredImpGloCompCTDCIF());
		this.sumaCredImpGloCompCDD31122016 += r(r.getCredImpGloCompCDD31122016());
		this.sumaCredImpGloCompSDD31122016 += r(r.getCredImpGloCompSDD31122016());
		this.sumaCredIGCAcumH31122016RXSDD_C105 += r(r.getCredIGCAcumH31122016RXSDD_C105());
		this.sumaCredIGCAcumH31122016RXCDD_C106 += r(r.getCredIGCAcumH31122016RXCDD_C106());
		this.sumaCredImpGloCompCTDCIF31122016 += r(r.getCredImpGloCompCTDCIF31122016());
		this.sumaCredImpGloCompCITAExArt21 += r(r.getCredImpGloCompCITAExArt21());
		this.sumaMontoInversionenCuotasCFIAnterior93 += r(r.getMontoInversionenCuotasCFIAnterior93());
		this.sumaDetBenNetoPerPOS_C113 += r(r.getDetBenNetoPerPOS_C113());
		this.sumaDetBenNetoPerNEG_C114 += r(r.getDetBenNetoPerNEG_C114());
	}

	
	
	
	
	/*
    public void acumular(final RegistroDJ1922 registroDJ1922) {        
        this.sumaDiferenciaPorRescateOEnajenacionActualizado += registroDJ1922.getSumaDiferenciaPorRescateOEnajenacionActualizado();
        this.sumaDifPorRescateOEnajenacionCFIAcogido107Actualizado += registroDJ1922.getSumaDifPorRescateOEnajenacionCFIAcogido107Actualizado();
        this.sumaDiferenciaValorCuotasFechaLiquidacionActualizado += registroDJ1922.getSumaDiferenciaValorCuotasFechaLiquidacionActualizado();
        this.sumaDividendosAfectosIGCoAdicionalActualizado += registroDJ1922.getSumaDividendosAfectosIGCoAdicionalActualizado();
        this.sumaDividendosExentosIGCoAdicionalActualizado += registroDJ1922.getSumaDividendosExentosIGCoAdicionalActualizado();
        this.sumaDividendosNoConstitutivosDeRentaActualizado += registroDJ1922.getSumaDividendosNoConstitutivosDeRentaActualizado();
        this.sumaDividendosAfectosImptoUnicoTasa10Actualizado += registroDJ1922.getSumaDividendosAfectosImptoUnicoTasa10Actualizado();
        this.sumaDividendosNoAfectosImptoUnicoTasa10Actualizado += registroDJ1922.getSumaDividendosNoAfectosImptoUnicoTasa10Actualizado();
        this.sumaDividendosAfectosImptoUnicoTasa4Actualizado += registroDJ1922.getSumaDividendosAfectosImptoUnicoTasa4Actualizado();
        this.sumaDistribucionXDiminucionNoImputadaCapital += registroDJ1922.getSumaDistribucionXDiminucionNoImputadaCapital();
        this.sumaDevolucionesDeCapitalActualizado += registroDJ1922.getSumaDevolucionesDeCapitalActualizado();
        this.sumaIncrementoPorImpuesto += registroDJ1922.getSumaIncrementoPorImpuesto();
        this.sumaCreditoImpto1CatConDevolucionAfectas += registroDJ1922.getSumaCreditoImpto1CatConDevolucionAfectas();
        this.sumaCreditoImpto1CatSinDevolucionAfectas += registroDJ1922.getSumaCreditoImpto1CatSinDevolucionAfectas();
        this.sumaCreditoImpto1CatConDevolucionExentas += registroDJ1922.getSumaCreditoImpto1CatConDevolucionExentas();
        this.sumaCreditoImpto1CatSinDevolucionExentas += registroDJ1922.getSumaCreditoImpto1CatSinDevolucionExentas();
        this.sumaCreditoPorImpuestosExternos += registroDJ1922.getSumaCreditoPorImpuestosExternos();
        this.sumaImpuestosTasaAdicionalExArticulo21 += registroDJ1922.getSumaImpuestosTasaAdicionalExArticulo21();
        this.sumaMontoInversionenCuotasCFIAnterior93 += registroDJ1922.getSumaMontoInversionenCuotasCFIAnterior93();
    }
	*/
}
