package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearValorCobro implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearValorCobro.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearValorCobro_Count_Existe.sql");
	private final String SQL_INSERT_VALOR = loadResourceToString("cl/bcs/plataforma/com/sql/CrearValorCobro_Insert_Valor.sql");

	public static class EntradaCrearValorCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdValorCobro;
		public String p_DescripcionValorCobro;
		public String p_IdTipoProducto;
		public String p_IdCobro;
		public BigDecimal p_ValorCobro;
		public BigDecimal p_ValorTopeMinimoOrden;
		public String p_IdMoneda;
		public String p_IdEstado;
	}

	public static class ErrorDetalle extends BcsPojo {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaCrearValorCobro extends BcsPojo {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearValorCobro() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class Contador {
		public Long Contador;
	}

	public static class InsertValor extends BcsPojo {
		public String IdValorCobro;
		public String DescripcionValorCobro;
		public String IdTipoProducto;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String FechaCreacion;
		public String IdEstado;
		public Long IdEmpresa;
	}

	public SalidaCrearValorCobro logica(Connection conn, EntradaCrearValorCobro in) throws Exception {
		logger.debug("INICIO::CrearValorCobro.logica");
		long inicio = System.currentTimeMillis();
		SalidaCrearValorCobro salida = new SalidaCrearValorCobro();
		SQL sql = new SQL();

		try {
			// Validar que no exista
			logger.debug("Validar que el valor de cobro no exista");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdValorCobro", in.p_IdValorCobro);

			List<Contador> countList = sql.query(conn, SQL_COUNT_EXISTE, params, Contador.class);
			Long v_existe = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;

			if (v_existe > 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR", "CODIGO_COBRO_YA_EXISTE"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());

			// Insertar valor de cobro
			logger.debug("INSERT valor cobro");
			InsertValor insertValor = new InsertValor();
			insertValor.IdValorCobro = in.p_IdValorCobro;
			insertValor.DescripcionValorCobro = in.p_DescripcionValorCobro;
			insertValor.IdTipoProducto = in.p_IdTipoProducto;
			insertValor.IdCobro = in.p_IdCobro;
			insertValor.ValorCobro = in.p_ValorCobro;
			insertValor.ValorTopeMinimoOrden = in.p_ValorTopeMinimoOrden;
			insertValor.IdMoneda = in.p_IdMoneda;
			insertValor.FechaCreacion = v_FechaHoraSistema;
			insertValor.IdEstado = in.p_IdEstado;
			insertValor.IdEmpresa = in.p_IdEmpresa;

			sql.batch(conn, SQL_INSERT_VALOR, java.util.Arrays.asList(insertValor), InsertValor.class);
			agregarLog(conn, in, insertValor);
			
			salida.P_OUT_REFID = 1L;

		} catch (Exception ex) {
			logger.error("Error en CrearValorCobro: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_VALOR_COBRO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearValorCobro.logica {} ms", fin - inicio);
		return salida;
	}
	
	private void agregarLog(Connection con, EntradaCrearValorCobro entrada, InsertValor valor) throws Exception {
	    logger.debug("INICIO::CrearValorCobro.agregarLog");
	    long inicio = System.currentTimeMillis();

	    AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

	    AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
	    log.IdCabeceraLog = entrada.p_IdUsuario;
	    log.IdTipoLog = "INGRESO";
	    log.IdModulo = entrada.p_IdModulo;
	    log.IdUsuario = entrada.p_IdUsuario;
	    log.IdEmpresa = entrada.p_IdEmpresa;
	    log.FechaSistemaLog = fechaSistema;
	    log.IdUsuarioAtendido = entrada.p_IdUsuario;
	    log.IdRegistro = entrada.p_IdValorCobro;
	    log.IdTipoRegistro = "PLANTILLA_COBROS";
	    log.EstadoRegistro = "";
	    log.IdProceso = 0L;
	    log.EstadoProceso = "";
	    log.NombreProceso = "Crear valor de cobros";

	    entradaAgregarLog.p_Log = Collections.singletonList(log);

	    List<AgregarLog.InAgregarLog.DetalleLog> detalles = new ArrayList<>();

	    detalles.add(crearDetalle(entrada, "IdValorCobro", valor.IdValorCobro));
	    detalles.add(crearDetalle(entrada, "DescripcionValorCobro", valor.DescripcionValorCobro));
	    detalles.add(crearDetalle(entrada, "IdTipoProducto", valor.IdTipoProducto));
	    detalles.add(crearDetalle(entrada, "IdCobro", valor.IdCobro));
	    detalles.add(crearDetalle(entrada, "ValorCobro", valor.ValorCobro.toString()));
	    detalles.add(crearDetalle(entrada, "ValorTopeMinimoOrden", MonedaFormato.formatear(con, valor.ValorTopeMinimoOrden, MonedaFormato.MONTO, valor.IdMoneda, "", "").valorConFormato));
	    detalles.add(crearDetalle(entrada, "IdMoneda", valor.IdMoneda));
	    detalles.add(crearDetalle(entrada, "FechaCreacion", valor.FechaCreacion));
	    detalles.add(crearDetalle(entrada, "IdEstado", valor.IdEstado));

	    entradaAgregarLog.p_LogDetalle = detalles;

	    AgregarLog agregarLogService = new AgregarLog();
	    agregarLogService.logica(con, entradaAgregarLog);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearValorCobro.agregarLog {} ms", fin - inicio);
	}
	
	private AgregarLog.InAgregarLog.DetalleLog crearDetalle(EntradaCrearValorCobro entrada,String campo, Object valorNuevo) {
	    AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
	    detalle.IdTrabajo = 0L;
	    detalle.IdCabeceraLog = entrada.p_IdUsuario;
	    detalle.SubRegistro = entrada.p_IdValorCobro;
	    detalle.CampoRegistro = campo;
	    detalle.ValorAntiguo = "";
	    detalle.ValorNuevo = valorNuevo == null ? "" : valorNuevo.toString();

	    return detalle;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearValorCobro in = gson.fromJson(jin.toString(), EntradaCrearValorCobro.class);
		SalidaCrearValorCobro out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"COM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdValorCobro\":\"VC001\",\"p_DescripcionValorCobro\":\"Valor Test\",\"p_IdTipoProducto\":\"ACC\",\"p_IdCobro\":\"COM\",\"p_ValorCobro\":1000.00,\"p_ValorTopeMinimoOrden\":500.00,\"p_IdMoneda\":\"CLP\",\"p_IdEstado\":\"ACT\"}";
			EntradaCrearValorCobro entrada = gson.fromJson(json, EntradaCrearValorCobro.class);
			CrearValorCobro demo = new CrearValorCobro();
			SalidaCrearValorCobro salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
