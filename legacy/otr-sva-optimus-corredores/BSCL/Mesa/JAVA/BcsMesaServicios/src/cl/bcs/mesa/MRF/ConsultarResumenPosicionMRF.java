package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.mesa.MRF.ConsultarDetallePosicionMRF.Operacion;
import cl.bcs.mesa.MRF.ConsultarPosicionMRF.*;
import cl.bcs.mesa.MRF.pojo.CacheMRF;

public class ConsultarResumenPosicionMRF implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConsultarResumenPosicionMRF.class);

	private final String SELECT_METODOS_POSICION_MRF = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectMetodosPosicionMRF.sql");
	private final String SELECT_POSICION_MRF = loadResourceToString(
			"cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRF.sql");

	private SQL _sql = new SQL();
	
	private static final CacheServicio CACHE = CacheServicio.registrar(CacheMRF.RESUMEN_POSICION, CacheMRF.TTL_POSICION_MS, CacheMRF.TAG_POSICION);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarResumenPosicionMRF_Entrada jin = gson.fromJson(in.toString(), ConsultarResumenPosicionMRF_Entrada.class);
		String clave = CacheServicio.clave(jin.p_IdEmpresa, jin.p_IdPais, jin.p_IdMesa, jin.p_IdTipoPosicion, jin.p_FechaPosicion);
		String json = CACHE.obtener(clave, () ->{
			ConsultarResumenPosicionMRF_Salida out = logica(conn, jin);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return gson.toJson(out);
		});
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(json).getAsJsonObject();
		return dataout;
	}

	public static class ConsultarResumenPosicionMRF_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdPais;
		public Long p_IdMesa;
		public String p_IdTipoPosicion = "";
		public String p_FechaPosicion;

		public ConsultarResumenPosicionMRF_Entrada() {
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdPais = 0L;
			p_IdMesa = 0L;
			p_IdTipoPosicion = "";
			p_FechaPosicion = "";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultarResumenPosicionMRF_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDMESA;
		public String P_OUT_NOMBREMESA;
		public List<ResumenPosicionMRF_Salida> P_OUT_RESUMENPOSICION;
		public List<PosicionMRF> P_OUT_EVENTOS;

		public ConsultarResumenPosicionMRF_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_IDMESA = 0L;
			P_OUT_NOMBREMESA = "";
			P_OUT_RESUMENPOSICION = new ArrayList<>();
			P_OUT_EVENTOS = new ArrayList<>();

		}

		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class MetodoPosicionMRF_Salida {
		public Long IdMetodo;
		public String NombreMetodo;
		public String Glosa;
		public String IdTipoReajuste;
		public Long IdMesa;
		public String IdEstado;
		public String IdTipoPosicion;
	}

	public static class ResumenPosicionMRF_Salida {
		public String IdTipoReajuste;
		public BigDecimal PosicionInicial;
		public BigDecimal PosicionFinal;

		public ResumenPosicionMRF_Salida() {
			IdTipoReajuste = "";
			PosicionInicial = new BigDecimal(0);
			PosicionFinal = new BigDecimal(0);
		}
	}



	public ConsultarResumenPosicionMRF_Salida logica(Connection conn, ConsultarResumenPosicionMRF_Entrada entrada)
			throws Exception {
		logger.info("INICIO::MRF.ConsultarPosicionMRF");

		ConsultarPosicionMRF_Entrada entradaCalculo = new ConsultarPosicionMRF_Entrada();		
		entradaCalculo.p_FechaPosicion = entrada.p_FechaPosicion;

		ConsultarResumenPosicionMRF_Salida salida = new ConsultarResumenPosicionMRF_Salida();
		ConsultarPosicionMRF conPosicion = new ConsultarPosicionMRF();

		HashMap<String, Object> filtrosConsulta = new HashMap<>();
		filtrosConsulta.put("p_IdMesa", entrada.p_IdMesa);
		List<MetodoPosicionMRF_Salida> metodosPosicionMRF = _sql.query(conn, SELECT_METODOS_POSICION_MRF,
				filtrosConsulta, MetodoPosicionMRF_Salida.class);

		if (entrada.p_IdTipoPosicion.equals("INICIAL")) {
			metodosPosicionMRF = metodosPosicionMRF.stream().filter(a -> a.IdTipoPosicion.equals("INICIAL"))
					.collect(Collectors.toList());
		}

		salida.P_OUT_IDMESA = entrada.p_IdMesa;

		List<ResumenPosicionMRF_Salida> metodoSalidaList = new ArrayList<ResumenPosicionMRF_Salida>();

		for (MetodoPosicionMRF_Salida metodo : metodosPosicionMRF) {

			ResumenPosicionMRF_Salida metodoSalida = new ResumenPosicionMRF_Salida();
			metodoSalida.IdTipoReajuste = metodo.IdTipoReajuste;

			filtrosConsulta = new HashMap<>();
			filtrosConsulta.put("p_IdMetodo", metodo.IdMetodo);
			logger.debug("Inicio SELECT_POSICION_MRF");
			List<PosicionMRF> posicionMRF = _sql.query(conn, SELECT_POSICION_MRF, filtrosConsulta, PosicionMRF.class);
			logger.debug("Fin SELECT_POSICION_MRF");

			if (posicionMRF.size() == 0) {
				metodoSalida.PosicionInicial = new BigDecimal(0);
				metodoSalida.PosicionFinal = new BigDecimal(0);
				metodoSalidaList.add(metodoSalida);
				continue;
			}

			conPosicion.calculaPosicion(conn, entradaCalculo, posicionMRF);

			for (PosicionMRF pos : posicionMRF) {
				if (pos.MtoEvento.compareTo(BigDecimal.ZERO) == 0) {
					continue;
				} else {
					salida.P_OUT_EVENTOS.add(pos);
				}
			}

			logger.debug("Obtiene Posicion");
			// P_OUT_POSICIONFINAL
			BigDecimal SUM_MtoSuma = posicionMRF.stream().map(a -> a.MtoSuma).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal SUM_MtoResta = posicionMRF.stream().map(a -> a.MtoResta).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			if (metodo.IdTipoPosicion.equals("INICIAL")) {
				metodoSalida.PosicionInicial = SUM_MtoSuma.subtract(SUM_MtoResta);
				metodoSalida.PosicionFinal = new BigDecimal(0);
			} else {
				metodoSalida.PosicionInicial = new BigDecimal(0);
				metodoSalida.PosicionFinal = SUM_MtoSuma.subtract(SUM_MtoResta);
			}

			metodoSalidaList.add(metodoSalida);

			logger.info("FIN::MRF.ConsultarPosicionMRF");
		}

		// CONSTRUYE SALIDA
		salida.P_OUT_ERRORESCONTADOR = 0L;
		// salida.P_OUT_MENSAJES.add(new
		// MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_POSICIONES_OK",null,conn,entrada.p_IdEmpresa));
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CONSULTA_RESUMEN_POSICIONES_OK",
				"CONSULTA_RESUMEN_POSICIONES_OK"));

		List<ResumenPosicionMRF_Salida> resumenPosicionMRF = new ArrayList<ResumenPosicionMRF_Salida>();

		metodoSalidaList.stream()
				.collect(Collectors.groupingBy(a -> a.IdTipoReajuste,
						Collectors.mapping(a -> a.PosicionInicial,
								Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
				.forEach((tipoReajuste, sumMontPosInicial) -> {
					ResumenPosicionMRF_Salida resumenPosicion = new ResumenPosicionMRF_Salida();
					resumenPosicion.IdTipoReajuste = tipoReajuste;
					resumenPosicion.PosicionInicial = sumMontPosInicial;
					resumenPosicion.PosicionFinal = new BigDecimal(0L);
					resumenPosicionMRF.add(resumenPosicion);
				});

		metodoSalidaList.stream()
				.collect(Collectors.groupingBy(a -> a.IdTipoReajuste,
						Collectors.mapping(a -> a.PosicionFinal,
								Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
				.forEach((tipoReajuste, sumMontPosFinal) -> {
					resumenPosicionMRF.stream().filter(a -> a.IdTipoReajuste.equals(tipoReajuste)).forEach((pos) -> {
						pos.PosicionFinal = sumMontPosFinal;
					});
				});

		salida.P_OUT_RESUMENPOSICION = resumenPosicionMRF;

		return salida;

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdPais\":44,\"p_IdMesa\":24}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdPais\":44,\"p_IdMesa\":39,\"p_IdTipoPosicion\":\"INICIAL\"}";
			//
			ConsultarResumenPosicionMRF_Entrada Objin = gson.fromJson(json, ConsultarResumenPosicionMRF_Entrada.class);
			ConsultarResumenPosicionMRF demo = new ConsultarResumenPosicionMRF();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				ConsultarResumenPosicionMRF_Salida ObjOut = demo.logica(con, Objin);
				// con.rollback();
				con.commit();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				System.out.println(er.toString());
				con.rollback();
			}
			// con.commit();

		}

		System.out.println("== END ==");
	}

}