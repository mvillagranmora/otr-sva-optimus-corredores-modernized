package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.DiaFeriado;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarFechaSolicitudAporteRescate implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarFechaSolicitudAporteRescate.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarFechaSolicitudAporteRescate_SelectSerieFondo.sql");
	private final String SQL_SELECT_DIAESFERIADO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectDiaEsFeriado.sql");
		
	public static class EntradaConsultarFechaSolicitudAporteRescate{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<SolicitudAporteRescate> p_SolicitudAporteRescate;
		
		public EntradaConsultarFechaSolicitudAporteRescate() {
			this.p_SolicitudAporteRescate = new ArrayList<SolicitudAporteRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarFechaSolicitudAporteRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Resultado> P_OUT_RESULTADO;
		
		public SalidaConsultarFechaSolicitudAporteRescate() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SolicitudAporteRescate{
		public Long IdInterno;
		public String FechaEmision;
		public String IdSerieFondo;
		public String TipoSolicitud;
		public String IdMoneda;
		public String IdMonedaPago;
		
		public SolicitudAporteRescate() {
			this.IdMoneda = "";
			this.IdMonedaPago = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado{
		public Long IdInterno;
		public String IdSerieFondo;
		public Date FechaEmision;
		public Date FechaDigitacion;
		public Date FechaAceptacion;
		public Date FechaValorCuota;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionSerieFondo{
		public String IdSerieFondo;
		public String EstadoAporteComercial;
		public String EstadoRescateComercial;
		public String EstadoAporteBackoffice;
		public String EstadoRescateBackoffice;
		public String EstadoLiquidezComercial;
		public String EstadoLiquidezBackoffice;
		public Long DiasRetencionesAporte;
		public Long DiasRetencionesRescate;
		public Long DiasValutaValorCuotaAporte;
		public Long DiasValutaValorCuotaRescate;
		public Long DiasValutaCertificacionAporte;
		public Long DiasValutaCertificacionRescate;
		public Long DiasValutaCurseAporte;
		public Long DiasValutaCurseRescate;
		public String IdEmisor;
		public String FondoExtranjero;
		public Long IdPaisFondo;
		public Long IdPaisEmisor;
		public Long IdPaisMoneda;
		public String IdMercadoFondoMutuo;
		public String FondoNacionalDolar;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarFechaSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaConsultarFechaSolicitudAporteRescate.class);
		SalidaConsultarFechaSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarFechaSolicitudAporteRescate logica(Connection con, EntradaConsultarFechaSolicitudAporteRescate entrada) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate");
		SalidaConsultarFechaSolicitudAporteRescate out = validacionesServicio(con, entrada);
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			Map<String,ConfiguracionSerieFondo> configuraciones = obtenerConfiguracionSerieFondo(con, entrada.p_SolicitudAporteRescate, entrada.p_IdEmpresa);
			SalidaCalcularFechaSolicitudAporteRescate salidaCalcularFechaSolicitudAporteRescate = obtenerFechasSolicitud(con,entrada.p_SolicitudAporteRescate,configuraciones,entrada.p_IdEmpresa,entrada.p_IdUsuario);
			out.P_OUT_RESULTADO = generarSalida(salidaCalcularFechaSolicitudAporteRescate);
		}
		
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate");
		return out;
	}
	
	private RevisarRequisitosBasicosSalida validarRequisitosBasicos(Connection con, EntradaConsultarFechaSolicitudAporteRescate entrada) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.validarRequisitosBasicos");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.validarRequisitosBasicos");
		return salidaBasicos;
	}
	
	private SalidaObtenerPerfilFondoMutuo obtenerPerfilFondosMutuos(Connection con, Long idUsuario) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerPerfilFondosMutuos");
		ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
		EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
		entradaObtenerPerfilFondoMutuo.p_IdUsuario = idUsuario;
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerPerfilFondosMutuos");
		return salidaObtenerPerfilFondoMutuo;
	}
	
	private Long validarCantidadSolicitudes(List<SolicitudAporteRescate> solicitudes) {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.validarCantidadSolicitudes");
		Long cantidadSolicitudes = new Long(solicitudes.size());
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.validarCantidadSolicitudes");
		return cantidadSolicitudes;
	}
	
	private SalidaConsultarFechaSolicitudAporteRescate validacionesServicio(Connection con, EntradaConsultarFechaSolicitudAporteRescate entrada) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.validacionesServicio");
		SalidaConsultarFechaSolicitudAporteRescate out = new SalidaConsultarFechaSolicitudAporteRescate();
		
		RevisarRequisitosBasicosSalida salidaRequisitosBasicos = validarRequisitosBasicos(con, entrada);
		
		if (MensajeSalida.getNumeroErrores(salidaRequisitosBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaRequisitosBasicos.p_out_Mensajes);
			return out;
		}
		
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondosMutuos(con, entrada.p_IdUsuario);
		
		if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long cantidadSolicitudes = validarCantidadSolicitudes(entrada.p_SolicitudAporteRescate);
		
		if(cantidadSolicitudes.equals(0L)){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.validacionesServicio");
		return out;
	}
	
	private Map<String,ConfiguracionSerieFondo> obtenerConfiguracionSerieFondo(Connection con, List<SolicitudAporteRescate> solicitudes, Long idEmpresa) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerConfiguracionSerieFondo");
		List<String> serieFondos = obtenerSerieFondos(solicitudes);
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSerieFondo", serieFondos);
		params.put("IdEmpresa", idEmpresa);
		List<ConfiguracionSerieFondo> listaConfiguraciones = sql.query(con, SQL_SELECT_SERIE_FONDO, params, ConfiguracionSerieFondo.class);
		Map<String,ConfiguracionSerieFondo> configuraciones = listaConfiguraciones.parallelStream().collect(Collectors.toMap(solicitud->solicitud.IdSerieFondo, solicitud->solicitud));
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerConfiguracionSerieFondo");
		return configuraciones;
		
	}
	
	private List<String> obtenerSerieFondos(List<SolicitudAporteRescate> solicitudes){
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerSerieFondos");
		List<String> serieFondos = solicitudes.parallelStream().map(solicitud->solicitud.IdSerieFondo).collect(Collectors.toList());
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerSerieFondos");
		return serieFondos;
	}
	
	private Long obtenerPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerPaisSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerPaisSistema");
		return idPaisEmpresa;
	}
	
	private SalidaCalcularFechaSolicitudAporteRescate obtenerFechasSolicitud(Connection con, List<SolicitudAporteRescate> solicitudes, Map<String,ConfiguracionSerieFondo> configuraciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerFechasSolicitud");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		CalcularFechaSolicitudAporteRescate calcularFechaSolicitudAporteRescate = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaCalcularFechaSolicitudAporteRescate = new EntradaCalcularFechaSolicitudAporteRescate();
		Long idPais = obtenerPaisSistema(con, idEmpresa);
		entradaCalcularFechaSolicitudAporteRescate.p_IdEmpresa = idEmpresa;
		entradaCalcularFechaSolicitudAporteRescate.p_IdPais = idPais;
		entradaCalcularFechaSolicitudAporteRescate.p_SolicitudAporteRescate = obtenerFechasEntrada(con,solicitudes,configuraciones,idUsuario);
		SalidaCalcularFechaSolicitudAporteRescate salidaCalcularFechaSolicitudAporteRescate = calcularFechaSolicitudAporteRescate.logica(con, entradaCalcularFechaSolicitudAporteRescate);
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerFechasSolicitud");
		return salidaCalcularFechaSolicitudAporteRescate;
	}
	
	private List<CalcularFechaSolicitudAporteRescate.InFechas> obtenerFechasEntrada(Connection con, List<SolicitudAporteRescate> solicitudes, Map<String,ConfiguracionSerieFondo> configuraciones, Long idUsuario) throws Exception{
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.obtenerFechasEntrada");
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondosMutuos(con, idUsuario);
		String perfil = salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo;
		
		List<CalcularFechaSolicitudAporteRescate.InFechas> fechasEntrada = solicitudes.parallelStream().map(solicitud->{
			CalcularFechaSolicitudAporteRescate.InFechas inFecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			ConfiguracionSerieFondo configuracion = configuraciones.get(solicitud.IdSerieFondo);
			inFecha.IdInterno = solicitud.IdInterno;
			inFecha.IdSerieFondo = solicitud.IdSerieFondo;
			inFecha.FechaEmision = solicitud.FechaEmision;
			inFecha.SolicitudBatch = _c.NO;
			inFecha.TipoSolicitud = solicitud.TipoSolicitud;
			
			//SE VERIFICA QUE LOS FONDOS SEAN EXTRANJEROS CUANDO SE CONSULTA POR LA REINVERSION
			String fondosExtranjeros = _c.NO;
			String mismoEmisor = _c.NO;
			String idMonedaReinversionDiferentes = "";
			Integer solicitudesSize = solicitudes.size();
			if(solicitudes.size() > 1) {
				String emisor = configuraciones.get(solicitudes.get(0).IdSerieFondo).IdEmisor;
				mismoEmisor = _c.SI;
				fondosExtranjeros = _c.SI;
				idMonedaReinversionDiferentes = _c.NO;
				String idMoneda = solicitudes.get(0).IdMoneda;
				for(SolicitudAporteRescate s : solicitudes) {
					if(_c.NO.equalsIgnoreCase(configuraciones.get(s.IdSerieFondo).FondoExtranjero)) fondosExtranjeros = _c.NO;
					if(!emisor.equalsIgnoreCase(configuraciones.get(s.IdSerieFondo).IdEmisor)) mismoEmisor = _c.NO;
					if(!idMoneda.equalsIgnoreCase(s.IdMoneda)) idMonedaReinversionDiferentes = _c.SI;
				}
			}
			
			if(configuracion != null) {
				inFecha.EstadoLiquidacion = _c.PerfilComercial.equals(perfil) ? configuracion.EstadoLiquidezComercial : configuracion.EstadoLiquidezBackoffice;
				if(_c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud)) {
					inFecha.EstadoFondo = _c.PerfilComercial.equals(perfil) ? configuracion.EstadoAporteComercial : configuracion.EstadoAporteBackoffice;
					inFecha.NumeroDiasRetencion = configuracion.DiasRetencionesAporte;
					inFecha.NumeroValutaValorCuota = configuracion.DiasValutaValorCuotaAporte;
					inFecha.NumeroValutaCertificacion = configuracion.DiasValutaCertificacionAporte;
					inFecha.NumeroValutaCurse = configuracion.DiasValutaCurseAporte;
				}else {
					inFecha.EstadoFondo = _c.PerfilComercial.equals(perfil) ? configuracion.EstadoRescateComercial : configuracion.EstadoRescateBackoffice;
					inFecha.NumeroDiasRetencion = fondosExtranjeros.equalsIgnoreCase(_c.SI) ? (mismoEmisor.equalsIgnoreCase(_c.NO) ? 4 : 2): configuracion.DiasRetencionesRescate;
					inFecha.NumeroValutaValorCuota = fondosExtranjeros.equalsIgnoreCase(_c.SI) ? 1: configuracion.DiasValutaValorCuotaRescate;
					inFecha.NumeroValutaCertificacion = fondosExtranjeros.equalsIgnoreCase(_c.SI) ? 2: configuracion.DiasValutaCertificacionRescate;
					inFecha.NumeroValutaCurse = configuracion.DiasValutaCurseRescate;
				}
				inFecha.FondoExtranjero = configuracion.FondoExtranjero;
				inFecha.IdPaisFondo = configuracion.IdPaisFondo;
				inFecha.IdPaisEmisor = configuracion.IdPaisEmisor;
				inFecha.IdPaisMoneda = configuracion.IdPaisMoneda;
				inFecha.FondoNacionalDolar = configuracion.FondoNacionalDolar;
				inFecha.IdMercadoFondoMutuo = configuracion.IdMercadoFondoMutuo;
				if(solicitudesSize.compareTo(1) == 0)
					inFecha.RescateGeneraSpotCertificacion = (!solicitud.IdMoneda.equalsIgnoreCase(solicitud.IdMonedaPago) && !"".equalsIgnoreCase(solicitud.IdMonedaPago) && !"".equalsIgnoreCase(solicitud.IdMoneda)) ? _c.SI : _c.NO;
				else
					inFecha.RescateGeneraSpotCertificacion = (_c.SI.equalsIgnoreCase(idMonedaReinversionDiferentes)) ? _c.SI : _c.NO;
			}
			
			return inFecha;
		}).collect(Collectors.toList());
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.obtenerFechasEntrada");
		return fechasEntrada;
	}
	
	private List<Resultado> generarSalida(SalidaCalcularFechaSolicitudAporteRescate fechasSolicitudes){
		logger.info("INICIO::ConsultarFechaSolicitudAporteRescate.generarSalida");
		List<Resultado> resultados = fechasSolicitudes.P_FECHASAPORTERESCATE.parallelStream().map(fecha->{
			Resultado resultado = new Resultado();
			resultado.IdInterno = fecha.IdInterno;
			resultado.IdSerieFondo = fecha.IdSerieFondo;
			resultado.FechaEmision = fecha.FechaEmision;
			resultado.FechaDigitacion = fecha.FechaDigitacion;
			resultado.FechaAceptacion = fecha.FechaAceptacion;
			resultado.FechaValorCuota = fecha.FechaValorCuota;
			resultado.FechaCertificacion = fecha.FechaCertificacion;
			resultado.FechaLiquidacion = fecha.FechaLiquidacion;
			return resultado;
		}).collect(Collectors.toList());
		logger.info("FIN::ConsultarFechaSolicitudAporteRescate.generarSalida");
		
		return resultados;
	}
	
	public static void main(String[] args){
		
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getBOLSAConnection();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudAporteRescate\":[{\"IdInterno\":0,\"FechaEmision\":\"2020-04-27\",\"IdSerieFondo\":\"00062PUSD\",\"TipoSolicitud\":\"RES\"},{\"IdInterno\":0,\"FechaEmision\":\"2020-04-27\",\"IdSerieFondo\":\"00047PUSD\",\"TipoSolicitud\":\"RES\"}]}";
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarFechaSolicitudAporteRescate entrada = gson.fromJson(json, EntradaConsultarFechaSolicitudAporteRescate.class);
			ConsultarFechaSolicitudAporteRescate consultarFechaSolicitudAporteRescate = new ConsultarFechaSolicitudAporteRescate();
			SalidaConsultarFechaSolicitudAporteRescate objOut = consultarFechaSolicitudAporteRescate.logica(con, entrada);
			System.out.println(objOut.toString());
			con.rollback();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

}