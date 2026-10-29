package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate;
import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.EntradaAnulacionSolicitudAporteRescate;
import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.ResultadoAnulacion;
import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.SalidaAnulacionSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RevalorizarMovimientosFFMM implements JavaCall {
	private final Log log = new Log("RevalorizarMovimientosFFMM");
	private final String SQL_SelectAportesCertificados = loadResourceToString("cl/bcs/plataforma/pau/sql/RevalorizarMovimientosFFMM_SEL_AportesCertificados.sql");

	public static class RevalorizarMovimientosFFMMEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaSistema;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RevalorizarMovimientosFFMMSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;

		public RevalorizarMovimientosFFMMSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjAportesCertificados {
		public Long IdSolicitudAporteRescate;
		public String ResultadoValorCuota;
		public BigDecimal ValorCuota;
		public Date FechaCierre;
		public String IdSerieFondo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public RevalorizarMovimientosFFMMSalida logica(Connection conn, RevalorizarMovimientosFFMMEntrada entrada) throws Exception {
		log.write("INICIO::RevalorizarMovimientosFFMM");
		RevalorizarMovimientosFFMMSalida out = new RevalorizarMovimientosFFMMSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		
		List<ObjAportesCertificados> aportesCertificados = new ArrayList<ObjAportesCertificados>();
		HashMap<String, Object> paramsMovimientos = new HashMap<>();
		paramsMovimientos.put("IdEmpresa", entrada.p_IdEmpresa);
		paramsMovimientos.put("FechaCertificacion", entrada.p_FechaSistema);
		
		Date fechaSistema = Fecha.toDate(entrada.p_FechaSistema);
		
		aportesCertificados = sql.query(conn, SQL_SelectAportesCertificados, paramsMovimientos, ObjAportesCertificados.class);
		AnulacionSolicitudAporteRescate.InSolicitudes solicitudAnulacion;
		
		if (aportesCertificados.size() == 0) {
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_HAN_ENCONTRADO_APORTES_CERTIFICADOS",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
			return out;
		}
		
		List<String> series = new ArrayList<String>();
		// desmarcar
		EntradaAnulacionSolicitudAporteRescate entradaAnulacion = new EntradaAnulacionSolicitudAporteRescate();
		entradaAnulacion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnulacion.p_IdUsuario = entrada.p_IdUsuario;
		entradaAnulacion.p_IdModulo = entrada.p_IdModulo;
		for (ObjAportesCertificados aporte : aportesCertificados) {
			 if("OK".equals(aporte.ResultadoValorCuota)) {
				if((fechaSistema.compareTo(aporte.FechaCierre) == 1 || aporte.ValorCuota.compareTo(BigDecimal.ZERO) == 0) && !series.contains(aporte.IdSerieFondo)) {
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> params = new HashMap<String,String>();
					params.put("IdSerieFondo", aporte.IdSerieFondo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_VALOR_CUOTA_SERIE",params,conn,entrada.p_IdEmpresa));
					series.add(aporte.IdSerieFondo);
				}
				solicitudAnulacion = new AnulacionSolicitudAporteRescate.InSolicitudes();
				solicitudAnulacion.IdSolicitudAporteRescate = aporte.IdSolicitudAporteRescate;
				solicitudAnulacion.Estado = "ING";
				entradaAnulacion.p_Solicitudes.add(solicitudAnulacion);
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR.longValue() > 0) {
			return out;
		}
		else if(entradaAnulacion.p_Solicitudes.size() == 0) {
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_VARIACION_VALOR_CUOTA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
			return out;
		}

		AnulacionSolicitudAporteRescate anulacionSolicitudAporteRescate = new AnulacionSolicitudAporteRescate();
		
		SalidaAnulacionSolicitudAporteRescate salidaAnulacion = anulacionSolicitudAporteRescate.logica(conn, entradaAnulacion);
		if (salidaAnulacion.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaAnulacion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES = salidaAnulacion.P_OUT_MENSAJES;
			return out;
		}
		if (salidaAnulacion.P_SOLICITUDES.size() > 0) {
			List<CertificarSolicitudAporteRescate.InSolicitudes> solicitudesCertificar = new ArrayList<CertificarSolicitudAporteRescate.InSolicitudes>();
			CertificarSolicitudAporteRescate.InSolicitudes solicitudCertificar;
			for (ResultadoAnulacion resultado : salidaAnulacion.P_SOLICITUDES) {
				if ("UI_EXITO".equalsIgnoreCase(resultado.Codigo)) {
					solicitudCertificar = new CertificarSolicitudAporteRescate.InSolicitudes();
					solicitudCertificar.IdSolicitudAporteRescate = resultado.IdSolicitudAporteRescate;
					solicitudesCertificar.add(solicitudCertificar);
				}
			}
			// certificar
			CertificarSolicitudAporteRescate certificarAporteRescate = new CertificarSolicitudAporteRescate();
			EntradaCertificarSolicitudAporteRescate entradaCertificar = new EntradaCertificarSolicitudAporteRescate();
			entradaCertificar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCertificar.p_IdUsuario = entrada.p_IdUsuario;
			entradaCertificar.p_IdModulo = entrada.p_IdModulo;
			entradaCertificar.p_SimularOperacion = "N";
			entradaCertificar.p_UsaReglasNegocio = "S";
			entradaCertificar.p_FechaCertificacion = entrada.p_FechaSistema;
			entradaCertificar.p_Solicitudes = solicitudesCertificar;
			
			SalidaCertificarSolicitudAporteRescate salidaCertificar = certificarAporteRescate.logica(conn, entradaCertificar);
			out.P_OUT_ERRORESCONTADOR = salidaCertificar.P_OUT_ERRORESCONTADOR;
			if (salidaCertificar.P_OUT_MENSAJES.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"PROCESO_REVALORIZACION_EJECUTADO_CORRECTAMENTE",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
			} else {
				out.P_OUT_MENSAJES.addAll(salidaCertificar.P_OUT_MENSAJES);
			}
		}
		
		log.write("FIN::RevalorizarMovimientosFFMM");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		RevalorizarMovimientosFFMMEntrada in = gson.fromJson(jin.toString(), RevalorizarMovimientosFFMMEntrada.class);
		RevalorizarMovimientosFFMMSalida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		try {
			Connection conn = PlataformaUtils.getDESAConnection();
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaSistema\":\"2018-10-12\"}";
			RevalorizarMovimientosFFMMEntrada entrada = gson.fromJson(json, RevalorizarMovimientosFFMMEntrada.class);
			RevalorizarMovimientosFFMM revalorizar = new RevalorizarMovimientosFFMM();
			RevalorizarMovimientosFFMMSalida salida = revalorizar.logica(conn, entrada);
			System.out.println(salida);
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
