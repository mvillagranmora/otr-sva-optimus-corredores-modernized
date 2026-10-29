package cl.bcs.fondomutuo.consultas.detalle;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;

/**
 * The NEW ConsultarDetalleSolicitud over the REAL JDBC adapters on the H2 of the case (the kit and series / cuotas adapters of the F1 closure and the new read adapter of
 * this package). The two collaborators outside the module that {@code CalcularMontoNumeroCuota} calls, Plataforma {@code CalcularParidad} and {@code EntregarPreciosTasas}
 * (HTTP ports), are answered by the case script ({@link Guion}), exactly as the oracle's registered doubles. Nothing here has state. Uses the assumed API of
 * {@code API_ASSUMED.md}.
 */
final class SujetoDet {

    private SujetoDet() {
    }

    static ConsultarDetallePort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarDetalleFabrica.Dependencias d = new ConsultarDetalleFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = new DiccionarioJdbc(conexion);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.series = new SeriesFondoJdbc(conexion);
        d.cuotasPort = new CuotasJdbc(conexion);
        d.paridad = e -> guion.responder("CalcularParidad", e);
        d.precios = e -> guion.responder("EntregarPreciosTasas", e);
        d.datos = new ConsultarDetalleJdbc(conexion);
        return ConsultarDetalleFabrica.crear(d);
    }
}
