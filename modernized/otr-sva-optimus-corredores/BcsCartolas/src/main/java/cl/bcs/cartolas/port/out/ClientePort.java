package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Cliente;

import java.util.Optional;

/**
 * Puerto de salida para consulta de clientes (vista NOR.ConsultarCliente)
 * y portafolios (POR.Portafolio).
 */
public interface ClientePort {

    Optional<Cliente> findById(long idCliente);

    Optional<String> findCodigoPortafolio(long idPortafolio);
}
