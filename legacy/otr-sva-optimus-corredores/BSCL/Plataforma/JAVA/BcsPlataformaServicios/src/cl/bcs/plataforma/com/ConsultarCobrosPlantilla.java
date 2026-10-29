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

public class ConsultarCobrosPlantilla implements JavaCall {
	
	private final String SQL_CONSULTAR_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosPlantilla_ConsultarCobros.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ConsultarCobrosPlantilla_Salida logica(Connection conn,ConsultarCobrosPlantilla_Entrada entrada) throws Exception {
		ConsultarCobrosPlantilla_Salida salida = new ConsultarCobrosPlantilla_Salida();
		
		HashMap<Object, Object> hashOperaciones = Utils.List2Hash(entrada.p_Operaciones, "IdInterno");
		long x0 = System.currentTimeMillis();
		SQL _sql = new SQL();
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		HashMap<String,Object> params2 = new HashMap<String,Object>();
		ArrayList<String> idsPerfilCobros = new ArrayList<String>();
		ArrayList<String> idsPerfilCobrosExtra = new ArrayList<String>();
		for (InformacionOperacion ope : entrada.p_Operaciones) {
			if (!idsPerfilCobros.contains(ope.IdPerfilCobros))
				idsPerfilCobros.add(ope.IdPerfilCobros);
			if (!idsPerfilCobros.contains(ope.IdPerfilCobrosExtra))
				idsPerfilCobrosExtra.add(ope.IdPerfilCobrosExtra);
		}
		params1.put("idsPerfilCobros", idsPerfilCobros);
		params1.put("idEmpresa", entrada.p_IdEmpresa);
		
		params2.put("idsPerfilCobros", idsPerfilCobrosExtra);
		params2.put("idEmpresa", entrada.p_IdEmpresa);
		List<LineaCobroProceso> _base = _sql.query(conn, SQL_CONSULTAR_COBROS, params1, LineaCobroProceso.class);
		List<LineaCobroProceso> _extra = _sql.query(conn, SQL_CONSULTAR_COBROS, params2, LineaCobroProceso.class);
		
		List<LineaCobroProceso> base = new ArrayList<>();
		List<LineaCobroProceso> extra = new ArrayList<>();
		
		System.out.println("tiempo query " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		for (LineaCobroProceso cob : _base) {
			for (InformacionOperacion ope : entrada.p_Operaciones) {
				if (cob.IdPerfilCobros.equalsIgnoreCase(ope.IdPerfilCobros)) {
					LineaCobroProceso c1 = new LineaCobroProceso();
					Utils.copyPojo(cob, c1);
					c1.IdInterno = ope.IdInterno;
					base.add(c1);
				}
			}
		}
		
		for (LineaCobroProceso cob : _extra) {
			for (InformacionOperacion ope : entrada.p_Operaciones) {
				if (cob.IdPerfilCobros.equalsIgnoreCase(ope.IdPerfilCobrosExtra)) {
					LineaCobroProceso c1 = new LineaCobroProceso();
					Utils.copyPojo(cob, c1);
					c1.IdInterno = ope.IdInterno;
					extra.add(c1);
				}
			}
		}
		
		
		//FORMAMOS GRUPOS CON LOS EXTRAS
		System.out.println("tiempo paso 1 " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		HashMap<GrupoCobro,ArrayList<LineaCobroProceso>> cobrosPorGrupo = new HashMap<>();
		for (LineaCobroProceso cobextra : extra) {
			if (!cobextra.IdEstadoDetallePerfilCobros.equalsIgnoreCase("ACT"))
				continue;
			GrupoCobro c = new GrupoCobro();
			c.IdBolsa = cobextra.IdBolsa;
			c.IdTipoCanalIngreso = cobextra.IdTipoCanalIngreso;
			c.IdTipoOperacion = cobextra.IdTipoOperacion;
			c.IdTipoProducto = cobextra.IdTipoProducto;
			c.IdInterno = cobextra.IdInterno;
			if (!cobrosPorGrupo.containsKey(c)) {
				cobrosPorGrupo.put(c, new ArrayList<>());
			}
			
			ArrayList<LineaCobroProceso> lineas = cobrosPorGrupo.get(c);
			lineas.add(cobextra);
			
		}
		
		
		//FORMAMOS GRUPOS CON LOS BASE
		System.out.println("tiempo paso 2 grupos extras " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		for (LineaCobroProceso cobbase : base) {
			if (!cobbase.IdEstadoDetallePerfilCobros.equalsIgnoreCase("ACT"))
				continue;
			GrupoCobro c = new GrupoCobro();
			c.IdBolsa = cobbase.IdBolsa;
			c.IdTipoCanalIngreso = cobbase.IdTipoCanalIngreso;
			c.IdTipoOperacion = cobbase.IdTipoOperacion;
			c.IdTipoProducto = cobbase.IdTipoProducto;
			c.IdInterno = cobbase.IdInterno;
			if (!cobrosPorGrupo.containsKey(c)) {
				cobrosPorGrupo.put(c, new ArrayList<>());
			}
			
			ArrayList<LineaCobroProceso> lineas = cobrosPorGrupo.get(c);
			
			boolean found = false;
			for (LineaCobroProceso cobactual : lineas) {
				if (cobactual.IdInterno.equals(cobbase.IdInterno) &&
						(cobactual.IdBolsa.equalsIgnoreCase(cobbase.IdBolsa) || cobbase.IdBolsa.equalsIgnoreCase("0")) &&
						(cobactual.IdTipoProducto.equalsIgnoreCase(cobbase.IdTipoProducto) || cobbase.IdTipoProducto.equalsIgnoreCase("0")) &&
						(cobactual.IdTipoOperacion.equalsIgnoreCase(cobbase.IdTipoOperacion) || cobbase.IdTipoOperacion.equalsIgnoreCase("0")) &&
						(cobactual.IdTipoCanalIngreso.equalsIgnoreCase(cobbase.IdTipoCanalIngreso) || cobbase.IdTipoCanalIngreso.equalsIgnoreCase("0")) && 
						(cobactual.IdCobro.equalsIgnoreCase(cobbase.IdCobro))) {
					
					found = true;
					break;
				}
			}
			if (!found) {
				lineas.add(cobbase);
			}

			
		}
		

		
		/*System.out.print("grupos");
		for (GrupoCobro asdf : cobrosPorGrupo.keySet()) {
			System.out.println(asdf.toString());
		}*/
		
		
		//FORMAMOS HASH OPERACION-GRUPOS ***
		System.out.println("tiempo paso 3 grupos base " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		HashMap<Long,ArrayList<GrupoCobro>> gruposPorOperacion = new HashMap<>();
		for (InformacionOperacion info : entrada.p_Operaciones) {
			for (GrupoCobro grupo : cobrosPorGrupo.keySet()) {
				
				if (
						(grupo.IdBolsa.equalsIgnoreCase(info.IdBolsa) || grupo.IdBolsa.equalsIgnoreCase("0")) &&
						(grupo.IdInterno.equals(info.IdInterno)) &&
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
		System.out.println("tiempo paso 4 hash operacion grupos " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
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
		System.out.println("tiempo paso 5 encontrar mejor grupo " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
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
		public Long	IdInterno;
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
					(this.IdInterno.equals(e.IdInterno)));
		}
		
		@Override
		public int hashCode() {
			return (this.IdTipoProducto + this.IdTipoOperacion + this.IdTipoCanalIngreso + this.IdBolsa + this.IdInterno).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class LineaCobroProceso {
		public Long IdInterno;
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
		
		@Override
		public int hashCode() {
			return (this.IdCobro + this.IdTipoProducto + this.IdTipoOperacion + this.IdTipoCanalIngreso + this.IdBolsa + this.IdInterno).hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			LineaCobroProceso e = (LineaCobroProceso)o;
			return (this.IdTipoProducto.equalsIgnoreCase(e.IdTipoProducto)) && 
					(this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion)) &&
					(this.IdCobro.equalsIgnoreCase(e.IdCobro)) &&
					(this.IdBolsa.equalsIgnoreCase(e.IdBolsa)) &&
					(this.IdTipoCanalIngreso.equalsIgnoreCase(e.IdTipoCanalIngreso) &&
					(this.IdInterno.equals(e.IdInterno)));
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobrosPlantilla_Salida {
		public List<LineaCobroProceso> p_out_MatrizOperacional;
		public List<MensajeSalida> p_out_Mensajes;
		
		public ConsultarCobrosPlantilla_Salida() {
			p_out_MatrizOperacional = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class ConsultarCobrosPlantilla_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InformacionOperacion> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobrosPlantilla_Entrada() {
			this.p_Operaciones = new ArrayList<InformacionOperacion>();
		}
	}
	
	public static class InformacionOperacion {
		public Long IdInterno;
		public String IdPerfilCobros;
		public String IdPerfilCobrosExtra;
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
		ConsultarCobrosPlantilla cc = new ConsultarCobrosPlantilla();
		ConsultarCobrosPlantilla_Entrada entrada = new ConsultarCobrosPlantilla_Entrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		InformacionOperacion e = new InformacionOperacion();
		e.IdBolsa = "0";
		e.IdInterno = 0L;
		e.IdPerfilCobros = "TODOS LOS COBROSH";
		e.IdPerfilCobrosExtra = "EXT_343710";
		e.IdInterno = 1L;
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
		ConsultarCobrosPlantilla_Salida a = cc.logica(con, entrada);
		System.out.println(a.toString());
		
	}
	

}