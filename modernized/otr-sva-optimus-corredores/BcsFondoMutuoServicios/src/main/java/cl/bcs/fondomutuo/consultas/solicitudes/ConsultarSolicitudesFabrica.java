package cl.bcs.fondomutuo.consultas.solicitudes;

import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

public final class ConsultarSolicitudesFabrica {

    private ConsultarSolicitudesFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public ConsultarSolicitudesDatos datos;
    }

    public static ConsultarSolicitudesPort crear(Dependencias d) {
        return new ConsultarSolicitudesService(d.configuracion, d.datos);
    }
}
