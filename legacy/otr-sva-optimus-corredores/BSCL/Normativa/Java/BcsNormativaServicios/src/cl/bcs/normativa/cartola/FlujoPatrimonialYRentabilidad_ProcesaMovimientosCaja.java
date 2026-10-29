package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad.FechaPrimerMovimientoPortafolio;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.TablaSalida;
import cl.bcs.normativa.cartola.pojo.SaldoCuentaCliente;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja implements JavaCall {
	private final Log log = new Log("NOR.FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja");
    public static class ProcesaMovimientosCaja_Entrada{
    	public Long p_IdEmpresa;
    	public List<DatosCliente> p_Datos;
    	public String p_FechaHasta;
    	List<HashMap<String, Object>> MovimientosCaja; 
    	HashMap<Long, FechaPrimerMovimientoPortafolio> FechasInicialesPortaFolioMovimientos;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ProcesaMovimientosCaja_Salida{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<SaldoCuentaCliente> P_OUT_SALIDA;
		
	public ProcesaMovimientosCaja_Salida(){
			this.P_OUT_SALIDA      =  new ArrayList<>();
			this.P_OUT_MENSAJES =  new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SaldoInicial {
		public String IdMoneda;
		public BigDecimal Monto = BigDecimal.ZERO;
		public Date FechaCreacionPortafolio;
		public Long IdPortafolio;
		public Long IdCliente;
		public Long Id;
   
		public BigDecimal getMonto() {
			return Monto;
		}
		public void setMonto(BigDecimal monto) {
			Monto = monto;
		}
		public Long getIdCliente() {
			return IdCliente;
		}
		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}
		public Long getId() {
			return Id;
		}
		public void setId(Long id) {
			Id = id;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class SaldoInicialCuenta{
		public Long IdCliente;
		public Long IdPortafolio;
		public String FechaCreacionPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesaMovimientosCaja_Entrada in = gson.fromJson(jin.toString(),ProcesaMovimientosCaja_Entrada.class);
		ProcesaMovimientosCaja_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// Utility function to get distinct IdCliente
	public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	@SuppressWarnings("unlikely-arg-type")
	public ProcesaMovimientosCaja_Salida  logica(Connection con, ProcesaMovimientosCaja_Entrada in) throws Exception {
		log.write("INICIO: FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja");
		ProcesaMovimientosCaja_Salida salida = new ProcesaMovimientosCaja_Salida();
		List<CalcularParidad.CalculoEntrada> listaParidadCargo = new ArrayList<CalcularParidad.CalculoEntrada>();
		HashMap<Long,BigDecimal> paridadCajaCliente = new HashMap<Long,BigDecimal>();
		SimpleDateFormat formatoFechaDia = new SimpleDateFormat("dd-MM-yyyy");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
		CalcularParidad CalculaParidad = new CalcularParidad();
		CalcularParidad.CalcularParidadEntrada ParidadEntrada = new CalcularParidad.CalcularParidadEntrada();
		ParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
		ParidadEntrada.p_IdTrabajo = 0L;
		Long id = 0L;
		
		//paridades de movimientos caja
		for(HashMap<String,Object> movimientosCajaCliente :in.MovimientosCaja) {
			if((!movimientosCajaCliente.get("idCliente").toString().equals(""))) {
				 id++; 
				 movimientosCajaCliente.put("Id", id);
				if (!movimientosCajaCliente.get("moneda").toString().equals("CLP")) {
					CalculoEntrada entradaCalParidadCargo = new CalculoEntrada();
					entradaCalParidadCargo.IdTrabajo = 0L;
					entradaCalParidadCargo.IdInterno = id;
					entradaCalParidadCargo.Fecha = LocalDate.parse(movimientosCajaCliente.get("fechaOperacion").toString(), formatter).toString();
					entradaCalParidadCargo.IdMonedaOrigen = (String) movimientosCajaCliente.get("moneda");
					entradaCalParidadCargo.IdMonedaDestino = "CLP"; // MONDEA A TRANSFORMAR
					entradaCalParidadCargo.ValorCambioLocalModificado = BigDecimal.ZERO;
					entradaCalParidadCargo.IdUso = "";
					if (((BigDecimal)movimientosCajaCliente.get("Egreso")).compareTo(BigDecimal.ZERO)!=0) {//CARGO
						entradaCalParidadCargo.MontoOrigen = (BigDecimal) movimientosCajaCliente.get("Egreso");
					} else if (((BigDecimal)movimientosCajaCliente.get("Ingreso")).compareTo(BigDecimal.ZERO)!=0) { // ABONO
						entradaCalParidadCargo.MontoOrigen = (BigDecimal) movimientosCajaCliente.get("Ingreso");
					}
					listaParidadCargo.add(entradaCalParidadCargo);
				}
				else {
					if (((BigDecimal)movimientosCajaCliente.get("Egreso")).compareTo(BigDecimal.ZERO)!=0) {
						paridadCajaCliente.put(id, (BigDecimal) movimientosCajaCliente.get("Egreso"));
					} else if (((BigDecimal)movimientosCajaCliente.get("Ingreso")).compareTo(BigDecimal.ZERO)!=0) {
						paridadCajaCliente.put(id, (BigDecimal) movimientosCajaCliente.get("Ingreso"));
					}
				}
			}
		}
		ParidadEntrada.p_CalculosEntrada = listaParidadCargo;
		CalcularParidadSalida paridadCargo = CalculaParidad.logica(con, ParidadEntrada); 
			
		for(CalculoSalida paridadTotal :paridadCargo.p_out_CalculosSalida ) {
			paridadCajaCliente.put(paridadTotal.IdInterno, paridadTotal.MontoDestino);
		}

		LocalDate fechaHastaMTD = LocalDate.parse(in.p_FechaHasta);
	    LocalDate fechaMTDDate = LocalDate.of(fechaHastaMTD.getYear(), fechaHastaMTD.getMonth(), 1);
        LocalDate fechaYTDDate = LocalDate.of(fechaHastaMTD.getYear(), 1, 1);
			  		
   		LocalDate periodoInicioMTD = fechaHastaMTD.with( fechaHastaMTD.minusMonths(1L).with( TemporalAdjusters.firstDayOfMonth()) );
   		LocalDate periodoFinMTD = fechaHastaMTD.with( fechaHastaMTD.minusMonths(1L).with( TemporalAdjusters.lastDayOfMonth()) );
   		
   		LocalDate periodoInicioYTD = fechaHastaMTD.with( fechaHastaMTD.minusYears(1L).with( TemporalAdjusters.firstDayOfYear()) );
   		LocalDate periodoFinYTD = fechaHastaMTD.with( fechaHastaMTD.minusYears(1L).with( TemporalAdjusters.lastDayOfYear()) );
   		
   		Collection<SaldoCuentaCliente> saldosCuentaCliente = in.MovimientosCaja.stream().map(movimiento ->{
   			SaldoCuentaCliente saldoCuentaCliente = new SaldoCuentaCliente();
   			if(!movimiento.get("idPortafolio").toString().equals("")) {
   			
				LocalDate fechaOperacion = LocalDate.parse(movimiento.get("fechaOperacion").toString(), formatter);
				//System.out.print(in.FechasInicialesPortaFolioMovimientos.get(movimiento.get("idPortafolio")));
				//System.out.println(in);
				LocalDate fechaITDDate = LocalDate.parse(formatoFechaDia.format(in.FechasInicialesPortaFolioMovimientos.get(movimiento.get("idPortafolio")).FechaCreacion), formatter);
				
	   			
	   			saldoCuentaCliente.setIdCliente((Long) movimiento.get("idCliente"));
	   			saldoCuentaCliente.setIdEmpresa((Long) in.p_IdEmpresa);
	   			String nombreconcepto ="CARGO";
	   			if (((BigDecimal)movimiento.get("Ingreso")).compareTo(BigDecimal.ZERO)==1) {
	   				nombreconcepto ="ABONO";
	   			}
				if (nombreconcepto.equals("ABONO") && movimiento.get("AfectaRentabilidad").equals("S") ){
					if (paridadCajaCliente.get((Long) movimiento.get("Id")) != null) 
					{					
					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalAbonosCajaMTD =  saldoCuentaCliente.totalAbonosCajaMTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
						saldoCuentaCliente.abonosCajaMTDRentabilidad = (saldoCuentaCliente.abonosCajaMTDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioMTD,fechaHastaMTD)),8, RoundingMode.HALF_UP));
		
					}
					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalAbonosCajaYTD = saldoCuentaCliente.totalAbonosCajaYTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
						saldoCuentaCliente.abonosCajaYTDRentabilidad = (saldoCuentaCliente.abonosCajaYTDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioYTD,fechaHastaMTD)),8, RoundingMode.HALF_UP));
						
					}
					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalAbonosCajaITD = saldoCuentaCliente.totalAbonosCajaITD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
						saldoCuentaCliente.abonosCajaITDRentabilidad = (saldoCuentaCliente.abonosCajaITDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD)),8, RoundingMode.HALF_UP));
						 
					}
					if (periodoInicioMTD.isBefore(fechaOperacion) || periodoInicioMTD.isEqual(fechaOperacion) && periodoFinMTD.isAfter(fechaOperacion) || periodoFinMTD.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalAbonosCajaMTDPeriodoAnterior = saldoCuentaCliente.totalAbonosCajaMTDPeriodoAnterior.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
					if (periodoInicioYTD.isBefore(fechaOperacion) || periodoInicioYTD.isEqual(fechaOperacion) && periodoFinYTD.isAfter(fechaOperacion) || periodoFinYTD.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalAbonosCajaYTDPeriodoAnterior = saldoCuentaCliente.totalAbonosCajaYTDPeriodoAnterior.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
				}
				}
					else if (nombreconcepto.equals("CARGO") && movimiento.get("AfectaRentabilidad").equals("S")){
						if (paridadCajaCliente.get((Long) movimiento.get("Id")) != null)
						{						
						if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
							saldoCuentaCliente.totalCargosCajaMTD = saldoCuentaCliente.totalCargosCajaMTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
							saldoCuentaCliente.cargosCajaMTDRentabilidad = (saldoCuentaCliente.cargosCajaMTDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioMTD,fechaHastaMTD)),8, RoundingMode.HALF_UP));
							
						}
						if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
							saldoCuentaCliente.totalCargosCajaYTD = saldoCuentaCliente.totalCargosCajaYTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
						    saldoCuentaCliente.cargosCajaYTDRentabilidad = (saldoCuentaCliente.cargosCajaYTDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioYTD,fechaHastaMTD)),8, RoundingMode.HALF_UP));
							 
						}
						if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
							saldoCuentaCliente.totalCargosCajaITD = saldoCuentaCliente.totalCargosCajaITD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
							if(!(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD)).compareTo(BigDecimal.ZERO)==0)) {
								saldoCuentaCliente.cargosCajaITDRentabilidad = (saldoCuentaCliente.cargosCajaITDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD)),8, RoundingMode.HALF_UP));
							}
							else {
								saldoCuentaCliente.cargosCajaITDRentabilidad = (saldoCuentaCliente.cargosCajaITDRentabilidad.add(paridadCajaCliente.get((Long) movimiento.get("Id")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)) )).divide(BigDecimal.ONE,8, RoundingMode.HALF_UP));
							}
							 
						}
						if (periodoInicioMTD.isBefore(fechaOperacion) || periodoInicioMTD.isEqual(fechaOperacion) && periodoFinMTD.isAfter(fechaOperacion) || periodoFinMTD.isEqual(fechaOperacion)) {
							saldoCuentaCliente.totalCargosCajaMTDPeriodoAnterior = saldoCuentaCliente.totalCargosCajaMTDPeriodoAnterior.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
						}
						if (periodoInicioYTD.isBefore(fechaOperacion) || periodoInicioYTD.isEqual(fechaOperacion) && periodoFinYTD.isAfter(fechaOperacion) || periodoFinYTD.isEqual(fechaOperacion)) {
							saldoCuentaCliente.totalCargosCajaYTDPeriodoAnterior = saldoCuentaCliente.totalCargosCajaYTDPeriodoAnterior.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
							}
						}
					}
				
				/// movmientos CRM Caja  (Ventas)
				/*if (nombreconcepto.equals("ABONO") && movimiento.get("TipoConcepto").equals("SISTEMA") && movimiento.get("AfectaRentabilidad").equals("S") ){
					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
					    saldoCuentaCliente.totalComprasCRMCajaMTD = saldoCuentaCliente.totalComprasCRMCajaMTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
			
					}
					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalComprasCRMCajaYTD = saldoCuentaCliente.totalComprasCRMCajaYTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalComprasCRMCajaITD = saldoCuentaCliente.totalComprasCRMCajaITD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
	 
					}
				}
				
				/// movmientos CRM Caja  (Compras)
				
				else if (nombreconcepto.equals("CARGO") && movimiento.get("TipoConcepto").equals("SISTEMA") && movimiento.get("AfectaRentabilidad").equals("S")){
					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalVentasCRMCajaMTD = saldoCuentaCliente.totalVentasCRMCajaMTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalVentasCRMCajaYTD = saldoCuentaCliente.totalVentasCRMCajaYTD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
						saldoCuentaCliente.totalVentasCRMCajaITD = saldoCuentaCliente.totalVentasCRMCajaITD.add(paridadCajaCliente.get((Long) movimiento.get("Id")));
					}
				}*/
				
	   			return saldoCuentaCliente;
   			}
   			return saldoCuentaCliente;
   		}).collect(Collectors.toMap(SaldoCuentaCliente::getIdCliente, Function.identity(), (left, rigth)->{
   			left.setTotalAbonosCajaMTD(left.getTotalAbonosCajaMTD().add(rigth.getTotalAbonosCajaMTD()));
   			left.setTotalAbonosCajaYTD(left.getTotalAbonosCajaYTD().add(rigth.getTotalAbonosCajaYTD()));
   			left.setTotalAbonosCajaITD(left.getTotalAbonosCajaITD().add(rigth.getTotalAbonosCajaITD()));
   			left.setAbonosCajaMTDRentabilidad(left.getAbonosCajaMTDRentabilidad().add(rigth.getAbonosCajaMTDRentabilidad()));
   			left.setAbonosCajaYTDRentabilidad(left.getAbonosCajaYTDRentabilidad().add(rigth.getAbonosCajaYTDRentabilidad()));
   			left.setAbonosCajaITDRentabilidad(left.getAbonosCajaITDRentabilidad().add(rigth.getAbonosCajaITDRentabilidad()));
   			left.setTotalAbonosCajaMTDPeriodoAnterior(left.getTotalAbonosCajaMTDPeriodoAnterior().add(rigth.getTotalAbonosCajaMTDPeriodoAnterior()));
   			left.setTotalAbonosCajaYTDPeriodoAnterior(left.getTotalAbonosCajaYTDPeriodoAnterior().add(rigth.getTotalAbonosCajaYTDPeriodoAnterior()));
   			left.setTotalCargosCajaMTD(left.getTotalCargosCajaMTD().add(rigth.getTotalCargosCajaMTD()));
   			left.setTotalCargosCajaYTD(left.getTotalCargosCajaYTD().add(rigth.getTotalCargosCajaYTD()));
   			left.setTotalCargosCajaITD(left.getTotalCargosCajaITD().add(rigth.getTotalCargosCajaITD()));
   			left.setCargosCajaMTDRentabilidad(left.getCargosCajaMTDRentabilidad().add(rigth.getCargosCajaMTDRentabilidad()));
   			left.setCargosCajaYTDRentabilidad(left.getCargosCajaYTDRentabilidad().add(rigth.getCargosCajaYTDRentabilidad()));
   			left.setCargosCajaITDRentabilidad(left.getCargosCajaITDRentabilidad().add(rigth.getCargosCajaITDRentabilidad()));
   			left.setTotalCargosCajaMTDPeriodoAnterior(left.getTotalCargosCajaMTDPeriodoAnterior().add(rigth.getTotalCargosCajaMTDPeriodoAnterior()));
   			left.setTotalCargosCajaYTDPeriodoAnterior(left.getTotalCargosCajaYTDPeriodoAnterior().add(rigth.getTotalCargosCajaYTDPeriodoAnterior()));
   			
   			left.setTotalComprasCRMCajaMTD(left.getTotalComprasCRMCajaMTD().add(rigth.getTotalComprasCRMCajaMTD()));
   			left.setTotalComprasCRMCajaYTD(left.getTotalComprasCRMCajaYTD().add(rigth.getTotalComprasCRMCajaYTD()));
   			left.setTotalComprasCRMCajaITD(left.getTotalComprasCRMCajaITD().add(rigth.getTotalComprasCRMCajaITD()));
   			
   			left.setTotalVentasCRMCajaMTD(left.getTotalVentasCRMCajaMTD().add(rigth.getTotalVentasCRMCajaMTD()));
   			left.setTotalVentasCRMCajaYTD(left.getTotalVentasCRMCajaYTD().add(rigth.getTotalVentasCRMCajaYTD()));
   			left.setTotalVentasCRMCajaITD(left.getTotalVentasCRMCajaITD().add(rigth.getTotalVentasCRMCajaITD()));   			

   			return left;
   		})).values();
   		   		List<SaldoCuentaCliente> saldoscuentasclientes = saldosCuentaCliente.stream().collect(Collectors.toList());
   		if(saldoscuentasclientes.isEmpty()) {
   			SaldoCuentaCliente saldoCuentaCliente = new SaldoCuentaCliente();
   			List<DatosCliente> listaClientes = in.p_Datos.stream().filter(distinctByKey(p -> p.getIdCliente())).collect(Collectors.toList());
   			// Recorre los distintos clientes
   			for (DatosCliente cliente : listaClientes) {
   				 saldoCuentaCliente.idCliente = cliente.IdCliente;
   				 saldoCuentaCliente.idEmpresa = in.p_IdEmpresa;
   				 saldoscuentasclientes.add(saldoCuentaCliente);
   			}	
   		}
   		saldoscuentasclientes.remove(0);
		salida.P_OUT_SALIDA.addAll(saldoscuentasclientes);
		log.write("TERMINO: FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja");
		return salida;
   		
	}
}