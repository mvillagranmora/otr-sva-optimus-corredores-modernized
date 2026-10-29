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

public class ConsultarMatrizOperacionalPortafolioExcel implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarMatrizOperacionalPortafolioExcel.class);

	private final String SQL_SELECT_PLANTILLA_COBROS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolioExcel_Select_PlantillaCobrosBase.sql");
	private final String SQL_SELECT_PLANTILLA_COBROS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolioExcel_Select_PlantillaCobrosExtra.sql");
	private final String SQL_SELECT_PLANTILLA_PRODUCTOS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolioExcel_Select_PlantillaProductosBase.sql");
	private final String SQL_SELECT_PLANTILLA_PRODUCTOS_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacionalPortafolioExcel_Select_PlantillaProductosExtra.sql");

	public static class EntradaConsultarMatrizOperacionalPortafolioExcel {
		public Long p_IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MatrizOperacionalPortafolio {
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String IdPortafolio;
		public String IdBolsa;
		public String IdCategoriaInversionista;
		public String IdTipoProducto;
		public String TipoProducto;
		public String EsExtraCobros;
		public String EsExtraProductos;
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

	public static class SalidaConsultarMatrizOperacionalPortafolioExcel {
		public List<MatrizOperacionalPortafolio> P_OUT_MATRIZOPERACIONAL;

		public SalidaConsultarMatrizOperacionalPortafolioExcel() {
			this.P_OUT_MATRIZOPERACIONAL = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PlantillaCobrosPortafolio {
		public String DescripcionPerfilComercialBase;
		public String IdPortafolio;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
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
		public String EsPersonalizado;
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
		public String IdClasificacionTipoInstrumento;
		public String EsExtra;
	}

	public static class PlantillaProductosPortafolio {
		public String IdPortafolio;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String IdPerfilProductos;
		public String DescripcionPerfilProductos;
		public String IdCategoriaInversionista;
		public String FechaCreacion;
		public String FechaModificacion;
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
		public String EsExtra;
	}

	public SalidaConsultarMatrizOperacionalPortafolioExcel logica(Connection conn, EntradaConsultarMatrizOperacionalPortafolioExcel in) throws Exception {
		logger.info("INIT ConsultarMatrizOperacionalPortafolioExcel");
		SalidaConsultarMatrizOperacionalPortafolioExcel salida = new SalidaConsultarMatrizOperacionalPortafolioExcel();
		SQL sql = new SQL();

		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);

			// Obtener plantillas de cobros (BASE y EXTRA)
			logger.debug("SELECT plantilla cobros base");
			List<PlantillaCobrosPortafolio> cobrosBase = sql.query(conn, SQL_SELECT_PLANTILLA_COBROS_BASE, params, PlantillaCobrosPortafolio.class);

			logger.debug("SELECT plantilla cobros extra");
			List<PlantillaCobrosPortafolio> cobrosExtra = sql.query(conn, SQL_SELECT_PLANTILLA_COBROS_EXTRA, params, PlantillaCobrosPortafolio.class);

			// Consolidar plantilla de cobros (FULL JOIN lógico)
			List<PlantillaCobrosPortafolio> cobrosConsolidada = consolidarPlantillaCobros(cobrosBase, cobrosExtra);

			// Obtener plantillas de productos (BASE y EXTRA)
			logger.debug("SELECT plantilla productos base");
			List<PlantillaProductosPortafolio> productosBase = sql.query(conn, SQL_SELECT_PLANTILLA_PRODUCTOS_BASE, params, PlantillaProductosPortafolio.class);

			logger.debug("SELECT plantilla productos extra");
			List<PlantillaProductosPortafolio> productosExtra = sql.query(conn, SQL_SELECT_PLANTILLA_PRODUCTOS_EXTRA, params, PlantillaProductosPortafolio.class);

			// Consolidar plantilla de productos (FULL JOIN lógico)
			List<PlantillaProductosPortafolio> productosConsolidada = consolidarPlantillaProductos(productosBase, productosExtra);

			// Generar intersección de matrices (INNER JOIN)
			logger.debug("Generar matriz operacional portafolio");
			salida.P_OUT_MATRIZOPERACIONAL = generarMatrizOperacional(cobrosConsolidada, productosConsolidada);

		} catch (Exception ex) {
			logger.error("Error en ConsultarMatrizOperacionalPortafolioExcel: " + ex.getMessage(), ex);
			throw ex;
		}

		logger.info("END ConsultarMatrizOperacionalPortafolioExcel");
		return salida;
	}

	private List<PlantillaCobrosPortafolio> consolidarPlantillaCobros(List<PlantillaCobrosPortafolio> base, List<PlantillaCobrosPortafolio> extra) {
		List<PlantillaCobrosPortafolio> consolidada = new ArrayList<>();

		// Agregar todos los de BASE
		if (base != null) {
			consolidada.addAll(base);
		}

		// Agregar EXTRA solo si no existe matching en BASE
		if (extra != null) {
			for (PlantillaCobrosPortafolio e : extra) {
				boolean found = false;
				if (base != null) {
					for (PlantillaCobrosPortafolio b : base) {
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

	private boolean matchCobros(PlantillaCobrosPortafolio a, PlantillaCobrosPortafolio b) {
		return equals(a.IdBolsa, b.IdBolsa) &&
			   equals(a.IdPortafolio, b.IdPortafolio) &&
			   equals(a.IdTipoProducto, b.IdTipoProducto) &&
			   equals(a.IdTipoOperacion, b.IdTipoOperacion) &&
			   equals(a.IdCanalIngreso, b.IdCanalIngreso) &&
			   equals(a.CodigoCobroClasificacion, b.CodigoCobroClasificacion);
	}

	private List<PlantillaProductosPortafolio> consolidarPlantillaProductos(List<PlantillaProductosPortafolio> base, List<PlantillaProductosPortafolio> extra) {
		List<PlantillaProductosPortafolio> consolidada = new ArrayList<>();

		// Agregar todos los de BASE
		if (base != null) {
			consolidada.addAll(base);
		}

		// Agregar EXTRA solo si no existe matching en BASE
		if (extra != null) {
			for (PlantillaProductosPortafolio e : extra) {
				boolean found = false;
				if (base != null) {
					for (PlantillaProductosPortafolio b : base) {
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

	private boolean matchProductos(PlantillaProductosPortafolio a, PlantillaProductosPortafolio b) {
		return equals(a.IdPortafolio, b.IdPortafolio) &&
			   equals(a.IdTipoProducto, b.IdTipoProducto) &&
			   equals(a.IdTipoOperacion, b.IdTipoOperacion) &&
			   equals(a.IdCanalIngreso, b.IdCanalIngreso);
	}

	private List<MatrizOperacionalPortafolio> generarMatrizOperacional(List<PlantillaCobrosPortafolio> cobros, List<PlantillaProductosPortafolio> productos) {
		List<MatrizOperacionalPortafolio> matriz = new ArrayList<>();

		for (PlantillaCobrosPortafolio cobro : cobros) {
			for (PlantillaProductosPortafolio producto : productos) {
				// Validar condiciones de JOIN
				if (!equals(cobro.IdPortafolio, producto.IdPortafolio)) {
					continue;
				}

				if (!equals(cobro.IdTipoProducto, producto.IdTipoProducto)) {
					continue;
				}

				// Validar IdTipoOperacion con lógica de comodín '0'
				boolean tipoOperacionMatch = equals(cobro.IdTipoOperacion, producto.IdTipoOperacion) ||
					("0".equals(cobro.IdTipoOperacion) && !isEmpty(producto.IdTipoOperacion)) ||
					("0".equals(producto.IdTipoOperacion) && !isEmpty(cobro.IdTipoOperacion));

				if (!tipoOperacionMatch) {
					continue;
				}

				// Validar IdCanalIngreso con lógica de comodín '0'
				boolean canalIngresoMatch = equals(cobro.IdCanalIngreso, producto.IdCanalIngreso) ||
					("0".equals(cobro.IdCanalIngreso) && !isEmpty(producto.IdCanalIngreso)) ||
					("0".equals(producto.IdCanalIngreso) && !isEmpty(cobro.IdCanalIngreso));

				if (!canalIngresoMatch) {
					continue;
				}

				// Crear registro de matriz
				MatrizOperacionalPortafolio m = new MatrizOperacionalPortafolio();
				m.CodigoPortafolio = producto.CodigoPortafolio;
				m.NombreCompleto = producto.NombreCompleto;
				m.DocumentoIdentidad = producto.DocumentoIdentidad;
				m.IdPortafolio = producto.IdPortafolio;
				m.IdBolsa = cobro.IdBolsa;
				m.IdCategoriaInversionista = producto.IdCategoriaInversionista;
				m.IdTipoProducto = producto.IdTipoProducto;
				m.TipoProducto = producto.TipoProducto;
				m.EsExtraCobros = cobro.EsExtra;
				m.EsExtraProductos = producto.EsExtra;

				// Determinar IdTipoOperacion según lógica CASE
				if (equals(producto.IdTipoOperacion, cobro.IdTipoOperacion)) {
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
				if (equals(producto.IdCanalIngreso, cobro.IdCanalIngreso)) {
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
		EntradaConsultarMatrizOperacionalPortafolioExcel in = gson.fromJson(jin.toString(), EntradaConsultarMatrizOperacionalPortafolioExcel.class);
		SalidaConsultarMatrizOperacionalPortafolioExcel out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1}";
			EntradaConsultarMatrizOperacionalPortafolioExcel entrada = gson.fromJson(json, EntradaConsultarMatrizOperacionalPortafolioExcel.class);
			ConsultarMatrizOperacionalPortafolioExcel demo = new ConsultarMatrizOperacionalPortafolioExcel();
			SalidaConsultarMatrizOperacionalPortafolioExcel salida = demo.logica(conn, entrada);
			System.out.println("Registros: " + salida.P_OUT_MATRIZOPERACIONAL.size());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
