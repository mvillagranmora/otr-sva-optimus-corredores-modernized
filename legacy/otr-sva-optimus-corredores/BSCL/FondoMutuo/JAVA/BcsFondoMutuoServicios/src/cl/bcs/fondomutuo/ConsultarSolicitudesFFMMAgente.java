package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConsultarAsignaciones;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.EntradaConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.SalidaConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.Solicitud;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarSolicitudesFFMMAgente implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarAsignaciones.class.getName());
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarSolicitudesFFMMAgente in = gson.fromJson(jin.toString(), EntradaConsultarSolicitudesFFMMAgente.class);
		SalidaConsultarSolicitudesFFMMAgente out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
		public Portafolio(Long idPortafolio, Long IdTrabajo) {
			this.IdPortafolio = idPortafolio;
			this.IdTrabajo = IdTrabajo;
		}
		public Portafolio() {}
	}
	
	public static class EntradaConsultarSolicitudesFFMMAgente{
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		//cliente
		public Long		p_IdPortafolio;
		public Long 	p_IdCliente;
		public Long 	p_Producto;
		
		//filtros
		public String p_IdEmisor;
		public String p_Estado;
		public String p_FechaAceptacionDesde;
		public String p_FechaAceptacionHasta;
		public String p_IdSerieFondo;
		public String p_IdTipoMovimientoFondoMutuo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaConsultarSolicitudesFFMMAgente{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<Solicitud> P_OUT_SOLICITUDES;
		
		public SalidaConsultarSolicitudesFFMMAgente() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_SOLICITUDES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	private List<Solicitud> filtraSolicitudesPortafolio(Connection conn, EntradaConsultarSolicitudesFFMMAgente entrada, List<Solicitud> SolicitudesFFMM) throws Exception {

		List<Long> listIdPortafolio = new ArrayList<Long>();
		List<Solicitud> consultaFiltrada =  new ArrayList<>();
		logger.debug("INICIO FILTRA PORTAFOLIO EJECUTIVO");
		try {
			if(entrada.p_IdPortafolio != 0) 
			{
				//parallel stream obteniendo solo la data de 1 cliente
				consultaFiltrada = SolicitudesFFMM.parallelStream().filter(registro -> entrada.p_IdPortafolio.equals(registro.IdPortafolio)).collect(Collectors.toList());
			}
			else 
			{
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					//Se filtra de la data los portafolios de la lista
					for (Solicitud reg : SolicitudesFFMM) {
						if(listIdPortafolio.contains(reg.IdPortafolio)) {
							consultaFiltrada.add(reg);
						}
					}
				}
				else {
					return SolicitudesFFMM;
				}
			}

			logger.debug("FIN FILTRA PORTAFOLIO EJECUTIVO");
			return consultaFiltrada;
		}
		catch (Exception e) {
			logger.debug("ERROR AL FILTRAR PORTAFOLIO EJECUTIVO - " + e.getMessage());
			throw e;
		}
		
	}
	
	
	@SuppressWarnings("deprecation")
	public SalidaConsultarSolicitudesFFMMAgente logica(Connection conn, EntradaConsultarSolicitudesFFMMAgente entrada) throws Exception{
		SalidaConsultarSolicitudesFFMMAgente salida = new SalidaConsultarSolicitudesFFMMAgente();
		logger.info("INICIO CONSULTA SOLICITUD FFMM");
		
		try {
			EntradaConsultarSolicitudAporteRescateUI paramsIn = new EntradaConsultarSolicitudAporteRescateUI();
			paramsIn.p_IdEmpresa = entrada.p_IdEmpresa;
			paramsIn.p_IdUsuario = entrada.p_IdUsuario;
			paramsIn.p_IdModulo = entrada.p_IdModulo;
			paramsIn.p_IdEmisor = entrada.p_IdEmisor;
			paramsIn.p_Estado = entrada.p_Estado;
			paramsIn.p_FechaAceptacionDesde = entrada.p_FechaAceptacionDesde;
			paramsIn.p_FechaAceptacionHasta = entrada.p_FechaAceptacionHasta;
			paramsIn.p_IdSerieFondo = entrada.p_IdSerieFondo;
			paramsIn.p_IdTipoMovimientoFondoMutuo = entrada.p_IdTipoMovimientoFondoMutuo;
			
			paramsIn.p_IdCliente = 0L;
			paramsIn.p_IdPortafolio = 0L;
			paramsIn.p_TipoSolicitud = "";
			paramsIn.p_IdMoneda = "";
			paramsIn.p_IdProductoAgrupador = 0L;
			paramsIn.p_IdSolicitudAporteRescate = 0L;
			
			ConsultarSolicitudAporteRescateUI  ConsultaSolicitudes =new ConsultarSolicitudAporteRescateUI();
			SalidaConsultarSolicitudAporteRescateUI ConsultaSolicitudesOut = ConsultaSolicitudes.logica(conn, paramsIn);
			salida.P_OUT_SOLICITUDES = filtraSolicitudesPortafolio(conn, entrada,ConsultaSolicitudesOut.P_OUT_RESULTADO);
			
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
				
		logger.info("FIN CONSULTA SOLICITUDES FFMM");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();			
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"MSP","p_IdEmpleadoConsulta":7889,"p_IdPortafolio":0,"p_IdCliente":0,"p_Producto":"0","p_Mesa":"MEME","p_FechaDesde":"2021-04-13T00:00:00","p_FechaHasta":"2021-04-13T23:59:59","p_IdTipoOperacion":"0"}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":7305,\"p_IdPortafolio\":0,\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-04-13T23:59:59\",\"p_IdTipoOperacion\":\"COMPRA\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_IdEmpleadoConsulta\":7889,\"p_IdPortafolio\":0,\"p_IdCliente\":0,\"p_Producto\":\"0\",\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-09-30T23:59:59\",\"p_IdTipoOperacion\":\"0\"}";
			EntradaConsultarSolicitudesFFMMAgente Objin = gson.fromJson(json, EntradaConsultarSolicitudesFFMMAgente.class);
			ConsultarSolicitudesFFMMAgente demo = new ConsultarSolicitudesFFMMAgente();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				SalidaConsultarSolicitudesFFMMAgente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA:" + ObjOut.P_OUT_SOLICITUDES.size());
				
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	

}
