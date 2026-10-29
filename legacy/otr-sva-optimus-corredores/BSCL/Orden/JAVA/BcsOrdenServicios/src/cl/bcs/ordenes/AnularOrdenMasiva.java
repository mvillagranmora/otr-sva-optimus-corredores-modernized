package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
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
import cl.bcs.ordenes.CrearOrdenes.UltimoCambioFechaPauta;
import cl.bcs.ordenes.pojo.Asignacion;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.OrdenModificada;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosSalidaServicio;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularOrdenMasiva implements JavaCall {
	private static final Logger logger = LogManager.getLogger(AnularOrdenMasiva.class);
	private final String SQL_CREAR_ENTRADA_TEMPORAL = "create local temporary table #entrada_anular_ordenes (\"IdOrden\" BIGINT , \"MotivoAnulacion\" nvarchar(5000),\"FechaHoraAnulacion\" TIMESTAMP)";
	private final String SQL_INSERT_ENTRADA_TEMPORAL = "insert into #entrada_anular_ordenes values(:IdOrden,:MotivoAnulacion,:FechaHoraAnulacion);";
	private final String SQL_ELIMINAR_ENTRADA_TEMPORAL = "DROP TABLE #entrada_anular_ordenes;";
	private final String SQL_SELECT_ORDENES_MODIFICADAS = loadResourceToString("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
	public String SQL_ANULAR_ORDENS_Call = loadResourceToString("cl/bcs/ordenes/sql/AnularOrdenMasiva_Call.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/AnularOrdenMasiva_SelectOrdenes.sql");
	private final String SQL_SELECT_ORDENES_HORARIOS = loadResourceToString("cl/bcs/ordenes/sql/AnularOrdenMasiva_SelectOrdenesHorarios.sql");
	
	public static class EntradaAnularOrdenMasiva {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_RequiereNotificacion = "S";
		public List<AnularOrden> p_ordenes;
		
		public EntradaAnularOrdenMasiva() {
			this.p_ordenes = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AnularOrden {
		public Long IdOrden;
		public String MotivoAnulacion;
		public java.sql.Timestamp FechaHoraAnulacion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnularOrdenProceso {
		public Long IdOrden;
		public String MotivoAnulacion;
		public java.sql.Timestamp FechaAnulacion;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaAnularOrdenMasiva {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		public List<InformacionDetalle> P_OUT_INFORMACIONDETALLE;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		
		public SalidaAnularOrdenMasiva() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_INFORMACIONCONTADOR = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_INFORMACIONDETALLE = new ArrayList<>();
			P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AnularOrdenHorario{
		public Long IdOrden;
		public String HorarioPreMarketInicio;
		public String HorarioMarketFin;
		public String AplazaOrdenPorHorarioNegociacion;
		public String ValidaHorario;
		public String IdTipoCanalDistribucion;
		public java.sql.Timestamp FechaHoraAnulacion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public void calculaFechaAnulacion(Connection con,EntradaAnularOrdenMasiva in) throws Exception {
		logger.info("INICIO :: calculaFechaAnulacion");
		HashMap<String,Object> paramsInOrdenes = new HashMap<>();
		CrearOrdenes crearOrdenes = new CrearOrdenes();
		SQL _sql = new SQL();
		logger.info("INICIO :: SQL_SELECT_ORDENES_HORARIOS");
		List<AnularOrdenHorario> horarioOrdenes = _sql.query(con, SQL_SELECT_ORDENES_HORARIOS, paramsInOrdenes, AnularOrdenHorario.class);
		logger.info("FIN :: SQL_SELECT_ORDENES_HORARIOS");
		HashMap<Long,Object> horarioOrdenesHash = Utils.List2Hash(horarioOrdenes,"IdOrden");
		Date fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate("0001-01-01");
		java.sql.Timestamp fechaUltimaEjecucionCambioFecha = Fecha.toTimeStamp("0001-01-01 00:00:00");
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
		Timestamp fechaHoraReal= Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa));
		java.sql.Date fechaReal = Fecha.toDate(fechaHoraReal.toString().split(" ")[0]);
		fechaUltimaEjecucionCambioFecha = Fecha.toLocalTimeStamp(crearOrdenes.obtieneUltimoProcesoCambioFecha(con));
		fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate(fechaUltimaEjecucionCambioFecha.toString().split(" ")[0]);
		Date fechaSistemaLocal = Fecha.toDate("0001-01-01");
		fechaSistemaLocal= Fecha.toDate(FechaDB.ObtenerFechaHoraSistemaLocal(con,in.p_IdEmpresa).toString().split(" ")[0]);
		String utcStringTime = Fecha.toStringTime(fechaHoraReal);
		LocalTime lt = LocalTime.parse(utcStringTime);
		LocalTime inicioDia = LocalTime.parse("00:00:00");
		LocalTime finDia = LocalTime.parse("23:59:59");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Timestamp fechaHoraSistemaTS = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,in.p_IdEmpresa));
		String fechaHoraSistema_ = Fecha.toStringDatetime(fechaHoraSistemaTS);
		
		//Obtiene dia siguiente habil segun la fecha sistema
		//FIN
		ObtenerDiaSiguiente sgte = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente entradaDiaSgte = new InObtenerDiaSiguiente();
		entradaDiaSgte.p_diaHabil = "S";
		entradaDiaSgte.p_idPais = empresa.IdPais;
		entradaDiaSgte.p_soloCaeDiaHabil = "S";
		FechasIn e = new FechasIn();
		e.fechaInicial = fechaSistema;
		e.numeroDias = 1L;
		e.pais = empresa.IdPais;
		entradaDiaSgte.p_fechas.add(e );
		logger.info("INICIO :: ObtenerDiaSiguiente");
		OutObtenerDiaSiguiente salidaDiaSgte = sgte.logica(con, entradaDiaSgte );
		logger.info("FIN :: ObtenerDiaSiguiente");
		String diaSiguiente_FechaSistema = salidaDiaSgte.p_out_fechas.get(0).fechaCalculada;
		//FIN
		
		Boolean esDiaHabil_FechaSistema = crearOrdenes.esDiaHabil(fechaSistema,in.p_IdEmpresa, con);
		Boolean esDiaHabil_FechaReal= crearOrdenes.esDiaHabil(fechaReal.toString(),in.p_IdEmpresa, con);
		
		for(AnularOrden orden : in.p_ordenes) {
			AnularOrdenHorario info = (AnularOrdenHorario) horarioOrdenesHash.get(orden.IdOrden);
			orden.FechaHoraAnulacion = Fecha.toDateTime(fechaHoraSistema_);
			if(info != null) {
				if(info.HorarioPreMarketInicio != null) {
					
				
					LocalTime premkInicio = LocalTime.parse(info.HorarioPreMarketInicio);
					LocalTime mkFin = LocalTime.parse(info.HorarioMarketFin);
					
					//inicio
					
								if (info.ValidaHorario.equalsIgnoreCase("S")) {
									if (!esDiaHabil_FechaReal) {
										if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
											//Valida si la fecha de sistema es dia habil, por pauta de fin de mes que asigna dia inhabil al sistema
											if(esDiaHabil_FechaSistema) {
												//la fecha de sistema es habil, se mantiene la fecha sistema creacion
												orden.FechaHoraAnulacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
												
											}
											else {
												orden.FechaHoraAnulacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);
												
											}
										}
									}
									//LOS 3 CASOS SIGUIENTES SON EN DIAS HABILES
									//CASO POST PAUTA, BUSCA LA ULTIMA FECHA DE EJECUCION DE PAUTA Y SI ES IGUAL A LA FECHA REAL QUIERE DECIR QUE SE EJECUTO LA PAUTA Y DEBE QUEDAR PARA MISMA FECHA SISTEMA + HORA 9:01
									else if (fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) ==  0) {
										if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
											//se mantiene la fecha sistema creacion
											orden.FechaHoraAnulacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
											
										}
									}
									//ORDEN ENTRE 00 Y 09 (LOCAL) MISMA FECHA SISTEMA + HORA 9:01 (HORARIO NO VALIDO) PREVIA A APERTURA DE MERCADO
									else if (lt.compareTo(inicioDia) >= 1 && lt.compareTo(premkInicio) <= -1) {
										if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
											orden.FechaHoraAnulacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
											
											//se mantiene la fecha sistema creacion
										}
									}
									//ORDEN ENTRE 16 Y 23 59 (LOCAL) MISMA FECHA SISTEMA, DEBE QUEDAR FECHA SISTEMA + 1 DIA HABIL + HORA 9:01  (HORARIO NO VALIDO), ESTO ES POST CIERRE MERCADO Y PREVIO A PAUTA
									else if (lt.compareTo(mkFin) >= 1 && lt.compareTo(finDia) <= -1) {
										if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
											//se le suma 1 dia habil a la fecha sistema
											orden.FechaHoraAnulacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);	
											
										}
									}
							}	
							
			//fin
				}
				
			}
		}
		
		
		logger.info("FIN :: calculaFechaAnulacion");
	}
	public SalidaAnularOrdenMasiva logica(Connection con, EntradaAnularOrdenMasiva in) throws Exception {
		SalidaAnularOrdenMasiva salida = new SalidaAnularOrdenMasiva();
		SQL sql = new SQL();
		SQL.exec(con, SQL_CREAR_ENTRADA_TEMPORAL);
		sql.batch(con, SQL_INSERT_ENTRADA_TEMPORAL, in.p_ordenes, AnularOrden.class);
		calculaFechaAnulacion(con,in);
		SQL.exec(con, SQL_ELIMINAR_ENTRADA_TEMPORAL);
		
		
		
		//si es peru, firmar automaticamente el documento
		HashMap<String, Object> paramsIn = new HashMap<String, Object>();
		paramsIn.put("p_IdEmpresa", in.p_IdEmpresa);
		paramsIn.put("p_IdUsuario", in.p_IdUsuario);
		paramsIn.put("p_IdModulo", in.p_IdModulo);
		SQL.exec(con, SQL_CREAR_ENTRADA_TEMPORAL);
		sql.batch(con, SQL_INSERT_ENTRADA_TEMPORAL, in.p_ordenes, AnularOrden.class);

		//agregar datos a la tabla de entrada #entrada_anular_ordenes
		HashMap<String, Class> paramsOut = new HashMap<String, Class>();
		paramsOut.put("p_out_InformacionContador", Long.class);
		paramsOut.put("p_out_InformacionDetalle", InformacionDetalle.class);
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
		HashMap<String, Object> salidaCall = sql.call(con, SQL_ANULAR_ORDENS_Call, paramsIn, paramsOut);
		salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) salidaCall.get("p_out_AdvertenciasDetalle");
		salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) salidaCall.get("p_out_ErroresDetalle");
		salida.P_OUT_INFORMACIONDETALLE = (List<InformacionDetalle>) salidaCall.get("p_out_InformacionDetalle");
		salida.P_OUT_INFORMACIONCONTADOR = (long)salida.P_OUT_INFORMACIONDETALLE.size();
		salida.P_OUT_ADVERTENCIASCONTADOR = (long)salida.P_OUT_ADVERTENCIASDETALLE.size();

		//notificar
		try {
			if(salida.P_OUT_ERRORESDETALLE!=null && salida.P_OUT_ERRORESDETALLE.size()==0 && in.p_RequiereNotificacion.equalsIgnoreCase("S")) {
				NotificarEstadoOrden notificar = new NotificarEstadoOrden();
				NotificarEstadoOrden.NotificarEstadoOrden_Entrada notificarEntrada = new NotificarEstadoOrden.NotificarEstadoOrden_Entrada(); 
				notificarEntrada.p_IdEmpresa = in.p_IdEmpresa;
				notificarEntrada.p_IdModulo = in.p_IdModulo;
				notificarEntrada.p_IdUsuario = in.p_IdUsuario;
				notificarEntrada.p_Ordenes = new ArrayList<NotificarEstadoOrden.Orden>();
				for(AnularOrden ord : in.p_ordenes) {
					notificarEntrada.p_Ordenes.add(new NotificarEstadoOrden.Orden(ord.IdOrden));
				}
				notificar.logica(con, notificarEntrada);
			}					
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		HashMap<String,Object> paramsInOrdenes = new HashMap<>();
		for (ErrorDetalle errorDetalle: salida.P_OUT_ERRORESDETALLE) {
			if ("ERROR_ANULAR_ORDEN_ESTADO_OFERTADA".equalsIgnoreCase(errorDetalle.Codigo)) {
				paramsInOrdenes.put("IdOrden", errorDetalle.IdInterno);
			}
		}
		for (InformacionDetalle infoDetalle: salida.P_OUT_INFORMACIONDETALLE){
			if("SOLICITUD_PROCESADA".equalsIgnoreCase(infoDetalle.Codigo)){
				paramsInOrdenes.putIfAbsent("IdOrden", infoDetalle.IdInterno);

			}
		}
		if (paramsInOrdenes.size() > 0) {
			List<ObjOrdenesEntrada> ordenes = sql.query(con, SQL_SELECT_ORDENES, paramsInOrdenes, ObjOrdenesEntrada.class);
			if (ordenes.size() > 0) {
				CrearOrdenesEntrada entradaCrearOrdenes = new CrearOrdenesEntrada();
				entradaCrearOrdenes.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearOrdenes.p_IdUsuario = in.p_IdUsuario;
				entradaCrearOrdenes.p_IdModulo = in.p_IdModulo;
				entradaCrearOrdenes.p_simularOperacion = "N";
				entradaCrearOrdenes.p_Ordenes.addAll(ordenes);
				CrearOrdenes crearOrdenes = new CrearOrdenes();
				CrearOrdenesSalida salidaCrearOrdenes = crearOrdenes.logica(con, entradaCrearOrdenes);
				List<AnularOrden> anularOrdenCreada = new ArrayList<AnularOrden>();
				AnularOrden ordenCreada;
				for (CrearOrdenes.ObjRESULTADO resultado : salidaCrearOrdenes.P_OUT_RESULTADO) {
					ordenCreada = new AnularOrden();
					ordenCreada.IdOrden = resultado.IdOrden;
					ordenCreada.MotivoAnulacion = "Anula Saldo Orden " + resultado.IdInterno;
					anularOrdenCreada.add(ordenCreada);
				}
				if (anularOrdenCreada.size() > 0) {
					SQL.exec(con, SQL_ELIMINAR_ENTRADA_TEMPORAL);
					EntradaAnularOrdenMasiva anularOrdenMasiva = new EntradaAnularOrdenMasiva();
					anularOrdenMasiva.p_IdEmpresa = in.p_IdEmpresa;
					anularOrdenMasiva.p_IdUsuario = in.p_IdUsuario;
					anularOrdenMasiva.p_IdModulo = in.p_IdModulo;
					anularOrdenMasiva.p_RequiereNotificacion = "S";
					anularOrdenMasiva.p_ordenes.addAll(anularOrdenCreada);
					SalidaAnularOrdenMasiva salidaAnularOrdenMasiva = logica(con, anularOrdenMasiva);
					SQL.exec(con, SQL_CREAR_ENTRADA_TEMPORAL);
					
					if (salidaAnularOrdenMasiva.P_OUT_ERRORESCONTADOR == 0) {
						salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaAnularOrdenMasiva.P_OUT_ADVERTENCIASDETALLE);
						salida.P_OUT_ERRORESDETALLE.addAll(salidaAnularOrdenMasiva.P_OUT_ERRORESDETALLE);
						salida.P_OUT_INFORMACIONDETALLE.addAll(salidaAnularOrdenMasiva.P_OUT_INFORMACIONDETALLE);
						salida.P_OUT_INFORMACIONCONTADOR = salidaAnularOrdenMasiva.P_OUT_INFORMACIONCONTADOR;
						salida.P_OUT_ADVERTENCIASCONTADOR = salidaAnularOrdenMasiva.P_OUT_ADVERTENCIASCONTADOR;
					}
				}
			}
		}
		
		//Notificacion OpenWorkPlace
		String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
		if("S".equalsIgnoreCase(notificaOWP)){
			HashMap<String,Object> parametro = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<>();
		
			for (AnularOrden ord : in.p_ordenes) {
				idsOrden.add(ord.IdOrden);
			}
			
			parametro.put("p_IdOrdenes", idsOrden);
			List<OrdenModificada> listaOrdenesModificadas  = sql.query(con, SQL_SELECT_ORDENES_MODIFICADAS, parametro, OrdenModificada.class);
			
			NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();
			List<NotificarEstadoOrdenes_Entrada> listOrdenNotiOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
			
			for (OrdenModificada ord : listaOrdenesModificadas) {
				if("RV".equalsIgnoreCase(ord.IdClasificacionTipoInstrumento)){
					NotificarEstadoOrdenes_Entrada entrada = new NotificarEstadoOrdenes_Entrada();
					entrada.IdEmpresa = in.p_IdEmpresa;
					entrada.IdUsuario = in.p_IdUsuario;
					entrada.Solsec = ord.IdOrden;
					entrada.NTitulos = ord.Cantidad;
					entrada.Precio = ord.PrecioOrden;
					entrada.Fecha = ord.FechaOperacion;
					entrada.Estado = ord.IdEstadoOrden;
					entrada.Comision = ord.Comision;
					entrada.Gastos = ord.Gasto;
					entrada.Mercado = "A";
					entrada.Divisa = ord.IdMonedaOrden;
					entrada.TipoCambio = ord.TipoCambio;
					entrada.SolsecAsoc = "";
					entrada.Nemo = ord.Nemotecnico;
					entrada.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
					listOrdenNotiOWP.add(entrada);
				}
			}
			
			if(listOrdenNotiOWP.size() > 0){
				servicioNotOwp.go(con, listOrdenNotiOWP);
			}
		}
		
		SQL.exec(con, SQL_ELIMINAR_ENTRADA_TEMPORAL);
		
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAnularOrdenMasiva in = gson.fromJson(jin.toString(), EntradaAnularOrdenMasiva.class);
		SalidaAnularOrdenMasiva out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws ConfigurationException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonParcial = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 1,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_ordenes\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdOrden\": 257393,\r\n"
					+ "            \"MotivoAnulacion\": \"\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			String jsonTotal = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"ORD\",\"p_ordenes\":[{\"IdOrden\":1090813,\"MotivoAnulacion\":\"\"}]}";
			
			EntradaAnularOrdenMasiva Objin = gson.fromJson(jsonParcial, EntradaAnularOrdenMasiva.class);

			AnularOrdenMasiva demo = new AnularOrdenMasiva();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaAnularOrdenMasiva ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
