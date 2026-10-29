
package cl.bcs.plataforma.pla.utils;


import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;

public class Feriados {
	
	private static FileLoader loader;
	private static String SQL_CONSULTAR_FERIADOS;
    
	
	public static class DiaFeriado {
		public Long				IdFeriado; 
		public String			Tipo; 
		public java.sql.Date 	Fecha; 
		public Integer			Dia; 
		public String			DescripcionDia; 
		public Integer			AgnoCarga; 
		public Long				IdPais; 
	}
	

	


	
	public static boolean esDiaHabil(Connection con, String fechaConsulta, Long idPais) throws Exception {
		SQL sql=new SQL();
		loader = new FileLoader();
		SQL_CONSULTAR_FERIADOS = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
	
		java.sql.Date fPivot = Fecha.toDate(fechaConsulta);
		boolean esHabil = true;
		
		// Consulta feriados para el pais
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdPais", idPais);
		List<DiaFeriado> lstDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,param,DiaFeriado.class);
		
		//Calcular si es dia habil
		Calendar c = Calendar.getInstance();
		c.setTime(fPivot);
		int day = c.get(Calendar.DAY_OF_WEEK); //java domingo  = 1, optimus lunes = 0
		if (day == 1) {
			day = 6;
		}
		else {
			day = day - 2;
		}
		
		for(DiaFeriado fer : lstDiaFeriado){
			if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.equals(fer.Fecha))
					|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
					){
				esHabil = false;
				break;
			}
		}
		
		
		return esHabil;
	}
	

	
}