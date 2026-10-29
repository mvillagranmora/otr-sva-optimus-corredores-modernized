package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoTraspasos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadotraspasos.CambiarEstadoTraspasosEntrada;
import cl.bcs.apv.api.dto.cambiarestadotraspasos.CambiarEstadoTraspasosResultado;
import cl.bcs.apv.api.dto.cambiarestadotraspasos.ParametrosTraspasoEstado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.traspasos.CambiarEstadoTraspasosRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.CambiarEstadoTraspasosRepositorio.EstadoTraspaso;

/** Use case {@code CambiarEstadoTraspasos}: legacy {@code CambiarEstadoTraspaso} (changes the state of each traspaso, one message per row). */
@Transactional(rollbackFor = Exception.class)
public class CambiarEstadoTraspasosServicio implements CambiarEstadoTraspasos {

    private final CambiarEstadoTraspasosRepositorio repositorio;

    public CambiarEstadoTraspasosServicio(CambiarEstadoTraspasosRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public CambiarEstadoTraspasosResultado ejecutar(CambiarEstadoTraspasosEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<EstadoTraspaso> lstTraspaso = new ArrayList<>();
        for (ParametrosTraspasoEstado fila : in.parametros()) {
            try {
                EstadoTraspaso traspaso = new EstadoTraspaso();
                traspaso.IdContratoSAAPV = fila.idContratoSAAPV();
                traspaso.IdTraspaso = fila.idTraspaso();
                traspaso.IdEmpresa = fila.idEmpresa();
                traspaso.IdEstado = fila.idEstado();
                traspaso.CierreSolicitud = null;
                // LEGACY DEFECT kept (see README APV-B-10): the list grows and is batched whole for every row; result[0] is the FIRST row's result
                lstTraspaso.add(traspaso);
                int[] result = repositorio.cambiarEstado(lstTraspaso);
                if (result != null && result.length > 0 && result[0] == 1) {
                    mensajes.add(Mensajes.de(1L, "OK", "CAMBIAR_ESTADO_TRASPASO", "Cambio de Estado de Traspaso exitoso. Folio: " + fila.idTraspaso().toString()));
                } else {
                    mensajes.add(Mensajes.de(1L, "ADVERTENCIA", "ADVERTENCIA_CAMBIAR_ESTADO_TRASPASO",
                            "No existe traspaso para cambio de estado  Folio: " + fila.idTraspaso().toString()));
                }
            } catch (Exception error) {
                // LEGACY DEFECT kept (see README APV-B-12/14): a failed row makes every later row fail; a null IdTraspaso makes this branch itself throw NullPointerException
                mensajes.add(Mensajes.de(1L, "ERROR", "ERROR_CAMBIAR_ESTADO_TRASPASO", "Problema al cambiar estado a traspaso Folio: " + fila.idTraspaso().toString()));
            }
        }
        return new CambiarEstadoTraspasosResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }
}
