package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

/**
 * ORACLE SHIM of cl.bcs.socionegocio.soc.SocioNegocioUtils (SocioNegocio/.../soc/SocioNegocioUtils.java): only the members the family-A classes use.
 * Empleado is verbatim (:114-118); obtenerEmpleadoPorIdUsuario / obtenerEmpleadoUsuario reproduce :458-485 (same SQL resource, same
 * Collectors.toMap that throws on a duplicated IdUsuario, same Optional) running the REAL SocioNegocioUtils_SelectEmpleadoPorUsuario.sql.
 */
public class SocioNegocioUtils {

	// legacy SocioNegocioUtils.java:114-118 (verbatim)
	public static class Empleado extends BcsPojo {
		public Long IdUsuario;
		public Long IdEmpleado;
		public String CodigoEmpleado;
	}

	private static String sqlEmpleadoPorUsuario() {
		try (var in = SocioNegocioUtils.class.getClassLoader().getResourceAsStream("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectEmpleadoPorUsuario.sql")) {
			if (in == null) throw new IllegalStateException("can't load resource: SocioNegocioUtils_SelectEmpleadoPorUsuario.sql");
			return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			throw new IllegalStateException(e);
		}
	}

	// legacy :458-472
	public static Map<Long, Empleado> obtenerEmpleadoPorIdUsuario(Connection conn, Long idEmpresa, List<Long> usuarioIds) throws SQLException {
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdEmpresa", idEmpresa);
		params.put("IdUsuario", usuarioIds);
		return new SQL().query(conn, sqlEmpleadoPorUsuario(), params, Empleado.class).stream().collect(Collectors.toMap(u -> u.IdUsuario, u -> u));
	}

	// legacy :474-485
	public static Optional<Empleado> obtenerEmpleadoUsuario(Connection conn, Long idEmpresa, Long idUsuario) throws SQLException {
		Map<Long, Empleado> empleadoPorIdUsuario = obtenerEmpleadoPorIdUsuario(conn, idEmpresa, new ArrayList<>(Collections.singletonList(idUsuario)));
		return Optional.ofNullable(empleadoPorIdUsuario.get(idUsuario));
	}
}
