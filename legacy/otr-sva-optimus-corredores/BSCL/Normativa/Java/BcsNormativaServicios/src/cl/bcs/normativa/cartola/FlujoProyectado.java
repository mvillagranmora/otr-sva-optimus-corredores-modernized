package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.bcs.normativa.cartola.CarteraForward.SalidaCarteraForward;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FlujoProyectado implements BcsCartola{
	
	public final Log log = new Log("NOR.FlujoProyectado");
	private final Logger logger =  LogManager.getLogger(FlujoProyectado.class.getName());
	
	private final String[] TIPO_FLUJO_PROYECTADO_CHILE = {"Cupones", "Dividendos", "Depositos", "Forwards", "Futuros", "Opciones", "Pactos", "Simultaneas"};
	private final String[] TIPO_FLUJO_PROYECTADO_PERU = {"Cupones", "Dividendos", "Simultaneas"};
	
	private final String SQL_SELECT_MOVIMIENTOS_30_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectMovimientos30Dias.sql");
	private final String SQL_SELECT_MOVIMIENTOS_90_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectMovimientos90Dias.sql");
	private final String SQL_SELECT_MOVIMIENTOS_30_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectMovimientos30DiasHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_90_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectMovimientos90DiasHistorico.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_SELECT_DIVIDENDOS_30_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectDividendosCliente_30Dias.sql");
	private final String SQL_SELECT_DIVIDENDOS_90_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectDividendosCliente_90Dias.sql");
	private final String SQL_SELECT_DIVIDENDOS_30_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectDividendosCliente_30DiasHistorico.sql");
	private final String SQL_SELECT_DIVIDENDOS_90_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoProyectado_SelectDividendosCliente_90DiasHistorico.sql");
	private final String SQL_INSERT_TEMPORAL_CLIENTES = "INSERT INTO #TempIdClientes SELECT \"IdCliente\"  FROM BSCL.\"BSCL.SocioNegocio.BD::CLI.Cliente\" WHERE \"IdCliente\"IN (:IdTemp)";
	private final String SQL_INSERT_TEMPORAL_PORTAFOLIOS = "INSERT INTO #TempIdPortafolios SELECT \"IdPortafolio\"  FROM BSCL.\"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdPortafolio\"IN (:IdTemp)";
	private final String SQL_CREATE_TEMPORAL_CLIENTES = "CREATE LOCAL TEMPORARY TABLE #TempIdClientes (IdCliente BIGINT)";
	private final String SQL_CREATE_TEMPORAL_PORTAFOLIOS = "CREATE LOCAL TEMPORARY TABLE #TempIdPortafolios (IdPortafolio BIGINT)"; 
	
	
	public static class DistribucionProyeccion{
		public Long IdCliente;
		public Long IdPortafolio;
		public Date FechaLiquidacion;
		public String Item;
		public String IdMonedaInstrumento;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String IdMonedaAdicional;
		public BigDecimal MontoMonedaAdicional;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class ResultadoFlujoProyectado{
		
		public Long IdCliente;
		public Long IdPortafolio;
		public String Item;
		public String TipoDivision;
		public String Moneda;
		public BigDecimal P30;
		public BigDecimal P30_90;
		
		public ResultadoFlujoProyectado() {
			this.P30 = BigDecimal.ZERO;
			this.P30_90 = BigDecimal.ZERO;
			this.TipoDivision = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertTemporalIds{
		public Long IdTemp;
		public String toString() {
			return Utils.printJson(this, false);
		}
	    public InsertTemporalIds(Long idTemp) {
	        this.IdTemp = idTemp;
	    }

	} 
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::FlujoProyectado");
		SQL sql = new SQL();
		
		SalidaCartola out = new SalidaCartola();
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		String monedaAdicional = null;
		Long idPaisEmpresa = empresa.IdPais;
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		
		InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
		inObtenerDiaSiguiente.p_diaHabil = "N";
		
		FechasIn inFecha30Dias = new FechasIn();
		inFecha30Dias.fechaInicial = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		inFecha30Dias.numeroDias = 30L;
		FechasIn inFecha90Dias = new FechasIn();
		inFecha90Dias.fechaInicial = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		inFecha90Dias.numeroDias = 90L;
		
		inObtenerDiaSiguiente.p_fechas.add(inFecha30Dias);
		inObtenerDiaSiguiente.p_fechas.add(inFecha90Dias);		
		
		OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		
		String fecha30Dias = null;
		String fecha90Dias = null;
		
		for(FechasOut fechaOut : outObtenerDiaSiguiente.p_out_fechas) {
			if(fechaOut.numeroDiasInicial.equals(30L)) {
				fecha30Dias = fechaOut.fechaCalculada;
			}
			if(fechaOut.numeroDiasInicial.equals(90L)) {
				fecha90Dias = fechaOut.fechaCalculada;
			}
		}
		
		List<Long> portafolios = new ArrayList<Long>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		for(Dato dato : entrada.p_Datos){
			if(!portafolios.contains(dato.IdPortafolio)) {
				 portafolios.add(dato.IdPortafolio);
					Portafolio por = new Portafolio();
					por.IdTrabajo = idTrabajo;
					por.IdPortafolio = dato.IdPortafolio;
					portObj.add(por);
			}
		}
		sql.batch(con,SQL_INSERT_PORTAFOLIOS_LEGACY,portObj,Portafolio.class);

		List<DistribucionProyeccion> distribucionProyeccion30Dias = new ArrayList<DistribucionProyeccion>();
		List<DistribucionProyeccion> distribucionProyeccion90Dias = new ArrayList<DistribucionProyeccion>();
		
		List<DistribucionProyeccion> distribucionProyeccion30DiasDV = new ArrayList<>();
		List<DistribucionProyeccion> distribucionProyeccion90DiasDV = new ArrayList<>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaProyectadaDesde", fecha30Dias);		
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			distribucionProyeccion30Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_30_DIAS, params, DistribucionProyeccion.class);
		}else {			
			params.put("FechaHasta", entrada.p_FechaHasta);			
			distribucionProyeccion30Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_30_DIAS_HISTORICO, params, DistribucionProyeccion.class);
		}
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			params.put("FechaProyectadaHasta", fecha90Dias);
			distribucionProyeccion90Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_90_DIAS, params, DistribucionProyeccion.class);
		}else {
			params.put("FechaProyectadaHasta", fecha90Dias);			
			distribucionProyeccion90Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_90_DIAS_HISTORICO, params, DistribucionProyeccion.class);
		}		
		
		//filtro para deporsitos 
		
		List<DistribucionProyeccion>  distribucionProyeccion30DiasDP = distribucionProyeccion30Dias.stream().filter(mov-> "DEPOSITOS".equals(mov.Item)).collect(Collectors.toList());
		List<DistribucionProyeccion>  distribucionProyeccion90DiasDP = distribucionProyeccion90Dias.stream().filter(mov-> "DEPOSITOS".equals(mov.Item)).collect(Collectors.toList());
		
		distribucionProyeccion30Dias.removeAll(distribucionProyeccion30DiasDP);
		distribucionProyeccion90Dias.removeAll(distribucionProyeccion90DiasDP);
		
		//paridad y tipo cambio
		
		this.calcularParidad( con,entrada,fechaSistema,monedaSistema ,distribucionProyeccion30DiasDP,distribucionProyeccion90DiasDP);
		
		distribucionProyeccion30Dias.addAll(distribucionProyeccion30DiasDP);
		distribucionProyeccion90Dias.addAll(distribucionProyeccion90DiasDP);
		
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
		
		log.write("Inicio consulta de dividendos - custodia");


		SQL.exec(con,SQL_CREATE_TEMPORAL_CLIENTES);
		SQL.exec(con,SQL_CREATE_TEMPORAL_PORTAFOLIOS);

		HashMap<String,Object> paramsDV = new HashMap<String,Object>();
		paramsDV.put("IdEmpresa", entrada.p_IdEmpresa);
		paramsDV.put("FechaDesde", inFecha30Dias.fechaInicial);
		List<InsertTemporalIds> paramsinsertCli = entrada.p_Datos.stream().map(dato -> new InsertTemporalIds(dato.IdCliente)).collect(Collectors.toList());
		sql.batch(con,SQL_INSERT_TEMPORAL_CLIENTES, paramsinsertCli, InsertTemporalIds.class);
		List<InsertTemporalIds> paramsinsertPor = entrada.p_Datos.stream().map(dato -> new InsertTemporalIds(dato.IdCliente)).collect(Collectors.toList());
		sql.batch(con,SQL_INSERT_TEMPORAL_PORTAFOLIOS, paramsinsertPor, InsertTemporalIds.class);
		
		if(fechaSistema.compareTo(inFecha30Dias.fechaInicial)<= 0) {
			distribucionProyeccion30DiasDV = sql.query(con, SQL_SELECT_DIVIDENDOS_30_DIAS, paramsDV, DistribucionProyeccion.class);
			distribucionProyeccion90DiasDV = sql.query(con, SQL_SELECT_DIVIDENDOS_90_DIAS, paramsDV, DistribucionProyeccion.class);
		}else {
			distribucionProyeccion30DiasDV = sql.query(con, SQL_SELECT_DIVIDENDOS_30_DIAS_HISTORICO, paramsDV, DistribucionProyeccion.class);
			distribucionProyeccion90DiasDV = sql.query(con, SQL_SELECT_DIVIDENDOS_90_DIAS_HISTORICO, paramsDV, DistribucionProyeccion.class);
		}
		
		SQL.exec(con, "DROP TABLE #TempIdClientes");
		SQL.exec(con, "DROP TABLE #TempIdPortafolios");
		log.write("Fin consulta dividendos - custodia");		

		//CarteraFWD
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = entrada.p_IdEmpresa;
		Objin.p_IdModulo = entrada.p_IdModulo;
		Objin.p_IdUsuario = entrada.p_IdUsuario;
		Objin.p_FechaDesde = entrada.p_FechaDesde;
		Objin.p_FechaHasta = entrada.p_FechaHasta;
		
		for(Dato datos : entrada.p_Datos){
			Dato dato = new Dato();
			dato.IdCliente 		= datos.IdCliente;		
			dato.IdPortafolio 	= datos.IdPortafolio;	
			Objin.p_Datos.add(dato);
		}
		
		CarteraForward  procesarCarteraFWD = new CarteraForward();
		SalidaCartola ObjOut = procesarCarteraFWD.procesar(con, Objin);
		
		if(!ObjOut.P_OUT_SALIDA.isEmpty() && ObjOut.P_OUT_SALIDA.containsKey("carteraForward")) {
			final ObjectMapper objMapper = new ObjectMapper();
			List<SalidaCarteraForward> pojo = objMapper.convertValue(ObjOut.P_OUT_SALIDA.get("carteraForward"), List.class);
			
			BigDecimal montoMonedaSistema30 = BigDecimal.ZERO;
			BigDecimal montoMonedaSistema90 = BigDecimal.ZERO;
			Integer plazoResidual = 0;
			
			for(int k=0; k<pojo.size();k++) {
				SalidaCarteraForward cartera = objMapper.convertValue(pojo.get(k), SalidaCarteraForward.class);				

				plazoResidual = cartera.saldoPlazo;

				if (plazoResidual <= 30 && distribucionProyeccion30Dias.size() > 0 && distribucionProyeccion30Dias.get(0).Item.contains("FORWARDS")) {	
					montoMonedaSistema30 = montoMonedaSistema30.add(cartera.montoPeso.setScale(2, RoundingMode.HALF_UP));
					distribucionProyeccion30Dias.get(0).MontoMonedaSistema = montoMonedaSistema30;
				}				
				
				if (plazoResidual > 30 && distribucionProyeccion90Dias.size() > 0 && distribucionProyeccion90Dias.get(0).Item.contains("FORWARDS")) {	
					montoMonedaSistema90 = montoMonedaSistema90.add(cartera.montoPeso.setScale(2, RoundingMode.HALF_UP));					
					distribucionProyeccion90Dias.get(0).MontoMonedaSistema = montoMonedaSistema90;
				}
			
			}		
		}

		String[] tiposFlujos = null;
		
		if("CL".equals(localizacionSistema)) {
			tiposFlujos = TIPO_FLUJO_PROYECTADO_CHILE;
		}else if("PE".equals(localizacionSistema)) {
			tiposFlujos = TIPO_FLUJO_PROYECTADO_PERU;
		}
		
		List<ResultadoFlujoProyectado> salidas = new ArrayList<ResultadoFlujoProyectado>();
		
		if(tiposFlujos != null) {
			for(Dato dato : entrada.p_Datos) {
				for(String tipoFlujo : tiposFlujos) {
					ResultadoFlujoProyectado resultadoFlujoProyectadoMonedaSistema = new ResultadoFlujoProyectado();
					resultadoFlujoProyectadoMonedaSistema.IdCliente = dato.IdCliente;
					resultadoFlujoProyectadoMonedaSistema.IdPortafolio = dato.IdPortafolio;
					resultadoFlujoProyectadoMonedaSistema.Item = tipoFlujo;
					resultadoFlujoProyectadoMonedaSistema.Moneda = monedaSistema;
					//P30
					for(DistribucionProyeccion distribucion : distribucionProyeccion30Dias) {
						if(dato.IdCliente.equals(distribucion.IdCliente) && dato.IdPortafolio.equals(distribucion.IdPortafolio) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
							resultadoFlujoProyectadoMonedaSistema.P30 = resultadoFlujoProyectadoMonedaSistema.P30.add(distribucion.MontoMonedaSistema);
						}
					}
					
					//P30_90
					for(DistribucionProyeccion distribucion : distribucionProyeccion90Dias) {
						if(dato.IdCliente.equals(distribucion.IdCliente) && dato.IdPortafolio.equals(distribucion.IdPortafolio) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
							resultadoFlujoProyectadoMonedaSistema.P30_90 = resultadoFlujoProyectadoMonedaSistema.P30_90.add(distribucion.MontoMonedaSistema);
						}
					}
													
					if("DIVIDENDOS".equalsIgnoreCase(tipoFlujo)) {
						resultadoFlujoProyectadoMonedaSistema.P30 = resultadoFlujoProyectadoMonedaSistema.P30.add(distribucionProyeccion30DiasDV.stream()
								.filter(pdv -> dato.IdCliente.equals(pdv.IdCliente) && dato.IdPortafolio.equals(pdv.IdPortafolio))
								.map(pdv -> pdv.MontoMonedaSistema)
								.reduce(BigDecimal.ZERO, BigDecimal::add));
						
						resultadoFlujoProyectadoMonedaSistema.P30_90 = resultadoFlujoProyectadoMonedaSistema.P30_90.add(distribucionProyeccion90DiasDV.stream()
								.filter(pdv -> dato.IdCliente.equals(pdv.IdCliente) && dato.IdPortafolio.equals(pdv.IdPortafolio))
								.map(pdv -> pdv.MontoMonedaSistema)
								.reduce(BigDecimal.ZERO, BigDecimal::add));		
					}
					
					salidas.add(resultadoFlujoProyectadoMonedaSistema);
					
					if("N".equals(entrada.p_FiltroSoloMonedaSistema)) {
						ResultadoFlujoProyectado resultadoFlujoProyectadoMonedaAdicional = new ResultadoFlujoProyectado();
						resultadoFlujoProyectadoMonedaAdicional.IdCliente = dato.IdCliente;
						resultadoFlujoProyectadoMonedaAdicional.IdPortafolio = dato.IdPortafolio;
						resultadoFlujoProyectadoMonedaAdicional.Item = tipoFlujo;
						resultadoFlujoProyectadoMonedaAdicional.Moneda = monedaAdicional;
						
						//P30
						for(DistribucionProyeccion distribucion : distribucionProyeccion30Dias) {
							if(dato.IdCliente.equals(distribucion.IdCliente) && dato.IdPortafolio.equals(distribucion.IdPortafolio) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
								resultadoFlujoProyectadoMonedaAdicional.P30 = resultadoFlujoProyectadoMonedaAdicional.P30.add(distribucion.MontoMonedaSistema);
							}
						}
						
						if("DIVIDENDOS".equalsIgnoreCase(tipoFlujo)) {
							resultadoFlujoProyectadoMonedaSistema.P30 = resultadoFlujoProyectadoMonedaSistema.P30.add(distribucionProyeccion30DiasDV.stream()
									.filter(pdv -> dato.IdCliente.equals(pdv.IdCliente) && dato.IdPortafolio.equals(pdv.IdPortafolio))
									.map(pdv -> pdv.MontoMonedaAdicional)
									.reduce(BigDecimal.ZERO, BigDecimal::add));
							
							resultadoFlujoProyectadoMonedaSistema.P30_90 = resultadoFlujoProyectadoMonedaSistema.P30_90.add(distribucionProyeccion90DiasDV.stream()
									.filter(pdv -> dato.IdCliente.equals(pdv.IdCliente) && dato.IdPortafolio.equals(pdv.IdPortafolio))
									.map(pdv -> pdv.MontoMonedaAdicional)
									.reduce(BigDecimal.ZERO, BigDecimal::add));	
						}
						
						//P30_90
						for(DistribucionProyeccion distribucion : distribucionProyeccion90Dias) {
							if(dato.IdCliente.equals(distribucion.IdCliente) && dato.IdPortafolio.equals(distribucion.IdPortafolio) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
								resultadoFlujoProyectadoMonedaAdicional.P30_90 = resultadoFlujoProyectadoMonedaAdicional.P30_90.add(distribucion.MontoMonedaSistema);
							}
						}
						
						salidas.add(resultadoFlujoProyectadoMonedaAdicional);
					}
					
				}
			}
		}
		
		List<HashMap<String,Object>> resultadoFlujoProyectado = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("flujoProyectado", resultadoFlujoProyectado);
		
		log.write("FIN::FlujoProyectado");
		
		return out;
	}
	
	private void calcularParidad(Connection con, EntradaCartola entrada, String fechaSistema, String monedaSistema, List<DistribucionProyeccion> distribucionProyeccion30DiasDP,List<DistribucionProyeccion> distribucionProyeccion90DiasDP) throws Exception {
		logger.trace("INICIO::FlujoProyectado.calcularParidad");
		long inicioMetodo = System.currentTimeMillis();
		
		CalcularParidad paridad = new CalcularParidad();
		CalcularParidad.CalcularParidadEntrada entradaParidad = new CalcularParidad.CalcularParidadEntrada();	
		CalcularParidad.CalcularParidadSalida salidaParidad = new CalcularParidad.CalcularParidadSalida();	

		HashMap<String,DistribucionProyeccion> hashMovimientos = new HashMap<String, DistribucionProyeccion>();
		List<CalculoEntrada> p_CalculosEntrada = new ArrayList <CalculoEntrada>();
		Integer correlativo = 0;			
		
		entradaParidad.p_IdEmpresa= entrada.p_IdEmpresa;
		entradaParidad.p_IdTrabajo=  (long)Math.random()*10000;
		
		for (DistribucionProyeccion mov : distribucionProyeccion30DiasDP) {
			String key = "D30_"+correlativo;			
			hashMovimientos.put(key, mov);
			correlativo++;
		}		

		for (DistribucionProyeccion mov : distribucionProyeccion90DiasDP) {
			String key = "D90_"+correlativo;			
			hashMovimientos.put(key, mov);
			correlativo++;
		}
		
		for(Map.Entry<String, DistribucionProyeccion> entry : hashMovimientos.entrySet()) {
			
			CalculoEntrada paridaCalculo = new CalculoEntrada(); 
			
			String[] keyS = (entry.getKey()).split("_"); 
			Integer IdInterno =  Integer.valueOf( keyS[1]);			
			
			paridaCalculo.IdTrabajo = entradaParidad.p_IdTrabajo;
			paridaCalculo.IdInterno = new Long(IdInterno) ;			
			paridaCalculo.Fecha= fechaSistema;
			paridaCalculo.IdMonedaOrigen= entry.getValue().IdMonedaInstrumento;
			paridaCalculo.IdMonedaDestino=monedaSistema ;			
			paridaCalculo.MontoOrigen= entry.getValue().MontoMonedaSistema;			
			
			p_CalculosEntrada.add(paridaCalculo);
		}
		
		entradaParidad.p_CalculosEntrada.addAll(p_CalculosEntrada);
		salidaParidad = paridad.logica(con, entradaParidad);
		
		for(CalculoSalida movSalida : salidaParidad.p_out_CalculosSalida) {
			
			String key30 = "D30_"+movSalida.IdInterno;
			String key90 = "D90_"+movSalida.IdInterno;
			
			DistribucionProyeccion objetoHash30 = hashMovimientos.get(key30);
			DistribucionProyeccion objetoHash90 = hashMovimientos.get(key90);
			
			if(objetoHash30!= null) {
				objetoHash30.MontoMonedaSistema = movSalida.MontoDestino;
				
			}else if(objetoHash90 != null) {
				objetoHash90.MontoMonedaSistema = movSalida.MontoDestino;				
			}
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::FlujoProyectado.calcularParidad :" , finMetodo- inicioMetodo);		
		
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("Desa02", "connections.properties")) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2020-06-01");
		Objin.p_FechaHasta = Fecha.toDate("2020-06-02");
		Dato dato = new Dato();
		dato.IdCliente = 1423323L;
		dato.IdPortafolio = 1451920L;
		Objin.p_Datos.add(dato);
		FlujoProyectado  demo= new FlujoProyectado();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
