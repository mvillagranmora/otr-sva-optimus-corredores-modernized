package cl.bcs.custodia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.EnvioOperacionesPendientes;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEnvioOperacionesPendientes implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearEnvioOperacionesPendientes.class);

	private final String SQL_INSERT_ENVIO_OPERACIONES_PENDIENTES = loadResourceToString(
			"cl/bcs/custodia/sql/EnvioOperacionesPendientes_InsertEnvioOperacionesPendientes.sql");

	private final String SQL_SELECT_IDGESTION_ENVIO_OPERACIONES_PENDIENTES = loadResourceToString(
			"cl/bcs/custodia/sql/EnvioOperacionesPendientes_SelectIdGestionEnvioOperacionesPendientes.sql");	

	public static class EntradaCrearEnvioOperacionesPendientes extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<EnvioOperacionesPendientes> P_ENVIAROPERACIONESPENDIENTES;

	}

	public static class SalidaCrearEnvioOperacionesPendientes extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<EnvioOperacionesPendientes> P_OUT_OPERACIONES;
		
		public SalidaCrearEnvioOperacionesPendientes() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_OPERACIONES = new ArrayList<EnvioOperacionesPendientes>();
			
		}
	}
	
	public static class IdGestion extends BcsPojo {
		public Integer idGestion; 
		
		public IdGestion() {
			idGestion = 1;
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearEnvioOperacionesPendientes jin = gson.fromJson(in.toString(),
				EntradaCrearEnvioOperacionesPendientes.class);
		SalidaCrearEnvioOperacionesPendientes out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearEnvioOperacionesPendientes logica(Connection conn, EntradaCrearEnvioOperacionesPendientes in)
			throws Exception {
		logger.info("INICIO::CrearEnvioOperacionesPendientes");
		long inicioServicio = System.currentTimeMillis();

		SalidaCrearEnvioOperacionesPendientes out = new SalidaCrearEnvioOperacionesPendientes();

		List<EnvioOperacionesPendientes> envioOperacionesPendientes = obtenerEnvioOperacionesPendientes(conn, in.P_ENVIAROPERACIONESPENDIENTES, in.p_IdEmpresa);

		insertarEnvioOperacionesPendientes(conn, envioOperacionesPendientes);

		out.P_OUT_OPERACIONES = envioOperacionesPendientes;

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearEnvioOperacionesPendientes", finServicio - inicioServicio);
		return out;
	}

	private List<EnvioOperacionesPendientes> obtenerEnvioOperacionesPendientes(Connection con,
			List<EnvioOperacionesPendientes> operacionesPendientes, Long idEmpresa) throws Exception {
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EnvioOperacionesPendientes",
				Long.valueOf(operacionesPendientes.size()));
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("FechaEjecucion", fechaSistema);
		
		SQL sql = new SQL();
		List<IdGestion> idGestionList = sql.query(con,SQL_SELECT_IDGESTION_ENVIO_OPERACIONES_PENDIENTES, parametros,IdGestion.class);
		IdGestion idGestion = idGestionList.stream().findFirst().orElse(new IdGestion());
				
		AtomicInteger index = new AtomicInteger();
		AtomicInteger IdGestionSec = new AtomicInteger(idGestion.idGestion);

		List<EnvioOperacionesPendientes> envioOperacionesPendientes = operacionesPendientes.parallelStream().map(op -> {
			Long idInterno = secuencias.get(index.getAndIncrement());
			Integer idGestionIncrement = IdGestionSec.getAndIncrement();
			EnvioOperacionesPendientes operacionesPendiente = obtenerEntidadEnvioOperacionesPendientes(op, idInterno, idEmpresa, idGestionIncrement );
			return operacionesPendiente;
		}).collect(Collectors.toList());

		return envioOperacionesPendientes;
	}

	private EnvioOperacionesPendientes obtenerEntidadEnvioOperacionesPendientes(EnvioOperacionesPendientes operacion,
			Long idInterno, Long idEmpresa, Integer idGestion) {
		logger.trace("INICIO::CrearEnvioOperacionesPendientes.obtenerEntidadEnvioOperacionesPendientes");
		long inicio = System.currentTimeMillis();
		EnvioOperacionesPendientes operacionPendiente = new EnvioOperacionesPendientes();
		operacionPendiente.IdInterno = idInterno;
		operacionPendiente.IdTipo = operacion.IdTipo;
		operacionPendiente.FolioOperacion = operacion.FolioOperacion;
		operacionPendiente.FechaIngresoRegistro = operacion.FechaIngresoRegistro;
		operacionPendiente.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionPendiente.TipoOperacion = operacion.TipoOperacion;
		operacionPendiente.NombreOperacion = operacion.NombreOperacion;
		operacionPendiente.IdTipoInstrumento = operacion.IdTipoInstrumento;
		operacionPendiente.Nemo = operacion.Nemo;
		operacionPendiente.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
		operacionPendiente.IdEmpresa = idEmpresa;
		operacionPendiente.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionPendiente.IdOperacion = operacion.IdOperacion;
		operacionPendiente.OpDirecto = operacion.OpDirecto;
		operacionPendiente.FechaMovimientoComprobante = operacion.FechaMovimientoComprobante;
		operacionPendiente.FechaVencimiento = operacion.FechaVencimiento;
		operacionPendiente.DocumentoIdentidad = operacion.DocumentoIdentidad;
		operacionPendiente.IdPortafolio = operacion.IdPortafolio;
		operacionPendiente.FolioComprobante = operacion.FolioComprobante;
		operacionPendiente.FolioComprobanteReferencia = operacion.FolioComprobanteReferencia;
		operacionPendiente.IdOrden = operacion.IdOrden;
		operacionPendiente.Cantidad = operacion.Cantidad;
		operacionPendiente.Precio = operacion.Precio;
		operacionPendiente.Tasa = operacion.Tasa;
		operacionPendiente.Monto = operacion.Monto;
		operacionPendiente.MontoMovimientoComprobante = operacion.MontoMovimientoComprobante;
		operacionPendiente.IdEstado = operacion.IdEstado;
		operacionPendiente.IdMoneda = operacion.IdMoneda;
		operacionPendiente.IdTipoGarantia = operacion.IdTipoGarantia;
		operacionPendiente.FondoAfp = operacion.FondoAfp;
		operacionPendiente.IdTipoComprobante = operacion.IdTipoComprobante;
		operacionPendiente.IdTipoProducto = operacion.IdTipoProducto;
		operacionPendiente.Producto = operacion.Producto;
		operacionPendiente.IdBolsa = operacion.IdBolsa;
		operacionPendiente.DocumentoIdentidadEntidad = operacion.DocumentoIdentidadEntidad;
		operacionPendiente.IndDcv = operacion.IndDcv;
		operacionPendiente.FolioTransaccion = operacion.FolioTransaccion;
		operacionPendiente.EstadoCclvDesc = operacion.EstadoCclvDesc;
		operacionPendiente.FechaEjecucion = operacion.FechaEjecucion;
		operacionPendiente.NemoSVS = operacion.NemoSVS;
		operacionPendiente.Categoria = operacion.Categoria;
		operacionPendiente.IdGestion = idGestion;

		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearEnvioOperacionesPendientes.obtenerEntidadEnvioOperacionesPendientes", fin - inicio);
		return operacionPendiente;
	}

	private void insertarEnvioOperacionesPendientes(Connection con,
			List<EnvioOperacionesPendientes> envioOperacionesPendientes) throws Exception {
		logger.debug("INICIO::CrearEnvioOperacionesPendientes.insertarEnvioOperacionesPendientes");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_ENVIO_OPERACIONES_PENDIENTES, envioOperacionesPendientes,
				EnvioOperacionesPendientes.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearEnvioOperacionesPendientes.insertarEnvioOperacionesPendientes", fin - inicio);
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"P_ENVIAROPERACIONESPENDIENTES\":[{\"FolioOperacion\":323232323,\"IdTipo\":\"ASIG\",\"FechaIngresoRegistro\":\"2021-09-30\",\"IdTipoOperacion\":\"COMPRA\",\"TipoOperacion\":\"C\",\"NombreOperacion\":\"FACTURA ELECTRONICA\",\"IdTipoInstrumento\":\"ACN\",\"Nemo\":\"BCI       \",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdCondicionLiquidacion\":\"T0\",\"IdOperacion\":\"C\",\"OpDirecto\":\"N\",\"FechaMovimientoComprobante\":\"1900-01-01\",\"FechaVencimiento\":\"2021-04-28\",\"DocumentoIdentidad\":\"15298053-1\",\"IdPortafolio\":1450341,\"FolioComprobante\":757683,\"FolioComprobanteReferencia\":0,\"IdOrden\":0,\"Cantidad\":2,\"Precio\":28400,\"Tasa\":3,\"Monto\":0,\"MontoMovimientoComprobante\":56800,\"IdEstado\":\"P\",\"IdAgente\":\"0\",\"IdMoneda\":\"CLP\",\"IdTipoGarantia\":\"00\",\"FondoAfp\":\"\",\"IdTipoComprobante\":\"\",\"IdTipoProducto\":\"\",\"Producto\":\"SIMULTANEA\",\"IdBolsa\":\"BCS\",\"DocumentoIdentidadEntidad\":\"F\",\"IndDcv\":\"\",\"FolioTransaccion\":0,\"EstadoCclvDesc\":\"\",\"FechaEjecucion\":\"2021-04-14\",\"NemoSVS\":\"\",\"Categoria\":\"RV\"},{\"FolioOperacion\":78788778,\"IdTipo\":\"ASIG\",\"FechaIngresoRegistro\":\"2021-09-30\",\"IdTipoOperacion\":\"COMPRA\",\"TipoOperacion\":\"C\",\"NombreOperacion\":\"FACTURA ELECTRONICA\",\"IdTipoInstrumento\":\"ACN\",\"Nemo\":\"CENCOSUD  \",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdCondicionLiquidacion\":\"T0\",\"IdOperacion\":\"C\",\"OpDirecto\":\"N\",\"FechaMovimientoComprobante\":\"1900-01-01\",\"FechaVencimiento\":\"2021-04-28\",\"DocumentoIdentidad\":\"15298053-1\",\"IdPortafolio\":1450341,\"FolioComprobante\":757684,\"FolioComprobanteReferencia\":0,\"IdOrden\":0,\"Cantidad\":20,\"Precio\":1009,\"Tasa\":3,\"Monto\":0,\"MontoMovimientoComprobante\":20180,\"IdEstado\":\"P\",\"IdAgente\":\"0\",\"IdMoneda\":\"CLP\",\"IdTipoGarantia\":\"00\",\"FondoAfp\":\"\",\"IdTipoComprobante\":\"\",\"IdTipoProducto\":\"\",\"Producto\":\"SIMULTANEA\",\"IdBolsa\":\"BCS\",\"DocumentoIdentidadEntidad\":\"F\",\"IndDcv\":\"\",\"FolioTransaccion\":0,\"EstadoCclvDesc\":\"\",\"FechaEjecucion\":\"2021-04-14\",\"NemoSVS\":\"\",\"Categoria\":\"RV\"},{\"FolioOperacion\":3232312,\"IdTipo\":\"ASIG\",\"FechaIngresoRegistro\":\"2021-09-30\",\"IdTipoOperacion\":\"COMPRA\",\"TipoOperacion\":\"C\",\"NombreOperacion\":\"FACTURA ELECTRONICA\",\"IdTipoInstrumento\":\"ETN\",\"Nemo\":\"CFMDIVO\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdCondicionLiquidacion\":\"T0\",\"IdOperacion\":\"C\",\"OpDirecto\":\"N\",\"FechaMovimientoComprobante\":\"1900-01-01\",\"FechaVencimiento\":\"2021-04-28\",\"DocumentoIdentidad\":\"15298053-1\",\"IdPortafolio\":1450341,\"FolioComprobante\":757676,\"FolioComprobanteReferencia\":0,\"IdOrden\":0,\"Cantidad\":20,\"Precio\":1132.8,\"Tasa\":3,\"Monto\":0,\"MontoMovimientoComprobante\":22656,\"IdEstado\":\"P\",\"IdAgente\":\"0\",\"IdMoneda\":\"CLP\",\"IdTipoGarantia\":\"00\",\"FondoAfp\":\"\",\"IdTipoComprobante\":\"\",\"IdTipoProducto\":\"\",\"Producto\":\"SIMULTANEA\",\"IdBolsa\":\"BCS\",\"DocumentoIdentidadEntidad\":\"F\",\"IndDcv\":\"\",\"FolioTransaccion\":0,\"EstadoCclvDesc\":\"\",\"FechaEjecucion\":\"2021-04-14\",\"NemoSVS\":\"\",\"Categoria\":\"RV\"},{\"FolioOperacion\":5926,\"IdTipo\":\"GTIA\",\"FechaIngresoRegistro\":\"2021-09-30\",\"IdTipoOperacion\":\"\",\"TipoOperacion\":\"I\",\"NombreOperacion\":\"INGRESO GARANTIA\",\"IdTipoInstrumento\":\"ACN\",\"Nemo\":\"CENCOSUD  \",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdCondicionLiquidacion\":\"T0\",\"IdOperacion\":\"I\",\"OpDirecto\":\"N\",\"FechaMovimientoComprobante\":\"1900-01-01\",\"FechaVencimiento\":\"0001-01-01\",\"DocumentoIdentidad\":\"15298053-1\",\"IdPortafolio\":1450341,\"FolioComprobante\":757684,\"FolioComprobanteReferencia\":0,\"IdOrden\":0,\"Cantidad\":20,\"Precio\":1009,\"Tasa\":3,\"Monto\":20180,\"MontoMovimientoComprobante\":0,\"IdEstado\":\"P\",\"IdAgente\":\"11556\",\"IdMoneda\":\"CLP\",\"IdTipoGarantia\":\"08\",\"FondoAfp\":\"\",\"IdTipoComprobante\":\"\",\"IdTipoProducto\":\"INSI\",\"Producto\":\"SIMULTANEA\",\"IdBolsa\":\"BCS\",\"DocumentoIdentidadEntidad\":\"F\",\"IndDcv\":\"\",\"FolioTransaccion\":0,\"EstadoCclvDesc\":\"\",\"FechaEjecucion\":\"2021-04-14\",\"NemoSVS\":\"CENCOSUD  \",\"Categoria\":\"AC\"}]}\r\n";
			
			EntradaCrearEnvioOperacionesPendientes entradaCrearEnvioOperacionesPendientes = gson.fromJson(json,
					EntradaCrearEnvioOperacionesPendientes.class);
			CrearEnvioOperacionesPendientes crearEnvioOperacionesPendientes = new CrearEnvioOperacionesPendientes();
			System.out.println("ENTRADA:");
			System.out.println(crearEnvioOperacionesPendientes.toString());
			try {
				SalidaCrearEnvioOperacionesPendientes salidaCrearEnvioOperacionesPendientes = crearEnvioOperacionesPendientes
						.logica(con, entradaCrearEnvioOperacionesPendientes);
				// con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA:");
				System.out.println(salidaCrearEnvioOperacionesPendientes.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}

}
