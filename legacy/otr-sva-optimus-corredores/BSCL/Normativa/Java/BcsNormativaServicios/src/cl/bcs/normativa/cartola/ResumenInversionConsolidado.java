package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

public class ResumenInversionConsolidado implements BcsCartola{
	
	static final Logger logger = LogManager.getLogger(ResumenInversionConsolidado.class);
	
	public static class AgrupacionResumenInversion{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal MontoMonedaAdicional;
		HashMap<String,AgrupacionResumenInversionItem> AgrupacionesResumenInversionItem;
		
		public AgrupacionResumenInversion() {
			this.MontoMonedaSistema = BigDecimal.ZERO;
			this.MontoMonedaAdicional = BigDecimal.ZERO;
			this.AgrupacionesResumenInversionItem = new HashMap<String,AgrupacionResumenInversionItem>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public static class AgrupacionResumenInversionItem{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Grupo;
		public String Item;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal TotalMonedaSistema;
		public String IdMonedaAdicional;
		public BigDecimal MontoMonedaAdicional;
		public BigDecimal TotalMonedaAdicional;
		
		public AgrupacionResumenInversionItem() {
			this.MontoMonedaSistema = BigDecimal.ZERO;
			this.MontoMonedaAdicional = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public static class SalidaResumenInversionConsolidado{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Grupo;
		public String Item;
		public String IdMonedaSistema;
		public BigDecimal PorcentajeActivo;
		public BigDecimal PorcentajeTotalActivos;
		public BigDecimal ActivoMontoMonedaSistema;
		public BigDecimal PasivoMontoMonedaSistema;
		public BigDecimal ActivoTotalMonedaSistema;
		public BigDecimal PasivoTotalMonedaSistema;
		public String TituloResumenInversionSistema;
		public String IdMonedaAdicional;
		public BigDecimal ActivoMontoMonedaAdicional;
		public BigDecimal PasivoMontoMonedaAdicional;
		public BigDecimal ActivoTotalMonedaAdicional;
		public BigDecimal PasivoTotalMonedaAdicional;
		public String TituloResumenInversionAdicional;
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.debug("INICIO::ResumenInversionConsolidado");
		SalidaCartola out = new SalidaCartola();
		
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_FechaDesde = entrada.p_FechaDesde;
		entradaResumenInversionBase.p_FechaHasta = entrada.p_FechaHasta;
		entradaResumenInversionBase.p_Datos = entrada.p_Datos;
		
		SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		HashMap<String,AgrupacionResumenInversion> hAgrupacionResumenInversion = new HashMap<String,AgrupacionResumenInversion>();
		
		logger.debug("ResumenInversionConsolidado::", salidaResumenInversionBase.p_out_ResumenInversion.size());
		
		for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
			String idAgrupacionResumenInversion = res.IdCliente + "_" + res.IdPortafolio;
			String idAgrupacionResumenInversionItem = res.IdCliente + "_" + res.IdPortafolio + res.Grupo + "_" + res.Item;
			
			AgrupacionResumenInversion agrupacionResumenInversion = hAgrupacionResumenInversion.get(idAgrupacionResumenInversion);
			
			if(agrupacionResumenInversion == null) {
				agrupacionResumenInversion = new AgrupacionResumenInversion();
				agrupacionResumenInversion.IdCliente = res.IdCliente;
				agrupacionResumenInversion.IdPortafolio = res.IdPortafolio;
				agrupacionResumenInversion.IdMonedaSistema = res.IdMonedaSistema;
			}
			
			agrupacionResumenInversion.MontoMonedaSistema = agrupacionResumenInversion.MontoMonedaSistema.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
			agrupacionResumenInversion.MontoMonedaAdicional = agrupacionResumenInversion.MontoMonedaAdicional.add(res.MontoMonedaAdicional, MathContext.DECIMAL128);
			
			AgrupacionResumenInversionItem agrupacionResumenInversionItem = agrupacionResumenInversion.AgrupacionesResumenInversionItem.get(idAgrupacionResumenInversionItem);
			
			if(agrupacionResumenInversionItem == null) {
				agrupacionResumenInversionItem = new AgrupacionResumenInversionItem();
				agrupacionResumenInversionItem.IdCliente = res.IdCliente;
				agrupacionResumenInversionItem.IdPortafolio = res.IdPortafolio;
				agrupacionResumenInversionItem.Grupo = res.Grupo;
				agrupacionResumenInversionItem.Item = res.Item;
				agrupacionResumenInversionItem.IdMonedaSistema = res.IdMonedaSistema;
				agrupacionResumenInversionItem.IdMonedaAdicional = res.IdMonedaAdicional;
				agrupacionResumenInversionItem.TotalMonedaSistema = res.TotalMonedaSistema;
				agrupacionResumenInversionItem.TotalMonedaAdicional = res.TotalMonedaAdicional;
			}
			
			agrupacionResumenInversionItem.MontoMonedaSistema = agrupacionResumenInversionItem.MontoMonedaSistema.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
			agrupacionResumenInversionItem.MontoMonedaAdicional = agrupacionResumenInversionItem.MontoMonedaAdicional.add(res.MontoMonedaAdicional, MathContext.DECIMAL128);
			
			hAgrupacionResumenInversion.put(idAgrupacionResumenInversion, agrupacionResumenInversion);
			agrupacionResumenInversion.AgrupacionesResumenInversionItem.put(idAgrupacionResumenInversionItem, agrupacionResumenInversionItem);
			
		}
		
		logger.debug("ResumenInversionConsolidado::TERMINO DE AGRUPAR");
		
		List<SalidaResumenInversionConsolidado> salidas = new ArrayList<SalidaResumenInversionConsolidado>();
		
		for(Map.Entry<String, AgrupacionResumenInversion> obj : hAgrupacionResumenInversion.entrySet()) {
			AgrupacionResumenInversion res = obj.getValue();
			for(Map.Entry<String, AgrupacionResumenInversionItem> objItem : res.AgrupacionesResumenInversionItem.entrySet()) {
				AgrupacionResumenInversionItem resItem = objItem.getValue();
				SalidaResumenInversionConsolidado salidaResumen = new SalidaResumenInversionConsolidado();
				salidaResumen.IdCliente = resItem.IdCliente;
				salidaResumen.IdPortafolio = resItem.IdPortafolio;
				salidaResumen.Grupo = resItem.Grupo;
				salidaResumen.Item = resItem.Item;
				salidaResumen.IdMonedaSistema = resItem.IdMonedaSistema;
				if(res.MontoMonedaSistema.compareTo(BigDecimal.ZERO) == 1 ) {
					salidaResumen.PorcentajeActivo = resItem.MontoMonedaSistema.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(res.MontoMonedaSistema, MathContext.DECIMAL128);
					salidaResumen.PorcentajeTotalActivos = resItem.TotalMonedaSistema.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(res.MontoMonedaSistema, MathContext.DECIMAL128);
				}else {
					salidaResumen.PorcentajeActivo = BigDecimal.ZERO;
					salidaResumen.PorcentajeTotalActivos = BigDecimal.ZERO;
				}
				
				salidaResumen.ActivoMontoMonedaSistema = resItem.MontoMonedaSistema.compareTo(BigDecimal.ZERO) == 1 ? resItem.MontoMonedaSistema : BigDecimal.ZERO;
				salidaResumen.PasivoMontoMonedaSistema = resItem.MontoMonedaSistema.compareTo(BigDecimal.ZERO) == -1 ? resItem.MontoMonedaSistema : BigDecimal.ZERO;
				salidaResumen.ActivoTotalMonedaSistema = resItem.TotalMonedaSistema.compareTo(BigDecimal.ZERO) == 1 ? resItem.TotalMonedaSistema : BigDecimal.ZERO;
				salidaResumen.PasivoTotalMonedaSistema = resItem.TotalMonedaSistema.compareTo(BigDecimal.ZERO) == -1 ? resItem.TotalMonedaSistema : BigDecimal.ZERO;
				salidaResumen.TituloResumenInversionSistema = "En " + resItem.IdMonedaSistema + ", a valor de mercado";
				salidaResumen.IdMonedaAdicional = resItem.IdMonedaAdicional;
				salidaResumen.ActivoMontoMonedaAdicional = resItem.MontoMonedaAdicional.compareTo(BigDecimal.ZERO) == 1 ? resItem.MontoMonedaAdicional : BigDecimal.ZERO;
				salidaResumen.PasivoMontoMonedaAdicional = resItem.MontoMonedaAdicional.compareTo(BigDecimal.ZERO) == -1 ? resItem.MontoMonedaAdicional : BigDecimal.ZERO;
				salidaResumen.ActivoTotalMonedaAdicional = resItem.TotalMonedaAdicional.compareTo(BigDecimal.ZERO) == 1 ? resItem.TotalMonedaAdicional : BigDecimal.ZERO;
				salidaResumen.PasivoTotalMonedaAdicional = resItem.TotalMonedaAdicional.compareTo(BigDecimal.ZERO) == -1 ? resItem.TotalMonedaAdicional : BigDecimal.ZERO;
				salidaResumen.TituloResumenInversionAdicional = "En " + resItem.IdMonedaAdicional + ", a valor de mercado";
				salidas.add(salidaResumen);
			}
		}
		
		salidas.sort(new Comparator<ResumenInversionConsolidado.SalidaResumenInversionConsolidado>() {
		    @Override
		    public int compare(ResumenInversionConsolidado.SalidaResumenInversionConsolidado mov1, ResumenInversionConsolidado.SalidaResumenInversionConsolidado mov2) {
		        if(mov1.Grupo.equals(mov2.Grupo)){
		            return 0;
		        }
		        return mov1.Grupo.compareTo(mov2.Grupo);
		     }
		});
		
		//SE AGREGAN LOS TODOS
		for(Map.Entry<String, AgrupacionResumenInversion> obj : hAgrupacionResumenInversion.entrySet()) {
			AgrupacionResumenInversion res = obj.getValue();
			SalidaResumenInversionConsolidado salidaResumen = new SalidaResumenInversionConsolidado();
			salidaResumen.IdCliente = res.IdCliente;
			salidaResumen.IdPortafolio = 0L;
			salidaResumen.Grupo = "TOTAL";
			salidaResumen.Item = "";
			salidaResumen.IdMonedaSistema = "";
			salidaResumen.PorcentajeActivo = BigDecimal.ZERO;
			salidaResumen.PorcentajeTotalActivos = BigDecimal.ZERO;
			salidaResumen.ActivoMontoMonedaSistema = BigDecimal.ZERO;
			salidaResumen.PasivoMontoMonedaSistema = BigDecimal.ZERO;
			salidaResumen.ActivoTotalMonedaSistema = res.MontoMonedaSistema;
			salidaResumen.PasivoTotalMonedaSistema = BigDecimal.ZERO;
			salidaResumen.TituloResumenInversionSistema = "";
			salidaResumen.IdMonedaAdicional = "";
			salidaResumen.ActivoMontoMonedaAdicional = BigDecimal.ZERO;
			salidaResumen.PasivoMontoMonedaAdicional = BigDecimal.ZERO;
			salidaResumen.ActivoTotalMonedaAdicional = res.MontoMonedaAdicional;
			salidaResumen.PasivoTotalMonedaAdicional = BigDecimal.ZERO;
			salidaResumen.TituloResumenInversionAdicional = "";
			salidas.add(salidaResumen);
		}
		
		List<HashMap<String,Object>> resultadoResumenInversionConsolidado = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("resumenInversionConsolidado", resultadoResumenInversionConsolidado);
		logger.debug("FIN::ResumenInversionConsolidado");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-08-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-08-03");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		ResumenInversionConsolidado  demo= new ResumenInversionConsolidado();				
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
