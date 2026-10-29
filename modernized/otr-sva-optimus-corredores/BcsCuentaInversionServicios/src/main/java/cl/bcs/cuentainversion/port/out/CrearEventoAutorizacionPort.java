package cl.bcs.cuentainversion.port.out;

import java.util.List;

public interface CrearEventoAutorizacionPort {
    void crearEventoAutorizacion(Long idEmpresa, Long idUsuario, String concepto, List<Long> idsMovimiento);
}
