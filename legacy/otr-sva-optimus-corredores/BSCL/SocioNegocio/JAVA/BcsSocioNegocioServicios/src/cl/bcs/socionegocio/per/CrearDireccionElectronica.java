package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDireccionElectronica implements JavaCall{
	public String SQL_CREARDIRECCIONELECTRONICA_INSERT = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionElectronica_Insert.sql");
	
	public final String ID_CONCEPTO = "NUEDIE";
	
	static final Logger logger = LogManager.getLogger(CrearDireccionElectronica.class);
	
	public static class EntradaCrearDireccionElectronica{
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;

		public String p_Direccion;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;
		public String p_Tipo;
		public Long p_IdPersona;
		public String p_Predeterminado;
		
		public EntradaCrearDireccionElectronica() {
			this.p_Predeterminado = "N";
		}
	}
	
	public static class EntidadDireccionElectronica extends EntradaCrearDireccionElectronica{
		public long p_IdDireccionElectronica;
		public String p_IdEstadoAutorizacion;
		public String p_FechaIngreso;
		
		public EntidadDireccionElectronica(){
			super();
		}
		
		public void setDireccionElectronica(EntradaCrearDireccionElectronica in){
			this.p_IdEmpresa = in.p_IdEmpresa;
			this.p_IdUsuario = in.p_IdUsuario;
			this.p_IdModulo = in.p_IdModulo;
			this.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			this.p_Direccion = in.p_Direccion;
			this.p_CodigoProposito = in.p_CodigoProposito;
			this.p_Comentario = in.p_Comentario;
			this.p_IdEstado = in.p_IdEstado;
			this.p_Tipo = in.p_Tipo;
			this.p_IdPersona = in.p_IdPersona;
			this.p_Predeterminado = in.p_Predeterminado;
		}
	}
	
	public static class SalidaCrearDireccionElectronica extends SalidaEstandar{
		public long p_out_RefId;
		
		public SalidaCrearDireccionElectronica(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
	    Gson gson = Utils.getGsonBuilder();

	    EntradaCrearDireccionElectronica entrada =
	        gson.fromJson(in.toString(), EntradaCrearDireccionElectronica.class);

	    SalidaCrearDireccionElectronica salida = logica(conn, entrada);

	    JsonParser parser = new JsonParser();
	    return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}
	
	public SalidaCrearDireccionElectronica logica(Connection conn, EntradaCrearDireccionElectronica in) throws Exception{
		SQL sql = new SQL();
		SalidaCrearDireccionElectronica salida = new SalidaCrearDireccionElectronica();
		
		Long idDireccionElectronica = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica", (long) 1).get(0);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		Boolean esOperacionConAutorizacion =
		    SocioNegocioUtils.esOperacionConAutorizacion(
		        conn,
		        in.p_IdEmpresa,
		        ID_CONCEPTO,
		        in.p_UsaReglasNegocio
		    );
		
		EntidadDireccionElectronica direccionElectronica = new EntidadDireccionElectronica();
		direccionElectronica.setDireccionElectronica(in);
		direccionElectronica.p_IdDireccionElectronica = idDireccionElectronica;
		direccionElectronica.p_FechaIngreso = fechaSistema;
		direccionElectronica.p_IdEstadoAutorizacion =
		    esOperacionConAutorizacion ? "PEN" : "ING";
		logger.debug("INICIO::SQL_CREARDIRECCIONELECTRONICA_INSERT");
		int[] res = sql.batch(conn, SQL_CREARDIRECCIONELECTRONICA_INSERT, Arrays.asList(direccionElectronica), EntidadDireccionElectronica.class);
		logger.debug("FIN::SQL_CREARDIRECCIONELECTRONICA_INSERT");
		if(res!=null && res.length>0 && res[0]==1){
			
		    salida.p_out_RefId = idDireccionElectronica;

		    if (esOperacionConAutorizacion) {
	        	CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();

		        CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion =
		            new CrearEventoAutorizacionMasivo.ServiceEntrada();

		        entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
		        entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
		        entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
		        entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
		        entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";

		        CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento =
		            new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();

		        evento.IdTipoEvento = ID_CONCEPTO;
		        evento.IdModuloSolicita = in.p_IdModulo;
		        evento.IdUsuarioSolicita = in.p_IdUsuario;
		        evento.Fecha = fechaSistema;
		        evento.IdConcepto = ID_CONCEPTO;
		        evento.NumeroOperacionConcepto = String.valueOf(idDireccionElectronica);
		        evento.FechaOperacionConcepto = fechaSistema;
		        evento.IdCliente = in.p_IdPersona;
		        evento.Glosa = in.p_Direccion;

		        entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);

		        CrearEventoAutorizacionMasivo.ServiceSalida salidaAut =
		            evtAutorizacion.logica(conn, entradaEvtAutorizacion);

		        salida.P_OUT_ERRORESCONTADOR = salidaAut.P_OUT_ERRORESCONTADOR;

		        for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE adv : salidaAut.P_OUT_ADVERTENCIASDETALLE) {
		            salida.addAdvertencia(adv.Codigo, adv.Descripcion);
		        }

		        for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE err : salidaAut.P_OUT_ERRORESDETALLE) {
		            salida.addError(err.Codigo, err.Descripcion);
		        }

		        if (salida.P_OUT_ERRORESCONTADOR == 0L) {
		            String mensajeAutorizacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String,String>());
		            salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
		        }
		    }
		}else{
			salida.addError("UI_ERROR_AL_CREAR_DIRECCION_ELECTRONICA", "UI_ERROR_AL_CREAR_DIRECCION_ELECTRONICA");
		}
		return salida;
	}
}