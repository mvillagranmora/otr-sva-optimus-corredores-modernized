package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCobrosCorredorCorresponsal implements JavaCall {
	
	private final String SQL_CONSULTAR_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosCorredorCorresponsal_ConsultarCobros.sql");
	
	ConstantesPlataformaComercial _com = new ConstantesPlataformaComercial();
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Trace
	public ConsultarCobros_Salida logica(Connection conn,ConsultarCobros_Entrada entrada) throws Exception {
		ConsultarCobros_Salida salida = new ConsultarCobros_Salida();
		
		HashMap<Object, Object> hashOperaciones = Utils.List2Hash(entrada.p_Operaciones, "IdInterno");
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		ArrayList<Long> idsCorredorCorresponsal = new ArrayList<Long>();
		for (InformacionOperacion ope : entrada.p_Operaciones) {
			idsCorredorCorresponsal.add(ope.IdCorredorCorresponsal);
		}
		params1.put("idsCorredorCorresponsal", idsCorredorCorresponsal);
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		List<LineaCobroProceso> base = new ArrayList<LineaCobroProceso>();
		base = _sql.query(conn, SQL_CONSULTAR_COBROS, params1, LineaCobroProceso.class);
		
		//CONSOLIDAMOS
		List<LineaCobroProceso> consolidado = new ArrayList<>();
		{
			
			
			for (LineaCobroProceso cobbase : base) {
				boolean found = false;
				for (LineaCobroProceso cobactual : consolidado) {
					if (cobactual.IdCorredorCorresponsal.equals(cobbase.IdCorredorCorresponsal) &&
							(cobactual.IdBolsa.equalsIgnoreCase(cobbase.IdBolsa) || cobbase.IdBolsa.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoProducto.equalsIgnoreCase(cobbase.IdTipoProducto) || cobbase.IdTipoProducto.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoOperacion.equalsIgnoreCase(cobbase.IdTipoOperacion) || cobbase.IdTipoOperacion.equalsIgnoreCase("0")) &&
							(cobactual.IdTipoCanalIngreso.equalsIgnoreCase(cobbase.IdTipoCanalIngreso) || cobbase.IdTipoCanalIngreso.equalsIgnoreCase("0")) && 
							(cobactual.IdCobro.equalsIgnoreCase(cobbase.IdCobro))) {
						
						found = true;
						break;
					}
				}
				if (!found && cobbase.IdEstadoDetallePerfilCobros.equalsIgnoreCase("ACT")) {
					consolidado.add(cobbase);
				}
			}
		}
		
		//FORMAMOS GRUPOS
		HashMap<GrupoCobro,ArrayList<LineaCobroProceso>> cobrosPorGrupo = new HashMap<>();
		for (LineaCobroProceso linea : consolidado) {
			
			GrupoCobro c = new GrupoCobro();
			c.IdBolsa = linea.IdBolsa;
			c.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
			c.IdTipoOperacion = linea.IdTipoOperacion;
			c.IdTipoProducto = linea.IdTipoProducto;
			c.IdCorredorCorresponsal = linea.IdCorredorCorresponsal;
			if (!cobrosPorGrupo.containsKey(c)) {
				cobrosPorGrupo.put(c, new ArrayList<>());
			}
			
			ArrayList<LineaCobroProceso> lineas = cobrosPorGrupo.get(c);
			lineas.add(linea);
			
		}
		
		/*System.out.print("grupos");
		for (GrupoCobro asdf : cobrosPorGrupo.keySet()) {
			System.out.println(asdf.toString());
		}*/
		
		
		//FORMAMOS HASH OPERACION-GRUPOS
		
		HashMap<Long,ArrayList<GrupoCobro>> gruposPorOperacion = new HashMap<>();
		for (InformacionOperacion info : entrada.p_Operaciones) {
			for (GrupoCobro grupo : cobrosPorGrupo.keySet()) {
				
				if (
						(grupo.IdBolsa.equalsIgnoreCase(info.IdBolsa) || grupo.IdBolsa.equalsIgnoreCase("0")) &&
						(grupo.IdCorredorCorresponsal.equals(info.IdCorredorCorresponsal)) &&
						(grupo.IdTipoCanalIngreso.equalsIgnoreCase(info.IdTipoCanalIngreso) || grupo.IdTipoCanalIngreso.equalsIgnoreCase("0")) &&
						(grupo.IdTipoOperacion.equalsIgnoreCase(info.IdTipoOperacion) || grupo.IdTipoOperacion.equalsIgnoreCase("0")) &&
						(grupo.IdTipoProducto.equalsIgnoreCase(info.IdTipoProducto) || grupo.IdTipoProducto.equalsIgnoreCase("0"))
						) {
					
					ArrayList<GrupoCobro> grupos = gruposPorOperacion.get(info.IdInterno);
					if (grupos == null) {
						grupos = new ArrayList<>();
						gruposPorOperacion.put(info.IdInterno, grupos);
					}
					grupos.add(grupo);	
				}
			}
		}
		
		/*for (Long operacion : gruposPorOperacion.keySet()) {
			System.out.print("operacion " + operacion);
			System.out.println(gruposPorOperacion.get(operacion).toString());
		}*/
		
		HashMap<Long,GrupoCobro> mejoresGruposPorOperacion = new HashMap<>();
		for (Long operacion : gruposPorOperacion.keySet()) {
			ArrayList<GrupoCobro> grupos = gruposPorOperacion.get(operacion);
			InformacionOperacion info = (InformacionOperacion) hashOperaciones.get(operacion);
			if (grupos == null) continue;
			
			long mejorPuntaje = 0;
			GrupoCobro mejorGrupo = null;
			
			for (GrupoCobro grupo : grupos) {
				
				//calculamos puntaje
				long puntaje = 0;
				if (info.IdBolsa.equalsIgnoreCase(grupo.IdBolsa)) {
					puntaje = 128;
				}
				else if (grupo.IdBolsa.equalsIgnoreCase("0")) {
					puntaje = 64;
				}
				else {
					puntaje = 0;
				}
				
				if (info.IdTipoProducto.equalsIgnoreCase(grupo.IdTipoProducto)) {
					puntaje = puntaje * 32;
				}
				else if (grupo.IdTipoProducto.equalsIgnoreCase("0")) {
					puntaje = puntaje * 16;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoOperacion.equalsIgnoreCase(grupo.IdTipoOperacion)) {
					puntaje = puntaje * 8;
				}
				else if (grupo.IdTipoOperacion.equalsIgnoreCase("0")) {
					puntaje = puntaje * 4;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoCanalIngreso.equalsIgnoreCase(grupo.IdTipoCanalIngreso)) {
					puntaje = puntaje * 2;
				}
				else if (grupo.IdTipoCanalIngreso.equalsIgnoreCase("0")) {
					puntaje = puntaje * 1;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (puntaje > mejorPuntaje) {
					mejorPuntaje = puntaje;
					mejorGrupo = grupo;
				}
				
			}
			if (mejorGrupo != null)
				mejoresGruposPorOperacion.put(operacion, mejorGrupo);
			
			
		}
		
		for (Long operacion : mejoresGruposPorOperacion.keySet()) {
			GrupoCobro grupo = mejoresGruposPorOperacion.get(operacion);
			ArrayList<LineaCobroProceso> cobros = cobrosPorGrupo.get(grupo);
			for (LineaCobroProceso linea : cobros) {
				LineaCobroProceso cob = new LineaCobroProceso();
				Utils.copyPojo(linea, cob );
				cob.IdInterno = operacion;
				salida.p_out_MatrizOperacional.add(cob);
				
			}
		}
		return salida;
	}
	
	class GrupoCobro {
		public Long	IdCorredorCorresponsal;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		
		@Override
		public boolean equals(Object o) {
			GrupoCobro e = (GrupoCobro)o;
			return (this.IdTipoProducto.equalsIgnoreCase(e.IdTipoProducto)) && 
					(this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion)) &&
					(this.IdBolsa.equalsIgnoreCase(e.IdBolsa)) &&
					(this.IdTipoCanalIngreso.equalsIgnoreCase(e.IdTipoCanalIngreso) &&
					(this.IdCorredorCorresponsal.equals(e.IdCorredorCorresponsal)));
		}
		
		@Override
		public int hashCode() {
			return (this.IdTipoProducto + this.IdTipoOperacion + this.IdTipoCanalIngreso + this.IdBolsa + this.IdCorredorCorresponsal).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class LineaCobroProceso {
		public Long IdInterno;
		public Long IdCorredorCorresponsal;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdValorCobro;
		public String DescripcionValorCobro;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor;
		public Long IdEmpresa;
		public String IdEstadoDetallePerfilCobros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobros_Salida {
		public List<LineaCobroProceso> p_out_MatrizOperacional;
		public List<MensajeSalida> p_out_Mensajes;
		
		public ConsultarCobros_Salida() {
			p_out_MatrizOperacional = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
		}
	}
	
	
	
	public static class ConsultarCobros_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InformacionOperacion> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobros_Entrada() {
			this.p_Operaciones = new ArrayList<InformacionOperacion>();
		}
	}
	
	public static class InformacionOperacion {
		public Long IdInterno;
		public Long IdCorredorCorresponsal;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		ConsultarCobrosCorredorCorresponsal cc = new ConsultarCobrosCorredorCorresponsal();
		ConsultarCobros_Entrada entrada = new ConsultarCobros_Entrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		InformacionOperacion e = new InformacionOperacion();
		e.IdBolsa = "0";
		e.IdInterno = 0L;
		e.IdTipoCanalIngreso = "AGE";
		e.IdTipoOperacion = "VENMIL";
		e.IdTipoProducto = "INMI";
		entrada.p_Operaciones.add(e);
		
		/*e = new InformacionOperacion();
		e.IdBolsa = "BCS";
		e.IdInterno = 1L;
		e.IdPortafolio = 27500L;
		e.IdTipoCanalIngreso = "TRA";
		e.IdTipoOperacion = "COMACN";
		e.IdTipoProducto = "INAC";
		entrada.p_Operaciones.add(e);*/
		ConsultarCobros_Salida a = cc.logica(con, entrada);
		System.out.println(a.toString());
		
	}
	

}