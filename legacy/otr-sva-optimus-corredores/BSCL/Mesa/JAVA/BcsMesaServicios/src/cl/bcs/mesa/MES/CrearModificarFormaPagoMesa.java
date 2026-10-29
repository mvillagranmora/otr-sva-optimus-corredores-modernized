package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearModificarFormaPagoMesa implements JavaCall {
	private final Log log = new Log("MESA_CrearModificarFormaPagoMesa");
	private final String SQL_InsertFormaPagoMesa_IN = loadResourceToString("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_InsertFormaPagoMesa.sql");
	private final String SQL_UpdateFormaPagoMesa_IN = loadResourceToString("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_UpdateFormaPagoMesa.sql");
	private final String SQL_FormaPagoMesa = loadResourceToString("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_SelFormaPagoMesa.sql");
	//datos de la tabla Forma de Pago
	//IdFormaDePago	,IdPreferenciaPago	,Nombre	,Dias	,FechaCreacion	,FechaModificacion	,PorDefecto	,IdUsuarioCreador	,IdEstado	,IdEmpresa
	
	public static class ObjFormaPago{
		public String IdFormaDePago;
		public String IdPreferenciaPago;
		public String Nombre;
		public Long Dias;
		public String FechaCreacion;
		public String FechaModificacion;
		public String PorDefecto;
		public Long IdUsuarioCreador;
		public String IdEstado;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjFormaPagoIN{
		public Long idEmpresa;
		public String accion;
		public ArrayList<ObjFormaPago> formaPago;
		public ObjFormaPagoIN (){
			formaPago = new ArrayList<ObjFormaPago>();
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjFormaPagoOUT{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public ObjFormaPagoOUT (){
			P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		ObjFormaPagoIN jin=gson.fromJson(in.toString() ,ObjFormaPagoIN.class);
		ObjFormaPagoOUT out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ObjFormaPagoOUT validaData(Connection conn, ObjFormaPagoIN in) throws Exception{
		SQL sql = new SQL();
		ObjFormaPagoOUT out = new ObjFormaPagoOUT();
		HashMap<String, String> valores = new HashMap<String,String>();
		Long index = new Long(0);
		try
		{
			if (in.formaPago.size() > 0){
				//busco formas de pago existentes
				HashMap<String, Object> params = new HashMap<String, Object>();
				ArrayList<String> dato = new ArrayList<>();
				params.put("IdEmpresa",in.idEmpresa);
				
				for(ObjFormaPago item : in.formaPago){
					dato.add(item.IdFormaDePago);					
				}
				params.put("FormasDePago",dato);
				List<ObjFormaPago> formaPago = sql.query(conn, SQL_FormaPagoMesa, params, ObjFormaPago.class);
				for (ObjFormaPago item : formaPago){
					out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_ERROR, "UI_FORMA_DE_PAGO_EXISTENTE", valores, conn, new Long(in.idEmpresa)));
					index++;
				}
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return out;
	}
	
	public ObjFormaPagoIN filtrarData(Connection conn, ObjFormaPagoIN in) throws Exception{
		SQL sql = new SQL();
		ObjFormaPagoIN out = new ObjFormaPagoIN();
		
		try
		{
			if (in.formaPago.size() > 0){
				//busco formas de pago existentes
				HashMap<String, Object> params = new HashMap<String, Object>();
				ArrayList<String> dato = new ArrayList<>();
				params.put("IdEmpresa",in.idEmpresa);
				
				for(ObjFormaPago item : in.formaPago){
					dato.add(item.IdFormaDePago);					
				}
				params.put("FormasDePago",dato);
				List<ObjFormaPago> formaPago = sql.query(conn, SQL_FormaPagoMesa, params, ObjFormaPago.class);
				for(ObjFormaPago item : in.formaPago){
					boolean existe = false;
					for(ObjFormaPago item2 : formaPago){
						if (item2.IdFormaDePago.equals(item.IdFormaDePago)){// && item2.IdEstado.equals(item.IdEstado) && "ACT".equals(item2.IdEstado)){
							existe = true;
						}
					}
					if (!existe || in.accion.equals("MODIFICAR")){
						item.IdEmpresa = in.idEmpresa;
						out.formaPago.add(item);
					}
				}
				//out.idEmpresa = in.idEmpresa;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return out;
	}
	
	public ObjFormaPagoOUT logica(Connection conn, ObjFormaPagoIN in) throws Exception {
		SQL sql = new SQL();
		ObjFormaPagoOUT out = new ObjFormaPagoOUT();
		HashMap<String, String> valores = new HashMap<String,String>();
		Long index = new Long(0);
		try {
			log.write("Inicia Ejecucion Crear/Modificar Forma Pago Mesa: "+in);
			HashMap<String,Object> paramsVacio = new HashMap<String,Object>();
			//parametro de entrada
			if (in.formaPago.size() > 0){
				log.write("Carga Tabla Global: "+in.formaPago);
				//ArrayList<ObjCambiarEstadoOperacion> p_DataCambiarEstado = (ArrayList<ObjCambiarEstadoOperacion>)in.p_DataCambiarEstado;
				//out = validaData(conn, in);
				ObjFormaPagoIN dataFormaPago = filtrarData(conn, in);
				if (dataFormaPago.formaPago.size() > 0){
					if (in.accion.equals("MODIFICAR")){
						sql.batch(conn, SQL_UpdateFormaPagoMesa_IN, dataFormaPago.formaPago, ObjFormaPago.class);
						valores.put("Cantidad", ""+dataFormaPago.formaPago.size());
						out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_INFORMACION, "UI_FORMA_DE_PAGO_ACTUALIZADAS", valores, conn, in.idEmpresa));
						index++;
					}
					else {
						sql.batch(conn, SQL_InsertFormaPagoMesa_IN, dataFormaPago.formaPago, ObjFormaPago.class);
						valores.put("Cantidad", ""+dataFormaPago.formaPago.size());
						out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_INFORMACION, "UI_FORMA_DE_PAGO_CREADAS", valores, conn, in.idEmpresa));
						index++;
					}
				}
				if ((in.formaPago.size() - dataFormaPago.formaPago.size()) > 0){
					valores.put("Cantidad", ""+(in.formaPago.size() - dataFormaPago.formaPago.size()));
					out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_FORMA_DE_PAGO_EXISTENTES", valores, conn, in.idEmpresa));
					index++;
				}
			}
			else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_ERROR, "UI_INGRESAR_FORMA_DE_PAGO", valores, conn, in.idEmpresa));
				index++;
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch (Exception e){
			e.printStackTrace();
		}

		return out;
	}
	private static ObjFormaPagoIN getDataPrueba(){
		/*ObjFormaPagoIN entrada = new ObjFormaPagoIN();
		entrada.idEmpresa = new Long(1);
		entrada.accion = "CREAR";
		ObjFormaPago entradaData = new ObjFormaPago(); 
		//prueba forma de pago con FW
		entradaData.Dias = new Long(2);
		entradaData.FechaCreacion = "2017-09-11";
		entradaData.FechaModificacion = "2017-09-11";
		entradaData.IdEstado = "ACT";
		entradaData.IdFormaDePago = "COMTER_CIN_USD_2";
		entradaData.IdPreferenciaPago = "COMTER_CIN_USD";
		entradaData.IdUsuarioCreador = new Long(9);
		entradaData.Nombre = "CUENTA INVERSION 24";
		entradaData.PorDefecto = "S";
		entrada.formaPago.add(entradaData);	
		
		entradaData = new ObjFormaPago(); 
		entradaData.Dias = new Long(2);
		entradaData.FechaCreacion = "2017-09-11";
		entradaData.FechaModificacion = "2017-09-11";
		entradaData.IdEstado = "ACT";
		entradaData.IdFormaDePago = "VENTER_CIN_USD_2";
		entradaData.IdPreferenciaPago = "VENTER_CIN_USD";
		entradaData.IdUsuarioCreador = new Long(9);
		entradaData.Nombre = "CUENTA INVERSION 24";
		entradaData.PorDefecto = "S";
		entrada.formaPago.add(entradaData);*/
		Gson gson = new Gson();
		String json = "{\"idEmpresa\":1,\"formaPago\":[{\"IdFormaDePago\":\"COMTER_CHE_CLP_0\",\"IdPreferenciaPago\":\"COMTER_CHE_CLP\",\"Nombre\":\"CHEQUE\",\"Dias\":0,\"FechaCreacion\":\"2017-09-22\",\"FechaModificacion\":\"2017-09-22\",\"PorDefecto\":\"S\",\"IdUsuarioCreador\":9,\"IdEstado\":\"INA\"}],\"accion\":\"CREAR\"}";
		ObjFormaPagoIN entrada= gson.fromJson(json ,ObjFormaPagoIN.class);
		return entrada;
	};
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			JsonObject in = parser.parse(gson.toJson(getDataPrueba())).getAsJsonObject();
			CrearModificarFormaPagoMesa prueba = new CrearModificarFormaPagoMesa(); 
			ObjFormaPagoOUT resp=gson.fromJson(prueba.call(conn, in),ObjFormaPagoOUT.class);
			//mientras pruebo hare el rollback
			System.out.println(resp);
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");
	}
}
