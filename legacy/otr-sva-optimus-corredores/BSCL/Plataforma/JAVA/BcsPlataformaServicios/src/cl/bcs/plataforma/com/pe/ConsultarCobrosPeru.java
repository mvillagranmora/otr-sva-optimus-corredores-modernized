package cl.bcs.plataforma.com.pe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCobrosPeru implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarCobrosPeru.class);
	
	private final String SQL_CONSULTAR_COBROS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosOperaciones_ConsultarCobrosBase.sql");
	private final String SQL_CONSULTAR_COBROS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarCobrosOperaciones_ConsultarCobrosExtra.sql");
	private final String SQL_CONSULTAR_DEFINICION_RANGO_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarDefinicionCobro_RangoCobro.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Trace
	public ConsultarCobrosOperacionesSalida logica(Connection conn,ConsultarCobrosOperacionesEntrada entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConsultarCobrosPeru");
		
		ConsultarCobrosOperacionesSalida salida = new ConsultarCobrosOperacionesSalida();
		SQL _sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		ArrayList<Long> idsPortafolios = new ArrayList<Long>();
		ArrayList<String> idsEmisor = new ArrayList<String>();
		ArrayList<Long> idsInstrumento = new ArrayList<Long>();
		ArrayList<Long> idsCorredoraCorresponsal = new ArrayList<Long>();
		
		for (InformacionOperacion ope : entrada.p_Operaciones) {
			if(!idsPortafolios.contains(ope.IdPortafolio)) idsPortafolios.add(ope.IdPortafolio);
			if(!idsEmisor.contains(ope.IdEmisor)) idsEmisor.add(ope.IdEmisor);
			if(!idsInstrumento.contains(ope.IdInstrumento)) idsInstrumento.add(ope.IdInstrumento);
			if(!idsCorredoraCorresponsal.contains(ope.IdCorredoraCorresponsal)) idsCorredoraCorresponsal.add(ope.IdCorredoraCorresponsal);
		}
		params1.put("idsPortafolios", idsPortafolios);
		params1.put("idsEmisor", idsEmisor);
		params1.put("idsInstrumento", idsInstrumento);
		params1.put("idsCorredoraCorresponsal", idsCorredoraCorresponsal);
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DefinicionCobroProceso> base = _sql.query(conn, SQL_CONSULTAR_COBROS_BASE, params1, DefinicionCobroProceso.class);
		List<DefinicionCobroProceso> extra = _sql.query(conn, SQL_CONSULTAR_COBROS_EXTRA, params1, DefinicionCobroProceso.class);
		params1.clear();
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DefinicionRangoCobro> rangos = _sql.query(conn, SQL_CONSULTAR_DEFINICION_RANGO_COBROS, params1, DefinicionRangoCobro.class);

		List<DefinicionCobroSalida> cobrosBase = new ArrayList<>();
		List<DefinicionCobroSalida> cobrosExtra = new ArrayList<>();
		obtenerCobros(entrada.p_Operaciones,base,cobrosBase );
		obtenerCobros(entrada.p_Operaciones,extra,cobrosExtra);
		HashMap<Object, Object> hashOperaciones = Utils.List2Hash(entrada.p_Operaciones, "IdInterno");
		
		//CONSOLIDAMOS
		List<DefinicionCobroSalida> cobrosConsolidados = new ArrayList<>();
		{
			for (DefinicionCobroSalida cobextra : cobrosExtra) {
				if (cobextra.IdEstado.equalsIgnoreCase("ACT")){
					for (DefinicionRangoCobro ran : rangos){
						if (cobextra.CodigoDefinicionCobro.equalsIgnoreCase(ran.CodigoDefinicionCobro)){
							cobextra.RangoCobros.add(ran);
						}
					}
					cobrosConsolidados.add(cobextra);
				}
			}
			
			for (DefinicionCobroSalida cobbase : cobrosBase) {
				boolean found = false;
				for (DefinicionCobroSalida cobactual : cobrosConsolidados) {
					if ( cobactual.IdInterno.equals(cobbase.IdInterno) &&
						 cobactual.CodigoDefinicionCobro.equalsIgnoreCase(cobbase.CodigoDefinicionCobro) &&
							( (cobactual.IdBolsa == null 				&& cobbase.IdBolsa == null) 			|| cobbase.IdBolsa.equalsIgnoreCase(cobactual.IdBolsa) ) &&
							( (cobactual.IdTipoProducto == null 		&& cobbase.IdTipoProducto == null) 		|| cobbase.IdTipoProducto.equalsIgnoreCase(cobactual.IdTipoProducto) ) &&
							( (cobactual.IdTipoOperacion == null 		&& cobbase.IdTipoOperacion == null) 	|| cobbase.IdTipoOperacion.equalsIgnoreCase(cobactual.IdTipoOperacion) ) &&
							( (cobactual.IdTipoCanalIngreso  == null 	&& cobbase.IdTipoCanalIngreso == null)	|| cobbase.IdTipoCanalIngreso.equalsIgnoreCase(cobactual.IdTipoCanalIngreso) ) 
						) {
						
						found = true;
						break;
					}
				}
				if (!found && cobbase.IdEstado.equalsIgnoreCase("ACT")) {
					for (DefinicionRangoCobro ran : rangos){
						if (cobbase.CodigoDefinicionCobro.equalsIgnoreCase(ran.CodigoDefinicionCobro)){
							cobbase.RangoCobros.add(ran);
						}
					}
					cobrosConsolidados.add(cobbase);
				}
			}
			
			//logger.debug(cobrosConsolidados);
		}
		
		
		//FORMAMOS GRUPOS
		HashMap<GrupoCobro,ArrayList<DefinicionCobroSalida>> cobrosPorGrupo = new HashMap<>();
		for (DefinicionCobroSalida linea : cobrosConsolidados) {
			
			GrupoCobro c = new GrupoCobro();
			c.IdInterno = linea.IdInterno;
			c.IdBolsa = linea.IdBolsa;
			c.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
			c.IdTipoOperacion = linea.IdTipoOperacion;
			c.IdTipoProducto = linea.IdTipoProducto;
			c.IdPortafolio = linea.IdPortafolio;
			if (!cobrosPorGrupo.containsKey(c)) {
				cobrosPorGrupo.put(c, new ArrayList<>());
			}
			
			ArrayList<DefinicionCobroSalida> lineas = cobrosPorGrupo.get(c);
			lineas.add(linea);	
			
			
			
		}
		
		//FORMAMOS HASH OPERACION-GRUPOS
		HashMap<Long,ArrayList<GrupoCobro>> gruposPorOperacion = new HashMap<>();
		for (InformacionOperacion info : entrada.p_Operaciones) {
			for (GrupoCobro grupo : cobrosPorGrupo.keySet()) {
				
				if (
						(grupo.IdBolsa == null || grupo.IdBolsa.equalsIgnoreCase(info.IdBolsa)) &&
						(grupo.IdPortafolio.equals(info.IdPortafolio)) &&
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
				else if (grupo.IdTipoCanalIngreso  == null) {
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
			ArrayList<DefinicionCobroSalida> cobros = cobrosPorGrupo.get(grupo);
			for (DefinicionCobroSalida linea : cobros) {
				//Utils.copyPojo(linea, cob );
				DefinicionCobroSalida cob = copyPojoDefinicionCobroSalidaDefinicionCobroSalida(linea);
				cob.IdInterno = operacion;
				salida.p_out_Cobros.add(cob);
			}
		}
		
		logger.info("Termino del servicio ConsultarCobrosPeru, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	public static void obtenerCobros(List<InformacionOperacion> operaciones,List<DefinicionCobroProceso> base, List<DefinicionCobroSalida> cobrosSalida) throws Exception {
		
		for (InformacionOperacion operacion : operaciones) {
			for (DefinicionCobroProceso cobro : base) {
				
				if (cobro.IdPortafolio.equals(operacion.IdPortafolio)
						&&	cobro.IdTipoProducto.equalsIgnoreCase(operacion.IdTipoProducto)
						&& ( cobro.IdTipoOperacion == null || cobro.IdTipoOperacion.equalsIgnoreCase(operacion.IdTipoOperacion) )
						&& ( cobro.IdTipoCanalIngreso == null || cobro.IdTipoCanalIngreso.equalsIgnoreCase(operacion.IdTipoCanalIngreso) )
						&& ( cobro.IdBolsa == null || cobro.IdBolsa.equalsIgnoreCase(operacion.IdBolsa) )
						) {
					
					if (cobro.Otros_IncluyeExcluyeCorresponsalesCheck.equalsIgnoreCase("S")) {
						if (cobro.Otros_IncluyeExcluyeCorresponsalesValor.equalsIgnoreCase("I")) {
							//TODO RECORRER LISTA DE CORRESPONSALES PARA VER SI COINCIDE
							//si no esta en la lista, se salta el cobro
						}
						else if (cobro.Otros_IncluyeExcluyeCorresponsalesValor.equalsIgnoreCase("E")) {
							//TODO RECORRER LISTA DE CORRESPONSALES PARA VER SI COINCIDE
							//si esta en la lista, se salta el cobro
						}
					}
					
					if (cobro.Otros_IncluyeExcluyeEmisoresCheck.equalsIgnoreCase("S")) {
						if (cobro.Otros_IncluyeExcluyeEmisoresValor.equalsIgnoreCase("I")) {
							//TODO RECORRER LISTA DE EMISORES PARA VER SI COINCIDE
						}
						else if (cobro.Otros_IncluyeExcluyeEmisoresValor.equalsIgnoreCase("E")) {
							//TODO RECORRER LISTA DE EMISORES PARA VER SI COINCIDE
						}
					}
					
					if (cobro.Otros_IncluyeExcluyeInstrumentosCheck.equalsIgnoreCase("S")) {
						if (cobro.Otros_IncluyeExcluyeInstrumentosValor.equalsIgnoreCase("I")) {
							//TODO RECORRER LISTA DE INSTRUMENTOS PARA VER SI COINCIDE
						}
						else if (cobro.Otros_IncluyeExcluyeInstrumentosValor.equalsIgnoreCase("E")) {
							//TODO RECORRER LISTA DE INSTRUMENTOS PARA VER SI COINCIDE
						}
					}
							
					//TODO AGREGAR COBROS POR RANGO
					
					//TODO REVISAR ESTADO DEL COBRO
					//Utils.copyPojo(cobro, cobroSalida);
					DefinicionCobroSalida cobroSalida = copyPojoDefinicionCobroProcesoDefinicionCobroSalida(cobro);
					cobroSalida.IdInterno = operacion.IdInterno;
					cobrosSalida.add(cobroSalida);
				}
			}
		}
	}
	
	class GrupoCobro {
		public Long IdInterno;
		public Long	IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		
		@Override
		public boolean equals(Object o) {
			GrupoCobro e = (GrupoCobro)o;
			return ((this.IdTipoProducto + "").equalsIgnoreCase(e.IdTipoProducto + "")) && 
					((this.IdTipoOperacion + "").equalsIgnoreCase(e.IdTipoOperacion + "")) &&
					((this.IdInterno + "").equalsIgnoreCase(e.IdInterno + "")) &&
					((this.IdBolsa + "").equalsIgnoreCase(e.IdBolsa + "")) &&
					((this.IdTipoCanalIngreso + "").equalsIgnoreCase(e.IdTipoCanalIngreso + "") &&
					(this.IdPortafolio.equals(e.IdPortafolio)));
		}
		
		@Override
		public int hashCode() {
			return (this.IdInterno + this.IdTipoProducto + this.IdTipoOperacion + this.IdTipoCanalIngreso + this.IdBolsa + this.IdPortafolio).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class DefinicionCobroProceso {
		public Long IdPortafolio;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdClaseCobro;
		public String IdClaseCobroBase;
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
		public String MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal MontoFijoPorUnidad_MontoCobro;
		
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
		public Long ValorBaseTasaCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionCobroSalida {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdClaseCobro;
		public String IdClaseCobroBase;
		public String IdTipoCobro;
		public String NombreTipoCobro;
		public String CodigoDefinicionCobro;
		public String Descripcion;
		public String IdEstado;
		public String CheckCobroMonedaOperacion;
		public String IdMonedaCobroOtraMoneda;
		public BigDecimal MontoFijo_MontoCobro;
		public String MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal MontoFijo_ValorMontoOperacionMenor;
		public String MontoFijo_CheckSiNo;
		public BigDecimal MontoFijo_ValorMontoSiNo;
		public String MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal MontoFijoPorUnidad_MontoCobro;
		
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
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList<DefinicionRangoCobro>();
		public Long ValorBaseTasaCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionRangoCobro {
		public Long IdCobroRangoOrden;
		public Long IdCobroOrden;
		public Long IdOrden;
		public Long IdGrupoCobro;
		public Long IdEmpresa;
		public String CodigoDefinicionCobro;
		public BigDecimal Hasta;
		public BigDecimal Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobrosOperacionesSalida {
		public List<DefinicionCobroSalida> p_out_Cobros;
		public List<MensajeSalida> p_out_Mensajes;
		
		public ConsultarCobrosOperacionesSalida() {
			p_out_Cobros = new ArrayList<DefinicionCobroSalida>();
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ConsultarCobrosOperacionesEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InformacionOperacion> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobrosOperacionesEntrada() {
			this.p_Operaciones = new ArrayList<InformacionOperacion>();
		}
	}
	
	public static class InformacionOperacion {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		
		public Long IdCorredoraCorresponsal;
		public String IdEmisor;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private DefinicionCobroSalida copyPojoDefinicionCobroSalidaDefinicionCobroSalida(DefinicionCobroSalida linea) {
		DefinicionCobroSalida cob = new DefinicionCobroSalida();
		cob.IdInterno = linea.IdInterno;								
		cob.IdPortafolio = linea.IdPortafolio;
		cob.IdPerfilCobros = linea.IdPerfilCobros;
		cob.EsPerfilPersonalizado = linea.EsPerfilPersonalizado;
		cob.IdEstadoPerfilCobros = linea.IdEstadoPerfilCobros;
		cob.IdTipoProducto = linea.IdTipoProducto;
		cob.IdTipoOperacion = linea.IdTipoOperacion;
		cob.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
		cob.IdBolsa = linea.IdBolsa;
		cob.IdClaseCobro = linea.IdClaseCobro;
		cob.IdClaseCobroBase = linea.IdClaseCobroBase;
		cob.IdTipoCobro = linea.IdTipoCobro;
		cob.NombreTipoCobro = linea.NombreTipoCobro;
		cob.CodigoDefinicionCobro = linea.CodigoDefinicionCobro;
		cob.Descripcion = linea.Descripcion;
		cob.IdEstado = linea.IdEstado;
		cob.CheckCobroMonedaOperacion = linea.CheckCobroMonedaOperacion;
		cob.IdMonedaCobroOtraMoneda = linea.IdMonedaCobroOtraMoneda;
		cob.MontoFijo_MontoCobro = linea.MontoFijo_MontoCobro;
		cob.MontoFijo_CheckMontoOperacionMenor = linea.MontoFijo_CheckMontoOperacionMenor;
		cob.MontoFijo_ValorMontoOperacionMenor = linea.MontoFijo_ValorMontoOperacionMenor;
		cob.MontoFijo_CheckSiNo = linea.MontoFijo_CheckSiNo;
		cob.MontoFijo_ValorMontoSiNo = linea.MontoFijo_ValorMontoSiNo;
		cob.MontoFijoPorUnidad_AllInCheckValor = linea.MontoFijoPorUnidad_AllInCheckValor;
		cob.MontoFijoPorUnidad_MontoCobro = linea.MontoFijoPorUnidad_MontoCobro;
		cob.PorcentualFijo_ValorCobro = linea.PorcentualFijo_ValorCobro;
		cob.PorcentualFijo_CheckSobreMonto = linea.PorcentualFijo_CheckSobreMonto;
		cob.PorcentualFijo_CheckSobreCantidad = linea.PorcentualFijo_CheckSobreCantidad;
		cob.PorcentualFijo_CheckMayorEntreDos = linea.PorcentualFijo_CheckMayorEntreDos;
		cob.PorcentualFijo_CheckMenorEntreDos = linea.PorcentualFijo_CheckMenorEntreDos;
		cob.PorcentualFijo_CheckMontoMinimo = linea.PorcentualFijo_CheckMontoMinimo;
		cob.PorcentualFijo_ValorMontoMinimo = linea.PorcentualFijo_ValorMontoMinimo;
		cob.PorcentualFijo_CheckMontoMaximo = linea.PorcentualFijo_CheckMontoMaximo;
		cob.PorcentualFijo_ValorMontoMaximo = linea.PorcentualFijo_ValorMontoMaximo;
		cob.PorcentualFijo_IdMonedaMinimoMaximo = linea.PorcentualFijo_IdMonedaMinimoMaximo;
		cob.PorcentualTasaPlazo_ValorCobro = linea.PorcentualTasaPlazo_ValorCobro;
		cob.Otros_CheckOperacionContado = linea.Otros_CheckOperacionContado;
		cob.Otros_CheckOperacionPlazo = linea.Otros_CheckOperacionPlazo;
		cob.Otros_CheckBaseTasa = linea.Otros_CheckBaseTasa;
		cob.Otros_ValorBaseTasa = linea.Otros_ValorBaseTasa;
		cob.Otros_CheckInteresSimple = linea.Otros_CheckInteresSimple;
		cob.Otros_CheckInteresCompuesto = linea.Otros_CheckInteresCompuesto;
		cob.Otros_CheckAllIn = linea.Otros_CheckAllIn;
		cob.Otros_ValorAllInMontoOperacionBase = linea.Otros_ValorAllInMontoOperacionBase;
		cob.Otros_CheckAfectoImpuesto = linea.Otros_CheckAfectoImpuesto;
		cob.Otros_CheckAplicaPortafolio = linea.Otros_CheckAplicaPortafolio;
		cob.Otros_CheckAplicaCorresponsal = linea.Otros_CheckAplicaCorresponsal;
		cob.Otros_CheckPermiteAumentarValor = linea.Otros_CheckPermiteAumentarValor;
		cob.Otros_CheckPermiteDisminuirValor = linea.Otros_CheckPermiteDisminuirValor;
		cob.PorcentualRango_IdMonedaRangoMonto = linea.PorcentualRango_IdMonedaRangoMonto;
		cob.PorcentualRango_IdTipoRangoCobro = linea.PorcentualRango_IdTipoRangoCobro;
		cob.Otros_IncluyeExcluyeEmisoresCheck = linea.Otros_IncluyeExcluyeEmisoresCheck;
		cob.Otros_IncluyeExcluyeInstrumentosCheck = linea.Otros_IncluyeExcluyeInstrumentosCheck;
		cob.Otros_IncluyeExcluyeCorresponsalesCheck = linea.Otros_IncluyeExcluyeCorresponsalesCheck;
		cob.Otros_IncluyeExcluyeEmisoresValor = linea.Otros_IncluyeExcluyeEmisoresValor;
		cob.Otros_IncluyeExcluyeInstrumentosValor = linea.Otros_IncluyeExcluyeInstrumentosValor;
		cob.Otros_IncluyeExcluyeCorresponsalesValor = linea.Otros_IncluyeExcluyeCorresponsalesValor;
		cob.RangoCobros = new ArrayList<DefinicionRangoCobro>(linea.RangoCobros);
		cob.ValorBaseTasaCobro = linea.ValorBaseTasaCobro;
		return cob;
	}
	
	private static DefinicionCobroSalida copyPojoDefinicionCobroProcesoDefinicionCobroSalida(DefinicionCobroProceso cobro) {
		DefinicionCobroSalida cob = new DefinicionCobroSalida();
		//cob.IdInterno = cobro.IdInterno;								
		cob.IdPortafolio = cobro.IdPortafolio;
		cob.IdPerfilCobros = cobro.IdPerfilCobros;
		cob.EsPerfilPersonalizado = cobro.EsPerfilPersonalizado;
		cob.IdEstadoPerfilCobros = cobro.IdEstadoPerfilCobros;
		cob.IdTipoProducto = cobro.IdTipoProducto;
		cob.IdTipoOperacion = cobro.IdTipoOperacion;
		cob.IdTipoCanalIngreso = cobro.IdTipoCanalIngreso;
		cob.IdBolsa = cobro.IdBolsa;
		cob.IdClaseCobro = cobro.IdClaseCobro;
		cob.IdClaseCobroBase = cobro.IdClaseCobroBase;
		cob.IdTipoCobro = cobro.IdTipoCobro;
		cob.NombreTipoCobro = cobro.NombreTipoCobro;
		cob.CodigoDefinicionCobro = cobro.CodigoDefinicionCobro;
		cob.Descripcion = cobro.Descripcion;
		cob.IdEstado = cobro.IdEstado;
		cob.CheckCobroMonedaOperacion = cobro.CheckCobroMonedaOperacion;
		cob.IdMonedaCobroOtraMoneda = cobro.IdMonedaCobroOtraMoneda;
		cob.MontoFijo_MontoCobro = cobro.MontoFijo_MontoCobro;
		cob.MontoFijo_CheckMontoOperacionMenor = cobro.MontoFijo_CheckMontoOperacionMenor;
		cob.MontoFijo_ValorMontoOperacionMenor = cobro.MontoFijo_ValorMontoOperacionMenor;
		cob.MontoFijo_CheckSiNo = cobro.MontoFijo_CheckSiNo;
		cob.MontoFijo_ValorMontoSiNo = cobro.MontoFijo_ValorMontoSiNo;
		cob.MontoFijoPorUnidad_AllInCheckValor = cobro.MontoFijoPorUnidad_AllInCheckValor;
		cob.MontoFijoPorUnidad_MontoCobro = cobro.MontoFijoPorUnidad_MontoCobro;
		cob.PorcentualFijo_ValorCobro = cobro.PorcentualFijo_ValorCobro;
		cob.PorcentualFijo_CheckSobreMonto = cobro.PorcentualFijo_CheckSobreMonto;
		cob.PorcentualFijo_CheckSobreCantidad = cobro.PorcentualFijo_CheckSobreCantidad;
		cob.PorcentualFijo_CheckMayorEntreDos = cobro.PorcentualFijo_CheckMayorEntreDos;
		cob.PorcentualFijo_CheckMenorEntreDos = cobro.PorcentualFijo_CheckMenorEntreDos;
		cob.PorcentualFijo_CheckMontoMinimo = cobro.PorcentualFijo_CheckMontoMinimo;
		cob.PorcentualFijo_ValorMontoMinimo = cobro.PorcentualFijo_ValorMontoMinimo;
		cob.PorcentualFijo_CheckMontoMaximo = cobro.PorcentualFijo_CheckMontoMaximo;
		cob.PorcentualFijo_ValorMontoMaximo = cobro.PorcentualFijo_ValorMontoMaximo;
		cob.PorcentualFijo_IdMonedaMinimoMaximo = cobro.PorcentualFijo_IdMonedaMinimoMaximo;
		cob.PorcentualTasaPlazo_ValorCobro = cobro.PorcentualTasaPlazo_ValorCobro;
		cob.Otros_CheckOperacionContado = cobro.Otros_CheckOperacionContado;
		cob.Otros_CheckOperacionPlazo = cobro.Otros_CheckOperacionPlazo;
		cob.Otros_CheckBaseTasa = cobro.Otros_CheckBaseTasa;
		cob.Otros_ValorBaseTasa = cobro.Otros_ValorBaseTasa;
		cob.Otros_CheckInteresSimple = cobro.Otros_CheckInteresSimple;
		cob.Otros_CheckInteresCompuesto = cobro.Otros_CheckInteresCompuesto;
		cob.Otros_CheckAllIn = cobro.Otros_CheckAllIn;
		cob.Otros_ValorAllInMontoOperacionBase = cobro.Otros_ValorAllInMontoOperacionBase;
		cob.Otros_CheckAfectoImpuesto = cobro.Otros_CheckAfectoImpuesto;
		cob.Otros_CheckAplicaPortafolio = cobro.Otros_CheckAplicaPortafolio;
		cob.Otros_CheckAplicaCorresponsal = cobro.Otros_CheckAplicaCorresponsal;
		cob.Otros_CheckPermiteAumentarValor = cobro.Otros_CheckPermiteAumentarValor;
		cob.Otros_CheckPermiteDisminuirValor = cobro.Otros_CheckPermiteDisminuirValor;
		cob.PorcentualRango_IdMonedaRangoMonto = cobro.PorcentualRango_IdMonedaRangoMonto;
		cob.PorcentualRango_IdTipoRangoCobro = cobro.PorcentualRango_IdTipoRangoCobro;
		cob.Otros_IncluyeExcluyeEmisoresCheck = cobro.Otros_IncluyeExcluyeEmisoresCheck;
		cob.Otros_IncluyeExcluyeInstrumentosCheck = cobro.Otros_IncluyeExcluyeInstrumentosCheck;
		cob.Otros_IncluyeExcluyeCorresponsalesCheck = cobro.Otros_IncluyeExcluyeCorresponsalesCheck;
		cob.Otros_IncluyeExcluyeEmisoresValor = cobro.Otros_IncluyeExcluyeEmisoresValor;
		cob.Otros_IncluyeExcluyeInstrumentosValor = cobro.Otros_IncluyeExcluyeInstrumentosValor;
		cob.Otros_IncluyeExcluyeCorresponsalesValor = cobro.Otros_IncluyeExcluyeCorresponsalesValor;
		//cob.RangoCobros = new ArrayList<DefinicionRangoCobro>(cobro.RangoCobros);
		cob.ValorBaseTasaCobro = cobro.ValorBaseTasaCobro;
		return cob;
	}
	
	
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		ConsultarCobrosPeru cc = new ConsultarCobrosPeru();
		ConsultarCobrosOperacionesEntrada entrada = new ConsultarCobrosOperacionesEntrada();
		entrada.p_IdEmpresa = 1313L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		InformacionOperacion e = new InformacionOperacion();
		e.IdBolsa = "BVL";
		e.IdCorredoraCorresponsal = null;
		e.IdInterno = 0L;
		e.IdPortafolio = 1450109L;
		e.IdTipoCanalIngreso = "AGE";
		e.IdTipoOperacion = "COMRRV";
		e.IdTipoProducto = "RERV";
		e.IdInstrumento = 502L;
		e.IdEmisor = "COPEC";
		entrada.p_Operaciones.add(e);
		ConsultarCobrosOperacionesSalida a = cc.logica(con, entrada);
		System.out.println(a.p_out_Cobros);
	}
}