package cl.bcs.cuentainversion.port.out;

import java.util.Map;

public interface AgregarEventoPort {
    void agregarEvento(Long idEmpresa, String codigoEvento, Map<String, Object> datos);
}
