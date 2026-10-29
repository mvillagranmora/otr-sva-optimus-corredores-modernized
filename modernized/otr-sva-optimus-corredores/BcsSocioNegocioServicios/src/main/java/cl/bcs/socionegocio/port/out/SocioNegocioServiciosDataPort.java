package cl.bcs.socionegocio.port.out;

import cl.bcs.socionegocio.domain.SocioNegocio;

import java.util.List;

public interface SocioNegocioServiciosDataPort {

    String obtenerFechaSistema(Long idEmpresa);

    List<SocioNegocio> consultarSociosNegocio(Long idEmpresa);

    void crearSocioNegocio(Long idEmpresa, String documentoIdentidad); // UOE

    void modificarSocioNegocio(Long idCliente, String estado); // UOE
}
