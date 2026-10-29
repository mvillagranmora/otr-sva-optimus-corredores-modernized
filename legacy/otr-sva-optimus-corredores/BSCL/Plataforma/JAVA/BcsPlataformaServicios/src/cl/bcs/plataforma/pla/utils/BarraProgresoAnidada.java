package cl.bcs.plataforma.pla.utils;

import com.google.gson.JsonObject;

public class BarraProgresoAnidada implements BarraProgreso{

    private final BarraProgreso base;
    private final long desde;
    private final long hasta;

    public BarraProgresoAnidada(BarraProgreso base, long desde, long hasta) {
        this.base = base;
        this.desde = desde;
        this.hasta = hasta;
    }

    private long mapear(long localPorcentaje) {
        return desde + ((hasta - desde) * localPorcentaje) / 100;
    }

    public void actualizar(Long porcentaje, String estado, String mensaje) throws Exception {
        base.actualizar(mapear(porcentaje), estado, mensaje);
    }

    public void actualizar(Long porcentaje, String estado, JsonObject jsonData) throws Exception {
        base.actualizar(mapear(porcentaje), estado, jsonData);
    }

    public void finalizar() throws Exception {
        actualizar(100L, "TERMINADO", "Subproceso finalizado");
    }
    
    @Override
    public BarraProgreso subrango(long desde, long hasta) {
        return new BarraProgresoAnidada(this, desde, hasta);
    }
    
}