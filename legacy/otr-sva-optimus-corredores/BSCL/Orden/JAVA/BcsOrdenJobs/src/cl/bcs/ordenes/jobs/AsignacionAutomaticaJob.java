package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AsignacionAutomaticaJob extends BcsJob implements JavaCall {
	
	AtomicBoolean running=new AtomicBoolean(false);
	String IdTipoPrograma = "ASIGNACION_AUTOMATICA";
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		go(false, conn);
		return true;
	}
	
	private final String SQL_EJECUTAR_ASIGNACION_POR_HORA = loadResourceToString("cl/bcs/ordenes/jobs/sql/AsignacionAutomaticaJob_EjecutarAsignacionPorHora.sql");
	private final String SQL_CONSULTAR_ESTADO_MODULO = loadResourceToString("cl/bcs/ordenes/jobs/sql/AsignacionAutomaticaJob_ConsultarEstadoModulo.sql");
	private final String SQL_CONSULTAR_LIBROS_ASIGNACION_AUTOMATICA = loadResourceToString("cl/bcs/ordenes/jobs/sql/AsignacionAutomaticaJob_ConsultarLibrosAsignacionAutomatica.sql");
	
	public static class EjecutarAsignacionPorHora_Resultado {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdOrden;
		public Long IdTransaccion;
		public String FolioTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);
			
		}
	}
	
	public static class EjecutarAsignacionPorHora_Cobros {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdCobroAsignacion;
		public String CodigoCobroClasificacion;
		public String IdCobro;
		public String IdMonedaCobro;
		public BigDecimal ValorCobroPerfil;
		public String FormaCobro;
		public BigDecimal MontoMinimo;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);
			
		}
	}
	
	public static class EstadoModulo {
		public String ModuloAbierto;
	}
	
	public static class LibroOrdenes {
		public Long IdLibroOrdenes;
		public String NombreLibroOrdenes;
		public String IdTipoAsignacion;
		public String IdEstado;
		
	}
	
	public static class Orden {
		public Timestamp FechaOperacionOrden;
		public Long IdOrden;
		public Long IdLibroOrdenes;
		public BigDecimal CantidadDisponibleOrden;
		public BigDecimal PrecioOrden;
		public String IdClasificacionTipoInstrumento;
		public String IdCondicionDeLiquidacion;
		public String IdTipoInstrumento;
		public String IdBolsa;
		public String TipoPrecio;
		public String NombreLibroOrdenes;
		public String IdMonedaOrden;
		public String Instrumento;
		
		
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public static class Transaccion {
		public Timestamp FechaOperacionTransaccion;
		public Long IdTransaccion;
		public Long IdLibroOrdenes;
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal ValorRescateDisponibleTransaccion;
		public BigDecimal PrecioTransaccion;
		public String FolioTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String IdCondicionDeLiquidacion;
		public String IdTipoInstrumento;
		public String IdBolsa;
		public String NombreLibroOrdenes;
		public String IdMonedaInstrumento;
		public String Instrumento;
		
		
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	
	public void go(boolean debugMode, Connection conn) throws InterruptedException, Throwable {
		SQL sql = new SQL();
		running.set(true);
		
		Long IdEmpresa = null;
		Long IdUsuario = null;
		String IdModulo = "ORD";
		int OffsetAsignacion = -10*60;		//en segundos
		int LoopTime = 5*60*1000;			//en segundos
		Long IdPrograma;
		
		if (!debugMode) {
			IdPrograma = this.getJobId();
			IdEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
			IdUsuario = Long.parseLong(super.getParam("IdUsuario"));
			OffsetAsignacion = Integer.parseInt(super.getParam("OffsetAsignacion")) * 60;
			LoopTime = Integer.parseInt(super.getParam("LoopTime")) * 60 * 1000;
		}
		else {
			conn = getDBConnection();
			IdEmpresa = 1L;
			IdPrograma = -1L;
			IdUsuario = 4L;
			OffsetAsignacion = 0;
			LoopTime = 5000;
		}
		
		//VALIDACION MODULO ABIERTO
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		params1.put("p_IdEmpresa", IdEmpresa);
		List<EstadoModulo> estadoLibro = sql.query(conn, SQL_CONSULTAR_ESTADO_MODULO,params1,EstadoModulo.class);
		
		if (estadoLibro.size() == 0 || estadoLibro.get(0).ModuloAbierto.equalsIgnoreCase("N")) {
			Thread.sleep(LoopTime);
			running.set(false);
			return;
		}
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,IdEmpresa);
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dfFechaHora.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dfHora = new SimpleDateFormat("HH:mm:ss");
		dfHora.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dfFechaHoraLocal = new SimpleDateFormat("HH:mm:ss",new Locale("es","CL"));
		Date dateFechaHoraSistema = dfFechaHora.parse(fechaHoraSistema);
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(dateFechaHoraSistema);
		cal.add(Calendar.SECOND, OffsetAsignacion);
		Date dateFechaHoraAjustada = cal.getTime();
		
		//RECUPERAR LIBROS CON ASIGNACION AUTOMATICA
		HashMap<String,Object> params2 = new HashMap<String,Object>();
		params2.put("p_IdEmpresa", IdEmpresa);
		List<LibroOrdenes> libros = sql.query(conn, SQL_CONSULTAR_LIBROS_ASIGNACION_AUTOMATICA,params2,LibroOrdenes.class);
		
		for (LibroOrdenes lib : libros) {
			try {
				
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "PROCESANDO LIBRO: " + lib.NombreLibroOrdenes +",BUSCANDO ELEMENTOS HASTA " + dfFechaHoraLocal.format(dateFechaHoraAjustada),true);
			
			HashMap<String,Object> parmsIN=new HashMap<String,Object>();
			parmsIN.put("p_IdEmpresa", IdEmpresa);	
			parmsIN.put("p_IdUsuario", IdUsuario);
			parmsIN.put("p_IdModulo", IdModulo);
			parmsIN.put("p_simularOperacion", "S");
			parmsIN.put("p_UsaReglasNegocio", "S");
			parmsIN.put("p_HoraHasta", dfHora.format(dateFechaHoraAjustada));
			parmsIN.put("p_IdTipoAsignacion", lib.IdTipoAsignacion);
			parmsIN.put("p_IdLibroOrdenes", lib.IdLibroOrdenes);
			
			HashMap<String,Class> parmsOUT=new HashMap<String,Class>();
			parmsOUT.put("p_out_Resultado", EjecutarAsignacionPorHora_Resultado.class);
			parmsOUT.put("p_out_Transacciones", Transaccion.class);
			parmsOUT.put("p_out_Ordenes", Orden.class);
			parmsOUT.put("p_out_cobros", EjecutarAsignacionPorHora_Cobros.class);
			parmsOUT.put("p_out_InformacionContador", Long.class);
			parmsOUT.put("p_out_InformacionDetalle", InformacionDetalle.class);
			parmsOUT.put("p_out_AdvertenciasContador", Long.class);
			parmsOUT.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
			parmsOUT.put("p_out_ErroresContador", Long.class);
			parmsOUT.put("p_out_ErroresDetalle", ErrorDetalle.class);
			
			HashMap<String,Object> respuesta=sql.call(conn, SQL_EJECUTAR_ASIGNACION_POR_HORA, parmsIN, parmsOUT);
			
			List<EjecutarAsignacionPorHora_Resultado> c1 = (List<EjecutarAsignacionPorHora_Resultado>) respuesta.get(("p_out_Resultado"));
			List<EjecutarAsignacionPorHora_Cobros> c2 = (List<EjecutarAsignacionPorHora_Cobros>) respuesta.get(("p_out_cobros"));
			List<ErrorDetalle> c3 = (List<ErrorDetalle>) respuesta.get(("p_out_ErroresDetalle"));
			List<AdvertenciaDetalle> c4 = (List<AdvertenciaDetalle>) respuesta.get(("p_out_AdvertenciasDetalle"));
			List<InformacionDetalle> c5 = (List<InformacionDetalle>) respuesta.get(("p_out_InformacionDetalle"));
			List<Transaccion> c6 = (List<Transaccion>) respuesta.get(("p_out_Transacciones"));
			List<Orden> c7 = (List<Orden>) respuesta.get(("p_out_Ordenes"));
			
			//se encontraron tantos palos
			String palos = "";
			for(Transaccion t: c6) {
				palos+="(ID:"+t.IdTransaccion + ",FOLIO:"+t.FolioTransaccion+")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "TRANSACCIONES : " + c6.size() + " " + palos,true);
			
			String ordenes = "";
			for(Orden t: c7) {
				ordenes+="(ID:"+t.IdOrden + ")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "ORDENES : " + c7.size() + " " + ordenes,true);
			
			String asignaciones = "";
			for(EjecutarAsignacionPorHora_Resultado t: c1) {
				asignaciones+="(ID:"+t.IdAsignacion + ")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "ASIGNACIONES : " + c1.size() + " " + asignaciones,true);
			
			String errores = "";
			for(ErrorDetalle t: c3) {
				errores+="(ERR:"+t.IdInterno + " " + t.Descripcion + ")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "ERRORES : " + c3.size() + " " + errores,true);
			
			String advertencias = "";
			for(AdvertenciaDetalle t: c4) {
				advertencias+="(ADV:"+t.IdInterno + " " + t.Descripcion + ")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "ADVERTENCIAS : " + c4.size() + " " + errores,true);
			
			String informaciones = "";
			for(InformacionDetalle t: c5) {
				informaciones+="(INF:"+t.IdInterno + " " + t.Descripcion + ")";
			}
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "INFORMACIONES : " + c5.size() + " " + informaciones,true);
			
			
			AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, "FIN LIBRO: " + lib.NombreLibroOrdenes,true);
			

			}
			catch(Exception e) {
				AgregarLogPrograma.logica(conn, IdPrograma, IdTipoPrograma, e.getMessage(),true);
			}
			
		}
		conn.commit();
		Thread.sleep(LoopTime);
		running.set(false);
		return;
	}
	
	@Override
    public void onError(Throwable t) {
        System.out.println(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	private static final String DB_DRIVER = "com.sap.db.jdbc.Driver";
	private static final String DB_CONNECTION_QA = "jdbc:sap://161.238.34.246:30315/?autocommit=false&currentschema=BSCL";
	private static final String DB_CONNECTION_DESA = "jdbc:sap://161.238.34.246:30215/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_QA = "MDCLIENT";
	private static final String DB_PASSWORD_QA = "B0lsa1234";
	private static final String DB_USER_DESA = "MDCLIENT";
	private static final String DB_PASSWORD_DESA = "Bolsa123";

	private static Connection getDBConnection() {
	    Connection dbConnection = null;
	    try {
	      Class.forName(DB_DRIVER);
	    } catch (ClassNotFoundException e) {
	      System.out.println(e.getMessage());
	    }
	    try {
	    	 dbConnection = DriverManager.getConnection(DB_CONNECTION_DESA, DB_USER_DESA,DB_PASSWORD_DESA);
	    	 //dbConnection = DriverManager.getConnection(DB_CONNECTION_QA, DB_USER_QA,DB_PASSWORD_QA);
	      return dbConnection;
	    } catch (SQLException e) {
	      System.out.println(e.getMessage());
	    }
	    return dbConnection;
	  }
	
	public static void main(String[] args){
	
		
		while(true) {
		AsignacionAutomaticaJob p = new AsignacionAutomaticaJob();
		try {
			p.go(true, getDBConnection());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
