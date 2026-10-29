package cl.bcs.normativa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.PatrimonioValorizado;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ResumenPatrimonial implements JavaCall{
	private final static Logger logger = LogManager.getLogger(ResumenPatrimonial.class);
	private final String SQL_SELECT_COMISIONES_DERECHOS_GASTOS = loadResourceToString("cl/bcs/normativa/sql/ResumenPatrimonial_ObtenerComisionDerechoGasto.sql");
	private final String SQL_SELECT_APORTE= loadResourceToString("cl/bcs/normativa/sql/ResumenPatrimonial_ObtenerAporte.sql");
	private final String SQL_SELECT_RESCATE = loadResourceToString("cl/bcs/normativa/sql/ResumenPatrimonial_ObtenerRescate.sql");

	public static class	ResumenPatrimonial_Entrada implements Serializable{
		public Long p_IdEmpresa;		
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdUsuario;
		public String p_IdModulo;
	    public String p_FechaDesde;
	    public String p_FechaHasta;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResumenPatrimonial_Salida{
		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public ResumenPeriodo				P_OUT_RESUMENPERIODO;
		
		public ResumenPatrimonial_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 	   =  new ArrayList<MensajeSalida>();
			this.P_OUT_RESUMENPERIODO  = new ResumenPeriodo();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResumenPeriodo{
		public BigDecimal PatrimonioInicial;
		public BigDecimal PatrimonioFinal;
		public BigDecimal Aportes;
		public BigDecimal Rescates;
		public BigDecimal ComisionDerechoGasto;
		public BigDecimal ResultadoPeriodo;
		public BigDecimal RentabilidadPeriodo;
		
		public ResumenPeriodo() {
			this.PatrimonioInicial=BigDecimal.ZERO;
			this.PatrimonioFinal  =BigDecimal.ZERO;
			this.Aportes		  =BigDecimal.ZERO;
			this.Rescates		  =BigDecimal.ZERO;
			this.ComisionDerechoGasto=BigDecimal.ZERO;
			this.ResultadoPeriodo	 =BigDecimal.ZERO;
			this.RentabilidadPeriodo =BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ComisionesDerechosGastos{
		public BigDecimal TotalComisiones;
		public BigDecimal TotalImpuestos;
		public BigDecimal TotalDerechos;
		public BigDecimal TotalGastos;
		
		public ComisionesDerechosGastos(){
			this.TotalComisiones=BigDecimal.ZERO;
			this.TotalImpuestos=BigDecimal.ZERO;
			this.TotalDerechos=BigDecimal.ZERO;
			this.TotalGastos=BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AporteRescate{
		
		public String TipoMovimiento;
		public String IdMoneda;
		public BigDecimal MontoSumado;
		
		public AporteRescate() {
			this.MontoSumado=BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	private ResumenPatrimonial_Salida logica(Connection con, ResumenPatrimonial_Entrada in) throws Exception {	

		logger.info("INICIO::ResumenPatrimonial");
		long inicioServicio = System.currentTimeMillis();
		ResumenPatrimonial_Salida out = new ResumenPatrimonial_Salida();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		int anio = Integer.valueOf(in.p_FechaDesde.split("-")[0]);
		int mes  = Integer.valueOf(in.p_FechaHasta.split("-")[1]) - 1;
		Calendar calendario = new GregorianCalendar(anio, mes, 1);
		
		if(mes==0) {
			mes=12;
			anio=anio-1;
		}

		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, anio);
		cal.set(Calendar.MONTH, mes);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		java.util.Date dateRepresentation = cal.getTime();
		String dateString = new SimpleDateFormat("dd-MM-yyyy").format(dateRepresentation) ;
		String day =  dateString.split("-")[0];
		
		
		String FechaPeriodoAnteriorDesde = String.valueOf(anio)+"-"+(mes<10?"0"+String.valueOf(mes):String.valueOf(mes))+"-01";
		String FechaPeriodoAnteriorHasta = String.valueOf(anio)+"-"+(mes<10?"0"+String.valueOf(mes):String.valueOf(mes))+"-" + day;

		
		String strDate =verificarFecha (con , in.p_IdEmpresa, cal, FechaPeriodoAnteriorHasta);  
		
		//Consulta Patrimonio Periodo Inicial y final		
		out.P_OUT_RESUMENPERIODO.PatrimonioFinal   = consultarPatrimonio(con, Fecha.toDate(in.p_FechaDesde), Fecha.toDate(in.p_FechaHasta), in);
	    out.P_OUT_RESUMENPERIODO.PatrimonioInicial = consultarPatrimonio(con, Fecha.toDate(FechaPeriodoAnteriorDesde), Fecha.toDate(strDate), in);
	    out.P_OUT_RESUMENPERIODO.Aportes           = calcularTotalAportes(con, in, idMonedaSistema);
	    out.P_OUT_RESUMENPERIODO.Rescates          = calcularTotalRescates(con, in, idMonedaSistema);
		
		out.P_OUT_RESUMENPERIODO.ComisionDerechoGasto = calcularTotalComisionesDerechosGastos(con, in);
		out.P_OUT_RESUMENPERIODO.ResultadoPeriodo = calcularResultadoPeriodo(out.P_OUT_RESUMENPERIODO);
		out.P_OUT_RESUMENPERIODO.RentabilidadPeriodo = (calcularRentabilidadPeriodo(out.P_OUT_RESUMENPERIODO)).multiply(new BigDecimal(100));
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ResumenPatrimonial: " + (finServicio - inicioServicio));
		return out;
	}
	private String verificarFecha(Connection con,Long idEmpresa,  Calendar cal, String FechaPeriodoAnteriorHasta) {
		cal.setTime(Fecha.toDate(FechaPeriodoAnteriorHasta));		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		java.util.Date date = null;
		String fechaReturn = null;
		int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if(week==0) { 
			cal.add(Calendar.DATE, -3);
			date = cal.getTime();
			fechaReturn = dateFormat.format(date);
		}
		if(week==6) {
			cal.add(Calendar.DATE, -2);
			date = cal.getTime();
			fechaReturn = dateFormat.format(date);
		}
		
		if(fechaReturn != null) {
			String siguienteDiaHabil= null;
			try {
				ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
				InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
			    Long idPaisSistema = empresa.IdPais;
				OutObtenerDiaSiguiente salDiaSiguiente = ObtenerDiaAnteriorHabil(con, fechaReturn, idPaisSistema);
				siguienteDiaHabil = salDiaSiguiente.p_out_fechas.get(0).fechaCalculada.toString();
				if(siguienteDiaHabil.split("-")[1].equalsIgnoreCase(fechaReturn.split("-")[1]) ) {	
					fechaReturn = siguienteDiaHabil;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String out = fechaReturn== null ? FechaPeriodoAnteriorHasta : fechaReturn;
		
		return out;
	}
	private OutObtenerDiaSiguiente ObtenerDiaAnteriorHabil(Connection con, String fechaReturn, Long idPaisSistema)
			throws Exception {
		ObtenerDiaSiguiente diaHabilSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente diaHabilSiguienteIn = new InObtenerDiaSiguiente();
		diaHabilSiguienteIn.p_idPais = idPaisSistema;
		diaHabilSiguienteIn.p_diaHabil = "S";
		FechasIn fecha = new FechasIn();
		fecha.fechaInicial = fechaReturn;
		fecha.numeroDias = 1L;
		fecha.pais = idPaisSistema;
		diaHabilSiguienteIn.p_fechas.add(fecha);
		OutObtenerDiaSiguiente salDiaSiguiente =  diaHabilSiguiente.logica(con, diaHabilSiguienteIn);
		return salDiaSiguiente;
	}
	private BigDecimal calcularTotalAportes(Connection con,  ResumenPatrimonial_Entrada in ,String MonedaSistema) throws Exception {
		SQL sql= new SQL();
		
		CalcularParidad CalculoParidad = new CalcularParidad();
		CalcularParidadEntrada InParidad = new CalcularParidadEntrada();
		CalcularParidadSalida  OutParidad = new CalcularParidadSalida();
		BigDecimal totalAportes = new BigDecimal(0);
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdCliente", in.p_IdCliente);
		params.put("p_IdPortafolio", in.p_IdPortafolio);
		params.put("p_TipoMovimiento", "AB");
		params.put("p_FechaInicio", in.p_FechaDesde);
		params.put("p_FechaFin", in.p_FechaHasta);
		
		List<AporteRescate> ListaAporte = sql.query(con, SQL_SELECT_APORTE, params, AporteRescate.class);
		
		List<AporteRescate> ListaAporteNoCLP = ListaAporte.stream().filter(c -> !c.IdMoneda.equals("CLP")).collect(Collectors.toList());
		List<AporteRescate> ListaAporteCLP = ListaAporte.stream().filter(c -> c.IdMoneda.equals("CLP")).collect(Collectors.toList());
		
		if(ListaAporteNoCLP.size()>0) {
			
			InParidad.p_IdEmpresa=in.p_IdEmpresa;
			InParidad.p_IdTrabajo=0L;
			Long IdInterno=0L;
			
			for(AporteRescate apo : ListaAporteNoCLP) {
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha = in.p_FechaHasta;
				calculoEntrada.IdInterno = IdInterno++;
				calculoEntrada.IdMonedaOrigen = apo.IdMoneda;
				calculoEntrada.IdMonedaDestino = MonedaSistema;
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.IdUso = "";
				calculoEntrada.MontoOrigen = apo.MontoSumado;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;				
				InParidad.p_CalculosEntrada.add(calculoEntrada);
			}
			
			OutParidad = CalculoParidad.logica(con, InParidad);
			
			for(CalculoSalida sal : OutParidad.p_out_CalculosSalida) {
				totalAportes=totalAportes.add(sal.MontoDestino);
			}
			for(AporteRescate sal : ListaAporteCLP) {
				totalAportes=totalAportes.add(sal.MontoSumado);
			}
			
		}else {
			for(AporteRescate sal : ListaAporteCLP) {
				totalAportes=totalAportes.add(sal.MontoSumado);
			}
		}
		
		return totalAportes;
	}

	private BigDecimal calcularTotalRescates(Connection con,  ResumenPatrimonial_Entrada in,String MonedaSistema) throws Exception {
		
		SQL sql= new SQL();
		
		CalcularParidad CalculoParidad = new CalcularParidad();
		CalcularParidadEntrada InParidad = new CalcularParidadEntrada();
		CalcularParidadSalida  OutParidad = new CalcularParidadSalida();
		BigDecimal totalRescates = new BigDecimal(0);
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdCliente", in.p_IdCliente);
		params.put("p_IdPortafolio", in.p_IdPortafolio);
		params.put("p_TipoMovimiento", "CA");
		params.put("p_FechaInicio", in.p_FechaDesde);
		params.put("p_FechaFin", in.p_FechaHasta);
		
		List<AporteRescate> ListaRescate = sql.query(con, SQL_SELECT_RESCATE, params, AporteRescate.class);
		
		List<AporteRescate> ListaRescateNoCLP = ListaRescate.stream().filter(c -> !c.IdMoneda.equals("CLP")).collect(Collectors.toList());
		List<AporteRescate> ListaRescateCLP = ListaRescate.stream().filter(c -> c.IdMoneda.equals("CLP")).collect(Collectors.toList());
		
		if(ListaRescateNoCLP.size()>0) {
			
			InParidad.p_IdEmpresa=in.p_IdEmpresa;
			InParidad.p_IdTrabajo=0L;
			Long IdInterno=0L;
			
			for(AporteRescate apo : ListaRescateNoCLP) {
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha = in.p_FechaHasta;
				calculoEntrada.IdInterno = IdInterno++;
				calculoEntrada.IdMonedaOrigen = apo.IdMoneda;
				calculoEntrada.IdMonedaDestino = MonedaSistema;
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.IdUso = "";
				calculoEntrada.MontoOrigen = apo.MontoSumado;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;				
				InParidad.p_CalculosEntrada.add(calculoEntrada);
			}
			
			OutParidad = CalculoParidad.logica(con, InParidad);
			
			for(CalculoSalida sal : OutParidad.p_out_CalculosSalida) {
				totalRescates=totalRescates.add(sal.MontoDestino);
			}
			
			for(AporteRescate sal : ListaRescateCLP) {
				totalRescates=totalRescates.add(sal.MontoSumado);
			}
			
		}else {
			for(AporteRescate sal : ListaRescateCLP) {
				totalRescates=totalRescates.add(sal.MontoSumado);
			}
		}
		
		return totalRescates;
		
	}

	private BigDecimal calcularTotalComisionesDerechosGastos(Connection con, ResumenPatrimonial_Entrada resumenPatrimonial) throws SQLException {
		SQL sql= new SQL();
		BigDecimal totalComisiones = BigDecimal.ZERO;
		BigDecimal totalImpuestos = BigDecimal.ZERO;
		BigDecimal totalDerechos = BigDecimal.ZERO;
		BigDecimal totalGastos = BigDecimal.ZERO;
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_FechaDesde", resumenPatrimonial.p_FechaDesde);
		params.put("p_FechaHasta", resumenPatrimonial.p_FechaHasta);
		params.put("p_IdPortafolio", resumenPatrimonial.p_IdPortafolio);
		params.put("p_IdEmpresa", resumenPatrimonial.p_IdEmpresa);
		List<ComisionesDerechosGastos> comisionesDerechosGastos = sql.query(con, SQL_SELECT_COMISIONES_DERECHOS_GASTOS, params, ComisionesDerechosGastos.class);
		System.out.println(comisionesDerechosGastos);
		for(ComisionesDerechosGastos comisionDerechoGasto: comisionesDerechosGastos) {
			totalComisiones = totalComisiones.add(comisionDerechoGasto.TotalComisiones);
			totalImpuestos = totalImpuestos.add(comisionDerechoGasto.TotalImpuestos);
			totalDerechos = totalDerechos.add(comisionDerechoGasto.TotalDerechos);
			totalGastos = totalGastos.add(comisionDerechoGasto.TotalGastos);
		}
		BigDecimal total = totalComisiones.add(totalImpuestos).add(totalDerechos).add(totalGastos);
		return total;
	}

	private BigDecimal calcularResultadoPeriodo(ResumenPeriodo resumenPeriodo) {
		BigDecimal preCalculo = resumenPeriodo.PatrimonioInicial.add(resumenPeriodo.Aportes.subtract(resumenPeriodo.Rescates.add(resumenPeriodo.ComisionDerechoGasto)));
		return resumenPeriodo.PatrimonioFinal
				.subtract(preCalculo);
	}

	private BigDecimal calcularRentabilidadPeriodo(ResumenPeriodo resumenPeriodo) {
		BigDecimal rentabilidad= BigDecimal.ZERO;
		if(resumenPeriodo.PatrimonioInicial.compareTo(BigDecimal.ZERO)!=0 || resumenPeriodo.Aportes.compareTo(BigDecimal.ZERO)!=0 ||  resumenPeriodo.Rescates.compareTo(BigDecimal.ZERO)!=0 || resumenPeriodo.ComisionDerechoGasto.compareTo(BigDecimal.ZERO)!=0) {
			rentabilidad = resumenPeriodo.PatrimonioFinal
					.divide((resumenPeriodo.PatrimonioInicial.add(resumenPeriodo.Aportes).subtract(resumenPeriodo.Rescates).subtract(resumenPeriodo.ComisionDerechoGasto)),20,RoundingMode.HALF_UP)
					.subtract(new BigDecimal(1));
		}
		
		return rentabilidad;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ResumenPatrimonial_Entrada in = gson.fromJson(jin.toString(), ResumenPatrimonial_Entrada.class);
		ResumenPatrimonial_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
	
	public BigDecimal consultarPatrimonio(Connection con, Date fechaDesde, Date fechaHasta ,ResumenPatrimonial_Entrada in) throws Exception {

		PatrimonioValorizado  patrimonio= new PatrimonioValorizado();
		EntradaCartola in_PatrimonioValorizado = new EntradaCartola();
		SalidaCartola out_PatrimonioValorizado= new SalidaCartola();
		BigDecimal resPatrimonio =  BigDecimal.ZERO;
		Dato dato = new Dato();

		in_PatrimonioValorizado.p_IdEmpresa=in.p_IdEmpresa;
		in_PatrimonioValorizado.p_IdModulo =in.p_IdModulo;
		in_PatrimonioValorizado.p_IdUsuario=in.p_IdUsuario;
		in_PatrimonioValorizado.p_FechaDesde=fechaDesde;
		in_PatrimonioValorizado.p_FechaHasta=fechaHasta;
		dato.IdCliente=in.p_IdCliente;
		dato.IdPortafolio=in.p_IdPortafolio;
		in_PatrimonioValorizado.p_Datos.add(dato);

		out_PatrimonioValorizado =patrimonio.procesar(con, in_PatrimonioValorizado);

		System.out.println(out_PatrimonioValorizado.P_OUT_SALIDA.get("patrimonioValorizado"));

		List<HashMap<String,Object>> ListPatrimoniosMonedas =  (List<HashMap<String, Object>>) out_PatrimonioValorizado.P_OUT_SALIDA.get("patrimonioValorizado");

		for (HashMap<String,Object> val : ListPatrimoniosMonedas) {
			if(("Peso Chileno(CLP)").equals(val.get("item")) ) {
				resPatrimonio= (BigDecimal) val.get("monto");
			}
		}

		return resPatrimonio;
	}

	public static void main(String[] args) throws Exception {

		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			//String json = "";
			ResumenPatrimonial_Entrada Objin = new ResumenPatrimonial_Entrada();//gson.fromJson(json, ResumenPatrimonial_Entrada.class);
			
			Objin.p_IdEmpresa = 1L;
			Objin.p_IdCliente = 27312L;//100209L;
			Objin.p_IdPortafolio =243512L ;//100221L;
			Objin.p_IdModulo = "NOR";
			Objin.p_IdUsuario = 1020L;
			Objin.p_FechaDesde = "2020-07-01";
			Objin.p_FechaHasta = "2020-07-31";
			
			ResumenPatrimonial demo = new ResumenPatrimonial();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ResumenPatrimonial_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
