package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1885.
 *
 * @author fpino@email.com
 *
 */
public class ValidadorDJ1885 extends Validador {

    /**
     * Tipos de sociedades de la DJ1885.
     */
    private static final List<String> TIPOS_SOCIEDADES = Arrays.asList("A", "C", "E");

    /**
     * Par&aacute;metro SII P28.
     */
    private final double p28;

    /**
     * Par&aacute;metro SII P31.
     */
    private final double p31;

    /**
     * Par&aacute;metro SII P84.
     */
    private final double p84;

    /**
     * Par&aacute;metro SII P85.
     */
    private final double p85;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1885(final ContextoValidacion contexto) throws SQLException {
        super(contexto);

        p28 = contexto.getParametroEsquemaAsDouble("P28");
        p31 = contexto.getParametroEsquemaAsDouble("P31");
        p84 = contexto.getParametroEsquemaAsDouble("P84");
        p85 = contexto.getParametroEsquemaAsDouble("P85");
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H115.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH090(final Registro registro) {
        final String tipoSociedad = (String) registro.get("TipoSociedad");
        return tipoSociedad != null && TIPOS_SOCIEDADES.contains(tipoSociedad);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H095.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH095(final Registro registro) {

        // El credito impuesto de primera categoria (afecto y exento, con y sin
        // derecho a devolucion) menos un monto de tolerancia determinado por el
        // SII, debe ser menor a la suma de los montos dividendos reajustados
        // afectos y exentos mas el incremento por impuesto de 1ra categoria y
        // el Credito por Impuestos Externos, multiplicado por la tasa del
        // Impuesto Primera Categoria.

        final long c12 = (Long) registro.getPorCodigoSII("C12");
        final long c27 = (Long) registro.getPorCodigoSII("C27");
        final long c13 = (Long) registro.getPorCodigoSII("C13");
        final long c28 = (Long) registro.getPorCodigoSII("C28");
        final long c6 = (Long) registro.getPorCodigoSII("C6");
        final long c7 = (Long) registro.getPorCodigoSII("C7");
        final long c9 = (Long) registro.getPorCodigoSII("C9");
        final long c31 = (Long) registro.getPorCodigoSII("C31");

        return (c12 + c27 + c13 + c28 - p31) <= (c6 + c7 + c9 + c31) * p84;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H096.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH096(final Registro registro) {

        // El credito impuesto tasa adicional Ex. Art. 21 L.I.R menos un monto
        // de tolerancia determinado por el SII, debe ser menor a la suma de los
        // montos dividendos reajustados afectos y exentos mas el incremento por
        // impuesto de 1ra categoria por tasa 40%.

        final long cgaa = (Long) registro.getPorCodigoSII("C11");
        final long dra = (Long) registro.getPorCodigoSII("C6");
        final long dre = (Long) registro.getPorCodigoSII("C7");
        final long iip = (Long) registro.getPorCodigoSII("C9");

        return (cgaa - p31) <= (dra + dre + iip) * p28;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2038.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2038(final Registro registro) {

        // Si ingresa monto en alguna de las columnas de credito afecto a GC o
        // Adicional, debe existir monto en la columna de dividendo reajustado
        // afectos a GC adicional.

        final long c12 = (Long) registro.getPorCodigoSII("C12");
        final long c27 = (Long) registro.getPorCodigoSII("C27");
        final long c6 = (Long) registro.getPorCodigoSII("C6");

        if ((c12 + c27) > 0) {
            return c6 > 0;
        }

        return true;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2039.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2039(final Registro registro) {

        // Si ingresa monto en alguna de las columnas de credito exento a GC o
        // adicional, debe existir monto en la columna de dividendo reajustado
        // exentos de GC o adicional.

        final long c13 = (Long) registro.getPorCodigoSII("C13");
        final long c28 = (Long) registro.getPorCodigoSII("C28");
        final long c7 = (Long) registro.getPorCodigoSII("C7");

        if ((c13 + c28) > 0) {
            return c7 > 0;
        }

        return true;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H257.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH257(final Registro registro) {

        // El incremento por impuesto de primera categoria menos un monto de
        // tolerancia determinado por el SII, debe ser menor o igual al credito
        // impuesto de primera categoria (afecto y exento, con y sin derecho a
        // devolucion).

        final long c9 = (Long) registro.getPorCodigoSII("C9");
        final long c12 = (Long) registro.getPorCodigoSII("C12");
        final long c27 = (Long) registro.getPorCodigoSII("C27");
        final long c13 = (Long) registro.getPorCodigoSII("C13");
        final long c28 = (Long) registro.getPorCodigoSII("C28");

        return (c9 - p31) <= (c12 + c27 + c13 + c28);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H258.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH258(final Registro registro) {

        // El incremento por impuesto de primera categoria menos un monto de
        // tolerancia determinado por el SII, debe ser menor a la suma de los
        // montos dividendos reajustados afectos a global complementario o
        // adicional y montos dividendos reajustados exentos de global
        // complementario y el Credito por Impuestos Externos por el factor de
        // incremento del AT.

        final long c9 = (Long) registro.getPorCodigoSII("C9");
        final long c6 = (Long) registro.getPorCodigoSII("C6");
        final long c7 = (Long) registro.getPorCodigoSII("C7");
        final long c31 = (Long) registro.getPorCodigoSII("C31");

        return (c9 - p31) <= (c6 + c7 + c31) * p85;
    }

}
