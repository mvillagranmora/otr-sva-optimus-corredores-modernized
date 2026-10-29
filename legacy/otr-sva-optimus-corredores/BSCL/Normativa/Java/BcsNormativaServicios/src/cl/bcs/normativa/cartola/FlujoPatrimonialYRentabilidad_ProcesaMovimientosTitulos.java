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
import cl.bcs.normativa.cartola.pojo.SaldoCuentaCliente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos implements JavaCall {
	
	public static class ProcesaMovimientosTitulo_Entrada{
		public String p_FechaHasta;
		public Long   p_IdEmpresa;
		public List<DatosCliente> p_Datos;
    	List<HashMap<String, Object>> MovimientosTitulo; 
    	HashMap<Long, FechaPrimerMovimientoPortafolio> FechasInicialesPortaFolioMovimientos;
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesaMovimientosTitulo_Salida{
		
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<SaldoCuentaCliente> P_OUT_SALIDA;
		
		
		public ProcesaMovimientosTitulo_Salida(){
			
			this.P_OUT_SALIDA    =  new ArrayList<>();
			this.P_OUT_MENSAJES  =  new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesaMovimientosTitulo_Entrada in = gson.fromJson(jin.toString(),ProcesaMovimientosTitulo_Entrada.class);
		ProcesaMovimientosTitulo_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// Utility function to get distinct IdCliente
		public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
			Map<Object, Boolean> map = new ConcurrentHashMap<>();
			return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
		}
	
	public ProcesaMovimientosTitulo_Salida  logica(Connection con, ProcesaMovimientosTitulo_Entrada in) throws Exception {
		
		ProcesaMovimientosTitulo_Salida salida = new ProcesaMovimientosTitulo_Salida();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
		SimpleDateFormat formatoFechaDia = new SimpleDateFormat("dd-MM-yyyy");

		LocalDate fechaHastaMTD = LocalDate.parse(in.p_FechaHasta);
	    LocalDate fechaMTDDate = LocalDate.of(fechaHastaMTD.getYear(), fechaHastaMTD.getMonth(), 1);
        LocalDate fechaYTDDate = LocalDate.of(fechaHastaMTD.getYear(), 1, 1);
			  		
   		LocalDate periodoInicioMTD = fechaHastaMTD.with( fechaHastaMTD.minusMonths(1L).with( TemporalAdjusters.firstDayOfMonth()) );
   		LocalDate periodoInicioYTD = fechaHastaMTD.with( fechaHastaMTD.minusYears(1L).with( TemporalAdjusters.firstDayOfYear()) );
   		
   		
   		Collection<SaldoCuentaCliente> saldosCuentaCliente = in.MovimientosTitulo.stream().map(movimiento ->{

			LocalDate fechaOperacion = LocalDate.parse(movimiento.get("FechaOperacion").toString(), formatter);
			//System.out.print(in.FechasInicialesPortaFolioMovimientos.get(movimiento.get("IdPortafolio")));
			//System.out.println(in);
			if (movimiento.get("IdPortafolio") == null) {
				System.out.println("");
			}
			LocalDate fechaITDDate = LocalDate.parse(formatoFechaDia.format(in.FechasInicialesPortaFolioMovimientos.get(movimiento.get("IdPortafolio")).FechaCreacion), formatter);
			
			SaldoCuentaCliente saldoCuentaCliente = new SaldoCuentaCliente();
   			saldoCuentaCliente.setIdCliente((Long) movimiento.get("IdCliente"));
   			saldoCuentaCliente.setIdEmpresa((Long) in.p_IdEmpresa);

   			if(movimiento.get("AfectaRentabilidad") == null) {
   				movimiento.put("AfectaRentabilidad", "N");
   			}
   			
   			//if(movimiento.containsKey("TipoConcepto")) {
   				
   				if (movimiento.get("Accion").equals("INGRESO") && movimiento.get("AfectaRentabilidad").equals("N")){
   					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalIngresoTituloMTD =  saldoCuentaCliente.totalIngresoTituloMTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.comprasTitulosMTDRentabilidad = (saldoCuentaCliente.comprasTitulosMTDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioMTD,fechaHastaMTD)),8, RoundingMode.HALF_UP);
   														
   					}
   					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalIngresoTituloYTD = saldoCuentaCliente.totalIngresoTituloYTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.comprasTitulosYTDRentabilidad = (saldoCuentaCliente.comprasTitulosYTDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioYTD,fechaHastaMTD)),8, RoundingMode.HALF_UP);
   					}
   					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalIngresoTituloITD = saldoCuentaCliente.totalIngresoTituloITD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.comprasTitulosITDRentabilidad = ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD) != 0 ? (saldoCuentaCliente.comprasTitulosITDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD)),8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
   					}
   	   			}
   	   			else if (movimiento.get("Accion").equals("RETIRO") && movimiento.get("AfectaRentabilidad").equals("N")){
   					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalRetiroTituloMTD = saldoCuentaCliente.totalRetiroTituloMTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.ventasTitulosMTDRentabilidad = (saldoCuentaCliente.ventasTitulosMTDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioMTD,fechaHastaMTD)),8, RoundingMode.HALF_UP);
   					}
   					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalRetiroTituloYTD = saldoCuentaCliente.totalRetiroTituloYTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.ventasTitulosYTDRentabilidad = (saldoCuentaCliente.ventasTitulosYTDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicioYTD,fechaHastaMTD)),8, RoundingMode.HALF_UP);

   					}
   					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalRetiroTituloITD = saldoCuentaCliente.totalRetiroTituloITD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   						saldoCuentaCliente.ventasTitulosITDRentabilidad = ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD) != 0 ? (saldoCuentaCliente.ventasTitulosITDRentabilidad.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaOperacion,fechaHastaMTD)))).divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(fechaITDDate,fechaHastaMTD)),8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
   					}
   				}
   	   			/*else if (movimiento.get("TipoConcepto").equals("SISTEMA") && movimiento.get("Accion").equals("INGRESO")){
   					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalComprasCRMTitulosMTD =  saldoCuentaCliente.totalComprasCRMTitulosMTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));								
   					}
   					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalComprasCRMTitulosYTD = saldoCuentaCliente.totalComprasCRMTitulosYTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));	
   					}
   					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalComprasCRMTitulosITD = saldoCuentaCliente.totalComprasCRMTitulosITD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal")); 
   					}
   	   			}
   	   			else if (movimiento.get("TipoConcepto").equals("SISTEMA") && movimiento.get("Accion").equals("RETIRO")){
   					if (fechaMTDDate.isBefore(fechaOperacion) || fechaMTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalVentasCRMTitulosMTD = saldoCuentaCliente.totalVentasCRMTitulosMTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   					}
   					if (fechaYTDDate.isBefore(fechaOperacion) || fechaYTDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalVentasCRMTitulosYTD = saldoCuentaCliente.totalVentasCRMTitulosYTD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   					}
   					if (fechaITDDate.isBefore(fechaOperacion) || fechaITDDate.isEqual(fechaOperacion)) {
   						saldoCuentaCliente.totalVentasCRMTitulosITD = saldoCuentaCliente.totalVentasCRMTitulosITD.add((BigDecimal) movimiento.get("MontoMercadoMonedaLocal"));
   					}
   				}*/
   	   			else {
   	   				
   	   			}
   			//}
   			
   		return saldoCuentaCliente;
   		}).collect(Collectors.toMap(SaldoCuentaCliente::getIdCliente, Function.identity(), (left, rigth)->{
   			left.setTotalIngresoTituloMTD(left.getTotalIngresoTituloMTD().add(rigth.getTotalIngresoTituloMTD()));
   			left.setTotalIngresoTituloYTD(left.getTotalIngresoTituloYTD().add(rigth.getTotalIngresoTituloYTD()));
   			left.setTotalIngresoTituloITD(left.getTotalIngresoTituloITD().add(rigth.getTotalIngresoTituloITD()));
   			left.setVentasTitulosMTDRentabilidad(left.getVentasTitulosMTDRentabilidad().add(rigth.getVentasTitulosMTDRentabilidad()));
   			left.setVentasTitulosYTDRentabilidad(left.getVentasTitulosYTDRentabilidad().add(rigth.getVentasTitulosYTDRentabilidad()));
   			left.setVentasTitulosITDRentabilidad(left.getVentasTitulosITDRentabilidad().add(rigth.getVentasTitulosITDRentabilidad()));
   			left.setTotalRetiroTituloMTD(left.getTotalRetiroTituloMTD().add(rigth.getTotalRetiroTituloMTD()));
   			left.setTotalRetiroTituloYTD(left.getTotalRetiroTituloYTD().add(rigth.getTotalRetiroTituloYTD()));
   			left.setTotalRetiroTituloITD(left.getTotalRetiroTituloITD().add(rigth.getTotalRetiroTituloITD()));
   			left.setComprasTitulosMTDRentabilidad(left.getComprasTitulosMTDRentabilidad().add(rigth.getComprasTitulosMTDRentabilidad()));
   			left.setComprasTitulosYTDRentabilidad(left.getComprasTitulosYTDRentabilidad().add(rigth.getComprasTitulosYTDRentabilidad()));
   			left.setComprasTitulosITDRentabilidad(left.getComprasTitulosITDRentabilidad().add(rigth.getComprasTitulosITDRentabilidad()));
   			
   			left.setTotalComprasCRMTitulosMTD(left.getTotalComprasCRMTitulosMTD().add(rigth.getTotalComprasCRMTitulosMTD()));
   			left.setTotalComprasCRMTitulosYTD(left.getTotalComprasCRMTitulosYTD().add(rigth.getTotalComprasCRMTitulosYTD()));
   			left.setTotalComprasCRMTitulosITD(left.getTotalComprasCRMTitulosITD().add(rigth.getTotalComprasCRMTitulosITD()));
   			
   			left.setTotalVentasCRMTitulosMTD(left.getTotalVentasCRMTitulosMTD().add(rigth.getTotalVentasCRMTitulosMTD()));
   			left.setTotalVentasCRMTitulosYTD(left.getTotalVentasCRMTitulosYTD().add(rigth.getTotalVentasCRMTitulosYTD()));
   			left.setTotalVentasCRMTitulosITD(left.getTotalVentasCRMTitulosITD().add(rigth.getTotalVentasCRMTitulosITD()));

   			return left;
   		})).values();

   		List<SaldoCuentaCliente> saldoscuentasTitulos = saldosCuentaCliente.stream().collect(Collectors.toList());
   		
   		if(saldoscuentasTitulos.isEmpty()) {
   			SaldoCuentaCliente saldoCuentaCliente = new SaldoCuentaCliente();
   			List<DatosCliente> listaClientes = in.p_Datos.stream().filter(distinctByKey(p -> p.getIdCliente()))
   					.collect(Collectors.toList());
   			
   			// Recorre los distintos clientes
   			for (DatosCliente cliente : listaClientes) {
   				 saldoCuentaCliente.idCliente = cliente.IdCliente;
   				 saldoCuentaCliente.idEmpresa = in.p_IdEmpresa;
   				saldoscuentasTitulos.add(saldoCuentaCliente);
   			}	
   		}
		salida.P_OUT_SALIDA.addAll(saldoscuentasTitulos);
		return salida;	
	}
}
