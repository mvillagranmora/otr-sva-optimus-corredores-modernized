package cl.bcs.plataforma.val;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.val.Valorizar.InValorizar.InstrumentoWS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValorizarSubyacentePacto implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValorizarSubyacentePacto.class.getName());
	public static BigDecimal BASE_REAJUSTABLE = new BigDecimal(360);
	public static BigDecimal BASE_NO_REAJUSTABLE =new BigDecimal(30);
	
	public static class EntradaValorizarSubyacentePacto extends BcsPojo{
		
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_IdModulo;
		public String			p_CalculaFactor;
		public List<Valorizar.InValorizar.InstrumentoWS>  p_InstrumentosWS;
		
	}
	
	public static class SalidaValorizarSubyacentePacto extends BcsPojo{
		
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;		
		public HashMap<Long,Valorizar.OutValorizar.InstrumentoWS>    P_RESULTADOS_VALORIZACION;
		
		
		public SalidaValorizarSubyacentePacto(){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR 		=0L;
			P_RESULTADOS_VALORIZACION   = new HashMap<Long,Valorizar.OutValorizar.InstrumentoWS>();
		}
	}
	
	
	@Trace
	public SalidaValorizarSubyacentePacto logica(Connection con, EntradaValorizarSubyacentePacto entrada) throws Exception {
		logger.info("INICIO::ValorizarSubyacentePacto");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaValorizarSubyacentePacto salida = new SalidaValorizarSubyacentePacto();
		List<CalculoSalida> listaParidad  = new ArrayList<>();	
		List<CalculoSalida> listaFactor  = new ArrayList<>();	
		List<Valorizar.OutValorizar.InstrumentoWS> listaSalidaValorizacion = new ArrayList<>();
		List<Valorizar.OutValorizar.InstrumentoWS> listaCalculoParidad = new ArrayList<>();
		List<Valorizar.OutValorizar.InstrumentoWS> listaSinCalculoParidad = new ArrayList<>();
		List<Valorizar.OutValorizar.InstrumentoWS> listaCalculoFactor = new ArrayList<>();

		entrada.p_InstrumentosWS.forEach(valorizacion -> {
			
			try {
				Valorizar.OutValorizar.InstrumentoWS InsWs = new Valorizar.OutValorizar.InstrumentoWS();			
				this.copyValores(valorizacion,InsWs);	
				this.valorizarPorPlazo(valorizacion, InsWs, salida);				
				
				
				listaSalidaValorizacion.add(InsWs);
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		});
		
		//filtro a quienes deberia calcular paridad		
		listaCalculoParidad = listaSalidaValorizacion.stream().filter( val-> !("CLP".equals(val.IdTipoReajusteInstrumento))).collect(Collectors.toList());
		listaSinCalculoParidad = listaSalidaValorizacion.stream().filter( val-> "CLP".equals(val.IdTipoReajusteInstrumento)).collect(Collectors.toList());		
		for (Valorizar.OutValorizar.InstrumentoWS val : listaSinCalculoParidad) {			
			val.MontoMonedaInstrumento =val.MontoMonedaSistema;
			val.MontoTotalMonedaSistema = val.MontoMonedaSistema;
			val.MontoTotalMonedaInstrumento = val.MontoMonedaSistema;
		}
		
		//crear salida 			
		listaSalidaValorizacion.forEach(val -> {				
			val.ResultadoProceso ="Registro procesado";
			salida.P_RESULTADOS_VALORIZACION.put(val.IdValorizacion,val);
		});		
		
		//Calculo de paridad		
		if(!listaCalculoParidad.isEmpty()) {
			listaParidad = this.calcularParidad(con,entrada, salida,listaCalculoParidad);
		}
		
		//Asigna resultados de la paridad 
		listaParidad.forEach(val -> {				
			if(salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno)!=null) {
				salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).MontoMonedaInstrumento= val.MontoDestino;
				salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).MontoTotalMonedaInstrumento= val.MontoDestino;
				salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).MontoMonedaSistema= val.MontoOrigen;
				salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).MontoTotalMonedaSistema= val.MontoOrigen;
				
			}		
			
		});	
		
		if("S".equals(entrada.p_CalculaFactor)) {
			HashMap<Long, String> mapReajusteCabecera = new HashMap<>();
			
			entrada.p_InstrumentosWS.forEach(x ->
			    mapReajusteCabecera.put(x.IdValorizacion, x.IdReajusteCabecera)
			);


			listaCalculoFactor = listaSalidaValorizacion.stream()
			    .filter(val -> {
			        String reajuste = mapReajusteCabecera.get(val.IdValorizacion);
			        return reajuste != null && !"CLP".equals(reajuste);
			    })
			    .collect(Collectors.toList());
			
			if(!listaCalculoFactor.isEmpty()) {
				listaFactor  = this.calcularFactor(con, entrada, salida, listaCalculoFactor, mapReajusteCabecera);
			}
			
			listaFactor.forEach(val -> {				
				if(salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno)!=null) {
					salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).ValorTipoReajuste= val.ValorParametroFinanciero;
					salida.P_RESULTADOS_VALORIZACION.get(val.IdInterno).FactorMonedaInstrumento = val.ValorParametroFinanciero;
				}					
			});
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValorizarSubyacentePacto :" , finServicio- inicioServicio);		
		return salida;		
	}
	
	
	private List<CalculoSalida> calcularParidad(Connection con,EntradaValorizarSubyacentePacto entrada,SalidaValorizarSubyacentePacto salida,List<Valorizar.OutValorizar.InstrumentoWS> listaCalculoParidad) throws Exception {
		logger.trace("INICIO::ValorizarSubyacentePacto.calcularParidad");
		long inicioServicio = System.currentTimeMillis();
		
		CalcularParidad paridad = new CalcularParidad ();
		CalcularParidadEntrada entradaParidad =  new CalcularParidadEntrada();
		CalcularParidadSalida  salidaParidad = new CalcularParidadSalida();
		List<CalculoEntrada> listaCalculosEntrada = new ArrayList<CalculoEntrada>();
		
		String IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		listaCalculosEntrada = listaCalculoParidad.stream().map(val-> {
			
			CalculoEntrada calculoValorizacion = new CalculoEntrada();
			calculoValorizacion.IdTrabajo = 0L;
			calculoValorizacion.IdInterno = val.IdValorizacion;
			calculoValorizacion.Fecha = val.FechaValorizacion.toString();
			calculoValorizacion.IdMonedaOrigen  = IdMonedaSistema;
			calculoValorizacion.IdMonedaDestino = val.IdTipoReajusteInstrumento; 
			calculoValorizacion.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoValorizacion.MontoOrigen = val.MontoMonedaSistema;
			calculoValorizacion.IdUso = "VALORIZADOR";			
			
			return calculoValorizacion;
		}).collect(Collectors.toList());
		
		entradaParidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaParidad.p_IdTrabajo = 1L;
		entradaParidad.p_CalculosEntrada=listaCalculosEntrada;
		
		salidaParidad = paridad.logica(con, entradaParidad);
		
		
		//controlar errores				
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.calcularParidad :" , finServicio- inicioServicio);	
		return salidaParidad.p_out_CalculosSalida;
	}
	
	private List<CalculoSalida> calcularFactor(Connection con,EntradaValorizarSubyacentePacto entrada,SalidaValorizarSubyacentePacto salida,List<Valorizar.OutValorizar.InstrumentoWS> listaCalculoFactor, HashMap<Long, String> mapReajusteCabecera) throws Exception {
		logger.trace("INICIO::ValorizarSubyacentePacto.calcularFactor");
		long inicioServicio = System.currentTimeMillis();
		
		CalcularParidad paridad = new CalcularParidad ();
		CalcularParidadEntrada entradaParidad =  new CalcularParidadEntrada();
		CalcularParidadSalida  salidaParidad = new CalcularParidadSalida();
		List<CalculoEntrada> listaCalculosEntrada = new ArrayList<CalculoEntrada>();
				
		listaCalculosEntrada = listaCalculoFactor.stream().map(val-> {
			String idReajusteCabecera = mapReajusteCabecera.get(val.IdValorizacion);
			
			CalculoEntrada calculoValorizacion = new CalculoEntrada();
			calculoValorizacion.IdTrabajo = 0L;
			calculoValorizacion.IdInterno = val.IdValorizacion;
			calculoValorizacion.Fecha = val.FechaValorizacion.toString();
			calculoValorizacion.IdMonedaOrigen  = idReajusteCabecera;
			calculoValorizacion.IdMonedaDestino = idReajusteCabecera; 
			calculoValorizacion.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoValorizacion.MontoOrigen = val.MontoMonedaSistema;
			calculoValorizacion.IdUso = "VALORIZADOR";			
			
			return calculoValorizacion;
		}).collect(Collectors.toList()); 		
		entradaParidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaParidad.p_IdTrabajo = 1L;
		entradaParidad.p_CalculosEntrada=listaCalculosEntrada;
		
		salidaParidad = paridad.logica(con, entradaParidad);
		
			
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.calcularFactor :" , finServicio- inicioServicio);	
		return salidaParidad.p_out_CalculosSalida;
	}


	private void copyValores(InstrumentoWS valorizacion,Valorizar.OutValorizar.InstrumentoWS InsWs) {
		logger.trace("INICIO::ValorizarSubyacentePacto.copyValores");
		long inicioServicio = System.currentTimeMillis();
		
		InsWs.IdValorizacion    = valorizacion.IdValorizacion;
		InsWs.FechaValorizacion = Fecha.toDate(valorizacion.FechaValorizacion);
		InsWs.IdInstrumento		= valorizacion.IdInstrumento;
		InsWs.Nemotecnico       = valorizacion.Nemotecnico;
		InsWs.FechaVencimiento  = Fecha.toDate(valorizacion.FechaVencimiento);
		InsWs.IdClasificacionTipoInstrumento = valorizacion.IdClasificacionTipoInstrumento;
		InsWs.Tasa				=valorizacion.Tasa;
		InsWs.Precio			=valorizacion.Precio;
		InsWs.Cantidad			=valorizacion.Cantidad;
		InsWs.ValorRescate		=valorizacion.ValorRescate;
		InsWs.IdTipoReajusteInstrumento= valorizacion.IdTipoReajusteInstrumento;
		InsWs.Emisor 			= valorizacion.Emisor;
		InsWs.IdCondicionDeLiquidacion = valorizacion.IdCondicionDeLiquidacion;
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.copyValores :" , finServicio- inicioServicio);		
		
	}


	private void valorizarPorPlazo (Valorizar.InValorizar.InstrumentoWS valorizacion ,Valorizar.OutValorizar.InstrumentoWS InsWs,SalidaValorizarSubyacentePacto salida) throws Exception {
		logger.trace("INICIO::ValorizarSubyacentePacto.valorizarPorPlazo");
		long inicioServicio = System.currentTimeMillis();
		
		BigDecimal base = BigDecimal.ZERO;
		BigDecimal plazo = BigDecimal.ZERO;
		
		//verificar cual base debe ocupar
		if(("CLP").equals(valorizacion.IdReajusteCabecera)) {
			base= BASE_NO_REAJUSTABLE;
		}else {
			base= BASE_REAJUSTABLE;
		}
		
		//plazo	
		if(valorizacion.FechaEmision.compareTo(valorizacion.FechaValorizacion)<0 ) {
			plazo = calculoPlazo(valorizacion.FechaEmision, valorizacion.FechaValorizacion, salida);
		}
		
		//calcular monto
		if(plazo.compareTo(BigDecimal.ZERO)==0) {
			InsWs.MontoMonedaSistema = valorizacion.Monto;
			InsWs.MontoTotalMonedaSistema = valorizacion.Monto;
		}else {
			InsWs.MontoMonedaSistema = this.formulaCalculo(valorizacion,base,plazo,salida);
			InsWs.MontoTotalMonedaInstrumento = this.formulaCalculo(valorizacion,base,plazo,salida);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.valorizarPorPlazo :" , finServicio- inicioServicio);				
	}
	
	private BigDecimal calculoPlazo (String fechaInicio, String fechaFinal, SalidaValorizarSubyacentePacto salida)  {	
		logger.trace("INICIO::ValorizarSubyacentePacto.calculoPlazo");
		long inicioServicio = System.currentTimeMillis();
		
		BigDecimal resultado = BigDecimal.ZERO;
		String formato = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(formato);
		
		try {
			Date dateInicio= sdf.parse(fechaInicio);
			Date dateFin = sdf.parse(fechaFinal);

			long diferenciaEnMinutos =  dateFin.getTime() - dateInicio.getTime();
			long cantidadDias = (TimeUnit.MINUTES.convert(diferenciaEnMinutos, TimeUnit.MILLISECONDS)/60)/24;
			
			resultado = new BigDecimal(cantidadDias);
			
		}catch (Exception e) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR_CALCULO_PLAZO","Error al calcular el plazo");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.calculoPlazo :" , finServicio- inicioServicio);	
		
		return resultado;
	}
	
	
	public BigDecimal formulaCalculo (Valorizar.InValorizar.InstrumentoWS valorizacion, BigDecimal base, BigDecimal plazo,SalidaValorizarSubyacentePacto salida) {		
		logger.trace("INICIO::ValorizarSubyacentePacto.formulaCalculo");
		long inicioServicio = System.currentTimeMillis();
		
		BigDecimal resultado  = BigDecimal.ZERO;
		BigDecimal plazo_base =  BigDecimal.ZERO;
		BigDecimal tasa =  BigDecimal.ZERO;
		
		try {
			plazo_base = plazo.divide(base, MathContext.DECIMAL128);
			tasa = valorizacion.Tasa.divide(new BigDecimal(100), MathContext.DECIMAL128);		
			resultado = (valorizacion.Monto.multiply(BigDecimal.ONE.add(tasa.multiply(plazo_base)))).setScale(4, RoundingMode.HALF_UP);	
			
		}catch(Exception e) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR_AL_VALORIZAR","Error al valorizar monto");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
		}
		
	
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ValorizarSubyacentePacto.formulaCalculo :" , finServicio- inicioServicio);	
		
		return resultado;
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaValorizarSubyacentePacto jin=gson.fromJson(in.toString() ,EntradaValorizarSubyacentePacto.class);
		SalidaValorizarSubyacentePacto out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			
			System.out.println("COMIENZA");
			Gson gson = Utils.getGsonBuilder();
			String jsonCLP = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_InstrumentosWS\":[{\"IdValorizacion\":10008,\"FechaValorizacion\":\"2023-03-29\",\"IdInstrumento\":2224,\"Nemotecnico\":\"PAGARE NR\",\"FechaEmision\":\"2023-03-28\",\"FechaVencimiento\":\"2023-04-07\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"Cantidad\":1.00000000,\"ValorRescate\":2400000.00000000,\"Precio\":null,\"Tasa\":3.10000000,\"Monto\":2386002.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"63\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"},{\"IdValorizacion\":10009,\"FechaValorizacion\":\"2023-03-30\",\"IdInstrumento\":2224,\"Nemotecnico\":\"PAGARE NR\",\"FechaEmision\":\"2023-03-28\",\"FechaVencimiento\":\"2023-04-07\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"Cantidad\":1.00000000,\"ValorRescate\":2400000.00000000,\"Precio\":null,\"Tasa\":3.10000000,\"Monto\":2386002.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"63\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"},{\"IdValorizacion\":10010,\"FechaValorizacion\":\"2023-03-31\",\"IdInstrumento\":2224,\"Nemotecnico\":\"PAGARE NR\",\"FechaEmision\":\"2023-03-28\",\"FechaVencimiento\":\"2023-04-07\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"Cantidad\":1.00000000,\"ValorRescate\":2400000.00000000,\"Precio\":null,\"Tasa\":3.10000000,\"Monto\":2386002.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"63\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"}]}";
			String jsonUSD = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_InstrumentosWS\":[{\"IdValorizacion\":10008,\"FechaValorizacion\":\"2023-03-27\",\"IdInstrumento\":2223,\"Nemotecnico\":\"PAGARE R\",\"FechaEmision\":\"2023-03-27\",\"FechaVencimiento\":\"2023-04-06\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"USD\",\"IdTipoReajusteInstrumento\":\"USD\",\"Cantidad\":1.00000000,\"ValorRescate\":5000.00000000,\"Precio\":null,\"Tasa\":3.30000000,\"Monto\":4055420.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"62\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"},{\"IdValorizacion\":10009,\"FechaValorizacion\":\"2023-03-27\",\"IdInstrumento\":2223,\"Nemotecnico\":\"PAGARE R\",\"FechaEmision\":\"2023-03-27\",\"FechaVencimiento\":\"2023-04-06\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"USD\",\"IdTipoReajusteInstrumento\":\"USD\",\"Cantidad\":1.00000000,\"ValorRescate\":5000.00000000,\"Precio\":null,\"Tasa\":3.30000000,\"Monto\":4055420.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"62\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"},{\"IdValorizacion\":10010,\"FechaValorizacion\":\"2023-03-28\",\"IdInstrumento\":2223,\"Nemotecnico\":\"PAGARE R\",\"FechaEmision\":\"2023-03-27\",\"FechaVencimiento\":\"2023-04-06\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"USD\",\"IdTipoReajusteInstrumento\":\"USD\",\"Cantidad\":1.00000000,\"ValorRescate\":5000.00000000,\"Precio\":null,\"Tasa\":3.30000000,\"Monto\":4055420.00000000,\"IdClasificacionTipoInstrumento\":\"IF\",\"Emisor\":\"AGUAS\",\"IdCondicionDeLiquidacion\":null,\"TipoValorizacionInstrumento\":\"62\",\"SubTipoValorizacion\":\"PAGARE\",\"TipoCalculoInteresesCupon\":null,\"Base\":30,\"ValorizaCorteDeCupon\":\"N\",\"OrigenValorizacion\":null,\"MontoUI\":null,\"IdReajusteCabecera\":\"CLP\"}]}";
			
			EntradaValorizarSubyacentePacto Objin = gson.fromJson(jsonUSD, EntradaValorizarSubyacentePacto.class);
							
			ValorizarSubyacentePacto  kk = new ValorizarSubyacentePacto();
			SalidaValorizarSubyacentePacto asdf = kk.logica(con, Objin);
			con.rollback();
	    	    	
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("TERMINA");

	}
}
