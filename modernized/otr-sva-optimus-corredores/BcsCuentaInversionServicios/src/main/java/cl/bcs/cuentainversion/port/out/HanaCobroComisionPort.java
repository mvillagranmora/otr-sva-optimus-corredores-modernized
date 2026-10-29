package cl.bcs.cuentainversion.port.out;

import java.util.List;
import java.util.Map;

public interface HanaCobroComisionPort {
    List<Map<String, Object>> consultarRegistrosVigentes(Long idEmpresa, String idMoneda);
    void marcarComoProcesado(Long idEmpresa, Long idRegistro);
}
