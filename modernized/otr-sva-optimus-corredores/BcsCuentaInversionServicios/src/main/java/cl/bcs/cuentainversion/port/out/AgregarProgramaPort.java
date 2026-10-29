package cl.bcs.cuentainversion.port.out;

import java.util.List;

public interface AgregarProgramaPort {
    void agendarActualizarSaldoCargoAbono(Long idEmpresa, Long idUsuario, List<Long> idsCuentaInversion);
}
