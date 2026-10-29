package cl.bcs.tesoreria.cloper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;
import cl.bcs.tesoreria.cloper.CloperWsBSC.ParametrosConexionCloperWS;

import com.google.gson.JsonElement;

/**
 * ORACLE SHIM of cl.bcs.tesoreria.cloper.Cloper. The data fields are verbatim (Cloper.java:15-42). obtenerDataCloper reproduces the semantics of
 * Cloper.java:59-86 around the HTTP call: null/blank connection parameters -> the exception is swallowed and null is returned (CloperWsBSC.java:53-60
 * throws, Cloper.java:80-82 catches); an empty answer -> null; rows with fecha "NO EXISTE" are dropped (:75). The WS answer (a JSON array of
 * rows) is scripted in case.json colaboradores.cloper; the request body is recorded as collaborator "Cloper".
 */
public class Cloper {

	public Long 				bolsa;
	public String				camara;
	public BigDecimal   		cantidad;
	public Long					cod_corr_com;
	public Long					cod_corr_ven;
	public Long					cod_liq_com;
	public Long					cod_liq_ven;
	public Long					cod_ope_com;
	public Long					cod_ope_ven;
	public Long					dias_ven;
    public String				emisor;
	public String				estado;
	public String				estado_liq_bil;
	public String				fec_liq;
	public String				fecha;
	public String				fecha_pago;
	public String       		folio;
    public String				hora;
	public String				madurez;
	public String				mercado;
	public String				moneda;
	public BigDecimal   		monto;
	public BigDecimal   		monto_div;
    public String				nemo;
	public String				tipo_liq;
	public String				tipo_liquida;
	public String				tipo_opera;

	public Cloper()	{}

	public List<Cloper> obtenerDataCloper(String jsonBody, ParametrosConexionCloperWS p) throws Exception {
		java.util.Map<String, Object> reg = new java.util.LinkedHashMap<>();
		reg.put("body", com.google.gson.JsonParser.parseString(jsonBody));
		reg.put("conexion", new java.util.TreeMap<>(java.util.Map.of("wsdl", String.valueOf(p.wsdl), "profile", String.valueOf(p.profile), "password", p.password == null ? "null" : "<set>")));
		Hooks.registrar("Cloper", reg);
		if (p.wsdl == null || p.profile == null || p.password == null) return null;
		if (p.wsdl.trim().isEmpty() || p.profile.trim().isEmpty() || p.password.trim().isEmpty()) return null;
		com.google.gson.JsonArray respuesta = Hooks.lista("cloper");
		if (respuesta.size() == 0) return null;
		com.google.gson.Gson gson = new com.google.gson.Gson();
		List<Cloper> lista = new ArrayList<>();
		for (JsonElement e : respuesta) {
			Cloper c = gson.fromJson(e, Cloper.class);
			if (!"NO EXISTE".equalsIgnoreCase(c.fecha)) lista.add(c);
		}
		return lista;
	}
}
