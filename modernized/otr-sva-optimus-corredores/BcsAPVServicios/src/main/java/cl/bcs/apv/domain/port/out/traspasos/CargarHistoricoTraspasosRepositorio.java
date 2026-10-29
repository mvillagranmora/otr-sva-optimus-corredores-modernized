package cl.bcs.apv.domain.port.out.traspasos;

/** Persistence port of {@code CargarHistoricoTraspasos} (legacy {@code CargaRegistroHistoricoTraspaso}). */
public interface CargarHistoricoTraspasosRepositorio {

    /** Inserts the header row of the mass load (type 5); answers the affected row count (legacy {@code res[0]}). */
    int insertarCabeceraCarga(String idEmpresa, Long idCargaMasiva, String nombreCarga, Long cantidadRegistros, String idUsuario, Long idTipoCarga,
            String descripcion, String fechaCarga, String idEstado);

    /** Inserts the XML history row. */
    void insertarRegistroXml(String idEmpresa, String idModulo, String idUsuario, String nombreCarga, String origen, String fechaCarga, String archivo);

    /** Legacy {@code con.commit()} at the end of {@code logica}: commits the connection of the call (mid-flow commit reproduced). */
    void confirmar();
}
