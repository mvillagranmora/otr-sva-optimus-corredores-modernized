package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarPreComprobante implements JavaCall {
	
	private final Logger log = LogManager.getLogger(EnviarPreComprobante.class);
	
	public String SQL_SELECT_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/EnviarPreComprobante_SelectPreComprobantes.sql");
	public String SQL_UPDATE_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/EnviarPreComprobante_UpdatePreComprobantes.sql");
	private final HashMap<String,Object> ESTADOS = new HashMap<>();
	
	public static class EnviarPreComprobante_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SecCmbte;
		public String p_Destino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EnviarPreComprobante_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public EnviarPreComprobante_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobante {
		public Long IdPrograma;
		public Long IdPreComprobante;
		public String Estado;
		public String NombreEstado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EnviarPreComprobante_Entrada in=gson.fromJson(jin.toString() ,EnviarPreComprobante_Entrada.class);
		EnviarPreComprobante_Salida out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}


	private EnviarPreComprobante_Salida logica(Connection con, EnviarPreComprobante_Entrada in) throws Exception {
		log.info("INICIO::EnviarPreComprobante");
		EnviarPreComprobante_Salida salida = new EnviarPreComprobante_Salida();
		SQL _sql = new SQL();
		
		//verificar que los precomprobantes no esten en estado procesado.
		List<Long> secuencias =  Arrays.asList(in.p_SecCmbte.split(";")).stream().filter(sec -> NumberUtils.isNumber(sec)).map(sec -> Long.parseLong(sec)).collect(Collectors.toList());
		if(secuencias.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_PRECOMPROBANTE_VACIO", new HashMap<>(), con, in.p_IdEmpresa));
			return salida;
		}
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdPreComprobantes", secuencias);
		List<PreComprobante> precomprobantes = _sql.query(con, SQL_SELECT_PRECOMPROBANTE, params, PreComprobante.class);
		
		ESTADOS.put("VIGENTE", "V");
		ESTADOS.put("DESCUADRADO", "D");
		ESTADOS.put("ERROR", "E");
		
		
		
		//obtener precomprobantes vigentes y/o descuadrados para el envio.
		AtomicLong cont = new AtomicLong(0);
		List<PreComprobante> preComprobantesValidos =  precomprobantes.stream().filter(comp -> {
			if(ESTADOS.containsValue(comp.Estado)) {
				return true;
			}else {
				
				try {
					HashMap<String, String> precomp = new HashMap<String, String>();
					String IdComprobante = String.valueOf(comp.IdPreComprobante);
					precomp.put("IdComprobante", IdComprobante);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(cont.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "ERROR_PROCESO_PRECOMPROBANTE", precomp, con, in.p_IdEmpresa ));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		}).collect(Collectors.toList());
		
		boolean valDescuadrados = preComprobantesValidos.stream().anyMatch(comprobantes -> comprobantes.Estado.equals("D"));

		//validar que no existan precomprobantes con estado distinto a vigente o descuadrado.
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if(!salida.P_OUT_MENSAJES.isEmpty()) {
			log.debug("Se estan enviando a procesar precomprobantes en estados distintos a vigente o descuadrado.");
			return salida;
		}
		
		//Contruir el proceso batch para enviar a SAP.
		long idInterno = 1L;
		long segundosDesfase = 1L;
		InscribirProcesoBatch agregarProgramasMasivo = new InscribirProcesoBatch();
		InscribirProcesoBatch.InscribirProcesoBatchEntrada entradaAgregarPrograma = new InscribirProcesoBatch.InscribirProcesoBatchEntrada(); 
		entradaAgregarPrograma.p_IdEmpresa = in.p_IdEmpresa;
		entradaAgregarPrograma.p_IdModulo = in.p_IdModulo;
		entradaAgregarPrograma.p_IdUsuario = in.p_IdUsuario;
		entradaAgregarPrograma.p_TipoPrograma = Arrays.asList(new InscribirProcesoBatch.InTipoPrograma(idInterno, "ASIENTO_SAP", segundosDesfase, ""));
		entradaAgregarPrograma.p_Parametro = new ArrayList<>();
		entradaAgregarPrograma.p_Parametro.add(new InscribirProcesoBatch.Parametro(idInterno, "IdEmpresa", String.valueOf(in.p_IdEmpresa), null));
		entradaAgregarPrograma.p_Parametro.add(new InscribirProcesoBatch.Parametro(idInterno, "IdUsuario", String.valueOf(in.p_IdUsuario), null));
		entradaAgregarPrograma.p_Parametro.add(new InscribirProcesoBatch.Parametro(idInterno, "Destino", String.valueOf(in.p_Destino), null));
		//codificar comprobantes en base64, para poder almacenarlos en el campo blob de la tabla parametros.
		String secuenciasValidas = StringUtils.join(preComprobantesValidos.stream().map(comp -> comp.IdPreComprobante).collect(Collectors.toList()),";");
		String comprobantesB64 =  Base64.getEncoder().encodeToString(secuenciasValidas.getBytes());
		entradaAgregarPrograma.p_Parametro.add(new InscribirProcesoBatch.Parametro(idInterno, "SecCmbte", null, comprobantesB64));

		//Agregar programa batch
		InscribirProcesoBatchSalida salidaAgregarPrograma = agregarProgramasMasivo.logica(con, entradaAgregarPrograma);
		if(salidaAgregarPrograma.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR = salidaAgregarPrograma.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES = salidaAgregarPrograma.P_OUT_MENSAJES;
		}else {
			Long idPrograma = salidaAgregarPrograma.P_OUT_PROGRAMA.stream().map(programa -> programa.IdPrograma).findFirst().get();
			preComprobantesValidos.forEach(comp -> {
				comp.IdPrograma = idPrograma;
			});
			
			if (!valDescuadrados) {
				_sql.batch(con, SQL_UPDATE_PRECOMPROBANTE, preComprobantesValidos, PreComprobante.class);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"PRECOMPROBANTES_EN_ENVIO_SAP", new HashMap<>(), con, in.p_IdEmpresa));
			}
			else {
				_sql.batch(con, SQL_UPDATE_PRECOMPROBANTE, preComprobantesValidos, PreComprobante.class);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"PRECOMPROBANTES_EN_ENVIO_SAP_DESC", new HashMap<>(), con, in.p_IdEmpresa));
			}
		}
		
		log.info("FIN::EnviarPreComprobante");
		return salida;
		
	}

	public static void main (String [] args) throws Exception {
		try(Connection conn = PlataformaUtils.getPropertiesConnection("presura")){
			String jsonUno = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CON\",\"p_UsaReglasNegocio\":\"N\",\"p_SecCmbte\":\"\",\"p_Destino\":\"B\"}";
			String jsonMulti = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CON\",\"p_UsaReglasNegocio\":\"N\",\"p_SecCmbte\":\"1132;1133;\",\"p_Destino\":\"B\"}";
	  		String jsonTodos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CON\",\"p_UsaReglasNegocio\":\"N\",\"p_SecCmbte\":\"1251;1409;1410;1411;1255;1256;1285;1258;1259;1407;1261;1262;1263;1264;1287;1408;1286;1268;1269;1270;1271;1272;1273;1274;1275;1276;1277;1278;1279;1280;\",\"p_Destino\":\"B\"}";
	  		JsonParser parser = new JsonParser();
	  		JsonObject jin = parser.parse(jsonUno).getAsJsonObject();
	  		EnviarPreComprobante p = new EnviarPreComprobante();
	  		System.out.println(p.call(conn, jin));
	  		conn.rollback();
		}
	}

	
}
