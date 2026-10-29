package cl.bcs.APV.jobs;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.APV.ProcesarDistribucion.DistribucionContrato;
import cl.bcs.APV.jobs.IngresarOrdenFMBonificacionFiscal.ParametrosPrograma;
import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.ordenes.CrearOrdenes.OrdenProceso;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class IngresarOrdenRVBonificacionFiscal extends BcsJob implements JavaCall{
	private final Log log = new Log("APV.IngresarOrdenRVBonificacionFiscal");
	private final String IdTipoPrograma = "INGRESAR_ORDEN_RV_BONIFICACION_FISCAL";
	
	private final String SQL_SELECT_ORDEN_BF = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenRVBonificacionFiscal_SelectOrdenBF.sql");	
	private final String SQL_SELECT_PARAMETROS = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectParametros.sql");
	private final String SQL_CALL_PRECIO_INSTRUMENTO = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenRVBonificacionFiscal_CALLPrecioInstrumento.sql");
	private final String SQL_CREATE_TABLE_PRECIO_INSTRUMENTOS = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenRVBonificacionFiscal_CreateTablePrecioInstrumentos.sql");
	private final String SQL_INSERT_TABLE_PRECIO_INSTRUMENTOS = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenRVBonificacionFiscal_InsertTablePrecioInstrumentos.sql");
	private final String SQL_DROP_TABLE_PRECIO_INSTRUMENTOS = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenRVBonificacionFiscal_DropTablePrecioInstrumentos.sql");
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
	}

	@Override
	public void onError(Throwable t) {
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	public static class ResultadoRV{
		public Long IdInterno;
	}

	public static class AdvertenciasDetalleRV{
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErroresDetalleRV{
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Instrumento{
		public Long IdEmpresa;
		public Long IdInstrumento;
		public String IdBolsa;
		public String Fecha;
		public String IdCondicionDeLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class ResultadoInstrumento{
		public Long IdInstrumento; 				
		public String IdBolsa; 					
		public String FechaConsulta;				
		public Date Fecha;						
		public BigDecimal VolumenTransado; 		
		public BigDecimal MontoTransado; 		
		public BigDecimal PrecioBase;			
		public BigDecimal PrecioCierre;			
		public BigDecimal PuntaCompra;			
		public BigDecimal PuntaVenta;			
		public BigDecimal Variacion;			
		public String IdMoneda;					
		public BigDecimal PrecioApertura;		
		public BigDecimal PrecioMayor;			
		public BigDecimal PrecioMenor;			
		public BigDecimal PrecioMedio;
		public BigDecimal PrecioMercado;		
		public BigDecimal TasaMercado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Orden {
		public Long			IdInterno;
		public Long			IdCliente;
		public String		FechaRecepcion;
		public String		FechaVigencia;
		public String		FechaEmision;
		public String		FechaOperacion;
		public String		IdTipoVigencia;
		public String		Glosa;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	ValorRescate;
		public String		IdBoveda;
		public String		IdFormaCustodia;
		public Long			IdPorcentajeVisibilidad;
		public BigDecimal	PrecioLimite;
		public String		EsFacturaInmediata;
		public String		IdTipoPrecio;
		public Long			IdPortafolio;
		public String		IdTipoLibroOrden;
		public Long			IdLibroOrdenes;
		public String		IdTipoCanalIngreso;
		public String		IdTipoReajuste;
		public String		IdEmisor;
		public String		IdBolsa;
		public String		EspecificaBolsa;
		public String		IdMonedaLiquidacionOrden;
		public String 		IdMonedaOrden; // solo para IF
		public BigDecimal	TipoCambio;
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public String		IdCondicionDeLiquidacionPlazo;
		public String		IdTipoInstrumento;
		
		public BigDecimal	TasaVentaCorta = BigDecimal.ZERO;
		public BigDecimal	TasaSimultanea = BigDecimal.ZERO;
		public BigDecimal	TasaRFIF = BigDecimal.ZERO;
		
		public Long			PlazoIF = 0L;
		public Long			PlazoSimultanea = 0L;
		public Long			PlazoVentaCorta = 0L;
		
		public String		EsVentaCorta = "N";
		public String		EsOD = "N";
		public String		EsFungible = "N";
		public Long			IdCorredorCorresponsal;
		public Long			IdPersonaAutorizadoDarOrdenes;
		public String		IdTipoCanalDistribucion;
		public BigDecimal	TasaReporte = BigDecimal.ZERO;
		public Long			PlazoReporte = 0L;
		public Long			BaseTasa = 0L; 
		public String		TipoInteres; //C o S (Compuesto Simple) 
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoOperacion;
		public Long 		IdCuentaCustodioPortafolio;
		public String		EsPorMonto = "N";
		public String		EsOrdenEspecial = "N";
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public String		IdMecanismoCaptacion = "";
		public BigDecimal	TasaPromedioReporte = BigDecimal.ZERO;
		public BigDecimal	TasaReportanteReferencia = BigDecimal.ZERO;
		public String 		NemotecnicoSVS = "";
		
		public String 		IdSistemaBursatil;
		public String 		InstrumentoAcreditado = "N";
		
		public String		FormadorMercado = "N";
		public String		EsClienteFinal;
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		public BigDecimal 	PorcentajeAllIn;
		
		public String		AccionOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
    	SQL sql=new SQL();
        
        Long IdEmpresa = 0L;
        Long IdPrograma = 0L;
        Long IdOrdenABonificar = 0L;
        Long IdUsuario = 0L;
        
		ArrayList<Long> IdsOrdenABonificar = new ArrayList<Long>();

        if (debug) {
        	IdPrograma = 1540284L;/*
            IdEmpresa = 1L;
            IdOrdenABonificar = 233606L;
            IdUsuario = 0L;*/
        } else{
			IdPrograma = this.getJobId();/*
        	IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            IdOrdenABonificar = Long.parseLong(this.getParam("IdOrden"));
            IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
            IdsOrdenABonificar.add(IdOrdenABonificar);*/
        }
		HashMap<String,Object> ParamsSelectParametrosPrograma = new HashMap<String,Object>();
		ParamsSelectParametrosPrograma.put("IdPrograma", IdPrograma);
		List<ParametrosPrograma> _ParametrosPrograma = sql.query(con, SQL_SELECT_PARAMETROS, ParamsSelectParametrosPrograma, ParametrosPrograma.class);
		for(ParametrosPrograma parametro : _ParametrosPrograma){
			if(parametro.NombreParametro.equals("IdEmpresa")) IdEmpresa = Long.parseLong(parametro.Valor);
			if(parametro.NombreParametro.equals("IdUsuario")) IdUsuario = Long.parseLong(parametro.Valor);
			if(parametro.NombreParametro.equals("IdOrden")) IdsOrdenABonificar.add(Long.parseLong(parametro.Valor));
			
		}
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String v_IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		HashMap<String, ObjFLUJOCAJA> v_out_FlujoCaja = new HashMap<>();
		BigDecimal SaldoFlujoCaja = BigDecimal.ZERO;
		
		Long PorcentajeBonificacionFiscalString = ConfiguracionSistema.getParametroConfiguracionLong(con,IdEmpresa, "APV","APV.PorcentajeBonificacionFiscal");
		HashMap<String,Object> ParamsSelectAporteRescate = new HashMap<String,Object>();
		ParamsSelectAporteRescate.put("IdsOrden", IdsOrdenABonificar);

		List<Orden> _OrdenRv = sql.query(con, SQL_SELECT_ORDEN_BF, ParamsSelectAporteRescate, Orden.class);
		List<CalcularMontoYCobrosOrden.Orden_Entrada> ObjCalculoMontoOrden = new ArrayList<CalcularMontoYCobrosOrden.Orden_Entrada>();

		
		List<CrearOrdenes.ObjOrdenesEntrada> ObjOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
		if (_OrdenRv.size()==0) {
			AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS RV : No cuentan con portafolio BF",true);
			return true;
		}
		//BDH Si es orden de compra, y no es de reinversion, se debe salir
		String tipoOrdenRein = "COMREI";
		try{

			List<Instrumento> ObjInstrumento = new ArrayList<Instrumento>();
			SQL.exec(con, SQL_CREATE_TABLE_PRECIO_INSTRUMENTOS);
			
			for(Orden filaOrden : _OrdenRv){
				CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();
				Instrumento instru = new Instrumento();

				instru.IdEmpresa = IdEmpresa;
				instru.IdInstrumento = filaOrden.IdInstrumento;
				instru.IdBolsa = "BCS";
				instru.Fecha = FechaSistema;
				instru.IdCondicionDeLiquidacion = "T0";
				ObjInstrumento.add(instru);

			}
			Long contadorOrden = 1L;
			sql.batch(con,SQL_INSERT_TABLE_PRECIO_INSTRUMENTOS,ObjInstrumento,Instrumento.class);
							
				HashMap<String,Object> paramsInstrumentoIn = new HashMap<String,Object>();
				HashMap<String,Class> paramsInstrumentoOut = new HashMap<String,Class>();
						
				paramsInstrumentoIn.put("p_Instrumentos",ObjInstrumento);
				paramsInstrumentoOut.put("p_out_Instrumento", ResultadoInstrumento.class);
				paramsInstrumentoOut.put("p_out_AdvertenciasContador", Long.class);
				paramsInstrumentoOut.put("p_out_AdvertenciasDetalle", AdvertenciasDetalleRV.class);
				paramsInstrumentoOut.put("p_out_ErroresContador", Long.class);
				paramsInstrumentoOut.put("p_out_ErroresDetalle", ErroresDetalleRV.class);
				
				HashMap<String,Object> rptInstrumento = sql.call(con, SQL_CALL_PRECIO_INSTRUMENTO, paramsInstrumentoIn, paramsInstrumentoOut);
				
				List<ResultadoInstrumento> resultadoInstrumento = new ArrayList<ResultadoInstrumento>();
				resultadoInstrumento = (List<ResultadoInstrumento>) rptInstrumento.get("p_out_Instrumento");

			
			
			for(Orden filaOrden : _OrdenRv){
				
				//CONSULTA LA CUSTODIA PARA EL CLIENTE, PORTAFOLIO E INSTRUMENTO
				ConsultarCustodiaPortafolio ConsultaCustodia = new ConsultarCustodiaPortafolio();
				
				ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio entradaConsulta= new  ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio();
				entradaConsulta.p_IdEmpresa=IdEmpresa;
				entradaConsulta.p_IdUsuario=IdUsuario;
				entradaConsulta.p_IdModulo="APV";
				
				entradaConsulta.p_IdCliente=filaOrden.IdCliente;
				entradaConsulta.p_IdPortafolio=filaOrden.IdPortafolio;
				entradaConsulta.p_IdInstrumento=filaOrden.IdInstrumento;
				entradaConsulta.p_IdTipoInstrumento="";
				entradaConsulta.p_IdClasificacionTipoInstrumento="RV";
				entradaConsulta.p_IdProductoAgrupador=null;
				entradaConsulta.p_AgrupaPorBoveda="S";
				entradaConsulta.p_AgrupaPorRelacion="S";
				entradaConsulta.p_TipoFechaConsulta="ADQ";
				entradaConsulta.p_FechaConsulta=FechaSistema;
				
				System.out.println("Entrada Consulta :"+entradaConsulta);
				
				BigDecimal cantidadBF = BigDecimal.ZERO;

				//SI ES VENTA	
				if(filaOrden.AccionOrden.equalsIgnoreCase("VENTA")){
					ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salidaConsulta = new ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio();
					ConsultarCustodiaPortafolio.Tabla_Salida res = new ConsultarCustodiaPortafolio.Tabla_Salida();
					
					salidaConsulta = ConsultaCustodia.logica(con, entradaConsulta);
					
					//SI TIENE CUSTODIA SE PUEDE HACER LA ORDEN
					if(salidaConsulta.P_OUT_TBL.size()>0){
						log.write(salidaConsulta.P_OUT_TBL.get(0).CantidadTotal.toString());
						log.write((filaOrden.Cantidad.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100),RoundingMode.UP).toString()));
						BigDecimal cantidadCustodia = salidaConsulta.P_OUT_TBL.get(0).CantidadTotal;
						cantidadBF = filaOrden.Cantidad.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100),RoundingMode.UP);
						cantidadBF = MonedaFormato.formatear(con,cantidadBF,MonedaFormato.CANTIDAD,filaOrden.IdMonedaLiquidacionOrden,filaOrden.IdTipoInstrumento,filaOrden.IdTipoOperacion).valor;
						
						if(cantidadCustodia.compareTo(cantidadBF)<0){
							//NO ALCANZA PARA LA ORDEN, SE VENDERA TODO
							cantidadBF = cantidadCustodia;
						}
					//SI NO TIENE CUSTODIA, SE TERMINA EL PROCESO.
					}else {
						return true;
					}
				}else if(filaOrden.AccionOrden.equalsIgnoreCase("COMPRA")){

					List<Long> idsPortafolio = _OrdenRv.parallelStream().map(p -> p.IdPortafolio).distinct()
							.collect(Collectors.toList());
					
					List<PortafolioAConsultar> portafoliosConsultar = idsPortafolio.parallelStream().map(idPortafolio -> {
						PortafolioAConsultar portafolio = new PortafolioAConsultar();
						portafolio.IdMoneda = filaOrden.IdMonedaLiquidacionOrden;
						portafolio.IdPortafolio = idPortafolio;
						return portafolio;
					}).collect(Collectors.toList());

					ConsultarFlujoCaja llamadoFlujoCaja = new ConsultarFlujoCaja();
					EntradaConsultarFlujoCaja entradaFlujoCaja = new EntradaConsultarFlujoCaja();
					entradaFlujoCaja.p_IdEmpresa = IdEmpresa;
					entradaFlujoCaja.p_IdUsuario = IdUsuario;
					entradaFlujoCaja.p_IdModulo = "APV";
					entradaFlujoCaja.p_FechaDesde = FechaSistema;
					entradaFlujoCaja.p_FechaHasta = FechaSistema;
					entradaFlujoCaja.p_Uso = "ORDENES_DE_COMPRA";
					entradaFlujoCaja.p_PortafolioAConsultar = portafoliosConsultar;
					// logger.debug("entradaFlujoCaja : " + entradaFlujoCaja);
					SalidaConsultarFlujoCaja salidaFlujoCaja = llamadoFlujoCaja.logica(con, entradaFlujoCaja);
					List<ObjFLUJOCAJA> flujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
					flujoCaja = flujoCaja.stream()
							.filter(s -> s.FechaSaldo.equals(FechaSistema) && filaOrden.IdMonedaLiquidacionOrden.equals(s.IdMoneda))
							.collect(Collectors.toList());

					for (ObjFLUJOCAJA flujo : flujoCaja) {
						String keyFlujoCaja = flujo.IdPortafolio + "";
						if (!v_out_FlujoCaja.containsKey(keyFlujoCaja)) {
							v_out_FlujoCaja.put(keyFlujoCaja, flujo);
						}
					}
					
					String keyFlujoCaja = filaOrden.IdPortafolio + "";
					
					ObjFLUJOCAJA objFlujoCaja = v_out_FlujoCaja.get(keyFlujoCaja);
					if (objFlujoCaja != null) {
						SaldoFlujoCaja = objFlujoCaja.Saldo;
					}
				}
						
				{

					CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();

					CalcularMontoYCobrosOrden.Orden_Entrada CalculoMontoOrdenes = new CalcularMontoYCobrosOrden.Orden_Entrada();
					for(ResultadoInstrumento filaResultadoInstrumento: resultadoInstrumento){
						if(filaResultadoInstrumento.IdInstrumento.equals(filaOrden.IdInstrumento))
						CalculoMontoOrdenes.PrecioOrden = filaResultadoInstrumento.PrecioCierre;
					}

					CalculoMontoOrdenes.IdInterno = contadorOrden;
					CalculoMontoOrdenes.CantidadOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.EsFungible="N";
					CalculoMontoOrdenes.IdBolsa="BCS";
					CalculoMontoOrdenes.IdCorredorCorresponsal=null;
					CalculoMontoOrdenes.IdInstrumento=filaOrden.IdInstrumento;
					CalculoMontoOrdenes.IdInstrumentoSubyacente=0L;
					CalculoMontoOrdenes.IdMonedaLiquidacionOrden=filaOrden.IdMonedaLiquidacionOrden;
					CalculoMontoOrdenes.IdPortafolio=filaOrden.IdPortafolio; //portafolio BF
					CalculoMontoOrdenes.IdTipoProducto="INAC";
					CalculoMontoOrdenes.IdTipoOperacion=filaOrden.IdTipoOperacion;
					CalculoMontoOrdenes.IdTipoCanalIngreso="AGE";
					CalculoMontoOrdenes.FechaOperacion=FechaSistema;
					CalculoMontoOrdenes.MontoOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaSimultanea=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaRFIF=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaVentaCorta=BigDecimal.ZERO;
					CalculoMontoOrdenes.TipoCambio=BigDecimal.ZERO;
					CalculoMontoOrdenes.ValorRescateOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.PlazoSimultanea=0L;
					CalculoMontoOrdenes.PlazoVentaCorta=0L;
					CalculoMontoOrdenes.PlazoIF=0L;
					CalculoMontoOrdenes.EsAPlazo="N";
					if("COMPRA".equals(filaOrden.AccionOrden)) {
						CalculoMontoOrdenes.MontoTotal=(filaOrden.MontoNeto.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100),RoundingMode.UP));
						CalculoMontoOrdenes.EsPorMonto="S";
						CalculoMontoOrdenes.CantidadOrden = BigDecimal.ZERO;
						
						if(SaldoFlujoCaja.compareTo(CalculoMontoOrdenes.MontoTotal) < 0) {
							if(SaldoFlujoCaja.compareTo(CalculoMontoOrdenes.PrecioOrden) < 0) {
								AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS RV : Saldo Caja Insuficiente para realizar la Orden BF.",true);
								//AGREGAR LOG
								{
									AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
									
									AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
									log.IdCabeceraLog = IdPrograma;
									log.IdTipoLog = "INGRESO";
									log.IdModulo = "APV";
									log.IdUsuario = IdUsuario;
									log.IdEmpresa = IdEmpresa;
									log.FechaSistemaLog = FechaSistema;
									log.IdUsuarioAtendido = IdUsuario;
									log.IdCliente = filaOrden.IdCliente;
									log.EstadoCliente = "";
									log.NombreCliente = "";
									log.IdTipoDocumentoIdentidad = "";
									log.IdDocumentoIdentidad = "";
									log.IdPortafolio = filaOrden.IdPortafolio;
									log.EstadoPortafolio = "";
									log.IdTipoRegistro = "ORDEN";
									log.EstadoRegistro = "";
									log.IdTipoOperacion = filaOrden.IdTipoOperacion;
									log.Folio = 0L;
									log.Cantidad = filaOrden.Cantidad;
									log.PrecioTasa = BigDecimal.ZERO;
									log.Monto = CalculoMontoOrdenes.MontoTotal;
									log.IdMonedaOrigen = filaOrden.IdMonedaOrden;
									log.IdMonedaLiquidacion = filaOrden.IdMonedaLiquidacionOrden;
									log.TipoCambio = filaOrden.TipoCambio.doubleValue()+"";
									log.Nemo = "";
									log.IdProceso = 0L;
									log.EstadoProceso = "";
									log.NombreProceso = "";						
									entradaAgregarLog.p_Log.add(log);
									
									InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
									detLog.IdCabeceraLog = IdPrograma;
									detLog.IdTrabajo=0L;
									detLog.SubRegistro = "ADVERTENCIA ";
									detLog.CampoRegistro = "SALDO CAJA";
									detLog.ValorAntiguo = "";
									detLog.ValorNuevo = "ADVERTENCIAS RV : Saldo Caja Insuficiente para realizar la Orden BF.";
									entradaAgregarLog.p_LogDetalle.add(detLog);
									
									AgregarLog agregarLogService = new AgregarLog();
									agregarLogService.logica(con, entradaAgregarLog);
									
								}
								return true;
							}else {
								CalculoMontoOrdenes.MontoTotal = SaldoFlujoCaja;
							}
						}
						
					}else if("VENTA".equals(filaOrden.AccionOrden)) {
						CalculoMontoOrdenes.MontoTotal=BigDecimal.ZERO;
						CalculoMontoOrdenes.EsPorMonto="N";
						CalculoMontoOrdenes.CantidadOrden=cantidadBF;
					}
					
					CalculoMontoOrdenes.TasaReporte=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaReportado=BigDecimal.ZERO;
					CalculoMontoOrdenes.PlazoReporte=0L;
					CalculoMontoOrdenes.BaseTasa=0L;
					 
					log.write(filaOrden);
					ObjCalculoMontoOrden.add(CalculoMontoOrdenes);
					Orden.IdInterno = contadorOrden;
					Orden.FechaRecepcion = "0001-01-01 00:00:00";
					Orden.FechaVigencia = filaOrden.FechaVigencia;
					Orden.FechaEmision = "0001-01-01";
					Orden.FechaOperacion = "0001-01-01";
					Orden.IdCondicionDeLiquidacion = filaOrden.IdCondicionDeLiquidacion; 
					Orden.IdTipoOperacion = filaOrden.IdTipoOperacion;
					Orden.IdTipoVigencia = filaOrden.IdTipoVigencia;
					Orden.Glosa = filaOrden.Glosa;
					if("COMPRA".equals(filaOrden.AccionOrden)) {
						Orden.MontoNeto= CalculoMontoOrdenes.MontoTotal;
						Orden.EsPorMonto="S";
						Orden.Cantidad = BigDecimal.ZERO;
					}else if("VENTA".equals(filaOrden.AccionOrden)) {
						Orden.EsPorMonto = "N";
						Orden.MontoNeto=BigDecimal.ZERO;
						Orden.Cantidad=cantidadBF;
					}
					Orden.ValorRescate = filaOrden.ValorRescate;
					Orden.IdBoveda = filaOrden.IdBoveda;
					Orden.IdFormaCustodia = filaOrden.IdFormaCustodia;
					Orden.IdPorcentajeVisibilidad = filaOrden.IdPorcentajeVisibilidad;
					Orden.PrecioLimite = filaOrden.PrecioLimite;
					Orden.EsFacturaInmediata = filaOrden.EsFacturaInmediata;
					Orden.IdTipoPrecio = filaOrden.IdTipoPrecio;
					Orden.IdPortafolio = filaOrden.IdPortafolio;
					Orden.IdTipoCanalIngreso = filaOrden.IdTipoCanalIngreso;
					Orden.IdTipoReajuste = filaOrden.IdTipoReajuste;
					Orden.IdEmisor = filaOrden.IdEmisor;
					Orden.IdBolsa = filaOrden.IdBolsa;
					Orden.EspecificaBolsa = filaOrden.EspecificaBolsa;
					Orden.IdMonedaLiquidacionOrden = filaOrden.IdMonedaLiquidacionOrden;
					Orden.TipoCambio = filaOrden.TipoCambio;
					Orden.IdInstrumento = filaOrden.IdInstrumento;
					Orden.IdInstrumentoSubyacente = filaOrden.IdInstrumentoSubyacente;
					Orden.IdCondicionDeLiquidacionPlazo = filaOrden.IdCondicionDeLiquidacionPlazo;
					Orden.TasaVentaCorta = filaOrden.TasaVentaCorta;
					Orden.PlazoVentaCorta = filaOrden.PlazoVentaCorta;
					Orden.EsVentaCorta = filaOrden.EsVentaCorta;
					Orden.TasaSimultanea = filaOrden.TasaSimultanea;
					Orden.PlazoSimultanea = filaOrden.PlazoSimultanea;
					Orden.PlazoIF = filaOrden.PlazoIF;
					Orden.TasaRFIF = filaOrden.TasaRFIF;
					Orden.EsOD = filaOrden.EsOD;
					Orden.EsFungible = filaOrden.EsFungible;
					Orden.IdTipoLibroOrden = "NORMAL";
					Orden.IdLibroOrdenes = null;
					Orden.IdCuentaCustodioPortafolio = filaOrden.IdCuentaCustodioPortafolio;
					Orden.IdCorredorCorresponsal = filaOrden.IdCorredorCorresponsal;
					Orden.IdPersonaAutorizadoDarOrdenes = filaOrden.IdPersonaAutorizadoDarOrdenes;
					Orden.IdTipoCanalDistribucion = filaOrden.IdTipoCanalDistribucion;
					Orden.EsOrdenEspecial = filaOrden.EsOrdenEspecial;
					Orden.PagoCobroCuentaInversion = "S"; //filaOrden.PagoCobroCuentaInversion;
					Orden.IdPreferenciaPagoPortafolio = filaOrden.IdPortafolio;
					Orden.BaseTasa = filaOrden.BaseTasa;
					Orden.TipoInteres = filaOrden.TipoInteres;
					Orden.IdMecanismoCaptacion = filaOrden.IdMecanismoCaptacion;
					
					ObjOrden.add(Orden);

					log.write(ObjOrden);
					contadorOrden++;

				}

			}
			/*ORDENES*/
			if (ObjOrden.size()>0){
				
				//CalculoMontoyCobroOrden
					CalcularMontoYCobrosOrden procesoCalcularMonto = new CalcularMontoYCobrosOrden();
					CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada paramsCalcularMonto_Entrada = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
					paramsCalcularMonto_Entrada.p_IdEmpresa = IdEmpresa;
					paramsCalcularMonto_Entrada.p_IdUsuario = IdUsuario;
					paramsCalcularMonto_Entrada.p_IdModulo = "APV";    
					paramsCalcularMonto_Entrada.p_Ordenes = ObjCalculoMontoOrden;
	
					CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida OutCalcularmonto = procesoCalcularMonto.logica(con, paramsCalcularMonto_Entrada); 

					for(int i = OutCalcularmonto.p_out_Ordenes.size()-1; i >= 0; i --){
						for(int j = ObjOrden.size()-1; j >= 0; j --){
							if(OutCalcularmonto.p_out_Ordenes.get(i).IdInterno.toString().equals(ObjOrden.get(j).IdInterno.toString())){
								ObjOrden.get(j).Cantidad = OutCalcularmonto.p_out_Ordenes.get(i).CantidadOperacion;
							}
							
						}
					}
					
				CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada = new CrearOrdenes.CrearOrdenesEntrada();
			    
				paramsOrdenesEntrada.p_IdEmpresa = IdEmpresa;
				paramsOrdenesEntrada.p_IdUsuario = IdUsuario;
				paramsOrdenesEntrada.p_IdModulo = "APV";
				paramsOrdenesEntrada.p_simularOperacion = "N";
				paramsOrdenesEntrada.p_UsaReglasNegocio = "S";
				paramsOrdenesEntrada.p_Ordenes = ObjOrden;
			    
				CrearOrdenes procesoOrdenes =new CrearOrdenes();
				CrearOrdenes.CrearOrdenesSalida OutOrdenes = procesoOrdenes.logica(con, paramsOrdenesEntrada);

				for(MensajeSalida filaResultadoRV : OutOrdenes.P_OUT_MENSAJES){
					AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS RV : "+filaResultadoRV.descripcion,true);
				}
 				log.write(OutOrdenes.toString());
			
		}
		}catch(Exception er) {
			log.write("ERROR", er);
			throw er;
		}
		finally 
		{
			try 
			{
				SQL.exec(con, SQL_DROP_TABLE_PRECIO_INSTRUMENTOS);		
			} 
			catch (Exception er) 
			{
				log.write("ERROR", er);	
			}

		}
		
		log.write("FIN JOB");	
		return true;
	}
	
	//DEBUG
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
            // ---------------------------------
            try {
            	IngresarOrdenRVBonificacionFiscal demo=new IngresarOrdenRVBonificacionFiscal();
            	demo.logica(con,true);
                //con.commit();
            	con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("== END ==");       
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
