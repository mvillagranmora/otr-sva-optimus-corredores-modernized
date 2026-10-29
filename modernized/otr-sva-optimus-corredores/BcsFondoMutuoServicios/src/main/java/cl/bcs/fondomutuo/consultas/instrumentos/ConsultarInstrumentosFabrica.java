package cl.bcs.fondomutuo.consultas.instrumentos;

import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

public final class ConsultarInstrumentosFabrica {

    private ConsultarInstrumentosFabrica() {
    }

    public static final class Dependencias {
        public DiccionarioMensajesPort diccionario;
        public ConsultarInstrumentosDatos datos;
    }

    public static ConsultarInstrumentosPort crear(Dependencias d) {
        return new ConsultarInstrumentosService(d.diccionario, d.datos);
    }
}
