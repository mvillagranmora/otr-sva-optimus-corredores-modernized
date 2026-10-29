package cl.bcs.normativa;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarLibroOperaciones implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarLibroOperaciones.class);

	private final String SQL_SELECT_CONSULTA_LIBRO_OPERACIONES = loadResourceToString("cl/bcs/normativa/sql/ConsultarLibroOperaciones.sql");
	

	public static class EntradaConsultarLibroOperaciones extends BcsPojo{
		public Long      p_IdEmpresa;

		public String    p_FechaDesde;
		public String    p_FechaHasta;
		
		public Long      p_IdCliente;
		public String    p_IdMoneda;
		public String    p_IdBolsa;
		public String    p_IdClasificacion;
		public String    p_IdTipoInstrumento;
		public Long      p_IdInstrumento;
		public String    p_IdTipOperacion;
		
		public String    p_MostrarPactos = "S";
		public String    p_MostrarEVC = "S";
		
	}
	public static class ConsultarLibroOperaciones_Salida{
		
	    public java.sql.Timestamp  FechaMovimiento;
	    public String     FolioTransaccion;
	    public String     IdOrden;
	    public String     IdComprobante;
	    public Long       IdOperacion;
	    public Long       IdCliente;
	    public String     RutCliente;
	    public String     NombreCliente;
	    public Long       IdPortafolio;
	    public String     CodigoPortafolio;
	    public String     AreaCliente;
	    public String     IdClasificacionTipoInstrumento;
	    public String     IdTipoInstrumento;
	    public String     IdTipoOperacion;
	    public String     ClasificacionTipoInstrumento;
	    public String     TipoInstrumento;
	    public String     TipoOperacion;
	    public String     AccionOperacion;
	    public String     Relacion;
	    public String     CodigoTipoRelacion;
	    public String     TipoRelacion;
	    public String     IdEmisor;
	    public String     Nemotecnico;
	    public BigDecimal Cantidad;
	    public BigDecimal Precio;
	    public BigDecimal Tasa;
	    public String     IdMoneda;
	    public String     IdMonedaSecundaria;
	    public BigDecimal TasaCompra;
	    public String     RelacionInstrumento;
	    public Integer     IdOperadorContraparte;
	    public Integer     IdOperadorCorredor;
	    public String     IdTipoComprobante;
	    public String     NombreTipoComprobante;
	    public Long     FolioComprobante;
	    public BigDecimal ValorPresenteCompraInst;
	    public java.sql.Timestamp  FechaVencimiento;
	    public BigDecimal MontoCompra;
	    public BigDecimal MontoVenta;
	    public BigDecimal Comision;
	    public BigDecimal Derecho;
	    public BigDecimal CargoFijo;
	    public BigDecimal IVA;
	    public BigDecimal TotalCompra;
	    public BigDecimal TotalVenta;
	    public String     IdCondicionLiquidacion;
	    public Long     IdDocumentoRelacion;
	    public java.sql.Timestamp  FechaVencimientoPacto;
	    public BigDecimal PrecioPlazo;
	    public BigDecimal TasaPlazo;
	    public String     IdMonedaPlazo;
	    public BigDecimal TotalCompraVencimiento;
	    public BigDecimal TotalVentaVencimiento;
	    public String Custodia;
	    public String     IdBoveda;
	    public BigDecimal CantidadGarantia;
	    public String     NemoSVS;
	    public BigDecimal TasaVenta;
	    public BigDecimal ValorPresente;
	    public BigDecimal UtilidadPerdida;
	    public String  FechaOrigenPacto;
	    public String     IdEstadoFactura;
	    public String     Canal;
	    public BigDecimal ValorIVA;
	    public Long     IdComprobanteReferencia;
	    public Long     FolioComprobanteAsociado;
	    public String     NombreComprador;
	    public String     NombreVendedor;
	    public String     RutComprador;
	    public String     RutVendedor;
	    public String     IdBolsa;
	    public String     Proceso;
	    public String     EsVentaCorta;
	    public String     EsVentaCortaUI;
	    public String     FolioDTE;
	    public BigDecimal ParidadCierre;
	    public Integer    PlazoVencimiento;
	    public Long     IdOperacionRelacionada;
	    public Long     IdOperacionOrigenReasignacion;
	    public String     InstrumentoNegociacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class SalidaConsultarLibroOperaciones extends BcsPojo{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<ConsultarLibroOperaciones_Salida> P_OUT_LISTAOPERACIONES;
		
		public SalidaConsultarLibroOperaciones() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_LISTAOPERACIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	

	public SalidaConsultarLibroOperaciones logica(Connection conn, EntradaConsultarLibroOperaciones in) throws Exception {
		logger.info("INICIO::ConsultarLibroOperaciones");
		long inicio = System.currentTimeMillis();
		String FechaDesde = in.p_FechaDesde;
		String FechaHasta = in.p_FechaHasta;
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		if(FechaDesde == null || FechaHasta == null ) {
			FechaDesde = fechaSistema;
			FechaHasta = fechaSistema;
		}
		HashMap<String,Object> params = null;
		params = new HashMap<>();
		params.put("p_IdEmpresa", in.p_IdEmpresa); 
		params.put("p_FechaDesde", FechaDesde);
		params.put("p_FechaHasta", FechaHasta);
		params.put("p_IdCliente", in.p_IdCliente);
		params.put("p_IdMoneda", in.p_IdMoneda);
		params.put("p_IdBolsa", in.p_IdBolsa);
		params.put("p_IdClasificacion", in.p_IdClasificacion);
		params.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
		params.put("p_IdInstrumento", in.p_IdInstrumento);
		params.put("p_IdTipOperacion", in.p_IdTipOperacion);
		params.put("p_MostrarPactos", in.p_MostrarPactos);
		params.put("p_MostrarEVC", in.p_MostrarEVC);
		SalidaConsultarLibroOperaciones salida = new SalidaConsultarLibroOperaciones();
		List<ConsultarLibroOperaciones_Salida> operaciones = null;
		SQL _sql = new SQL();
		logger.debug("INICIO::SQL_SELECT_CONSULTA_LIBRO_OPERACIONES");
		operaciones = _sql.query(conn, SQL_SELECT_CONSULTA_LIBRO_OPERACIONES, params, ConsultarLibroOperaciones_Salida.class);
		logger.debug("FIN::SQL_SELECT_CONSULTA_LIBRO_OPERACIONES");
		for(ConsultarLibroOperaciones_Salida ope : operaciones) {
			salida.P_OUT_LISTAOPERACIONES.add(ope);
		}
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarLibroOperaciones", fin - inicio);
		return salida;		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarLibroOperaciones in = gson.fromJson(jin.toString(), EntradaConsultarLibroOperaciones.class);
		SalidaConsultarLibroOperaciones out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1}";

			EntradaConsultarLibroOperaciones objIn = gson.fromJson(json, EntradaConsultarLibroOperaciones.class);
			ConsultarLibroOperaciones demo = new ConsultarLibroOperaciones();
			SalidaConsultarLibroOperaciones ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
