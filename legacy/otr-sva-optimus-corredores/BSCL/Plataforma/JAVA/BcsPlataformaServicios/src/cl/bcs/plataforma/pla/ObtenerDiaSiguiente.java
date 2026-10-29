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

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ObtenerDiaSiguiente implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(ObtenerDiaSiguiente.class);
	
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
	public static class InObtenerDiaSiguiente{
		public Long	p_idPais;
		public String p_diaHabil;					//al sumar dias, considera solo dias habiles
		public String p_soloCaeDiaHabil;			//el resultado debe ser un dia habil
		public String p_consideraHabilAnterior;
		public List<FechasIn> p_fechas;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public static class FechasIn{
			public String fechaInicial;
			public Long numeroDias;
			public Long pais;
			public Long	IdInterno = 0L;
			public FechasIn() {}
			public FechasIn(String fechaInicial, Long numeroDias, Long idPais) {
				this.fechaInicial = fechaInicial;
				this.numeroDias = numeroDias;
				this.pais = idPais;
			}

			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		public InObtenerDiaSiguiente(){
			p_fechas = new ArrayList<FechasIn>();
			p_consideraHabilAnterior = "N";
		}
	}

	public static class OutObtenerDiaSiguiente{
		public Long P_OUT_ERRORESCONTADOR;
		public List<FechasOut> p_out_fechas;
		public static class FechasOut{
			public String fechaInicial;
			public Long numeroDiasInicial;
			public Long numeroDias;
			public String fechaCalculada;
			public Long idPais;
			public Long IdInterno;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public OutObtenerDiaSiguiente(){
			p_out_fechas = new ArrayList<FechasOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();	
		InObtenerDiaSiguiente in=gson.fromJson(jin.toString() ,InObtenerDiaSiguiente.class);
		OutObtenerDiaSiguiente out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public OutObtenerDiaSiguiente logica(Connection con, InObtenerDiaSiguiente entrada) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ObtenerDiaSiguiente");
		
		SQL sql=new SQL();
		OutObtenerDiaSiguiente salida = new OutObtenerDiaSiguiente();
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		
		entrada.p_soloCaeDiaHabil = entrada.p_diaHabil.equalsIgnoreCase("S") ? "S" : entrada.p_soloCaeDiaHabil == null ? "N" : entrada.p_soloCaeDiaHabil;
		
		List<Long> paises = new ArrayList<>();
		HashMap<Long,Object> hashFechas = new HashMap<Long, Object>();
		for (FechasIn f : entrada.p_fechas) {
			f.pais = obtenerPaisConsulta(entrada.p_idPais, f.pais);
			
			if (!hashFechas.containsKey(f.pais)) {
				paises.add(f.pais);
				hashFechas.put(f.pais, new Object());
			}
		}
		
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", paises);
		logger.debug("INICIO::SQL_CONSULTAR_FERIADOS");
		List<DiaFeriado> listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
		logger.debug("FIN::SQL_CONSULTAR_FERIADOS");
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
		
		for(InObtenerDiaSiguiente.FechasIn item : entrada.p_fechas){
			//busca los feriados del dia en proceso
			Long idPais = obtenerPaisConsulta(entrada.p_idPais, item.pais);;
			OutObtenerDiaSiguiente.FechasOut temp = new OutObtenerDiaSiguiente.FechasOut();
			temp.fechaInicial = item.fechaInicial;
			temp.numeroDiasInicial = item.numeroDias;
			temp.numeroDias = item.numeroDias;
			temp.fechaCalculada = item.fechaInicial;
			temp.IdInterno = item.IdInterno;
			
			boolean buscar = item.numeroDias == null || item.numeroDias < 1 ? false : true;
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
						if (esInhabil(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,idPais)){
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
			temp.idPais = idPais; 
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
					if (esInhabil(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,out.idPais) && entrada.p_consideraHabilAnterior.equalsIgnoreCase("S")) {
						esinhabil = true;
						fPivot = Fecha.addDays(fPivot, -1);
						out.numeroDias--;
					}
					else if (esInhabil(hashDiaFeriado,hashDiaInhabil,Fecha.toStringDate(fPivot),day,out.idPais)) {
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
	
	private Long obtenerPaisConsulta(Long paisEntrada, Long paisFecha) {
		Long idPais = paisFecha == null && paisEntrada != null ? paisEntrada : paisFecha;
		return idPais;
	}
	
	private Boolean esInhabil(HashMap<String, DiaFeriado> hashDiaFeriado, HashMap<String, DiaFeriado> hashDiaInhabil, String fecha, int dia, Long idPais) {
		return hashDiaFeriado.get("FERIADO_"+fecha+"_"+idPais) != null || hashDiaInhabil.get("INHABIL_"+dia+"_"+idPais) != null;
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getSURAConnection();
			
			String json = "{\"p_fechas\":[{\"fechaInicial\":\"2021-08-19\",\"numeroDias\":30}],\"p_diaHabil\":\"N\",\"p_soloCaeDiaHabil\":\"S\",\"p_idPais\":44}";
			Gson gson = Utils.getGsonBuilder();
			InObtenerDiaSiguiente in = gson.fromJson(json, InObtenerDiaSiguiente.class);
			
			ObtenerDiaSiguiente o = new ObtenerDiaSiguiente();
			System.out.println(o.logica(con, in));
			System.exit(0);
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
}
