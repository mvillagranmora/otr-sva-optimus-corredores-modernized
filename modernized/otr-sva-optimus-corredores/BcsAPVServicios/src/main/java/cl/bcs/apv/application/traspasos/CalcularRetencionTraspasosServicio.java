package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CalcularRetencionTraspasos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.calcularretenciontraspasos.CalcularRetencionTraspasosEntrada;
import cl.bcs.apv.api.dto.calcularretenciontraspasos.CalcularRetencionTraspasosResultado;
import cl.bcs.apv.api.dto.calcularretenciontraspasos.Traspaso;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.traspasos.CalcularRetencionTraspasosRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.CalcularRetencionTraspasosRepositorio.OrdenTraspaso;
import cl.bcs.apv.domain.port.out.traspasos.CalcularRetencionTraspasosRepositorio.Retencion;

/** Use case {@code CalcularRetencionTraspasos}: legacy {@code CalculoRetencionTraspasos} (totals of the certified FFMM / invoiced RV orders with the retention applied). */
@Transactional(rollbackFor = Exception.class)
public class CalcularRetencionTraspasosServicio implements CalcularRetencionTraspasos {

    private final CalcularRetencionTraspasosRepositorio repositorio;

    public CalcularRetencionTraspasosServicio(CalcularRetencionTraspasosRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public CalcularRetencionTraspasosResultado ejecutar(CalcularRetencionTraspasosEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        BigDecimal montoConDescuento = BigDecimal.ZERO;
        BigDecimal montoRetencionTotal = BigDecimal.ZERO;
        try {
            for (Traspaso fila : in.parametros()) {
                List<OrdenTraspaso> consultaOrdenTraspaso = repositorio.ordenesTraspaso(fila.idTraspaso());
                for (OrdenTraspaso otRow : consultaOrdenTraspaso) {
                    List<Retencion> configuracionRetencion = repositorio.configuracionRetencion();
                    if (otRow.MontoOrden == null && otRow.MontoOrdenFFMM == null) {
                        continue;
                    }
                    if (fila.idTraspaso().compareTo(otRow.IdTraspaso) != 0) {
                        continue;
                    }
                    BigDecimal montoOrden = otRow.MontoOrden == null || otRow.MontoOrden.compareTo(BigDecimal.ZERO) == 0 ? otRow.MontoOrdenFFMM : otRow.MontoOrden;
                    BigDecimal montoRetencion = BigDecimal.ZERO;
                    for (Retencion retRow : configuracionRetencion) {
                        if (otRow.RazonRetiro.equals(retRow.CodigoRetiro) && retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO) != 0) {
                            // LEGACY DEFECT kept (see README APV-B-41): the regimen of the retention is compared with the portafolio's IdMatrizAgrupadorAPV
                            if (retRow.IdRegimenTributario == null || retRow.IdRegimenTributario.compareTo(otRow.IdMatrizAgrupadorAPV) == 0) {
                                // LEGACY DEFECT kept (see README APV-B-44): every matching retention is applied one after the other, only the last amount is reported
                                montoRetencion = montoOrden.multiply(retRow.PorcentajeRetencion);
                                montoOrden = montoOrden.subtract(montoRetencion);
                            }
                        }
                    }
                    montoConDescuento = montoConDescuento.add(montoOrden);
                    montoRetencionTotal = montoRetencionTotal.add(montoRetencion);
                }
            }
        } catch (Exception er) {
            repositorio.revertir();
            // LEGACY DEFECT kept (see README APV-B-51): the error message has no type, so the error counter stays 0
            mensajes.add(Mensajes.de(1L, null, "ERROR", "Error : " + er));
            return new CalcularRetencionTraspasosResultado(mensajes, Mensajes.numeroErrores(mensajes), BigDecimal.ZERO, montoConDescuento,
                    montoRetencionTotal);
        }
        return new CalcularRetencionTraspasosResultado(mensajes, Mensajes.numeroErrores(mensajes), montoConDescuento.add(montoRetencionTotal),
                montoConDescuento, montoRetencionTotal);
    }
}
