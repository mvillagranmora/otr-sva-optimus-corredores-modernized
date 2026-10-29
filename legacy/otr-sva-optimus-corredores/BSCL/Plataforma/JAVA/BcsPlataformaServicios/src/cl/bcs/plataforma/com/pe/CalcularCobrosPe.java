package cl.bcs.plataforma.com.pe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.ConsultarCobrosOperacionesEntrada;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.ConsultarCobrosOperacionesSalida;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionCobroSalida;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.InformacionOperacion;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularCobrosPe {
	
	public static class CalcularCobrosPe_Entrada {
		public Long p_IdEmpresa;
		public List<DefinicionCobro_Entrada> p_Cobros;
		public List<InformacionOperacion_Entrada> p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularCobrosPe_Entrada() {
			this.p_Cobros = new ArrayList<DefinicionCobro_Entrada>();
			this.p_Operaciones = new ArrayList<InformacionOperacion_Entrada>();
		}
	}
	
	public static class CalcularCobrosPe_Salida {
		public List<Cobro_Salida> p_out_Cobros;
		public List<Operacion_Salida> p_out_operaciones;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularCobrosPe_Salida() {
			p_out_Cobros = new ArrayList<Cobro_Salida>();
			p_out_operaciones = new ArrayList<>();
		}
	}
	public BigDecimal calculaMontoParidad(BigDecimal Precio,BigDecimal Cantidad,BigDecimal Factor){
		BigDecimal puntoCinco = new BigDecimal(0.5);
		Factor = Factor.setScale(10, RoundingMode.HALF_UP);
		BigDecimal y = Precio.multiply(Factor);
		
		if (y.compareTo(puntoCinco) < 0){
			//y = y.setScale(10, RoundingMode.HALF_UP);
			y = y.setScale(3, RoundingMode.DOWN);
		}
		else {
			y = y.setScale(2, RoundingMode.HALF_UP);;
		}

		return y.multiply(Cantidad);
		/*
		 * BigDecimal y = ord.PrecioOrden.multiply(calc.FactorOrigenDestino);
				BigDecimal puntoCinco = new BigDecimal(0.5);
				y = y.setScale(10, RoundingMode.HALF_UP);
				if (y.compareTo(puntoCinco) < 0){
					y = y.setScale(3, RoundingMode.DOWN);
				}
				else {
					y = y.setScale(2, RoundingMode.HALF_UP);;
				}
				BigDecimal montoDestino = y.multiply(ord.CantidadOrden);
		 */
	}
	@Trace
	public CalcularCobrosPe_Salida logica(Connection conn,CalcularCobrosPe_Entrada entrada) throws Exception {
		CalcularCobrosPe_Salida salida = new CalcularCobrosPe_Salida();
		Long IdTrabajo = 0L;
		HashMap<Long,Cobro_Salida> hashCobrosSalida = new HashMap<>();
		HashMap<Long, Object> hashOperaciones = Utils.List2Hash(entrada.p_Operaciones,"IdInterno");
		HashMap<Long, Operacion_Salida> hashOperacionesSalida = new HashMap<>();
		
		//MONEDA DEL COBRO Y SALIDA
		for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
			InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
			
			Cobro_Salida cobro = new Cobro_Salida();
			cobro.IdInterno = def.IdInterno;
			cobro.IdInternoCobro = def.IdInternoCobro;
			cobro.MontoCobroCalculado = BigDecimal.ZERO;
			
			//MONEDA DEL COBRO
			{
				if (def.CheckCobroMonedaOperacion.equalsIgnoreCase("S")) {
					cobro.IdMonedaCobro = operacion.IdMonedaOperacion;
				}
				else {
					if (def.IdMonedaCobroOtraMoneda == null) throw new Exception("Moneda cobro no configurada");
					cobro.IdMonedaCobro = def.IdMonedaCobroOtraMoneda;
				}
			}
			
			cobro.MontoCobroCalculado = BigDecimal.ZERO;
			cobro.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
			cobro.MontoCobroCalculadoLocal = BigDecimal.ZERO;
			cobro.ValorCobroFinal = BigDecimal.ZERO;
			cobro.ValorCobroPerfil = BigDecimal.ZERO;
			hashCobrosSalida.put(cobro.IdInternoCobro, cobro);
			salida.p_out_Cobros.add(cobro);
		}
		
		//CALCULO DE COBROS (EXCEPTO IMPUESTOS VARIABLES)
		for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
			
			if (def.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO") && def.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) {
				continue;
			}
			
			InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
			Cobro_Salida cobro = hashCobrosSalida.get(def.IdInternoCobro);
			
			//MONTO FIJO
			if (def.IdTipoCobro.equalsIgnoreCase("MONTOFIJO")) {
				if (def.MontoFijo_CheckMontoOperacionMenor.equalsIgnoreCase("S")) {
					if (operacion.MontoOperacion.compareTo(def.MontoFijo_ValorMontoOperacionMenor) == -1) {
						cobro.ValorCobroPerfil = def.MontoFijo_MontoCobro;
					} 
					else 
					{
						if (def.MontoFijo_CheckSiNo.equalsIgnoreCase("S")) {
							cobro.ValorCobroPerfil = def.MontoFijo_ValorMontoSiNo;
						}
						else {
							cobro.ValorCobroPerfil = BigDecimal.ZERO;
						}
					}
				} 
				else {
					cobro.ValorCobroPerfil = def.MontoFijo_MontoCobro;
				}
				
				if (def.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(cobro.ValorCobroPerfil) == 1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else if (def.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(cobro.ValorCobroPerfil) == -1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else {
					cobro.ValorCobroFinal = cobro.ValorCobroPerfil;
				}
				
				cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, cobro.ValorCobroFinal, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			}	
			
			//puede venir sin allin, en el caso que modifique los cobros
			if (def.IdTipoCobro.equalsIgnoreCase("MONTOFIJOPORUNIDAD")) {
				
				cobro.ValorCobroPerfil = def.MontoFijoPorUnidad_MontoCobro;
				
				//SI VIENE POR ALL IN, NOS SALTAMOS ESTA PARTE
				if (!def.MontoFijoPorUnidad_AllInCheckValor.equalsIgnoreCase("S") || (operacion.MontoAllIn == null && operacion.PorcentajeAllIn == null)) {
					
					if (def.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(cobro.ValorCobroPerfil) == 1) {
						cobro.ValorCobroFinal = def.ValorCobroAplicado;
					}
					else if (def.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(cobro.ValorCobroPerfil) == -1) {
						cobro.ValorCobroFinal = def.ValorCobroAplicado;
					}
					else {
						cobro.ValorCobroFinal = def.MontoFijoPorUnidad_MontoCobro;
					}
					
					BigDecimal montoCobro = cobro.ValorCobroFinal.multiply(operacion.CantidadOperacion);
					cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
				}
			}
			
			//PORCENTAJE TASA/PLAZO
			if (def.IdTipoCobro.equalsIgnoreCase("PORCENTAJETASAPLAZO")) {
				
				cobro.ValorCobroPerfil = def.PorcentualTasaPlazo_ValorCobro;
				
				if (def.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualTasaPlazo_ValorCobro) == 1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else if (def.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualTasaPlazo_ValorCobro) == -1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else {
					cobro.ValorCobroFinal = cobro.ValorCobroPerfil;
				}
				
				if ("S".equalsIgnoreCase(def.Otros_CheckInteresSimple)){
					BigDecimal plazo = new BigDecimal(operacion.PlazoEnDias);
					BigDecimal ValorCobro = cobro.ValorCobroFinal.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					cobro.ValorCobroFinal = ValorCobro.multiply(plazo);
				}
				else if ("S".equalsIgnoreCase(def.Otros_CheckInteresCompuesto)){
					BigDecimal plazo = new BigDecimal(operacion.PlazoEnDias);
					BigDecimal baseTasa =  new BigDecimal(def.ValorBaseTasaCobro);
					BigDecimal ValorCobro = BigDecimal.ONE.add(cobro.ValorCobroFinal.divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
					BigDecimal periodos = (plazo.divide(baseTasa,20,RoundingMode.HALF_UP));
					cobro.ValorCobroFinal = (new BigDecimal(Math.pow(ValorCobro.doubleValue(),periodos.doubleValue()))).subtract(new BigDecimal(1L));
				}
				else {
					cobro.ValorCobroFinal = cobro.ValorCobroFinal.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
				}
				
				BigDecimal montoCobroMonedaOperacion = operacion.MontoOperacionPlazo.multiply(cobro.ValorCobroFinal);
				BigDecimal montoDestino = null;
				
				if (cobro.IdMonedaCobro.equalsIgnoreCase(operacion.IdMonedaOperacion)) {
					montoDestino = montoCobroMonedaOperacion;
				}
				else {
					CalcularParidad paridadService = new CalcularParidad();
					CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
					calculoEntrada.IdInterno = cobro.IdInternoCobro;
					calculoEntrada.IdMonedaDestino = cobro.IdMonedaCobro;
					calculoEntrada.IdUso = "ORDENES";
					calculoEntrada.IdMonedaOrigen = operacion.IdMonedaOperacion;
					calculoEntrada.IdTrabajo = IdTrabajo;
					calculoEntrada.MontoOrigen = montoCobroMonedaOperacion;
					calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
					paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
					paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
					paridadEntrada.p_IdTrabajo = IdTrabajo++;
					CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
					montoDestino = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
				}
				
				cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, montoDestino, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			}
			
			//PORCENTAJEFIJO
			if (def.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) {
				
				cobro.ValorCobroPerfil = def.PorcentualFijo_ValorCobro;
				BigDecimal numeroCalculo = null;
				
				//SOBRE EL MONTO
				if (def.PorcentualFijo_CheckSobreMonto.equalsIgnoreCase("S")) {
					numeroCalculo = operacion.MontoOperacion;
				}
				//SOBRE LA CANTIDAD
				else if (def.PorcentualFijo_CheckSobreCantidad.equalsIgnoreCase("S")) {
					numeroCalculo = operacion.CantidadOperacion;
				}
				//MAYOR ENTRE CANTIDAD Y MONTO
				else if (def.PorcentualFijo_CheckMayorEntreDos.equalsIgnoreCase("S")) {
					
					if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == 1) {
						numeroCalculo = operacion.MontoOperacion;
					}
					else if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == -1) {
						numeroCalculo = operacion.CantidadOperacion;
					}
					else if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == 0) {
						numeroCalculo = operacion.CantidadOperacion;
					}
				}
				//MENOR ENTRE CANTIDAD Y MONTO
				else if (def.PorcentualFijo_CheckMenorEntreDos != null && def.PorcentualFijo_CheckMenorEntreDos.equalsIgnoreCase("S")) {
					if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == -1) {
						numeroCalculo = operacion.MontoOperacion;
					}
					else if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == 1) {
						numeroCalculo = operacion.CantidadOperacion;
					}
					else if (operacion.MontoOperacion.compareTo(operacion.CantidadOperacion) == 0) {
						numeroCalculo = operacion.CantidadOperacion;
					}
				}
				//cobro mal configurado, asumimos porcentaje del monto
				else {
					numeroCalculo = operacion.MontoOperacion;
				}
				
				if (def.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualFijo_ValorCobro) == 1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else if (def.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualFijo_ValorCobro) == -1) {
					cobro.ValorCobroFinal = def.ValorCobroAplicado;
				}
				else {
					cobro.ValorCobroFinal = cobro.ValorCobroPerfil;
				}
				
				BigDecimal montoCobroMonedaOperacion = cobro.ValorCobroFinal.multiply(numeroCalculo).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
				BigDecimal montoCobro = null;
				BigDecimal montoMaximo = null;
				BigDecimal montoMinimo = null;
				
				
				if (!cobro.IdMonedaCobro.equalsIgnoreCase(operacion.IdMonedaOperacion)) {
					CalcularParidad paridadService = new CalcularParidad();
					CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
					calculoEntrada.IdInterno = cobro.IdInternoCobro;
					calculoEntrada.IdMonedaDestino = cobro.IdMonedaCobro;
					calculoEntrada.IdUso = "ORDENES";
					calculoEntrada.IdMonedaOrigen = operacion.IdMonedaOperacion;
					calculoEntrada.IdTrabajo = IdTrabajo;
					calculoEntrada.MontoOrigen = montoCobroMonedaOperacion;
					calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
					paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
					paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
					paridadEntrada.p_IdTrabajo = IdTrabajo++;
					CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
					montoCobro = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
				}
				else {
					montoCobro = montoCobroMonedaOperacion;
				}
				
				if (def.PorcentualFijo_IdMonedaMinimoMaximo != null && !def.PorcentualFijo_IdMonedaMinimoMaximo.equalsIgnoreCase("") && def.PorcentualFijo_CheckMontoMaximo.equalsIgnoreCase("S") && def.PorcentualFijo_ValorMontoMaximo != null) {
					if (cobro.IdMonedaCobro.equalsIgnoreCase(def.PorcentualFijo_IdMonedaMinimoMaximo)) {
						montoMaximo = def.PorcentualFijo_ValorMontoMaximo;
					}
					else {
						CalcularParidad paridadService = new CalcularParidad();
						CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
						calculoEntrada.IdInterno = cobro.IdInternoCobro;
						calculoEntrada.IdMonedaDestino = cobro.IdMonedaCobro;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = def.PorcentualFijo_IdMonedaMinimoMaximo;
						calculoEntrada.IdTrabajo = IdTrabajo;
						calculoEntrada.MontoOrigen = def.PorcentualFijo_ValorMontoMaximo;
						calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						paridadEntrada.p_IdTrabajo = IdTrabajo++;
						CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
						montoMaximo = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
					}
				}
				
				if (def.PorcentualFijo_IdMonedaMinimoMaximo != null && !def.PorcentualFijo_IdMonedaMinimoMaximo.equalsIgnoreCase("") && def.PorcentualFijo_CheckMontoMinimo.equalsIgnoreCase("S") && def.PorcentualFijo_ValorMontoMinimo != null) {
					if (cobro.IdMonedaCobro.equalsIgnoreCase(def.PorcentualFijo_IdMonedaMinimoMaximo)) {
						montoMinimo = def.PorcentualFijo_ValorMontoMinimo;
					}
					else {
						CalcularParidad paridadService = new CalcularParidad();
						CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
						calculoEntrada.IdInterno = cobro.IdInternoCobro;
						calculoEntrada.IdMonedaDestino = cobro.IdMonedaCobro;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = def.PorcentualFijo_IdMonedaMinimoMaximo;
						calculoEntrada.IdTrabajo = IdTrabajo;
						calculoEntrada.MontoOrigen = def.PorcentualFijo_ValorMontoMinimo;
						calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						paridadEntrada.p_IdTrabajo = IdTrabajo++;
						CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
						montoMinimo = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
					}
				}
				
				if (montoMaximo != null && montoCobro.compareTo(montoMaximo) == 1) {
					cobro.MontoCobroCalculado = montoMaximo;
				}
				else if (montoMinimo != null && montoCobro.compareTo(montoMinimo) == -1) {
					cobro.MontoCobroCalculado = montoMinimo;
				}
				else {
					cobro.MontoCobroCalculado = montoCobro;
				}
				
				cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, cobro.MontoCobroCalculado, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			}
			//PORCENTAJERANGOS
			if (def.IdTipoCobro.equalsIgnoreCase("PORCENTAJERANGOS")) {
				//uso tabla de rangos
				BigDecimal numeroComparacion = null;
				String monedaCobro = cobro.IdMonedaCobro;
				//plazo
				if ("Plazo".equalsIgnoreCase(def.PorcentualRango_IdTipoRangoCobro)){
					numeroComparacion = new BigDecimal(operacion.PlazoEnDias);
				}
				//monto
				if ("Monto".equalsIgnoreCase(def.PorcentualRango_IdTipoRangoCobro)){
					if (!"".equals(def.PorcentualRango_IdMonedaRangoMonto) && !def.PorcentualRango_IdMonedaRangoMonto.equalsIgnoreCase(operacion.IdMonedaOperacion)){
						CalcularParidad paridadService = new CalcularParidad();
						CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
						calculoEntrada.IdInterno = cobro.IdInternoCobro;
						calculoEntrada.IdMonedaDestino = def.PorcentualRango_IdMonedaRangoMonto;//cobro.IdMonedaCobro;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = operacion.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = IdTrabajo;
						calculoEntrada.MontoOrigen = operacion.MontoOperacion;//montoCobroMonedaOperacion;
						calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						paridadEntrada.p_IdTrabajo = IdTrabajo++;
						CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
						numeroComparacion = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
						
					}
					else 
						numeroComparacion = operacion.MontoOperacion;
					
				}
				//cantidad
				if ("Cantidad".equalsIgnoreCase(def.PorcentualRango_IdTipoRangoCobro)){
					numeroComparacion = operacion.CantidadOperacion;
				}
				
				for (DefinicionRangoCobro ran : def.RangoCobros){
					if (numeroComparacion.compareTo(ran.Hasta) == -1 || numeroComparacion.compareTo(ran.Hasta) == 0){
						System.out.println(ran.Hasta);
						cobro.ValorCobroFinal = ran.Valor;
						cobro.ValorCobroPerfil = ran.Valor;
					}
					
				}
				BigDecimal MontoOperacion = BigDecimal.ZERO;
				if ("S".equalsIgnoreCase(def.Otros_CheckOperacionPlazo)) {
					MontoOperacion = operacion.MontoOperacionPlazo;
				}
				if ("S".equalsIgnoreCase(def.Otros_CheckOperacionContado)) {
					MontoOperacion = operacion.MontoOperacion;
				}
				BigDecimal montoCobroMonedaOperacion = cobro.ValorCobroFinal.multiply(MontoOperacion).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
				BigDecimal montoCobro = null;
				if (!cobro.IdMonedaCobro.equalsIgnoreCase(operacion.IdMonedaOperacion)) {//falta moneda rango por monto
					CalcularParidad paridadService = new CalcularParidad();
					CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
					calculoEntrada.IdInterno = cobro.IdInternoCobro;
					calculoEntrada.IdMonedaDestino = cobro.IdMonedaCobro;
					calculoEntrada.IdUso = "ORDENES";
					calculoEntrada.IdMonedaOrigen = operacion.IdMonedaOperacion;
					calculoEntrada.IdTrabajo = IdTrabajo;
					calculoEntrada.MontoOrigen = montoCobroMonedaOperacion;
					calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
					paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
					paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
					paridadEntrada.p_IdTrabajo = IdTrabajo++;
					CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
					montoCobro = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
				}
				else {
					montoCobro = montoCobroMonedaOperacion;
				}
				cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
			}
			
			
		}
		
		
		
		
		//MONTO AFECTOS A IMPUESTO
		//HashMap<String,BigDecimal> montoAfectoImpuesto = new HashMap<>();
		//HashMap<String,BigDecimal> montoAfectoImpuestoPlazo = new HashMap<>();
		
		HashMap<Long,HashMap<String,BigDecimal>> montoAfectoImpuesto = new HashMap<>();
		HashMap<Long,HashMap<String,BigDecimal>> montoAfectoImpuestoPlazo = new HashMap<>();
		
		for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
			
			if (!montoAfectoImpuesto.containsKey(def.IdInterno)) {
				montoAfectoImpuesto.put(def.IdInterno, new HashMap<String,BigDecimal>());
			}
			
			if (!montoAfectoImpuestoPlazo.containsKey(def.IdInterno)) {
				montoAfectoImpuestoPlazo.put(def.IdInterno, new HashMap<String,BigDecimal>());
			}
			
			if (!def.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO") && def.Otros_CheckAfectoImpuesto.equalsIgnoreCase("S") && def.Otros_CheckAllIn.equalsIgnoreCase("N")) {
				
				Cobro_Salida cobro = hashCobrosSalida.get(def.IdInternoCobro);
				
				if (!(montoAfectoImpuesto.get(cobro.IdInterno).containsKey(cobro.IdMonedaCobro))) {
					montoAfectoImpuesto.get(cobro.IdInterno).put(cobro.IdMonedaCobro, BigDecimal.ZERO);
				}
				
				if (!(montoAfectoImpuestoPlazo.get(cobro.IdInterno).containsKey(cobro.IdMonedaCobro))) {
					montoAfectoImpuestoPlazo.get(cobro.IdInterno).put(cobro.IdMonedaCobro, BigDecimal.ZERO);
				}
				
				//si los dos son N, asumimos contado
				if (def.Otros_CheckOperacionContado.equalsIgnoreCase("N") && def.Otros_CheckOperacionPlazo.equalsIgnoreCase("N")) {
					def.Otros_CheckOperacionContado = "S";
				}
				
				if (def.Otros_CheckOperacionContado.equalsIgnoreCase("S")) {
					BigDecimal monto = montoAfectoImpuesto.get(def.IdInterno).get(cobro.IdMonedaCobro);
					monto = monto.add(cobro.MontoCobroCalculado);
					montoAfectoImpuesto.get(def.IdInterno).put(cobro.IdMonedaCobro, monto);
				}
				else if (def.Otros_CheckOperacionPlazo.equalsIgnoreCase("S")) {
					
					BigDecimal monto = montoAfectoImpuestoPlazo.get(def.IdInterno).get(cobro.IdMonedaCobro);
					monto = monto.add(cobro.MontoCobroCalculado);
					montoAfectoImpuestoPlazo.get(def.IdInterno).put(cobro.IdMonedaCobro, monto);
					
				}
			}
		}
		
		//impuestos
		HashMap<Long,BigDecimal> sumaImpuestosPorcentajeFijo = new HashMap<>();
		for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
			
			if (! (def.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO") && def.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) ) {
				continue;
			}
			
			Cobro_Salida impuesto = hashCobrosSalida.get(def.IdInternoCobro);
			InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
			
			
			impuesto.ValorCobroPerfil = def.PorcentualFijo_ValorCobro;
			
			if (def.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualFijo_ValorCobro) == 1) {
				impuesto.ValorCobroFinal = def.ValorCobroAplicado;
			}
			else if (def.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S") && def.ValorCobroAplicado != null && def.ValorCobroAplicado.compareTo(def.PorcentualFijo_ValorCobro) == -1) {
				impuesto.ValorCobroFinal = def.ValorCobroAplicado;
			}
			else {
				impuesto.ValorCobroFinal = impuesto.ValorCobroPerfil;
			}
			
			
			HashMap<String, BigDecimal> montoAfectoImpuestoTemp = null;
			if (def.Otros_CheckOperacionContado.equalsIgnoreCase("S")) {
				montoAfectoImpuestoTemp = montoAfectoImpuesto.get(def.IdInterno);
			}
			else if (def.Otros_CheckOperacionPlazo.equalsIgnoreCase("S")) {
				montoAfectoImpuestoTemp = montoAfectoImpuestoPlazo.get(def.IdInterno);
			}
			else {
				montoAfectoImpuestoTemp = montoAfectoImpuesto.get(def.IdInterno);
			}
			
			if (!sumaImpuestosPorcentajeFijo.containsKey(impuesto.IdInterno)) {
				sumaImpuestosPorcentajeFijo.put(impuesto.IdInterno,BigDecimal.ZERO);
			}
			
			//guardamos los porcentajes para al calculo del allin
			BigDecimal porcentajeImpuesto = sumaImpuestosPorcentajeFijo.get(impuesto.IdInterno);
			porcentajeImpuesto = porcentajeImpuesto.add(impuesto.ValorCobroFinal);
			sumaImpuestosPorcentajeFijo.put(impuesto.IdInterno, porcentajeImpuesto);
			
			
			List<CalculoEntrada> calculosEntradaParidad = new ArrayList<>();
			for (String moneda : montoAfectoImpuestoTemp.keySet()) {
				if (moneda.equalsIgnoreCase(impuesto.IdMonedaCobro)) {
					BigDecimal montoDestino = montoAfectoImpuestoTemp.get(moneda).multiply(impuesto.ValorCobroFinal).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					impuesto.MontoCobroCalculado = impuesto.MontoCobroCalculado.add(montoDestino);
					impuesto.MontoCobroCalculado = MonedaFormato.formatear(conn, impuesto.MontoCobroCalculado, MonedaFormato.MONTO,impuesto.IdMonedaCobro).valor;
					
				}
				else {
					
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
					calculoEntrada.IdInterno = impuesto.IdInternoCobro;
					calculoEntrada.IdMonedaDestino = impuesto.IdMonedaCobro;
					calculoEntrada.IdUso = "ORDENES";
					calculoEntrada.IdMonedaOrigen = moneda;
					calculoEntrada.IdTrabajo = IdTrabajo;
					calculoEntrada.MontoOrigen = montoAfectoImpuestoTemp.get(moneda);
					calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
					calculosEntradaParidad.add(calculoEntrada);
				}
			}
			
			if (calculosEntradaParidad.size() > 0) {
				CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
				paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				paridadEntrada.p_IdTrabajo = IdTrabajo++;
				paridadEntrada.p_CalculosEntrada = calculosEntradaParidad;
				CalcularParidad paridadService = new CalcularParidad();
				CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
				
				for (CalculoSalida calc : paridadSalida.p_out_CalculosSalida) {
					BigDecimal montoDestino = calc.MontoDestino.multiply(impuesto.ValorCobroFinal).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					impuesto.MontoCobroCalculado = impuesto.MontoCobroCalculado.add(montoDestino);
					impuesto.MontoCobroCalculado = MonedaFormato.formatear(conn, impuesto.MontoCobroCalculado, MonedaFormato.MONTO,impuesto.IdMonedaCobro).valor;
				}
			}
		}

		
		//ARREGLAR ALL IN, CUANDO ASIGNAMOS NO ES NECESARIO, YA VIENEN LOS COBROS LISTOS
		{
			for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
				
				//ALL IN POR MONTO o PORCENTAJE
				if (def.MontoFijoPorUnidad_AllInCheckValor.equalsIgnoreCase("S") || def.Otros_CheckAllIn.equalsIgnoreCase("S")) {
					
					Cobro_Salida cobroAllin = hashCobrosSalida.get(def.IdInternoCobro);
					InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
					Long idOperacion = cobroAllin.IdInterno;
					
					//OBTENER MONTO OPERACION EN MONEDA DEL ALL IN PARA CALCULAR EL PORCENTAJE REAL
					BigDecimal montoOperacionMonedaAllIn = BigDecimal.ZERO;
					
					if (operacion.IdMonedaOperacion.equalsIgnoreCase(cobroAllin.IdMonedaCobro)) {
						montoOperacionMonedaAllIn = operacion.MontoOperacion;
					}
					else {
						CalcularParidad paridadService = new CalcularParidad();
						CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
						calculoEntrada.IdInterno = operacion.IdInterno;
						calculoEntrada.IdMonedaDestino = cobroAllin.IdMonedaCobro;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = operacion.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = IdTrabajo;
						calculoEntrada.MontoOrigen = operacion.MontoOperacion;
						calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						paridadEntrada.p_IdTrabajo = IdTrabajo++;
						CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
						montoOperacionMonedaAllIn = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
					}
					
					//MINIMO DE ALL IN
					BigDecimal montoMinimoAllIn = null;
					BigDecimal montoMaximoAllIn = null;
					if (def.PorcentualFijo_IdMonedaMinimoMaximo != null && !def.PorcentualFijo_IdMonedaMinimoMaximo.equalsIgnoreCase("") && def.PorcentualFijo_CheckMontoMinimo.equalsIgnoreCase("S") && def.PorcentualFijo_ValorMontoMinimo != null) {
						if (cobroAllin.IdMonedaCobro.equalsIgnoreCase(def.PorcentualFijo_IdMonedaMinimoMaximo)) {
							montoMinimoAllIn = def.PorcentualFijo_ValorMontoMinimo;
						}
						else {
							CalcularParidad paridadService = new CalcularParidad();
							CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
							CalculoEntrada calculoEntrada = new CalculoEntrada();
							calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
							calculoEntrada.IdInterno = cobroAllin.IdInternoCobro;
							calculoEntrada.IdMonedaDestino = cobroAllin.IdMonedaCobro;
							calculoEntrada.IdUso = "ORDENES";
							calculoEntrada.IdMonedaOrigen = def.PorcentualFijo_IdMonedaMinimoMaximo;
							calculoEntrada.IdTrabajo = IdTrabajo;
							calculoEntrada.MontoOrigen = def.PorcentualFijo_ValorMontoMinimo;
							calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
							paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
							paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
							paridadEntrada.p_IdTrabajo = IdTrabajo++;
							CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
							montoMinimoAllIn = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
						}
					}
					
					//MAXIMO DE ALL IN
					if (def.PorcentualFijo_IdMonedaMinimoMaximo != null && !def.PorcentualFijo_IdMonedaMinimoMaximo.equalsIgnoreCase("") && def.PorcentualFijo_CheckMontoMaximo.equalsIgnoreCase("S") && def.PorcentualFijo_ValorMontoMaximo != null) {
						if (cobroAllin.IdMonedaCobro.equalsIgnoreCase(def.PorcentualFijo_IdMonedaMinimoMaximo)) {
							montoMaximoAllIn = def.PorcentualFijo_ValorMontoMaximo;
						}
						else {
							CalcularParidad paridadService = new CalcularParidad();
							CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
							CalculoEntrada calculoEntrada = new CalculoEntrada();
							calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
							calculoEntrada.IdInterno = cobroAllin.IdInternoCobro;
							calculoEntrada.IdMonedaDestino = cobroAllin.IdMonedaCobro;
							calculoEntrada.IdUso = "ORDENES";
							calculoEntrada.IdMonedaOrigen = def.PorcentualFijo_IdMonedaMinimoMaximo;
							calculoEntrada.IdTrabajo = IdTrabajo;
							calculoEntrada.MontoOrigen = def.PorcentualFijo_ValorMontoMaximo;
							calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
							paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
							paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
							paridadEntrada.p_IdTrabajo = IdTrabajo++;
							CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
							montoMaximoAllIn = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
						}
					}
					
					//monto AllIn se asume en la moneda del cobro all in
					BigDecimal montoAllIn = BigDecimal.ZERO;
					BigDecimal montoAllInVerificacion = BigDecimal.ZERO;
					if (operacion.MontoAllIn != null) {
						montoAllIn = operacion.MontoAllIn;
					}
					else if(operacion.PorcentajeAllIn != null) {
						montoAllIn = operacion.PorcentajeAllIn.multiply(montoOperacionMonedaAllIn).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					}
					else if(operacion.PorcentajeAllIn == null && def.Otros_CheckAllIn.equalsIgnoreCase("S")) {
						montoAllIn = cobroAllin.ValorCobroFinal.multiply(montoOperacionMonedaAllIn).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);;
					}
					else {
						//es all in pero no llego el porcentaje ni el monto
						continue;
					}
					
					if (montoMinimoAllIn != null && montoAllIn.compareTo(montoMinimoAllIn) == -1) {
						montoAllIn = montoMinimoAllIn;
					}
					
					if (montoMaximoAllIn != null && montoAllIn.compareTo(montoMaximoAllIn) == 1) {
						montoAllIn = montoMaximoAllIn;
					}
					
					//recorremos los otros cobros de la operacion, restando del total
					for (DefinicionCobro_Entrada defOtros : entrada.p_Cobros) { //TODO ignorar los cobros plazo
						if (!defOtros.IdInterno.equals(idOperacion) || defOtros.IdInternoCobro.equals(cobroAllin.IdInternoCobro)) {
							continue;
						}
						
						Cobro_Salida cobro = hashCobrosSalida.get(defOtros.IdInternoCobro);
						//si esta en al misma moneda guardamos, si no cambiamos
						if (cobro.IdMonedaCobro.equalsIgnoreCase(cobroAllin.IdMonedaCobro)) {
							montoAllIn = montoAllIn.subtract(cobro.MontoCobroCalculado);
							montoAllInVerificacion = montoAllInVerificacion.add(cobro.MontoCobroCalculado);
						}
						else {
							
							CalcularParidad paridadService = new CalcularParidad();
							CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
							CalculoEntrada calculoEntrada = new CalculoEntrada();
							calculoEntrada.Fecha = Fecha.toStringDate(operacion.FechaOperacion);
							calculoEntrada.IdInterno = cobro.IdInternoCobro;
							calculoEntrada.IdMonedaDestino = cobroAllin.IdMonedaCobro;
							calculoEntrada.IdUso = "ORDENES";
							calculoEntrada.IdMonedaOrigen = cobro.IdMonedaCobro;
							calculoEntrada.IdTrabajo = IdTrabajo;
							calculoEntrada.MontoOrigen = cobro.MontoCobroCalculado;
							calculoEntrada.ValorCambioLocalModificado = operacion.TipoCambio;
							paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
							paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
							paridadEntrada.p_IdTrabajo = IdTrabajo++;
							CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
							BigDecimal montoCobroMonedaAllin = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
							montoAllIn = montoAllIn.subtract(montoCobroMonedaAllin);
							montoAllInVerificacion = montoAllInVerificacion.add(montoCobroMonedaAllin);
							
						}
						
						if (montoAllIn.compareTo(BigDecimal.ZERO) == -1) {
							montoAllIn = BigDecimal.ZERO;
							montoAllInVerificacion = BigDecimal.ZERO;
						}
					}
					
					//tenemos monto cobro final
					//hay que calcular el valor del factor
					if (montoAllIn.compareTo(BigDecimal.ZERO) == 1) {
						
						BigDecimal sumaImpuestosOperacion = sumaImpuestosPorcentajeFijo.get(operacion.IdInterno);
						if (sumaImpuestosOperacion == null) {
							sumaImpuestosOperacion = BigDecimal.ZERO;
						}
						
						sumaImpuestosOperacion = sumaImpuestosOperacion.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						
						BigDecimal divisor = BigDecimal.ONE.add(sumaImpuestosOperacion);
						
						
						BigDecimal cobroAllinFinal = montoAllIn.divide(divisor,20,RoundingMode.HALF_UP);
						cobroAllinFinal = MonedaFormato.formatear(conn, cobroAllinFinal, MonedaFormato.MONTO,cobroAllin.IdMonedaCobro).valor;
						
						for (DefinicionCobro_Entrada defImpuesto : entrada.p_Cobros) {
							
							if (!defImpuesto.IdInterno.equals(idOperacion) || !(defImpuesto.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO") && defImpuesto.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) ) {
								continue;
							}
							//TODO PASAR COBRO ALL IN A MONEDA DEL IMPUESTO
							Cobro_Salida impuesto = hashCobrosSalida.get(defImpuesto.IdInternoCobro);
							BigDecimal montoImpuesto = cobroAllinFinal.multiply(impuesto.ValorCobroFinal).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
							montoImpuesto = MonedaFormato.formatear(conn, montoImpuesto, MonedaFormato.MONTO,impuesto.IdMonedaCobro).valor;
							impuesto.MontoCobroCalculado = impuesto.MontoCobroCalculado.add(montoImpuesto);
							impuesto.MontoCobroCalculado = MonedaFormato.formatear(conn, impuesto.MontoCobroCalculado, MonedaFormato.MONTO,impuesto.IdMonedaCobro).valor;
							montoAllInVerificacion = montoAllInVerificacion.add(montoImpuesto);
							
						}
						
						cobroAllin.MontoCobroCalculado = MonedaFormato.formatear(conn, cobroAllinFinal, MonedaFormato.MONTO,cobroAllin.IdMonedaCobro).valor;
						montoAllInVerificacion = montoAllInVerificacion.add(cobroAllin.MontoCobroCalculado);
						
						if (def.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) {
							cobroAllin.ValorCobroFinal = cobroAllin.MontoCobroCalculado.multiply(new BigDecimal(100)).divide(montoOperacionMonedaAllIn,20,RoundingMode.HALF_UP);
						}
						else if (def.IdTipoCobro.equalsIgnoreCase("MONTOFIJOPORUNIDAD")){
							cobroAllin.ValorCobroFinal = cobroAllin.MontoCobroCalculado.divide(operacion.CantidadOperacion,20,RoundingMode.HALF_UP);
						}
					}
					else {
						cobroAllin.ValorCobroFinal = BigDecimal.ZERO;
						cobroAllin.MontoCobroCalculado = BigDecimal.ZERO;
					}
					
					//crear operacion salida con monto y valor allin, en este caso el monto all in esta en la moneda del cobro all in
					Operacion_Salida opSalida = new Operacion_Salida();
					opSalida.IdInterno = operacion.IdInterno;
					opSalida.MontoAllIn = montoAllInVerificacion;
					opSalida.PorcentajeAllIn = null;// se calcula al final con los montos de liquidacion
					
					if (!hashOperacionesSalida.containsKey(opSalida.IdInterno)) {
						hashOperacionesSalida.put(opSalida.IdInterno, opSalida);
						salida.p_out_operaciones.add(opSalida);
					}
					
				}
			}
		}
		
		{
			//CALCULAR MONTO LIQUIDACION Y LOCAL
			IdTrabajo++;
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = IdTrabajo;
			
			for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
				
				Cobro_Salida cobroSalida = hashCobrosSalida.get(def.IdInternoCobro);
				InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
				
				CalculoEntrada c1 = new CalculoEntrada();
				c1.Fecha = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");;
				c1.IdInterno = def.IdInternoCobro;
				c1.IdMonedaDestino = operacion.IdMonedaLiquidacion;
				c1.IdMonedaOrigen = cobroSalida.IdMonedaCobro;
				c1.IdTrabajo =IdTrabajo;
				c1.IdUso = "ORDENES";
				c1.MontoOrigen = cobroSalida.MontoCobroCalculado;
				c1.ValorCambioLocalModificado = operacion.TipoCambio;
				calcularParidadEntrada.p_CalculosEntrada.add(c1);
			}
			
			CalcularParidad calcularParidadService = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
			
			for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
				
				Cobro_Salida cobroSalida = hashCobrosSalida.get(calc.IdInterno);
				InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(cobroSalida.IdInterno);
				
				if (!calc.Resultado.equals("OK")) {
					String codigo = calc.CodigoMensaje;
					String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo)
							.add("IdMonedaOrigen", calc.IdMonedaOrigen)
							.add("IdMonedaDestino", calc.IdMonedaDestino)
							.msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					return salida;
				}
				else {
					cobroSalida.MontoCobroCalculadoLiquidacion = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO, operacion.IdMonedaLiquidacion).valor;
					cobroSalida.MontoCobroCalculadoLocal = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO, ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema")).valor;
				}
			}
			
		}
		
		{//COMISION INDIRECTA SAB
			//BigDecimal MontoCobrosContado = BigDecimal.ZERO;
			//BigDecimal MontoOperacionPlazo = BigDecimal.ZERO;
			boolean existeComIndirecta = false;
			for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
				InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
				Cobro_Salida cobro = hashCobrosSalida.get(def.IdInternoCobro);
				if (!def.IdTipoCobro.equalsIgnoreCase("COMISIONINDIRECTA")) {
					//ver el monto calculado con los dartos de plazo. Trabaja solo con la operacion plazo
					if ("S".equalsIgnoreCase(def.Otros_CheckOperacionPlazo)){
						if (operacion.AccionOrden.equalsIgnoreCase("VENTA")){//es reportado
						//MontoOperacionPlazo = MontoOperacionPlazo.add(cobro.MontoCobroCalculado);
							operacion.MontoLiquidacionOperacionPlazo = operacion.MontoLiquidacionOperacionPlazo.add(cobro.MontoCobroCalculadoLiquidacion);//operacion.MontoLiquidacionOperacionPlazo.add(cobro.MontoCobroCalculado);
						}
						else{//sie es compra
							operacion.MontoLiquidacionOperacionPlazo = operacion.MontoLiquidacionOperacionPlazo.subtract(cobro.MontoCobroCalculadoLiquidacion);//operacion.MontoLiquidacionOperacionPlazo.subtract(cobro.MontoCobroCalculado);
						}
						/*BigDecimal tasa = operacion.TasaReportado.divide(new BigDecimal(100),20,RoundingMode.HALF_UP).add(BigDecimal.ONE);
						BigDecimal BasePlazo = BigDecimal.ZERO;
						if (operacion.BaseTasa.compareTo(0L) != 0) {
							BasePlazo = new BigDecimal(operacion.PlazoEnDias).divide(new BigDecimal(operacion.BaseTasa),20,RoundingMode.HALF_UP); 
						}
												
						Double factor = Math.pow(tasa.doubleValue(), BasePlazo.doubleValue());
						operacion.MontoLiquidacionOperacion = operacion.MontoLiquidacionOperacionPlazo.divide(new BigDecimal(factor),20,RoundingMode.HALF_UP);*/
						//System.out.println(operacion.MontoLiquidacionOperacion);
						//System.out.println(operacion.MontoLiquidacionOperacionPlazo);
					}
					if ("S".equalsIgnoreCase(def.Otros_CheckOperacionContado)){
						//System.out.println("cobro="+def.CodigoDefinicionCobro+" " + cobro.MontoCobroCalculado);
						//if (operacion.AccionOrden.equalsIgnoreCase("VENTA")){//es reportado
						//	operacion.MontoTotalComExternaContado = operacion.MontoTotalComExternaContado.subtract(cobro.MontoCobroCalculadoLiquidacion);
						//}
						//else {// si es compra
							operacion.MontoTotalComExternaContado = operacion.MontoTotalComExternaContado.add(cobro.MontoCobroCalculadoLiquidacion);//operacion.MontoTotalComExternaContado.add(cobro.MontoCobroCalculado);
						//}
						//System.out.println(def.IdClaseCobroBase);
						if (def.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO")){
							operacion.ValorIGVContado = cobro.ValorCobroFinal;
						}
					}
					//System.out.println(operacion.MontoTotalComExternaContado);
				}
				else {
					existeComIndirecta = true;
				}
				
			}
			for (InformacionOperacion_Entrada operacion : entrada.p_Operaciones) {
				if (!existeComIndirecta) 
				{
					continue;
				}
				BigDecimal tasa = operacion.TasaReportado.divide(new BigDecimal(100),20,RoundingMode.HALF_UP).add(BigDecimal.ONE);
				BigDecimal BasePlazo = BigDecimal.ZERO;
				if (operacion.BaseTasa.compareTo(0L) != 0) {
					BasePlazo = new BigDecimal(operacion.PlazoEnDias).divide(new BigDecimal(operacion.BaseTasa),20,RoundingMode.HALF_UP); 
				}
											
				Double factor = Math.pow(tasa.doubleValue(), BasePlazo.doubleValue());
				operacion.MontoLiquidacionOperacionPlazo = operacion.MontoLiquidacionOperacionPlazo.add(operacion.MontoInteresPlazo);
				operacion.MontoLiquidacionOperacion = (operacion.MontoLiquidacionOperacionPlazo).divide(new BigDecimal(factor),20,RoundingMode.HALF_UP);
			}
			for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
				InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
				Cobro_Salida cobro = hashCobrosSalida.get(def.IdInternoCobro);
				if (def.IdTipoCobro.equalsIgnoreCase("COMISIONINDIRECTA")) {
					existeComIndirecta = true;
					//ver el monto calculado con los dartos de plazo. Trabaja solo con la operacion plazo
					if ("S".equalsIgnoreCase(def.Otros_CheckOperacionContado)){
						BigDecimal montoCobro =  BigDecimal.ZERO;
						if (operacion.AccionOrden.equalsIgnoreCase("VENTA")){
							montoCobro =  operacion.MontoOperacion.add(operacion.MontoInteresContado).subtract(operacion.MontoLiquidacionOperacion).subtract(operacion.MontoTotalComExternaContado);
						}
						else {//Compra
							montoCobro =  operacion.MontoLiquidacionOperacion.add(operacion.MontoInteresContado).subtract(operacion.MontoOperacion).subtract(operacion.MontoTotalComExternaContado);
						}
						BigDecimal igv =  operacion.ValorIGVContado.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
						montoCobro = montoCobro.divide(igv.add(BigDecimal.ONE),20,RoundingMode.HALF_UP);
						cobro.MontoCobroCalculado = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;
						cobro.MontoCobroCalculadoLiquidacion = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;//que lo calcule mas adelante
						//cobro.MontoCobroCalculadoLocal = MonedaFormato.formatear(conn, montoCobro, MonedaFormato.MONTO,cobro.IdMonedaCobro).valor;//que lo calcule mas adelante
						
						BigDecimal montoIgv = montoCobro.multiply(igv);
						operacion.ValorIGVContado = montoIgv;
					}
				}
			}
			for (DefinicionCobro_Entrada def : entrada.p_Cobros) {
				InformacionOperacion_Entrada operacion = (InformacionOperacion_Entrada) hashOperaciones.get(def.IdInterno);
				Cobro_Salida cobro = hashCobrosSalida.get(def.IdInternoCobro);
				if (def.IdClaseCobroBase.equalsIgnoreCase("IMPUESTO") && "S".equalsIgnoreCase(def.Otros_CheckOperacionContado) && existeComIndirecta){
					cobro.MontoCobroCalculado = cobro.MontoCobroCalculado.add(operacion.ValorIGVContado);
					cobro.MontoCobroCalculadoLiquidacion = cobro.MontoCobroCalculadoLiquidacion.add(operacion.ValorIGVContado);
				}
			}
			//System.out.println(MontoOperacionPlazo);
			//System.out.println(MontoCobrosContado);
			
		}
		
		{
			//SOLO PARA OPERACIONES SIN ALL IN
			for (InformacionOperacion_Entrada op : entrada.p_Operaciones) {
				Operacion_Salida opSalida = hashOperacionesSalida.get(op.IdInterno);
				BigDecimal montoAllin = BigDecimal.ZERO;
				
				//buscar moneda all in
				String IdMonedaAllIn = null;
				for (DefinicionCobro_Entrada cob : entrada.p_Cobros) {
					if (cob.MontoFijoPorUnidad_AllInCheckValor.equalsIgnoreCase("S") || cob.Otros_CheckAllIn.equalsIgnoreCase("S")) {
						IdMonedaAllIn = hashCobrosSalida.get(cob.IdInternoCobro).IdMonedaCobro;
						break;
					}
				}
				
				if (IdMonedaAllIn == null) {
					IdMonedaAllIn = op.IdMonedaLiquidacion; //si no hay all in los llevamos a moneda liquidacion
				}
				
				for (Cobro_Salida cob : salida.p_out_Cobros) {
					if (cob.IdInterno.equals(op.IdInterno)) {
						
						if (IdMonedaAllIn.equalsIgnoreCase(cob.IdMonedaCobro)) {
							montoAllin = montoAllin.add(cob.MontoCobroCalculadoLiquidacion);	
						}
						else {
							CalcularParidad paridadService = new CalcularParidad();
							CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
							CalculoEntrada calculoEntrada = new CalculoEntrada();
							calculoEntrada.Fecha = Fecha.toStringDate(op.FechaOperacion);
							calculoEntrada.IdInterno = cob.IdInternoCobro;
							calculoEntrada.IdMonedaDestino = IdMonedaAllIn;
							calculoEntrada.IdUso = "ORDENES";
							calculoEntrada.IdMonedaOrigen = cob.IdMonedaCobro;
							calculoEntrada.IdTrabajo = IdTrabajo;
							calculoEntrada.MontoOrigen = cob.MontoCobroCalculado;
							calculoEntrada.ValorCambioLocalModificado = op.TipoCambio;
							paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
							paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
							paridadEntrada.p_IdTrabajo = IdTrabajo++;
							CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
							BigDecimal montoCobroMonedaAllin = paridadSalida.p_out_CalculosSalida.get(0).MontoDestino;
							montoAllin = montoAllin.add(montoCobroMonedaAllin);
						}
					}
				}
				
				if (opSalida == null) {
					opSalida = new Operacion_Salida();
					opSalida.IdInterno = op.IdInterno;
					hashOperacionesSalida.put(opSalida.IdInterno, opSalida);
					salida.p_out_operaciones.add(opSalida );
				}
				
				opSalida.MontoAllIn = montoAllin;
				if (op.MontoLiquidacionOperacion == null || op.MontoLiquidacionOperacion.compareTo(BigDecimal.ZERO) == 0) {
					op.MontoLiquidacionOperacion = BigDecimal.ONE;
				}
				opSalida.PorcentajeAllIn = new BigDecimal(100).multiply(montoAllin).divide(op.MontoLiquidacionOperacion,20,RoundingMode.HALF_UP);
				
			}
		}
		
		
		//Calculo paridad para resumen
		{
			//retornocalculo ordenes contado y plazo (Liquidacion)
			for (Operacion_Salida operacion : salida.p_out_operaciones) {
				InformacionOperacion_Entrada operacionIn = (InformacionOperacion_Entrada) hashOperaciones.get(operacion.IdInterno);
				if (operacionIn != null){
					operacion.MontoOperacion = MonedaFormato.formatear(conn, operacionIn.MontoOperacion, MonedaFormato.MONTO, operacionIn.IdMonedaOperacion).valor;
					operacion.MontoOperacionPlazo = MonedaFormato.formatear(conn, operacionIn.MontoOperacionPlazo, MonedaFormato.MONTO, operacionIn.IdMonedaOperacion).valor;
					operacion.MontoLiquidacionOperacion = MonedaFormato.formatear(conn, operacionIn.MontoLiquidacionOperacion, MonedaFormato.MONTO, operacionIn.IdMonedaOperacion).valor;
					operacion.MontoLiquidacionOperacionPlazo = MonedaFormato.formatear(conn, operacionIn.MontoLiquidacionOperacionPlazo, MonedaFormato.MONTO, operacionIn.IdMonedaOperacion).valor;
					/*operacion.MontoOperacion = operacionIn.MontoOperacion;
					operacion.MontoOperacionPlazo = operacionIn.MontoOperacionPlazo;
					operacion.MontoLiquidacionOperacion = operacionIn.MontoLiquidacionOperacion;
					operacion.MontoLiquidacionOperacionPlazo = operacionIn.MontoLiquidacionOperacionPlazo;*/
				}
				/*
				 * public static class CalcularCobrosPe_Salida {
					public List<Cobro_Salida> p_out_Cobros;
					public List<Operacion_Salida> p_out_operaciones;
					public List<MensajeSalida> P_OUT_MENSAJES;	
				 */
			}
			/*
			CalcularParidad paridadService = new CalcularParidad();
			
			for (InformacionOperacion_Entrada parIn : entrada.p_Operaciones){
				CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
				paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				paridadEntrada.p_IdTrabajo = parIn.IdInterno;
				for (Operacion_Salida parOut : salida.p_out_operaciones){
					if (parIn.IdInterno.equals(parOut.IdInterno)){
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(parIn.FechaOperacion);
						calculoEntrada.IdInterno = 1L;
						calculoEntrada.IdMonedaDestino = parIn.IdMonedaLiquidacion;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = parIn.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = paridadEntrada.p_IdTrabajo;
						calculoEntrada.MontoOrigen = parOut.MontoOperacion;
						calculoEntrada.ValorCambioLocalModificado = parIn.TipoCambio;//null
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(parIn.FechaOperacion);
						calculoEntrada.IdInterno = 2L;
						calculoEntrada.IdMonedaDestino = parIn.IdMonedaLiquidacion;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = parIn.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = paridadEntrada.p_IdTrabajo;
						calculoEntrada.MontoOrigen = parOut.MontoOperacionPlazo;
						calculoEntrada.ValorCambioLocalModificado = parIn.TipoCambio;//null
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(parIn.FechaOperacion);
						calculoEntrada.IdInterno = 3L;
						calculoEntrada.IdMonedaDestino = parIn.IdMonedaLiquidacion;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = parIn.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = paridadEntrada.p_IdTrabajo;
						calculoEntrada.MontoOrigen = parOut.MontoLiquidacionOperacion;
						calculoEntrada.ValorCambioLocalModificado = parIn.TipoCambio;//null
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
						calculoEntrada = new CalculoEntrada();
						calculoEntrada.Fecha = Fecha.toStringDate(parIn.FechaOperacion);
						calculoEntrada.IdInterno = 4L;
						calculoEntrada.IdMonedaDestino = parIn.IdMonedaLiquidacion;
						calculoEntrada.IdUso = "ORDENES";
						calculoEntrada.IdMonedaOrigen = parIn.IdMonedaOperacion;
						calculoEntrada.IdTrabajo = paridadEntrada.p_IdTrabajo;
						calculoEntrada.MontoOrigen = parOut.MontoLiquidacionOperacionPlazo;
						calculoEntrada.ValorCambioLocalModificado = parIn.TipoCambio;//null
						paridadEntrada.p_CalculosEntrada.add(calculoEntrada);
					}
					if (paridadEntrada.p_CalculosEntrada.size() > 0){
						CalcularParidadSalida paridadSalida = paridadService.logica(conn, paridadEntrada);
						BigDecimal precio = BigDecimal.ZERO;
						for(CalculoSalida sal: paridadSalida.p_out_CalculosSalida){
							switch (sal.IdInterno.intValue()) {
							case 1:
								precio = parIn.MontoOperacion.divide(parIn.CantidadOperacion);
								parOut.MontoOperacion = MonedaFormato.formatear(conn, calculaMontoParidad(precio, parIn.CantidadOperacion, sal.FactorOrigenDestino), MonedaFormato.MONTO, sal.IdMonedaDestino).valor;
								break;
							case 2:
								precio = parIn.MontoOperacionPlazo.divide(parIn.CantidadOperacion);
								parOut.MontoOperacionPlazo = MonedaFormato.formatear(conn, calculaMontoParidad(precio, parIn.CantidadOperacion, sal.FactorOrigenDestino), MonedaFormato.MONTO, sal.IdMonedaDestino).valor;
								break;
							case 3:
								precio = parIn.MontoLiquidacionOperacion.divide(parIn.CantidadOperacion);
								parOut.MontoLiquidacionOperacion = MonedaFormato.formatear(conn, calculaMontoParidad(precio, parIn.CantidadOperacion, sal.FactorOrigenDestino), MonedaFormato.MONTO, sal.IdMonedaDestino).valor;
								break;
							case 4:
								precio = parIn.MontoLiquidacionOperacionPlazo.divide(parIn.CantidadOperacion);
								parOut.MontoLiquidacionOperacionPlazo = MonedaFormato.formatear(conn, calculaMontoParidad(precio, parIn.CantidadOperacion, sal.FactorOrigenDestino), MonedaFormato.MONTO, sal.IdMonedaDestino).valor;
								break;
							default:
								break;
							}
							
						}
					}
				}
			}*/
			
			
		}
		
		
		return salida;
	}
	
	public static class Cobro_Salida {
		public BigDecimal ValorCobroPerfil;
		public Long IdInterno;
		public Long IdInternoCobro;
		
		public String IdMonedaCobro;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public BigDecimal ValorCobroFinal;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Operacion_Salida {
		public Long IdInterno;
		public BigDecimal MontoAllIn;
		public BigDecimal PorcentajeAllIn;
		
		public BigDecimal		MontoOperacion = BigDecimal.ZERO;
		public BigDecimal		MontoOperacionPlazo = BigDecimal.ZERO;
		public BigDecimal		MontoLiquidacionOperacion = BigDecimal.ZERO;
		public BigDecimal		MontoLiquidacionOperacionPlazo = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionOperacion_Entrada {
		public java.sql.Date 	FechaOperacion;
		public Long 			PlazoEnDias;
		public BigDecimal		Tasa;
		public BigDecimal		TasaReportado;
		public Long				BaseTasa;
		public Long 			IdInterno;
		public BigDecimal 		TipoCambio;
		public String			IdMonedaLiquidacion;
		public String			IdMonedaOperacion;
		public BigDecimal		MontoOperacion;
		public BigDecimal		MontoOperacionPlazo;
		public BigDecimal		MontoLiquidacionOperacion;
		public BigDecimal		MontoLiquidacionOperacionPlazo;
		public BigDecimal		MontoTotalComExternaContado;
		public BigDecimal		ValorIGVContado;
		public BigDecimal		CantidadOperacion;
		public BigDecimal 		PorcentajeAllIn;
		public BigDecimal 		MontoAllIn;
		
		public BigDecimal		MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal		MontoInteresPlazo = BigDecimal.ZERO;
		public String			AccionOrden = "";//Compra - Venta
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionCobro_Entrada {
		public Long IdInterno;
		public Long IdInternoCobro;
		
		public BigDecimal ValorCobroAplicado;
		
		public String IdClaseCobro;
		public String IdClaseCobroBase;
		public String IdTipoCobro;
		public String CodigoDefinicionCobro;
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
		public BigDecimal	PorcentualTasaPlazo_ValorCobro;
		public String Otros_CheckAfectoImpuesto;
		public String Otros_CheckAllIn;
		public String Otros_CheckPermiteAumentarValor;
		public String Otros_CheckPermiteDisminuirValor;
		//public String PorcentualRango_IdMonedaRangoMonto;
		//public String PorcentualRango_IdTipoRangoCobro;
		public String Otros_CheckOperacionContado;
		public String Otros_CheckOperacionPlazo;
		
		//TODO IMPLEMENTAR POR RANGO
		public String PorcentualRango_IdMonedaRangoMonto;
		public String PorcentualRango_IdTipoRangoCobro;
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList();
		public String Otros_CheckInteresSimple;
		public String Otros_CheckInteresCompuesto;
		public Long ValorBaseTasaCobro;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		
		ConsultarCobrosPeru cc = new ConsultarCobrosPeru();
		ConsultarCobrosOperacionesEntrada entrada = new ConsultarCobrosOperacionesEntrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		InformacionOperacion e = new InformacionOperacion();
		e.IdBolsa = null;
		e.IdCorredoraCorresponsal = null;
		e.IdInterno = 0L;
		e.IdPortafolio = 1450084L;
		e.IdTipoCanalIngreso = "AGE";
		e.IdTipoOperacion = "VENACN";
		e.IdTipoProducto = "INAC";
		e.IdInstrumento = 502L;
		e.IdEmisor = "COPEC";
		entrada.p_Operaciones.add(e);
		ConsultarCobrosOperacionesSalida salida = cc.logica(con, entrada);
		
		
		CalcularCobrosPe cc1 = new CalcularCobrosPe();
		CalcularCobrosPe_Entrada entrada1 = new CalcularCobrosPe_Entrada();
		
		InformacionOperacion_Entrada op = new InformacionOperacion_Entrada();
		op.CantidadOperacion = new BigDecimal(1000);
		op.IdInterno = 0L;
		op.FechaOperacion = Fecha.toDate("2017-05-08");
		op.IdMonedaLiquidacion = "USD";
		op.IdMonedaOperacion = "PEN";
		op.MontoOperacion = new BigDecimal(1000000);
		op.PlazoEnDias = 0L;
		op.TipoCambio = null;
		
		entrada1.p_Operaciones.add(op);
		entrada1.p_IdEmpresa = 1L;
		List<DefinicionCobroSalida> a = salida.p_out_Cobros;
		Long idinternocobro = 0L;
		for (DefinicionCobroSalida cob1 : a) {
			DefinicionCobro_Entrada cob2 = new DefinicionCobro_Entrada();
			Utils.copyPojo(cob1, cob2);
			cob2.IdInternoCobro = idinternocobro++;
			entrada1.p_Cobros.add(cob2);
		}
		
		CalcularCobrosPe_Salida aa = cc1.logica(con, entrada1);
		System.out.println(aa.p_out_Cobros);
		
	}


}
