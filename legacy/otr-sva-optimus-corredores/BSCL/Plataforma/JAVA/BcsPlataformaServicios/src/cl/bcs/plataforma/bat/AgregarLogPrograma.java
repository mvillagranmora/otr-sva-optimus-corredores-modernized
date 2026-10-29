package cl.bcs.plataforma.bat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AgregarLogPrograma {
	
	public static void logica(Connection conn,Long IdPrograma,String IdTipoPrograma,String Mensaje,boolean print) throws SQLException {
		String SQL_AGREGAR_LOG_PROGRAMA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/bat/sql/AgregarLogPrograma_Insert.sql");
		
		SQL sql = new SQL();
		HashMap<String,Object> parmsIN=new HashMap<String,Object>();
		parmsIN.put("IdTipoPrograma", IdTipoPrograma);	
		parmsIN.put("IdPrograma", IdPrograma);
		parmsIN.put("Mensaje", Mensaje);
			
		HashMap<String,Class> parmsOUT=new HashMap<String,Class>();
		
		HashMap<String,Object> respuesta=sql.call(conn, SQL_AGREGAR_LOG_PROGRAMA, parmsIN, parmsOUT);
		
		if (print) {
			System.out.println("Programa " + IdPrograma + "-" + IdTipoPrograma + " // " + Mensaje);
		}
		
	}
	
	

}