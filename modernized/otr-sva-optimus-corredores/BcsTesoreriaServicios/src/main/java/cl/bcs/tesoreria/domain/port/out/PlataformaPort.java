package cl.bcs.tesoreria.domain.port.out;

import cl.bcs.tesoreria.domain.model.DepositanteResult;
import cl.bcs.tesoreria.domain.model.TransaccionBCPResult;
import java.util.List;

public interface PlataformaPort {

    boolean validarRequisitosBasicos(Long idEmpresa, Long idUsuario, String idModulo);
    String obtenerFechaSistema(Long idEmpresa);
    List<String> obtenerMensajesRequisitosBasicos(Long idEmpresa, Long idUsuario, String idModulo);

    void agregarDetalleLog(Object idEmpresa, Object idUsuario, String campo, String valorAnterior, String valorActual);

    DepositanteResult consultarDepositanteBCP(Object request);
    TransaccionBCPResult procesarTransaccionBCP(Object request);

    void revocarContratoCSC(Long idFolioCSC);
}
