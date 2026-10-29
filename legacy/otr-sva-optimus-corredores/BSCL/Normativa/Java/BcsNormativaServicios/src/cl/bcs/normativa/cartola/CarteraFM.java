package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.custodia.ConsultarDetallePrecioPromedio;
import cl.bcs.custodia.ConsultarDetallePrecioPromedio.EntradaConsultarDetallePrecioPromedio;
import cl.bcs.custodia.ConsultarDetallePrecioPromedio.SalidaConsultarDetallePrecioPromedio;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.EntradaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodiaTemp;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraFM implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraFM");
	
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFM_SelectCartera.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFM_SelectCarteraHistorico.sql");
	
	private final String SQL_SELECT_GARANTIA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFM_SelectGarantia.sql");
	private final String SQL_SELECT_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFM_SelectCarteraGarantiaHistorico.sql");
	
	public static class SalidaCarteraFM{
		public Long IdCliente;
		public Long IdPortafolio;
		public String alert;
		public String nemo;
		public String emisor;
		public String serie;
		public String RAZON_SOCIAL;
		public BigDecimal CAN_CUSTODIA;
		public BigDecimal garantia;
		public BigDecimal valorCuotaAporte;
		public BigDecimal PromedioAporte;
		public BigDecimal valorCuotaActual;
		public String COD_IMP_RES;
		public String IND_DIFERIDO;
		public String NEMO_ASOC;
		public BigDecimal TASA_CAMBIO;
		public BigDecimal BLOQUEO_FONDO;
		public BigDecimal TIR_CONTAB;
		public String TIPO_FAM;
		public String categorizacion;
		public BigDecimal MONTO;
		public BigDecimal MONTO_VALORIZ;
		public BigDecimal MONTO_APORTE;
		public BigDecimal valorActual;
		public BigDecimal RENTABILIDAD;
		public Long ID_INSTRUMENTO;	//GCMBIMECAR-1 Mejoramiento de Cartolas
		public String ID_MONEDA_INSTRUMENTO; //GCMBIMECAR-1 Mejoramiento de Cartolas
		public String IdBoveda;
		public BigDecimal MontoMonedaInstrumento;
		public BigDecimal MontoCalculadoMonInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiaFM{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::CarteraFM");
		
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		EntradaConsultarDetallePrecioPromedio entradaConsultarDetallePrecioPromedio = new EntradaConsultarDetallePrecioPromedio();
		entradaConsultarDetallePrecioPromedio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarDetallePrecioPromedio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarDetallePrecioPromedio.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarDetallePrecioPromedio.p_FechaBusqueda = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		List<SalidaCustodia> salidaPreliminar = new ArrayList<SalidaCustodia>();
		
		if (entrada.p_Datos.size() > 100) {
			List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias = obtenerCustodias(con, entrada,null);
			salidaPreliminar.addAll(custodias);
		}
		else {
			for(Dato dato : entrada.p_Datos){
				List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias = obtenerCustodias(con, entrada,dato);
				salidaPreliminar.addAll(custodias);
			}
		}
		
		List<SalidaCarteraFM> salidas2 = new ArrayList<SalidaCarteraFM>();
		
		for(int k=0; k<salidaPreliminar.size();k++) {
			SalidaCarteraFM salidas3  = new SalidaCarteraFM();
			salidas3.IdCliente = salidaPreliminar.get(k).IdCliente;
			salidas3.IdPortafolio = salidaPreliminar.get(k).IdPortafolio;
			salidas3.nemo = salidaPreliminar.get(k).Nemotecnico;
			salidas3.IdBoveda = salidaPreliminar.get(k).IdBoveda;
			salidas3.CAN_CUSTODIA =  salidaPreliminar.get(k).CantidadCustodia;
			salidas3.emisor = salidaPreliminar.get(k).IdEmisor;
			salidas3.garantia = salidaPreliminar.get(k).CantidadGarantia;
			salidas3.ID_INSTRUMENTO = salidaPreliminar.get(k).IdInstrumento;
			salidas3.valorCuotaActual= salidaPreliminar.get(k).ValorCuota;
			salidas3.ID_MONEDA_INSTRUMENTO = salidaPreliminar.get(k).IdMoneda;
			salidas3.MONTO_VALORIZ = salidaPreliminar.get(k).MontoMonedaSistema.add(salidaPreliminar.get(k).MontoGarantiaMonedaSistema, MathContext.DECIMAL128);
			salidas3.MONTO = salidaPreliminar.get(k).Monto.add(salidaPreliminar.get(k).MontoGarantia, MathContext.DECIMAL128);
			salidas3.NEMO_ASOC = salidaPreliminar.get(k).Nemotecnico;
			salidas3.serie = salidaPreliminar.get(k).CodigoSerie;
			salidas3.alert = "";
			salidas3.RAZON_SOCIAL = salidaPreliminar.get(k).NombreFondo;
			salidas3.valorCuotaAporte = salidaPreliminar.get(k).INGPrecioPromedioCompra;
			salidas3.PromedioAporte = salidaPreliminar.get(k).PPPCustodia;
			salidas3.MONTO_APORTE = salidaPreliminar.get(k).INGMontoPrecioPromedioCompra;
			salidas3.valorActual = salidaPreliminar.get(k).ValorCuota;
			salidas3.categorizacion = "";
			salidas3.MontoMonedaInstrumento = salidaPreliminar.get(k).Monto.add(salidaPreliminar.get(k).MontoGarantia, MathContext.DECIMAL128);
			salidas3.MontoCalculadoMonInstrumento = salidaPreliminar.get(k).CantidadCustodia.add(salidaPreliminar.get(k).CantidadGarantia, MathContext.DECIMAL128).multiply(salidaPreliminar.get(k).ValorCuota, MathContext.DECIMAL128);
			salidas2.add(salidas3);
		};
	  
		
		salidas2.stream().forEach(cartera->{			
			if(cartera.valorCuotaActual != null && cartera.valorCuotaActual.compareTo(BigDecimal.ZERO) != 0 && cartera.PromedioAporte != null && cartera.PromedioAporte.compareTo(BigDecimal.ZERO) != 0) {
				if("CLP".equals(cartera.ID_MONEDA_INSTRUMENTO)) {
					cartera.RENTABILIDAD = cartera.MONTO_VALORIZ
							.divide(cartera.CAN_CUSTODIA.multiply(cartera.PromedioAporte, MathContext.DECIMAL128), MathContext.DECIMAL128)
							.subtract(BigDecimal.ONE)
							.multiply(new BigDecimal("100"));
				}else {
					cartera.RENTABILIDAD = cartera.MontoCalculadoMonInstrumento
							.divide(cartera.CAN_CUSTODIA.multiply(cartera.PromedioAporte, MathContext.DECIMAL128), MathContext.DECIMAL128)
							.subtract(BigDecimal.ONE)
							.multiply(new BigDecimal("100"));
				}												
			} else {
				cartera.RENTABILIDAD = new BigDecimal(0);
			}			
		});
		
		List<HashMap<String,Object>> resultadoCarteraFM = Utils.List2ListHashGenericObject(salidas2);
		
		out.P_OUT_SALIDA.put("carteraFM", resultadoCarteraFM);
		
		log.write("FIN::CarteraFM");
		return out;
	}
	
	private List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> obtenerCustodias(Connection con, EntradaCartola entrada,Dato dato) throws Exception{
		log.write("INICIO::resultadoCarteraFM.obtenerCustodias");
		long inicio = System.currentTimeMillis();
		ConsultarCustodiaPortafolioFondoMutuo consultarCustodiaPortafolioFondoMutuo = new ConsultarCustodiaPortafolioFondoMutuo();
		EntradaConsultarCustodiaPortafolioFondoMutuo entradaConsultarCustodiaPortafolioFondoMutuo = new EntradaConsultarCustodiaPortafolioFondoMutuo();
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdModulo = "FMM";
		entradaConsultarCustodiaPortafolioFondoMutuo.p_FechaConsulta =  Fecha.stringToSqlDate(entrada.p_FechaHasta);
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdProductoAgrupador = null;
		if (dato == null) {
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdCliente = null;
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdPortafolio = null;
		}
		else {
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdCliente = dato.IdCliente;
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdPortafolio = dato.IdPortafolio;
		}
		SalidaConsultarCustodiaPortafolioFondoMutuo salidaConsultarCustodiaPortafolioFondoMutuo = consultarCustodiaPortafolioFondoMutuo.logica(con, entradaConsultarCustodiaPortafolioFondoMutuo);
		List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodia = salidaConsultarCustodiaPortafolioFondoMutuo.P_OUT_TBL;
		
		long fin = System.currentTimeMillis();
		log.write("FIN::resultadoCarteraFM.obtenerCustodias", fin - inicio);
		return custodia;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 82040L;
		Objin.p_FechaDesde = Fecha.toDate("2026-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2026-03-18");
		Dato dato = new Dato();
		dato.IdCliente = 61625L;
		dato.IdPortafolio = 206349L;
		Objin.p_Datos.add(dato);
		CarteraFM  demo= new CarteraFM();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.rollback();
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
