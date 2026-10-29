package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.AnularOrdenMasiva.AnularOrden;
import cl.bcs.ordenes.AnularOrdenMasiva.EntradaAnularOrdenMasiva;
import cl.bcs.ordenes.AnularOrdenMasiva.SalidaAnularOrdenMasiva;
import cl.bcs.ordenes.CrearAsociaciones.Orden;
import cl.bcs.ordenes.ModificarOrdenes.ModificarOrdenes_Entrada;
import cl.bcs.ordenes.ModificarOrdenes.ModificarOrdenes_Salida;
import cl.bcs.ordenes.ModificarOrdenes.Orden_Entrada;
import cl.bcs.ordenes.pojo.Asociacion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularAsociaciones implements JavaCall { 
	
	private final Log log = new Log("ORD.AnularAsociaciones");
	
	private final String SQL_SELECT_ESTADO_MODULO  = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_ANULA_ORDEN_ASOCIADA = loadResourceToString("cl/bcs/ordenes/sql/AnularAsociaciones_DeleteOrdenAsociada.sql");
	private final String SQL_SELECT_ORDEN_ASOCIADA = loadResourceToString("cl/bcs/ordenes/sql/AnularAsociaciones_SelectOrdenAsociada.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/AnularAsociaciones_SelectOrdenes.sql");
	private final ConstantesOrdenes _C = new ConstantesOrdenes();
	
	public static class AnularAsociaciones_Entrada {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public String	p_simularOperacion;
		public String	p_UsaReglasNegocio;

		
		public List<Asociacion_Entrada> p_Asociaciones;
		
		public AnularAsociaciones_Entrada() {
			this.p_simularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
			this.p_Asociaciones = new ArrayList<Asociacion_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Asociacion_Entrada {
		public Long 	IdInterno;
		public Long 	IdOrdenAsociada;
				
		public Asociacion_Entrada() {
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class AnularAsociaciones_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<Resultado_Salida> p_out_Resultado;
				
		public AnularAsociaciones_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_out_Resultado = new ArrayList<Resultado_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdOrdenAsociada;
		public String Msg;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class OrdenAsociada {
		public Long IdOrdenAsociada;
		public Long IdOrdenN;
		public Long IdOrdenM;
		public Date FechaIngresoRegistroCorta;
		public Date FechaIngresoRegistro;
		public String ModalidadCalculo;
		public BigDecimal Monto;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public Long IdUsuario;
		public Long IdOrdenMOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public Orden_Entrada copyDataReportanteUpd (int pos, Orden ord, OrdenAsociada asoc) throws Exception{
		Orden_Entrada out = new Orden_Entrada();
		
		out.IdOrden				= ord.IdOrden;
		out.ModificaCantidad 	= "N";
		out.Cantidad 			= ord.CantidadOrden;//.add(asoc.Cantidad);
		out.ModificaCobros 		= "N";
		out.IdAsociacion 		= 0L;
		out.TasaPromedioReporte = ord.TasaPromedioReporte;
		out.ModificaTasaPromedioReporte = "S";
		out.ModificaSistemaBursatil 	= "N";
		out.IdSistemaBursatil	= "";
		out.ModificaMonto 		= "S";
		out.Monto 				= ord.MontoOrden.add(asoc.Monto);
		out.Precio				= asoc.Precio;//ord.PrecioOrden;
		out.IdInstrumentoSubyacente  = ord.IdInstrumentoSubyacente;
		out.IdEmisor				 = ord.IdEmisor;
		out.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
		/*out.IdInstrumentoSubyacente  = ordReportado.IdInstrumentoSubyacente;
		out.IdEmisor				 = ordReportado.IdEmisor;
		out.IdMonedaLiquidacionOrden = ordReportado.IdMonedaLiquidacionOrden;*/
				
		return out;
	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		AnularAsociaciones_Entrada in = gson.fromJson(jin.toString(), AnularAsociaciones_Entrada.class);
		AnularAsociaciones_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	
	public AnularAsociaciones_Salida logica(Connection conn,  AnularAsociaciones_Entrada in) throws Exception {
		SQL _sql = new SQL();
		AnularAsociaciones_Salida out = new AnularAsociaciones_Salida();
		log.writeAsJson("Ingreso Anular Asociaciones",in);
		Diccionario dic = new Diccionario();
		Long Id = 0L;
		//validaciones basicas
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			if (in.p_Asociaciones.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ASOCIACIONES",null,conn,in.p_IdEmpresa));
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (Asociacion_Entrada asig: in.p_Asociaciones) {
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
										
		//TODO: otiene asociaciones actuales
		
		//TODO: obtiene informacion de la ordenes 
		//-- estado de ordenes
		//-- status confirmada
		//-- condiciones para hacer match
		//-- situacion actual (asociada alguna otra)		
		
		//Procesa las asociaciones
		List<Asociacion> asociacionPojos = new ArrayList<Asociacion>();
		List<Long> Asociada = new ArrayList<Long>();
		
		for (Asociacion_Entrada asig: in.p_Asociaciones) {
			Resultado_Salida rsal= new Resultado_Salida();
			rsal.IdInterno=asig.IdInterno;
			rsal.Msg="";
			out.p_out_Resultado.add(rsal);
			
			//TODO: valida que la ordenN este ingresada
			
			//TODO: valida que la ordenM este ingresada
						
			//TODO: validar estado de la orden ya no esta confirmada
						
			Asociada.add(asig.IdOrdenAsociada);
			//Registro agregar
			Asociacion newReg=new Asociacion();
			Utils.copyPojo(asig, newReg);			
			newReg.IdUsuario=in.p_IdUsuario;
			asociacionPojos.add(newReg);
			rsal.IdOrdenAsociada=newReg.IdOrdenAsociada;
			rsal.Msg="OK";
		}
		List<Orden_Entrada> ordenesActualizar = new ArrayList<>();
		HashMap<String, Object> ordenAsocIn = new HashMap<>();
		ordenAsocIn.put("IdOrdenAsociada", Asociada);
		List<OrdenAsociada> ordenAsoc = _sql.query(conn, SQL_SELECT_ORDEN_ASOCIADA, ordenAsocIn, OrdenAsociada.class);
		HashMap<String, Object> ordenAsocHash = Utils.List2Hash(ordenAsoc, "IdOrdenMOrigen");
		//Ingreso Base datos
		if (in.p_simularOperacion.equalsIgnoreCase("N") && asociacionPojos.size()>0) {
			log.writeAsJson("Registros a borrar", asociacionPojos);
			_sql.batch(conn, SQL_ANULA_ORDEN_ASOCIADA, asociacionPojos, Asociacion.class);
			//actualizo la orden padre si es que se esta asociacion creo una orden reportante hija
			List<Object> id = new ArrayList<>();
			HashMap<String, Object> idOrd = new HashMap<>();
			for (OrdenAsociada ord: ordenAsoc){
				id.add(ord.IdOrdenMOrigen);
			}
			idOrd.put("Ordenes", id);
			List<Orden> ordenesOut = _sql.query(conn, SQL_SELECT_ORDENES, idOrd, Orden.class);
			for (Orden ord : ordenesOut){
				ordenesActualizar.add(copyDataReportanteUpd(ordenesActualizar.size(), ord, (OrdenAsociada)ordenAsocHash.get(ord.IdOrden)));//actualizo la orden origen adicionando el monto que estoy eliminabd0o0 en la otra orden
			}
			if (ordenesActualizar.size() > 0 ){
				ModificarOrdenes modOrden = new ModificarOrdenes();
				ModificarOrdenes_Entrada ordIn = new ModificarOrdenes_Entrada();
				
				ordIn.p_IdEmpresa = in.p_IdEmpresa;
				ordIn.p_IdUsuario = in.p_IdUsuario;
				ordIn.p_IdModulo = in.p_IdModulo;
				ordIn.p_Ordenes.addAll(ordenesActualizar);
				ordIn.p_CobrosModificados = new ArrayList<>();
				ModificarOrdenes_Salida modOrdenOut = modOrden.logica(conn, ordIn);
				out.P_OUT_MENSAJES.addAll(modOrdenOut.P_OUT_MENSAJES);
			}			
			//anular las ordenes que se crearon
			AnularOrdenMasiva anuOrden = new AnularOrdenMasiva();
			EntradaAnularOrdenMasiva anuOrdenIn = new EntradaAnularOrdenMasiva();
			anuOrdenIn.p_IdEmpresa = in.p_IdEmpresa;
			anuOrdenIn.p_IdUsuario = in.p_IdUsuario;
			anuOrdenIn.p_IdModulo = in.p_IdModulo;
			anuOrdenIn.p_RequiereNotificacion = "N";
			for (OrdenAsociada asoc: ordenAsoc){
				AnularOrden ord = new AnularOrden();
				ord.IdOrden = asoc.IdOrdenM; //reportante
				ord.MotivoAnulacion = Diccionario.getMensaje(conn,in.p_IdEmpresa,"UI_ANULACION_ASOCIACION").msg(); 
				anuOrdenIn.p_ordenes.add(ord);
			}
			SalidaAnularOrdenMasiva anuOrdenOut = anuOrden.logica(conn, anuOrdenIn);
			for (AdvertenciaDetalle adv: anuOrdenOut.P_OUT_ADVERTENCIASDETALLE){
				HashMap<String,String> mapMsg = new HashMap<>();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,adv.Codigo,mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (ErrorDetalle adv: anuOrdenOut.P_OUT_ERRORESDETALLE){
				HashMap<String,String> mapMsg = new HashMap<>();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,adv.Codigo,mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}						
		}
		out.P_OUT_ERRORESCONTADOR=MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Asociaciones\":[{\"IdInterno\":249,\"IdOrdenAsociada\":249}]}";
								
				AnularAsociaciones_Entrada Objin = gson.fromJson(json, AnularAsociaciones_Entrada.class);				
				AnularAsociaciones demo=new AnularAsociaciones();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			AnularAsociaciones_Salida ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.rollback();
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
