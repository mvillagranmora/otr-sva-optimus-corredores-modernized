package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
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
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarAsignaciones implements JavaCall{
	
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarAsignaciones_Select.sql");
	private final String SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarAsignaciones_InsertIdPortafolioTablaPaso.sql");
	private final String SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarAsignaciones_InsertFullPortafolioTablaPaso.sql");
	private final String SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarAsignaciones_DeleteIdPortafolioTablaPaso.sql");

	static final Logger logger = LogManager.getLogger(ConsultarAsignaciones.class.getName());
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarAsignaciones_Entrada in = gson.fromJson(jin.toString(), ConsultarAsignaciones_Entrada.class);
		ConsultarAsignaciones_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class ConsultarAsignaciones_Entrada{
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		//Parametros de ventana
		public String 	p_Mercado;
		public String 	p_TipoInstrumento;
		public String 	p_Operacion;
		public Long		p_Instrumento;
		public String 	p_FechaAsignacionI;
		public String 	p_FechaAsignacionF;
		public String 	p_CondicionLiquidacion;
		public Long 	p_LibroOrdenes;
		public String 	p_Bolsa;
		public String 	p_Estado;
		public Long		p_IdPortafolio;
		public String	p_IdMonedaOperacion;
		public String 	p_MarcaAsignacion;
		public String 	p_CodigoOperador;
		
		//Para APV
		public Long 	p_IdCliente;
		public Long 	p_Producto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Estructura de datos para la Grilla
	public static class Asignacion_Salida{
		public Long IdEmpresa;
		public Timestamp FechaAsignacion;
		public Long IdOrden;
		public String FolioTransaccion;
		public Timestamp FechaOperacionTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String NombreTipoOperacion;
		public String CodigoOperador;
		public String InstrumentoUI;
		public BigDecimal PrecioAsignacion;
		public BigDecimal MontoAsignado;
		public Long MontoCLP;
		public BigDecimal TipoCambio;
		public BigDecimal Tasa;
		public String IdBolsa;
		public Date FechaLiquidacion;
		public Date FechaVencimiento;
		public String IdEmisor;
		public String CodigoCondicionDeLiquidacion;
		public String EstadoAsignacion;
		public BigDecimal CantidadAsignada;
		public String NombreEstadoFacturacion;
		public String IdTipoReajuste;
		public String IdMonedaOperacion;
		public String IdTipoInstrumento;
		public String NombreLibroOrdenes;
		public String NombreBoveda;
		public String CodigoBoveda;
		public String IndicadorBancoCustodio;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public String NombreFormaCustodia;
		public String CodigoEmpleadoPortafolio;
		public String CodigoEmpleadoDigitador;
		public BigDecimal MontoAsignadoPlazo;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String NombreTipoCanalIngreso;
		public String IndicadorOrdenDirecta;
		public String FolioOrden;
		public Timestamp FechaOperacionOrden;
		public Long IdAsignacion;
		public String CodigoIFRS;
		public String NombreMedioDePago;
		public String ModalidadLiquidacion;
		public String ModalidadEjercicio;
		public String TipoOpcion;
		public String EstiloOpcion;
		public Long Tamano;
		public String Producto;
		public String IdMonedaInstrumento;
		public String IdMonedaLiquidacionOrden;
		public String RazonSocialPortafolio;
		public String TipoLiquidacion;
		public String ModoFacturacion;
		public Long IdAsignacionMasiva;
		public String NombreTipoAsignacion;
		public String AplicaComisionAsignacionMasiva;
		
		//filtros
		public Long IdInstrumentoSubyacente;
		public String AccionOrden;
		public Long IdInstrumentoUI;
		public String IdCondicionDeLiquidacion;
		public Long IdLibroOrdenes;
		public Integer IdProductoAgrupador;
		public String IdEstadoAsignacion;
		public String IdEstadoFacturacion;
		public Long FolioSpot;
		public String EstadoSpot;
		public String InstrumentoNegociacion;
		
		public String EsAPlazo;
		public String EsVentaCorta;
		public String EsAPlazoUI;
		public String EsVentaCortaUI;
		public BigDecimal TasaVentaCorta;
		public String MarcaAsignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Portafolio(Long idPortafolio) {
			this.IdPortafolio = idPortafolio;
		}
		public Portafolio() {}
	}
	
	//Salida para la grilla
	public static class ConsultarAsignaciones_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<Asignacion_Salida> P_OUT_ASIGNACIONES;
		
		public ConsultarAsignaciones_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ASIGNACIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	public ConsultarAsignaciones_Salida logica(Connection conn, ConsultarAsignaciones_Entrada entrada) throws Exception{
		logger.info("INICIO CONSULTA ASIGNACIONES");
		SQL _sql = new SQL();
		ConsultarAsignaciones_Salida salida = new ConsultarAsignaciones_Salida();
		List<Long> listIdPortafolio = new ArrayList<Long>();
		logger.debug("PORTAFOLIO EJECUTIVO");
		if(entrada.p_IdPortafolio != 0) 
		{
			listIdPortafolio.add(entrada.p_IdPortafolio);
			logger.debug("INSERT TABLA DE PASO");
			//se utiliza una tabla de paso para guardar idsPortafolio, optimiza consulta de Asignaciones
			List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
				Portafolio portafolio = new Portafolio();
				portafolio.IdPortafolio = id;
				return portafolio;
			}).collect(Collectors.toList());
			
			_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
		}
		else 
		{
			//Obtenemos los portafolios del ejecutivo
			ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
			
			ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
			
			entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsulta.p_IdModulo = entrada.p_IdModulo;
			entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
			boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
			if (restringeCartera) {
				listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
				logger.debug("INSERT TABLA DE PASO");
				//se utiliza una tabla de paso para guardar idsPortafolio, optimiza consulta de Asignaciones
				List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = id;
					return portafolio;
				}).collect(Collectors.toList());
				
				_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
			}
			else {
				logger.debug("No restringe cartera");
				_sql.batch(conn, SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO, new ArrayList<>(), Portafolio.class);
			}
			
		}
		
		
		//Obtenemos las asignaciones
		
		int varMercado = 0;
		int varTipoInstrumento = 0;
		int varOperacion = 0;
		int varInstrumento = 0;
		int varCondicionLiquidacion = 0;
		int varLibroOrdenes = 0;
		int varBolsa = 0;
		int varProducto = 0;
		int varEstado = 0;
		int varIdCliente = 0;
		int varIdMonedaOperacion = 0;
		int varMarcaAsignacion = 0;
		int varCodigoOperador = 0;
		
		if (entrada.p_Mercado.equalsIgnoreCase("0")) {
			entrada.p_Mercado = "";
			varMercado = 1;
		}
		
		if(entrada.p_TipoInstrumento.equalsIgnoreCase("0")) {
			entrada.p_TipoInstrumento = "";
			varTipoInstrumento = 1;
		}
		
		if(entrada.p_Operacion.equalsIgnoreCase("0")) {
			entrada.p_Operacion = "";
			varOperacion = 1;
		}
		
		if(entrada.p_Instrumento==0) {
			varInstrumento = 1;
		}
		
		if(entrada.p_CondicionLiquidacion.equalsIgnoreCase("0")) {
			entrada.p_CondicionLiquidacion = "";
			varCondicionLiquidacion = 1;
		}
		
		if(entrada.p_LibroOrdenes == 0) {
			varLibroOrdenes = 1;
		}
		
		if(entrada.p_Bolsa.equalsIgnoreCase("0")) {
			entrada.p_Bolsa = "";
			varBolsa = 1;
		}
		
		if(entrada.p_Producto == 0) {
			varProducto = 1;
		}
		
		if(entrada.p_Estado.equalsIgnoreCase("0")) {
			entrada.p_Estado = "";
			varEstado = 1;
		}
		
		if(entrada.p_IdCliente == 0) {
			varIdCliente = 1;
		}
		
		if (entrada.p_IdMonedaOperacion.equalsIgnoreCase("0")) {
			entrada.p_IdMonedaOperacion = "";
			varIdMonedaOperacion = 1;
		}
		if(entrada.p_MarcaAsignacion.equals("")) {
			varMarcaAsignacion = 1;
		}
		
		if (entrada.p_CodigoOperador.equalsIgnoreCase("0")) {
		    entrada.p_CodigoOperador = "";
		    varCodigoOperador = 1;
		}
		
		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		//params.put("p_listIdPortafolio", listIdPortafolio);
		params.put("p_IdClasificacionTipoInstrumento", entrada.p_Mercado);
		params.put("var_IdClasificacionTipoInstrumento", varMercado);
		params.put("p_IdTipoInstrumento", entrada.p_TipoInstrumento);
		params.put("var_IdTipoInstrumento", varTipoInstrumento);
		params.put("p_AccionOrden", entrada.p_Operacion);
		params.put("var_AccionOrden", varOperacion);
		params.put("p_IdInstrumentoUI", entrada.p_Instrumento);
		params.put("var_IdInstrumentoUI", varInstrumento);
		params.put("p_FechaAsignacionI", entrada.p_FechaAsignacionI);
		params.put("p_FechaAsignacionF", entrada.p_FechaAsignacionF);
		params.put("p_IdCondicionDeLiquidacion", entrada.p_CondicionLiquidacion);
		params.put("var_IdCondicionDeLiquidacion", varCondicionLiquidacion);
		params.put("p_IdLibroOrdenes", entrada.p_LibroOrdenes);
		params.put("var_IdLibroOrdenes", varLibroOrdenes);
		params.put("p_IdBolsa", entrada.p_Bolsa);
		params.put("var_IdBolsa", varBolsa);
		params.put("p_IdProductoAgrupador", entrada.p_Producto);
		params.put("var_IdProductoAgrupador", varProducto);
		params.put("p_IdEstadoAsignacion", entrada.p_Estado);
		params.put("var_IdEstadoAsignacion", varEstado);
		params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("var_IdCliente", varIdCliente);
		params.put("p_IdMonedaOperacion", entrada.p_IdMonedaOperacion);
		params.put("varIdMonedaOperacion", varIdMonedaOperacion);
		params.put("p_CodigoOperador", entrada.p_CodigoOperador);
		params.put("var_CodigoOperador", varCodigoOperador);
		
		params.put("p_MarcaAsignacion", entrada.p_MarcaAsignacion);
		params.put("var_MarcaAsignacion", varMarcaAsignacion);
				
		salida.P_OUT_ASIGNACIONES = _sql.queryParallel(conn, SQL_SELECT_ASIGNACIONES, params, Asignacion_Salida.class);
	
		logger.debug("antes eliminacion tabla");
		SQL.exec(conn, SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO);
		logger.info("TERMINO CONSULTAR ASIGNACIONES");
		
		return salida;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPRESURAConnection())
		{
			Gson gson = Utils.getGsonBuilder();
			
			//Datos simulados, obtenidos desde ventana
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9681,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_IdEmpleadoConsulta\": 108,\r\n"
					+ "    \"p_Mercado\": \"0\",\r\n"
					+ "    \"p_TipoInstrumento\": \"0\",\r\n"
					+ "    \"p_Operacion\": \"0\",\r\n"
					+ "    \"p_Instrumento\": \"0\",\r\n"
					+ "    \"p_FechaAsignacionI\": \"2026-05-04T04:00:00.000Z\",\r\n"
					+ "    \"p_FechaAsignacionF\": \"2026-05-05T03:59:59.000Z\",\r\n"
					+ "    \"p_CondicionLiquidacion\": \"0\",\r\n"
					+ "    \"p_LibroOrdenes\": \"0\",\r\n"
					+ "    \"p_Bolsa\": \"0\",\r\n"
					+ "    \"p_Estado\": \"0\",\r\n"
					+ "    \"p_IdPortafolio\": \"0\",\r\n"
					+ "    \"p_MarcaAsignacion\": \"TEST\",\r\n"
					+ "    \"p_IdCliente\": \"0\",\r\n"
					+ "    \"p_Producto\": \"0\"\r\n"
					+ "}";
			ConsultarAsignaciones_Entrada Objin = gson.fromJson(json, ConsultarAsignaciones_Entrada.class);
			ConsultarAsignaciones demo = new ConsultarAsignaciones();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				ConsultarAsignaciones_Salida ObjOut = demo.logica(con, Objin);
				System.out.println("SALIDA:" + ObjOut.P_OUT_ASIGNACIONES.size());
				
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
		
}
