package cl.bcs.socionegocio.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;

public class ValidacionesSocioNegocio {
	/*
	 * Clases utilitarias
	 */
	public static class Contador{
		public long Count;
		@Override
		public String toString() {
			return String.format("Contador [Count=%s]", Count);
		}
	}
	
	/*
	 * Funciones para validar la creacion de socios de negocio
	 */
	public String SQL_ConsultarExistenciaEmpleadoUsuario = "SELECT COUNT(1) AS \"Count\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" as USU  INNER JOIN \"BSCL.SocioNegocio.BD::EMP.Empleado\" as EMP ON (EMP.\"IdEmpleado\" = USU.\"IdEntidad\") WHERE USU.\"TipoEntidad\" = 'EMP' and USU.\"IdUsuario\" = :p_IdUsuario and USU.\"IdEmpresa\" = :p_IdEmpresa;";
	public boolean existeEmpleado(Connection conn, long idUsuario, long idEmpresa) throws SQLException{
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdUsuario", idUsuario);
		params.put("p_IdEmpresa", idEmpresa);
		List<Contador> contadorEmpleado = sql.query(conn, SQL_ConsultarExistenciaEmpleadoUsuario, params, Contador.class);
		if(!contadorEmpleado.isEmpty() && contadorEmpleado.size()==1){
			return true;
		}else{
			return false;
		}
	}
	 
}
