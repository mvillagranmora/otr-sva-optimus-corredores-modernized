package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1949 {

    /**
     * Declaracion de variables para la DJ
     */
	/*private Date fechaOperacion;
	private long numeroCertificado;
	private String RutCliente;
    private String RutInstitucion;
    */
	private Date FecMovto; //Fecha del dividendo distribuido
	private String RutSociedad;
	private String TipoSociedad;
	private String RutCliente; //RUT del beneficiario del los dividendos distribuidos
	
	private double CantCuotas3112; //Cantidad de acciones al 31/12
    private double MtoDivReaAfecGCCCIDPC_C6;
    private double MtoDivReaAfecGCCCIDPCAL311216_C7;
    private double MtoDivReaAfecGCCDIDPCVol_C8;
    private double MtoDivReaAfecGCSinDer_C9;
    private double MtoDivReaREXTribCumpRentasRap_C10;
    private double MtoDivReaREXTribCumpOtrasRentas_C11;
    private double MtoDivReaREXTribCumpExcDist_C12;
    private double MtoDivReaREXTribCumpUtiISFUT_C13;
    private double MtoDivReaREXTribCumpRentasISFUT_C14;
    private double MtoDivReaREXRentExIGCAfecIAart11_C15;
    private double MtoDivReaREXRentExIGCIA_C16;
    private double MtoDivReaREXNoConstRenta_C17;
    private double CIGCDesde01012017RentAfecNoRestSDD_C18;
    private double CIGCDesde01012017RentAfecNoRestCDD_C19;
    private double CIGCDesde01012017RentAfecNoRestSDD_C20;
    private double CIGCDesde01012017RentAfecNoRestCDD_C21;
    private double CIGCDesde01012017RentAfecRestSDD_C22;
    private double CIGCDesde01012017RentAfecRestCDD_C23;
    private double CIGCDesde01012017RentExenRestSDD_C24;
    private double CIGCDesde01012017RentExenRestCDD_C25;
    private double CIGCDesde01012017CredIPE_C26;
    private double CIGCHasta31122016RentAfecSDD_C27;
    private double CIGCHasta31122016RentAfecCDD_C28;
    private double CIGCHasta31122016RentExenSDD_C29;
    private double CIGCHasta31122016RentExenCDD_C30;
    private double CIGCHasta31122016CredIPE_C31;
    private double CIGCImptoTasaAdicional_C32;
    private double DevCapArt17N7LIR_C33;
    private long NumCertificado_C34;
    /**
     * Declaracion de sumatorias para la DJ
     */
    private double sumaCantCuotas3112;
    private double sumaMtoDivReaAfecGCCCIDPC_C6;
    private double sumaMtoDivReaAfecGCCCIDPCAL311216_C7;
    private double sumaMtoDivReaAfecGCCDIDPCVol_C8;
    private double sumaMtoDivReaAfecGCSinDer_C9;
    private double sumaMtoDivReaREXTribCumpRentasRap_C10;
    private double sumaMtoDivReaREXTribCumpOtrasRentas_C11;
    private double sumaMtoDivReaREXTribCumpExcDist_C12;
    private double sumaMtoDivReaREXTribCumpUtiISFUT_C13;
    private double sumaMtoDivReaREXTribCumpRentasISFUT_C14;
    private double sumaMtoDivReaREXRentExIGCAfecIAart11_C15;
    private double sumaMtoDivReaREXRentExIGCIA_C16;
    private double sumaMtoDivReaREXNoConstRenta_C17;
    private double sumaCIGCDesde01012017RentAfecNoRestSDD_C18;
    private double sumaCIGCDesde01012017RentAfecNoRestCDD_C19;
    private double sumaCIGCDesde01012017RentAfecNoRestSDD_C20;
    private double sumaCIGCDesde01012017RentAfecNoRestCDD_C21;
    private double sumaCIGCDesde01012017RentAfecRestSDD_C22;
    private double sumaCIGCDesde01012017RentAfecRestCDD_C23;
    private double sumaCIGCDesde01012017RentExenRestSDD_C24;
    private double sumaCIGCDesde01012017RentExenRestCDD_C25;
    private double sumaCIGCDesde01012017CredIPE_C26;
    private double sumaCIGCHasta31122016RentAfecSDD_C27;
    private double sumaCIGCHasta31122016RentAfecCDD_C28;
    private double sumaCIGCHasta31122016RentExenSDD_C29;
    private double sumaCIGCHasta31122016RentExenCDD_C30;
    private double sumaCIGCHasta31122016CredIPE_C31;
    private double sumaCIGCImptoTasaAdicional_C32;
    private double sumaDevCapArt17N7LIR_C33;
    
    public RegistroDJ1949(final Registro registro) {
    	
   	 /*
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.numeroCertificado = (Long) registro.get("NumeroCertificado");
   	 this.RutInstitucion= (String) registro.get("RutInstitucion");
   	 this.RutCliente= (String) registro.get("RutCliente");
   	 */
     this.FecMovto = (Date) registro.get("FecMovto");	
     this.RutSociedad= (String) registro.get("RutSociedad");
     this.TipoSociedad= (String) registro.get("TipoSociedad");
     this.RutCliente= (String) registro.get("RutCliente");
     
     this.CantCuotas3112=(double) ((Number) registro.get("CantCuotas3112")).doubleValue();
   	 this.MtoDivReaAfecGCCCIDPC_C6=(double) ((Number) registro.get("MtoDivReaAfecGCCCIDPC_C6")).doubleValue();
   	 this.MtoDivReaAfecGCCCIDPCAL311216_C7=(double) ((Number) registro.get("MtoDivReaAfecGCCCIDPCAL311216_C7")).doubleValue();
   	 this.MtoDivReaAfecGCCDIDPCVol_C8=(double) ((Number) registro.get("MtoDivReaAfecGCCDIDPCVol_C8")).doubleValue();
   	 this.MtoDivReaAfecGCSinDer_C9=(double) ((Number) registro.get("MtoDivReaAfecGCSinDer_C9")).doubleValue();
   	 this.MtoDivReaREXTribCumpRentasRap_C10=(double) ((Number) registro.get("MtoDivReaREXTribCumpRentasRap_C10")).doubleValue();
   	 this.MtoDivReaREXTribCumpOtrasRentas_C11=(double) ((Number) registro.get("MtoDivReaREXTribCumpOtrasRentas_C11")).doubleValue();
   	 this.MtoDivReaREXTribCumpExcDist_C12=(double) ((Number) registro.get("MtoDivReaREXTribCumpExcDist_C12")).doubleValue();
   	 this.MtoDivReaREXTribCumpUtiISFUT_C13=(double) ((Number) registro.get("MtoDivReaREXTribCumpUtiISFUT_C13")).doubleValue();
   	 this.MtoDivReaREXTribCumpRentasISFUT_C14=(double) ((Number) registro.get("MtoDivReaREXTribCumpRentasISFUT_C14")).doubleValue();
   	 this.MtoDivReaREXRentExIGCAfecIAart11_C15=(double) ((Number) registro.get("MtoDivReaREXRentExIGCAfecIAart11_C15")).doubleValue();
   	 this.MtoDivReaREXRentExIGCIA_C16=(double) ((Number) registro.get("MtoDivReaREXRentExIGCIA_C16")).doubleValue();
   	 this.MtoDivReaREXNoConstRenta_C17=(double) ((Number) registro.get("MtoDivReaREXNoConstRenta_C17")).doubleValue();
	 this.CIGCDesde01012017RentAfecNoRestSDD_C18=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestSDD_C18")).doubleValue();
	 this.CIGCDesde01012017RentAfecNoRestCDD_C19=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestCDD_C19")).doubleValue();
	 this.CIGCDesde01012017RentAfecNoRestSDD_C20=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestSDD_C20")).doubleValue();
	 this.CIGCDesde01012017RentAfecNoRestCDD_C21=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestCDD_C21")).doubleValue();
	 this.CIGCDesde01012017RentAfecRestSDD_C22=(double) ((Number) registro.get("CIGCDesde01012017RentAfecRestSDD_C22")).doubleValue();
	 this.CIGCDesde01012017RentAfecRestCDD_C23=(double) ((Number) registro.get("CIGCDesde01012017RentAfecRestCDD_C23")).doubleValue();
	 this.CIGCDesde01012017RentExenRestSDD_C24=(double) ((Number) registro.get("CIGCDesde01012017RentExenRestSDD_C24")).doubleValue();
	 this.CIGCDesde01012017RentExenRestCDD_C25=(double) ((Number) registro.get("CIGCDesde01012017RentExenRestCDD_C25")).doubleValue();
	 this.CIGCDesde01012017CredIPE_C26=(double) ((Number) registro.get("CIGCDesde01012017CredIPE_C26")).doubleValue();
	 this.CIGCHasta31122016RentAfecSDD_C27=(double) ((Number) registro.get("CIGCHasta31122016RentAfecSDD_C27")).doubleValue();
	 this.CIGCHasta31122016RentAfecCDD_C28=(double) ((Number) registro.get("CIGCHasta31122016RentAfecCDD_C28")).doubleValue();
	 this.CIGCHasta31122016RentExenSDD_C29=(double) ((Number) registro.get("CIGCHasta31122016RentExenSDD_C29")).doubleValue();
	 this.CIGCHasta31122016RentExenCDD_C30=(double) ((Number) registro.get("CIGCHasta31122016RentExenCDD_C30")).doubleValue();
	 this.CIGCHasta31122016CredIPE_C31=(double) ((Number) registro.get("CIGCHasta31122016CredIPE_C31")).doubleValue();
	 this.CIGCImptoTasaAdicional_C32=(double) ((Number) registro.get("CIGCImptoTasaAdicional_C32")).doubleValue();
	 this.DevCapArt17N7LIR_C33=(double) ((Number) registro.get("DevCapArt17N7LIR_C33")).doubleValue();
	 this.NumCertificado_C34 = (Long) registro.get("NumCertificado_C34");
	 
	 this.sumaCantCuotas3112=(double) ((Number) registro.get("CantCuotas3112")).doubleValue();
	 this.sumaMtoDivReaAfecGCCCIDPC_C6=(double) ((Number) registro.get("MtoDivReaAfecGCCCIDPC_C6")).doubleValue();
   	 this.sumaMtoDivReaAfecGCCCIDPCAL311216_C7=(double) ((Number) registro.get("MtoDivReaAfecGCCCIDPCAL311216_C7")).doubleValue();
   	 this.sumaMtoDivReaAfecGCCDIDPCVol_C8=(double) ((Number) registro.get("MtoDivReaAfecGCCDIDPCVol_C8")).doubleValue();
   	 this.sumaMtoDivReaAfecGCSinDer_C9=(double) ((Number) registro.get("MtoDivReaAfecGCSinDer_C9")).doubleValue();
   	 this.sumaMtoDivReaREXTribCumpRentasRap_C10=(double) ((Number) registro.get("MtoDivReaREXTribCumpRentasRap_C10")).doubleValue();
   	 this.sumaMtoDivReaREXTribCumpOtrasRentas_C11=(double) ((Number) registro.get("MtoDivReaREXTribCumpOtrasRentas_C11")).doubleValue();
   	 this.sumaMtoDivReaREXTribCumpExcDist_C12=(double) ((Number) registro.get("MtoDivReaREXTribCumpExcDist_C12")).doubleValue();
   	 this.sumaMtoDivReaREXTribCumpUtiISFUT_C13=(double) ((Number) registro.get("MtoDivReaREXTribCumpUtiISFUT_C13")).doubleValue();
   	 this.sumaMtoDivReaREXTribCumpRentasISFUT_C14=(double) ((Number) registro.get("MtoDivReaREXTribCumpRentasISFUT_C14")).doubleValue();
   	 this.sumaMtoDivReaREXRentExIGCAfecIAart11_C15=(double) ((Number) registro.get("MtoDivReaREXRentExIGCAfecIAart11_C15")).doubleValue();
   	 this.sumaMtoDivReaREXRentExIGCIA_C16=(double) ((Number) registro.get("MtoDivReaREXRentExIGCIA_C16")).doubleValue();
   	 this.sumaMtoDivReaREXNoConstRenta_C17=(double) ((Number) registro.get("MtoDivReaREXNoConstRenta_C17")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecNoRestSDD_C18=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestSDD_C18")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecNoRestCDD_C19=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestCDD_C19")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecNoRestSDD_C20=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestSDD_C20")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecNoRestCDD_C21=(double) ((Number) registro.get("CIGCDesde01012017RentAfecNoRestCDD_C21")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecRestSDD_C22=(double) ((Number) registro.get("CIGCDesde01012017RentAfecRestSDD_C22")).doubleValue();
	 this.sumaCIGCDesde01012017RentAfecRestCDD_C23=(double) ((Number) registro.get("CIGCDesde01012017RentAfecRestCDD_C23")).doubleValue();
	 this.sumaCIGCDesde01012017RentExenRestSDD_C24=(double) ((Number) registro.get("CIGCDesde01012017RentExenRestSDD_C24")).doubleValue();
	 this.sumaCIGCDesde01012017RentExenRestCDD_C25=(double) ((Number) registro.get("CIGCDesde01012017RentExenRestCDD_C25")).doubleValue();
	 this.sumaCIGCDesde01012017CredIPE_C26=(double) ((Number) registro.get("CIGCDesde01012017CredIPE_C26")).doubleValue();
	 this.sumaCIGCHasta31122016RentAfecSDD_C27=(double) ((Number) registro.get("CIGCHasta31122016RentAfecSDD_C27")).doubleValue();
	 this.sumaCIGCHasta31122016RentAfecCDD_C28=(double) ((Number) registro.get("CIGCHasta31122016RentAfecCDD_C28")).doubleValue();
	 this.sumaCIGCHasta31122016RentExenSDD_C29=(double) ((Number) registro.get("CIGCHasta31122016RentExenSDD_C29")).doubleValue();
	 this.sumaCIGCHasta31122016RentExenCDD_C30=(double) ((Number) registro.get("CIGCHasta31122016RentExenCDD_C30")).doubleValue();
	 this.sumaCIGCHasta31122016CredIPE_C31=(double) ((Number) registro.get("CIGCHasta31122016CredIPE_C31")).doubleValue();
	 this.sumaCIGCImptoTasaAdicional_C32=(double) ((Number) registro.get("CIGCImptoTasaAdicional_C32")).doubleValue();
	 this.sumaDevCapArt17N7LIR_C33=(double) ((Number) registro.get("DevCapArt17N7LIR_C33")).doubleValue(); 
    
    }
    
	/*
	public Date getFechaOperacion() {
        return fechaOperacion;
    }
	
	public long getNumeroCertificado() {
		return numeroCertificado;
	}

	public void setNumeroCertificado(long numeroCertificado) {
		this.numeroCertificado = numeroCertificado;
	}

	public String getRutCliente() {
		return RutInstitucion;
	}
	
	public void setRutCliente(String RutCliente) {
		this.RutCliente=RutCliente;
	}
	
	public String getRutInstitucion() {
		return RutInstitucion;
	}
	
	public void setRutInstitucion(String RutInstitucion) {
		this.RutInstitucion=RutInstitucion;
	}
	*/
    
    public Date getFecMovto() {
        return FecMovto;
    }
    
    public void setFecMovto(Date FecMovto) {
		this.FecMovto=FecMovto; 
	}
    
    public String getRutSociedad() {
		return RutSociedad;
	}
	
	public void setRutSociedad(String RutSociedad) {
		this.RutSociedad=RutSociedad;
	}
	
	public String getTipoSociedad() {
		return TipoSociedad;
	}
	
	public void setTipoSociedad(String TipoSociedad) {
		this.TipoSociedad=TipoSociedad;
	}
    
	public String getRutCliente() {
		return RutCliente;
	}
	
	public void setRutCliente(String RutCliente) {
		this.RutCliente=RutCliente;
	}
	
	public double getCantCuotas3112() {
		return CantCuotas3112;
	}
	
	public void setCantCuotas3112(double CantCuotas3112) {
		this.CantCuotas3112=CantCuotas3112;
	}
	
	public double getMtoDivReaAfecGCCCIDPC_C6() {
		return MtoDivReaAfecGCCCIDPC_C6;
	}
	
	public void setMtoDivReaAfecGCCCIDPC_C6(double MtoDivReaAfecGCCCIDPC_C6) {
		this.MtoDivReaAfecGCCCIDPC_C6=MtoDivReaAfecGCCCIDPC_C6;
	}
	
	public double getMtoDivReaAfecGCCCIDPCAL311216_C7() {
		return MtoDivReaAfecGCCCIDPCAL311216_C7;
	}
	
	public void setMtoDivReaAfecGCCCIDPCAL311216_C7(double MtoDivReaAfecGCCCIDPCAL311216_C7) {
		this.MtoDivReaAfecGCCCIDPCAL311216_C7=MtoDivReaAfecGCCCIDPCAL311216_C7;
	}
	
	public double getMtoDivReaAfecGCCDIDPCVol_C8() {
		return MtoDivReaAfecGCCDIDPCVol_C8;
	}
	
	public void setMtoDivReaAfecGCCDIDPCVol_C8(double MtoDivReaAfecGCCDIDPCVol_C8) {
		this.MtoDivReaAfecGCCDIDPCVol_C8=MtoDivReaAfecGCCDIDPCVol_C8;
	}
	
	public double getMtoDivReaAfecGCSinDer_C9() {
		return MtoDivReaAfecGCSinDer_C9;
	}
	
	public void setMtoDivReaAfecGCSinDer_C9(double MtoDivReaAfecGCSinDer_C9) {
		this.MtoDivReaAfecGCSinDer_C9=MtoDivReaAfecGCSinDer_C9;
	}
	
	public double getMtoDivReaREXTribCumpRentasRap_C10() {
		return MtoDivReaREXTribCumpRentasRap_C10;
	}
	
	public void setMtoDivReaREXTribCumpRentasRap_C10(double MtoDivReaREXTribCumpRentasRap_C10) {
		this.MtoDivReaREXTribCumpRentasRap_C10=MtoDivReaREXTribCumpRentasRap_C10;
	}
	
	public double getMtoDivReaREXTribCumpOtrasRentas_C11() {
		return MtoDivReaREXTribCumpOtrasRentas_C11;
	}
	
	public void setMtoDivReaREXTribCumpOtrasRentas_C11(double MtoDivReaREXTribCumpOtrasRentas_C11) {
		this.MtoDivReaREXTribCumpOtrasRentas_C11=MtoDivReaREXTribCumpOtrasRentas_C11;
	}
	
	public double getMtoDivReaREXTribCumpExcDist_C12() {
		return MtoDivReaREXTribCumpExcDist_C12;
	}
	
	public void setMtoDivReaREXTribCumpExcDist_C12(double MtoDivReaREXTribCumpExcDist_C12) {
		this.MtoDivReaREXTribCumpExcDist_C12=MtoDivReaREXTribCumpExcDist_C12;
	}
	
	public double getMtoDivReaREXTribCumpUtiISFUT_C13() {
		return MtoDivReaREXTribCumpUtiISFUT_C13;
	}
	
	public void setMtoDivReaREXTribCumpUtiISFUT_C13(double MtoDivReaREXTribCumpUtiISFUT_C13) {
		this.MtoDivReaREXTribCumpUtiISFUT_C13=MtoDivReaREXTribCumpUtiISFUT_C13;
	}
	
	public double getMtoDivReaREXTribCumpRentasISFUT_C14() {
		return MtoDivReaREXTribCumpRentasISFUT_C14;
	}
	
	public void setMtoDivReaREXTribCumpRentasISFUT_C14(double MtoDivReaREXTribCumpRentasISFUT_C14) {
		this.MtoDivReaREXTribCumpRentasISFUT_C14=MtoDivReaREXTribCumpRentasISFUT_C14;
	}
	
	public double getMtoDivReaREXRentExIGCAfecIAart11_C15() {
		return MtoDivReaREXRentExIGCAfecIAart11_C15;
	}
	
	public void setMtoDivReaREXRentExIGCAfecIAart11_C15(double MtoDivReaREXRentExIGCAfecIAart11_C15) {
		this.MtoDivReaREXRentExIGCAfecIAart11_C15=MtoDivReaREXRentExIGCAfecIAart11_C15;
	}
	
	public double getMtoDivReaREXRentExIGCIA_C16(){
		return MtoDivReaREXRentExIGCIA_C16;
	}
	
	public void setMtoDivReaREXRentExIGCIA_C16(double MtoDivReaREXRentExIGCIA_C16) {
		this.MtoDivReaREXRentExIGCIA_C16=MtoDivReaREXRentExIGCIA_C16;
	}
	
	public double getMtoDivReaREXNoConstRenta_C17() {
		return MtoDivReaREXNoConstRenta_C17;
	}
	
	public void setMtoDivReaREXNoConstRenta_C17(double MtoDivReaREXNoConstRenta_C17) {
		this.MtoDivReaREXNoConstRenta_C17=MtoDivReaREXNoConstRenta_C17;
	}
	
	public double getCIGCDesde01012017RentAfecNoRestSDD_C18() {
		return CIGCDesde01012017RentAfecNoRestSDD_C18;
	}
	
	public void setCIGCDesde01012017RentAfecNoRestSDD_C18(double CIGCDesde01012017RentAfecNoRestSDD_C18) {
		this.CIGCDesde01012017RentAfecNoRestSDD_C18=CIGCDesde01012017RentAfecNoRestSDD_C18;
	}
	
	public double getCIGCDesde01012017RentAfecNoRestCDD_C19() {
		return CIGCDesde01012017RentAfecNoRestCDD_C19;
	}
	
	public void setCIGCDesde01012017RentAfecNoRestCDD_C19(double CIGCDesde01012017RentAfecNoRestCDD_C19) {
		this.CIGCDesde01012017RentAfecNoRestCDD_C19=CIGCDesde01012017RentAfecNoRestCDD_C19;
	}
	
	public double getCIGCDesde01012017RentAfecNoRestSDD_C20() {
		return CIGCDesde01012017RentAfecNoRestSDD_C20;
	}
	
	public void setCIGCDesde01012017RentAfecNoRestSDD_C20(double CIGCDesde01012017RentAfecNoRestSDD_C20) {
		this.CIGCDesde01012017RentAfecNoRestSDD_C20=CIGCDesde01012017RentAfecNoRestSDD_C20;
	}
	
	public double getCIGCDesde01012017RentAfecNoRestCDD_C21() {
		return CIGCDesde01012017RentAfecNoRestCDD_C21;
	}
	
	public void setCIGCDesde01012017RentAfecNoRestCDD_C21(double CIGCDesde01012017RentAfecNoRestCDD_C21) {
		this.CIGCDesde01012017RentAfecNoRestCDD_C21=CIGCDesde01012017RentAfecNoRestCDD_C21;
	}
	
	public double getCIGCDesde01012017RentAfecRestSDD_C22() {
		return CIGCDesde01012017RentAfecRestSDD_C22;
	}
	
	public void setCIGCDesde01012017RentAfecRestSDD_C22(double CIGCDesde01012017RentAfecRestSDD_C22) {
		this.CIGCDesde01012017RentAfecRestSDD_C22=CIGCDesde01012017RentAfecRestSDD_C22;
	}
	
	public double getCIGCDesde01012017RentAfecRestCDD_C23() {
		return CIGCDesde01012017RentAfecRestCDD_C23;
	}
	
	public void setCIGCDesde01012017RentAfecRestCDD_C23(double CIGCDesde01012017RentAfecRestCDD_C23) {
		this.CIGCDesde01012017RentAfecRestCDD_C23=CIGCDesde01012017RentAfecRestCDD_C23;
	}
	
	public double getCIGCDesde01012017RentExenRestSDD_C24() {
		return CIGCDesde01012017RentExenRestSDD_C24;
	}
	
	public void setCIGCDesde01012017RentExenRestSDD_C24(double CIGCDesde01012017RentExenRestSDD_C24) {
		this.CIGCDesde01012017RentExenRestSDD_C24=CIGCDesde01012017RentExenRestSDD_C24;
	}
	
	public double getCIGCDesde01012017RentExenRestCDD_C25() {
		return CIGCDesde01012017RentExenRestCDD_C25;
	}
	
	public void setCIGCDesde01012017RentExenRestCDD_C25(double CIGCDesde01012017RentExenRestCDD_C25) {
		this.CIGCDesde01012017RentExenRestCDD_C25=CIGCDesde01012017RentExenRestCDD_C25;
	}
	
	public double getCIGCDesde01012017CredIPE_C26() {
		return CIGCDesde01012017CredIPE_C26;
	}
	
	public void setCIGCDesde01012017CredIPE_C26(double CIGCDesde01012017CredIPE_C26) {
		this.CIGCDesde01012017CredIPE_C26=CIGCDesde01012017CredIPE_C26;
	}
	
	public double getCIGCHasta31122016RentAfecSDD_C27() {
		return CIGCHasta31122016RentAfecSDD_C27;
	}
	
	public void setCIGCHasta31122016RentAfecSDD_C27(double CIGCHasta31122016RentAfecSDD_C27) {
		this.CIGCHasta31122016RentAfecSDD_C27=CIGCHasta31122016RentAfecSDD_C27;
	}
	
	public double getCIGCHasta31122016RentAfecCDD_C28() {
		return CIGCHasta31122016RentAfecCDD_C28;
	}
	
	public void setCIGCHasta31122016RentAfecCDD_C28(double CIGCHasta31122016RentAfecCDD_C28) {
		this.CIGCHasta31122016RentAfecCDD_C28=CIGCHasta31122016RentAfecCDD_C28;
	}
	
	public double getCIGCHasta31122016RentExenSDD_C29() {
		return CIGCHasta31122016RentExenSDD_C29;
	}
	
	public void setCIGCHasta31122016RentExenSDD_C29(double CIGCHasta31122016RentExenSDD_C29) {
		this.CIGCHasta31122016RentExenSDD_C29=CIGCHasta31122016RentExenSDD_C29;
	}
	
	public double getCIGCHasta31122016RentExenCDD_C30() {
		return CIGCHasta31122016RentExenCDD_C30;
	}
	
	public void setCIGCHasta31122016RentExenCDD_C30(double CIGCHasta31122016RentExenCDD_C30) {
		this.CIGCHasta31122016RentExenCDD_C30=CIGCHasta31122016RentExenCDD_C30;
	}
	
	public double getCIGCHasta31122016CredIPE_C31() {
		return CIGCHasta31122016CredIPE_C31;
	}
	
	public void setCIGCHasta31122016CredIPE_C31(double CIGCHasta31122016CredIPE_C31) {
		this.CIGCHasta31122016CredIPE_C31=CIGCHasta31122016CredIPE_C31;
	}
	
	public double getCIGCImptoTasaAdicional_C32() {
		return CIGCImptoTasaAdicional_C32;
	}
	
	public void setCIGCImptoTasaAdicional_C32(double CIGCImptoTasaAdicional_C32) {
		this.CIGCImptoTasaAdicional_C32=CIGCImptoTasaAdicional_C32;
	}

	public double getDevCapArt17N7LIR_C33() {
		return DevCapArt17N7LIR_C33;
	}
	
	public void setDevCapArt17N7LIR_C33(double DevCapArt17N7LIR_C33) {
		this.DevCapArt17N7LIR_C33=DevCapArt17N7LIR_C33;
	}
	
	public long getNumCertificado_C34() {
		return NumCertificado_C34;
	}

	public void setNumCertificado_C34(long NumCertificado_C34) {
		this.NumCertificado_C34 = NumCertificado_C34;
	}
	
	public double getsumaCantCuotas3112() {
		return sumaCantCuotas3112;
	}
	
	public void setsumaCantCuotas3112(double sumaCantCuotas3112) {
		this.sumaCantCuotas3112=sumaCantCuotas3112;
	}

	public double getsumaMtoDivReaAfecGCCCIDPC_C6() {
		return sumaMtoDivReaAfecGCCCIDPC_C6;
	}
	
	public void setsumaMtoDivReaAfecGCCCIDPC_C6(double sumaMtoDivReaAfecGCCCIDPC_C6) {
		this.sumaMtoDivReaAfecGCCCIDPC_C6=sumaMtoDivReaAfecGCCCIDPC_C6;
	}
	
	public double getsumaMtoDivReaAfecGCCCIDPCAL311216_C7() {
		return sumaMtoDivReaAfecGCCCIDPCAL311216_C7;
	}
	
	public void setsumaMtoDivReaAfecGCCCIDPCAL311216_C7(double sumaMtoDivReaAfecGCCCIDPCAL311216_C7) {
		this.sumaMtoDivReaAfecGCCCIDPCAL311216_C7=sumaMtoDivReaAfecGCCCIDPCAL311216_C7;
	}
	
	public double getsumaMtoDivReaAfecGCCDIDPCVol_C8() {
		return sumaMtoDivReaAfecGCCDIDPCVol_C8;
	}
	
	public void setsumaMtoDivReaAfecGCCDIDPCVol_C8(double sumaMtoDivReaAfecGCCDIDPCVol_C8) {
		this.sumaMtoDivReaAfecGCCDIDPCVol_C8=sumaMtoDivReaAfecGCCDIDPCVol_C8;
	}
	
	public double getsumaMtoDivReaAfecGCSinDer_C9() {
		return sumaMtoDivReaAfecGCSinDer_C9;
	}
	
	public void setsumaMtoDivReaAfecGCSinDer_C9(double sumaMtoDivReaAfecGCSinDer_C9) {
		this.sumaMtoDivReaAfecGCSinDer_C9=sumaMtoDivReaAfecGCSinDer_C9;
	}
	
	public double getsumaMtoDivReaREXTribCumpRentasRap_C10() {
		return sumaMtoDivReaREXTribCumpRentasRap_C10;
	}
	
	public void setsumaMtoDivReaREXTribCumpRentasRap_C10(double sumaMtoDivReaREXTribCumpRentasRap_C10) {
		this.sumaMtoDivReaREXTribCumpRentasRap_C10=sumaMtoDivReaREXTribCumpRentasRap_C10;
	}
	
	public double getsumaMtoDivReaREXTribCumpOtrasRentas_C11() {
		return sumaMtoDivReaREXTribCumpOtrasRentas_C11;
	}
	
	public void setsumaMtoDivReaREXTribCumpOtrasRentas_C11(double sumaMtoDivReaREXTribCumpOtrasRentas_C11) {
		this.sumaMtoDivReaREXTribCumpOtrasRentas_C11=sumaMtoDivReaREXTribCumpOtrasRentas_C11;
	}
	
	public double getsumaMtoDivReaREXTribCumpExcDist_C12() {
		return sumaMtoDivReaREXTribCumpExcDist_C12;
	}
	
	public void setsumaMtoDivReaREXTribCumpExcDist_C12(double sumaMtoDivReaREXTribCumpExcDist_C12) {
		this.sumaMtoDivReaREXTribCumpExcDist_C12=sumaMtoDivReaREXTribCumpExcDist_C12;
	}
	
	public double getsumaMtoDivReaREXTribCumpUtiISFUT_C13() {
		return sumaMtoDivReaREXTribCumpUtiISFUT_C13;
	}
	
	public void setsumaMtoDivReaREXTribCumpUtiISFUT_C13(double sumaMtoDivReaREXTribCumpUtiISFUT_C13) {
		this.sumaMtoDivReaREXTribCumpUtiISFUT_C13=sumaMtoDivReaREXTribCumpUtiISFUT_C13;
	}
	
	public double getsumaMtoDivReaREXTribCumpRentasISFUT_C14() {
		return sumaMtoDivReaREXTribCumpRentasISFUT_C14;
	}
	
	public void setsumaMtoDivReaREXTribCumpRentasISFUT_C14(double sumaMtoDivReaREXTribCumpRentasISFUT_C14) {
		this.sumaMtoDivReaREXTribCumpRentasISFUT_C14=sumaMtoDivReaREXTribCumpRentasISFUT_C14;
	}
	
	public double getsumaMtoDivReaREXRentExIGCAfecIAart11_C15() {
		return sumaMtoDivReaREXRentExIGCAfecIAart11_C15;
	}
	
	public void setsumaMtoDivReaREXRentExIGCAfecIAart11_C15(double sumaMtoDivReaREXRentExIGCAfecIAart11_C15) {
		this.sumaMtoDivReaREXRentExIGCAfecIAart11_C15=sumaMtoDivReaREXRentExIGCAfecIAart11_C15;
	}
	
	public double getsumaMtoDivReaREXRentExIGCIA_C16(){
		return sumaMtoDivReaREXRentExIGCIA_C16;
	}
	
	public void setsumaMtoDivReaREXRentExIGCIA_C16(double sumaMtoDivReaREXRentExIGCIA_C16) {
		this.sumaMtoDivReaREXRentExIGCIA_C16=sumaMtoDivReaREXRentExIGCIA_C16;
	}
	
	public double getsumaMtoDivReaREXNoConstRenta_C17() {
		return sumaMtoDivReaREXNoConstRenta_C17;
	}
	
	public void setsumaMtoDivReaREXNoConstRenta_C17(double sumaMtoDivReaREXNoConstRenta_C17) {
		this.sumaMtoDivReaREXNoConstRenta_C17=sumaMtoDivReaREXNoConstRenta_C17;
	}
	
	public double getsumaCIGCDesde01012017RentAfecNoRestSDD_C18() {
		return sumaCIGCDesde01012017RentAfecNoRestSDD_C18;
	}
	
	public void setsumaCIGCDesde01012017RentAfecNoRestSDD_C18(double sumaCIGCDesde01012017RentAfecNoRestSDD_C18) {
		this.sumaCIGCDesde01012017RentAfecNoRestSDD_C18=sumaCIGCDesde01012017RentAfecNoRestSDD_C18;
	}
	
	public double getsumaCIGCDesde01012017RentAfecNoRestCDD_C19() {
		return sumaCIGCDesde01012017RentAfecNoRestCDD_C19;
	}
	
	public void setsumaCIGCDesde01012017RentAfecNoRestCDD_C19(double sumaCIGCDesde01012017RentAfecNoRestCDD_C19) {
		this.sumaCIGCDesde01012017RentAfecNoRestCDD_C19=sumaCIGCDesde01012017RentAfecNoRestCDD_C19;
	}
	
	public double getsumaCIGCDesde01012017RentAfecNoRestSDD_C20() {
		return sumaCIGCDesde01012017RentAfecNoRestSDD_C20;
	}
	
	public void setsumaCIGCDesde01012017RentAfecNoRestSDD_C20(double sumaCIGCDesde01012017RentAfecNoRestSDD_C20) {
		this.sumaCIGCDesde01012017RentAfecNoRestSDD_C20=sumaCIGCDesde01012017RentAfecNoRestSDD_C20;
	}
	
	public double getsumaCIGCDesde01012017RentAfecNoRestCDD_C21() {
		return sumaCIGCDesde01012017RentAfecNoRestCDD_C21;
	}
	
	public void setsumaCIGCDesde01012017RentAfecNoRestCDD_C21(double sumaCIGCDesde01012017RentAfecNoRestCDD_C21) {
		this.sumaCIGCDesde01012017RentAfecNoRestCDD_C21=sumaCIGCDesde01012017RentAfecNoRestCDD_C21;
	}
	
	public double getsumaCIGCDesde01012017RentAfecRestSDD_C22() {
		return sumaCIGCDesde01012017RentAfecRestSDD_C22;
	}
	
	public void setsumaCIGCDesde01012017RentAfecRestSDD_C22(double sumaCIGCDesde01012017RentAfecRestSDD_C22) {
		this.sumaCIGCDesde01012017RentAfecRestSDD_C22=sumaCIGCDesde01012017RentAfecRestSDD_C22;
	}
	
	public double getsumaCIGCDesde01012017RentAfecRestCDD_C23() {
		return sumaCIGCDesde01012017RentAfecRestCDD_C23;
	}
	
	public void setsumaCIGCDesde01012017RentAfecRestCDD_C23(double sumaCIGCDesde01012017RentAfecRestCDD_C23) {
		this.sumaCIGCDesde01012017RentAfecRestCDD_C23=sumaCIGCDesde01012017RentAfecRestCDD_C23;
	}
	
	public double getsumaCIGCDesde01012017RentExenRestSDD_C24() {
		return sumaCIGCDesde01012017RentExenRestSDD_C24;
	}
	
	public void setsumaCIGCDesde01012017RentExenRestSDD_C24(double sumaCIGCDesde01012017RentExenRestSDD_C24) {
		this.sumaCIGCDesde01012017RentExenRestSDD_C24=sumaCIGCDesde01012017RentExenRestSDD_C24;
	}
	
	public double getsumaCIGCDesde01012017RentExenRestCDD_C25() {
		return sumaCIGCDesde01012017RentExenRestCDD_C25;
	}
	
	public void setsumaCIGCDesde01012017RentExenRestCDD_C25(double sumaCIGCDesde01012017RentExenRestCDD_C25) {
		this.sumaCIGCDesde01012017RentExenRestCDD_C25=sumaCIGCDesde01012017RentExenRestCDD_C25;
	}
	
	public double getsumaCIGCDesde01012017CredIPE_C26() {
		return sumaCIGCDesde01012017CredIPE_C26;
	}
	
	public void setsumaCIGCDesde01012017CredIPE_C26(double sumaCIGCDesde01012017CredIPE_C26) {
		this.sumaCIGCDesde01012017CredIPE_C26=sumaCIGCDesde01012017CredIPE_C26;
	}
	
	public double getsumaCIGCHasta31122016RentAfecSDD_C27() {
		return sumaCIGCHasta31122016RentAfecSDD_C27;
	}
	
	public void setsumaCIGCHasta31122016RentAfecSDD_C27(double sumaCIGCHasta31122016RentAfecSDD_C27) {
		this.sumaCIGCHasta31122016RentAfecSDD_C27=sumaCIGCHasta31122016RentAfecSDD_C27;
	}
	
	public double getsumaCIGCHasta31122016RentAfecCDD_C28() {
		return sumaCIGCHasta31122016RentAfecCDD_C28;
	}
	
	public void setsumaCIGCHasta31122016RentAfecCDD_C28(double sumaCIGCHasta31122016RentAfecCDD_C28) {
		this.sumaCIGCHasta31122016RentAfecCDD_C28=sumaCIGCHasta31122016RentAfecCDD_C28;
	}
	
	public double getsumaCIGCHasta31122016RentExenSDD_C29() {
		return sumaCIGCHasta31122016RentExenSDD_C29;
	}
	
	public void setsumaCIGCHasta31122016RentExenSDD_C29(double sumaCIGCHasta31122016RentExenSDD_C29) {
		this.sumaCIGCHasta31122016RentExenSDD_C29=sumaCIGCHasta31122016RentExenSDD_C29;
	}
	
	public double getsumaCIGCHasta31122016RentExenCDD_C30() {
		return sumaCIGCHasta31122016RentExenCDD_C30;
	}
	
	public void setsumaCIGCHasta31122016RentExenCDD_C30(double sumaCIGCHasta31122016RentExenCDD_C30) {
		this.sumaCIGCHasta31122016RentExenCDD_C30=sumaCIGCHasta31122016RentExenCDD_C30;
	}
	
	public double getsumaCIGCHasta31122016CredIPE_C31() {
		return sumaCIGCHasta31122016CredIPE_C31;
	}
	
	public void setsumaCIGCHasta31122016CredIPE_C31(double sumaCIGCHasta31122016CredIPE_C31) {
		this.sumaCIGCHasta31122016CredIPE_C31=sumaCIGCHasta31122016CredIPE_C31;
	}
	
	public double getsumaCIGCImptoTasaAdicional_C32() {
		return sumaCIGCImptoTasaAdicional_C32;
	}
	
	public void setsumaCIGCImptoTasaAdicional_C32(double sumaCIGCImptoTasaAdicional_C32) {
		this.sumaCIGCImptoTasaAdicional_C32=sumaCIGCImptoTasaAdicional_C32;
	}

	public double getsumaDevCapArt17N7LIR_C33() {
		return sumaDevCapArt17N7LIR_C33;
	}
	
	public void setsumaDevCapArt17N7LIR_C33(double sumaDevCapArt17N7LIR_C33) {
		this.sumaDevCapArt17N7LIR_C33=sumaDevCapArt17N7LIR_C33;
	}


	public RegistroDJ1949() {
    }
	
    public void acumular(final RegistroDJ1949 registroDJ1949) {
    	 this.sumaCantCuotas3112 += registroDJ1949.getsumaCantCuotas3112();
    	 this.sumaMtoDivReaAfecGCCCIDPC_C6 += registroDJ1949.getsumaMtoDivReaAfecGCCCIDPC_C6();
      	 this.sumaMtoDivReaAfecGCCCIDPCAL311216_C7 += registroDJ1949.getsumaMtoDivReaAfecGCCCIDPCAL311216_C7();
      	 this.sumaMtoDivReaAfecGCCDIDPCVol_C8 += registroDJ1949.getsumaMtoDivReaAfecGCCDIDPCVol_C8();
      	 this.sumaMtoDivReaAfecGCSinDer_C9 += registroDJ1949.getsumaMtoDivReaAfecGCSinDer_C9();
      	 this.sumaMtoDivReaREXTribCumpRentasRap_C10 += registroDJ1949.getsumaMtoDivReaREXTribCumpRentasRap_C10();
      	 this.sumaMtoDivReaREXTribCumpOtrasRentas_C11 += registroDJ1949.getsumaMtoDivReaREXTribCumpOtrasRentas_C11();
      	 this.sumaMtoDivReaREXTribCumpExcDist_C12 += registroDJ1949.getsumaMtoDivReaREXTribCumpExcDist_C12();
      	 this.sumaMtoDivReaREXTribCumpUtiISFUT_C13 += registroDJ1949.getsumaMtoDivReaREXTribCumpUtiISFUT_C13();
      	 this.sumaMtoDivReaREXTribCumpRentasISFUT_C14 += registroDJ1949.getsumaMtoDivReaREXTribCumpRentasISFUT_C14();
      	 this.sumaMtoDivReaREXRentExIGCAfecIAart11_C15 += registroDJ1949.getsumaMtoDivReaREXRentExIGCAfecIAart11_C15();
      	 this.sumaMtoDivReaREXRentExIGCIA_C16 += registroDJ1949.getsumaMtoDivReaREXRentExIGCIA_C16();
      	 this.sumaMtoDivReaREXNoConstRenta_C17 += registroDJ1949.getsumaMtoDivReaREXNoConstRenta_C17();
      	 this.sumaCIGCDesde01012017RentAfecNoRestSDD_C18 += registroDJ1949.getsumaCIGCDesde01012017RentAfecNoRestSDD_C18();
      	 this.sumaCIGCDesde01012017RentAfecNoRestCDD_C19 += registroDJ1949.getsumaCIGCDesde01012017RentAfecNoRestCDD_C19();
      	 this.sumaCIGCDesde01012017RentAfecNoRestSDD_C20 += registroDJ1949.getsumaCIGCDesde01012017RentAfecNoRestSDD_C20();
      	 this.sumaCIGCDesde01012017RentAfecNoRestCDD_C21 += registroDJ1949.getsumaCIGCDesde01012017RentAfecNoRestCDD_C21();
      	 this.sumaCIGCDesde01012017RentAfecRestSDD_C22 += registroDJ1949.getsumaCIGCDesde01012017RentAfecRestSDD_C22();
      	 this.sumaCIGCDesde01012017RentAfecRestCDD_C23 += registroDJ1949.getsumaCIGCDesde01012017RentAfecRestCDD_C23();
      	 this.sumaCIGCDesde01012017RentExenRestSDD_C24 += registroDJ1949.getsumaCIGCDesde01012017RentExenRestSDD_C24();
      	 this.sumaCIGCDesde01012017RentExenRestCDD_C25 += registroDJ1949.getsumaCIGCDesde01012017RentExenRestCDD_C25();
      	 this.sumaCIGCDesde01012017CredIPE_C26 += registroDJ1949.getsumaCIGCDesde01012017CredIPE_C26();
      	 this.sumaCIGCHasta31122016RentAfecSDD_C27 += registroDJ1949.getsumaCIGCHasta31122016RentAfecSDD_C27();
      	 this.sumaCIGCHasta31122016RentAfecCDD_C28 += registroDJ1949.getsumaCIGCHasta31122016RentAfecCDD_C28();
      	 this.sumaCIGCHasta31122016RentExenSDD_C29 += registroDJ1949.getsumaCIGCHasta31122016RentExenSDD_C29();
      	 this.sumaCIGCHasta31122016RentExenCDD_C30 += registroDJ1949.getsumaCIGCHasta31122016RentExenCDD_C30();
      	 this.sumaCIGCHasta31122016CredIPE_C31 += registroDJ1949.getsumaCIGCHasta31122016CredIPE_C31();
      	 this.sumaCIGCImptoTasaAdicional_C32 += registroDJ1949.getsumaCIGCImptoTasaAdicional_C32();
      	 this.sumaDevCapArt17N7LIR_C33 += registroDJ1949.getsumaDevCapArt17N7LIR_C33();
       
    }
	
}
