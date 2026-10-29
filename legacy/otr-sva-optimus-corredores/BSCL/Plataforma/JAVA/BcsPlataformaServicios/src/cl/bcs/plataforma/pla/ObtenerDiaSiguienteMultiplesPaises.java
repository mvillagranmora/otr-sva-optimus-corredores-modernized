package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises.FechasOut;
import cl.bcs.plataforma.pla.pojo.FechasIn;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ObtenerDiaSiguienteMultiplesPaises implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ObtenerDiaSiguienteMultiplesPaises.class);
	
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
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//ENTRADA Y SALIDA DE LA CLASE
	public static class InObtenerDiaSiguienteMultiplesPaises extends BcsPojo{
		public String p_diaHabil;					//al sumar dias, considera solo dias habiles
		public String p_soloCaeDiaHabil;			//el resultado debe ser un dia habil
		public List<FechasIn> p_fechas;
		public String p_operacionSpot;				//valida si operacion viene de SPOT
		public Long p_paisSistema;					//Se usa para determinar si feriado es Nacional o Internacional
		
		public InObtenerDiaSiguienteMultiplesPaises(){
			p_fechas = new ArrayList<FechasIn>();
			p_operacionSpot = "N"; 		
			p_paisSistema = null;
		}
	}

	public static class OutObtenerDiaSiguienteMultiplesPaises{
		public Long P_OUT_ERRORESCONTADOR;
		public List<FechasOut> p_out_fechas;
		public static class FechasOut{
			public Long idInterno;
			public String fechaInicial;
			public Long numeroDiasInicial;
			public Long numeroDias;
			public String fechaCalculada;
			public List<Long> idPaises;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public OutObtenerDiaSiguienteMultiplesPaises(){
			p_out_fechas = new ArrayList<FechasOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FeriadoInhabilSpot {
		public boolean esFeriadoNacional = false;
		public boolean esFeriadoInternacional = false;
		public boolean esInhabil = false;
	}	
	
	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();	
		InObtenerDiaSiguienteMultiplesPaises in=gson.fromJson(jin.toString() ,InObtenerDiaSiguienteMultiplesPaises.class);
		OutObtenerDiaSiguienteMultiplesPaises out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public OutObtenerDiaSiguienteMultiplesPaises logica(Connection con, InObtenerDiaSiguienteMultiplesPaises entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ObtenerDiaSiguiente");
		
		SQL sql=new SQL();
		OutObtenerDiaSiguienteMultiplesPaises salida = new OutObtenerDiaSiguienteMultiplesPaises();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		entrada.p_soloCaeDiaHabil = entrada.p_diaHabil.equalsIgnoreCase("S") ? "S" : entrada.p_soloCaeDiaHabil == null ? "N" : entrada.p_soloCaeDiaHabil;
		
		List<Long> paises = new ArrayList<>();
		for (FechasIn f : entrada.p_fechas) {
			for(Long pais : f.paises) {
				paises.add(pais);
			}
		}
		paises = paises.parallelStream().distinct().collect(Collectors.toList());
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", paises);
		List<DiaFeriado> listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
		
		HashMap<String, DiaFeriado> hashDiaFeriado = new HashMap<String, DiaFeriado>();
		HashMap<String, DiaFeriado> hashDiaInhabil = new HashMap<String, DiaFeriado>();
		for (DiaFeriado diaFeriado : listaDiaFeriado) {
			if ("FERIADO".equals(diaFeriado.Tipo) && !hashDiaFeriado.containsKey(diaFeriado.Tipo+"_"+diaFeriado.Fecha)) {
				hashDiaFeriado.put(diaFeriado.Tipo+"_"+diaFeriado.Fecha+"_"+diaFeriado.IdPais, diaFeriado);
			}
			if ("INHABIL".equals(diaFeriado.Tipo) && !hashDiaInhabil.containsKey(diaFeriado.Tipo+"_"+diaFeriado.Dia)) {
				hashDiaInhabil.put(diaFeriado.Tipo+"_"+diaFeriado.Dia+"_"+diaFeriado.IdPais, diaFeriado);
			}
		}
		
		if(entrada.p_operacionSpot.equals("S")){
			for(FechasIn item : entrada.p_fechas){
				//busca los feriados nacionales y/o internacionales y dias inhabiles en proceso y adelanta si lo son
				OutObtenerDiaSiguienteMultiplesPaises.FechasOut temp = new OutObtenerDiaSiguienteMultiplesPaises.FechasOut();
				temp.fechaInicial = item.fechaInicial;
				temp.numeroDiasInicial = item.numeroDias;
				temp.numeroDias = item.numeroDias;
				temp.fechaCalculada = item.fechaInicial;
				
				boolean buscar = item.numeroDias == null || item.numeroDias <= 0 ? false : true;
				int diasSumadosReal = 0;
				boolean esinhabil = true;
				java.sql.Date fPivot = Fecha.toDate(temp.fechaInicial);
				
				while(buscar){
					diasSumadosReal++;
					fPivot = Fecha.addDays(fPivot, 1);
					
					Calendar c = Calendar.getInstance();
					esinhabil = true;
					while (esinhabil) {
						esinhabil = false;
						c.setTime(fPivot);
						int day = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2; //java domingo  = 1, optimus lunes = 0
						FeriadoInhabilSpot feriadoInhabilSpot = new FeriadoInhabilSpot();
						esFeriadoInhabilSpot(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,item.paises, feriadoInhabilSpot, entrada.p_paisSistema);
						
						if (feriadoInhabilSpot.esInhabil){
							esinhabil = true;
							fPivot = Fecha.addDays(fPivot, 1);
						}
						else if (feriadoInhabilSpot.esFeriadoNacional){
							esinhabil = true;
							fPivot = Fecha.addDays(fPivot, 1);
						}
						else if (feriadoInhabilSpot.esFeriadoInternacional) {
							esinhabil = true;
							fPivot = Fecha.addDays(fPivot, 1);
							diasSumadosReal++;
						}
					}
					
					if (diasSumadosReal >= item.numeroDias)
						break;
					
				}
				
				temp.fechaCalculada = Fecha.toStringDate(fPivot);
				temp.numeroDias = (long) diasSumadosReal;
				temp.idPaises = item.paises; 
				temp.idInterno = item.idInterno;
				salida.p_out_fechas.add(temp);
			}			
		}
		else {
			for(FechasIn item : entrada.p_fechas){
				//busca los feriados del dia en proceso
				OutObtenerDiaSiguienteMultiplesPaises.FechasOut temp = new OutObtenerDiaSiguienteMultiplesPaises.FechasOut();
				temp.fechaInicial = item.fechaInicial;
				temp.numeroDiasInicial = item.numeroDias;
				temp.numeroDias = item.numeroDias;
				temp.fechaCalculada = item.fechaInicial;
				
				boolean buscar = item.numeroDias == null || item.numeroDias <= 0 ? false : true;
				int diasSumados = 0;
				int diasSumadosReal = 0;
				boolean esinhabil = true;
				java.sql.Date fPivot = Fecha.toDate(temp.fechaInicial);
				
				while(buscar){
					fPivot = Fecha.addDays(fPivot, 1);
					diasSumados++;
					diasSumadosReal++;
					
					if(entrada.p_diaHabil.equals("S")){
						Calendar c = Calendar.getInstance();
						esinhabil = true;
						while (esinhabil) {
							esinhabil = false;
							c.setTime(fPivot);
							int day = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2; //java domingo  = 1, optimus lunes = 0
							
							/*for(DiaFeriado fer : listaDiaFeriado){
								if((fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.equals(fer.Fecha)) || (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )){
									esinhabil = true;
									break;
								}
							}*/
							if (esInhabil(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,item.paises)){
								esinhabil = true;
								fPivot = Fecha.addDays(fPivot, 1);
								diasSumadosReal++;
							}
							/*if (esinhabil) {
								fPivot = Fecha.addDays(fPivot, 1);
								diasSumadosReal++;
							}*/
						}
					}
					
					if (diasSumados == item.numeroDias)
						break;
				}
				temp.fechaCalculada = Fecha.toStringDate(fPivot);
				temp.numeroDias = (long) diasSumadosReal;
				temp.idPaises = item.paises; 
				temp.idInterno = item.idInterno;
				salida.p_out_fechas.add(temp);
			} 
			
			if (entrada.p_diaHabil.equalsIgnoreCase("N") && entrada.p_soloCaeDiaHabil.equalsIgnoreCase("S")) {
				for (FechasOut out : salida.p_out_fechas) {
					boolean esinhabil = true;
					Calendar c = Calendar.getInstance();
					java.sql.Date fPivot = Fecha.toDate(out.fechaCalculada);
						
					while (esinhabil) {
						esinhabil = false;
						c.setTime(fPivot);
						int day = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2; //java domingo  = 1, optimus lunes = 0
						
						/*for(DiaFeriado fer : listaDiaFeriado){
							if( (fer.Tipo.equalsIgnoreCase("FERIADO") && fPivot.equals(fer.Fecha))
									|| (fer.Tipo.equalsIgnoreCase("INHABIL") && day == fer.Dia )
									){
								esinhabil = true;
								break;
							}
						}*/
						if (esInhabil(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,out.idPaises)) {
							esinhabil = true;
							fPivot = Fecha.addDays(fPivot, 1);
							out.numeroDias++;
						}
						/*if (esinhabil) {
							fPivot = Fecha.addDays(fPivot, 1);
							out.numeroDias++;
						}*/
					}
					
					out.fechaCalculada = Fecha.toStringDate(fPivot);
				}
			}
		}
		logger.info("Termino del servicio ObtenerDiaSiguiente, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	public java.sql.Date sumarDia(String fecha,Long dias){
		if(fecha==null)return null;
		java.sql.Date fActual = java.sql.Date.valueOf(fecha);
		int d = (dias==null)?0:dias.intValue();
		Calendar c = Calendar.getInstance(); 
		c.setTime(fActual); 
		c.add(Calendar.DATE,d);
		java.sql.Date fCalculada = new java.sql.Date(c.getTime().getTime());
		return fCalculada;
	}
	
	private Boolean esInhabil(HashMap<String, DiaFeriado> hashDiaFeriado, HashMap<String, DiaFeriado> hashDiaInhabil, String fecha, int dia, List<Long> idPaises) {
		Boolean esInhabil = false;
		for(Long idPais : idPaises) {
			esInhabil = hashDiaFeriado.get("FERIADO_"+fecha+"_"+idPais) != null || hashDiaInhabil.get("INHABIL_"+dia+"_"+idPais) != null;
			if(esInhabil) {
				return esInhabil;
			}
		}
		return esInhabil;
	}
	
	private FeriadoInhabilSpot esFeriadoInhabilSpot(HashMap<String, DiaFeriado> hashDiaFeriado, HashMap<String, DiaFeriado> hashDiaInhabil, String fecha, int dia, List<Long> idPaises, FeriadoInhabilSpot feriadoInhabilSpot, Long idPaisSistema) {	
		for(Long idPais : idPaises) {
			if(!feriadoInhabilSpot.esInhabil) feriadoInhabilSpot.esInhabil = hashDiaInhabil.get("INHABIL_"+dia+"_"+idPais) != null;
			if(!feriadoInhabilSpot.esFeriadoNacional && idPais==idPaisSistema) feriadoInhabilSpot.esFeriadoNacional = hashDiaFeriado.get("FERIADO_"+fecha+"_"+idPais) != null;
			if(!feriadoInhabilSpot.esFeriadoInternacional && idPais != idPaisSistema) feriadoInhabilSpot.esFeriadoInternacional = hashDiaFeriado.get("FERIADO_"+fecha+"_"+idPais) != null;
		}
		return feriadoInhabilSpot;
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getBOLSAConnection();
			String json = "{\"p_diaHabil\":\"S\",\"p_soloCaeDiaHabil\":\"S\",\"p_fechas\":[{\"fechaInicial\":\"2023-03-22\",\"numeroDias\":1,\"paises\":[44,223]}]}";
			//String json = "{\"p_idPais\":null,\"p_diaHabil\":\"S\",\"p_soloCaeDiaHabil\":\"S\",\"p_fechas\":[{\"fechaInicial\":\"2020-10-18\",\"numeroDias\":0,\"pais\":44}]}";
			Gson gson = Utils.getGsonBuilder();
			InObtenerDiaSiguienteMultiplesPaises in = gson.fromJson(json, InObtenerDiaSiguienteMultiplesPaises.class);
			
			ObtenerDiaSiguienteMultiplesPaises o = new ObtenerDiaSiguienteMultiplesPaises();
			System.out.println(o.logica(con, in));
			System.exit(0);
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
}
