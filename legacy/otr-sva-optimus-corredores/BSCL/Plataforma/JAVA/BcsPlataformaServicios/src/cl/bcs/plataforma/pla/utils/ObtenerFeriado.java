package cl.bcs.plataforma.pla.utils;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import datadog.trace.api.Trace;

public class ObtenerFeriado implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ObtenerFeriado.class);
	
	private final String SQL_CONSULTAR_FERIADOS = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
			
	public static class DiaFeriado {
		public Long				IdFeriado; 
		public String			Tipo; 
		public java.sql.Date 	Fecha; 
		public Integer			Dia; 
		public String			DescripcionDia; 
		public Integer			AgnoCarga; 
		public Long				IdPais; 
	}
	
//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public Long IdEmpresa;
		public String fechaConsulta;
		public List<DiasAConsultar> fechasConsulta;//EN CASO DE QUERER CONSULTAR MAS DE UNA FECHA
		public ParametrosEntradaServicio(){
			fechasConsulta = new ArrayList<DiasAConsultar>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	public static class DiasAConsultar{
		public Long IdInterno;
		public String fechaConsulta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class DiasAConsultarSalida{
		public Long IdInterno;
		public String fechaConsulta;
		public Boolean habil;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public Boolean habil;
		public List<DiasAConsultarSalida> fechasConsultaOut;//EN CASO DE QUERER CONSULTAR MAS DE UNA FECHA
		
		public ParametrosSalidaServicio(){
			fechasConsultaOut = new ArrayList<DiasAConsultarSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ParametrosSalidaServicio logica (Connection con, ParametrosEntradaServicio in) throws Exception {
		
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 	= new ParametrosSalidaServicio ();
		
		String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		
		boolean esHabil = true;
		
		// Consulta feriados para el pais
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdPais",  Long.parseLong(paisSistema));
		List<DiaFeriado> lstDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,param,DiaFeriado.class);
		if(in.fechasConsulta.size()>0) {
			for(DiasAConsultar dia: in.fechasConsulta) {
				Calendar c = Calendar.getInstance();
				DiasAConsultarSalida fechaSalida = new DiasAConsultarSalida();
				java.sql.Date fPivot = Fecha.toDate(dia.fechaConsulta);
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
				fechaSalida.IdInterno= dia.IdInterno;
				fechaSalida.fechaConsulta = dia.fechaConsulta;
				fechaSalida.habil= esHabil;
				salida.fechasConsultaOut.add(fechaSalida);
			}
		}
		else {
			java.sql.Date fPivot = Fecha.toDate(in.fechaConsulta);
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
					salida.habil= esHabil;
		}
		
				
		logger.info("Termino de Obtener fecha, tiempo:" + (System.currentTimeMillis() - init));		
		
		return salida;
		
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();				
				String json = "{\"IdEmpresa\":1,\"fechaConsulta\":\"2020-05-30\"}";
				
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				ObtenerFeriado demo = new ObtenerFeriado();		 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 			    con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
	
	
}



