package cl.bcs.plataforma.pla.utils;
import java.sql.Connection;
import com.google.gson.JsonObject;
public class BarraProgresoWrapper implements BarraProgreso{

    protected final Connection con;
    protected final Long idEmpresa;
    protected final String servicio;
    protected final Long idTrabajo;
    protected final Long idUsuario;
    private boolean iniciado = false;

    public BarraProgresoWrapper(Connection con, Long idEmpresa, String servicio, Long idTrabajo, Long idUsuario) {
        this.con = con;
        this.idEmpresa = idEmpresa;
        this.servicio = servicio;
        this.idTrabajo = idTrabajo;
        this.idUsuario = idUsuario;
        this.iniciado = false;
    }

    public void iniciar() throws Exception {
        if (!iniciado) {
            BarraProgresoServicio.iniciar(con, idEmpresa, servicio, idTrabajo, idUsuario);
            iniciado = true;
        }
    }

    public void actualizar(Long porcentaje, String estado, String mensaje) throws Exception {
        if (!iniciado) iniciar();
        BarraProgresoServicio.actualizar(con, idEmpresa, servicio, estado, mensaje, idTrabajo, porcentaje);
    }

    public void actualizar(Long porcentaje, String estado, JsonObject jsonData) throws Exception {
        if (!iniciado) iniciar();
        BarraProgresoServicio.actualizarConJSON(con, idEmpresa, servicio, estado, jsonData, idTrabajo, porcentaje);
    }

    public void finalizar() throws Exception {
        actualizar(100L, "TERMINADO", "Proceso finalizado correctamente.");
    }

    @Override
    public BarraProgreso subrango(long desde, long hasta) {
        return new BarraProgresoAnidada(this, desde, hasta);
    }
}