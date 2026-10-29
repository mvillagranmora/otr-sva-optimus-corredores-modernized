package cl.bcs.scheduler.sql;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.scheduler.pojo.Agenda;
import cl.bcs.scheduler.pojo.Programa;
import cl.bcs.scheduler.tools.BcsJobManagerSingleBean;
import cl.bcs.utils.Utils;

 
public class TransactionManager {

	private static final Logger logger = LogManager.getLogger(BcsJobManagerSingleBean.class);
	
	public List<Programa> readProgramm(DataSource datasource, String node,
			String ptaskType) throws SQLException {
		List<Programa> programalist = new ArrayList<Programa>();
		String jobtype = "";
		if (ptaskType.replace("-", "").trim().length()>0)
		{
			jobtype = " AND P.\"IdTipoPrograma\" = ?";
		}
 		final String block = "SELECT P.\"IdPrograma\",P.\"idEstado\",P.\"IdTipoPrograma\",P.\"Intento\",TP.\"TipoClasePrograma\",TP.\"NombreClasePrograma\",TP.\"MaximoIntentos\",P.\"Bloqueo\",COALESCE(AGENDA.\"Minuto\",'0') as \"Minuto\" "
 				+ "FROM  \"BSCL.Plataforma.BD::BAT.Programa\" as P "
 				+ "INNER JOIN \"BSCL.Plataforma.BD::BAT.TipoPrograma\" as TP on P.\"IdTipoPrograma\"=TP.\"IdTipoPrograma\""
 				+ "LEFT JOIN \"BSCL.Plataforma.BD::BAT.Agenda\" as AGENDA on AGENDA.\"IdAgenda\"= P.\"IdAgenda\" "
 				+ " WHERE P.\"Bloqueo\"  = 'N' "
				+ jobtype
				+ "  and P.\"Nodo\"  = ? and P.\"idEstado\" = ? and  P.\"Intento\" < TP.\"MaximoIntentos\"  and P.\"fechaEjecucion\" between (to_seconddate(CURRENT_DATE  || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS')) and  CURRENT_UTCTIMESTAMP "
				+ " "; //+ " LIMIT 1 FOR UPDATE OF P.\"Bloqueo\" "; //ESTA GENERANDO LOG TRAN
 		try (Connection con = datasource.getConnection();
 			 PreparedStatement stB = con.prepareStatement(block);
				) {
			
			if (ptaskType.replace("-", "").trim().length() > 0) {
				stB.setString(1, ptaskType);
				stB.setString(2, node.replace("-", ""));
				stB.setString(3, "PEN");
				
			}else {
				stB.setString(1, node.replace("-", ""));
				stB.setString(2, "PEN");
				
			}
 			try (ResultSet rsB = stB.executeQuery();) { 
				while (rsB.next()) {					
					Integer IdPrograma = Integer.parseInt(rsB.getString("IdPrograma"));
					logger.info("NEW JOB #" + IdPrograma + " $"+ rsB.getString("IdTipoPrograma"));
					// Bloquea Registro y Libera
					String update = "update p set p.\"Bloqueo\" = ? from bscl.\"BSCL.Plataforma.BD::BAT.Programa\" as p where p.\"IdPrograma\" = ?";
					PreparedStatement stC = con.prepareStatement(update);
					stC.setString(1,"S");
					stC.setInt(2, IdPrograma);
					stC.executeUpdate();
					con.commit();
					// Procesa Tarea
					Programa programa = new Programa();
					programa.setIdEstado(rsB.getString("idEstado"));
					programa.setMinuto(rsB.getString("Minuto"));
					programa.setNombreClasePrograma(rsB.getString("NombreClasePrograma"));
					programa.setIdTipoPrograma(rsB.getString("IdTipoPrograma"));
					programa.setMaximoIntentos(Integer.parseInt(rsB.getString("MaximoIntentos")));
					programa.setIntento(Integer.parseInt(rsB.getString("Intento")));
					programa.setIdPrograma(IdPrograma);
					programa.setTipoClasePrograma(rsB.getString("TipoClasePrograma"));
					programalist.add(programa);					
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
		
		return programalist;
	}

	public List<Agenda> readDiary(DataSource datasource) throws SQLException {
		final List<Agenda> agendalist = new ArrayList<Agenda>();
		final String query = "select distinct  \"UltimaFechaDeEjecucion\",\"SiguienteFechaDeEjecucion\",\"IdAgenda\",\"habilitado\",\"agenda\".\"IdTipoPrograma\",\"TipoClasePrograma\",\"NombreClasePrograma\",\"EsDemonio\" from \"BSCL.Plataforma.BD::BAT.Agenda\" \"agenda\" LEFT JOIN bscl.\"BSCL.Plataforma.BD::BAT.TipoPrograma\" \"tipo\" ON \"tipo\".\"IdTipoPrograma\" = \"agenda\".\"IdTipoPrograma\" WHERE \"habilitado\" = 'S'  ";
		logger.trace(query);
		try (Connection con = datasource.getConnection();
				PreparedStatement st = con.prepareStatement(query);) {
			try (ResultSet rs = st.executeQuery()) {
				while (rs.next()) {
					Agenda agenda = new Agenda();
					agenda.setIdAgenda(rs.getInt("IdAgenda"));
					agenda.setHabilitado(rs.getString("habilitado"));
					agenda.setIdTipoPrograma(rs.getString("IdTipoPrograma"));
					agenda.setTipoClasePrograma(rs.getString("TipoClasePrograma"));
					agenda.setNombreClasePrograma(rs.getString("NombreClasePrograma"));
					agenda.setUltimaFechaEjecucion(rs.getString("UltimaFechaDeEjecucion"));
					agenda.setSiguienteFechaEjecucion(rs.getString("SiguienteFechaDeEjecucion"));
					agenda.setEsDemonio(rs.getString("EsDemonio"));
					agendalist.add(agenda);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
 		}
		return agendalist;
	}
	public boolean pingDB(DataSource datasource) throws SQLException
	{
		String query=  "select 1 from dummy";
 		try(Connection con = datasource.getConnection();PreparedStatement st = con.prepareStatement(query))
		{		
 			try (ResultSet rs = st.executeQuery()) {
 				while (rs.next()) { }
 			}
			return true;
 		}catch(Exception e)
		{
 			logger.error(e);	
			return false;
 		}
	}	
	
	public boolean isDaemaon(DataSource datasource,long programId) {
		String query=  "SELECT \"EsDemonio\" FROM BSCL.\"BSCL.Plataforma.BD::BAT.Agenda\"  WHERE \"IdAgenda\"  IN (SELECT \"IdAgenda\" FROM  bscl.\"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = ? )";
 		try(Connection con = datasource.getConnection();PreparedStatement st = con.prepareStatement(query))
		{		
 			st.setLong(1, programId);
 			try (ResultSet rs = st.executeQuery()) {
 				if (rs.next()) { 
 					if (rs.getString("EsDemonio").equals("S")) 
 						return true; 
 				}
 			}
 		}catch(Exception e)
		{
 			logger.error(e);
 		} 		
		return false;
	}
	
	public void updateCycleDaemon(DataSource datasource,long programId) throws SQLException
	{
		String queryD=  "UPDATE AG SET  \"SiguienteFechaDeEjecucion\" = ADD_SECONDS(CURRENT_UTCTIMESTAMP,1) , \"UltimaFechaDeEjecucion\" = CURRENT_UTCTIMESTAMP FROM BSCL.\"BSCL.Plataforma.BD::BAT.Agenda\" as AG WHERE \"IdAgenda\"  IN (SELECT \"IdAgenda\" FROM  bscl.\"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = ? ) ";
		String queryP=  "UPDATE PG SET \"idEstado\" = 'ASI',\"Bloqueo\" = 'S',\"fechaEjecucion\" = CURRENT_UTCTIMESTAMP,\"fechaSistema\" = CURRENT_UTCTIMESTAMP  FROM bscl.\"BSCL.Plataforma.BD::BAT.Programa\" PG WHERE \"IdPrograma\" = ? ";
 		try(Connection con = datasource.getConnection();
 				PreparedStatement stD = con.prepareStatement(queryD);
 				PreparedStatement stP = con.prepareStatement(queryP);
 				)
		{	
 			stD.setLong(1, programId);
 			stD.executeUpdate();
 			stP.setLong(1, programId);
 			stP.executeUpdate(); 			
 			con.commit();
 		}catch(Exception e)
		{ 			
 			logger.error(e);				
 		}
	}	
	
	public void updateStartDaemon(DataSource datasource,long diaryId) throws SQLException
	{
		String queryD=  "UPDATE BSCL.\"BSCL.Plataforma.BD::BAT.Agenda\"  SET \"SiguienteFechaDeEjecucion\" = ADD_SECONDS(CURRENT_UTCTIMESTAMP,1) , \"UltimaFechaDeEjecucion\" = CURRENT_UTCTIMESTAMP    WHERE \"IdAgenda\" = ? ";
		String queryP=  "UPDATE PG SET \"idEstado\" = 'TER' ,  \"Bloqueo\" = 'S' ,\"Resultado\" = 'Proceso abortado/reiniciado', \"fechaFin\" = CURRENT_UTCTIMESTAMP  FROM bscl.\"BSCL.Plataforma.BD::BAT.Programa\" PG WHERE \"IdPrograma\" IN (SELECT \"IdPrograma\" FROM bscl.\"BSCL.Plataforma.BD::BAT.Programa\"  WHERE  \"IdAgenda\" = ? AND  \"idEstado\" IN ('PEN','ASI') )";
 		try(Connection con = datasource.getConnection();
 				PreparedStatement stD = con.prepareStatement(queryD);
 				PreparedStatement stP = con.prepareStatement(queryP);
 				)
		{	
 			stD.setLong(1, diaryId);
 			stD.executeUpdate();
 			stP.setLong(1, diaryId);
 			stP.executeUpdate(); 			
 			con.commit();
 		}catch(Exception e)
		{ 			
 			logger.error(e);	
 		}
	}		
	
	public void updateNextExecutionDateDiary(DataSource datasource,long diaryId, int segundos) throws SQLException
	{
		String query=  "{CALL bscl.\"BSCL.Plataforma.BD.Servicios::BAT.ActualizarSiguienteFechaEjecucionAgenda\"(?,?,?,?,?,?)}";
		logger.trace(query);
 		try(Connection con = datasource.getConnection();CallableStatement cs = con.prepareCall(query)) {
			cs.setInt(1, 1);
			cs.setInt(2, 3);
			cs.setString(3, "PLA");
			cs.setLong(4, diaryId);
			cs.setInt(5, segundos);
			cs.registerOutParameter(6, Types.NVARCHAR);
			cs.execute();
			con.commit();
 		}catch(Exception e) {			 
 			logger.error(e);
 		}
	}

	public boolean updateProgramm(long idTarea, int resultado,String descripcion, DataSource datasource) throws SQLException {
		String query = "";
		String descripcionAux ="";
		
		if(descripcion.trim().length()>2000){
			descripcionAux = descripcion.substring(0,1999);
		}else{
			descripcionAux = descripcion;
		}
		if(resultado==0)
		{
			query = "update \"BSCL.Plataforma.BD::BAT.Programa\" set \"idEstado\" = 'ASI',\"fechaFin\" = to_seconddate('0001-01-01 00:00:00'), \"Resultado\" = ? where \"IdPrograma\" = ?";
		}else if(resultado==1)
		{
			query = "update \"BSCL.Plataforma.BD::BAT.Programa\" set \"idEstado\" = 'TER',\"fechaFin\" = CURRENT_UTCTIMESTAMP, \"Resultado\" = ? where \"IdPrograma\" = ?";
		}else if(resultado==-1)
		{
			query = "update \"BSCL.Plataforma.BD::BAT.Programa\" set \"idEstado\" = 'ERR',\"fechaFin\" = CURRENT_UTCTIMESTAMP, \"Resultado\" = ? where \"IdPrograma\" = ?";
		}
		logger.trace(query);

  		try (Connection con = datasource.getConnection();
  			 PreparedStatement st = con.prepareStatement(query);	) {
			
			st.setString(1, descripcionAux);
			st.setLong(2, idTarea);
			st.execute();
			
			        if(resultado==1 || resultado==-1)
			        {
			        	query = "update  ag set \"UltimaFechaDeEjecucion\" = CURRENT_UTCTIMESTAMP "
			        			+"from \"BSCL.Plataforma.BD::BAT.Programa\" pg INNER JOIN \"BSCL.Plataforma.BD::BAT.Agenda\" ag  "
			        			+ "ON pg.\"IdAgenda\"=ag.\"IdAgenda\" and pg.\"IdPrograma\" = ?";
			        	try(PreparedStatement pst = con.prepareStatement(query))
			        	{
			        		pst.setLong(1, idTarea);
			        		pst.execute();
			        	}
			        }
			con.commit();
 			return true;

		} catch (SQLException e) {
			logger.error(e);
		}
		return false;
	}	
	public int updateTryProgramm(Connection connection) {
		String query = " UPDATE P SET P.\"Bloqueo\" = 'N', P.\"idEstado\" = 'PEN',  P.\"Resultado\" = '(Reintento '|| (P.\"Intento\"+1) ||')'||P.\"Resultado\" , P.\"fechaEjecucion\" = ADD_SECONDS(CURRENT_UTCTIMESTAMP,60)   "
				+ " FROM  \"BSCL.Plataforma.BD::BAT.Programa\" as P "
				+ " JOIN \"BSCL.Plataforma.BD::BAT.TipoPrograma\" as TP on P.\"IdTipoPrograma\"=TP.\"IdTipoPrograma\" "
				+ " WHERE P.\"Bloqueo\"  = 'S' and P.\"idEstado\" = 'ERR' and  P.\"Intento\" < TP.\"MaximoIntentos\"  and P.\"fechaEjecucion\" between ADD_SECONDS(CURRENT_UTCTIMESTAMP,-3600) AND CURRENT_UTCTIMESTAMP \n"
				+ " ";
		
		logger.trace(query);
		              
		try (	Connection con = connection;
				PreparedStatement st = con.prepareStatement(query);) {
			st.executeUpdate();
			con.commit();
			return 0;
		} catch (SQLException e) {
			logger.error(e);
		}
		
		return -1;

	}

	public int updateIntent(long idPrograma, int intento, Connection connection) {
		String query = "update \"BSCL.Plataforma.BD::BAT.Programa\" set \"Intento\" = "
				+ intento + " where \"IdPrograma\" = " + idPrograma + "";
		logger.trace(query);
		Connection con = connection;
		try (
				PreparedStatement st = con.prepareStatement(query);) {
			int resultado = st.executeUpdate();
			con.commit();
			return resultado;
		}

		catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}

		return -1;

	}

	public int addProgramm(int idAgenda, String idTipoTarea, int date,Map<String, String> parameters, DataSource datasource) throws SQLException {
 		String nombreTabla = "#Parametros_" + idAgenda;
		String query = "call bscl.\"BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma\" (?,?,?,?,?,?,"+ nombreTabla + ",?,?,?,?,?,?)";
		String queryCreate = "CREATE LOCAL TEMPORARY TABLE "+ nombreTabla+ " (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))";
		String querInsert = "INSERT INTO " + nombreTabla+ " values (?,?)";
	
		logger.trace(query);		
		
		try (   Connection con = datasource.getConnection(); PreparedStatement cpst = con.prepareStatement(queryCreate);	) {
    		try {
				 cpst.executeUpdate();
			} catch (SQLException e) {
				logger.error(e);

			}
			
			try(PreparedStatement ipst = con.prepareStatement(querInsert))
			{
				for (Entry<String, String> parametros : parameters.entrySet()) {
					
					ipst.setString(1, parametros.getKey()==null?"":parametros.getKey());
					ipst.setString(2, parametros.getValue()==null?"":parametros.getValue());
					ipst.addBatch();
	
				}
				ipst.executeBatch();
			}catch(Exception e)
			{
				logger.error(e);
			}
			
			try(CallableStatement st = con.prepareCall(query);)
			{
				    st.setLong(1, 1);
				    st.setLong(2, 0);
				    st.setString(3, "ORD");
				    st.setLong(4, idAgenda);
				    st.setString(5, idTipoTarea);
				    st.setLong(6, date);
				    st.setString(7, "S");
				    st.registerOutParameter(8, Types.NVARCHAR);
				    st.registerOutParameter(9, Types.NVARCHAR);
				if(st.execute())
				{
					try(ResultSet rs = st.getResultSet())
					{
						while (rs.next()) {
							dropTable(nombreTabla,con);
							con.commit();
							return Integer.parseInt(rs.getString(1));
	
						}
					}
				}
			}

			

		}catch(Exception e)
		{
			e.printStackTrace();
			logger.error(e);
		 
		}

		return -1;

	}



	public Map<String, String> paramsDiary(long diaryId, DataSource datasource)
			throws SQLException {
		Map<String, String> param = new HashMap<String, String>();
		String query = "select \"NombreParametro\", \"Valor\"  from \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda\"  where \"IdAgenda\" ="
				+ diaryId;
		logger.trace(query);
		
		
		try ( Connection con = datasource.getConnection();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();) {

			while (rs.next()) {
				param.put(rs.getString("NombreParametro"),
						rs.getString("Valor"));
			}

		} catch (SQLException e) {
			// TODO: handle exception
			logger.error(e);
 		}

		return param;

	}

	public int getDateToProgramm(long diaryId, DataSource datasource)
			throws SQLException {

		String query = "call \"BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada\"(?,?,?,?,?,?)";
		int date = -1;
		logger.trace(query);
	
		try (		Connection con = datasource.getConnection();
			CallableStatement st = con.prepareCall(query);) {
			st.setLong(1, 1);
			st.setLong(2, 1);
			st.setString(3, "ORD");
			st.setLong(4, diaryId);
			st.registerOutParameter(5, Types.BIGINT); 
 
			if(st.execute())
			{
				try(ResultSet rs =st.getResultSet()){
					while (rs.next()) {
						logger.debug("-->"  +  diaryId);
						date = Integer.parseInt(rs.getString(1));
 					}
				}
			}
			return date;
		} catch (Exception e) {
			logger.error(e);
  			return 0;
		}

		
	}
	
	public boolean firstExecution(DataSource datasource){

		String query = "call \"BSCL.Plataforma.BD.Servicios::BAT.CorregirPrimeraEjecucion\"(?,?,?,?)";
 		logger.trace(query);
	
		try (Connection con = datasource.getConnection();
				CallableStatement st = con.prepareCall(query);) {
 			st.setLong(1, 1);
			st.setLong(2, 1);
			st.setString(3, "BAT");
 			st.executeUpdate();
 			con.commit();
 			return true;
		} catch (SQLException e) {
			// TODO: handle exception
			logger.error(e);
 			return false;
		}

	}

	public void cancelProgramm(long programmId, DataSource datasource)
			throws SQLException {
		String query = "call bscl.\"BSCL.Plataforma.BD.Servicios::BAT.CancelarPrograma\"(1,1,'PLA',?,?,?,?,?)";
		logger.trace(query);

		try (Connection con = datasource.getConnection();
				CallableStatement st = con.prepareCall(query);
				
				) {
			st.setLong(1, 1);
			st.setLong(2, 1);
			st.setString(3, "ORD");
			st.setLong(4, programmId);
			st.registerOutParameter(5, Types.NVARCHAR);
			st.registerOutParameter(6, Types.NVARCHAR);
			st.execute();

		}

	}

	public Map<String, String> paramsProgram(long programmId,
			DataSource datasource) throws SQLException {
		String query = "select \"NombreParametro\", \"Valor\", \"ValorBlob\"  from \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas\"  where \"IdPrograma\" = ?";
		Map<String, String> param = new HashMap<String, String>();
		logger.trace(query);
		
		
		
		try (		Connection con = datasource.getConnection();PreparedStatement st = con.prepareStatement(query);){
			st.setFetchSize(1000);
			st.setLong(1, programmId);
			
			if(st.execute())
			{
				try(ResultSet rs = st.getResultSet();) {
						while (rs.next()) {
								if(rs.getString("ValorBlob") == null) 
								{	
									param.put(rs.getString("NombreParametro"),rs.getString("Valor"));
								}else {
									String valorBlob = new String(rs.getBytes("ValorBlob"), StandardCharsets.UTF_8);
									param.put(rs.getString("NombreParametro"), valorBlob);
								}
						}	
					}}
		} catch (SQLException e) {
			// TODO: handle exception
 		}
		
 
		return param;
	}

	public String getNow(DataSource datasource) {
		final String now = "select CURRENT_UTCTIMESTAMP from dummy";
		try (Connection con = datasource.getConnection();
				PreparedStatement st = con.prepareStatement(now)) {
			try (ResultSet rs = st.executeQuery();) {
				if (rs.next()) {
					return rs.getString(1);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}
	
	public int executionCount(String taskType, String parameterName,
			String parameterValue, DataSource datasource) throws SQLException {
		int contador = -1;
		String fecha = getNow(datasource);
		String query = "SELECT count(*) as contador from \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma\" where \"idEstado\" = 'ASI' and \"NombreParametro\"=? and \"Valor\"=? and \"IdTipoPrograma\"=? and \"fechaEjecucion\" between (to_seconddate(TO_DATE(?) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS')) and (to_seconddate(?, 'YYYY-MM-DD HH24:MI:SS'))";
		int taskTypeInt;
		try { taskTypeInt = Integer.parseInt(taskType); }
		catch (NumberFormatException | NullPointerException e) { throw new SQLException("Invalid taskType: " + taskType, e); }
		logger.trace(query);
		try (Connection con = datasource.getConnection();
				PreparedStatement st = con.prepareStatement(query)) {
			st.setString(1, parameterName);
			st.setString(2, parameterValue);
			st.setInt(3, taskTypeInt);
			st.setString(4, fecha.substring(0, 10));
			st.setString(5, fecha.substring(0, 19));
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next())
					return contador = Integer.parseInt(rs.getString(1));
			}
		} catch (SQLException e) {
			logger.warn("executionCount query failed for taskType={}", taskType, e);
		}
		return contador;
	}

	public boolean dropTable(String nombreTabla, Connection connection) {

		String query = "drop table " + nombreTabla;
		logger.trace(query);
		Connection con = connection;
		try (PreparedStatement st = con.prepareStatement(query);) {
		 
			return st.execute();
		} catch (Throwable e) {
			logger.error(e);
			return false;
		}

	}

	public boolean compararFechasConDate(String fecha1, String fechaActual) {

		try {
			boolean resultado = false;

			SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fechaDate1 = formateador.parse(fecha1);
			Date fechaDate2 = formateador.parse(fechaActual);

			if (fechaDate1.before(fechaDate2)) {
				resultado = true;
			} else {
				if (fechaDate2.before(fechaDate1)) {
					resultado = false;
				} else {
					resultado = true;
				}
			}
			return resultado;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public String classPathCapitalice(String classpath)
	{
		classpath = classpath.toLowerCase();
		int pos = 0;
		boolean capitalize = true;
		StringBuilder sb = new StringBuilder(classpath);
		while (pos < sb.length()) {
		    if (sb.charAt(pos) == '.') {
		        capitalize = true;
		    } else if (capitalize && !Character.isWhitespace(sb.charAt(pos))) {
		        sb.setCharAt(pos, Character.toUpperCase(sb.charAt(pos)));
		        capitalize = false;
		    }
		    pos++;
		}
 		return sb.toString();
	}
	
	public void addNodo(DataSource datasource,String Nodo,String Job,int pid, String processId) throws SQLException {
		try (   Connection con = datasource.getConnection(); 	) {
			//Borra antiguos - tabla debe estar limpia
			String queryDel = "DELETE from \"BSCL.Plataforma.BD::BAT.Nodos\" WHERE  \"Nodo\" = ?  AND \"Job\" = ? ";
        	try(PreparedStatement pst = con.prepareStatement(queryDel))
        	{
        		pst.setString(1, Nodo);
        		pst.setString(2, Job);
        		pst.execute();
        	}	
        	
			//Nuevo
			String queryInsert = "INSERT INTO \"BSCL.Plataforma.BD::BAT.Nodos\" (\"Nodo\" ,\"Job\" , \"Pid\" ,\"FechaUltimoCiclo\" ,\"Reiniciar\" )  "
					              + " VALUES (?,?,?,CURRENT_UTCTIMESTAMP,?)";	
        	try(PreparedStatement pst = con.prepareStatement(queryInsert))
        	{
        		pst.setString(1, Nodo);
        		pst.setString(2, Job);
        		pst.setInt(3, pid);
        		pst.setString(4,"N");
        		pst.execute();    		
        	}		
        	
        	// HeartBeat
        	// Solo si se ingresa parametro identificador proceso
			if (!processId.isEmpty()) {

				// Borrar antiguos
				String queryDelHeartbeat = "DELETE FROM \"BSCL.Plataforma.BD::PLA.Heartbeat\" WHERE \"Id\" = ?";
				try (PreparedStatement pst = con.prepareStatement(queryDelHeartbeat)) {
					pst.setString(1, processId);
					pst.execute();
				}

				// Nuevo
				String queryInsertHeartbeat = "INSERT INTO \"BSCL.Plataforma.BD::PLA.Heartbeat\" (\"Id\", \"LastTimestamp\") VALUES(?, CURRENT_TIMESTAMP)";
				try (PreparedStatement pst = con.prepareStatement(queryInsertHeartbeat)) {
					pst.setString(1, processId);
					pst.execute();
				}
			}
        		
        	con.commit();
		}
	}
	
	public List<Integer> getZombieNodo(DataSource datasource,int timeout,boolean clean) throws SQLException {
		List<Integer> list=new ArrayList<>();
		//Muertos
		String query = "select \"Pid\" from \"BSCL.Plataforma.BD::BAT.Nodos\" where \"FechaUltimoCiclo\" < add_Seconds(CURRENT_UTCTIMESTAMP,?)";
		try (Connection con = datasource.getConnection();PreparedStatement st = con.prepareStatement(query);) {	
			st.setInt(1, -1*timeout);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					list.add(rs.getInt(1));
				}
			}
		} 
		//Limpia Muertos
		if (clean) {
			for (int pid:list) {
				String queryUpd = "delete from \"BSCL.Plataforma.BD::BAT.Nodos\" WHERE  \"Pid\" = ? ";
				try (   Connection con = datasource.getConnection(); 	) {
		        	try(PreparedStatement pst = con.prepareStatement(queryUpd))
		        	{
		        		pst.setInt(1, pid);
		        		pst.execute();
		        		con.commit();
		        	}			
				}
			}
		}
		return list;
	}		
	
	
	public boolean isRebootNodo(DataSource datasource,int pid) throws SQLException {
		boolean reboot=false;
		//Tiene reinicio
		String query = "select TOP 1 \"Reiniciar\" from \"BSCL.Plataforma.BD::BAT.Nodos\" where \"Pid\" = ? order by \"FechaUltimoCiclo\" DESC ";
		try (Connection con = datasource.getConnection();PreparedStatement st = con.prepareStatement(query);) {	
			st.setInt(1, pid);
			try (ResultSet rs = st.executeQuery();) {
				if (rs.next()) {
					if (rs.getString(1).equals("S")) {
						reboot=true;
					}
				}
			}
		} 	
		return reboot;
	}	
	
	
	public void updateCycleNodo(DataSource datasource,int pid, String Job, String processId) throws SQLException {
		//Actualiza ciclo
		String queryUpd = "update \"BSCL.Plataforma.BD::BAT.Nodos\"  SET \"FechaUltimoCiclo\" = CURRENT_UTCTIMESTAMP  WHERE \"Pid\" = ? ";
		String queryUpdHeartbeat = "UPDATE \"BSCL.Plataforma.BD::PLA.Heartbeat\"SET \"LastTimestamp\"= CURRENT_TIMESTAMP WHERE \"Id\" = ?";	
		
		
		try (   Connection con = datasource.getConnection(); 	) {
			
        	try(PreparedStatement pst = con.prepareStatement(queryUpd))
        	{
        		pst.setInt(1, pid);
        		pst.execute();
        		con.commit();
        	}
        	
			if (!processId.isEmpty()) {
				try (PreparedStatement pst = con.prepareStatement(queryUpdHeartbeat)) {
					pst.setString(1, processId);
					pst.execute();
					con.commit();
				}
			}
        	
		}
	}	
	
}
