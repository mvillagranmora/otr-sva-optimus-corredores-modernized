package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMovimientosEnTransitoTotalizador implements JavaCall {
		
	private static final Logger logger = LogManager.getLogger(ConsultarMovimientosEnTransitoTotalizador.class);

	private final String SQL_CONSULTAR_REGISTROS = loadResourceToString("cl/bcs/custodia/sql/ConsultarMovimientosEnTransitoTotalizador.sql");

	public static class EntradaMovEnTransitoTotalizador extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaTermino;
		public List<FiltrosObjeto> Filtros;
		
	}
	
	public static class FiltrosObjeto extends BcsPojo{
		public String Campo;
		public String Operador;
		public String Valor;
		public String Tipo;
	}
	
	public static class Registros extends BcsPojo{
		public BigDecimal CantidadAgrupada;
		public BigDecimal MontoAgrupadoMonedaInstrumento;
		
		public Registros() {
		    this.CantidadAgrupada = BigDecimal.ZERO;
		    this.MontoAgrupadoMonedaInstrumento = BigDecimal.ZERO;
		    
		}

	}
	
	


	public static class SalidaMovEnTransitoTotalizador extends BcsPojo {
		public List<Registros> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaMovEnTransitoTotalizador() {
			this.P_OUT_TABLA = new ArrayList<Registros>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}
	
	public String construirSqlTotalizador(EntradaMovEnTransitoTotalizador entrada , String in) {
	    String consulta = in;

	    List<String> condiciones = new ArrayList<>();

	    if (entrada.Filtros != null) {
	        for (FiltrosObjeto obj : entrada.Filtros) {
	        	
	            String campo = obj.Campo;
	            String operador = obj.Operador;
	            String valor = obj.Valor;
	            
	            String condicion = "";

	            if ("string".equals(obj.Tipo)) {
	            	switch (operador) {
                    case "lk": 
                        condicion = String.format("\"%s\" LIKE '%%%s%%'", campo, valor);
                        break;
                    case "nlk":
                        condicion = String.format("\"%s\" NOT LIKE '%%%s%%'", campo, valor);
                        break;
                    case "wt": 
                        condicion = String.format("\"%s\" LIKE '%s%%'", campo, valor);
                        break;
                    case "inl": 
                        condicion = String.format("\"%s\" IS NULL", campo);
                        break;
                    case "innl": 
                        condicion = String.format("\"%s\" IS NOT NULL", campo);
                        break;    
                    default: 
                        condicion = String.format("\"%s\" %s '%s'", campo, operador, valor);
                        break;
                }
	                
	            } else {
	                condicion = String.format("\"%s\" %s %s", campo, operador, valor);
	            }

	            condiciones.add(condicion);
	        }
	    }

	    String whereConsulta = condiciones.isEmpty() ? "" : "AND " + String.join(" AND ", condiciones);

	    return consulta.replace("/* AND */", whereConsulta);
	}


	public SalidaMovEnTransitoTotalizador logica(Connection conn, EntradaMovEnTransitoTotalizador in) throws SQLException, Exception {
		logger.info("INICIO::ConsultarMovimientosEnTransitoTotalizador");
		long inicio = System.currentTimeMillis();

		SQL sql=new SQL();
		SalidaMovEnTransitoTotalizador Salida = new SalidaMovEnTransitoTotalizador();
		List<Registros> resultadoRegistros = new ArrayList<Registros>();
		
		String Consulta = construirSqlTotalizador(in, SQL_CONSULTAR_REGISTROS);

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		
		logger.info("INFO::ConsultarMovimientosEnTransitoTotalizador Consulta registros en la BD");
		try {
			resultadoRegistros = sql.query(conn,Consulta,parametros, Registros.class);
			
			if (!resultadoRegistros.isEmpty()) {
				Salida.P_OUT_TABLA = resultadoRegistros;
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_INFORMACION, "UI_OPERACION_EXITOSA", new HashMap<>(), conn, in.p_IdEmpresa));
			}
				
		} catch (Exception e) {
			Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_OPERACION_CON_ERRORES", new HashMap<>(), conn, in.p_IdEmpresa));
		}
		
		Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarMovimientosEnTransitoTotalizador.", fin - inicio);
		
		return Salida;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaMovEnTransitoTotalizador jin=gson.fromJson(in.toString() , EntradaMovEnTransitoTotalizador.class);
		SalidaMovEnTransitoTotalizador out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json3 = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"CUS\",\r\n"
					+ "    \"Filtros\": [\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"PermiteCertificacionPorUsuario\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"S\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdConcepto\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"MOVREG\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IndicadorIngEgr\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"INGRESO\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdClasificacionTipoInstrumento\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"RV\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdTipoInstrumento\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"ACN\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdInstrumento\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"252930\",\r\n"
					+ "            \"Tipo\": \"number\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdCliente\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"2490\",\r\n"
					+ "            \"Tipo\": \"number\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdPortafolio\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"2119\",\r\n"
					+ "            \"Tipo\": \"number\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"IdBoveda\",\r\n"
					+ "            \"Operador\": \"=\",\r\n"
					+ "            \"Valor\": \"DCV\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"FechaOperacion\",\r\n"
					+ "            \"Operador\": \">=\",\r\n"
					+ "            \"Valor\": \"2025-06-02\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Campo\": \"FechaOperacion\",\r\n"
					+ "            \"Operador\": \"<=\",\r\n"
					+ "            \"Valor\": \"2025-06-25\",\r\n"
					+ "            \"Tipo\": \"string\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
							
			EntradaMovEnTransitoTotalizador Objin = gson.fromJson(json3, EntradaMovEnTransitoTotalizador.class);
			ConsultarMovimientosEnTransitoTotalizador demo = new ConsultarMovimientosEnTransitoTotalizador();
			try {
				SalidaMovEnTransitoTotalizador salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
				//con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}