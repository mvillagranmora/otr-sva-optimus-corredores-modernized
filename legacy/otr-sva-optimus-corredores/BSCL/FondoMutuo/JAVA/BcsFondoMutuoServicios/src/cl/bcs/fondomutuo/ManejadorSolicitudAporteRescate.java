package cl.bcs.fondomutuo;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateProgramado.EntradaCrearSolicitudAporteRescateProgramado;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateProgramado.SalidaCrearSolicitudAporteRescateProgramado;
import cl.bcs.fondomutuo.CrearSolicitudCambioSerie.SalidaCrearSolicitudCambioSerie;
import cl.bcs.fondomutuo.CrearSolicitudCambioSerie.EntradaCrearSolicitudCambioSerie;
import cl.bcs.fondomutuo.CrearSolicitudAporteOvernight.EntradaCrearSolicitudAporteOvernight;
import cl.bcs.fondomutuo.CrearSolicitudAporteOvernight.SalidaCrearSolicitudAporteOvernight;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.EntradaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.SalidaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.EntradaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.SalidaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateTransferencia.EntradaCrearSolicitudRescateTransferencia;
import cl.bcs.fondomutuo.CrearSolicitudRescateTransferencia.SalidaCrearSolicitudRescateTransferencia;
import cl.bcs.fondomutuo.CrearSolicitudRescateTraspaso.EntradaCrearSolicitudRescateTraspaso;
import cl.bcs.fondomutuo.CrearSolicitudRescateTraspaso.SalidaCrearSolicitudRescateTraspaso;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ManejadorSolicitudAporteRescate implements JavaCall{
	private final Log log = new Log("FM.ManejadorSolicitudAporteRescate");
	
	public static class EntradaManejadorSolicitudAporteRescate{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		
		public List<CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate> p_SolicitudesAporteRescateNormal;
		public List<CrearSolicitudAporteRescateProgramado.InSolicitudesAporte> p_SolicitudesAporteRescateProgramado;
		public List<CrearSolicitudAporteOvernight.InSolicitudesAporte> p_SolicitudAporteOvernight;
		public List<CrearSolicitudAporteReinversion.InSolicitudesAporte> p_SolicitudAporteReinversion;
		public List<CrearSolicitudCambioSerie.InSolicitudesRescateCambioSerie> p_SolicitudCambioSerieRescate;
		public List<CrearSolicitudCambioSerie.InSolicitudesAporteCambioSerie> p_SolicitudCambioSerieAporte;
		public List<CrearSolicitudRescateReinversion.InSolicitudesRescate> p_SolicitudRescateReinversionRescate;
		public List<CrearSolicitudRescateReinversion.InSolicitudesAporte> p_SolicitudRescateReinversionAporte;
		public List<CrearSolicitudRescateTransferencia.InSolicitudesTransferencia> p_SolicitudRescateTransferencia;
		public List<CrearSolicitudRescateTraspaso.InSolicitudesTraspaso> p_SolicitudRescateTraspaso;
		
		public EntradaManejadorSolicitudAporteRescate() {
			this.p_SolicitudesAporteRescateNormal = new ArrayList<CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate>();
			this.p_SolicitudesAporteRescateProgramado = new ArrayList<CrearSolicitudAporteRescateProgramado.InSolicitudesAporte>();
			this.p_SolicitudAporteOvernight = new ArrayList<CrearSolicitudAporteOvernight.InSolicitudesAporte>();
			this.p_SolicitudAporteReinversion = new ArrayList<CrearSolicitudAporteReinversion.InSolicitudesAporte>();
			this.p_SolicitudCambioSerieRescate = new ArrayList<CrearSolicitudCambioSerie.InSolicitudesRescateCambioSerie>();
			this.p_SolicitudCambioSerieAporte = new ArrayList<CrearSolicitudCambioSerie.InSolicitudesAporteCambioSerie>();
			this.p_SolicitudRescateReinversionRescate = new ArrayList<CrearSolicitudRescateReinversion.InSolicitudesRescate>();
			this.p_SolicitudRescateReinversionAporte = new ArrayList<CrearSolicitudRescateReinversion.InSolicitudesAporte>();
			this.p_SolicitudRescateTransferencia = new ArrayList<CrearSolicitudRescateTransferencia.InSolicitudesTransferencia>();
			this.p_SolicitudRescateTraspaso = new ArrayList<CrearSolicitudRescateTraspaso.InSolicitudesTraspaso>();
		}
		
		public List<InFormaPagoSpot> p_FormaPagoSpot;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaManejadorSolicitudAporteRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoSolicitud> P_OUT_RESULTADO;

		public SalidaManejadorSolicitudAporteRescate(){
			P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaManejadorSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaManejadorSolicitudAporteRescate.class);
		SalidaManejadorSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//map
	@Trace
	public SalidaManejadorSolicitudAporteRescate logica (Connection con, EntradaManejadorSolicitudAporteRescate entrada) throws Exception{		
		log.write("INICIO ManejadorSolicitudAporteRescate");
		SalidaManejadorSolicitudAporteRescate out = new SalidaManejadorSolicitudAporteRescate();
		try {
			if(entrada.p_SolicitudesAporteRescateNormal.size() > 0){
				CrearSolicitudAporteRescateNormal servicio = new CrearSolicitudAporteRescateNormal();
				EntradaCrearSolicitudAporteRescateNormal in = new EntradaCrearSolicitudAporteRescateNormal();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesAporteRescate = entrada.p_SolicitudesAporteRescateNormal;
				in.p_FormaPagoSpot = entrada.p_FormaPagoSpot;
				SalidaCrearSolicitudAporteRescateNormal salida = servicio.logica(con, in);
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate> entradaBuenos = in.p_SolicitudesAporteRescate.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				List<CrearSolicitudAporteRescateNormal.InFormaPagoSpot> entradaFP = in.p_FormaPagoSpot.parallelStream().filter(formapago->resultado.get(formapago.IdOperacion) != null).collect(Collectors.toList());
				if(entradaBuenos.size() > 0){
					in.p_SolicitudesAporteRescate = entradaBuenos;
					in.p_FormaPagoSpot = entradaFP;
					in.p_SimularOperacion = "N";
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			
			if(entrada.p_SolicitudesAporteRescateProgramado.size() > 0){
				CrearSolicitudAporteRescateProgramado servicio = new CrearSolicitudAporteRescateProgramado();
				EntradaCrearSolicitudAporteRescateProgramado in = new EntradaCrearSolicitudAporteRescateProgramado();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesAporte = entrada.p_SolicitudesAporteRescateProgramado;
				SalidaCrearSolicitudAporteRescateProgramado salida = servicio.logica(con, in);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudAporteRescateProgramado.InSolicitudesAporte> entradaBuenos = in.p_SolicitudesAporte.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				if(entradaBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesAporte = entradaBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;					
				}				
			}
			
			if(entrada.p_SolicitudAporteOvernight.size()>0){
				CrearSolicitudAporteOvernight servicio = new CrearSolicitudAporteOvernight();
				EntradaCrearSolicitudAporteOvernight in = new EntradaCrearSolicitudAporteOvernight();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesAporte = entrada.p_SolicitudAporteOvernight;
				SalidaCrearSolicitudAporteOvernight salida = servicio.logica(con, in);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudAporteOvernight.InSolicitudesAporte> entradaBuenos = in.p_SolicitudesAporte.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				if(entradaBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesAporte = entradaBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			 
			if(entrada.p_SolicitudAporteReinversion.size()>0){
				CrearSolicitudAporteReinversion servicio = new CrearSolicitudAporteReinversion();
				EntradaCrearSolicitudAporteReinversion in =  new EntradaCrearSolicitudAporteReinversion();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesAporte = entrada.p_SolicitudAporteReinversion;
				SalidaCrearSolicitudAporteReinversion salida = servicio.logica(con, in);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				List<CrearSolicitudAporteReinversion.InSolicitudesAporte> entradaBuenos = in.p_SolicitudesAporte.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				if(entradaBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesAporte = entradaBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}				
			}
				
			if(entrada.p_SolicitudCambioSerieRescate.size() > 0 || entrada.p_SolicitudCambioSerieAporte.size()>0){
				CrearSolicitudCambioSerie servicio =  new CrearSolicitudCambioSerie();
				EntradaCrearSolicitudCambioSerie in = new EntradaCrearSolicitudCambioSerie();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesRescate = entrada.p_SolicitudCambioSerieRescate;
				in.p_SolicitudesAporte = entrada.p_SolicitudCambioSerieAporte;
				SalidaCrearSolicitudCambioSerie salida = servicio.logica(con, in);
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudCambioSerie.InSolicitudesAporteCambioSerie> entradaAportesBuenos = in.p_SolicitudesAporte.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				List<CrearSolicitudCambioSerie.InSolicitudesRescateCambioSerie> entradaRescatesBuenos = in.p_SolicitudesRescate.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				if(entradaAportesBuenos.size() > 0 && entradaRescatesBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesRescate = entradaRescatesBuenos;
					in.p_SolicitudesAporte = entradaAportesBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			
			if(entrada.p_SolicitudRescateReinversionRescate.size() > 0 || entrada.p_SolicitudRescateReinversionAporte.size()>0){
				CrearSolicitudRescateReinversion servicio =  new CrearSolicitudRescateReinversion();
				EntradaCrearSolicitudRescateReinversion in = new EntradaCrearSolicitudRescateReinversion();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesRescate = entrada.p_SolicitudRescateReinversionRescate;
				in.p_SolicitudesAporte = entrada.p_SolicitudRescateReinversionAporte;
				SalidaCrearSolicitudRescateReinversion salida = servicio.logica(con, in);
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudRescateReinversion.InSolicitudesAporte> entradaAportesBuenos = in.p_SolicitudesAporte.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				List<CrearSolicitudRescateReinversion.InSolicitudesRescate> entradaRescatesBuenos = in.p_SolicitudesRescate.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				if(entradaAportesBuenos.size() > 0 && entradaRescatesBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesRescate = entradaRescatesBuenos;
					in.p_SolicitudesAporte = entradaAportesBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			
			if(entrada.p_SolicitudRescateTransferencia.size() > 0){
				CrearSolicitudRescateTransferencia servicio =  new CrearSolicitudRescateTransferencia();
				EntradaCrearSolicitudRescateTransferencia in = new EntradaCrearSolicitudRescateTransferencia();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesRescate = entrada.p_SolicitudRescateTransferencia;
				SalidaCrearSolicitudRescateTransferencia salida = servicio.logica(con, in);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudRescateTransferencia.InSolicitudesTransferencia> entradaBuenos = in.p_SolicitudesRescate.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				if(entradaBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesRescate = entradaBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			
			if(entrada.p_SolicitudRescateTraspaso.size() > 0){
				CrearSolicitudRescateTraspaso servicio = new CrearSolicitudRescateTraspaso();
				EntradaCrearSolicitudRescateTraspaso in = new EntradaCrearSolicitudRescateTraspaso();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_SimularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
				in.p_SolicitudesRescate = entrada.p_SolicitudRescateTraspaso;
				SalidaCrearSolicitudRescateTraspaso salida = servicio.logica(con, in);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearSolicitudRescateTraspaso.InSolicitudesTraspaso> entradaBuenos = in.p_SolicitudesRescate.parallelStream().filter(solicitud->resultado.get(solicitud.IdInterno) != null).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				if (entradaBuenos.size() > 0){
					in.p_SimularOperacion = "N";
					in.p_SolicitudesRescate = entradaBuenos;
					salida = servicio.logica(con, in);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				}
			}
			
			return out;	
		
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}	
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"AGE\",\"p_SolicitudesAporteRescateNormal\":[],\"p_SolicitudesAporteRescateProgramado\":[],\"p_SolicitudAporteOvernight\":[],\"p_SolicitudAporteReinversion\":[],\"p_SolicitudCambioSerieRescate\":[],\"p_SolicitudCambioSerieAporte\":[],\"p_SolicitudRescateReinversionRescate\":[],\"p_SolicitudRescateReinversionAporte\":[],\"p_SolicitudRescateTransferencia\":[],\"p_SolicitudRescateTraspaso\":[]}";	
			EntradaManejadorSolicitudAporteRescate  Objin = gson.fromJson(json, EntradaManejadorSolicitudAporteRescate.class);
			ManejadorSolicitudAporteRescate  demo = new ManejadorSolicitudAporteRescate();
			try {
				SalidaManejadorSolicitudAporteRescate ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
	}
}

