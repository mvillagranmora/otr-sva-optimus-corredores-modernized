package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.InformacionOrden;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.OrdenProceso;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.LibrosOrdenesGrupo;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Transaccion_Proceso;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjERRORESDETALLE;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjORDENES;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ServiceSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Fecha;

public class ConsultarOrdenesConSobregiroCustodia implements JavaCall,JavaCallMetadata {
	
	private static final Logger logger = LogManager.getLogger(ConsultarOrdenesConSobregiroCustodia.class);

	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	private final String SQL_INSERT_ORDENES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_InsertOrdenesWrapper.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectOrdenes.sql");
	private final String SQL_SELECT_TRANSACCIONES= loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectTransacciones.sql");
	private final String SQL_SELECT_LIBROS_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectLibroOrdenesGrupo.sql");
	private final String SQL_UPDATE_LIBRO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_UpdateLibroOrdenes.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	class FlujoCustodiaElem {
		public String IdBoveda;
		public String IdTipoOperacion;
		public String Fecha;
		public Long IdInstrumento;
		public Long IdPortafolio;
		
		public FlujoCustodiaElem(String idBoveda,String idTipoOperacion,Long idPortafolio,String fecha,Long idInstrumento) {
			this.IdTipoOperacion = idTipoOperacion;
			this.IdBoveda = idBoveda;
			this.IdInstrumento = idInstrumento;
			this.Fecha = fecha;
			this.IdPortafolio = idPortafolio;
		}
		
		@Override
		public boolean equals(Object o) {
			FlujoCustodiaElem e = (FlujoCustodiaElem)o;
			boolean b = this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion) && this.Fecha.equalsIgnoreCase(e.Fecha) 
					&& this.IdPortafolio.equals(e.IdPortafolio) && (this.IdBoveda == e.IdBoveda || this.IdBoveda.equalsIgnoreCase(e.IdBoveda) ) && this.IdInstrumento.equals(e.IdInstrumento);
			return b;
		}

		@Override
		public int hashCode() {
		    return (this.IdBoveda + this.IdTipoOperacion + this.Fecha + this.IdInstrumento + this.IdPortafolio).hashCode();  
		}
		
	}
	
	public CrearOrdenesPostreras_Salida logica(Connection con,  CrearOrdenesPostreras_Entrada in) throws Exception {
		SQL _sql = new SQL();
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
		ArrayList<OrdenProceso> ordenes = new ArrayList<>();
		CrearOrdenesSalida outOrdenes = new CrearOrdenesSalida();
		CrearOrdenesPostreras_Salida out = new CrearOrdenesPostreras_Salida();
		String operacionesParam = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.OperacionesNegativas");
		String paramLiquidacionComisionFact = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PRODUCTO", "PRO.CobroComisionesMensuales");
		paramLiquidacionComisionFact = "ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N";
		String horaSistema = FechaDB.ObtenerHoraSistemaLocal(con,in.p_IdEmpresa);
		CrearOrdenes crearOrdenes = new CrearOrdenes();
		
		List<String> operaciones = new ArrayList<>();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Timestamp fechaHoraSistemaTS = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,in.p_IdEmpresa));
		String fechaHoraSistema_ = Fecha.toStringDatetime(fechaHoraSistemaTS);
		
        Timestamp fechaHoraReal= Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa));
		java.sql.Date fechaReal = Fecha.toDate(fechaHoraReal.toString().split(" ")[0]);
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
        String diaSiguiente_FechaSistema = crearOrdenes.obtieneFechaDiaSiguiente(fechaSistema,idPaisEmpresa,con,outOrdenes);
		Boolean esDiaHabil_FechaSistema = crearOrdenes.esDiaHabil(fechaSistema,in.p_IdEmpresa, con);
		Boolean esDiaHabil_FechaReal= crearOrdenes.esDiaHabil(fechaReal.toString(),in.p_IdEmpresa, con);
		Date fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate("0001-01-01");
		java.sql.Timestamp fechaUltimaEjecucionCambioFecha = Fecha.toTimeStamp("0001-01-01 00:00:00");
		fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate(fechaUltimaEjecucionCambioFecha.toString().split(" ")[0]);
		String plazoConf = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", "PlazoConfirmacionOrden");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		CrearOrdenesPostreras postreras = new CrearOrdenesPostreras();
		
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		List<Long> idsTransaccion = new ArrayList<Long>();
		for (OrdenPostrera_Entrada ordenPostrera : in.p_Ordenes) {
			idsTransaccion.add(ordenPostrera.IdTransaccion);
		}
		
		params1.put("idsTransaccion", idsTransaccion);
		List<Transaccion_Proceso> _transacciones = _sql.query(con, SQL_SELECT_TRANSACCIONES, params1, Transaccion_Proceso.class);
		HashMap<Long,Transaccion_Proceso> transacciones = new HashMap<Long,Transaccion_Proceso>();
		for ( Transaccion_Proceso tran : _transacciones ) {
			transacciones.put(tran.IdTransaccion, tran);
		}
		//--------------------------
		
		
		if (in.p_Ordenes.get(0).IdGrupo != null) {
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("IdGrupo", in.p_Ordenes.get(0).IdGrupo);
			List<LibrosOrdenesGrupo> LibrosGrupo = _sql.query(con, SQL_SELECT_LIBROS_ORDENES, param, LibrosOrdenesGrupo.class);
			if (!LibrosGrupo.isEmpty()) {
				List<LibrosOrdenesGrupo> LibroOrden = new ArrayList<>();
				for ( Transaccion_Proceso tran : _transacciones ) {
					if(!tran.IdLibroOrdenes.equals(LibrosGrupo.get(0).IdLibroOrdenes)) {
						LibrosOrdenesGrupo Libro = new  LibrosOrdenesGrupo();
						Libro.IdLibroOrdenes = LibrosGrupo.get(0).IdLibroOrdenes;
						Libro.IdTransaccion = tran.IdTransaccion;
						LibroOrden.add(Libro);
						tran.IdLibroOrdenes = LibrosGrupo.get(0).IdLibroOrdenes;
					}
				}
				_sql.batch(con,SQL_UPDATE_LIBRO_ORDEN,LibroOrden, LibrosOrdenesGrupo.class);
			}
		}
		
		
		//----------------------------
		CrearOrdenesEntrada crearOrdenesEntrada = new CrearOrdenesEntrada();
		crearOrdenesEntrada.p_IdEmpresa = in.p_IdEmpresa;
		crearOrdenesEntrada.p_IdModulo = in.p_IdModulo;
		crearOrdenesEntrada.p_IdUsuario = in.p_IdUsuario;
		crearOrdenesEntrada.p_simularOperacion = _c.NO;
		if(in.p_simularOperacion != null) {
			crearOrdenesEntrada.p_simularOperacion = in.p_simularOperacion;
		}
		crearOrdenesEntrada.p_UsaReglasNegocio = _c.SI;
	
		
		
		for (OrdenPostrera_Entrada ordenPostrera : in.p_Ordenes) {
			ObjOrdenesEntrada orden = new ObjOrdenesEntrada();
			
			Transaccion_Proceso tran = transacciones.get(ordenPostrera.IdTransaccion);
			if (tran == null) {
				String codigo = "TRANSACCION_NO_EXISTE";
				String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(orden.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}
			//Utils.copyPojo(ordenPostrera, orden);
			orden = postreras.copyPojoObjOrdenesEntrada(ordenPostrera);
			
			orden.EsPorMonto = "N";
			orden.FechaVigencia = "0001-01-01";
			orden.FechaVencimiento = ("IF".equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)? tran.FechaVencimiento: Fecha.toDate("0001-01-01"));

			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con,in.p_IdEmpresa);	
			if (_c.ORIGEN_REFACTURACION.equalsIgnoreCase(in.p_OrigenPost) || (tran.FechaIngresoOferta.before(Fecha.toDate(fechaHoraSistema))))
			{	
				//Para refacturacion la FechaOperacion de la orden debe ser la fecha actual ya que la transaccion puede ser anterior a la fecha actual		
				orden.FechaOperacion = fechaHoraSistema;
				orden.FechaRecepcion = fechaHoraSistema;
				
			}
			else
			{
				//FechaOperacion de la orden postrera tendra 5 minutos menos que FechaOperacion de la Transaccion. FechaRecepcion 4 minutos menos. FechaAceptacion 3 -> este en CrearOrdenes.java
				orden.FechaOperacion = Fecha.toStringDatetime(Fecha.substractSeconds(tran.FechaIngresoOferta, 300));//300 segundos = 5 minutos
				orden.FechaRecepcion = Fecha.toStringDatetime(Fecha.substractSeconds(tran.FechaIngresoOferta, 300));
			}
			
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				orden.FechaEmision = Fecha.toStringDate(tran.FechaEmision);
				orden.IdMonedaOrden= tran.IdMoneda;
			} else {
				orden.FechaEmision = "0001-01-01";
			}
			
			//AJUSTE DE CANTIDAD Y VALOR RESCATE
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.SI)) {
				orden.Cantidad = BigDecimal.ONE;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.NO) && 
					tran.CorteMinimo != null && tran.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
				orden.Cantidad = ordenPostrera.ValorRescate.divide(tran.CorteMinimo,0,RoundingMode.FLOOR);
			}
			else {
				orden.ValorRescate = BigDecimal.ZERO;
			}
			orden.IdTipoCanalIngreso = _c.TIPO_CANAL_INGRESO_TRADER;
			orden.IdTipoReajuste = tran.IdTipoReajuste;
			orden.IdEmisor = tran.IdEmisor;
			orden.IdBolsa = tran.IdBolsa;
			orden.IdInstrumento = tran.IdInstrumento;
			orden.IdInstrumentoSubyacente = tran.IdInstrumentoSubyacente;
			
			orden.IdCondicionDeLiquidacion = tran.IdCondicionDeLiquidacion;
			orden.IdTipoOperacion = tran.IdTipoOperacion;
			
			orden.NemotecnicoSVS = tran.NemotecnicoSVS;
			orden.EsVentaCorta = tran.EsVentaCorta;
			orden.EsFungible = tran.EsFungible;
			crearOrdenesEntrada.p_Ordenes.add(orden);
		}
		
		String[] plazoCnf = plazoConf.split(";");
		HashMap< String, Long> plazoConfirmacion = new HashMap<>();
		for (String p: plazoCnf){
			String [] val = p.split(":");
			plazoConfirmacion.put(val[0], new Long(val[1]));
		}
		
		try {
			String[] array = operacionesParam.split(",");
			operaciones = Arrays.asList(array);
		}
		catch (Exception e) {
			logger.error("Error",e);
		}
		
		HashMap<Long,OrdenProceso> ordenesHash = new HashMap<Long,OrdenProceso>();
		//LECTURA DE DATOS DE LA ORDEN
		{	
			//Se insertan datos de ordenes en la tabla de paso
			Long idTrabajo = CrearOrdenes.CONTADOR_ID_TRABAJO.incrementAndGet();
			for (ObjOrdenesEntrada ordenEntradaTrabajo : crearOrdenesEntrada.p_Ordenes) {
				ordenEntradaTrabajo.IdTrabajo = idTrabajo;
			}
			_sql.batch(con,SQL_INSERT_ORDENES_WRAPPER,crearOrdenesEntrada.p_Ordenes, ObjOrdenesEntrada.class);
			
			HashMap<String,Object> paramsIn1 = new HashMap<>();
			paramsIn1.put("IdEmpresa", crearOrdenesEntrada.p_IdEmpresa);
			paramsIn1.put("IdUsuario", crearOrdenesEntrada.p_IdUsuario);
			paramsIn1.put("IdTrabajo", idTrabajo);
		
			List<InformacionOrden> _infoordenes = _sql.query(con, SQL_SELECT_ORDENES, paramsIn1, InformacionOrden.class);
			HashMap<Long,Object> infoOrdenes = Utils.List2Hash(_infoordenes, "IdInterno");
			
			for (ObjOrdenesEntrada ordenEntrada : crearOrdenesEntrada.p_Ordenes) {
				InformacionOrden info = (InformacionOrden) infoOrdenes.get(ordenEntrada.IdInterno);
				OrdenProceso ordenProceso = crearOrdenes.copyPojoOrdenProceso(ordenEntrada);
				
				
				ordenProceso.IdInterno = ordenEntrada.IdInterno;
				ordenProceso.Cantidad = ordenEntrada.Cantidad;
				String horaRecepcion = ordenEntrada.FechaRecepcion.length() == 19 ? ordenEntrada.FechaRecepcion.substring(11) : ordenEntrada.FechaRecepcion; // "0001-01-01 00:00:00"
				String horaUtcChile = horaRecepcion.length() == 8 ? crearOrdenes.convertirHoraUtcAHoraChile(horaRecepcion) : horaSistema;// "00:00:00"
				String horaUtcSistema = FechaDB.ObtenerHoraUTC(con);

				
				if (ordenEntrada.FechaRecepcion == null || ("0001-01-01 00:00:00").equalsIgnoreCase(ordenEntrada.FechaRecepcion) || ("00:00:00").equalsIgnoreCase(horaUtcChile)) {
				    ordenProceso.FechaRecepcion = Fecha.toDateTime(fechaHoraSistema_);
				}
				else {
				    ordenEntrada.FechaRecepcion = ordenEntrada.FechaRecepcion.length() == 19 ? ordenEntrada.FechaRecepcion : ordenEntrada.FechaRecepcion+" "+horaUtcSistema; 
				    ordenProceso.FechaRecepcion = Fecha.toDateTime(ordenEntrada.FechaRecepcion);
				}
				
				
				if (ordenEntrada.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE) && !ordenEntrada.FechaOperacion.equalsIgnoreCase("0001-01-01") && !ordenEntrada.FechaOperacion.equalsIgnoreCase(""))
				{	// Aca entran las ordenes por carma masiva, donde se envia la FechaOperacion con una hora definida
					ordenProceso.FechaOperacion = Fecha.toDateTime(ordenEntrada.FechaOperacion);
					ordenProceso.FechaSistemaCreacion = Fecha.toDate(ordenEntrada.FechaOperacion);
				}
				else if (ordenEntrada.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) { //// TRA
					ordenProceso.FechaOperacion = Fecha.toDateTime(ordenEntrada.FechaOperacion);
					ordenProceso.FechaSistemaCreacion = Fecha.toDate(ordenEntrada.FechaOperacion);
				} else {
					
					ordenProceso.FechaOperacion = Fecha.toDateTime(fechaHoraSistema_);
					ordenProceso.FechaSistemaCreacion = Fecha.toDate(fechaSistema);
					
					String utcStringTime = Fecha.toStringTime(ordenProceso.FechaOperacion);
						LocalTime lt = LocalTime.parse(utcStringTime).plusSeconds(segundosZonaHoraria);
						 
						LocalTime inicioDia = LocalTime.parse("00:00:00");
						LocalTime finDia = LocalTime.parse("23:59:59");
						LocalTime premkInicio = LocalTime.parse(info.HorarioPreMarketInicio);
						LocalTime mkFin = LocalTime.parse(info.HorarioMarketFin);

					if (info.ValidaHorario.equalsIgnoreCase("S") && ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("MAN")) {
							if (!esDiaHabil_FechaReal) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									//Valida si la fecha de sistema es dia habil, por pauta de fin de mes que asigna dia inhabil al sistema
									if(esDiaHabil_FechaSistema) {
										//la fecha de sistema es habil, se mantiene la fecha sistema creacion
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									}
									else {
										ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);	
									}
									
								}
							}
							//LOS 3 CASOS SIGUIENTES SON EN DIAS HABILES
							//CASO POST PAUTA, BUSCA LA ULTIMA FECHA DE EJECUCION DE PAUTA Y SI ES IGUAL A LA FECHA REAL QUIERE DECIR QUE SE EJECUTO LA PAUTA Y DEBE QUEDAR PARA MISMA FECHA SISTEMA + HORA 9:01
							else if (fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) ==  0) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									//se mantiene la fecha sistema creacion
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
								}
							}
							//ORDEN ENTRE 00 Y 09 (LOCAL) MISMA FECHA SISTEMA + HORA 9:01 (HORARIO NO VALIDO) PREVIA A APERTURA DE MERCADO
							else if (lt.compareTo(inicioDia) >= 1 && lt.compareTo(premkInicio) <= -1) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									//se mantiene la fecha sistema creacion
								}
							}
							//ORDEN ENTRE 16 Y 23 59 (LOCAL) MISMA FECHA SISTEMA, DEBE QUEDAR FECHA SISTEMA + 1 DIA HABIL + HORA 9:01  (HORARIO NO VALIDO), ESTO ES POST CIERRE MERCADO Y PREVIO A PAUTA
							else if (lt.compareTo(mkFin) >= 1 && lt.compareTo(finDia) <= -1) {
								ordenProceso.CumpleHorario = _c.NO;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									
									//se le suma 1 dia habil a la fecha sistema
									ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);	
								}
							}
					}	
					//RECALCULO DE FECHA DE OPERACION PARA ORDENES DE RUTEO FUERA DEL HORARIO DE MERCADO
					//SI NO SABE PREGUNTE
					if (info.ValidaHorario.equalsIgnoreCase("S") && (ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("RUR") || ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("RUA"))) {
						//FECHA REAL INHABIL?, ESTO ES PARA EL CASO QUE EL CLIENTE INGRESE UNA ORDEN SABADO O DOMINGO O FERIADO, DEBE QUEDAR PARA LA MISMA FECHA DEL SISTEMA A LAS 9AM
						if (!esDiaHabil_FechaReal) {
							ordenProceso.CumpleHorario = _c.NO;
							ordenProceso.AplazaSoloHora = _c.SI;
							ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
							if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
								//Valida si la fecha de sistema es dia habil, por pauta de fin de mes que asigna dia inhabil al sistema
								if(esDiaHabil_FechaSistema) {
								//la fecha de sistema es habil, se mantiene la fecha sistema creacion
								ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
								}
								else {
									//la fecha de sistema no es habil, se le suma 1 dia habil a la fecha sistema
									ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema , info.HorarioPreMarketInicio);	
								}
								
							}
						}
						//LOS 3 CASOS SIGUIENTES SON EN DIAS HABILES
						//CASO POST PAUTA, BUSCA LA ULTIMA FECHA DE EJECUCION DE PAUTA Y SI ES IGUAL A LA FECHA REAL QUIERE DECIR QUE SE EJECUTO LA PAUTA Y DEBE QUEDAR PARA MISMA FECHA SISTEMA + HORA 9:01
						else if (fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) ==  0) {
							ordenProceso.CumpleHorario = _c.NO;
							ordenProceso.AplazaSoloHora = _c.SI;
							if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
								//se mantiene la fecha sistema creacion
								ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
								ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
							}
						}
						//ORDEN ENTRE 00 Y 09 (LOCAL) MISMA FECHA SISTEMA + HORA 9:01 (HORARIO NO VALIDO) PREVIA A APERTURA DE MERCADO
						else if (lt.compareTo(inicioDia) >= 1 && lt.compareTo(premkInicio) <= -1) {
							ordenProceso.CumpleHorario = _c.NO;
							ordenProceso.AplazaSoloHora = _c.SI;
							if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
								ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
								//se mantiene la fecha sistema creacion
							}
						}
						//ORDEN ENTRE 16 Y 23 59 (LOCAL) MISMA FECHA SISTEMA, DEBE QUEDAR FECHA SISTEMA + 1 DIA HABIL + HORA 9:01  (HORARIO NO VALIDO), ESTO ES POST CIERRE MERCADO Y PREVIO A PAUTA
						else if (lt.compareTo(mkFin) >= 1 && lt.compareTo(finDia) <= -1) {
							ordenProceso.CumpleHorario = _c.NO;
							if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
								ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
								ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema , info.HorarioPreMarketInicio);	
							}
						}
						
					}
											
				}
				
				if (ordenEntrada.IdBoveda == null || ordenEntrada.IdBoveda.equalsIgnoreCase("0")) {
					ordenProceso.IdBoveda = null;
				} else {
					ordenProceso.IdBoveda = ordenEntrada.IdBoveda;
				}
				ordenProceso.IdPortafolioCliente = ordenEntrada.IdPortafolio;
				if (ordenEntrada.EsVentaCorta == null) {
					ordenProceso.EsVentaCorta = _c.NO;
					ordenProceso.MontoPrimaVentaCorta = BigDecimal.ZERO;
				} else {
					ordenProceso.EsVentaCorta = ordenEntrada.EsVentaCorta;
				}
				
				
				ordenProceso.IdOrden = info.IdOrden;
				ordenProceso.IdClasificacionTipoInstrumento = info.IdClasificacionTipoInstrumento;
				ordenProceso.IdEmpresa = crearOrdenesEntrada.p_IdEmpresa;
				ordenProceso.IdTipoInstrumento = info.IdTipoInstrumento;
				
				
				if (localizacionSistema.equalsIgnoreCase("CL") && info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
					ordenProceso.IdTipoReajuste = info.IdTipoReajusteInstrumento;
				}
				else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
					ordenProceso.IdTipoReajuste = ordenEntrada.IdTipoReajuste;
				}
				else {
					ordenProceso.IdTipoReajuste = "";
				}
				
				if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
					ordenProceso.IdEmisor = ordenEntrada.IdEmisor;
				}
				else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
					ordenProceso.IdEmisor = info.IdEmisor;
				}
				else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
						&& info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)
						&& ordenEntrada.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
					ordenProceso.IdEmisor = info.IdEmisorInstrumentoSubyacente;
				}
				else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
						&& info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)
						&& ordenEntrada.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)) {
					ordenProceso.IdEmisor = "";
				}
				else {
					ordenProceso.IdEmisor = info.IdEmisor;
				}
				
				if (info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
					ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumentoSubyacente;
				}
				else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
					ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumentoSubyacente;
				}
				else {
					ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumento;
				}
				
				//datos leidos desde tabla
				ordenProceso.AccionOrden = info.AccionOrden;
				ordenProceso.CodigoPortafolio = info.CodigoPortafolio;
							
				ordenProceso.Nemotecnico = info.Nemotecnico;
				
				
				ordenProceso.DiasLiquidacionContado = info.DiasLiquidacionContado;
				
				
				ordenProceso.ValidaHorario = info.ValidaHorario;
				ordenProceso.AplazaOrdenPorHorarioNegociacion = info.AplazaOrdenPorHorarioNegociacion;
				ordenProceso.HorarioPreMarketInicio = info.HorarioPreMarketInicio;
				ordenProceso.HorarioPreMarketFin = info.HorarioPreMarketFin;
				ordenProceso.HorarioMarketInicio = info.HorarioMarketInicio;
				ordenProceso.HorarioMarketFin = info.HorarioMarketFin;
				
				ordenProceso.DocumentoIdentidad = info.DocumentoIdentidad;
				ordenProceso.NombreCompleto = info.NombreCompleto;
				ordenProceso.FechaVencimientoIF = ordenEntrada.FechaVencimiento;
				ordenProceso.FechaVencimientoInstrumento = info.FechaVencimientoInstrumento;
				ordenProceso.FechaVencimientoInstrumentoSubyacente = info.FechaVencimientoInstrumentoSubyacente;
				
				
			
				
				
				//FIN
				ordenesHash.put(ordenProceso.IdInterno, ordenProceso);
				ordenes.add(ordenProceso);
			}
		}
		
		
		//INDICADORES PORTAFOLIO
		HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
		{
			HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
			ArrayList<Long> idsPortafolio = new ArrayList<>();
			
			for (OrdenProceso ord : ordenes) {
				idsPortafolio.add(ord.IdPortafolioCliente);
				if (ord.IdFormaCustodia.equalsIgnoreCase("CUS") && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA) && ord.EsVentaCorta.equals(_c.NO) && !ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_DV)) {
					ord.ValidaSaldoCustodia = _c.SI;
				} else {
					ord.ValidaSaldoCustodia = _c.NO;
				}
			}
			
			paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
			List<IndicadoresPortafolio> _indicadoresPortafolio = _sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
			HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
			for(Object key: __indicadoresPortafolio.keySet()) {
				indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
			}
		}
		//FECHAS LIQUIDACION
		{
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			for (OrdenProceso ord : ordenes) {
				FechasIn f = new FechasIn();
				f.fechaInicial = Fecha.toStringDate(ord.FechaSistemaCreacion);
				if (ord.DiasLiquidacionContado == null)
					ord.DiasLiquidacionContado = 0L;
				f.numeroDias = new Long(ord.DiasLiquidacionContado);
				inObtenerDiaSiguiente.p_fechas.add(f);
			}
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				for (OrdenProceso ord : ordenes) {
					if (Fecha.toStringDate(ord.FechaSistemaCreacion).equalsIgnoreCase(fecha.fechaInicial) && ord.DiasLiquidacionContado.equals(fecha.numeroDiasInicial)){
						ord.FechaLiquidacion = Fecha.toDate(fecha.fechaCalculada);
					}
				}
			}
			//ordenProceso.FechaLiquidacion = Fecha.toDate(outObtenerDiaSiguiente.fechas.get(0).fechaCalculada);
			
		}
		
		//FLUJO CUSTODIA 
		{
			
			
			class FlujoCustodiaIFElem {
				String IdBoveda;
				String IdTipoOperacion;
				String Fecha;
				Long IdInstrumento;
				Long IdPortafolio;
				
				public String IdEmisor;
				public String IdTipoReajuste;
				public String EsFungible;
				public String FechaVencimiento;
				
				public FlujoCustodiaIFElem(String idBoveda,String idTipoOperacion,Long idPortafolio,String fecha,Long idInstrumento, String idEmisor, String idTipoReajuste, String esFungible, String fechaVencimiento) {
					this.IdTipoOperacion = idTipoOperacion;
					this.IdBoveda = idBoveda;
					this.IdInstrumento = idInstrumento;
					this.Fecha = fecha;
					this.IdPortafolio = idPortafolio;
					this.IdEmisor = idEmisor;
					this.IdTipoReajuste = idTipoReajuste;
					this.EsFungible = esFungible;
					this.FechaVencimiento = fechaVencimiento;
				}
				
				@Override
				public int hashCode() {
				    return (this.IdTipoOperacion + this.IdBoveda + this.IdInstrumento + this.Fecha + this.IdPortafolio + this.IdEmisor + this.IdTipoReajuste + this.EsFungible + this.FechaVencimiento).hashCode();  
				}
				
				@Override
				public boolean equals(Object o) {
					FlujoCustodiaIFElem e = (FlujoCustodiaIFElem)o;
					return this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.EsFungible.equalsIgnoreCase(e.EsFungible)
							&& this.IdPortafolio.equals(e.IdPortafolio) && (this.IdBoveda == e.IdBoveda || this.IdBoveda.equalsIgnoreCase(e.IdBoveda)) && this.IdInstrumento.equals(e.IdInstrumento)
							&& this.IdTipoReajuste.equalsIgnoreCase(e.IdTipoReajuste) && this.IdEmisor.equalsIgnoreCase(e.IdEmisor)
							&& this.FechaVencimiento.equalsIgnoreCase(e.FechaVencimiento);
				}
				
			}
			
			ConsultarCustodiaIngresoOrden consultarCustodiaService = new ConsultarCustodiaIngresoOrden();
			Long DiasLiquidacionContadoMaximo = ordenes.get(0).DiasLiquidacionContado;
			java.sql.Date fechaOperacionMinima = ordenes.get(0).FechaSistemaCreacion;
			HashMap<FlujoCustodiaElem,BigDecimal> totalCustodiaOrdenes = new HashMap<>();
			HashMap<FlujoCustodiaIFElem,BigDecimal> totalCantidadOrdenesIF = new HashMap<>();
			HashMap<FlujoCustodiaIFElem,BigDecimal> totalValorRescateOrdenesIF = new HashMap<>();
			
			HashSet<ObjORDENES> ordenesConsultarCustodia = new HashSet<ObjORDENES>();
			for (OrdenProceso ord : ordenes) {
				if (ord.ValidaSaldoCustodia.equalsIgnoreCase(_c.NO))
					continue;
				
				if (ord.DiasLiquidacionContado.compareTo(DiasLiquidacionContadoMaximo) == 1) {
					DiasLiquidacionContadoMaximo = ord.DiasLiquidacionContado;
				}
				
				java.sql.Date fechaLiquidacion = ord.FechaLiquidacion;
				
				if (fechaLiquidacion.before(fechaOperacionMinima)) {
					fechaOperacionMinima = fechaLiquidacion;
				}
				
				
				if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
					FlujoCustodiaIFElem llaveIF = new FlujoCustodiaIFElem(ord.IdBoveda, ord.IdTipoOperacion, ord.IdPortafolioCliente, Fecha.toStringDate(ord.FechaLiquidacion), ord.IdInstrumento,ord.IdEmisor, ord.IdTipoReajuste, ord.EsFungible, Fecha.toStringDate(ord.FechaVencimientoIF));
					if (!totalCantidadOrdenesIF.containsKey(llaveIF)) {
						totalCantidadOrdenesIF.put(llaveIF, BigDecimal.ZERO);
						totalValorRescateOrdenesIF.put(llaveIF, BigDecimal.ZERO);
					}
					BigDecimal totalCantidad = totalCantidadOrdenesIF.get(llaveIF);
					totalCantidad = totalCantidad.add(ord.Cantidad);
					totalCantidadOrdenesIF.put(llaveIF,totalCantidad);
					
					BigDecimal totalValorRescate = totalValorRescateOrdenesIF.get(llaveIF);
					totalValorRescate = totalValorRescate.add(ord.ValorRescate);
					totalValorRescateOrdenesIF.put(llaveIF,totalValorRescate);
					
				}else {
					FlujoCustodiaElem llave = new FlujoCustodiaElem(ord.IdBoveda, ord.IdTipoOperacion, ord.IdPortafolioCliente, Fecha.toStringDate(ord.FechaLiquidacion), ord.IdInstrumentoCustodia);
					
					if (!totalCustodiaOrdenes.containsKey(llave)) {
						totalCustodiaOrdenes.put(llave, BigDecimal.ZERO);
					}
					BigDecimal totalCantidad = totalCustodiaOrdenes.get(llave);
					totalCantidad = totalCantidad.add(ord.Cantidad);
					totalCustodiaOrdenes.put(llave,totalCantidad);
				}
				
				ObjORDENES port = new ObjORDENES();
				port.IdBoveda = ord.IdBoveda;
				port.IdPortafolio = ord.IdPortafolioCliente;
				port.IdTipoOperacion = ord.IdTipoOperacion;
				port.IdInstrumento = ord.IdInstrumento;
				port.CumpleParaVentaDescubierta = ord.CumpleParaVentaDescubierta;  //Si orden cumple requisitos para una posible venta descubierta
				port.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
				ordenesConsultarCustodia.add(port);
			}
			
			//actualizamos ordenes con total por grupo
			Iterator<Entry<FlujoCustodiaIFElem, BigDecimal>> it1 = totalCantidadOrdenesIF.entrySet().iterator();
			Iterator<Entry<FlujoCustodiaIFElem, BigDecimal>> it2 = totalValorRescateOrdenesIF.entrySet().iterator();
		    while (it1.hasNext()) {
		    	Entry<FlujoCustodiaIFElem, BigDecimal> pairCantidad = it1.next();
		    	Entry<FlujoCustodiaIFElem, BigDecimal> pairValorRescate = it2.next();
		        for (OrdenProceso orden : ordenes) {
		        	if (orden.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && pairCantidad.getKey().Fecha.equalsIgnoreCase(Fecha.toStringDate(orden.FechaLiquidacion))
		        			&& (pairCantidad.getKey().IdBoveda == orden.IdBoveda || pairCantidad.getKey().IdBoveda.equalsIgnoreCase(orden.IdBoveda)) && pairCantidad.getKey().IdInstrumento.equals(orden.IdInstrumentoCustodia)
		        			&& pairCantidad.getKey().IdPortafolio.equals(orden.IdPortafolioCliente) && pairCantidad.getKey().IdTipoOperacion.equalsIgnoreCase(orden.IdTipoOperacion)
		        			&& pairCantidad.getKey().EsFungible.equalsIgnoreCase(orden.EsFungible) && pairCantidad.getKey().IdEmisor.equalsIgnoreCase(orden.IdEmisor)
		        			&& pairCantidad.getKey().IdTipoReajuste.equalsIgnoreCase(orden.IdTipoReajuste) && pairCantidad.getKey().FechaVencimiento.equalsIgnoreCase(Fecha.toStringDate(orden.FechaVencimientoIF))
		        			) {
		        		orden.TotalCantidadCustodiaPortafolio = pairCantidad.getValue();
		        		orden.TotalValorRescateCustodiaPortafolio = pairValorRescate.getValue();
		        	}
		        	
		        }
		    }
		    
		  //actualizamos ordenes con total por grupo
			Iterator<Entry<FlujoCustodiaElem, BigDecimal>> it3 = totalCustodiaOrdenes.entrySet().iterator();
		    while (it3.hasNext()) {
		    	Entry<FlujoCustodiaElem, BigDecimal> pair = it3.next();
		        for (OrdenProceso orden : ordenes) {
		        	if (!orden.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && pair.getKey().Fecha.equalsIgnoreCase(Fecha.toStringDate(orden.FechaLiquidacion))
		        			&& (pair.getKey().IdBoveda == orden.IdBoveda || pair.getKey().IdBoveda.equalsIgnoreCase(orden.IdBoveda)) && pair.getKey().IdInstrumento.equals(orden.IdInstrumentoCustodia)
		        			&& pair.getKey().IdPortafolio.equals(orden.IdPortafolioCliente) && pair.getKey().IdTipoOperacion.equalsIgnoreCase(orden.IdTipoOperacion)
		        			) {
		        		orden.TotalCantidadCustodiaPortafolio = pair.getValue();
		        	}
		        }
		    }
			
			if (ordenesConsultarCustodia.size() > 0) {
				ConsultarCustodiaIngresoOrden.ServiceEntrada entradaConsultarCustodia = new ConsultarCustodiaIngresoOrden.ServiceEntrada();
				entradaConsultarCustodia.P_DIASPROYECCION = DiasLiquidacionContadoMaximo;
				entradaConsultarCustodia.P_FECHASALDO = Fecha.toStringDate(fechaOperacionMinima);
				entradaConsultarCustodia.P_IDEMPRESA = crearOrdenesEntrada.p_IdEmpresa;
				entradaConsultarCustodia.P_IDMODULO = crearOrdenesEntrada.p_IdModulo;
				entradaConsultarCustodia.P_IDUSUARIO = crearOrdenesEntrada.p_IdUsuario;
				entradaConsultarCustodia.P_VALORIZARFLUJO = "N";
				
				entradaConsultarCustodia.P_ORDENES = new ArrayList<ObjORDENES>(ordenesConsultarCustodia) ;
				ServiceSalida salidaConsultarCustodia = consultarCustodiaService.logica(con,entradaConsultarCustodia);
				
				List<ObjERRORESDETALLE> erroresSaldoCustodia = salidaConsultarCustodia.P_OUT_ERRORESDETALLE;
				
				for (OrdenProceso ord : ordenes){
					for (ObjERRORESDETALLE err : erroresSaldoCustodia) {
						if (ord.IdTipoOperacion.equalsIgnoreCase(err.IdTipoOperacion)
								&& ord.IdPortafolioCliente.equals(err.IdPortafolio)) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
							ord.errores++;
						}
					}
				}
				
				
				BigDecimal totalCantidad = BigDecimal.ZERO;
				String ultimaFechaSaldo = "";
				Long idInstrumentoActual = 0L;
				String ultimoUsoTipoOperacion = "";
				salidaConsultarCustodia.P_OUT_SALIDA.sort(Comparator
						.comparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.FechaSaldo,
								Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.IdInstrumento,
								Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.UsoTipoOperacion,
								Comparator.nullsLast(Comparator.naturalOrder()))
						); // nullsLast pone los null al final
				
				for (ConsultarCustodiaIngresoOrden.ObjSALIDA sld : salidaConsultarCustodia.P_OUT_SALIDA) {
					if (sld.IdInstrumento != null) {
						if (!ultimaFechaSaldo.equalsIgnoreCase(sld.FechaSaldo) || idInstrumentoActual.compareTo(sld.IdInstrumento) != 0 || !ultimoUsoTipoOperacion.equalsIgnoreCase(sld.UsoTipoOperacion)) {
							totalCantidad = BigDecimal.ZERO; // Reiniciar la suma
						}

						idInstrumentoActual = sld.IdInstrumento;
						ultimaFechaSaldo = sld.FechaSaldo;
						totalCantidad = totalCantidad.add(sld.Cantidad);
						ultimoUsoTipoOperacion = sld.UsoTipoOperacion;
					}
					for (OrdenProceso ord : ordenes) {
						if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && 
								sld.FechaSaldo.equals(Fecha.toStringDate(ord.FechaLiquidacion)) && sld.EsFungible.equals(ord.EsFungible) &&
								sld.FechaVencimiento.equals(ord.FechaVencimientoIF) && (sld.IdBoveda == ord.IdBoveda || sld.IdBoveda.equals(ord.IdBoveda)) && 
								sld.IdEmisor.equalsIgnoreCase(ord.IdEmisor) && sld.IdInstrumento.equals(ord.IdInstrumentoCustodia) &&
								sld.IdPortafolio.equals(ord.IdPortafolioCliente) && sld.IdTipoOperacion.equalsIgnoreCase(ord.IdTipoOperacion) &&
								sld.IdTipoReajuste.equalsIgnoreCase(ord.IdTipoReajuste)
								) {
							ord.SaldoCantidadCustodiaPortafolio = sld.Cantidad;
							ord.SaldoValorRescateCustodiaPortafolio = sld.ValorRescate;
						}
						if(!ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && sld.IdInstrumento != null &&
								sld.FechaSaldo.equals(Fecha.toStringDate(ord.FechaLiquidacion)) && (sld.IdBoveda != null && sld.IdBoveda.equals(ord.IdBoveda)) && 
								sld.IdInstrumento.equals(ord.IdInstrumentoCustodia) && sld.IdPortafolio.equals(ord.IdPortafolioCliente) && sld.IdTipoOperacion.equalsIgnoreCase(ord.IdTipoOperacion)) {
							if (sld.UsoTipoOperacion.equalsIgnoreCase("VENDES")) 
							{	// Flujo Venta Descubierta (Solo para RV)
								ord.SaldoCantidadVentaDescubiertaCustodiaPortafolio = sld.Cantidad;
							}
							else
							{	// Otros flujos
								ord.SaldoCantidadCustodiaPortafolio = totalCantidad;
							}
						}			
					}
				}
			}
		}
		
		//VALIDACIONES
		{
			for (OrdenProceso ord : ordenes) {
				boolean indicador_sobregiro = false;
				if (operaciones.stream().anyMatch(o -> o.equals(ord.IdTipoOperacion))) {
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroRF.equalsIgnoreCase("S") ) {
						indicador_sobregiro = true;
					} else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroRV.equalsIgnoreCase("S") ) {
						indicador_sobregiro = true;
					} else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroIF.equalsIgnoreCase("S") ) {
						indicador_sobregiro = true;
					}
				}
				
				//flujo para if fungible
				if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && ord.EsFungible.equalsIgnoreCase(_c.SI)) {
					if (indicador_sobregiro) {
						if (ord.TotalValorRescateCustodiaPortafolio.doubleValue() > ord.SaldoValorRescateCustodiaPortafolio.doubleValue()) {
							
							HashMap<String, String> datos = new HashMap<String, String>();
							DecimalFormat formatea = new DecimalFormat("###,###.##");
							datos.put("Cantidad", ""+formatea.format(ord.TotalValorRescateCustodiaPortafolio.doubleValue() - ord.SaldoValorRescateCustodiaPortafolio.doubleValue()));
							datos.put("Nemotecnico", ord.NemotecnicoSVS);
							datos.put("Nombre", ord.NombreCompleto);
							datos.put("Portafolio", ord.CodigoPortafolio);
							out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,crearOrdenesEntrada.p_IdEmpresa));
						}
					}
				}
				//flujo para if no fungible
				else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && ord.EsFungible.equalsIgnoreCase(_c.NO)) {
					if (indicador_sobregiro) {
							if (ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()) {
								HashMap<String, String> datos = new HashMap<String, String>();
								DecimalFormat formatea = new DecimalFormat("###,###.##");
								datos.put("Cantidad", ""+formatea.format(ord.TotalCantidadCustodiaPortafolio.doubleValue() - ord.SaldoCantidadCustodiaPortafolio.doubleValue()));
								datos.put("Nemotecnico", ord.NemotecnicoSVS);
								datos.put("Nombre", ord.NombreCompleto);
								datos.put("Portafolio", ord.CodigoPortafolio);
								out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,crearOrdenesEntrada.p_IdEmpresa));
							}
					}

				}
				//flujo para los demas
				else {
					if (indicador_sobregiro) {
							if (ord.CumpleParaVentaDescubierta.equalsIgnoreCase("N") && (ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue())) {
								
								HashMap<String, String> datos = new HashMap<String, String>();
								DecimalFormat formatea = new DecimalFormat("###,###.##");
								datos.put("Cantidad", ""+formatea.format(ord.TotalCantidadCustodiaPortafolio.doubleValue() - ord.SaldoCantidadCustodiaPortafolio.doubleValue()));
								datos.put("Nemotecnico", ord.Nemotecnico);
								datos.put("Nombre", ord.NombreCompleto);
								datos.put("Portafolio", ord.CodigoPortafolio);
								out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,crearOrdenesEntrada.p_IdEmpresa));
							}
					}
				}
				
				
			}
			
			
		}
		
		return out;
	}
	
	
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOrdenesPostreras_Entrada in = gson.fromJson(jin.toString(), CrearOrdenesPostreras_Entrada.class);
		CrearOrdenesPostreras_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		this.setMetadata(in, out);
		return dataout;
	}



	@Override
	public void setMetadata(Object in, Object out) {
		CrearOrdenesPostreras_Salida salida = (CrearOrdenesPostreras_Salida)out;
		metadata[0] = salida.P_OUT_RESULTADO.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("mbi","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();		
			
			String pruebaRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":339,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"IdInterno\":0,\"FechaRecepcion\":\"2026-08-26 19:23:23\",\"Glosa\":\"\",\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"EsFacturaInmediata\":\"N\",\"IdTransaccion\":4245941,\"FolioTransaccion\":\"259125\",\"IdPortafolio\":12313,\"IdGrupo\":null,\"FolioCartera\":0,\"TipoCambio\":0,\"IdMonedaLiquidacionOrden\":\"CLP\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"PagoCobroCuentaInversion\":\"S\",\"IdPreferenciaPagoPortafolio\":null,\"CodigoIFRS\":\"\",\"IngresoDesde\":\"COR\",\"Cantidad\":73,\"ValorRescate\":0},{\"IdInterno\":1,\"FechaRecepcion\":\"2026-08-26 19:23:23\",\"Glosa\":\"\",\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"EsFacturaInmediata\":\"N\",\"IdTransaccion\":4245941,\"FolioTransaccion\":\"259125\",\"IdPortafolio\":2184,\"IdGrupo\":null,\"FolioCartera\":0,\"TipoCambio\":0,\"IdMonedaLiquidacionOrden\":\"CLP\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"PagoCobroCuentaInversion\":\"S\",\"IdPreferenciaPagoPortafolio\":1944959,\"CodigoIFRS\":\"\",\"IngresoDesde\":\"COR\",\"Cantidad\":6,\"ValorRescate\":0}]}";
	        
//			CrearOrdenesEntrada Objin = gson.fromJson(json, CrearOrdenesEntrada.class);
			CrearOrdenesPostreras_Entrada Objincheck = gson.fromJson(pruebaRF, CrearOrdenesPostreras_Entrada.class);
			ConsultarOrdenesConSobregiroCustodia demo=new ConsultarOrdenesConSobregiroCustodia();				
 			System.out.println("ENTRADA");
			System.out.println(Objincheck.toString());
 			System.out.println("SALIDA");
 				CrearOrdenesPostreras_Salida ObjOut=demo.logica(con, Objincheck);
 				con.rollback();
 				System.out.println(ObjOut.toString());

 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
	
}
