package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FiltrarCuentas implements JavaCall {
	
	private final Log log = new Log("CON.FiltrarCuentas");
	
	private final String SQL_SELECT_CUENTAS_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/FiltrarCuentas_SelectCuentaContable.sql");
	
	public static class InFiltrarCuentas{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public List<Filtros> p_Filtros;
	}
	
	public static class Filtros{
		public String IdTipoDatoEvento;
		public String Condicion;
		public String Valor;
		public String EsTodos;
	}
	
	public static class SelectCuentasContables{
		public String CuentaOptimus;
		public String CuentaContabilidad;
		public String NombreCuenta;
		public String DetalleCuenta;
		public String IdTipoDatoEvento;
		public String Condicion;
		public String Valor;
	}
	
	public static class OutFiltrarCuentas{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public List<CuentasContables> P_OUT_CUENTASCONTABLES = new ArrayList<CuentasContables>();
		public List<CamposFiltros> P_OUT_CAMPOSFILTROS = new ArrayList<CamposFiltros>();
	}
	
	public static class CuentasContablesFiltros{
		public String CuentaOptimus;
		public String CuentaContabilidad;
		public String NombreCuenta;
		public String DetalleCuenta;
		public List<Filtros> Filtros = new ArrayList<Filtros>();
		public List<String> IdTipoDatoEventos = new ArrayList<String>();
	}
	
	public static class CuentasContables{
		public String CuentaOptimus;
		public String CuentaContabilidad;
		public String NombreCuenta;
		public String DetalleCuenta;
	}
	
	public static class CamposFiltros{
		public String CuentaOptimus;
		public String IdTipoDatoEvento;
		public String Condicion;
		public String Valor;
		public String EsTodos;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InFiltrarCuentas in=gson.fromJson(jin.toString() ,InFiltrarCuentas.class);
		OutFiltrarCuentas  out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutFiltrarCuentas logica(Connection con, InFiltrarCuentas entrada) throws Exception {
		OutFiltrarCuentas out = new OutFiltrarCuentas();
		out.P_OUT_ERRORESCONTADOR = 0L;
		try{
			SQL sql=new SQL();
			List<String> idTipoDatoEvento = new ArrayList<String>();
			List<Filtros> filtrosUnicos = new ArrayList<Filtros>();
			for(Filtros filtro : entrada.p_Filtros){
				if(!idTipoDatoEvento.contains(filtro.IdTipoDatoEvento)){
					idTipoDatoEvento.add(filtro.IdTipoDatoEvento);
					filtrosUnicos.add(filtro);
				}
			}
			HashMap<String,Object> filtrosHash = Utils.List2Hash(filtrosUnicos, "IdTipoDatoEvento");
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdTipoDatoEvento", idTipoDatoEvento);
			List<SelectCuentasContables> selectCuentasContables = sql.query(con,SQL_SELECT_CUENTAS_CONTABLE,params,SelectCuentasContables.class);
			HashMap<String,CuentasContablesFiltros> cuentas = new HashMap<String,CuentasContablesFiltros>();
			for(SelectCuentasContables cuenta : selectCuentasContables){
				if(cuentas.get(cuenta.CuentaOptimus) == null){
					CuentasContablesFiltros ccf = new CuentasContablesFiltros();
					ccf.CuentaOptimus = cuenta.CuentaOptimus;
					ccf.CuentaContabilidad = cuenta.CuentaContabilidad;
					ccf.NombreCuenta = cuenta.NombreCuenta;
					ccf.DetalleCuenta = cuenta.DetalleCuenta;
					cuentas.put(cuenta.CuentaOptimus, ccf);
				}
				
				CuentasContablesFiltros ccf = cuentas.get(cuenta.CuentaOptimus);
				Filtros filtro = (Filtros) filtrosHash.get(cuenta.IdTipoDatoEvento);
				if(filtro != null && ("T".equalsIgnoreCase(filtro.Condicion) || (filtro.Condicion.equalsIgnoreCase(cuenta.Condicion) && filtro.Valor.equalsIgnoreCase(cuenta.Valor)))){
					if(!ccf.IdTipoDatoEventos.contains(cuenta.IdTipoDatoEvento)) ccf.IdTipoDatoEventos.add(cuenta.IdTipoDatoEvento);
					Filtros filtroAdd = new Filtros();
					filtroAdd.IdTipoDatoEvento = cuenta.IdTipoDatoEvento;
					filtroAdd.Condicion = cuenta.Condicion;
					filtroAdd.Valor = cuenta.Valor;
					filtroAdd.EsTodos = "T".equalsIgnoreCase(filtro.Condicion) ? "S" : "N";
					ccf.Filtros.add(filtroAdd);
				}
			}
			
			for ( String key : cuentas.keySet() ) {
				CuentasContablesFiltros cuenta = cuentas.get(key);
				if(cuenta.IdTipoDatoEventos.size() == filtrosUnicos.size()){
					CuentasContables cuentaContable = new CuentasContables();
					cuentaContable.CuentaOptimus = cuenta.CuentaOptimus;
					cuentaContable.CuentaContabilidad = cuenta.CuentaContabilidad;
					cuentaContable.NombreCuenta = cuenta.NombreCuenta;
					cuentaContable.DetalleCuenta = cuenta.DetalleCuenta;
					for(Filtros filtro : cuenta.Filtros){
						CamposFiltros camposFiltros = new CamposFiltros();
						camposFiltros.CuentaOptimus = cuenta.CuentaOptimus;
						camposFiltros.IdTipoDatoEvento = filtro.IdTipoDatoEvento;
						camposFiltros.Condicion = filtro.Condicion;
						camposFiltros.Valor = filtro.Valor;
						camposFiltros.EsTodos = filtro.EsTodos;
						out.P_OUT_CAMPOSFILTROS.add(camposFiltros);
					}
					out.P_OUT_CUENTASCONTABLES.add(cuentaContable);
				}
			}
			
			out.P_OUT_MENSAJE = "UI_EXITO";
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error",e);
		}
		return out;
	}
	
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getDESAConnection()){
  				InFiltrarCuentas entrada= new InFiltrarCuentas();
				entrada.p_IdEmpresa		= new Long(1);
				entrada.p_IdUsuario		= new Long(21);
				entrada.p_IdModulo 		= "CON";
				List<Filtros> p_Filtros = new ArrayList<Filtros>();
				Filtros filtro1 = new Filtros();
				filtro1.IdTipoDatoEvento = "Moneda";
				filtro1.Condicion = "T";
				filtro1.Valor = "";
				p_Filtros.add(filtro1);
				entrada.p_Filtros = p_Filtros;
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				FiltrarCuentas demo= new FiltrarCuentas();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}
}
