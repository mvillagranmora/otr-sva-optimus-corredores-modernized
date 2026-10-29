package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ObtenerPlazo implements JavaCall{	
	private final Log log = new Log("ObtenerPlazo");
	public List<DiaFeriado> listaDiaFeriado = null;
	//QUERIES
	private final String SQL_CONSULTAR_FERIADOS = loadResourceToString("cl/bcs/plataforma/pla/sql/ObtenerPlazo_SelectFeriados.sql");
	private final String SQL_INSERT_TEMP_FECHAS = "INSERT INTO \"BSCL.Plataforma.BD::PLA.ObtenerPlazo_Fechas\" VALUES(:IdInterno, :FechaDesde, :FechaHasta, :SoloDiasHabiles, :Plazo);";
	//POJOS
	public static class DiaFeriado {
		public Long			IdFeriado; 
		public String		Tipo; 
		public java.sql.Date Fecha; 
		public Integer		Dia; 
		public String		DescripcionDia; 
		public Integer		AgnoCarga; 
		public Long			IdPais; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//ENTRADA Y SALIDA DE LA CLASE
	public static class InObtenerPlazo{
		public List<FechasIn> p_Fechas;
		public Long p_IdPais;
		// CON VALOR "S" Ingresa las fechas en tabla "BSCL.Plataforma.BD::PLA.ObtenerPlazo_Fechas";
		public String p_EnTablaTemporal = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public InObtenerPlazo(){
			p_Fechas = new ArrayList<FechasIn>();
		}
	}
	
	public static class FechasIn{
		public Long IdInterno;
		public String FechaDesde;
		public String FechaHasta;
		public String SoloDiasHabiles;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class OutObtenerPlazo {
		public List<FechasOut> p_out_Fechas;
		
		public OutObtenerPlazo(){
			p_out_Fechas = new ArrayList<FechasOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String toString(boolean pretty) {
			return Utils.printJson(this, pretty);
		}
	}
	
	public static class FechasOut{
		
		public Long IdInterno;
		public String FechaDesde;
		public String FechaHasta;
		public String SoloDiasHabiles;
		public Long Plazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		//prueba de la clase
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			InObtenerPlazo entrada = new InObtenerPlazo();
			entrada.p_IdPais = 44L;
			
			FechasIn f1 = new FechasIn();
			f1.FechaDesde = "2017-08-17";
			f1.FechaHasta = "2017-08-31";
			f1.IdInterno = 0L;
			f1.SoloDiasHabiles = "S";
			entrada.p_Fechas.add(f1);
			
			FechasIn f2 = new FechasIn();
			f2.FechaDesde = "2017-08-16";
			f2.FechaHasta = "2017-08-20";
			f2.IdInterno = 0L;
			f2.SoloDiasHabiles = "N";
			entrada.p_Fechas.add(f2);
			
			
			ObtenerPlazo oo = new ObtenerPlazo();
			
			System.out.println(oo.logica(con,entrada).toString(true));
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		throw new Exception("no implementado");
	}
	
	private FechasOut buscar(FechasIn item) {
		FechasOut temp = new FechasOut();
		
		java.sql.Date dateFechaDesde= Fecha.toDate(item.FechaDesde);
		java.sql.Date dateFechaHasta= Fecha.toDate(item.FechaHasta);
		
		temp.FechaDesde = item.FechaDesde;
		temp.FechaHasta = item.FechaHasta;
		temp.IdInterno = item.IdInterno;
		temp.Plazo = 0L;
		temp.SoloDiasHabiles = item.SoloDiasHabiles;
		
		boolean buscar = true;
		if(dateFechaDesde.compareTo(dateFechaHasta) == 0 || dateFechaDesde.compareTo(dateFechaHasta) == 1) { 
			buscar=false;
		}
		int diasSumadosReal = 0;
		int diasSumados = 0;
		boolean esinhabil = true;
		java.sql.Date fPivot = Fecha.toDate(temp.FechaHasta);
		while(buscar){
			
			fPivot = Fecha.substractDays(fPivot, 1);
			diasSumadosReal++;
			diasSumados++;
			
			if(item.SoloDiasHabiles.equals("S")){
				
				Calendar c = Calendar.getInstance();
				
				esinhabil = true;
				while (esinhabil) {
					esinhabil = false;
					
					c.setTime(fPivot);
					int day = c.get(Calendar.DAY_OF_WEEK); //java domingo  = 1, optimus lunes = 0
					if (day == 1)
						day = 6;
					else
						day = day - 2;
					
					for(DiaFeriado fer : listaDiaFeriado){
						if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.equals(fer.Fecha))
								|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
								){
							esinhabil = true;
							break;
						}
					}
					
					if (esinhabil) {
						fPivot = Fecha.substractDays(fPivot, 1);
						diasSumadosReal++;
					}
				}
			}
			
			if (dateFechaDesde.compareTo(fPivot) == 0) {
			//if (item.FechaDesde.equals(Fecha.toStringDate(fPivot))) {
				break;
			}
			
			//por si nos pasamos
			if (dateFechaDesde.compareTo(fPivot) == 1) {
			//if (Fecha.toDate(item.FechaDesde).compareTo(fPivot) == 1) {
				break;
			}
			
			//if(!buscar) temp.fechaCalculada = df.format(fPivot);
		}
		temp.Plazo = (long) diasSumados;
		return temp;
	}
	
	@Trace
	public OutObtenerPlazo logica(Connection con, InObtenerPlazo entrada) throws Exception {
		SQL sql=new SQL();
		OutObtenerPlazo salida = new OutObtenerPlazo();
		
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", entrada.p_IdPais);
		listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
		salida.p_out_Fechas = entrada.p_Fechas.stream().parallel().map(t -> buscar(t)).collect(Collectors.toList());
		if(entrada.p_EnTablaTemporal.equals("S")) {
			sql.batch(con, SQL_INSERT_TEMP_FECHAS, salida.p_out_Fechas, FechasOut.class);
		}
		return salida;
	}

	
	
}
