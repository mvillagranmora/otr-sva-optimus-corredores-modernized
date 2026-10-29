package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class GraficoConsolidadoPorProducto implements BcsCartola{
	
	public final Log log = new Log("NOR.GraficoConsolidadoPorProducto");
	
	public static class SalidaGraficoConsolidadoPorProducto{
		public Long IdCliente;
		public Long IdPortafolio;
		public String grupo;
		public BigDecimal totPorcActivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::GraficoConsolidadoPorProducto");
		SalidaCartola out = new SalidaCartola();
		
		List<Dato> datosModifcados = new ArrayList<Dato>();
		
		for(Dato dato : entrada.p_Datos) {
			Dato nDato = new Dato();
			nDato.IdCliente = dato.IdCliente;
			nDato.IdPortafolio = dato.IdPortafolio;
			datosModifcados.add(nDato);
		}
		
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_FechaDesde = entrada.p_FechaDesde;
		entradaResumenInversionBase.p_FechaHasta = entrada.p_FechaHasta;
		entradaResumenInversionBase.p_Datos = datosModifcados;
		
		SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		HashMap<String,SalidaGraficoConsolidadoPorProducto> hSalidaGrafico = new HashMap<String,SalidaGraficoConsolidadoPorProducto>();
		
		for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
			String id = res.IdCliente + "_" + res.IdPortafolio + "_" + res.Grupo;
			SalidaGraficoConsolidadoPorProducto salidaGraficoConsolidadoPorProducto = hSalidaGrafico.get(id);
			if(salidaGraficoConsolidadoPorProducto == null) {
				salidaGraficoConsolidadoPorProducto = new SalidaGraficoConsolidadoPorProducto();
				salidaGraficoConsolidadoPorProducto.IdCliente = res.IdCliente;
				salidaGraficoConsolidadoPorProducto.IdPortafolio = res.IdPortafolio;
				salidaGraficoConsolidadoPorProducto.grupo = res.Grupo;
				salidaGraficoConsolidadoPorProducto.totPorcActivo = res.TotalMonedaSistema;
				hSalidaGrafico.put(id, salidaGraficoConsolidadoPorProducto);
			}
		}
		
		List<SalidaGraficoConsolidadoPorProducto> salidas = new ArrayList<SalidaGraficoConsolidadoPorProducto>();
		
		for(Map.Entry<String, SalidaGraficoConsolidadoPorProducto> obj : hSalidaGrafico.entrySet()){
			salidas.add(obj.getValue());
		}
		
		List<HashMap<String,Object>> resultadoGraficoConsolidadoPorProducto = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("graficoConsolidadoPorProducto", resultadoGraficoConsolidadoPorProducto);
		
		log.write("FIN::GraficoConsolidadoPorProducto");
		return  out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-07-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-07-31");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		GraficoConsolidadoPorProducto  demo= new GraficoConsolidadoPorProducto();				
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
