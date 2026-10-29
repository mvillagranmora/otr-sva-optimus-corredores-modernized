package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores.FechasOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import datadog.trace.api.Trace;

public class ObtenerDiasHabilesAnteriores implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(ObtenerDiasHabilesAnteriores.class);
	//QUERIES
	private final String SQL_CONSULTAR_FERIADOS = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
	
	//POJOS
	public static class DiaFeriado {
		public Long			IdFeriado; 
		public String		Tipo; 
		public java.sql.Date Fecha; 
		public Integer		Dia; 
		public String		DescripcionDia; 
		public Integer		AgnoCarga; 
		public Long			IdPais; 
	}
	//ENTRADA Y SALIDA DE LA CLASE
	public static class InObtenerDiasHabilesAnteriores{
		public Long	idPais;
		public String diaHabil;
		public String soloCaeDiaHabil;
		public List<FechasIn> fechas;
		public static class FechasIn{
			public FechasIn() {}
			public FechasIn(String fechaInicial, Long numeroDias) {
				this.fechaInicial = fechaInicial;
				this.numeroDias = numeroDias;
			}
			public String fechaInicial;
			public Long numeroDias;
		}
		public InObtenerDiasHabilesAnteriores(){
			fechas = new ArrayList<FechasIn>();
			soloCaeDiaHabil = "N";
		}
	}

	public static class OutObtenerDiasHabilesAnteriores{
		public Long erroresContador;
		public List<FechasOut> fechas;
		public static class FechasOut{
			public String fechaInicial;
			public Long numeroDiasInicial;
			public Long numeroDias;
			public String fechaCalculada;
		}
		public OutObtenerDiasHabilesAnteriores(){
			fechas = new ArrayList<FechasOut>();
		}
	}
	
	

	/**********************
	 * mÃ©todos bÃ¡sicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InObtenerDiasHabilesAnteriores in=gson.fromJson(jin.toString() ,InObtenerDiasHabilesAnteriores.class);
		OutObtenerDiasHabilesAnteriores out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public OutObtenerDiasHabilesAnteriores logica(Connection con, InObtenerDiasHabilesAnteriores entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ObtenerDiasHabilesAnteriores");
		SQL sql=new SQL();
		OutObtenerDiasHabilesAnteriores salida = new OutObtenerDiasHabilesAnteriores();
		salida.erroresContador = new Long(0);
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", entrada.idPais);
		List<DiaFeriado> listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
		for(InObtenerDiasHabilesAnteriores.FechasIn item : entrada.fechas){
			OutObtenerDiasHabilesAnteriores.FechasOut temp = new OutObtenerDiasHabilesAnteriores.FechasOut();
			temp.fechaInicial = item.fechaInicial;
			temp.numeroDiasInicial = item.numeroDias;
			temp.numeroDias = item.numeroDias;
			temp.fechaCalculada = "0001-01-01";
			boolean esinhabil = true;
			int diasSumados = 0;
			int diasSumadosReal = 0;
			int diasrestar = item.numeroDias.intValue();
			java.sql.Date fPivot = Fecha.toDate(temp.fechaInicial);
			for(int i = diasrestar; i > 0; i--) {
				
				fPivot = Fecha.substractDays(fPivot, 1);
				diasSumados++;
				diasSumadosReal++;
				
				if(entrada.diaHabil.equals("S")){
					Calendar c = Calendar.getInstance();
					
					esinhabil = true;
					while (esinhabil) {
						esinhabil = false;
						
						c.setTime(fPivot);
						int day = c.get(Calendar.DAY_OF_WEEK); //java domingo  = 1, optimus lunes = 0
						if (day == 1) {
							day = 6;
						}
						else {
							day = day - 2;
						}
						
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
				if (diasSumados == item.numeroDias)
					break;
			} 
			
		
			temp.fechaCalculada = Fecha.toStringDate(fPivot);
			temp.numeroDias = (long) diasSumadosReal;
			salida.fechas.add(temp);
			
		}
			
		
		if (entrada.diaHabil.equalsIgnoreCase("N") && entrada.soloCaeDiaHabil.equalsIgnoreCase("S")) {
			
			for (FechasOut out : salida.fechas) {
				
				boolean esinhabil = true;
				Calendar c = Calendar.getInstance();
				java.sql.Date fPivot = Fecha.toDate(out.fechaCalculada);
					
				while (esinhabil) {
					esinhabil = false;
					
					c.setTime(fPivot);
					int day = c.get(Calendar.DAY_OF_WEEK); //java domingo  = 1, optimus lunes = 0
					if (day == 1) {
						day = 6;
					}
					else {
						day = day - 2;
					}
					
					for(DiaFeriado fer : listaDiaFeriado){
						if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.equals(fer.Fecha))
								|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
								){
							esinhabil = true;
							break;
						}
					}
					
					if (esinhabil) {
						fPivot = Fecha.addDays(fPivot, 1);
						out.numeroDias++;
					}
				}
				
				out.fechaCalculada = Fecha.toStringDate(fPivot);
			}
		}
		logger.info("Termino del servicio ObtenerDiasHabilesAnteriores, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		ObtenerDiasHabilesAnteriores o = new ObtenerDiasHabilesAnteriores();
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			InObtenerDiasHabilesAnteriores entrada = new InObtenerDiasHabilesAnteriores();
			entrada.idPais = new Long(44);
			entrada.diaHabil = "S";
			entrada.soloCaeDiaHabil="S";
			InObtenerDiasHabilesAnteriores.FechasIn f1 = new InObtenerDiasHabilesAnteriores.FechasIn();
			f1.fechaInicial = "2022-01-30";
			f1.numeroDias = new Long(30);
			
			List<InObtenerDiasHabilesAnteriores.FechasIn> fechasIn = new ArrayList<InObtenerDiasHabilesAnteriores.FechasIn>();
			fechasIn.add(f1);
			entrada.fechas = fechasIn;
			logger.debug("Entrada:");
			logger.debug(entrada);
			logger.debug("Salida:");
			logger.debug(o.logica(con,entrada));
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
}
