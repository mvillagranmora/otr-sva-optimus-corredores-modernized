package cl.bcs.apv.jobs.job;

import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/**
 * What the legacy jobs read from the platform configuration on top of the {@link ConfiguracionSistema} port, which answers {@code null} for a parameter
 * that is not configured: the legacy raised then, and which exception depends on the way it was read.
 */
final class ConfiguracionLegacy {

    static final String PLATAFORMA = "PLATAFORMA";

    private ConfiguracionLegacy() {
    }

    /**
     * Legacy {@code Utils.getConfiguracion(con, schema, idEmpresa, codigo)} (BcsUtils Utils.java:131-138): the HANA procedure
     * {@code ObtenerConfiguracion} SIGNALs error 10007 for a missing parameter, a {@link StoredProcedureException}.
     */
    static String plataforma(ConfiguracionSistema configuracion, long idEmpresa, String codigo) throws StoredProcedureException {
        // the legacy signature takes a primitive long: a null company id fails at the call site with a NullPointerException when unboxed (CMP-020)
        String valor = configuracion.parametro(idEmpresa, PLATAFORMA, codigo);
        if (valor == null) {
            throw new StoredProcedureException("PARAMETRO_DE_CONFIGURACION_NO_EXISTE (SQL error code 10007)");
        }
        return valor;
    }

    /** Legacy {@code ConfiguracionSistema.getParametroConfiguracionString} (ConfiguracionSistema.java:70-71): a missing parameter is an {@link Exception}. */
    static String parametro(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        String valor = configuracion.parametro(idEmpresa, modulo, nombre);
        if (valor == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }

    /** Legacy {@code getParametroConfiguracionLong}: the text parsed with {@code Long.parseLong} (a non numeric value is a NumberFormatException). */
    static long parametroLong(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        return Long.parseLong(parametro(configuracion, idEmpresa, modulo, nombre));
    }
}
