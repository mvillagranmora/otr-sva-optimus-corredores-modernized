package cl.bcs.tesoreria.javacall.distribucion;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.colaborador.ResultadoSolicitud;

/**
 * Response of ProcesarDistribucion / ValidaPreRendicion (legacy {@code SalidaRecaudacionTraspaso}) and, at the same time, the mutable state the steps of
 * one run write into (the legacy kept it in an instance field of the service; here a run owns its instance, the services stay stateless).
 * {@code P_OUT_RESULTADO} is part of the contract but nothing fills it (legacy).
 */
public class SalidaRecaudacionTraspaso {

    public Long P_OUT_ERRORESCONTADOR = 0L;
    public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    public List<RegistroRecaudacion> P_OUT_SOLICITUDES_PROCESADAS = new ArrayList<>();
    public List<RegistroRecaudacion> P_OUT_SOLICITUDES_NO_PROCESADAS = new ArrayList<>();
    public List<RegistroRecaudacion> P_OUT_SOLICITUDES_A_PROCESAR = new ArrayList<>();
    public List<ResultadoSolicitud> P_OUT_RESULTADO = new ArrayList<>();
}
