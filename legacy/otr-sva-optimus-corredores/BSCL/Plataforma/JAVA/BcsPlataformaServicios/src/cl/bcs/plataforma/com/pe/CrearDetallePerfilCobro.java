package cl.bcs.plataforma.com.pe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pe.CrearDefinicionCobro.CrearDefinicionCobro_Entrada;
import cl.bcs.plataforma.com.pe.CrearDefinicionCobro.CrearDefinicionCobro_Salida;
import cl.bcs.plataforma.com.pojo.DefinicionCobro_DB;
import cl.bcs.plataforma.com.pojo.DetallePerfilCobro_COM2;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDetallePerfilCobro implements JavaCall {

	private final String SQL_INSERT_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/CrearDetallePerfilCobro_InsertDetallePerfilCobro.sql");
	private final String SQL_CONSULTAR_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/CrearDetallePerfilCobro_ConsultarDetallePerfilCobro.sql");
	private final String SQL_CONSULTAR_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/CrearDefinicionCobro_ConsultarDefinicionCobro.sql");
	
	public CrearDetallePerfilCobro_Salida logica(Connection conn,CrearDetallePerfilCobro_Entrada entrada) throws Exception {
		CrearDetallePerfilCobro_Salida salida = new CrearDetallePerfilCobro_Salida();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida basicosSalida = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(basicosSalida.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(basicosSalida.p_out_Mensajes);
			return salida;
		}
		
		
		//TODO VALIDACION, QUE VENGA PERFIL COBRO
		//TODO VALIDACION, QUE VENGAN DETALLES
		SQL sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<>();
		params1.put("IdPerfilCobros", entrada.p_IdPerfilCobros);
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		params1.put("Borrado", "N");
		
		List<DetallePerfilCobro_COM2> datosActuales = sql.query(conn, SQL_CONSULTAR_DETALLE_PERFIL_COBRO, params1, DetallePerfilCobro_COM2.class);
		
		ArrayList<DetallePerfilCobro_COM2> datos = new ArrayList<>();
		boolean repetido = false;
		
		
		List<Long> sec = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::COM_2.DetallePerfilCobro", new Long(entrada.p_DetallesPerfilCobro.size()));
		
		/***********************************/
		List<CrearDefinicionCobro_Entrada> entradaDefinicionCobro = new ArrayList<CrearDefinicionCobro_Entrada>();
		if (entrada.p_IdPerfilCobros.contains("EXT")){
			HashMap<String,Object> params2 = new HashMap<>();
			params2.put("IdEmpresa", entrada.p_IdEmpresa);
			List<String> dataCodDefCobro = new ArrayList<>();
			for (DetallePerfilCobro_Entrada detalle : entrada.p_DetallesPerfilCobro){
				if (!dataCodDefCobro.contains(detalle.CodigoDefinicionCobro))
					dataCodDefCobro.add(detalle.CodigoDefinicionCobro);
			}
			
			params2.put("CodigoDefinicionCobro", dataCodDefCobro);
			List<DefinicionCobro_DB> datosDefinicionCobro = sql.query(conn, SQL_CONSULTAR_DEFINICION_COBRO, params2, DefinicionCobro_DB.class);
			for (DefinicionCobro_DB defCobro : datosDefinicionCobro){
				int i = 0; 
				for (DetallePerfilCobro_Entrada ent :entrada.p_DetallesPerfilCobro){
					System.out.println(ent.CodigoDefinicionCobro);
					System.out.println(defCobro.CodigoDefinicionCobro);
					if (ent.CodigoDefinicionCobro.equalsIgnoreCase(defCobro.CodigoDefinicionCobro)){
						CrearDefinicionCobro_Entrada entDef = new CrearDefinicionCobro_Entrada();
						entDef.p_IdEmpresa = entrada.p_IdEmpresa;
						entDef.p_IdUsuario = entrada.p_IdUsuario;
						entDef.p_IdModulo = entrada.p_IdModulo;
						
						ent.CodigoDefinicionCobroBase = ent.CodigoDefinicionCobro;
						ent.CodigoDefinicionCobro = ent.CodigoDefinicionCobro + "-" + entrada.p_IdPerfilCobros + "_" + sec.get(i);
						Utils.copyPojo(defCobro, entDef.p_DefinicionCobro);
						entDef.p_DefinicionCobro.CodigoDefinicionCobroBase 		= defCobro.CodigoDefinicionCobro;
						entDef.p_DefinicionCobro.CodigoDefinicionCobro 			= defCobro.CodigoDefinicionCobro + "-" + entrada.p_IdPerfilCobros + "_" + sec.get(i);
						entDef.p_DefinicionCobro.MontoFijo_MontoCobro 			= "MONTOFIJO".equalsIgnoreCase(defCobro.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
						entDef.p_DefinicionCobro.PorcentualFijo_ValorCobro		= "PORCENTAJEFIJO".equalsIgnoreCase(defCobro.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
						entDef.p_DefinicionCobro.PorcentualTasaPlazo_ValorCobro	= "PORCENTAJETASAPLAZO".equalsIgnoreCase(defCobro.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
						entDef.p_DefinicionCobro.IdEstado 						= "ACT";
						entDef.p_DefinicionCobro.Rangos							= new ArrayList<>();
						entDef.p_DefinicionCobro.Emisores 						= new ArrayList<>();
						entDef.p_DefinicionCobro.Instrumentos 					= new ArrayList<>();
						entDef.p_DefinicionCobro.TiposCliente 					= new ArrayList<>();
						entDef.p_DefinicionCobro.Clientes 						= new ArrayList<>();
						entDef.p_DefinicionCobro.CorredorasCorresponsales 		= new ArrayList<>();
						entradaDefinicionCobro.add(entDef);
						
					}
					i=i+1;
				}
				//defCobro.CodigoDefinicionCobroBase = defCobro.CodigoDefinicionCobro;
				//defCobro.CodigoDefinicionCobro = defCobro.CodigoDefinicionCobro + "-" + entrada.p_IdPerfilCobros;
				
				
			}
			/*for (DetallePerfilCobro_Entrada ent :entrada.p_DetallesPerfilCobro){
				ent.CodigoDefinicionCobro = ent.CodigoDefinicionCobro + "-" + entrada.p_IdPerfilCobros;
			}*/
			
		}
		
		/************************************/
		
		int index = 0;
		for (DetallePerfilCobro_Entrada detalle : entrada.p_DetallesPerfilCobro) {
			
			for (DetallePerfilCobro_COM2 actual: datosActuales) {
				if ((("".equals(actual.CodigoDefinicionCobroBase) && actual.CodigoDefinicionCobro.equalsIgnoreCase(detalle.CodigoDefinicionCobro)) || (!"".equals(actual.CodigoDefinicionCobroBase) && actual.CodigoDefinicionCobroBase.equalsIgnoreCase(detalle.CodigoDefinicionCobroBase))) &&
						(actual.IdTipoCanalIngreso+"").equalsIgnoreCase(detalle.IdTipoCanalIngreso+"") &&
						actual.IdTipoProducto.equalsIgnoreCase(detalle.IdTipoProducto) && 
						actual.IdTipoOperacion.equalsIgnoreCase(detalle.IdTipoOperacion) &&
						(actual.IdBolsa+"").equalsIgnoreCase(detalle.IdBolsa+"")
						) {
					salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DETALLE_PERFIL_COBRO_YA_EXISTE",""));
					
					repetido = true;
					break;
				}
			}
			
			if (repetido) {
				break;
			}
			
			DetallePerfilCobro_COM2 temp = new DetallePerfilCobro_COM2();
			temp.IdDetallePerfilCobros = sec.get(index++);
			temp.IdEmpresa = entrada.p_IdEmpresa;
			temp.IdPerfilCobros = entrada.p_IdPerfilCobros;
			temp.IdTipoProducto = detalle.IdTipoProducto;
			temp.IdTipoOperacion = detalle.IdTipoOperacion;
			temp.IdBolsa = detalle.IdBolsa;
			temp.IdTipoCanalIngreso = detalle.IdTipoCanalIngreso;
			temp.CodigoDefinicionCobro = detalle.CodigoDefinicionCobro;
			temp.CodigoDefinicionCobroBase = detalle.CodigoDefinicionCobroBase;
			temp.FechaCreacion = FechaDB.ObtenerFechaHoraSistemaUTC(conn, entrada.p_IdEmpresa);
			temp.IdEstadoAutorizacion = "ING";
			temp.IdUsuarioCreador = entrada.p_IdUsuario;
			
			datos.add(temp);
		}
		
		if (datos.size() > 0) {
			sql.batch(conn, SQL_INSERT_DETALLE_PERFIL_COBRO, datos, DetallePerfilCobro_COM2.class);
		
			for (CrearDefinicionCobro_Entrada ent : entradaDefinicionCobro){
				CrearDefinicionCobro cdc = new CrearDefinicionCobro();
				CrearDefinicionCobro_Salida salidaDefCobro = cdc.logica(conn, ent);
				salida.p_out_Mensajes.addAll(salidaDefCobro.p_out_Mensajes);
			}
		}
		return salida;

	}	
	
	public static class CrearDetallePerfilCobro_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_IdPerfilCobros;
		
		public List<DetallePerfilCobro_Entrada> p_DetallesPerfilCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetallePerfilCobro_Entrada {
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdBolsa;
		public String IdTipoCanalIngreso;
		public String CodigoDefinicionCobro;
		public String CodigoDefinicionCobroBase = "";
		public BigDecimal ValorCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearDetallePerfilCobro_Salida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;
		
		public CrearDetallePerfilCobro_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getDESAPEConnection()){
			CrearDetallePerfilCobro_Entrada ent = new CrearDetallePerfilCobro_Entrada();
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"PLA\",\"p_IdPerfilCobros\":\"PLANTILLA COBROS AGRESIVO\",\"p_DetallesPerfilCobro\":[{\"IdTipoProducto\":\"INRF\",\"IdTipoOperacion\":\"VENIRF\",\"IdTipoCanalIngreso\":\"TRA\",\"IdBolsa\":\"BVL\",\"CodigoDefinicionCobro\":\"COMISION CAVALI RF\"}]}";
			String json =   "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"PLA\",\"p_IdPerfilCobros\":\"EXT_20262\",\"p_DetallesPerfilCobro\":[{\"IdTipoProducto\":\"RERV\",\"IdTipoOperacion\":\"VENRRV\",\"IdTipoCanalIngreso\":null,\"IdBolsa\":null,\"CodigoDefinicionCobro\":\"CAVALI\",\"ValorCobro\":7.5}]}";
			CrearDetallePerfilCobro_Entrada Objin = gson.fromJson(json, CrearDetallePerfilCobro_Entrada.class);
			
			
			/*ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 41L;
			ent.p_IdModulo = "ORD";
			ent.p_IdPerfilCobros = "HOLA";
			ent.p_DetallesPerfilCobro = new ArrayList<DetallePerfilCobro_Entrada>();
			DetallePerfilCobro_Entrada aux = new DetallePerfilCobro_Entrada();
			aux.CodigoDefinicionCobro = "";
			aux.IdBolsa = "BVL";
			aux.IdTipoCanalIngreso = "AGE";
			aux.IdTipoProducto = "INRV";
			aux.IdTipoOperacion = "COMAML";
			
			ent.p_DetallesPerfilCobro.add(aux);*/
			
			CrearDetallePerfilCobro asdf = new CrearDetallePerfilCobro();
			CrearDetallePerfilCobro_Salida salida = asdf.logica(conn, Objin);
			conn.commit();

		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		CrearDetallePerfilCobro_Entrada in=gson.fromJson(jin.toString() ,CrearDetallePerfilCobro_Entrada.class);
		CrearDetallePerfilCobro_Salida out=logica(conn,in);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,msje.codigo, new HashMap());
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}






