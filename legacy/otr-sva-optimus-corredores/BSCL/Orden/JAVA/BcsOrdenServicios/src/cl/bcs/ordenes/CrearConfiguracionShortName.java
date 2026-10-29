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

import cl.bcs.ordenes.CrearTransacciones.LibrosOrdenes;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearConfiguracionShortName implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(CrearOrdenes.class);
	private final String SQL_SELECT_SHORTNAMES = loadResourceToString("cl/bcs/ordenes/sql/CrearConfiguracionShortName_SelectShortName.sql");
	private final String SQL_SELECT_RUT = loadResourceToString("cl/bcs/ordenes/sql/CrearConfiguracionShortName_SelectDocumentoIdentidad.sql");
	private final String SQL_INSERT_SHORTNAME = loadResourceToString("cl/bcs/ordenes/sql/CrearConfiguracionShortName_InsertShortName.sql");
	private final String SQL_INSERT_DETALLES_SHORTNAME = loadResourceToString("cl/bcs/ordenes/sql/CrearConfiguracionShortName_InsertDetallesShortName.sql");
	
	
	public static class CrearConfiguracionShortName_Entrada {
		public Long							p_IdUsuario;
		public Date 						p_FechaSistema;
		public String						p_NewShortName;
		public String						p_IdSocioDeNegocio;
		public String						p_IdPortafolio;
		public String						p_Habilitado;

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
	
	public static class CrearConfiguracionShortName_Salida {
		public Long									P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>					P_OUT_ERRORESDETALLE;
		public List<MensajeSalida>					P_OUT_MENSAJES;
		
		public CrearConfiguracionShortName_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
	}
	public static class ClaseShortName {
		public Long							IdUsuario;
		public Date 						FechaSistema;
		public String						ShortName;
		public String						IdSocioDeNegocio;
		public String						IdPortafolio;
		public String						Habilitado;
		public String						DocumentoIdentidad;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class DocumentoIdentidad {
		
		public String						DocumentoIdentidad;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	public class DetalleConfiguracionExtendida {
	    public String IdTipoInstrumento;
	    public String AsignacionAuto;
	    public String ComprobanteAuto;
	    public String MovimientoFacturarAuto;
	    
	    public Long p_IdUsuario;
	    public String p_NewShortName;
	    public Date p_FechaSistema;

	    public DetalleConfiguracionExtendida(DetalleConfiguracion detalle, Long idUsuario, String NewShortName, Date FechaSistema) {
	        this.IdTipoInstrumento = detalle.IdTipoInstrumento;
	        this.AsignacionAuto = detalle.AsignacionAuto;
	        this.ComprobanteAuto = detalle.ComprobanteAuto;
	        this.MovimientoFacturarAuto = detalle.MovimientoFacturarAuto;
	        this.p_IdUsuario = idUsuario;
	        this.p_NewShortName = NewShortName;
	        this.p_FechaSistema = FechaSistema;
	    }

	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearConfiguracionShortName_Entrada in = gson.fromJson(jin.toString(), CrearConfiguracionShortName_Entrada.class);
		CrearConfiguracionShortName_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearConfiguracionShortName_Salida logica(Connection conn, CrearConfiguracionShortName_Entrada in) throws Exception {
		SQL sql = new SQL();
		CrearConfiguracionShortName_Salida out = new CrearConfiguracionShortName_Salida();
		logger.info("Inicio servicio CrearConfiguracionShortName");
		HashMap<String,Object> param = new HashMap<>();
		param.put("ShortName", in.p_NewShortName);

		Long ShortNameExiste = sql.countQuery(conn, SQL_SELECT_SHORTNAMES, param);
		if(ShortNameExiste>0){
			MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_GUARDADO_SHORTNAME_YA_EXISTE","Shortname ya existe");
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR ++;
            logger.info("Fin servicio CrearConfiguracionShortName");
			return out;
		}
		HashMap<String,Object> param2 = new HashMap<>();
		param2.put("IdSocioDeNegocio", in.p_IdSocioDeNegocio);
		List<DocumentoIdentidad> DocumentoIdentidad = new ArrayList<>();
		DocumentoIdentidad = sql.query(conn, SQL_SELECT_RUT, param2, DocumentoIdentidad.class);
		
		ClaseShortName ShortNameAgregar = new ClaseShortName();
		ShortNameAgregar.IdPortafolio = in.p_IdPortafolio;
		ShortNameAgregar.IdSocioDeNegocio = in.p_IdSocioDeNegocio;
		ShortNameAgregar.Habilitado = in.p_Habilitado;
		ShortNameAgregar.ShortName = in.p_NewShortName;
		ShortNameAgregar.FechaSistema = in.p_FechaSistema;
		ShortNameAgregar.IdUsuario = in.p_IdUsuario;
		ShortNameAgregar.DocumentoIdentidad = DocumentoIdentidad.get(0).DocumentoIdentidad;
		List<ClaseShortName> listaShortName = new ArrayList<>();
		listaShortName.add(ShortNameAgregar);
		
		List<DetalleConfiguracionExtendida> listaDetalleExtendido = new ArrayList<>();
	    for (DetalleConfiguracion detalle : in.p_DetalleConfiguracion) {
	        DetalleConfiguracionExtendida detalleExtendido = new DetalleConfiguracionExtendida(
	            detalle,
	            in.p_IdUsuario, 
	            in.p_NewShortName,
	            in.p_FechaSistema
	        );
	        listaDetalleExtendido.add(detalleExtendido);
	    }
		try {
			sql.batch(conn,SQL_INSERT_SHORTNAME,listaShortName, ClaseShortName.class);
			sql.batch(conn,SQL_INSERT_DETALLES_SHORTNAME,listaDetalleExtendido, DetalleConfiguracionExtendida.class);
			MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"SHORTNAME_AGREGADO","");
            out.P_OUT_MENSAJES.add(msg);
            logger.info("Fin servicio CrearConfiguracionShortName");
			return out;
		
	        }catch (SQLException e) {
	            MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SHORTNAME_NO_GUARDADO","");
	            e.printStackTrace();
	            out.P_OUT_MENSAJES.add(msg);
	            out.P_OUT_ERRORESCONTADOR ++;
	            logger.info("Fin servicio CrearConfiguracionShortName");
				return out;
	        }
	}

	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;		
		String json = 	"{\"p_IdUsuario\":97,\"p_NewShortName\":\"FSD94\",\"p_IdSocioDeNegocio\":66488,\"p_IdPortafolio\":3422,\"p_FechaSistema\":\"2024-02-19T03:00:00.000Z\",\"p_Habilitado\":\"S\",\"p_DetalleConfiguracion\":[{\"IdTipoInstrumento\":\"ACN\",\"AsignacionAuto\":\"S\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"S\"},{\"IdTipoInstrumento\":\"MIL\",\"AsignacionAuto\":\"S\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"S\"},{\"IdTipoInstrumento\":\"PAG\",\"AsignacionAuto\":\"S\",\"ComprobanteAuto\":\"S\",\"MovimientoFacturarAuto\":\"S\"},{\"IdTipoInstrumento\":\"BBC\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BCD\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BCP\",\"AsignacionAuto\":\"S\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"S\"},{\"IdTipoInstrumento\":\"BCU\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BEM\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BTU\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BUS\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BTP\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BCA\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BCX\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"COR\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BR\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"BH\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"PCX\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"PRD\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"LTH\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"},{\"IdTipoInstrumento\":\"ACX\",\"AsignacionAuto\":\"N\",\"ComprobanteAuto\":\"N\",\"MovimientoFacturarAuto\":\"N\"}]}";
		Gson gson = Utils.getGsonBuilder();
		CrearConfiguracionShortName_Entrada entrada = gson.fromJson(json, CrearConfiguracionShortName_Entrada.class);	
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
			CrearConfiguracionShortName Demo = new  CrearConfiguracionShortName();
			try {
				CrearConfiguracionShortName_Salida ObjOut = Demo.logica(conn, entrada);
				System.out.println(ObjOut);
				//conn.rollback();
			}catch(Exception e){
				e.printStackTrace();
				conn.rollback();
				
			}
			
			
			conn.commit();
			
		}catch (Exception e){	
			System.out.println(e);	
			} finally {		conn.close();		}
		System.out.println("== END ==");
	}
}