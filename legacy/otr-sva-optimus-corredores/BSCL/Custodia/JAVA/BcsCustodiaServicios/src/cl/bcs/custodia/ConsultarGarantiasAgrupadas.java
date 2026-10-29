package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ConsultarGarantiasAgrupadas implements JavaCall {
		
	private static final Logger logger = LogManager.getLogger(ConsultarGarantiasAgrupadas.class);

	private final String SQL_CONSULTAR_REGISTROS = loadResourceToString("cl/bcs/custodia/sql/ConsultarGarantiasAgrupadas.sql");

	public static class EntradaGarantiasAgrupadas extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_Cliente;
		public Long p_Portafolio;
		public String p_FechaDeLiquidacion;
		public String p_IdClasificacionIntrumento;
		public String p_IdContraparteGarantia;
		public String p_IdMonedaInstrumento;
		public String p_IdEstado;
		public Long p_IdInstrumento;
		public String p_IdOrigenGarantia;

		public EntradaGarantiasAgrupadas() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CUS";
			this.p_UsaReglasNegocio = "N";
			this.p_Cliente = 0L;
			this.p_Portafolio = 0L;
			this.p_FechaDeLiquidacion = "0001-01-01";
			this.p_IdClasificacionIntrumento = "";
			this.p_IdContraparteGarantia = "";
			this.p_IdMonedaInstrumento ="";
			this.p_IdEstado = "0";
			this.p_IdInstrumento = 0L;
			this.p_IdOrigenGarantia = "";
		}
	}
	
	public static class Registros extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreCliente;
		public String NombreOrigenGarantia;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdConcepto;
		public String NombreConcepto;
		public String RazonSocialContraparte;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdContraparteGarantia;
		public String IdMonedaInstrumento;
		public String IdOrigenGarantia;
		public BigDecimal INGCantidadAgrupada;
		public BigDecimal CantidadAgrupada;
		public BigDecimal MontoAgrupado;
		public BigDecimal MontoValorizadoMonedaInstrumentoAGR;
		
		public Registros() {
			this.IdCliente = 0L;
		    this.IdPortafolio = 0L;
		    this.NombreCliente = "";
		    this.NombreOrigenGarantia = "";
		    this.DocumentoIdentidad = "";
		    this.CodigoPortafolio = "";
		    this.IdClasificacionTipoInstrumento = "";
		    this.IdConcepto = "";
		    this.NombreConcepto = "";
		    this.RazonSocialContraparte = "";
		    this.IdInstrumento = 0L;
		    this.Nemotecnico = "";
		    this.IdOrigenGarantia = "";
		    this.IdTipoInstrumento = "";
		    this.NombreTipoInstrumento = "";
		    this.IdContraparteGarantia = 0L;
		    this.IdMonedaInstrumento = "";
		    this.INGCantidadAgrupada = BigDecimal.ZERO;
		    this.CantidadAgrupada = BigDecimal.ZERO;
		    this.MontoAgrupado = BigDecimal.ZERO;
		    this.MontoValorizadoMonedaInstrumentoAGR = BigDecimal.ZERO;
		}

	}
	
	


	public static class SalidaGarantiasAgrupadas extends BcsPojo {
		public List<Registros> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaGarantiasAgrupadas() {
			this.P_OUT_TABLA = new ArrayList<Registros>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}

	public SalidaGarantiasAgrupadas logica(Connection conn, EntradaGarantiasAgrupadas in) throws SQLException, Exception {
		logger.info("INICIO::ConsultarGarantiasAgrupadas");
		long inicio = System.currentTimeMillis();

		SQL sql=new SQL();
		SalidaGarantiasAgrupadas Salida = new SalidaGarantiasAgrupadas();
		List<Registros> resultadoRegistros = new ArrayList<Registros>();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		
		
		parametros.put("p_IdCliente", in.p_Cliente);
		parametros.put("p_IdPortafolio", in.p_Portafolio);
		parametros.put("p_FechaDeLiquidacion", in.p_FechaDeLiquidacion);
		parametros.put("p_IdContraparteGarantia", in.p_IdContraparteGarantia);
		parametros.put("p_IdMonedaInstrumento", in.p_IdMonedaInstrumento);
		parametros.put("p_IdInstrumento", in.p_IdInstrumento);
		parametros.put("p_IdOrigenGarantia", in.p_IdOrigenGarantia);
		
		logger.info("INFO::ConsultarGarantiasAgrupadas Consulta registros en la BD");
		resultadoRegistros = sql.query(conn,SQL_CONSULTAR_REGISTROS,parametros, Registros.class);

		Salida.P_OUT_TABLA = resultadoRegistros;
		Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarGarantiasAgrupadas.", fin - inicio);
		
		return Salida;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaGarantiasAgrupadas jin=gson.fromJson(in.toString() , EntradaGarantiasAgrupadas.class);
		SalidaGarantiasAgrupadas out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"N\",\"p_Cliente\":3050,\"p_Portafolio\":7757,\"p_FechaDeLiquidacion\":\"2025-05-15\",\"p_IdClasificacionIntrumento\":null,\"p_IdContraparteGarantia\":null,\"p_IdMonedaInstrumento\":null,\"p_IdEstado\":\"VIG\",\"p_IdInstrumento\":null,\"p_IdOrigenGarantia\":\"CTA\"}\r\n";

			EntradaGarantiasAgrupadas Objin = gson.fromJson(json, EntradaGarantiasAgrupadas.class);
			ConsultarGarantiasAgrupadas demo = new ConsultarGarantiasAgrupadas();
			try {
				SalidaGarantiasAgrupadas salida = demo.logica(con, Objin);
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