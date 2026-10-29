package cl.bcs.plataforma.com.pe;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CopiarPerfilCobros implements JavaCall{
	
	private final Log log = new Log("PLA.CopiarPerfilCobros");
	
	private final String SQL_INSERT_PERFIL_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/CopiarPerfilCobros_InsertPerfilCobros.sql");
	private final String SQL_SELECT_DETALLE_PERFIL_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/CopiarPerfilCobros_SelectDetallePerfilCobros.sql");
	private final String SQL_INSERT_DETALLE_PERFIL_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/CopiarPerfilCobros_InsertDetallePerfilCobros.sql");
	
	public static class EntradaCopiarPerfilCobros{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilCobros;
		public String p_DescripcionPerfilCobros;
		public String p_EsPerfilPersonalizado;
		public String p_TipoEntidadReferencia;//dedault ""
		public Long p_IdReferencia = 0L;//default 0
		public String p_IdEstado;
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaCopiarPerfilCobros(){
			this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCopiarPerfilCobros{
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCopiarPerfilCobros(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistroCambio{
		public String CampoModificado;
		public String ValorNuevo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PerfilCobro{
		public String IdPerfilCobrosNuevo;
		public String DescripcionPerfilCobros;
		public String EsPerfilPersonalizado;
		public String TipoEntidadReferencia;
		public Long IdReferencia;			  
		public String IdEstado;
		public Long IdEmpresa;
		public String FechaIngreso;
		public String IdEstadoAutorizacion;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetallePerfilCobro{
		public Long IdEmpresa;
		public Long IdDetallePerfilCobros;
		public String  IdPerfilCobros;
		public String  IdTipoProducto;
		public String  IdTipoOperacion;
		public String  IdTipoCanalIngreso;
		public String  IdBolsa;
		public String  CodigoDefinicionCobro;
		public String  IdEstadoAutorizacion;
		public String  Borrado;
		public Long  IdUsuarioBorra;
		public Long  IdUsuarioCreador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCopiarPerfilCobros jin = gson.fromJson(in.toString(), EntradaCopiarPerfilCobros.class);
		SalidaCopiarPerfilCobros out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCopiarPerfilCobros logica(Connection con, EntradaCopiarPerfilCobros entrada) throws Exception{
		log.write("INICIO::CopiarPerfilCobros");
		SalidaCopiarPerfilCobros out = new SalidaCopiarPerfilCobros();
		SQL sql=new SQL();
		
		//Ingresar Perfil Cobro Nuevo
		PerfilCobro perfil= new PerfilCobro();
		for(RegistroCambio regcamb : entrada.p_RegistroCambio){
			if(regcamb.CampoModificado.equals("IdPerfilCobrosNuevo")){
				perfil.IdPerfilCobrosNuevo = regcamb.ValorNuevo;
			}
		}
		
		perfil.DescripcionPerfilCobros = entrada.p_DescripcionPerfilCobros;
		perfil.EsPerfilPersonalizado = entrada.p_EsPerfilPersonalizado;
		perfil.IdEmpresa = entrada.p_IdEmpresa;
		perfil.IdEstado = entrada.p_IdEstado;
		perfil.IdEstadoAutorizacion = "ING";
		perfil.FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		perfil.IdReferencia = entrada.p_IdReferencia; 
		perfil.IdUsuario = entrada.p_IdUsuario;
		perfil.TipoEntidadReferencia = "";
		
		List<PerfilCobro> perfilesCobros = new ArrayList<PerfilCobro>();
		perfilesCobros.add(perfil);
		sql.batch(con, SQL_INSERT_PERFIL_COBROS, perfilesCobros, PerfilCobro.class);
		
		//Seleccionar filas para copiar
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPerfilCobros", entrada.p_IdPerfilCobros);
		List<DetallePerfilCobro> detalles = sql.query(con, SQL_SELECT_DETALLE_PERFIL_COBROS, params, DetallePerfilCobro.class);
		
		//Inserta datos seleccionados
		for(DetallePerfilCobro detall :detalles){
			for(RegistroCambio regcamb : entrada.p_RegistroCambio){
				if(regcamb.CampoModificado.equals("IdPerfilCobrosNuevo")){
					detall.IdPerfilCobros = regcamb.ValorNuevo;
					break;
				}
			}
		}
		sql.batch(con, SQL_INSERT_DETALLE_PERFIL_COBROS, detalles, DetallePerfilCobro.class);
		
		log.write("FIN::CopiarPerfilCobros");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPerfilCobros\":\"HOLAS\",\"p_DescripcionPerfilCobros\":\"COBROS NUEVA\",\"p_EsPerfilPersonalizado\":\"N\",\"p_IdEstado\":\"ACT\",\"p_RegistroCambio\":[{\"CampoModificado\":\"IdPerfilCobrosNuevo\",\"ValorNuevo\":\"PLANTILLA COBROS NUEVA\"}]}";		
			EntradaCopiarPerfilCobros Objin = gson.fromJson(json, EntradaCopiarPerfilCobros.class);
			CopiarPerfilCobros  demo=new CopiarPerfilCobros();				
			try {
				
				SalidaCopiarPerfilCobros ObjOut=demo.logica(con, Objin);
				//con.rollback();
	
				System.out.println(ObjOut);
				con.commit();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			//con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
