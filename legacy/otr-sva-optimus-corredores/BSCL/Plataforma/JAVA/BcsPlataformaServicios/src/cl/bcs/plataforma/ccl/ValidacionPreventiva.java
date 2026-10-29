package cl.bcs.plataforma.ccl;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidacionPreventiva implements JavaCall {
	final static Logger logger = LogManager.getLogger(ValidacionPreventiva.class);
	
	private final String SQL_UPSERT_RESUMEN = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_UpsertResumen.sql");
	private final String SQL_CLIENTE_DUPLICADO = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClienteDuplicado.sql");
	private final String SQL_CLIENTE_MOVIMIENTOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClienteMovimientos.sql");
	private final String SQL_CLIENTE_PERSONA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClientePersona.sql");
	private final String SQL_CLIENTE_SALDO = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ClienteSaldo.sql");
	private final String SQL_CONCEPTO_FFMM = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ConceptoFFMM.sql");
	private final String SQL_CONTRATOCSC_MOVIMIENTOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ContratoCSCMovimientos.sql");
	private final String SQL_CONTRATOSAAPV_MOVIMIENTOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ContratoSAAPVMovimientos.sql");
	private final String SQL_DOCUMENTOIDENTIDAD_SIN_PUNTOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_DocumentoIdentidadSinPuntos.sql");
	private final String SQL_PORTAFOLIO_MOVIMIENTOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_PortafolioMovimientos.sql");
	private final String SQL_PORTAFOLIO_SALDOS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_PortafolioSaldo.sql");
	private final String SQL_TIPOMOVIMIENTO_FFMM = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_TipoMovimientoFFMM.sql");
	private final String SQL_PORTAFOLIO_AMBOS_PLANES_INVERSION = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_PortafolioAmbosPlanesInversion.sql");
	
	private final String SQL_MONEDAS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_Monedas.sql");
	private final String SQL_PRECIO_CUSTODIA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_PrecioCustodia.sql");
	private final String SQL_FECHA_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_FechaPatrimonioCaja.sql");
	private final String SQL_FECHA_PATRIMONIO_CONSOLIDADO = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_FechaPatrimonioConsolidado.sql");
	private final String SQL_FECHA_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_FechaPatrimonioCustodia.sql");
	private final String SQL_FECHA_VALIDACION_RETIROS = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_ValorizacionRetiros.sql");
	private final String SQL_PATRIMONIO_SALDO_CERO = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_PatrimonioSaldoCero.sql");
	private final String SQL_MOVIMIENTOS_MATRIZ_INVALIDA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_MovimientosMatrizInvalida.sql");
	private final String SQL_MOVIMIENTOS_VALOR_CUOTA = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_MovimientosValorCuota.sql");
	private final String SQL_MOVIMIENTOS_CONCEPTO_INVALIDO = loadResourceToString("cl/bcs/plataforma/ccl/sql/ValidacionPreventiva_MovimientosConceptoInvalido.sql");

	private final String SQL_SELECT_MAX_FECHACUSTODIA = "SELECT CAST(MAX(\"FechaRespaldo\") AS NVARCHAR(10)) AS \"FechaCustodia\" FROM \"BSCL.Custodia.BD::HIS.Cartera\";";
	
	public static class ValidacionPreventivaEntrada extends BcsPojo {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaSistemaEjecucion;
	}
	
	public static class ValidacionPreventivaSalida extends BcsPojo {
		public String Validacion;
		public String Estado = "OK";
		public List<String> Detalle = new ArrayList<String>();
	}
	
	public static class RespuestaValidacion extends BcsPojo{
		public String Valor1;
		public String Valor2;
		public String Valor3;
		public String Valor4;
		public Long   Valor5;
		public Long   Valor6;
		public Long   Valor7;
		public Long   Valor8;
	}
	
	public static class FechaCustodia extends BcsPojo{
		public String FechaCustodia = "0001-01-01";
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ValidacionPreventivaEntrada in = gson.fromJson(jin.toString(), ValidacionPreventivaEntrada.class);
		List<ValidacionPreventivaSalida> out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private String generarMensajeResultado(List<ValidacionPreventivaSalida> s,Long offset) throws JSONException, JsonProcessingException {
		StringBuilder b = new StringBuilder();
		b.append("La ejecucion de la Validacion Preventiva ha <strong>finalizado</strong> a las "+ obtenerFechaHora("Hora",offset) +".<br>"); 
		b.append("<br>");
		b.append("Resultados<br>");
		b.append("<br>");
		
		for (ValidacionPreventivaSalida res : s) {
			b.append("Tipo Validacion: " + res.Validacion + "<br>");
			b.append("Estado: " + res.Estado+ "<br>");
			b.append("Detalle: <br>");
			for(String detalle : res.Detalle){
				b.append(detalle + "<br>");
			}
			b.append("<br>");
		}	

		return b.toString();
	}
	
    private String obtenerFechaHora(String peticion,Long offset) {
    	
    	String fechaHora = "";
    	ZonedDateTime.now();
    	LocalDateTime localDate = LocalDateTime.now();
    	localDate = localDate.plusSeconds(offset);
    	DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    	DateTimeFormatter formatterFechaHoraISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    	DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    	
    	switch(peticion) {
    	case "Fecha":    		
    		fechaHora = localDate.format(formatterFecha);
    		break;
    	case "FechaHoraISO":    		
    		fechaHora = localDate.format(formatterFechaHoraISO);
    		break;
    	case "Hora":
    		fechaHora = localDate.format(formatterHora);
    		break;    	
    	case "Ambos":
    		fechaHora = localDate.format(formatterFechaHora);
    		break;    		
    	}
    	
    	return fechaHora;    	
    }
	
	public List<ValidacionPreventivaSalida> logica(Connection con,ValidacionPreventivaEntrada in) throws Exception {
		logger.info("**** INICIO VALIDACION PREVENTIVA ****");
		SQL sql = new SQL();
		List<ValidacionPreventivaSalida> salida = new ArrayList<ValidacionPreventivaSalida>();
		Long offset = ConfiguracionSistema.getParametroConfiguracionLong(con,in.p_IdEmpresa,"PLATAFORMA","PLA.SegundosZonaHoraria");
		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		String fechaSistemaEjecucion = in.p_FechaSistemaEjecucion;
		if (fechaSistemaEjecucion == null) {
			fechaSistemaEjecucion = fechaSistema;
		}
		logger.debug("FechaSistema:" + fechaSistema + " FechaSistemaEjecucion:" + fechaSistemaEjecucion);
		HashMap<String,Object> params = new HashMap<String,Object>();
			
		List<RespuestaValidacion> resultados1 = sql.query(con, SQL_CLIENTE_DUPLICADO, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado1 = new ValidacionPreventivaSalida();
		resultado1.Validacion = "Cliente Duplicado";
		if(!resultados1.isEmpty()){
			resultado1.Estado = "NOK";
			for(RespuestaValidacion res : resultados1){
				resultado1.Detalle.add("DocumentoIdentidad: " + res.Valor1);
			}
		}
		
		salida.add(resultado1);
		
		List<RespuestaValidacion> resultados2 = sql.query(con, SQL_CLIENTE_MOVIMIENTOS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado2 = new ValidacionPreventivaSalida();
		resultado2.Validacion = "Cliente asociado a movimientos no existe";
		if(!resultados2.isEmpty()){
			resultado2.Estado = "NOK";
			for(RespuestaValidacion res : resultados2){
				resultado2.Detalle.add("IdCliente: " + res.Valor5);
			}
		}
		
		salida.add(resultado2);
		
		List<RespuestaValidacion> resultados3 = sql.query(con, SQL_CLIENTE_PERSONA, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado3 = new ValidacionPreventivaSalida();
		resultado3.Validacion = "Persona asociada a Cliente no existe";
		if(!resultados3.isEmpty()){	
			resultado3.Estado = "NOK";
			for(RespuestaValidacion res : resultados3){
				resultado3.Detalle.add("IdCliente: " + res.Valor5);
			}
		}
		
		salida.add(resultado3);
		
		//if (1==2)
		{
			List<RespuestaValidacion> resultados4 = sql.query(con, SQL_CLIENTE_SALDO, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado4 = new ValidacionPreventivaSalida();
			resultado4.Validacion = "Cliente asociado a saldo no existe"; 
			if(!resultados4.isEmpty()){
				resultado4.Estado = "NOK";
				for(RespuestaValidacion res : resultados4){
					resultado4.Detalle.add("IdCliente: " + res.Valor5);
				}
			}
			salida.add(resultado4);
		}
 		
		List<RespuestaValidacion>resultados5 = sql.query(con, SQL_CONCEPTO_FFMM, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado5 = new ValidacionPreventivaSalida();
		resultado5.Validacion = "Concepto de Fondo Mutuo sin detalle";
		if(!resultados5.isEmpty()){
			resultado5.Estado = "NOK";
			for(RespuestaValidacion res : resultados5){
				resultado5.Detalle.add("CodigoConcepto: " + res.Valor1);
			}
		}

		salida.add(resultado5);
		
		List<RespuestaValidacion> resultados6 = sql.query(con, SQL_CONTRATOCSC_MOVIMIENTOS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado6 = new ValidacionPreventivaSalida();
		resultado6.Validacion = "Contrato CSC asociado a operacion no existe";
		if(!resultados6.isEmpty()){
			resultado6.Estado = "NOK";
			for(RespuestaValidacion res : resultados6){
				resultado6.Detalle.add("IdSolicitudAporteRescate: " + res.Valor5 + " - " + "FolioContratoOperacion: " + res.Valor2);
			}
		}
		
		salida.add(resultado6);
		
		List<RespuestaValidacion> resultados7 = sql.query(con, SQL_CONTRATOSAAPV_MOVIMIENTOS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado7 = new ValidacionPreventivaSalida();
		resultado7.Validacion = "Contrato SAAPV asociado a operacion no existe";
		if(!resultados7.isEmpty()){
			resultado7.Estado = "NOK";
			for(RespuestaValidacion res : resultados7){
				resultado7.Detalle.add("IdSolicitudAporteRescate: " + res.Valor5 + " - " + "FolioContratoOperacion: " + res.Valor2);
			}
		}
		
		salida.add(resultado7);
		
		List<RespuestaValidacion> resultados8 = sql.query(con, SQL_DOCUMENTOIDENTIDAD_SIN_PUNTOS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado8 = new ValidacionPreventivaSalida();
		resultado8.Validacion = "DocumentoIdentidad sin puntos";
		if(!resultados8.isEmpty()){
			resultado8.Estado = "NOK";
			for(RespuestaValidacion res : resultados8){
				resultado8.Detalle.add("DocumentoIdentidad: " + res.Valor1);
			}
		}
		
		salida.add(resultado8);
		
		List<RespuestaValidacion> resultados9 = sql.query(con, SQL_PORTAFOLIO_MOVIMIENTOS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado9 = new ValidacionPreventivaSalida();
		resultado9.Validacion = "Portafolio asociado a movimiento no existe";
		if(!resultados9.isEmpty()){
			resultado9.Estado = "NOK";
			for(RespuestaValidacion res : resultados9){
				resultado9.Detalle.add("IdPortafolio: " + res.Valor5);
			}
		}
		
		salida.add(resultado9);
		
		//if (1==2)
		{
			List<RespuestaValidacion> resultados10 = sql.query(con, SQL_PORTAFOLIO_SALDOS, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado10 = new ValidacionPreventivaSalida();
			resultado10.Validacion = "Portafolio asociado a saldo no existe";
			if(!resultados10.isEmpty()){
				resultado10.Estado = "NOK";
				for(RespuestaValidacion res : resultados10){
					resultado10.Detalle.add("IdPortafolio: " + res.Valor5);
				}
			}
			salida.add(resultado10);
		}
		
		List<RespuestaValidacion> resultados11 = sql.query(con, SQL_TIPOMOVIMIENTO_FFMM, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado11 = new ValidacionPreventivaSalida();
		resultado11.Validacion = "Tipo Movimiento Fondo Mutuo sin detalle";
		if(!resultados11.isEmpty()){
			resultado11.Estado = "NOK";
			for(RespuestaValidacion res : resultados11){
				resultado11.Detalle.add("IdTipoMovimientoFondoMutuo: " + res.Valor1);
			}
		}
		
		salida.add(resultado11);
		
		List<FechaCustodia> fechaCustodia = sql.query(con, SQL_SELECT_MAX_FECHACUSTODIA, params, FechaCustodia.class); 
		
		params.put("p_FechaCustodia", fechaCustodia.get(0).FechaCustodia);
		
		List<RespuestaValidacion>  resultados15 = sql.query(con, SQL_PRECIO_CUSTODIA, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado15 = new ValidacionPreventivaSalida();
		resultado15.Validacion = "Custodia con PrecioMercadoCustodia = 0";
		if(!resultados15.isEmpty()){
			resultado15.Estado = "NOK";
			for(RespuestaValidacion res : resultados15){
				resultado15.Detalle.add("DocumentoIdentidad: " + res.Valor1 + " - CodigoPortafolio: " + res.Valor2 + " - Nemotecnico: " + res.Valor3 + " - FechaCustodia: " + res.Valor4);
			}
		}
		
		salida.add(resultado15);
		
		//Validaciones con FechaSistema
		params.clear();
		params.put("p_FechaSistema", fechaSistema.toString());
				
		List<RespuestaValidacion> resultados12 = sql.query(con, SQL_MONEDAS, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado12 = new ValidacionPreventivaSalida();
		resultado12.Validacion = "Monedas cargadas para el dia de cierre";
		if(resultados12.isEmpty()){
			resultado12.Estado = "NOK";
			resultado12.Detalle.add("No hay monedas cargadas para el día de cierre");
		} else if(resultados12.size() == 1){
			String moneda = resultados12.get(0).Valor1;
			if(moneda.equals("DO")){				
				resultado12.Estado = "NOK";
				resultado12.Detalle.add("Moneda EUR (EURO) no cargada");
			}else{
				resultado12.Estado = "NOK";
				resultado12.Detalle.add("Moneda DO (DOLAR) no cargada");
			}
		}else{
			for(RespuestaValidacion res : resultados12){
				resultado12.Detalle.add("IdMoneda: " + res.Valor1 + " - Valor: " + res.Valor2 + " - Fecha: " + res.Valor3);
			}
		}
		
		salida.add(resultado12); 

		List<RespuestaValidacion>  resultados16 = sql.query(con, SQL_FECHA_PATRIMONIO_CAJA, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado16 = new ValidacionPreventivaSalida();
		resultado16.Validacion = "Fecha Patrimonio Caja mayor a FechaSistema";
		if(!resultados16.isEmpty()){
			resultado16.Estado = "NOK";
			for(RespuestaValidacion res : resultados16){
				resultado16.Detalle.add("DocumentoIdentidad: " + res.Valor1 + " - CodigoPortafolio: " + res.Valor2 + " - FechaPatrimonio: " + res.Valor3);
			}
		}
		
		salida.add(resultado16);
		
		List<RespuestaValidacion> resultados17 = sql.query(con, SQL_FECHA_PATRIMONIO_CONSOLIDADO, params, RespuestaValidacion.class);
		ValidacionPreventivaSalida resultado17 = new ValidacionPreventivaSalida();
		resultado17.Validacion = "Fecha Patrimonio Consolidado mayor a FechaSistema";
		if(!resultados17.isEmpty()){
			resultado17.Estado = "NOK";
			for(RespuestaValidacion res : resultados17){
				resultado17.Detalle.add("DocumentoIdentidad: " + res.Valor1 + " - CodigoPortafolio: " + res.Valor2 + " - Nemotecnico: " + res.Valor3 + " - FechaPatrimonio: " + res.Valor4);
			}
		}
		
		salida.add(resultado17);
		
		{
			List<RespuestaValidacion> resultados18 = sql.query(con, SQL_FECHA_PATRIMONIO_CUSTODIA, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado18 = new ValidacionPreventivaSalida();
			resultado18.Validacion = "Fecha Patrimonio Custodia mayor a FechaSistema";
			if(!resultados18.isEmpty()){
				resultado18.Estado = "NOK";
				for(RespuestaValidacion res : resultados18){
					resultado18.Detalle.add("DocumentoIdentidad: " + res.Valor1 + " - CodigoPortafolio: " + res.Valor2 + " - Nemotecnico: " + res.Valor3 + " - FechaPatrimonio: " + res.Valor4);
				}
			}
			salida.add(resultado18);
		}
		
		{
			params.clear();
			List<RespuestaValidacion> resultados19 = sql.query(con, SQL_PORTAFOLIO_AMBOS_PLANES_INVERSION, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado19 = new ValidacionPreventivaSalida();
			resultado19.Validacion = "Portafolios en Ambos Planes de Inversion";
			if(!resultados19.isEmpty()){
				resultado19.Estado = "NOK";
				for(RespuestaValidacion res : resultados19){
					resultado19.Detalle.add("DocumentoIdentidad: " + res.Valor1 + " - IdMatrizAgrupadorAPV1 : "+ res.Valor5 +  " - CodigoPortafolio1: " + res.Valor6 + " - IdMatrizAgrupadorAPV2 : "+ res.Valor7 + " - CodigoPortafolio2: " + res.Valor8);
				}
			}
			
			salida.add(resultado19);
		}
		
		{
			params.clear();
			params.put("p_FechaSistema", fechaSistema.toString());
			List<RespuestaValidacion> resultados20 = sql.query(con, SQL_FECHA_VALIDACION_RETIROS, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado20 = new ValidacionPreventivaSalida();
			resultado20.Validacion = "Valorizacion de Retiros";
			if(!resultados20.isEmpty()){
				resultado20.Estado = "NOK";
				for(RespuestaValidacion res : resultados20){
					resultado20.Detalle.add("FolioCartera: " + res.Valor5 + " - IdClasificacionTipoInstrumento : "+ res.Valor1 +  " - IdTipoComprobante: " + res.Valor2);
				}
			}
			salida.add(resultado20);
		}
		
		{
			params.clear();
			List<RespuestaValidacion> resultados21 = sql.query(con, SQL_PATRIMONIO_SALDO_CERO, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado21 = new ValidacionPreventivaSalida();
			resultado21.Validacion = "Patrimonios con saldo 0";
			if(resultados21.size() == 1){
				if (resultados21.get(0).Valor5 > 0) {
					resultado21.Estado = "OK";
				}
				else {
					resultado21.Estado = "NOK";
				}
				resultado21.Detalle.add("Registros con patrimonio 0:" + resultados21.get(0).Valor5);
			}
			else {
				resultado21.Estado = "NOK";
				resultado21.Detalle.add("Error al ejecutar validacion");
			}
			salida.add(resultado21);
		}
		
		{
			params.clear();
			List<RespuestaValidacion> resultados22 = sql.query(con, SQL_MOVIMIENTOS_MATRIZ_INVALIDA, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado22 = new ValidacionPreventivaSalida();
			resultado22.Validacion = "Movimientos con matriz agrupador invalida";
			if(resultados22.size() == 1){
				if (resultados22.get(0).Valor5 == 0) {
					resultado22.Estado = "OK";
				}
				else {
					resultado22.Estado = "NOK";
				}
				resultado22.Detalle.add("Movimientos con matriz agrupador invalida:" + resultados22.get(0).Valor5);
			}
			else {
				resultado22.Estado = "NOK";
				resultado22.Detalle.add("Error al ejecutar validacion");
			}
			salida.add(resultado22);
		}
		
		{
			params.clear();
			List<RespuestaValidacion> resultados22 = sql.query(con, SQL_MOVIMIENTOS_VALOR_CUOTA, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado22 = new ValidacionPreventivaSalida();
			resultado22.Validacion = "Movimientos con valor cuota historico inexistente";
			if(resultados22.size() > 0){
				if (resultados22.get(0).Valor5 == 0) {
					resultado22.Estado = "OK";
				}
				else {
					resultado22.Estado = "NOK";
				}
				resultado22.Detalle.add("Movimientos con valor cuota historico inexistente:" + resultados22.get(0).Valor5);
			}
			else {
				resultado22.Estado = "NOK";
				resultado22.Detalle.add("Error al ejecutar validacion");
			}
			salida.add(resultado22);
		}
		
		{
			params.clear();
			List<RespuestaValidacion> resultados24 = sql.query(con, SQL_MOVIMIENTOS_CONCEPTO_INVALIDO, params, RespuestaValidacion.class);
			ValidacionPreventivaSalida resultado24 = new ValidacionPreventivaSalida();
			resultado24.Validacion = "Movimientos con conceptos que no existen";
			if(!resultados24.isEmpty()){
				resultado24.Estado = "NOK";
				List<String> conceptos = resultados24.stream().map(s -> s.Valor1).collect(Collectors.toList());
				resultado24.Detalle.add("Conceptos que no existen:" + String.join(",", conceptos));
			}
			else {
				resultado24.Estado = "OK";
				resultado24.Detalle.add("");
			}
			salida.add(resultado24);
		}
		
		
		Timestamp ts = Fecha.toDateTime(obtenerFechaHora("FechaHoraISO",offset));
		List<ResumenReplica> resumen = new ArrayList<>();
		for (ValidacionPreventivaSalida s : salida) {
			ResumenReplica r = new ResumenReplica();
			r.Tipo = "PREVENTIVO";
			r.Estado = s.Estado;
			r.FechaSistema = Fecha.toDate(fechaSistemaEjecucion);
			r.Nombre = s.Validacion;
			String detalle = "";
			for (String f : s.Detalle) {
				detalle+=f;
			}
			if (detalle.length() >= 4000) {
				r.Detalle = detalle.substring(0,4000);
			}
			else {
				r.Detalle = detalle;
					
			}
			r.UltimaActualizacion = ts;
			resumen.add(r);
		}

		try {
			sql.batch(con, SQL_UPSERT_RESUMEN, resumen, ResumenReplica.class);
		}
		catch(Exception e) {
			logger.error(e);
		}
		
		logger.info("**** FIN VALIDACION PREVENTIVA ****");
		String mensaje = generarMensajeResultado(salida,offset);
		
		//Notificacion
		PautaUtils.notificarUsuariosTI("VALIDACION_PREVENTIVA", in.p_IdEmpresa, "NO", mensaje , 1L, con, null);
		
		return salida;
	}
	
	public static class ResumenReplica {
		public String Tipo;
		public String Nombre;
		public String Estado;
		public String Detalle;
		public Date FechaSistema;
		public Timestamp UltimaActualizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("mbipre","connections.properties")) {
			ValidacionPreventiva validacion =  new ValidacionPreventiva();
			ValidacionPreventivaEntrada in = new ValidacionPreventivaEntrada();
			in.p_IdEmpresa = new Long(1);
			in.p_IdUsuario = new Long(0);
			in.p_IdModulo = "PLA";
			List<ValidacionPreventivaSalida> out = validacion.logica(con,in);
			System.out.println(out);
			con.commit();
	    }
		 	
	}
}
