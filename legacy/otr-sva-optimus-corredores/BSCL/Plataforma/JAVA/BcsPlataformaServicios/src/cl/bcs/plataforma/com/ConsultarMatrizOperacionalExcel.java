package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMatrizOperacionalExcel implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarMatrizOperacionalExcel.class);

	private final String SQL_SELECT_PLANTILLA_COBROS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaCobrosBase.sql");
	private final String SQL_SELECT_PLANTILLA_COBROS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaCobrosExtra.sql");
	private final String SQL_SELECT_PLANTILLA_PRODUCTOS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaProductosBase.sql");
	private final String SQL_SELECT_PLANTILLA_PRODUCTOS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalExcel_Select_PlantillaProductosExtra.sql");

	public static class EntradaConsultarMatrizOperacionalExcel {
		public Long p_IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MatrizOperacional {
		public String DescripcionPerfilComercialBase;
		public String IdBolsa;
		public String IdCategoriaInversionista;
		public String IdTipoProducto;
		public String TipoProducto;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public String IdCanalIngreso;
		public String CanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public Long Prioridad;
		public Long DiasPlazo;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String CodigoCobroClasificacion;
		public Long IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaConsultarMatrizOperacionalExcel {
		public List<MatrizOperacional> P_OUT_MATRIZOPERACIONAL;

		public SalidaConsultarMatrizOperacionalExcel() {
			this.P_OUT_MATRIZOPERACIONAL = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PlantillaCobros {
		public String DescripcionPerfilComercialBase;
		public Long IdInternal;
		public Long Prioridad;
		public String IdPerfilCobros;
		public String DescripcionPerfilCobros;
		public Long IdEmpresa;
		public String EstadoPerfilCobros;
		public String NombreEstadoPerfilCobros;
		public String EsPerfilPersonalizado;
		public Long IdDetallePerfilCobros;
		public String IdBolsa;
		public String IdTipoProducto;
		public String TipoProducto;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public String IdCanalIngreso;
		public String CanalIngreso;
		public Long DiasPlazo;
		public String EstadoDetalle;
		public String NombreEstadoDetalle;
		public String IdValorCobro;
		public String DescripcionValorCobro;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String ValorCobroTexto;
		public BigDecimal ValorTopeMinimoOrden;
		public String ValorTopeMinimoOrdenTexto;
		public String IdMoneda;
		public String EstadoValorCobro;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String FormaCobro;
		public String PermiteCambiarValor;
		public String CodigoCobroClasificacion;
		public Integer CobroCalculado;
	}

	public static class PlantillaProductos {
		public String DescripcionPerfilComercialBase;
		public String IdPerfilProductos;
		public String DescripcionPerfilProductos;
		public String IdCategoriaInversionista;
		public java.sql.Date FechaCreacion;
		public java.sql.Date FechaModificacion;
		public String EstadoPerfilProductos;
		public String PorDefecto;
		public Long IdEmpresa;
		public String EsPerfilPersonalizado;
		public String IdTipoProducto;
		public String TipoProducto;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public String IdCanalIngreso;
		public String CanalIngreso;
		public String IdMoneda;
		public String IdGrupoActivos;
		public String EstadoProducto;
		public String NombreEstadoProducto;
	}

	public SalidaConsultarMatrizOperacionalExcel logica(Connection conn, EntradaConsultarMatrizOperacionalExcel in) throws Exception {
		logger.info("INIT ConsultarMatrizOperacionalExcel");
		SalidaConsultarMatrizOperacionalExcel salida = new SalidaConsultarMatrizOperacionalExcel();
		SQL sql = new SQL();

		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);

			// Obtener plantillas de cobros (BASE y EXTRA)
			logger.debug("SELECT plantilla cobros base");
			List<PlantillaCobros> cobrosBase = sql.query(conn, SQL_SELECT_PLANTILLA_COBROS_BASE, params, PlantillaCobros.class);

			logger.debug("SELECT plantilla cobros extra");
			List<PlantillaCobros> cobrosExtra = sql.query(conn, SQL_SELECT_PLANTILLA_COBROS_EXTRA, params, PlantillaCobros.class);

			// Consolidar plantilla de cobros (FULL JOIN lógico)
			List<PlantillaCobros> cobrosConsolidada = consolidarPlantillaCobros(cobrosBase, cobrosExtra);

			// Obtener plantillas de productos (BASE y EXTRA)
			logger.debug("SELECT plantilla productos base");
			List<PlantillaProductos> productosBase = sql.query(conn, SQL_SELECT_PLANTILLA_PRODUCTOS_BASE, params, PlantillaProductos.class);

			logger.debug("SELECT plantilla productos extra");
			List<PlantillaProductos> productosExtra = sql.query(conn, SQL_SELECT_PLANTILLA_PRODUCTOS_EXTRA, params, PlantillaProductos.class);

			// Consolidar plantilla de productos (FULL JOIN lógico)
			List<PlantillaProductos> productosConsolidada = consolidarPlantillaProductos(productosBase, productosExtra);

			// Generar intersección de matrices (INNER JOIN)
			logger.debug("Generar matriz operacional");
			salida.P_OUT_MATRIZOPERACIONAL = generarMatrizOperacional(cobrosConsolidada, productosConsolidada);

		} catch (Exception ex) {
			logger.error("Error en ConsultarMatrizOperacionalExcel: " + ex.getMessage(), ex);
			throw ex;
		}

		logger.info("END ConsultarMatrizOperacionalExcel");
		return salida;
	}

	private List<PlantillaCobros> consolidarPlantillaCobros(List<PlantillaCobros> base, List<PlantillaCobros> extra) {
		List<PlantillaCobros> consolidada = new ArrayList<>();

		// Agregar todos los de BASE
		if (base != null) {
			consolidada.addAll(base);
		}

		// Agregar EXTRA solo si no existe matching en BASE
		if (extra != null) {
			for (PlantillaCobros e : extra) {
				boolean found = false;
				if (base != null) {
					for (PlantillaCobros b : base) {
						if (matchCobros(e, b)) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					consolidada.add(e);
				}
			}
		}

		return consolidada;
	}

	private boolean matchCobros(PlantillaCobros a, PlantillaCobros b) {
		return equals(a.IdBolsa, b.IdBolsa) &&
			   equals(a.IdTipoProducto, b.IdTipoProducto) &&
			   equals(a.IdTipoOperacion, b.IdTipoOperacion) &&
			   equals(a.IdCanalIngreso, b.IdCanalIngreso) &&
			   equals(a.CodigoCobroClasificacion, b.CodigoCobroClasificacion);
	}

	private List<PlantillaProductos> consolidarPlantillaProductos(List<PlantillaProductos> base, List<PlantillaProductos> extra) {
		List<PlantillaProductos> consolidada = new ArrayList<>();

		// Agregar todos los de BASE
		if (base != null) {
			consolidada.addAll(base);
		}

		// Agregar EXTRA solo si no existe matching en BASE
		if (extra != null) {
			for (PlantillaProductos e : extra) {
				boolean found = false;
				if (base != null) {
					for (PlantillaProductos b : base) {
						if (matchProductos(e, b)) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					consolidada.add(e);
				}
			}
		}

		return consolidada;
	}

	private boolean matchProductos(PlantillaProductos a, PlantillaProductos b) {
		return equals(a.IdTipoProducto, b.IdTipoProducto) &&
			   equals(a.IdTipoOperacion, b.IdTipoOperacion) &&
			   equals(a.IdCanalIngreso, b.IdCanalIngreso);
	}

	private List<MatrizOperacional> generarMatrizOperacional(List<PlantillaCobros> cobros, List<PlantillaProductos> productos) {
		List<MatrizOperacional> matriz = new ArrayList<>();

		for (PlantillaCobros cobro : cobros) {
			for (PlantillaProductos producto : productos) {
				// Validar condiciones de JOIN
				if (!cobro.IdTipoProducto.equals(producto.IdTipoProducto)) {
					continue;
				}

				if (!cobro.DescripcionPerfilComercialBase.equals(producto.DescripcionPerfilComercialBase)) {
					continue;
				}

				// Validar IdTipoOperacion con lógica de comodín '0'
				boolean tipoOperacionMatch = cobro.IdTipoOperacion.equals(producto.IdTipoOperacion) ||
					("0".equals(cobro.IdTipoOperacion) && !isEmpty(producto.IdTipoOperacion)) ||
					("0".equals(producto.IdTipoOperacion) && !isEmpty(cobro.IdTipoOperacion));

				if (!tipoOperacionMatch) {
					continue;
				}

				// Validar IdCanalIngreso con lógica de comodín '0'
				boolean canalIngresoMatch = cobro.IdCanalIngreso.equals(producto.IdCanalIngreso) ||
					("0".equals(cobro.IdCanalIngreso) && !isEmpty(producto.IdCanalIngreso)) ||
					("0".equals(producto.IdCanalIngreso) && !isEmpty(cobro.IdCanalIngreso));

				if (!canalIngresoMatch) {
					continue;
				}

				// Crear registro de matriz
				MatrizOperacional m = new MatrizOperacional();
				m.DescripcionPerfilComercialBase = cobro.DescripcionPerfilComercialBase;
				m.IdBolsa = cobro.IdBolsa;
				m.IdCategoriaInversionista = producto.IdCategoriaInversionista;
				m.IdTipoProducto = producto.IdTipoProducto;
				m.TipoProducto = producto.TipoProducto;

				// Determinar IdTipoOperacion según lógica CASE
				if (producto.IdTipoOperacion.equals(cobro.IdTipoOperacion)) {
					m.IdTipoOperacion = producto.IdTipoOperacion;
					m.TipoOperacion = producto.TipoOperacion;
				} else if ("0".equals(producto.IdTipoOperacion)) {
					m.IdTipoOperacion = cobro.IdTipoOperacion;
					m.TipoOperacion = cobro.TipoOperacion;
				} else {
					m.IdTipoOperacion = producto.IdTipoOperacion;
					m.TipoOperacion = producto.TipoOperacion;
				}

				// Determinar IdCanalIngreso según lógica CASE
				if (producto.IdCanalIngreso.equals(cobro.IdCanalIngreso)) {
					m.IdCanalIngreso = producto.IdCanalIngreso;
					m.CanalIngreso = producto.CanalIngreso;
				} else if ("0".equals(producto.IdCanalIngreso)) {
					m.IdCanalIngreso = cobro.IdCanalIngreso;
					m.CanalIngreso = cobro.CanalIngreso;
				} else {
					m.IdCanalIngreso = producto.IdCanalIngreso;
					m.CanalIngreso = producto.CanalIngreso;
				}

				m.IdCobro = cobro.IdCobro;
				m.ValorCobro = cobro.ValorCobro;
				m.FormaCobro = cobro.FormaCobro;
				m.ValorTopeMinimoOrden = cobro.ValorTopeMinimoOrden;
				m.IdMoneda = cobro.IdMoneda;
				m.Prioridad = cobro.Prioridad;
				m.DiasPlazo = cobro.DiasPlazo;
				m.NombreCobro = cobro.NombreCobro;
				m.IdCobroClasificacion = cobro.IdCobroClasificacion;
				m.CodigoCobroClasificacion = cobro.CodigoCobroClasificacion;
				m.IdEmpresa = producto.IdEmpresa;

				matriz.add(m);
			}
		}

		return matriz;
	}

	private boolean equals(String a, String b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return a.equals(b);
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarMatrizOperacionalExcel in = gson.fromJson(jin.toString(), EntradaConsultarMatrizOperacionalExcel.class);
		SalidaConsultarMatrizOperacionalExcel out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1}";
			EntradaConsultarMatrizOperacionalExcel entrada = gson.fromJson(json, EntradaConsultarMatrizOperacionalExcel.class);
			ConsultarMatrizOperacionalExcel demo = new ConsultarMatrizOperacionalExcel();
			SalidaConsultarMatrizOperacionalExcel salida = demo.logica(conn, entrada);
			System.out.println("Registros: " + salida.P_OUT_MATRIZOPERACIONAL.size());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
