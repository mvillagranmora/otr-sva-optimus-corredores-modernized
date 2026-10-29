package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoNotificacionContratos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadonotificacioncontratos.CambiarEstadoNotificacionContratosEntrada;
import cl.bcs.apv.api.dto.cambiarestadonotificacioncontratos.CambiarEstadoNotificacionContratosResultado;
import cl.bcs.apv.api.dto.cambiarestadonotificacioncontratos.ContratosSAAPV;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoNotificacionContratosRepositorio;

/** Use case {@code CambiarEstadoNotificacionContratos}: legacy {@code CambiarEstadoEnvioNotificacionContratoSAAPV} (marks the contracts as sent: ENV). */
@Transactional(rollbackFor = Exception.class)
public class CambiarEstadoNotificacionContratosServicio implements CambiarEstadoNotificacionContratos {

    private final ConfiguracionSistema configuracion;
    private final CambiarEstadoNotificacionContratosRepositorio repositorio;

    public CambiarEstadoNotificacionContratosServicio(ConfiguracionSistema configuracion, CambiarEstadoNotificacionContratosRepositorio repositorio) {
        this.configuracion = configuracion;
        this.repositorio = repositorio;
    }

    @Override
    public CambiarEstadoNotificacionContratosResultado ejecutar(CambiarEstadoNotificacionContratosEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        // the legacy reads the system date (and fails when it is not configured) although it does not use it
        Parametros.fechaSistema(configuracion, in.idEmpresa());

        List<String> folios = new ArrayList<>();
        for (ContratosSAAPV row : in.contratosSAAPV()) {
            folios.add(row.idFolioContratoSAAPV());
        }
        repositorio.actualizarEstadoEnvio("ENV", folios);

        return new CambiarEstadoNotificacionContratosResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }
}
