package cl.bcs.fondomutuo.certificacion;

/** THE SEAM of T1: the phases after F-B (fronts C..J, T2..T5). No production implementation exists: a bean of it must not be wired by accident. */
public interface CertificarAporteRescateFasesPosteriores {

    CertificarAporteRescateContrato.Salida continuar(CertificarAporteRescateContexto contexto) throws Exception;
}
