package cl.bcs.socionegocio.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaCelebracionNacimientosJob extends BcsJob {

	final static String createTemporalTable ="CREATE LOCAL TEMPORARY TABLE #temp_table_asuntos_pendiente ( \"Codigo\"     					NVARCHAR(50)		not null, \"FechaVisibleDesde\"			SECONDDATE			not null,	 \"FechaVisibleHasta\"			SECONDDATE			not null,	 \"IdEmpleado\"					BIGINT		        not null,	 \"Observaciones\" 				NVARCHAR(200)		not null,	 \"TipoEntidad\" 					NVARCHAR(3)			not null,	 \"IdEntidad\"					BIGINT not null,	 \"TipoOperacion\" 				NVARCHAR(3)			not null, \"IdOperacion\"  				BIGINT			not null, \"Glosa\"						NVARCHAR(200)		not null, \"Monto\"						BIGINT not null);";
	public static boolean DEBUG = false;
	
	public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
		if(DEBUG){
			final long idEmpresa = 1;
			final String Codigo = "CUCLI";
			return logica(dbConnection, idEmpresa, Codigo);			
		}else{
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	        final String Codigo = this.getParam("CodigoCumpleanio");
			return logica(dbConnection, idEmpresa, Codigo);
		}
	}
	
	public boolean logica(Connection dbConnection, long idEmpresa, String Codigo){
		System.out.println(":CARGACUMPLEAÃ‘OS:  ****** INICIO  *******");
		
		try {
	        final long idUsuario = 288;//Sistema		
	        String fechaSistema = Utils.getFechaSistema(dbConnection, Utils.getSchema(), idEmpresa);
	        
			System.out.println(":CARGACUMPLEAÃ‘OS:  ****** GET_DATASOURCE  *******");  
			try{
				final String selectParametrosAsunto = "SELECT CURRENT_UTCTIMESTAMP AS \"FECHA\", \"CumplimientoAutomatico\", \"DiasVisibleDesde\", \"DiasVisibleHasta\", \"FormatoGlosa\" FROM \"BSCL.Plataforma.BD::PND.AsuntoPendiente\" WHERE \"Codigo\"=? AND \"IdEmpresa\"=?";
	    		try(PreparedStatement pstmt = dbConnection.prepareStatement(selectParametrosAsunto)){
		    		pstmt.setString(1, Codigo);
		    		pstmt.setLong(2, idEmpresa);
		    		System.out.println(":CARGACUMPLEAÃ‘OS:  ****** GET_FECHA_ACTUAL  *******");  
		    		ResultSet rs = pstmt.executeQuery();
		    		DateFormat fmt = new SimpleDateFormat("MM-dd");
		    		DateFormat fmtFull = new SimpleDateFormat("yyyy-MM-dd");
		    		DateFormat fmtEspaniol = new SimpleDateFormat("dd 'de' MMMM", new Locale("es","ES"));
		    		rs.next();//solo trae un registro
		    			
					Calendar cal = Calendar.getInstance();
					cal.setTime( fmtFull.parse(fechaSistema));
					String mesDia = fmt.format(cal.getTime());
					String mesDiaEspaniol = fmtEspaniol.format(cal.getTime()).toUpperCase();
					System.out.println(rs.toString());
					int diasDesde = rs.getInt("DiasVisibleDesde");
					int diasHasta = rs.getInt("DiasVisibleHasta");
					
					Calendar calDesde = Calendar.getInstance();
					calDesde.setTime( cal.getTime() );
					calDesde.add(Calendar.DAY_OF_MONTH, diasDesde);
					String fechaDesde = fmtFull.format(calDesde.getTime());
					calDesde.add(Calendar.DAY_OF_MONTH, diasHasta);
					String fechaHasta = fmtFull.format(calDesde.getTime());
					String formatoGlosa = rs.getString("FormatoGlosa");
					
					pstmt.close();
					System.out.println(":CARGACUMPLEAÃ‘OS:  ****** PARSE_FECHA: "+mesDia+"  *******");  
					//Ahora cargar todos los clientes con cumpleaÃ±os en una tabla temporal
					try(PreparedStatement pstmt2 = dbConnection.prepareStatement(createTemporalTable);){
						pstmt2.execute();
					}
										    			
					final String selectCargaCelebraciones = "INSERT INTO #temp_table_asuntos_pendiente (\"Codigo\", \"FechaVisibleDesde\", \"FechaVisibleHasta\", \"IdEmpleado\", \"Observaciones\", \"TipoEntidad\",\"IdEntidad\",\"TipoOperacion\",\"IdOperacion\",\"Glosa\", \"Monto\")"
							+ "select "
							+ "'"+Codigo+"' as \"Codigo\", "
							+ "'"+fechaDesde+"' as \"FechaVisibleDesde\", "
							+ "'"+fechaHasta+"' as \"FechaVisibleHasta\", "
							+ "\"IdEmpleadoEjecutivo\" AS \"IdEmpleado\", "
							+ "'' AS \"Observaciones\", "
							+ "'CLI' AS \"TipoEntidad\", "
							+ "\"IdCliente\" AS \"IdEntidad\", "
							+ "'' AS \"TipoOperacion\", "
							+ "0 AS \"IdOperacion\", "
							+ "REPLACE(REPLACE(REPLACE('"+formatoGlosa+"', '#nombre#', \"NombreCompleto\"), '#anios#',CAST( TO_INT(DAYS_BETWEEN(\"FechaNacimiento\", CURRENT_UTCTIMESTAMP)/365) AS VARCHAR)),'#fecha#','"+mesDiaEspaniol+"') AS \"Glosa\","
							+ "0 AS \"Monto\" "
							+ "from \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" "
							+ "where  \"IdEstado\" = 'ACT' and TO_VARCHAR(\"FechaNacimiento\", 'MM-DD') = ? and \"IdEmpresa\"=?";
					
					
			
					
					try(PreparedStatement pstmt3 = dbConnection.prepareStatement(selectCargaCelebraciones)){
						System.out.println(selectCargaCelebraciones);
						pstmt3.setString(1, mesDia);
						pstmt3.setLong(2, idEmpresa);
						pstmt3.execute();
						System.out.println(":CARGACUMPLEAÃ‘OS:  ****** TO_TEMP_TABLE  *******");  
					}
					
					//Realizar el ingreso de asuntos pendientes 
					String callSpCrearAsuntoPendiente = "{CALL \"BSCL.Plataforma.BD.Servicios::PND.CrearAsuntoPendiente\" (?,?,?,?,#temp_table_asuntos_pendiente,  ?,?,?,?,?)}";
					try(CallableStatement cstmt = dbConnection.prepareCall(callSpCrearAsuntoPendiente);){
						cstmt.setLong(1, idEmpresa);
						cstmt.setLong(2, idUsuario);
						cstmt.setString(3, "PLA");
						cstmt.setString(4, Codigo);
						cstmt.registerOutParameter(5, Types.BIGINT);
						cstmt.registerOutParameter(6, Types.INTEGER);
						cstmt.registerOutParameter(7, Types.INTEGER);
						cstmt.execute();
						//System.out.println(":CARGACUMPLEAÃ‘OS:  ****** EXEC IN: "+cstmt.getInt(1)+"  *******");  
					}
					//Ejecutar procedimiento para vencer automaticamente los cumpleaÃ±os mediante el codigo
					String callSpVencerAsuntoPendiente = "{CALL \"BSCL.Plataforma.BD.Servicios::PND.VencerAsuntosPendientes\"(?,?,?,?,?,?,?,?,?)}";
					try(CallableStatement cstmtV = dbConnection.prepareCall(callSpVencerAsuntoPendiente);){
						cstmtV.setLong(1, idEmpresa);
						cstmtV.setLong(2, idUsuario);
						cstmtV.setString(3, "PLA");
						cstmtV.setString(4, Codigo);
						cstmtV.registerOutParameter(5, Types.BIGINT);
						cstmtV.registerOutParameter(6, Types.INTEGER);
						cstmtV.registerOutParameter(7, Types.INTEGER);
						cstmtV.execute();
						//System.out.println(":CARGACUMPLEAÃ‘OS:  ****** ASUNTOS VENCIDOS AUTOMATICO:: "+cstmtV.getInt(1)+"  *******");
					}
					//BORRAR TABLA TEMPORAL
					try(PreparedStatement pstmt4 = dbConnection.prepareStatement("DROP TABLE #temp_table_asuntos_pendiente");){
						pstmt4.execute();
					}
					
					//OK, Grabar operacion
					System.out.println(":CARGACUMPLEAÃ‘OS:  ****** COMMIT  *******");  
					return true;
	    		}
	    	}catch(Exception ex){
	    		ex.printStackTrace();
	    		System.out.println(":CARGACUMPLEAÃ‘OS:  ****** ROLLBACK  *******");  
	    		System.out.println("EXCEPTION AL OBTENER LOS CUMPLEAÃ‘OS DE LOS CLIENTES");
	    	}
	
		} catch (Exception e) {
			System.out.println(":CARGACUMPLEAÃ‘OS:  ****** ERROR AL OBTENER DATASOURCE  *******");  
			System.out.println(e.getMessage());
		}
		return false;
	}
	 
	@Override
	public void onError(Throwable t) {
	    System.out.println(":CARGACUMPLEAÃ‘OS:  ****** ERROR  *******");  
	    t.printStackTrace();
	}
	
	@Override
	public void pause() {
	
	}
	//solo para test
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				DEBUG = true;
				CargaCelebracionNacimientosJob apf = new CargaCelebracionNacimientosJob();
				System.out.println(apf.transaction(con));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}