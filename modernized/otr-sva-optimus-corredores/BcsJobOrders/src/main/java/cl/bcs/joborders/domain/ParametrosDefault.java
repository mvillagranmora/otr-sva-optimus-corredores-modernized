package cl.bcs.joborders.domain;

// Parámetros de auditoría usados en SPs de bloqueo/desbloqueo
public record ParametrosDefault(int idEmpresa, int idUsuario, String modulo) {
    public static ParametrosDefault defecto() {
        return new ParametrosDefault(1, 3, "ORD");
    }
}
