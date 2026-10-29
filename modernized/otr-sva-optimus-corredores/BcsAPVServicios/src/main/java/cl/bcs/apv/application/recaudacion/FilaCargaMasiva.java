package cl.bcs.apv.application.recaudacion;

/** Row of the load header insert {@code CargaMasivaArchivo_Insert} (legacy {@code CargaArchivo} of the mass-load classes): public fields are the SQL parameter names. */
public class FilaCargaMasiva {
    public Long IdEmpresa;
    public Long IdCargaMasiva;
    public String NombreCargaMasiva;
    public Long CantidadRegistros;
    public Long IdUsuario;
    public int IdTipoCarga;
    public String Descripcion;
    public String FechaCarga;
    public String IdEstado;

    public FilaCargaMasiva() {
    }

    public FilaCargaMasiva(Long idEmpresa, Long idCargaMasiva, String nombreCargaMasiva, Long cantidadRegistros, Long idUsuario, int idTipoCarga,
            String descripcion, String fechaCarga, String idEstado) {
        this.IdEmpresa = idEmpresa;
        this.IdCargaMasiva = idCargaMasiva;
        this.NombreCargaMasiva = nombreCargaMasiva;
        this.CantidadRegistros = cantidadRegistros;
        this.IdUsuario = idUsuario;
        this.IdTipoCarga = idTipoCarga;
        this.Descripcion = descripcion;
        this.FechaCarga = fechaCarga;
        this.IdEstado = idEstado;
    }
}
