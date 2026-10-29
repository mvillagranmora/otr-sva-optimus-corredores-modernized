package cl.bcs.cartolas.master.port.out;

import cl.bcs.cartolas.master.domain.ConfiguracionCartola;
import cl.bcs.cartolas.master.domain.ParametroCartola;

import java.util.List;

/**
 * Puerto de salida (repositorio) para consultas de datos de la Cartola Master.
 *
 * <p>El adaptador que implementa este puerto se conecta a SAP HANA (producción)
 * o a H2 in-memory (perfil test).</p>
 */
public interface CartolaMasterDataPort {

    /**
     * Obtiene la configuración de cartolas para la empresa indicada.
     *
     * <p>Consulta {@code BSCL.Normativa.BD::CAR.Config}.</p>
     *
     * @param idEmpresa identificador de empresa
     * @return lista de configuraciones; vacía si no existen registros
     */
    List<ConfiguracionCartola> findConfiguracionByEmpresa(long idEmpresa);

    /**
     * Obtiene los parámetros de una nómina.
     *
     * <p>Consulta {@code BSCL.Normativa.BD::CAR.Parametro}.</p>
     *
     * @param idNomina identificador de nómina
     * @return lista de parámetros; vacía si no existen registros
     */
    List<ParametroCartola> findParametrosByNomina(long idNomina);
}
