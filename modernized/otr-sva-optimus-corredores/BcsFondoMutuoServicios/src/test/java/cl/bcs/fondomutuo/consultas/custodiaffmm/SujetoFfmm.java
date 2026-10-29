package cl.bcs.fondomutuo.consultas.custodiaffmm;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;

/**
 * The NEW ConsultarCustodiaPortafolioFFMM over the REAL JDBC adapters on the H2 of the case (the kit {@code ConfiguracionSistemaJdbc} and the new read adapter
 * {@link ConsultarCustodiaPortafolioFfmmJdbc}), seen from where CPF sees it: the port {@link ConsultarCustodiaPortafolioCustodiaPort} implemented by
 * {@link ConsultarCustodiaPortafolioFfmmAdaptador}. The only collaborator outside the module, Plataforma {@code ConsultarEmpresa} (kit port {@code EmpresaPort}), is answered by the case
 * script ({@link Guion}), exactly as the oracle's registered double. Nothing here has state. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoFfmm {

    private SujetoFfmm() {
    }

    /** The service alone (no JSON): what the adapter wraps. */
    static ConsultarCustodiaPortafolioFfmmPort servicio(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarCustodiaPortafolioFfmmFabrica.Dependencias d = new ConsultarCustodiaPortafolioFfmmFabrica.Dependencias();
        d.configuracion = new ConfiguracionSistemaJdbc(conexion);
        d.empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", id == null ? JsonNull.INSTANCE : new JsonPrimitive(id)), InformacionEmpresa.class);
        d.datos = new ConsultarCustodiaPortafolioFfmmJdbc(conexion);
        return ConsultarCustodiaPortafolioFfmmFabrica.crear(d);
    }

    /** The adapter CPF consumes (the unit under test of the JSON contract). */
    static ConsultarCustodiaPortafolioCustodiaPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        return new ConsultarCustodiaPortafolioFfmmAdaptador(servicio(conexion, guion));
    }
}
