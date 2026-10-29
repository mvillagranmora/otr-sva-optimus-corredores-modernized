package cl.bcs.normativa.registros;

import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.utils.Fecha;

public class RegistroDJ1 {	
    /**
     * Declaracion de variables para la DJ
     */
	private Long seccion;
	private String fuenteDatos;
	private String kRutCtraparte1;	
	private String kIdContrato;
	private Date kFechaContrato;
	private String horaContrato;
	private String kIdOpeEstruc;
	private String s1_NaturaInfo;
	private String s1_EventoReporte;
	private String s1_LEICtraparte1;
	private String s1_RutCtraparte2;
	private String s1_LEICtraparte2;
	private String s1_NomCtraparte2;
	private String s1_PaisCtraparte2;
	private String s1_RutEntidad;
	private String s1_LEIEntidad;
	private String s1_RUTLiqCtraparte1;
	private String s1_LEILiqCtraparte1;
	private String s1_RutAgenteCal;
	private String s1_LEIAgenteCal;
	private String s1_NomAgenteCal;
	private String s1_RutCedente;
	private String s1_LEICedente;
	private String s1_PlataformaNego;
	private String s1_RutIntermediario;
	private String s1_LEIIntermediario;
	private String s2_Instrumento;
	private String s2_IdContratoAgente;
	private String s2_OpsClase;
	private String s2_OpPosicionOp;
	private String s2_ModalidadLiq;
	private String s2_MonedaComp;
	private Date s2_FechaInicio;
	private Date s2_FechaFixing1;
	private Date s2_FechaTermino;
	private Date s2_FechaPagOpe;
	private Date s2_FechaInicioMod;
	private String s2_MonedaPrima;
	private double s2_MontoPrima;
	private String s2_ObjetivoOpe;
	private String s2_ConvenioMarco;
	private String s2_PaisContrato;
	private String s2_Garantias;
	private String s2_ClausulaRecou;
	private String s2_ClausulaTermino;
	private Date s2_FechaEfecTermino;
	private String s2_CompresionCartera;
	private Long s2_NumTotIntercam;
	private Long s2_NumTotPagos;
	private String s3_XR_OpInstruSub;
	private String s3_MonedaComprada;
	private double s3_MontoComprada;
	private String s3_XR_MonedaVendida;
	private double s3_XR_MontoVendida;
	private String s3_XR_TipoTasaEnt;
	private String s3_XR_TipoTasaRec;
	private double s3_R_TasaFija;
	private double s3_R_Spread;
	private String s3_I_TipoActivoSub;
	private String s3_I_IdActivoSuby;
	private double s3_I_TasaPactada;
	private Date s3_XR_FechaFixing2;
	private String s3_XR_ConvTipoCambio;
	private double s3_PrecioFuturo;
	private double s3_XR_PuntosForward;
	private double s3_XR_PrecioEntrada;
	private double s3_XR_PrecioSalida;
	private String s3_I_PosicionOpe;
	private String s8_MtodoValoriza;
	private String s8_MonedaValoriza;
	private double s8_ValorMercado;
	private double s8_OpVolatilidad;
	private double s8_OpDelta;
	private double s8_OpGamma;
	private double s8_OpVega;
	private double s8_AjusteRiesgo;
	private double s8_AjusteBID;
	private double s8_OtrosAjustes;
	private double s8_ValorMercadoActivo;
	private double s8_ValorMercadoPasivo;
	private Long kS4_XR_NroFlu;
	private String s4_XR_TipoFlujo;
	private double s4_XR_FacTasa;
	private double s4_XR_SpreadTasa;
	private double s4_XR_TasaFija;
	private Date s4_XR_FecFixTasa;
	private Date s4_XR_FecIni;
	private Date s4_XR_FecTer;
	private double s4_XR_MontoNoc;
	private double s4_XR_MontoInt;
	private String s4_X_MonMonto;
	private double kS5_CorrPago;
	private String kS5_IdPago;
	private Date s5_FecPago;
	private double s5_MontoPago;
	private String s5_TipoPago;
	private String S5_CodMoneda;
	private String s6_TipoGar;
	private String s6_MonVal;
	private String kS6_IdGar;
	private double s6_MontoUmb;
	private double s6_ValGtia;
	private String s7_TipoGar;
	private String kS7_IdGar;
	private String s7_TipoActGar;
	private double s7_PorcGar;
	private String kProducto;
	private Date kPeriodoComercial;
	private String Rectifica;

	public Long getSeccion() {
		return seccion;
	}

	public void setSeccion(Long seccion) {
		this.seccion = seccion;
	}
	
	public String getfuenteDatos() {
		return fuenteDatos;
	}

	public void setfuenteDatos(String fuenteDatos) {
		this.fuenteDatos = fuenteDatos;
	}

	public String getkRutCtraparte1() {
		return kRutCtraparte1;
	}

	public void setkRutCtraparte1(String kRutCtraparte1) {
		this.kRutCtraparte1 = kRutCtraparte1;
	}

	public String getkIdContrato() {
		return kIdContrato;
	}

	public void setkIdContrato(String kIdContrato) {
		this.kIdContrato = kIdContrato;
	}

	public Date getkFechaContrato() {
		return kFechaContrato;
	}

	public void setkFechaContrato(Date kFechaContrato) {
		this.kFechaContrato = kFechaContrato;
	}
	
	public String getHoraContrato() {
		return horaContrato;
	}

	public void setHoraContrato(String horaContrato) {
		this.horaContrato = horaContrato;
	}

	public String getkIdOpeEstruc() {
		return kIdOpeEstruc;
	}

	public void setkIdOpeEstruc(String kIdOpeEstruc) {
		this.kIdOpeEstruc = kIdOpeEstruc;
	}

	public String getS1_NaturaInfo() {
		return s1_NaturaInfo;
	}

	public void setS1_NaturaInfo(String s1_NaturaInfo) {
		this.s1_NaturaInfo = s1_NaturaInfo;
	}

	public String getS1_EventoReporte() {
		return s1_EventoReporte;
	}

	public void setS1_EventoReporte(String s1_EventoReporte) {
		this.s1_EventoReporte = s1_EventoReporte;
	}

	public String getS1_LEICtraparte1() {
		return s1_LEICtraparte1;
	}

	public void setS1_LEICtraparte1(String s1_LEICtraparte1) {
		this.s1_LEICtraparte1 = s1_LEICtraparte1;
	}

	public String getS1_RutCtraparte2() {
		return s1_RutCtraparte2;
	}

	public void setS1_RutCtraparte2(String s1_RutCtraparte2) {
		this.s1_RutCtraparte2 = s1_RutCtraparte2;
	}

	public String getS1_LEICtraparte2() {
		return s1_LEICtraparte2;
	}

	public void setS1_LEICtraparte2(String s1_LEICtraparte2) {
		this.s1_LEICtraparte2 = s1_LEICtraparte2;
	}

	public String getS1_NomCtraparte2() {
		return s1_NomCtraparte2;
	}

	public void setS1_NomCtraparte2(String s1_NomCtraparte2) {
		this.s1_NomCtraparte2 = s1_NomCtraparte2;
	}

	public String getS1_PaisCtraparte2() {
		return s1_PaisCtraparte2;
	}

	public void setS1_PaisCtraparte2(String s1_PaisCtraparte2) {
		this.s1_PaisCtraparte2 = s1_PaisCtraparte2;
	}

	public String getS1_RutEntidad() {
		return s1_RutEntidad;
	}

	public void setS1_RutEntidad(String s1_RutEntidad) {
		this.s1_RutEntidad = s1_RutEntidad;
	}

	public String getS1_LEIEntidad() {
		return s1_LEIEntidad;
	}

	public void setS1_LEIEntidad(String s1_LEIEntidad) {
		this.s1_LEIEntidad = s1_LEIEntidad;
	}

	public String getS1_RUTLiqCtraparte1() {
		return s1_RUTLiqCtraparte1;
	}

	public void setS1_RUTLiqCtraparte1(String s1_RUTLiqCtraparte1) {
		this.s1_RUTLiqCtraparte1 = s1_RUTLiqCtraparte1;
	}

	public String getS1_LEILiqCtraparte1() {
		return s1_LEILiqCtraparte1;
	}

	public void setS1_LEILiqCtraparte1(String s1_LEILiqCtraparte1) {
		this.s1_LEILiqCtraparte1 = s1_LEILiqCtraparte1;
	}

	public String getS1_RutAgenteCal() {
		return s1_RutAgenteCal;
	}

	public void setS1_RutAgenteCal(String s1_RutAgenteCal) {
		this.s1_RutAgenteCal = s1_RutAgenteCal;
	}

	public String getS1_LEIAgenteCal() {
		return s1_LEIAgenteCal;
	}

	public void setS1_LEIAgenteCal(String s1_LEIAgenteCal) {
		this.s1_LEIAgenteCal = s1_LEIAgenteCal;
	}

	public String getS1_NomAgenteCal() {
		return s1_NomAgenteCal;
	}

	public void setS1_NomAgenteCal(String s1_NomAgenteCal) {
		this.s1_NomAgenteCal = s1_NomAgenteCal;
	}

	public String getS1_RutCedente() {
		return s1_RutCedente;
	}

	public void setS1_RutCedente(String s1_RutCedente) {
		this.s1_RutCedente = s1_RutCedente;
	}

	public String getS1_LEICedente() {
		return s1_LEICedente;
	}

	public void setS1_LEICedente(String s1_LEICedente) {
		this.s1_LEICedente = s1_LEICedente;
	}

	public String getS1_PlataformaNego() {
		return s1_PlataformaNego;
	}

	public void setS1_PlataformaNego(String s1_PlataformaNego) {
		this.s1_PlataformaNego = s1_PlataformaNego;
	}

	public String getS1_RutIntermediario() {
		return s1_RutIntermediario;
	}

	public void setS1_RutIntermediario(String s1_RutIntermediario) {
		this.s1_RutIntermediario = s1_RutIntermediario;
	}

	public String getS1_LEIIntermediario() {
		return s1_LEIIntermediario;
	}

	public void setS1_LEIIntermediario(String s1_LEIIntermediario) {
		this.s1_LEIIntermediario = s1_LEIIntermediario;
	}

	public String getS2_Instrumento() {
		return s2_Instrumento;
	}

	public void setS2_Instrumento(String s2_Instrumento) {
		this.s2_Instrumento = s2_Instrumento;
	}

	public String getS2_IdContratoAgente() {
		return s2_IdContratoAgente;
	}

	public void setS2_IdContratoAgente(String s2_IdContratoAgente) {
		this.s2_IdContratoAgente = s2_IdContratoAgente;
	}

	public String getS2_OpsClase() {
		return s2_OpsClase;
	}

	public void setS2_OpsClase(String s2_OpsClase) {
		this.s2_OpsClase = s2_OpsClase;
	}

	public String getS2_OpPosicionOp() {
		return s2_OpPosicionOp;
	}

	public void setS2_OpPosicionOp(String s2_OpPosicionOp) {
		this.s2_OpPosicionOp = s2_OpPosicionOp;
	}

	public String getS2_ModalidadLiq() {
		return s2_ModalidadLiq;
	}

	public void setS2_ModalidadLiq(String s2_ModalidadLiq) {
		this.s2_ModalidadLiq = s2_ModalidadLiq;
	}

	public String getS2_MonedaComp() {
		return s2_MonedaComp;
	}

	public void setS2_MonedaComp(String s2_MonedaComp) {
		this.s2_MonedaComp = s2_MonedaComp;
	}

	public Date getS2_FechaInicio() {
		return s2_FechaInicio;
	}

	public void setS2_FechaInicio(Date s2_FechaInicio) {
		this.s2_FechaInicio = s2_FechaInicio;
	}

	public Date getS2_FechaFixing1() {
		return s2_FechaFixing1;
	}

	public void setS2_FechaFixing1(Date s2_FechaFixing1) {
		this.s2_FechaFixing1 = s2_FechaFixing1;
	}

	public Date getS2_FechaTermino() {
		return s2_FechaTermino;
	}

	public void setS2_FechaTermino(Date s2_FechaTermino) {
		this.s2_FechaTermino = s2_FechaTermino;
	}

	public Date getS2_FechaPagOpe() {
		return s2_FechaPagOpe;
	}

	public void setS2_FechaPagOpe(Date s2_FechaPagOpe) {
		this.s2_FechaPagOpe = s2_FechaPagOpe;
	}

	public Date getS2_FechaInicioMod() {
		return s2_FechaInicioMod;
	}

	public void setS2_FechaInicioMod(Date s2_FechaInicioMod) {
		this.s2_FechaInicioMod = s2_FechaInicioMod;
	}

	public String getS2_MonedaPrima() {
		return s2_MonedaPrima;
	}

	public void setS2_MonedaPrima(String s2_MonedaPrima) {
		this.s2_MonedaPrima = s2_MonedaPrima;
	}

	public double getS2_MontoPrima() {
		return s2_MontoPrima;
	}

	public void setS2_MontoPrima(double s2_MontoPrima) {
		this.s2_MontoPrima = s2_MontoPrima;
	}

	public String getS2_ObjetivoOpe() {
		return s2_ObjetivoOpe;
	}

	public void setS2_ObjetivoOpe(String s2_ObjetivoOpe) {
		this.s2_ObjetivoOpe = s2_ObjetivoOpe;
	}

	public String getS2_ConvenioMarco() {
		return s2_ConvenioMarco;
	}

	public void setS2_ConvenioMarco(String s2_ConvenioMarco) {
		this.s2_ConvenioMarco = s2_ConvenioMarco;
	}

	public String getS2_PaisContrato() {
		return s2_PaisContrato;
	}

	public void setS2_PaisContrato(String s2_PaisContrato) {
		this.s2_PaisContrato = s2_PaisContrato;
	}

	public String getS2_Garantias() {
		return s2_Garantias;
	}

	public void setS2_Garantias(String s2_Garantias) {
		this.s2_Garantias = s2_Garantias;
	}

	public String getS2_ClausulaRecou() {
		return s2_ClausulaRecou;
	}

	public void setS2_ClausulaRecou(String s2_ClausulaRecou) {
		this.s2_ClausulaRecou = s2_ClausulaRecou;
	}

	public String getS2_ClausulaTermino() {
		return s2_ClausulaTermino;
	}

	public void setS2_ClausulaTermino(String s2_ClausulaTermino) {
		this.s2_ClausulaTermino = s2_ClausulaTermino;
	}

	public Date getS2_FechaEfecTermino() {
		return s2_FechaEfecTermino;
	}

	public void setS2_FechaEfecTermino(Date s2_FechaEfecTermino) {
		this.s2_FechaEfecTermino = s2_FechaEfecTermino;
	}

	public String getS2_CompresionCartera() {
		return s2_CompresionCartera;
	}

	public void setS2_CompresionCartera(String s2_CompresionCartera) {
		this.s2_CompresionCartera = s2_CompresionCartera;
	}

	public Long getS2_NumTotIntercam() {
		return s2_NumTotIntercam;
	}

	public void setS2_NumTotIntercam(Long s2_NumTotIntercam) {
		this.s2_NumTotIntercam = s2_NumTotIntercam;
	}

	public Long getS2_NumTotPagos() {
		return s2_NumTotPagos;
	}

	public void setS2_NumTotPagos(Long s2_NumTotPagos) {
		this.s2_NumTotPagos = s2_NumTotPagos;
	}

	public String getS3_XR_OpInstruSub() {
		return s3_XR_OpInstruSub;
	}

	public void setS3_XR_OpInstruSub(String s3_XR_OpInstruSub) {
		this.s3_XR_OpInstruSub = s3_XR_OpInstruSub;
	}

	public String getS3_MonedaComprada() {
		return s3_MonedaComprada;
	}

	public void setS3_MonedaComprada(String s3_MonedaComprada) {
		this.s3_MonedaComprada = s3_MonedaComprada;
	}

	public double getS3_MontoComprada() {
		return s3_MontoComprada;
	}

	public void setS3_MontoComprada(double s3_MontoComprada) {
		this.s3_MontoComprada = s3_MontoComprada;
	}

	public String getS3_XR_MonedaVendida() {
		return s3_XR_MonedaVendida;
	}

	public void setS3_XR_MonedaVendida(String s3_XR_MonedaVendida) {
		this.s3_XR_MonedaVendida = s3_XR_MonedaVendida;
	}

	public double getS3_XR_MontoVendida() {
		return s3_XR_MontoVendida;
	}

	public void setS3_XR_MontoVendida(double s3_XR_MontoVendida) {
		this.s3_XR_MontoVendida = s3_XR_MontoVendida;
	}

	public String getS3_XR_TipoTasaEnt() {
		return s3_XR_TipoTasaEnt;
	}

	public void setS3_XR_TipoTasaEnt(String s3_XR_TipoTasaEnt) {
		this.s3_XR_TipoTasaEnt = s3_XR_TipoTasaEnt;
	}
	
	public String getS3_XR_TipoTasaRec() {
		return s3_XR_TipoTasaRec;
	}

	public void setS3_XR_TipoTasaRec(String s3_XR_TipoTasaRec) {
		this.s3_XR_TipoTasaRec = s3_XR_TipoTasaRec;
	}
	
	public double getS3_R_TasaFija() {
		return s3_R_TasaFija;
	}

	public void setS3_R_TasaFija(double s3_R_TasaFija) {
		this.s3_R_TasaFija = s3_R_TasaFija;
	}

	public double getS3_R_Spread() {
		return s3_R_Spread;
	}

	public void setS3_R_Spread(double s3_R_Spread) {
		this.s3_R_Spread = s3_R_Spread;
	}

	public String getS3_I_TipoActivoSub() {
		return s3_I_TipoActivoSub;
	}

	public void setS3_I_TipoActivoSub(String s3_I_TipoActivoSub) {
		this.s3_I_TipoActivoSub = s3_I_TipoActivoSub;
	}

	public String getS3_I_IdActivoSuby() {
		return s3_I_IdActivoSuby;
	}

	public void setS3_I_IdActivoSuby(String s3_I_IdActivoSuby) {
		this.s3_I_IdActivoSuby = s3_I_IdActivoSuby;
	}

	public double getS3_I_TasaPactada() {
		return s3_I_TasaPactada;
	}

	public void setS3_I_TasaPactada(double s3_I_TasaPactada) {
		this.s3_I_TasaPactada = s3_I_TasaPactada;
	}

	public Date getS3_XR_FechaFixing2() {
		return s3_XR_FechaFixing2;
	}

	public void setS3_XR_FechaFixing2(Date s3_XR_FechaFixing2) {
		this.s3_XR_FechaFixing2 = s3_XR_FechaFixing2;
	}

	public String getS3_XR_ConvTipoCambio() {
		return s3_XR_ConvTipoCambio;
	}

	public void setS3_XR_ConvTipoCambio(String s3_XR_ConvTipoCambio) {
		this.s3_XR_ConvTipoCambio = s3_XR_ConvTipoCambio;
	}

	public double getS3_PrecioFuturo() {
		return s3_PrecioFuturo;
	}

	public void setS3_PrecioFuturo(double s3_PrecioFuturo) {
		this.s3_PrecioFuturo = s3_PrecioFuturo;
	}

	public double getS3_XR_PuntosForward() {
		return s3_XR_PuntosForward;
	}

	public void setS3_XR_PuntosForward(double s3_XR_PuntosForward) {
		this.s3_XR_PuntosForward = s3_XR_PuntosForward;
	}

	public double getS3_XR_PrecioEntrada() {
		return s3_XR_PrecioEntrada;
	}

	public void setS3_XR_PrecioEntrada(double s3_XR_PrecioEntrada) {
		this.s3_XR_PrecioEntrada = s3_XR_PrecioEntrada;
	}

	public double getS3_XR_PrecioSalida() {
		return s3_XR_PrecioSalida;
	}

	public void setS3_XR_PrecioSalida(double s3_XR_PrecioSalida) {
		this.s3_XR_PrecioSalida = s3_XR_PrecioSalida;
	}

	public String getS3_I_PosicionOpe() {
		return s3_I_PosicionOpe;
	}

	public void setS3_I_PosicionOpe(String s3_I_PosicionOpe) {
		this.s3_I_PosicionOpe = s3_I_PosicionOpe;
	}

	public String getS8_MtodoValoriza() {
		return s8_MtodoValoriza;
	}

	public void setS8_MtodoValoriza(String s8_MtodoValoriza) {
		this.s8_MtodoValoriza = s8_MtodoValoriza;
	}

	public String getS8_MonedaValoriza() {
		return s8_MonedaValoriza;
	}

	public void setS8_MonedaValoriza(String s8_MonedaValoriza) {
		this.s8_MonedaValoriza = s8_MonedaValoriza;
	}

	public double getS8_ValorMercado() {
		return s8_ValorMercado;
	}

	public void setS8_ValorMercado(double s8_ValorMercado) {
		this.s8_ValorMercado = s8_ValorMercado;
	}

	public double getS8_ValorMercadoActivo() {
		return s8_ValorMercadoActivo;
	}

	public void setS8_ValorMercadoActivo(double s8_ValorMercadoActivo) {
		this.s8_ValorMercadoActivo = s8_ValorMercadoActivo;
	}

	public double getS8_ValorMercadoPasivo() {
		return s8_ValorMercadoPasivo;
	}

	public void setS8_ValorMercadoPasivo(double s8_ValorMercadoPasivo) {
		this.s8_ValorMercadoPasivo = s8_ValorMercadoPasivo;
	}
	
	public double getS8_OpVolatilidad() {
		return s8_OpVolatilidad;
	}

	public void setS8_OpVolatilidad(double s8_OpVolatilidad) {
		this.s8_OpVolatilidad = s8_OpVolatilidad;
	}

	public double getS8_OpDelta() {
		return s8_OpDelta;
	}

	public void setS8_OpDelta(double s8_OpDelta) {
		this.s8_OpDelta = s8_OpDelta;
	}

	public double getS8_OpGamma() {
		return s8_OpGamma;
	}

	public void setS8_OpGamma(double s8_OpGamma) {
		this.s8_OpGamma = s8_OpGamma;
	}

	public double getS8_OpVega() {
		return s8_OpVega;
	}

	public void setS8_OpVega(double s8_OpVega) {
		this.s8_OpVega = s8_OpVega;
	}

	public double getS8_AjusteRiesgo() {
		return s8_AjusteRiesgo;
	}

	public void setS8_AjusteRiesgo(double s8_AjusteRiesgo) {
		this.s8_AjusteRiesgo = s8_AjusteRiesgo;
	}

	public double getS8_AjusteBID() {
		return s8_AjusteBID;
	}

	public void setS8_AjusteBID(double s8_AjusteBID) {
		this.s8_AjusteBID = s8_AjusteBID;
	}

	public double getS8_OtrosAjustes() {
		return s8_OtrosAjustes;
	}

	public void setS8_OtrosAjustes(double s8_OtrosAjustes) {
		this.s8_OtrosAjustes = s8_OtrosAjustes;
	}

	public Long getkS4_XR_NroFlu() {
		return kS4_XR_NroFlu;
	}

	public void setkS4_XR_NroFlu(Long kS4_XR_NroFlu) {
		this.kS4_XR_NroFlu = kS4_XR_NroFlu;
	}

	public String getS4_XR_TipoFlujo() {
		return s4_XR_TipoFlujo;
	}

	public void setS4_XR_TipoFlujo(String s4_XR_TipoFlujo) {
		this.s4_XR_TipoFlujo = s4_XR_TipoFlujo;
	}

	public double getS4_XR_FacTasa() {
		return s4_XR_FacTasa;
	}

	public void setS4_XR_FacTasa(double s4_XR_FacTasa) {
		this.s4_XR_FacTasa = s4_XR_FacTasa;
	}

	public double getS4_XR_SpreadTasa() {
		return s4_XR_SpreadTasa;
	}

	public void setS4_XR_SpreadTasa(double s4_XR_SpreadTasa) {
		this.s4_XR_SpreadTasa = s4_XR_SpreadTasa;
	}

	public double getS4_XR_TasaFija() {
		return s4_XR_TasaFija;
	}

	public void setS4_XR_TasaFija(double s4_XR_TasaFija) {
		this.s4_XR_TasaFija = s4_XR_TasaFija;
	}

	public Date getS4_XR_FecFixTasa() {
		return s4_XR_FecFixTasa;
	}

	public void setS4_XR_FecFixTasa(Date s4_XR_FecFixTasa) {
		this.s4_XR_FecFixTasa = s4_XR_FecFixTasa;
	}

	public Date getS4_XR_FecIni() {
		return s4_XR_FecIni;
	}

	public void setS4_XR_FecIni(Date s4_XR_FecIni) {
		this.s4_XR_FecIni = s4_XR_FecIni;
	}

	public Date getS4_XR_FecTer() {
		return s4_XR_FecTer;
	}

	public void setS4_XR_FecTer(Date s4_XR_FecTer) {
		this.s4_XR_FecTer = s4_XR_FecTer;
	}

	public double getS4_XR_MontoNoc() {
		return s4_XR_MontoNoc;
	}

	public void setS4_XR_MontoNoc(double s4_XR_MontoNoc) {
		this.s4_XR_MontoNoc = s4_XR_MontoNoc;
	}

	public double getS4_XR_MontoInt() {
		return s4_XR_MontoInt;
	}

	public void setS4_XR_MontoInt(double s4_XR_MontoInt) {
		this.s4_XR_MontoInt = s4_XR_MontoInt;
	}

	public String getS4_X_MonMonto() {
		return s4_X_MonMonto;
	}

	public void setS4_X_MonMonto(String s4_X_MonMonto) {
		this.s4_X_MonMonto = s4_X_MonMonto;
	}

	public double getkS5_CorrPago() {
		return kS5_CorrPago;
	}

	public void setkS5_CorrPago(double kS5_CorrPago) {
		this.kS5_CorrPago = kS5_CorrPago;
	}

	public String getkS5_IdPago() {
		return kS5_IdPago;
	}

	public void setkS5_IdPago(String kS5_IdPago) {
		this.kS5_IdPago = kS5_IdPago;
	}

	public Date getS5_FecPago() {
		return s5_FecPago;
	}

	public void setS5_FecPago(Date s5_FecPago) {
		this.s5_FecPago = s5_FecPago;
	}

	public double getS5_MontoPago() {
		return s5_MontoPago;
	}

	public void setS5_MontoPago(double s5_MontoPago) {
		this.s5_MontoPago = s5_MontoPago;
	}

	public String getS5_TipoPago() {
		return s5_TipoPago;
	}

	public void setS5_TipoPago(String s5_TipoPago) {
		this.s5_TipoPago = s5_TipoPago;
	}
	
	public void setS5_CodMoneda(String S5_CodMoneda) {
		this.S5_CodMoneda = S5_CodMoneda;
	}
	
	public String getS5_CodMoneda() {
		return S5_CodMoneda;
	}

	public String getS6_TipoGar() {
		return s6_TipoGar;
	}

	public void setS6_TipoGar(String s6_TipoGar) {
		this.s6_TipoGar = s6_TipoGar;
	}

	public String getS6_MonVal() {
		return s6_MonVal;
	}

	public void setS6_MonVal(String s6_MonVal) {
		this.s6_MonVal = s6_MonVal;
	}

	public String getkS6_IdGar() {
		return kS6_IdGar;
	}

	public void setkS6_IdGar(String kS6_IdGar) {
		this.kS6_IdGar = kS6_IdGar;
	}

	public double getS6_MontoUmb() {
		return s6_MontoUmb;
	}

	public void setS6_MontoUmb(double s6_MontoUmb) {
		this.s6_MontoUmb = s6_MontoUmb;
	}

	public double getS6_ValGtia() {
		return s6_ValGtia;
	}

	public void setS6_ValGtia(double s6_ValGtia) {
		this.s6_ValGtia = s6_ValGtia;
	}

	public String getS7_TipoGar() {
		return s7_TipoGar;
	}

	public void setS7_TipoGar(String s7_TipoGar) {
		this.s7_TipoGar = s7_TipoGar;
	}

	public String getkS7_IdGar() {
		return kS7_IdGar;
	}

	public void setkS7_IdGar(String kS7_IdGar) {
		this.kS7_IdGar = kS7_IdGar;
	}

	public String getS7_TipoActGar() {
		return s7_TipoActGar;
	}

	public void setS7_TipoActGar(String s7_TipoActGar) {
		this.s7_TipoActGar = s7_TipoActGar;
	}

	public double getS7_PorcGar() {
		return s7_PorcGar;
	}

	public void setS7_PorcGar(double s7_PorcGar) {
		this.s7_PorcGar = s7_PorcGar;
	}

	public String getkProducto() {
		return kProducto;
	}

	public void setkProducto(String kProducto) {
		this.kProducto = kProducto;
	}

	public Date getkPeriodoComercial() {
		return kPeriodoComercial;
	}

	public void setkPeriodoComercial(Date kPeriodoComercial) {
		this.kPeriodoComercial = kPeriodoComercial;
	}

	public RegistroDJ1() {
    }

	public String getRectifica() {
		return Rectifica;
	}
	
	//Obtener valor del registro
    public RegistroDJ1(final Registro registro) {    	
    	this.seccion			 	 =  ((Long) registro.get("Seccion")).longValue();
    	this.fuenteDatos			 =  (String) registro.get("FuenteDatos");
    	this.kRutCtraparte1			 =  (String) registro.get("KRutCtraparte1");
    	this.kIdContrato             =  (String) registro.get("KIdContrato");
    	this.kFechaContrato          =  (Date) registro.get("KFechaContrato");
    	this.horaContrato			 =  (String) registro.get("HoraContrato");
    	this.kIdOpeEstruc            =  (String) registro.get("KIdOpeEstruc");
    	this.s1_NaturaInfo           =  this.seccion == 1 ? (String) registro.get("S1_NaturaInfo") : "";
    	this.s1_EventoReporte        =  this.seccion == 1 ? (String) registro.get("S1_EventoReporte") : "";
    	this.s1_LEICtraparte1        =  this.seccion == 1 ? (String) registro.get("S1_LEICtraparte1") : "";
    	this.s1_RutCtraparte2        =  this.seccion == 1 ? (String) registro.get("S1_RutCtraparte2") : "";
    	this.s1_LEICtraparte2        =  this.seccion == 1 ? (String) registro.get("S1_LEICtraparte2") : "";
    	this.s1_NomCtraparte2        =  this.seccion == 1 ? (String) registro.get("S1_NomCtraparte2") : "";
    	this.s1_PaisCtraparte2       =  this.seccion == 1 ? (String) registro.get("S1_PaisCtraparte2") : "";
    	this.s1_RutEntidad           =  this.seccion == 1 ? (String) registro.get("S1_RutEntidad") : "";
    	this.s1_LEIEntidad           =  this.seccion == 1 ? (String) registro.get("S1_LEIEntidad") : "";
    	this.s1_RUTLiqCtraparte1     =  this.seccion == 1 ? (String) registro.get("S1_RUTLiqCtraparte1") : "";
    	this.s1_LEILiqCtraparte1     =  this.seccion == 1 ? (String) registro.get("S1_LEILiqCtraparte1") : "";
    	this.s1_RutAgenteCal         =  this.seccion == 1 ? (String) registro.get("S1_RutAgenteCal") : "";
    	this.s1_LEIAgenteCal         =  this.seccion == 1 ? (String) registro.get("S1_LEIAgenteCal") : "";
    	this.s1_NomAgenteCal         =  this.seccion == 1 ? (String) registro.get("S1_NomAgenteCal") : "";
    	this.s1_RutCedente           =  this.seccion == 1 ? (String) registro.get("S1_RutCedente") : "";
    	this.s1_LEICedente           =  this.seccion == 1 ? (String) registro.get("S1_LEICedente") : "";
    	this.s1_PlataformaNego       =  this.seccion == 1 ? (String) registro.get("S1_PlataformaNego") : "";
    	this.s1_RutIntermediario     =  this.seccion == 1 ? (String) registro.get("S1_RutIntermediario") : "";
    	this.s1_LEIIntermediario     =  this.seccion == 1 ? (String) registro.get("S1_LEIIntermediario") : "";
    	this.s2_Instrumento          =  this.seccion == 1 ? (String) registro.get("S2_Instrumento") : "";
    	this.s2_IdContratoAgente     =  this.seccion == 1 ? (String) registro.get("S2_IdContratoAgente") : "";
    	this.s2_OpsClase             =  this.seccion == 1 ? (String) registro.get("S2_OpsClase") : "";
    	this.s2_OpPosicionOp         =  this.seccion == 1 ? (String) registro.get("S2_OpPosicionOp") : "";
    	this.s2_ModalidadLiq         =  this.seccion == 1 ? (String) registro.get("S2_ModalidadLiq") : "";
    	this.s2_MonedaComp           =  this.seccion == 1 ? (String) registro.get("S2_MonedaComp") : "";
    	this.s2_FechaInicio          =  this.seccion == 1 ? (Date) registro.get("S2_FechaInicio") : Fecha.toDate("0001-01-01");
    	this.s2_FechaFixing1         =  this.seccion == 1 ? (Date) registro.get("S2_FechaFixing1") : Fecha.toDate("0001-01-01");
    	this.s2_FechaTermino         =  this.seccion == 1 ? (Date) registro.get("S2_FechaTermino") : Fecha.toDate("0001-01-01");
    	this.s2_FechaPagOpe          =  this.seccion == 1 ? (Date) registro.get("S2_FechaPagOpe") : Fecha.toDate("0001-01-01");
    	this.s2_FechaInicioMod       =  this.seccion == 1 ? (Date) registro.get("S2_FechaInicioMod") : Fecha.toDate("0001-01-01");
    	this.s2_MonedaPrima          =  this.seccion == 1 ? (String) registro.get("S2_MonedaPrima") : "";
    	this.s2_MontoPrima           = 	this.seccion == 1 ? ((Number) registro.get("S2_MontoPrima")).doubleValue() : 0;
    	this.s2_ObjetivoOpe          =  this.seccion == 1 ? (String) registro.get("S2_ObjetivoOpe") : "";
    	this.s2_ConvenioMarco        =  this.seccion == 1 ? (String) registro.get("S2_ConvenioMarco") : "";
    	this.s2_PaisContrato         =  this.seccion == 1 ? (String) registro.get("S2_PaisContrato") : "";
    	this.s2_Garantias            =  this.seccion == 1 ? (String) registro.get("S2_Garantias") : "";
    	this.s2_ClausulaRecou        =  this.seccion == 1 ? (String) registro.get("S2_ClausulaRecou") : "";
    	this.s2_ClausulaTermino      =  this.seccion == 1 ? (String) registro.get("S2_ClausulaTermino") : "";
    	this.s2_FechaEfecTermino     =  this.seccion == 1 ? (Date) registro.get("S2_FechaEfecTermino") : Fecha.toDate("0001-01-01");
    	this.s2_CompresionCartera    =  this.seccion == 1 ? (String) registro.get("S2_CompresionCartera") : "";
    	this.s2_NumTotIntercam       =  this.seccion == 1 ? (Long) registro.get("S2_NumTotIntercam") : 0L;
    	this.s2_NumTotPagos          =  this.seccion == 1 ? (Long) registro.get("S2_NumTotPagos") : 0L;
    	this.s3_XR_OpInstruSub       =  this.seccion == 1 ? (String) registro.get("S3_XR_OpInstruSub") : "";
    	this.s3_MonedaComprada       =  this.seccion == 1 ? (String) registro.get("S3_MonedaComprada") : "";
    	this.s3_MontoComprada        =  this.seccion == 1 ? ((Number) registro.get("S3_MontoComprada")).doubleValue() : 0;
    	this.s3_XR_MonedaVendida     =  this.seccion == 1 ? (String) registro.get("S3_XR_MonedaVendida") : "";
    	this.s3_XR_MontoVendida      =  this.seccion == 1 ? ((Number) registro.get("S3_XR_MontoVendida")).doubleValue() : 0;
    	this.s3_XR_TipoTasaEnt     	 =  this.seccion == 1 ? (String) registro.get("S3_XR_TipoTasaEnt") : "";
    	this.s3_XR_TipoTasaRec     	 =  this.seccion == 1 ? (String) registro.get("S3_XR_TipoTasaRec") : "";
    	this.s3_R_TasaFija      	 =  this.seccion == 1 ? ((Number) registro.get("S3_R_TasaFija")).doubleValue() : 0;
    	this.s3_R_Spread      		 =  this.seccion == 1 ? ((Number) registro.get("S3_R_Spread")).doubleValue() : 0;
    	this.s3_I_TipoActivoSub      =  this.seccion == 1 ? (String) registro.get("S3_I_TipoActivoSub") : "";
    	this.s3_I_IdActivoSuby       =  this.seccion == 1 ? (String) registro.get("S3_I_IdActivoSuby") : "";
    	this.s3_I_TasaPactada        =  this.seccion == 1 ? ((Number) registro.get("S3_I_TasaPactada")).doubleValue() : 0;
    	this.s3_XR_FechaFixing2      =  this.seccion == 1 ? (Date) registro.get("S3_XR_FechaFixing2") : Fecha.toDate("0001-01-01");
    	this.s3_XR_ConvTipoCambio    =  this.seccion == 1 ? (String) registro.get("S3_XR_ConvTipoCambio") : "";
    	this.s3_PrecioFuturo         =  this.seccion == 1 ? ((Number) registro.get("S3_PrecioFuturo")).doubleValue() : 0;
    	this.s3_XR_PuntosForward     =  this.seccion == 1 ? ((Number) registro.get("S3_XR_PuntosForward")).doubleValue() : 0;
    	this.s3_XR_PrecioEntrada     =  this.seccion == 1 ? ((Number) registro.get("S3_XR_PrecioEntrada")).doubleValue() : 0;
    	this.s3_XR_PrecioSalida      =  this.seccion == 1 ? ((Number) registro.get("S3_XR_PrecioSalida")).doubleValue() : 0;
    	this.s3_I_PosicionOpe        =  this.seccion == 1 ? (String) registro.get("S3_I_PosicionOpe") : "";
    	this.s8_MtodoValoriza        =  this.seccion == 1 ? (String) registro.get("S8_MtodoValoriza") : "";
    	this.s8_MonedaValoriza       =  this.seccion == 1 ? (String) registro.get("S8_MonedaValoriza") : "";
    	this.s8_ValorMercado         =  this.seccion == 1 ? ((Number) registro.get("S8_ValorMercado")).doubleValue() : 0;
    	this.s8_OpVolatilidad        =  this.seccion == 1 ? ((Number) registro.get("S8_OpVolatilidad")).doubleValue() : 0;
    	this.s8_OpDelta              =  this.seccion == 1 ? ((Number) registro.get("S8_OpDelta")).doubleValue() : 0;
    	this.s8_OpGamma              =  this.seccion == 1 ? ((Number) registro.get("S8_OpGamma")).doubleValue() : 0;
    	this.s8_OpVega               =  this.seccion == 1 ? ((Number) registro.get("S8_OpVega")).doubleValue() : 0;
    	this.s8_AjusteRiesgo         =  this.seccion == 1 ? ((Number) registro.get("S8_AjusteRiesgo")).doubleValue() : 0;
    	this.s8_AjusteBID            =  this.seccion == 1 ? ((Number) registro.get("S8_AjusteBID")).doubleValue() : 0;
    	this.s8_OtrosAjustes         =  this.seccion == 1 ? ((Number) registro.get("S8_OtrosAjustes")).doubleValue() : 0;
    	this.s8_ValorMercadoActivo   =  this.seccion == 1 ? ((Number) registro.get("S8_ValorMercadoActivo")).doubleValue() : 0;
    	this.s8_ValorMercadoPasivo   =  this.seccion == 1 ? ((Number) registro.get("S8_ValorMercadoPasivo")).doubleValue() : 0;
    	this.kS4_XR_NroFlu			 =  this.seccion == 4 ? (Long) registro.get("KS4_XR_NroFlu") : 0L;
    	this.s4_XR_TipoFlujo 		 =  this.seccion == 4 ? (String) registro.get("S4_XR_TipoFlujo") : "";
    	this.s4_XR_FacTasa 			 =  this.seccion == 4 ? ((Number) registro.get("S4_XR_FacTasa")).doubleValue() : 0;
    	this.s4_XR_SpreadTasa 		 =  this.seccion == 4 ? ((Number) registro.get("S4_XR_SpreadTasa")).doubleValue() : 0;
    	this.s4_XR_TasaFija 		 =  this.seccion == 4 ? ((Number) registro.get("S4_XR_TasaFija")).doubleValue() : 0;
    	this.s4_XR_FecFixTasa		 =  this.seccion == 4 ? (Date) registro.get("S4_XR_FecFixTasa") : Fecha.toDate("0001-01-01");
    	this.s4_XR_FecIni			 =  this.seccion == 4 ? (Date) registro.get("S4_XR_FecIni") : Fecha.toDate("0001-01-01");
    	this.s4_XR_FecTer			 =  this.seccion == 4 ? (Date) registro.get("S4_XR_FecTer") : Fecha.toDate("0001-01-01");
    	this.s4_XR_MontoNoc			 =  this.seccion == 4 ? ((Number) registro.get("S4_XR_MontoNoc")).doubleValue() : 0;
    	this.s4_XR_MontoInt			 =  this.seccion == 4 ? ((Number) registro.get("S4_XR_MontoInt")).doubleValue() : 0;
    	this.s4_X_MonMonto			 =  this.seccion == 4 ? (String) registro.get("S4_X_MonMonto") : "";
    	this.kS5_CorrPago 			 =  this.seccion == 5 ? ((Number) registro.get("KS5_CorrPago")).doubleValue() : 0;
    	this.kS5_IdPago				 =  this.seccion == 5 ? (String) registro.get("KS5_IdPago") : "";
    	this.s5_FecPago				 =  this.seccion == 5 ? (Date) registro.get("S5_FecPago") : Fecha.toDate("0001-01-01");
    	this.s5_MontoPago			 =  this.seccion == 5 ? ((Number) registro.get("S5_MontoPago")).doubleValue() : 0;
    	this.s5_TipoPago			 =  this.seccion == 5 ? (String) registro.get("S5_TipoPago") : "";
    	this.S5_CodMoneda			 =  this.seccion == 5 ? (String) registro.get("S5_CodMoneda") : "";
    	this.s6_TipoGar				 =  this.seccion == 6 ? (String) registro.get("S6_TipoGar") : "";
    	this.s6_MonVal				 =  this.seccion == 6 ? (String) registro.get("S6_MonVal") : "";
    	this.kS6_IdGar				 =  this.seccion == 6 ? (String) registro.get("KS6_IdGar") : "";
    	this.s6_MontoUmb			 =  this.seccion == 6 ? ((Number) registro.get("S6_MontoUmb")).doubleValue() : 0;
    	this.s6_ValGtia				 =  this.seccion == 6 ? ((Number) registro.get("S6_ValGtia")).doubleValue() : 0;
    	this.s7_TipoGar				 =  this.seccion == 7 ? (String) registro.get("S7_TipoGar") : "";
    	this.kS7_IdGar				 =  this.seccion == 7 ? (String) registro.get("KS7_IdGar") : "";
    	this.s7_TipoActGar 			 =  this.seccion == 7 ? (String) registro.get("S7_TipoActGar") : "";
    	this.s7_PorcGar				 =  this.seccion == 7 ? ((Number) registro.get("S7_PorcGar")).doubleValue() : 0;
    	this.kProducto               =  (String) registro.get("kProducto");
    	this.kPeriodoComercial 		 =  (Date) registro.get("kPeriodoComercial");
    	this.Rectifica				 =	(String) registro.get("Rectifica");
    }  
}
