package cl.bcs.plataforma.cre;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosAgenda;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosPrograma;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class CambiarEstadoModulo implements JavaCall {	
	private final Log log = new Log("CambiarEstadoModulo");
	//QUERIES
	private final String SQL_SELECT_MODULOEMPRESA = "select \"IdCREModuloEmpresa\",\"IdEmpresa\",\"IdModulo\",\"NombreModuloEmpresa\",\"TipoApertura\",\"HoraApertura\",\"TipoCierre\",\"HoraCierre\",\"IdEstado\",\"FechaProceso\",\"TipoEstado\" "
			+ "from \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\" where \"IdModulo\"=:IdModulo and \"IdEmpresa\"=:IdEmpresa";
	private final String SQL_UPDATE_ESTADOMODULOEMPRESA="update \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\" set \"TipoEstado\"=:TipoEstado where \"IdModulo\"=:IdModulo and \"IdEmpresa\"=:IdEmpresa";          
	//clases
	public static class ModuloEmpresa {
		public Long					IdCREModuloEmpresa; 
		public Long					IdEmpresa; 
		public String				IdModulo; 
		public String				NombreModuloEmpresa; 
		public String				TipoApertura; 
		public String				HoraApertura; 
		public String				TipoCierre; 
		public String				HoraCierre; 
		public String				IdEstado; 
		public String				FechaProceso; 
		public String				TipoEstado; 
	}
	public static class UpdateModuloEmpresa{
		public String				IdModulo; 
		public Long					IdEmpresa;
		public String				TipoEstado; //C:cerrado|A:abierto
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InCambiarEstadoModulo {
		public Long 	IdEmpresa;
		public String	IdModulo;
		public String 	TipoEstado;
	}
	public static class OutCambiarEstadoModulo {
		public Long 	ErroresContador;
		public String 	TipoEstado;
		public String 	TipoEstadoAnterior;
		public String 	Mensaje;
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InCambiarEstadoModulo in=gson.fromJson(jin.toString() ,InCambiarEstadoModulo.class);
		OutCambiarEstadoModulo out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InCambiarEstadoModulo entrada = new InCambiarEstadoModulo();
			entrada.IdEmpresa = 1L;
			entrada.TipoEstado = "A";
			entrada.IdModulo = "SIS";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambiarEstadoModulo demo=new CambiarEstadoModulo();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCambiarEstadoModulo logica(Connection con, InCambiarEstadoModulo entrada) throws Exception {
		OutCambiarEstadoModulo salida = new OutCambiarEstadoModulo();
		salida.ErroresContador = 0L;
		salida.Mensaje = "";
		SQL sql=new SQL();
		HashMap<String,Object> parmsselect = new HashMap<String,Object>();
		parmsselect.put("IdModulo", entrada.IdModulo);
		parmsselect.put("IdEmpresa", entrada.IdEmpresa);
		List<ModuloEmpresa> listaModuloEmpresa = sql.query(con, SQL_SELECT_MODULOEMPRESA,parmsselect,ModuloEmpresa.class);		
		if(listaModuloEmpresa.size()==0){
			salida.ErroresContador++;
			salida.Mensaje="Módulo no existe";
			return salida;
		}
		log.writeAsJson("Consulta Módulo Empresa:",listaModuloEmpresa);
		if(listaModuloEmpresa.get(0).TipoEstado.equals(entrada.TipoEstado)){
			salida.ErroresContador++;
			salida.Mensaje="Mismo estado";
			salida.TipoEstadoAnterior=entrada.TipoEstado;
			return salida;
		}else salida.TipoEstadoAnterior=listaModuloEmpresa.get(0).TipoEstado;
		//realizar update
		List<UpdateModuloEmpresa> listaUpdate = new ArrayList<UpdateModuloEmpresa>();
		UpdateModuloEmpresa parmsupdate = new UpdateModuloEmpresa();
		parmsupdate.IdModulo=entrada.IdModulo;
		parmsupdate.IdEmpresa=entrada.IdEmpresa;
		parmsupdate.TipoEstado=entrada.TipoEstado;
		listaUpdate.add(parmsupdate);
		sql.batch(con,SQL_UPDATE_ESTADOMODULOEMPRESA, listaUpdate, UpdateModuloEmpresa.class);
		salida.Mensaje="Cambio de estado realizado";
		salida.TipoEstado=entrada.TipoEstado;
		log.writeAsJson("Cambiar Estado Módulo, salida:",salida);
		return salida;
	}
}
