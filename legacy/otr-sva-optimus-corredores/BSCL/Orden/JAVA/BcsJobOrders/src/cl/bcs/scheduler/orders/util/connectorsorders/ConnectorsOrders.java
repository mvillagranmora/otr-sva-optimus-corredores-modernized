package cl.bcs.scheduler.orders.util.connectorsorders;

 import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
 import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.scheduler.orders.bcsjob.BcsJobOrderChronologicAllocation;
import cl.bcs.scheduler.orders.pojo.Asignacion;
import cl.bcs.scheduler.orders.pojo.Orden;
import cl.bcs.scheduler.orders.pojo.ParametrosDefault;
import cl.bcs.scheduler.orders.pojo.Transaccion;

public class ConnectorsOrders {
	private DataSource datasource;
	public ConnectorsOrders() {
		super();
	}
	private static final String tableOrdersBook ="BSCL.Orden.BD::ORD.LibroOrdenes";
	private static final String schema ="BSCL";

	private static final String executionLast ="UltimaFechaDeEjecucion";
	private static final String ordersBookId ="IdLibroOrdenes";
	private static final String ordersBookName ="NombreLibroOrdenes";
	private static final String paramName ="NombreParametros";
	private static final String paramValue ="Valor";
	private static final String programmId ="IdPrograma";
	private static final String programmType ="ASIG_CRONO";
	private static final String viewGetProgrammParam = "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas";
	private static final String spGetLastExecution = "BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada";
	private static final String spGetLastExecutionAssignDate = "BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada";
	private static final String spChronologicAssignPrepare = "BSCL.Orden.BD.Servicios::PrepararAsignacionCronologica";
	private static final String orderId = "IdOrden";
	private static final String orderIdAux = "IdOrdenPaso";
	private static final String createTableTemporary = "CREATE LOCAL TEMPORARY TABLE ";
	private static final String dataTypeBIGINT = "BIGINT";
	private static final String insertInto = "INSERT INTO ";
	private static final String tableOrderAux = "BSCL.Orden.BD::ORD.OrdenPaso";
	private static final String tableTransactionAux = "BSCL.Orden.BD::ORD.TransaccionPaso";
	private static final String tableOrdenTemporary = "#orden_in_";
	private static final String tableTransaccionTemporary = "#transaccion_in_";
	private static final String tableAssignTemporary = "#asignacion_in_";
	private static final String tableAssignAux = "BSCL.Orden.BD::ORD.AsignacionPaso";
	private static final String assignId = "IdAsignacion";
	private static final String auxAssignId ="IdAsignacionPaso";
	private static final String transactionId = "IdTransaccion";
	private static final String AuxtransactionId = "IdTransaccionPaso";
	private static final String select = " SELECT ";
	private static final String from = " from ";
	private static final String spChronologicAssignPrepareWithDate = "BSCL.Orden.BD.Servicios::PrepararAsignacionCronologicaConFecha";
	private static final String spBlockUnblockOrders = "BSCL.Orden.BD.Servicios::BloquearDesbloquearOrdenes";
	private static final String spBlockUnblockTransactions = "BSCL.Orden.BD.Servicios::BloquearDesbloquearTransacciones";
	private static final String assignDescryption = "Asignación Cronológica";
	private static final String spGetAnulationMassive = "BSCL.Orden.BD.Servicios::AnularAsignacionMasiva";
	private static  String tableOrdenAux = "#orden_in_";
	private static  String tableTransaccionAux = "#transaccion_in_";
	private static final String spAddAssign = "BSCL.Orden.BD.Servicios::ORD.AgregarAsignacion";

	
	 private static String getQuotedString(final String name) {
	        return "\"" + name + "\"";
	    }

	 public static Connection sapHanaConnection() throws SQLException {
	        return DriverManager.getConnection("jdbc:sap://161.238.34.39:30215/?autocommit=false",
	                "JOBMAN", "Bolsa123");
	    }
	public void result(long diaryId, Connection connection) throws SQLException {
		final String sql = "call \"BSCL.Orden.BD.Servicios::ORD.AsignacionCronologicaAutomaticaActiva\"(1,1,'PLA',"
				+ diaryId + ",?,?,?,?,?)";
		try {
			Connection con = connection;
			try (Statement st = con.createStatement()) {
				st.executeUpdate("SET SCHEMA BSCL");
				st.executeQuery(sql);
			}
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, sql);
		} catch (SQLException e) {
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());

		}
	}
 
	public   String getUltimaFechaLibroOrdenes(int libroOrdenes, Connection connection) throws SQLException
	{
		
		String query = String.format("Select %s from %s.%s where %s = %s ",getQuotedString(executionLast),getQuotedString(schema),getQuotedString(tableOrdersBook),getQuotedString(ordersBookId),libroOrdenes);
 		
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		
		 Connection con = connection;
		 try(Statement st = con.createStatement(); ResultSet rs = st.executeQuery(query))
		 {
			  while(rs.next())
			  {
				  return rs.getString(1);
			  }
		 }
		return null;
	}
	public   String OrdersBookName(int idLibroOrdenes, Connection connection) throws SQLException {
 
		
		final String query = String.format("select %s from  %s.%s  where %s = %s"
				  ,getQuotedString(ordersBookName),getQuotedString(schema),getQuotedString(tableOrdersBook),getQuotedString(ordersBookId),idLibroOrdenes);
 
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;
			try (Statement st = con.createStatement();) {

				try (ResultSet rs = st.executeQuery(query);) {
					while (rs.next()) {
						return rs.getString(ordersBookName);
						
					}
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
 			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
		}
		return null;
	}

	public void isActiveExecutionAssignAutomatic(long diaryId, int seconds, Connection connection) {

		final String sql  = String.format("select %s, %s from %s.%s where %s = %s"  ,getQuotedString(paramName),getQuotedString(paramValue),getQuotedString(schema),getQuotedString(viewGetProgrammParam),getQuotedString(programmId),diaryId);
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, sql);
 		try {
			Connection con = connection;
			try (Statement st = con.createStatement(); Statement anotherSt = con.createStatement();) {
				
				String nombreTabla = "#Parametros_" + diaryId;
				
				try {
					anotherSt.execute("drop table" + nombreTabla);
				} catch (SQLException e) {
					Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
							"No existe tabla, no puedo dropear (NO GRAVE)");
				}
				anotherSt.execute("CREATE LOCAL TEMPORARY TABLE " + nombreTabla
						+ " (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))");
				try (ResultSet rs = st.executeQuery(sql);) {
					String nombreParametro = "";
					String valor = "";
					while (rs.next()) {
						nombreParametro = rs.getString("NombreParametro");
						valor = rs.getString("Valor");
						if (nombreTabla == null || !nombreTabla.matches("[A-Za-z0-9_.#]+")) {
							throw new java.sql.SQLException("Rejected unsafe table name");
						}
						try (java.sql.PreparedStatement insertSt = connection.prepareStatement(
								"INSERT INTO " + nombreTabla + " values (?,?)")) {
							insertSt.setString(1, nombreParametro);
							insertSt.setString(2, valor);
							insertSt.executeUpdate();
						}
					}

					int programmId = addProgramm((int) diaryId, "ASIG_CRONO", seconds, nombreTabla, connection);
					addLog(diaryId, "ACT", nombreTabla + "  parametros " + nombreParametro + " " + valor, connection);
					anotherSt.execute("drop table " + nombreTabla);
 
				}

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
		}

	}

	public   int getLastExecutionAssignCount(String ordersBook, Connection connection, ParametrosDefault param) {
		int result = -1;
 
		
		final String sql  = String.format("{CALL %s.%s(?,?,?,?,?,?,?,?,?)}",getQuotedString(schema),getQuotedString(spGetLastExecution));
		
		Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO, sql);
		try {
			Connection con = connection;
			try (CallableStatement cstmt = con.prepareCall(sql)) {
				
				
				cstmt.setLong(1, param.getIdEmpresa());
				cstmt.setLong(2, param.getIdUsuario());
				cstmt.setString(3, param.getModulo());
				cstmt.setLong(4, Integer.parseInt(ordersBook));
				cstmt.registerOutParameter(5, Types.NVARCHAR);
	            cstmt.registerOutParameter(6, Types.NVARCHAR);
				
	            if(cstmt.execute())
	            {
 	            	try(ResultSet rs = cstmt.getResultSet())
	            	{
	            		if (rs.next()) {
	            			
 	                        return rs.getInt("contador");
	                    }
	            	}
	            }
			

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
 			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
		}
		return result;
	}

	public String getLastExecutionAssignDate(String ordersBook, Connection connection, ParametrosDefault param ) {
		String result = "";
		
		final String sql = String.format("call  %s.%s(?,?,?,?,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spGetLastExecutionAssignDate));
		Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO, sql);
		//FechaModificacionTransaccion
		try {
			Connection con = connection;
				try (CallableStatement cstmt = con.prepareCall(sql)) {
				
				
					cstmt.setLong(1, param.getIdEmpresa());
					cstmt.setLong(2, param.getIdUsuario());
					cstmt.setString(3, param.getModulo());
					cstmt.setLong(4, Integer.parseInt(ordersBook));
					cstmt.registerOutParameter(5, Types.NVARCHAR);
		            cstmt.registerOutParameter(6, Types.NVARCHAR);
					
		            if(cstmt.execute())
		            {
	 	            	try(ResultSet rs = cstmt.getResultSet())
		            	{
		            		if (rs.next()) {
		            			System.out.println("FEEEEECHA"+ rs.getString("FechaModificacionTransaccion"));
		            			System.out.println("FEEEEECHA"+ rs.getString("contador"));
		            			System.out.println("LibroOrdenes "+ordersBook);
	 	                        return rs.getString("FechaModificacionTransaccion");
		                    }
		            	}
		            }
				

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
					"getLastExecutionAssignDate " + e.getMessage(), e.getCause());
		}
		return result;
	}

	public void temporaryTableCreate(long jobId, Connection connection)
	{
		 final String tableOrder = tableOrdenAux +   jobId;
		 final String tableTrx =  tableTransaccionAux  + jobId;
		final String sqlCreateTableOrders = createTableTemporary + tableOrder + " ("+getQuotedString(orderId)+" "+dataTypeBIGINT+")";
		final String sqlInsertIntoOrders = insertInto+tableOrder+select+getQuotedString(orderIdAux)+from+getQuotedString(tableOrderAux)+"";
		final String sqlCreateTableTransaction= createTableTemporary + tableTrx + " ("+getQuotedString(transactionId)+dataTypeBIGINT+")";
		final String sqlInsertIntoTransaction = insertInto + tableTrx+select+getQuotedString(AuxtransactionId)+from+getQuotedString(tableTransactionAux)+"";
		Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO,
				sqlCreateTableOrders);
		Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO,
				sqlCreateTableTransaction);

		try{
			Connection conn = connection;
		try (Statement createTemporary = conn.createStatement()) {
			createTemporary.execute(sqlCreateTableOrders);
			createTemporary.executeUpdate(sqlInsertIntoOrders);
			createTemporary.execute(sqlCreateTableTransaction);
			createTemporary.executeUpdate(sqlInsertIntoTransaction);
		}  
		}catch(Throwable e)
		{
			 
			Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.SEVERE,
					"temporaryTableCreate - " + e.getMessage(), e.getCause());
		}
	}
	public boolean prepareDataForAssign(long jobId, String ordersBook, Connection connection, ParametrosDefault param) {
 
		final String sql = String.format("call %s.%s(?,?,?,?,?,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spChronologicAssignPrepare));
		int countOrders =0;
		int countTransaction =0;

		try {
			Connection con = connection;
      
			try (CallableStatement cstmt = con.prepareCall(sql)) {
				
				
				cstmt.setLong(1, param.getIdEmpresa());
				cstmt.setLong(2, param.getIdUsuario());
				cstmt.setString(3, param.getModulo());
				cstmt.setLong(4,jobId );
				cstmt.setLong(5, Integer.parseInt(ordersBook));
				cstmt.registerOutParameter(5, Types.NVARCHAR);
	            cstmt.registerOutParameter(6, Types.NVARCHAR);
	            if(cstmt.execute())
	            {
 	            	try(ResultSet rs = cstmt.getResultSet())
	            	{
	            		while(rs.next()) {
	            			countOrders = rs.getInt("CantidadOrdenes");
	            			countTransaction = rs.getInt("TransaccionOrdenes");
	            			System.out.println(rs.getString("Fecha") + "Fec");
	            			System.out.println(rs.getString("Asignaciones") + "asf");
	            			System.out.println(rs.getString("IdPrograma") + "IdPr");

 	                    }
	            	}
	            }
	            System.out.println(countOrders + " ord");
	            System.out.println(countTransaction + "  trx");
	            if(countOrders==0||countTransaction==0)return false;
	            temporaryTableCreate(jobId, connection);
	            return true;
	          
			

		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO,
					"prepareDataForAssign - " + e.getMessage(), e.getCause());
			return false;
		}
 	}
	

	public boolean prepareDataForAssignWithDate(long jobId, String ordersBookId, String fechaCorte,Connection connection,ParametrosDefault param) {
 
		int countOrders =0;
		int countTransaction =0;
		final String sql = String.format("call %s.%s(?,?,?,?,?,?,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spChronologicAssignPrepareWithDate),"");
		Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO, sql);

		try {
			Connection con = connection;
 
				try (CallableStatement cstmt = con.prepareCall(sql)) {
				
				
				cstmt.setLong(1, param.getIdEmpresa());
				cstmt.setLong(2, param.getIdUsuario());
				cstmt.setString(3, param.getModulo());
				cstmt.setLong(4,jobId );
				cstmt.setLong(5, Integer.parseInt(ordersBookId));
				cstmt.setString(6, fechaCorte);
				cstmt.registerOutParameter(5, Types.NVARCHAR);
	            cstmt.registerOutParameter(6, Types.NVARCHAR);
	            if(cstmt.execute())
	            {
 	            	try(ResultSet rs = cstmt.getResultSet())
	            	{
	            		while(rs.next()) {
	            			countOrders = rs.getInt("CantidadOrdenes");
	            			countTransaction = rs.getInt("TransaccionOrdenes");

 	                    }
	            	}
	            }
	            if(countOrders==0||countTransaction==0)return false;
	            temporaryTableCreate(jobId, connection);
	            
	            return true;
	          
			

		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(BcsJobOrderChronologicAllocation.class.getName()).log(Level.INFO,
					"prepareDataForAssignWithDate -" + e.getMessage(), e.getCause());
			dropTable(tableOrdenAux+jobId, connection);
			dropTable(tableTransaccionAux+jobId, connection);

			return false;
		}
 	}

	 

	 

	public void addLog(final long idTarea, final String estadoProceso, final String nombreProceso,
			Connection connection) throws SQLException {
		final String sql = "call \"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogProgramas\"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "addLog idTarea={0}", idTarea);

		try {
			try (java.sql.CallableStatement cs = connection.prepareCall(sql)) {
				cs.setInt(1, 1);
				cs.setInt(2, 1);
				cs.setString(3, "ORD");
				cs.setString(4, "INFORMATIVO");
				cs.setString(5, estadoProceso);
				cs.setString(6, "Asignación Automática Cronológica");
				cs.setLong(7, idTarea);
				cs.setString(8, "Asignación Crónológica");
				cs.setLong(9, idTarea);
				cs.setString(10, estadoProceso);
				cs.setString(11, nombreProceso);
				cs.registerOutParameter(12, java.sql.Types.VARCHAR);
				cs.registerOutParameter(13, java.sql.Types.VARCHAR);
				cs.registerOutParameter(14, java.sql.Types.VARCHAR);
				cs.registerOutParameter(15, java.sql.Types.VARCHAR);
				cs.execute();
			}
		} catch (SQLException e) {
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "addLog- " + e.getMessage(),
					e.getCause());

		}
	}

	public void lockUnlockOrders(String blockUnblock, long jobId, Connection connection, ParametrosDefault param) throws SQLException {
		 
		final String tableTemporary = tableOrdenTemporary + jobId;
		final String sql  = String.format("call %s.%s(?,?,?,?,?,%s,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spBlockUnblockOrders),tableTemporary);
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO,  sql);

		try {
			Connection con = connection;
			try (CallableStatement cstmt = con.prepareCall(sql)) {
				
				
			cstmt.setLong(1, param.getIdEmpresa());
			cstmt.setLong(2, param.getIdUsuario());
			cstmt.setString(3, param.getModulo());
			cstmt.setString(4,blockUnblock );
			cstmt.setString(5, assignDescryption);
			cstmt.registerOutParameter(5, Types.NVARCHAR);
            cstmt.registerOutParameter(6, Types.NVARCHAR);
          
          
          
		

	}
		} catch (SQLException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();

			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
					"lockUnlockOrders - " + "ERROR   " + e.getMessage());

		}
	}

	public void lockUnlockTransactions(String blockUnblock, long jobId, Connection connection, ParametrosDefault param) throws Throwable {
		final String tableTransacction = tableTransaccionTemporary+jobId;
		 
		final String sql  = String.format("call %s.%s(?,?,?,?,?,%s,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spBlockUnblockTransactions),tableTransacction);

		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "lockUnlockTransactions - " + sql);

		try {
			try {
				Connection con = connection;
				try (CallableStatement cstmt = con.prepareCall(sql)) {
					
					
				cstmt.setLong(1, param.getIdEmpresa());
				cstmt.setLong(2, param.getIdUsuario());
				cstmt.setString(3, param.getModulo());
				cstmt.setString(4,blockUnblock );
				cstmt.setString(5, assignDescryption);
				cstmt.registerOutParameter(5, Types.NVARCHAR);
	            cstmt.registerOutParameter(6, Types.NVARCHAR);
				}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
					"lockUnlockTransactions - " + e.getMessage(), e.getCause());
 			throw new Throwable(e.getMessage());

		}} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
 			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
					"lockUnlockTransactions -" + e.getMessage(), e.getCause());

		}
		
	}

	public int dropTable(String nombreTabla, Connection connection) {
		int rs = 0;

		String query = "drop table " + nombreTabla;
		try {
			Connection con = connection;
			try (Statement st = con.createStatement()) {
				Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "drop table " + nombreTabla);
				rs = st.executeUpdate(query);

			}
		} catch (SQLException e) {
			rs = -1;
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "ERROR   " + e.getMessage());

		}

		return rs;
	}

	public void createAndInsertTableTemporallyAssign(long jobId, int ordersBook, Connection connection) {
		final String query = "CREATE local TEMPORARY TABLE #created_asignacion_" + jobId
				+ " (\"IdInterno\" BIGINT, \"IdOrden\" BIGINT, \"IdTransaccion\" BIGINT, \"Cantidad\" BIGINT , \"ValorRescate\" DECIMAL(26,8) )";
		final String queryInsert = "insert into #created_asignacion_" + jobId
				+ " SELECT \"IdAsignacionResultado\", \"IdOrdenPaso\", \"IdTransaccionPaso\", \"CantidadAsignada\", \"ValorRescate\" from \"BSCL.Orden.BD::ORD.AsignacionResultado\" "
				+ "LEFT JOIN \"BSCL.Orden.BD::ORD.Transaccion\""
				+ "on \"IdTransaccionPaso\" = \"IdTransaccion\""
				+ "where \"IdPrograma\" = "
				+ jobId + "";
		
		final String sql = "call \"BSCL.Orden.BD.Servicios::CrearAsignacionMM\"(?,?,?, ? ,#created_asignacion_" + jobId + ",?,?,?,?,?,?)";

		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, sql);
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, queryInsert);        
		
 		try {
			Connection con = connection;
 			try (Statement tempCreate = con.createStatement();){
 				tempCreate.execute(query);
 				tempCreate.executeUpdate(queryInsert);
 				try(CallableStatement cstm = con.prepareCall(sql)){
					cstm.setInt(1, 1);
					cstm.setInt(2, 3);
					cstm.setString(3, "ORD");
					cstm.setString(4, "JOB");
					boolean moreResult = cstm.execute();
					while(moreResult){
						
					try(ResultSet rs = cstm.getResultSet()){
						int cycle = 0;
					
						List<String> columnName = new ArrayList<String>();
						while(cycle<rs.getMetaData().getColumnCount()){
							cycle++;
							if(rs.getMetaData().getTableName(cycle).contains("P_OUT_ERRORESDETALLE")){
							
 							columnName.add(rs.getMetaData().getColumnName(cycle));
							}
						}
						while(rs.next() && moreResult){
							  for(String column : columnName)
							  System.out.println(rs.getString(column));
						  }					
						}
					  moreResult = cstm.getMoreResults();
	  				}
 				}

			}
		} catch (SQLException e) {
			
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
					"createAndInsertTableTemporallyAssign -" + e.getMessage(), e.getCause());
		}

	}

	public int addProgramm(int idAgenda, String idTipoTarea, int seconds, String nombreTabla, Connection connection) {
		String query = "call \"BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma\" (1,1,'ORD'," + idAgenda + ",'"
				+ idTipoTarea + "',0,1,'PEN','Esperando Ejecucion'," + seconds + "," + nombreTabla + ",'S',?,?,?,?,?)";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;
			try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(query);) {

				while (rs.next()) {
					con.commit();
					return Integer.parseInt(rs.getString(1));

				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "addProgramm" + e.getMessage(),
					e.getCause());
		}

		return -1;
	}

	public boolean updateProgramm(long idTarea, int resultado, String descripcion, Connection connection) {
		String query = "call \"BSCL.Plataforma.BD.Servicios::BAT.ActualizarEstadoPrograma\"(1,1,'PLA','" + idTarea
				+ "'," + resultado + ",'" + descripcion + "',?,?,?,?) ";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;
			try (Statement st = con.createStatement();) {
				try (ResultSet rs = st.executeQuery(query);) {
					Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);

					if (rs.next())
						return true;

				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "updateProgramm " + e.getMessage(),
					e.getCause());
		}
		return false;

	}
	public int updateStateAssign(String date, String orderBooks, Connection connection)
	{
		String query = "UPDATE \"BSCL.Orden.BD::ORD.Asignacion\" asig SET \"IdEstadoAsignacion\" = 'ANU' FROM \"BSCL.Orden.BD::ORD.Transaccion\" trx where asig.\"IdTransaccion\" = trx.\"IdTransaccion\" and trx.\"FechaOperacion\" >= to_seconddate( '"+date+"' , 'YYYY-MM-DD hh24:mi:ss') and trx.\"IdLibroOrdenes\" = asig.\"IdLibroOrdenes\" and asig.\"IdLibroOrdenes\" = "+orderBooks+"";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;
			try (Statement st = con.createStatement();) {
	
						return  st.executeUpdate(query);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "updateProgramm " + e.getMessage(),
					e.getCause());
		}
		return -1;
	}

	public boolean compararFechasConDate(String fecha1, String fechaActual) throws java.text.ParseException {

		boolean resultado = false;

		SimpleDateFormat formateador = new SimpleDateFormat("dd-MM-yyyy");
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
	}

	public int getFechaInSeconds(Connection connection) {
		try {
			Connection con = connection;
			try (Statement st = con.createStatement()) {
				try (ResultSet rs = st.executeQuery("select seconds_between(to_seconddate(to_date(now()) || ' 00:00:00','YYYY-MM-DD hh24:mi:ss'), now()) from dummy");) {
					while (rs.next()) {
						final int seconds = Integer.parseInt(rs.getString(1));
						return seconds;
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "getFecha - select NOW() from dummy");
		}

		return -1;
	}
	public String getDateNowString(Connection connection) {
		try {
			Connection con = connection;
			try (Statement st = con.createStatement()) {
				try (ResultSet rs = st.executeQuery("select now() from dummy");) {
					while (rs.next()) {
					
						return rs.getString(1);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "getFecha - select NOW() from dummy");
		}

		return null;
	}
	public int setFechaUltimaEjecucion(String fecha, Connection connection) throws SQLException
	{
		 
			Connection con = connection;
			try (Statement st = con.createStatement()) {
				try (ResultSet rs = st.executeQuery("update \"BSCL.Orden.BD::ORD.LibroOrdenes\" set \"UltimaFechaDeEjecucion\" = "+fecha+"");) {
					while (rs.next()) {
					 
						return  Integer.parseInt(rs.getString(1));
					}
				}
			}
			return -1;
		 

		 
	}
	public List<Orden> getOrdenes(long jobId, Connection connection) throws SQLException {
		List<Orden> ordenlist = new ArrayList<Orden>();
		final String query = "select \"IdPrograma\", \"IdOrdenPaso\", \"IdClasificacionTipoInstrumento\", \"IdTipoInstrumento\", \"IdBolsa\", \"IdTipoOperacion\", \"idCondicionDeLiquidacion\", \"Precio\", \"CantidadDisponible\", \"CantidadAsignada\", \"Monto\", \"IdTipoCanalDistribucion\", \"FechaIngreso\", \"FechaOperacion\", \"IdLibroOrdenes\", \"IdTipoPrecio\", \"IdTipoCanalIngreso\", \"EspecificaBolsa\", \"IdInstrumento\" from \"BSCL.Orden.BD::ORD.OrdenPaso\" where \"IdPrograma\" = "
				+ jobId + " ORDER BY \"FechaOperacion\" ASC  ";
		try {
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "getOrdenes -" + query);

			Connection con = connection;
 			try (Statement st = con.createStatement()) {

				try (ResultSet rs = st.executeQuery(query);) {
					while (rs.next()) {
 						Orden orden = new Orden(
 								jobId,
 								Integer.parseInt(rs.getString("IdOrdenPaso")),
 								rs.getString("IdClasificacionTipoInstrumento"),
								rs.getString("IdTipoInstrumento"),
								rs.getString("IdBolsa"),
								rs.getString("IdTipoOperacion"),
								rs.getString("idCondicionDeLiquidacion"),
								rs.getDouble("Precio"),
								rs.getDouble("CantidadDisponible"),
								rs.getDouble("CantidadAsignada"),
								rs.getDouble("Monto"),
								rs.getString("IdTipoCanalDistribucion"), 
								rs.getString("FechaIngreso"),
								rs.getString("FechaOperacion"),
								Integer.parseInt(rs.getString("IdLibroOrdenes")),
								rs.getString("IdTipoPrecio"),
								rs.getString("EspecificaBolsa"),
								rs.getInt("IdInstrumento"));
						ordenlist.add(orden);
					}
 
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "getOrdenes -" + e.getMessage());
		}
 
		return ordenlist;
	}

	public List<Transaccion> getTransactions(long jobId, Connection connection) throws SQLException {
		ResultSet rs = null;
		List<Transaccion> transaccionlist = new ArrayList<Transaccion>();
		final String query = "select \"IdPrograma\", \"IdTransaccionPaso\", \"IdClasificacionTipoInstrumento\", \"IdTipoInstrumento\", \"IdBolsa\", \"IdTipoOperacion\", \"idCondicionDeLiquidacion\", \"Precio\", \"CantidadTotal\", \"CantidadDisponible\", \"CantidadAsignada\", \"Monto\", \"FechaIngreso\", \"FechaOperacion\", \"IdInstrumento\"  from \"BSCL.Orden.BD::ORD.TransaccionPaso\" where \"IdPrograma\" ="
				+ jobId + " ORDER BY \"FechaOperacion\" ASC  ";
		try {
			Connection con = connection;
			try (Statement st = con.createStatement()) {

				rs = st.executeQuery(query);
 				while (rs.next()) {
 					Transaccion transaccion = new Transaccion(
 							Integer.parseInt(rs.getString("IdTransaccionPaso")),
 							rs.getString("IdClasificacionTipoInstrumento"),
							rs.getString("IdTipoInstrumento"),
							rs.getString("IdBolsa"),
							rs.getString("IdTipoOperacion"),
							rs.getString("idCondicionDeLiquidacion"),
							Double.parseDouble(rs.getString("Precio")),
							Double.parseDouble(rs.getString("CantidadTotal")),
							Double.parseDouble(rs.getString("CantidadDisponible")),
							Double.parseDouble(rs.getString("CantidadAsignada")),
							Double.parseDouble(rs.getString("Monto")), 
							rs.getString("FechaIngreso"),
							rs.getString("FechaOperacion"),
							rs.getInt("IdInstrumento"));
					        transaccionlist.add(transaccion);
				}
 

			}
		} catch (SQLException e) {
			try {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "No puedo lanzar exception");

			}
		}
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);

		 
		return transaccionlist;
	}

	public void assignMassively(List<Asignacion> assignList, long IdPrograma, Connection connection, ParametrosDefault param) {
		//final String query = String.format("call %s.%s(?,?,?,?,?,?,?,?,?)",getQuotedString(schema),getQuotedString(spAddAssign));
        final String query = "insert into \"BSCL.Orden.BD::ORD.AsignacionResultado\" (\"IdAsignacionResultado\",\"IdEstadoFacturacion\",\"CantidadAsignada\",\"FechaAsignacion\",\"IdTransaccionPaso\",\"IdOrdenPaso\",\"IdPrograma\") values(\"BSCL.Orden.BD.Secuencias::AsignacionResultado\".nextval,'ANF',?,now(),?,?,?) ";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;
			final int batchSize = 1000; 
			int count = 0;
			try (PreparedStatement pst = con.prepareStatement(query)) {
   
				for (Asignacion asig : assignList) {
					 
				 
					pst.setInt(1, asig.getCantidadAsignada()); 
					pst.setInt(2, asig.getIdTransaccionPaso());
					pst.setInt(3, asig.getIdOrdenPaso());
					pst.setLong(4, IdPrograma);
				 
					
					pst.addBatch();
					if(++count%batchSize==0)
					{
						Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "Ejecutando las primera 1000 filas de " + assignList.size());

						pst.executeBatch();
					}
				}
				Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "Ejecutando el restante de filas");

				pst.executeBatch();
				
			}
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);

		} catch (SQLException e) {
			
				e.printStackTrace();       
			 
				Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE,
						"assignMassively " + e.getMessage());

			 
		}   

	}

	public void nullifyMassively(long jobId, Connection connection, ParametrosDefault param) {
		final String createAssingTemporary = createTableTemporary+ tableAssignTemporary+ jobId
				+ " ("+getQuotedString(assignId)+dataTypeBIGINT+")";
		final String insertAssingTemporary = insertInto+tableAssignTemporary + jobId
				+ "("+getQuotedString(assignId)+") SELECT  "+getQuotedString(auxAssignId)+from+getQuotedString(tableAssignAux);
		final String query = "call \"BSCL.Orden.BD.Servicios::AnularAsignacionMasiva\"(1,1,'ORD',#asignacion_in_" + jobId+",?,?,?,?)" ;
		final String sql = String.format("call %s.%s(?,?,?,%s,?,?,?,?)",getQuotedString(schema),getQuotedString(spGetAnulationMassive),tableAssignTemporary+ jobId) ;
 
 		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, sql);
 
		try {
			Connection con = connection;
			try (Statement st = con.createStatement(); Statement createTemporary = con.createStatement()) {
				createTemporary.execute(createAssingTemporary);
				createTemporary.executeUpdate(insertAssingTemporary);
				
				try (CallableStatement cstmt = con.prepareCall(sql)) {
					
					
					cstmt.setLong(1, param.getIdEmpresa());
					cstmt.setLong(2, param.getIdUsuario());
					cstmt.setString(3, param.getModulo());
					cstmt.registerOutParameter(4, Types.NVARCHAR);
		            cstmt.registerOutParameter(5, Types.NVARCHAR);
					}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "nullifyMassively " + e.getMessage(),
					e.getCause());

		}
	}
	public void deleteMassively(String orderBooks, Connection connection) {
		 
		final String query = "delete from \"BSCL.Orden.BD::ORD.Asignacion\" where \"IdEstadoAsignacion\" = 'ANU' and \"IdLibroOrdenes\" = "+orderBooks+" " ;
		try {
			Connection con = connection;
			try (Statement st = con.createStatement();  ) {
 
				 st.executeUpdate(query);

				Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.SEVERE, "deleteMassively " + e.getMessage(),
					e.getCause());

		}
	}

	public boolean bookIsActive(String IdLibroOrdenes, Connection connection) {
		final String query = "select count(1) from \"BSCL.Orden.BD::ORD.LibroOrdenes\" where \"EstadoAsignacionAutomatica\" = 'ACT' and \"IdLibroOrdenes\" = "
				+ IdLibroOrdenes + "";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			Connection con = connection;

			try (Statement st = con.createStatement()) {
				try (ResultSet rs = st.executeQuery(query)) {
					if (rs.next()) {
						return Integer.parseInt(rs.getString(1)) == 1;
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, "bookIsActive" + e.getMessage(),
					e.getCause());
			return false;
		}
		return false;

	}

	public int existAssignAutomaticActive(int IdLibroOrdenes, String TipoPrograma, Connection connection)
			throws SQLException {
		final String query = "select count(1) from \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas\" where \"Valor\" = ? and \"NombreParametro\" = 'IdLibroOrdenes' and (\"idEstado\" = 'PEN' or (\"idEstado\" = 'ASI' and \"IdTipoPrograma\" = ?))";
		Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO, query);
		try {
			try (java.sql.PreparedStatement st = connection.prepareStatement(query)) {
				st.setInt(1, IdLibroOrdenes);
				st.setString(2, TipoPrograma);
				try (ResultSet rs = st.executeQuery()) {
					int resultado = 0;
					if (rs.next()) {
						return Integer.parseInt(rs.getString(1));
					}
					return resultado;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ConnectorsOrders.class.getName()).log(Level.INFO,
					"existAssignAutomaticActive" + e.getMessage(), e.getCause());
			return -1;
		}

	}

	public void setDataSource(DataSource datasource) {
		this.datasource = datasource;
	}

	private DataSource getDataSource() {
		return datasource;
	}

	public Connection getConnection() throws SQLException {
		if (datasource == null)
			System.out.println("Datasource null");
		return getDataSource().getConnection();
	}

}