package cl.bcs.contabilidad.domain.service.basilea;

/**
 * Outcome of the Basilea account lookup (legacy {@code buscarCuenta}, ProcesarInterfazBasilea.java:361-367, which
 * returned "ERROR", null or the account). {@code cuentaContable} is set only for {@link Resultado#UNICA} and may be
 * the empty string (an account configured with no number, CONT-D-63).
 */
public record ResolucionCuentaBasilea(Resultado resultado, String cuentaContable) {

    public enum Resultado { UNICA, MULTIPLE, SIN_CUENTA }

    static ResolucionCuentaBasilea unica(String cuenta) {
        return new ResolucionCuentaBasilea(Resultado.UNICA, cuenta);
    }

    static final ResolucionCuentaBasilea MULTIPLE = new ResolucionCuentaBasilea(Resultado.MULTIPLE, null);
    static final ResolucionCuentaBasilea SIN_CUENTA = new ResolucionCuentaBasilea(Resultado.SIN_CUENTA, null);
}
