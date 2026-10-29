package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarParametroFinanciero implements JavaCall{
	
	private final Log log = new Log("PLA.ConsultarParametroFinanciero");
	
	private final String SQL_SELECT_PARAMETROS_FINANCIEROS_TIPO_CAMBIO = loadResourceToString("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectParametrosFinancierosTipoCambio.sql");
	private final String SQL_SELECT_PARAMETROS_FINANCIEROS_INDICES = loadResourceToString("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectParametrosFinancierosIndices.sql");
	private final String SQL_SELECT_PARAMETROS_FINANCIEROS_VALORES = loadResourceToString("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectParametrosFinancierosValores.sql");
	private final String SQL_SELECT_DIA_INHABIL = loadResourceToString("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectDiaInhabil.sql");
	private final String SQL_SELECT_DIA_FERIADO = loadResourceToString("cl/bcs/plataforma/mer/sql/ConsultarParametroFinanciero_SelectDiaFeriado.sql");
	
	public static class EntradaConsultarParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha; //Cuando TipoPeriodo DI : MM-yyyy. Sino yyyy
		public String p_TipoPeriodo; //DI,ME,AN
		public String p_TipoParametro; //TipoCambio - Indice
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class SalidaConsultarParametroFinanciero{
		public Long P_OUT_ERRORESCONTADOR;
		public List<ParametroFinanciero> P_PARAMETROS_FINANCIEROS;
		public List<ValorParametroFinanciero> P_VALORES_PARAMETROS_FINANCIEROS;
		
		public SalidaConsultarParametroFinanciero(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_PARAMETROS_FINANCIEROS = new ArrayList<ParametroFinanciero>();
			this.P_VALORES_PARAMETROS_FINANCIEROS = new ArrayList<ValorParametroFinanciero>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ParametroFinanciero{
		public String IdParametroFinanciero;
		public String Periodo;
		public String Nombre;
		public String Descripcion;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ValorParametroFinanciero{
		public String IdParametroFinanciero;
		public String Fecha;
		public String AgnoMes;
		public String Agno;
		public String Dia;
		public BigDecimal Valor;
		public String TipoFecha;
		
		public ValorParametroFinanciero(){
			this.AgnoMes = "";
			this.Agno = "";
			this.Dia = "";
			this.Valor = BigDecimal.ZERO;
			this.TipoFecha = "HABIL";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Dia{
		public Integer Dia;
		public String Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarParametroFinanciero in = gson.fromJson(jin.toString(), EntradaConsultarParametroFinanciero.class);
		SalidaConsultarParametroFinanciero out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarParametroFinanciero logica(Connection con, EntradaConsultarParametroFinanciero entrada) throws Exception {
		log.write("INICIO::ConsultarParametroFinanciero");
		
		SalidaConsultarParametroFinanciero out = new SalidaConsultarParametroFinanciero();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		String fechaDesde = "0001-01-01";
		String fechaHasta = "0001-01-01";
		List<String> valoresPosibles = new ArrayList<String>();
		List<Dia> feriados = new ArrayList<Dia>();
		HashMap<String,Object> hFeriados = new HashMap<String,Object>(); 
		List<Dia> inhabiles = new ArrayList<Dia>();
		HashMap<Long,Object> hInhabiles = new HashMap<Long,Object>();
		
		//Calculamos Fecha Desde y Hasta y posibles valores
		if("DI".equals(entrada.p_TipoPeriodo)){
			int anio = Integer.valueOf(entrada.p_Fecha.split("-")[0]);
			int mes = Integer.valueOf(entrada.p_Fecha.split("-")[1]) - 1;
			fechaDesde = entrada.p_Fecha + "-01";
			Calendar calendario = new GregorianCalendar(anio, mes, 1);
			fechaHasta = entrada.p_Fecha + "-" + calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
			Date fFechaDesde = Fecha.toDate(fechaDesde);
			Date fFechaHasta = Fecha.toDate(fechaHasta);
			while(fFechaDesde.compareTo(fFechaHasta) != 1){
				valoresPosibles.add(Fecha.stringToSqlDate(fFechaDesde));
				fFechaDesde = Fecha.addDays(fFechaDesde, 1);
			}
			params.put("FechaDesde", fechaDesde);
			params.put("FechaHasta", fechaHasta);
			params.put("Agno", anio);
			feriados = sql.query(con, SQL_SELECT_DIA_FERIADO, params, Dia.class);
			hFeriados = Utils.List2Hash(feriados, "Fecha");
			params.clear();
			params.put("Agno", anio);
			inhabiles = sql.query(con, SQL_SELECT_DIA_INHABIL, params, Dia.class);
			hInhabiles = Utils.List2Hash(inhabiles, "Dia");
		}
		else{
			fechaDesde = entrada.p_Fecha + "-01-01";
			fechaHasta = entrada.p_Fecha + "-12-31";
			if("ME".equals(entrada.p_TipoPeriodo)){
				for(int i = 1; i<13; i++){
					valoresPosibles.add(entrada.p_Fecha + "-" + String.format("%02d", i));
				}
			}else{
				valoresPosibles.add(entrada.p_Fecha);
			}
		}
		
		params.clear();
		params.put("TipoPeriodo", entrada.p_TipoPeriodo);
		//Buscamos parametros financieros
		if("TipoCambio".equals(entrada.p_TipoParametro)){
			out.P_PARAMETROS_FINANCIEROS = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS_TIPO_CAMBIO, params, ParametroFinanciero.class);
		}else{
			out.P_PARAMETROS_FINANCIEROS = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS_INDICES, params, ParametroFinanciero.class);
		}
		
		List<String> idsParametrosFinancieros = new ArrayList<String>();
		for(ParametroFinanciero parametroFinanciero : out.P_PARAMETROS_FINANCIEROS){
			idsParametrosFinancieros.add(parametroFinanciero.IdParametroFinanciero);
		}
		
		//Buscamos los valores de los parametros financieros
		params.clear();
		params.put("IdParametroFinanciero",idsParametrosFinancieros);
		params.put("FechaDesde",fechaDesde);
		params.put("FechaHasta",fechaHasta);
		List<ValorParametroFinanciero> valoresParametrosFinancieros = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS_VALORES, params, ValorParametroFinanciero.class);
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		//Iteramos para la salida
		for(ParametroFinanciero parametroFinanciero : out.P_PARAMETROS_FINANCIEROS){
			for(String valorPosible : valoresPosibles){
				ValorParametroFinanciero valorParametro = new ValorParametroFinanciero();
				valorParametro.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
				if("DI".equals(entrada.p_TipoPeriodo)){
					valorParametro.Fecha = valorPosible;
					valorParametro.Dia = valorPosible.split("-")[2];
					Dia feriado = (Dia) hFeriados.get(valorParametro.Fecha);
					LocalDate fecha = LocalDate.parse(valorParametro.Fecha , formato);
					Dia inhabil = (Dia) hInhabiles.get(fecha.getDayOfWeek().getValue() - 1);
					if(feriado != null) valorParametro.TipoFecha = "FERIADO";
					if(inhabil != null) valorParametro.TipoFecha = "INHABIL";
				} else if("ME".equals(entrada.p_TipoPeriodo)){
					valorParametro.Fecha = valorPosible + "-01";
					valorParametro.Dia = "01";
					valorParametro.AgnoMes = valorPosible;
				} else if("AN".equals(entrada.p_TipoPeriodo)){
					valorParametro.Fecha = valorPosible + "-01-01";
					valorParametro.Dia = "01";
					valorParametro.Agno = valorPosible;
				}
				for(ValorParametroFinanciero valor : valoresParametrosFinancieros){
					if(valor.IdParametroFinanciero.equals(valorParametro.IdParametroFinanciero) 
					   && (("DI".equals(parametroFinanciero.Periodo) && valor.Fecha.equals(valorPosible))
							   || ("ME".equals(parametroFinanciero.Periodo) && valor.AgnoMes.equals(valorPosible))
							   || ("AN".equals(parametroFinanciero.Periodo) && valor.Agno.equals(valorPosible)))){
						valorParametro.Valor = valor.Valor;
						break;
					}
				}
				out.P_VALORES_PARAMETROS_FINANCIEROS.add(valorParametro);
			}
		}
		
		log.write("FIN::ConsultarParametroFinanciero");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"PLA\",\"p_Fecha\":\"2019-05\",\"p_TipoPeriodo\":\"DI\",\"p_TipoParametro\":\"TipoCambio\"}";
				
				EntradaConsultarParametroFinanciero Objin = gson.fromJson(json, EntradaConsultarParametroFinanciero.class);

				ConsultarParametroFinanciero  kk = new ConsultarParametroFinanciero();
				System.out.println(kk.logica(con, Objin));
				con.commit();
				con.close();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END MAIN ==");
	};
	
	

}
