package cl.bcs.ordenes;

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


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GuardarConfiguracionAsignacionDirecta implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(CrearOrdenes.class);
	private final String SQL_UPDATE_CONFIGURACION_ASIGNACION_DIRECTA = loadResourceToString("cl/bcs/ordenes/sql/GuardarConfiguracionAsignacionDirecta_UpdateConfiguracionAsignacionDirecta.sql");
	private final String SQL_INSERT_CONFIGURACION_ASIGNACION_DIRECTA = loadResourceToString("cl/bcs/ordenes/sql/GuardarConfiguracionAsignacionDirecta_InsertConfiguracionAsignacionDirecta.sql");
	private final String SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA = loadResourceToString("cl/bcs/ordenes/sql/GuardarConfiguracionAsignacionDirecta_SelectConfiguracionAsignacionDirecta.sql");
	
	public static class GuardarConfiguracionAsignacionDirecta_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public Date 						p_FechaSistema;

		public List<DetalleConfiguracion>		p_DetalleConfiguracion;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class DetalleConfiguracion {
		public  String IdTipoInstrumento;
		public  String AsignacionAuto;
		public  String ComprobanteAuto;
		public  String MovimientoFacturarAuto;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class GuardarConfiguracionAsignacionDirecta_Salida {
		public Long									P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>					P_OUT_MENSAJES;
		
		public GuardarConfiguracionAsignacionDirecta_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
	}

	public class DetalleConfiguracionExtendida {
	    public String IdTipoInstrumento;
	    public String AsignacionAuto;
	    public String ComprobanteAuto;
	    public String MovimientoFacturarAuto;
	    
	    public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public Date p_FechaSistema;

	    public DetalleConfiguracionExtendida(DetalleConfiguracion detalle, Long idEmpresa, Long idUsuario, String idModulo, Date FechaSistema) {
	        this.IdTipoInstrumento = detalle.IdTipoInstrumento;
	        this.AsignacionAuto = detalle.AsignacionAuto;
	        this.ComprobanteAuto = detalle.ComprobanteAuto;
	        this.MovimientoFacturarAuto = detalle.MovimientoFacturarAuto;
	        this.p_IdEmpresa = idEmpresa;
	        this.p_IdUsuario = idUsuario;
	        this.p_IdModulo = idModulo;
	        this.p_FechaSistema = FechaSistema;
	    }

	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		GuardarConfiguracionAsignacionDirecta_Entrada in = gson.fromJson(jin.toString(), GuardarConfiguracionAsignacionDirecta_Entrada.class);
		GuardarConfiguracionAsignacionDirecta_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public GuardarConfiguracionAsignacionDirecta_Salida logica(Connection conn, GuardarConfiguracionAsignacionDirecta_Entrada in) throws Exception {
		SQL sql = new SQL();
		GuardarConfiguracionAsignacionDirecta_Salida out = new GuardarConfiguracionAsignacionDirecta_Salida();
		logger.info("Inicio servicio GuardarConfiguracionAsignacionDirecta");
		List<DetalleConfiguracionExtendida> listaDetalleExtendido = new ArrayList<>();
	    for (DetalleConfiguracion detalle : in.p_DetalleConfiguracion) {
	        DetalleConfiguracionExtendida detalleExtendido = new DetalleConfiguracionExtendida(
	            detalle, 
	            in.p_IdEmpresa, 
	            in.p_IdUsuario, 
	            in.p_IdModulo,
	            in.p_FechaSistema
	        );
	        listaDetalleExtendido.add(detalleExtendido);
	    }
		try {
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdTipoInstrumento", in.p_DetalleConfiguracion.get(0).IdTipoInstrumento);
			Long ConfiguracionEmpresa = sql.countQuery(conn, SQL_SELECT_CONFIGURACION_ASIGNACION_DIRECTA, params);
			if(ConfiguracionEmpresa>0L) {
				sql.batch(conn,SQL_UPDATE_CONFIGURACION_ASIGNACION_DIRECTA,listaDetalleExtendido, DetalleConfiguracionExtendida.class);
				MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONFIGURACION_ACTUALIZADA","");
	            out.P_OUT_MENSAJES.add(msg);
	            logger.info("Fin servicio GuardarConfiguracionAsignacionDirecta");
				return out;
				}
			else {
				sql.batch(conn,SQL_INSERT_CONFIGURACION_ASIGNACION_DIRECTA,listaDetalleExtendido, DetalleConfiguracionExtendida.class);
				MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONFIGURACION_CREADA","");
	            out.P_OUT_MENSAJES.add(msg);
	            logger.info("Fin servicio GuardarConfiguracionAsignacionDirecta");
				return out;
				}
		
	        }catch (SQLException e) {
	            MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONFIGURACION_NO_GUARDADA","");
	            e.printStackTrace();
	            out.P_OUT_MENSAJES.add(msg);
	            out.P_OUT_ERRORESCONTADOR ++;
	            logger.info("Fin servicio GuardarConfiguracionAsignacionDirecta");
				return out;
	        }
	}

		
	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;		
		String json = 	"{\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 97,\r\n"
				+ "  \"p_IdModulo\": \"PLA\",\r\n"
				+ "  \"p_FechaSistema\": \"2023-12-04T03:00:00.000Z\",\r\n"
				+ "  \"p_DetalleConfiguracion\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"ACN\",\r\n"
				+ "      \"AsignacionAuto\": \"S\",\r\n"
				+ "      \"ComprobanteAuto\": \"S\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"MIL\",\r\n"
				+ "      \"AsignacionAuto\": \"S\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"S\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"PAG\",\r\n"
				+ "      \"AsignacionAuto\": \"S\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BBC\",\r\n"
				+ "      \"AsignacionAuto\": \"S\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BCD\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BCP\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BCU\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BEM\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BTU\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BUS\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BTP\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BCA\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BCX\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"COR\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BR\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"BH\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"PCX\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"PRD\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdTipoInstrumento\": \"LTH\",\r\n"
				+ "      \"AsignacionAuto\": \"N\",\r\n"
				+ "      \"ComprobanteAuto\": \"N\",\r\n"
				+ "      \"MovimientoFacturarAuto\": \"N\"\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";
		Gson gson = Utils.getGsonBuilder();
		GuardarConfiguracionAsignacionDirecta_Entrada entrada = gson.fromJson(json, GuardarConfiguracionAsignacionDirecta_Entrada.class);	
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			GuardarConfiguracionAsignacionDirecta Demo = new  GuardarConfiguracionAsignacionDirecta();
			try {
				GuardarConfiguracionAsignacionDirecta_Salida ObjOut = Demo.logica(conn, entrada);
				System.out.println(ObjOut);
			}catch(Exception e){
				e.printStackTrace();
				conn.rollback();
				
			}
			
			
			//conn.commit();
			
		}catch (Exception e){	
			System.out.println(e);	
			} finally {		conn.close();		}
		System.out.println("== END ==");
	}
}