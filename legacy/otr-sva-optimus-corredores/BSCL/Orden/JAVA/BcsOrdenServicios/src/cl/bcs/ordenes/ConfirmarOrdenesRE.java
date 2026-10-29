package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Asociacion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarOrdenesRE implements JavaCall { 
	
	private final Log log = new Log("ORD.ConfirmarOrden");
	
	private final String SQL_SELECT_ESTADO_MODULO  = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_CONFIRMA_ORDEN        = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarOrden_UpdateOrden.sql");
	private final ConstantesOrdenes _C = new ConstantesOrdenes();
	
	public static class ConfirmarOrden_Entrada {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public String	p_simularOperacion;
		public String	p_UsaReglasNegocio;

		
		public List<Orden_Entrada> p_Ordenes;
		
		public ConfirmarOrden_Entrada() {
			this.p_simularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
			this.p_Ordenes = new ArrayList<Orden_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Orden_Entrada {
		public Long 	IdInterno;
		public Long 	IdOrden;
		public String   EsConfirmada;
		public Orden_Entrada() {			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class ConfirmarOrden_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<Resultado_Salida> p_out_Resultado;
				
		public ConfirmarOrden_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_out_Resultado = new ArrayList<Resultado_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdOrden;
		public String Msg;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConfirmarOrden_Entrada in = gson.fromJson(jin.toString(), ConfirmarOrden_Entrada.class);
		ConfirmarOrden_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	
	public ConfirmarOrden_Salida logica(Connection conn,  ConfirmarOrden_Entrada in) throws Exception {
		SQL _sql = new SQL();
		ConfirmarOrden_Salida out = new ConfirmarOrden_Salida();
		log.writeAsJson("Confirma Ordenes",in);
		
		//validaciones basicas
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			if (in.p_Ordenes.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ORDENES",null,conn,in.p_IdEmpresa));
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (Orden_Entrada asig: in.p_Ordenes) {
				if (correlativos.contains(asig.IdInterno)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(asig.IdInterno);
				}
			}
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdModulo", _C.ID_MODULO_ORDENES);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<EstadoModulo> modulo = _sql.query(conn, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
			if (modulo.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",null,conn,in.p_IdEmpresa));
				return out;
			}
			if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_C.ESTADO_MODULO_CERRADO)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,conn,in.p_IdEmpresa));
				return out;
			}
		}
										
		//TODO: obtiene informacion de las ordenes actuales
		
		
		//Procesa las ordenes
		List<Orden> ordenPojos = new ArrayList<Orden>();
		for (Orden_Entrada ord: in.p_Ordenes) {
			Resultado_Salida rsal= new Resultado_Salida();
			rsal.IdInterno=ord.IdInterno;
			rsal.Msg="";
			out.p_out_Resultado.add(rsal);
			
			//Valida tipo dato
			if (!(ord.EsConfirmada.equals("S") || ord.EsConfirmada.equals("N"))) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PARAMETROS_INCORRECTOS",null,conn,in.p_IdEmpresa));
			}
			
			//TODO: valida que la orden dada exista
			
			//TODO: valida el estado de la orden sea ING
			
			//TODO: valida si el tipo de la orden permite confirmar/desconfirmar
			
			//TODO: validar que la orden no este ya confirmada si se quiere confirmar
			
			//TODO: validar que la orden no este ya confirmada si se quiere confirmar
								
			//TODO: validar si es reportado y se confirma que este correctamente asociada 
			
			//Registro agregar
			Orden newReg=new Orden();
			Utils.copyPojo(ord, newReg);			
			ordenPojos.add(newReg);
			rsal.IdOrden=newReg.IdOrden;
			rsal.Msg="OK";
			
			//TODO: Log auditoria quien cambio estado
		}
		
		
		//Ingreso Base datos
		if (in.p_simularOperacion.equalsIgnoreCase("N") && ordenPojos.size()>0) {
			log.writeAsJson("Registros a actualizar", ordenPojos);
			_sql.batch(conn, SQL_CONFIRMA_ORDEN, ordenPojos, Orden.class);
		}
		out.P_OUT_ERRORESCONTADOR=MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdInterno\":1,\"IdOrden\":311626 ,  \"EsConfirmada\" : \"N\" }]}";
								
				ConfirmarOrden_Entrada Objin = gson.fromJson(json, ConfirmarOrden_Entrada.class);				
				ConfirmarOrdenesRE demo=new ConfirmarOrdenesRE();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			ConfirmarOrden_Salida ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.commit();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}	
}
