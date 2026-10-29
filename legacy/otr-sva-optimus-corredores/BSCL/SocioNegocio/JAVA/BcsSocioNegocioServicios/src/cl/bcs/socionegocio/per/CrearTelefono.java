package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTelefono implements JavaCall{
	public String SQL_CREARTELEFONO_INSERT = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearTelefono_Insert.sql");
	
	public final String ID_CONCEPTO = "NUETEL";
	
	static final Logger logger = LogManager.getLogger(CrearTelefono.class);
	
	public static class EntradaCrearTelefono{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;

		public Long p_IdPais;
		public String p_IdTipoTelefono;
		public String p_Numero;
		public String p_Anexo;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;
		public String p_Tipo;
		public Long p_IdPersona;
		public String p_Predeterminado;
		
		public EntradaCrearTelefono() {
			this.p_Predeterminado = "N";
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadTelefono extends EntradaCrearTelefono{
		public long p_IdTelefonoPrincipal;
		public String p_IdEstadoAutorizacion;
		
		public EntidadTelefono(){
			super();
		}
		
		public void setTelefono(EntradaCrearTelefono in){
			this.p_IdEmpresa = in.p_IdEmpresa;
			this.p_IdUsuario = in.p_IdUsuario;
			this.p_IdModulo = in.p_IdModulo;
			this.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			this.p_IdPais = in.p_IdPais;
			this.p_IdTipoTelefono = in.p_IdTipoTelefono;
			this.p_Numero = in.p_Numero;
			this.p_Anexo = in.p_Anexo;
			this.p_CodigoProposito = in.p_CodigoProposito;
			this.p_Comentario = in.p_Comentario;
			this.p_IdEstado = in.p_IdEstado;
			this.p_IdPersona = in.p_IdPersona;
			this.p_Predeterminado = in.p_Predeterminado;
		}
	}
	
	public static class SalidaCrearTelefono extends SalidaEstandar{
		public long p_out_RefId;
		
		public SalidaCrearTelefono(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaCrearTelefono logica(Connection conn, EntradaCrearTelefono in) throws Exception{
		SQL sql = new SQL();
		SalidaCrearTelefono salida = new SalidaCrearTelefono();
		
		Long idTelefono = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Telefono", (long) 1).get(0);
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		
		//preguntar si requiere autorizacion
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
		
		EntidadTelefono Telefono = new EntidadTelefono();
		Telefono.setTelefono(in);
		Telefono.p_IdTelefonoPrincipal = idTelefono;
		Telefono.p_IdEstadoAutorizacion = (requiereAutorizacion.equals("S") && in.p_UsaReglasNegocio.equals("S")) ? "PEN" : "ING";
		logger.debug("INICIO::SQL_CREARTELEFONO_INSERT");
		int[] res = sql.batch(conn, SQL_CREARTELEFONO_INSERT, Arrays.asList(Telefono), EntidadTelefono.class);
		logger.debug("FIN::SQL_CREARTELEFONO_INSERT");
		if(res!=null && res.length>0 && res[0]==1){
			
			salida.p_out_RefId = idTelefono;
			
			if(requiereAutorizacion.equals("S")){
				CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
				
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
				entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
				entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
				entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
				entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
				
				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				evento.IdTipoEvento = ID_CONCEPTO;
				evento.IdModuloSolicita = in.p_IdModulo;
				evento.IdUsuarioSolicita = in.p_IdUsuario;
				evento.Fecha = fechaSistema;
				evento.IdConcepto = ID_CONCEPTO;
				evento.NumeroOperacionConcepto = String.valueOf(idTelefono);
				evento.FechaOperacionConcepto = fechaSistema;
				evento.IdCliente = in.p_IdPersona;
				evento.Glosa = in.p_Numero + " / " + "TEST";
				
				entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
				
				CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn, entradaEvtAutorizacion);
				
				for(CrearEventoAutorizacionMasivo.ObjERRORESDETALLE advertencia : salidaAut.P_OUT_ERRORESDETALLE){
					salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
				}
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
					salida.addError(error.Codigo, error.Descripcion);
				}
			}
		}else{
			salida.addError("UI_ERROR_AL_CREAR_TELEFONO", "UI_ERROR_AL_CREAR_TELEFONO");
		}
		return salida;
	}

}
