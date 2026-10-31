package cl.bcs.fondomutuo.certificacion;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;

/**
 * The recursion of the legacy ({@code new CertificarSolicitudAporteRescate().logica(con, entradaSwitch)}) for the aportes of switch (F-I, FM-CER-63). Its implementation is the facade of T5
 * (which must also bound the depth: the legacy has no guard). No production implementation exists yet.
 */
public interface CertificacionHijaPort {

    Salida certificar(Entrada entrada) throws Exception;
}
