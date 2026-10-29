package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AsociarPerfilesAPV implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(AsociarPerfilesAPV.class);
	
	public final String SQL_INSERT_PERFIL_COMERCIAL_APV = loadResourceToString("cl/bcs/APV/sql/AsociarPerfilesAPV_InsertPerfilComercialAPV.sql");
	public final String SQL_SELECT_VALIDACIONES = loadResourceToString("cl/bcs/APV/sql/AsociarPerfilesAPV_SelectValidaciones.sql");
	
	
	public static class AsociarPerfilesAPVIn {
		public String IdCategoriaInversionista;
		public String IdPerfilComercialAbierto;
		public String IdPerfilComercialActivo;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AsociarPerfilesAPVEntrada {
	    public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public List<AsociarPerfilesAPVIn> p_AsociarPerfilesAPVIn;
	    
	    public AsociarPerfilesAPVEntrada() {
	    	this.p_AsociarPerfilesAPVIn = new ArrayList<AsociarPerfilesAPVIn>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class AsociarPerfilesAPVSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public AsociarPerfilesAPVSalida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class DatosPerfilComercialAPV {
		public Long IdPerfilComercialAPV;
		public Long IdEmpresa;
		public String IdCategoriaInversionista;
		public String IdPerfilComercialAbierto;
		public String IdPerfilComercialActivo;
		public String IdEstado;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Validaciones {
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AsociarPerfilesAPVEntrada jin = gson.fromJson(in.toString(), AsociarPerfilesAPVEntrada.class);
		AsociarPerfilesAPVSalida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
			
	public AsociarPerfilesAPVSalida logica(Connection con, AsociarPerfilesAPVEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio AsociarPerfilesAPV");
		
		SQL sql = new SQL();
		AsociarPerfilesAPVSalida out = new AsociarPerfilesAPVSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		try {
			//Validacion entrada al servicio
			if (in.p_AsociarPerfilesAPVIn.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_PARAMETROS","NO_SE_INGRESARON_PARAMETROS"));
				return out;
			}
			
			String hash = "";
			HashMap<String, Boolean> perfilComercialAPVFiltro = new HashMap<String, Boolean>();
			HashMap<String, Boolean> idCategoriaInversionistaFiltro = new HashMap<String, Boolean>();
			HashMap<String, Boolean> idPerfilComercialAbiertoFiltro = new HashMap<String, Boolean>();
			HashMap<String, Boolean> idPerfilComercialActivoFiltro = new HashMap<String, Boolean>();
			List<DatosPerfilComercialAPV> perfilComercialAPVList = new ArrayList<DatosPerfilComercialAPV>();
			List<String> idCategoriaInversionistaList = new ArrayList<String>();
			List<String> idPerfilComercialAbiertoList = new ArrayList<String>();
			List<String> idPerfilComercialActivoList = new ArrayList<String>();
			
			//Validacion parametros
			for (AsociarPerfilesAPVIn perfil : in.p_AsociarPerfilesAPVIn) {
				if (perfil.IdCategoriaInversionista == null || "".equals(perfil.IdCategoriaInversionista)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CATEGORIA_INVERSIONISTA_VACIO","CATEGORIA_INVERSIONISTA_VACIO"));
					return out;
				}
				
				if (perfil.IdPerfilComercialAbierto == null || "".equals(perfil.IdPerfilComercialAbierto)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_COMERCIAL_ABIERTO_VACIO","PERFIL_COMERCIAL_ABIERTO_VACIO"));
					return out;
				}
				
				if (perfil.IdPerfilComercialActivo == null || "".equals(perfil.IdPerfilComercialActivo)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_COMERCIAL_ACTIVO_VACIO","PERFIL_COMERCIAL_ACTIVO_VACIO"));
					return out;
				}
				
				hash = perfil.IdCategoriaInversionista + "_" + perfil.IdPerfilComercialAbierto + "_" + perfil.IdPerfilComercialActivo;
				
				if (!perfilComercialAPVFiltro.containsKey(hash)) {
					perfilComercialAPVFiltro.put(hash, true);
					
					DatosPerfilComercialAPV datosPerfil = new DatosPerfilComercialAPV();
					datosPerfil.IdCategoriaInversionista = perfil.IdCategoriaInversionista;
					datosPerfil.IdPerfilComercialAbierto = perfil.IdPerfilComercialAbierto;
					datosPerfil.IdPerfilComercialActivo = perfil.IdPerfilComercialActivo;
					perfilComercialAPVList.add(datosPerfil);
				}
				
				if (!idCategoriaInversionistaFiltro.containsKey(perfil.IdCategoriaInversionista)) {
					idCategoriaInversionistaFiltro.put(perfil.IdCategoriaInversionista, true);
					idCategoriaInversionistaList.add(perfil.IdCategoriaInversionista);
				}
				if (!idPerfilComercialAbiertoFiltro.containsKey(perfil.IdPerfilComercialAbierto)) {
					idPerfilComercialAbiertoFiltro.put(perfil.IdPerfilComercialAbierto, true);
					idPerfilComercialAbiertoList.add(perfil.IdPerfilComercialAbierto);
				}
				if (!idPerfilComercialActivoFiltro.containsKey(perfil.IdPerfilComercialActivo)) {
					idPerfilComercialActivoFiltro.put(perfil.IdPerfilComercialActivo, true);
					idPerfilComercialActivoList.add(perfil.IdPerfilComercialActivo);
				}
			}
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdEstado", "VIG");
			params.put("IdCategoriaInversionista", idCategoriaInversionistaList);
			params.put("IdPerfilComercialAbierto", idPerfilComercialAbiertoList);
			params.put("IdPerfilComercialActivo", idPerfilComercialActivoList);
			List<Validaciones> validaciones = sql.query(con, SQL_SELECT_VALIDACIONES, params, Validaciones.class);
			if (validaciones.size() > 0) {
				for (Validaciones validacion : validaciones) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,validacion.Codigo,validacion.Codigo + " : " + validacion.Descripcion));
				}
				
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
					return out;
				}
			}
			
			//Insert PerfilComercialAPV
			if (perfilComercialAPVList.size() > 0) {
				List<Long> secuenciaIdperfilComercialAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV", Long.valueOf(perfilComercialAPVList.size()));
				int i = 0;
				
				for (DatosPerfilComercialAPV datosPerfil : perfilComercialAPVList) {
					datosPerfil.IdPerfilComercialAPV = secuenciaIdperfilComercialAPV.get(i);
					datosPerfil.IdEmpresa = in.p_IdEmpresa;
					datosPerfil.IdEstado = "VIG";
				}
				sql.batch(con,SQL_INSERT_PERFIL_COMERCIAL_APV, perfilComercialAPVList, DatosPerfilComercialAPV.class);
			}
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio AsociarPerfilesAPV, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"APV\",\"p_AsociarPerfilesAPVIn\":[{\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercialAbierto\":\"222\",\"IdPerfilComercialActivo\":\"AAAAPPPPPAAA\"}]}";
			AsociarPerfilesAPVEntrada Objin = gson.fromJson(json, AsociarPerfilesAPVEntrada.class);
			
			AsociarPerfilesAPV demo = new AsociarPerfilesAPV();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				AsociarPerfilesAPVSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}