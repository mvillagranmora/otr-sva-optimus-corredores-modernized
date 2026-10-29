package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ItemDerivados implements BcsCartola{
	
	public final Log log = new Log("NOR.ItemDerivados");
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/ItemDerivados_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/ItemDerivados_SelectCarteraHistorico.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaItemDerivados{
		
		public Long IdCliente;
        public Long IdPortafolio;
        public String grupo;
        public BigDecimal total;
        public String item;
        public BigDecimal monto;
        public BigDecimal totPorcActivo;
        
        public SalidaItemDerivados() {
        	this.monto = BigDecimal.ZERO;
        	this.total = BigDecimal.ZERO;
        	this.totPorcActivo = BigDecimal.ZERO;
        }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Cartera{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long Folio;
		public String MonedaRecibe;
		public String MonedaEntrega;
		public String Tipo;
		public String Grupo;
		public String Item;
		public BigDecimal Monto;
		public String Moneda;
		public BigDecimal ValorRazonableActivo;
		public BigDecimal ValorRazonablePasivo;
		public BigDecimal TipoCambioMercado;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioMercadoSecundaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::ItemDerivados");
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
		
		List<Cartera> carteras = new ArrayList<Cartera>();
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			carteras.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL, params, Cartera.class));
		}else {
			params.put("FechaHasta", entrada.p_FechaHasta);
			carteras.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO, params, Cartera.class));
		}
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		HashMap<String,BigDecimal> totalGrupos = new HashMap<String,BigDecimal>();
		
		
		//TODO: Se debe incorporar derivados
		for(Cartera cartera : carteras) {
			BigDecimal total = totalGrupos.get(cartera.IdPortafolio + "_" + cartera.Grupo);
			if(total == null) total = BigDecimal.ZERO;
			if("FW".equals(cartera.Grupo)) {
				BigDecimal resultadoNeto = cartera.ValorRazonableActivo.subtract(cartera.ValorRazonablePasivo, MathContext.DECIMAL128).negate();
				total = total.add(resultadoNeto, MathContext.DECIMAL128);
			}else {
				total = total.add(cartera.Monto, MathContext.DECIMAL128);
			}
			totalGrupos.put(cartera.IdPortafolio + "_" + cartera.Grupo, total);
		}
		
		HashMap<String,SalidaItemDerivados> hSalidas = new HashMap<String,SalidaItemDerivados>();
		
		
		for(Cartera cartera : carteras) {
			SalidaItemDerivados salida = hSalidas.get(cartera.IdPortafolio + "_" + cartera.Grupo + "_" + cartera.Item);
			
			if(salida == null) {
				salida = new SalidaItemDerivados();
				salida.IdCliente = cartera.IdCliente;
				salida.IdPortafolio = cartera.IdPortafolio;
				salida.grupo = cartera.Grupo;
				salida.item = cartera.Item;
				BigDecimal total = totalGrupos.get(cartera.IdPortafolio + "_" + cartera.Grupo);
				if(total != null) salida.total = total;
			}
			
			if("Forward".equals(cartera.Item)) {
				BigDecimal resultadoNeto = cartera.ValorRazonableActivo.subtract(cartera.ValorRazonablePasivo, MathContext.DECIMAL128).negate();
				salida.monto = salida.monto.add(resultadoNeto, MathContext.DECIMAL128);
			}else {
				salida.monto = salida.monto.add(cartera.Monto, MathContext.DECIMAL128);
			}
			
			hSalidas.put(cartera.IdPortafolio + "_" + cartera.Grupo + "_" + cartera.Item, salida);
		}
		
		List<SalidaItemDerivados> salidas = new ArrayList<SalidaItemDerivados>();
		
		for(Map.Entry<String, SalidaItemDerivados> obj : hSalidas.entrySet()) {
			salidas.add(obj.getValue());
		}
		
		List<HashMap<String,Object>> resultadoItemDerivados = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("itemDerivados", resultadoItemDerivados);
		
		log.write("FIN::ItemDerivados");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-12-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-12-27");
		Dato dato = new Dato();
		dato.IdCliente = 18145L;
		dato.IdPortafolio = 24144L;
		Objin.p_Datos.add(dato);
		ItemDerivados demo= new ItemDerivados();				
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
