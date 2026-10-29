package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CrearOrdenesTraspaso;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.crearordenestraspaso.CrearOrdenesTraspasoEntrada;
import cl.bcs.apv.api.dto.crearordenestraspaso.CrearOrdenesTraspasoResultado;
import cl.bcs.apv.api.dto.crearordenestraspaso.OrdenTraspaso;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.CrearOrdenesTraspasoRepositorio;

/** Use case {@code CrearOrdenesTraspaso}: legacy {@code CrearOrdenTraspaso} (relation between an order / FFMM request and a traspaso). */
@Transactional(rollbackFor = Exception.class)
public class CrearOrdenesTraspasoServicio implements CrearOrdenesTraspaso {

    private final Secuencias secuencias;
    private final CrearOrdenesTraspasoRepositorio repositorio;

    public CrearOrdenesTraspasoServicio(Secuencias secuencias, CrearOrdenesTraspasoRepositorio repositorio) {
        this.secuencias = secuencias;
        this.repositorio = repositorio;
    }

    @Override
    public CrearOrdenesTraspasoResultado ejecutar(CrearOrdenesTraspasoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        // legacy Utils.getSecuencia(con, name, n): one block of n ids requested up front (the port draws one value per call)
        List<Long> idOrdenTraspaso = new ArrayList<>();
        for (long i = 0; i < in.ordenTraspaso().size(); i++) {
            idOrdenTraspaso.add(secuencias.siguiente("BSCL.APV.BD.Secuencia::TRA.IdOrdenTraspaso"));
        }

        for (OrdenTraspaso fila : in.ordenTraspaso()) {
            if (fila.idTraspaso() > 0) {
                int resultado = repositorio.insertar(fila.idTraspaso(), fila.idOrden(), fila.idSolicitudAporteRescate());
                // LEGACY DEFECT kept (see README APV-B-18): every message carries the FIRST id of the sequence block while the rows take their ids from the database sequence
                if (resultado == 1) {
                    mensajes.add(Mensajes.de(idOrdenTraspaso.get(0), Mensajes.MENSAJE_OK, "CREAR_ORDEN_TRASPASO_OK", "Se crea relacion Orden con Traspaso."));
                } else {
                    mensajes.add(Mensajes.de(idOrdenTraspaso.get(0), Mensajes.MENSAJE_ERROR, "CREAR_ORDEN_TRASPASO_ERROR",
                            "Problema al crear relacion Orden con Traspaso."));
                }
            } else {
                mensajes.add(Mensajes.de(1L, Mensajes.MENSAJE_ERROR, "ERROR_DATOS", "No hay orden creada para el traspaso " + fila.idTraspaso().toString()));
            }
        }

        return new CrearOrdenesTraspasoResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }
}
