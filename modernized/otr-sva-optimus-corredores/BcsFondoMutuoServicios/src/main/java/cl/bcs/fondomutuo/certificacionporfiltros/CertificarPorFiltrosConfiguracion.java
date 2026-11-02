package cl.bcs.fondomutuo.certificacionporfiltros;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;

/**
 * Spring wiring of {@link CertificarPorFiltrosPort} for the in-process caller (the massive certification job of another module, {@code BcsJobCertificacionMasivaSolicitudes}). No handler, no FQN in the
 * JavaCall dispatcher, no list in {@code fondomutuo.solicitud.listas-servidas}: nothing here is reachable from the router.
 */
@Configuration
public class CertificarPorFiltrosConfiguracion {

    @Bean
    public CertificarPorFiltrosDatos certificarPorFiltrosDatos(ConexionUnidadDeTrabajo conexion) {
        return new CertificarPorFiltrosDatosJdbc(conexion);
    }

    @Bean
    public CertificarPorFiltrosPort certificarPorFiltrosPort(CertificarAporteRescatePort certificar, CertificarPorFiltrosDatos datos) {
        return new CertificarPorFiltrosFachada(certificar, datos);
    }
}
