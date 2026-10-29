package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;



public class CalcularCobros {
	
	@Trace
	public CalcularCobrosSalida logica(Connection conn,CalcularCobrosEntrada entrada) throws Exception {
		
		CalcularCobrosSalida salida = new CalcularCobrosSalida();
		ConstantesPlataformaComercial _com = new ConstantesPlataformaComercial();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.MonedaSistema");
		BigDecimal totalCobros = BigDecimal.ZERO;
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada entradaParidad = new CalcularParidadEntrada();
		entradaParidad.p_IdEmpresa = 1L;
		entradaParidad.p_IdTrabajo = 0L;
		for (CobroEntrada cobro: entrada.cobros) {
			CobroSalida cobroSalida = new CobroSalida();
			cobroSalida.IdInternoCobro = cobro.IdInternoCobro;
			if (cobro.CodigoCobroClasificacion.equalsIgnoreCase("IMPUESTO"))
				continue;
			
			if (cobro.ValorCobroAplicado != null && cobro.PermiteCambiarValor.equalsIgnoreCase("S")) {
				cobroSalida.ValorCobroFinal = cobro.ValorCobroAplicado;
			} else {
				cobroSalida.ValorCobroFinal = cobro.ValorCobroPerfil;
			}
			
			BigDecimal montoCobro = null;
			if (_com.cobrosTasa.contains(cobro.IdCobro)) {
				montoCobro = cobroSalida.ValorCobroFinal.multiply(cobro.MontoOperacionMonedaCobro)
						.multiply(new BigDecimal(entrada.DiasPlazo))
						.divide(new BigDecimal(30),20,RoundingMode.HALF_UP)
						.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			}
			else if (_com.cobrosFijos.contains(cobro.IdCobro)) {
				montoCobro = cobroSalida.ValorCobroFinal;
			}
			else if (_com.cobrosVariables.contains(cobro.IdCobro)) {
				montoCobro = cobroSalida.ValorCobroFinal.multiply(cobro.MontoOperacionMonedaCobro).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			}
			else if (_com.cobrosVariables.contains(cobro.IdCobro)) {
				montoCobro = cobroSalida.ValorCobroFinal.multiply(cobro.MontoOperacionMonedaCobro).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			}
			else if (_com.cobrosExentos.contains(cobro.IdCobro)) {
				montoCobro = cobroSalida.ValorCobroFinal.multiply(cobro.MontoOperacionMonedaCobro).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			}
			//si llega aqui, el cobro no esta implementado
			else{
				montoCobro = BigDecimal.ZERO;
			}
			
			montoCobro = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			
			if(cobro.MontoMinimo.compareTo(BigDecimal.ZERO) == 0 || montoCobro.compareTo(cobro.MontoMinimo) == 1) {
				cobroSalida.MontoCobroCalculado = montoCobro;
			} else {
				cobroSalida.MontoCobroCalculado = cobro.MontoMinimo;
				
				if ("V".equals(cobro.FormaCobro)) {
					BigDecimal porcentaje = cobro.MontoMinimo.multiply(BigDecimal.valueOf(100)).divide(cobro.MontoOperacionMonedaCobro, 8, RoundingMode.HALF_UP);
					cobroSalida.ValorCobroFinal = porcentaje;
				}
			}
			salida.p_out_Cobros.add(cobroSalida);
			
			//formatear costos con paridad para que los valores se sumen en una moneda para calculo de IVA
			CalculoEntrada cEntrada = new CalculoEntrada();
			cEntrada.Fecha = fechaSistema;
			cEntrada.IdInterno = cobroSalida.IdInternoCobro;
			cEntrada.IdMonedaOrigen = cobro.IdMonedaCobro;
			cEntrada.IdMonedaDestino = monedaSistema;
			cEntrada.IdTrabajo = 0L;
			cEntrada.IdUso = "Calculo Cobro";
			cEntrada.MontoOrigen = cobroSalida.MontoCobroCalculado;
			cEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			entradaParidad.p_CalculosEntrada.add(cEntrada);
		}
		CalcularParidadSalida salidaParidad = calcularParidad.logica(conn, entradaParidad );
		for (CobroEntrada cobro: entrada.cobros) {
			if (!_com.cobrosExentos.contains(cobro.IdCobro)) {
				for(CalculoSalida paridad :salidaParidad.p_out_CalculosSalida) {
					if(paridad.IdInterno.equals(cobro.IdInternoCobro))
						totalCobros = totalCobros.add(paridad.MontoLocal);
				}
			}
		}
		
		for (CobroEntrada cobro: entrada.cobros) {
			CobroSalida cobroSalida = new CobroSalida();
			cobroSalida.IdInternoCobro = cobro.IdInternoCobro;
			if (!cobro.CodigoCobroClasificacion.equalsIgnoreCase("IMPUESTO"))
				continue;
			if (cobro.PermiteCambiarValor.equalsIgnoreCase("S") && cobro.ValorCobroAplicado != null) {
				cobroSalida.ValorCobroFinal = cobro.ValorCobroAplicado;
			}else{
				cobroSalida.ValorCobroFinal = cobro.ValorCobroPerfil;
			}
			
			
			BigDecimal montoCobro = cobroSalida.ValorCobroFinal.multiply(totalCobros).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			
			montoCobro = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			
			cobroSalida.MontoCobroCalculado = montoCobro;
			salida.p_out_Cobros.add(cobroSalida);
		}
		
		  
		
		return salida; 
	}
	
	public static void main (String[] args) {
		CalcularCobros c = new CalcularCobros();
		CalcularCobrosEntrada entrada = new CalcularCobrosEntrada();
		/*entrada.cobros.add(new Cobro("COVA",new BigDecimal("0.5"),new BigDecimal("5000"),"COMISION","S",new BigDecimal("0.6")));
		entrada.cobros.add(new Cobro("GAFI",new BigDecimal("5000"),new BigDecimal("0"),"GASTO","S",null));
		entrada.cobros.add(new Cobro("DEFI",new BigDecimal("2000"),new BigDecimal("0"),"DERECHO","S",null));
		entrada.cobros.add(new Cobro("IVA",new BigDecimal(19),new BigDecimal(0),"IMPUESTO","N",null));
		*/
		//CalcularCobrosSalida s = c.logica(entrada);
		
		//System.out.println(s.toString());
		
		
		
	}
	
	public static class CalcularCobrosSalida {
		public List<CobroSalida> p_out_Cobros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularCobrosSalida() {
			p_out_Cobros = new ArrayList<>();
		}
	}
	
	public static class CalcularCobrosEntrada {
		
		public BigDecimal Tasa;
		public Long DiasPlazo;
		public List<CobroEntrada> cobros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularCobrosEntrada() {
			cobros = new ArrayList<>();
		}
	}	 
	
	public static class CobroEntrada {
		public Long			IdInternoCobro;
		public BigDecimal	MontoOperacionMonedaCobro;
		public String		IdCobro;
		public BigDecimal	ValorCobroPerfil;
		public BigDecimal	MontoMinimo;
		public String		CodigoCobroClasificacion;
		public String 		PermiteCambiarValor;
		public BigDecimal 	ValorCobroAplicado;
		public String		IdMonedaCobro;
		public String 		FormaCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroSalida { 
		public Long IdInternoCobro;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal ValorCobroFinal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		/*public Cobro(String IdCobro,BigDecimal ValorCobroPerfil,BigDecimal MontoMinimo,String CodigoCobroClasificacion,
				String PermiteCambiarValor,BigDecimal ValorCobroAplicado) {
			this.IdCobro = IdCobro;
			this.ValorCobroPerfil = ValorCobroPerfil;
			this.MontoMinimo = MontoMinimo;
			this.CodigoCobroClasificacion = CodigoCobroClasificacion;
			this.PermiteCambiarValor = PermiteCambiarValor;
			this.ValorCobroAplicado = ValorCobroAplicado;
			
		}
		
		public Cobro(Cobro c) {
			this.IdCobro = c.IdCobro;
			this.ValorCobroPerfil = c.ValorCobroPerfil;
			this.MontoMinimo = c.MontoMinimo;
			this.CodigoCobroClasificacion = c.CodigoCobroClasificacion;
			this.PermiteCambiarValor = c.PermiteCambiarValor;
			this.ValorCobroAplicado = c.ValorCobroAplicado;
			this.ValorCobroFinal = c.ValorCobroFinal;
			
			this.MontoCobroCalculado = c.MontoCobroCalculado;
		}*/
	}
	


}
