package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
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

public class CrearPerfilProductos implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfilProductos.class);

	private final String SQL_INSERT_PERFIL = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProductos_Insert_Perfil.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProductos_Insert_Detalle.sql");

	public static class DetallePerfil {
		public String p_IdTipoProducto;
		public String p_IdTipoOperacion;
		public String p_IdCanalIngreso;
		public String p_IdMoneda;
		public String p_IdEstado;
		public String p_IdGrupoActivos;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaCrearPerfilProductos {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilProductos;
		public String p_DescripcionPerfilProductos;
		public String p_IdCategoriaInversionista;
		public String p_EsPerfilPersonalizado;
		public String p_IdEstado;
		public String p_PorDefecto;
		public List<DetallePerfil> p_DetallePerfil;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaCrearPerfilProductos {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfilProductos() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertPerfil {
		public String IdPerfilProductos;
		public String DescripcionPerfilProductos;
		public String IdCategoriaInversionista;
		public String EsPerfilPersonalizado;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
	}

	public static class InsertDetalle {
		public Long IdDetallePerfilProductos;
		public String IdPerfilProductos;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String IdMoneda;
		public String IdGrupoActivos;
		public String IdEstado;
		public Long IdEmpresa;
	}

	public SalidaCrearPerfilProductos logica(Connection conn, EntradaCrearPerfilProductos in) throws Exception {
		logger.info("INIT CrearPerfilProductos");
		SalidaCrearPerfilProductos salida = new SalidaCrearPerfilProductos();
		SQL sql = new SQL();

		try {
			// Validaciones básicas (comentadas en el SP original)
			
			// Solo simulación
			if ("S".equals(in.p_simularOperacion)) {
				logger.debug("Solo simulación, saliendo");
				return salida;
			}

			// Insertar perfil de productos
			logger.debug("INSERT perfil productos");
			InsertPerfil insertPerfil = new InsertPerfil();
			insertPerfil.IdPerfilProductos = in.p_IdPerfilProductos;
			insertPerfil.DescripcionPerfilProductos = in.p_DescripcionPerfilProductos;
			insertPerfil.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
			insertPerfil.EsPerfilPersonalizado = in.p_EsPerfilPersonalizado;
			insertPerfil.IdEstado = in.p_IdEstado;
			insertPerfil.PorDefecto = in.p_PorDefecto;
			insertPerfil.IdEmpresa = in.p_IdEmpresa;

			sql.batch(conn, SQL_INSERT_PERFIL, java.util.Arrays.asList(insertPerfil), InsertPerfil.class);

			// Insertar detalles del perfil
			if (in.p_DetallePerfil != null && !in.p_DetallePerfil.isEmpty()) {
				logger.debug("INSERT detalle perfil productos - " + in.p_DetallePerfil.size() + " registros");
				List<InsertDetalle> detalles = new ArrayList<>();

				for (DetallePerfil det : in.p_DetallePerfil) {
					// Obtener secuencia para cada detalle
					List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::DetallePerfilProductos", 1L);
					
					InsertDetalle insertDet = new InsertDetalle();
					insertDet.IdDetallePerfilProductos = secuencia.get(0);
					insertDet.IdPerfilProductos = in.p_IdPerfilProductos;
					insertDet.IdTipoProducto = det.p_IdTipoProducto;
					insertDet.IdTipoOperacion = det.p_IdTipoOperacion;
					insertDet.IdCanalIngreso = det.p_IdCanalIngreso;
					insertDet.IdMoneda = "CLP"; // Hardcoded como en el SP
					insertDet.IdGrupoActivos = det.p_IdGrupoActivos;
					insertDet.IdEstado = det.p_IdEstado;
					insertDet.IdEmpresa = in.p_IdEmpresa;

					detalles.add(insertDet);
				}

				sql.batch(conn, SQL_INSERT_DETALLE, detalles, InsertDetalle.class);
			}

			salida.P_OUT_REFID = 1L;

		} catch (Exception ex) {
			logger.error("Error en CrearPerfilProductos: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL_PRODUCTOS", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfilProductos");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfilProductos in = gson.fromJson(jin.toString(), EntradaCrearPerfilProductos.class);
		SalidaCrearPerfilProductos out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"COM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPerfilProductos\":\"PERFPROD001\",\"p_DescripcionPerfilProductos\":\"Perfil Test\",\"p_IdCategoriaInversionista\":\"CAT001\",\"p_EsPerfilPersonalizado\":\"N\",\"p_IdEstado\":\"ACT\",\"p_PorDefecto\":\"N\",\"p_DetallePerfil\":[{\"p_IdTipoProducto\":\"ACC\",\"p_IdTipoOperacion\":\"COM\",\"p_IdCanalIngreso\":\"WEB\",\"p_IdMoneda\":\"CLP\",\"p_IdEstado\":\"ACT\",\"p_IdGrupoActivos\":\"GRP001\"}]}";
			EntradaCrearPerfilProductos entrada = gson.fromJson(json, EntradaCrearPerfilProductos.class);
			CrearPerfilProductos demo = new CrearPerfilProductos();
			SalidaCrearPerfilProductos salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
