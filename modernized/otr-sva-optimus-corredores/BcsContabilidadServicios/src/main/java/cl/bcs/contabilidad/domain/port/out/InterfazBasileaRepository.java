package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.CuentaPropia;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;

import java.util.List;

/**
 * Persistence of legacy {@code ProcesarInterfazBasilea} (ProcesarInterfazBasilea.java:33-41, 287-303, 341).
 * Dates are passed as yyyy-MM-dd strings, as the legacy did.
 */
public interface InterfazBasileaRepository {

    /** ProcesarInterfazBasilea_SelectCuentasBasilea.sql: configured accounts with IdEstado 'ACT'. */
    List<CuentaBasilea> consultarCuentasBasileaActivas();

    /** ProcesarInterfazBasilea_SelectCuentaPropia.sql (legacy custodia ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql). */
    List<CuentaPropia> consultarCuentaPropia(Long idEmpresa);

    /** ProcesarInterfazBasilea_SelectAcciones.sql. */
    List<DatoBusquedaBasilea> consultarAcciones(Long idCliente);

    /** ProcesarInterfazBasilea_SelectRfIf.sql. */
    List<DatoBusquedaBasilea> consultarRfIf(Long idCliente, String fechaSistema);

    /** ProcesarInterfazBasilea_SelectVentasCortas.sql. */
    List<DatoBusquedaBasilea> consultarVentasCortas(Long idCliente, String fechaSistema);

    /** ProcesarInterfazBasilea_SelectPactosVigentes.sql. */
    List<DatoBusquedaBasilea> consultarPactosVigentes(String fechaSistema);

    /** ProcesarInterfazBasilea_SelectForward.sql. */
    List<DatoBusquedaBasilea> consultarForward(String fechaSistema);

    /** ProcesarInterfazBasilea_InsertInterfazBasilea.sql, as one batch. */
    void insertarInterfazBasilea(List<RegistroInterfazBasilea> registros);
}
