package cl.bcs.fondomutuo.consultas.reinversion;

public final class ConsultarReinversionFabrica {

    private ConsultarReinversionFabrica() {
    }

    public static ConsultarReinversionPort crear(ConsultarReinversionDatos datos) {
        return new ConsultarReinversionService(datos);
    }
}
