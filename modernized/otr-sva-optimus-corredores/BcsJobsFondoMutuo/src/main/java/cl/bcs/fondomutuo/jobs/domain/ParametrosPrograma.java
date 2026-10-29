package cl.bcs.fondomutuo.jobs.domain;

/**
 * Job parameter row from BSCL.Plataforma.BD::BAT.ParametrosPrograma.
 * Mirrors legacy SQL SELECT "IdSecuencia","NombreParametro","Valor".
 */
public record ParametrosPrograma(Long idSecuencia, String nombreParametro, String valor) {}
