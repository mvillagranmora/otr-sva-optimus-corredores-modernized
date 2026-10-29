package cl.bcs.contabilidad.domain.service.devengo;

/**
 * The three accrual centralizations that read the valuation report CarteraValorizadaIFRF, with the SQL resources each
 * one runs (legacy DevengoATasaCompraCarteraPactada.java:19-21, DevengoATasaPacto.java:22-24,
 * DevengoCarteraPropiaLibreYGarantia.java:23-25; the movement / event statements are the raw projections that replace
 * the legacy derived queries, see {@link DevengoCalculator}).
 */
public enum TipoDevengo {

    /** IdTipoCentralizacion 440, "Pacto a Tasa Compra". */
    TASA_COMPRA_CARTERA_PACTADA(
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraMovimientos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraEventos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectDatosMovimiento.sql",
            true),

    /** IdTipoCentralizacion 430, "Pacto a Tasa Pacto". */
    TASA_PACTO(
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectCarteraMovimientos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectCarteraEventos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectDatosMovimiento.sql",
            true),

    /** IdTipoCentralizacion 450, "Propia a Tasa Compra". */
    CARTERA_PROPIA_LIBRE_Y_GARANTIA(
            "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraMovimientos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraEventos.sql",
            "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectDatosMovimiento.sql",
            false);

    private final String sqlMovimientos;
    private final String sqlEventos;
    private final String sqlDatos;
    private final boolean datosPorFechaContable;

    TipoDevengo(String sqlMovimientos, String sqlEventos, String sqlDatos, boolean datosPorFechaContable) {
        this.sqlMovimientos = sqlMovimientos;
        this.sqlEventos = sqlEventos;
        this.sqlDatos = sqlDatos;
        this.datosPorFechaContable = datosPorFechaContable;
    }

    public String sqlMovimientos() { return sqlMovimientos; }
    public String sqlEventos() { return sqlEventos; }
    public String sqlDatos() { return sqlDatos; }

    /** Whether the datos query also binds {@code :p_FechaContable} (CarteraPropia binds only empresa and informe, :89-91). */
    public boolean datosPorFechaContable() { return datosPorFechaContable; }
}
