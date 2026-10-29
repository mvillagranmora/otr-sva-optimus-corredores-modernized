package cl.bcs.fondomutuo.perfil;

import java.util.List;

/**
 * Read port of {@link ObtenerPerfilFondoMutuo}: {@code Comunes_SelectPerfilFondoMutuo.sql} (verbatim, {@code SELECT TOP 1 "Key" AS "Perfil"} without
 * {@code ORDER BY}: the row HANA returns first is the one the legacy took, D-REQ-8) - the commercial / back-office profile key of the profiles of the
 * user. The list holds the {@code Perfil} column of the returned rows. Reads only.
 */
public interface PerfilFondoMutuoPort {

    List<String> perfiles(Long idUsuario);
}
