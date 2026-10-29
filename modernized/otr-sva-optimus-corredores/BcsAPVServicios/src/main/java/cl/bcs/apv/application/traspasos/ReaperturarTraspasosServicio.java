package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.IngresarRecaudacionTraspaso;
import cl.bcs.apv.api.casouso.ModificarPortafoliosTraspaso;
import cl.bcs.apv.api.casouso.ReaperturarTraspasos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.EntradaRecaudacion;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.EntradaTraspaso;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.IngresarRecaudacionTraspasoEntrada;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.IngresarRecaudacionTraspasoResultado;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.ModificarPortafoliosTraspasoEntrada;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.ModificarPortafoliosTraspasoResultado;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.PortafoliosModificados;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.Traspaso;
import cl.bcs.apv.api.dto.reaperturartraspasos.ReaperturarTraspasosEntrada;
import cl.bcs.apv.api.dto.reaperturartraspasos.ReaperturarTraspasosResultado;
import cl.bcs.apv.api.dto.reaperturartraspasos.Resultado;
import cl.bcs.apv.api.dto.reaperturartraspasos.TraspasoReapertura;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.traspasos.ReaperturarTraspasosRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.ReaperturarTraspasosRepositorio.EstadoDetalleTraspaso;

/**
 * Use case {@code ReaperturarTraspasos}: legacy {@code cl.bcs.APV.ReaperturarTraspaso}. Stage 1 runs {@code ModificarPortafoliosTraspaso} once per row with a
 * reopening amount; stage 2 runs {@code IngresarRecaudacionTraspaso} (type R). Both are injected typed; their rows stay written when a later step fails
 * (README APV-B-248, 344: no commit/rollback is issued here).
 */
@Transactional(rollbackFor = Exception.class)
public class ReaperturarTraspasosServicio implements ReaperturarTraspasos {

    private final ReaperturarTraspasosRepositorio repositorio;
    private final ModificarPortafoliosTraspaso modificarPortafoliosTraspaso;
    private final IngresarRecaudacionTraspaso ingresarRecaudacionTraspaso;

    public ReaperturarTraspasosServicio(ReaperturarTraspasosRepositorio repositorio, ModificarPortafoliosTraspaso modificarPortafoliosTraspaso,
            IngresarRecaudacionTraspaso ingresarRecaudacionTraspaso) {
        this.repositorio = repositorio;
        this.modificarPortafoliosTraspaso = modificarPortafoliosTraspaso;
        this.ingresarRecaudacionTraspaso = ingresarRecaudacionTraspaso;
    }

    @Override
    public ReaperturarTraspasosResultado ejecutar(ReaperturarTraspasosEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<Resultado> idResultado = new ArrayList<>();
        long errores = 0L;
        BigDecimal montoReapertura = BigDecimal.ZERO;

        List<EstadoDetalleTraspaso> detalleTraspaso = repositorio.detalleDelTraspaso(in.traspasos().get(0).idTraspaso());
        List<Long> lstDetalleTraspasoReapertura = new ArrayList<>();

        if (!in.traspasos().isEmpty() || detalleTraspaso.get(0).IdEstado().compareTo("COM") == 0) {
            for (TraspasoReapertura fila : in.traspasos()) {
                if (fila.montoReapertura().compareTo(BigDecimal.ZERO) > 0) {
                    List<Traspaso> lstTraspasoMod = new ArrayList<>();
                    lstTraspasoMod.add(new Traspaso(fila.idTraspaso(), fila.idContratoSAAPV(), fila.folioContratoSAAPV(), fila.fechaIngreso(),
                            fila.fechaModificacion(), fila.idPersonaCliente(), fila.idCliente(), fila.documentoIdentidad(), fila.idPerfilComercial(), 0L, 0L,
                            fila.idPropositoPortafolio(), fila.idPlanTipoInversion(), fila.nombrePlan(), fila.idProductoAgrupador(), fila.productoAgrupador(),
                            fila.idRegimenTributario(), fila.regimenTributario(), fila.codigoAgrupador(),
                            fila.montoReapertura(), // monto reapertura asignado en el monto peso
                            fila.montoUF(), fila.porcentaje(), "S"));
                    List<PortafoliosModificados> modificados = new ArrayList<>();
                    for (cl.bcs.apv.api.dto.reaperturartraspasos.PortafoliosModificados p : in.portafoliosModificados()) {
                        modificados.add(new PortafoliosModificados(p.codigoAgrupador(), p.idMatrizAgrupador(), p.origenAhorro(), p.idPortafolio(),
                                p.idPropositoPortafolio(), p.mesInicio(), p.mesTermino(), p.montoPesos(), p.montoUF(), p.pago(), p.pagoNombre(), p.porcentaje(),
                                p.producto(), p.regimen()));
                    }
                    ModificarPortafoliosTraspasoResultado outModificaTraspaso = modificarPortafoliosTraspaso
                            .ejecutar(new ModificarPortafoliosTraspasoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "R", lstTraspasoMod, modificados));

                    // the legacy read the error counter of logica (== the number of ERROR messages of the typed answer)
                    if (outModificaTraspaso.contadorErrores() < 1) {
                        lstDetalleTraspasoReapertura.add(outModificaTraspaso.idResultado().get(0));
                    }

                    mensajes.add(outModificaTraspaso.mensajes().get(0));
                    errores = errores + outModificaTraspaso.contadorErrores();
                    montoReapertura = montoReapertura.add(fila.montoReapertura());
                }
            }
            if (errores > 0) {
                mensajes.add(Mensajes.de(0L, "ERROR", "ERROR_MODIFICA_ESTADO", "Problema al reaperturar el Traspaso."));
            } else {
                EntradaRecaudacion recaudacion = null;
                if (in.recaudacion() != null) {
                    cl.bcs.apv.api.dto.reaperturartraspasos.EntradaRecaudacion r = in.recaudacion();
                    recaudacion = new EntradaRecaudacion(r.folioCartola(), r.lineaCartola(), r.idSucursal(), r.cuentaCorriente(), r.idTipoDeposito(), r.idBanco());
                }
                List<EntradaTraspaso> lstIdTraspaso = new ArrayList<>();
                List<Long> detalles = new ArrayList<>(lstDetalleTraspasoReapertura);
                lstIdTraspaso.add(new EntradaTraspaso(in.traspasos().get(0).idTraspaso(), montoReapertura, detalles));
                IngresarRecaudacionTraspasoResultado outRecaudacionReapertura = ingresarRecaudacionTraspaso
                        .ejecutar(new IngresarRecaudacionTraspasoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "R", recaudacion, lstIdTraspaso));

                if (outRecaudacionReapertura.contadorErrores() == 0) {
                    // LEGACY DEFECT kept (see README APV-B-343): a recaudacion that did not reconcile is answered as REAPERTURAR_TRASPASO_OK plus the not-matching row
                    mensajes.add(Mensajes.de(0L, "OK", "REAPERTURAR_TRASPASO_OK", "Se crea reaperturacion correctamente."));
                    mensajes.addAll(outRecaudacionReapertura.mensajes());
                    idResultado.addAll(aResultados(outRecaudacionReapertura));
                } else {
                    mensajes.add(Mensajes.de(0L, "ERROR", "ERROR_REAPERTURAR_TRASPASO", "Problema al reaperturar el Traspaso."));
                    errores = errores + outRecaudacionReapertura.contadorErrores();
                    idResultado.addAll(aResultados(outRecaudacionReapertura));
                }
            }
        } else {
            mensajes.add(Mensajes.de(0L, "ERROR", "ERROR_MODIFICO_PORTAFOLIO_CONTRATO", "Problema al reaperturar el Traspaso."));
            errores++;
        }
        // the counter of the answer is the legacy call(): getNumeroErrores(P_OUT_MENSAJES)
        return new ReaperturarTraspasosResultado(mensajes, Mensajes.numeroErrores(mensajes), idResultado);
    }

    private static List<Resultado> aResultados(IngresarRecaudacionTraspasoResultado r) {
        List<Resultado> lista = new ArrayList<>();
        for (cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.Resultado x : r.idResultado()) {
            lista.add(x == null ? null : new Resultado(x.codigoResultado(), x.nombreResultado(), x.idResultado()));
        }
        return lista;
    }
}
