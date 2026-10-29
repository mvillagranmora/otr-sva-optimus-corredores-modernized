package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarComisionesNNFondoMutuo implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarComisionesNNFondoMutuo.class);
	
	
	private final String SQL_SELECT_CUSTODIA = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarComisionesNNFFMM_SelectConsultarCartera.sql");
		
	public static class EntradaConsultarComisionesNNFFMM extends BcsPojo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_FechaConsultaDesde;
		public String p_FechaConsultaHasta;
		
		public EntradaConsultarComisionesNNFFMM() {
			this.p_IdCliente = 0L;
			this.p_IdPortafolio = 0L;
			this.p_FechaConsultaDesde = "0001-01-01";
			this.p_FechaConsultaHasta = "0001-01-01";
		}
		
		
	}
	
	public static class SalidaConsultarComisionNNFFMM extends BcsPojo{
		List<SalidaCustodia> P_OUT_TBL;
		
		public SalidaConsultarComisionNNFFMM() {
			this.P_OUT_TBL = new ArrayList<SalidaCustodia>();
		}
	}
	
	public static class SalidaCustodia  extends BcsPojo{
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String CodigoPortafolio;
		public String IdSerieFondo;
		public Long FolioCartera;
		public String FechaHasta;
		public java.sql.Date FechaCompra;
		public int DiasPermanencia;
		public BigDecimal ComisionAnual;
		public BigDecimal ComisionDiaria;
		public BigDecimal CuotasCliente;
		public BigDecimal ComisionNN;
		public BigDecimal CuotasFinalesCliente;
		public Long IdCartera;
		public String IdSecuenciaRescateNN;
		
		public SalidaCustodia () {
			this.IdSecuenciaRescateNN = "";
		}
	   	
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarComisionesNNFFMM jin = gson.fromJson(in.toString(), EntradaConsultarComisionesNNFFMM.class);
		SalidaConsultarComisionNNFFMM out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarComisionNNFFMM logica(Connection con, EntradaConsultarComisionesNNFFMM entrada) throws Exception{
		logger.info("INICIO::ConsultarComisionesNNFondoMutuo");
		long inicio = System.currentTimeMillis();
		SQL sql=new SQL();
		
		SalidaConsultarComisionNNFFMM out = new SalidaConsultarComisionNNFFMM();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				
		List<SalidaCustodia> listCustodias = new ArrayList<SalidaCustodia>();
		
		
		String fechaHastaUltimoDiaHabil = obtenerDiaHabilAnterior(entrada.p_FechaConsultaHasta);
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("FechaConsultaDesde", entrada.p_FechaConsultaDesde);
		params.put("FechaConsultaHasta", entrada.p_FechaConsultaHasta);
		params.put("FechaHastaUltimoDiaHabil", fechaHastaUltimoDiaHabil);
		
		long inicioSelectCustodia = System.currentTimeMillis();
		listCustodias = sql.query(con, SQL_SELECT_CUSTODIA, params, SalidaCustodia.class);
		out.P_OUT_TBL = obtenerDatosSalida(listCustodias);
		long finSelectCustodia = System.currentTimeMillis();
		
		logger.info("Tiempo::SELECTCUSTODIA:",finSelectCustodia-inicioSelectCustodia);
		
		long fin = System.currentTimeMillis();
		
		logger.info("FIN::ConsultarComisionesNNFondoMutuo",fin - inicio);

		return out;
	}
	
	private List<SalidaCustodia> obtenerDatosSalida(List<SalidaCustodia> listCustodias) {

		List<SalidaCustodia> custodias = listCustodias.stream().map( cust ->{ 
			SalidaCustodia custodia = new SalidaCustodia();
			custodia.DocumentoIdentidad = cust.DocumentoIdentidad;
			custodia.NombreCompleto = cust.NombreCompleto;
			custodia.CodigoPortafolio = cust.CodigoPortafolio;
			custodia.IdSerieFondo = cust.IdSerieFondo;
			custodia.FolioCartera = cust.FolioCartera;
			custodia.IdCartera = cust.IdCartera;
			custodia.FechaHasta = cust.FechaHasta;
			custodia.FechaCompra = cust.FechaCompra;
			custodia.DiasPermanencia = cust.DiasPermanencia;
			custodia.ComisionAnual = cust.ComisionAnual;
			custodia.ComisionDiaria = cust.ComisionDiaria;
			custodia.CuotasCliente = cust.CuotasCliente;
			custodia.ComisionNN = cust.ComisionNN.setScale(3, BigDecimal.ROUND_UP);
			custodia.CuotasFinalesCliente = cust.CuotasFinalesCliente.setScale(3, BigDecimal.ROUND_UP);
			custodia.IdSecuenciaRescateNN = cust.IdSecuenciaRescateNN;
			return custodia;
		}).collect(Collectors.toList());
		return custodias;
	}

	public String obtenerDiaHabilAnterior(String date) throws ParseException {
		java.util.Date infechaDiaHabil = new SimpleDateFormat("yyyy-MM-dd").parse(date);
		SimpleDateFormat outFechaDiaHabil = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
	    cal.setTime(infechaDiaHabil);
	    
	    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
	    if(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
	    	return outFechaDiaHabil.format(cal.getTime());
	    }
	    
	    do {
	        cal.add(Calendar.DAY_OF_MONTH, -1);
	        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
	    } while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
	    
		return outFechaDiaHabil.format(cal.getTime());
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_AgrupaPorBoveda\":\"S\",\"p_FechaConsultaDesde\":\"2022-01-01\",\"p_FechaConsultaHasta\":\"2022-03-31\",\"p_IdCliente\":0,\"p_IdPortafolio\":0}";
			EntradaConsultarComisionesNNFFMM Objin = gson.fromJson(json, EntradaConsultarComisionesNNFFMM.class);
			ConsultarComisionesNNFondoMutuo  demo=new ConsultarComisionesNNFondoMutuo();				
			try {
				SalidaConsultarComisionNNFFMM ObjOut=demo.logica(con, Objin);
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
