package cl.bcs.fondomutuo.consultas.solicitudes;

import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

public final class ConsultarSolicitudesWsFabrica {

    private ConsultarSolicitudesWsFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public ConsultarSolicitudesWsDatos datos;
    }

    public static ConsultarSolicitudesWsPort crear(Dependencias d) {
        return new ConsultarSolicitudesWsService(d.configuracion, d.datos);
    }
}
