package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
import java.math.BigDecimal;

public class RegistroDJ1962 {

   
    private Long ID;
    private Long IdDocumento;
    private String VboUsuario;
    private String Observaciones;
    private String FuenteDatos;

    private String RutAdministradora;
    private String RutFondoB;
    private String RunFondo;
    private String TipoFondo;
    private String TributaSa;
    private BigDecimal DetBenNetoPerPOS_C6;
    private BigDecimal DetBenNetoPerNEG_C7;
    private BigDecimal DetBenNetoUtilPer_C8;
    private BigDecimal DetBenNetoIntPer_C9;
    private BigDecimal DetBenNetoDivPer_C10;
    private BigDecimal DetBenNetoGanCapPer_C11;
    private BigDecimal DetBenNetoPerGastDev_C12;

    private Long Numero;
    private String RutFondoC;
    private String RutCliente;
    private String NombreParticipe;
    private String TaxId;
    private String CodigoPais;
    private String MonedaReajuste;
    private BigDecimal MontoAporteNominal;
    private BigDecimal MontoNominalAportesActualizado;
    private BigDecimal CantidadCuotasParticipe;
    private Long ParticipeCumpleRequisitos;
    private Long CuotasCumplenRequisitos;
    private BigDecimal ValorCuotaPesos;
    private BigDecimal PorcentajeParticipacion31Dic;
    private String TipoContribuyente;

    private Long IdOperacion;
    private String IdEmisor;
    private Date FechaRespaldo;
    private String Fondo;
    private Long IdCliente;
    private Long IdPortafolio;
    private Long FolioCartera;
    private Date FechaOperacion;
    private Long IdCartera;


    public RegistroDJ1962(final Registro registro) {
        this.ID = (Long) registro.get("ID");
        this.IdDocumento = (Long) registro.get("IdDocumento");
        this.VboUsuario = (String) registro.get("VboUsuario");
        this.Observaciones = (String) registro.get("Observaciones");
        this.FuenteDatos = (String) registro.get("FuenteDatos");

        this.RutAdministradora = (String) registro.get("RutAdministradora");
        this.RutFondoB = (String) registro.get("RutFondoB");
        this.RunFondo = (String) registro.get("RunFondo");
        this.TipoFondo = (String) registro.get("TipoFondo");
        this.TributaSa = (String) registro.get("TributaSa");
        this.DetBenNetoPerPOS_C6 = (BigDecimal) registro.get("DetBenNetoPerPOS_C6");
        this.DetBenNetoPerNEG_C7 = (BigDecimal) registro.get("DetBenNetoPerNEG_C7");
        this.DetBenNetoUtilPer_C8 = (BigDecimal) registro.get("DetBenNetoUtilPer_C8");
        this.DetBenNetoIntPer_C9 = (BigDecimal) registro.get("DetBenNetoIntPer_C9");
        this.DetBenNetoDivPer_C10 = (BigDecimal) registro.get("DetBenNetoDivPer_C10");
        this.DetBenNetoGanCapPer_C11 = (BigDecimal) registro.get("DetBenNetoGanCapPer_C11");
        this.DetBenNetoPerGastDev_C12 = (BigDecimal) registro.get("DetBenNetoPerGastDev_C12");

        this.Numero = (Long) registro.get("Numero");
        this.RutFondoC = (String) registro.get("RutFondoC");
        this.RutCliente = (String) registro.get("RutCliente");
        this.NombreParticipe = (String) registro.get("NombreParticipe");
        this.TaxId = (String) registro.get("TaxId");
        this.CodigoPais = (String) registro.get("CodigoPais");
        this.MonedaReajuste = (String) registro.get("MonedaReajuste");
        this.MontoAporteNominal = (BigDecimal) registro.get("MontoAporteNominal");
        this.MontoNominalAportesActualizado = (BigDecimal) registro.get("MontoNominalAportesActualizado");
        this.CantidadCuotasParticipe = (BigDecimal) registro.get("CantidadCuotasParticipe");
        this.ParticipeCumpleRequisitos = (Long) registro.get("ParticipeCumpleRequisitos");
        this.CuotasCumplenRequisitos = (Long) registro.get("CuotasCumplenRequisitos");
        this.ValorCuotaPesos = (BigDecimal) registro.get("ValorCuotaPesos");
        this.PorcentajeParticipacion31Dic = (BigDecimal) registro.get("PorcentajeParticipacion31Dic");
        this.TipoContribuyente = (String) registro.get("TipoContribuyente");

        this.IdOperacion = (Long) registro.get("IdOperacion");
        this.IdEmisor = (String) registro.get("IdEmisor");
        this.FechaRespaldo = (Date) registro.get("FechaRespaldo");
        this.Fondo = (String) registro.get("Fondo");
        this.IdCliente = (Long) registro.get("IdCliente");
        this.IdPortafolio = (Long) registro.get("IdPortafolio");
        this.FolioCartera = (Long) registro.get("FolioCartera");
        this.FechaOperacion = (Date) registro.get("FechaOperacion");
        this.IdCartera = (Long) registro.get("IdCartera");
    }

    public Long getID(){
	return this.ID;
    }

    public void setID(Long ID){
        this.ID = ID;
    }

    public Long getIdDocumento(){
        return this.IdDocumento;
    }

    public void setIdDocumento(Long IdDocumento){
        this.IdDocumento = IdDocumento;
    }

    public String getVboUsuario(){
        return this.VboUsuario;
    }

    public void setVboUsuario(String VboUsuario){
        this.VboUsuario = VboUsuario;
    }

    public String getObservaciones(){
        return this.Observaciones;
    }

    public void setObservaciones(String Observaciones){
        this.Observaciones = Observaciones;
    }

    public String getFuenteDatos(){
        return this.FuenteDatos;
    }

    public void setFuenteDatos(String FuenteDatos){
        this.FuenteDatos = FuenteDatos;
    }

    public String getRutAdministradora(){
        return this.RutAdministradora;
    }

    public void setRutAdministradora(String RutAdministradora){
        this.RutAdministradora = RutAdministradora;
    }

    public String getRutFondoB(){
        return this.RutFondoB;
    }

    public void setRutFondoB(String RutFondoB){
        this.RutFondoB = RutFondoB;
    }

    public String getRunFondo(){
        return this.RunFondo;
    }

    public void setRunFondo(String RunFondo) {
        this.RunFondo = RunFondo;
    }


    public String getTipoFondo(){
        return this.TipoFondo;
    }

    public void setTipoFondo(String TipoFondo){
        this.TipoFondo = TipoFondo;
    }

    public String getTributaSa(){
        return this.TributaSa;
    }

    public void setTributaSa(String TributaSa){
        this.TributaSa = TributaSa;
    }

    public BigDecimal getDetBenNetoPerPOS_C6(){
        return this.DetBenNetoPerPOS_C6;
    }

    public void setDetBenNetoPerPOS_C6(BigDecimal DetBenNetoPerPOS_C6){
        this.DetBenNetoPerPOS_C6 = DetBenNetoPerPOS_C6;
    }

    public BigDecimal getDetBenNetoPerNEG_C7(){
        return this.DetBenNetoPerNEG_C7;
    }

    public void setDetBenNetoPerNEG_C7(BigDecimal DetBenNetoPerNEG_C7){
        this.DetBenNetoPerNEG_C7 = DetBenNetoPerNEG_C7;
    }

    public BigDecimal getDetBenNetoUtilPer_C8(){
        return this.DetBenNetoUtilPer_C8;
    }

    public void setDetBenNetoUtilPer_C8(BigDecimal DetBenNetoUtilPer_C8){
        this.DetBenNetoUtilPer_C8 = DetBenNetoUtilPer_C8;
    }

    public BigDecimal getDetBenNetoIntPer_C9(){
        return this.DetBenNetoIntPer_C9;
    }

    public void setDetBenNetoIntPer_C9(BigDecimal DetBenNetoIntPer_C9){
        this.DetBenNetoIntPer_C9 = DetBenNetoIntPer_C9;
    }

    public BigDecimal getDetBenNetoDivPer_C10(){
        return this.DetBenNetoDivPer_C10;
    }

    public void setDetBenNetoDivPer_C10(BigDecimal DetBenNetoDivPer_C10){
        this.DetBenNetoDivPer_C10 = DetBenNetoDivPer_C10;
    }

    public BigDecimal getDetBenNetoGanCapPer_C11(){
        return this.DetBenNetoGanCapPer_C11;
    }

    public void setDetBenNetoGanCapPer_C11(BigDecimal DetBenNetoGanCapPer_C11){
        this.DetBenNetoGanCapPer_C11 = DetBenNetoGanCapPer_C11;
    }

    public BigDecimal getDetBenNetoPerGastDev_C12(){
        return this.DetBenNetoPerGastDev_C12;
    }

    public void setDetBenNetoPerGastDev_C12(BigDecimal DetBenNetoPerGastDev_C12){
        this.DetBenNetoPerGastDev_C12 = DetBenNetoPerGastDev_C12;
    }

    public Long getNumero(){
        return this.Numero;
    }

    public void setNumero(Long Numero){
        this.Numero = Numero;
    }

    public String getRutFondoC(){
        return this.RutFondoC;
    }

    public void setRutFondoC(String RutFondoC){
        this.RutFondoC = RutFondoC;
    }

    public String getRutCliente(){
        return this.RutCliente;
    }

    public void setRutCliente(String RutCliente){
        this.RutCliente = RutCliente;
    }

    public String getNombreParticipe(){
        return this.NombreParticipe;
    }

    public void setNombreParticipe(String NombreParticipe){
        this.NombreParticipe = NombreParticipe;
    }

    public String getTaxId(){
        return this.TaxId;
    }

    public void setTaxId(String TaxId){
        this.TaxId = TaxId;
    }

    public String getCodigoPais(){
        return this.CodigoPais;
    }

    public void setCodigoPais(String CodigoPais){
        this.CodigoPais = CodigoPais;
    }

    public String getMonedaReajuste(){
        return this.MonedaReajuste;
    }

    public void setMonedaReajuste(String MonedaReajuste){
        this.MonedaReajuste = MonedaReajuste;
    }

    public BigDecimal getMontoAporteNominal(){
        return this.MontoAporteNominal;
    }

    public void setMontoAporteNominal(BigDecimal MontoAporteNominal){
        this.MontoAporteNominal = MontoAporteNominal;
    }

    public BigDecimal getMontoNominalAportesActualizado(){
        return this.MontoNominalAportesActualizado;
    }

    public void setMontoNominalAportesActualizado(BigDecimal MontoNominalAportesActualizado){
        this.MontoNominalAportesActualizado = MontoNominalAportesActualizado;
    }

    public BigDecimal getCantidadCuotasParticipe(){
        return this.CantidadCuotasParticipe;
    }

    public void setCantidadCuotasParticipe(BigDecimal CantidadCuotasParticipe){
        this.CantidadCuotasParticipe = CantidadCuotasParticipe;
    }

    public Long getParticipeCumpleRequisitos(){
        return this.ParticipeCumpleRequisitos;
    }

    public void setParticipeCumpleRequisitos(Long ParticipeCumpleRequisitos){
        this.ParticipeCumpleRequisitos = ParticipeCumpleRequisitos;
    }

    public Long getCuotasCumplenRequisitos(){
        return this.CuotasCumplenRequisitos;
    }

    public void setCuotasCumplenRequisitos(Long CuotasCumplenRequisitos){
        this.CuotasCumplenRequisitos = CuotasCumplenRequisitos;
    }

    public BigDecimal getValorCuotaPesos(){
        return this.ValorCuotaPesos;
    }

    public void setValorCuotaPesos(BigDecimal ValorCuotaPesos){
        this.ValorCuotaPesos = ValorCuotaPesos;
    }

    public BigDecimal getPorcentajeParticipacion31Dic(){
        return this.PorcentajeParticipacion31Dic;
    }

    public void setPorcentajeParticipacion31Dic(BigDecimal PorcentajeParticipacion31Dic){
        this.PorcentajeParticipacion31Dic = PorcentajeParticipacion31Dic;
    }

    public String getTipoContribuyente(){
        return this.TipoContribuyente;
    }

    public void setTipoContribuyente(String TipoContribuyente){
        this.TipoContribuyente = TipoContribuyente;
    }

    public Long getIdOperacion(){
        return this.IdOperacion;
    }

    public void setIdOperacion(Long IdOperacion){
        this.IdOperacion = IdOperacion;
    }

    public String getIdEmisor(){
        return this.IdEmisor;
    }

    public void setIdEmisor(String IdEmisor){
        this.IdEmisor = IdEmisor;
    }

    public Date getFechaRespaldo(){
        return this.FechaRespaldo;
    }

    public void setFechaRespaldo(Date FechaRespaldo){
        this.FechaRespaldo = FechaRespaldo;
    }

    public String getFondo(){
        return this.Fondo;
    }

    public void setFondo(String Fondo){
        this.Fondo = Fondo;
    }

    public Long getIdCliente(){
        return this.IdCliente;
    }

    public void setIdCliente(Long IdCliente){
        this.IdCliente = IdCliente;
    }

    public Long getIdPortafolio(){
        return this.IdPortafolio;
    }

    public void setIdPortafolio(Long IdPortafolio){
        this.IdPortafolio = IdPortafolio;
    }

    public Long getFolioCartera(){
        return this.FolioCartera;
    }

    public void setFolioCartera(Long FolioCartera){
        this.FolioCartera = FolioCartera;
    }

    public Long getIdCartera(){
        return this.IdCartera;
    }

    public void setIdCartera(Long IdCartera){
        this.IdCartera = IdCartera;
    }

    public Date getFechaOperacion(){
        return this.FechaOperacion;
    }

    public void setFechaOperacion(Date FechaOperacion){
        this.FechaOperacion = FechaOperacion;
    }

    public RegistroDJ1962() {
    }


}