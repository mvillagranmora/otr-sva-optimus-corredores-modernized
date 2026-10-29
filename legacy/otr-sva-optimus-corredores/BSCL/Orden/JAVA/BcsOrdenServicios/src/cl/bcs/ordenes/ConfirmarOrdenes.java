package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarOrdenes implements JavaCall { 
	
	private final Log log = new Log("ORD.ConfirmarOrdenes");
	
	private final String SQL_SELECT_ESTADO_MODULO  	= loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_SELECT_ORDENES     	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarOrdenes_ConsultarOrdenes.sql");
	private final String SQL_SELECT_USUARIO_AUTORIZADO 	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarOrdenes_ConsultarUsuarioCliente.sql");
	private final String SQL_UPDATE_ORDENES        	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarOrdenes_UpdateOrden.sql");
	private final ConstantesOrdenes _C = new ConstantesOrdenes();
	
	public static class ConfirmarOrden_Entrada {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_UsaReglasNegocio;
		
		public List<Orden_Entrada> p_Ordenes;
		
		public ConfirmarOrden_Entrada() {
			this.p_UsaReglasNegocio = "S";
			this.p_Ordenes = new ArrayList<Orden_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Orden_Entrada {
		public Long 	IdOrden;
		public Long 	IdPortafolio;
		
		public Orden_Entrada() {			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class ConfirmarOrden_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
				
		public ConfirmarOrden_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class Usuario {
		public Long IdPersona;
	    public String TipoEntidad;
	    public String CodigoUsuario;
	    public Long IdEmpresa;
	    public Long IdUsuario;
	    public Long IdPortafolio;
	    public Long IdCliente;
	    
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
		Long Id = 0L;
		HashMap<String, String> mapMsg = new HashMap<>();
		List<Long> IdOrden = new ArrayList<>();
		List<Long> Usuarios = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>();
		List<Orden> ordenesUpd = new ArrayList<>();
		log.writeAsJson("Confirma Ordenes",in);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		//buscar las ordenes que seran confirmadas
		for(Orden_Entrada ord: in.p_Ordenes){
			IdOrden.add(ord.IdOrden);
			Usuarios.add(in.p_IdUsuario);
		}
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdOrden", IdOrden);
		List<Orden> ordenes = _sql.query(conn,SQL_SELECT_ORDENES,params,Orden.class);
		HashMap<Long, Object> ordenesHash = Utils.List2Hash(ordenes, "IdOrden");
		params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdUsuario", Usuarios);
		List<Usuario> usr = _sql.query(conn,SQL_SELECT_USUARIO_AUTORIZADO,params,Usuario.class);
		
		//validaciones basicas
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			}
			
			if (in.p_Ordenes.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ORDENES",null,conn,in.p_IdEmpresa));Id++;
				Id++;
			}
			
			params = new HashMap<>();
			params.put("IdModulo", _C.ID_MODULO_ORDENES);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<EstadoModulo> modulo = _sql.query(conn, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
			
			if (modulo.size() == 0) {
				mapMsg.clear();
				mapMsg.put("IdOrden", _C.ID_MODULO_ORDENES);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
			}
			
			if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_C.ESTADO_MODULO_CERRADO)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,conn,in.p_IdEmpresa));
				Id++;
			}
						
			for (Orden ord: ordenes){
				long coincidencia = usr.stream().filter(s -> s.IdPortafolio.equals(ord.IdPortafolioCliente) && s.IdCliente.equals(ord.IdCliente)).count();
				Orden o = (Orden) ordenesHash.get(ord.IdOrden);
				if (o == null){
					mapMsg.clear();
					mapMsg.put("IdOrden", ord.IdOrden.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"ORDEN_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					continue;
				}
				if (_C.ESTADO_ORDEN_ANULADA.equals(ord.IdEstadoOrden) || _C.ESTADO_ORDEN_RECHAZADA.equals(ord.IdEstadoOrden) || _C.ESTADO_ORDEN_VENCIDA.equals(ord.IdEstadoOrden)){
					mapMsg.clear();
					mapMsg.put("IdOrden", ord.IdOrden.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"ORDEN_ESTADO_ANULADA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					continue;
				}
				if (_C.ESTADO_CONFIRMACION_ORDEN_EN_PLAZO.equals(ord.IdEstadoConfirmacion) || _C.ESTADO_CONFIRMACION_ORDEN_FUERA_PLAZO.equals(ord.IdEstadoConfirmacion)){
					mapMsg.clear();
					mapMsg.put("IdOrden", ord.IdOrden.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"ORDEN_ESTADO_CONFIRMADA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					continue;
				}
				if (coincidencia == 0) {
					mapMsg.clear();
					mapMsg.put("IdOperacion", ord.IdOrden.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"USUARIO_NO_AUTORIZADO_A_CONFIRMAR_OPERACION",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					continue;
				}
				
				mapMsg.clear();
				mapMsg.put("IdOrden", ord.IdOrden.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"ORDEN_CONFIRMADA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				int comp = ord.FechaConfirmacionPlazo.compareTo(Fecha.toDateTime(fechaSistema));
				if (comp >= 0) {
					ord.IdEstadoConfirmacion = _C.ESTADO_CONFIRMACION_ORDEN_EN_PLAZO;
				}
				else {
					ord.IdEstadoConfirmacion = _C.ESTADO_CONFIRMACION_ORDEN_FUERA_PLAZO;
				}
				
				ord.FechaConfirmacion = Fecha.toDateTime(fechaSistema);
				ordenesUpd.add(ord);
			}
			
			/*
			if ( MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}*/
		}
		
		//Ingreso Base datos actualizar estado confirmacion
		{
			log.writeAsJson("Registros a actualizar: ", ordenesUpd);
			_sql.batch(conn, SQL_UPDATE_ORDENES, ordenesUpd, Orden.class);
		}
		
		//AGREGAR LOG
		{
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (Orden ord : ordenesUpd) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = ord.IdOrden;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = _C.ID_MODULO_ORDENES;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = ord.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = ord.NombreCompleto;
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = ord.DocumentoIdentidad;
				log.IdPortafolio = ord.IdPortafolioCliente;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "ORDEN";
				log.EstadoRegistro = ord.IdEstadoOrden;
				log.IdTipoOperacion = ord.IdTipoOperacion;
				log.Folio = ord.IdOrden;
				log.Cantidad = ord.Cantidad;
				log.PrecioTasa = ord.PrecioOrden;
				log.Monto = ord.MontoOrden;
				log.IdMonedaOrigen = ord.IdMonedaOrden;
				log.IdMonedaLiquidacion = ord.IdMonedaLiquidacionOrden;
				log.TipoCambio = "";
				log.Nemo = ord.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = "OK";
				log.NombreProceso = "CONFIRMACION_DE_ORDENES";						
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
		log.write("Termino Confirmacion Ordenes");
		return out;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getUATConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdOrden\":15347, \"IdPortafolio\":100069},{\"IdOrden\":15344, \"IdPortafolio\":100069}]}";
								
				ConfirmarOrden_Entrada Objin = gson.fromJson(json, ConfirmarOrden_Entrada.class);				
				ConfirmarOrdenes demo=new ConfirmarOrdenes();				
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
