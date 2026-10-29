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

public class GraficoActivoPorProducto implements BcsCartola{
	
	public final Log log = new Log("NOR.GraficoActivoPorProducto");
	
	public static class SalidaGraficoActivoPorProducto{
		public Long IdCliente;
		public Long IdPortafolio;
		public String grupo;
		public BigDecimal totPorcActivo;
		
		public String toString() {
			return Utils.printJson(this, false);
			
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::GraficoActivoPorProducto");
		SalidaCartola out = new SalidaCartola();
				
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_FechaDesde = entrada.p_FechaDesde;
		entradaResumenInversionBase.p_FechaHasta = entrada.p_FechaHasta;
		entradaResumenInversionBase.p_Datos = entrada.p_Datos;
		
		SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		HashMap<String,SalidaGraficoActivoPorProducto> hSalidaGrafico = new HashMap<String,SalidaGraficoActivoPorProducto>();
		
		for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
			String id = res.IdCliente + "_" + res.IdPortafolio + "_" + res.Grupo;
			SalidaGraficoActivoPorProducto salidaGraficoActivoPorProducto = hSalidaGrafico.get(id);
			if(salidaGraficoActivoPorProducto == null) {
				salidaGraficoActivoPorProducto = new SalidaGraficoActivoPorProducto();
				salidaGraficoActivoPorProducto.IdCliente = res.IdCliente;
				salidaGraficoActivoPorProducto.IdPortafolio = res.IdPortafolio;
				salidaGraficoActivoPorProducto.grupo = res.Grupo;
				salidaGraficoActivoPorProducto.totPorcActivo = res.TotalMonedaSistema;
				hSalidaGrafico.put(id, salidaGraficoActivoPorProducto);
			}
		}
		
		List<SalidaGraficoActivoPorProducto> salidas = new ArrayList<SalidaGraficoActivoPorProducto>();
		
		for(Map.Entry<String, SalidaGraficoActivoPorProducto> obj : hSalidaGrafico.entrySet()){
			salidas.add(obj.getValue());
		}
		
		List<HashMap<String,Object>> resultadoGraficoActivoPorProducto = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("graficoActivoPorProducto", resultadoGraficoActivoPorProducto);
		
		log.write("FIN::GraficoActivoPorProducto");
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
		GraficoActivoPorProducto  demo= new GraficoActivoPorProducto();				
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
