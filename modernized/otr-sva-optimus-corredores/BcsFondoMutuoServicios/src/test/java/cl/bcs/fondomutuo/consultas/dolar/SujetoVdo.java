package cl.bcs.fondomutuo.consultas.dolar;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;

/**
 * The NEW ConsultarValorDolarFondoMutuo over the REAL JDBC adapters on the H2 of the case (kit configuration, the one SQL
 * {@code ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql} verbatim); the only collaborator outside the module, Plataforma {@code CalcularParidad}
 * (port H {@link cl.bcs.fondomutuo.port.out.http.ParidadPort}, legacy JSON in and out), is answered by the case script ({@link Guion}), which records each call as
 * the oracle's registered double does. Nothing here has state. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoVdo {

    private SujetoVdo() {
    }

    static ConsultarDolarPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarDolarFabrica.Dependencias d = new ConsultarDolarFabrica.Dependencias();
        d.configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.paridad = entrada -> guion.responder("CalcularParidad", entrada);
        d.datos = new ConsultarDolarJdbc(conexion);
        return ConsultarDolarFabrica.crear(d);
    }
}
