package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ObtenerNemoSVS implements JavaCall{	
	private final Log log = new Log("PLA.ObtenerNemoSVS");
	private final String SQL_SELECT_EMISORES = loadResourceToString("cl/bcs/plataforma/pla/sql/ObtenerNemoSVS_ConsultarEmisores.sql");
	
	public static class ObtenerNemoSVS_Entrada {
		
		public List<Instrumento_Entrada> p_Instrumentos;
		
		public ObtenerNemoSVS_Entrada(){
			p_Instrumentos = new ArrayList<>();
		}
	}
	
	public static class Instrumento_Entrada {
		public Long IdInterno;
		public String IdEmisor;
		public String FechaVencimiento;//FechaOperacion;
		public Long Plazo;
		public String Nemotecnico;
		public String IdTipoReajuste;
		public String PagaderoEn;
	}

	public static class ObtenerNemoSVS_Salida {
		
		public List<Instrumento_Salida> p_out_Instrumento;
		
		public ObtenerNemoSVS_Salida(){
			p_out_Instrumento = new ArrayList<>();
		}
	}
	
	public static class Instrumento_Salida {
		public Long IdInterno;
		public String NemoSVS;
		
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();
		ObtenerNemoSVS_Entrada in = gson.fromJson(jin.toString(), ObtenerNemoSVS_Entrada.class);
		ObtenerNemoSVS_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Emisor_Proceso {
		public String IdEmisor;
		public String CodigoRegulador;
		public String TipoEntidad;
	}
	
	@Trace
	public ObtenerNemoSVS_Salida logica(Connection conn, ObtenerNemoSVS_Entrada entrada) throws Exception {
		ObtenerNemoSVS_Salida salida = new ObtenerNemoSVS_Salida();
		SQL _sql = new SQL();
		List<String> idsEmisor = new ArrayList<>();
		for (Instrumento_Entrada inst : entrada.p_Instrumentos) {
			if (!idsEmisor.contains(inst.IdEmisor))
				idsEmisor.add(inst.IdEmisor);
		}
		
		HashMap<String,Object> params1 = new HashMap<>();
		params1.put("idsEmisor", idsEmisor);
		List<Emisor_Proceso> _emisores = _sql.query(conn, SQL_SELECT_EMISORES, params1, Emisor_Proceso.class);
		HashMap<String,Object> emisores = Utils.List2Hash(_emisores, "IdEmisor");
		
		for (Instrumento_Entrada inst : entrada.p_Instrumentos) {
			Emisor_Proceso emi = (Emisor_Proceso) emisores.get(inst.IdEmisor);
			
			java.sql.Date fechaVencimiento = Fecha.toDate(inst.FechaVencimiento);
			//fechaVencimiento = Fecha.addDays(fechaVencimiento,inst.Plazo.intValue());
			
			String fechaVencimientoDay = Fecha.toFormat(fechaVencimiento, "dd");
			String fechaVencimientoMonth = Fecha.toFormat(fechaVencimiento, "MM");
			String fechaVencimientoYear = Fecha.toFormat(fechaVencimiento, "yy");
			
			String codigoTipoEntidad = "";
			switch(emi.TipoEntidad) {
				case "1" : codigoTipoEntidad = "B";break;
				case "2" : codigoTipoEntidad = "F";break;
				case "3" : codigoTipoEntidad = "S";break;
				case "4" : codigoTipoEntidad = "O";break;
				case "5" : codigoTipoEntidad = "T";break;
				default : codigoTipoEntidad = "";
			}
			
			
			String codigoEmisor = "";
			if (emi.IdEmisor.equalsIgnoreCase("CENTRAL")) {
				if (inst.Nemotecnico.equalsIgnoreCase("PAGARE R")) {
					codigoEmisor = "PRBC";
				}
				else if (inst.Nemotecnico.equalsIgnoreCase("PRBC")) {
					codigoEmisor = "PRBC";
				}
				else if (inst.Nemotecnico.equalsIgnoreCase("PAGARE NR")) {
					codigoEmisor = "PDBC";
				}
				else if (inst.Nemotecnico.equalsIgnoreCase("PDBC")) {
					codigoEmisor = "PDBC";
				}
				else {
					codigoEmisor = "";
				}
			}
			else {
				if (emi.CodigoRegulador.length() == 3) {
					codigoEmisor = emi.CodigoRegulador + "-";
				}
				else {
					codigoEmisor = emi.CodigoRegulador;
				}
			}
			
			
			String codigoTipoReajuste = "";
			if (inst.IdTipoReajuste.equalsIgnoreCase("CLP")) {
				codigoTipoReajuste = "N";
			}
			else if (inst.IdTipoReajuste.equalsIgnoreCase("UF")) {
				codigoTipoReajuste = "U";
			}
			else if (inst.IdTipoReajuste.equalsIgnoreCase("USD")) {
				if(inst.Nemotecnico.equalsIgnoreCase("AUTODESEMBOLSO") || inst.Nemotecnico.equalsIgnoreCase("DEPOSITO") || inst.Nemotecnico.equalsIgnoreCase("PAGARE R")){
					codigoTipoReajuste = "*";
				}else{
					if (inst.PagaderoEn.equalsIgnoreCase("CLP")) {
						codigoTipoReajuste = "D";
					}
					else {
						codigoTipoReajuste = "*";
					}
				}
			}
			else if (inst.IdTipoReajuste.equalsIgnoreCase("EUR")) {
				if (inst.PagaderoEn.equalsIgnoreCase("EUR")) {
					codigoTipoReajuste = "R";
				}
				else {
					codigoTipoReajuste = "E";
				}
			}
			else if (inst.IdTipoReajuste.equalsIgnoreCase("IVP")) {
				codigoTipoReajuste = "I";
			}
			else if (inst.IdTipoReajuste.equalsIgnoreCase("ICP")) {
				codigoTipoReajuste = "O";
			}
			else {
				codigoTipoReajuste = "O";
			}
			
			
			
			Instrumento_Salida instSalida = new Instrumento_Salida();
			instSalida.IdInterno = inst.IdInterno;
			instSalida.NemoSVS = codigoTipoEntidad + codigoTipoReajuste + codigoEmisor + fechaVencimientoDay + fechaVencimientoMonth + fechaVencimientoYear;
			salida.p_out_Instrumento.add(instSalida);
		}
		
		return salida;
	}
	
	
}
