package cl.bcs.fondomutuo.consultas.dolar;

import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

public final class ConsultarDolarFabrica {

    private ConsultarDolarFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public ParidadPort paridad;
        public ConsultarDolarDatos datos;
    }

    public static ConsultarDolarPort crear(Dependencias d) {
        return new ConsultarDolarService(d.configuracion, d.paridad, d.datos);
    }
}
