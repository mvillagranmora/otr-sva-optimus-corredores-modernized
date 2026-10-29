package cl.bcs.normativa.svc.domain.cartola;

/**
 * Información básica de un cliente para la cartola.
 */
public class ClienteInfo {

    public Long idCliente;
    public Long idEmpresa;
    public String nombre;

    public ClienteInfo() {}

    public ClienteInfo(Long idCliente, Long idEmpresa) {
        this.idCliente = idCliente;
        this.idEmpresa = idEmpresa;
    }
}
