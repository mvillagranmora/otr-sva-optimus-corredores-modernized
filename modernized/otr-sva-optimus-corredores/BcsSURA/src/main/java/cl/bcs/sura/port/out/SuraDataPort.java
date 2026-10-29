package cl.bcs.sura.port.out;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;

import java.util.List;

public interface SuraDataPort {
    List<IdsEntidadYPerfiles> consultarPerfilesComerciales(List<String> documentosIdentidad);
    List<IdsEntidadYPerfiles> consultarIdsPortafolioIdCliente(List<String> documentosIdentidad);
    void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles);
    void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, String valorIndicador);
    void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles);
}
