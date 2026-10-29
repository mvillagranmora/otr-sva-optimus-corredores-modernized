package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Persistence of the mass load header (legacy CargaMasivaArchivo SQL). */
public interface CargaMasivaRepositorio {

    /** Parameters of the insert (the component names are the SQL parameter names). */
    record CargaArchivoFila(Long IdEmpresa, Long IdCargaMasiva, String NombreCargaMasiva, Long CantidadRegistros, Long IdUsuario, int IdTipoCarga,
            String Descripcion, String FechaCarga, String IdEstado) {
    }

    /** Runs the insert as one batch, one execution per row, and answers the update counts of the batch (legacy {@code SQL.batch}). */
    int[] insertarArchivos(List<CargaArchivoFila> filas);
}
