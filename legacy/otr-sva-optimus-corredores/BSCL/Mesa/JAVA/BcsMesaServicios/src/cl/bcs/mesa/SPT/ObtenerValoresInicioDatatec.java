package cl.bcs.mesa.SPT;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.plataforma.pla.ModificarParametrosConfiguracion.SelectConfiguracion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerValoresInicioDatatec implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ObtenerValoresInicioDatatec.class);
	
	private final String SQL_CONSULTAR_USUARIO = "SELECT \"IdUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoUsuario\" = :CodigoUsuario";
	private final String SQL_CONSULTAR_CONFIGURACION_PLA = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Plataforma_Select.sql");
	private final String SQL_CONSULTAR_CORREDORA_EMPRESA = loadResourceToString("cl/bcs/mesa/SPT/sql/ObtenerValoresInicioDatatec_ConsultarCorredoraEmpresa.sql");
	
	public static class ObtenerValoresInicioDatatecEntrada {
		public Long p_IdEmpresa;
		
		//Validar si UI Datatec esta en uso. Obtener IdUsuario logueado
		public String p_CodigoUsuario;
		public String p_BloqueoDesbloqueo;
		
		//Obtener FechaSistema
		public String p_CodigoParametroFechaSistema;
		
		//Obtener Puntas por Tramo
		public String p_IdMoneda;
		public Long p_MontoPorTramo;
		
		//ConsultarConfiguracionMesa: TIEMPOESPERAREPROCESO
		public String p_CodigoParametroTiempoEsperaProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObtenerValoresInicioDatatecSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public Long BloqueoDatatecErroresContador;
		public String BloqueoDatatecDesccripcion;
		public Long IdUsuario;
		public String Fechasistema;
		public List<ConsultarPuntasPorTramo.PuntaPorTramoSalida> P_OUT_PUNTAS;
		public List<MensajeSalida> P_OUT_ADVERTENCIASDETALLEPUNTAS;
		public String TiempoEspera;
		public List<IngresoDatatec.DatosDatatecObjOut> P_OUT_DATOSDATATEC;
		//public String Nombrecorredora;
		public String CodigoDatatec;
		public String RutaDatatec;
		public String NombreArchivoDatatec;
		public String Ambiente;
		public String Version;
		public String NombreEmpresa;
		
		public ObtenerValoresInicioDatatecSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PUNTAS = new ArrayList<ConsultarPuntasPorTramo.PuntaPorTramoSalida>();
			this.P_OUT_ADVERTENCIASDETALLEPUNTAS = new ArrayList<MensajeSalida>();
			this.P_OUT_DATOSDATATEC = new ArrayList<IngresoDatatec.DatosDatatecObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosUsuario {
        public String CodigoUsuario;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String IdEstado;
    }
	
	public static class DatosCorredoraEmpresa {
        public Long IdEmpresa;
        public Long IdSocioDeNegocio;
        public Long IdPersona;
        public String CodigoDatatec;
        public String NombreCompleto;
    }
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ObtenerValoresInicioDatatecEntrada in = gson.fromJson(jin.toString(), ObtenerValoresInicioDatatecEntrada.class);
		ObtenerValoresInicioDatatecSalida out = logica(con, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ObtenerValoresInicioDatatecSalida logica(Connection conn, ObtenerValoresInicioDatatecEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ObtenerValoresInicioDatatec");
		
		SQL sql = new SQL();
		ObtenerValoresInicioDatatecSalida out = new ObtenerValoresInicioDatatecSalida();
		
		try {
			//Validar si UI Datatec esta en uso
			BloqueoDatatec bloqueoDatatec = new BloqueoDatatec();
			BloqueoDatatec.BloqueoDatatecEntrada bloqueoDatatecEntrada = new BloqueoDatatec.BloqueoDatatecEntrada();
			bloqueoDatatecEntrada.p_IdEmpresa = in.p_IdEmpresa;
			bloqueoDatatecEntrada.p_CodigoUsuario = in.p_CodigoUsuario;
            bloqueoDatatecEntrada.p_BloqueoDesbloqueo = "B";
            BloqueoDatatec.BloqueoDatatecSalida bloqueoDatatecSalida = bloqueoDatatec.logica(conn, bloqueoDatatecEntrada);
            out.BloqueoDatatecErroresContador = bloqueoDatatecSalida.P_OUT_ERRORESCONTADOR;
            out.BloqueoDatatecDesccripcion = bloqueoDatatecSalida.P_OUT_MENSAJES.size() > 0 ? bloqueoDatatecSalida.P_OUT_MENSAJES.get(0).descripcion : "";
            
            if (MensajeSalida.getNumeroErrores(bloqueoDatatecSalida.P_OUT_MENSAJES) > 0) {
				out.P_OUT_MENSAJES.addAll(bloqueoDatatecSalida.P_OUT_MENSAJES);
				//return out;
			}
            
            //Obtener IdUsuario logueado
            HashMap<String,Object> params = new HashMap<>();
            params.put("IdEmpresa", in.p_IdEmpresa);
            params.put("CodigoUsuario", in.p_CodigoUsuario);
            out.IdUsuario = sql.query(conn, SQL_CONSULTAR_USUARIO, params, DatosUsuario.class).get(0).IdUsuario;
            
            //Obtener FechaSistema
            params.clear();
            params.put("p_IdEmpresa", in.p_IdEmpresa);
            params.put("p_Codigo", "PLA.FechaSistema");
            out.Fechasistema = sql.query(conn, SQL_CONSULTAR_CONFIGURACION_PLA, params, SelectConfiguracion.class).get(0).Valor;

            String ambiente = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.Ambiente");
            //Obtener Ambiente
            String uiAmbiente = "UI_AMBIENTE";
        	if ("DES".equalsIgnoreCase(ambiente)) 		uiAmbiente = "UI_AMBIENTE_DESARROLLO";
        	else if ("QA".equalsIgnoreCase(ambiente))   uiAmbiente="UI_AMBIENTE_QA";
        	else if ("UAT".equalsIgnoreCase(ambiente))  uiAmbiente="UI_AMBIENTE_CERTIFICACION";
        	else if ("PRO".equalsIgnoreCase(ambiente))  uiAmbiente="UI_AMBIENTE_PRODUCCION";	
        	else 			 						    uiAmbiente="UI_AMBIENTE_SIN_DEFINIR";

            out.Ambiente = Diccionario.getMensaje(conn,in.p_IdEmpresa, uiAmbiente).msg();
            
			String rutaDatatec = "";
            try{ rutaDatatec = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "CODRUTADATATEC").Valor;} catch(Exception er) {};

			String nombreArchivoDatatec = "";
            try{ nombreArchivoDatatec = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "NOMBREARCHIVODATATEC").Valor;} catch(Exception er) {};
            
            out.RutaDatatec = rutaDatatec;
            out.NombreArchivoDatatec = nombreArchivoDatatec;
            out.Version = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.Version");

			//Obtener Puntas por Tramo
            ConsultarPuntasPorTramo consultarPuntasPorTramo = new ConsultarPuntasPorTramo();
            ConsultarPuntasPorTramo.ConsultarPuntasPorTramoEntrada consultarPuntasPorTramoEntrada = new ConsultarPuntasPorTramo.ConsultarPuntasPorTramoEntrada();
            consultarPuntasPorTramoEntrada.p_IdEmpresa = in.p_IdEmpresa;
            consultarPuntasPorTramoEntrada.p_IdMoneda = "USD";
            ConsultarPuntasPorTramo.ConsultarPuntasPorTramoSalida consultarPuntasPorTramoSalida = consultarPuntasPorTramo.logica(conn, consultarPuntasPorTramoEntrada);
            out.P_OUT_ADVERTENCIASDETALLEPUNTAS = new ArrayList<MensajeSalida>(consultarPuntasPorTramoSalida.P_OUT_MENSAJES);
            out.P_OUT_PUNTAS = new ArrayList<ConsultarPuntasPorTramo.PuntaPorTramoSalida>(consultarPuntasPorTramoSalida.P_OUT_PUNTAS);
            
            if (MensajeSalida.getNumeroErrores(consultarPuntasPorTramoSalida.P_OUT_MENSAJES) > 0) {
				out.P_OUT_MENSAJES.addAll(consultarPuntasPorTramoSalida.P_OUT_MENSAJES);
				//return out;
			}
            
            //ConsultarConfiguracionMesa: TIEMPOESPERAREPROCESO
			out.TiempoEspera = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "TIEMPOESPERAREPROCESO").Valor;
			
			//Reprocesar Datatec Inicio
			ReprocesoDatatec reprocesoDatatec = new ReprocesoDatatec();
			ReprocesoDatatec.ReprocesoDatatecEntrada reprocesoDatatecEntrada = new ReprocesoDatatec.ReprocesoDatatecEntrada();
            reprocesoDatatecEntrada.p_IdEmpresa = in.p_IdEmpresa;
            reprocesoDatatecEntrada.p_IdUsuario = out.IdUsuario;
            reprocesoDatatecEntrada.p_IdModulo = "MSP";
            reprocesoDatatecEntrada.p_UsaReglasNegocio = "S";
            reprocesoDatatecEntrada.p_Fecha = out.Fechasistema;
            reprocesoDatatecEntrada.p_IdEstado = "0";
            reprocesoDatatecEntrada.p_Origen = "REPROCESO";
            ReprocesoDatatec.ReprocesoDatatecSalida reprocesoDatatecSalida = reprocesoDatatec.logica(conn, reprocesoDatatecEntrada);
            out.P_OUT_DATOSDATATEC = new ArrayList<IngresoDatatec.DatosDatatecObjOut>(reprocesoDatatecSalida.P_OUT_DATOSDATATEC);
            
            if (MensajeSalida.getNumeroErrores(reprocesoDatatecSalida.P_OUT_MENSAJES) > 0) {
				out.P_OUT_MENSAJES.addAll(reprocesoDatatecSalida.P_OUT_MENSAJES);
				//return out;
			}
            
            if (reprocesoDatatecSalida.P_OUT_ERRORESCONTADOR.longValue() == 0) {
            	for (MensajeSalida mensajeSalida : reprocesoDatatecSalida.P_OUT_ERRORCONFIGURACIONDETALLE) {
            		if (!"NO_HAY_REGISTROS_A_REPROCESAR".equals(mensajeSalida.codigo)) {
            			IngresoDatatec.DatosDatatecObjOut datatecOut = new IngresoDatatec.DatosDatatecObjOut();
                        datatecOut.NumeroRegistro = mensajeSalida.id;
                        datatecOut.GlosaProceso = mensajeSalida.codigo;
                        out.P_OUT_DATOSDATATEC.add(datatecOut);
            		}
            	}
            }
            else {
            	IngresoDatatec.DatosDatatecObjOut datatecOut = new IngresoDatatec.DatosDatatecObjOut();
                datatecOut.NumeroRegistro = 0L;
                datatecOut.GlosaProceso = "Error en ReprocesoDatatec. Numero de errores :: " + reprocesoDatatecSalida.P_OUT_ERRORESCONTADOR;
                out.P_OUT_DATOSDATATEC.add(datatecOut);
            }
            
            //Obtener IdPersona de CorredoraEmpresa (IdEmpresa) y NombreCorredora de IdPersona
            params.clear();
            params.put("IdEmpresa", in.p_IdEmpresa);
            //out.Nombrecorredora = sql.query(conn, SQL_CONSULTAR_CORREDORA_EMPRESA, params, DatosCorredoraEmpresa.class).get(0).NombreCompleto;
            DatosCorredoraEmpresa datosEmpresa = new DatosCorredoraEmpresa();
            datosEmpresa = sql.query(conn, SQL_CONSULTAR_CORREDORA_EMPRESA, params, DatosCorredoraEmpresa.class).get(0);
            out.CodigoDatatec = datosEmpresa.CodigoDatatec;
            out.NombreEmpresa = datosEmpresa.NombreCompleto;
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Termino del servicio ObtenerValoresInicioDatatec, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_CodigoUsuario\":\"MURIBE\",\"p_BloqueoDesbloqueo\":\"B\",\"p_CodigoParametroFechaSistema\":\"PLA.FechaSistema\",\"p_IdMoneda\":\"USD\",\"p_MontoPorTramo\":0,\"p_CodigoParametroTiempoEsperaProceso\":\"TIEMPOESPERAREPROCESO\"}";
			ObtenerValoresInicioDatatecEntrada Objin = gson.fromJson(json, ObtenerValoresInicioDatatecEntrada.class);
			
			ObtenerValoresInicioDatatec demo = new ObtenerValoresInicioDatatec();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ObtenerValoresInicioDatatecSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.rollback();
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
