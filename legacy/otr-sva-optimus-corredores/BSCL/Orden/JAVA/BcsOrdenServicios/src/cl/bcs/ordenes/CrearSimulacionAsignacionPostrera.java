package cl.bcs.ordenes;

import java.math.MathContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Simulacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.utils.Fecha;

import org.apache.commons.collections.map.MultiKeyMap;

public class CrearSimulacionAsignacionPostrera implements JavaCall{
	private final String SQL_SELECT_INFO_TRANSACCION = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacionPostrera_SelectInfoTransaccion.sql");
	private final String SQL_SELECT_INFO_PORTFOLIO   = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacionPostrera_SelectInfoPortafolio.sql");

	private Logger logger = LogManager.getLogger(CrearSimulacionAsignacionPostrera.class);
	

	// -----------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -----------------------------------------------------------------------
	public static class EntradaCrearSimulacionAsignacionPostrera{		
		public Long 						p_IdEmpresa;
		public String 						p_IdTipo;
		public String 						p_Origen;
		public List<Transaccion_Entrada> 	p_Transacciones;
		public List<Portafolio_Entrada> 	p_Portafolios;

		
		public EntradaCrearSimulacionAsignacionPostrera() {			
			this.p_IdTipo 			= "ASIG";  //valor por defecto
			this.p_Transacciones 	= new ArrayList<>();
			this.p_Portafolios 		= new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	// -----------------------------------------------------------------------
	// CLASES DE SALIDA
	// -----------------------------------------------------------------------
	
	public static class SalidaCrearSimulacionAsignacionPostrera{
		public List<Simulacion_Salida> p_out_SimAsignacion;
		
		public SalidaCrearSimulacionAsignacionPostrera(){
			this.p_out_SimAsignacion = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	


	
	// ---------------------------------------------------------------------------------------
	// CLASES DE PROCESO
	// ---------------------------------------------------------------------------------------
	public static class InfoTransacciones{
		public Long 			IdMovimiento;
		public Long 			IdTransaccion;
		public String 			FolioTransaccion;
		public Timestamp 		FechaOperacionTransaccion;
		public java.sql.Date 	FechaTransaccion;
		public String 			EsFungible;
		public BigDecimal 		Tasa;
		public Long 			IdInstrumento;
		public BigDecimal 		Precio;
		public String 			Instrumento;		
		public String 			IdTipoInstrumento;
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdMonedaInstrumento;
		public String 			IdTipoOperacion;
		public String			IdTipoProducto;
		public String 			IdCondicionDeLiquidacion;
		public String 			IdBolsa;
		public Long 			IdLibroOrdenes;
		public BigDecimal 		CantidadTotalTransaccion;
		public BigDecimal 		CantidadDisponibleTransaccion;
		public BigDecimal 		CantidadAsignadaTransaccion;
		public BigDecimal 		ValorRescateDisponibleTransaccion;
		public BigDecimal 		ValorRescateAsignadoTransaccion;
		public BigDecimal 		MontoDisponibleTransaccion;
		public BigDecimal 		MontoAsignadoTransaccion;				
		public BigDecimal 		CorteMinimo;		
		public String 			EsExtranjera;		
		public java.sql.Timestamp	FechaIngresoRegistro;
		public String 			IdMonedaLiquidacionOrden;
		public Long 			Tamano;
		public BigDecimal 		MontoTransaccion;
		public BigDecimal 		ValorRescateTransaccion;
		public BigDecimal 		PrecioStrike;		
		public String 			EstiloOpcion;
		public String 			ModalidadEjercicio;
		public String 			ModalidadLiquidacion;		
		public String 			TipoOpcion;
		public String 			InstrumentoNegociacionTran;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class InfoPortafolios{
		
		public Long 		IdPortafolio;		
		public BigDecimal 	Cantidad;
		public BigDecimal 	Porcentaje;
		public String 		CodigoPortafolio;
		public Long 		IdCliente;
		public String 		NombreCompleto;
		public String 		DocumentoIdentidad;
			
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InfoPortafoliosProrrateo{
			
			public Long 		IdPortafolio;		
			public BigDecimal 	CantidadDeficit;
			public Long 		IdInstrumento;
			public String 		IdTipoOperacion;
				
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
	
	public static class PrecioPromedio{
		
		public Long 			IdPortafolio;
		public Long 			IdInstrumento;
		public BigDecimal 		Cantidad;
		public BigDecimal 		Monto;
		public BigDecimal 		ppp;
			
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// ---------------------------------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------------------------------
	@Trace
	public SalidaCrearSimulacionAsignacionPostrera logica(Connection conn, EntradaCrearSimulacionAsignacionPostrera in) {
		logger.info("INICIO");
		SQL sql = new SQL();
		SalidaCrearSimulacionAsignacionPostrera salida = new SalidaCrearSimulacionAsignacionPostrera();
		List<Simulacion_Salida> simulacionAsignacionProceso = new ArrayList<Simulacion_Salida>();
		BigDecimal porcentajeTotalPortafolio = BigDecimal.ZERO;
		
		try {

			HashMap<String, Object> params = new HashMap<String, Object>();
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
			
			logger.debug("Obtener transacciones");
			params.clear();
			params.put("p_IdTipo", in.p_IdTipo);
			List<InfoTransacciones> listaTransacciones = sql.query(conn, SQL_SELECT_INFO_TRANSACCION, params, InfoTransacciones.class);
			
			
			logger.debug("Obtener portolios");
			params.clear();
			List<InfoPortafolios> listaPortafolios = sql.query(conn, SQL_SELECT_INFO_PORTFOLIO, params, InfoPortafolios.class);
			
			
			// --------------------------------------------------------------------------------------------------------------------------------
			// ASIGNACION POSTRERA PRODUCTO CRUZ			
			logger.debug("Realizar asignacion de transacciones postrera, producto cruz");

			for (InfoPortafolios por : listaPortafolios) {
				porcentajeTotalPortafolio = porcentajeTotalPortafolio.add(por.Porcentaje); //calcula porcentaje total
				for (InfoTransacciones tra : listaTransacciones) {
							Simulacion_Salida asig = new Simulacion_Salida();
							asig.IdMovimiento 			= tra.IdMovimiento;
							asig.IdPortafolio 			= por.IdPortafolio;
							asig.CodigoPortafolio 		= por.CodigoPortafolio;
							asig.IdCliente 				= por.IdCliente;
							asig.NombreCompleto 		= por.NombreCompleto;
							asig.DocumentoIdentidad 	= por.DocumentoIdentidad;
							asig.PorcentajeAsignacion 	= por.Porcentaje;							
							asig.IdInstrumento 			= tra.IdInstrumento;
							asig.Instrumento 			= tra.Instrumento;
							asig.IdTipoInstrumento 		= tra.IdTipoInstrumento;
							asig.IdLibroOrdenes 		= tra.IdLibroOrdenes;
							asig.IdTransaccion 			= tra.IdTransaccion;
							asig.FolioTransaccion 		= tra.FolioTransaccion;
							asig.FechaTransaccion 		= tra.FechaTransaccion;
							asig.IdTipoOperacion 		= tra.IdTipoOperacion;
							asig.IdMonedaInstrumento 	= tra.IdMonedaInstrumento;
							asig.PrecioSimulacion 		= tra.Precio;

							asig.CantidadAsignacion 	= (tra.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF"))							
									? (tra.CantidadDisponibleTransaccion.multiply(por.Porcentaje.divide(new BigDecimal(100))).divide(tra.CorteMinimo)).multiply(tra.CorteMinimo)
									: (tra.CantidadDisponibleTransaccion.multiply(por.Porcentaje.divide(new BigDecimal(100))));
							
							//Solo debe tomar la parte entera(despues se hace el prorrateo de lo sobrante)
							asig.CantidadAsignacion 			= asig.CantidadAsignacion.setScale(0, BigDecimal.ROUND_DOWN);
	 
							//asig.ValorRescateAsignacion 		= tra.EsFungible.equals("S") ? (tra.ValorRescateDisponibleTransaccion.multiply(por.Porcentaje.divide(new BigDecimal(100)))) : tra.ValorRescateDisponibleTransaccion;
							asig.ValorRescateAsignacion 		= tra.ValorRescateDisponibleTransaccion.multiply(por.Porcentaje.divide(new BigDecimal(100)));

							asig.IdTipoProducto 				= tra.IdTipoProducto;
							asig.IdTipoCanalIngreso 			= "TRA";
							asig.IdBolsa 						= tra.IdBolsa;
							
							
							asig.CantidadTransaccion 			= tra.CantidadDisponibleTransaccion; // solo lo disponible ya que CantidadTotalTransaccion contiene lo asignado si es que lo ubiese
							
							
							
							asig.EsFungible  					= tra.EsFungible;
							asig.EsExtranjera 					= tra.EsExtranjera;
							asig.FechaSimulacion 				= Fecha.toTimeStamp(fechaHoraSistema);
							asig.FechaIngresoRegistro 			= tra.FechaIngresoRegistro;		
							asig.IdClasificacionTipoInstrumento = tra.IdClasificacionTipoInstrumento;
							asig.IdMonedaLiquidacionOrden 		= tra.IdMonedaLiquidacionOrden;						
							asig.Tamano  						= tra.Tamano;						
							asig.CorteMinimo  					= tra.CorteMinimo;
							
							asig.MontoAsignacion 				= (tra.MontoTransaccion.multiply(asig.CantidadAsignacion)).divide(tra.CantidadDisponibleTransaccion, MathContext.DECIMAL128); // Regla de 3: ((Monto Palo x Cantidad Asignada)/Cantidad Palo)
							
							asig.MontoTransaccion 				= tra.MontoTransaccion;
							asig.CantidadAsignadaSimulacion 	= (!tra.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? asig.CantidadAsignacion : asig.ValorRescateAsignacion;
							
							asig.ValorRescateMontoTransaccion 	= (!tra.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? tra.CantidadDisponibleTransaccion : tra.ValorRescateTransaccion;
							asig.CantidadTransaccionSimulacion 	= (!tra.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? tra.CantidadDisponibleTransaccion : asig.ValorRescateAsignacion;
							
							
							
							asig.PrecioStrike 					= tra.PrecioStrike;
							asig.EstiloOpcion 					= tra.EstiloOpcion;
							asig.FolioCartera 					= 0L;				
							asig.IdOrden 						= 0L;
							asig.ModalidadEjercicio             = tra.ModalidadEjercicio;
							asig.ModalidadLiquidacion			= tra.ModalidadLiquidacion;
							asig.TipoOpcion 					= tra.TipoOpcion;
							asig.InstrumentoNegociacionTran 	= tra.InstrumentoNegociacionTran;
							simulacionAsignacionProceso.add(asig);
				}
			}

			
			
			logger.debug("Asignar ids asignacion");

			LinkedList<Long> idsAsignacion = new LinkedList<Long>(Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion", (long) simulacionAsignacionProceso.size()));
			simulacionAsignacionProceso = simulacionAsignacionProceso.stream().map(asignacion -> {
				asignacion.IdInterno = idsAsignacion.pop();
				return asignacion;
			}).collect(Collectors.toList());
			
	

			
			// --------------------------------------------------------------------------------------------------------------------------------
			// PRORRATEO DE PUCHOS SOBRANTES (al ser con porcentaje quedan puchos que hay que prorratear)(solo si la asignacion es al 100 por ciento)
			if (porcentajeTotalPortafolio.compareTo(new BigDecimal(99.4)) == 1 && porcentajeTotalPortafolio.compareTo(new BigDecimal(100.9)) == -1)
			{
				//Calcula totales asignados y por asignar por transaccion
				MultiKeyMap mapResumenAsignacion = new MultiKeyMap();
				MultiKeyMap mapResumenPortafolio = new MultiKeyMap();
				for (Simulacion_Salida asig : simulacionAsignacionProceso) {
					InfoTransacciones objResumenAsignacion = (InfoTransacciones) mapResumenAsignacion.get(asig.FolioTransaccion, asig.IdInstrumento, asig.IdTipoOperacion);
					
					if (objResumenAsignacion == null)
					{
						InfoTransacciones transaccion = new InfoTransacciones();
						transaccion.FolioTransaccion = asig.FolioTransaccion;
						transaccion.CantidadTotalTransaccion = asig.CantidadTransaccion;
						transaccion.CantidadAsignadaTransaccion = asig.CantidadAsignacion;
						transaccion.CantidadDisponibleTransaccion = transaccion.CantidadTotalTransaccion.subtract(transaccion.CantidadAsignadaTransaccion);
						mapResumenAsignacion.put(asig.FolioTransaccion, asig.IdInstrumento, asig.IdTipoOperacion, transaccion);
					}
					else
					{					
						objResumenAsignacion.CantidadAsignadaTransaccion = objResumenAsignacion.CantidadAsignadaTransaccion.add(asig.CantidadAsignacion);
						objResumenAsignacion.CantidadDisponibleTransaccion = objResumenAsignacion.CantidadTotalTransaccion.subtract(objResumenAsignacion.CantidadAsignadaTransaccion);	
					}
					//Calcula deficit por asignar a portafolios.

					InfoPortafoliosProrrateo objResumenPortafolio = (InfoPortafoliosProrrateo) mapResumenPortafolio.get(asig.IdPortafolio, asig.IdInstrumento, asig.IdTipoOperacion);

					if (objResumenPortafolio == null)
					{
						InfoPortafoliosProrrateo portafolioProrrateo = new InfoPortafoliosProrrateo();
						portafolioProrrateo.IdPortafolio = asig.IdPortafolio;
						portafolioProrrateo.IdInstrumento = asig.IdInstrumento;
						portafolioProrrateo.IdTipoOperacion = asig.IdTipoOperacion;
						portafolioProrrateo.CantidadDeficit = asig.CantidadTransaccion
							    .multiply(asig.PorcentajeAsignacion)
							    .divide(new BigDecimal("100"))
							    .setScale(4, RoundingMode.HALF_UP)
							    .remainder(BigDecimal.ONE);
						mapResumenPortafolio.put(asig.IdPortafolio, asig.IdInstrumento, asig.IdTipoOperacion, portafolioProrrateo);
					}
					else
					{					
						objResumenPortafolio.CantidadDeficit = objResumenPortafolio.CantidadDeficit.add(asig.CantidadTransaccion
							    .multiply(asig.PorcentajeAsignacion)
							    .divide(new BigDecimal("100"))
							    .setScale(4, RoundingMode.HALF_UP)
							    .remainder(BigDecimal.ONE));
					}

				}
				for (Simulacion_Salida asig : simulacionAsignacionProceso) {
					InfoPortafoliosProrrateo objResumenPortafolio = (InfoPortafoliosProrrateo) mapResumenPortafolio.get(asig.IdPortafolio, asig.IdInstrumento, asig.IdTipoOperacion);
					objResumenPortafolio.CantidadDeficit = objResumenPortafolio.CantidadDeficit.setScale(1, RoundingMode.HALF_UP);
				}

				
				System.out.println("PARA REVISAR RESUMEN DE TOTALES");			
				mapResumenAsignacion.forEach((key, value) -> {
					System.out.println(key +" > " + value);				
				});	
					

				Double desviacionEstandar = 0.0;
				//Simulacion de prorrateo de 1 en 1 para calcular ppp general y desviacion estandar
				for (Simulacion_Salida asig : simulacionAsignacionProceso)
				{
					InfoTransacciones objResumenAsignacion = (InfoTransacciones) mapResumenAsignacion.get(asig.FolioTransaccion, asig.IdInstrumento, asig.IdTipoOperacion);

					if (objResumenAsignacion.CantidadDisponibleTransaccion.compareTo(BigDecimal.ZERO) > 0)
					{	//Si hay cantidades disponibles a prorratear se hace simulacion de 1 en 1.
						asig.CantidadAsignacion = asig.CantidadAsignacion.add(BigDecimal.ONE);	  //simula prorrateo aumentando en 1 la cantidad asignada	
						desviacionEstandar = obtenerDesviacionEstandarPrecioPromedio(simulacionAsignacionProceso); // //calcula el ppp general y desviacion estandar para la simulacion anterior
						asig.DesviacionEstandar = (desviacionEstandar != null) ? desviacionEstandar : 0.0; 
						asig.CantidadAsignacion = asig.CantidadAsignacion.subtract(BigDecimal.ONE); // devuelve cantidad asignacion original para hacer otra simulacion				
					}
				};
				
				
				System.out.println("PARA REVISAR CALCULO DE DESVIACION ESTANDAR");
				for (Simulacion_Salida asig : simulacionAsignacionProceso)
				{		
					System.out.println(asig.FolioTransaccion +" - "+ asig.DesviacionEstandar);
				};			
				
				
				

				//Ordenamos la asignacion en forma ascendente por Folio transaccion y desviacion estandar
				Collections.sort(simulacionAsignacionProceso, (asig1, asig2) -> compararAsignacion(asig1, asig2));
				
				/*
				System.out.println("PARA REVISAR NUEVO ORDEN DE ASIGNACION. ASCENDENTE POR FOLIO Y DESVIACION");
				for (Simulacion_Salida asig1 : simulacionAsignacionProceso)
				{		
					System.out.println(asig1.FolioTransaccion +" - "+ asig1.DesviacionEstandar);
				};			
				*/
							
				

				//Prorrateamos de 1 en 1 segun la desviacion estandar menor (deben estar ordenadas de menor a mayor por desviacion)
				boolean prorratear = true;
				while(prorratear) {
					prorratear = false;
					for (Simulacion_Salida asig : simulacionAsignacionProceso)
					{
						InfoTransacciones objResumenAsignacion = (InfoTransacciones) mapResumenAsignacion.get(asig.FolioTransaccion, asig.IdInstrumento, asig.IdTipoOperacion);
						InfoPortafoliosProrrateo objResumenPortafolio = (InfoPortafoliosProrrateo) mapResumenPortafolio.get(asig.IdPortafolio, asig.IdInstrumento, asig.IdTipoOperacion);
						
						if (objResumenAsignacion.CantidadDisponibleTransaccion.compareTo(BigDecimal.ZERO) > 0 && objResumenPortafolio.CantidadDeficit.compareTo(BigDecimal.ZERO) > 0) {
				            asig.CantidadAsignacion = (!asig.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? asig.CantidadAsignacion.add(BigDecimal.ONE) :asig.CantidadAsignacion.add(objResumenPortafolio.CantidadDeficit);
				            asig.MontoAsignacion = (asig.MontoTransaccion.multiply(asig.CantidadAsignacion)).divide(asig.CantidadTransaccion, MathContext.DECIMAL128);
				            asig.CantidadAsignadaSimulacion = (!asig.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? asig.CantidadAsignacion : asig.ValorRescateAsignacion;

				            objResumenAsignacion.CantidadDisponibleTransaccion = objResumenAsignacion.CantidadDisponibleTransaccion.subtract((!asig.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) ? BigDecimal.ONE : asig.CantidadAsignacion);
				            objResumenPortafolio.CantidadDeficit = objResumenPortafolio.CantidadDeficit.subtract(BigDecimal.ONE);

				            prorratear = true;
				        }
						
					};
				}
				
				/*
				System.out.println("PARA REVISAR DESCUENTO DE CANTIDAD DISPONIBLE EN RESUMEN");
				mapResumenAsignacion.forEach((key, value) -> {
				    System.out.println("KeyTra2 : " + key + " ValueTra2 : " + value);
				});			
				*/
			
			}
			simulacionAsignacionProceso.removeIf(asig -> asig.getCantidadAsignacion().compareTo(BigDecimal.ZERO) == 0);


			salida.p_out_SimAsignacion = simulacionAsignacionProceso;
			System.out.println(salida.p_out_SimAsignacion);

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("TERMINO");
		return salida;
	}


	// -------------------------------------------------------------------------------------------------
	// METODOS DE PROCESO
	// -------------------------------------------------------------------------------------------------

	public int compararAsignacion(Simulacion_Salida asig1,Simulacion_Salida asig2) {

	    int compararPorFolio = asig1.FolioTransaccion.compareTo(asig2.FolioTransaccion);

	    if (compararPorFolio != 0)
	        return compararPorFolio;

	    return (asig1.DesviacionEstandar.compareTo(asig2.DesviacionEstandar));
	}
	
	
	/**
	 * Agrupa las transacciones por Portafolio e Instrumento para obtener PPP por grupo y luego la desviacion estandar
	 * @param simAsignacion
	 * @return desviacionEstandar de los ppp calculados
	 */
	private Double obtenerDesviacionEstandarPrecioPromedio(List<Simulacion_Salida> simAsignacion) throws Exception {
		Double desviacionEstandar = 0.0;
		MultiKeyMap mapPPP = new MultiKeyMap();
		
		// Agrupa las transacciones por Portafolio e Instrumento para obtener el PPP por grupo
		simAsignacion.forEach(asig -> {		
				PrecioPromedio ppp = (PrecioPromedio) mapPPP.get(asig.IdPortafolio, asig.IdInstrumento);
				
				if (ppp == null)
				{
					PrecioPromedio objPPP = new PrecioPromedio();			
					objPPP.IdPortafolio 	= asig.IdPortafolio;
					objPPP.IdInstrumento 	= asig.IdInstrumento;					
					objPPP.Cantidad         = asig.CantidadAsignacion;										
					objPPP.Monto			= objPPP.Cantidad.multiply(asig.PrecioSimulacion);
					if (objPPP.Cantidad.compareTo(BigDecimal.ZERO) > 0) {
					    objPPP.ppp = objPPP.Monto.divide(objPPP.Cantidad, 8, RoundingMode.HALF_UP);
					} else {
					    objPPP.ppp = BigDecimal.ZERO; 
					}					
					mapPPP.put(asig.IdPortafolio, asig.IdInstrumento, objPPP);
				}
				else
				{
					ppp.Cantidad 	= ppp.Cantidad.add(asig.CantidadAsignacion);
					ppp.Monto	 	= ppp.Monto.add(asig.CantidadAsignacion.multiply(asig.PrecioSimulacion));
					if (ppp.Cantidad.compareTo(BigDecimal.ZERO) > 0) {
					    ppp.ppp = ppp.Monto.divide(ppp.Cantidad, 8, RoundingMode.HALF_UP);
					} else {
					    ppp.ppp = BigDecimal.ZERO;
					}
				}
		});
		

		//Obtiene la desviacion estandar de los PPP calculados anteriormente
		List<Double> arrPPP = new ArrayList<Double>();

		mapPPP.forEach((key, value) -> {
			PrecioPromedio precioPromedio = (PrecioPromedio)value;
			arrPPP.add((precioPromedio.ppp).doubleValue());
		});
		
		if (arrPPP.size() > 0)
		{
			desviacionEstandar = obtenerDesviacionEstandar(arrPPP);
		}
		
		return desviacionEstandar;
	}


	

	
	  public static Double obtenerPromedio (List<Double> valores) 
	  {		  
		  double promedio = 0.0;		  
		  for (Double val : valores)
			  promedio += val;
		  
		  return promedio / (double) valores.size();		  
	  }
	

	  public static Double obtenerDesviacionEstandar (List<Double> valores) 
	  {
		    double promedio, sum = 0;
		    promedio = obtenerPromedio(valores);

		    for (Double val : valores)
		      sum += Math.pow(val - promedio, 2 );

		    return Math.sqrt (sum / (double) valores.size());
	}
	  
	  


	
	
	// ---------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------------------------------------------
	public static void main(String[]args) throws Exception{
	
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();
		
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_IdTipoAsignacion\": \"ASIGNACION_POSTRERA\",\r\n"
					+ "    \"p_IdTipo\": \"REFACTURACION\",\r\n"
					+ "    \"p_Origen\": \"COMP\",\r\n"
					+ "    \"p_Transacciones\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdMovimiento\": 1037666,\r\n"
					+ "            \"IdTransaccion\": 1037666,\r\n"
					+ "            \"Cantidad\": 320,\r\n"
					+ "            \"Monto\": 0,\r\n"
					+ "            \"ValorRescate\": 320\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdMovimiento\": 1037665,\r\n"
					+ "            \"IdTransaccion\": 1037665,\r\n"
					+ "            \"Cantidad\": 100,\r\n"
					+ "            \"Monto\": 0,\r\n"
					+ "            \"ValorRescate\": 100\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdMovimiento\": 1037664,\r\n"
					+ "            \"IdTransaccion\": 1037664,\r\n"
					+ "            \"Cantidad\": 1,\r\n"
					+ "            \"Monto\": 0,\r\n"
					+ "            \"ValorRescate\": 1\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"p_Carteras\": [],\r\n"
					+ "    \"p_Ordenes\": [],\r\n"
					+ "    \"p_Portafolios\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 463,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 7.37\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 7371,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 13.3915\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 5747,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 61.8985\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 13805,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 6.7825\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 7645,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 4.6785\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 1389,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 0.9915\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 3388,\r\n"
					+ "            \"Cantidad\": 0,\r\n"
					+ "            \"Porcentaje\": 4.8875\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
		   
			EntradaCrearSimulacionAsignacionPostrera entrada = gson.fromJson(json, EntradaCrearSimulacionAsignacionPostrera.class);
			
			CrearSimulacionAsignacionPostrera servicio = new CrearSimulacionAsignacionPostrera();
			SalidaCrearSimulacionAsignacionPostrera salida = servicio.logica(con, entrada);
			System.out.println(salida);
			con.rollback();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	// ---------------------------------------------------------------------------------------
	// CALL
	// ---------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
