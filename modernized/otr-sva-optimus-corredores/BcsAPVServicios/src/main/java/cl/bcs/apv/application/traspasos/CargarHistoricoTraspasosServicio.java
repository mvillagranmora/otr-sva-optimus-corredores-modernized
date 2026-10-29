package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarHistoricoTraspasos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargarhistoricotraspasos.CargarHistoricoTraspasosEntrada;
import cl.bcs.apv.api.dto.cargarhistoricotraspasos.CargarHistoricoTraspasosResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.CargarHistoricoTraspasosRepositorio;

/** Use case {@code CargarHistoricoTraspasos}: legacy {@code CargaRegistroHistoricoTraspaso} (header row of type 5 plus the XML history row). */
@Transactional(rollbackFor = Exception.class)
public class CargarHistoricoTraspasosServicio implements CargarHistoricoTraspasos {

    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final CargarHistoricoTraspasosRepositorio repositorio;

    public CargarHistoricoTraspasosServicio(ConfiguracionSistema configuracion, Secuencias secuencias, CargarHistoricoTraspasosRepositorio repositorio) {
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.repositorio = repositorio;
    }

    @Override
    public CargarHistoricoTraspasosResultado ejecutar(CargarHistoricoTraspasosEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        Long contadorErrores = null; // LEGACY DEFECT kept (see README APV-B-24): the counter is only set when the failure message is added
        // NumberFormatException for a non numeric company (APV-B-26) and Exception for a missing system date (APV-B-27) escape before the try
        String fechaSistema = Parametros.fechaSistema(configuracion, Long.valueOf(in.idEmpresa()));

        try {
            Long idCargaMasiva = secuencias.siguiente("BSCL.APV.BD.Secuencia::APV.IdCargaMasiva");
            int actualizadas = repositorio.insertarCabeceraCarga(in.idEmpresa(), idCargaMasiva, in.nombreCarga(), 0L, in.idUsuario(), 5L, in.origen(),
                    fechaSistema, "VIG");
            if (actualizadas == 1) {
                repositorio.insertarRegistroXml(in.idEmpresa(), in.idModulo(), in.idUsuario(), in.nombreCarga(), in.origen(), in.fechaCarga(), in.archivo());
            }
        } catch (Exception error) {
            mensajes.add(Mensajes.de(1L, Mensajes.MENSAJE_ERROR, "CARGA_HISTORICO_APV_ERROR", error.getMessage()));
            contadorErrores = (long) mensajes.size();
        }

        repositorio.confirmar();
        return new CargarHistoricoTraspasosResultado(mensajes, contadorErrores);
    }
}
