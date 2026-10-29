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

public class CrearDireccionFisica implements JavaCall{
	public String SQL_CREARDIRECCIONFISICA_INSERT = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionFisica_Insert.sql");
	
	public final String ID_CONCEPTO = "NUEDIF";
	
	static final Logger logger = LogManager.getLogger(CrearDireccionFisica.class);
	
	public static class EntradaCrearDireccionFisica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;

		public String p_Calle;
		public String p_Numero;
		public String p_Departamento;
		public String p_Sector;
		public String p_CodigoPostal;
		public Long p_IdCiudad;
		public Long p_IdDivisionPais;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;
		public Long p_IdPersona;
		public String p_CodigoCompuesto;
		public String p_Tipo;
		public String p_Predeterminado;
		
		public EntradaCrearDireccionFisica() {
			this.p_Predeterminado = "N";
		}
	}
	
	public static class EntidadDireccionFisica extends EntradaCrearDireccionFisica{
		public long p_IdDireccionFisicaPrincipal;
		public String p_IdEstadoAutorizacion;
		
		public EntidadDireccionFisica(){
			super();
		}
		
		public void setDireccionFisica(EntradaCrearDireccionFisica in){
			this.p_IdEmpresa = in.p_IdEmpresa;
			this.p_IdUsuario = in.p_IdUsuario;
			this.p_IdModulo = in.p_IdModulo;
			this.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			this.p_Calle = in.p_Calle;
			this.p_Numero = in.p_Numero;
			this.p_Departamento = in.p_Departamento;
			this.p_Sector = in.p_Sector;
			this.p_CodigoPostal = in.p_CodigoPostal;
			this.p_IdCiudad = in.p_IdCiudad;
			this.p_IdDivisionPais = in.p_IdDivisionPais;
			this.p_CodigoProposito = in.p_CodigoProposito;
			this.p_Comentario = in.p_Comentario;
			this.p_IdEstado = in.p_IdEstado;
			this.p_IdPersona = in.p_IdPersona;
			this.p_CodigoCompuesto = in.p_CodigoCompuesto;
			this.p_Tipo = in.p_Tipo;
			this.p_Predeterminado = in.p_Predeterminado;
		}
	}
	
	public static class SalidaCrearDireccionFisica extends SalidaEstandar{
		public long p_out_RefId;
		
		public SalidaCrearDireccionFisica(){
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
	
	public SalidaCrearDireccionFisica logica(Connection conn, EntradaCrearDireccionFisica in) throws Exception{
		SQL sql = new SQL();
		SalidaCrearDireccionFisica salida = new SalidaCrearDireccionFisica();
		
		Long idDireccionFisica = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica", (long) 1).get(0);
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		
		//preguntar si requiere autorizacion
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
		
		EntidadDireccionFisica direccionFisica = new EntidadDireccionFisica();
		direccionFisica.setDireccionFisica(in);
		direccionFisica.p_IdDireccionFisicaPrincipal = idDireccionFisica;
		direccionFisica.p_IdEstadoAutorizacion = (requiereAutorizacion.equals("S") && in.p_UsaReglasNegocio.equals("S")) ? "PEN" : "ING";
		logger.debug("INICIO::SQL_CREARDIRECCIONFISICA_INSERT");
		int[] res = sql.batch(conn, SQL_CREARDIRECCIONFISICA_INSERT, Arrays.asList(direccionFisica), EntidadDireccionFisica.class);
		logger.debug("FIN::SQL_CREARDIRECCIONFISICA_INSERT");
		if(res!=null && res.length>0 && res[0]==1){
			
			salida.p_out_RefId = idDireccionFisica;
			
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
				evento.NumeroOperacionConcepto = String.valueOf(idDireccionFisica);
				evento.FechaOperacionConcepto = fechaSistema;
				evento.IdCliente = in.p_IdPersona;
				evento.Glosa = in.p_Calle + " / " + "TEST";
				
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
			salida.addError("UI_ERROR_AL_CREAR_DIRECCION_FISICA", "UI_ERROR_AL_CREAR_DIRECCION_FISICA");
		}
		return salida;
	}

}
