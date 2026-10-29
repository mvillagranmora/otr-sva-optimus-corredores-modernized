package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConsultarInformacionEnvioFacturacionMasivo.Asignacion_Salida;
import cl.bcs.ordenes.ConsultarInformacionEnvioFacturacionMasivo.ConsultarInformacionEnvioFacturacionMasivo_Entrada;
import cl.bcs.ordenes.ConsultarInformacionEnvioFacturacionMasivo.ConsultarInformacionEnvioFacturacionMasivo_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnvioFacturacionMasivo implements JavaCall {

	private final Log log = new Log("ORD.EnvioFacturacionMasivo");

	public static class EnvioFacturacionMasivo_Entrada {

		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		
		public String 	p_HoraHasta;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;
		public Long 	p_IdLibroOrdenes;
		public String 	p_IdAccionOrden;
		public Long		p_IdInstrumento;
		public String 	p_IdCondicionDeLiquidacion;
		public Long		p_IdAsignacion;
		public String	p_FolioTransaccion;
		public Long		p_IdOrden;
		public String 	p_IdBolsa;
		public Long   	p_IdAsignacionMasiva;
		public String   p_MarcaAsignacion;
		public String   p_IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Resultado_Salida {
		public Long IdAsignacion;
		public Long IdMovimientoComprobante;
		
	}
	
	public static class EnvioFacturacionMasivo_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public EnvioFacturacionMasivo_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_RESULTADO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EnvioFacturacionMasivo_Entrada in = gson.fromJson(jin.toString(), EnvioFacturacionMasivo_Entrada.class);
		EnvioFacturacionMasivo_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public EnvioFacturacionMasivo_Salida logica(Connection con, EnvioFacturacionMasivo_Entrada in) throws Exception {
		EnvioFacturacionMasivo_Salida out = new EnvioFacturacionMasivo_Salida();
		SQL _sql = new SQL();
		
		ConsultarInformacionEnvioFacturacionMasivo_Entrada serviceEntrada1 = new ConsultarInformacionEnvioFacturacionMasivo_Entrada();
		serviceEntrada1.p_IdUsuario = in.p_IdUsuario;
		serviceEntrada1.p_IdModulo = in.p_IdModulo;
		serviceEntrada1.p_HoraHasta = in.p_HoraHasta;
		serviceEntrada1.p_IdAccionOrden = in.p_IdAccionOrden;
		serviceEntrada1.p_IdBolsa = in.p_IdBolsa;
		serviceEntrada1.p_IdClasificacionTipoInstrumento = in.p_IdClasificacionTipoInstrumento;
		serviceEntrada1.p_IdCondicionDeLiquidacion = in.p_IdCondicionDeLiquidacion;
		serviceEntrada1.p_IdEmpresa = in.p_IdEmpresa;
		serviceEntrada1.p_IdLibroOrdenes = in.p_IdLibroOrdenes;
		serviceEntrada1.p_IdTipoInstrumento = in.p_IdTipoInstrumento;
		serviceEntrada1.p_FolioTransaccion = in.p_FolioTransaccion;
		serviceEntrada1.p_IdAsignacion = in.p_IdAsignacion;
		serviceEntrada1.p_IdInstrumento = in.p_IdInstrumento;
		serviceEntrada1.p_IdOrden = in.p_IdOrden;
		serviceEntrada1.p_IdAsignacionMasiva = in.p_IdAsignacionMasiva;
		serviceEntrada1.p_IdTipoCanalIngreso = in.p_IdTipoCanalIngreso;
		serviceEntrada1.p_MarcaAsignacion = in.p_MarcaAsignacion;
		
		ConsultarInformacionEnvioFacturacionMasivo serviceConsultar = new ConsultarInformacionEnvioFacturacionMasivo();
		ConsultarInformacionEnvioFacturacionMasivo_Salida serviceSalida1 = serviceConsultar.logica(con, serviceEntrada1);
		
		if (serviceSalida1.P_OUT_ASIGNACIONES.size() == 0) {
			String codigo = "NO_SE_ENCONTRARON_ASIGNACIONES";
			String desc = Diccionario.getMensaje(con, in.p_IdEmpresa,codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}		
		
		List<cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada> asignaciones = new ArrayList<>();
		
		for (Asignacion_Salida asig : serviceSalida1.P_OUT_ASIGNACIONES) {
			
			cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada arg0 = new cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada();
			arg0.GeneraComprobante = "N";
			arg0.IdAsignacion = asig.IdAsignacion;
			asignaciones.add(arg0);
		}
		
		EnviarAsignacionesFacturar_Entrada enviarEntrada = new EnviarAsignacionesFacturar_Entrada();
		enviarEntrada.p_Asignaciones = asignaciones;
		enviarEntrada.p_IdEmpresa = in.p_IdEmpresa;
		enviarEntrada.p_IdModulo = in.p_IdModulo;
		enviarEntrada.p_IdUsuario = in.p_IdUsuario;
		enviarEntrada.p_simularOperacion = "N";
		
		EnviarAsignacionesFacturar enviar = new EnviarAsignacionesFacturar();
		EnviarAsignacionesFacturar_Salida salida = enviar.logica(con, enviarEntrada);
		
		//RESULTADO
		
		out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
		
		for (Resultado res: salida.P_OUT_RESULTADO) {
			
			Resultado_Salida rr = new Resultado_Salida();
			rr.IdMovimientoComprobante = res.IdMovimientoComprobante;
			rr.IdAsignacion = res.IdAsignacion;
			out.P_OUT_RESULTADO.add(rr);
			
		}
		
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_HoraHasta\":\"19:06:36\"}";
			EnvioFacturacionMasivo_Entrada asdf = gson.fromJson(json, EnvioFacturacionMasivo_Entrada.class);
			EnvioFacturacionMasivo demo=new EnvioFacturacionMasivo();
			System.out.println(demo.logica(con, asdf).toString());
			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
