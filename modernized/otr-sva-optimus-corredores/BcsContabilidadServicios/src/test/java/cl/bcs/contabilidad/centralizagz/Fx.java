package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;

/**
 * Fixed fixture values shared by every WP-F characterization test.
 *
 * No value here comes from the legacy code base: the RUT of the broker is a
 * clearly fake value of the same shape ("76999999-9"), there are no
 * credentials, hosts or connection strings anywhere in this package.
 */
final class Fx {

    static final Long   ID_EMPRESA        = 1L;
    static final Long   ID_USUARIO        = 97L;
    static final String CODIGO_USUARIO    = "97";            // String.valueOf(ID_USUARIO), CentralizaMultiSeleccion.java:415
    static final String ID_MODULO         = "CON";
    static final Long   ID_TIPO           = 470L;
    static final String FECHA_CONTABLE    = "2025-03-10";
    static final String FECHA_ANTERIOR    = "2025-03-07";    // previous business day returned by the platform stub
    static final String ID_INFORME        = "INF-470-20250310";
    static final Long   EJERCICIO         = 2025L;
    static final Long   ID_PAIS           = 56L;
    static final String LOC_SISTEMA       = "CL";
    static final String MONEDA_SISTEMA    = "CLP";
    /** Fake broker RUT (same shape as a Chilean RUT, not a real one). */
    static final String RUT_CORREDORA     = "76999999-9";
    static final String NOMBRE_CORREDORA  = "CORREDORA DE PRUEBA S.A.";

    static final EmpresaInfo EMPRESA =
            new EmpresaInfo(ID_EMPRESA, ID_PAIS, RUT_CORREDORA, NOMBRE_CORREDORA);

    static EntradaCentralizacion entrada(String simulaOperacion) {
        return new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, ID_TIPO,
                FECHA_CONTABLE, ID_INFORME, EJERCICIO, simulaOperacion);
    }

    private Fx() {}
}
