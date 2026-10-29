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

public class ConsultarCobrosPlantillaPeru implements JavaCall {
	
	private final String SQL_CONSULTAR_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosPlantillaPeru_ConsultarCobros.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ConsultarCobrosPlantillaPeru_Salida logica(Connection conn,ConsultarCobrosPlantillaPeru_Entrada entrada) throws Exception {
		ConsultarCobrosPlantillaPeru_Salida salida = new ConsultarCobrosPlantillaPeru_Salida();
		
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
		List<DefinicionCobro> _base = _sql.query(conn, SQL_CONSULTAR_COBROS, params1, DefinicionCobro.class);
		List<DefinicionCobro> _extra = _sql.query(conn, SQL_CONSULTAR_COBROS, params2, DefinicionCobro.class);
		
		List<DefinicionCobro> base = new ArrayList<>();
		List<DefinicionCobro> extra = new ArrayList<>();
		
		System.out.println("tiempo query " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		for (DefinicionCobro cob : _base) {
			for (InformacionOperacion ope : entrada.p_Operaciones) {
				if (cob.IdPerfilCobros.equalsIgnoreCase(ope.IdPerfilCobros)) {
					DefinicionCobro c1 = new DefinicionCobro();
					Utils.copyPojo(cob, c1);
					c1.IdInterno = ope.IdInterno;
					base.add(c1);
				}
			}
		}
		
		for (DefinicionCobro cob : _extra) {
			for (InformacionOperacion ope : entrada.p_Operaciones) {
				if (cob.IdPerfilCobros.equalsIgnoreCase(ope.IdPerfilCobrosExtra)) {
					DefinicionCobro c1 = new DefinicionCobro();
					Utils.copyPojo(cob, c1);
					c1.IdInterno = ope.IdInterno;
					extra.add(c1);
				}
			}
		}
		
		
		//FORMAMOS GRUPOS CON LOS EXTRAS
		System.out.println("tiempo paso 1 " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		HashMap<GrupoCobro,ArrayList<DefinicionCobro>> cobrosPorGrupo = new HashMap<>();
		for (DefinicionCobro cobextra : extra) {
			if (!cobextra.IdEstado.equalsIgnoreCase("ACT"))
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
			
			ArrayList<DefinicionCobro> lineas = cobrosPorGrupo.get(c);
			lineas.add(cobextra);
			
		}
		
		
		//FORMAMOS GRUPOS CON LOS BASE
		System.out.println("tiempo paso 2 grupos extras " + (System.currentTimeMillis() - x0));
		x0 = System.currentTimeMillis();
		
		for (DefinicionCobro cobbase : base) {
			if (!cobbase.IdEstado.equalsIgnoreCase("ACT"))
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
			
			ArrayList<DefinicionCobro> lineas = cobrosPorGrupo.get(c);
			
			boolean found = false;
			for (DefinicionCobro cobactual : lineas) {
				if (cobactual.IdInterno.equals(cobbase.IdInterno) &&
						(cobactual.IdBolsa == null || cobactual.IdBolsa.equalsIgnoreCase(cobbase.IdBolsa)) &&
						(cobactual.IdTipoProducto == null || cobactual.IdTipoProducto.equalsIgnoreCase(cobbase.IdTipoProducto)) &&
						(cobactual.IdTipoOperacion == null || cobactual.IdTipoOperacion.equalsIgnoreCase(cobbase.IdTipoOperacion)) &&
						(cobactual.IdTipoCanalIngreso == null || cobactual.IdTipoCanalIngreso.equalsIgnoreCase(cobbase.IdTipoCanalIngreso)) && 
						(cobactual.CodigoDefinicionCobro.equalsIgnoreCase(cobbase.CodigoDefinicionCobro))) {
					
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
						(grupo.IdBolsa == null || grupo.IdBolsa.equalsIgnoreCase(info.IdBolsa)) &&
						(grupo.IdInterno.equals(info.IdInterno)) &&
						(grupo.IdTipoCanalIngreso == null || grupo.IdTipoCanalIngreso.equalsIgnoreCase(info.IdTipoCanalIngreso)) &&
						(grupo.IdTipoOperacion == null || grupo.IdTipoOperacion.equalsIgnoreCase(info.IdTipoOperacion)) &&
						(grupo.IdTipoProducto == null || grupo.IdTipoProducto.equalsIgnoreCase(info.IdTipoProducto))
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
				else if (grupo.IdBolsa == null) {
					puntaje = 64;
				}
				else {
					puntaje = 0;
				}
				
				if (info.IdTipoProducto.equalsIgnoreCase(grupo.IdTipoProducto)) {
					puntaje = puntaje * 32;
				}
				else if (grupo.IdTipoProducto == null) {
					puntaje = puntaje * 16;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoOperacion.equalsIgnoreCase(grupo.IdTipoOperacion)) {
					puntaje = puntaje * 8;
				}
				else if (grupo.IdTipoOperacion == null) {
					puntaje = puntaje * 4;
				}
				else {
					puntaje = puntaje * 0;
				}
				
				if (info.IdTipoCanalIngreso.equalsIgnoreCase(grupo.IdTipoCanalIngreso)) {
					puntaje = puntaje * 2;
				}
				else if (grupo.IdTipoCanalIngreso == null) {
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
			ArrayList<DefinicionCobro> cobros = cobrosPorGrupo.get(grupo);
			for (DefinicionCobro linea : cobros) {
				DefinicionCobro cob = new DefinicionCobro();
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
			return ((this.IdTipoProducto + "").equalsIgnoreCase(e.IdTipoProducto + "")) && 
					((this.IdTipoOperacion + "").equalsIgnoreCase(e.IdTipoOperacion + "")) &&
					((this.IdBolsa + "").equalsIgnoreCase(e.IdBolsa + "")) &&
					((this.IdTipoCanalIngreso + "").equalsIgnoreCase(e.IdTipoCanalIngreso+ "") &&
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
	
	public static class DefinicionCobro {
		
		public Long IdInterno;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdClaseCobro;
		public String IdTipoCobro;
		public String NombreTipoCobro;
		public String CodigoDefinicionCobro;
		public String IdEstado;
		public String Descripcion;
		public String CheckCobroMonedaOperacion;
		public String IdMonedaCobroOtraMoneda;
		public BigDecimal MontoFijo_MontoCobro;
		public String MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal MontoFijo_ValorMontoOperacionMenor;
		public String MontoFijo_CheckSiNo;
		public BigDecimal MontoFijo_ValorMontoSiNo;
		public BigDecimal PorcentualFijo_ValorCobro;
		public String PorcentualFijo_CheckSobreMonto;
		public String PorcentualFijo_CheckSobreCantidad;
		public String PorcentualFijo_CheckMayorEntreDos;
		public String PorcentualFijo_CheckMenorEntreDos;
		public String PorcentualFijo_CheckMontoMinimo;
		public BigDecimal PorcentualFijo_ValorMontoMinimo;
		public String PorcentualFijo_CheckMontoMaximo;
		public BigDecimal PorcentualFijo_ValorMontoMaximo;
		public String PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal PorcentualTasaPlazo_ValorCobro;
		public String Otros_CheckOperacionContado;
		public String Otros_CheckOperacionPlazo;
		public String Otros_CheckBaseTasa;
		public Long Otros_ValorBaseTasa;
		public String Otros_CheckInteresSimple;
		public String Otros_CheckInteresCompuesto;
		public String Otros_CheckAllIn;
		public BigDecimal Otros_ValorAllInMontoOperacionBase;
		public String Otros_CheckAfectoImpuesto;
		public String Otros_CheckAplicaPortafolio;
		public String Otros_CheckAplicaCorresponsal;
		public String Otros_CheckPermiteAumentarValor;
		public String Otros_CheckPermiteDisminuirValor;
		public String PorcentualRango_IdMonedaRangoMonto;
		public String PorcentualRango_IdTipoRangoCobro;
		public String Otros_IncluyeExcluyeEmisoresCheck;
		public String Otros_IncluyeExcluyeInstrumentosCheck;
		public String Otros_IncluyeExcluyeCorresponsalesCheck;
		public String Otros_IncluyeExcluyeEmisoresValor;
		public String Otros_IncluyeExcluyeInstrumentosValor;
		public String Otros_IncluyeExcluyeCorresponsalesValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobrosPlantillaPeru_Salida {
		public List<DefinicionCobro> p_out_MatrizOperacional;
		public List<MensajeSalida> p_out_Mensajes;
		
		public ConsultarCobrosPlantillaPeru_Salida() {
			p_out_MatrizOperacional = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultarCobrosPlantillaPeru_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InformacionOperacion> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobrosPlantillaPeru_Entrada() {
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
		ConsultarCobrosPlantillaPeru cc = new ConsultarCobrosPlantillaPeru();
		ConsultarCobrosPlantillaPeru_Entrada entrada = new ConsultarCobrosPlantillaPeru_Entrada();
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
		ConsultarCobrosPlantillaPeru_Salida a = cc.logica(con, entrada);
		System.out.println(a.toString());
		
	}
	

}