package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.replica.EnviarTablaReplica;
import cl.bcs.replica.EnviarTablaReplica.Entrada_EnviarTablaReplica;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LaPauta extends BcsJob {
	private Log log = null;

	FileLoader loader = new FileLoader();
	
	private String SQL_SELECT_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_SelectEjecucion.sql");
	
	private String SQL_SELECT_PROCESOS_PAUTA = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_SelectProcesosPauta.sql");
	private String SQL_INSERT_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_InsertEjecucion.sql");
	private String SQL_UPDATE_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_UpdateEjecucion.sql");
	private String SQL_INSERT_DETALLE_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_InsertDetalleEjecucion.sql");
	private String SQL_SELECT_DETALLE_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_SelectDetalleEjecucion.sql");
	private String SQL_UPDATE_DETALLE_EJECUCION = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_UpdateDetalleEjecucion.sql");
	private String SQL_SELECT_MODULOS_PAUTA = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/LaPauta_SelectModulosPauta.sql");
	private final String SQL_CONSULTAR_PAUTA = "CALL \"BSCL.Plataforma.BD.Servicios::PAU.ConsultarPauta\" ("
    		+ "p_IdEmpresa 						=> :p_IdEmpresa,"
    		+ "p_IdUsuario 						=> :p_IdUsuario,"
    		+ "p_IdModulo 						=> :p_IdModulo,"
    		+ "p_FechaEjecucion 				=> :p_FechaEjecucion,"
    		+ "p_out_tbl						=> $p_out_tbl)";
	
	private static final String ESTADO_TERMINADO = "TER";
	private static final String ESTADO_ERROR = "ERR";
	private static final String ESTADO_PROCESANDO = "PRO";
	private static final String ESTADO_PENDIENTE = "PEN";
	private static final String SI = "S";
	private static final String NO = "N";
	private static final int LARGO_MAXIMO_MENSAJE = 500;
	
	
    @Override
    public boolean transaction(Connection con) throws Throwable {
        return logica(con,false);   
    }
    
    public static class DetallePauta {
		public Long IdEmpresa;
		public String RutaLog;
		public String FechaSistema;
		public String CodigoProceso;
		public String Descripcion;
		public Timestamp FechaFin;
		public String Estado;
		public Timestamp FechaInicio;
		public Long Orden;
		public String Mensaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class EstadoModulo {
		public String IdModulo;
		public String IdEstado;
		public Long IdEmpresa;
		public String NombreModulo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class Ejecucion {
    	public Long IdEmpresa;
    	public String FechaSistema;
    	public java.sql.Timestamp FechaInicio;
    	public java.sql.Timestamp FechaFin;
    	public String RutaLog;
    	public String Estado;
    
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
    
    public static class DetalleEjecucion {
    	public Long IdEmpresa;
    	public String FechaSistema;
    	public String CodigoProceso;
    	public java.sql.Timestamp FechaInicio;
    	public java.sql.Timestamp FechaFin;
    	public String Estado;
    	public String Mensaje;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
    
    public static class ProcesoPauta {
    	public String CodigoProceso;
    	public String NombreClase;
    	public Long Orden;
    	public String DetienePauta;
    	public String CodigoProcesoDependencia;
    	public String Estado;
    	
    	@Override
    	public boolean equals(Object o) {
    		ProcesoPauta p = (ProcesoPauta)o;
    		return this.CodigoProceso.equals(p.CodigoProceso) && this.NombreClase.equals(p.NombreClase);
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		} 
    }
   
    public boolean logica(Connection conn, boolean debug) throws Throwable {
    	SQL _sql = new SQL();
    	
    	//parametros entrada
    	Long idEmpresa = null;
    	Long idUsuario = null;
    	String fechaSistemaPauta = null;
    	String jobsString = null;
    	Long parIdPrograma= super.getJobId();
    	List<ProcesoPauta> procesosPauta = new ArrayList<>();
    	List<EstadoModulo> estadosModulos = new ArrayList<>();
    	if (debug) {
    		idEmpresa = 1L;
    		idUsuario = 1L;
    		jobsString = "";
    	}
    	else {
    		idEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
    		idUsuario = Long.parseLong(super.getParam("IdUsuario"));
    		fechaSistemaPauta = super.getParam("FechaSistemaPauta");
    		jobsString = super.getParam("Jobs");
    	}
    	
    	HashMap<String,Object> params =new HashMap<>();
    	int filtroCodigoProceso = 0;
    	List<String> codigoProceso = new ArrayList<String>();
    	
    	if (jobsString != null && !"".equals(jobsString)) {
			codigoProceso = new ArrayList<String>(Arrays.asList(jobsString.split("\\|")));
		}
    	if (codigoProceso.size() == 0) {
    		codigoProceso.add("");
    		filtroCodigoProceso = 1;
    	}
    	
    	
    	String rutaBaseArchivos = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
		if (debug) {
			rutaBaseArchivos = "C:\\";
		}
		log = new Log("PLA.LaPauta",rutaBaseArchivos,"PAU","yyyyMMdd_HHmmss");
		String filePath = log.getRelativePath();
    	boolean moduloAbierto = false;
    	List<EstadoModulo> Modulos = _sql.query(conn, SQL_SELECT_MODULOS_PAUTA, params , EstadoModulo.class);
    	estadosModulos.addAll(Modulos);
    	for (EstadoModulo modulos : estadosModulos) {
    		if(modulos.IdModulo.compareTo("SIS")!=0 && modulos.IdEstado.compareTo("ABT")==0 ) {
    			log.write("***** MODULO "+modulos.NombreModulo +" ABIERTO *****");
    		    moduloAbierto=true;
    		}
    			
    	};
    	if(moduloAbierto) {
    		log.write("***** CERRAR MODULOS PAUTA  *****");
    		return false;
    	}
    	
    	if (fechaSistemaPauta == null) {
			fechaSistemaPauta = ConfiguracionSistema.getParametroConfiguracionString(conn,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
		}
    	
    	PautaUtils pautaUtil = new PautaUtils();
    	
    	params.put("IdEmpresa", idEmpresa);
    	params.put("FiltroCodigoProceso", filtroCodigoProceso);
    	params.put("CodigoProceso", codigoProceso);
    	params.put("EjecutarDiaInhabil", pautaUtil.ejecutarDiaInhabil(conn,fechaSistemaPauta,idEmpresa)?"S":"N");
    	
		List<ProcesoPauta> procesos = _sql.query(conn, SQL_SELECT_PROCESOS_PAUTA, params , ProcesoPauta.class);
		
		if (params.get("EjecutarDiaInhabil") == "N") {
			for (ProcesoPauta proce : procesos) {
				if(proce.CodigoProceso.equals("EJECUTAR_PROCESOS_DIAINHABIL")) {
					procesos.remove(proce);
					break;
				}
			}
		}
		params.put("EjecutarDiaInhabil", pautaUtil.valorizarCuponFeriado(conn,fechaSistemaPauta,idEmpresa)?"S":"N");
		if (params.get("EjecutarDiaInhabil") == "N") {
			for (ProcesoPauta proce : procesos) {
				if(proce.CodigoProceso.equals("VALORIZAR_CUPON_FERIADO")) {
					procesos.remove(proce);
					break;
				}
			}
		}
		Long offset = ConfiguracionSistema.getParametroConfiguracionLong(conn,idEmpresa,"PLATAFORMA","PLA.SegundosZonaHoraria");

		//ejecucion completa
		procesosPauta.addAll(procesos);
		
		if (fechaSistemaPauta == null) {
			fechaSistemaPauta = ConfiguracionSistema.getParametroConfiguracionString(conn,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
		}
		
		
		log.write("***** INICIO PAUTA *****");
		
		//****************
		PautaUtils.notificarUsuariosTI("PAUTA", idEmpresa, "NO", "<br>"+ obtenerFechaHora("Fecha",offset) +
				": Se ha <strong>iniciado</strong> la ejecucion de la Pauta de cierre a las  "+ obtenerFechaHora("Hora",offset) +". <br><br>", parIdPrograma, conn, log);
		//****************
		
		Ejecucion ejecucion = new Ejecucion();
		ejecucion.FechaFin = Fecha.toDateTime("0001-01-01 00:00:00");
		ejecucion.FechaInicio = FechaDB.ObtenerFechaHoraUTCTs(conn);
		ejecucion.FechaSistema = fechaSistemaPauta;
		ejecucion.IdEmpresa = idEmpresa;
		ejecucion.Estado = ESTADO_PROCESANDO;
		
		HashMap<String,Object> paramsEjecuciones = new HashMap<>();
		paramsEjecuciones.put("FechaSistema", fechaSistemaPauta);
		paramsEjecuciones.put("IdEmpresa", idEmpresa);
		List<Ejecucion> _ejecucionActual = _sql.query(conn, SQL_SELECT_EJECUCION, paramsEjecuciones, Ejecucion.class);
	
		
		List<Ejecucion> ejecuciones = new ArrayList<>();
		List<DetalleEjecucion> detalles = new ArrayList<>();
		HashMap<ProcesoPauta,DetalleEjecucion> hashProcesos = new HashMap<>();
		
		if (_ejecucionActual.size() == 0) {
			ejecuciones.add(ejecucion);
			_sql.batch(conn, SQL_INSERT_EJECUCION, ejecuciones, Ejecucion.class);
			
			for (ProcesoPauta proceso : procesosPauta) {
				DetalleEjecucion detalle = new DetalleEjecucion();
				detalle.CodigoProceso = proceso.CodigoProceso;
				proceso.Estado = ESTADO_PENDIENTE;
				detalle.Estado = ESTADO_PENDIENTE;
				detalle.FechaFin = Fecha.toDateTime("0001-01-01 00:00:00");
				detalle.FechaInicio = Fecha.toDateTime("0001-01-01 00:00:00");
				detalle.FechaSistema = fechaSistemaPauta;
				detalle.IdEmpresa = idEmpresa;
				detalles.add(detalle);
				hashProcesos.put(proceso, detalle);
			}
			
			_sql.batch(conn, SQL_INSERT_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
		}
		else {
			HashMap<String,Object> paramsDetalleEjecucion = new HashMap<>();
			paramsDetalleEjecucion.put("FechaSistema", _ejecucionActual.get(0).FechaSistema);
			paramsDetalleEjecucion.put("IdEmpresa", _ejecucionActual.get(0).IdEmpresa);
			detalles = _sql.query(conn, SQL_SELECT_DETALLE_EJECUCION, paramsDetalleEjecucion, DetalleEjecucion.class);
			for (ProcesoPauta proceso : procesosPauta) {
				for (DetalleEjecucion detalle : detalles) {
					if (detalle.CodigoProceso.equalsIgnoreCase(proceso.CodigoProceso)) {
						hashProcesos.put(proceso, detalle);
					}
					
					//limpiamos los no terminados
					if (ESTADO_TERMINADO.equalsIgnoreCase(detalle.Estado)) {
						proceso.Estado = ESTADO_TERMINADO;
						continue;
					}
					detalle.Mensaje = "";
					proceso.Estado = ESTADO_PENDIENTE;
					detalle.Estado = ESTADO_PENDIENTE;
					detalle.FechaInicio = Fecha.toDateTime("0001-01-01 00:00:00");
					detalle.FechaFin = Fecha.toDateTime("0001-01-01 00:00:00");
					
				}
			}
			
			_sql.batch(conn, SQL_UPDATE_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
			if (!debug) conn.commit();
		}
		
		detalles.clear();
		
		int contadorErrores = 0;
		String procesoError;
		ProcesoPauta procesoDependencia;
		loopProcesosPauta:
		for (ProcesoPauta proceso : procesosPauta) {
			detalles.clear();
			procesoError = NO;
			
			DetalleEjecucion detalle = hashProcesos.get(proceso);
			if (ESTADO_TERMINADO.equalsIgnoreCase(detalle.Estado)) {
				log.write("Proceso " + proceso.CodigoProceso + " ya ejecutado");
				continue;
			}
			log.write("Iniciando proceso " + proceso.CodigoProceso);
			
			proceso.Estado = ESTADO_PROCESANDO;
			detalle.Estado = ESTADO_PROCESANDO;
			detalle.FechaInicio = FechaDB.ObtenerFechaHoraUTCTs(conn);
			detalles.add(detalle);
			_sql.batch(conn, SQL_UPDATE_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
			
			if (!debug) conn.commit();
			
			Class<?> tclass = null;
			try {
				tclass = Class.forName(proceso.NombreClase);
			} catch (Exception e) {
				proceso.Estado = ESTADO_ERROR;
				detalle.Estado = ESTADO_ERROR;
				detalle.FechaFin = FechaDB.ObtenerFechaHoraUTCTs(conn);
				detalle.Mensaje = e.toString().length() > LARGO_MAXIMO_MENSAJE ? e.toString().substring(0, LARGO_MAXIMO_MENSAJE) : e.toString();
				_sql.batch(conn, SQL_UPDATE_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
				if (!debug) conn.commit();
				log.write(e);
				log.write("Fin proceso " + proceso.CodigoProceso);
				procesoError = SI;
				
				//Notifica excepcion en proceso de pauta
				PautaUtils.notificarUsuariosTI(proceso.CodigoProceso, idEmpresa, procesoError, e.toString(), parIdPrograma, conn, log);
				
				if (SI.equalsIgnoreCase(proceso.DetienePauta)) {
					contadorErrores++;
					break loopProcesosPauta;
				}
				continue;
			}
			
			if (!"".equals(proceso.CodigoProcesoDependencia)) {
				procesoDependencia = obtenerProcesoDependencia(proceso.CodigoProcesoDependencia, procesosPauta);
				if (procesoDependencia != null) {
					if (!ESTADO_TERMINADO.equalsIgnoreCase(procesoDependencia.Estado)) {
						contadorErrores++;
						log.write("Proceso " + proceso.CodigoProceso + " no se puede ejecutar, proceso " + procesoDependencia.CodigoProceso + " en estado " + procesoDependencia.Estado);
						if (SI.equalsIgnoreCase(proceso.DetienePauta)) {
							break loopProcesosPauta;
						}
						continue;
					}
				} else {
					contadorErrores++;
					log.write("Proceso " + proceso.CodigoProceso + " no se puede ejecutar, proceso " + proceso.CodigoProcesoDependencia + " no ha sido ejecutado");
					if (SI.equalsIgnoreCase(proceso.DetienePauta)) {
						break loopProcesosPauta;
					}
					continue;
				}
			}
			
			SalidaProcesoPauta salidaProceso = null;
			try {
				BcsProcesoPauta proc = (BcsProcesoPauta) tclass.newInstance();
				Map<String, Object> parametrosJob = new HashMap<>();
				parametrosJob.put("IdEmpresa", idEmpresa);
				parametrosJob.put("IdUsuario", idUsuario);
				parametrosJob.put("FechaSistema", fechaSistemaPauta);
				parametrosJob.put("IdModulo", "PLA");
				parametrosJob.put("IdEjecucion", parIdPrograma);
				parametrosJob.put("CodigoProceso", proceso.CodigoProceso);
				
				salidaProceso = proc.transaction(conn, parametrosJob);
				log.write("Fin proceso " + proceso.CodigoProceso);
				
			} catch (Exception e) {
				proceso.Estado = ESTADO_ERROR;
				detalle.Estado = ESTADO_ERROR;
				detalle.FechaFin = FechaDB.ObtenerFechaHoraUTCTs(conn);
				detalle.Mensaje = e.toString().length() > LARGO_MAXIMO_MENSAJE ? e.toString().substring(0, LARGO_MAXIMO_MENSAJE) : e.toString();
				_sql.batch(conn, SQL_UPDATE_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
				if (!debug) conn.commit();
				log.write(e);
				log.write("Fin proceso con error " + proceso.CodigoProceso);
				procesoError = SI;
				//Notifica excepcion en proceso de pauta
				PautaUtils.notificarUsuariosTI(proceso.CodigoProceso, idEmpresa, procesoError, e.toString(), parIdPrograma, conn, log);
				if (SI.equalsIgnoreCase(proceso.DetienePauta)) {
					contadorErrores++;
					break loopProcesosPauta;
				}
			}

			if (salidaProceso != null) {
				detalle.FechaFin = FechaDB.ObtenerFechaHoraUTCTs(conn);
				detalle.Mensaje = salidaProceso.P_OUT_MENSAJE.length() > LARGO_MAXIMO_MENSAJE ? salidaProceso.P_OUT_MENSAJE.substring(0, LARGO_MAXIMO_MENSAJE) : salidaProceso.P_OUT_MENSAJE;
				if (salidaProceso.P_OUT_ERRORESCONTADOR > 0) {
					proceso.Estado = ESTADO_ERROR;
					detalle.Estado = ESTADO_ERROR;
					procesoError = SI;
				} else {
					proceso.Estado = ESTADO_TERMINADO;
					detalle.Estado = ESTADO_TERMINADO;
					procesoError = NO;
				}
				_sql.batch(conn, SQL_UPDATE_DETALLE_EJECUCION, detalles, DetalleEjecucion.class);
				log.write("Update detalle ejecucion " + proceso.CodigoProceso + " " + proceso.Estado);
				
				try {
					if (detalle.Estado == ESTADO_TERMINADO) {
						log.write("Inicio logica de replica para proceso " + proceso.CodigoProceso);
						String fechaSistemaEjecucion = ConfiguracionSistema.getParametroConfiguracionString(conn,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
						EnviarTablaReplica r = new EnviarTablaReplica();
						Entrada_EnviarTablaReplica in = new Entrada_EnviarTablaReplica();
						in.p_Parametros.put("FechaSistemaEjecucion",fechaSistemaEjecucion);
						in.p_Parametros.put("SalidaProceso",salidaProceso);
						in.p_IdEmpresa = idEmpresa;
						in.p_Proceso = proceso.CodigoProceso;
						r.logica(conn, in);
						log.write("Logica de replica ejecutada para proceso " + proceso.CodigoProceso);
					}
					else {
						log.write("Proceso " + proceso.CodigoProceso + " con error, no llamamos a la replica");
					}
				}
				catch (Exception e) {
					log.write("Error en replica " + proceso.CodigoProceso);
					log.write(e);
					//keep going
				}
				
				if (!debug) conn.commit();
				log.write("Salida: " + detalle.Mensaje);
				log.write("Fin proceso " + proceso.CodigoProceso);
				//Notifica estado en proceso de pauta
				salidaProceso.P_OUT_MENSAJE = salidaProceso.P_OUT_MENSAJE+" finalizado a las: "+ obtenerFechaHora("Hora",offset);
				PautaUtils.notificarUsuariosTI(proceso.CodigoProceso, idEmpresa, procesoError, salidaProceso.P_OUT_MENSAJE, parIdPrograma, conn, log);
				if (salidaProceso.P_OUT_ERRORESCONTADOR > 0) {
					if (SI.equalsIgnoreCase(proceso.DetienePauta)) {
						contadorErrores++;
						break loopProcesosPauta;
					}
				}
			}
			else {
				//salidaProceso == null
				log.write("No deberia caer aca " + proceso.CodigoProceso);
			}
		}
		
		ejecucion.FechaFin = FechaDB.ObtenerFechaHoraUTCTs(conn);
		ejecucion.RutaLog = filePath;
		
		_sql.batch(conn, SQL_UPDATE_EJECUCION, ejecuciones, Ejecucion.class);
		if (!debug) conn.commit();
		
		//**************
		//NOTIFICAR LA FINALIZACION DEL PROCESO DE LA PAUTA, INFORMANDO SI HA TENIDO ERRORES O NO, Y EN CASO DE HABERLOS INDICAR CUALES
		HashMap<String,Object> paramIn = new HashMap<>();	
		paramIn.put("p_IdEmpresa", idEmpresa);
		paramIn.put("p_IdUsuario", idUsuario);
		paramIn.put("p_IdModulo", "PLA");
		paramIn.put("p_FechaEjecucion", ejecucion.FechaSistema);
		
		HashMap<String,Class> paramOut = new HashMap<>();
		paramOut.put("p_out_tbl", DetallePauta.class);
		
		HashMap<String,Object> consultaPauta = _sql.call(conn, SQL_CONSULTAR_PAUTA, paramIn, paramOut);
		List<DetallePauta> ListaPauta = (List<DetallePauta>) consultaPauta.get("p_out_tbl");

		int contadorErr = 0;
		String listaErroresTitulo = "";
		for (DetallePauta detallePauta : ListaPauta) {			
			if(detallePauta.Estado.equals("ERR")) {				
				listaErroresTitulo = listaErroresTitulo + "<li>" +  detallePauta.CodigoProceso + " : " + detallePauta.Descripcion + "</li>";
				contadorErr++;
			}			
		}
		
		if(contadorErr != 0) {
			PautaUtils.notificarUsuariosTI("PAUTA", idEmpresa, "NO", ""+ obtenerFechaHora("Fecha",offset) +
					": La ejecucion de la Pauta ha <strong>finalizado</strong> a las "+ obtenerFechaHora("Hora",offset) +". <br>"+ contadorErr +" Procesos con error que debe revisar : <br><br> "+ listaErroresTitulo +"<br><br>", parIdPrograma, conn, log);
		}else {
			PautaUtils.notificarUsuariosTI("PAUTA", idEmpresa, "NO", ""+ obtenerFechaHora("Fecha",offset) +
					": La ejecucion de la Pauta ha <strong>finalizado Correctamente</strong> a las "+ obtenerFechaHora("Hora",offset) +". <br>", parIdPrograma, conn, log);
		}		
		//**************
		
		log.write("***** TERMINO PAUTA *****");
    	return true;
    }

    private ProcesoPauta obtenerProcesoDependencia(String CodigoProcesoDependencia, List<ProcesoPauta> procesos) {
    	for (ProcesoPauta proceso : procesos) {
    		if (CodigoProcesoDependencia.equalsIgnoreCase(proceso.CodigoProceso)) {
    			return proceso;
    		}
    	}
    	return null;
    }
    
    private String obtenerFechaHora(String peticion,Long offset) {
    	
    	String fechaHora = "";
    	ZonedDateTime.now();
    	LocalDateTime localDate = LocalDateTime.now();
    	localDate = localDate.plusSeconds(offset);
    	DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    	DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    	DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    	
    	switch(peticion) {
    	case "Fecha":    		
    		fechaHora = localDate.format(formatterFecha);
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
    
    
    @Override
	public void pause() {

	}
	
	public static void main(String [] args) throws Throwable {
		LaPauta pauta = new LaPauta();
		Long offset = -10800L;
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Fecha",offset));
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Hora",offset));
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Ambos",offset));
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("scotia","connections.properties")) {
			pauta.logica(conn, true);
			//conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(Throwable t) {
		log.write(t);
		t.printStackTrace();
	}
}