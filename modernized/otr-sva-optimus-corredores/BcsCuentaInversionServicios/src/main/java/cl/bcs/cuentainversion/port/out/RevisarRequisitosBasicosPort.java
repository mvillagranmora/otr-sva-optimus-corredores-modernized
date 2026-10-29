package cl.bcs.cuentainversion.port.out;

public interface RevisarRequisitosBasicosPort {
    boolean cumpleRequisitos(Long idPortafolio, Long idEmpresa, Long idUsuario, String idModulo);
}
