package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.ConsultarCobrosOperacionesEntrada;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.ConsultarCobrosOperacionesSalida;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionCobroSalida;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.InformacionOperacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCobrosOrdenesPe implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarCobrosOrdenesPe.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//private final String SQL_SELECT_COBROS_CORRESPONSAL = loadResourceToString("cl/bcs/ordenes/sql/ConsultarCobrosOrdenesPe_SelectCobrosCorresponsal.sql");
	
	@Trace
	public ConsultarCobrosOrdenesPe_Salida logica(Connection conn,ConsultarCobrosOrdenesPe_Entrada ent) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConsultarCobrosOrdenesPe");
		
		ConsultarCobrosOrdenesPe_Salida salida = new ConsultarCobrosOrdenesPe_Salida();
		
		ConsultarCobrosPeru serviceConsultarCobros = new ConsultarCobrosPeru();
		ConsultarCobrosOperacionesEntrada entradaConsultarCobros = new ConsultarCobrosOperacionesEntrada();
		entradaConsultarCobros.p_IdEmpresa = ent.p_IdEmpresa;
		entradaConsultarCobros.p_IdModulo = ent.p_IdModulo;
		entradaConsultarCobros.p_IdUsuario = ent.p_IdUsuario;
		
		for (InformacionOrden_Entrada orden : ent.p_Ordenes) {
			InformacionOperacion op = new InformacionOperacion();
			//Utils.copyPojo(orden, op);
			op.IdInterno = orden.IdInterno;
			op.IdPortafolio = orden.IdPortafolio;
			op.IdTipoProducto = orden.IdTipoProducto;
			op.IdTipoOperacion = orden.IdTipoOperacion;
			op.IdTipoCanalIngreso = orden.IdTipoCanalIngreso;
			op.IdBolsa = orden.IdBolsa;
			op.IdCorredoraCorresponsal = orden.IdCorredoraCorresponsal;
			op.IdEmisor = orden.IdEmisor;
			op.IdInstrumento = orden.IdInstrumento;
			entradaConsultarCobros.p_Operaciones.add(op);
		}
			
		ConsultarCobrosOperacionesSalida salidaConsultarCobros = serviceConsultarCobros.logica(conn, entradaConsultarCobros);
		for (DefinicionCobroSalida def : salidaConsultarCobros.p_out_Cobros) {
			//Utils.copyPojo(def, cobroSalida);
			DefinicionCobro_Salida cobroSalida = copyPojoDefinicionCobroSalida(def);
			cobroSalida.IdOrigenCobro = "PORTAFOLIO";
			salida.p_out_Cobros.add(cobroSalida);
		}
		
		//COBRO DE CORRESPONSAL	
		cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.ConsultarCobrosOperacionesEntrada entradaCorr = new cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.ConsultarCobrosOperacionesEntrada();
		for (InformacionOrden_Entrada orden : ent.p_Ordenes) {
			if (orden.IdCorredoraCorresponsal != null) {
				cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.InformacionOperacion arg0 = new cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.InformacionOperacion();
				arg0.IdBolsa = orden.IdBolsa;
				arg0.IdCorredorCorresponsal = orden.IdCorredoraCorresponsal;
				arg0.IdEmisor = orden.IdEmisor;
				arg0.IdInstrumento = orden.IdInstrumento;
				arg0.IdInterno = orden.IdInterno;
				arg0.IdTipoOperacion = orden.IdTipoOperacion;
				arg0.IdTipoProducto = orden.IdTipoProducto;
				arg0.IdTipoCanalIngreso = orden.IdTipoCanalIngreso;
				entradaCorr.p_Operaciones.add(arg0);
			}
		}
		
		if (entradaCorr.p_Operaciones.size() > 0) {
			entradaCorr.p_IdEmpresa = ent.p_IdEmpresa;
			entradaCorr.p_IdModulo = ent.p_IdModulo;
			entradaCorr.p_IdUsuario = ent.p_IdUsuario;
			ConsultarCobrosCorredorCorresponsalPeru corr = new ConsultarCobrosCorredorCorresponsalPeru();
			cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.ConsultarCobrosOperacionesSalida salidaCorr = corr.logica(conn, entradaCorr );
			
			for (cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.DefinicionCobroSalida def : salidaCorr.p_out_Cobros) {
				for (InformacionOrden_Entrada orden : ent.p_Ordenes) {
					if (orden.IdInterno.equals(def.IdInterno) && orden.IdCorredoraCorresponsal.equals(def.IdCorredorCorresponsal)) {
						//Utils.copyPojo(def, cobroSalida);
						DefinicionCobro_Salida cobroSalida = copyPojoDefinicionCobroSalida(def);
						cobroSalida.IdOrigenCobro = "CORRESPONSAL";
						cobroSalida.IdInterno = orden.IdInterno;
						salida.p_out_Cobros.add(cobroSalida);
					}
				}
			}
		}
		
		logger.info("Termino del servicio ConsultarCobrosOrdenesPe, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	public static class DefinicionCobro_Salida {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdOrigenCobro;
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
	
	public static class InformacionOrden_Entrada {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		
		public Long IdCorredoraCorresponsal;
		public String IdEmisor;
		public Long IdInstrumento;
		public String EsAPlazo = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class ConsultarCobrosOrdenesPe_Salida {
		public List<DefinicionCobro_Salida> p_out_Cobros;
		public List<MensajeSalida> p_out_Mensajes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobrosOrdenesPe_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
			p_out_Cobros = new ArrayList<DefinicionCobro_Salida>();
		}
	}
	
	public static class ConsultarCobrosOrdenesPe_Entrada {
		public List<InformacionOrden_Entrada> p_Ordenes;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public ConsultarCobrosOrdenesPe_Entrada() {
			p_Ordenes = new ArrayList<InformacionOrden_Entrada>();
		}
	}
	
	private DefinicionCobro_Salida copyPojoDefinicionCobroSalida(DefinicionCobroSalida def) {
		DefinicionCobro_Salida cobroSalida = new DefinicionCobro_Salida();
		cobroSalida.IdInterno = def.IdInterno;
		cobroSalida.IdPortafolio = def.IdPortafolio;
		//cobroSalida.IdOrigenCobro = def.IdOrigenCobro;
		cobroSalida.IdPerfilCobros = def.IdPerfilCobros;
		cobroSalida.EsPerfilPersonalizado = def.EsPerfilPersonalizado;
		cobroSalida.IdEstadoPerfilCobros = def.IdEstadoPerfilCobros;
		cobroSalida.IdTipoProducto = def.IdTipoProducto;
		cobroSalida.IdTipoOperacion = def.IdTipoOperacion;
		cobroSalida.IdTipoCanalIngreso = def.IdTipoCanalIngreso;
		cobroSalida.IdBolsa = def.IdBolsa;
		cobroSalida.IdClaseCobro = def.IdClaseCobro;
		cobroSalida.IdClaseCobroBase = def.IdClaseCobroBase;
		cobroSalida.IdTipoCobro = def.IdTipoCobro;
		cobroSalida.NombreTipoCobro = def.NombreTipoCobro;
		cobroSalida.CodigoDefinicionCobro = def.CodigoDefinicionCobro;
		cobroSalida.Descripcion = def.Descripcion;
		cobroSalida.IdEstado = def.IdEstado;
		cobroSalida.CheckCobroMonedaOperacion = def.CheckCobroMonedaOperacion;
		cobroSalida.IdMonedaCobroOtraMoneda = def.IdMonedaCobroOtraMoneda;
		cobroSalida.MontoFijo_MontoCobro = def.MontoFijo_MontoCobro;
		cobroSalida.MontoFijo_CheckMontoOperacionMenor = def.MontoFijo_CheckMontoOperacionMenor;
		cobroSalida.MontoFijo_ValorMontoOperacionMenor = def.MontoFijo_ValorMontoOperacionMenor;
		cobroSalida.MontoFijo_CheckSiNo = def.MontoFijo_CheckSiNo;
		cobroSalida.MontoFijo_ValorMontoSiNo = def.MontoFijo_ValorMontoSiNo;
		cobroSalida.MontoFijoPorUnidad_AllInCheckValor = def.MontoFijoPorUnidad_AllInCheckValor;
		cobroSalida.MontoFijoPorUnidad_MontoCobro = def.MontoFijoPorUnidad_MontoCobro;
		cobroSalida.PorcentualFijo_ValorCobro = def.PorcentualFijo_ValorCobro;
		cobroSalida.PorcentualFijo_CheckSobreMonto = def.PorcentualFijo_CheckSobreMonto;
		cobroSalida.PorcentualFijo_CheckSobreCantidad = def.PorcentualFijo_CheckSobreCantidad;
		cobroSalida.PorcentualFijo_CheckMayorEntreDos = def.PorcentualFijo_CheckMayorEntreDos;
		cobroSalida.PorcentualFijo_CheckMenorEntreDos = def.PorcentualFijo_CheckMenorEntreDos;
		cobroSalida.PorcentualFijo_CheckMontoMinimo = def.PorcentualFijo_CheckMontoMinimo;
		cobroSalida.PorcentualFijo_ValorMontoMinimo = def.PorcentualFijo_ValorMontoMinimo;
		cobroSalida.PorcentualFijo_CheckMontoMaximo = def.PorcentualFijo_CheckMontoMaximo;
		cobroSalida.PorcentualFijo_ValorMontoMaximo = def.PorcentualFijo_ValorMontoMaximo;
		cobroSalida.PorcentualFijo_IdMonedaMinimoMaximo = def.PorcentualFijo_IdMonedaMinimoMaximo;
		cobroSalida.PorcentualTasaPlazo_ValorCobro = def.PorcentualTasaPlazo_ValorCobro;
		cobroSalida.Otros_CheckOperacionContado = def.Otros_CheckOperacionContado;
		cobroSalida.Otros_CheckOperacionPlazo = def.Otros_CheckOperacionPlazo;
		cobroSalida.Otros_CheckBaseTasa = def.Otros_CheckBaseTasa;
		cobroSalida.Otros_ValorBaseTasa = def.Otros_ValorBaseTasa;
		cobroSalida.Otros_CheckInteresSimple = def.Otros_CheckInteresSimple;
		cobroSalida.Otros_CheckInteresCompuesto = def.Otros_CheckInteresCompuesto;
		cobroSalida.Otros_CheckAllIn = def.Otros_CheckAllIn;
		cobroSalida.Otros_ValorAllInMontoOperacionBase = def.Otros_ValorAllInMontoOperacionBase;
		cobroSalida.Otros_CheckAfectoImpuesto = def.Otros_CheckAfectoImpuesto;
		cobroSalida.Otros_CheckAplicaPortafolio = def.Otros_CheckAplicaPortafolio;
		cobroSalida.Otros_CheckAplicaCorresponsal = def.Otros_CheckAplicaCorresponsal;
		cobroSalida.Otros_CheckPermiteAumentarValor = def.Otros_CheckPermiteAumentarValor;
		cobroSalida.Otros_CheckPermiteDisminuirValor = def.Otros_CheckPermiteDisminuirValor;
		cobroSalida.PorcentualRango_IdMonedaRangoMonto = def.PorcentualRango_IdMonedaRangoMonto;
		cobroSalida.PorcentualRango_IdTipoRangoCobro = def.PorcentualRango_IdTipoRangoCobro;
		cobroSalida.Otros_IncluyeExcluyeEmisoresCheck = def.Otros_IncluyeExcluyeEmisoresCheck;
		cobroSalida.Otros_IncluyeExcluyeInstrumentosCheck = def.Otros_IncluyeExcluyeInstrumentosCheck;
		cobroSalida.Otros_IncluyeExcluyeCorresponsalesCheck = def.Otros_IncluyeExcluyeCorresponsalesCheck;
		cobroSalida.Otros_IncluyeExcluyeEmisoresValor = def.Otros_IncluyeExcluyeEmisoresValor;
		cobroSalida.Otros_IncluyeExcluyeInstrumentosValor = def.Otros_IncluyeExcluyeInstrumentosValor;
		cobroSalida.Otros_IncluyeExcluyeCorresponsalesValor = def.Otros_IncluyeExcluyeCorresponsalesValor;
		cobroSalida.RangoCobros = new ArrayList<DefinicionRangoCobro>(def.RangoCobros);
		cobroSalida.ValorBaseTasaCobro = def.ValorBaseTasaCobro;
		return cobroSalida;
	}
	
	private DefinicionCobro_Salida copyPojoDefinicionCobroSalida(cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.DefinicionCobroSalida def) {
		DefinicionCobro_Salida cobroSalida = new DefinicionCobro_Salida();
		cobroSalida.IdInterno = def.IdInterno;
		//cobroSalida.IdPortafolio = def.IdPortafolio;
		//cobroSalida.IdOrigenCobro = def.IdOrigenCobro;
		cobroSalida.IdPerfilCobros = def.IdPerfilCobros;
		cobroSalida.EsPerfilPersonalizado = def.EsPerfilPersonalizado;
		cobroSalida.IdEstadoPerfilCobros = def.IdEstadoPerfilCobros;
		cobroSalida.IdTipoProducto = def.IdTipoProducto;
		cobroSalida.IdTipoOperacion = def.IdTipoOperacion;
		cobroSalida.IdTipoCanalIngreso = def.IdTipoCanalIngreso;
		cobroSalida.IdBolsa = def.IdBolsa;
		cobroSalida.IdClaseCobro = def.IdClaseCobro;
		cobroSalida.IdClaseCobroBase = def.IdClaseCobroBase;
		cobroSalida.IdTipoCobro = def.IdTipoCobro;
		cobroSalida.NombreTipoCobro = def.NombreTipoCobro;
		cobroSalida.CodigoDefinicionCobro = def.CodigoDefinicionCobro;
		cobroSalida.Descripcion = def.Descripcion;
		cobroSalida.IdEstado = def.IdEstado;
		cobroSalida.CheckCobroMonedaOperacion = def.CheckCobroMonedaOperacion;
		cobroSalida.IdMonedaCobroOtraMoneda = def.IdMonedaCobroOtraMoneda;
		cobroSalida.MontoFijo_MontoCobro = def.MontoFijo_MontoCobro;
		cobroSalida.MontoFijo_CheckMontoOperacionMenor = def.MontoFijo_CheckMontoOperacionMenor;
		cobroSalida.MontoFijo_ValorMontoOperacionMenor = def.MontoFijo_ValorMontoOperacionMenor;
		cobroSalida.MontoFijo_CheckSiNo = def.MontoFijo_CheckSiNo;
		cobroSalida.MontoFijo_ValorMontoSiNo = def.MontoFijo_ValorMontoSiNo;
		cobroSalida.MontoFijoPorUnidad_AllInCheckValor = def.MontoFijoPorUnidad_AllInCheckValor;
		cobroSalida.MontoFijoPorUnidad_MontoCobro = def.MontoFijoPorUnidad_MontoCobro;
		cobroSalida.PorcentualFijo_ValorCobro = def.PorcentualFijo_ValorCobro;
		cobroSalida.PorcentualFijo_CheckSobreMonto = def.PorcentualFijo_CheckSobreMonto;
		cobroSalida.PorcentualFijo_CheckSobreCantidad = def.PorcentualFijo_CheckSobreCantidad;
		cobroSalida.PorcentualFijo_CheckMayorEntreDos = def.PorcentualFijo_CheckMayorEntreDos;
		cobroSalida.PorcentualFijo_CheckMenorEntreDos = def.PorcentualFijo_CheckMenorEntreDos;
		cobroSalida.PorcentualFijo_CheckMontoMinimo = def.PorcentualFijo_CheckMontoMinimo;
		cobroSalida.PorcentualFijo_ValorMontoMinimo = def.PorcentualFijo_ValorMontoMinimo;
		cobroSalida.PorcentualFijo_CheckMontoMaximo = def.PorcentualFijo_CheckMontoMaximo;
		cobroSalida.PorcentualFijo_ValorMontoMaximo = def.PorcentualFijo_ValorMontoMaximo;
		cobroSalida.PorcentualFijo_IdMonedaMinimoMaximo = def.PorcentualFijo_IdMonedaMinimoMaximo;
		cobroSalida.PorcentualTasaPlazo_ValorCobro = def.PorcentualTasaPlazo_ValorCobro;
		cobroSalida.Otros_CheckOperacionContado = def.Otros_CheckOperacionContado;
		cobroSalida.Otros_CheckOperacionPlazo = def.Otros_CheckOperacionPlazo;
		cobroSalida.Otros_CheckBaseTasa = def.Otros_CheckBaseTasa;
		cobroSalida.Otros_ValorBaseTasa = def.Otros_ValorBaseTasa;
		cobroSalida.Otros_CheckInteresSimple = def.Otros_CheckInteresSimple;
		cobroSalida.Otros_CheckInteresCompuesto = def.Otros_CheckInteresCompuesto;
		cobroSalida.Otros_CheckAllIn = def.Otros_CheckAllIn;
		cobroSalida.Otros_ValorAllInMontoOperacionBase = def.Otros_ValorAllInMontoOperacionBase;
		cobroSalida.Otros_CheckAfectoImpuesto = def.Otros_CheckAfectoImpuesto;
		cobroSalida.Otros_CheckAplicaPortafolio = def.Otros_CheckAplicaPortafolio;
		cobroSalida.Otros_CheckAplicaCorresponsal = def.Otros_CheckAplicaCorresponsal;
		cobroSalida.Otros_CheckPermiteAumentarValor = def.Otros_CheckPermiteAumentarValor;
		cobroSalida.Otros_CheckPermiteDisminuirValor = def.Otros_CheckPermiteDisminuirValor;
		cobroSalida.PorcentualRango_IdMonedaRangoMonto = def.PorcentualRango_IdMonedaRangoMonto;
		cobroSalida.PorcentualRango_IdTipoRangoCobro = def.PorcentualRango_IdTipoRangoCobro;
		cobroSalida.Otros_IncluyeExcluyeEmisoresCheck = def.Otros_IncluyeExcluyeEmisoresCheck;
		cobroSalida.Otros_IncluyeExcluyeInstrumentosCheck = def.Otros_IncluyeExcluyeInstrumentosCheck;
		cobroSalida.Otros_IncluyeExcluyeCorresponsalesCheck = def.Otros_IncluyeExcluyeCorresponsalesCheck;
		cobroSalida.Otros_IncluyeExcluyeEmisoresValor = def.Otros_IncluyeExcluyeEmisoresValor;
		cobroSalida.Otros_IncluyeExcluyeInstrumentosValor = def.Otros_IncluyeExcluyeInstrumentosValor;
		cobroSalida.Otros_IncluyeExcluyeCorresponsalesValor = def.Otros_IncluyeExcluyeCorresponsalesValor;
		//cobroSalida.RangoCobros = new ArrayList<DefinicionRangoCobro>(def.RangoCobros);
		cobroSalida.ValorBaseTasaCobro = def.ValorBaseTasaCobro;
		
		for (cl.bcs.plataforma.com.pe.ConsultarCobrosCorredorCorresponsalPeru.DefinicionRangoCobro rangoCobros: def.RangoCobros) {
			DefinicionRangoCobro definicionRangoCobro = new DefinicionRangoCobro();
			definicionRangoCobro.IdCobroRangoOrden = rangoCobros.IdCobroRangoOrden;
			definicionRangoCobro.IdCobroOrden = rangoCobros.IdCobroOrden;
			definicionRangoCobro.IdOrden = rangoCobros.IdOrden;
			definicionRangoCobro.IdGrupoCobro = rangoCobros.IdGrupoCobro;
			definicionRangoCobro.IdEmpresa = rangoCobros.IdEmpresa;
			definicionRangoCobro.CodigoDefinicionCobro = rangoCobros.CodigoDefinicionCobro;
			definicionRangoCobro.Hasta = rangoCobros.Hasta;
			definicionRangoCobro.Valor = rangoCobros.Valor;
			cobroSalida.RangoCobros.add(definicionRangoCobro);
		}
		
		return cobroSalida;
	}
}