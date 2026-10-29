package cl.bcs.plataforma.lim;

/*
 * Clase generada automaticamente por Sp2Java
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EvaluacionLimite implements JavaCall {

	/*
	 * El log
	 */
	private final Log log = new Log("PLA.EvaluacionLimite");

	/*
	 * 
	 * Queries
	 * 
	 */
	// private final String SQL_XXX = loadResourceToString(".....");

	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT
	 */
	public static class ServiceEntrada {
		public Long						P_IDEMPRESA;
		public Long						P_IDUSUARIO;
		public String					P_IDMODULO;
		public String					P_USAREGLASNEGOCIO;
		public String					P_SIMULAROPERACION;
		public List<ObjTABLAOPERACION>	P_TABLAOPERACION;
		public String					P_IDTIPOENTIDAD;
		public String					P_GENERAAUTORIZACION;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServiceSalida {
		public List<ObjREGISTROEVALUACIONLIMITE>	P_OUT_REGISTROEVALUACIONLIMITE;
		public List<ObjESTADOOPERACION>				P_OUT_ESTADOOPERACION;
		public List<ObjIDAUTHTABLE>					P_OUT_IDAUTHTABLE;
		public Long									P_OUT_ADVERTENCIASCONTADOR;
		public List<ObjADVERTENCIASDETALLE>			P_OUT_ADVERTENCIASDETALLE;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ObjERRORESDETALLE>				P_OUT_ERRORESDETALLE;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */

	public static class ObjTABLAOPERACION {
		public Long			IdPortafolio;
		public Long			IdCliente;
		public BigDecimal	MontoUtilizado;
		public String		IdMoneda;
		public BigDecimal	TipoCambio;
		public Long			IdOperacion;
		public String		IdTipoOperacion;
		public String		IdTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoReajuste;
		public Long			IdInstrumento;
		public String		IdEmisor;
		public BigDecimal	Tasa;
		public Integer		DiasPlazo;
		public String		FechaOperacion;
		public String		CodigoOperador;
		public Long			IdAgente;
		public String		IdCanalIngreso;
	}

	public static class ObjREGISTROEVALUACIONLIMITE {
		public Long			IdOperacion;
		public String		IdTipoOperacion;
		public String		IdTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public BigDecimal	MontoUtilizado;
		public String		IdMoneda;
		public Long			IdLimite;
		public String		IdTipoLimite;
		public BigDecimal	MontoExcedido;
		public String		ConceptoExceso;
		public String		IdEstado;
		public String		IdEstadoAutorizacion;
		public String		IdTipoEvento;
		public Long			Prioridad;
		public String		ConceptoEvaluacion;
		public BigDecimal	MontoLimite;
		public BigDecimal	MontoCalculado;
		public String		IdMonedaCalculo;
		public String		IdTipoEntidad;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdCanalIngreso;
	}

	public static class ObjESTADOOPERACION {
		public Long		IdOperacion;
		public String	IdEstado;
	}
	
	public static class ObjIDAUTHTABLE {
		public Long		IdEvento;
		public String	NumeroOperacionConcepto;
	}
	
	public static class ObjADVERTENCIASDETALLE {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}
	
	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
		ServiceSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/*
	 * Logica Servicio
	 * 
	 */
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		SQL sql = new SQL();
		ServiceSalida out = new ServiceSalida();
		log.write("INICIO");
		try {

			// ------------------------------
			// Crea las tablas temporales

			SQL.exec(con,
					"CREATE LOCAL TEMPORARY TABLE #EvaluacionLimite_P_TABLAOPERACION (\"IdPortafolio\" BIGINT,\"IdCliente\" BIGINT,\"MontoUtilizado\" DECIMAL(26,8),\"IdMoneda\" NVARCHAR(3),\"TipoCambio\" DECIMAL(26,8),\"IdOperacion\" BIGINT,\"IdTipoOperacion\" NVARCHAR(6),\"IdTipoInstrumento\" NVARCHAR(3),\"IdClasificacionTipoInstrumento\" NVARCHAR(3),\"IdTipoReajuste\" NVARCHAR(3),\"IdInstrumento\" BIGINT,\"IdEmisor\" NVARCHAR(40),\"Tasa\" DECIMAL (26,8),\"DiasPlazo\" INTEGER,\"FechaOperacion\" NVARCHAR(10),\"CodigoOperador\" NVARCHAR(40),\"IdAgente\" BIGINT,\"IdCanalIngreso\" NVARCHAR(3))");
			
			sql.batch(con,
					"INSERT INTO #EvaluacionLimite_P_TABLAOPERACION (\"IdPortafolio\",\"IdCliente\",\"MontoUtilizado\",\"IdMoneda\",\"TipoCambio\",\"IdOperacion\",\"IdTipoOperacion\",\"IdTipoInstrumento\",\"IdClasificacionTipoInstrumento\",\"IdTipoReajuste\",\"IdInstrumento\",\"IdEmisor\",\"Tasa\",\"DiasPlazo\",\"FechaOperacion\",\"CodigoOperador\",\"IdAgente\",\"IdCanalIngreso\") VALUES (:IdPortafolio,:IdCliente,:MontoUtilizado,:IdMoneda,:TipoCambio,:IdOperacion,:IdTipoOperacion,:IdTipoInstrumento,:IdClasificacionTipoInstrumento,:IdTipoReajuste,:IdInstrumento,:IdEmisor,:Tasa,:DiasPlazo,:FechaOperacion,:CodigoOperador,:IdAgente,:IdCanalIngreso)",
					in.P_TABLAOPERACION, ObjTABLAOPERACION.class);

			// ------------------------------
			// Inserta tablas temporales globales

			// ------------------------------
			// Ejecuta el SP
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();

			parmsIN.put("P_IDEMPRESA", in.P_IDEMPRESA);
			parmsIN.put("P_IDUSUARIO", in.P_IDUSUARIO);
			parmsIN.put("P_IDMODULO", in.P_IDMODULO);
			parmsIN.put("P_USAREGLASNEGOCIO", in.P_USAREGLASNEGOCIO);
			parmsIN.put("P_SIMULAROPERACION", in.P_SIMULAROPERACION);
			parmsIN.put("P_TABLAOPERACION", in.P_TABLAOPERACION);
			parmsIN.put("P_IDTIPOENTIDAD", in.P_IDTIPOENTIDAD);
			parmsIN.put("P_GENERAAUTORIZACION", in.P_GENERAAUTORIZACION);

			HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

			parmsOUT.put("P_OUT_REGISTROEVALUACIONLIMITE", ObjREGISTROEVALUACIONLIMITE.class);
			parmsOUT.put("P_OUT_ESTADOOPERACION", ObjESTADOOPERACION.class);
			parmsOUT.put("P_OUT_IDAUTHTABLE", ObjIDAUTHTABLE.class);
			parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
			parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
			parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
			parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class);

			String queryCall = "call \"BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_USAREGLASNEGOCIO,:P_SIMULAROPERACION,#EvaluacionLimite_P_TABLAOPERACION,:P_IDTIPOENTIDAD,:P_GENERAAUTORIZACION,$P_OUT_REGISTROEVALUACIONLIMITE,$P_OUT_ESTADOOPERACION,$P_OUT_IDAUTHTABLE,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);

			// ------------------------------
			// Lee scalar
			
			out.P_OUT_ADVERTENCIASCONTADOR = (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
			out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

			// ------------------------------
			
			out.P_OUT_REGISTROEVALUACIONLIMITE = (List<ObjREGISTROEVALUACIONLIMITE>) rpt.get("P_OUT_REGISTROEVALUACIONLIMITE");
			out.P_OUT_ESTADOOPERACION = (List<ObjESTADOOPERACION>) rpt.get("P_OUT_ESTADOOPERACION");
			out.P_OUT_IDAUTHTABLE = (List<ObjIDAUTHTABLE>) rpt.get("P_OUT_IDAUTHTABLE");
			out.P_OUT_ADVERTENCIASDETALLE = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
			out.P_OUT_ERRORESDETALLE = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");

			// ------------------------------
			// Lee tablas temporales globales

		} finally {
			// Elimina tablas temporales locales

			try {
				SQL.exec(con, "DROP TABLE #EvaluacionLimite_P_TABLAOPERACION");
			} catch (Exception er) {
			}
		}
		log.write("TERMINO");
		return out;
	}
}