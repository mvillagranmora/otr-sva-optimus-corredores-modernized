package cl.bcs.apv.support;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipeline;
import cl.bcs.apv.adapter.javacall.ApvJavaCallPipelineEnProceso;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.application.ApvCasosDeUso;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * CONTRACT for the Step 1 characterization tests of BcsAPVServicios: every case runs through
 * {@code ApvPipelineDeCaso.crear(...).invocar("cl.bcs.APV.<Clase>", jsonLegacy)}, i.e. legacy JSON, adapter, REAL use case on the jdbc given
 * (H2 in the tests), adapter, legacy JSON, in process and without HTTP. The result is compared with the golden recorded from the unmodified legacy.
 * The ports are the same as in BcsTesoreriaServicios.
 */
public final class ApvPipelineDeCaso {

    private ApvPipelineDeCaso() {
    }

    public static ApvJavaCallPipeline crear(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema cfg, DiccionarioMensajes dic,
            Colaboradores col, Secuencias sec, Reloj reloj, AlmacenArchivos archivos) {
        return new ApvJavaCallPipelineEnProceso(new ApvJavaCallRegistro(),
                ApvCasosDeUso.crear(jdbc, cfg, dic, sec, reloj, archivos, col));
    }
}
