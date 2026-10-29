package cl.bcs.tesoreria.javacall.distribucion;

import java.sql.Date;

/** A movement generated for a request (legacy {@code Traspaso.InMovimientoGenerado}); the private EstadoProceso is serialised as "" like the legacy did. */
public class InMovimientoGenerado {

    public Long IdRecaudacion;
    public Long IdEmpresa;
    public Long IdUsuario;
    public String IdModulo;
    public Long IdSecuenciaMovimiento;
    public Date FechaIngreso;
    public String IdSolicitud;
    public String FolioCSC;
    public String FolioAPV;

    @SuppressWarnings("unused")
    private String EstadoProceso = "";
}
