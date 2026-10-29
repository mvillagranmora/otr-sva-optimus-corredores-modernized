package cl.bcs.cuentainversion.port.out;

public interface AgregarLogPort {
    void agregarLog(Long idEmpresa, Long idUsuario, String idModulo, String descripcion);
}
