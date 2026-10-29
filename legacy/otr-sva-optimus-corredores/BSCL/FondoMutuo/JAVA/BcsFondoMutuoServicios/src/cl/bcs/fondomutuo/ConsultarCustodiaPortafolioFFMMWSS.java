package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCustodiaPortafolioFFMMWSS implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioFFMMWSS.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_PERSONA_DESTINO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMMWSS_SelectPersonaDestino.sql");
	
	//ACTUAL
	private final String SQL_SELECT_CUSTODIA = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMMWSS_SelectConsultarCustodia.sql");
	private final String SQL_SELECT_GARANTIA = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMMWSS_SelectConsultarGarantia.sql");
	
	//CUSTODIA
	private final String SQL_SELECT_CUSTODIA_HISTORICO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMMWSS_SelectConsultarCustodiaHistorico.sql");
	private final String SQL_SELECT_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMMWSS_SelectConsultarGarantiaHistorico.sql");
	
	public static class EntradaConsultarCustodiaPortafolioFFMMWSS{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdProductoAgrupador;
		public String p_IdEmisor;
		public Long p_IdInstrumento;
		public String p_IdBoveda; // solo actua como filtro, no agrupa x boveda
		public String p_AgrupaPorBoveda; 
		public String p_AgrupaPorRelacion; // S: usa relacion como un criterio más de agrupacion y la muestra   N: no considera relacion en la agrupacion
		public String p_FechaConsulta;
		public String p_TipoFechaConsulta; // Consultar por LIQ: Fecha Liquidación  ADQ: Fecha Adquisicion
		public String p_IdFamiliaFondo;
		
		public EntradaConsultarCustodiaPortafolioFFMMWSS() {
			this.p_IdCliente = 0L;
			this.p_IdPortafolio = 0L;
			this.p_IdInstrumento = 0L;
			this.p_IdBoveda = "";
			this.p_AgrupaPorBoveda = "N";
			this.p_AgrupaPorRelacion = "N";
			this.p_FechaConsulta = "0001-01-01";
			this.p_TipoFechaConsulta = "LIQ";
			this.p_IdFamiliaFondo = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaConsultarCustodiaPortafolioFFMMWSS{
		List<SalidaCustodiaWSS> p_out_tbl;
		
		public SalidaConsultarCustodiaPortafolioFFMMWSS() {
			this.p_out_tbl = new ArrayList<SalidaCustodiaWSS>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCustodiaWSS{
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public Long IdSocioDeNegocio;
		public Long IdCliente;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public String IdRegimenFondoMutuo;
		public String RegimenTributario;
		public String Producto;
		public BigDecimal CantidadCustodia;
		public BigDecimal CantidadGarantia;
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
		public BigDecimal TotalPosicion;
		public Long IdMatrizAgrupador;
	   	public String EsADC;
	   	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CustodiaWSS{
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdSocioDeNegocio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public BigDecimal Cantidad;
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
	   	public BigDecimal ValorCompra;
	   	public BigDecimal MontoCompra;
	   	public String IdRegimenFondoMutuo;
	   	public Long IdPersona;
	   	public String Producto;
	   	public Long IdMatrizAgrupador;
	   	public String EsADC;
	   	public String RegimenTributario;
	   	
	   	public CustodiaWSS() {
	   		this.Cantidad = BigDecimal.ZERO;
	   		this.INGPrecioPromedioCompra = BigDecimal.ZERO;
	   		this.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
	   		this.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
	   		this.ValorCompra = BigDecimal.ZERO;
	   		this.MontoCompra = BigDecimal.ZERO;
	   		this.RegimenTributario="";
	   	}
	   	
	   	
	   	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiaWSS{
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdSocioDeNegocio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public BigDecimal Cantidad;
		public String IdRegimenFondoMutuo;
		public Long IdPersona;
		public String Producto;
		public Long IdMatrizAgrupador;
	   	public String EsADC;
		public GarantiaWSS() {
			this.Cantidad = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PersonaDestinoWSS{
		public Long IdPersonaDestino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoWSS{
		public String Id;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdSocioDeNegocio;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public String IdBoveda;
		public String Relacion;
		public String IdRegimenFondoMutuo;
		public String Producto;
		public String EsADC;
		public Long IdMatrizAgrupador;
		public String RegimenTributario;
		public String toString() {
			return Utils.printJson(this, false);
		} 
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarCustodiaPortafolioFFMMWSS jin = gson.fromJson(in.toString(), EntradaConsultarCustodiaPortafolioFFMMWSS.class);
		SalidaConsultarCustodiaPortafolioFFMMWSS out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCustodiaPortafolioFFMMWSS logica(Connection con, EntradaConsultarCustodiaPortafolioFFMMWSS entrada) throws Exception{
		logger.info("INICIO::ConsultarCustodiaPortafolioFFMM");
		long inicioConsultarCustodiaPortafolioFFMM = System.currentTimeMillis();
		SQL sql=new SQL();
		
		SalidaConsultarCustodiaPortafolioFFMMWSS out = new SalidaConsultarCustodiaPortafolioFFMMWSS();
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		
		List<CustodiaWSS> listCustodias = new ArrayList<CustodiaWSS>();
		List<GarantiaWSS> listGarantias = new ArrayList<GarantiaWSS>(); 
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		Date dFechaEntrada = Fecha.toDate(entrada.p_FechaConsulta);
		Date dFechaSistema = Fecha.toDate(fechaSistema);
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("FechaConsulta", entrada.p_FechaConsulta);
		
		
		if("0001-01-01".equals(entrada.p_FechaConsulta) || dFechaEntrada.compareTo(dFechaSistema) != -1) {
			long inicioSelectGarantia = System.currentTimeMillis();
			listGarantias = sql.query(con, SQL_SELECT_GARANTIA, params, GarantiaWSS.class);
			long finSelectGarantia = System.currentTimeMillis();
			logger.debug("Tiempo SQL_SELECT_GARANTIA: " + (finSelectGarantia - inicioSelectGarantia));
		}else{
			long inicioSelectGarantiaHis = System.currentTimeMillis();
			params.put("TipoFechaConsulta", entrada.p_TipoFechaConsulta);
			listGarantias = sql.query(con, SQL_SELECT_GARANTIA_HISTORICO, params, GarantiaWSS.class);
			long finSelectGarantiaHis = System.currentTimeMillis();
			logger.debug("Tiempo SQL_SELECT_GARANTIA_HISTORICO: " + (finSelectGarantiaHis - inicioSelectGarantiaHis));
		}
		
		
		if("0001-01-01".equals(entrada.p_FechaConsulta) || dFechaEntrada.compareTo(dFechaSistema) != -1) {
			long inicioSelectCustodia = System.currentTimeMillis();
			listCustodias = sql.query(con, SQL_SELECT_CUSTODIA, params, CustodiaWSS.class);
			long finSelectCustodia = System.currentTimeMillis();
			logger.debug("Tiempo SQL_SELECT_CUSTODIA: " + (finSelectCustodia - inicioSelectCustodia)+", SIZE: "+listCustodias.size());
		}else{
			long inicioSelectCustodiaHis = System.currentTimeMillis();
			params.put("TipoFechaConsulta", entrada.p_TipoFechaConsulta);
			listCustodias = sql.query(con, SQL_SELECT_CUSTODIA_HISTORICO, params, CustodiaWSS.class);
			long finSelectCustodiaHis = System.currentTimeMillis();
			logger.debug("Tiempo SQL_SELECT_CUSTODIA_HISTORICO: " + (finSelectCustodiaHis - inicioSelectCustodiaHis));
		}
		
		params.clear();
		params.put("IdPersonaCuentaPropia", empresa.IdPersona);
		long inicioSelectPersonaDestino = System.currentTimeMillis();
		List<PersonaDestinoWSS> listPersona = sql.query(con, SQL_SELECT_PERSONA_DESTINO, params, PersonaDestinoWSS.class);
		long finSelectPersonaDestino = System.currentTimeMillis();
		logger.debug("Tiempo SQL_SELECT_PERSONA_DESTINO: " + (finSelectPersonaDestino - inicioSelectPersonaDestino) + ", SIZE: "+listPersona.size() );
		HashMap<Long,Object> hPersonaDestino = Utils.List2Hash(listPersona, "IdPersonaDestino");
		
		HashMap<String,CustodiaWSS> hCustodia = new HashMap<String,CustodiaWSS>();
		HashMap<String,GarantiaWSS> hGarantia = new HashMap<String,GarantiaWSS>();
		HashMap<String,InstrumentoWSS> hInstrumento = new HashMap<String,InstrumentoWSS>();
		
		for(CustodiaWSS cus : listCustodias) {
			String key = cus.IdInstrumento + "_" + cus.Nemotecnico + "_" + cus.IdEmisor + "_" + cus.IdCliente + "_" + cus.IdPortafolio + "_" + cus.IdRegimenFondoMutuo;
			String relacion = "";
			String boveda = "";
			if(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {
				if(cus.IdCliente.equals(empresa.IdCliente)) relacion = "CPRO";
				else if(hPersonaDestino.get(cus.IdPersona) != null) relacion = "REL";
				else relacion = "TER";
				key += "_" + relacion;
			}
			if(_c.SI.equals(entrada.p_AgrupaPorBoveda)) {
				boveda = cus.IdBoveda;
				key += "_" + boveda;
			}
			CustodiaWSS custodia = hCustodia.get(key);
			if(custodia == null) {
				custodia = new CustodiaWSS();
				custodia.IdClasificacionTipoInstrumento = cus.IdClasificacionTipoInstrumento;
				custodia.NombreClasificacionTipoInstrumento = cus.NombreClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = cus.IdTipoInstrumento;
				custodia.NombreTipoInstrumento = cus.NombreTipoInstrumento;
				custodia.IdInstrumento = cus.IdInstrumento;
				custodia.Nemotecnico = cus.Nemotecnico;
				custodia.IdEmisor = cus.IdEmisor;
				custodia.IdCliente = cus.IdCliente;
				custodia.IdPortafolio = cus.IdPortafolio;
				custodia.Relacion = relacion;
				custodia.IdBoveda = boveda;
				custodia.IdRegimenFondoMutuo = cus.IdRegimenFondoMutuo;
				custodia.RegimenTributario = cus.RegimenTributario;
				custodia.IdSocioDeNegocio = cus.IdSocioDeNegocio;
				custodia.DocumentoIdentidad = cus.DocumentoIdentidad;
				custodia.RazonSocial = cus.RazonSocial;
				custodia.CodigoPortafolio = cus.CodigoPortafolio;
				custodia.Producto = cus.Producto;
				custodia.EsADC = cus.EsADC;
				custodia.IdMatrizAgrupador = cus.IdMatrizAgrupador;
				
				InstrumentoWSS ins = hInstrumento.get(key);
				if(ins == null) {
					InstrumentoWSS instrumento = new InstrumentoWSS();
					instrumento = Utils.copyPojo(custodia, instrumento);
					instrumento.Id = key;
					hInstrumento.put(key, instrumento);
				}
			}
			custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL128);
			custodia.ValorCompra = custodia.ValorCompra.add(cus.ValorCompra, MathContext.DECIMAL128);
			custodia.MontoCompra = custodia.MontoCompra.add(cus.MontoCompra, MathContext.DECIMAL128);
			
			hCustodia.put(key, custodia);
			
		}
		
		for(GarantiaWSS gar : listGarantias) {
			String key = gar.IdInstrumento + "_" + gar.Nemotecnico + "_" + gar.IdEmisor + "_" + gar.IdCliente + "_" + gar.IdPortafolio + "_" + gar.IdRegimenFondoMutuo;
			String relacion = "";
			String boveda = "";
			if(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {
				if(gar.IdCliente.equals(empresa.IdCliente)) relacion = "CPRO";
				else if(hPersonaDestino.get(gar.IdPersona) != null) relacion = "REL";
				else relacion = "TER";
				key += "_" + relacion;
			}
			if(_c.SI.equals(entrada.p_AgrupaPorBoveda)) {
				boveda = gar.IdBoveda;
				key += "_" + boveda;
			}
			GarantiaWSS garantia = hGarantia.get(key);
			if(garantia == null) {
				garantia = new GarantiaWSS();
				garantia.IdClasificacionTipoInstrumento = gar.IdClasificacionTipoInstrumento;
				garantia.NombreClasificacionTipoInstrumento = gar.NombreClasificacionTipoInstrumento;
				garantia.IdTipoInstrumento = gar.IdTipoInstrumento;
				garantia.NombreTipoInstrumento = gar.NombreTipoInstrumento;
				garantia.IdInstrumento = gar.IdInstrumento;
				garantia.Nemotecnico = gar.Nemotecnico;
				garantia.IdEmisor = gar.IdEmisor;
				garantia.IdCliente = gar.IdCliente;
				garantia.IdPortafolio = gar.IdPortafolio;
				garantia.Relacion = relacion;
				garantia.IdBoveda = boveda;
				garantia.IdRegimenFondoMutuo = gar.IdRegimenFondoMutuo;
				garantia.IdSocioDeNegocio = gar.IdSocioDeNegocio;
				garantia.DocumentoIdentidad = gar.DocumentoIdentidad;
				garantia.RazonSocial = gar.RazonSocial;
				garantia.CodigoPortafolio = gar.CodigoPortafolio;
				garantia.Producto = gar.Producto;
				garantia.EsADC = gar.EsADC;
				garantia.IdMatrizAgrupador = gar.IdMatrizAgrupador;
				
				InstrumentoWSS ins = hInstrumento.get(key);
				if(ins == null) {
					InstrumentoWSS instrumento = new InstrumentoWSS();
					instrumento = Utils.copyPojo(garantia, instrumento);
					instrumento.Id = key;
					hInstrumento.put(key, instrumento);
				}
			}
			garantia.Cantidad = garantia.Cantidad.add(gar.Cantidad, MathContext.DECIMAL128);
			hGarantia.put(key,garantia);
		}
		
		for(Map.Entry<String,InstrumentoWSS> obj : hInstrumento.entrySet()) {
			InstrumentoWSS ins = obj.getValue();
			SalidaCustodiaWSS salida = new SalidaCustodiaWSS();
			salida.IdClasificacionTipoInstrumento = ins.IdClasificacionTipoInstrumento;
			salida.NombreClasificacionTipoInstrumento = ins.NombreClasificacionTipoInstrumento;
			salida.IdTipoInstrumento = ins.IdTipoInstrumento;
			salida.NombreTipoInstrumento = ins.NombreTipoInstrumento;
			salida.IdInstrumento = ins.IdInstrumento;
			salida.Nemotecnico = ins.Nemotecnico;
			salida.IdEmisor = ins.IdEmisor;
			salida.IdSocioDeNegocio = ins.IdSocioDeNegocio;
			salida.IdCliente = ins.IdCliente;
			salida.IdPortafolio = ins.IdPortafolio;
			salida.DocumentoIdentidad = ins.DocumentoIdentidad;
			salida.RazonSocial = ins.RazonSocial;
			salida.CodigoPortafolio = ins.CodigoPortafolio;
			salida.IdBoveda = ins.IdBoveda;
			salida.Relacion = ins.Relacion;
			salida.IdRegimenFondoMutuo = ins.IdRegimenFondoMutuo;
			salida.RegimenTributario = ins.RegimenTributario;
			salida.Producto = ins.Producto;
			salida.EsADC = ins.EsADC;
			salida.IdMatrizAgrupador = ins.IdMatrizAgrupador;
			CustodiaWSS custodia = hCustodia.get(ins.Id);
			if(custodia == null) {
				salida.CantidadCustodia = BigDecimal.ZERO;
				salida.INGPrecioPromedioCompra = BigDecimal.ZERO;
				salida.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
				salida.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
			}else {
				salida.CantidadCustodia = custodia.Cantidad;					
				
				if (custodia.Cantidad.compareTo(BigDecimal.ZERO) == 0) {						
					salida.INGPrecioPromedioCompra = BigDecimal.ZERO;
					salida.INGMontoPrecioPromedioMonedaInstrumento  = BigDecimal.ZERO;
					salida.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
				}else {					
					salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);
					salida.INGMontoPrecioPromedioMonedaInstrumento = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);
					salida.INGMontoPrecioPromedioCompra = custodia.MontoCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);
				}			
			}
			
			GarantiaWSS garantia = hGarantia.get(ins.Id);
			if(garantia == null) {
				salida.CantidadGarantia = BigDecimal.ZERO;
			}else {
				salida.CantidadGarantia = garantia.Cantidad;
			}
			salida.TotalPosicion = salida.CantidadCustodia.add(salida.CantidadGarantia, MathContext.DECIMAL128);
			
			out.p_out_tbl.add(salida);
		}
		long finConsultarCustodiaPortafolioFFMM = System.currentTimeMillis();
		logger.debug("Tiempo ConsultarCustodiaPortafolioFFMM: " + (finConsultarCustodiaPortafolioFFMM - inicioConsultarCustodiaPortafolioFFMM));
		logger.info("FIN::ConsultarCustodiaPortafolioFFMM");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":\"1\",\"p_IdModulo\":\"FMM\",\"p_FechaConsulta\":\"0001-01-01\",\"p_IdCliente\":3045,\"p_IdPortafolio\":0,\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"N\",\"p_IdProductoAgrupador\":null}";
			EntradaConsultarCustodiaPortafolioFFMMWSS Objin = gson.fromJson(json, EntradaConsultarCustodiaPortafolioFFMMWSS.class);
			ConsultarCustodiaPortafolioFFMMWSS  demo=new ConsultarCustodiaPortafolioFFMMWSS();				
			try {
				SalidaConsultarCustodiaPortafolioFFMMWSS ObjOut=demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
				con.rollback(); 			
	    	}
			System.out.println("== END ==");
	}

}
