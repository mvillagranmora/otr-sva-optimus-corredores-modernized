package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValidaPatrimonioEmisor implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidaPatrimonioEmisor.class);
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_SelectSerieFondo.sql");
	private final String SQL_DELETE_TABLA_PASO = loadResourceToString("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_DeleteTablaPaso.sql");
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_InsertTablaPaso.sql");
	private final String SQL_SELECT_CUSTODIA = loadResourceToString("cl/bcs/fondomutuo/sql/ValidaPatrimonioEmisor_SelectCustodia.sql");
	
	public static class EntradaValidaPatrimonioEmisor{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
		public List<Solicitud> p_Solicitudes;
		public EntradaValidaPatrimonioEmisor() {
			this.p_Solicitudes= new ArrayList<Solicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaValidaPatrimonioEmisor{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidaPatrimonioEmisor(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud{
		public Long IdInterno;
		public Long IdCliente;
		public String IdSerieFondo;
		public String IdEmisor;
		public BigDecimal MontoPatrimonio;
		public BigDecimal ValorCuota;
		
		public Solicitud() {
			this.MontoPatrimonio = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SerieFondo{
		public String IdSerieFondo;
		public String IdEmisor;
		public BigDecimal MontoPatrimonioEmisor;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Custodia{
		public String Id;
		public String IdClienteEmisor;
		public String IdMonedaInstrumento;
		public Long IdCliente;
		public String IdEmisor;
		public BigDecimal Monto;
		public BigDecimal Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaValidaPatrimonioEmisor jin = gson.fromJson(in.toString(), EntradaValidaPatrimonioEmisor.class);
		SalidaValidaPatrimonioEmisor out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaValidaPatrimonioEmisor logica(Connection con, EntradaValidaPatrimonioEmisor entrada) throws Exception{
		logger.info("INICIO::SalidaValidaPatrimonioEmisor");
		SalidaValidaPatrimonioEmisor out = new SalidaValidaPatrimonioEmisor();
		SQL sql=new SQL();
			
		HashMap<String,Boolean> hashIdsSerieFondo = new HashMap<String,Boolean>();
		HashMap<Long,Boolean> hashIdsCliente = new HashMap<Long,Boolean>();
		
		List<String> idsSerie = new ArrayList<String>();
		List<Solicitud> insertTablaPaso = new ArrayList<Solicitud>();
		for(Solicitud sol : entrada.p_Solicitudes) {
			if(hashIdsSerieFondo.get(sol.IdSerieFondo) == null) {
				idsSerie.add(sol.IdSerieFondo);
				hashIdsSerieFondo.put(sol.IdSerieFondo, true);
			}
			if(hashIdsCliente.get(sol.IdCliente) == null) {
				insertTablaPaso.add(sol);
				hashIdsCliente.put(sol.IdCliente, true);
			}
		}
	
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSerieFondo", idsSerie);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondo> series = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondo.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		HashMap<String,Object> hashSeries = Utils.List2Hash(series, "IdSerieFondo");
		logger.debug("INICIO::SQL_DELETE_TABLA_PASO");
		SQL.exec(con, SQL_DELETE_TABLA_PASO);
		logger.debug("FIN::SQL_DELETE_TABLA_PASO");
		logger.debug("INICIO::SQL_INSERT_TABLA_PASO");
		sql.batch(con, SQL_INSERT_TABLA_PASO, insertTablaPaso, Solicitud.class);
		logger.debug("FIN::SQL_INSERT_TABLA_PASO");
		params.clear();
		params.put("Fecha", entrada.p_Fecha);
		logger.debug("INICIO::SQL_SELECT_CUSTODIA");
		List<Custodia> custodias = sql.query(con, SQL_SELECT_CUSTODIA, params, Custodia.class);
		logger.debug("FIN::SQL_SELECT_CUSTODIA");
		Map<String,List<Custodia>> hashCustodiaClienteEmisor = custodias.stream().collect(Collectors.groupingBy(custodia->custodia.IdClienteEmisor));
		
		HashMap<Long,BigDecimal> hashPatrimonioEmisorPorIdInternoSolicitud = new HashMap<Long,BigDecimal>(entrada.p_Solicitudes.size());
		Long i = 1L;
		HashMap<Long,Long> hashIdsCalculo = new HashMap<Long,Long>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
				
		for(Solicitud sol : entrada.p_Solicitudes) {
			SerieFondo serie = (SerieFondo) hashSeries.get(sol.IdSerieFondo);
			if(serie!=null) {
				List<Custodia> custodiasClienteEmisor = hashCustodiaClienteEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
				if(custodiasClienteEmisor != null) {
					for(Custodia custodia : custodiasClienteEmisor) {
						BigDecimal montoCustodia = hashPatrimonioEmisorPorIdInternoSolicitud.get(sol.IdInterno) == null ? BigDecimal.ZERO : hashPatrimonioEmisorPorIdInternoSolicitud.get(sol.IdInterno);
						
						if(custodia.IdMonedaInstrumento.equals(serie.IdMoneda)) {
							montoCustodia = montoCustodia.add(custodia.Monto, MathContext.DECIMAL128);
							hashPatrimonioEmisorPorIdInternoSolicitud.put(sol.IdInterno, montoCustodia);
							continue;
						}
						
						CalculoEntrada calculoEntrada = new CalculoEntrada();
						calculoEntrada.IdInterno = i;
						calculoEntrada.IdTrabajo = 1L;
						calculoEntrada.Fecha = entrada.p_Fecha;
						calculoEntrada.IdMonedaOrigen = custodia.IdMonedaInstrumento;
						calculoEntrada.IdMonedaDestino = serie.IdMoneda;
						calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
						calculoEntrada.MontoOrigen = custodia.Monto;
						calculoEntrada.IdUso = "";
						calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
						hashIdsCalculo.put(i, sol.IdInterno);
						i++;
						
					}
				}
			}
		}
		
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		
		for(CalculoSalida calculoParidad : calcularParidadSalida.p_out_CalculosSalida) {
			Long idInternoSoliciud = hashIdsCalculo.get(calculoParidad.IdInterno);
			BigDecimal montoCustodia = hashPatrimonioEmisorPorIdInternoSolicitud.get(idInternoSoliciud) == null ? BigDecimal.ZERO : hashPatrimonioEmisorPorIdInternoSolicitud.get(idInternoSoliciud);
			montoCustodia = montoCustodia.add(calculoParidad.MontoDestino, MathContext.DECIMAL128);
			hashPatrimonioEmisorPorIdInternoSolicitud.put(idInternoSoliciud, montoCustodia);
		}
	
		for(Solicitud sol : entrada.p_Solicitudes) {
			SerieFondo serie = (SerieFondo) hashSeries.get(sol.IdSerieFondo);
			if(serie != null) {
				BigDecimal montoPatrimonio = hashPatrimonioEmisorPorIdInternoSolicitud.get(sol.IdInterno) == null ? BigDecimal.ZERO : hashPatrimonioEmisorPorIdInternoSolicitud.get(sol.IdInterno);
				BigDecimal montoPatrimonioTotal = montoPatrimonio.add(sol.MontoPatrimonio, MathContext.DECIMAL128);
				if(montoPatrimonioTotal.compareTo(serie.MontoPatrimonioEmisor) == -1) {
					HashMap<String,String> parametrosError = new HashMap<String,String>(1);
					parametrosError.put("IdInstrumento", sol.IdSerieFondo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE",parametrosError,con,entrada.p_IdEmpresa));
				}
			}else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
		}
	
		out.P_OUT_ERRORESCONTADOR = Long.valueOf(out.P_OUT_MENSAJES.size());
		
		logger.info("FIN::SalidaValidaPatrimonioEmisor");
		return out;
	}
	
	

}
