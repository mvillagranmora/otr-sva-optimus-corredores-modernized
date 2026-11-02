package cl.bcs.sura.port.out;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;

import java.util.List;
import java.util.Map;

/**
 * Persistence seam of the SURA profile flows (behaviors B-SURA-07..23). The only implementation that talks JDBC is
 * {@code LegacyHanaSuraPort}; services depend on this interface alone.
 */
public interface SuraDataPort {

    /**
     * Legacy ships two copies of the indicator INSERT/UPDATE statements: the one of ModificarPerfilesPortafolioUtils and
     * the one of ModificarClienteModificarPerfilesPortafolioWS. They differ in one quirk (the WS UPDATE does not
     * upper-case the value parameter), so the caller states which one it means (B-SURA-21).
     */
    enum Variante { UTILS, WS }

    List<IdsEntidadYPerfiles> consultarPerfilesComerciales(List<String> documentosIdentidad);

    List<IdsEntidadYPerfiles> consultarIdsPortafolioIdCliente(List<String> documentosIdentidad);

    /**
     * Replaces legacy generarTablaTemporal + ConsultarPerfilesComercialesFiltradosPorTablaTemporal + Borrar: the seeded
     * rows (IdCliente, IdFormaCobro, DocumentoIdentidad, IdPortafolio) are joined to the profile catalogue (B-SURA-11).
     */
    List<IdsEntidadYPerfiles> consultarPerfilesFiltrados(List<IdsEntidadYPerfiles> semilla);

    /**
     * Stands in for legacy PlataformaUtils.MensajeSalida(idCliente, ERROR, codigo, params, conn, idEmpresa).
     * Returns a map with the keys {@code codigo}, {@code descripcion} and {@code tipo}.
     */
    Map<String, String> resolverMensajeError(Long idCliente, Long idEmpresa, String codigo, Map<String, String> params);

    void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles);

    void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, String valorIndicador, Variante variante);

    default void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, String valorIndicador) {
        ingresarIndicadoresCobroCustodia(perfiles, valorIndicador, Variante.UTILS);
    }

    void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, Variante variante);

    default void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles) {
        modificarIndicadoresCobroCustodia(perfiles, Variante.UTILS);
    }
}
