package cl.bcs.valo2.fechas.contadores;

import java.io.Serializable;

import cl.bcs.valo2.fechas.Fecha;

public class ContadorDiasParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ajusteEOM;
    private String calendario;
    private String convencionDias;
    private Fecha fechaVencimiento;

    public String getCalendario() {
        return calendario;
    }

    public String getConvencionDias() {
        return convencionDias;
    }

    public Fecha getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isAjusteEOM() {
        return ajusteEOM;
    }

    public void setAjusteEOM(
            final boolean ajusteEOM) {
        this.ajusteEOM = ajusteEOM;
    }

    public void setCalendario(
            final String calendario) {
        this.calendario = calendario;
    }

    public void setConvencionDias(
            final String convencionDias) {
        this.convencionDias = convencionDias;
    }

    public void setFechaVencimiento(
            final Fecha fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public String toString() {
        return "ContadorDiasParams [ajusteEOM=" + ajusteEOM + ", calendario=" + calendario
                + ", convencionDias=" + convencionDias + ", fechaVencimiento=" + fechaVencimiento
                + "]";
    }
}
