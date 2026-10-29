package cl.bcs.tesoreria.javacall.distribucion;

import java.util.ArrayList;
import java.util.List;

/**
 * Request of ProcesarDistribucion / ValidaPreRendicion (legacy {@code EntradaRecaudacionTraspaso}). {@code p_Recaudacion} is accepted but ignored:
 * everything is read from the file load (CAR.CargaMasivaDetallePAC), the mandates and the contracts (README, determinism decisions).
 */
public class EntradaRecaudacionTraspaso {

    public Long p_IdEmpresa;
    public Long p_IdUsuario;
    public String p_IdModulo;
    public String p_simular;
    public List<InRecaudacion> p_Recaudacion = new ArrayList<>();
}
