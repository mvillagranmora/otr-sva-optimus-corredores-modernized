package cl.bcs.ordenes.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JobLogger {
	public static void logAuditoria(Connection conn,String modulo,long idUsuario,long idEmpresa,long idUsuarioAtendido,
			String idTipoRegistro,long idProceso,String estadoProceso,String nombreProceso,String idTipoPrograma) {
		
		PreparedStatement pstmt = null;
		final String callSpLogAuditoria = "CALL \"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogPrograma\"("
				+ "p_IdTipoLog 			=> 'INFORMATIVO',"
				+ "p_IdModulo 			=> ?,"
				+ "p_IdUsuario 			=> ?,"
				+ "p_IdEmpresa 			=> ?,"
				+ "p_IdUsuarioAtendido 	=> ?,"
				+ "p_IdTipoRegistro 	=> ?,"
				+ "p_IdProceso 			=> ?,"
				+ "p_EstadoProceso 		=> ?,"
				+ "p_NombreProceso 		=> ?,"
				+ "p_IdTipoPrograma 	=> ?"				
				+ ")";
		try {
			pstmt = conn.prepareStatement(callSpLogAuditoria);
			pstmt.setString(1, modulo); 
			pstmt.setLong(2, idUsuario); 
			pstmt.setLong(3, idEmpresa); 
			pstmt.setLong(4, idUsuario);
			pstmt.setString(5, idTipoRegistro);
			pstmt.setLong(6, idProceso);
			pstmt.setString(7, estadoProceso);
			pstmt.setString(8, idTipoPrograma); 
			pstmt.setString(9, idTipoPrograma); 
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		
	
	}
	public static void logDebug(Connection conn,String idTipoPrograma,long idPrograma , String mensaje) {
		String callLog = "CALL \"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogDebugPrograma\"( p_IdPrograma => ?,p_IdTipoPrograma => ?, p_Mensaje => ?)";
		PreparedStatement ps = null;
		try {
			System.out.println("Programa:" + idTipoPrograma + " Id " + idPrograma + ": " + mensaje);
			ps = conn.prepareStatement(callLog);
			ps.setLong(1, idPrograma);
			ps.setString(2, idTipoPrograma);
			ps.setString(3, mensaje);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}