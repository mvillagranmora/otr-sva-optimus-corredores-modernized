package cl.bcs.apv.jobs.support;

/** The scripted exceptions of a case ({"_lanza": {"clase", "mensaje"}}): the same factory as the oracle's Hooks.excepcion. */
final class Excepciones {

    private Excepciones() {
    }

    static Throwable crear(String clase, String mensaje) {
        return switch (clase) {
            case "Exception" -> new Exception(mensaje);
            case "SQLException" -> new java.sql.SQLException(mensaje);
            case "RuntimeException" -> new RuntimeException(mensaje);
            case "IllegalStateException" -> new IllegalStateException(mensaje);
            case "IllegalArgumentException" -> new IllegalArgumentException(mensaje);
            case "NullPointerException" -> new NullPointerException(mensaje);
            case "ArithmeticException" -> new ArithmeticException(mensaje);
            case "Error" -> new Error(mensaje);
            case "AssertionError" -> new AssertionError(mensaje);
            default -> throw new IllegalArgumentException("unknown scripted exception class " + clase);
        };
    }
}
