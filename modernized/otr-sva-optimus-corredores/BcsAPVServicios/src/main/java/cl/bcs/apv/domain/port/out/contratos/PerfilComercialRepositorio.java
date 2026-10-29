package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Persistence of the commercial profile associations (legacy AsociarPerfilesAPV SQL). */
public interface PerfilComercialRepositorio {

    /** Row of the validation query. */
    record ValidacionFila(Long IdInterno, String Codigo, String Descripcion) {
    }

    /** Parameters of the insert (the component names are the SQL parameter names). */
    record PerfilInsertar(Long IdPerfilComercialAPV, Long IdEmpresa, String IdCategoriaInversionista, String IdPerfilComercialAbierto,
            String IdPerfilComercialActivo, String IdEstado) {
    }

    List<ValidacionFila> validaciones(Long idEmpresa, String idEstado, List<String> categorias, List<String> abiertos, List<String> activos);

    void insertar(List<PerfilInsertar> perfiles);
}
