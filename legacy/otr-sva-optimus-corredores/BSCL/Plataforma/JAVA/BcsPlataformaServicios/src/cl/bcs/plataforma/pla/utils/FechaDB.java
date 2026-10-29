package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import cl.bcs.utils.Fecha;

public class FechaDB {
	private static Timestamp	cacheCurrentUTC=null;
	private static long 		ultTimeMillis=0;
		
	/**
	 *  Obtiene la fecha y hora como string en formato UTC
	 *  Importante: La parte de la Fecha, usa la fecha Sistema configurada en plataforma
	 * @return
	 * @throws Exception 
	 */
	
	private static void updateCache(Connection con) throws Exception {
		if (System.currentTimeMillis()-ultTimeMillis>3600000) {
			cacheCurrentUTC=null;			
			try (PreparedStatement st=con.prepareStatement("SELECT CURRENT_UTCTIMESTAMP as UTC  FROM DUMMY");) {
				try  (ResultSet rs=st.executeQuery();) {
					if (rs.next()) {
						cacheCurrentUTC=rs.getTimestamp("UTC");
					}
				}
			}
			ultTimeMillis=System.currentTimeMillis();
		}
		if (cacheCurrentUTC==null) throw new Exception("No se puede obtener cache UTC");
	}
	
	public static String ObtenerFechaHoraSistemaUTC(Connection con,long idEmpresa) throws Exception {
		String FechaSistema=ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		updateCache(con);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		Timestamp FechaHoraSistemaUTC=Fecha.addMilliseconds(cacheCurrentUTC, diffCache);
		return FechaSistema+" "+Fecha.toStringTime(FechaHoraSistemaUTC);
	}
	
	public static Timestamp ObtenerFechaHoraUTCTs(Connection con) throws Exception {
		updateCache(con);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		return Fecha.addMilliseconds(cacheCurrentUTC, diffCache);
		
	}
	
	public static String ObtenerHoraUTC(Connection con) throws Exception {
		updateCache(con);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		return Fecha.toStringTime(Fecha.addMilliseconds(cacheCurrentUTC, diffCache));
		
	}
	
	/**
	 *  Obtiene la fecha y hora como string en formato LOCAL
	 *  Importante: La parte de la Fecha, usa la fecha Sistema configurada en plataforma
	 *  Importante: Recordar que las fechas se guardan en UTC en la BD.
	 * @return
	 * @throws Exception 
	 */
	public static String ObtenerFechaHoraSistemaLocal(Connection con,long idEmpresa) throws Exception {
		String FechaSistema=ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		if (System.currentTimeMillis()-ultTimeMillis>3600000) {
			cacheCurrentUTC=null;			
			try (PreparedStatement st=con.prepareStatement("SELECT CURRENT_UTCTIMESTAMP as UTC  FROM DUMMY");) {
				try  (ResultSet rs=st.executeQuery();) {
					if (rs.next()) {
						cacheCurrentUTC=rs.getTimestamp("UTC");
					}
				}
			}
			ultTimeMillis=System.currentTimeMillis();
		}
		if (cacheCurrentUTC==null) throw new Exception("No se puede obtener cache UTC");
		int offsetSecs=ObtenerSegundosZonaHoraria(con,idEmpresa);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		Timestamp FechaHoraSistemaUTC=Fecha.addMilliseconds(cacheCurrentUTC, diffCache);
		Timestamp FechaHoraSistemaLocal=Fecha.addSeconds(FechaHoraSistemaUTC, offsetSecs);
		return FechaSistema+" "+Fecha.toStringTime(FechaHoraSistemaLocal);
	}
	
	/**
	 *  Obtiene la hora como string en formato LOCAL
	 *  Importante: Recordar que las horas  se guardan en UTC en la BD.
	 * @return
	 * @throws Exception 
	 */
	public static String ObtenerHoraSistemaLocal(Connection con,long idEmpresa) throws Exception {
		String FechaSistema=ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		if (System.currentTimeMillis()-ultTimeMillis>3600000) {
			cacheCurrentUTC=null;			
			try (PreparedStatement st=con.prepareStatement("SELECT CURRENT_UTCTIMESTAMP as UTC  FROM DUMMY");) {
				try  (ResultSet rs=st.executeQuery();) {
					if (rs.next()) {
						cacheCurrentUTC=rs.getTimestamp("UTC");
					}
				}
			}
			ultTimeMillis=System.currentTimeMillis();
		}
		if (cacheCurrentUTC==null) throw new Exception("No se puede obtener cache UTC");
		int offsetSecs=ObtenerSegundosZonaHoraria(con,idEmpresa);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		Timestamp FechaHoraSistemaUTC=Fecha.addMilliseconds(cacheCurrentUTC, diffCache);
		Timestamp FechaHoraSistemaLocal=Fecha.addSeconds(FechaHoraSistemaUTC, offsetSecs);
		return Fecha.toStringTime(FechaHoraSistemaLocal);
	}
	

	/**
	 *  Obtiene la fecha y hora Timestamp string en formato UTC
	 *  Importante: La parte de la Fecha, usa la fecha Sistema
	 *  Es el quivalente a FechaSistema , valor como queda el negocio registrado. 
	 * @param con
	 * @param idEmpresa
	 * @return
	 * @throws Exception
	 */
	public static Timestamp ObtenerFechaHoraSistemaTs(Connection con,long idEmpresa) throws Exception {
		String fecha=ObtenerFechaHoraSistemaUTC(con,idEmpresa);
		return Fecha.toDateTime(fecha);
	}
	
	
	/**
	 *  Obtiene la fecha y hora Timestamp string en formato UTC
	 *  Importante: La parte de la Fecha, usa la fecha de BD 
	 *  Es el quivalente a FechaRegistro , valor como queda el log o fecha  Real. 
	 * @param con
	 * @param idEmpresa
	 * @return
	 * @throws Exception
	 */
	public static Timestamp ObtenerFechaHoraTs(Connection con,long idEmpresa) throws Exception {
		ObtenerFechaHoraSistemaUTC(con,idEmpresa);
		return cacheCurrentUTC;
	}	
	
	
	/**
	 * Obtiene los segundos de la zona horaria configurados para el sistema
	 * @param con
	 * @param idEmpresa
	 * @return
	 * @throws Exception
	 */
	public static int ObtenerSegundosZonaHoraria(Connection con,long idEmpresa)  throws Exception {
		return (int)ConfiguracionSistema.getParametroConfiguracionLong(con,idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
	}
}
