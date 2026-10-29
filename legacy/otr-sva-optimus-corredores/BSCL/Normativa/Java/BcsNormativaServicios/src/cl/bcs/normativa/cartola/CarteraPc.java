package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraPc implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraPactos");
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraPc_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraPc_SelectCarteraHistorico.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class CarteraPactos{
		public Long IdCliente;
		public Long IdPortafolio;
		public String operacion;
		public String moneda;
		public BigDecimal SEC_COMPROM;
		public BigDecimal plazo;
		public BigDecimal TASA_COMPROM;
		public String operacionContado;
		public Timestamp fechaContado;
		public BigDecimal montoContratadoContado;
		public BigDecimal equivClpContado;
		public String operacionPlazo;
		public Timestamp fechaPlazo;
		public BigDecimal montoContratadoPlazo;
		public BigDecimal equivClpPlazo;
		public BigDecimal VALOR_PTE;
		public BigDecimal equivClpValPresen;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraPactos{
		public Long IdCliente;
		public Long IdPortafolio;
		public String operacion;
		public String moneda;
		public BigDecimal SEC_COMPROM;
		public BigDecimal plazo;
		public BigDecimal TASA_COMPROM;
		public String operacionContado;
		public Timestamp fechaContado;
		public BigDecimal montoContratadoContado;
		public BigDecimal equivClpContado;
		public String operacionPlazo;
		public Timestamp fechaPlazo;
		public BigDecimal montoContratadoPlazo;
		public BigDecimal equivClpPlazo;
		public BigDecimal VALOR_PTE;
		public BigDecimal equivClpValPresen;
		public BigDecimal equivClpCompra;
		public BigDecimal equivClpVenta;
		public BigDecimal equivPresenClpCompra;
		public BigDecimal equivPresenClpVenta;
		public Integer CantVenta;
		public Integer cantCompra;
		
		public SalidaCarteraPactos() {
			this.CantVenta = 0;
			this.cantCompra = 0;
			this.equivClpCompra = BigDecimal.ZERO;
			this.equivClpVenta = BigDecimal.ZERO;
			this.equivPresenClpCompra = BigDecimal.ZERO;
			this.equivPresenClpVenta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Total{
		public Integer CantVenta;
		public Integer CantCompra;
		public BigDecimal TotalPlazoCompra;
		public BigDecimal TotalPlazoVenta;
		public BigDecimal TotalPresenteCompra;
		public BigDecimal TotalPresenteVenta;
		
		public Total() {
			this.CantVenta = 0;
			this.CantCompra = 0;
			this.TotalPlazoCompra = BigDecimal.ZERO;
			this.TotalPlazoVenta = BigDecimal.ZERO;
			this.TotalPresenteCompra = BigDecimal.ZERO;
			this.TotalPresenteVenta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::CarteraPactos");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		List<Long> portafolios = new ArrayList<Long>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
			
		}
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		List<CarteraPactos> carterasPacto = new ArrayList<CarteraPactos>();
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			carterasPacto.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL, params, CarteraPactos.class));
		}else {
			params.put("FechaHasta", entrada.p_FechaHasta);
			carterasPacto.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO, params, CarteraPactos.class));
		}
		
		HashMap<String,Total> hTotales = new HashMap<String,Total>(); 
		
		for(CarteraPactos carteraPacto : carterasPacto) {
			Total total = hTotales.get(carteraPacto.IdCliente + "_" + carteraPacto.IdPortafolio);
			if(total == null) total = new Total();
			if("Venta Contado".equals(carteraPacto.operacionContado)) {
				total.CantVenta++;
				total.TotalPlazoVenta = total.TotalPlazoVenta.add(carteraPacto.equivClpPlazo,MathContext.DECIMAL128);
				total.TotalPresenteVenta = total.TotalPresenteVenta.add(carteraPacto.equivClpValPresen,MathContext.DECIMAL128);
			}
			if("Compra Contado".equals(carteraPacto.operacionContado)) {
				total.CantCompra++;
				total.TotalPlazoCompra = total.TotalPlazoCompra.add(carteraPacto.equivClpPlazo,MathContext.DECIMAL128);
				total.TotalPresenteCompra = total.TotalPresenteCompra.add(carteraPacto.equivClpValPresen,MathContext.DECIMAL128);
			}
			hTotales.put(carteraPacto.IdCliente + "_" + carteraPacto.IdPortafolio, total);
		}
		
		List<SalidaCarteraPactos> salidas = new ArrayList<SalidaCarteraPactos>();
		for(CarteraPactos carteraPacto : carterasPacto) {
			SalidaCarteraPactos salida = new SalidaCarteraPactos();
			salida = Utils.copyPojo(carteraPacto, salida);
			Total total = hTotales.get(carteraPacto.IdCliente + "_" + carteraPacto.IdPortafolio);
			if(total == null) total = new Total();
			salida.cantCompra = total.CantCompra;
			salida.CantVenta = total.CantVenta;
			salida.equivClpCompra = total.TotalPlazoCompra;
			salida.equivClpVenta = total.TotalPlazoVenta;
			salida.equivPresenClpCompra = total.TotalPresenteCompra;
			salida.equivPresenClpVenta = total.TotalPresenteVenta;
			salidas.add(salida);
		}
		
		List<HashMap<String,Object>> resultadoCarteraPactos = Utils.List2ListHashGenericObject(salidas);
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		out.P_OUT_SALIDA.put("carteraPactos", resultadoCarteraPactos);
		
		log.write("FIN::CarteraPactos");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 6L;
		//Objin.p_FechaDesde = Fecha.toDate("2018-09-01");
		//Objin.p_FechaHasta = Fecha.toDate("2018-09-28");
		Objin.p_FechaDesde = Fecha.toDate("2018-10-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-10-09");
		Dato dato = new Dato();
		///dato.IdCliente = 524891L;
		//dato.IdPortafolio = 512341L;
		dato.IdCliente = 1222267L;
		dato.IdPortafolio = 1418747L;  //[{IdCliente: 1222267, IdPortafolio: 1418747}]
		Objin.p_Datos.add(dato);
		CarteraPc demo= new CarteraPc();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
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