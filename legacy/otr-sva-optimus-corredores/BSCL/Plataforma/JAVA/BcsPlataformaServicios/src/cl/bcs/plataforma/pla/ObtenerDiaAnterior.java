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

import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior.FechasOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import datadog.trace.api.Trace;

public class ObtenerDiaAnterior implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(ObtenerDiaAnterior.class);
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
	public static class InObtenerDiaAnterior{
		public Long	idPais;
		public String diaHabil;
		public String soloCaeDiaHabil;
		public boolean excluirConsultarDiaFeriado = false;
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
		public InObtenerDiaAnterior(){
			fechas = new ArrayList<FechasIn>();
			soloCaeDiaHabil = "N";
		}
	}

	public static class OutObtenerDiaAnterior{
		public Long erroresContador;
		public List<FechasOut> fechas;
		public static class FechasOut{
			public String fechaInicial;
			public Long numeroDiasInicial;
			public Long numeroDias;
			public String fechaCalculada;
		}
		public OutObtenerDiaAnterior(){
			fechas = new ArrayList<FechasOut>();
		}
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		ObtenerDiaAnterior o = new ObtenerDiaAnterior();
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getMBConnection();
			InObtenerDiaAnterior entrada = new InObtenerDiaAnterior();
			entrada.idPais = new Long(44);
			entrada.diaHabil = "S";
			InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn();
			f1.fechaInicial = "2019-02-04";
			f1.numeroDias = new Long(1);
			InObtenerDiaAnterior.FechasIn f2 = new InObtenerDiaAnterior.FechasIn();
			f2.fechaInicial = "2019-02-08";
			f2.numeroDias = new Long(1);
			List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<InObtenerDiaAnterior.FechasIn>();
			fechasIn.add(f1);
			fechasIn.add(f2);
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

	/**********************
	 * mÃ©todos bÃ¡sicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InObtenerDiaAnterior in=gson.fromJson(jin.toString() ,InObtenerDiaAnterior.class);
		OutObtenerDiaAnterior out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public OutObtenerDiaAnterior logica(Connection con, InObtenerDiaAnterior entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ObtenerDiaAnterior");
		SQL sql=new SQL();
		OutObtenerDiaAnterior salida = new OutObtenerDiaAnterior();
		salida.erroresContador = new Long(0);
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", entrada.idPais);
		List<DiaFeriado> listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
		for(InObtenerDiaAnterior.FechasIn item : entrada.fechas){
			OutObtenerDiaAnterior.FechasOut temp = new OutObtenerDiaAnterior.FechasOut();
			temp.fechaInicial = item.fechaInicial;
			temp.numeroDiasInicial = item.numeroDias;
			temp.numeroDias = item.numeroDias;
			temp.fechaCalculada = "0001-01-01";
			boolean esinhabil = true;
			boolean buscar = true;
			int diasSumados = 0;
			int diasSumadosReal = 0;
			if(item.numeroDias==null || item.numeroDias<1) buscar=false;
			java.sql.Date fPivot = Fecha.toDate(temp.fechaInicial);
			while(buscar){
				fPivot = Fecha.substractDays(fPivot, 1);
				diasSumados++;
				diasSumadosReal++;
				buscar = false;
				
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
							
							if(entrada.excluirConsultarDiaFeriado == false) {
								if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.toString().equalsIgnoreCase(fer.Fecha.toString()))
										|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
										){
									esinhabil = true;
									break;
								}
							}else {
								if(fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia ){
									esinhabil = true;
									break;
								}
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
				else
					buscar = true;
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
						if(entrada.excluirConsultarDiaFeriado == false) {
							if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.toString().equalsIgnoreCase(fer.Fecha.toString()))
									|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
									){
								esinhabil = true;
								break;
							}
						}else {
							if(fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia ){
								esinhabil = true;
								break;
							}
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
		logger.info("Termino del servicio ObtenerDiaAnterior, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
}
