package cl.bcs.normativa.ils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ItemConfiguracionMixto implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ItemConfiguracionMixto.class);
	
	public static class ItemConfiguracionMixto_Entrada {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_UsaReglasNegocio;
		public List<ItemConfiguracion> p_tablaItemConfiguracion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ItemConfiguracion{
		public Long IdConfiguracionItem = 0L;
		public Long IdItem = 0L;
		public Long IdProcesoFuncion = 0L;
		public String CuentaContable = "";
		public String CuentaOptimus = "";
		public String SaldoDebeHaber = "";
		public String Condicion = "";
		public BigDecimal Monto = BigDecimal.ZERO;
		public String Operacion = "";
		public BigDecimal Factor = BigDecimal.ZERO;
		public String MayorACero = "";
		public String MenorACero = "";
		public String PermiteCero = "";
		public String Estado = "";
		public String TipoItem = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ItemConfiguracionMixto_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ItemConfiguracionMixto_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private final String SQL_UPDATE_ITEM_CONFIGURACION_DIGITADO = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_UpdateItemDigitado.sql");
	private final String SQL_UPDATE_ITEM_CONFIGURACION_PROGRAMADO = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_UpdateItemProgramado.sql");
	private final String SQL_UPDATE_ITEM_CONFIGURACION_CONTABLE = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_UpdateItemContable.sql");
	private final String SQL_INSERT_ITEM_CONFIGURACION = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_InsertItem.sql");
	private final String SQL_DELETE_ITEM_CONFIGURACION = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_DeleteItem.sql");
	private final String SQL_SELECT_ITEM = loadResourceToString("cl/bcs/normativa/ils/sql/ItemConfiguracionMixto_SelectItem.sql");
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ItemConfiguracionMixto_Entrada in=gson.fromJson(jin.toString() ,ItemConfiguracionMixto_Entrada.class);
		ItemConfiguracionMixto_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ItemConfiguracionMixto_Salida logica(Connection con, ItemConfiguracionMixto_Entrada entrada) throws Exception {
		ItemConfiguracionMixto_Salida out = new ItemConfiguracionMixto_Salida();
		SQL sql = new SQL();
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ItemConfiguracionMixto");
		for (ItemConfiguracion itemEntrada : entrada.p_tablaItemConfiguracion) {
			if (itemEntrada.TipoItem.equals("E")) {
				List<ItemConfiguracion> eliminaItem = new ArrayList<>();
				ItemConfiguracion datosEliminaItem = new ItemConfiguracion();
				datosEliminaItem.IdConfiguracionItem = itemEntrada.IdConfiguracionItem;
				eliminaItem.add(datosEliminaItem);
				sql.batch(con, SQL_DELETE_ITEM_CONFIGURACION, eliminaItem, ItemConfiguracion.class);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ITEM_ELIMINADO", "Item Eliminado"));
				out.P_OUT_ERRORESCONTADOR = 0L;
			} else {

				// Trae todos las configuraciones guardadas para un item
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdItem", itemEntrada.IdItem);
				List<ItemConfiguracion> cuentas = sql.query(con, SQL_SELECT_ITEM, params, ItemConfiguracion.class);

				List<ItemConfiguracion> nuevoParamItem = new ArrayList<>();
				List<ItemConfiguracion> modificaParamItem = new ArrayList<>();
				ItemConfiguracion datosNuevoItem = new ItemConfiguracion();

				for (ItemConfiguracion paramItem : cuentas) {
					if (itemEntrada.TipoItem.equals("D") && paramItem.TipoItem.equals("D")) {
						datosNuevoItem.IdConfiguracionItem = paramItem.IdConfiguracionItem;
						datosNuevoItem.MayorACero = itemEntrada.MayorACero;
						datosNuevoItem.MenorACero = itemEntrada.MenorACero;
						datosNuevoItem.PermiteCero = itemEntrada.PermiteCero;
						modificaParamItem.add(datosNuevoItem);
						sql.batch(con, SQL_UPDATE_ITEM_CONFIGURACION_DIGITADO, modificaParamItem, ItemConfiguracion.class);
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ITEM_MODIFICADO", "Item Modificado"));
						out.P_OUT_ERRORESCONTADOR = 0L;
						break;
					}
					if (itemEntrada.TipoItem.equals("P") && paramItem.TipoItem.equals("P")) {
						datosNuevoItem.IdConfiguracionItem = paramItem.IdConfiguracionItem;
						datosNuevoItem.IdProcesoFuncion = itemEntrada.IdProcesoFuncion;
						modificaParamItem.add(datosNuevoItem);
						sql.batch(con, SQL_UPDATE_ITEM_CONFIGURACION_PROGRAMADO, modificaParamItem,	ItemConfiguracion.class);
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ITEM_MODIFICADO", "Item Modificado"));
						out.P_OUT_ERRORESCONTADOR = 0L;
						break;
					}
					if (itemEntrada.TipoItem.equals("C") && paramItem.TipoItem.equals("C")
							&& itemEntrada.CuentaContable.equals(paramItem.CuentaContable)) {
						datosNuevoItem.IdConfiguracionItem = paramItem.IdConfiguracionItem;
						datosNuevoItem.SaldoDebeHaber = itemEntrada.SaldoDebeHaber;
						datosNuevoItem.Condicion = itemEntrada.Condicion;
						datosNuevoItem.Monto = itemEntrada.Monto;
						datosNuevoItem.Operacion = itemEntrada.Operacion;
						datosNuevoItem.Factor = itemEntrada.Factor;
						modificaParamItem.add(datosNuevoItem);
						sql.batch(con, SQL_UPDATE_ITEM_CONFIGURACION_CONTABLE, modificaParamItem, ItemConfiguracion.class);
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ITEM_MODIFICADO", "Item Modificado"));
						out.P_OUT_ERRORESCONTADOR = 0L;
						break;
					}
				}

				if (modificaParamItem.isEmpty()) {
					if (itemEntrada.TipoItem.equals("D")) {
						datosNuevoItem.IdConfiguracionItem = itemEntrada.IdConfiguracionItem;
						datosNuevoItem.IdItem = itemEntrada.IdItem;
						datosNuevoItem.MayorACero = itemEntrada.MayorACero;
						datosNuevoItem.MenorACero = itemEntrada.MenorACero;
						datosNuevoItem.PermiteCero = itemEntrada.PermiteCero;
						datosNuevoItem.Estado = itemEntrada.Estado;
						datosNuevoItem.TipoItem = itemEntrada.TipoItem;
						nuevoParamItem.add(datosNuevoItem);
					}
					if (itemEntrada.TipoItem.equals("P")) {
						datosNuevoItem.IdConfiguracionItem = itemEntrada.IdConfiguracionItem;
						datosNuevoItem.IdItem = itemEntrada.IdItem;
						datosNuevoItem.IdProcesoFuncion = itemEntrada.IdProcesoFuncion;
						datosNuevoItem.Estado = itemEntrada.Estado;
						datosNuevoItem.TipoItem = itemEntrada.TipoItem;
						nuevoParamItem.add(datosNuevoItem);
					}
					if (itemEntrada.TipoItem.equals("C")) {
						datosNuevoItem.IdConfiguracionItem = itemEntrada.IdConfiguracionItem;
						datosNuevoItem.IdItem = itemEntrada.IdItem;
						datosNuevoItem.CuentaContable = itemEntrada.CuentaContable;
						datosNuevoItem.CuentaOptimus = itemEntrada.CuentaOptimus;
						datosNuevoItem.SaldoDebeHaber = itemEntrada.SaldoDebeHaber;
						datosNuevoItem.Condicion = itemEntrada.Condicion;
						datosNuevoItem.Monto = itemEntrada.Monto;
						datosNuevoItem.Operacion = itemEntrada.Operacion;
						datosNuevoItem.Factor = itemEntrada.Factor;
						datosNuevoItem.Estado = itemEntrada.Estado;
						datosNuevoItem.TipoItem = itemEntrada.TipoItem;
						nuevoParamItem.add(datosNuevoItem);
					}
				}
				if (!nuevoParamItem.isEmpty()) {
					sql.batch(con, SQL_INSERT_ITEM_CONFIGURACION, nuevoParamItem, ItemConfiguracion.class);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ITEM_MODIFICADO", "Item Modificado"));
					out.P_OUT_ERRORESCONTADOR = 0L;
				}
			}
		}
		logger.info("Termino del servicio ItemConfiguracionMixto, Tiempo:" + (System.currentTimeMillis() - init));
		return out; 
	}
//MAIN	VOID
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_TipoOperacion\":\"D\",\"p_tablaItemConfiguracion\":[{\"IdConfiguracionItem\":165,\"IdItem\":3,\"IdProcesoFuncion\":2,\"CuentaContable\":\"110302000000\",\"CuentaOptimus\":\"110302000000\",\"SaldoDebeHaber\":\"D\",\"Condicion\":\">\",\"Monto\":100,\"Operacion\":\"+\",\"Factor\":1,\"MayorACero\":\"S\",\"MenorACero\":\"N\",\"PermiteCero\":\"S\",\"Estado\":\"VIG\",\"TipoItem\":\"D\"}]}";
			
			ItemConfiguracionMixto_Entrada Objin = gson.fromJson(json, ItemConfiguracionMixto_Entrada.class);

			ItemConfiguracionMixto demo = new ItemConfiguracionMixto();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ItemConfiguracionMixto_Salida ObjOut = demo.logica(con, Objin);
				con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}