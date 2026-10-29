package cl.bcs.cuentaInversion;

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

public class ConsultarCobroComisionRegistros implements JavaCall {
		
	private static final Logger logger = LogManager.getLogger(ConsultarCobroComisionRegistros.class);

	private final String SQL_CONSULTAR_REGISTROS = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_ConsultarRegistros.sql");

	public static class EntradaCobroComisionConsultarRegistros extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_Cliente;
		public Long p_Portafolio;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_Periodo;
		public String p_IdEstado;
		public Long p_IdProductoAgrupador;

		public EntradaCobroComisionConsultarRegistros() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CTA";
			this.p_UsaReglasNegocio = "N";
			this.p_Cliente = 0L;
			this.p_Portafolio = 0L;
			this.p_FechaDesde = "0001-01-01";
			this.p_FechaHasta = "0001-01-01";
			this.p_Periodo = 0L;
			this.p_IdEstado = "0";
			this.p_IdProductoAgrupador = 0L;
		}
	}
	
	public static class Registros extends BcsPojo{
		public Long IdCliente;
		public String RutCliente;
		public String Nombre;
		public Long IdPortafolio;
		public String Portafolio;
		public String Producto;
		public BigDecimal MontoComision;
		public String EstadoComision;
		public String NombreEstado;
		public String PortafolioAPV;
		
		public Registros() {
			this.IdCliente = 0L;
			this.RutCliente = "";
			this.Nombre = "";
			this.IdPortafolio = 0L;
			this.Portafolio = "";
			this.Producto = "";
			this.MontoComision = BigDecimal.ZERO;
			this.EstadoComision = "";
			this.NombreEstado = "";
			this.PortafolioAPV = "N";
		}

	}
	
	public static class Tabla_Salida{
		public String RutCliente;
		public String Nombre;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Portafolio;
		public String Producto;
		public BigDecimal MontoComision;
		public String EstadoComision;
		public String NombreEstado;
		public String PortafolioAPV;

		public Tabla_Salida() {
			this.RutCliente = "";
			this.Nombre = "";
			this.IdCliente = 0L;
			this.IdPortafolio = 0L;
			this.Portafolio = "";
			this.Producto = "";
			this.MontoComision = BigDecimal.ZERO;
			this.EstadoComision = "";
			this.NombreEstado = "";
			this.PortafolioAPV = "N";

		}

	}
	
	private Tabla_Salida copyData(Registros reg){
		Tabla_Salida tbl = new Tabla_Salida();
		tbl.IdCliente		= reg.IdCliente;
		tbl.RutCliente		= reg.RutCliente;
		tbl.Nombre			= reg.Nombre;
		tbl.IdPortafolio	= reg.IdPortafolio;
		tbl.Portafolio		= reg.Portafolio;
		tbl.Producto		= reg.Producto;
		tbl.MontoComision	= reg.MontoComision;
		tbl.EstadoComision	= reg.EstadoComision;
		tbl.NombreEstado	= reg.NombreEstado;
		tbl.PortafolioAPV	= reg.PortafolioAPV;

		return tbl;
	}

	public static class SalidaCobroComisionEditarRegistro extends BcsPojo {
		public List<Tabla_Salida> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaCobroComisionEditarRegistro() {
			this.P_OUT_TABLA = new ArrayList<Tabla_Salida>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}

	public SalidaCobroComisionEditarRegistro logica(Connection conn, EntradaCobroComisionConsultarRegistros in) throws SQLException, Exception {
		logger.info("INICIO::ConsultarCobroComisionRegistros");
		long inicio = System.currentTimeMillis();

		SQL sql=new SQL();
		SalidaCobroComisionEditarRegistro Salida = new SalidaCobroComisionEditarRegistro();
		List<Registros> resultadoRegistros = new ArrayList<Registros>();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("p_Cliente", in.p_Cliente);
		parametros.put("p_Portafolio", in.p_Portafolio);
		parametros.put("p_FechaDesde", in.p_FechaDesde);
		parametros.put("p_FechaHasta", in.p_FechaHasta);
		parametros.put("p_Periodo", in.p_Periodo);
		parametros.put("p_EstadoComision", in.p_IdEstado);
		parametros.put("p_ProductoAgrupador", in.p_IdProductoAgrupador);
		
		logger.info("INFO::ConsultarCobroComisionRegistros Consulta registros en la BD");
		resultadoRegistros = sql.query(conn,SQL_CONSULTAR_REGISTROS,parametros, Registros.class);

		Salida.P_OUT_TABLA = resultadoRegistros.parallelStream().map(por -> copyData(por)).collect(Collectors.toList());
		Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarCobroComisionRegistros.", fin - inicio);
		
		return Salida;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCobroComisionConsultarRegistros jin=gson.fromJson(in.toString() , EntradaCobroComisionConsultarRegistros.class);
		SalidaCobroComisionEditarRegistro out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 0,\r\n"
					+ "  \"p_IdModulo\": \"CTA\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_Cliente\": 25983,\r\n"
					+ "  \"p_Portafolio\": 0,\r\n"
					+ "  \"p_FechaDesde\": \"2024-05-16\",\r\n"
					+ "  \"p_FechaHasta\": \"2024-05-16\",\r\n"
					+ "  \"p_Periodo\": 2,\r\n"
					+ "  \"p_IdEstadoComision\": \"\",\r\n"
					+ "  \"p_IdProductoAgrupador\": 0\r\n"
					+ "}";
			EntradaCobroComisionConsultarRegistros Objin = gson.fromJson(json, EntradaCobroComisionConsultarRegistros.class);
			ConsultarCobroComisionRegistros demo = new ConsultarCobroComisionRegistros();
			try {
				SalidaCobroComisionEditarRegistro salida = demo.logica(con, Objin);
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