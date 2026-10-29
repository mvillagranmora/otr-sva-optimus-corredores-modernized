package cl.bcs.fondomutuo.port.out.kit;

import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;

/** Kit port K: legacy {@code RevisarRequisitosBasicos.logica(con, idEmpresa, idUsuario, idModulo)} (company, business partner, user and module checks, reads only). */
public interface RevisarRequisitosBasicosPort {

    RevisarRequisitosBasicosSalida revisar(Long idEmpresa, Long idUsuario, String idModulo);
}
