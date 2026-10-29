package cl.bcs.contabilidad;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.google.gson.JsonObject;

import cl.bcs.contabilidad.pojo.BusinessPartner;
import cl.bcs.contabilidad.pojo.JournalEntry;
import cl.bcs.contabilidad.pojo.JournalEntryLine;
import cl.bcs.contabilidad.pojo.JournalVoucher;
import cl.bcs.contabilidad.pojo.JournalVoucher_;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.sap.pojo.OACT;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarJsonAsientoSap implements JavaCall {
	
	public Log log = new Log("CON.GenerarJsonAsientoSap");
	public String SQL_SELECT_OACT = loadResourceToString("cl/bcs/contabilidad/sql/InterfazContable_SelectOACT.sql");
	public String SQL_SELECT_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/InterfazContable_SelectPreComprobante.sql");
	public String SQL_SELECT_PRECOMPROBANTE_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/InterfazContable_SelectPrecomprobanteAsiento.sql");
	
	public String localizacionSistema;
	public List<Short> cuentasResultado = Arrays.asList(new Short[]{4,5,6,7,8});
	public final String CLASE_CUENTA_VENTAS = "I";
	public final String CLASE_CUENTA_SALIDA = "E";

	public static void main (String [] args) throws Exception {
  		
	}

	public static class GenerarJsonAsientoSap_Entrada {

		public List<Long> p_IdPreComprobantes;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Destino;
		
		public GenerarJsonAsientoSap_Entrada() {
			p_IdPreComprobantes = new ArrayList<>();
		}
		
	}
	
	public static class GenerarJsonAsientoSap_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<BusinessPartner> P_OUT_SOCIOS;
		public List<JournalVoucher> P_OUT_VOUCHERS;
		
		public GenerarJsonAsientoSap_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_SOCIOS = new ArrayList<>();
			P_OUT_VOUCHERS = new ArrayList<>();
			
		}
	}

	public static class PreComprobante_Proceso {
		public Long JdtNum;
		public String ReferenceDate;
		public String Memo;
		public String DueDate;
		public String TaxDate;
		public Long U_BCS_IDPreComp;
		public Long U_BCS_TipoCen;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteAsiento_Proceso {
		public Long IdTipoContabilidad;
		public Long ParentKey;
		public BigDecimal Debit;
		public BigDecimal Credit;
		public String U_glosa;
		public String LineMemo;
		public String ProfitCode;
		public Long BPLId;
		public String AccountCode;
		public String DueDate;
		public String Formato;
		public String TieneProject;
		public String Project;
		public String ProductoMovimiento;
		public String RelacionCorredora;
		public String CodSecuencia;
		public Long IdMovimiento;
		public String Reference1;
		public String Reference2;
		public String AdditionalReference;
		public BigDecimal FCDebit;
		public BigDecimal FCCredit;
		public String FCCurrency;
		public String ShortName;
		public String TipoSocioNegocio;
		public String ReferenceDate1;
		public String TaxDate;
		public String CostingCode;
		public String CostingCode2;
		public String CostingCode3;
		public String CostingCode4;
		public String CostingCode5;
		public String U_BCS_TipoPersona;
		public String U_BCS_Rut;
		public String U_BCS_Portafolio;
		public String U_BCS_Nombre;
		public String U_BCS_CtaDCV;
		public String U_BCS_EmitidoA;
		public String U_BCS_Comprobante;
		public String U_BCS_Folio;
		public String U_BCS_NDcto;
		public String U_BCS_Concepto;
		public String U_BCS_CV;
		public String U_BCS_Nemo;
		public String U_BCS_IFRSCtaPropia;
		public String U_BCS_CodigoProducto;
		public String U_BCS_Bolsa;
		public String U_BCS_Relacion;
		public String U_RelacionCodigoSap;
		public String U_BCS_FolioDTE;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AdvertenciaDetalle extends PlataformaUtils.AdvertenciaDetalle {
		public Long IdInterno;
	}
	
	public static class ErrorDetalle extends PlataformaUtils.ErrorDetalle {
		public Long IdInterno;
	}

	public GenerarJsonAsientoSap_Salida logica(Connection conn, GenerarJsonAsientoSap_Entrada in) throws Exception {
		
		SQL _sql = new SQL();
		
		localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		//Crear json para login
		Properties prop = new Properties();
        //InputStream input = new FileInputStream(RutaArchivosConfiguracion + "interfazSAP.properties");
		InputStream input = new FileInputStream("interfazSAP.properties");
        prop.load(input);
        
		Connection sapConnection = Utils.getSAPConnection(prop.getProperty("HANA_SERVER"),prop.getProperty("HANA_PORT"),prop.getProperty("B1_COMPANY"),prop.getProperty("HANA_USER"),prop.getProperty("HANA_PASS"));
		List<OACT> cuentas = _sql.query(sapConnection, SQL_SELECT_OACT.replace(":EMPRESA", prop.getProperty("B1_COMPANY")), new HashMap<>(), OACT.class);
		HashMap<String,OACT> hashCuentas = new HashMap<>();
		for (OACT rec : cuentas) {
			hashCuentas.put(rec.AcctCode, rec);
		}
		
		GenerarJsonAsientoSap_Salida salida = new GenerarJsonAsientoSap_Salida();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPrecomprobante", in.p_IdPreComprobantes);
		List<PreComprobante_Proceso> procomprobantes = _sql.query(conn, SQL_SELECT_PRECOMPROBANTE, params, PreComprobante_Proceso.class);
		List<PreComprobanteAsiento_Proceso> procomprobantesAsientos = _sql.query(conn, SQL_SELECT_PRECOMPROBANTE_ASIENTO, params, PreComprobanteAsiento_Proceso.class);
		
		List<PreComprobante_Proceso> precomprobantesExistentes = new ArrayList<PreComprobante_Proceso>();
		for(Long IdPreComprobante : in.p_IdPreComprobantes){
			PreComprobante_Proceso precomprobanteReal = null;
			for(PreComprobante_Proceso p : procomprobantes){
				if(p.JdtNum.equals(IdPreComprobante)){
					precomprobanteReal = p;
					break;   
				}
			}
			if(precomprobanteReal == null){
				log.write("Precomprobante " + IdPreComprobante + " sin asientos");
			}else{
				precomprobantesExistentes.add(precomprobanteReal);
			}
		}
		
		for(PreComprobante_Proceso precomprobante : precomprobantesExistentes){
			JournalEntry je = new JournalEntry();
			je.DueDate = precomprobante.DueDate;
			je.JdtNum = precomprobante.JdtNum;
			je.Memo = precomprobante.Memo;
			je.ReferenceDate = precomprobante.ReferenceDate;
			je.TaxDate = precomprobante.TaxDate;
			je.U_BCS_IDPreComp = precomprobante.U_BCS_IDPreComp+"";
			je.U_BCS_TipoCen = precomprobante.U_BCS_TipoCen+"";
			
			List<BusinessPartner> socios = new ArrayList<BusinessPartner>();
			List<PreComprobanteAsiento_Proceso> asientos = new ArrayList<PreComprobanteAsiento_Proceso>();
			for(PreComprobanteAsiento_Proceso asiento : procomprobantesAsientos){
				if(asiento.ParentKey.equals(precomprobante.JdtNum)){
					asientos.add(asiento);
				}
			}
			if(asientos.size() > 0){
				je.JournalEntryLines = CrearDetalle(asientos,hashCuentas,socios);
			}
			
			JournalVoucher jV = new JournalVoucher();
		    JournalVoucher_ jV_ = new JournalVoucher_();
		    jV_.setJournalEntry(je);
		    jV.setJournalVoucher(jV_);
		    salida.P_OUT_SOCIOS.addAll(socios);
		    salida.P_OUT_VOUCHERS.add(jV);
		}
		    
		return salida ;
		
	}

	private ArrayList<JournalEntryLine> CrearDetalle(List<PreComprobanteAsiento_Proceso> asientos, HashMap<String, OACT> hashCuentas,List<BusinessPartner> socios) throws Exception {
		
		ArrayList<JournalEntryLine> journalEntryLines= new ArrayList<JournalEntryLine>();
		
		for (PreComprobanteAsiento_Proceso asiento : asientos) {
			JournalEntryLine oJournalEntryLine = new JournalEntryLine();
			String tipoSocio = asiento.TipoSocioNegocio;
			OACT oACT = hashCuentas.get(asiento.AccountCode);
			boolean esCuentaAnalitica = oACT != null && "Y".equals(oACT.LocManTran);
			boolean esCuentaCostingCode = oACT != null && (CLASE_CUENTA_SALIDA.equalsIgnoreCase(oACT.ActType) || CLASE_CUENTA_VENTAS.equalsIgnoreCase(oACT.ActType));
			if(esCuentaAnalitica) {
				oJournalEntryLine.ShortName = asiento.ShortName;
				//AGREGAMOS EL SOCIO A LA LISTA PARA CREARLO POSTERIORMENTE (SI NO EXISTE)
				BusinessPartner bp;
				if (tipoSocio.equalsIgnoreCase("CONTRAPARTE")) {
					if (localizacionSistema.equalsIgnoreCase("PE")) {
						bp = new BusinessPartner(asiento.ShortName,"0",asiento.U_BCS_Nombre,"cCustomer",BusinessPartner.CONTRAPARTE);
						socios.add(bp);
					}
					else {
						bp = new BusinessPartner(asiento.ShortName,asiento.U_BCS_Rut,asiento.U_BCS_Nombre,"cCustomer",BusinessPartner.CONTRAPARTE);
						socios.add(bp);
					}
				}
				else if (tipoSocio.equalsIgnoreCase("PROVEEDOR")) {
					bp = new BusinessPartner(asiento.ShortName,asiento.U_BCS_Rut,asiento.U_BCS_Nombre,"cSupplier",BusinessPartner.PROVEEDOR);
					socios.add(bp);
				}
				else {
					//CLIENTE
					if (localizacionSistema.equalsIgnoreCase("PE")) {
						bp = new BusinessPartner(asiento.ShortName,"0",asiento.U_BCS_Nombre,"cCustomer");
						socios.add(bp);
					}
					else {
						bp = new BusinessPartner(asiento.ShortName,asiento.U_BCS_Rut,asiento.U_BCS_Nombre,"cCustomer");
						socios.add(bp);
					}
				}
			}
			
			BigDecimal debit = asiento.Debit;
			BigDecimal credit = asiento.Credit;
			BigDecimal fCDebit = asiento.FCDebit;
			BigDecimal fCCredit = asiento.FCCredit;
			
			if ((debit.compareTo(BigDecimal.ZERO) == 0) 
					&& (credit.compareTo(BigDecimal.ZERO) == 0) 
					&& (fCDebit.compareTo(BigDecimal.ZERO) == 0) 
					&& (fCCredit.compareTo(BigDecimal.ZERO) == 0)) {
				continue;
			}
			
			
			log.write("DEBIT: " + debit.toString());
			log.write("FCDEBIT: " + fCDebit.toString());
			log.write("CREDIT: " + credit.toString());
			log.write("FCCREDIT: " + fCCredit.toString());
			
			if (debit.compareTo(BigDecimal.ZERO) == 1 || credit.compareTo(BigDecimal.ZERO) == 1) {
				oJournalEntryLine.Debit = debit;
				oJournalEntryLine.Credit = credit;
				
			}
			if (fCCredit.compareTo(BigDecimal.ZERO) == 1 || fCDebit.compareTo(BigDecimal.ZERO) == 1) {
				oJournalEntryLine.FCDebit = fCDebit;
				oJournalEntryLine.FCCredit = fCCredit;
			}

			oJournalEntryLine.LineMemo = asiento.LineMemo.replaceAll("\u003c", "").replaceAll("\u003e", "");
			oJournalEntryLine.ProjectCode = asiento.Project;
			oJournalEntryLine.BPLID = asiento.BPLId;
			oJournalEntryLine.DueDate = asiento.DueDate;
			oJournalEntryLine.AccountCode =  asiento.AccountCode;
			
			//Formato Detallado o Agrupado
			if(asiento.Formato.equalsIgnoreCase("Detallado")){
				log.write("FORMATO DETALLADO");
				
				oJournalEntryLine.Reference1 = asiento.Reference1;
				oJournalEntryLine.Reference2 = asiento.Reference2;
				oJournalEntryLine.AdditionalReference = asiento.AdditionalReference;
				oJournalEntryLine.FCCurrency = asiento.FCCurrency;
				
				oJournalEntryLine.ReferenceDate1 = asiento.ReferenceDate1;
				oJournalEntryLine.TaxDate = asiento.TaxDate;
				
				if(esCuentaCostingCode) {
					oJournalEntryLine.CostingCode = asiento.CostingCode;
					oJournalEntryLine.CostingCode2 = asiento.CostingCode2;
					oJournalEntryLine.CostingCode3 = asiento.CostingCode3;
					oJournalEntryLine.CostingCode4 = asiento.CostingCode4;
					oJournalEntryLine.CostingCode5 = asiento.CostingCode5;
				}
					
				oJournalEntryLine.U_BCS_TipoPersona = asiento.U_BCS_TipoPersona;
				oJournalEntryLine.U_BCS_Rut = asiento.U_BCS_Rut;
				oJournalEntryLine.U_BCS_Portafolio = asiento.U_BCS_Portafolio;
				oJournalEntryLine.U_BCS_Nombre = asiento.U_BCS_Nombre;
				oJournalEntryLine.U_BCS_CtaDCV = asiento.U_BCS_CtaDCV;
				oJournalEntryLine.U_BCS_EmitidoA = asiento.U_BCS_EmitidoA;
				oJournalEntryLine.U_BCS_Comprobante = asiento.U_BCS_Comprobante;
				oJournalEntryLine.U_BCS_Folio = asiento.U_BCS_Folio;
				oJournalEntryLine.U_BCS_NDcto = asiento.U_BCS_NDcto;
				oJournalEntryLine.U_BCS_Concepto = asiento.U_BCS_Concepto;
				oJournalEntryLine.U_BCS_CV = asiento.U_BCS_CV;
				oJournalEntryLine.U_BCS_Nemo = asiento.U_BCS_Nemo;
				oJournalEntryLine.U_BCS_IFRSCtaPropia = asiento.U_BCS_IFRSCtaPropia;
				oJournalEntryLine.U_BCS_Producto = asiento.U_BCS_CodigoProducto;
				oJournalEntryLine.U_BCS_Bolsa = asiento.U_BCS_Bolsa;
				oJournalEntryLine.U_BCS_Relacion = asiento.U_BCS_Relacion;
				oJournalEntryLine.U_BCS_GlosaExt = asiento.U_glosa;
				oJournalEntryLine.U_BCS_FecLiquid = asiento.DueDate;
				oJournalEntryLine.U_BCS_FolioDTE = asiento.U_BCS_FolioDTE;
						
			}
			
			journalEntryLines.add(oJournalEntryLine);
		}
		
		return journalEntryLines;
		
	}



	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
