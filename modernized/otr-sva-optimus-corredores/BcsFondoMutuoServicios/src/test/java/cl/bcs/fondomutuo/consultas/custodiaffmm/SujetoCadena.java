package cl.bcs.fondomutuo.consultas.custodiaffmm;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioFabrica;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioJdbc;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioPort;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;

/**
 * THE CHAIN, new side: the NEW ConsultarCustodiaPortafolioFondoMutuo (real JDBC adapters of the F1 closure and of the CPF package) whose custody port is the REAL
 * {@link ConsultarCustodiaPortafolioFfmmAdaptador} over the NEW FFMM service (real JDBC adapter of this package) on the SAME connection / unit of work: the same wiring the production start-up
 * makes once the adapter replaces {@code ConsultarCustodiaPortafolioCustodiaCerrada}. NO double of the custody calculation (the CPF front, {@code SujetoCpf}, scripts it). The collaborators outside the
 * module are answered by ONE case script ({@link Guion}): {@code CalcularParidad}, {@code EntregarPreciosTasas} and {@code ConsultarEmpresa} (called by FFMM and by CPF).
 */
final class SujetoCadena {

    /** Test hook (null in every other test): sees the JSON request CPF sends to the custody port, before the real adapter answers it. Used by CadenaReglasTest to compare it with the CPF front's goldens. */
    static volatile java.util.function.Consumer<com.google.gson.JsonObject> espia;

    private SujetoCadena() {
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
        cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort real = SujetoFfmm.crear(conexion, guion);
        d.custodia = e -> {
            java.util.function.Consumer<com.google.gson.JsonObject> visor = espia;
            if (visor != null) visor.accept(e.deepCopy());
            return real.consultar(e);
        };
        d.datos = new ConsultarCustodiaPortafolioJdbc(conexion);
        return ConsultarCustodiaPortafolioFabrica.crear(d);
    }
}
