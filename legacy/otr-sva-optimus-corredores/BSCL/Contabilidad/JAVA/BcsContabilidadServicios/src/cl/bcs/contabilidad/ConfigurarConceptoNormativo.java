package cl.bcs.contabilidad;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import datadog.trace.api.Trace;

public class ConfigurarConceptoNormativo implements JavaCall {

	private final String SELECT_CONDICIONES_EXISTENTES = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_Select_CondicionConceptoExistentes.sql");
	private final String SELECT_CONCEPTO = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_SelectConceptoNormativo.sql");
	private final String SELECT_TIPO_DATO_EVENTO_VALIDOS = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_SelectTipoDatoEvento.sql");
	private final String SELECT_TIPO_DATO_VALOR_VALIDOS = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_SelectTipoDatoValorValidos.sql");
	
	private final String INSERTAR_CONDICION = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_InsCondicionConcepto.sql");
	private final String MODIFICAR_CONDICION = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_UpdateCondicionConcepto.sql");
	private final String DELETE_CONDICION_CONCEPTO = loadResourceToString("cl/bcs/contabilidad/sql/ConfigurarConceptoNormativo_DeleteCondicionConcepto.sql");

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ConfigurarCondicionConcepto_IN in = gson.fromJson(jin.toString() ,ConfigurarCondicionConcepto_IN.class);
		ConfigurarCondicionConcepto_OUT out = logica(conn,in);
		JsonParser parser 	= new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}

	public static class ConfigurarCondicionConcepto_OUT {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<CondicionConceptoNormativo> P_OUT_INSERTADOS;
		public List<CondicionConceptoNormativo> P_OUT_ACTUALIZADOS;
		public List<Long> P_OUT_ELIMINADOS;
	}


	public static class ConfigurarCondicionConcepto_IN {
		public Long IdConceptoNormativo;
		public Long IdEmpresa;
		public List<CondicionConceptoNormativo> condiciones;
	}

	public static class CondicionConceptoNormativo {
		public Long IdCondicion;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public String OperadorFuncion;
		public Long IdConceptoNormativo;
	}

	public static class TipoDatoEventoValido {
		public String IdTipoDatoEvento;
	}

	public static class EventoTipoDatoValorValido {
		public String IdTipoDatoEvento;
		public String ValorVarchar;
	}

	@Trace
	public ConfigurarCondicionConcepto_OUT logica(Connection conn, ConfigurarCondicionConcepto_IN in) throws Exception {
		SQL sql = new SQL();
		ConfigurarCondicionConcepto_OUT out = new ConfigurarCondicionConcepto_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJES = new ArrayList<>();
		out.P_OUT_INSERTADOS = new ArrayList<>();
		out.P_OUT_ACTUALIZADOS = new ArrayList<>();
		out.P_OUT_ELIMINADOS = new ArrayList<>();

		try {
			HashMap<String, Object> paramConcepto = new HashMap<>(1);
			paramConcepto.put("IdConceptoNormativo", in.IdConceptoNormativo);
			Long existe = sql.countQuery(conn, SELECT_CONCEPTO, paramConcepto);
			if (existe == 0) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONCEPTO_NO_EXISTE", "CONCEPTO_NO_EXISTE"));
				return out;
			}

			List<CondicionConceptoNormativo> condicionesActuales = sql.query(conn, SELECT_CONDICIONES_EXISTENTES, paramConcepto, CondicionConceptoNormativo.class);
			List<Long> idsActuales = condicionesActuales.stream().map(c -> c.IdCondicion).collect(Collectors.toList());

			Set<String> tiposCondiciones = in.condiciones.stream()
				.map(c -> c.IdTipoDatoEvento)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

			HashMap<String, Object> paramTipos = new HashMap<>(1);
			paramTipos.put("tipos", new ArrayList<>(tiposCondiciones));
			List<TipoDatoEventoValido> tiposValidosResult = sql.query(conn, SELECT_TIPO_DATO_EVENTO_VALIDOS, paramTipos, TipoDatoEventoValido.class);
			Set<String> tiposValidos = tiposValidosResult.stream().map(t -> t.IdTipoDatoEvento).collect(Collectors.toSet());

			Set<String> valores = in.condiciones.stream().map(c -> c.ValorVarchar).filter(Objects::nonNull).collect(Collectors.toSet());
			HashMap<String, Object> paramTipoValor = new HashMap<>(2);
			paramTipoValor.put("tipos", new ArrayList<>(tiposCondiciones));
			paramTipoValor.put("valores", new ArrayList<>(valores));
			List<EventoTipoDatoValorValido> valoresValidos = sql.query(conn, SELECT_TIPO_DATO_VALOR_VALIDOS, paramTipoValor, EventoTipoDatoValorValido.class);
			Set<String> combinacionesValidas = valoresValidos.stream()
				.map(v -> v.IdTipoDatoEvento + "_" + v.ValorVarchar)
				.collect(Collectors.toSet());

			List<Long> idsEntrantes = new ArrayList<>();
			List<CondicionConceptoNormativo> condicionesInsertar = new ArrayList<>();
			List<CondicionConceptoNormativo> condicionesActualizar = new ArrayList<>();
			List<CondicionConceptoNormativo> condicionesEliminar = new ArrayList<>();

			for (CondicionConceptoNormativo cond : in.condiciones) {
				String clave = cond.IdTipoDatoEvento + "_" + cond.ValorVarchar;

				if (!tiposValidos.contains(cond.IdTipoDatoEvento)) {
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String, String> diccionarioParms = new HashMap<>(1);
					diccionarioParms.put("IdTipoCondicion", cond.IdTipoDatoEvento);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "TIPO_EVENTO_NO_EXISTE", diccionarioParms, conn, in.IdEmpresa));
					continue;
				}
				if (!combinacionesValidas.contains(clave)) {
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String, String> diccionarioParms = new HashMap<>(2);
					diccionarioParms.put("Valor", cond.ValorVarchar);
					diccionarioParms.put("TipoEvento", cond.IdTipoDatoEvento);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "TIPO_EVENTO_VALOR_INVALIDO", diccionarioParms, conn, in.IdEmpresa));
					continue;
				}

				cond.IdConceptoNormativo = in.IdConceptoNormativo;

				if (cond.IdCondicion == null || !idsActuales.contains(cond.IdCondicion)) {
					condicionesInsertar.add(cond);
				} else {
					condicionesActualizar.add(cond);
					idsEntrantes.add(cond.IdCondicion);
				}
			}

			for (Long idActual : idsActuales) {
				if (!idsEntrantes.contains(idActual)) {
					CondicionConceptoNormativo c = new CondicionConceptoNormativo();
					c.IdCondicion = idActual;
					condicionesEliminar.add(c);
				}
			}
			
			if (out.P_OUT_ERRORESCONTADOR >= 1L) {
				return out;
			}

			if (!condicionesInsertar.isEmpty()) {
				sql.batch(conn, INSERTAR_CONDICION, condicionesInsertar, CondicionConceptoNormativo.class);
				out.P_OUT_INSERTADOS.addAll(condicionesInsertar);
			}
			if (!condicionesActualizar.isEmpty()) {
				sql.batch(conn, MODIFICAR_CONDICION, condicionesActualizar, CondicionConceptoNormativo.class);
				out.P_OUT_ACTUALIZADOS.addAll(condicionesActualizar);
			}
			if (!condicionesEliminar.isEmpty()) {
				sql.batch(conn, DELETE_CONDICION_CONCEPTO, condicionesEliminar, CondicionConceptoNormativo.class);
				out.P_OUT_ELIMINADOS.addAll(condicionesEliminar.stream().map(c -> c.IdCondicion).collect(Collectors.toList()));
			}

		} catch (Exception ex) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", ex.toString()));
		}

		return out;
	}

	public static void main(String[] args) throws Exception, SQLException {
		Connection conn = null;

		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa02", "C:\\Users\\lportales_ext\\Documents\\workspace\\optimus-corredores\\connections.properties");

			ConfigurarCondicionConcepto_IN objIn = new ConfigurarCondicionConcepto_IN();
			objIn.IdConceptoNormativo = 1L;

			CondicionConceptoNormativo c1 = new CondicionConceptoNormativo();
			c1.IdCondicion = null;
			c1.IdTipoDatoEvento = "TipoComprobantes";
			c1.ValorVarchar = "FAE";
			c1.OperadorFuncion = "+";

			CondicionConceptoNormativo c2 = new CondicionConceptoNormativo();
			c2.IdCondicion = null;
			c2.IdTipoDatoEvento = "TipoInstrumeno";
			c2.ValorVarchar = "ACX";
			c2.OperadorFuncion = "-";
			
			CondicionConceptoNormativo c3 = new CondicionConceptoNormativo();
			c3.IdCondicion = null;
			c3.IdTipoDatoEvento = "TipoInstrumento";
			c3.ValorVarchar = "ACff";
			c3.OperadorFuncion = "-";
			
			CondicionConceptoNormativo c4 = new CondicionConceptoNormativo();
			c4.IdCondicion = null;
			c4.IdTipoDatoEvento = "CompraVenta";
			c4.ValorVarchar = "C";
			c4.OperadorFuncion = "+";

			objIn.condiciones = Arrays.asList(c1,c2,c3,c4);

			ConfigurarConceptoNormativo demo = new ConfigurarConceptoNormativo();
			ConfigurarCondicionConcepto_OUT ObjOut = demo.logica(conn, objIn);

			Gson gson = new Gson();
			System.out.println(gson.toJson(ObjOut));

			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.close();
		}
		System.out.println("== END ==");
	}
}
