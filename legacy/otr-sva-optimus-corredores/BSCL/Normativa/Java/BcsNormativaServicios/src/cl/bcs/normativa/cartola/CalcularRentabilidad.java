package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.TablaSalida;
import cl.bcs.normativa.cartola.pojo.SaldoCuentaCliente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CalcularRentabilidad implements JavaCall  {
	private final Log   log = new Log("NOR.CalcularRentabilidad");	
	
	public static class  CalcularRentabilidad_Entrada{
		public String FechaConsulta;
		public Boolean esPauta = false;
		public List<SaldoCuentaCliente> MovimientosCaja;
		public List<SaldoCuentaCliente> MovimientosTitulo;
		public List<TablaSalida> SaldoFinal;
		public List<DatosCliente> ListaClientes;
		public List<ListaClientesEmpresa> p_Datos;
		public Long p_IdEmpresa;
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	public static class  ListaClientesEmpresa{
		
		public Long IdCliente;
		public Long IdEmpresa;
		
		public ListaClientesEmpresa() {
			this.IdCliente = 0L;
			this.IdEmpresa = 0L;

		}

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

		public Long getIdCliente() {
			return IdCliente;
		}

		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}

		public Long getIdEmpresa() {
			return IdEmpresa;
		}

		public void setIdEmpresa(Long idEmpresa) {
			IdEmpresa = idEmpresa;
		}
	}

	public static class CalcularRentabilidad_Salida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<Rentabilidad> P_OUT_SALIDA = new ArrayList<>();
		public CalcularRentabilidad_Salida(){	
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Rentabilidad{
		public Long IdCliente;
		public Long IdEmpresa;
		public Long getIdEmpresa() {
			return IdEmpresa;
		}
		public void setIdEmpresa(Long idEmpresa) {
			IdEmpresa = idEmpresa;
		}
		public BigDecimal RentabilidadMTD;
		public BigDecimal RentabilidadYTD;
		public BigDecimal RentabilidadITD;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Long getIdCliente() {
			return IdCliente;
		}
		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}
		public BigDecimal getRentabilidadMTD() {
			return RentabilidadMTD;
		}
		public void setRentabilidadMTD(BigDecimal rentabilidadMTD) {
			RentabilidadMTD = rentabilidadMTD;
		}
		public BigDecimal getRentabilidadYTD() {
			return RentabilidadYTD;
		}
		public void setRentabilidadYTD(BigDecimal rentabilidadYTD) {
			RentabilidadYTD = rentabilidadYTD;
		}
		public BigDecimal getRentabilidadITD() {
			return RentabilidadITD;
		}
		public void setRentabilidadITD(BigDecimal rentabilidadITD) {
			RentabilidadITD = rentabilidadITD;
		}
	}
	
	public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CalcularRentabilidad_Entrada in = gson.fromJson(jin.toString(), CalcularRentabilidad_Entrada.class);
		CalcularRentabilidad_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CalcularRentabilidad_Salida logica(Connection conn, CalcularRentabilidad_Entrada in) throws Exception {
		
		CalcularRentabilidad_Salida out = new CalcularRentabilidad_Salida();
   		
   		List<DatosCliente> listaClientes = in.ListaClientes.stream().filter(distinctByKey(p -> p.getIdCliente())).collect(Collectors.toList());
		List<Rentabilidad> listRentabilidad;
		List<ListaClientesEmpresa> listaCliEmpresa = new ArrayList<ListaClientesEmpresa>();
		
		if(in.esPauta) {
			listRentabilidad = TotalRentabilidad(listaCliEmpresa ,listaClientes, in, "idCliente");
		} else {
			for ( DatosCliente DatosLiente : listaClientes ) {
				ListaClientesEmpresa empresa = new ListaClientesEmpresa();
				empresa.IdCliente = DatosLiente.IdCliente;
				empresa.IdEmpresa = in.p_IdEmpresa;
				listaCliEmpresa.add(empresa);
			}
			listRentabilidad = TotalRentabilidad(listaCliEmpresa, listaClientes, in, "idCliente");
		}
		out.P_OUT_SALIDA.addAll(listRentabilidad);
		return out;
	}

	public List<Rentabilidad> TotalRentabilidad(List<ListaClientesEmpresa> listaClienteEmpresa, List<DatosCliente> listaClientes, CalcularRentabilidad_Entrada in, String key) throws Exception
	{		
	    List<Rentabilidad> listRentabilidad;
	    
	    HashMap<Long, Object> movimientosCaja = Utils.List2Hash(in.MovimientosCaja, key);
		HashMap<Long, Object> movimientosTitulos = Utils.List2Hash(in.MovimientosTitulo, key);
		HashMap<Long, Object> saldosFinales = Utils.List2Hash(in.SaldoFinal, key);	
		
		List<Object> lista;
		if (in.esPauta) {
			 lista = listaClientes.stream().filter(distinctByKey(p -> p.getIdCliente())).collect(Collectors.toList());			
		} else {
			 lista = listaClienteEmpresa.stream().filter(distinctByKey(p -> p.getIdEmpresa())).collect(Collectors.toList());			
		}
		
		int cont=0;
		Collection<Rentabilidad> rentabilidadEmpresa = lista.stream().map(empresa ->{
   			Rentabilidad rentabilidad = new Rentabilidad();
   			
	   				   			
	   			rentabilidad.IdEmpresa = in.p_IdEmpresa;
	   			Long IdCliente = 0L;
	   			if(empresa instanceof ListaClientesEmpresa){  
	   				ListaClientesEmpresa d =(ListaClientesEmpresa) empresa;//downcasting  
	   				IdCliente = d.IdCliente;
	   				
	   			}  else if(empresa instanceof DatosCliente){  
	   				DatosCliente d =(DatosCliente) empresa;//downcasting  
	   				IdCliente = d.IdCliente;
	   			}
	   			
	   			SaldoCuentaCliente caja    = (SaldoCuentaCliente) movimientosCaja.get(IdCliente) != null? (SaldoCuentaCliente) movimientosCaja.get(IdCliente): new SaldoCuentaCliente() ;
	   			SaldoCuentaCliente titulos = (SaldoCuentaCliente) movimientosTitulos.get(IdCliente) != null?(SaldoCuentaCliente) movimientosTitulos.get(IdCliente): new SaldoCuentaCliente();
	   			TablaSalida saldoFinal     = (TablaSalida) saldosFinales.get(IdCliente) != null? (TablaSalida) saldosFinales.get(IdCliente): new TablaSalida();

	   			rentabilidad.IdCliente = IdCliente;
	   			
	   			BigDecimal aportesMTD = caja.totalAbonosCajaMTD.add(titulos.totalIngresoTituloMTD);
	   			BigDecimal rescatesMTD = caja.totalCargosCajaMTD.add(titulos.totalRetiroTituloMTD);
	   			BigDecimal patrimonioMTD = saldoFinal.totalPatrimonioFechaConsulta.subtract(saldoFinal.totalMTDSaldoFinal) ;
	   			BigDecimal aportesPeriodoMTD = caja.abonosCajaMTDRentabilidad.add(caja.ventasTitulosMTDRentabilidad);
	   			BigDecimal rescatesPeriodoMTD = caja.cargosCajaMTDRentabilidad.add(caja.comprasTitulosMTDRentabilidad);
	   			BigDecimal numeradorMTD = patrimonioMTD.subtract(aportesMTD).add(rescatesMTD);
	   			BigDecimal denominadorMTD = saldoFinal.totalMTDSaldoFinal.add(aportesPeriodoMTD).subtract(rescatesPeriodoMTD);
	   			rentabilidad.RentabilidadMTD = (denominadorMTD.compareTo(BigDecimal.ZERO) == 0)? BigDecimal.ZERO : numeradorMTD.divide(denominadorMTD, 2, RoundingMode.HALF_UP);
	   			
	   			BigDecimal aportesYTD = caja.totalAbonosCajaYTD.add(titulos.totalIngresoTituloYTD);
	   			BigDecimal rescatesYTD = caja.totalCargosCajaYTD.add(titulos.totalRetiroTituloYTD);
	   			BigDecimal patrimonioYTD = saldoFinal.totalPatrimonioFechaConsulta.subtract(saldoFinal.totalYTDSaldoFinal);
	   			BigDecimal aportesPeriodoYTD = caja.abonosCajaYTDRentabilidad.add(caja.ventasTitulosYTDRentabilidad);
	   			BigDecimal rescatesPeriodoYTD = caja.cargosCajaYTDRentabilidad.add(caja.comprasTitulosYTDRentabilidad);
	   			BigDecimal numeradorYTD = (patrimonioYTD.subtract(aportesYTD).add(rescatesYTD));
	   			BigDecimal denominadorYTD = saldoFinal.totalYTDSaldoFinal.add(aportesPeriodoYTD).subtract(rescatesPeriodoYTD);
	   			rentabilidad.RentabilidadYTD = (denominadorYTD.compareTo(BigDecimal.ZERO) == 0)? BigDecimal.ZERO : (numeradorYTD).divide(denominadorYTD, 2, RoundingMode.HALF_UP);
	   			
	   			BigDecimal aportesITD = caja.totalAbonosCajaITD.add(titulos.totalIngresoTituloITD);
	   			BigDecimal rescatesITD = caja.totalCargosCajaITD.add(titulos.totalRetiroTituloITD);
	   			BigDecimal patrimonioITD = saldoFinal.totalPatrimonioFechaConsulta.subtract(saldoFinal.totalITDSaldoFinal);
	   			BigDecimal aportesPeriodoITD = caja.abonosCajaITDRentabilidad.add(caja.ventasTitulosITDRentabilidad);
	   			BigDecimal rescatesPeriodoITD = caja.cargosCajaITDRentabilidad.add(caja.comprasTitulosITDRentabilidad);
	   			BigDecimal numeradorITD = (patrimonioITD.subtract(aportesITD).add(rescatesITD));
	   			BigDecimal denominadorITD = saldoFinal.totalITDSaldoFinal.add(aportesPeriodoITD).subtract(rescatesPeriodoITD);
	   			rentabilidad.RentabilidadITD = (denominadorITD.compareTo(BigDecimal.ZERO)== 0)? BigDecimal.ZERO : (numeradorITD).divide(denominadorITD, 2, RoundingMode.HALF_UP);
	   			log.write("RENTABILIDAD: " + rentabilidad);
			
	   		return rentabilidad;
	   		}).collect(Collectors.toMap(Rentabilidad::getIdCliente, Function.identity(), (left, rigth)->{
	   			left.setRentabilidadMTD(left.getRentabilidadMTD().add(rigth.getRentabilidadMTD()));
	   			left.setRentabilidadYTD(left.getRentabilidadYTD().add(rigth.getRentabilidadYTD()));
	   			left.setRentabilidadITD(left.getRentabilidadITD().add(rigth.getRentabilidadITD()));
	   			return left;
	   		})).values();
			
		  listRentabilidad = rentabilidadEmpresa.stream().collect(Collectors.toList());
		  return listRentabilidad;
	}
	
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"FechaConsulta\":\"2019-07-02\",\"SaldoFinal\":15000000,\"Mtd_SaldoInicial\":10000000,\"Mtd_TotalAportes\":1777710,\"Mtd_TotalRescates\":1200000,\"Ytd_SaldoInicial\":4654981,\"Ytd_TotalAportes\":1772626,\"Ytd_TotalRescates\":1000000,\"Itd_SaldoInicial\":1156165,\"Itd_TotalAportes\":189410,\"Itd_TotalRescates\":14196165,\"AportesPonderadosMtd\":19415123,\"AportesPonderadosYtd\":1548123,\"AportesPonderadosItd\":25000515,\"RescatesPonderadosMtd\":14415123,\"RescatesPonderadosYtd\":1548123,\"RescatesPonderadosItd\":25000515,\"Mtd_SaldoFinal\":80000000,\"Ytd_SaldoFinal\":5000000,\"Itd_SaldoFinal\":13000000}";
			CalcularRentabilidad_Entrada Objin = gson.fromJson(json, CalcularRentabilidad_Entrada.class);
			CalcularRentabilidad demo = new CalcularRentabilidad();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				CalcularRentabilidad_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}