package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.RespaldarCustodia;
import cl.bcs.custodia.RespaldarCustodia.Salida;
import cl.bcs.plataforma.pau.utils.PautaUtils.DiaFeriado;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCuponFeriado implements JavaCall{
	FileLoader loader = new FileLoader();
	//QUERIES
		private final String SQL_CONSULTAR_FERIADOS = "SELECT \"IdFeriado\",\"Tipo\",\"Fecha\",\"Dia\",\"DescripcionDia\",\"AgnoCarga\",\"IdPais\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdPais\" IN (:IdPais) AND \"Fecha\" IN (:FechaConsulta) AND \"IdEstado\" = 'ACT'";
		private final String SQL_CONSULTAR_CUPONES_CARTERA = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/ValorizarCuponFeriado_SelectCartera.sql");
				
		private Log log = null;
	
	public static class ValorizarCuponFeriadoEntrada {
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public Long IdPais;
		public String DiaHoy;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorizarCuponFeriadoSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ValorizarCuponFeriadoSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJE = "";
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuponesEnFeriado {
		public String INGFechaCorteCupon;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		ValorizarCuponFeriadoEntrada in=gson.fromJson(jin.toString() ,ValorizarCuponFeriadoEntrada.class);
		ValorizarCuponFeriadoSalida out=logica(con,in, true);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	public ValorizarCuponFeriadoSalida logica(Connection conn, ValorizarCuponFeriadoEntrada entrada, boolean debug) throws Exception {
		ValorizarCuponFeriadoSalida out = new ValorizarCuponFeriadoSalida();
		
    	String rutaBaseArchivos = Utils.getConfiguracion(conn, Utils.getSchema(), entrada.IdEmpresa, "PLA.RutaBaseArchivos");
		if (debug) {
			rutaBaseArchivos = "C:\\";
		}
		log = new Log("PLA.ValorizarCuponFeriado",rutaBaseArchivos,"PAU","yyyyMMdd_HHmmss");
		
		//Revisar cuantos dias feriados consecutivos hay
		List<String> listFeriados = new ArrayList<String>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SQL sql=new SQL();
		
		Date fechaEnDate=format.parse(entrada.DiaHoy);
		Calendar hoy = Calendar.getInstance();
		hoy.setTime(fechaEnDate);
		String DiaDeSemanaHoy = hoy.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toUpperCase(); 
		String siguienteDiaString = format.format(hoy.getTime());
		if("FRIDAY".equals(DiaDeSemanaHoy)) {
			hoy.add(Calendar.DAY_OF_YEAR, 1); //sabado
			siguienteDiaString = format.format(hoy.getTime());
        	listFeriados.add(siguienteDiaString);
        	hoy.add(Calendar.DAY_OF_YEAR, 1); //domingo
        	siguienteDiaString = format.format(hoy.getTime());
        	listFeriados.add(siguienteDiaString);
		}
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
	    parmsFeriado.put("IdPais", entrada.IdPais);
		while(true) {
			hoy.add(Calendar.DAY_OF_YEAR, 1);
			siguienteDiaString = format.format(hoy.getTime());
			parmsFeriado.put("FechaConsulta", siguienteDiaString);
			List<DiaFeriado> SiguienteDiaEsFeriado = sql.query(conn,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
			if(SiguienteDiaEsFeriado.isEmpty()) {
				break;
			}else {
				listFeriados.add(siguienteDiaString);
			}		
		}
		fechaEnDate=format.parse(siguienteDiaString);
		hoy = Calendar.getInstance();
		hoy.setTime(fechaEnDate);
		DiaDeSemanaHoy = hoy.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toUpperCase(); 
		siguienteDiaString = format.format(hoy.getTime());
		if("SATURDAY".equals(DiaDeSemanaHoy)) { //Se repite en caso de que un viernes haya sido feriado
			siguienteDiaString = format.format(hoy.getTime());
        	listFeriados.add(siguienteDiaString); //sabado
        	hoy.add(Calendar.DAY_OF_YEAR, 1); 
        	siguienteDiaString = format.format(hoy.getTime());
        	listFeriados.add(siguienteDiaString); //domingo
		}
		
		//Revisar si dentro de esos dias feriados hay cortes de cupon
		parmsFeriado.clear();
		parmsFeriado.put("FechaDesde", listFeriados.get(0).toString());
		parmsFeriado.put("FechaHasta", listFeriados.get(listFeriados.size()-1).toString());
		List<CuponesEnFeriado> cupones = sql.query(conn,SQL_CONSULTAR_CUPONES_CARTERA,parmsFeriado,CuponesEnFeriado.class);
		
		List<String> listaCupones= new ArrayList<String>();
		
		for (CuponesEnFeriado cupon: cupones) {
			listaCupones.add(cupon.INGFechaCorteCupon);
		}

		if(cupones.isEmpty()) {
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.IdEmpresa, entrada.IdUsuario, entrada.IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			out.P_OUT_MENSAJE = "Error Requisitos Basicos";
			return out;
		}
		
       
		for(String lista : listFeriados) {				
			//Respaldar Custodias de fin de semana/feriados			
		
			Salida salidaRespaldar = new Salida(); 
			try {				
				
				if(listaCupones.contains(lista)) {				
					RespaldarCustodia respaldarCustodia = new RespaldarCustodia();
					RespaldarCustodia.Entrada entradaCustodia = new RespaldarCustodia.Entrada();
					entradaCustodia.p_FechaRespaldo = lista;					
					salidaRespaldar = respaldarCustodia.logica(conn, entradaCustodia);
					log.write("ValorizarCuponFeriado: Proceso Respaldar Custodias -- OK");
				}
			} catch (Throwable e) {
				out.P_OUT_ERRORESCONTADOR = salidaRespaldar.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJE = "Error Respaldo Custodia";
				e.printStackTrace();
				return out;
			}
		}
		return out;
	}
	

	public static void main(String[] args) throws Throwable {
		
		ValorizarCuponFeriado demo=new ValorizarCuponFeriado();		
		ValorizarCuponFeriadoEntrada entrada = new ValorizarCuponFeriadoEntrada();
		entrada.IdEmpresa = 1L;
		entrada.IdUsuario = 0L;
		entrada.IdModulo  = "PLA";
		entrada.IdPais    = 44L;
		entrada.DiaHoy    = "2023-04-14";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			System.out.println(demo.logica(conn, entrada, true).toString());
			conn.rollback();
			//conn.commit();
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}   
}
