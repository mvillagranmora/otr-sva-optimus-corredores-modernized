package cl.bcs.normativa.cartola;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DatosCartolaSocioNegocio implements BcsCartola {
	
	private final String SQL_SELECT_DATOS_SOCIONEGOCIO = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_select_DatosSocioNegocio.sql");
	
	public static class SalidaDatosCartolaSocioNegocio{
			
		public String NombreSocioNegocio;
		public String DocumentosIdentidad;
		public String Telefono;
		public String NombreEjecutivo;
		public String MonedaCartola;// Esta en Duro desde SQL_SELECT_DATOS_SOCIONEGOCIO
		public String CodigoPortafolio;
		public String FechaActual;
		public Long IdPortafolio;
		public Long IdCliente;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaDatosCartolaSocioNegocio{
		
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public List<Dato> p_Datos;
		
		public EntradaDatosCartolaSocioNegocio() {
			this.p_Datos = new ArrayList<Dato>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Dato {
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	@Override
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception {
		
		SalidaCartola out = new SalidaCartola();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		EntradaDatosCartolaSocioNegocio gpdf = new EntradaDatosCartolaSocioNegocio();
		
		gpdf.p_IdUsuario = entrada.p_IdUsuario;
		gpdf.p_IdEmpresa = entrada.p_IdEmpresa;
		gpdf.p_FechaDesde = Fecha.stringToSqlDate(entrada.p_FechaDesde);
		gpdf.p_FechaHasta = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		gpdf.p_IdModulo = entrada.p_IdModulo;
		
		List<Long> portafolios = new ArrayList<Long>();
		for(BcsCartola.Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
		}
		params.put("IdPortafolio", portafolios);
		
		List<SalidaDatosCartolaSocioNegocio> valor = sql.query(con, SQL_SELECT_DATOS_SOCIONEGOCIO, params, SalidaDatosCartolaSocioNegocio.class);
		 String fecha = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		for(int i =0; i < valor.size(); i++) {
			valor.get(i).FechaActual =fecha;  
		}
		
		List<HashMap<String,Object>> resultadoSocio= Utils.List2ListHashGenericObject(valor);
		out.P_OUT_SALIDA.put("datosSocioNegocio", resultadoSocio);
	
		return out;
	}
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {	
		Gson gson = Utils.getGsonBuilder();	
		String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":133,\"p_FechaDesde\":\"2019-07-01\",\"p_FechaHasta\":\"2019-07-24\",\"p_Datos\":[{\"IdCliente\":1423958,\"IdPortafolio\":1451391}]}";
		EntradaCartola Objin_json = gson.fromJson(json, EntradaCartola.class);
		DatosCartolaSocioNegocio demo = new DatosCartolaSocioNegocio();				
		System.out.println("ENTRADA");
		System.out.println(Objin_json.toString());
		System.out.println("SALIDA");
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin_json);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");

	}

}
