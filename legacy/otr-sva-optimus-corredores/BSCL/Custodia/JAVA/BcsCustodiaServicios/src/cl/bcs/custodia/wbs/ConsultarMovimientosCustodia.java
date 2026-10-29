package cl.bcs.custodia.wbs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaConsultarClientePorDocumentoIdentidad;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaDocumentoIdentidad;
import cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.SalidaConsultarClientePorDocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMovimientosCustodia implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarMovimientosCustodia.class);
	
	private final String SQL_SELECT_CONSULTAR_MOVIMIENTOS_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/wbs/sql/ConsultarMovimientos_SelectMovimientosEnTransito.sql");
	private final String SQL_SELECT_CONSULTAR_CLIENTEPORTAFOLIO = loadResourceToString("cl/bcs/custodia/wbs/sql/ConsultarMovimientos_SelectClientePortafolio.sql");
	
	
	public static class EntradaConsultarMovimientosCustodia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico;
		public String IdEstado;
		public String IdMonedaInstrumento;
		public String IdTipoOperacion;
		public String Concepto;
		public String IdMatrizAgrupador;
		public String FechaInOperacion;
		public String FechaTerOperacion;
	    
	    
	    public EntradaConsultarMovimientosCustodia() {
	    	//this.p_Datos = new ArrayList<FiltroConsulta>();
	    }
	}
	
	public static class SalidaConsultaMovimientosEnTransito extends BcsPojo {
		public Long IdMovimientoCarteraEnTransito;
		public String EsNominal;
		public String NombreEstado;
		public String CodigoPortafolio;
		public Timestamp FechaIngresoRegistro;
		public Date FechaLiquidacion;
		public String IdTipoOperacion;
		public String NombreConcepto;
		public String Nemotecnico;
		public String IdCondicionLiquidacion;
		public BigDecimal Cantidad;
		public String IdMonedaInstrumento;
		public BigDecimal MontoCompraMonedaInstrumento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String IdTipoReajuste;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompra;
		public String NombreBoveda;
		public Long IdMatrizAgrupador;
		public String NemoSVS;
		public String IdTipoInstrumento;
		public String TipoInstrumento;
	}
	
	public static class ClientePortafolioVista extends BcsPojo {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
	}
	
	public static class SalidaClientePortafolio extends BcsPojo {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
	}

	public class SalidaConsultarMovimientosCustodia extends BcsPojo{
		public List<SalidaConsultaMovimientosEnTransito> P_OUT_MOVIMIENTOS_EN_TRANSITO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;


		
		public SalidaConsultarMovimientosCustodia() {

			this.P_OUT_ERRORESCONTADOR = 0L;
	    	this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	    	this.P_OUT_MOVIMIENTOS_EN_TRANSITO = new ArrayList<SalidaConsultaMovimientosEnTransito>();

		}
	}
	

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new GsonBuilder().serializeNulls().create();		
		EntradaConsultarMovimientosCustodia jin = gson.fromJson(in.toString(), EntradaConsultarMovimientosCustodia.class);
		SalidaConsultarMovimientosCustodia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarMovimientosCustodia logica(Connection con, EntradaConsultarMovimientosCustodia entrada) throws Exception {
		logger.info("INICIO::ConsultarMovimientosCustodia.logica");
		SQL sql = new SQL();
		SalidaConsultarMovimientosCustodia salida = new SalidaConsultarMovimientosCustodia();
		long inicio = System.currentTimeMillis();		
		
		//// CLIENTE- PORTAFOLIO
		
		HashMap<String, Object> cliPor = new HashMap<>();
		cliPor.put("DocumentoIdentidad", entrada.DocumentoIdentidad);
		cliPor.put("CodigoPortafolio", entrada.CodigoPortafolio);		
		List<ClientePortafolioVista> valCliPor = sql.query(con, SQL_SELECT_CONSULTAR_CLIENTEPORTAFOLIO, cliPor, ClientePortafolioVista.class);	
		if(valCliPor.isEmpty()) {
			if(entrada.CodigoPortafolio != null) {
				HashMap<String, String> res = new HashMap<>();
				res.put("DocumentoIdentidad", entrada.DocumentoIdentidad);
				res.put("CodigoPortafolio", entrada.CodigoPortafolio);
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CLIENTE_PORTAFOLIO_NO_EXISTE", res, con, entrada.p_IdEmpresa));
				return salida;
			}
			else {
				HashMap<String, String> res = new HashMap<>();
				res.put("DocumentoIdentidad", entrada.DocumentoIdentidad);
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DOCUMENTO_IDENTIDAD_NO_EXISTE", res, con, entrada.p_IdEmpresa));
				return salida;
				
			}
		}
		
		/// CONSULTA MOVIMIENTOS EN TRANSITO
		
		if(entrada.FechaInOperacion != null && entrada.FechaTerOperacion != null) {
			

			HashMap<String, Object> params = new HashMap<>();
			
			params.put("FechaInOperacion", entrada.FechaInOperacion);
			params.put("FechaTerOperacion", entrada.FechaTerOperacion);
			params.put("IdTipoInstrumento", entrada.IdTipoInstrumento);
			params.put("DocumentoIdentidad", entrada.DocumentoIdentidad);
			params.put("CodigoPortafolio", entrada.CodigoPortafolio);
			params.put("IdClasificacionTipoInstrumento", entrada.IdClasificacionTipoInstrumento);
			params.put("Nemotecnico", entrada.Nemotecnico);
			params.put("IdEstado", entrada.IdEstado);
			params.put("IdMonedaInstrumento", entrada.IdMonedaInstrumento);
			params.put("IdTipoOperacion", entrada.IdTipoOperacion);
			params.put("Concepto", entrada.Concepto);
			params.put("IdMatrizAgrupador", entrada.IdMatrizAgrupador);
			
			List <SalidaConsultaMovimientosEnTransito> resultado = sql.query(con, SQL_SELECT_CONSULTAR_MOVIMIENTOS_EN_TRANSITO, params, SalidaConsultaMovimientosEnTransito.class);
			if(resultado.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_NO_EXISTE_INFORMACION", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
				return salida;
				
			}
			
			salida.P_OUT_MOVIMIENTOS_EN_TRANSITO = resultado;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_CONSULTA_MOVIMIENTOS_EXITOSA", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			
		}
		else {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_FILTRO_FECHA_OBLIGATORIO", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			return salida;
			
		}
		
		
		long fin= System.currentTimeMillis();
		logger.info("FIN::ConsultarMovimientosCustodia.logica", fin - inicio);
		return salida;
	}
	
	
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties")) {	
			Gson gson = new GsonBuilder().serializeNulls().create();
			String json = "{\r\n"
						+ "  \"p_IdEmpresa\": 1,\r\n"
						+ "  \"p_IdUsuario\": 0,\r\n"
						+ "  \"p_IdModulo\": \"CUS\",\r\n"
						+ "  \"DocumentoIdentidad\": \"3187722-9\",\r\n"
						+ "  \"CodigoPortafolio\": null,\r\n"
						+ "  \"IdTipoInstrumento\": \"ACN\",\r\n"
						+ "  \"IdClasificacionTipoInstrumento\": null,\r\n"
						+ "  \"Nemotecnico\": null,\r\n"
						+ "  \"IdEstado\": null,\r\n"
						+ "  \"IdMonedaInstrumento\": \"CLP\",\r\n"
						+ "  \"IdTipoOperacion\": null,\r\n"
						+ "  \"Concepto\":\"IGRV85\",\r\n"
						+ "  \"IdMatrizAgrupador\": null,\r\n"
						+ "  \"FechaInOperacion\": \"2026-05-03\",\r\n"
						+ "  \"FechaTerOperacion\":\"2026-07-03\"\r\n"
						+ "}";
			
			EntradaConsultarMovimientosCustodia Objin= gson.fromJson(json, EntradaConsultarMovimientosCustodia.class);
			ConsultarMovimientosCustodia  demo= new ConsultarMovimientosCustodia();				
			try {
				long inicio = System.currentTimeMillis();
				SalidaConsultarMovimientosCustodia ObjOut= demo.logica(con, Objin);
				long fin = System.currentTimeMillis();
				System.out.println("TOTAL:ConsultarMovimientosCustodia:" + (fin - inicio));
				System.out.println(ObjOut.toString());
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
