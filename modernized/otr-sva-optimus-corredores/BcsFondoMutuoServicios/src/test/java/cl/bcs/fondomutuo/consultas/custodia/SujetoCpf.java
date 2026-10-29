package cl.bcs.fondomutuo.consultas.custodia;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;

/**
 * The NEW ConsultarCustodiaPortafolioFondoMutuo over the REAL JDBC adapters on the H2 of the case (the kit and series / cuotas / calendar adapters of the F1 closure and the new read
 * adapter of this package). The four collaborators outside the module are answered by the case script ({@link Guion}), exactly as the oracle's registered doubles: Plataforma
 * {@code CalcularParidad} and {@code EntregarPreciosTasas} (HTTP ports, also used by the valuation), {@code ConsultarEmpresa} (kit port) and the custody calculation
 * {@code ConsultarCustodiaPortafolioFFMM} (the NEW port {@link ConsultarCustodiaPortafolioCustodiaPort}: another piece, the custody library). Nothing here has state. Uses the assumed API
 * of {@code API_ASSUMED.md}.
 */
final class SujetoCpf {

    private SujetoCpf() {
    }

    static ConsultarCustodiaPortafolioPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarCustodiaPortafolioFabrica.Dependencias d = new ConsultarCustodiaPortafolioFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.configuracion = configuracion;
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.series = new SeriesFondoJdbc(conexion);
        d.cuotasPort = new CuotasJdbc(conexion);
        d.paridad = e -> guion.responder("CalcularParidad", e);
        d.precios = e -> guion.responder("EntregarPreciosTasas", e);
        d.empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class);
        d.custodia = e -> guion.responder("ConsultarCustodiaPortafolioFFMM", e);
        d.datos = new ConsultarCustodiaPortafolioJdbc(conexion);
        return ConsultarCustodiaPortafolioFabrica.crear(d);
    }
}
