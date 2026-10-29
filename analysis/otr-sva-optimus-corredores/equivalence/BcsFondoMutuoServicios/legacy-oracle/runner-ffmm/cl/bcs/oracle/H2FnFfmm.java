package cl.bcs.oracle;

/**
 * H2 implementation (registered with CREATE ALIAS in schema-ffmm.sql) of the one HANA SQL function that the SQL of ConsultarCustodiaPortafolioFFMM uses and that cl.bcs.oracle.H2Fn
 * (shared, NOT modified) does not have. Own copy of the idea of H2FnCpf (the CPF front is not touched). A COPY with the same class name and body lives in the test source set of
 * BcsFondoMutuoServicios (src/test/java/cl/bcs/oracle/H2FnFfmm.java, written by sync_ffmm.py): the new code runs on the same H2 function.
 *
 * <ul>
 *   <li>{@code MAP(value, search, result, default)}: HANA decode. The SQL uses it only as {@code MAP(x, NULL, 0, x)} to turn a NULL into 0: a NULL value matches a NULL search
 *       (ASSUMED: it is the intent of the SQL; HANA's treatment of NULL in MAP was not verified: see the HANA question in the README).</li>
 * </ul>
 */
public final class H2FnFfmm {
    private H2FnFfmm() { }

    public static Long map(Long value, Long search, Long result, Long otherwise) {
        boolean igual = value == null ? search == null : value.equals(search);
        return igual ? result : otherwise;
    }
}
