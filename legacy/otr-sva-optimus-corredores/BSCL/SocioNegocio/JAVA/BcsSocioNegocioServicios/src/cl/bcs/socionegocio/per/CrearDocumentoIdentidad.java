package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaDocumentosIdentidad.EntradaConfirmarCrearPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaDocumentosIdentidad.SalidaConfirmarCrearPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDocumentoIdentidad implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearDocumentoIdentidad.class);
	
	private String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidad_SelectDocumentoIdentidad.sql");
	private String SQL_SELECT_DOCUMENTO_IDENTIDAD_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidad_SelectDocumentoIdentidadPersona.sql");

	private String SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDocumentoIdentidad_Insert.sql");
	
	private final String ID_CONCEPTO = "NUEDOC";
	
	
	public static class EntradaCrearDocumentoIdentidad{
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public long p_IdPersona;
		public String p_IdTipoDocumentoIdentidad;
		public String p_DocumentoIdentidad;
		public String p_NumeroSerie;
		public String p_FechaEmision;
		public String p_FechaVencimiento;
		public String p_IdTipoDocumentoIdentidadAsociado;
		public String p_TipoPersona;
	}
	
	public static class EntidadDocumentoIdentidad extends EntradaCrearDocumentoIdentidad{
		public long p_IdDocumentoIdentidad;
		public String p_FechaIngreso;
		public String p_Predeterminado;
		public String p_DocumentoIdentidadAsociado;
		public String p_IdEstadoAutorizacion;
		
		public EntidadDocumentoIdentidad(EntradaCrearDocumentoIdentidad in){
			super();
			this.p_IdUsuario = in.p_IdUsuario;
			this.p_IdPersona = in.p_IdPersona;
			this.p_IdTipoDocumentoIdentidad = in.p_IdTipoDocumentoIdentidad;
			this.p_DocumentoIdentidad = in.p_DocumentoIdentidad;
			this.p_NumeroSerie = in.p_NumeroSerie;
			this.p_FechaEmision = in.p_FechaEmision;
			this.p_FechaVencimiento = in.p_FechaVencimiento;
			this.p_IdTipoDocumentoIdentidadAsociado = in.p_IdTipoDocumentoIdentidadAsociado;
			this.p_TipoPersona = in.p_TipoPersona;
			this.p_Predeterminado = "N";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearDocumentoIdentidad extends SalidaEstandar{
		public SalidaCrearDocumentoIdentidad(){
			this.p_out_RefId = 0;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE= new ArrayList<ErrorDetalle>();
		}
		public long p_out_RefId;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contador{
		public long Count;
		@Override
		public String toString() {
			return String.format("Contador [Count=%s]", Count);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		
		return null;
	}
	
	public SalidaCrearDocumentoIdentidad logica(Connection conn, EntradaCrearDocumentoIdentidad in) throws Exception{
		
		SalidaCrearDocumentoIdentidad salida = new SalidaCrearDocumentoIdentidad();
		SQL sql = new SQL();
		
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		String CodigoPais = Utils.getConfiguracion(conn, Utils.getSchema(), in.p_IdEmpresa, "PLA.LocalizacionSistema");
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("DocumentoIdentidad", in.p_DocumentoIdentidad);
		params.put("TipoDocumentoIdentidad", in.p_IdTipoDocumentoIdentidad);
		logger.debug("INICIO::SQL_CREAR_DOCUMENTO_IDENTIDAD_ExisteDocumento");
		List<Contador> contador = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, params, Contador.class);
		logger.debug("FIN::SQL_CREAR_DOCUMENTO_IDENTIDAD_ExisteDocumento");
		if(!contador.isEmpty() && contador.get(0).Count>0){
			salida.addError("UI_DOCUMENTO_IDENTIDAD_EXISTE", "UI_DOCUMENTO_IDENTIDAD_EXISTE");
			return salida;
		}
		
		String requiereAutorizacion = (in.p_UsaReglasNegocio!=null && in.p_UsaReglasNegocio.equals("S")) ? Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO) : "N";

		params = new HashMap<String, Object>();
		params.put("IdPersona", in.p_IdPersona);
		params.put("IdTipoDocumentoIdentidad", in.p_IdTipoDocumentoIdentidad);
		logger.debug("INICIO::SQL_CREAR_DOCUMENTO_IDENTIDAD_ExisteDocumentoPersona");
		contador = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD_PERSONA, params, Contador.class);
		logger.debug("FIN::SQL_CREAR_DOCUMENTO_IDENTIDAD_ExisteDocumentoPersona");
		if(!contador.isEmpty() && contador.get(0).Count>0){
			salida.addError("UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE", "UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE");
			return salida;
		}

		Long idDocumentoIdentidad = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", (long) 1).get(0);
		//map con entidad de entrada
		EntidadDocumentoIdentidad documento = new EntidadDocumentoIdentidad(in);
		documento.p_IdDocumentoIdentidad = idDocumentoIdentidad;
		documento.p_FechaIngreso = fechaSistema;
		documento.p_IdEstadoAutorizacion = (requiereAutorizacion.equals("S")) ? "PEN" : "ING";
		
		if(CodigoPais.equals("PE") && documento.p_IdTipoDocumentoIdentidad.equals("IUP")){
			documento.p_DocumentoIdentidadAsociado = IUP.obtenerDocumentoIdentidadAsociado(documento.p_DocumentoIdentidad, documento.p_IdTipoDocumentoIdentidadAsociado, documento.p_TipoPersona);
		}
		logger.debug("INICIO::SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert");
		sql.batch(conn, SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert, Arrays.asList(documento), EntidadDocumentoIdentidad.class);
		logger.debug("INICIO::SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert");
		
		//si es peru, y es IUP, crear un documento nuevo con los datos del doc asociado.
		if(CodigoPais.equals("PE") && documento.p_IdTipoDocumentoIdentidad.equals("IUP")){
			documento.p_IdDocumentoIdentidad = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", (long) 1).get(0);
			documento.p_DocumentoIdentidad = documento.p_DocumentoIdentidadAsociado;
			documento.p_IdTipoDocumentoIdentidad = documento.p_IdTipoDocumentoIdentidadAsociado;
			logger.debug("INICIO::SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert");
			sql.batch(conn, SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert, Arrays.asList(documento), EntidadDocumentoIdentidad.class);
			logger.debug("FIN::SQL_CREAR_DOCUMENTO_IDENTIDAD_Insert");
		}
		salida.p_out_RefId = idDocumentoIdentidad;
		
		
		if(requiereAutorizacion!=null && requiereAutorizacion.equals("S")){
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
			evento.NumeroOperacionConcepto = String.valueOf(idDocumentoIdentidad);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = in.p_IdPersona;
			evento.Glosa = in.p_DocumentoIdentidad + " / " + "TEST";
			
			entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
			
			CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn, entradaEvtAutorizacion);
			
			for(CrearEventoAutorizacionMasivo.ObjERRORESDETALLE advertencia : salidaAut.P_OUT_ERRORESDETALLE){
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}
			for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
				salida.addError(error.Codigo, error.Descripcion);
			}
		}else{
			
			EntradaConfirmarCrearPersonaDocumentosIdentidad entradaConfirmarCrearPersonaDocumentosIdentidad = new EntradaConfirmarCrearPersonaDocumentosIdentidad();
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_IdEmpresa = in.p_IdEmpresa;
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_IdUsuario = in.p_IdUsuario;
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_IdModulo = in.p_IdModulo;
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_UsaReglasNegocio = "N";
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_IdDocumentoIdentidad = idDocumentoIdentidad;
			entradaConfirmarCrearPersonaDocumentosIdentidad.p_IdEstadoAutorizacion = "ACT";
			ConfirmarCrearPersonaDocumentosIdentidad confirmarCrearPersonaDocumentosIdentidad = new ConfirmarCrearPersonaDocumentosIdentidad();
			SalidaConfirmarCrearPersonaDocumentosIdentidad salidaConfirmarCrearPersonaDocumentosIdentidad = confirmarCrearPersonaDocumentosIdentidad.logica(conn, entradaConfirmarCrearPersonaDocumentosIdentidad);

			salida.P_OUT_ERRORESCONTADOR += salidaConfirmarCrearPersonaDocumentosIdentidad.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mensaje : salidaConfirmarCrearPersonaDocumentosIdentidad.P_OUT_MENSAJES) {
				if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
					ErrorDetalle errorDetalle = new ErrorDetalle();
					errorDetalle.Codigo = mensaje.codigo;
					errorDetalle.Descripcion = mensaje.descripcion;
					salida.P_OUT_ERRORESDETALLE.add(errorDetalle);
				}
				
				if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
					AdvertenciaDetalle adevertenciaDetalle = new AdvertenciaDetalle();
					adevertenciaDetalle.Codigo = mensaje.codigo;
					adevertenciaDetalle.Descripcion = mensaje.descripcion;
					adevertenciaDetalle.RequiereAutorizacion = "N";
					salida.P_OUT_ADVERTENCIASDETALLE.add(adevertenciaDetalle);
				}
				
				if(MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
					AdvertenciaDetalle adevertenciaDetalle = new AdvertenciaDetalle();
					adevertenciaDetalle.Codigo = mensaje.codigo;
					adevertenciaDetalle.Descripcion = mensaje.descripcion;
					adevertenciaDetalle.RequiereAutorizacion = "S";
					salida.P_OUT_ADVERTENCIASDETALLE.add(adevertenciaDetalle);
				}
			}
		}
		
		return salida;
	}

}
