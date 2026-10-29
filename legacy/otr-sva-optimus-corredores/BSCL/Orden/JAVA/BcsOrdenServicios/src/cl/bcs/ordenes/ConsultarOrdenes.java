package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ConsultarOrdenes implements JavaCall{
	
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenes_Select.sql");
	private final String SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenes_InsertIdPortafolioTablaPaso.sql");
	private final String SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenes_InsertFullPortafolioTablaPaso.sql");
	private final String SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenes_DeleteIdPortafolioTablaPaso.sql");
	
	private final static Logger logger = LogManager.getLogger(ConsultarOrdenes.class);

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarOrdenes_Entrada in = gson.fromJson(jin.toString(), ConsultarOrdenes_Entrada.class);
		ConsultarOrdenes_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class ConsultarOrdenes_Entrada{
		//Parametros Fijos
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEmpleadoConsulta;
		
		//Parametros de ventana
		public String 	p_Mercado;
		public String 	p_TipoInstrumento;
		public String 	p_Operacion;
		public Long		p_Instrumento;
		public String 	p_FechaOperacionOrdenI;
		public String 	p_FechaOperacionOrdenF;
		public Long 	p_LibroOrdenes;
		public String 	p_CondicionLiquidacion;
		public String   p_IdTipoCanalIngreso;
		public Long		p_IdPortafolio;
		public Long 	p_IdCliente;
		public Long 	p_IdProducto;
		public String 	p_EsVentaCorta;
		public String 	p_IncluyeOrdenesVigentes;
		public Boolean	p_OrdenesPorAsignar;
		public Long     p_CantidadDisponibleOrden;
		public String   p_IdEstadoOrden;
		public String 	p_Moneda;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Estructura de datos para la Grilla
	public static class Orden_Salida{
		
		public Long 		IdOrden;
		public String 		FolioOrden;
		public Timestamp 	FechaRecepcion;
		public Timestamp 	FechaIngresoRegistro;
		public Date 		FechaIngresoRegistroCorta;
		public Timestamp	FechaSolicitudAnulacion;
		public Timestamp 	FechaAnulacion;
		public Timestamp 	FechaOferta;
		public Timestamp 	FechaOperacionOrden;
		public Date 		FechaVigencia;
		public Date 		FechaVencimiento;
		public Date 		FechaEmision;
		public Date 		FechaLiquidacion;
		public BigDecimal 	MontoLiquidacion;
		public Timestamp 	FechaAceptacion;
		public Date 		FechaAutorizacion;
		public String 		IdTipoVigenciaOrden;
		public String 		NombreTipoVigenciaOrden;
		public String 		GlosaOrden;
		public String 		IdEstadoOrden;
		public String 		EstadoOrden;
		public BigDecimal 	CantidadOrden;
		public BigDecimal 	MontoOrden;
		public Long 		Plazo;
		public BigDecimal 	Tasa;
		public String 		IdBoveda;
		public String 		IdFormaCustodia;
		public String 		NombreFormaCustodia;
		public String 		PorcentajeVisibilidad;
		public BigDecimal 	ValorRescate;
		public String 		IdTipoReajuste;
		public BigDecimal 	PrecioOrden;
		public BigDecimal 	PrecioMercado;
		public BigDecimal 	TasaSimultanea;
		public BigDecimal 	TasaVentaCorta;
		public BigDecimal 	TasaRFIF;
		public BigDecimal 	TasaReporte;
		public Long 		PlazoVentaCorta;
		public Long 		PlazoSimultanea;
		public Long 		PlazoIF;
		public Long 		PlazoReporte;
		public String 		EsVentaCorta;
		public String 		EsVentaCortaUI;
		public String 		IndicadorFacturacionInmediata;
		public String 		IdTipoPrecio;
		public String 		IdTipoCanalDistribucion;
		public String 		NombreCanalDistribucion;
		public String 		EspecificaBolsa;
		public String 		Reservado;
		public String 		Bloqueado;
		public String 		TipoPrecio;
		public String 		IdTipoTasa;
		public BigDecimal 	ValorCaptacion;
		public String 		IdCondicionDeLiquidacion;
		public String 		CodigoCondicionDeLiquidacion;
		public String 		NombreCondicionDeLiquidacion;
		public String 		IdTipoOperacion;
		public String 		IdTipoProducto;
		public Long 		IdCliente;
		public String 		RazonSocial;
		public String 		NombreCompleto;
		public String 		DocumentoIdentidad;
		public Long 		IdPortafolio;
		public String 		CodigoPortafolio;
		public Long 		IdGrupoCobro;
		public String 		IdTipoCanalIngreso;
		public String 		NombreCanal;
		public Long 		IdLibroOrdenes;
		public String 		NombreLibroOrdenes;
		public String 		IdTipoLibroOrden;
		public String 		IdEmisor;
		public Long 		IdEmpresa;
		public String 		IdBolsa;
		public String 		NombreBolsa;
		public Long 		IdInstrumento;
		public String 		Instrumento;
		public String 		IdTipoInstrumento;
		public String 		NombreTipoInstrumento;
		public String 		IdClasificacionTipoInstrumento;
		public String 		NombreClasificacionTipoInstrumento;
		public Long 		IdInstrumentoSubyacente;
		public String 		InstrumentoSubyacente;
		public String 		IdTipoInstrumentoSubyacente;
		public String 		NombreTipoInstrumentoSubyacente;
		public String 		IdClasificacionTipoInstrumentoSubyacente;
		public String 		NombreClasificacionTipoInstrumentoSubyacente;
		public Long 		IdInstrumentoUI;
		public String 		InstrumentoUI;
		public String 		OrdenVigente;
		public String 		OrdenVigenteUI;
		public String 		IdEstadoAsignacion;
		public String 		EstadoOrdenAsignacion;
		public Long 		Tamano;
		public String 		IdMonedaInstrumento;
		public String 		IdMonedaOrden;
		public String 		IdMonedaLiquidacionOrden;
		public String 		NombreTipoOperacion;
		public String 		AccionTipoOperacion;
		public String		AccionOrden;
		public BigDecimal 	CantidadDisponibleOrden;
		public BigDecimal	CantidadAsignadaOrden;
		public BigDecimal	MontoDisponibleOrden;
		public BigDecimal	MontoAsignadoOrden;
		public Long 		SecOrdenMigracion;
		public BigDecimal	TipoCambio;
		public Long 		IdEmpleadoPortafolio;
		public String 		CodigoEmpleadoPortafolio;
		public String 		NombreEmpleadoPortafolio;
		public String 		NombreSucursalEmpleadoPortafolio;
		public String 		NombreSucursalEmpleadoDigitador;
		public Long 		IdEmpleadoDigitador;
		public String 		CodigoEmpleadoDigitador;
		public String 		NombreEmpleadoDigitador;
		public String 		CodigoClaseOrden;
		public String 		CodigoProcedenciaOrden;
		public String 		NombreProcedenciaOrden;
		public String 		NombreClaseOrden;
		public String 		CodigoCorredorCorresponsal;
		public Long 		IdPersonaAutorizadoDarOrdenes;
		public String 		NombreAutorizadorOrden;
		public String	 	EsOrdenEspecial;
		public String 		OrdenEspecialUI;
		public String 		EsConfirmada;
		public BigDecimal 	MontoOrdenPlazo;
		public BigDecimal	PrecioPlazo;
		public BigDecimal	MontoLiquidacionPlazo;
		public BigDecimal	MontoLocalPlazo;
		public BigDecimal	TasaPromedioReporte;
		public Long			BaseTasa;
		public String 		TipoInteres;
		public String 		IdSistemaBursatil;
		public String		EsPorMonto;
		public String		EsPorMontoUI;
		public String 		MercadoSMV;
		public String 		ModalidadSMV;
		public String		EsClienteFinal;
		public Long 		IdPersonaBroker;
		public String 		CodigoBrokerExtranjero;
		public String 		IdMedioDePago;
		public String 		NombreMedioDePago;
		public String		EsPutEsCall;
		public BigDecimal 	PrecioStrike;
		public String 		IdModalidadLiquidacion;
		public String 		ModalidadLiquidacion;
		public String 		IdModalidadEjercicio;
		public String 		ModalidadEjercicio;
		public String 		IdTipoOpcion;
		public String 		TipoOpcion;
		public String 		IdEstiloOpcion;
		public String 		EstiloOpcion;
		public String 		NombreBoveda;
		public Long 		IdCuentaCustodioPortafolio;
		public String 		IndicadorBancoCustodio;
		public String 		CodigoBancoCustodio;
		public String 		CodigoBoveda;
		public String 		CodigoBancoLiquidador;
		public String 		NombreModeloCustodia;
		public String 		NombreTipoCustodia;
		public String 		NombreCompletoBancoCustodio;
		public String 		NombreCompletoBancoLiquidador;
		public String 		Producto;
		public Integer 		IdProductoAgrupador;
		public BigDecimal 	AsignadoDia;
		public String		RazonSocialPortafolio;
		public BigDecimal	PrecioPromedioAsignacion;
		public String		TipoLiquidacion;
		public String		ModoFacturacion;
		public String 		InstrumentoNegociacion;
		public String		IdMonedaNegociacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
	}
	
	//Salida para la grilla
	public static class ConsultarOrdenes_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<Orden_Salida> P_OUT_ORDENES;
		
		public ConsultarOrdenes_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ORDENES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	public ConsultarOrdenes_Salida logica(Connection conn, ConsultarOrdenes_Entrada entrada) throws Exception{
		logger.info("Inicio de servicio ConsultarOrdenes");
		SQL _sql = new SQL();
		ConsultarOrdenes_Salida salida = new ConsultarOrdenes_Salida();
		List<Long> listIdPortafolio = new ArrayList<Long>();
		
		
		if(entrada.p_IdPortafolio != 0) 
		{
			listIdPortafolio.add(entrada.p_IdPortafolio);
			logger.debug("INSERT TABLA DE PASO");
			//se utiliza una tabla de paso para guardar idsPortafolio, optimiza consulta de Ordenes
			List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
			for (Long id : listIdPortafolio) {
				Portafolio portafolio = new Portafolio();
				portafolio.IdPortafolio = id;
				idsPortafolioTablaPaso.add(portafolio);
			}
			
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
				//se utiliza una tabla de paso para guardar idsPortafolio, optimiza consulta de Ordenes
				List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
				for (Long id : listIdPortafolio) {
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = id;
					idsPortafolioTablaPaso.add(portafolio);
				}
				
				_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
				
			}
			else {
				logger.debug("No restringe cartera");
				_sql.batch(conn, SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO, new ArrayList<>(), Portafolio.class);
				
			}
			
		}
		
		int varMercado = 0;
		int varTipoInstrumento = 0;
		int varOperacion = 0;
		int varInstrumento = 0;
		int varCondicionLiquidacion = 0;
		int varLibroOrdenes = 0;
		int varProducto = 0;
		int varIdTipoCanalIngreso = 0;
		int varIncluyeOrdenesNoVigentes = 0;
		int varOrdenesPorAsignar = 1;
		int varIdCliente = 0;
		int varMoneda = 0;

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
		
		if(entrada.p_LibroOrdenes == 0) {
			varLibroOrdenes = 1;
		}
		
		if(entrada.p_CondicionLiquidacion.equalsIgnoreCase("0")) {
			entrada.p_CondicionLiquidacion = "";
			varCondicionLiquidacion = 1;
		}

		if(entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("0")) {
			entrada.p_IdTipoCanalIngreso = "";
			varIdTipoCanalIngreso = 1;
		}
		
		if(entrada.p_IdProducto == 0) {
			varProducto = 1;
		}
		
		//Se modifica logica para que incluya las ordenes NO vigentes
		if("S".equals(entrada.p_IncluyeOrdenesVigentes)){
			entrada.p_IncluyeOrdenesVigentes = null;
			varIncluyeOrdenesNoVigentes = 1;
		}
		else {
			entrada.p_IncluyeOrdenesVigentes = "S";
		}
		
		if(entrada.p_OrdenesPorAsignar){
			entrada.p_CantidadDisponibleOrden = 0L;
			entrada.p_IdEstadoOrden = "OFE";
			varOrdenesPorAsignar = 0;
		}
			
		if(entrada.p_IdCliente == 0) {
			varIdCliente = 1;
		}
		
		if(entrada.p_Moneda == null ||  entrada.p_Moneda.equalsIgnoreCase("0")) {
			entrada.p_Moneda = "";
			varMoneda = 1;
		}

		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdClasificacionTipoInstrumento", entrada.p_Mercado);
		params.put("var_IdClasificacionTipoInstrumento", varMercado);
		params.put("p_IdTipoInstrumento", entrada.p_TipoInstrumento);
		params.put("var_IdTipoInstrumento", varTipoInstrumento);
		params.put("p_AccionOrden", entrada.p_Operacion);
		params.put("var_AccionOrden", varOperacion);
		params.put("p_IdInstrumentoUI", entrada.p_Instrumento);
		params.put("var_IdInstrumentoUI", varInstrumento);
		params.put("p_FechaOperacionOrdenI", entrada.p_FechaOperacionOrdenI);
		params.put("p_FechaOperacionOrdenF", entrada.p_FechaOperacionOrdenF);
		params.put("p_IdCondicionDeLiquidacion", entrada.p_CondicionLiquidacion);
		params.put("var_IdCondicionDeLiquidacion", varCondicionLiquidacion);
		params.put("p_IdLibroOrdenes", entrada.p_LibroOrdenes);
		params.put("var_IdLibroOrdenes", varLibroOrdenes);
		params.put("p_IdTipoCanalIngreso", entrada.p_IdTipoCanalIngreso);
		params.put("var_IdTipoCanalIngreso", varIdTipoCanalIngreso);
		params.put("p_IdProducto", entrada.p_IdProducto);
		params.put("var_IdProducto", varProducto);
		params.put("p_EsVentaCorta", entrada.p_EsVentaCorta);
		params.put("p_IncluyeOrdenesVigentes", entrada.p_IncluyeOrdenesVigentes);
		params.put("var_IncluyeOrdenesNoVigentes", varIncluyeOrdenesNoVigentes);
		params.put("p_CantidadDisponibleOrden", entrada.p_CantidadDisponibleOrden);
		params.put("p_IdEstadoOrden", entrada.p_IdEstadoOrden);
		params.put("var_OrdenesPorAsignar", varOrdenesPorAsignar);
		params.put("p_IdCliente", entrada.p_IdCliente);
		params.put("var_IdCliente", varIdCliente);
		params.put("p_Moneda", entrada.p_Moneda);
		params.put("varMoneda", varMoneda);
		
		List<Orden_Salida> ordenes = null;
		logger.debug("INICIO - Select ObtenerOrdenes");
		ordenes = _sql.query(conn, SQL_SELECT_ORDENES, params, Orden_Salida.class);
		logger.debug("FIN - Select ObtenerOrdenes"); 
		
		logger.debug("INICIO - Ciclo agregar Ordenes");
		if(ordenes != null && ordenes.size() > 0) {
			for(Orden_Salida orden : ordenes) {
				salida.P_OUT_ORDENES.add(orden);
			}
		}
		logger.debug("FIN - Ciclo agregar Ordenes");
		
		SQL.exec(conn, SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO);
		logger.info("Fin de servicio ConsultarOrdenes");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdCliente\":\"0\",\"p_IdPortafolio\":3654,\"p_IdProducto\":\"0\",\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":108,\"p_Mercado\":\"0\",\"p_TipoInstrumento\":\"0\",\"p_Operacion\":\"0\",\"p_Instrumento\":\"0\",\"p_FechaOperacionOrdenI\":\"2025-09-04T04:00:00.000Z\",\"p_FechaOperacionOrdenF\":\"2025-09-09T02:59:59.000Z\",\"p_LibroOrdenes\":\"0\",\"p_CondicionLiquidacion\":\"0\",\"p_Moneda\":\"0\",\"p_IdTipoCanalIngreso\":0,\"p_EsVentaCorta\":\"N\",\"p_IncluyeOrdenesVigentes\":\"N\",\"p_OrdenesPorAsignar\":false}";
			
			ConsultarOrdenes_Entrada Objin = gson.fromJson(json, ConsultarOrdenes_Entrada.class);
			ConsultarOrdenes demo = new ConsultarOrdenes();
			try 
			{
				ConsultarOrdenes_Salida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}
