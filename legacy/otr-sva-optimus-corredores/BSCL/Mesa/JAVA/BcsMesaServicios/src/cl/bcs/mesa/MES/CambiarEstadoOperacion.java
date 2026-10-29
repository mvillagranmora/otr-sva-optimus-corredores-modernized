package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * @author PRojo
 *
 */
public class CambiarEstadoOperacion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CambiarEstadoOperacion.class);
	
	private final String SQL_OperacionMesaCambiarEstadoIN = loadResourceToString("cl/bcs/mesa/sql/CambiarEstadoOperacion_ParametroINCambiarEstadoOperacion.sql");
	private final String SQL_OperacionMesaCambiarEstado = "CALL \"BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion\" (p_IdEmpresa => :p_IdEmpresa, p_IdUsuario => :p_IdUsuario, p_IdModulo => :p_IdModulo, p_simularOperacion => :p_simularOperacion, p_UsaReglasNegocio => :p_UsaReglasNegocio, p_IdPais => :p_IdPaisSistema)";
	private final String SQL_OperacionMesaCambiarEstadoOUT = "Select \"IdOperacion\",\"FolioOperacion\",\"IdReserva\",\"CodResultado\",\"MsgResultado\",'' as \"IdEstado\" from \"BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_OUT\"";
	private final String SQL_DELETE_CAMBIAR_ESTADO_OPERACION_IN = loadResourceToString("cl/bcs/mesa/sql/CambiarEstadoOperacion_DeleteCambiarEstadoOperacionIn.sql");
	
	public static class ObjCambiarEstadoOperacionIN{
		public Long	IdOperacion;
		public String IdTipoProducto;
		public String IdNuevoEstado;
		public Long	IdReserva;
		public Long	FolioCarteraCustodia;
		public BigDecimal MontoOperacionAnticipo;
		public BigDecimal MontoOperacionInicialAnticipo;
		public BigDecimal MontoOperacionFinalAnticipo;
		public BigDecimal TasaAnticipo;
		public BigDecimal MontoOperacionMonedaAnticipo;
		public BigDecimal MontoOperacionInicialMonedaAnticipo;
		public BigDecimal MontoOperacionFinalMonedaAnticipo;
		public String IdMonedaLiquidacionAnticipo;
		public String IdCondicionLiquidacionAnticipo;
		public Date FechaCondicionLiquidacionAnticipo;
		public String EstadoConfirmacion;
		public String Glosa = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjCambiarEstadoOperacionOUT {
		public Long IdOperacion;
		public Long FolioOperacion;
		public Long IdReserva;
		public String CodResultado;
		public String MsgResultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	public static class ObjEstadoOperacionOUT {
		public Long IdOperacion;
		public String IdEstado;

		public ObjEstadoOperacionOUT(Long idOperacion, String idEstado) {
			this.IdOperacion = idOperacion;
			this.IdEstado = idEstado;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class CambiarEstadoOperacionEntrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public Long							p_IdPais;
		public ArrayList<ObjCambiarEstadoOperacionIN>	p_DataCambiarEstado;
				
		
		public CambiarEstadoOperacionEntrada(Long p_IdEmpresa, Long p_IdUsuario, String p_IdModulo,
				String p_simularOperacion, String p_UsaReglasNegocio, Long p_IdPais) {
			super();
			this.p_IdEmpresa = p_IdEmpresa;
			this.p_IdUsuario = p_IdUsuario;
			this.p_IdModulo = p_IdModulo;
			this.p_simularOperacion = p_simularOperacion;
			this.p_UsaReglasNegocio = p_UsaReglasNegocio;
			this.p_IdPais = p_IdPais;
			this.p_DataCambiarEstado = new ArrayList<ObjCambiarEstadoOperacionIN>();
		}


		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class CambiarEstadoOperacionSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjEstadoOperacionOUT>	P_OUT_OPERACION;
		
		public CambiarEstadoOperacionSalida (){
			P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			P_OUT_OPERACION			= new ArrayList<>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public CambiarEstadoOperacionSalida logica(Connection conn, CambiarEstadoOperacionEntrada in) throws Exception {
		SQL sql = new SQL();
		CambiarEstadoOperacionSalida out = new CambiarEstadoOperacionSalida();
		
		try {
			logger.info("Inicia Ejeccion Cambio Estado Operacion Mesa: "+in);
			HashMap<String,Object> paramsVacio = new HashMap<String,Object>();
			//parametro de entrada
			HashMap<String, Object> paramsIn = new HashMap<String, Object>(); 
			paramsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			paramsIn.put("p_IdUsuario", in.p_IdUsuario);
			paramsIn.put("p_IdModulo", in.p_IdModulo);
			paramsIn.put("p_simularOperacion", in.p_simularOperacion);
			paramsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			paramsIn.put("p_IdPais", in.p_IdPais);
			//parametros de salida
			HashMap<String,Class> paramsOut =new HashMap<String,Class>();
			paramsOut.put("p_out_InformacionContador", Long.class);
			paramsOut.put("p_out_AdvertenciasContador", Long.class);
			paramsOut.put("p_out_ErroresContador", Long.class);
			
			if (in.p_DataCambiarEstado.size() > 0){
				logger.info("Carga Tabla Global: "+in.p_DataCambiarEstado);
				//ArrayList<ObjCambiarEstadoOperacion> p_DataCambiarEstado = (ArrayList<ObjCambiarEstadoOperacion>)in.p_DataCambiarEstado;
				sql.batch(conn, SQL_OperacionMesaCambiarEstadoIN, in.p_DataCambiarEstado, ObjCambiarEstadoOperacionIN.class);
			}
			//ejecuto sp de cambio de estado
			HashMap<String,Object> resCambioEstado = sql.call(conn, SQL_OperacionMesaCambiarEstado, paramsIn, paramsOut);
			
			//obtengo el resultado del cambio de estado
			List<ObjCambiarEstadoOperacionOUT> respuestaCambioEstado = sql.query(conn, SQL_OperacionMesaCambiarEstadoOUT, paramsVacio, ObjCambiarEstadoOperacionOUT.class);
			logger.info("Termina Cambio estado... " +respuestaCambioEstado);
			for (ObjCambiarEstadoOperacionOUT resp: respuestaCambioEstado){
				if ("OK".equals(resp.CodResultado)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(resp.IdOperacion,MensajeSalida.MENSAJE_OK, "", ""));
					out.P_OUT_OPERACION.add(new ObjEstadoOperacionOUT(resp.IdOperacion, MensajeSalida.MENSAJE_OK));
				}
				if ("ERR".equals(resp.CodResultado)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(resp.IdOperacion,MensajeSalida.MENSAJE_ERROR, resp.MsgResultado,  Diccionario.getMensaje(conn, in.p_IdEmpresa, resp.MsgResultado, null)));
					out.P_OUT_OPERACION.add(new ObjEstadoOperacionOUT(resp.IdOperacion, MensajeSalida.MENSAJE_ERROR));
				}
				if ("ADV".equals(resp.CodResultado)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(resp.IdOperacion,MensajeSalida.MENSAJE_ADVERTENCIA, resp.MsgResultado,  Diccionario.getMensaje(conn, in.p_IdEmpresa,resp.MsgResultado, null)));
					out.P_OUT_OPERACION.add(new ObjEstadoOperacionOUT(resp.IdOperacion, MensajeSalida.MENSAJE_ADVERTENCIA));
				}
			}
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch (Exception e){
			logger.error(e);
			throw e;
		}finally {
			sql.batch(conn, SQL_DELETE_CAMBIAR_ESTADO_OPERACION_IN, in.p_DataCambiarEstado, ObjCambiarEstadoOperacionIN.class);
		}

		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		CambiarEstadoOperacionEntrada jin=gson.fromJson(in.toString() ,CambiarEstadoOperacionEntrada.class);
		CambiarEstadoOperacionSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	private static CambiarEstadoOperacionEntrada getDataPrueba(){
		CambiarEstadoOperacionEntrada entrada = null;
		ObjCambiarEstadoOperacionIN entradaData = new ObjCambiarEstadoOperacionIN();
		
		entradaData.IdOperacion = Long.valueOf(230);
		entradaData.IdTipoProducto = "MERF";
		entradaData.IdNuevoEstado = "LIQ";
		entradaData.IdReserva =  Long.valueOf(1515);
		entradaData.FolioCarteraCustodia =  Long.valueOf(0);
		entradaData.MontoOperacionAnticipo = new BigDecimal(0);
		entradaData.MontoOperacionInicialAnticipo = new BigDecimal(0);
		entradaData.MontoOperacionFinalAnticipo = new BigDecimal(0);
		entradaData.TasaAnticipo = new BigDecimal(0);
		entradaData.MontoOperacionMonedaAnticipo = new BigDecimal(0);
		entradaData.MontoOperacionInicialMonedaAnticipo = new BigDecimal(0);
		entradaData.MontoOperacionFinalMonedaAnticipo = new BigDecimal(0);
		entradaData.IdMonedaLiquidacionAnticipo = "";
		entradaData.IdCondicionLiquidacionAnticipo = "";
		entradaData.FechaCondicionLiquidacionAnticipo = new Date();
		
		entrada = new CambiarEstadoOperacionEntrada(Long.valueOf(1), Long.valueOf(9),"MES","S","S", Long.valueOf(44));
		entrada.p_DataCambiarEstado.add(entradaData);
		
		
		
		return entrada;
	};
	public static void main(String[] args) throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			JsonObject in = parser.parse(gson.toJson(getDataPrueba())).getAsJsonObject();
			CambiarEstadoOperacion prueba = new CambiarEstadoOperacion(); 
			CambiarEstadoOperacionSalida resp=gson.fromJson(prueba.call(conn, in),CambiarEstadoOperacionSalida.class);
			//mientras pruebo hare el rollback
			System.out.println(resp);
			conn.rollback();
			//conn.commit();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");
	}
}
